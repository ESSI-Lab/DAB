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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Pages<T extends MGnifyObject> {

    private List<T> objects = new ArrayList<>();
    private String nextLink = null;
    private MGnifyObjectFactory<T> factory;
    private JSONObject json;

    public Pages(MGnifyObjectFactory<T> factory, String link) throws Exception {
	this.factory = factory;
	this.json = MGnifyClient.retrieveJSONObject(link);
	JSONArray data = json.getJSONArray("data");
	List<T> objs = new ArrayList<>();
	for (int i = 0; i < data.length(); i++) {
	    JSONObject da = data.getJSONObject(i);
	    T obj = factory.createMGnifyObject(da);
	    objs.add(obj);
	    setObjects(objs);
	}
	JSONObject links = json.getJSONObject("links");
	if (!links.isNull("next")) {
	    String next = links.getString("next");
	    setNextLink(next);
	}

    }

    public List<T> getObjects() {
	return objects;
    }

    public void setObjects(List<T> objs) {
	this.objects = objs;

    }

    public Pages<T> getNext() throws Exception {
	if (nextLink == null) {
	    return null;
	} else {
	    return new Pages<T>(factory, nextLink);
	}
    }

    public void setNextLink(String next) {
	this.nextLink = next;

    }
    
    public Integer getPage() {
	return json.getJSONObject("meta").getJSONObject("pagination").getInt("page");

    }

    public Integer getPages() {
	return json.getJSONObject("meta").getJSONObject("pagination").getInt("pages");

    }

}
