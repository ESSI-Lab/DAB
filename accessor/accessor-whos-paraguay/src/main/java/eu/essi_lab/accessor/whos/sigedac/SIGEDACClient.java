package eu.essi_lab.accessor.whos.sigedac;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class SIGEDACClient {

    private String endpoint = null;

    public SIGEDACClient() {
	this("https://sigedac.meteorologia.gov.py");
    }

    public SIGEDACClient(String endpoint) {
	if (endpoint.endsWith("/")) {
	    endpoint = endpoint.substring(0, endpoint.length() - 1);
	}
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

    private static ExpiringCache<List<SIGEDACStation>> stationCache;

    static {
	stationCache = new ExpiringCache<>();
	stationCache.setDuration(12000000);
    }

    private static ExpiringCache<List<SIGEDACProperty>> propertyCache;

    static {
	propertyCache = new ExpiringCache<>();
	propertyCache.setDuration(12000000);
    }

    public List<SIGEDACStation> getStations() throws Exception {
	List<SIGEDACStation> ret = stationCache.get("stations");
	if (ret != null) {
	    return ret;
	} else {
	    ret = new ArrayList<>();
	}
	String url = endpoint + "/api/v1/stations";
	JSONObject json = getResponse(url);
	if (json.has("data")) {
	    JSONArray array = json.getJSONArray("data");
	    for (int i = 0; i < array.length(); i++) {
		JSONObject obj = array.getJSONObject(i);
		SIGEDACStation station = new SIGEDACStation(obj);
		ret.add(station);
	    }
	}
	stationCache.put("stations", ret);
	return ret;
    }

    public List<SIGEDACProperty> getProperties(String id, String resolution) throws Exception {
	List<SIGEDACProperty> ret = new ArrayList<SIGEDACProperty>();
	List<String> dates = new ArrayList<>();
	Date beginDate = ISO8601DateTimeUtils.getISO8601DateTimeWithMillisecondsAsDate(1900, 1, 1, 00, 00, 00, 00);
	Date endDate = new Date();
	if (resolution.equals("null")) {

	    JSONObject json = new JSONObject();
	    json.put("name", "Nivel de Río");
	    json.put("id", 9999);
	    json.put("short_name", "Nivel");
	    json.put("measure_unit_id", "mm");
	    json.put("measure_unit_code", "mm");
	    json.put("measure_unit_name", "millimiters");
	    json.put("measure_unit_symbol", "mm");
	    json.put("measure_unit_magnitude", "null");
	    SIGEDACProperty prop = new SIGEDACProperty(json);
	    dates = getTemporalExtent(id, prop.getId().toString(), beginDate, endDate, resolution);
	    if (!dates.isEmpty()) {
		prop.set("observations_start", dates.get(0));
		prop.set("observations_end", dates.get(1));
		ret.add(prop);
	    }
	} else {

	    List<SIGEDACProperty> properties = getProperties();
	    for (SIGEDACProperty property : properties) {

		dates = getTemporalExtent(id, property.getId().toString(), beginDate, endDate, resolution);
		if (!dates.isEmpty()) {
		    property.set("observations_start", dates.get(0));
		    property.set("observations_end", dates.get(1));
		    ret.add(property);
		}
	    }
	}
	return ret;
    }

    public List<String> getTemporalExtent(String stationCode, String variableCode, Date dateStart, Date dateEnd, String resolution)
	    throws Exception {
	List<String> ret = new ArrayList<>();

	String start = ISO8601DateTimeUtils.getISO8601DateTime(dateStart).replace("Z", "");
	String end = ISO8601DateTimeUtils.getISO8601DateTime(dateEnd).replace("Z", "");

	String url = "";
	// JSONObject response = null;
	// river use-case
	SIGEDACData response = null;
	boolean isInverse = false;
	if (resolution.equals("null")) {
	    response = getRiverLevel(stationCode, dateStart, dateEnd, 1);
	    isInverse = true;
	} else if (resolution.equals("daily")) {
	    response = getDailyData(stationCode, variableCode, dateStart, dateEnd, null);
	} else if (resolution.equals("hourly")) {
	    response = getHourlyData(stationCode, variableCode, dateStart, dateEnd, null);
	}

	if (response != null) {
	    List<SimpleEntry<Date, BigDecimal>> data = response.getData();
	    if (data != null && !data.isEmpty()) {

		SimpleEntry<Date, BigDecimal> value = data.get(0);
		String startDate = ISO8601DateTimeUtils.getISO8601DateTime(value.getKey());
		// String startDate = stationJSON.optString("observation_date");
		ret.add(startDate);
		Integer lastPage = response.getTotalPages();
		if (lastPage != null) {
		    SIGEDACData lastDataResponse = null;
		    if (resolution == "null") {
			lastDataResponse = getRiverLevel(stationCode, dateStart, dateEnd, lastPage);
		    } else if (resolution == "daily") {
			lastDataResponse = getDailyData(stationCode, variableCode, dateStart, dateEnd, lastPage);
		    } else if (resolution == "hourly") {
			lastDataResponse = getHourlyData(stationCode, variableCode, dateStart, dateEnd, lastPage);
		    }
		    if (lastDataResponse != null) {
			List<SimpleEntry<Date, BigDecimal>> lastData = lastDataResponse.getData();
			if (lastData != null && lastData.size() > 0) {
			    SimpleEntry<Date, BigDecimal> lastJSONData = lastData.get(lastData.size() - 1);
			    String endDate = ISO8601DateTimeUtils.getISO8601DateTime(lastJSONData.getKey());
			    ret.add(endDate);
			}
		    }
		}

	    }
	}
	if (isInverse) {
	    Collections.swap(ret, 0, 1);
	}
	return ret;
    }

    public List<SIGEDACProperty> getProperties() throws Exception {
	List<SIGEDACProperty> ret = propertyCache.get("properties");
	if (ret != null) {
	    return ret;
	} else {
	    ret = new ArrayList<>();
	}
	String url = endpoint + "/api/v1/measure_elements";
	JSONObject json = getResponse(url);
	if (json.has("data")) {
	    JSONArray array = json.getJSONArray("data");
	    for (int i = 0; i < array.length(); i++) {
		JSONObject obj = array.getJSONObject(i);
		SIGEDACProperty property = new SIGEDACProperty(obj);
		ret.add(property);
	    }
	}
	propertyCache.put("properties", ret);
	return ret;
    }

    public SIGEDACData getRiverLevel(String stationCode, Date start, Date end, Integer page) throws Exception {
	String url = endpoint + "/api/webpage/river/show";
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	String firstDate = sdf.format(start);
	String lastDate = sdf.format(end);
	if (start.after(end) || firstDate.equals(lastDate))
	    return null;
	String parameters = "code=" + stationCode + "&date_start=" + firstDate + "&date_end=" + lastDate;
	if (page != null) {
	    parameters += "&page=" + page;
	}
	JSONObject json = getPostResponse(url, parameters);
	SIGEDACData data = new SIGEDACData(json);
	return data;
    }

    public SIGEDACData getHourlyData(String stationId, String propertyId, Date start, Date end, Integer page) throws Exception {
	String url = endpoint + "/api/v1/observations_hourly";
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	String firstDate = sdf.format(start);
	String lastDate = sdf.format(end);
	if (start.after(end) || firstDate.equals(lastDate))
	    return null;
	url += "?station_id=" + stationId;
	url += "&measure_element_id=" + propertyId;
	url += "&date_start=" + firstDate;
	url += "&date_end=" + lastDate;
	if (page != null) {
	    url += "&page=" + page;
	}
	JSONObject res = getResponse(url);
	SIGEDACData data = new SIGEDACData(res);
	return data;
    }

    public SIGEDACData getDailyData(String stationId, String propertyId, Date start, Date end, Integer page) throws Exception {
	String url = endpoint + "/api/v1/observations_daily";
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	String firstDate = sdf.format(start);
	String lastDate = sdf.format(end);
	if (start.after(end) || firstDate.equals(lastDate))
	    return null;
	url += "?station_id=" + stationId;
	url += "&measure_element_id=" + propertyId;
	url += "&date_start=" + firstDate;
	url += "&date_end=" + lastDate;
	if (page != null) {
	    url += "&page=" + page;
	}
	JSONObject res = getResponse(url);
	SIGEDACData data = new SIGEDACData(res);
	return data;
    }

    private JSONObject getResponse(String url) throws Exception {
	return getResponseObject(url, "GET", (Object) null);
    }

    private JSONObject getPostResponse(String url, JSONObject json) throws Exception {
	return getResponseObject(url, "POST", json);
    }

    private JSONObject getPostResponse(String url, String parameters) throws Exception {
	return getResponseObject(url, "POST", parameters);
    }

    private JSONObject getResponseObject(String url, String method, Object parameters) throws Exception {
	int maxTries = 5;
	InputStream stream;
	do {

	    byte[] body = null;

	    List<SimpleEntry<String, String>> headers = new ArrayList<>();
	    HttpRequest request = null;

	    if (parameters != null) {
		if (parameters instanceof String) {
		    String pars = (String) parameters;
		    body = pars.getBytes();
		    headers.add(new SimpleEntry<String, String>("Content-Type", "application/x-www-form-urlencoded"));
		} else if (parameters instanceof JSONObject) {
		    JSONObject json = (JSONObject) parameters;
		    body = json.toString().getBytes();
		    headers.add(new SimpleEntry<String, String>("Content-Type", "application/json"));
		} else {
		    GSLoggerFactory.getLogger(getClass()).error("should not happen");
		}
	    }
	    GSLoggerFactory.getLogger(getClass()).info("Sending request to: {}", url);
	    String proxyEndpoint = getGiProxyEndpoint();
	    if (proxyEndpoint != null) {
		String trail = "/";
		if (proxyEndpoint.endsWith("/")) {
		    trail = "";
		}
		url = proxyEndpoint + trail + "get" //
			+ "?url=" + URLEncoder.encode(url, "UTF-8");
	    }
	    String headerString = "";
	    if (headers != null && !headers.isEmpty()) {
		headerString = "&header=";
		for (SimpleEntry<String, String> header : headers) {
		    headerString += header.getKey() + ":" + header.getValue() + ",";
		}
	    }
	    url += headerString;
	    switch (method.toUpperCase()) {
	    case "GET":
		request = HttpRequestUtils.build(MethodNoBody.GET, url);
		break;
	    case "POST":
		// HttpPost post = new HttpPost(url);
		// HttpEntity entity = new ByteArrayEntity(body);
		// post.setEntity(entity);
		//
		request = HttpRequestUtils.build(MethodWithBody.POST, url, body);

		break;
	    default:
		GSLoggerFactory.getLogger(getClass()).error("Unexpected");
		throw new RuntimeException("Unexpected");
	    }

	    GSLoggerFactory.getLogger(getClass()).trace("Downloading from: " + url + " STARTED");

	    Downloader executor = new Downloader();

	    try {

		HttpResponse<InputStream> response = executor.downloadResponse(request);
		int code = response.statusCode();
		stream = response.body();
		if (code == 200) {
		    break;
		} else {
		    stream.close();
		}
	    } catch (Exception e) {
		stream = null;
	    }
	    GSLoggerFactory.getLogger(getClass()).info("Sleeping and retrying {}", maxTries);
	    Thread.sleep(5000);

	} while (maxTries-- > 0);
	if (maxTries < 0) {
	    throw new RuntimeException("HTTP error code getting from SIGEDAC");
	}
	String source = IOUtils.toString(stream, StandardCharsets.UTF_8);
	stream.close();
	JSONObject json = new JSONObject(source);
	return json;

    }

    public List<SimpleEntry<Date, BigDecimal>> getObservations(String stationCode, String parameterCode, Date begin, Date end,
	    String frequency) throws Exception {

	List<SimpleEntry<Date, BigDecimal>> ret = new ArrayList<>();

	boolean complete = false;
	// List<JSONObject> responses = getResponses(url);
	int page = 1;
	while (!complete) {
	    SIGEDACData data = null;
	    // RIVER USE-CAsE
	    if (frequency == null) {
		data = getRiverLevel(stationCode, begin, end, page);
	    } else if (frequency.toLowerCase().equals("d")) {
		data = getDailyData(stationCode, parameterCode, begin, end, page);
	    } else if (frequency.toLowerCase().equals("h")) {
		data = getHourlyData(stationCode, parameterCode, begin, end, page);
	    }

	    if (data != null) {
		Integer lastPage = data.getTotalPages();
		List<SimpleEntry<Date, BigDecimal>> res = data.getData();
		for (SimpleEntry<Date, BigDecimal> s : res) {
		    ret.add(s);
		}
		if (page < lastPage) {
		    page++;
		} else {
		    complete = true;
		}

	    } else {
		complete = true;
	    }
	}

	return ret;
    }

}
