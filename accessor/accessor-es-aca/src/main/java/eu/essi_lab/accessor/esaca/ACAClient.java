package eu.essi_lab.accessor.esaca;

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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/**
 * Client of the Sentilo REST API of the Agència Catalana de l'Aigua (ACA) (https://aplicacions.aca.gencat.cat/sdim2/apirest/).<br>
 * The catalog lists, for each component type (e.g. "aforament" for river gauging stations, "embassament" for reservoirs), the
 * Sentilo providers (e.g. "AFORAMENT-EST") and their sensors; each sensor measures a single property at a component (the station).
 * A time series is identified by a provider and a sensor.<br>
 * The data service returns the latest observation of all the sensors of a provider, and the observations of a sensor in a time
 * window, sorted from the most recent and at most {@link #MAX_LIMIT} per request. Only the observations of about the last 3 months
 * are kept (rolling window, see {@link #ROLLING_WINDOW}). Times are UTC, formatted as "dd/MM/yyyy'T'HH:mm:ss".
 *
 * @author boldrini
 */
public class ACAClient {

    public static final String DEFAULT_ENDPOINT = "https://aplicacions.aca.gencat.cat/sdim2/apirest/";

    static final String CATALOG_PATH = "catalog";
    static final String DATA_PATH = "data/";

    /**
     * Maximum number of observations returned by a single data request (larger limits are reduced to this value by the service)
     */
    public static final int MAX_LIMIT = 200;

    /**
     * Width of the time window certainly covered by the data service (the observations of the last 93 days are kept)
     */
    public static final long ROLLING_WINDOW = TimeUnit.DAYS.toMillis(92);

    /**
     * Maximum number of data requests for a single download (more than the requests needed to read the whole rolling window of a
     * sensor with 1 minute observations)
     */
    static final int MAX_REQUESTS = 1000;

    // keys of the catalog sensors
    public static final String SENSOR = "sensor";
    public static final String DESCRIPTION = "description";
    public static final String UNIT = "unit";
    public static final String TYPE = "type";
    public static final String LOCATION = "location";
    public static final String PUBLIC_ACCESS = "publicAccess";
    public static final String STATE = "state";
    public static final String COMPONENT = "component";
    public static final String COMPONENT_TYPE = "componentType";
    public static final String COMPONENT_DESCRIPTION = "componentDesc";
    public static final String ADDITIONAL_INFO = "additionalInfo";
    public static final String COMPONENT_ADDITIONAL_INFO = "componentAdditionalInfo";

    // keys of the additional info of the sensors
    public static final String SAMPLING_MINUTES = "Temps mostreig (min)";

    // keys of the additional info of the components
    public static final String RIVER = "Riu";
    public static final String BASIN = "Conca";
    public static final String SUB_BASIN = "Subconca";
    public static final String RIVER_DISTRICT = "Districte fluvial";
    public static final String COUNTY = "Comarca";
    public static final String PROVINCE = "Província";
    public static final String MUNICIPALITY = "Terme municipal";
    public static final String AQUIFER = "Aqüífer";
    public static final String WATER_BODY = "Massa d'aigua";
    public static final String DRAINAGE_AREA = "Superfície conca drenada";
    public static final String MAX_CAPACITY = "Capacitat màxima embassament";
    public static final String OWNER = "Titular";
    public static final String STATUS = "Estat";
    public static final String UTM_X = "Coordenada X (UTM ETRS89)";
    public static final String UTM_Y = "Coordenada Y (UTM ETRS89)";

    // keys added by the client
    public static final String PROVIDER = "provider";
    public static final String LAT = "latitude";
    public static final String LON = "longitude";
    public static final String SERIES_ID = "seriesId";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    private static final String SERIES_ID_SEPARATOR = "|";

    /**
     * UTM zone of the coordinates of the components (ETRS89 / UTM zone 31N)
     */
    private static final int UTM_ZONE = 31;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy'T'HH:mm:ss");

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public ACAClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public ACAClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 2);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    /**
     * Retrieves the time series of the public sensors of the given component types. Series without observations in the rolling
     * window are discarded. Two requests are made for each provider: the catalog and the latest observations
     *
     * @param componentTypes e.g. "aforament", "embassament"
     * @return the series sorted by component and sensor
     * @throws GSException
     */
    public List<ACAEntity> retrieveSeries(Collection<String> componentTypes) throws GSException {

	List<ACAEntity> ret = new ArrayList<>();
	Date now = new Date();

	for (String componentType : componentTypes) {

	    JSONObject catalog = new JSONObject(get(CATALOG_PATH + "?componentType=" + componentType));

	    JSONArray providers = catalog.optJSONArray("providers");
	    if (providers == null) {
		GSLoggerFactory.getLogger(getClass()).warn("No ACA providers of component type {}", componentType);
		continue;
	    }

	    for (int i = 0; i < providers.length(); i++) {

		JSONObject provider = providers.getJSONObject(i);
		String providerId = provider.getString(PROVIDER);

		Map<String, Date> latest = parseLatestObservations(new JSONObject(get(DATA_PATH + providerId)));

		JSONArray sensors = provider.optJSONArray("sensors");
		for (int j = 0; sensors != null && j < sensors.length(); j++) {

		    JSONObject sensor = sensors.getJSONObject(j);
		    createSeries(providerId, sensor, latest.get(sensor.optString(SENSOR)), now).ifPresent(ret::add);
		}
	    }
	}

	ret.sort(Comparator.comparing(ACAEntity::getComponent).thenComparing(ACAEntity::getSeriesId));

	return ret;
    }

    /**
     * @param provider the Sentilo provider, e.g. "AFORAMENT-EST"
     * @param sensor the sensor from the catalog
     * @param latest the time of the latest observation of the sensor, possibly null
     * @param now the current time
     * @return the series, or empty if the sensor is not public or it has no observations in the rolling window
     */
    static Optional<ACAEntity> createSeries(String provider, JSONObject sensor, Date latest, Date now) {

	if (!sensor.optBoolean(PUBLIC_ACCESS, true) || !sensor.optBoolean("componentPublicAccess", true)) {
	    return Optional.empty();
	}

	Date windowBegin = new Date(now.getTime() - ROLLING_WINDOW);
	if (latest == null || latest.before(windowBegin)) {
	    return Optional.empty();
	}

	JSONObject object = new JSONObject();

	// descriptions and keys sometimes end with spaces, tabs or new lines
	for (String key : sensor.keySet()) {
	    Object value = sensor.get(key);
	    if (value instanceof String) {
		object.put(key, ((String) value).trim());
	    } else if (value instanceof JSONObject && (key.equals(ADDITIONAL_INFO) || key.equals(COMPONENT_ADDITIONAL_INFO))) {
		object.put(key, trim((JSONObject) value));
	    } else {
		object.put(key, value);
	    }
	}

	object.put(PROVIDER, provider);
	object.put(SERIES_ID, buildSeriesId(provider, object.optString(SENSOR)));

	parseLocation(object).ifPresent(location -> {
	    object.put(LAT, location[0]);
	    object.put(LON, location[1]);
	});

	object.put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(windowBegin));
	object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(latest));

	return Optional.of(new ACAEntity(object));
    }

    /**
     * @param info
     * @return a copy of the given object with trimmed keys and values, without empty values
     */
    private static JSONObject trim(JSONObject info) {

	JSONObject ret = new JSONObject();

	for (String key : info.keySet()) {
	    String value = info.optString(key, "").trim();
	    if (!key.trim().isEmpty() && !value.isEmpty()) {
		ret.put(key.trim(), value);
	    }
	}

	return ret;
    }

    /**
     * @param sensor
     * @return the latitude and the longitude of the sensor, from its location ("lat lon") or from the UTM coordinates of the
     *         component
     */
    static Optional<BigDecimal[]> parseLocation(JSONObject sensor) {

	String[] split = sensor.optString(LOCATION, "").trim().split("\\s+");
	if (split.length == 2) {
	    try {
		return Optional.of(new BigDecimal[] { new BigDecimal(split[0]), new BigDecimal(split[1]) });
	    } catch (NumberFormatException e) {
		// trying UTM coordinates
	    }
	}

	JSONObject info = sensor.optJSONObject(COMPONENT_ADDITIONAL_INFO);
	if (info == null) {
	    return Optional.empty();
	}

	try {
	    double x = Double.parseDouble(info.optString(UTM_X, "").trim().replace(",", "."));
	    double y = Double.parseDouble(info.optString(UTM_Y, "").trim().replace(",", "."));
	    double[] latLon = utmToLatLon(x, y, UTM_ZONE);
	    return Optional.of(new BigDecimal[] { //
		    BigDecimal.valueOf(latLon[0]).setScale(6, RoundingMode.HALF_UP), //
		    BigDecimal.valueOf(latLon[1]).setScale(6, RoundingMode.HALF_UP) });
	} catch (NumberFormatException e) {
	    return Optional.empty();
	}
    }

    /**
     * Converts UTM coordinates of the northern hemisphere (GRS80 ellipsoid) to geographic coordinates (Snyder, Map Projections - A
     * Working Manual, 1987)
     *
     * @param easting
     * @param northing
     * @param zone
     * @return latitude and longitude in degrees
     */
    static double[] utmToLatLon(double easting, double northing, int zone) {

	double k0 = 0.9996;
	double a = 6378137.0;
	double f = 1 / 298.257222101;
	double e2 = f * (2 - f);
	double ep2 = e2 / (1 - e2);

	double x = easting - 500000.0;
	double m = northing / k0;
	double mu = m / (a * (1 - e2 / 4 - 3 * e2 * e2 / 64 - 5 * e2 * e2 * e2 / 256));

	double e1 = (1 - Math.sqrt(1 - e2)) / (1 + Math.sqrt(1 - e2));
	double phi1 = mu + (3 * e1 / 2 - 27 * Math.pow(e1, 3) / 32) * Math.sin(2 * mu) //
		+ (21 * e1 * e1 / 16 - 55 * Math.pow(e1, 4) / 32) * Math.sin(4 * mu) //
		+ (151 * Math.pow(e1, 3) / 96) * Math.sin(6 * mu) //
		+ (1097 * Math.pow(e1, 4) / 512) * Math.sin(8 * mu);

	double sin = Math.sin(phi1);
	double cos = Math.cos(phi1);
	double tan = Math.tan(phi1);

	double c1 = ep2 * cos * cos;
	double t1 = tan * tan;
	double n1 = a / Math.sqrt(1 - e2 * sin * sin);
	double r1 = a * (1 - e2) / Math.pow(1 - e2 * sin * sin, 1.5);
	double d = x / (n1 * k0);

	double lat = phi1 - (n1 * tan / r1) * (d * d / 2 //
		- (5 + 3 * t1 + 10 * c1 - 4 * c1 * c1 - 9 * ep2) * Math.pow(d, 4) / 24 //
		+ (61 + 90 * t1 + 298 * c1 + 45 * t1 * t1 - 252 * ep2 - 3 * c1 * c1) * Math.pow(d, 6) / 720);

	double lon = (d - (1 + 2 * t1 + c1) * Math.pow(d, 3) / 6 //
		+ (5 - 2 * c1 + 28 * t1 - 3 * c1 * c1 + 8 * ep2 + 24 * t1 * t1) * Math.pow(d, 5) / 120) / cos;

	double centralMeridian = zone * 6 - 183;

	return new double[] { Math.toDegrees(lat), centralMeridian + Math.toDegrees(lon) };
    }

    /**
     * @param latest the response of the data service for a provider
     * @return the time of the latest observation of each sensor
     */
    static Map<String, Date> parseLatestObservations(JSONObject latest) {

	HashMap<String, Date> ret = new HashMap<>();

	JSONArray sensors = latest.optJSONArray("sensors");
	for (int i = 0; sensors != null && i < sensors.length(); i++) {

	    JSONObject sensor = sensors.getJSONObject(i);
	    TreeMap<Date, BigDecimal> values = new TreeMap<>();
	    parseObservations(sensor, values);

	    if (!values.isEmpty()) {
		ret.put(sensor.optString(SENSOR), values.lastKey());
	    }
	}

	return ret;
    }

    /**
     * @param series
     * @return the time of the latest valid observation of the series
     * @throws GSException
     */
    public Optional<Date> retrieveLatestObservation(ACAEntity series) throws GSException {

	TreeMap<Date, BigDecimal> values = new TreeMap<>();
	parseObservations(new JSONObject(get(getSensorPath(series) + "?limit=1")), values);

	return values.isEmpty() ? Optional.empty() : Optional.of(values.lastKey());
    }

    /**
     * Retrieves the valid observations of the given period. Since the observations are returned from the most recent, at most
     * {@link #MAX_LIMIT} per request, the period is read backwards, moving the end of the requested window before the oldest
     * observation received
     *
     * @param series
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(ACAEntity series, Date begin, Date end) throws GSException {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	String from = formatDate(begin);
	Date to = end;

	for (int i = 0; i < MAX_REQUESTS && !to.before(begin); i++) {

	    String path = getSensorPath(series) + "?from=" + from + "&to=" + formatDate(to) + "&limit=" + MAX_LIMIT;

	    Page page = parseObservations(new JSONObject(get(path)), ret);

	    if (page.count < MAX_LIMIT || page.oldest == null) {
		break;
	    }

	    // dates have a resolution of 1 second
	    to = new Date(page.oldest.getTime() - 1000);
	}

	return new TreeMap<>(ret.subMap(begin, true, end, true));
    }

    /**
     * A page of observations
     */
    static class Page {

	/**
	 * Number of observations of the page (also of invalid values)
	 */
	int count;

	/**
	 * Time of the oldest observation of the page
	 */
	Date oldest;
    }

    /**
     * @param response an object with the "observations" array, e.g.
     *        {"observations":[{"value":"0.01","timestamp":"27/10/2026T14:15:00","time":1793110500000}]}
     * @param values the map where the valid values are added
     * @return
     */
    static Page parseObservations(JSONObject response, TreeMap<Date, BigDecimal> values) {

	Page ret = new Page();

	JSONArray observations = response.optJSONArray("observations");
	for (int i = 0; observations != null && i < observations.length(); i++) {

	    JSONObject observation = observations.getJSONObject(i);
	    ret.count++;

	    Optional<Date> date = observation.has("time") ? Optional.of(new Date(observation.getLong("time")))
		    : parseDate(observation.optString("timestamp", null));
	    if (date.isEmpty()) {
		continue;
	    }

	    if (ret.oldest == null || date.get().before(ret.oldest)) {
		ret.oldest = date.get();
	    }

	    BigDecimal value = parseDecimal(observation.optString("value", ""));
	    if (value != null) {
		values.put(date.get(), value);
	    }
	}

	return ret;
    }

    /**
     * @param provider
     * @param sensor
     * @return
     */
    static String buildSeriesId(String provider, String sensor) {

	return provider + SERIES_ID_SEPARATOR + sensor;
    }

    /**
     * @param seriesId
     * @return the series identified by the given identifier, or null if not valid
     */
    public static ACAEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	int index = seriesId.indexOf(SERIES_ID_SEPARATOR);
	if (index <= 0 || index == seriesId.length() - 1) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, seriesId);
	object.put(PROVIDER, seriesId.substring(0, index));
	object.put(SENSOR, seriesId.substring(index + 1));

	return new ACAEntity(object);
    }

    /**
     * @param series
     * @return e.g. "data/AFORAMENT-EST/CALC001658"
     */
    static String getSensorPath(ACAEntity series) {

	return DATA_PATH + series.getProvider() + "/" + series.getSensor();
    }

    /**
     * @param text e.g. "27/10/2026T14:15:00" (UTC)
     * @return
     */
    static Optional<Date> parseDate(String text) {

	if (text == null || text.isEmpty()) {
	    return Optional.empty();
	}

	try {
	    return Optional.of(Date.from(LocalDateTime.parse(text, DATE_FORMAT).toInstant(ZoneOffset.UTC)));
	} catch (DateTimeParseException e) {
	    return Optional.empty();
	}
    }

    /**
     * @param date
     * @return the date in the format of the service, e.g. "27/10/2026T14:15:00" (UTC)
     */
    static String formatDate(Date date) {

	return DATE_FORMAT.format(date.toInstant().atOffset(ZoneOffset.UTC));
    }

    /**
     * @param text
     * @return
     */
    private static BigDecimal parseDecimal(String text) {

	try {
	    return text.isBlank() ? null : new BigDecimal(text.trim());
	} catch (NumberFormatException e) {
	    return null;
	}
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
		throw createException("ACA " + path + " returned HTTP " + response.statusCode(), null);
	    }

	    return text.isBlank() ? "{}" : text;

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("ACA " + path + " request failed", e);
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
