package eu.essi_lab.model.index.jaxb;

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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.lib.xml.NameSpace;

/**
 * @author Fabrizio
 */
public class DisjointValues {

    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String disjSouth;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String disjWest;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String disjNorth;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String disjEast;

    @XmlTransient
    public String getDisjSouth() {
	return disjSouth;
    }

    public void setDisjSouth(String disjSouth) {
	this.disjSouth = disjSouth;
    }

    @XmlTransient
    public String getDisjWest() {
	return disjWest;
    }

    public void setDisjWest(String disjWest) {
	this.disjWest = disjWest;
    }

    @XmlTransient
    public String getDisjNorth() {
	return disjNorth;
    }

    public void setDisjNorth(String disjNorth) {
	this.disjNorth = disjNorth;
    }

    @XmlTransient
    public String getDisjEast() {
	return disjEast;
    }

    public void setDisjEast(String disjEast) {
	this.disjEast = disjEast;
    }

}
