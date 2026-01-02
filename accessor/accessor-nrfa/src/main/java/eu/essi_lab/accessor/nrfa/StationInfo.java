package eu.essi_lab.accessor.nrfa;

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

/**
 * @author boldrini
 */
public class StationInfo {

    private JSONObject json;

    public StationInfo(JSONObject json) {
	this.json = json;
    }

    public String getIdentifier() {
	return json.get("id").toString();
    }

    public String getName() {
	return json.get("name").toString();
    }

    public String getLatitude() {
	return json.get("latitude").toString();
    }

    public String getLongitude() {
	return json.get("longitude").toString();
    }

    public String getRiver() {
	return json.get("river").toString();
    }

    public String getLocation() {
	return json.get("location").toString();
    }

    public String getElevation() {
	return json.get("station-level").toString();
    }

    public String getCatchmentArea() {
	return json.get("catchment-area").toString();
    }

    public String getMeasuringAuthorityId() {
	return json.get("measuring-authority-id").toString();
    }

    public List<ParameterInfo> getParameterInfos() {
	List<ParameterInfo> ret = new ArrayList<>();
	JSONArray types = json.getJSONObject("data-summary").getJSONArray("data-types");
	for (int i = 0; i < types.length(); i++) {
	    JSONObject type = types.getJSONObject(i);
	    ParameterInfo info = new ParameterInfo(type);
	    ret.add(info);
	}

	return ret;
    }

    public void setJSON(JSONObject json) {
	this.json = json;
    }

    public JSONObject getJSON() {
	return json;
    }
}
