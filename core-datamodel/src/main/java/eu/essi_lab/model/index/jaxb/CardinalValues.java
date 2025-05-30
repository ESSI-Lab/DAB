package eu.essi_lab.model.index.jaxb;

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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.lib.xml.NameSpace;

/**
 * @author Fabrizio
 */
public class CardinalValues {

    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String south;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String west;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String east;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String north;

    @XmlTransient
    public String getSouth() {
	return south;
    }

    public void setSouth(String south) {
	this.south = south;
    }

    @XmlTransient
    public String getWest() {
	return west;
    }

    public void setWest(String west) {
	this.west = west;
    }

    @XmlTransient
    public String getEast() {
	return east;
    }

    public void setEast(String east) {
	this.east = east;
    }

    @XmlTransient
    public String getNorth() {
	return north;
    }

    public void setNorth(String north) {
	this.north = north;
    }

    public boolean equals(Object o) {

	if (o instanceof CardinalValues) {

	    CardinalValues other = (CardinalValues) o;
	    return ((this.east == null && other.east == null) || (this.east != null && other.east != null && this.east.equals(other.east)))
		    && ((this.west == null && other.west == null)
			    || (this.west != null && other.west != null && this.west.equals(other.west)))
		    && ((this.south == null && other.south == null)
			    || (this.south != null && other.south != null && this.south.equals(other.south)))
		    && ((this.north == null && other.north == null)
			    || (this.north != null && other.north != null && this.north.equals(other.north)));
	}

	return false;
    }
}
