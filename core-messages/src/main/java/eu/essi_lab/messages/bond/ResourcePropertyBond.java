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

import javax.xml.bind.annotation.XmlRootElement;

import eu.essi_lab.model.resource.ResourceProperty;

/**
 * A bond applied to a {@link ResourceProperty}
 * 
 * @author Fabrizio
 */
@XmlRootElement
public class ResourcePropertyBond extends QueryableBond<String> {

    /**
     * No-arg constructor only to be used by JAXB
     */
    public ResourcePropertyBond() {

    }

    /**
     * @param operator
     * @param property
     */
    ResourcePropertyBond(BondOperator operator, ResourceProperty property) {
	this(operator, property, null);
    }

    /**
     * @param operator
     * @param property
     * @param value
     */
    ResourcePropertyBond(BondOperator operator, ResourceProperty property, String value) {
	setOperator(operator);
	setPropertyValue(value);
	setProperty(property);
    }

    public ResourceProperty getProperty() {

	return (ResourceProperty) super.getProperty();
    }

    @Override
    public String toString() {
	return getProperty() + " " + getOperator().getShortRepresentation() + " " + getPropertyValue();
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof ResourcePropertyBond) {
	    ResourcePropertyBond bond = (ResourcePropertyBond) obj;
	    return Objects.equals(bond.getOperator(), getOperator()) && //
		    Objects.equals(bond.getProperty(), getProperty()) && //
		    Objects.equals(bond.getPropertyValue(), getPropertyValue());

	}
	return super.equals(obj);
    }

    @Override
    public Bond clone() {

	return new ResourcePropertyBond(getOperator(), getProperty(), getPropertyValue());
    }
}
