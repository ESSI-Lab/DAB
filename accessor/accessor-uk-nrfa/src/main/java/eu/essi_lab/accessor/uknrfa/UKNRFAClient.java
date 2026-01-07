package eu.essi_lab.accessor.uknrfa;

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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;

public class UKNRFAClient {

    public static final String DEFAULT_ENDPOINT = "https://nrfaapps.ceh.ac.uk/nrfa/ws/";

    public static final String STATION_IDS_PATH = "station-ids";
    public static final String STATION_INFO_PATH = "station-info";
    public static final String TIME_SERIES_PATH = "time-series";

    public enum TabularDataFormat {

	CSV("csv"), HTML("html"), JSON_ARRAY("json-array"), JSON_OBJECT("json-object");

	private String key;

	TabularDataFormat(String key) {
	    this.key = key;
	}

	@Override
	public String toString() {
	    return key;
	}

    }

    public enum TimeSeriesDataFormat {

	NRFA_CSV("nrfa-csv"), FEH_DATA("feh-data"), FEH_CSV("feh-csv"), JSON_OBJECT("json-object");

	private String key;

	TimeSeriesDataFormat(String key) {
	    this.key = key;
	}

	@Override
	public String toString() {
	    return key;
	}

    }

    public enum DataType {
	GDF("gdf", "Gauged daily flows"), //
	NDF("ndf", "Naturalised daily flows"), //
	GMF("gmf", "Gauged monthly flows"), //
	NMF("nmf", "Naturalised monthly flows"), //
	CDR("cdr", "Catchment daily rainfall"), //
	CDR_D("cdr-d", "Catchment daily rainfall distance to rain gauge"), //
	CMR("cmr", "Catchment monthly rainfall"), //
	POT_STAGE("pot-stage", "Peaks over threshold stage"), //
	POT_FLOW("pot-flow", "Peaks over threshold flow"), //
	GAUGING_STAGE("gauging-stage", "Gauging stage"), //
	GAUGING_FLOW("gauging-flow", "Gauging flow"), //
	AMAX_STAGE("amax-stage", "Annual maxima stage"), //
	AMAX_FLOW("amax-flow", "Annual maxima flow");//

	private String key;
	private String label;

	DataType(String key, String label) {
	    this.key = key;
	    this.label = label;
	}

	@Override
	public String toString() {
	    return key;
	}

	public String getLabel() {
	    return label;
	}

	public String getKey() {
	    return key;
	}

    }

    private String endpoint;

    public UKNRFAClient() {
	this(DEFAULT_ENDPOINT);
    }

    public UKNRFAClient(String endpoint) {
	this.endpoint = endpoint;
    }

    public Set<String> getStationIdentifiers() {
	String url = endpoint + STATION_IDS_PATH + "?format=" + TabularDataFormat.JSON_OBJECT;
	Downloader downloader = new Downloader();
	Optional<String> response = downloader.downloadOptionalString(url);
	Set<String> ret = new HashSet<>();
	if (response.isPresent()) {
	    JSONObject obj = new JSONObject(response.get());
	    if (obj.has("station-ids")) {
		JSONArray array = obj.getJSONArray("station-ids");
		for (int i = 0; i < array.length(); i++) {
		    Object id = array.get(i);
		    ret.add(id.toString());
		}
	    }
	}
	return ret;
    }

    public JSONObject getStationInfo(String id) {
	String url = endpoint + STATION_INFO_PATH + "?format=" + TabularDataFormat.JSON_OBJECT + "&station=" + id + "&fields=all";
	Downloader downloader = new Downloader();
	Optional<String> response = downloader.downloadOptionalString(url);
	if (response.isPresent()) {
	    JSONObject obj = new JSONObject(response.get());
	    return obj;

	}
	return null;
    }

    public JSONObject getData(String id, String parameter) {
	String url = endpoint + TIME_SERIES_PATH + "?format=" + TimeSeriesDataFormat.JSON_OBJECT + "&station=" + id + "&data-type="
		+ parameter;
	Downloader downloader = new Downloader();
	Optional<String> response = downloader.downloadOptionalString(url);
	if (response.isPresent()) {
	    JSONObject obj = new JSONObject(response.get());
	    return obj;

	}
	return null;
    }

}
