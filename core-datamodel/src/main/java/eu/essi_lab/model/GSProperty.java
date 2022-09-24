package eu.essi_lab.model;

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

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlRootElement;

import eu.essi_lab.lib.xml.NameSpace;

/**
 * @author Fabrizio
 * @param <T>
 */
@XmlRootElement(name = "property", namespace = NameSpace.GS_DATA_MODEL_SCHEMA_URI)
public class GSProperty<T> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2529687944996847887L;
    private T value;
    private String name;

    /**
     * 
     */
    public GSProperty() {
    }

    /**
     * @param name
     * @param value
     */
    public GSProperty(String name, T value) {
	this.value = value;
	this.name = name;
    }

    /**
     * @return
     */
    public String getName() {

	return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {

	this.name = name;
    }

    /**
     * @return
     */
    public T getValue() {

	return value;
    }

    /**
     * @param value
     */
    public void setValue(T value) {

	this.value = value;
    }

    @Override
    public String toString() {

	String value = "(null)";
	if (getValue() != null) {
	    value = getValue().toString();
	}
	return getName() + ":" + value;
    }

    @Override
    public int hashCode() {
	return Objects.hash(getName(), getValue());
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	@SuppressWarnings("rawtypes")
	GSProperty other = (GSProperty) obj;
	if (name == null) {
	    if (other.name != null)
		return false;
	} else if (!name.equals(other.name))
	    return false;
	if (value == null) {
	    if (other.value != null)
		return false;
	} else if (!value.equals(other.value))
	    return false;
	return true;
    }
}
