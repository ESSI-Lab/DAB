package eu.essi_lab.accessor.mgnify;

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

import org.json.JSONObject;

public class Study extends MGnifyObject {

    private JSONObject attributes;
    private JSONObject relationships;

    public Study(JSONObject da) {
	super(da);
	this.attributes = da.getJSONObject("attributes");
	this.relationships = da.getJSONObject("relationships");

    }

    public String getId() {
	return json.getString("id");
    }

    public String getName() {
	return attributes.getString("study-name");
    }

    public String getAbstract() {
	return attributes.getString("study-abstract");
    }

    public String getSamplesLink() {
	return relationships.getJSONObject("samples").getJSONObject("links").getString("related");
    }
    
    public String getAnalysisLink() {
	return relationships.getJSONObject("analyses").getJSONObject("links").getString("related");
    }
    
    public String getGeocoordinatesLink() {
	return relationships.getJSONObject("geocoordinates").getJSONObject("links").getString("related");
    }

    public String getCentreName() {
	return attributes.optString("centre-name");
    }
    
    public String getLastUpdate() {
	return attributes.getString("last-update");
    }
    
    public String getAccession() {
	return attributes.getString("accession");
    }
    
    public String getSecondaryAccession() {
	return attributes.getString("secondary-accession");
    }



}
