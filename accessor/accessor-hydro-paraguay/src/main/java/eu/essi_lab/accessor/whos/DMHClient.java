package eu.essi_lab.accessor.whos;

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
public class DMHClient {

    public static final String STANDARD_ENDPOINT = "https://mch-api.meteorologia.gov.py/api";
    private String endpoint = null;
    private String token;

    public String getToken() {
	return token;
    }

    public void setToken(String token) {
	this.token = token;
    }

    public DMHClient() {
	this(STANDARD_ENDPOINT);
    }

    public DMHClient(String endpoint) {
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

    private static ExpiringCache<List<DMHStation>> stationCache;

    static {
	stationCache = new ExpiringCache<>();
	stationCache.setDuration(12000000);
    }

    public List<DMHStation> getStations() throws Exception {
	List<DMHStation> ret = stationCache.get("stations");

	if (ret != null) {
	    return ret;
	} else {
	    ret = new ArrayList<>();
	}
	String url = endpoint + "/stations";
	List<JSONObject> responses = getResponses(url);
	for (JSONObject response : responses) {
	    JSONArray stationArray = response.getJSONArray("data");
	    for (int i = 0; i < stationArray.length(); i++) {
		JSONObject stationJSON = stationArray.getJSONObject(i);
		DMHStation station = new DMHStation(stationJSON);
		ret.add(station);
	    }
	}
	stationCache.put("stations", ret);
	return ret;
    }

    public List<DMHObservation> getObservations(String stationCode, String variableCode, Date dateStart, Date dateEnd) throws Exception {
	List<DMHObservation> ret = new ArrayList<>();

	String start = ISO8601DateTimeUtils.getISO8601DateTime(dateStart).replace("Z", "");
	String end = ISO8601DateTimeUtils.getISO8601DateTime(dateEnd).replace("Z", "");
	String url = endpoint + "/observations?station_code=" + stationCode + "&variable=" + normalizeVariable(variableCode)
		+ "&date_start=" + start + "&date_end=" + end;
	List<JSONObject> responses = getResponses(url);
	for (JSONObject response : responses) {
	    JSONArray stationArray = response.getJSONArray("data");
	    for (int i = 0; i < stationArray.length(); i++) {
		JSONObject stationJSON = stationArray.getJSONObject(i);
		DMHObservation station = new DMHObservation(stationJSON);
		ret.add(station);
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
	    JSONObject payload = response.getJSONObject("payload");
	    JSONObject data = null;
	    if (payload.has("stations")) {
		data = payload.getJSONObject("stations");
	    } else {
		data = payload.getJSONObject("observations");
	    }
	    ret.add(data);
	    url = data.isNull("next_page_url") ? null : data.getString("next_page_url");
	}
	return ret;
    }

    private JSONObject getResponse(String url) throws Exception {
	Downloader downloader = new Downloader();
	String proxyEndpoint = getGiProxyEndpoint();

	if (proxyEndpoint != null) {
	    String trail = "/";
	    if (proxyEndpoint.endsWith("/")) {
		trail = "";
	    }
	    url = proxyEndpoint + trail + "get" //
		    + "?url=" + URLEncoder.encode(url, "UTF-8") + //
		    "&header=" + URLEncoder.encode("Authorization: " + token, "UTF-8");
	}
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

    public DMHStation getStation(String stationCode) throws Exception {
	List<DMHStation> stations = getStations();
	for (DMHStation station : stations) {
	    if (stationCode.equals(station.getCode())) {
		return station;
	    }
	}
	return null;
    }

}
