package eu.essi_lab.accessor.enhydris;

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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Client of the REST API of Enhydris (https://enhydris.readthedocs.io/), the hydrological/meteorological database used, among
 * others, by OpenHi.net (https://openhi.net/), the Greek open hydrological information system.<br>
 * The stations are listed by the paginated "stations/" service; each station has time series groups (a variable at the station,
 * e.g. "Mean stage"), each one with one or more time series (e.g. "Initial", "Checked", "Aggregated"). The values of a time series
 * are downloaded as CSV ("timestamp,value,flags" lines) in UTC, by means of the "timezone" parameter (by default they are in the
 * display time zone of the station).<br>
 * A time series is identified by the station, group and time series identifiers, e.g. "2022/1244/10846"
 *
 * @author boldrini
 */
public class EnhydrisClient {

    public static final String DEFAULT_ENDPOINT = "https://system.openhi.net/api/";

    static final String API_PATH = "api/";

    static final String STATIONS = "stations/";
    static final String VARIABLES = "variables/";
    static final String UNITS = "units/";
    static final String ORGANIZATIONS = "organizations/";

    /**
     * Minimum interval between two requests
     */
    static final long MIN_REQUEST_INTERVAL = 250;

    private static final Object REQUEST_LOCK = new Object();
    private static long lastRequestTime;

    /**
     * e.g. "SRID=4326;POINT (20.975265 39.15104)"
     */
    private static final Pattern POINT = Pattern.compile("(?:SRID=(\\d+);)?\\s*POINT\\s*\\(\\s*([-0-9.eE]+)\\s+([-0-9.eE]+)");

    private static final DateTimeFormatter RECORD_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm[:ss]");
    private static final DateTimeFormatter QUERY_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    // keys of the API objects
    static final String RESULTS = "results";
    static final String NEXT = "next";
    static final String ID = "id";
    static final String API_NAME = "name";
    static final String API_CODE = "code";
    static final String API_REMARKS = "remarks";
    static final String GEOM = "geom";
    static final String API_ALTITUDE = "altitude";
    static final String API_OWNER = "owner";
    static final String HIDDEN = "hidden";
    static final String API_VARIABLE = "variable";
    static final String UNIT_OF_MEASUREMENT = "unit_of_measurement";
    static final String API_PRECISION = "precision";
    static final String API_TYPE = "type";
    static final String API_TIME_STEP = "time_step";
    static final String PUBLICLY_AVAILABLE = "publicly_available";
    static final String TRANSLATIONS = "translations";
    static final String DESCR = "descr";
    static final String SYMBOL = "symbol";
    static final String ACRONYM = "acronym";
    static final String TIMESTAMP = "timestamp";

    // keys of the series
    public static final String STATION_ID = "stationId";
    public static final String STATION_NAME = "stationName";
    public static final String STATION_CODE = "stationCode";
    public static final String STATION_REMARKS = "stationRemarks";
    public static final String LAT = "latitude";
    public static final String LON = "longitude";
    public static final String ALTITUDE = "altitude";
    public static final String OWNER_ID = "ownerId";
    public static final String OWNER = "owner";
    public static final String OWNER_ACRONYM = "ownerAcronym";
    public static final String GROUP_ID = "groupId";
    public static final String GROUP_NAME = "groupName";
    public static final String VARIABLE_ID = "variableId";
    public static final String VARIABLE = "variable";
    public static final String UNIT = "unit";
    public static final String UNIT_NAME = "unitName";
    public static final String PRECISION = "precision";
    public static final String TIMESERIES_ID = "timeseriesId";
    /**
     * e.g. "Initial", "Checked", "Regularized", "Aggregated"
     */
    public static final String TIMESERIES_TYPE = "timeseriesType";
    /**
     * Name of the time series in the group, e.g. "Mean" for an aggregated time series
     */
    public static final String TIMESERIES_NAME = "timeseriesName";
    /**
     * Pandas like time step, e.g. "10min", "3h", "1D", empty if irregular
     */
    public static final String TIME_STEP = "timeStep";
    public static final String SERIES_ID = "seriesId";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    private static final String SERIES_ID_SEPARATOR = "/";

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public EnhydrisClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint the URL of the Enhydris API, e.g. "https://system.openhi.net/api/" (the "api/" path is added if missing)
     */
    public EnhydrisClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 2);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 20);
    }

    /**
     * @return the stations, sorted by identifier
     * @throws GSException
     */
    public List<JSONObject> retrieveStations() throws GSException {

	List<JSONObject> ret = getList(STATIONS);
	ret.sort((s1, s2) -> Integer.compare(s1.getInt(ID), s2.getInt(ID)));

	return ret;
    }

    /**
     * @return the variables by identifier
     * @throws GSException
     */
    public Map<Integer, JSONObject> retrieveVariables() throws GSException {

	return getMap(VARIABLES);
    }

    /**
     * @return the units of measurement by identifier
     * @throws GSException
     */
    public Map<Integer, JSONObject> retrieveUnits() throws GSException {

	return getMap(UNITS);
    }

    /**
     * @return the organizations (the station owners) by identifier
     * @throws GSException
     */
    public Map<Integer, JSONObject> retrieveOrganizations() throws GSException {

	return getMap(ORGANIZATIONS);
    }

    /**
     * Retrieves the public time series of the given station with at least a value
     *
     * @param station
     * @param variables
     * @param units
     * @param organizations
     * @return
     * @throws GSException
     */
    public List<EnhydrisEntity> retrieveSeries(//
	    JSONObject station, //
	    Map<Integer, JSONObject> variables, //
	    Map<Integer, JSONObject> units, //
	    Map<Integer, JSONObject> organizations) throws GSException {

	List<EnhydrisEntity> ret = new ArrayList<>();

	int stationId = station.getInt(ID);

	for (JSONObject group : getList(getGroupsPath(stationId))) {

	    if (group.optBoolean(HIDDEN, false)) {
		continue;
	    }

	    for (JSONObject timeseries : getList(getTimeseriesPath(stationId, group.getInt(ID)))) {

		if (!timeseries.optBoolean(PUBLICLY_AVAILABLE, true)) {
		    continue;
		}

		String seriesId = buildSeriesId(stationId, group.getInt(ID), timeseries.getInt(ID));

		Optional<SimpleEntry<Date, Date>> extent = retrieveExtent(seriesId);
		if (extent.isEmpty()) {
		    GSLoggerFactory.getLogger(getClass()).debug("Skipping empty Enhydris time series {}", seriesId);
		    continue;
		}

		ret.add(createSeries(station, group, timeseries, variables, units, organizations, extent.get().getKey(),
			extent.get().getValue()));
	    }
	}

	return ret;
    }

    /**
     * @param station
     * @param group
     * @param timeseries
     * @param variables
     * @param units
     * @param organizations
     * @param begin
     * @param end
     * @return
     */
    static EnhydrisEntity createSeries(//
	    JSONObject station, //
	    JSONObject group, //
	    JSONObject timeseries, //
	    Map<Integer, JSONObject> variables, //
	    Map<Integer, JSONObject> units, //
	    Map<Integer, JSONObject> organizations, //
	    Date begin, //
	    Date end) {

	JSONObject object = new JSONObject();

	int stationId = station.getInt(ID);

	object.put(STATION_ID, stationId);
	object.put(STATION_NAME, getText(station, API_NAME).orElse(String.valueOf(stationId)));
	getText(station, API_CODE).ifPresent(v -> object.put(STATION_CODE, v));
	getText(station, API_REMARKS).ifPresent(v -> object.put(STATION_REMARKS, v));

	parsePoint(station.optString(GEOM, "")).ifPresent(p -> {
	    object.put(LON, p.getKey());
	    object.put(LAT, p.getValue());
	});

	if (!station.isNull(API_ALTITUDE) && station.has(API_ALTITUDE)) {
	    object.put(ALTITUDE, station.getBigDecimal(API_ALTITUDE));
	}

	if (!station.isNull(API_OWNER) && station.has(API_OWNER)) {
	    int ownerId = station.getInt(API_OWNER);
	    object.put(OWNER_ID, ownerId);
	    JSONObject owner = organizations.get(ownerId);
	    if (owner != null) {
		getText(owner, API_NAME).ifPresent(v -> object.put(OWNER, v));
		getText(owner, ACRONYM).ifPresent(v -> object.put(OWNER_ACRONYM, v));
	    }
	}

	object.put(GROUP_ID, group.getInt(ID));
	getText(group, API_NAME).ifPresent(v -> object.put(GROUP_NAME, v));
	if (!group.isNull(API_PRECISION) && group.has(API_PRECISION)) {
	    object.put(PRECISION, group.getInt(API_PRECISION));
	}

	if (!group.isNull(API_VARIABLE) && group.has(API_VARIABLE)) {
	    int variableId = group.getInt(API_VARIABLE);
	    object.put(VARIABLE_ID, variableId);
	    getDescription(variables.get(variableId)).ifPresent(v -> object.put(VARIABLE, v));
	}

	if (!group.isNull(UNIT_OF_MEASUREMENT) && group.has(UNIT_OF_MEASUREMENT)) {
	    JSONObject unit = units.get(group.getInt(UNIT_OF_MEASUREMENT));
	    if (unit != null) {
		getText(unit, SYMBOL).ifPresent(v -> object.put(UNIT, v));
		// e.g. "Centimetres [Εκατοστόμετρα]"
		getText(unit, DESCR).map(v -> v.replaceAll("\\s*\\[[^\\]]*\\]\\s*$", "").trim()).filter(v -> !v.isEmpty())
			.ifPresent(v -> object.put(UNIT_NAME, v));
	    }
	}

	object.put(TIMESERIES_ID, timeseries.getInt(ID));
	getText(timeseries, API_TYPE).ifPresent(v -> object.put(TIMESERIES_TYPE, v));
	getText(timeseries, API_NAME).ifPresent(v -> object.put(TIMESERIES_NAME, v));
	getText(timeseries, API_TIME_STEP).ifPresent(v -> object.put(TIME_STEP, v));

	object.put(SERIES_ID, buildSeriesId(stationId, group.getInt(ID), timeseries.getInt(ID)));
	object.put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(begin));
	object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(end));

	return new EnhydrisEntity(object);
    }

    /**
     * Retrieves the temporal extent of the given time series. The last timestamp is provided by the "bottom/" service; the first one
     * is read from the beginning of the data, limited to the first bin of the "chart/" service (200 bins spanning the whole series)
     *
     * @param seriesId
     * @return the extent, empty if the time series has no values
     * @throws GSException
     */
    public Optional<SimpleEntry<Date, Date>> retrieveExtent(String seriesId) throws GSException {

	String path = getSeriesPath(seriesId);

	Optional<Date> end = getOptional(path + "bottom/?timezone=UTC").flatMap(t -> parseTimestamp(t.trim()));
	if (end.isEmpty()) {
	    return Optional.empty();
	}

	String dataPath = path + "data/?fmt=csv&timezone=UTC";

	Optional<Date> begin = Optional.empty();

	Optional<Date> firstBinEnd = getOptional(path + "chart/").flatMap(EnhydrisClient::parseFirstBinEnd);
	if (firstBinEnd.isPresent()) {
	    begin = readFirstTimestamp(dataPath + "&end_date=" + formatQueryDate(firstBinEnd.get()));
	}

	if (begin.isEmpty()) {
	    begin = readFirstTimestamp(dataPath);
	}

	return begin.map(b -> new SimpleEntry<>(b, end.get()));
    }

    /**
     * Retrieves the observations of the given period
     *
     * @param series
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(EnhydrisEntity series, Date begin, Date end) throws GSException {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	Optional<String> text = getOptional(getSeriesPath(series.getSeriesId()) + "data/?fmt=csv&timezone=UTC&start_date="
		+ formatQueryDate(begin) + "&end_date=" + formatQueryDate(end));

	if (text.isEmpty()) {
	    return ret;
	}

	ret.putAll(parseRecords(text.get()));

	return new TreeMap<>(ret.subMap(begin, true, end, true));
    }

    /**
     * @param text the CSV data of a time series
     * @return the records with a value
     */
    static TreeMap<Date, BigDecimal> parseRecords(String text) {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	for (String line : text.split("\\r?\\n")) {

	    String[] fields = line.split(",", -1);
	    if (fields.length < 2 || fields[1].trim().isEmpty()) {
		continue;
	    }

	    Optional<Date> date = parseTimestamp(line);
	    if (date.isEmpty()) {
		continue;
	    }

	    try {
		ret.put(date.get(), new BigDecimal(fields[1].trim()));
	    } catch (NumberFormatException e) {
		GSLoggerFactory.getLogger(EnhydrisClient.class).warn("Unexpected Enhydris value: {}", line);
	    }
	}

	return ret;
    }

    /**
     * @param line a CSV record, e.g. "2026-10-26 06:00,0.82,MISSING2"
     * @return the timestamp of the record, in UTC
     */
    static Optional<Date> parseTimestamp(String line) {

	if (line == null) {
	    return Optional.empty();
	}

	int index = line.indexOf(',');
	String text = (index < 0 ? line : line.substring(0, index)).trim();
	if (text.isEmpty()) {
	    return Optional.empty();
	}

	try {

	    return Optional.of(Date.from(LocalDateTime.parse(text, RECORD_DATE).toInstant(ZoneOffset.UTC)));

	} catch (DateTimeParseException e) {

	    return Optional.empty();
	}
    }

    /**
     * @param chart the bins of the "chart/" service, e.g. [{"timestamp":1574130402,"min":0.0,"max":0.69,"mean":0.03}, ...]
     * @return the end of the first bin (the timestamp of the second bin), or the timestamp of the first bin if it is the only one
     */
    static Optional<Date> parseFirstBinEnd(String chart) {

	try {

	    JSONArray bins = new JSONArray(chart);
	    if (bins.length() == 0) {
		return Optional.empty();
	    }

	    long timestamp = bins.getJSONObject(bins.length() > 1 ? 1 : 0).getLong(TIMESTAMP);

	    return Optional.of(new Date(TimeUnit.SECONDS.toMillis(timestamp)));

	} catch (Exception e) {

	    return Optional.empty();
	}
    }

    /**
     * @param geom e.g. "SRID=4326;POINT (20.975265 39.15104)"
     * @return the longitude and latitude, empty if not a WGS84 point
     */
    static Optional<SimpleEntry<BigDecimal, BigDecimal>> parsePoint(String geom) {

	Matcher matcher = POINT.matcher(geom);
	if (!matcher.find() || (matcher.group(1) != null && !matcher.group(1).equals("4326"))) {
	    return Optional.empty();
	}

	try {
	    return Optional.of(new SimpleEntry<>(new BigDecimal(matcher.group(2)), new BigDecimal(matcher.group(3))));
	} catch (NumberFormatException e) {
	    return Optional.empty();
	}
    }

    /**
     * @param object a variable or a unit of measurement
     * @return the english description, or any other description if not available
     */
    static Optional<String> getDescription(JSONObject object) {

	if (object == null) {
	    return Optional.empty();
	}

	JSONObject translations = object.optJSONObject(TRANSLATIONS);
	if (translations != null) {

	    Optional<String> english = Optional.ofNullable(translations.optJSONObject("en")).flatMap(t -> getText(t, DESCR));
	    if (english.isPresent()) {
		return english;
	    }

	    for (String language : translations.keySet()) {
		Optional<String> descr = Optional.ofNullable(translations.optJSONObject(language)).flatMap(t -> getText(t, DESCR));
		if (descr.isPresent()) {
		    return descr;
		}
	    }
	}

	return getText(object, DESCR);
    }

    /**
     * @param object
     * @param key
     * @return the trimmed text, empty if missing, null or blank
     */
    static Optional<String> getText(JSONObject object, String key) {

	if (!object.has(key) || object.isNull(key)) {
	    return Optional.empty();
	}

	String value = object.get(key).toString().trim();

	return value.isEmpty() ? Optional.empty() : Optional.of(value);
    }

    /**
     * @param stationId
     * @param groupId
     * @param timeseriesId
     * @return e.g. "2022/1244/10846"
     */
    static String buildSeriesId(int stationId, int groupId, int timeseriesId) {

	return stationId + SERIES_ID_SEPARATOR + groupId + SERIES_ID_SEPARATOR + timeseriesId;
    }

    /**
     * @param seriesId
     * @return the series identified by the given identifier, or null if not valid
     */
    public static EnhydrisEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	String[] split = seriesId.split(SERIES_ID_SEPARATOR, -1);
	if (split.length != 3) {
	    return null;
	}

	try {

	    JSONObject object = new JSONObject();
	    object.put(SERIES_ID, seriesId);
	    object.put(STATION_ID, Integer.parseInt(split[0]));
	    object.put(GROUP_ID, Integer.parseInt(split[1]));
	    object.put(TIMESERIES_ID, Integer.parseInt(split[2]));

	    return new EnhydrisEntity(object);

	} catch (NumberFormatException e) {

	    return null;
	}
    }

    /**
     * @param stationId
     * @return
     */
    static String getGroupsPath(int stationId) {

	return STATIONS + stationId + "/timeseriesgroups/";
    }

    /**
     * @param stationId
     * @param groupId
     * @return
     */
    static String getTimeseriesPath(int stationId, int groupId) {

	return getGroupsPath(stationId) + groupId + "/timeseries/";
    }

    /**
     * @param seriesId
     * @return e.g. "stations/2022/timeseriesgroups/1244/timeseries/10846/"
     * @throws GSException
     */
    String getSeriesPath(String seriesId) throws GSException {

	EnhydrisEntity series = parseSeriesId(seriesId);
	if (series == null) {
	    throw createException("Invalid Enhydris series identifier " + seriesId, null);
	}

	return getTimeseriesPath(series.getStationId(), series.getGroupId()) + series.getTimeseriesId() + "/";
    }

    /**
     * @param date
     * @return e.g. "2026-10-25T13:30:00Z"
     */
    static String formatQueryDate(Date date) {

	return QUERY_DATE.format(date.toInstant().atOffset(ZoneOffset.UTC));
    }

    /**
     * @param path
     * @return the objects of all the pages of the given list service
     * @throws GSException
     */
    private List<JSONObject> getList(String path) throws GSException {

	List<JSONObject> ret = new ArrayList<>();

	String url = endpoint + path + "?format=json";

	while (url != null) {

	    String text = getOptionalURL(url).orElseThrow(() -> createException("Enhydris " + path + " not available", null));

	    JSONArray results;
	    if (text.trim().startsWith("[")) {
		results = new JSONArray(text);
		url = null;
	    } else {
		JSONObject page = new JSONObject(text);
		results = page.optJSONArray(RESULTS);
		url = page.isNull(NEXT) ? null : page.optString(NEXT, null);
	    }

	    if (results != null) {
		for (int i = 0; i < results.length(); i++) {
		    JSONObject object = results.optJSONObject(i);
		    if (object != null) {
			ret.add(object);
		    }
		}
	    }
	}

	return ret;
    }

    /**
     * @param path
     * @return the objects of the given list service, by identifier
     * @throws GSException
     */
    private Map<Integer, JSONObject> getMap(String path) throws GSException {

	Map<Integer, JSONObject> ret = new HashMap<>();

	for (JSONObject object : getList(path)) {
	    if (object.has(ID)) {
		ret.put(object.getInt(ID), object);
	    }
	}

	return ret;
    }

    /**
     * Reads the data of a time series only until the first record, without downloading the remaining ones
     *
     * @param path
     * @return the timestamp of the first record
     * @throws GSException
     */
    private Optional<Date> readFirstTimestamp(String path) throws GSException {

	try {

	    throttle();

	    HttpResponse<InputStream> response = downloader.downloadResponse(endpoint + path);

	    try (InputStream stream = response.body()) {

		if (stream == null || response.statusCode() != 200) {
		    return Optional.empty();
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

		String line;
		while ((line = reader.readLine()) != null) {
		    Optional<Date> timestamp = parseTimestamp(line);
		    if (timestamp.isPresent()) {
			return timestamp;
		    }
		}
	    }

	    return Optional.empty();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("Enhydris " + path + " request failed", e);
	}
    }

    /**
     * @param path
     * @return the response, or empty if the resource is not available
     * @throws GSException
     */
    private Optional<String> getOptional(String path) throws GSException {

	return getOptionalURL(endpoint + path);
    }

    /**
     * @param url
     * @return the response, or empty if the resource is not available
     * @throws GSException
     */
    private Optional<String> getOptionalURL(String url) throws GSException {

	try {

	    throttle();

	    HttpResponse<InputStream> response = downloader.downloadResponse(url);

	    String text;
	    try (InputStream stream = response.body()) {
		text = stream == null ? "" : IOUtils.toString(stream, StandardCharsets.UTF_8);
	    }

	    if (response.statusCode() == 404) {
		return Optional.empty();
	    }

	    if (response.statusCode() != 200) {
		throw createException("Enhydris " + url + " returned HTTP " + response.statusCode(), null);
	    }

	    return text.isBlank() ? Optional.empty() : Optional.of(text);

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("Enhydris " + url + " request failed", e);
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
     * @return the endpoint with the "api/" path and the trailing slash
     */
    static String normalizeEndpoint(String endpoint) {

	if (endpoint == null || endpoint.trim().isEmpty()) {
	    return DEFAULT_ENDPOINT;
	}

	String normalized = endpoint.trim();
	if (!normalized.endsWith("/")) {
	    normalized += "/";
	}

	if (!normalized.endsWith("/" + API_PATH)) {
	    normalized += API_PATH;
	}

	return normalized;
    }
}
