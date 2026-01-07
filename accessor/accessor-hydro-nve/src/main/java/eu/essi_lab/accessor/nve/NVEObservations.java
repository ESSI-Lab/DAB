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

public class NVEObservations extends JSONWrapper {

    public NVEObservations(JSONObject stationObject) {
	super(stationObject);
    }

    public String getStationId() {
	return getStringProperty("stationId");
    }

    public String getStationName() {
	return getStringProperty("stationName");
    }

    public String getParameterId() {
	return getStringProperty("parameter");
    }

    public String getParameterName() {
	return getStringProperty("parameterName");
    }

    public String getParameterNameEnglish() {
	return getStringProperty("parameterNameEng");
    }

    public String getMethod() {
	return getStringProperty("method");
    }

    public String getUnit() {
	return getStringProperty("unit");
    }

    public String getObservationCount() {
	return getStringProperty("observationCount");
    }

    public List<NVEObservation> getObservations() {
	JSONArray seriesListArray = getJsonObject().getJSONArray("observations");
	List<NVEObservation> ret = new ArrayList<>();
	for (int i = 0; i < seriesListArray.length(); i++) {
	    JSONObject series = seriesListArray.getJSONObject(i);
	    NVEObservation ns = new NVEObservation(series);
	    ret.add(ns);
	}
	return ret;
    }

    @Override
    public String toString() {
	return "#" + getObservationCount() + " observations";
    }

}
