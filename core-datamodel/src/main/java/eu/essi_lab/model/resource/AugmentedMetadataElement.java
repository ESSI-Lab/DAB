package eu.essi_lab.model.resource;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.UUID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.jaxb.common.NameSpace;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
public class AugmentedMetadataElement {

    @XmlAttribute(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String updateTimeStamp;

    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String name;

    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String oldValue;

    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String newValue;

    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String id;

    public AugmentedMetadataElement() {
	setIdentifier(UUID.randomUUID().toString());
    }

    @XmlTransient
    public String getIdentifier() {
	return id;
    }

    public void setIdentifier(String augmenterIdentifier) {
	this.id = augmenterIdentifier;
    }

    @XmlTransient
    public String getUpdateTimeStamp() {

	return updateTimeStamp;
    }

    public void setUpdateTimeStamp(String updateTimeStamp) {

	this.updateTimeStamp = updateTimeStamp;
    }

    public void setUpdateTimeStamp() {

	this.updateTimeStamp = ISO8601DateTimeUtils.getISO8601DateTime();
    }

    @XmlTransient
    public String getName() {

	return name;
    }

    public void setName(String name) {

	this.name = name;
    }

    public void setName(MetadataElement element) {

	this.name = element.getName();
    }

    @XmlTransient
    public String getOldValue() {

	return oldValue;
    }

    public void setOldValue(String oldValue) {

	this.oldValue = oldValue;
    }

    @XmlTransient
    public String getNewValue() {

	return newValue;
    }

    public void setNewValue(String newValue) {

	this.newValue = newValue;
    }

    public String toString() {

	return getName() + ": " + getOldValue() + ", " + getNewValue();
    }

    @Override
    public int hashCode() {

	return (getName() + getOldValue() + getNewValue()).hashCode();
    }

    @Override
    public boolean equals(Object o) {

	boolean out = false;

	if (o instanceof AugmentedMetadataElement) {

	    AugmentedMetadataElement e = (AugmentedMetadataElement) o;
	    out = Objects.equals(e.getName(), getName()) && //
		    Objects.equals(e.getOldValue(), getOldValue()) && //
		    Objects.equals(e.getNewValue(), getNewValue());

	}

	return out;
    }
}
