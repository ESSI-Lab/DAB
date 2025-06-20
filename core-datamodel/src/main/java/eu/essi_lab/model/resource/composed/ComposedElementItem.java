/**
 * 
 */
package eu.essi_lab.model.resource.composed;

import java.util.Date;
import java.util.Optional;

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

    static final String NO_VALUE = "novalue";

    /**
     * 
     */
    public ComposedElementItem() {

    }

    /**
     * @param name
     * @param type
     */
    public ComposedElementItem(String name, ContentType type) {

	this.name = name;
	this.type = type;

	this.value = switch (type) {
	case BOOLEAN -> {
	    yield false;
	}
	case DOUBLE -> {
	    yield 0.0;
	}
	case INTEGER -> {
	    yield 0;
	}
	case TEXTUAL -> {
	    yield NO_VALUE;
	}
	case ISO8601_DATE -> {
	    yield ISO8601DateTimeUtils.getISO8601Date(new Date(ISO8601DateTimeUtils.EPOCH));
	}
	case ISO8601_DATE_TIME -> {
	    yield ISO8601DateTimeUtils.getISO8601DateTime(new Date(ISO8601DateTimeUtils.EPOCH));
	}
	case LONG -> {
	    yield Long.valueOf(0);
	}

	case COMPOSED -> throw new UnsupportedOperationException("Unimplemented case: " + type);
	case SPATIAL -> throw new UnsupportedOperationException("Unimplemented case: " + type);
	};
    }

    /**
     * @param name
     */
    public ComposedElementItem(String name) {

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
    public void setValue(String value) {

	this.value = value;
    }

    /**
     * @param value
     */
    public void setValue(Integer value) {

	this.value = value;
    }

    /**
     * @param value
     */
    public void setValue(Boolean value) {

	this.value = value;
    }

    /**
     * @param value
     */
    public void setValue(Long value) {

	this.value = value;
    }

    /**
     * @param value
     */
    public void setValue(Double value) {

	this.value = value;
    }
}
