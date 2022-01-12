package eu.essi_lab.model;

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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonInclude;

import eu.essi_lab.jaxb.common.NameSpace;
import eu.essi_lab.model.configuration.GSJSONSerializable;
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class Source extends GSJSONSerializable {
    /**
     * 
     */
    private static final long serialVersionUID = -3996229170199641878L;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String label;
    @XmlElement(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String endpoint;
    @XmlAttribute(namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String uniqueIdentifier;

    @XmlTransient
    public String getUniqueIdentifier() {
	return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
	this.uniqueIdentifier = uniqueIdentifier;
    }

    @XmlTransient
    public String getEndpoint() {
	return endpoint;
    }

    public void setEndpoint(String endpoint) {
	this.endpoint = endpoint;
    }

    @XmlTransient
    public String getLabel() {
	return label;
    }

    public void setLabel(String label) {
	this.label = label;
    }

}
