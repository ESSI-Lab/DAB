package eu.essi_lab.profiler.esri.feature.query.parser;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.profiler.esri.feature.Field;

public class ESRICondition extends ESRIToken {

    public ESRICondition(String parse, int begin, int end) {
	super(parse, begin, end);
    }

    private String operator;

    private ESRIProperty property;

    private String literal;

    public String getOperator() {
	return operator;
    }

    public void setOperator(String operator) {
	this.operator = operator;
    }

    public String getLiteral() {
	return literal;
    }

    public void setLiteral(String literal) {
	this.literal = literal;
    }

    public ESRIProperty getProperty() {
	return property;
    }

    public void setProperty(ESRIProperty property) {
	this.property = property;
    }

    public Bond getBond() {

	boolean ignoreCase = false;

	if (getProperty() instanceof ESRIFunction) {
	    ESRIFunction function = (ESRIFunction) getProperty();
	    if (function.getOperator().toUpperCase().equals("UPPER") || function.getOperator().toUpperCase().equals("LOWER")) {
		ignoreCase = true;
	    }
	}
	Field field = property.getField();
	MetadataElement element = field.getMetadataElement();
	BondOperator operator = BondOperator.EQUAL;
	if (ignoreCase) {
	    operator = BondOperator.TEXT_SEARCH;
	}
	SimpleValueBond bond = BondFactory.createSimpleValueBond(operator, element, getLiteral());

	return bond;

    }

}
