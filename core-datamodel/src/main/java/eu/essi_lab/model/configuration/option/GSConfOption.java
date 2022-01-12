package eu.essi_lab.model.configuration.option;

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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import eu.essi_lab.model.configuration.GSJSONSerializable;
import eu.essi_lab.model.exceptions.GSException;

@JsonInclude(Include.NON_ABSENT)
@JsonTypeInfo(use = Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "concrete")
@JsonPropertyOrder({ "concrete", "key", "mandatory", "value", "allowedValues", "label", "type" })
@JsonSubTypes({ @JsonSubTypes.Type(value = GSConfOptionBoolean.class), @JsonSubTypes.Type(value = GSConfOptionGSSource.class),
	@JsonSubTypes.Type(value = GSConfOptionString.class), @JsonSubTypes.Type(value = GSConfOptionDate.class) })
@JsonIgnoreProperties(value = { "getAllowedValues" }, allowGetters = true)
public abstract class GSConfOption<T> extends GSJSONSerializable {

    @JsonTypeInfo(use = Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "valueConcrete")
    private T value;

    @JsonProperty("key")
    private String k;

    @JsonProperty("mandatory")
    private boolean m;

    private String label;
    private Class<T> type;

    @JsonProperty("allowedValues")
    private List<T> allowedValues = new ArrayList<>();

    protected GSConfOption(Class<T> cl) {
	this.type = cl;
    }

    public abstract void validate() throws GSException;

    public List<T> getAllowedValues() {
	return allowedValues;
    }

    public void setAllowedValues(List<T> values) {
	allowedValues = values;
    }

    public void setMandatory(boolean mandatory) {

	m = mandatory;
    }

    public boolean isMandatory() {

	return m;

    }

    public String getKey() {

	return k;

    }

    public void setKey(String key) {

	k = key;

    }

    public void setValue(T object) {
	value = object;
    }

    public T getValue() {

	return value;

    }

    public Class<T> getType() {
	return type;
    }

    public void setType(Class<T> type) {
	this.type = type;
    }

    public String getLabel() {
	return label;
    }

    public void setLabel(String label) {
	this.label = label;
    }

    @Override
    public String toString() {
	return value == null ? "null" : value.getClass().getSimpleName() + " " + value.toString();
    }

}
