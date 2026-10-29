package eu.essi_lab.accessor.essaihjucar;

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
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import eu.essi_lab.accessor.essaihjucar.SAIHJucarVariableType.SAIHJucarCategory;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.CRSUtils;
import eu.essi_lab.model.resource.data.EPSGCRS;

/**
 * Client of the web site of the SAIH (Sistema Automático de Información Hidrológica) of the Confederación Hidrográfica del Júcar
 * (https://saih.chj.es/).<br>
 * The variables are listed by the map pages of the site, as inline JavaScript arrays: river gauging stations (mapa-aforos, one
 * discharge variable per station), reservoirs (mapa-embalses, level, volume, inflow, total outflow and outflow to river) and
 * rain gauges (mapa-lluvias, whose variables are listed by the chart page of each station). Station coordinates are given in
 * ETRS89 / UTM zone 30N (EPSG:25830) and are converted to WGS 84.<br>
 * The five minutes values of a variable are returned by "admin/variables/valor/{idVariable}/{from}/{to}", where the dates are
 * local times of Spain (Europe/Madrid), the end being excluded. The archive starts in 2024; there is no documented limit on the
 * requested period, but long periods are slow, so they are split in windows of {@link #MAX_REQUEST_WINDOW}.
 *
 * @author boldrini
 */
public class SAIHJucarClient {

    public static final String DEFAULT_ENDPOINT = "https://saih.chj.es/";

    static final String GAUGES_PAGE = "mapa-aforos";
    static final String GAUGES_ARRAY = "aforos";
    static final String RESERVOIRS_PAGE = "mapa-embalses";
    static final String RESERVOIRS_ARRAY = "embalses";
    static final String RAIN_PAGE = "mapa-lluvias";
    static final String RAIN_ARRAY = "estaciones";
    static final String RAIN_CHART_PAGE = "chart-lluvia/";
    static final String RAIN_CHART_ARRAY = "varLluvia";
    static final String VALUES_PATH = "admin/variables/valor/";

    /**
     * Time zone of the dates of the requests
     */
    static final ZoneId PROVIDER_ZONE = ZoneId.of("Europe/Madrid");

    private static final DateTimeFormatter REQUEST_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
	    .withZone(PROVIDER_ZONE);

    /**
     * Lower bound of the search of the first observation (the archive starts in July 2024)
     */
    static final Date ARCHIVE_START = ISO8601DateTimeUtils.parseISO8601ToDate("2024-01-01T00:00:00Z").get();

    /**
     * Begin of the final check of the first observation (observations before the found one)
     */
    static final Date FIRST_CHECK_START = ISO8601DateTimeUtils.parseISO8601ToDate("2000-01-01T00:00:00Z").get();

    /**
     * Width of the windows used to probe the presence of observations
     */
    static final long PROBE_WINDOW = TimeUnit.DAYS.toMillis(1);

    /**
     * Width of the window used to look for the latest observation
     */
    static final long RECENT_WINDOW = TimeUnit.DAYS.toMillis(2);

    /**
     * Maximum width of the period of a single request (about 8640 values, 500 KB)
     */
    static final long MAX_REQUEST_WINDOW = TimeUnit.DAYS.toMillis(30);

    /**
     * Margin added to the requested periods, to be safe with the changes of the daylight saving time
     */
    static final long REQUEST_MARGIN = TimeUnit.HOURS.toMillis(1);

    /**
     * Bit of the state of a value set when the value is wrong (see checkErrorState in https://saih.chj.es/js/utils.js)
     */
    static final int ERROR_STATE_FLAG = 2;

    /**
     * Trust store with the root certificate of the Spanish FNMT-RCM (AC RAIZ FNMT-RCM, valid until 2030), which issued the
     * certificate of the site but is not included in the default trust store of the JVM. It is used together with the default
     * trust store
     */
    static final String TRUST_STORE = "saih-jucar-truststore.p12";
    static final String TRUST_STORE_PASSWORD = "saihjucar";

    /**
     * ETRS89 / UTM zone 30N
     */
    private static final CRS UTM_30N = new EPSGCRS(25830);

    // keys of the entries of the map pages (fldNCoordGPSLat and fldNCoordGPSLon are the UTM easting and northing)
    static final String MAP_UTM_X = "fldNCoordGPSLat";
    static final String MAP_UTM_Y = "fldNCoordGPSLon";
    static final String MAP_VARIABLE_NAME = "fldTNombreVariable";
    static final String MAP_STATION_TYPE = "fldTTipo";
    static final String CHART_VARIABLE_NAME = "nombreVariable";
    static final String CHART_UNIT = "unidadVariable";
    static final String CHART_DESCRIPTION = "fldTDescripcion";
    static final String CHART_CODE = "fldTCodigo";

    /**
     * Type of the gauging stations located at the outlet of a reservoir
     */
    static final String RESERVOIR_STATION_TYPE = "Em";

    // keys of the entries of the map pages, kept in the series
    public static final String VARIABLE_ID = "idVariable";
    public static final String STATION_ID = "idEstacionRemota";
    public static final String STATION_NAME = "fldTNombre";
    public static final String STATION_CODE = "fldTCodigo";
    public static final String TOWN = "fldTPoblacion";
    public static final String PROVINCE = "fldTProvincia";
    public static final String SUB_BASIN = "fldTSubCuenca";
    public static final String RESERVOIR_CAPACITY = "fldFVolumenNMN";
    public static final String RESERVOIR_SPILLWAY_LEVEL = "fldFCotaVertido";

    // keys added by the client and by the connector
    public static final String TYPE = "type";
    public static final String VARIABLE_NAME = "variableName";
    public static final String VARIABLE_CODE = "variableCode";
    public static final String VARIABLE_DESCRIPTION = "variableDescription";
    public static final String UNIT = "unit";
    public static final String UTM_X = "utmX";
    public static final String UTM_Y = "utmY";
    public static final String LAT = "latitude";
    public static final String LON = "longitude";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    /**
     * Keys of the variables of a reservoir entry, with their type and the name given by the provider
     */
    static final String[][] RESERVOIR_VARIABLES = new String[][] { //
	    { "idCotaEmbalse", SAIHJucarVariableType.RESERVOIR_LEVEL.name(), "COTA DE EMBALSE" }, //
	    { "idVolumenEmbalse", SAIHJucarVariableType.RESERVOIR_VOLUME.name(), "VOLUMEN DE EMBALSE" }, //
	    { "idCaudalRecibido", SAIHJucarVariableType.RESERVOIR_INFLOW.name(), "CAUDAL RECIBIDO" }, //
	    { "idCaudalSalida", SAIHJucarVariableType.RESERVOIR_OUTFLOW.name(), "CAUDAL TOTAL DE SALIDA" }, //
	    { "idCaudalSalidaRio", SAIHJucarVariableType.RESERVOIR_RIVER_OUTFLOW.name(), "CAUDAL SALIDA RÍO" } };

    /**
     * Keys of the station entries copied in the series
     */
    private static final String[] STATION_KEYS = new String[] { STATION_ID, STATION_NAME, STATION_CODE, TOWN, PROVINCE, SUB_BASIN };

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public SAIHJucarClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public SAIHJucarClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setConnectionTimeout(TimeUnit.SECONDS, 30);
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 5);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 502 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    /**
     * Retrieves the variables of the given categories. Reservoirs are read first, so that the outflow to river of a reservoir,
     * also listed among the gauging stations, is described as a reservoir variable. Each variable is returned only once
     *
     * @param categories
     * @return
     * @throws GSException
     */
    public List<SAIHJucarEntity> retrieveVariables(Collection<SAIHJucarCategory> categories) throws GSException {

	LinkedHashMap<String, SAIHJucarEntity> ret = new LinkedHashMap<>();

	if (categories.contains(SAIHJucarCategory.EMBALSES)) {
	    JSONArray reservoirs = parseInlineArray(get(RESERVOIRS_PAGE), RESERVOIRS_ARRAY);
	    for (int i = 0; i < reservoirs.length(); i++) {
		createReservoirVariables(reservoirs.getJSONObject(i)).forEach(v -> ret.putIfAbsent(v.getVariableId(), v));
	    }
	}

	if (categories.contains(SAIHJucarCategory.AFOROS)) {
	    JSONArray gauges = parseInlineArray(get(GAUGES_PAGE), GAUGES_ARRAY);
	    for (int i = 0; i < gauges.length(); i++) {
		createGaugeVariable(gauges.getJSONObject(i)).ifPresent(v -> ret.putIfAbsent(v.getVariableId(), v));
	    }
	}

	if (categories.contains(SAIHJucarCategory.LLUVIAS)) {
	    JSONArray stations = parseInlineArray(get(RAIN_PAGE), RAIN_ARRAY);
	    for (int i = 0; i < stations.length(); i++) {

		JSONObject station = stations.getJSONObject(i);
		String stationId = station.optString(STATION_ID, "");
		if (stationId.isEmpty()) {
		    continue;
		}

		Optional<String> page = getOptional(RAIN_CHART_PAGE + stationId);
		if (page.isEmpty()) {
		    GSLoggerFactory.getLogger(getClass()).warn("Chart page of rain gauge {} not available", stationId);
		    continue;
		}

		createRainVariable(station, parseInlineArray(page.get(), RAIN_CHART_ARRAY)).//
			ifPresent(v -> ret.putIfAbsent(v.getVariableId(), v));
	    }
	}

	return new ArrayList<>(ret.values());
    }

    /**
     * @param html
     * @param name
     * @return the JSON array assigned to the JavaScript variable with the given name ("let name = [...]"), or an empty array
     */
    static JSONArray parseInlineArray(String html, String name) {

	Matcher matcher = Pattern.compile("let\\s+" + Pattern.quote(name) + "\\s*=\\s*\\[").matcher(html);
	if (!matcher.find()) {
	    return new JSONArray();
	}

	try {
	    Object value = new JSONTokener(html.substring(matcher.end() - 1)).nextValue();
	    if (value instanceof JSONArray) {
		JSONArray array = (JSONArray) value;
		// trailing commas could produce null elements
		JSONArray ret = new JSONArray();
		for (int i = 0; i < array.length(); i++) {
		    if (array.opt(i) instanceof JSONObject || array.opt(i) instanceof JSONArray) {
			ret.put(array.get(i));
		    }
		}
		return ret;
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(SAIHJucarClient.class).warn("Unable to parse array {}: {}", name, e.getMessage());
	}

	return new JSONArray();
    }

    /**
     * @param gauge an entry of the gauging stations map page
     * @return the discharge variable of the station
     */
    static Optional<SAIHJucarEntity> createGaugeVariable(JSONObject gauge) {

	String variableId = gauge.optString(VARIABLE_ID, "");
	if (variableId.isEmpty()) {
	    return Optional.empty();
	}

	SAIHJucarVariableType type = RESERVOIR_STATION_TYPE.equals(gauge.optString(MAP_STATION_TYPE))
		? SAIHJucarVariableType.RESERVOIR_RIVER_OUTFLOW
		: SAIHJucarVariableType.RIVER_DISCHARGE;

	JSONObject object = createStation(gauge);
	object.put(VARIABLE_ID, variableId);
	object.put(TYPE, type.name());
	object.put(VARIABLE_NAME, gauge.optString(MAP_VARIABLE_NAME, ""));
	object.put(UNIT, type.getUnit());

	return Optional.of(new SAIHJucarEntity(object));
    }

    /**
     * @param reservoir an entry of the reservoirs map page
     * @return the variables of the reservoir
     */
    static List<SAIHJucarEntity> createReservoirVariables(JSONObject reservoir) {

	List<SAIHJucarEntity> ret = new ArrayList<>();

	for (String[] variable : RESERVOIR_VARIABLES) {

	    String variableId = reservoir.isNull(variable[0]) ? "" : reservoir.optString(variable[0], "");
	    if (variableId.isEmpty()) {
		continue;
	    }

	    JSONObject object = createStation(reservoir);
	    for (String key : new String[] { RESERVOIR_CAPACITY, RESERVOIR_SPILLWAY_LEVEL }) {
		if (reservoir.has(key) && !reservoir.isNull(key)) {
		    object.put(key, reservoir.get(key));
		}
	    }

	    SAIHJucarVariableType type = SAIHJucarVariableType.valueOf(variable[1]);
	    object.put(VARIABLE_ID, variableId);
	    object.put(TYPE, type.name());
	    object.put(VARIABLE_NAME, variable[2]);
	    object.put(UNIT, type.getUnit());

	    ret.add(new SAIHJucarEntity(object));
	}

	return ret;
    }

    /**
     * @param station an entry of the rain gauges map page
     * @param variables the variables listed by the chart page of the station
     * @return the rainfall intensity variable of the station
     */
    static Optional<SAIHJucarEntity> createRainVariable(JSONObject station, JSONArray variables) {

	for (int i = 0; i < variables.length(); i++) {

	    JSONObject variable = variables.getJSONObject(i);
	    String name = variable.optString(CHART_VARIABLE_NAME, "");
	    String variableId = variable.optString(VARIABLE_ID, "");

	    if (variableId.isEmpty() || !name.toUpperCase().startsWith("INTENSIDAD DE LLUVIA")) {
		continue;
	    }

	    SAIHJucarVariableType type = SAIHJucarVariableType.PRECIPITATION_INTENSITY;

	    JSONObject object = createStation(station);
	    object.put(VARIABLE_ID, variableId);
	    object.put(TYPE, type.name());
	    object.put(VARIABLE_NAME, name);
	    object.put(VARIABLE_CODE, variable.optString(CHART_CODE, ""));
	    object.put(VARIABLE_DESCRIPTION, variable.optString(CHART_DESCRIPTION, ""));
	    object.put(UNIT, variable.optString(CHART_UNIT, type.getUnit()));

	    return Optional.of(new SAIHJucarEntity(object));
	}

	return Optional.empty();
    }

    /**
     * @param entry
     * @return the description of the station of the given map page entry, with WGS 84 coordinates
     */
    private static JSONObject createStation(JSONObject entry) {

	JSONObject object = new JSONObject();

	for (String key : STATION_KEYS) {
	    String value = entry.isNull(key) ? "" : entry.optString(key, "").trim();
	    if (!value.isEmpty()) {
		object.put(key, value);
	    }
	}

	if (entry.has(MAP_UTM_X) && entry.has(MAP_UTM_Y) && !entry.isNull(MAP_UTM_X) && !entry.isNull(MAP_UTM_Y)) {

	    double x = entry.optDouble(MAP_UTM_X);
	    double y = entry.optDouble(MAP_UTM_Y);

	    object.put(UTM_X, x);
	    object.put(UTM_Y, y);

	    toLatLon(x, y).ifPresent(latLon -> {
		object.put(LAT, latLon.getKey());
		object.put(LON, latLon.getValue());
	    });
	}

	return object;
    }

    /**
     * @param x the ETRS89 / UTM zone 30N easting
     * @param y the ETRS89 / UTM zone 30N northing
     * @return the WGS 84 latitude and longitude, rounded to 6 decimals
     */
    static Optional<SimpleEntry<Double, Double>> toLatLon(double x, double y) {

	if (Double.isNaN(x) || Double.isNaN(y) || x == 0 || y == 0) {
	    return Optional.empty();
	}

	try {

	    SimpleEntry<Double, Double> latLon = CRSUtils.translatePoint(new SimpleEntry<>(x, y), UTM_30N, CRS.EPSG_4326());

	    double lat = round(latLon.getKey());
	    double lon = round(latLon.getValue());

	    if (Math.abs(lat) > 90 || Math.abs(lon) > 180) {
		return Optional.empty();
	    }

	    return Optional.of(new SimpleEntry<>(lat, lon));

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(SAIHJucarClient.class).warn("Unable to convert coordinates {} {}: {}", x, y, e.getMessage());
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
     * Retrieves the time of the first and of the last valid observations of a variable. The last observation is looked for in the
     * last {@link #RECENT_WINDOW}, the first one with a binary search of daily windows since {@link #ARCHIVE_START}, checking at
     * last that no observation precedes the found one (series with long gaps)
     *
     * @param variableId
     * @return the extent, or empty if the variable has no observations
     * @throws GSException
     */
    public Optional<SimpleEntry<Date, Date>> retrieveExtent(String variableId) throws GSException {

	long now = System.currentTimeMillis();
	Date recentBegin = new Date(now - RECENT_WINDOW);

	TreeMap<Date, BigDecimal> recent = retrieveValues(variableId, recentBegin, new Date(now + PROBE_WINDOW));

	Optional<Date> first = findFirstObservation(variableId, recentBegin, recent);
	if (first.isEmpty()) {
	    return Optional.empty();
	}

	Date last = recent.isEmpty() ? findLastObservation(variableId, first.get(), recentBegin) : recent.lastKey();

	return Optional.of(new SimpleEntry<>(first.get(), last));
    }

    /**
     * @param variableId
     * @return the time of the latest valid observation of the last {@link #RECENT_WINDOW}
     * @throws GSException
     */
    public Optional<Date> retrieveLatestObservation(String variableId) throws GSException {

	long now = System.currentTimeMillis();

	TreeMap<Date, BigDecimal> recent = retrieveValues(variableId, new Date(now - RECENT_WINDOW), new Date(now + PROBE_WINDOW));

	return recent.isEmpty() ? Optional.empty() : Optional.of(recent.lastKey());
    }

    /**
     * @param variableId
     * @param upper the end of the search
     * @param upperValues the values following the end of the search
     * @return
     * @throws GSException
     */
    private Optional<Date> findFirstObservation(String variableId, Date upper, TreeMap<Date, BigDecimal> upperValues)
	    throws GSException {

	long lo = ARCHIVE_START.getTime();
	long hi = upper.getTime();

	// values of the window starting at hi
	TreeMap<Date, BigDecimal> found = upperValues.isEmpty() ? null : upperValues;

	while (hi - lo > PROBE_WINDOW) {

	    long mid = lo + (hi - lo) / 2;

	    TreeMap<Date, BigDecimal> values = retrieveValues(variableId, new Date(mid), new Date(mid + PROBE_WINDOW));
	    if (values.isEmpty()) {
		lo = mid;
	    } else {
		hi = mid;
		found = values;
	    }
	}

	Date candidate = found == null ? upper : found.firstKey();

	// observations preceding the candidate (before the archive start or before a gap)
	TreeMap<Date, BigDecimal> before = retrieveValues(variableId, FIRST_CHECK_START, candidate);
	if (!before.isEmpty()) {
	    return Optional.of(before.firstKey());
	}

	return found == null ? Optional.empty() : Optional.of(found.firstKey());
    }

    /**
     * @param variableId
     * @param first the time of the first observation
     * @param upper the end of the search (no observations follow)
     * @return
     * @throws GSException
     */
    private Date findLastObservation(String variableId, Date first, Date upper) throws GSException {

	long lo = first.getTime();
	long hi = upper.getTime();

	// values of the window starting at lo
	TreeMap<Date, BigDecimal> found = null;

	while (hi - lo > PROBE_WINDOW) {

	    long mid = lo + (hi - lo) / 2;

	    TreeMap<Date, BigDecimal> values = retrieveValues(variableId, new Date(mid), new Date(mid + PROBE_WINDOW));
	    if (values.isEmpty()) {
		hi = mid;
	    } else {
		lo = mid;
		found = values;
	    }
	}

	if (found == null) {
	    found = retrieveValues(variableId, first, new Date(first.getTime() + PROBE_WINDOW));
	}

	return found.isEmpty() ? first : found.lastKey();
    }

    /**
     * Retrieves the valid observations of the given period (both ends included), splitting it in windows of
     * {@link #MAX_REQUEST_WINDOW}
     *
     * @param variableId
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(String variableId, Date begin, Date end) throws GSException {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	long from = begin.getTime();
	long to = end.getTime() + 1;

	while (from < to) {

	    long next = Math.min(from + MAX_REQUEST_WINDOW, to);
	    ret.putAll(retrieveValues(variableId, new Date(from), new Date(next)));
	    from = next;
	}

	return ret;
    }

    /**
     * @param variableId
     * @param begin
     * @param end
     * @return the valid observations of the given period (end excluded)
     * @throws GSException
     */
    TreeMap<Date, BigDecimal> retrieveValues(String variableId, Date begin, Date end) throws GSException {

	String path = VALUES_PATH + variableId + "/" + formatDate(new Date(begin.getTime() - REQUEST_MARGIN)) + "/"
		+ formatDate(new Date(end.getTime() + REQUEST_MARGIN));

	TreeMap<Date, BigDecimal> values = parseValues(new JSONArray(getOptional(path).orElse("[]")));

	return new TreeMap<>(values.subMap(begin, true, end, false));
    }

    /**
     * @param array e.g. [{"valor":1.3859,"fecha":"2025-09-30T22:00:00.000Z","estado":0}]
     * @return the valid values, without missing and wrong values
     */
    static TreeMap<Date, BigDecimal> parseValues(JSONArray array) {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	for (int i = 0; i < array.length(); i++) {

	    JSONObject row = array.optJSONObject(i);
	    if (row == null || row.isNull("valor")) {
		continue;
	    }

	    if (!row.isNull("estado") && (row.optInt("estado") & ERROR_STATE_FLAG) != 0) {
		continue;
	    }

	    Optional<Date> date = parseDate(row.optString("fecha", null));
	    BigDecimal value = row.optBigDecimal("valor", null);

	    if (date.isPresent() && value != null) {
		ret.put(date.get(), value);
	    }
	}

	return ret;
    }

    /**
     * @param text e.g. "2025-09-30T22:00:00.000Z"
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
     * @return the local time of Spain used by the requests, e.g. "2025-10-01T00:00:00"
     */
    static String formatDate(Date date) {

	return REQUEST_DATE_FORMAT.format(date.toInstant());
    }

    /**
     * @param variableId
     * @return the series of the given variable, or null if not valid
     */
    public static SAIHJucarEntity parseSeriesId(String variableId) {

	if (variableId == null || variableId.isEmpty() || !variableId.chars().allMatch(Character::isDigit)) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(VARIABLE_ID, variableId);

	return new SAIHJucarEntity(object);
    }

    /**
     * @param path
     * @return
     * @throws GSException
     */
    private String get(String path) throws GSException {

	return getOptional(path).orElseThrow(() -> createException("SAIH Júcar " + path + " not available", null));
    }

    /**
     * @param path
     * @return the response, or empty if the resource is not available
     * @throws GSException
     */
    private Optional<String> getOptional(String path) throws GSException {

	try {

	    HttpResponse<InputStream> response = downloader.downloadResponse(//
		    HttpRequestUtils.build(MethodNoBody.GET, endpoint + path), //
		    SAIHJucarClient.class.getClassLoader().getResourceAsStream(TRUST_STORE), //
		    TRUST_STORE_PASSWORD);

	    String text;
	    try (InputStream stream = response.body()) {
		text = stream == null ? "" : IOUtils.toString(stream, StandardCharsets.UTF_8);
	    }

	    // the chart pages of unknown stations return HTTP 500
	    if (response.statusCode() == 404 || (response.statusCode() == 500 && !path.startsWith(VALUES_PATH))) {
		return Optional.empty();
	    }

	    if (response.statusCode() != 200) {
		throw createException("SAIH Júcar " + path + " returned HTTP " + response.statusCode(), null);
	    }

	    return text.isBlank() ? Optional.empty() : Optional.of(text);

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("SAIH Júcar " + path + " request failed", e);
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
