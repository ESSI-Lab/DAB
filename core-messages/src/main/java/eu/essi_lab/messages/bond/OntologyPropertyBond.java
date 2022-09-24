package eu.essi_lab.messages.bond;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.model.ontology.OntologyObjectProperty;

/**
 * @author Fabrizio
 */
public class OntologyPropertyBond extends QueryableBond<String> {

    /**
     * @param operator
     * @param property
     */
    OntologyPropertyBond(BondOperator operator, OntologyObjectProperty property) {
	this(operator, property, null);
    }

    /**
     * @param operator
     * @param property
     * @param value
     */
    OntologyPropertyBond(BondOperator operator, OntologyObjectProperty property, String value) {
	setOperator(operator);
	setPropertyValue(value);
	setProperty(property);
    }

    public OntologyObjectProperty getProperty() {

	return (OntologyObjectProperty) super.getProperty();
    }

    @Override
    public String toString() {
	return getProperty() + " " + getOperator().getShortRepresentation() + " " + getPropertyValue();
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof OntologyPropertyBond) {
	    OntologyPropertyBond bond = (OntologyPropertyBond) obj;
	    return bond.getOperator().equals(getOperator()) && //
		    ((bond.getProperty() == null && getProperty() == null) || //
			    bond.getProperty().equals(getProperty()))
		    && //

		    bond.getPropertyValue().equals(getPropertyValue());

	}
	return super.equals(obj);
    }

    @Override
    public Bond clone() {

	return new OntologyPropertyBond(getOperator(), getProperty(), getPropertyValue());
    }
}
