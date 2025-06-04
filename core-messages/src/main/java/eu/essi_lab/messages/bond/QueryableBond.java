package eu.essi_lab.messages.bond;

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

import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.index.IndexedElement;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * A bond applied to a {@link Queryable} property with a supplied operator. This class provides common
 * fields and methods for the subclasses and is used to define custom bonds with the method
 * {@link BondFactory#createCustomBond(Queryable, BondOperator, String)}
 * 
 * @see Queryable
 * @see MetadataElementBond
 * @see ResourcePropertyBond
 * @author Fabrizio
 * @param <T> the type of the queryable property which reflects the content of the related {@link IndexedElement}. For
 *        example, {@link IndexedElement}s having a simple textual content, can have the {@link String} type
 */
@SuppressWarnings("serial")
public class QueryableBond<T> implements Bond {

    @XmlElements({ @XmlElement(name = "metadataElement", type = MetadataElement.class),
	    @XmlElement(name = "resourceProperty", type = ResourceProperty.class) })
    private Queryable property;

    private BondOperator operator;

    private T propertyValue;

    QueryableBond() {
    }

    @XmlTransient
    public Queryable getProperty() {

	return property;
    }

    public void setProperty(Queryable property) {

	this.property = property;
    }

    public void setPropertyValue(T value) {

	this.propertyValue = value;
    }

    public T getPropertyValue() {

	return propertyValue;
    }

    public BondOperator getOperator() {

	return operator;
    }

    public void setOperator(BondOperator operator) {

	this.operator = operator;
    }

    public Bond clone() {

	QueryableBond<T> bond = new QueryableBond<T>();
	bond.setOperator(operator);
	bond.setProperty(property);
	bond.setPropertyValue(propertyValue);

	return bond;
    }

    public boolean equals(Object o) {

	if (o instanceof QueryableBond) {
	    QueryableBond<?> bond = (QueryableBond<?>) o;
	    return Objects.equals(bond.getOperator(), getOperator()) && //
		    Objects.equals(bond.getProperty(), getProperty()) && //
		    Objects.equals(bond.getPropertyValue(), getPropertyValue());
	}
	return false;
    }

    @Override
    public int hashCode() {

	int opCode = operator != null ? operator.hashCode() : 0;
	int valCode = getPropertyValue() != null ? getPropertyValue().hashCode() : 0;
	int propCode = getProperty() != null ? getProperty().hashCode() : 0;

	return opCode + valCode + propCode;
    }

    public String toString() {

	return getProperty() + " " + getOperator() + " " + getPropertyValue();
    }

}
