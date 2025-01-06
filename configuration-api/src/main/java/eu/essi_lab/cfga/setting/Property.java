package eu.essi_lab.cfga.setting;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.Optional;

/**
 * 
 */
public class Property<T> {

    /**
     * 
     */
    private String key;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private boolean optional;

    /**
     * 
     */
    private Optional<T> defaultValue;

    /**
     * 
     */
    private Property() {

    }

    /**
     * @param name
     * @param key
     * @param optional
     * @param optionalKey
     * @param defaultValue
     */
    private Property(String name, String key, boolean optional, Optional<T> defaultValue) {

	this.name = name;
	this.key = key;
	this.optional = optional;
	this.defaultValue = defaultValue;
    }

    /**
     * @param <T>
     * @param name
     * @param key
     * @param optional
     * @param optionalKey
     * @param defaultValue
     * @return
     */
    protected static <T> Property<T> of(String name, String key, boolean optional, Optional<T> defaultValue) {

	return new Property<T>(name, key, optional, defaultValue);
    }

    /**
     * @return
     */
    public String getKey() {

	return key;
    }

    /**
     * @return
     */
    public String getName() {

	return name;
    }

    /**
     * @return
     */
    public boolean isOptional() {

	return optional;
    }

    /**
     * @return
     */
    public Optional<T> getDefaultValue() {

	return defaultValue;
    }

    /**
     * @return
     */
    public boolean isOptionalKey() {

	return getDefaultValue().isPresent();
    }

    @Override
    public String toString() {

	return "(" + name + ", " + key + ", " + optional + ", " + isOptionalKey() + ", " + defaultValue + ")";
    }
}
