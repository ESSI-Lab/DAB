package eu.essi_lab.accessor.ukeafloodmonitoring;

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
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Client of the Real Time flood-monitoring API (Beta) of the Environment Agency (England)
 * (https://environment.data.gov.uk/flood-monitoring/doc/reference).<br>
 * A time series is a measure of a station (e.g. "1029TH-level-stage-i-15_min-mASD"), listed with its latest reading by
 * /id/measures. Readings are published only for a rolling window of about four weeks (from the start of the day 29 days ago, at the
 * time of writing); older readings are only available as daily archive files of the whole country (/archive/readings-full-YYYY-MM-DD.csv),
 * not usable per series. The validated archive of the series is published by the Hydrology API (see the accessor-uk-hydrology
 * module).<br>
 * Each request returns at most {@link #MAX_LIMIT} readings, longer periods are split in more requests
 *
 * @author boldrini
 */
public class EAFloodMonitoringClient {

    public static final String DEFAULT_ENDPOINT = "https://environment.data.gov.uk/flood-monitoring/";

    /**
     * Number of days of readings certainly published by the API (the actual window starts at the beginning of the day 29 days ago)
     */
    public static final int WINDOW_DAYS = 28;

    /**
     * Readings older than this number of days are never requested
     */
    static final int MAX_WINDOW_DAYS = 31;

    /**
     * Maximum number of items returned by a request
     */
    static final int MAX_LIMIT = 10000;

    /**
     * Number of days of readings requested at once (split if the readings are more than {@link #MAX_LIMIT})
     */
    static final int DAYS_PER_REQUEST = 32;

    static final String UNKNOWN_UNIT = "---";

    // keys of the measures
    public static final String ID = "@id";
    public static final String NOTATION = "notation";
    public static final String LABEL = "label";
    public static final String PARAMETER = "parameter";
    public static final String PARAMETER_NAME = "parameterName";
    public static final String QUALIFIER = "qualifier";
    public static final String UNIT_NAME = "unitName";
    public static final String VALUE_TYPE = "valueType";
    public static final String PERIOD = "period";
    public static final String DATUM_TYPE = "datumType";
    public static final String STATION = "station";
    public static final String STATION_REFERENCE = "stationReference";
    public static final String LATEST_READING = "latestReading";

    // keys of the readings
    public static final String DATE_TIME = "dateTime";
    public static final String VALUE = "value";

    // keys of the stations
    public static final String LAT = "lat";
    public static final String LON = "long";
    public static final String RIVER_NAME = "riverName";
    public static final String CATCHMENT_NAME = "catchmentName";
    public static final String TOWN = "town";
    public static final String DATE_OPENED = "dateOpened";
    public static final String STATUS = "status";
    public static final String RLOI_ID = "RLOIid";
    public static final String WISKI_ID = "wiskiID";
    public static final String GRID_REFERENCE = "gridReference";

    // keys added by the client and by the connector
    public static final String STATION_LABEL = "stationLabel";
    public static final String STATION_STATUS = "stationStatus";
    public static final String COORDINATES_FROM_GRID_REFERENCE = "coordinatesFromGridReference";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    private static final String[] STATION_KEYS = new String[] { RIVER_NAME, CATCHMENT_NAME, TOWN, DATE_OPENED, RLOI_ID, WISKI_ID,
	    GRID_REFERENCE };

    private final String endpoint;
    private final Downloader downloader;
    private int limit = MAX_LIMIT;

    /**
     *
     */
    public EAFloodMonitoringClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public EAFloodMonitoringClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 5);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    /**
     * Retrieves the time series of the given parameters. Measures without readings in the rolling window are discarded
     *
     * @param parameters the parameters to harvest, as "parameter" (e.g. "level") or "parameter:qualifier" (e.g.
     *        "temperature:Water")
     * @param stations the station references to harvest, empty for all the stations
     * @return the series sorted by identifier
     * @throws GSException
     */
    public List<EAFloodMonitoringEntity> retrieveSeries(Collection<String> parameters, Collection<String> stations) throws GSException {

	HashMap<String, JSONObject> stationMap = new HashMap<>();

	JSONArray stationList = getItems(get("id/stations"));
	for (int i = 0; i < stationList.length(); i++) {
	    JSONObject station = stationList.getJSONObject(i);
	    stationMap.put(station.optString(ID), station);
	}

	Set<String> codes = new LinkedHashSet<>();
	for (String parameter : parameters) {
	    codes.add(parameter.split(":")[0].trim());
	}

	Date now = new Date();
	List<EAFloodMonitoringEntity> ret = new ArrayList<>();

	for (String code : codes) {

	    JSONArray measures = getItems(get("id/measures?parameter=" + URLEncoder.encode(code, StandardCharsets.UTF_8)));

	    for (int i = 0; i < measures.length(); i++) {

		JSONObject measure = measures.getJSONObject(i);

		if (!matches(parameters, measure)) {
		    continue;
		}
		if (!stations.isEmpty() && !stations.contains(optString(measure, STATION_REFERENCE))) {
		    continue;
		}

		createSeries(measure, stationMap.get(measure.optString(STATION)), now).ifPresent(ret::add);
	    }
	}

	ret.sort(Comparator.comparing(EAFloodMonitoringEntity::getSeriesId));

	return ret;
    }

    /**
     * @param parameters the parameters, as "parameter" or "parameter:qualifier"
     * @param measure
     * @return true if the measure matches one of the parameters
     */
    static boolean matches(Collection<String> parameters, JSONObject measure) {

	String parameter = optString(measure, PARAMETER);
	String qualifier = optString(measure, QUALIFIER);

	for (String filter : parameters) {

	    String[] split = filter.split(":", 2);
	    if (!split[0].trim().equalsIgnoreCase(parameter)) {
		continue;
	    }
	    if (split.length == 1 || split[1].trim().equalsIgnoreCase(qualifier)) {
		return true;
	    }
	}

	return false;
    }

    /**
     * @param measure the measure, as listed by /id/measures
     * @param station the station of the measure, possibly null
     * @param now
     * @return the series, or empty if the measure has no readings in the rolling window
     */
    static Optional<EAFloodMonitoringEntity> createSeries(JSONObject measure, JSONObject station, Date now) {

	Optional<Date> latest = getLatestReadingDate(measure);
	Date begin = getWindowBegin(now);

	if (latest.isEmpty() || latest.get().before(begin)) {
	    return Optional.empty();
	}

	JSONObject object = new JSONObject();
	for (String key : new String[] { ID, NOTATION, LABEL, PARAMETER, PARAMETER_NAME, QUALIFIER, UNIT_NAME, VALUE_TYPE, DATUM_TYPE,
		STATION, STATION_REFERENCE }) {
	    String value = optString(measure, key);
	    if (!value.isEmpty()) {
		object.put(key, value);
	    }
	}
	if (measure.has(PERIOD)) {
	    object.put(PERIOD, measure.optInt(PERIOD));
	}

	if (station != null) {
	    addStation(object, station);
	}

	object.put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(begin));
	object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(latest.get()));

	return Optional.of(new EAFloodMonitoringEntity(object));
    }

    /**
     * @param object
     * @param station
     */
    private static void addStation(JSONObject object, JSONObject station) {

	String label = optString(station, LABEL);
	if (!label.isEmpty()) {
	    object.put(STATION_LABEL, label);
	}

	for (String key : STATION_KEYS) {
	    String value = optString(station, key);
	    if (!value.isEmpty()) {
		object.put(key, value);
	    }
	}

	String status = optString(station, STATUS);
	if (!status.isEmpty()) {
	    // e.g. http://environment.data.gov.uk/flood-monitoring/def/core/statusActive
	    object.put(STATION_STATUS, status.replaceAll(".*/status", ""));
	}

	Optional<BigDecimal> lat = optDecimal(station, LAT);
	Optional<BigDecimal> lon = optDecimal(station, LON);

	if (lat.isPresent() && lon.isPresent()) {

	    object.put(LAT, lat.get());
	    object.put(LON, lon.get());

	} else {

	    EAFloodMonitoringGridReference.toLatLon(optString(station, GRID_REFERENCE)).ifPresent(latLon -> {
		object.put(LAT, BigDecimal.valueOf(latLon[0]).setScale(6, RoundingMode.HALF_UP));
		object.put(LON, BigDecimal.valueOf(latLon[1]).setScale(6, RoundingMode.HALF_UP));
		object.put(COORDINATES_FROM_GRID_REFERENCE, true);
	    });
	}
    }

    /**
     * @param measure
     * @return the time of the latest reading of the measure
     */
    static Optional<Date> getLatestReadingDate(JSONObject measure) {

	JSONObject latest = measure.optJSONObject(LATEST_READING);
	if (latest == null) {
	    return Optional.empty();
	}

	return parseDate(latest.optString(DATE_TIME, null));
    }

    /**
     * @param now
     * @return the beginning of the rolling window of the readings certainly published by the API
     */
    public static Date getWindowBegin(Date now) {

	Instant day = now.toInstant().truncatedTo(ChronoUnit.DAYS);
	return Date.from(day.minus(WINDOW_DAYS, ChronoUnit.DAYS));
    }

    /**
     * @param seriesId the measure notation
     * @return the time of the latest reading of the measure, or empty if not available
     * @throws GSException
     */
    public Optional<Date> retrieveLatestObservation(String seriesId) throws GSException {

	Optional<String> text = getOptional("id/measures/" + encode(seriesId));
	if (text.isEmpty()) {
	    return Optional.empty();
	}

	JSONObject object = new JSONObject(text.get());
	JSONObject measure = object.optJSONObject("items");
	if (measure == null) {
	    JSONArray items = object.optJSONArray("items");
	    if (items == null || items.isEmpty()) {
		return Optional.empty();
	    }
	    measure = items.getJSONObject(0);
	}

	return getLatestReadingDate(measure);
    }

    /**
     * @param limit the maximum number of readings requested at once (default {@link #MAX_LIMIT}, the maximum allowed by the API)
     */
    void setLimit(int limit) {

	this.limit = Math.min(limit, MAX_LIMIT);
    }

    /**
     * Retrieves the readings of the given period, requesting {@link #DAYS_PER_REQUEST} days at a time (fewer if the readings are
     * more than {@link #MAX_LIMIT}). Periods older than {@link #MAX_WINDOW_DAYS} days are not requested
     *
     * @param seriesId the measure notation
     * @param begin
     * @param end
     * @return the valid readings
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(String seriesId, Date begin, Date end) throws GSException {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	Date oldest = Date.from(Instant.now().truncatedTo(ChronoUnit.DAYS).minus(MAX_WINDOW_DAYS, ChronoUnit.DAYS));
	Date from = begin.before(oldest) ? oldest : begin;

	if (from.after(end)) {
	    return ret;
	}

	LocalDate first = LocalDate.ofInstant(from.toInstant(), ZoneOffset.UTC);
	LocalDate last = LocalDate.ofInstant(end.toInstant(), ZoneOffset.UTC);

	LocalDate day = first;
	while (!day.isAfter(last)) {

	    LocalDate chunkEnd = day.plusDays(DAYS_PER_REQUEST - 1);
	    if (chunkEnd.isAfter(last)) {
		chunkEnd = last;
	    }

	    retrieveDays(seriesId, day, chunkEnd, from, end, ret);

	    day = chunkEnd.plusDays(1);
	}

	return ret;
    }

    /**
     * Retrieves the readings of the given days (startdate and enddate are both included), splitting the days in two halves if the
     * response is truncated
     *
     * @param seriesId
     * @param startDay
     * @param endDay
     * @param begin
     * @param end
     * @param values
     * @throws GSException
     */
    private void retrieveDays(String seriesId, LocalDate startDay, LocalDate endDay, Date begin, Date end, TreeMap<Date, BigDecimal> values)
	    throws GSException {

	String path = "id/measures/" + encode(seriesId) + "/readings?_sorted&_limit=" + limit + "&startdate=" + startDay + "&enddate="
		+ endDay;

	Optional<String> text = getOptional(path);
	if (text.isEmpty()) {
	    return;
	}

	JSONArray items = getItems(text.get());

	if (items.length() >= limit) {

	    long days = ChronoUnit.DAYS.between(startDay, endDay) + 1;
	    if (days > 1) {
		LocalDate middle = startDay.plusDays(days / 2 - 1);
		retrieveDays(seriesId, startDay, middle, begin, end, values);
		retrieveDays(seriesId, middle.plusDays(1), endDay, begin, end, values);
		return;
	    }

	    GSLoggerFactory.getLogger(getClass()).warn("Readings of {} on {} truncated to {}", seriesId, startDay, limit);
	}

	parseReadings(items, begin, end, values);
    }

    /**
     * @param items the readings
     * @param begin
     * @param end
     * @param values the map where the valid readings of the given period are added
     */
    static void parseReadings(JSONArray items, Date begin, Date end, TreeMap<Date, BigDecimal> values) {

	for (int i = 0; i < items.length(); i++) {

	    JSONObject reading = items.optJSONObject(i);
	    if (reading == null) {
		continue;
	    }

	    Optional<Date> date = parseDate(reading.optString(DATE_TIME, null));
	    if (date.isEmpty() || date.get().before(begin) || date.get().after(end)) {
		continue;
	    }

	    Object value = reading.opt(VALUE);
	    // a few readings have more values (e.g. duplicated readings), they are discarded as not reliable
	    if (value instanceof Number) {
		values.put(date.get(), new BigDecimal(value.toString()));
	    }
	}
    }

    /**
     * @param text a response of the API
     * @return the items, as an array also when the response describes a single item
     */
    static JSONArray getItems(String text) {

	JSONObject object = new JSONObject(text);

	JSONArray items = object.optJSONArray("items");
	if (items != null) {
	    return items;
	}

	JSONArray ret = new JSONArray();
	JSONObject item = object.optJSONObject("items");
	if (item != null) {
	    ret.put(item);
	}

	return ret;
    }

    /**
     * Some properties of the stations are arrays (e.g. stations made of more sites): the first value is returned
     *
     * @param object
     * @param key
     * @return the value, or an empty string if not set
     */
    static String optString(JSONObject object, String key) {

	Object value = object.opt(key);

	if (value instanceof JSONArray) {
	    JSONArray array = (JSONArray) value;
	    value = array.isEmpty() ? null : array.opt(0);
	}

	if (value == null || value == JSONObject.NULL) {
	    return "";
	}

	return value.toString().trim();
    }

    /**
     * @param object
     * @param key
     * @return
     */
    private static Optional<BigDecimal> optDecimal(JSONObject object, String key) {

	try {
	    String value = optString(object, key);
	    return value.isEmpty() ? Optional.empty() : Optional.of(new BigDecimal(value));
	} catch (NumberFormatException e) {
	    return Optional.empty();
	}
    }

    /**
     * @param text e.g. "2026-10-27T14:00:00Z"
     * @return
     */
    static Optional<Date> parseDate(String text) {

	if (text == null || text.isEmpty() || text.equals("null")) {
	    return Optional.empty();
	}

	try {
	    return Optional.of(Date.from(Instant.parse(text)));
	} catch (DateTimeParseException e) {
	    return ISO8601DateTimeUtils.parseISO8601ToDate(text);
	}
    }

    /**
     * @param seriesId
     * @return
     */
    private static String encode(String seriesId) {

	return URLEncoder.encode(seriesId, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * @param path
     * @return
     * @throws GSException
     */
    private String get(String path) throws GSException {

	return getOptional(path).orElseThrow(() -> createException("EA flood-monitoring " + path + " not available", null));
    }

    /**
     * @param path
     * @return the response, or empty if the resource is not available
     * @throws GSException
     */
    private Optional<String> getOptional(String path) throws GSException {

	try {

	    HttpResponse<InputStream> response = downloader.downloadResponse(endpoint + path);

	    String text;
	    try (InputStream stream = response.body()) {
		text = stream == null ? "" : IOUtils.toString(stream, StandardCharsets.UTF_8);
	    }

	    if (response.statusCode() == 404) {
		return Optional.empty();
	    }

	    if (response.statusCode() != 200) {
		throw createException("EA flood-monitoring " + path + " returned HTTP " + response.statusCode(), null);
	    }

	    return text.isBlank() ? Optional.empty() : Optional.of(text);

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("EA flood-monitoring " + path + " request failed", e);
	}
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
