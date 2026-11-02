package eu.essi_lab.accessor.ltlhmt;

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
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
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
 * Client of the hydrological observations of the Meteo LT API of the Lithuanian Hydrometeorological Service (LHMT)
 * (https://api.meteo.lt/).<br>
 * A time series is identified by a station code and a parameter (see {@link LHMTParameter}). Each station publishes two types of
 * observations:
 * <ul>
 * <li>measured: hourly water level and water temperature of the last 30 days, one request per day</li>
 * <li>historical: daily mean water level and discharge from 2000 until the end of the last consolidated year, one request per
 * month</li>
 * </ul>
 * Requests are limited to 180 per minute and to 20000 per day from the same IP address, so they are throttled
 *
 * @author boldrini
 */
public class LHMTClient {

    public static final String DEFAULT_ENDPOINT = "https://api.meteo.lt/v1/";

    static final String STATIONS_PATH = "hydro-stations";

    public static final String MEASURED = "measured";
    public static final String HISTORICAL = "historical";
    static final String LATEST = "latest";

    /**
     * Stations where the water temperature is not measured (no values in the 30 days ending on 2026-10-27, without ice cover)
     */
    public static final List<String> DEFAULT_STATIONS_WITHOUT_TEMPERATURE = Arrays.asList(//
	    "lazdenu-vms", //
	    "rusnes-skirvyte-vms", //
	    "smalininku-vms", //
	    "uostadvario-vms");

    /**
     * Width of the time window of the measured observations (30 days, plus a margin since the window starts at midnight)
     */
    static final long MEASURED_WINDOW = TimeUnit.DAYS.toMillis(32);

    /**
     * Minimum interval between two requests, to stay below the limit of 180 requests per minute
     */
    static final long MIN_REQUEST_INTERVAL = 400;

    private static final Object REQUEST_LOCK = new Object();
    private static long lastRequestTime;

    // keys of the API responses
    public static final String CODE = "code";
    public static final String NAME = "name";
    public static final String WATER_BODY = "waterBody";
    static final String COORDINATES = "coordinates";
    public static final String LAT = "latitude";
    public static final String LON = "longitude";
    static final String OBSERVATIONS = "observations";
    static final String DATA_RANGE = "observationsDataRange";
    static final String START_TIME = "startTimeUtc";
    static final String END_TIME = "endTimeUtc";
    static final String START_DATE = "startDateUtc";
    static final String END_DATE = "endDateUtc";
    static final String OBSERVATION_TIME = "observationTimeUtc";
    static final String OBSERVATION_DATE = "observationDateUtc";

    // keys added by the client and by the connector
    public static final String PARAMETER = "parameter";
    public static final String SERIES_ID = "seriesId";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    private static final String SERIES_ID_SEPARATOR = "|";

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public LHMTClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public LHMTClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 2);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 20);
    }

    /**
     * @return the hydrological stations, sorted by code
     * @throws GSException
     */
    public List<JSONObject> retrieveStations() throws GSException {

	JSONArray array = new JSONArray(get(STATIONS_PATH));

	List<JSONObject> ret = new ArrayList<>();
	for (int i = 0; i < array.length(); i++) {
	    ret.add(array.getJSONObject(i));
	}

	ret.sort((s1, s2) -> s1.optString(CODE).compareTo(s2.optString(CODE)));

	return ret;
    }

    /**
     * Retrieves the time series of the given parameters at the given station, assuming that the water temperature is not measured
     * at the {@link #DEFAULT_STATIONS_WITHOUT_TEMPERATURE}
     *
     * @param station
     * @param parameters
     * @return
     * @throws GSException
     * @see #retrieveSeries(JSONObject, Collection, Collection)
     */
    public List<LHMTEntity> retrieveSeries(JSONObject station, Collection<LHMTParameter> parameters) throws GSException {

	return retrieveSeries(station, parameters, DEFAULT_STATIONS_WITHOUT_TEMPERATURE);
    }

    /**
     * Retrieves the time series of the given parameters at the given station. A series is created if the station stores
     * observations of its type and (see {@link #hasSeries(String, LHMTParameter, JSONArray, Collection)}) the most recent ones (the
     * last 24 hours of the measured observations, the last stored 31 days of the historical ones) include values of the parameter
     *
     * @param station
     * @param parameters
     * @param stationsWithoutTemperature codes of the stations where the water temperature is not measured
     * @return
     * @throws GSException
     */
    public List<LHMTEntity> retrieveSeries(JSONObject station, Collection<LHMTParameter> parameters,
	    Collection<String> stationsWithoutTemperature) throws GSException {

	List<LHMTEntity> ret = new ArrayList<>();

	String code = station.optString(CODE);

	Set<String> types = new LinkedHashSet<>();
	parameters.forEach(p -> types.add(p.getObservationType()));

	for (String type : types) {

	    String path = getObservationsPath(code, type);

	    Optional<String> range = getOptional(path);
	    if (range.isEmpty()) {
		continue;
	    }

	    JSONObject dataRange = new JSONObject(range.get()).optJSONObject(DATA_RANGE);
	    if (dataRange == null) {
		continue;
	    }

	    boolean measured = type.equals(MEASURED);
	    Optional<Date> begin = parseDate(dataRange.optString(measured ? START_TIME : START_DATE, null));
	    Optional<Date> end = parseDate(dataRange.optString(measured ? END_TIME : END_DATE, null));
	    if (begin.isEmpty() || end.isEmpty()) {
		continue;
	    }

	    JSONArray latest = getOptional(path + "/" + LATEST).//
		    map(t -> new JSONObject(t).optJSONArray(OBSERVATIONS)).//
		    orElse(new JSONArray());

	    for (LHMTParameter parameter : parameters) {

		if (parameter.getObservationType().equals(type) && hasSeries(code, parameter, latest, stationsWithoutTemperature)) {

		    ret.add(createSeries(station, parameter, begin.get(), end.get()));
		}
	    }
	}

	return ret;
    }

    /**
     * The water temperature is not measured with ice cover, possibly for longer than the 30 days of the measured observations, so
     * missing recent values do not distinguish an ice covered station from a station without temperature sensor. For this
     * reason the water temperature series is created at all the stations not listed as stations without temperature, and also at
     * the listed ones if their most recent observations include values
     *
     * @param stationCode
     * @param parameter
     * @param latest the most recent observations of the type of the parameter
     * @param stationsWithoutTemperature codes of the stations where the water temperature is not measured
     * @return true if the station has a series of the given parameter
     */
    static boolean hasSeries(String stationCode, LHMTParameter parameter, JSONArray latest,
	    Collection<String> stationsWithoutTemperature) {

	if (!parseObservations(latest, parameter).isEmpty()) {
	    return true;
	}

	return parameter == LHMTParameter.WATER_TEMPERATURE && !stationsWithoutTemperature.contains(stationCode);
    }

    /**
     * @param station
     * @param parameter
     * @param begin
     * @param end
     * @return
     */
    static LHMTEntity createSeries(JSONObject station, LHMTParameter parameter, Date begin, Date end) {

	JSONObject object = new JSONObject();

	String code = station.optString(CODE);

	object.put(CODE, code);
	object.put(NAME, station.optString(NAME, code));
	object.put(WATER_BODY, station.optString(WATER_BODY, ""));

	JSONObject coordinates = station.optJSONObject(COORDINATES);
	if (coordinates != null && coordinates.has(LAT) && coordinates.has(LON)) {
	    object.put(LAT, coordinates.get(LAT));
	    object.put(LON, coordinates.get(LON));
	}

	object.put(PARAMETER, parameter.name());
	object.put(SERIES_ID, buildSeriesId(code, parameter));
	object.put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(begin));
	object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(end));

	return new LHMTEntity(object);
    }

    /**
     * Retrieves the valid observations of the given period. The measured observations are requested day by day (only for the days
     * of the last 30 days window), the historical ones month by month
     *
     * @param series
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(LHMTEntity series, Date begin, Date end) throws GSException {

	LHMTParameter parameter = series.getParameter()
		.orElseThrow(() -> createException("Invalid LHMT series " + series.getSeriesId(), null));

	String path = getObservationsPath(series.getStationCode(), parameter.getObservationType()) + "/";

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	if (parameter.isDaily()) {

	    YearMonth last = YearMonth.from(end.toInstant().atZone(ZoneOffset.UTC));

	    for (YearMonth month = YearMonth.from(begin.toInstant().atZone(ZoneOffset.UTC)); !month.isAfter(last); month = month
		    .plusMonths(1)) {

		ret.putAll(readObservations(path + MONTH_FORMAT.format(month), parameter));
	    }

	} else {

	    Date from = new Date(Math.max(begin.getTime(), System.currentTimeMillis() - MEASURED_WINDOW));

	    LocalDate last = end.toInstant().atZone(ZoneOffset.UTC).toLocalDate();

	    for (LocalDate day = from.toInstant().atZone(ZoneOffset.UTC).toLocalDate(); !day.isAfter(last); day = day.plusDays(1)) {

		ret.putAll(readObservations(path + day.toString(), parameter));
	    }
	}

	return new TreeMap<>(ret.subMap(begin, true, end, true));
    }

    /**
     * @param path
     * @param parameter
     * @return the observations, empty if not available
     * @throws GSException
     */
    private TreeMap<Date, BigDecimal> readObservations(String path, LHMTParameter parameter) throws GSException {

	Optional<String> text = getOptional(path);
	if (text.isEmpty()) {
	    return new TreeMap<>();
	}

	try {

	    return parseObservations(new JSONObject(text.get()).optJSONArray(OBSERVATIONS), parameter);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).warn("Unable to parse LHMT observations {}: {}", path, e.getMessage());
	    return new TreeMap<>();
	}
    }

    /**
     * @param observations
     * @param parameter
     * @return the non null values of the given parameter
     */
    static TreeMap<Date, BigDecimal> parseObservations(JSONArray observations, LHMTParameter parameter) {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	if (observations == null) {
	    return ret;
	}

	String timeKey = parameter.isDaily() ? OBSERVATION_DATE : OBSERVATION_TIME;

	for (int i = 0; i < observations.length(); i++) {

	    JSONObject observation = observations.optJSONObject(i);
	    if (observation == null || observation.isNull(parameter.getKey())) {
		continue;
	    }

	    Optional<Date> date = parseDate(observation.optString(timeKey, null));
	    BigDecimal value = observation.optBigDecimal(parameter.getKey(), null);

	    if (date.isPresent() && value != null) {
		ret.put(date.get(), value);
	    }
	}

	return ret;
    }

    /**
     * @param stationCode
     * @param parameter
     * @return
     */
    static String buildSeriesId(String stationCode, LHMTParameter parameter) {

	return stationCode + SERIES_ID_SEPARATOR + parameter.name();
    }

    /**
     * @param seriesId
     * @return the series identified by the given identifier, or null if not valid
     */
    public static LHMTEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	int index = seriesId.indexOf(SERIES_ID_SEPARATOR);
	if (index <= 0) {
	    return null;
	}

	String stationCode = seriesId.substring(0, index);
	Optional<LHMTParameter> parameter = LHMTParameter.decode(seriesId.substring(index + 1));
	if (parameter.isEmpty()) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, buildSeriesId(stationCode, parameter.get()));
	object.put(CODE, stationCode);
	object.put(PARAMETER, parameter.get().name());

	return new LHMTEntity(object);
    }

    /**
     * @param stationCode
     * @param observationType
     * @return e.g. "hydro-stations/nemajunu-vms/observations/measured"
     */
    static String getObservationsPath(String stationCode, String observationType) {

	return STATIONS_PATH + "/" + stationCode + "/" + OBSERVATIONS + "/" + observationType;
    }

    /**
     * @param text a UTC time, e.g. "2026-10-26 11:00:00", or a UTC date, e.g. "2024-12-31"
     * @return
     */
    static Optional<Date> parseDate(String text) {

	if (text == null || text.isEmpty() || text.equals("null")) {
	    return Optional.empty();
	}

	try {

	    if (text.length() == 10) {
		return Optional.of(Date.from(LocalDate.parse(text).atStartOfDay(ZoneOffset.UTC).toInstant()));
	    }

	    return Optional.of(Date.from(LocalDateTime.parse(text, TIME_FORMAT).toInstant(ZoneOffset.UTC)));

	} catch (DateTimeParseException e) {

	    return ISO8601DateTimeUtils.parseISO8601ToDate(text);
	}
    }

    /**
     * @param path
     * @return
     * @throws GSException
     */
    private String get(String path) throws GSException {

	return getOptional(path).orElseThrow(() -> createException("LHMT " + path + " not available", null));
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
		throw createException("LHMT " + path + " returned HTTP " + response.statusCode(), null);
	    }

	    return text.isBlank() ? Optional.empty() : Optional.of(text);

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("LHMT " + path + " request failed", e);
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
