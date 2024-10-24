package eu.essi_lab.model.resource.data;

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

import java.io.Serializable;

public class Authority implements Serializable {

    public static Authority EPSG = new Authority("EPSG");
    public static Authority OGC = new Authority("OGC");

    private String identifier;

    public String getIdentifier() {
	return identifier;
    }

    public void setIdentifier(String identifier) {
	this.identifier = identifier;
    }

    protected Authority(String identifier) {
	this.identifier = identifier;
    }

    /**
     * @param identifier
     * @return
     */
    public static Authority fromIdentifier(String identifier) {
	if (identifier.toLowerCase().contains("epsg") || //
		identifier.toLowerCase().contains("european petroleum survey group")) {
	    return Authority.EPSG;
	}
	if (identifier.toLowerCase().contains("ogc") || //
		identifier.toLowerCase().contains("opengis") || //
		identifier.toLowerCase().contains("open geospatial consortium")) {
	    return Authority.OGC;
	}
	return new Authority(identifier);
    }

    @Override
    public String toString() {
	return identifier;
    }

}
