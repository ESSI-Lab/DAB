package eu.essi_lab.accessor.ukhydrology;

import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

import eu.essi_lab.accessor.ukhydrology.UKHydrologyEntity.EntityType;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class UKHydrologyClient {

    public static final String DEFAULT_ENDPOINT = "https://environment.data.gov.uk/hydrology/";

    public static final String STATION_GUID = "stationGuid";
    public static final String STATION_LABEL = "stationLabel";
    public static final String STATION_LAT = "stationLat";
    public static final String STATION_LONG = "stationLong";
    public static final String RIVER_NAME = "riverName";
    public static final String DATE_OPENED = "dateOpened";
    public static final String DATE_CLOSED = "dateClosed";
    public static final String WISKI_ID = "wiskiID";
    public static final String STATION_REFERENCE = "stationReference";
    public static final String STATION_STATUS = "stationStatus";

    public static final String MEASURE_NOTATION = "measureNotation";
    public static final String MEASURE_LABEL = "measureLabel";
    public static final String PARAMETER = "parameter";
    public static final String PARAMETER_NAME = "parameterName";
    public static final String PERIOD = "period";
    public static final String PERIOD_NAME = "periodName";
    public static final String VALUE_TYPE = "valueType";
    public static final String VALUE_STATISTIC = "valueStatistic";
    public static final String UNIT_NAME = "unitName";
    public static final String OBSERVED_PROPERTY = "observedProperty";
    public static final String FROM = "from";
    public static final String TO = "to";

    public static final String READING_DATE_TIME = "dateTime";
    public static final String READING_VALUE = "value";

    private static final int PAGE_SIZE = 500;

    private final String endpoint;
    private final Downloader downloader;
    private final SimpleDateFormat dateFormat;

    public UKHydrologyClient() {

	this(DEFAULT_ENDPOINT);
    }

    public UKHydrologyClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * @return
     */
    public List<UKHydrologyEntity> retrieveStations() {

	return retrievePagedItems("id/stations.json", EntityType.STATION);
    }

    /**
     * @param limit
     * @param offset
     * @return
     */
    public List<UKHydrologyEntity> retrieveStationsPage(int limit, int offset) {

	return fetchPage("id/stations.json", EntityType.STATION, limit, offset);
    }

    /**
     * @return
     */
    public static int getStationPageSize() {

	return PAGE_SIZE;
    }

    /**
     * @param stationGuid
     * @return
     */
    public List<UKHydrologyEntity> retrieveMeasures(String stationGuid) {

	return retrievePagedItems("id/measures.json?station=" + stationGuid, EntityType.MEASURE);
    }

    /**
     * @param measureNotation
     * @return
     */
    public UKHydrologyEntity retrieveMeasure(String measureNotation) {

	String url = buildUrl("id/measures/" + measureNotation + ".json");
	Optional<String> response = downloader.downloadOptionalString(url);
	if (response.isEmpty()) {
	    return null;
	}

	JSONObject object = new JSONObject(response.get());
	JSONArray items = object.getJSONArray("items");
	if (items.isEmpty()) {
	    return null;
	}

	return new UKHydrologyEntity(items.getJSONObject(0), EntityType.MEASURE);
    }

    /**
     * @param measureNotation
     * @return
     */
    public SimpleEntry<Date, Date> retrieveReadingExtent(String measureNotation) {

	String earliestUrl = buildUrl("id/measures/" + measureNotation + "/readings.json?_limit=1&_sort=date");
	String latestUrl = buildUrl("id/measures/" + measureNotation + "/readings.json?_limit=1&_sort=-date");

	Optional<String> earliestResponse = downloader.downloadOptionalString(earliestUrl);
	Optional<String> latestResponse = downloader.downloadOptionalString(latestUrl);

	if (earliestResponse.isEmpty() || latestResponse.isEmpty()) {
	    return null;
	}

	Date begin = extractReadingDate(earliestResponse.get());
	Date end = extractReadingDate(latestResponse.get());

	if (begin == null || end == null) {
	    return null;
	}

	return new SimpleEntry<>(begin, end);
    }

    /**
     * @param measureNotation
     * @param from
     * @param to
     * @return
     */
    public List<UKHydrologyEntity> retrieveReadings(String measureNotation, Date from, Date to) {

	String fromDate = dateFormat.format(from);
	String toDate = dateFormat.format(to);
	String query = "id/measures/" + measureNotation + "/readings.json?mineq-date=" + fromDate + "&max-date=" + toDate;

	return retrievePagedItems(query, EntityType.READING);
    }

    /**
     * @param query
     * @param type
     * @return
     */
    private List<UKHydrologyEntity> retrievePagedItems(String query, EntityType type) {

	List<UKHydrologyEntity> entities = new ArrayList<>();
	int offset = 0;

	while (true) {
	    List<UKHydrologyEntity> page = fetchPage(query, type, PAGE_SIZE, offset);
	    if (page.isEmpty()) {
		break;
	    }

	    entities.addAll(page);

	    if (page.size() < PAGE_SIZE) {
		break;
	    }

	    offset += PAGE_SIZE;
	}

	return entities;
    }

    /**
     * @param query
     * @param type
     * @param limit
     * @param offset
     * @return
     */
    private List<UKHydrologyEntity> fetchPage(String query, EntityType type, int limit, int offset) {

	List<UKHydrologyEntity> entities = new ArrayList<>();
	String separator = query.contains("?") ? "&" : "?";
	String url = buildUrl(query + separator + "_limit=" + limit + "&_offset=" + offset);
	Optional<String> response = downloader.downloadOptionalString(url);

	if (response.isEmpty()) {
	    return entities;
	}

	JSONObject object = new JSONObject(response.get());
	JSONArray items = object.optJSONArray("items");
	if (items == null || items.isEmpty()) {
	    return entities;
	}

	for (int i = 0; i < items.length(); i++) {
	    entities.add(new UKHydrologyEntity(items.getJSONObject(i), type));
	}

	return entities;
    }

    /**
     * @param response
     * @return
     */
    private Date extractReadingDate(String response) {

	JSONObject object = new JSONObject(response);
	JSONArray items = object.optJSONArray("items");
	if (items == null || items.isEmpty()) {
	    return null;
	}

	JSONObject item = items.getJSONObject(0);
	String dateTime = item.optString(READING_DATE_TIME, null);
	if (dateTime == null || dateTime.isEmpty()) {
	    dateTime = item.optString("date", null);
	}

	if (dateTime == null || dateTime.isEmpty()) {
	    return null;
	}

	return ISO8601DateTimeUtils.parseISO8601ToDate(dateTime).orElse(null);
    }

    /**
     * @param path
     * @return
     */
    private String buildUrl(String path) {

	return endpoint + path;
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
     * @param station
     * @return
     */
    public static String getStationGuid(UKHydrologyEntity station) {

	JSONObject object = station.getObject();

	String notation = optStringField(object, "notation");
	if (notation != null && !notation.isEmpty()) {
	    return notation;
	}

	String stationGuid = optStringField(object, STATION_GUID);
	if (stationGuid != null && !stationGuid.isEmpty()) {
	    return stationGuid;
	}

	return object.optString("@id", "").replaceAll(".*/stations/", "");
    }

    /**
     * @param measure
     * @return
     */
    public static String getMeasureNotation(UKHydrologyEntity measure) {

	JSONObject object = measure.getObject();

	String measureNotation = optStringField(object, MEASURE_NOTATION);
	if (measureNotation != null && !measureNotation.isEmpty()) {
	    return measureNotation;
	}

	measureNotation = optStringField(object, "notation");
	if (measureNotation != null && !measureNotation.isEmpty()) {
	    return measureNotation;
	}

	return object.optString("@id", "").replaceAll(".*/measures/", "");
    }

    /**
     * @param object
     * @param key
     * @return
     */
    private static String optStringField(JSONObject object, String key) {

	if (!object.has(key) || object.isNull(key)) {
	    return null;
	}

	Object value = object.get(key);
	if (value instanceof String) {
	    return (String) value;
	}

	if (value instanceof JSONArray) {
	    JSONArray array = (JSONArray) value;
	    if (array.isEmpty()) {
		return null;
	    }

	    String fromId = object.optString("@id", "");
	    if (!fromId.isEmpty()) {
		int slash = fromId.lastIndexOf('/');
		if (slash >= 0) {
		    fromId = fromId.substring(slash + 1);
		}
		for (int i = 0; i < array.length(); i++) {
		    String candidate = array.optString(i, null);
		    if (fromId.equals(candidate)) {
			return candidate;
		    }
		}
	    }

	    return array.optString(0, null);
	}

	return value.toString();
    }

    /**
     * @param object
     * @param key
     * @return
     */
    public static String extractLabel(JSONObject object, String key) {

	if (!object.has(key) || object.isNull(key)) {
	    return null;
	}

	Object value = object.get(key);
	if (value instanceof String) {
	    String text = (String) value;
	    return text.isEmpty() ? null : text;
	}

	if (value instanceof JSONObject) {
	    String label = ((JSONObject) value).optString("label", null);
	    if (label != null && !label.isEmpty()) {
		return label;
	    }
	}

	return null;
    }

    /**
     * @param object
     * @param key
     */
    private static void putLabel(JSONObject object, String key) {

	String label = extractLabel(object, key);
	if (label != null) {
	    object.put(key, label);
	}
    }

    /**
     * @param source
     * @param target
     */
    public static void mergeStationIntoMeasure(UKHydrologyEntity source, UKHydrologyEntity target) {

	JSONObject station = source.getObject();
	JSONObject measure = target.getObject();

	putIfAbsent(measure, STATION_GUID, getStationGuid(source));
	putIfAbsent(measure, STATION_LABEL, station.optString("label", null));
	putIfAbsent(measure, STATION_LAT, station.opt("lat"));
	putIfAbsent(measure, STATION_LONG, station.opt("long"));
	putIfAbsent(measure, RIVER_NAME, station.optString("riverName", null));
	putIfAbsent(measure, DATE_OPENED, station.optString("dateOpened", null));
	putIfAbsent(measure, DATE_CLOSED, station.optString("dateClosed", null));
	putIfAbsent(measure, WISKI_ID, station.optString("wiskiID", null));
	putIfAbsent(measure, STATION_REFERENCE, station.optString("stationReference", null));

	if (station.has("status")) {
	    Object status = station.get("status");
	    if (status instanceof JSONArray) {
		JSONArray array = (JSONArray) status;
		if (!array.isEmpty()) {
		    JSONObject statusObject = array.getJSONObject(0);
		    putIfAbsent(measure, STATION_STATUS, statusObject.optString("label", null));
		}
	    } else if (status instanceof JSONObject) {
		putIfAbsent(measure, STATION_STATUS, ((JSONObject) status).optString("label", null));
	    }
	}

	putIfAbsent(measure, MEASURE_NOTATION, getMeasureNotation(target));
	putIfAbsent(measure, MEASURE_LABEL, measure.optString("label", null));
	putIfAbsent(measure, PARAMETER, measure.optString("parameter", null));
	putIfAbsent(measure, PARAMETER_NAME, measure.optString("parameterName", null));
	putIfAbsent(measure, PERIOD, measure.opt("period"));
	putIfAbsent(measure, PERIOD_NAME, measure.optString("periodName", null));
	putIfAbsent(measure, VALUE_TYPE, measure.optString("valueType", null));
	putIfAbsent(measure, UNIT_NAME, measure.optString("unitName", null));

	putLabel(measure, VALUE_STATISTIC);
	putLabel(measure, OBSERVED_PROPERTY);

	String from = measure.optString(DATE_OPENED, null);
	String to = measure.optString(DATE_CLOSED, null);
	if (to == null || to.isEmpty()) {
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	    to = sdf.format(new Date());
	}
	if (from != null && !from.isEmpty()) {
	    measure.put(FROM, from);
	}
	if (to != null && !to.isEmpty()) {
	    measure.put(TO, to);
	}
    }

    /**
     * @param object
     * @param key
     * @param value
     */
    private static void putIfAbsent(JSONObject object, String key, Object value) {

	if (value == null) {
	    return;
	}
	if (value instanceof String && ((String) value).isEmpty()) {
	    return;
	}
	if (!object.has(key) || object.isNull(key)) {
	    object.put(key, value);
	}
    }
}
