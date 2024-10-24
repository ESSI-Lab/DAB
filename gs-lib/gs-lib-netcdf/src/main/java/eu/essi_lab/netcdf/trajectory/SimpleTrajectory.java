package eu.essi_lab.netcdf.trajectory;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

public class SimpleTrajectory {

    private String name;
    private String description;
    private String identifier;
    private String verticalDatum;
    
    public void setVerticalDatum(String verticalDatum) {
        this.verticalDatum = verticalDatum;
    }

    private List<SimpleEntry<String, String>> stationProperties = new ArrayList<SimpleEntry<String, String>>();

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public void setIdentifier(String identifier) {
	this.identifier = identifier;

    }

    public String getIdentifier() {
	return identifier;
    }

    public void addProperty(String name, String value) {
	getStationProperties().add(new SimpleEntry<String, String>(name, value));

    }

    public String getProperty(String name) {
	for (SimpleEntry<String, String> stationProperty : stationProperties) {
	    if (stationProperty.getKey().equals(name)) {
		return stationProperty.getValue();
	    }
	}
	return null;
    }

    public List<SimpleEntry<String, String>> getStationProperties() {
	return stationProperties;
    }

    public String  getVerticalDatum() {
	return verticalDatum;
    }

}
