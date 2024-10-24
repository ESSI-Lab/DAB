package eu.essi_lab.accessor.inumet;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.GSLoggerFactory.GSLogger;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class INUMETClient {

    private static final String INUMET_CLIENT_LOGIN_ERROR = "INUMET_CLIENT_LOGIN_ERROR";
    private static final String INUMET_REMOTE_SERVICE_ERROR = "INUMET_REMOTE_SERVICE_ERROR";
    private static final String INUMET_CLIENT_UNABLE_TO_GET_STATIONS_ERROR = "INUMET_CLIENT_UNABLE_TO_GET_STATIONS_ERROR";

    private static String token;
    private static String user;
    private static String password;

    private static Long tokenTimestamp;
    private String endpoint;
    private GSLogger logger;
    private static String giProxyEndpoint = null;

    public static String getGiProxyEndpoint() {
	if (giProxyEndpoint == null) {
	    giProxyEndpoint = ConfigurationWrapper.getSystemSettings().getProxyEndpoint().orElse(null);
	}
	return giProxyEndpoint;
    }

    public static void setGiProxyEndpoint(String endpoint) {
	giProxyEndpoint = endpoint;
    }

    public static String getUser() {
	if (user == null) {
	    user = ConfigurationWrapper.getCredentialsSetting().getINUMETUser().orElse(null);
	}
	return user;
    }

    public static void setUser(String user) {
	INUMETClient.user = user;
    }

    public static String getPassword() {
	if (password == null) {
	    password = ConfigurationWrapper.getCredentialsSetting().getINUMETPassword().orElse(null);
	}
	return password;
    }

    public static void setPassword(String password) {
	INUMETClient.password = password;
    }

    public INUMETClient(String endpoint) {

	this.endpoint = endpoint;
	this.logger = GSLoggerFactory.getLogger(INUMETClient.class);
    }

    public String getToken() {

	return token;
    }

    public synchronized void login() throws GSException {

	long now = System.currentTimeMillis();

	if (tokenTimestamp != null) {
	    long gap = now - tokenTimestamp;
	    long twentyMinutes = 1000 * 60 * 5l;
	    if (gap < twentyMinutes) {
		// the token expires after 10 minutes, so it is safe to use this
		return;
	    }
	}

	String proxyEndpoint = getGiProxyEndpoint();

	String url = endpoint.trim() + "/sesiones/login";

	if (proxyEndpoint != null) {
	    url = proxyEndpoint + "/post?url=" + url;
	}

	HashMap<String, String> headers = new HashMap<String, String>();
	headers.put("accept", "text/plain");
	headers.put("Content-Type", "application/json");

	String user = getUser();
	String password = getPassword();

	String login = "{\"userId\": \"" + user + "\", \"password\": \"" + password + "\"}";

	logger.info("Sending login request to: {}", url);

	try {

	    HttpResponse<InputStream> response = new Downloader().downloadResponse(

		    HttpRequestUtils.build(MethodWithBody.POST, url, login, headers));

	    InputStream input = response.body();

	    ByteArrayOutputStream output = new ByteArrayOutputStream();

	    IOUtils.copy(input, output);

	    token = new String(output.toByteArray());

	    logger.info("got token: {}", token);

	    tokenTimestamp = now;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException( //
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    INUMET_CLIENT_LOGIN_ERROR, //
		    e);
	}
    }

    public List<Map<String, String>> getVariables() throws GSException {
	List<Map<String, String>> variables = new ArrayList<>();
	String source = getResponse("/variables");
	JSONArray array = new JSONArray(source);
	for (int i = 0; i < array.length(); i++) {
	    HashMap<String, String> variable = new HashMap<String, String>();
	    JSONObject item = array.getJSONObject(i);
	    Iterator<String> keys = item.keys();
	    while (keys.hasNext()) {
		String key = (String) keys.next();
		String value = item.get(key).toString();
		variable.put(key, value);
	    }
	    variables.add(variable);

	}
	return variables;
    }

    public List<Map<String, String>> getStations() throws GSException {
	List<Map<String, String>> stations = new ArrayList<>();
	String source = getResponse("/estaciones");

	JSONArray array = null;
	try {
	    array = new JSONArray(source);
	} catch (JSONException ex) {

	    String description = ex.getMessage() + "\n";
	    description += endpoint.trim() + "/estaciones\n";
	    description += source;

	    throw GSException.createException(//
		    getClass(), //
		    description, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    INUMET_CLIENT_UNABLE_TO_GET_STATIONS_ERROR);
	}

	for (int i = 0; i < array.length(); i++) {
	    HashMap<String, String> variable = new HashMap<String, String>();
	    JSONObject item = array.getJSONObject(i);
	    Iterator<String> keys = item.keys();
	    while (keys.hasNext()) {
		String key = (String) keys.next();
		String value = item.get(key).toString();
		variable.put(key, value);
	    }
	    stations.add(variable);

	}
	return stations;
    }

    private static final boolean GET_ALL_DATA_AT_ONCE = true;

    /**
     * Returns available data for the given station (it is allowed to get data only from the latest 5 days)
     * 
     * @param variableId
     * @param stationId
     * @return
     * @throws GSException
     */
    public List<SimpleEntry<Date, BigDecimal>> getLastData(String variableId, String stationId) throws GSException {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	Date now = new Date();
	long fiveDays = 1000 * 60 * 60 * 24 * 4l;
	String dateStart = sdf.format(new Date(now.getTime() - fiveDays)) + " 00:00";
	try {
	    dateStart = URLEncoder.encode(dateStart, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	String dateEnd = sdf.format(now) + " 00:00";
	try {
	    dateEnd = URLEncoder.encode(dateEnd, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	List<SimpleEntry<Date, BigDecimal>> ret = new ArrayList<>();

	String stationPath = "";

	if (GET_ALL_DATA_AT_ONCE) {
	    List<Map<String, String>> stations = getStations();
	    for (Map<String, String> station : stations) {
		String id = station.get("id");
		stationPath += "&idsEstaciones=" + id;
	    }
	} else {
	    stationPath = "&idsEstaciones=" + stationId;
	}

	String source = getResponse(
		"/datos?fechaDesde=" + dateStart + "&fechaHasta=" + dateEnd + stationPath + "&idsVariables=" + variableId + "&formato=2");
	JSONObject dataObject = new JSONObject(source);
	JSONArray datos = dataObject.getJSONArray("datos");
	JSONArray array = null;
	for (int i = 0; i < datos.length(); i++) {
	    JSONObject dato = datos.getJSONObject(i);
	    if (dato.get("estacion").toString().equals(stationId)) {
		array = dato.getJSONArray("datos");
		break;
	    }
	}

	for (int i = 0; i < array.length(); i++) {
	    JSONObject item = array.getJSONObject(i);
	    String dateString = item.getString("fecha");

	    if (dateString != null) {
		Optional<Date> date = ISO8601DateTimeUtils.parseISO8601ToDate(dateString);
		if (date.isPresent()) {
		    if (item.has("valor")) {
			String value;
			if (item.isNull("valor")) {
			    value = INUMETMapper.MISSING_VALUE;
			} else {
			    value = item.getString("valor");
			}
			try {
			    if (value.equals("TRAZA")) {
				value = "0.1";
			    }
			    ret.add(new SimpleEntry<>(date.get(), new BigDecimal(value)));
			} catch (Exception e) {
			    e.printStackTrace();
			}
		    }
		}
	    }

	}
	logger.debug("Returned data size: {}", ret.size());
	if (!ret.isEmpty()) {
	    logger.debug("Returned data begin: {}", ret.get(0).getKey());
	    logger.debug("Returned data end: {}", ret.get(ret.size() - 1).getKey());
	}
	return ret;
    }

    private static ExpiringCache<String> responseCache;

    static {
	responseCache = new ExpiringCache<>();
	responseCache.setDuration(1000 * 60 * 30l);
	responseCache.setMaxSize(500);
    }

    public synchronized String getResponse(String path) throws GSException {

	String ret = responseCache.get(path);
	if (ret != null) {
	    return ret;
	}

	int tries = 20;
	do {
	    try {

		login();

		String parameter = endpoint.trim() + path;
		parameter = URLEncoder.encode(parameter, "UTF-8");

		String url = parameter;

		String proxyEndpoint = getGiProxyEndpoint();
		if (proxyEndpoint != null) {
		    url = proxyEndpoint + "/get?url=" + url;
		}

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("accept", "application/json");
		headers.put("x-access-token", token);

		logger.info("Sending request to: {}", url);

		HttpResponse<InputStream> response = new Downloader()
			.downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url, headers));

		InputStream input = response.body();

		ByteArrayOutputStream output = new ByteArrayOutputStream();

		IOUtils.copy(input, output);

		input.close();

		ret = new String(output.toByteArray());

		if (ret.contains("Too many requests, please try again later") //
			|| ret.contains("504 Gateway Time-out")//
			|| ret.contains("usuario no se encuentra logeado")//
			|| ret.contains("sesión expiro")//

		) {
		    logger.warn("Invalid response: {}", ret);

		} else {
		    responseCache.put(path, ret);
		    return ret;
		}

	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    logger.info("Waiting 40s...(left #{} tries}", tries);
	    try {
		Thread.sleep(40000);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	} while (tries-- > 0);

	throw GSException.createException( //
		getClass(), //
		"Remote service issue", //
		null, //
		ErrorInfo.ERRORTYPE_SERVICE, //
		ErrorInfo.SEVERITY_ERROR, //
		INUMET_REMOTE_SERVICE_ERROR //
	);
    }
}
