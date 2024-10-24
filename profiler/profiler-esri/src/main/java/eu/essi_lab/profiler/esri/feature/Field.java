package eu.essi_lab.profiler.esri.feature;

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

import org.json.JSONObject;

import eu.essi_lab.model.resource.BNHSProperty;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author boldrini
 */
public class Field {

    String name = "";

    ESRIFieldType type;
    String alias = "";
    boolean editable = false;
    Integer length = null;
    String domain = null;

    private BNHSProperty bnhsProperty;
    private MetadataElement metadataElement;

    public MetadataElement getMetadataElement() {
	return metadataElement;
    }

    public void setBNHSProperty(BNHSProperty reference) {
	this.bnhsProperty = reference;
    }

    public BNHSProperty getBNHSProperty() {
	return bnhsProperty;
    }

    public Field(String name, ESRIFieldType type, String alias, Integer length, MetadataElement metadataElement) {
	super();
	this.name = name;
	this.type = type;
	this.alias = alias;
	this.length = length;
	this.metadataElement = metadataElement;
    }

    public boolean isObjectId() {
	return type.equals(ESRIFieldType.OID);
    }

    public boolean isLatitude() {
	return type.equals(ESRIFieldType.LATITUDE);
    }

    public boolean isLongitude() {
	return type.equals(ESRIFieldType.LONGITUDE);
    }

    public boolean isStartDate() {
	return type.equals(ESRIFieldType.START_DATE);
    }

    public boolean isEndDate() {
	return type.equals(ESRIFieldType.END_DATE);
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public ESRIFieldType getType() {
	return type;
    }

    public void setType(ESRIFieldType type) {
	this.type = type;
    }

    public String getAlias() {
	return alias;
    }

    public void setAlias(String alias) {
	this.alias = alias;
    }

    public boolean getEditable() {
	return !isObjectId();
    }
    
    public boolean getNullable() {
	return !isObjectId();
    }

    public void setEditable(boolean editable) {
	this.editable = editable;
    }

    public Integer getLength() {
	return length;
    }

    public void setLength(Integer length) {
	this.length = length;
    }

    public String getDomain() {
	return domain;
    }

    public void setDomain(String domain) {
	this.domain = domain;
    }

    public JSONObject getJSON() {
	JSONObject ret = new JSONObject();
	ret.put("name", getName());
	ret.put("type", getType().getId());
	ret.put("alias", getAlias());
	ret.put("nullable", getNullable());
	ret.put("editable", getEditable());
	if (getLength() != null) {
	    ret.put("length", getLength());
	}
	ret.put("domain", getDomain());
	ret.put("defaultValue", (Object)null);
	return ret;
    }

    @Override
    public String toString() {
	return getName() + " (" + getAlias() + ")";
    }

}
