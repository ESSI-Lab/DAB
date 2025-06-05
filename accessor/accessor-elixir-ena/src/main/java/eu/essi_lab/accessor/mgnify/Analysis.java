package eu.essi_lab.accessor.mgnify;

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

import org.json.JSONObject;

public class Analysis extends MGnifyObject {

    private JSONObject attributes;
    private JSONObject relationships;

    public Analysis(JSONObject da) {
	super(da);
	this.attributes = da.getJSONObject("attributes");

	this.relationships = da.getJSONObject("relationships");

    }

    public String getId() {
	return json.getString("id");
    }

    public String getCompleteTime() {	
	return attributes.optString("complete-time");
    }

    public String getInstrumentPlatform() {
	return attributes.optString("instrument-platform");
    }

    public String getInstrumentModel() {
	return attributes.optString("instrument-model");
    }

    public String getTaxonomyLSULink() {
	return relationships.getJSONObject("taxonomy-lsu").getJSONObject("links").getString("related");
    }

    public String getDownloadsLink() {
	return relationships.getJSONObject("downloads").getJSONObject("links").getString("related");
    }

}
