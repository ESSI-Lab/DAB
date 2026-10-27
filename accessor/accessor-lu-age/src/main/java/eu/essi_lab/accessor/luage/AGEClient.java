package eu.essi_lab.accessor.luage;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Client of the real-time water levels published by the Water Management Agency of Luxembourg (Administration de la gestion de
 * l'eau, AGE) on the inondations.lu flood portal (https://inondations.public.lu/).<br>
 * The portal has no station catalogue service, so the stations are discovered from the links of the home page to the station pages.
 * Each station page embeds the station metadata (name, water course, operator, gauge zero, ...) and the path of a JSON file with the
 * time series of the station: the 15 minutes values of the last 7 days, with explicit UTC offset and not validated.<br>
 * A time series is identified by the name of the JSON file (the station code) and the WISKI parameter type, e.g. "Hesperange|W"
 *
 * @author boldrini
 */
public class AGEClient {

    public static final String DEFAULT_ENDPOINT = "https://inondations.public.lu/";

    static final String HOME_PAGE = "fr.html";

    /**
     * Folder of the station JSON files
     */
    static final String DATA_FOLDER = "content/dam/inondations/ctie/datas/";

    static final String JSON_EXTENSION = ".json";

    /**
     * Width of the time window of the published observations
     */
    static final long PUBLISHED_WINDOW = TimeUnit.DAYS.toMillis(7);

    /**
     * Width of the time window of the published observations, plus a margin
     */
    static final long DATA_WINDOW = PUBLISHED_WINDOW + TimeUnit.DAYS.toMillis(1);

    /**
     * Minimum interval between two requests
     */
    static final long MIN_REQUEST_INTERVAL = 250;

    private static final Object REQUEST_LOCK = new Object();
    private static long lastRequestTime;

    /**
     * Links of the home page to the station pages, e.g. href="//inondations.public.lu/fr/alzette/alzette/hesperange.html"
     */
    private static final Pattern STATION_PAGE_LINK = Pattern
	    .compile("href=\"(?:(?:https?:)?//[^/\"]+)?/(fr/[a-z0-9-]+/[a-z0-9-]+/[a-z0-9-]+\\.html)\"");

    /**
     * HTML escaped JSON object of the station chart component, the only one with the "jsonFile" key
     */
    private static final Pattern STATION_COMPONENT = Pattern.compile("data-[a-z-]+=\"(\\{[^\"]*jsonFile[^\"]*\\})\"");

    private static final Pattern LATITUDE = Pattern.compile("data-latitude=\"([-0-9.]+)\"");
    private static final Pattern LONGITUDE = Pattern.compile("data-longitude=\"([-0-9.]+)\"");

    /**
     * Operator of the stations of the Water Management Agency, the only ones released under CC0. The portal also publishes stations
     * operated by other agencies (e.g. Landesamt für Umwelt Rheinland-Pfalz, WSV, Service de la navigation), whose licence is unknown
     */
    static final String AGE_OPERATOR = "Administration de la gestion de l'eau";

    // keys of the station pages
    static final String JSON_FILE = "jsonFile";
    static final String STATION_NAME = "stationName";
    public static final String WATER_COURSE = "waterCourse";
    public static final String OPERATOR = "operator";
    /**
     * Gauge zero, e.g. "255,74 m NN."
     */
    public static final String GAUGE_ZERO = "zeroScale";
    /**
     * Catchment area, e.g. "292,3 km²"
     */
    public static final String CATCHMENT_AREA = "basinVersion";
    /**
     * Date of start of service, e.g. "01.09.1996"
     */
    public static final String SERVICE_DATE = "serviceDate";

    // keys of the station JSON files
    static final String PARAMETER_TYPE = "parametertype_name";
    static final String UNIT_SYMBOL = "ts_unitsymbol";
    static final String TS_PATH = "ts_path";
    static final String DATA = "data";

    // keys added by the client
    public static final String CODE = "code";
    public static final String NAME = "name";
    public static final String PAGE = "page";
    public static final String LAT = "latitude";
    public static final String LON = "longitude";
    public static final String PARAMETER = "parameter";
    public static final String UNIT = "unit";
    public static final String WISKI_PATH = "wiskiPath";
    public static final String SERIES_ID = "seriesId";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    private static final String SERIES_ID_SEPARATOR = "|";

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public AGEClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public AGEClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 2);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 20);
    }

    /**
     * @return the paths of the station pages linked by the home page, sorted, e.g. "fr/alzette/alzette/hesperange.html"
     * @throws GSException
     */
    public List<String> retrieveStationPages() throws GSException {

	List<String> ret = new ArrayList<>(parseStationPages(get(HOME_PAGE)));
	ret.sort(String::compareTo);

	return ret;
    }

    /**
     * @param path the path of a station page
     * @return the station, empty if the page does not describe a station with a JSON file
     * @throws GSException
     */
    public Optional<JSONObject> retrieveStation(String path) throws GSException {

	Optional<String> page = getOptional(path);
	if (page.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).warn("AGE station page {} not available", path);
	    return Optional.empty();
	}

	return parseStation(page.get(), endpoint + path);
    }

    /**
     * Retrieves the time series of the given station, one for each parameter of its JSON file with at least a value
     *
     * @param station
     * @return
     * @throws GSException
     */
    public List<AGEEntity> retrieveSeries(JSONObject station) throws GSException {

	List<AGEEntity> ret = new ArrayList<>();

	String code = station.getString(CODE);

	Optional<String> text = getOptional(getDataPath(code));
	if (text.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).warn("AGE data of station {} not available", code);
	    return ret;
	}

	JSONArray array = new JSONArray(text.get());
	for (int i = 0; i < array.length(); i++) {

	    JSONObject series = array.optJSONObject(i);
	    if (series == null || series.optString(PARAMETER_TYPE, "").isEmpty()) {
		continue;
	    }

	    TreeMap<Date, BigDecimal> values = parseObservations(series);
	    if (!values.isEmpty()) {
		ret.add(createSeries(station, series, values.firstKey(), values.lastKey()));
	    }
	}

	return ret;
    }

    /**
     * @param station
     * @param series a series of the station JSON file
     * @param begin
     * @param end
     * @return
     */
    static AGEEntity createSeries(JSONObject station, JSONObject series, Date begin, Date end) {

	JSONObject object = new JSONObject(station.toString());

	String parameter = series.getString(PARAMETER_TYPE);

	object.put(PARAMETER, parameter);
	object.put(UNIT, series.optString(UNIT_SYMBOL, ""));
	object.put(WISKI_PATH, series.optString(TS_PATH, ""));
	object.put(SERIES_ID, buildSeriesId(station.getString(CODE), parameter));
	object.put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(begin));
	object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(end));

	return new AGEEntity(object);
    }

    /**
     * Retrieves the observations of the given period, available only for the last 7 days
     *
     * @param series
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(AGEEntity series, Date begin, Date end) throws GSException {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	if (end.getTime() < System.currentTimeMillis() - DATA_WINDOW) {
	    return ret;
	}

	Optional<String> text = getOptional(getDataPath(series.getStationCode()));
	if (text.isEmpty()) {
	    return ret;
	}

	try {

	    JSONArray array = new JSONArray(text.get());
	    for (int i = 0; i < array.length(); i++) {

		JSONObject object = array.optJSONObject(i);
		if (object != null && series.getParameter().equals(object.optString(PARAMETER_TYPE, null))) {
		    ret.putAll(parseObservations(object));
		}
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).warn("Unable to parse AGE observations of {}: {}", series.getSeriesId(), e.getMessage());
	}

	return new TreeMap<>(ret.subMap(begin, true, end, true));
    }

    /**
     * @param homePage
     * @return the paths of the linked station pages
     */
    static Set<String> parseStationPages(String homePage) {

	Set<String> ret = new LinkedHashSet<>();

	Matcher matcher = STATION_PAGE_LINK.matcher(homePage);
	while (matcher.find()) {
	    ret.add(matcher.group(1));
	}

	return ret;
    }

    /**
     * @param page the HTML of a station page
     * @param url the URL of the page
     * @return the station, empty if the page does not describe a station with a JSON file in the {@link #DATA_FOLDER}
     */
    static Optional<JSONObject> parseStation(String page, String url) {

	Matcher matcher = STATION_COMPONENT.matcher(page);
	if (!matcher.find()) {
	    return Optional.empty();
	}

	JSONObject component;
	try {
	    component = new JSONObject(StringEscapeUtils.unescapeHtml4(matcher.group(1)));
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(AGEClient.class).warn("Unable to parse AGE station page {}: {}", url, e.getMessage());
	    return Optional.empty();
	}

	Optional<String> code = getStationCode(component.optString(JSON_FILE, ""));
	if (code.isEmpty()) {
	    GSLoggerFactory.getLogger(AGEClient.class).warn("Unexpected JSON file of AGE station page {}: {}", url,
		    component.optString(JSON_FILE));
	    return Optional.empty();
	}

	JSONObject station = new JSONObject();
	station.put(CODE, code.get());
	station.put(NAME, getText(component, STATION_NAME).orElse(code.get()));
	station.put(PAGE, url);

	for (String key : new String[] { WATER_COURSE, OPERATOR, GAUGE_ZERO, CATCHMENT_AREA, SERVICE_DATE }) {
	    getText(component, key).ifPresent(v -> station.put(key, v));
	}

	Matcher lat = LATITUDE.matcher(page);
	Matcher lon = LONGITUDE.matcher(page);
	if (lat.find() && lon.find()) {
	    station.put(LAT, new BigDecimal(lat.group(1)));
	    station.put(LON, new BigDecimal(lon.group(1)));
	}

	return Optional.of(station);
    }

    /**
     * @param station
     * @return true if the station is operated by the Water Management Agency (stations without operator are excluded)
     */
    public static boolean isOperatedByAGE(JSONObject station) {

	return AGE_OPERATOR.equalsIgnoreCase(station.optString(OPERATOR, "").replace('’', '\'').trim());
    }

    /**
     * @param jsonFile e.g. "/content/dam/inondations/ctie/datas/Hesperange.json"
     * @return e.g. "Hesperange"
     */
    static Optional<String> getStationCode(String jsonFile) {

	String path = jsonFile.startsWith("/") ? jsonFile.substring(1) : jsonFile;

	if (!path.startsWith(DATA_FOLDER) || !path.endsWith(JSON_EXTENSION)) {
	    return Optional.empty();
	}

	String code = path.substring(DATA_FOLDER.length(), path.length() - JSON_EXTENSION.length());
	if (code.isEmpty() || code.contains("/") || code.contains(SERIES_ID_SEPARATOR)) {
	    return Optional.empty();
	}

	return Optional.of(code);
    }

    /**
     * @param object
     * @param key
     * @return the trimmed text, empty if missing or blank
     */
    private static Optional<String> getText(JSONObject object, String key) {

	String value = object.optString(key, "").trim();

	return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }

    /**
     * @param series a series of a station JSON file
     * @return the non null values
     */
    static TreeMap<Date, BigDecimal> parseObservations(JSONObject series) {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	JSONArray data = series.optJSONArray(DATA);
	if (data == null) {
	    return ret;
	}

	for (int i = 0; i < data.length(); i++) {

	    JSONArray row = data.optJSONArray(i);
	    if (row == null || row.length() < 2 || row.isNull(1)) {
		continue;
	    }

	    Optional<Date> date = parseDate(row.optString(0, null));
	    BigDecimal value = row.optBigDecimal(1, null);

	    if (date.isPresent() && value != null) {
		ret.put(date.get(), value);
	    }
	}

	return ret;
    }

    /**
     * @param stationCode
     * @param parameter
     * @return e.g. "Hesperange|W"
     */
    static String buildSeriesId(String stationCode, String parameter) {

	return stationCode + SERIES_ID_SEPARATOR + parameter;
    }

    /**
     * @param seriesId
     * @return the series identified by the given identifier, or null if not valid
     */
    public static AGEEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	int index = seriesId.indexOf(SERIES_ID_SEPARATOR);
	if (index <= 0 || index == seriesId.length() - 1) {
	    return null;
	}

	String stationCode = seriesId.substring(0, index);
	String parameter = seriesId.substring(index + 1);

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, seriesId);
	object.put(CODE, stationCode);
	object.put(PARAMETER, parameter);

	return new AGEEntity(object);
    }

    /**
     * @param stationCode
     * @return e.g. "content/dam/inondations/ctie/datas/Ettelbr%C3%BCck-Alzette.json"
     */
    static String getDataPath(String stationCode) {

	return DATA_FOLDER + URLEncoder.encode(stationCode, StandardCharsets.UTF_8).replace("+", "%20") + JSON_EXTENSION;
    }

    /**
     * @param text e.g. "2026-10-20T12:00:00.000+02:00"
     * @return
     */
    static Optional<Date> parseDate(String text) {

	if (text == null || text.isEmpty()) {
	    return Optional.empty();
	}

	try {

	    return Optional.of(Date.from(OffsetDateTime.parse(text).toInstant()));

	} catch (DateTimeParseException e) {

	    return Optional.empty();
	}
    }

    /**
     * @param path
     * @return
     * @throws GSException
     */
    private String get(String path) throws GSException {

	return getOptional(path).orElseThrow(() -> createException("AGE " + path + " not available", null));
    }

    /**
     * @param path
     * @return the response, or empty if the resource is not available
     * @throws GSException
     */
    private Optional<String> getOptional(String path) throws GSException {

	try {

	    throttle();

	    HttpResponse<InputStream> response = downloader.downloadResponse(endpoint + path);

	    String text;
	    try (InputStream stream = response.body()) {
		text = stream == null ? "" : IOUtils.toString(stream, StandardCharsets.UTF_8);
	    }

	    if (response.statusCode() == 404) {
		return Optional.empty();
	    }

	    if (response.statusCode() != 200) {
		throw createException("AGE " + path + " returned HTTP " + response.statusCode(), null);
	    }

	    return text.isBlank() ? Optional.empty() : Optional.of(text);

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("AGE " + path + " request failed", e);
	}
    }

    /**
     * Waits until {@link #MIN_REQUEST_INTERVAL} is elapsed since the previous request of any client
     *
     * @throws InterruptedException
     */
    private static void throttle() throws InterruptedException {

	synchronized (REQUEST_LOCK) {

	    long elapsed = System.currentTimeMillis() - lastRequestTime;
	    if (elapsed < MIN_REQUEST_INTERVAL) {
		Thread.sleep(MIN_REQUEST_INTERVAL - elapsed);
	    }

	    lastRequestTime = System.currentTimeMillis();
	}
    }

    /**
     * @param message
     * @param cause
     * @return
     */
    private GSException createException(String message, Exception cause) {

	GSLoggerFactory.getLogger(getClass()).error(message);

	return GSException.createException(//
		getClass(), //
		ErrorInfo.ERRORTYPE_SERVICE, //
		ErrorInfo.SEVERITY_ERROR, //
		message, //
		cause);
    }

    /**
     * @param endpoint
     * @return
     */
    static String normalizeEndpoint(String endpoint) {

	if (endpoint == null || endpoint.isEmpty()) {
	    return DEFAULT_ENDPOINT;
	}

	String normalized = endpoint.trim();
	if (!normalized.endsWith("/")) {
	    normalized += "/";
	}

	return normalized;
    }
}
