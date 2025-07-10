/**
 * 
 */
package eu.essi_lab.model.resource.composed;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.NameSpace;

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

import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class ComposedElementItem {

    @XmlElement(name = "name", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private String name;
    @XmlElement(name = "type", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private ContentType type;
    @XmlElement(name = "value", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
    private Object value;

    public static final String DEFAULT_STRING_VALUE = "novalue";
    public static final double DEFAULT_DOUBLE_VALUE = 0.0;
    public static final boolean DEFAULT_BOOLEAN_VALUE = false;
    public static final int DEFAULT_INT_VALUE = 0;
    public static final long DEFAULT_LONG_VALUE = 0l;
    public static final String DEFAULT_ISO8601_DATE_VALUE = ISO8601DateTimeUtils.getISO8601Date(new Date(ISO8601DateTimeUtils.EPOCH));
    public static final String DEFAULT_SO8601_DATE_TIME_VALUE = ISO8601DateTimeUtils
	    .getISO8601DateTime(new Date(ISO8601DateTimeUtils.EPOCH));

    /**
     * 
     */
    public ComposedElementItem() {

    }

    /**
     * @param name
     * @param type
     */
    ComposedElementItem(String name, ContentType type) {

	this.name = name;
	this.type = type;

	this.value = switch (type) {
	case BOOLEAN -> DEFAULT_BOOLEAN_VALUE;
	case DOUBLE -> DEFAULT_DOUBLE_VALUE;
	case INTEGER -> DEFAULT_INT_VALUE;
	case TEXTUAL -> DEFAULT_STRING_VALUE;
	case ISO8601_DATE -> DEFAULT_ISO8601_DATE_VALUE;
	case ISO8601_DATE_TIME -> DEFAULT_SO8601_DATE_TIME_VALUE;
	case LONG -> Long.valueOf(0);
	case COMPOSED, SPATIAL -> throw new UnsupportedOperationException("Unimplemented case: " + type);
	};
    }

    /**
     * @param name
     */
    ComposedElementItem(String name) {

	this.name = name;
    }

    /**
     * @return the name
     */
    @XmlTransient
    public String getName() {

	return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {

	this.name = name;
    }

    /**
     * @return the type
     */
    @XmlTransient
    public ContentType getType() {

	return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(ContentType type) {

	this.type = type;
    }

    /**
     * @return
     */
    public String getStringValue() {

	return value != null ? value.toString() : null;
    }

    /**
     * @return
     */
    @XmlTransient
    public Object getValue() {

	return switch (type) {
	case BOOLEAN -> {
	    yield Boolean.valueOf(getStringValue());
	}
	case DOUBLE -> {
	    yield Double.valueOf(getStringValue());
	}
	case INTEGER -> {
	    yield Integer.valueOf(getStringValue());
	}
	case TEXTUAL, ISO8601_DATE, ISO8601_DATE_TIME -> {
	    yield getStringValue();
	}
	case LONG -> {
	    yield Long.valueOf(getStringValue());
	}

	case COMPOSED -> throw new UnsupportedOperationException("Unimplemented case: " + type);
	case SPATIAL -> throw new UnsupportedOperationException("Unimplemented case: " + type);
	};
    }

    /**
     * @param value
     */
    public void setValue(Object value) {

	switch (getType()) {
	case INTEGER -> this.value = Integer.valueOf(value.toString());
	case DOUBLE -> this.value = Double.valueOf(value.toString());
	case LONG -> this.value = Long.valueOf(value.toString());
	case BOOLEAN -> this.value = Boolean.valueOf(value.toString());
	case TEXTUAL, ISO8601_DATE, ISO8601_DATE_TIME -> this.value = value.toString();
	case SPATIAL -> throw new UnsupportedOperationException("Unimplemented case: " + getType());
	case COMPOSED -> throw new UnsupportedOperationException("Unimplemented case: " + getType());
	}
    }
}
