package eu.essi_lab.accessor.nve;

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

public abstract class JSONWrapper {

    private JSONObject jsonObject;

    public JSONObject getJsonObject() {
	return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
	this.jsonObject = jsonObject;
    }

    public JSONWrapper(JSONObject jsonObject) {
	this.jsonObject = jsonObject;
    }

    public Double getDoubleProperty(String name) {
	if (jsonObject.has(name)) {
	    return Double.parseDouble(jsonObject.get(name).toString());
	} else {
	    return null;
	}
    }

    public String getStringProperty(String name) {
	if (jsonObject.has(name)) {
	    Object obj = jsonObject.get(name);
	    if (obj == null) {
		return null;
	    }
	    String s = obj.toString();
	    if (s.toLowerCase().equals("null")) {
		return null;
	    }
	    return obj.toString();
	} else {
	    return null;
	}
    }

    public JSONObject cloneJsonObject() {
	try {
	    JSONObject obj = new JSONObject(getJsonObject().toString());
	    return obj;
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }
}
