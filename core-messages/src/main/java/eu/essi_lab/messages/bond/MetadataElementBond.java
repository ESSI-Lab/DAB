package eu.essi_lab.messages.bond;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * A bond applied to a {@link MetadataElement}
 * 
 * @author Fabrizio
 * @see SimpleValueBond
 * @see SpatialBond
 * @see MetadataElement#isVolatile()
 * @param <T> the bond value type. The bond value is used by the {@link DiscoveryBondParser} to build the query for the
 *        related
 *        data base. In most cases the String or Boolean type is enough, but in some cases a complex type can be
 *        required (see {@link SpatialBond} )
 */
public abstract class MetadataElementBond<T> extends QueryableBond<T> {

    /**
     * No-arg constructor needed by JAXB
     */
    public MetadataElementBond() {

    }

    /**
     * Creates a metadata element bond without value, so the given {@link BondOperator} must be a unary operator.
     * Accepted operators are:
     * <ul>
     * <li>{@link BondOperator#MAX}</li>
     * <li>{@link BondOperator#MIN}</li>
     * <li>{@link BondOperator#NULL}</li>
     * <li>{@link BondOperator#NOT_NULL}</li>
     * <li></li>
     * <li></li>
     * </ul>
     * 
     * @param operator
     * @param element
     */
    MetadataElementBond(BondOperator operator, MetadataElement element) {
	this(operator, element, null);
    }

    /**
     * Creates a metadata element bond applied on the given <code>value</code> so the given {@link BondOperator} must be
     * a binary operator. Accepted operators are:
     * <ul>
     * <li>{@link BondOperator#EQUAL}</li>
     * <li>{@link BondOperator#LIKE}</li>
     * <li>{@link BondOperator#LESS}</li>
     * <li>{@link BondOperator#LESS_OR_EQUAL}</li>
     * <li>{@link BondOperator#GREATER}</li>
     * <li>{@link BondOperator#GREATER_OR_EQUAL}</li>
     * </ul>
     * 
     * @param operator
     * @param element
     * @param value
     */
    MetadataElementBond(BondOperator operator, MetadataElement element, T value) {
	setOperator(operator);
	setPropertyValue(value);
	setProperty(element);
    }

    public MetadataElement getProperty() {
	return (MetadataElement) super.getProperty();
    }

    @Override
    public String toString() {
	return getProperty() + " " + getOperator().getShortRepresentation() + " " + getPropertyValue();
    }

    @Override
    public MetadataElementBond<T> clone() {
	MetadataElement cloneProperty = getProperty();
	MetadataElementBond<T> clone = createClone(getOperator(), cloneProperty, getPropertyValue());
	return clone;
    }

    /**
     * @param type
     * @param element
     * @param value
     * @return
     */
    protected abstract MetadataElementBond<T> createClone(BondOperator type, MetadataElement element, T value);

}
