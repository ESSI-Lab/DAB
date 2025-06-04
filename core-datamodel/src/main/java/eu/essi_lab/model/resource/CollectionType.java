package eu.essi_lab.model.resource;

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

import java.util.Arrays;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Fabrizio
 */
public enum CollectionType {

    @XmlEnumValue("Station")
    STATION("Station");

    private String type;

    /**
     * @return
     */
    public String getType() {

	return type;
    }

    /**
     * @param type
     * @return
     */
    public static CollectionType fromType(String type) {

	return Arrays.asList(values()).//
		stream().//
		filter(rt -> rt.getType().equals(type)).//
		findFirst().//
		orElseThrow(() -> new IllegalArgumentException("Invalid name: name"));
    }

    @Override
    public String toString() {

	return this.type;
    }

    /**
     * @param type
     */
    private CollectionType(String type) {

	this.type = type;
    }
}
