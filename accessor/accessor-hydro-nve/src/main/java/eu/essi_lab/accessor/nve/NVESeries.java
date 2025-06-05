package eu.essi_lab.accessor.nve;

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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class NVESeries extends JSONWrapper {

    public NVESeries(JSONObject series) {
	super(series);
    }

    public String getParameterName() {
	return getStringProperty("parameterName");
    }

    public String getParameterId() {
	return getStringProperty("parameter");
    }

    public String getUnit() {
	return getStringProperty("unit");
    }

    public String getSeriesFrom() {
	return getStringProperty("serieFrom");
    }

    public String getSeriesTo() {
	return getStringProperty("serieTo");
    }

    public List<NVEResolution> getResolutions() {
	List<NVEResolution> ret = new ArrayList<NVEResolution>();
	JSONArray resolutionListArray = getJsonObject().getJSONArray("resolutionList");
	for (int i = 0; i < resolutionListArray.length(); i++) {
	    JSONObject resolution = resolutionListArray.getJSONObject(i);
	    NVEResolution res = new NVEResolution(resolution);
	    ret.add(res);
	}
	return ret;
    }

    public void removeResolution(String restime) {
	JSONArray resolutionListArray = getJsonObject().getJSONArray("resolutionList");
	for (int i = 0; i < resolutionListArray.length(); i++) {
	    JSONObject resolution = resolutionListArray.getJSONObject(i);
	    NVEResolution res = new NVEResolution(resolution);
	    if (res.getResTime().equals(restime)) {
		resolutionListArray.remove(i);
		return;
	    }
	}
    }

    public String getVersionNo() {
	return getStringProperty("versionNo");
    }

    public void removeSeriesAtOtherIndex(int i) {

	JSONArray seriesListArray = getJsonObject().getJSONArray("resolutionList");
	JSONObject seriesObject = seriesListArray.getJSONObject(i);
	seriesListArray = new JSONArray();
	seriesListArray.put(seriesObject);
	getJsonObject().put("resolutionList", seriesListArray);

    }

}
