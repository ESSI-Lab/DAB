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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class NVEStation extends JSONWrapper {

    public NVEStation(JSONObject stationObject) {
	super(stationObject);
    }

    public String getId() {
	return getStringProperty("stationId");
    }

    public String getName() {
	return getStringProperty("stationName");
    }

    public Double getLatitude() {
	return getDoubleProperty("latitude");
    }

    public Double getLongitude() {
	return getDoubleProperty("longitude");
    }

    public List<NVESeries> getSeries() {
	JSONArray seriesListArray = getJsonObject().getJSONArray("seriesList");
	List<NVESeries> ret = new ArrayList<>();
	for (int i = 0; i < seriesListArray.length(); i++) {
	    JSONObject series = seriesListArray.getJSONObject(i);
	    NVESeries ns = new NVESeries(series);
	    ret.add(ns);
	}
	return ret;
    }

    @Override
    public String toString() {
	return getId() + " " + getName() + " (" + getLatitude() + "/" + getLongitude() + ")";
    }

    @Override
    protected NVEStation clone() {
	JSONObject jsonClone = cloneJsonObject();
	NVEStation station = new NVEStation(jsonClone);
	return station;
    }

    public void removeSeriesAtOtherIndex(int index) {
	JSONArray seriesListArray = getJsonObject().getJSONArray("seriesList");
	Object seriesObject = seriesListArray.get(index);
	seriesListArray = new JSONArray();
	seriesListArray.put(seriesObject);
	getJsonObject().put("seriesList", seriesListArray);

    }

    public void removeSeries(String parameterId) {
	JSONArray seriesListArray = getJsonObject().getJSONArray("seriesList");
	for (int i = 0; i < seriesListArray.length(); i++) {
	    JSONObject series = seriesListArray.getJSONObject(i);
	    NVESeries ns = new NVESeries(series);
	    String id = ns.getParameterId();
	    if (id.equals(parameterId)) {
		seriesListArray.remove(i);
		return;
	    }
	}

    }

}
