package eu.essi_lab.accessor.lvlvgmc;

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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
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
 * Client of the hydrological observations of the Latvian Environment, Geology and Meteorology Centre (LVGMC), published as
 * CC0-1.0 open data in the dataset "Hidrometeoroloģiskie novērojumi" of the Latvian Open Data Portal (CKAN,
 * https://data.gov.lv/dati/dataset/40d80be5-0c09-47c4-80f3-fad4bec19f33).<br>
 * The dataset resources (station list, parameter list, operational data of the last 48 hours updated hourly and archive data
 * of the last 365 days updated daily) are discovered by name with the CKAN package_show action (falling back to the default
 * resource identifiers) and they are queried with the CKAN DataStore SQL API (datastore_search_sql), as suggested by the provider.
 * In this way the CSV files (the archive is about 62 MB) are never downloaded: a full harvest costs 4 requests and the data of
 * a series are requested in windows of {@link #MAX_WINDOW} (the DataStore returns at most 32000 rows per request).<br>
 * A time series is identified by a station identifier and a parameter abbreviation, e.g. "HD073548|LIMEN"
 *
 * @author boldrini
 */
public class LVGMCClient {

    /**
     * The Latvian Open Data Portal page of the dataset; the CKAN API endpoint and the dataset identifier are derived from it
     */
    public static final String DEFAULT_ENDPOINT = "https://data.gov.lv/dati/dataset/40d80be5-0c09-47c4-80f3-fad4bec19f33";

    public static final String DATASET_ID = "40d80be5-0c09-47c4-80f3-fad4bec19f33";
    public static final String DATASET_NAME = "hidrometeorologiskie-noverojumi";

    static final String API_PATH = "/api/3/action/";
    static final String DATASET_PATH = "/dataset/";

    /**
     * The dataset resources used by the client
     */
    public enum Resource {

	/**
	 * Hydrological stations
	 */
	STATIONS("hidro_stacijas", "Hidroloģiskās stacijas", "93fd5e2c-20c4-496e-a920-ff29bda20383"), //
	/**
	 * Hydrological parameters
	 */
	PARAMETERS("hidro_parametri", "Hidroloģiskie parametri", "714ab60d-d93e-4403-b76d-2fb865d15d63"), //
	/**
	 * Hydrological observations of the last 48 hours, updated hourly
	 */
	OPERATIONAL("hidro_operativie_dati", "Hidroloģiskie operatīvie dati", "de5f06e9-6f44-497d-8ec2-72a2483608e8"), //
	/**
	 * Hydrological observations of the last 365 days, updated daily
	 */
	ARCHIVE("hidro_arhiva_dati", "Hidroloģiskie arhīva dati", "a90de53b-e8b6-4cda-97c4-ecb86fbafc2d");

	private final String fileName;
	private final String name;
	private final String defaultId;

	/**
	 * @param fileName
	 * @param name
	 * @param defaultId
	 */
	private Resource(String fileName, String name, String defaultId) {

	    this.fileName = fileName;
	    this.name = name;
	    this.defaultId = defaultId;
	}

	/**
	 * @return the CSV file name without extension, e.g. "hidro_arhiva_dati"
	 */
	public String getFileName() {

	    return fileName;
	}

	/**
	 * @return the resource name, e.g. "Hidroloģiskie arhīva dati"
	 */
	public String getName() {

	    return name;
	}

	/**
	 * @return the resource identifier at the time of writing
	 */
	public String getDefaultId() {

	    return defaultId;
	}
    }

    /**
     * Maximum width of the time window of a single data request (about 4400 hourly values)
     */
    static final long MAX_WINDOW = TimeUnit.DAYS.toMillis(183);

    /**
     * Time to live of the discovered resource identifiers
     */
    static final long RESOURCES_TTL = TimeUnit.HOURS.toMillis(1);

    /**
     * Minimum interval between two requests
     */
    static final long MIN_REQUEST_INTERVAL = 200;

    private static final Object REQUEST_LOCK = new Object();
    private static long lastRequestTime;

    private static final Map<String, SimpleEntry<Long, Map<Resource, String>>> RESOURCES_CACHE = new ConcurrentHashMap<>();

    /**
     * Pattern of the station identifiers and of the parameter abbreviations, checked before using them in the SQL queries
     */
    private static final Pattern CODE_PATTERN = Pattern.compile("[A-Za-z0-9_\\-]+");
    private static final Pattern RESOURCE_ID_PATTERN = Pattern.compile("[0-9a-fA-F\\-]{36}");

    // columns of the DataStore resources
    public static final String STATION_ID = "STATION_ID";
    public static final String NAME = "NAME";
    public static final String BEGIN_DATE = "BEGIN_DATE";
    public static final String END_DATE = "END_DATE";
    public static final String LONGITUDE = "GEOGR1";
    public static final String LATITUDE = "GEOGR2";
    public static final String ELEVATION = "ELEVATION";
    public static final String ABBREVIATION = "ABBREVIATION";
    public static final String EN_DESCRIPTION = "EN_DESCRIPTION";
    public static final String LV_DESCRIPTION = "LV_DESCRIPTION";
    public static final String MEASUREMENT_UNIT = "MEASUREMENT_UNIT";
    public static final String DATETIME = "DATETIME";
    public static final String VALUE = "VALUE";

    // keys added by the client
    public static final String SERIES_ID = "seriesId";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    /**
     * End date of the stations still in operation
     */
    static final String OPEN_END_DATE = "3999";

    private static final String SERIES_ID_SEPARATOR = "|";

    private static final DateTimeFormatter SQL_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final String apiEndpoint;
    private final String datasetId;
    private final Downloader downloader;

    /**
     *
     */
    public LVGMCClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint the dataset page (e.g. {@link #DEFAULT_ENDPOINT}) or the CKAN API endpoint (e.g.
     *        "https://data.gov.lv/dati/api/3/action/", in this case the default dataset is used)
     */
    public LVGMCClient(String endpoint) {

	String[] parsed = parseEndpoint(endpoint);
	this.apiEndpoint = parsed[0];
	this.datasetId = parsed[1];
	this.downloader = new Downloader();
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 2);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 20);
    }

    /**
     * @return the CKAN API endpoint, e.g. "https://data.gov.lv/dati/api/3/action/"
     */
    public String getApiEndpoint() {

	return apiEndpoint;
    }

    /**
     * @return the dataset identifier or name
     */
    public String getDatasetId() {

	return datasetId;
    }

    /**
     * @return the hydrological stations, by identifier
     * @throws GSException
     */
    public Map<String, JSONObject> retrieveStations() throws GSException {

	return retrieveTable(Resource.STATIONS, STATION_ID);
    }

    /**
     * @return the hydrological parameters, by abbreviation
     * @throws GSException
     */
    public Map<String, JSONObject> retrieveParameters() throws GSException {

	return retrieveTable(Resource.PARAMETERS, ABBREVIATION);
    }

    /**
     * Retrieves the time series having observations in the archive or in the operational data, with their temporal extent (a
     * single aggregation query)
     *
     * @param stationIds the station identifiers, empty for all
     * @param abbreviations the parameter abbreviations, empty for all
     * @return the series, sorted by identifier
     * @throws GSException
     */
    public List<LVGMCEntity> retrieveSeries(Collection<String> stationIds, Collection<String> abbreviations) throws GSException {

	Map<String, JSONObject> stations = retrieveStations();
	Map<String, JSONObject> parameters = retrieveParameters();

	Map<Resource, String> resources = getResources();

	String sql = "SELECT \"" + STATION_ID + "\", \"" + ABBREVIATION + "\", MIN(t) AS b, MAX(t) AS e FROM ("//
		+ selectTimes(resources.get(Resource.ARCHIVE), null) + " UNION ALL "//
		+ selectTimes(resources.get(Resource.OPERATIONAL), null) + ") u GROUP BY \"" + STATION_ID + "\", \"" + ABBREVIATION
		+ "\"";

	JSONArray records = sql(sql);

	TreeMap<String, LVGMCEntity> ret = new TreeMap<>();

	for (int i = 0; i < records.length(); i++) {

	    JSONObject record = records.getJSONObject(i);

	    String stationId = record.optString(STATION_ID);
	    String abbreviation = record.optString(ABBREVIATION);

	    if ((!stationIds.isEmpty() && !stationIds.contains(stationId))
		    || (!abbreviations.isEmpty() && !abbreviations.contains(abbreviation))) {
		continue;
	    }

	    Optional<Date> begin = parseDate(record.optString("b", null));
	    Optional<Date> end = parseDate(record.optString("e", null));
	    if (begin.isEmpty() || end.isEmpty()) {
		continue;
	    }

	    JSONObject station = stations.getOrDefault(stationId, new JSONObject().put(STATION_ID, stationId));
	    JSONObject parameter = parameters.getOrDefault(abbreviation, new JSONObject().put(ABBREVIATION, abbreviation));

	    LVGMCEntity series = createSeries(station, parameter, begin.get(), end.get());
	    ret.put(series.getSeriesId(), series);
	}

	return new ArrayList<>(ret.values());
    }

    /**
     * @param station
     * @param parameter
     * @param begin
     * @param end
     * @return
     */
    static LVGMCEntity createSeries(JSONObject station, JSONObject parameter, Date begin, Date end) {

	JSONObject object = new JSONObject();

	String stationId = station.optString(STATION_ID);
	String abbreviation = parameter.optString(ABBREVIATION);

	object.put(STATION_ID, stationId);
	object.put(NAME, station.optString(NAME, stationId));

	if (!station.isNull(LATITUDE) && !station.isNull(LONGITUDE) && station.has(LATITUDE) && station.has(LONGITUDE)) {
	    object.put(LATITUDE, station.get(LATITUDE));
	    object.put(LONGITUDE, station.get(LONGITUDE));
	}

	if (station.has(ELEVATION) && !station.isNull(ELEVATION)) {
	    object.put(ELEVATION, station.get(ELEVATION));
	}

	parseDate(station.optString(BEGIN_DATE, null)).//
		ifPresent(d -> object.put(BEGIN_DATE, ISO8601DateTimeUtils.getISO8601DateTime(d)));

	String stationEnd = station.optString(END_DATE, "");
	if (!stationEnd.isEmpty() && !stationEnd.startsWith(OPEN_END_DATE)) {
	    parseDate(stationEnd).ifPresent(d -> object.put(END_DATE, ISO8601DateTimeUtils.getISO8601DateTime(d)));
	}

	object.put(ABBREVIATION, abbreviation);
	object.put(EN_DESCRIPTION, parameter.optString(EN_DESCRIPTION, ""));
	object.put(LV_DESCRIPTION, parameter.optString(LV_DESCRIPTION, ""));
	object.put(MEASUREMENT_UNIT, parameter.optString(MEASUREMENT_UNIT, ""));

	object.put(SERIES_ID, buildSeriesId(stationId, abbreviation));
	object.put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(begin));
	object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(end));

	return new LVGMCEntity(object);
    }

    /**
     * Retrieves the current first and last observation of the given series (a single aggregation query)
     *
     * @param series
     * @return the extent, empty if the series has no observations
     * @throws GSException
     */
    public Optional<SimpleEntry<Date, Date>> retrieveExtent(LVGMCEntity series) throws GSException {

	Map<Resource, String> resources = getResources();

	String sql = "SELECT MIN(t) AS b, MAX(t) AS e FROM (" //
		+ selectTimes(resources.get(Resource.ARCHIVE), series) + " UNION ALL "//
		+ selectTimes(resources.get(Resource.OPERATIONAL), series) + ") u";

	JSONArray records = sql(sql);
	if (records.length() == 0) {
	    return Optional.empty();
	}

	Optional<Date> begin = parseDate(records.getJSONObject(0).optString("b", null));
	Optional<Date> end = parseDate(records.getJSONObject(0).optString("e", null));

	if (begin.isEmpty() || end.isEmpty()) {
	    return Optional.empty();
	}

	return Optional.of(new SimpleEntry<>(begin.get(), end.get()));
    }

    /**
     * Retrieves the observations of the given period, in windows of {@link #MAX_WINDOW}. Both the archive and the operational
     * data are queried; where they overlap the archive values are kept, since they passed additional quality checks
     *
     * @param series
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(LVGMCEntity series, Date begin, Date end) throws GSException {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	if (begin.after(end)) {
	    return ret;
	}

	Map<Resource, String> resources = getResources();

	for (long from = begin.getTime(); from <= end.getTime(); from += MAX_WINDOW + 1000) {

	    Date windowBegin = new Date(from);
	    Date windowEnd = new Date(Math.min(from + MAX_WINDOW, end.getTime()));

	    String sql = "SELECT \"" + DATETIME + "\", \"" + VALUE + "\", p FROM ("//
		    + selectValues(resources.get(Resource.ARCHIVE), series, windowBegin, windowEnd, 1) + " UNION ALL "//
		    + selectValues(resources.get(Resource.OPERATIONAL), series, windowBegin, windowEnd, 2) + ") u ORDER BY \""
		    + DATETIME + "\", p";

	    parseObservations(sql(sql), ret);
	}

	return ret;
    }

    /**
     * @param records the records with the {@link #DATETIME} and {@link #VALUE} columns, sorted by priority for the same time
     * @param target the observations to which the non null values are added, the first value of each time is kept
     */
    static void parseObservations(JSONArray records, TreeMap<Date, BigDecimal> target) {

	for (int i = 0; i < records.length(); i++) {

	    JSONObject record = records.optJSONObject(i);
	    if (record == null || record.isNull(VALUE)) {
		continue;
	    }

	    Optional<Date> date = parseDate(record.optString(DATETIME, null));
	    BigDecimal value = record.optBigDecimal(VALUE, null);

	    if (date.isPresent() && value != null) {
		target.putIfAbsent(date.get(), value);
	    }
	}
    }

    /**
     * @param resourceId
     * @param series the series, null for all
     * @return
     */
    static String selectTimes(String resourceId, LVGMCEntity series) {

	return "SELECT \"" + STATION_ID + "\", \"" + ABBREVIATION + "\", \"" + DATETIME + "\" AS t FROM \"" + checkResourceId(resourceId)
		+ "\"" + (series == null ? "" : " WHERE " + whereSeries(series));
    }

    /**
     * @param resourceId
     * @param series
     * @param begin
     * @param end
     * @param priority
     * @return
     */
    static String selectValues(String resourceId, LVGMCEntity series, Date begin, Date end, int priority) {

	return "SELECT \"" + DATETIME + "\", \"" + VALUE + "\", " + priority + " AS p FROM \"" + checkResourceId(resourceId) + "\" WHERE "
		+ whereSeries(series) + " AND \"" + DATETIME + "\" BETWEEN '" + formatSQLTime(begin) + "' AND '" + formatSQLTime(end)
		+ "'";
    }

    /**
     * @param series
     * @return
     */
    private static String whereSeries(LVGMCEntity series) {

	return "\"" + STATION_ID + "\" = '" + checkCode(series.getStationId()) + "' AND \"" + ABBREVIATION + "\" = '"
		+ checkCode(series.getAbbreviation()) + "'";
    }

    /**
     * @param code
     * @return the code, if valid
     * @throws IllegalArgumentException if the code is not valid
     */
    static String checkCode(String code) {

	if (code == null || !CODE_PATTERN.matcher(code).matches()) {
	    throw new IllegalArgumentException("Invalid LVGMC code: " + code);
	}

	return code;
    }

    /**
     * @param id
     * @return the identifier, if valid
     * @throws IllegalArgumentException if the identifier is not valid
     */
    static String checkResourceId(String id) {

	if (id == null || !RESOURCE_ID_PATTERN.matcher(id).matches()) {
	    throw new IllegalArgumentException("Invalid LVGMC resource identifier: " + id);
	}

	return id;
    }

    /**
     * @param date
     * @return e.g. "2026-10-01T00:00:00"
     */
    static String formatSQLTime(Date date) {

	return SQL_TIME_FORMAT.format(date.toInstant().atZone(ZoneOffset.UTC));
    }

    /**
     * @param resource
     * @param key
     * @return the records of the given small resource, by key
     * @throws GSException
     */
    private Map<String, JSONObject> retrieveTable(Resource resource, String key) throws GSException {

	String id = getResources().get(resource);

	JSONObject result = getResult("datastore_search?resource_id=" + encode(checkResourceId(id)) + "&limit=1000");

	JSONArray records = result.optJSONArray("records");

	Map<String, JSONObject> ret = new LinkedHashMap<>();
	for (int i = 0; records != null && i < records.length(); i++) {
	    JSONObject record = records.getJSONObject(i);
	    ret.put(record.optString(key), record);
	}

	return ret;
    }

    /**
     * @return the identifiers of the dataset resources, discovered by name and cached for {@link #RESOURCES_TTL}
     * @throws GSException
     */
    public Map<Resource, String> getResources() throws GSException {

	String key = apiEndpoint + datasetId;

	SimpleEntry<Long, Map<Resource, String>> cached = RESOURCES_CACHE.get(key);
	if (cached != null && System.currentTimeMillis() - cached.getKey() < RESOURCES_TTL) {
	    return cached.getValue();
	}

	Map<Resource, String> resources;

	try {

	    JSONObject result = getResult("package_show?id=" + encode(datasetId));
	    resources = findResources(result.optJSONArray("resources"));

	} catch (GSException e) {

	    GSLoggerFactory.getLogger(getClass()).warn("Unable to discover the LVGMC resources, using the default ones");
	    resources = findResources(null);
	}

	RESOURCES_CACHE.put(key, new SimpleEntry<>(System.currentTimeMillis(), resources));

	return resources;
    }

    /**
     * @param resources the resources of the package_show response
     * @return the identifiers of the resources found by file name or by name, or the default identifiers
     */
    static Map<Resource, String> findResources(JSONArray resources) {

	Map<Resource, String> ret = new HashMap<>();

	for (Resource resource : Resource.values()) {

	    String id = resource.getDefaultId();

	    for (int i = 0; resources != null && i < resources.length(); i++) {

		JSONObject object = resources.optJSONObject(i);
		if (object == null || !object.optBoolean("datastore_active", true)) {
		    continue;
		}

		String url = object.optString("url", "");
		String fileName = url.substring(url.lastIndexOf('/') + 1).replaceFirst("\\.[A-Za-z]+$", "");

		if (fileName.equalsIgnoreCase(resource.getFileName()) || object.optString("name", "").trim().equalsIgnoreCase(resource.getName())) {
		    id = object.optString("id", id);
		    break;
		}
	    }

	    ret.put(resource, id);
	}

	return ret;
    }

    /**
     * @param sql
     * @return the records
     * @throws GSException
     */
    private JSONArray sql(String sql) throws GSException {

	JSONObject result = getResult("datastore_search_sql?sql=" + encode(sql));

	if (result.optBoolean("records_truncated", false)) {
	    GSLoggerFactory.getLogger(getClass()).warn("LVGMC records truncated: {}", sql);
	}

	return Optional.ofNullable(result.optJSONArray("records")).orElse(new JSONArray());
    }

    /**
     * @param path
     * @return the result of the CKAN action
     * @throws GSException
     */
    private JSONObject getResult(String path) throws GSException {

	String url = apiEndpoint + path;

	try {

	    throttle();

	    HttpResponse<InputStream> response = downloader.downloadResponse(url);

	    String text;
	    try (InputStream stream = response.body()) {
		text = stream == null ? "" : IOUtils.toString(stream, StandardCharsets.UTF_8);
	    }

	    if (response.statusCode() != 200) {
		throw createException("LVGMC request " + path + " returned HTTP " + response.statusCode() + ": "
			+ text.substring(0, Math.min(500, text.length())), null);
	    }

	    JSONObject object = new JSONObject(text);
	    if (!object.optBoolean("success", false) || object.optJSONObject("result") == null) {
		throw createException("LVGMC request " + path + " failed: " + object.opt("error"), null);
	    }

	    return object.getJSONObject("result");

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("LVGMC request " + path + " failed", e);
	}
    }

    /**
     * @param value
     * @return
     */
    private static String encode(String value) {

	return URLEncoder.encode(value, StandardCharsets.UTF_8);
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
     * @param stationId
     * @param abbreviation
     * @return
     */
    static String buildSeriesId(String stationId, String abbreviation) {

	return stationId + SERIES_ID_SEPARATOR + abbreviation;
    }

    /**
     * @param seriesId
     * @return the series identified by the given identifier, or null if not valid
     */
    public static LVGMCEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	int index = seriesId.indexOf(SERIES_ID_SEPARATOR);
	if (index <= 0) {
	    return null;
	}

	String stationId = seriesId.substring(0, index);
	String abbreviation = seriesId.substring(index + 1);

	if (!CODE_PATTERN.matcher(stationId).matches() || !CODE_PATTERN.matcher(abbreviation).matches()) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, buildSeriesId(stationId, abbreviation));
	object.put(STATION_ID, stationId);
	object.put(ABBREVIATION, abbreviation);

	return new LVGMCEntity(object);
    }

    /**
     * @param text a UTC time of the DataStore, e.g. "2026-10-26T22:00:00", or of the CSV files, e.g. "2026.10.26 22:00:00"
     * @return
     */
    static Optional<Date> parseDate(String text) {

	if (text == null || text.isEmpty() || text.equals("null")) {
	    return Optional.empty();
	}

	String normalized = text.trim().replace('.', '-').replace(' ', 'T');

	try {

	    return Optional.of(Date.from(LocalDateTime.parse(normalized, SQL_TIME_FORMAT).toInstant(ZoneOffset.UTC)));

	} catch (DateTimeParseException e) {

	    return ISO8601DateTimeUtils.parseISO8601ToDate(text);
	}
    }

    /**
     * @param endpoint
     * @return the CKAN API endpoint and the dataset identifier
     */
    static String[] parseEndpoint(String endpoint) {

	String normalized = endpoint == null || endpoint.isBlank() ? DEFAULT_ENDPOINT : endpoint.trim();

	int api = normalized.indexOf(API_PATH.substring(0, API_PATH.length() - 1));
	if (api > 0) {

	    String datasetId = DATASET_ID;
	    int id = normalized.indexOf("id=");
	    if (id > 0) {
		datasetId = normalized.substring(id + 3).split("&")[0];
	    }

	    return new String[] { normalized.substring(0, api) + API_PATH, datasetId };
	}

	int dataset = normalized.indexOf(DATASET_PATH);
	if (dataset > 0) {

	    String datasetId = normalized.substring(dataset + DATASET_PATH.length()).split("[/?#]")[0];
	    return new String[] { normalized.substring(0, dataset) + API_PATH, datasetId.isEmpty() ? DATASET_ID : datasetId };
	}

	String base = normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
	return new String[] { base + API_PATH, DATASET_ID };
    }

    /**
     * @param message
     * @param cause
     * @return
     */
    private GSException createException(String message, Exception cause) {

	GSLoggerFactory.getLogger(getClass()).error(message);

	// the overload taking only the cause uses its message as description, and fails when there is no cause
	return GSException.createException(//
		getClass(), //
		message, //
		null, //
		ErrorInfo.ERRORTYPE_SERVICE, //
		ErrorInfo.SEVERITY_ERROR, //
		getClass().getSimpleName() + "_ERROR", //
		cause);
    }
}
