package eu.essi_lab.messages.bond;

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

import javax.xml.bind.annotation.XmlRootElement;

import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.Queryable.ContentType;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * A {@link MetadataElementBond} applied on element with a simple value (for example a text or a number)
 * 
 * @see SpatialBond
 * @author Fabrizio
 */
@SuppressWarnings("serial")
@XmlRootElement
public class SimpleValueBond extends MetadataElementBond<String> {

    /**
     * No-arg constructor needed by JAXB
     */
    public SimpleValueBond() {

    }

    /**
     * Creates a simple value bond without value, so the supplied {@link BondOperator} must be one of the following
     * unary operators:
     * <ul>
     * <li>{@link BondOperator#MAX}</li>
     * <li>{@link BondOperator#MIN}</li>
     * <li>{@link BondOperator#NOT_EXISTS}</li>
     * <li>{@link BondOperator#EXISTS}</li>
     * </ul>
     * 
     * @param operator
     * @param element
     */
    SimpleValueBond(BondOperator operator, MetadataElement element) {
	this(operator, element, null);
    }

    /**
     * Creates a simple value bond applied on the given string <code>value</code>.
     * The supplied <code>element</code> must have one of the following {@link ContentType}:
     * <ul>
     * <li>{@link ContentType#TEXTUAL}</li>
     * <li>{@link ContentType#ISO8601_DATE}</li>
     * <li>{@link ContentType#ISO8601_DATE_TIME}</li>
     * </ul>
     * The supplied <code>operator</code> must be one of the following binary operators:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#NOT_EQUAL}</li>
     * <li>{@link BondOperator#LIKE}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * </ul>
     * 
     * @see Queryable#getContentType()
     * @param operator
     * @param element
     * @param value
     */
    SimpleValueBond(BondOperator operator, MetadataElement element, String value) {
	super(operator, element, value);
    }

    /**
     * Creates a simple value bond applied on the given double <code>value</code>.
     * The supplied <code>element</code> must have the {@link ContentType#DOUBLE}.
     * The supplied <code>operator</code> must be one of the following binary operators:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#NOT_EQUAL}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * </ul>
     * 
     * @see Queryable#getContentType()
     * @param operator
     * @param element
     * @param value
     */
    SimpleValueBond(BondOperator operator, MetadataElement element, double value) {
	super(operator, element, String.valueOf(value));
    }

    /**
     * Creates a simple value bond applied on the given int <code>value</code>.
     * The supplied <code>element</code> must have the {@link ContentType#INTEGER}.
     * The supplied <code>operator</code> must be one of the following binary operators:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#NOT_EQUAL}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * </ul>
     * 
     * @see Queryable#getContentType()
     * @param operator
     * @param element
     * @param value
     */
    SimpleValueBond(BondOperator operator, MetadataElement element, int value) {
	super(operator, element, String.valueOf(value));
    }

    /**
     * Creates a simple value bond applied on the given boolean <code>value</code> with the {@link BondOperator#EQUAL}
     * operator
     * The supplied <code>element</code> must have the {@link ContentType#BOOLEAN}.
     * 
     * @see Queryable#getContentType()
     * @param element
     * @param value
     */
    SimpleValueBond(MetadataElement element, boolean value) {
	super(BondOperator.EQUAL, element, String.valueOf(value));
    }

    @Override
    protected MetadataElementBond<String> createClone(BondOperator type, MetadataElement element, String value) {

	MetadataElement cloneProperty = getProperty();
	SimpleValueBond clone = new SimpleValueBond(getOperator(), cloneProperty, getPropertyValue());
	return clone;
    }
}
