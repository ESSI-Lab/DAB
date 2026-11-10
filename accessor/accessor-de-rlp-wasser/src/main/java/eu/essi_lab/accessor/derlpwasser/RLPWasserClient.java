package eu.essi_lab.accessor.derlpwasser;

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
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
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
 * Client of the public API of the flood portal (Hochwassermeldedienst) of the Landesamt für Umwelt Rheinland-Pfalz
 * (https://hochwasser.rlp.de).<br>
 * The configuration endpoint lists the measurement sites (gauges of the state, of municipalities and of neighbouring agencies)
 * with their UTM 32N coordinates (EPSG:25832), rivers and operators. The measurement site endpoint returns the water level (W) and,
 * where available, the discharge (Q) of the last 5 days (15 minutes values), together with the forecasts of some sites (not
 * harvested). The API keeps only this rolling window and does not accept time parameters.<br>
 * A time series is identified by a site number and a parameter (see {@link RLPWasserParameter}).
 *
 * @author boldrini
 */
public class RLPWasserClient {

    public static final String DEFAULT_ENDPOINT = "https://hochwasser.rlp.de/api/v1/";

    static final String API_PATH = "api/v1/";
    static final String CONFIG_PATH = "config";
    static final String SITE_PATH = "measurement-site/";

    /**
     * Width of the rolling window published by the measurement site endpoint
     */
    public static final long ROLLING_WINDOW = TimeUnit.DAYS.toMillis(5);

    // keys of the configuration
    static final String MEASUREMENT_SITES = "measurementsite";
    static final String OPERATORS = "operators";
    static final String RIVERS = "rivers";
    static final String MEASUREMENTS = "measurements";

    // keys of the measurement sites (from the configuration)
    public static final String NUMBER = "number";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String IS_EXTERNAL = "isExternal";
    public static final String IS_SEA_SITE = "isSeaSite";
    public static final String CATCHMENT_AREA = "catchmentArea";
    public static final String ELEVATION = "elevation";
    public static final String RIVER_KILOMETER = "riverKilometer";
    public static final String EASTING = "easting";
    public static final String NORTHING = "northing";
    public static final String RIVER_MUNICIPAL_NAME = "riverMunicipalName";

    // keys added by the client
    public static final String RIVER = "river";
    public static final String OPERATOR = "operator";
    public static final String OPERATOR_URL = "operatorUrl";
    public static final String COUNTRY = "country";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String HINT = "hint";
    public static final String PARAMETER = "parameter";
    public static final String UNIT = "unit";
    public static final String SERIES_ID = "seriesId";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    /**
     * Type of the measurement sites operated by municipalities and districts
     */
    public static final String MUNICIPAL_TYPE = "municipal";

    private static final String SERIES_ID_SEPARATOR = "|";

    private static final CRS UTM_32N = new EPSGCRS(25832);

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public RLPWasserClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public RLPWasserClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setConnectionTimeout(TimeUnit.SECONDS, 30);
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 2);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    /**
     * @return the measurement sites listed by the configuration, sorted by number
     * @throws GSException
     */
    public List<JSONObject> retrieveSites() throws GSException {

	return parseSites(new JSONObject(get(CONFIG_PATH)));
    }

    /**
     * @param config the configuration of the portal
     * @return the measurement sites, sorted by number, with river, operator and WGS84 coordinates
     */
    static List<JSONObject> parseSites(JSONObject config) {

	JSONObject sites = config.optJSONObject(MEASUREMENT_SITES);
	JSONObject operators = Optional.ofNullable(config.optJSONObject(OPERATORS)).orElse(new JSONObject());
	JSONObject rivers = Optional.ofNullable(config.optJSONObject(RIVERS)).orElse(new JSONObject());

	List<JSONObject> ret = new ArrayList<>();

	if (sites == null) {
	    return ret;
	}

	for (String key : sites.keySet()) {

	    JSONObject site = sites.optJSONObject(key);
	    if (site == null) {
		continue;
	    }

	    String number = site.optString(NUMBER, key).trim();
	    if (number.isEmpty()) {
		continue;
	    }

	    JSONObject object = new JSONObject();
	    object.put(NUMBER, number);
	    object.put(NAME, site.optString(NAME, number).trim());
	    object.put(TYPE, site.optString(TYPE, ""));
	    object.put(IS_EXTERNAL, site.optBoolean(IS_EXTERNAL, false));
	    object.put(IS_SEA_SITE, site.optBoolean(IS_SEA_SITE, false));

	    putPositive(site, object, CATCHMENT_AREA);
	    putPositive(site, object, ELEVATION);
	    putPositive(site, object, RIVER_KILOMETER);

	    String river = getRiverName(site, rivers);
	    if (!river.isEmpty()) {
		object.put(RIVER, river);
	    }

	    JSONObject operator = operators.optJSONObject(key);
	    if (operator != null) {
		String operatorName = operator.optString("op_name", "").trim();
		if (!operatorName.isEmpty() && !operatorName.equals("null")) {
		    object.put(OPERATOR, operatorName);
		}
		String operatorUrl = operator.optString("op_url", "").trim();
		if (!operatorUrl.isEmpty() && !operatorUrl.equals("null")) {
		    object.put(OPERATOR_URL, operatorUrl);
		}
	    }

	    object.put(COUNTRY, getCountry(object.optString(OPERATOR, "")));

	    if (site.has(EASTING) && site.has(NORTHING) && !site.isNull(EASTING) && !site.isNull(NORTHING)) {
		object.put(EASTING, site.getDouble(EASTING));
		object.put(NORTHING, site.getDouble(NORTHING));
		toWGS84(site.getDouble(EASTING), site.getDouble(NORTHING)).ifPresent(latLon -> {
		    object.put(LAT, latLon.getKey());
		    object.put(LON, latLon.getValue());
		});
	    }

	    ret.add(object);
	}

	ret.sort(Comparator.comparing(o -> o.getString(NUMBER)));

	return ret;
    }

    /**
     * @param site
     * @param object
     * @param key
     */
    private static void putPositive(JSONObject site, JSONObject object, String key) {

	if (site.has(key) && !site.isNull(key)) {
	    double value = site.optDouble(key, 0);
	    if (value > 0) {
		object.put(key, site.get(key));
	    }
	}
    }

    /**
     * @param site
     * @param rivers
     * @return the name of the first river of the site, or the name of the water body given by the municipality
     */
    private static String getRiverName(JSONObject site, JSONObject rivers) {

	JSONArray siteRivers = site.optJSONArray(RIVERS);
	if (siteRivers != null && !siteRivers.isEmpty()) {
	    JSONObject river = rivers.optJSONObject(siteRivers.optString(0));
	    if (river != null && !river.optString(NAME, "").isBlank()) {
		return river.getString(NAME).trim();
	    }
	}

	String municipalName = site.optString(RIVER_MUNICIPAL_NAME, "").trim();
	return municipalName.equals("null") ? "" : municipalName;
    }

    /**
     * The portal also shows gauges of the neighbouring countries: their country is guessed from the operator
     *
     * @param operator
     * @return
     */
    static String getCountry(String operator) {

	if (operator.startsWith("DREAL")) {
	    return "France";
	}
	if (operator.contains("Luxemburg")) {
	    return "Luxembourg";
	}
	if (operator.contains("Belgien")) {
	    return "Belgium";
	}
	if (operator.contains("Schweiz")) {
	    return "Switzerland";
	}

	return "Germany";
    }

    /**
     * @param easting UTM 32N (EPSG:25832) easting
     * @param northing UTM 32N (EPSG:25832) northing
     * @return latitude and longitude (WGS84)
     */
    static Optional<SimpleEntry<Double, Double>> toWGS84(double easting, double northing) {

	try {

	    SimpleEntry<Double, Double> latLon = CRSUtils.translatePoint(new SimpleEntry<>(easting, northing), UTM_32N,
		    CRS.EPSG_4326());

	    double lat = round(latLon.getKey());
	    double lon = round(latLon.getValue());

	    if (Math.abs(lat) > 90 || Math.abs(lon) > 180) {
		return Optional.empty();
	    }

	    return Optional.of(new SimpleEntry<>(lat, lon));

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(RLPWasserClient.class).warn("Unable to convert coordinates {} {}: {}", easting, northing,
		    e.getMessage());
	    return Optional.empty();
	}
    }

    /**
     * @param value
     * @return the value rounded to 6 decimals
     */
    private static double round(double value) {

	return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * @param number the site number, e.g. "26600128"
     * @return the response of the measurement site endpoint, or empty if not available
     * @throws GSException
     */
    public Optional<JSONObject> retrieveSiteData(String number) throws GSException {

	Optional<String> text = getOptional(SITE_PATH + number);
	if (text.isEmpty()) {
	    return Optional.empty();
	}

	try {

	    return Optional.of(new JSONObject(text.get()));

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).warn("Unable to parse measurement site {}: {}", number, e.getMessage());
	    return Optional.empty();
	}
    }

    /**
     * Creates the series of the given parameters measured at the site. Series without valid measurements are discarded
     *
     * @param site the site, as returned by {@link #parseSites(JSONObject)}
     * @param data the response of the measurement site endpoint
     * @param parameters
     * @return
     */
    static List<RLPWasserEntity> createSeries(JSONObject site, JSONObject data, Collection<RLPWasserParameter> parameters) {

	List<RLPWasserEntity> ret = new ArrayList<>();

	String number = site.getString(NUMBER);

	for (RLPWasserParameter parameter : parameters) {

	    TreeMap<Date, BigDecimal> values = parseMeasurements(data, parameter);
	    if (values.isEmpty()) {
		continue;
	    }

	    JSONObject object = new JSONObject(site.toString());

	    object.put(PARAMETER, parameter.name());
	    object.put(UNIT, parameter.getUnit(site.optBoolean(IS_SEA_SITE, false)));
	    object.put(SERIES_ID, buildSeriesId(number, parameter));
	    object.put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(values.firstKey()));
	    object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(values.lastKey()));

	    JSONObject hint = data.optJSONObject(HINT);
	    if (hint != null && !hint.optString("text", "").isBlank()) {
		object.put(HINT, hint.getString("text").trim());
	    }

	    ret.add(new RLPWasserEntity(object));
	}

	return ret;
    }

    /**
     * @param data the response of the measurement site endpoint
     * @param parameter
     * @return the valid measurements of the parameter (forecasts are ignored)
     */
    static TreeMap<Date, BigDecimal> parseMeasurements(JSONObject data, RLPWasserParameter parameter) {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	JSONObject series = data.optJSONObject(parameter.name());
	if (series == null) {
	    return ret;
	}

	JSONArray measurements = series.optJSONArray(MEASUREMENTS);
	if (measurements == null) {
	    return ret;
	}

	for (int i = 0; i < measurements.length(); i++) {

	    JSONObject measurement = measurements.optJSONObject(i);
	    if (measurement == null || measurement.isNull("y")) {
		continue;
	    }

	    Optional<Date> date = parseDate(measurement.optString("x", null));
	    BigDecimal value = measurement.optBigDecimal("y", null);

	    if (date.isPresent() && value != null) {
		ret.put(date.get(), value);
	    }
	}

	return ret;
    }

    /**
     * @param series
     * @return the first and the last valid measurement currently published for the series, or empty if none
     * @throws GSException
     */
    public Optional<SimpleEntry<Date, Date>> retrieveExtent(RLPWasserEntity series) throws GSException {

	TreeMap<Date, BigDecimal> values = retrieveObservations(series);
	if (values.isEmpty()) {
	    return Optional.empty();
	}

	return Optional.of(new SimpleEntry<>(values.firstKey(), values.lastKey()));
    }

    /**
     * @param series
     * @return all the valid measurements currently published for the series (the rolling window of the last days)
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(RLPWasserEntity series) throws GSException {

	RLPWasserParameter parameter = series.getParameter()
		.orElseThrow(() -> createException("Invalid RLP series " + series.getSeriesId(), null));

	Optional<JSONObject> data = retrieveSiteData(series.getSiteNumber());
	if (data.isEmpty()) {
	    return new TreeMap<>();
	}

	return parseMeasurements(data.get(), parameter);
    }

    /**
     * The API publishes the whole rolling window in a single response, which is filtered locally
     *
     * @param series
     * @param begin
     * @param end
     * @return the valid measurements of the given period
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(RLPWasserEntity series, Date begin, Date end) throws GSException {

	TreeMap<Date, BigDecimal> values = retrieveObservations(series);

	if (begin.after(end)) {
	    return new TreeMap<>();
	}

	return new TreeMap<>(values.subMap(begin, true, end, true));
    }

    /**
     * @param number
     * @param parameter
     * @return
     */
    static String buildSeriesId(String number, RLPWasserParameter parameter) {

	return number + SERIES_ID_SEPARATOR + parameter.name();
    }

    /**
     * @param seriesId e.g. "26600128|W"
     * @return the series identified by the given identifier, or null if not valid
     */
    public static RLPWasserEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	int index = seriesId.indexOf(SERIES_ID_SEPARATOR);
	if (index <= 0) {
	    return null;
	}

	String number = seriesId.substring(0, index).trim();
	if (!number.chars().allMatch(Character::isDigit)) {
	    return null;
	}

	Optional<RLPWasserParameter> parameter = RLPWasserParameter.decode(seriesId.substring(index + 1));
	if (parameter.isEmpty()) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, buildSeriesId(number, parameter.get()));
	object.put(NUMBER, number);
	object.put(PARAMETER, parameter.get().name());

	return new RLPWasserEntity(object);
    }

    /**
     * @param text e.g. "2026-10-27T20:00:00Z"
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
     * @param path
     * @return
     * @throws GSException
     */
    private String get(String path) throws GSException {

	return getOptional(path).orElseThrow(() -> createException("RLP " + path + " not available", null));
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
		throw createException("RLP " + path + " returned HTTP " + response.statusCode(), null);
	    }

	    return text.isBlank() ? Optional.empty() : Optional.of(text);

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("RLP " + path + " request failed", e);
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
     * @param endpoint e.g. "https://hochwasser.rlp.de/api/v1/" or "https://hochwasser.rlp.de"
     * @return the base URL of the API, ending with "/"
     */
    static String normalizeEndpoint(String endpoint) {

	if (endpoint == null || endpoint.isBlank()) {
	    return DEFAULT_ENDPOINT;
	}

	String normalized = endpoint.trim();
	if (!normalized.endsWith("/")) {
	    normalized += "/";
	}

	if (!normalized.contains("/api/")) {
	    normalized += API_PATH;
	}

	return normalized;
    }
}
