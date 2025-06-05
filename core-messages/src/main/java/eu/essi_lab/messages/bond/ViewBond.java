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

/**
 * Implementation of {@link Bond} connected with a view, through its identifier
 * 
 * @author Boldrini
 */
@XmlRootElement
public class ViewBond implements Bond {

    private String viewIdentifier;

    /**
     * Default constructor needed by JAXB
     */
    public ViewBond() {

    }

    ViewBond(String viewIdentifier) {
	super();
	this.viewIdentifier = viewIdentifier;
    }

    public String getViewIdentifier() {
	return viewIdentifier;
    }

    public void setViewIdentifier(String viewIdentifier) {
	this.viewIdentifier = viewIdentifier;
    }

    @Override
    public String toString() {
	return "View bond: " + viewIdentifier;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof ViewBond) {
	    ViewBond vb = (ViewBond) obj;
	    return Objects.equals(vb.getViewIdentifier(), getViewIdentifier());
	}
	return super.equals(obj);
    }

    @Override
    public ViewBond clone() {
	return new ViewBond(viewIdentifier);
    }

    @Override
    public int hashCode() {
	return toString().hashCode();
    }
}
