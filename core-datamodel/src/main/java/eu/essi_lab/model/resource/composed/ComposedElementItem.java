/**
 * 
 */
package eu.essi_lab.model.resource.composed;

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

    private String name;
    private ContentType type;
    private Object value;

    /**
     * 
     */
    public ComposedElementItem() {

    }

    /**
     * @param name
     * @param type
     * @param value
     */
    public ComposedElementItem(String name, ContentType type) {

	this.name = name;
	this.type = type;
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
    public String getValue() {

	return value.toString();
    }

    /**
     * @return
     */
    public Object getObjectValue() {

	return switch (type) {
	case BOOLEAN -> {
	    yield Boolean.valueOf(getValue());
	}
	case DOUBLE -> {
	    yield Double.valueOf(getValue());
	}
	case INTEGER -> {
	    yield Integer.valueOf(getValue());
	}
	case TEXTUAL, ISO8601_DATE, ISO8601_DATE_TIME -> {
	    yield getValue();
	}
	case LONG -> {
	    yield Long.valueOf(getValue());
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
