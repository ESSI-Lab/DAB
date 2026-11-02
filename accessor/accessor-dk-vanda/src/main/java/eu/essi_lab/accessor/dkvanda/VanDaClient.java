package eu.essi_lab.accessor.dkvanda;

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
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.CRSUtils;
import eu.essi_lab.model.resource.data.EPSGCRS;

/**
 * Client of the Hydro API of the VanDa database of Danmarks Miljøportal (https://vandah.miljoeportal.dk/api/swagger/index.html).
 * <br>
 * The station list describes, for each station, its measurement points and, for each measurement point, the examinations made
 * there (examination type, parameter, unit, time of the first and of the latest result). A time series is identified by a
 * station, a measurement point, an examination type and a parameter (see {@link #buildSeriesId(String, int, int, int)}). The
 * whole archive of each series is available through the measurements endpoint, which is queried by time windows of at most
 * {@link #MAX_REQUEST_WINDOW} (high frequency series have about 4500 values per month, about 2 MB of JSON).<br>
 * Coordinates are provided in ETRS89 / UTM zone 32N (EPSG:25832) and are converted to WGS 84 latitude and longitude.
 *
 * @author boldrini
 */
public class VanDaClient {

    public static final String DEFAULT_ENDPOINT = "https://vandah.miljoeportal.dk/api/";

    static final String STATIONS_PATH = "stations";
    static final String MEASUREMENTS_PATH = "measurements/results/current";

    /**
     * Maximum time window of a single measurements request
     */
    static final long MAX_REQUEST_WINDOW = TimeUnit.DAYS.toMillis(31);

    /**
     * Placeholder dates used by VanDa for examinations without a real date (e.g. migrated gauge datum surveys)
     */
    static final String[] PLACEHOLDER_YEARS = new String[] { "1900-01-01", "1916-12-31" };

    // keys of the station list
    public static final String STATION_ID = "stationId";
    public static final String OPERATOR_STATION_ID = "operatorStationId";
    public static final String OLD_STATION_NUMBER = "oldStationNumber";
    public static final String STATION_NAME = "name";
    public static final String STATION_DESCRIPTION = "description";
    public static final String LOCATION_TYPE = "locationType";
    public static final String STATION_OWNER = "stationOwnerName";
    public static final String OPERATOR = "operatorName";
    public static final String CATCHMENT_AREA = "catchmentAreaKm2";
    public static final String LOCATION = "location";
    public static final String MEASUREMENT_POINTS = "measurementPoints";
    public static final String EXAMINATIONS = "examinations";

    // keys of the measurement points
    public static final String NUMBER = "number";
    public static final String MEASUREMENT_POINT_NAME = "name";
    public static final String MEASUREMENT_POINT_TYPE = "measurementPointType";
    public static final String MEASUREMENT_POINT_DESCRIPTION = "description";

    // keys of the examinations
    public static final String PARAMETER = "parameter";
    public static final String PARAMETER_SC = "parameterSc";
    public static final String EXAMINATION_TYPE = "examinationType";
    public static final String EXAMINATION_TYPE_SC = "examinationTypeSc";
    public static final String UNIT = "unit";
    public static final String FIRST_RESULT = "firstResult";
    public static final String LATEST_RESULT = "latestResult";

    // keys of the measurements
    static final String RESULTS = "results";
    static final String MEASUREMENT_DATE_TIME = "measurementDateTime";
    static final String RESULT = "result";

    // keys added by the client
    public static final String SERIES_ID = "seriesId";
    public static final String MEASUREMENT_POINT_NUMBER = "measurementPointNumber";
    public static final String MEASUREMENT_POINT_NAME_KEY = "measurementPointName";
    public static final String MEASUREMENT_POINT_DESCRIPTION_KEY = "measurementPointDescription";
    public static final String MEASUREMENT_POINTS_COUNT = "measurementPointsCount";
    public static final String LAT = "latitude";
    public static final String LON = "longitude";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    private static final String SERIES_ID_SEPARATOR = "|";

    private static final DateTimeFormatter REQUEST_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'")
	    .withZone(ZoneOffset.UTC);

    private static final CRS UTM_32N = new EPSGCRS(25832);

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public VanDaClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public VanDaClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 5);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    /**
     * Retrieves the time series of all the stations (a single request of about 5 MB). Series of the excluded examination types and
     * series without results are discarded
     *
     * @param excludedExaminationTypes the examination type codes to discard
     * @return the series sorted by identifier
     * @throws GSException
     */
    public List<VanDaEntity> retrieveSeries(Collection<Integer> excludedExaminationTypes) throws GSException {

	return retrieveSeries(null, excludedExaminationTypes);
    }

    /**
     * @param stationId the station identifier, or null for all the stations
     * @param excludedExaminationTypes the examination type codes to discard
     * @return the series sorted by identifier
     * @throws GSException
     */
    public List<VanDaEntity> retrieveSeries(String stationId, Collection<Integer> excludedExaminationTypes) throws GSException {

	String path = STATIONS_PATH;
	if (stationId != null) {
	    path += "?" + STATION_ID + "=" + URLEncoder.encode(stationId, StandardCharsets.UTF_8);
	}

	JSONArray stations = new JSONArray(get(path));

	List<VanDaEntity> ret = new ArrayList<>();
	for (int i = 0; i < stations.length(); i++) {
	    ret.addAll(createSeries(stations.getJSONObject(i), excludedExaminationTypes));
	}

	ret.sort(Comparator.comparing(VanDaEntity::getSeriesId));

	return ret;
    }

    /**
     * @param station an element of the station list
     * @param excludedExaminationTypes the examination type codes to discard
     * @return a series for each examination of each measurement point of the station, with results
     */
    static List<VanDaEntity> createSeries(JSONObject station, Collection<Integer> excludedExaminationTypes) {

	List<VanDaEntity> ret = new ArrayList<>();

	String stationId = station.optString(STATION_ID, "");
	JSONArray points = station.optJSONArray(MEASUREMENT_POINTS);
	if (stationId.isEmpty() || points == null) {
	    return ret;
	}

	Optional<SimpleEntry<Double, Double>> stationLatLon = toLatLon(station.optJSONObject(LOCATION));

	for (int i = 0; i < points.length(); i++) {

	    JSONObject point = points.getJSONObject(i);
	    JSONArray examinations = point.optJSONArray(EXAMINATIONS);
	    if (examinations == null || !point.has(NUMBER)) {
		continue;
	    }

	    int pointNumber = point.getInt(NUMBER);

	    Optional<SimpleEntry<Double, Double>> latLon = toLatLon(point.optJSONObject(LOCATION));
	    if (latLon.isEmpty()) {
		latLon = stationLatLon;
	    }

	    for (int j = 0; j < examinations.length(); j++) {

		JSONObject examination = examinations.getJSONObject(j);

		int examinationType = examination.optInt(EXAMINATION_TYPE_SC, -1);
		int parameter = examination.optInt(PARAMETER_SC, -1);
		if (examinationType < 0 || parameter < 0 || excludedExaminationTypes.contains(examinationType)) {
		    continue;
		}

		Optional<String> first = parseResultDate(examination.optString(FIRST_RESULT, null));
		Optional<String> latest = parseResultDate(examination.optString(LATEST_RESULT, null));
		if (first.isEmpty() || latest.isEmpty()) {
		    continue;
		}

		JSONObject object = new JSONObject();

		for (String key : new String[] { STATION_ID, OPERATOR_STATION_ID, OLD_STATION_NUMBER, STATION_NAME, STATION_DESCRIPTION,
			LOCATION_TYPE, STATION_OWNER, OPERATOR, CATCHMENT_AREA }) {
		    putString(object, key, station.opt(key));
		}

		object.put(MEASUREMENT_POINT_NUMBER, pointNumber);
		object.put(MEASUREMENT_POINTS_COUNT, points.length());
		putString(object, MEASUREMENT_POINT_NAME_KEY, point.opt(MEASUREMENT_POINT_NAME));
		putString(object, MEASUREMENT_POINT_TYPE, point.opt(MEASUREMENT_POINT_TYPE));
		putString(object, MEASUREMENT_POINT_DESCRIPTION_KEY, point.opt(MEASUREMENT_POINT_DESCRIPTION));

		object.put(EXAMINATION_TYPE_SC, examinationType);
		object.put(PARAMETER_SC, parameter);
		putString(object, EXAMINATION_TYPE, examination.opt(EXAMINATION_TYPE));
		putString(object, PARAMETER, examination.opt(PARAMETER));
		putString(object, UNIT, examination.opt(UNIT));

		latLon.ifPresent(ll -> {
		    object.put(LAT, ll.getKey());
		    object.put(LON, ll.getValue());
		});

		object.put(SERIES_ID, buildSeriesId(stationId, pointNumber, examinationType, parameter));
		object.put(BEGIN, first.get());
		object.put(END, latest.get());

		ret.add(new VanDaEntity(object));
	    }
	}

	return ret;
    }

    /**
     * Retrieves the current time of the first and of the latest result of the series from the station list (a request of a few KB)
     *
     * @param series
     * @return
     * @throws GSException
     */
    public Optional<SimpleEntry<Date, Date>> retrieveExtent(VanDaEntity series) throws GSException {

	List<VanDaEntity> list = retrieveSeries(series.getStationId(), new ArrayList<>());

	return list.stream().//
		filter(s -> s.getSeriesId().equals(series.getSeriesId())).//
		findFirst().//
		flatMap(s -> {
		    Optional<Date> begin = ISO8601DateTimeUtils.parseISO8601ToDate(s.getString(BEGIN));
		    Optional<Date> end = ISO8601DateTimeUtils.parseISO8601ToDate(s.getString(END));
		    return begin.isPresent() && end.isPresent() ? Optional.of(new SimpleEntry<>(begin.get(), end.get())) : Optional.empty();
		});
    }

    /**
     * Retrieves the valid observations of the given period, splitting the period in windows of at most {@link #MAX_REQUEST_WINDOW}
     *
     * @param series
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(VanDaEntity series, Date begin, Date end) throws GSException {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	for (SimpleEntry<Date, Date> window : splitPeriod(begin, end)) {

	    String path = MEASUREMENTS_PATH + "?" + //
		    STATION_ID + "=" + URLEncoder.encode(series.getStationId(), StandardCharsets.UTF_8) + //
		    "&" + MEASUREMENT_POINT_NUMBER + "=" + series.getMeasurementPointNumber() + //
		    "&" + EXAMINATION_TYPE_SC + "=" + series.getExaminationTypeCode() + //
		    "&" + PARAMETER_SC + "=" + series.getParameterCode() + //
		    "&from=" + formatDate(window.getKey()) + //
		    "&to=" + formatDate(window.getValue());

	    ret.putAll(parseMeasurements(new JSONArray(get(path)), series));
	}

	return new TreeMap<>(ret.subMap(begin, true, end, true));
    }

    /**
     * @param begin
     * @param end
     * @return consecutive windows covering the period (the request dates have a minute resolution and are inclusive)
     */
    static List<SimpleEntry<Date, Date>> splitPeriod(Date begin, Date end) {

	List<SimpleEntry<Date, Date>> ret = new ArrayList<>();

	long minute = TimeUnit.MINUTES.toMillis(1);
	long from = begin.getTime() - begin.getTime() % minute;
	long to = end.getTime() % minute == 0 ? end.getTime() : end.getTime() - end.getTime() % minute + minute;

	do {
	    long windowEnd = Math.min(from + MAX_REQUEST_WINDOW, to);
	    ret.add(new SimpleEntry<>(new Date(from), new Date(windowEnd)));
	    from = windowEnd + minute;
	} while (from <= to);

	return ret;
    }

    /**
     * @param response the response of the measurements endpoint
     * @param series
     * @return the valid values of the series
     */
    static TreeMap<Date, BigDecimal> parseMeasurements(JSONArray response, VanDaEntity series) {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	for (int i = 0; i < response.length(); i++) {

	    JSONArray results = response.getJSONObject(i).optJSONArray(RESULTS);
	    if (results == null) {
		continue;
	    }

	    for (int j = 0; j < results.length(); j++) {

		JSONObject result = results.getJSONObject(j);

		if (result.optInt(MEASUREMENT_POINT_NUMBER, series.getMeasurementPointNumber()) != series.getMeasurementPointNumber()
			|| result.optInt(EXAMINATION_TYPE_SC, series.getExaminationTypeCode()) != series.getExaminationTypeCode()
			|| result.optInt(PARAMETER_SC, series.getParameterCode()) != series.getParameterCode()) {
		    continue;
		}

		Optional<Date> date = parseDate(result.optString(MEASUREMENT_DATE_TIME, null));
		BigDecimal value = result.isNull(RESULT) ? null : result.optBigDecimal(RESULT, null);

		if (date.isPresent() && value != null) {
		    ret.put(date.get(), value);
		}
	    }
	}

	return ret;
    }

    /**
     * @param location e.g. {"x":547271.3133,"y":6231639.0415,"srid":"25832"}
     * @return the latitude and the longitude, if available
     */
    static Optional<SimpleEntry<Double, Double>> toLatLon(JSONObject location) {

	if (location == null || !location.has("x") || !location.has("y") || location.isNull("x") || location.isNull("y")) {
	    return Optional.empty();
	}

	String srid = location.optString("srid", "25832");
	double x = location.getDouble("x");
	double y = location.getDouble("y");

	try {

	    if (srid.equals("4326")) {
		return Optional.of(new SimpleEntry<>(y, x));
	    }

	    CRS source = srid.equals("25832") ? UTM_32N : new EPSGCRS(Integer.parseInt(srid));

	    SimpleEntry<Double, Double> latLon = CRSUtils.translatePoint(new SimpleEntry<>(x, y), source, CRS.EPSG_4326());

	    double lat = round(latLon.getKey());
	    double lon = round(latLon.getValue());

	    if (Math.abs(lat) > 90 || Math.abs(lon) > 180) {
		return Optional.empty();
	    }

	    return Optional.of(new SimpleEntry<>(lat, lon));

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(VanDaClient.class).warn("Unable to convert coordinates {}: {}", location, e.getMessage());
	    return Optional.empty();
	}
    }

    /**
     * @param value
     * @return the value rounded to 6 decimals (about 10 cm)
     */
    private static double round(double value) {

	return Math.round(value * 1000000.0) / 1000000.0;
    }

    /**
     * @param stationId
     * @param measurementPoint
     * @param examinationType
     * @param parameter
     * @return e.g. "21006844|1|25|1233"
     */
    static String buildSeriesId(String stationId, int measurementPoint, int examinationType, int parameter) {

	return stationId + SERIES_ID_SEPARATOR + measurementPoint + SERIES_ID_SEPARATOR + examinationType + SERIES_ID_SEPARATOR
		+ parameter;
    }

    /**
     * @param seriesId
     * @return the series identified by the given identifier, or null if not valid
     */
    public static VanDaEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	String[] split = seriesId.split("\\" + SERIES_ID_SEPARATOR, -1);
	if (split.length != 4 || split[0].isEmpty()) {
	    return null;
	}

	try {

	    int point = Integer.parseInt(split[1]);
	    int examinationType = Integer.parseInt(split[2]);
	    int parameter = Integer.parseInt(split[3]);

	    JSONObject object = new JSONObject();
	    object.put(SERIES_ID, buildSeriesId(split[0], point, examinationType, parameter));
	    object.put(STATION_ID, split[0]);
	    object.put(MEASUREMENT_POINT_NUMBER, point);
	    object.put(EXAMINATION_TYPE_SC, examinationType);
	    object.put(PARAMETER_SC, parameter);

	    return new VanDaEntity(object);

	} catch (NumberFormatException e) {

	    return null;
	}
    }

    /**
     * @param object
     * @param key
     * @param value
     */
    private static void putString(JSONObject object, String key, Object value) {

	if (value == null || value == JSONObject.NULL) {
	    return;
	}

	// e.g. "Volt "
	String text = value.toString().replace(' ', ' ').trim();
	if (!text.isEmpty()) {
	    object.put(key, text);
	}
    }

    /**
     * @param text e.g. "2024-01-10T12:15:00.00Z"
     * @return the date in ISO 8601 format, or empty if not set or a placeholder
     */
    static Optional<String> parseResultDate(String text) {

	if (text == null) {
	    return Optional.empty();
	}

	for (String placeholder : PLACEHOLDER_YEARS) {
	    if (text.startsWith(placeholder)) {
		return Optional.empty();
	    }
	}

	return parseDate(text).map(ISO8601DateTimeUtils::getISO8601DateTime);
    }

    /**
     * @param text e.g. "2026-10-26T12:00:00.00Z"
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
     * @param date
     * @return the date in the format of the requests, e.g. "2023-09-21T14:34Z"
     */
    static String formatDate(Date date) {

	return REQUEST_DATE_FORMAT.format(date.toInstant());
    }

    /**
     * @param path
     * @return
     * @throws GSException
     */
    private String get(String path) throws GSException {

	try {

	    HttpResponse<InputStream> response = downloader.downloadResponse(endpoint + path);

	    String text;
	    try (InputStream stream = response.body()) {
		text = stream == null ? "" : IOUtils.toString(stream, StandardCharsets.UTF_8);
	    }

	    if (response.statusCode() != 200) {
		throw createException("VanDa " + path + " returned HTTP " + response.statusCode() + ": " + text, null);
	    }

	    return text.isBlank() ? "[]" : text;

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("VanDa " + path + " request failed", e);
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
