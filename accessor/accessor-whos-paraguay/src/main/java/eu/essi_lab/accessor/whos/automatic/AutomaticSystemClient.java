package eu.essi_lab.accessor.whos.automatic;

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

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class AutomaticSystemClient {

    public static final String STANDARD_ENDPOINT = "https://automaticas.meteorologia.gov.py";
    private String endpoint = null;
    private String token;

    public String getToken() {
	return token;
    }

    public void setToken(String token) {
	this.token = token;
    }

    public AutomaticSystemClient() {
	this(STANDARD_ENDPOINT);
    }

    public AutomaticSystemClient(String endpoint) {
	this.endpoint = endpoint;
    }

    private static String giProxyEndpoint = null;

    public static String getGiProxyEndpoint() {
	if (giProxyEndpoint == null) {
	    try {
		giProxyEndpoint = ConfigurationWrapper.getSystemSettings().getProxyEndpoint().orElse(null);
	    } catch (Exception e) {
		// TODO: handle exception
	    }
	}
	return giProxyEndpoint;
    }

    public static void setGiProxyEndpoint(String endpoint) {
	giProxyEndpoint = endpoint;
    }

    private static ExpiringCache<List<AutomaticSystemStation>> stationCache;

    private static List<AutomaticSystemVariable> variableList = new ArrayList<AutomaticSystemVariable>();

    static {
	stationCache = new ExpiringCache<>();
	stationCache.setDuration(12000000);
    }

    public List<AutomaticSystemStation> getStations() throws Exception {
	List<AutomaticSystemStation> ret = stationCache.get("stations");

	if (ret != null) {
	    return ret;
	} else {
	    ret = new ArrayList<>();
	}
	String url = endpoint + "/api/v1/stations";

	JSONObject response = getResponse(url);
	JSONArray stationArray = response.getJSONArray("data");
	for (int i = 0; i < stationArray.length(); i++) {
	    JSONObject stationJSON = stationArray.getJSONObject(i);
	    AutomaticSystemStation station = new AutomaticSystemStation(stationJSON);
	    ret.add(station);
	}
	stationCache.put("stations", ret);
	return ret;
    }

    public List<AutomaticSystemObservation> getObservations(String stationCode, String variableCode, Date dateStart, Date dateEnd)
	    throws Exception {
	List<AutomaticSystemObservation> ret = new ArrayList<>();

	String start = ISO8601DateTimeUtils.getISO8601DateTime(dateStart).replace("Z", "");
	String end = ISO8601DateTimeUtils.getISO8601DateTime(dateEnd).replace("Z", "");

	boolean complete = false;
	// List<JSONObject> responses = getResponses(url);
	int page = 1;
	while (!complete) {
	    String url = endpoint + "/api/v1/observations?station_id=" + stationCode + "&measure_element_id="
		    + normalizeVariable(variableCode) + "&date_start=" + start + "&date_end=" + end + "&page=" + page;
	    JSONObject response = getResponse(url);
	    JSONArray dataArray = response.getJSONArray("data");
	    for (int i = 0; i < dataArray.length(); i++) {
		JSONObject stationJSON = dataArray.getJSONObject(i);
		AutomaticSystemObservation station = new AutomaticSystemObservation(stationJSON);
		ret.add(station);
	    }
	    JSONObject meta = response.optJSONObject("meta");
	    int lastPage = meta.optInt("last_page");
	    if (page < lastPage) {
		page++;
	    } else {
		complete = true;
	    }
	}
	return ret;
    }

    public List<String> getTemporalExtent(String stationCode, String variableCode, Date dateStart, Date dateEnd) throws Exception {
	List<String> ret = new ArrayList<>();

	String start = ISO8601DateTimeUtils.getISO8601DateTime(dateStart).replace("Z", "");
	String end = ISO8601DateTimeUtils.getISO8601DateTime(dateEnd).replace("Z", "");
	String url = endpoint + "/api/v1/observations?station_id=" + stationCode + "&measure_element_id=" + normalizeVariable(variableCode)
		+ "&date_start=" + start + "&date_end=" + end;
	// List<JSONObject> responses = getResponses(url);
	JSONObject response = getResponse(url);
	JSONArray dataArray = response.optJSONArray("data");
	if (dataArray != null && dataArray.length() > 0) {
	    // observation_date":"2004-03-10T00:10:00+00:00
	    JSONObject stationJSON = dataArray.getJSONObject(0);
	    String startDate = stationJSON.optString("observation_date");
	    ret.add(startDate);
	    JSONObject links = response.optJSONObject("links");
	    if (links != null) {
		String lastUrl = links.optString("last");
		JSONObject lastDataResponse = getResponse(lastUrl);
		if (lastDataResponse != null) {
		    JSONArray lastDataArray = lastDataResponse.optJSONArray("data");
		    if (lastDataArray != null) {
			JSONObject lastJSONData = lastDataArray.getJSONObject(lastDataArray.length() - 1);
			String endDate = lastJSONData.optString("observation_date");
			ret.add(endDate);
		    }
		}
	    }
	}

	return ret;
    }

    private String normalizeVariable(String variableCode) {
	// variableCode = variableCode.replace("í", "i");
	return variableCode;
    }

    private List<JSONObject> getResponses(String url) throws Exception {
	List<JSONObject> ret = new ArrayList<>();
	while (url != null) {
	    JSONObject response = getResponse(url);

	    JSONObject data = response.getJSONObject("data");

	    ret.add(data);
	    JSONObject links = response.optJSONObject("links");
	    url = links.isNull("next") ? null : data.getString("next");
	}
	return ret;
    }

    private JSONObject getResponse(String url) throws Exception {

	String proxyEndpoint = getGiProxyEndpoint();

	if (proxyEndpoint != null) {
	    String trail = "/";
	    if (proxyEndpoint.endsWith("/")) {
		trail = "";
	    }
	    url = proxyEndpoint + trail + "get" //
		    + "?url=" + URLEncoder.encode(url, "UTF-8");
	}

	Downloader downloader = new Downloader();
	HttpResponse<InputStream> ret;

	int maxTries = 5;
	do {
	    ret = downloader.downloadResponse(url);
	    if (ret.statusCode() == 200) {
		break;
	    } else {
		GSLoggerFactory.getLogger(getClass()).info("Sleeping and retrying {}", maxTries);
		Thread.sleep(5000);
	    }
	} while (maxTries-- > 0);

	if (maxTries < 0) {
	    throw new RuntimeException("HTTP error code getting from DMH");
	}

	InputStream stream = ret.body();
	String source = IOUtils.toString(stream, StandardCharsets.UTF_8);
	stream.close();
	JSONObject json = new JSONObject(source);
	return json;

    }

    public AutomaticSystemStation getStation(String stationCode) throws Exception {
	List<AutomaticSystemStation> stations = getStations();
	for (AutomaticSystemStation station : stations) {
	    if (stationCode.equals(station.getCode())) {
		return station;
	    }
	}
	return null;
    }

    public List<AutomaticSystemVariable> getVariables(BigDecimal stationCode) throws Exception {
	List<AutomaticSystemVariable> ret = new ArrayList<AutomaticSystemVariable>();
	if (variableList.isEmpty()) {
	    String url = endpoint + "/api/v1/measure_elements";
	    JSONObject response = getResponse(url);

	    JSONArray variablesArray = response.getJSONArray("data");
	    for (int i = 0; i < variablesArray.length(); i++) {
		JSONObject variableJSON = variablesArray.getJSONObject(i);
		AutomaticSystemVariable var = new AutomaticSystemVariable(variableJSON);
		variableList.add(var);
	    }
	}

	for (AutomaticSystemVariable var : variableList) {

	    Date beginDate = ISO8601DateTimeUtils.getISO8601DateTimeWithMillisecondsAsDate(1900, 1, 1, 00, 00, 00, 00);
	    Date endDate = new Date();
	    List<String> dates = getTemporalExtent(stationCode.toString(), var.getId().toString(), beginDate, endDate);
	    if (!dates.isEmpty()) {
		var.set("observations_start", dates.get(0));
		var.set("observations_end", dates.get(1));
		ret.add(var);
	    }
	}

	return ret;

    }

    // public List<DMHVariable> getVariables() {
    // return variableList;
    // }

}
