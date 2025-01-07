package eu.essi_lab.profiler.esri.feature.query.parser;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.profiler.esri.feature.Field;

public class ESRIParser {

    /**
     * Parses the ESRI where clause, e.g.
     * //(UPPER(country) = 'NORWAY')
     * //1=1
     * //(((UPPER(country) = 'NORWAY') AND (wmo_region = 6)))
     * // TODO add <, >
     * 
     * @param parse
     * @param fields
     * @return
     */
    public Bond parse(String parse, List<Field> fields) {
	if (parse == null || parse.isEmpty()) {
	    return null;
	}
	// to catch simple cases such as 1=1 (1=1) ((1=1))
	String simple = parse;
	if (simple.contains("(")) {
	    while (simple.startsWith("(") && simple.endsWith(")")) {
		simple = simple.substring(1, simple.length() - 1);
	    }
	}
	if (simple.contains("=")) {
	    String[] split = simple.split("=");
	    if ((split.length == 2) && split[0].equals(split[1])) {
		return null;
	    }
	}

	Bond ret = null;
	// first spaces are removed
	parse = removeSpaces(parse);
	// columns are identified
	List<ESRIColumn> parsedColumns = new ArrayList<>();
	for (Field field : fields) {
	    String column = field.getName();
	    String tmp = parse;
	    while (tmp.contains(column)) {
		int i = tmp.indexOf(column);
		ESRIColumn parsedColumn = new ESRIColumn(parse, i, i + column.length());
		parsedColumn.setField(field);
		parsedColumns.add(parsedColumn);
		tmp = tmp.substring(i + column.length());
	    }
	}
	parsedColumns.sort(new Comparator<ESRIColumn>() {

	    @Override
	    public int compare(ESRIColumn o1, ESRIColumn o2) {
		Integer b1 =  o1.getBegin();
		Integer b2 =  o2.getBegin();
		return b1.compareTo(b2);
	    }
	});
	// let's identify functions
	List<ESRIProperty> parsedProperties = new ArrayList<>();
	for (ESRIColumn parsedColumn : parsedColumns) {
	    int begin = parsedColumn.getBegin();
	    int end = parsedColumn.getEnd();
	    String before = readFunction(parse, begin, end);
	    if (before != null) {
		ESRIFunction function = new ESRIFunction(parse, begin - (before.length() + 1), end + 1);
		function.setOperator(before);
		function.setField(parsedColumn.getField());
		parsedProperties.add(function);
	    } else {
		parsedProperties.add(parsedColumn);
	    }
	}
	// let's identify conditions
	List<ESRICondition> parsedConditions = new ArrayList<>();
	List<Bond> operands = new ArrayList<>();

	for (ESRIProperty parsedProperty : parsedProperties) {
	    int end = parsedProperty.getEnd();
	    String operator = readOperator(parse, end);
	    String literal = readLiteral(parse, end + operator.length());
	    int begin = parsedProperty.getBegin();
	    end = end + operator.length() + literal.length();
	    ESRICondition condition = new ESRICondition(parse, begin, end);
	    literal = literal.replace("''", "'");
	    if (literal.startsWith("'") && literal.endsWith("'")) {
		literal = literal.substring(1, literal.length() - 1);
	    }
	    condition.setLiteral(literal);
	    while (inParenthesis(parse, begin, end)) {
		begin = begin - 1;
		end = end + 1;
	    }
	    condition.setBegin(begin);
	    condition.setEnd(end);
	    condition.setProperty(parsedProperty);
	    parsedConditions.add(condition);
	    operands.add(condition.getBond());
	}
	if (operands.size() == 1) {
	    return operands.get(0);
	}
	String operator = null;
	for (ESRICondition parsedCondition : parsedConditions) {
	    int end = parsedCondition.getEnd();
	    operator = readAfterUntil(parse, end, "(");
	    switch (operator.toUpperCase()) {
	    case "AND":
		ret = BondFactory.createAndBond(operands);
		break;
	    case "OR":
		ret = BondFactory.createOrBond(operands);
		break;
	    default:
		GSLoggerFactory.getLogger(getClass()).error("[ESRI_NOT_PARSABLE_SQL] {}", parse);

		break;
	    }
	    return ret;
	}

	return null;

    }

    private String readAfterUntil(String parse, int i, String stopToken) {
	String ret = parse.substring(i, i + 1);
	while (true) {
	    i = i + 1;
	    if (i == parse.length() - 1) {
		break;
	    }
	    String afterChar = parse.substring(i, i + 1);
	    if (afterChar.equals(stopToken)) {
		break;
	    } else {
		ret = ret + afterChar;
	    }
	}
	return ret;
    }

    private boolean inParenthesis(String parse, int begin, int end) {
	if (begin == 0 || end == parse.length()) {
	    return false;
	}
	String left = parse.substring(begin - 1, begin);
	if (!left.equals("(")) {
	    return false;
	}
	String right = parse.substring(end, end + 1);
	if (!right.equals(")")) {
	    return false;
	}
	return true;
    }

    private String readLiteral(String parse, int i) {
	String ret = parse.substring(i, i + 1);
	Integer quotes = null;
	if (ret.equals("'")) {
	    // string
	    quotes = 1;
	} else {
	    // number
	}
	while (true) {
	    i = i + 1;
	    if (i == parse.length() - 1) {
		break;
	    }
	    String afterChar = parse.substring(i, i + 1);
	    String afterAfterChar = parse.length() > i + 1 ? parse.substring(i + 1, i + 2) : "";
	    if (quotes == null) {
		// number
		if (afterChar.equals(")")) {
		    break;
		} else {
		    ret = ret + afterChar;
		}
	    } else {
		// string
		ret = ret + afterChar;
		if (afterChar.equals("'")) {
		    if (afterAfterChar.equals("'")) { // rare case of escaped quote!
			quotes++;
		    } else {
			quotes--;
		    }
		    if (quotes.equals(0)) {
			break;
		    }
		}
	    }

	}
	return ret;
    }

    private String readOperator(String parse, int end) {
	String operator = parse.substring(end, end + 1);
	String possibleEnd = null;
	switch (operator) {
	case "<":
	    possibleEnd = parse.substring(end + 1, end + 2);
	    switch (possibleEnd) {
	    case "=":
	    case ">":
		operator = operator + possibleEnd;
		break;
	    default:
		break;
	    }
	    break;

	case ">":
	    possibleEnd = parse.substring(end + 1, end + 2);
	    switch (possibleEnd) {
	    case "=":
		operator = operator + possibleEnd;
		break;
	    default:
		break;
	    }
	    break;
	case "=":
	default:
	    break;
	}
	return operator;
    }

    private String readFunction(String parse, int begin, int end) {
	if (begin == 0) {
	    return null; // begin
	}
	String afterChar = parse.substring(end, end + 1);
	if (!afterChar.equals(")")) { // not a function
	    return null;
	}
	String beforeChar = parse.substring(begin - 1, begin);
	if (!beforeChar.equals("(")) { // should not happen
	    return null;
	}
	begin = begin - 1;
	String ret = "";
	while (true) {
	    begin = begin - 1;
	    if (begin == -1) {
		break;
	    }
	    beforeChar = parse.substring(begin, begin + 1);
	    if (beforeChar.equals("(")) {
		break;
	    } else {
		ret = beforeChar + ret;
	    }
	}
	return ret;
    }

    public String removeSpaces(String parse) {
	String ret = "";
	for (int i = 0; i < parse.length(); i++) {
	    int i1 = parse.indexOf("'");
	    if (i1 == -1) {
		ret += parse.replace(" ", "");
		break;
	    } else {
		String before = parse.substring(0, i1);
		String after = parse.substring(i1 + 1);
		int i2 = after.indexOf("'");
		if (i2 == -1) {
		    // should not happen
		    System.err.println("Unbalanced '");
		    i2 = after.length() - 1;
		}
		String inside = after.substring(0, i2);
		parse = after.substring(i2 + 1);
		ret += before.replace(" ", "") + "'" + inside + "'";
		i = -1;
	    }
	}
	return ret;
    }

}
