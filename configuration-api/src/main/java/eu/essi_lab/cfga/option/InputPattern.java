package eu.essi_lab.cfga.option;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Fabrizio
 */
public class InputPattern {

    /**
     * 
     */
    public static final InputPattern ALPHANUMERIC = new InputPattern("alphaNum", "^[a-zA-Z0-9]+$", "[a-zA-Z0-9]*$");
    /**
     * 
     */
    public static final InputPattern ALPHANUMERIC_AND_UNDERSCORE = new InputPattern("alphaNumUnderscore", "^\\w+$", "^\\w*$");

    /**
     * This pattern if fine but an eventual trailing space should be removed
     */
    public static final InputPattern ALPHANUMERIC_AND_SPACE = new InputPattern(//
	    "alphaNumSpace", //
	    "^([a-zA-Z0-9]+|[a-zA-Z0-9]+ {1})+$", //
	    "^([a-zA-Z0-9]+|[a-zA-Z0-9]+ {1})*$");
    /**
     * This pattern if fine but an eventual trailing space should be removed
     */
    public static final InputPattern ALPHANUMERIC_AND_UNDERSCORE_AND_SPACE = new InputPattern(//
	    "alphaNumUnderscoreSpace", //
	    "^(\\w+|\\w+ {1})+$", //
	    "^(\\w+|\\w+ {1})*$");
    
    public static final InputPattern ALPHANUMERIC_AND_UNDERSCORE_AND_MINUS = new InputPattern(//
	    "alphaNumUnderscoreMinus", //
	    "^(\\w+|\\w+-{1})+$", //
	    "^(\\w+|\\w+-{1})*$");

    private final String pattern;
    private final String requiredPattern;
    private final String name;

    /**
     * @param name
     * @param requiredPattern
     * @param pattern
     */
    public InputPattern(String name, String requiredPattern, String pattern) {

	this.name = name;
	this.requiredPattern = requiredPattern;
	this.pattern = pattern;
    }

    /**
     * @return
     */
    public String getPattern() {

	return pattern;
    }

    /**
     * @return
     */
    public String getRequiredPattern() {

	return requiredPattern;
    }

    /**
     * @return the name
     */
    public String getName() {

	return name;
    }

    /**
     * @param name
     */
    public static InputPattern fromName(String name) {

	return fromName(Arrays.asList(InputPattern.class.getDeclaredFields()), name);
    }

    /**
     * @param name
     */
    protected static InputPattern fromName(List<Field> fields, String name) {

	return fields.//
		stream().//
		filter(f -> Modifier.isStatic(f.getModifiers())).//
		map(InputPattern::fromField).//
		filter(Objects::nonNull).//
		filter(i -> i.getName() != null && i.getName().equals(name)).//
		findFirst().//
		get();

    }

    /**
     * @param field
     * @return
     */
    protected static InputPattern fromField(Field field) {

	try {
	    return (InputPattern) field.get(null);
	} catch (Exception e) {
	}
	return null;
    }

    public static void main(String[] args) {

	Pattern pattern = Pattern.compile("^([a-zA-Z0-9]+|[a-zA-Z0-9]+ {1})+$");
	Matcher matcher = pattern.matcher("");

	boolean matches = matcher.matches();

	System.out.println(matches);
    }
}
