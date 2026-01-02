package eu.essi_lab.lib.sensorthings._1_1.client.request;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

/**
 * @see https://docs.ogc.org/is/18-088/18-088.html#usage-address-property-of-entity
 * @see https://docs.ogc.org/is/18-088/18-088.html#usage-address-value-of-property
 * @author Fabrizio
 */
public class EntityProperty implements AddressingPathSegment {

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private boolean getValue;

    /**
     * @param name
     * @param getValue
     */
    EntityProperty() {
    }

    /**
     * @param name the name to set
     */
    public EntityProperty setName(String name) {

	this.name = name;
	return this;
    }

    /**
     * @param getValue
     */
    public EntityProperty setGetValue(boolean getValue) {

	this.getValue = getValue;
	return this;
    }

    @Override
    public String compose() {

	return getValue ? name + "/$value" : name;
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
    public boolean isGetValueSet() {

	return getValue;
    }

    /**
     * @return
     */
    @Override
    public String toString() {

	return compose();
    }
}
