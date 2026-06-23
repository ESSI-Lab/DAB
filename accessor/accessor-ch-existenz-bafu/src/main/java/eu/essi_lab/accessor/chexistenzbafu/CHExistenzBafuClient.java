package eu.essi_lab.accessor.chexistenzbafu;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.chexistenzbafu.CHExistenzBafuEntity.EntityType;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class CHExistenzBafuClient {

    public static final String DEFAULT_ENDPOINT = "https://api.existenz.ch/apiv1/";

    public static final String APP_NAME = "dab";

    public static final String SWISS_TIMEZONE = "Europe/Zurich";

    public static final String API_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final String DEFAULT_HISTORICAL_START = "1990-01-01 00:00:00";

    public static final String LOCATION_ID = "locationId";
    public static final String LOCATION_NAME = "locationName";
    public static final String WATER_BODY_NAME = "waterBodyName";
    public static final String WATER_BODY_TYPE = "waterBodyType";
    public static final String STATION_LAT = "lat";
    public static final String STATION_LON = "lon";

    public static final String PARAMETER = "parameter";
    public static final String PARAMETER_NAME = "parameterName";
    public static final String PARAMETER_UNIT = "parameterUnit";
    public static final String MEASURE_NOTATION = "measureNotation";

    public static final String FROM = "from";
    public static final String TO = "to";

    public static final String READING_TIMESTAMP = "timestamp";
    public static final String READING_VALUE = "val";
    public static final String READING_LOCATION = "loc";
    public static final String READING_PARAMETER = "par";

    private final String endpoint;
    private final Downloader downloader;
    private final SimpleDateFormat apiDateFormat;

    public CHExistenzBafuClient() {

	this(DEFAULT_ENDPOINT);
    }

    public CHExistenzBafuClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.apiDateFormat = new SimpleDateFormat(API_DATE_PATTERN);
	this.apiDateFormat.setTimeZone(TimeZone.getTimeZone(SWISS_TIMEZONE));
    }

    /**
     * @return
     */
    public List<CHExistenzBafuEntity> retrieveLocations() {

	return parsePayloadObjects(download("hydro/locations"), EntityType.LOCATION);
    }

    /**
     * @return
     */
    public Map<String, CHExistenzBafuEntity> retrieveParameters() {

	Map<String, CHExistenzBafuEntity> parameters = new HashMap<>();
	for (CHExistenzBafuEntity parameter : parsePayloadObjects(download("hydro/parameters"), EntityType.PARAMETER)) {
	    parameters.put(parameter.getObject().getString("name"), parameter);
	}
	return parameters;
    }

    /**
     * @param locationId
     * @return
     */
    public List<CHExistenzBafuEntity> retrieveLatest(String locationId) {

	String query = "hydro/latest?locations=" + encode(locationId);
	return parsePayloadArray(download(query), EntityType.READING);
    }

    /**
     * @param locationId
     * @param parameter
     * @param from
     * @param to
     * @return
     */
    public List<CHExistenzBafuEntity> retrieveReadings(String locationId, String parameter, Date from, Date to) {

	String query = "hydro/daterange?locations=" + encode(locationId) //
		+ "&parameters=" + encode(parameter) //
		+ "&startdate=" + encode(apiDateFormat.format(from)) //
		+ "&enddate=" + encode(apiDateFormat.format(to));

	return parsePayloadArray(download(query), EntityType.READING);
    }

    /**
     * @param locationId
     * @param parameter
     * @return
     */
    public SimpleEntry<Date, Date> retrieveReadingExtent(String locationId, String parameter) {

	Date end = null;
	for (CHExistenzBafuEntity reading : retrieveLatest(locationId)) {
	    JSONObject object = reading.getObject();
	    if (!parameter.equals(object.optString(READING_PARAMETER, null))) {
		continue;
	    }
	    end = timestampToDate(object.optLong(READING_TIMESTAMP, 0));
	    break;
	}

	if (end == null) {
	    end = new Date();
	}

	Date begin = parseApiDate(DEFAULT_HISTORICAL_START).orElse(new Date(0));
	return new SimpleEntry<>(begin, end);
    }

    /**
     * @param location
     * @return
     */
    public static String getLocationId(CHExistenzBafuEntity location) {

	JSONObject object = location.getObject();
	if (object.has("details")) {
	    return object.getJSONObject("details").optString("id", object.optString("name", ""));
	}
	return object.optString("name", "");
    }

    /**
     * @param locationId
     * @param parameter
     * @return
     */
    public static String getMeasureNotation(String locationId, String parameter) {

	return locationId + ":" + parameter;
    }

    /**
     * @param measureNotation
     * @return
     */
    public static SimpleEntry<String, String> parseMeasureNotation(String measureNotation) {

	int separator = measureNotation.indexOf(':');
	if (separator < 0) {
	    throw new IllegalArgumentException("Invalid measure notation: " + measureNotation);
	}
	return new SimpleEntry<>(measureNotation.substring(0, separator), measureNotation.substring(separator + 1));
    }

    /**
     * @param location
     * @param parameter
     * @param latestTimestamp
     * @return
     */
    public static CHExistenzBafuEntity createMeasure(CHExistenzBafuEntity location, CHExistenzBafuEntity parameter, long latestTimestamp) {

	JSONObject measure = new JSONObject();
	CHExistenzBafuEntity entity = new CHExistenzBafuEntity(measure, EntityType.MEASURE);
	mergeLocationIntoMeasure(location, entity);
	mergeParameterIntoMeasure(parameter, entity);

	String locationId = getLocationId(location);
	String parameterName = parameter.getObject().getString("name");
	measure.put(MEASURE_NOTATION, getMeasureNotation(locationId, parameterName));
	measure.put(FROM, toIso8601Utc(DEFAULT_HISTORICAL_START));
	measure.put(TO, ISO8601DateTimeUtils.getISO8601DateTime(timestampToDate(latestTimestamp)));

	return entity;
    }

    /**
     * @param location
     * @param measure
     */
    public static void mergeLocationIntoMeasure(CHExistenzBafuEntity location, CHExistenzBafuEntity measure) {

	JSONObject source = location.getObject();
	JSONObject target = measure.getObject();
	JSONObject details = source.optJSONObject("details");

	String locationId = getLocationId(location);
	putIfPresent(target, LOCATION_ID, locationId);
	if (details != null) {
	    putIfPresent(target, LOCATION_NAME, details.optString("name", null));
	    putIfPresent(target, WATER_BODY_NAME, details.optString("water-body-name", null));
	    putIfPresent(target, WATER_BODY_TYPE, details.optString("water-body-type", null));
	    putIfPresent(target, STATION_LAT, details.opt("lat"));
	    putIfPresent(target, STATION_LON, details.opt("lon"));
	}
    }

    /**
     * @param parameter
     * @param measure
     */
    public static void mergeParameterIntoMeasure(CHExistenzBafuEntity parameter, CHExistenzBafuEntity measure) {

	JSONObject source = parameter.getObject();
	JSONObject target = measure.getObject();
	JSONObject details = source.optJSONObject("details");

	putIfPresent(target, PARAMETER, source.optString("name", null));
	if (details != null) {
	    putIfPresent(target, PARAMETER_NAME, details.optString("name", null));
	    putIfPresent(target, PARAMETER_UNIT, details.optString("unit", source.optString("unit", null)));
	} else {
	    putIfPresent(target, PARAMETER_NAME, source.optString("name", null));
	    putIfPresent(target, PARAMETER_UNIT, source.optString("unit", null));
	}
    }

    /**
     * @param path
     * @return
     */
    private Optional<String> download(String path) {

	return downloader.downloadOptionalString(buildUrl(path));
    }

    /**
     * @param response
     * @param type
     * @return
     */
    private List<CHExistenzBafuEntity> parsePayloadObjects(Optional<String> response, EntityType type) {

	List<CHExistenzBafuEntity> entities = new ArrayList<>();
	if (response.isEmpty()) {
	    return entities;
	}

	JSONObject payload = new JSONObject(response.get()).optJSONObject("payload");
	if (payload == null) {
	    return entities;
	}

	Iterator<String> keys = payload.keys();
	while (keys.hasNext()) {
	    String key = keys.next();
	    JSONObject object = payload.getJSONObject(key);
	    object.put("name", object.optString("name", key));
	    entities.add(new CHExistenzBafuEntity(object, type));
	}

	return entities;
    }

    /**
     * @param response
     * @param type
     * @return
     */
    private List<CHExistenzBafuEntity> parsePayloadArray(Optional<String> response, EntityType type) {

	List<CHExistenzBafuEntity> entities = new ArrayList<>();
	if (response.isEmpty()) {
	    return entities;
	}

	JSONArray payload = new JSONObject(response.get()).optJSONArray("payload");
	if (payload == null) {
	    return entities;
	}

	for (int i = 0; i < payload.length(); i++) {
	    entities.add(new CHExistenzBafuEntity(payload.getJSONObject(i), type));
	}

	return entities;
    }

    /**
     * @param timestamp
     * @return
     */
    public static Date timestampToDate(long timestamp) {

	return new Date(timestamp * 1000L);
    }

    /**
     * @param timestamp
     * @return
     */
    public static String formatTimestamp(long timestamp) {

	return ISO8601DateTimeUtils.getISO8601DateTime(timestampToDate(timestamp));
    }

    /**
     * Converts a Swiss local date-time or an ISO8601 UTC string to ISO8601 UTC.
     *
     * @param dateTime
     * @return
     */
    public static String toIso8601Utc(String dateTime) {

	if (dateTime == null || dateTime.isEmpty()) {
	    return dateTime;
	}

	if (dateTime.contains("T")) {
	    Optional<Date> parsed = ISO8601DateTimeUtils.parseISO8601ToDate(dateTime);
	    if (parsed.isPresent()) {
		return ISO8601DateTimeUtils.getISO8601DateTime(parsed.get());
	    }
	}

	return parseSwissLocalDateTime(dateTime).map(ISO8601DateTimeUtils::getISO8601DateTime).orElse(dateTime);
    }

    /**
     * @param value
     * @return
     */
    private static Optional<Date> parseSwissLocalDateTime(String value) {

	try {
	    SimpleDateFormat format = new SimpleDateFormat(API_DATE_PATTERN);
	    format.setTimeZone(TimeZone.getTimeZone(SWISS_TIMEZONE));
	    return Optional.of(format.parse(value));
	} catch (Exception e) {
	    return Optional.empty();
	}
    }

    /**
     * @param value
     * @return
     */
    private Optional<Date> parseApiDate(String value) {

	return parseSwissLocalDateTime(value);
    }

    /**
     * @param value
     * @return
     */
    private String encode(String value) {

	return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * @param path
     * @return
     */
    private String buildUrl(String path) {

	String separator = path.contains("?") ? "&" : "?";
	return endpoint + path + separator + "app=" + APP_NAME;
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

    /**
     * @param object
     * @param key
     * @param value
     */
    private static void putIfPresent(JSONObject object, String key, Object value) {

	if (value == null) {
	    return;
	}
	if (value instanceof String && ((String) value).isEmpty()) {
	    return;
	}
	object.put(key, value);
    }
}
