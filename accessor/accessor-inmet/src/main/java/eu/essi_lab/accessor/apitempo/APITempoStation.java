package eu.essi_lab.accessor.apitempo;

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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class APITempoStation extends JSONObjectWrapper {

    private static final String PARAMETERS = "parameters";

    @Override
    protected APITempoStation clone() {
	JSONObject copy = new JSONObject(getJsonObject(), JSONObject.getNames(getJsonObject()));
	return new APITempoStation(copy);
    }

    public APITempoStation(JSONObject value) {
	super(value);
    }

    public String getValue(APITempoStationCode key) {
	return super.getValue(key.getKey());
    }

    public List<APITempoParameter> getParameters() {
	List<APITempoParameter> ret = new ArrayList<>();
	if (!jsonObject.isNull(PARAMETERS)) {
	    JSONArray array = this.jsonObject.getJSONArray(PARAMETERS);
	    for (int i = 0; i < array.length(); i++) {
		JSONObject child = array.getJSONObject(i);
		APITempoParameter parameter = new APITempoParameter(child);
		ret.add(parameter);
	    }
	}
	return ret;

    }

    public void setParameters(List<APITempoParameter> parameters) {
	JSONArray parameterObject = new JSONArray();
	for (APITempoParameter parameter : parameters) {
	    parameterObject.put(parameter.getJsonObject());
	}
	this.jsonObject.put(PARAMETERS, parameterObject);

    }

    public enum APITempoStationCode {
	ID("id"), // e.g. 1115
	LATITUDE("latitude"), // e.g. -15.244620000000001
	LONGITUDE("longitude"), // e.g. -40.22955
	NAME("nome"), // e.g. ITAPETINGA
	ELEVATION("altura"), // e.g. 271.5
	WIGOS_ID("wigos"), // e.g. null
	RESPONSIBLE("responsavel"); // e.g. SBBX

	private String key;

	public String getKey() {
	    return key;
	}

	APITempoStationCode(String key) {
	    this.key = key;
	}
    }
}
