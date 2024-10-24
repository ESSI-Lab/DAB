package eu.essi_lab.accessor.mgnify;

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

import org.json.JSONObject;

public class Download extends MGnifyObject {

    private JSONObject attributes;
    private JSONObject links;

    public Download(JSONObject da) {
	super(da);
	this.attributes = da.getJSONObject("attributes");
	this.links= da.getJSONObject("links");
    }

    public String getId() {
	return json.getString("id");
    }
    
    public String getAlias() {
	return attributes.getString("alias");
    } 
    
    public String getFileFormat() {
	return attributes.getJSONObject("file-format").getString("name");
    }
    
    public String getDescription() {
	return attributes.getJSONObject("description").getString("label");
    }
    
    public String getDownloadLink() {
	return links.getString("self");
    }
}
