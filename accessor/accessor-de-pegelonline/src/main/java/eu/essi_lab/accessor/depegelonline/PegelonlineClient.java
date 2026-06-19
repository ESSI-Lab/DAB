package eu.essi_lab.accessor.depegelonline;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

import eu.essi_lab.accessor.depegelonline.PegelonlineEntity.EntityType;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class PegelonlineClient {

    public static final String DEFAULT_ENDPOINT = "https://www.pegelonline.wsv.de/webservices/rest-api/v2/";

    public static final int DATA_RETENTION_DAYS = 30;

    public static final String STATION_UUID = "stationUuid";
    public static final String STATION_NUMBER = "stationNumber";
    public static final String STATION_SHORTNAME = "stationShortname";
    public static final String STATION_LABEL = "stationLabel";
    public static final String STATION_LAT = "stationLat";
    public static final String STATION_LONG = "stationLong";
    public static final String STATION_KM = "stationKm";
    public static final String STATION_AGENCY = "stationAgency";
    public static final String WATER_SHORTNAME = "waterShortname";
    public static final String WATER_NAME = "waterName";

    public static final String TIMESERIES_ID = "timeseriesId";
    public static final String TIMESERIES_SHORTNAME = "timeseriesShortname";
    public static final String TIMESERIES_LABEL = "timeseriesLabel";
    public static final String UNIT_NAME = "unitName";
    public static final String PERIOD = "period";
    public static final String PERIOD_NAME = "periodName";
    public static final String FROM = "from";
    public static final String TO = "to";

    public static final String MEASUREMENT_TIMESTAMP = "timestamp";
    public static final String MEASUREMENT_VALUE = "value";

    private static final String TIME_ZONE = "Europe/Berlin";

    private final String endpoint;
    private final Downloader downloader;
    private final SimpleDateFormat dateFormat;

    public PegelonlineClient() {

	this(DEFAULT_ENDPOINT);
    }

    public PegelonlineClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	this.dateFormat.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
    }

    /**
     * @return
     */
    public List<PegelonlineEntity> retrieveStations() {

	return fetchStations("stations.json?includeTimeseries=true");
    }

    /**
     * @param stationUuid
     * @return
     */
    public PegelonlineEntity retrieveStation(String stationUuid) {

	String url = buildUrl("stations/" + stationUuid + ".json?includeTimeseries=true");
	Optional<String> response = downloader.downloadOptionalString(url);
	if (response.isEmpty()) {
	    return null;
	}

	return new PegelonlineEntity(new JSONObject(response.get()), EntityType.STATION);
    }

    /**
     * @param timeseriesId
     * @return
     */
    public SimpleEntry<Date, Date> retrieveMeasurementExtent(String timeseriesId) {

	SimpleEntry<String, String> parts = parseTimeseriesId(timeseriesId);
	if (parts == null) {
	    return null;
	}

	String url = buildUrl("stations/" + parts.getKey() + "/" + parts.getValue() + "/measurements.json?start=P"
		+ DATA_RETENTION_DAYS + "D");
	Optional<String> response = downloader.downloadOptionalString(url);
	if (response.isEmpty()) {
	    return getDefaultExtent();
	}

	JSONArray items = new JSONArray(response.get());
	if (items.isEmpty()) {
	    return getDefaultExtent();
	}

	Date begin = parseTimestamp(items.getJSONObject(0));
	Date end = parseTimestamp(items.getJSONObject(items.length() - 1));

	if (begin == null || end == null) {
	    return getDefaultExtent();
	}

	return new SimpleEntry<>(begin, end);
    }

    /**
     * @param timeseriesId
     * @param from
     * @param to
     * @return
     */
    public List<PegelonlineEntity> retrieveMeasurements(String timeseriesId, Date from, Date to) {

	SimpleEntry<String, String> parts = parseTimeseriesId(timeseriesId);
	if (parts == null) {
	    return new ArrayList<>();
	}

	SimpleEntry<Date, Date> clamped = clampToRetention(from, to);
	String start = formatApiDateTime(clamped.getKey());
	String end = formatApiDateTime(clamped.getValue());

	String query = "stations/" + parts.getKey() + "/" + parts.getValue() + "/measurements.json?start="
		+ encodeQueryValue(start) + "&end=" + encodeQueryValue(end);

	Optional<String> response = downloader.downloadOptionalString(buildUrl(query));
	if (response.isEmpty()) {
	    return new ArrayList<>();
	}

	JSONArray items = new JSONArray(response.get());
	List<PegelonlineEntity> entities = new ArrayList<>();

	for (int i = 0; i < items.length(); i++) {
	    entities.add(new PegelonlineEntity(items.getJSONObject(i), EntityType.MEASUREMENT));
	}

	return entities;
    }

    /**
     * @param station
     * @return
     */
    public static String getStationUuid(PegelonlineEntity station) {

	return station.getObject().optString("uuid", null);
    }

    /**
     * @param station
     * @param timeseries
     * @return
     */
    public static String getTimeseriesId(PegelonlineEntity station, JSONObject timeseries) {

	String stationUuid = getStationUuid(station);
	String shortname = timeseries.optString("shortname", null);
	if (stationUuid == null || shortname == null || shortname.isEmpty()) {
	    return null;
	}

	return stationUuid + "/" + shortname;
    }

    /**
     * @param timeseriesId
     * @return
     */
    public static SimpleEntry<String, String> parseTimeseriesId(String timeseriesId) {

	if (timeseriesId == null || timeseriesId.isEmpty()) {
	    return null;
	}

	int slash = timeseriesId.indexOf('/');
	if (slash <= 0 || slash >= timeseriesId.length() - 1) {
	    return null;
	}

	return new SimpleEntry<>(timeseriesId.substring(0, slash), timeseriesId.substring(slash + 1));
    }

    /**
     * @param station
     * @param timeseries
     * @param target
     */
    public static void mergeStationIntoTimeseries(PegelonlineEntity station, JSONObject timeseries, PegelonlineEntity target) {

	JSONObject stationObject = station.getObject();
	JSONObject merged = target.getObject();

	putIfAbsent(merged, STATION_UUID, getStationUuid(station));
	putIfAbsent(merged, STATION_NUMBER, stationObject.optString("number", null));
	putIfAbsent(merged, STATION_SHORTNAME, stationObject.optString("shortname", null));
	putIfAbsent(merged, STATION_LABEL, stationObject.optString("longname", null));
	putIfAbsent(merged, STATION_LAT, stationObject.opt("latitude"));
	putIfAbsent(merged, STATION_LONG, stationObject.opt("longitude"));
	putIfAbsent(merged, STATION_KM, stationObject.opt("km"));
	putIfAbsent(merged, STATION_AGENCY, stationObject.optString("agency", null));

	if (stationObject.has("water")) {
	    JSONObject water = stationObject.getJSONObject("water");
	    putIfAbsent(merged, WATER_SHORTNAME, water.optString("shortname", null));
	    putIfAbsent(merged, WATER_NAME, water.optString("longname", null));
	}

	String timeseriesId = getTimeseriesId(station, timeseries);
	putIfAbsent(merged, TIMESERIES_ID, timeseriesId);
	putIfAbsent(merged, TIMESERIES_SHORTNAME, timeseries.optString("shortname", null));
	putIfAbsent(merged, TIMESERIES_LABEL, timeseries.optString("longname", null));
	putIfAbsent(merged, UNIT_NAME, timeseries.optString("unit", null));

	if (timeseries.has("equidistance")) {
	    int equidistanceMinutes = timeseries.getInt("equidistance");
	    merged.put(PERIOD, equidistanceMinutes * 60);
	    merged.put(PERIOD_NAME, equidistanceMinutes + " minutes");
	}

	SimpleEntry<String, String> extent = getDefaultExtentAsStrings();
	merged.put(FROM, extent.getKey());
	merged.put(TO, extent.getValue());
    }

    /**
     * @return
     */
    public static SimpleEntry<String, String> getDefaultExtentAsStrings() {

	SimpleEntry<Date, Date> extent = getDefaultExtent();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	sdf.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
	return new SimpleEntry<>(sdf.format(extent.getKey()), sdf.format(extent.getValue()));
    }

    /**
     * @return
     */
    public static SimpleEntry<Date, Date> getDefaultExtent() {

	Date end = new Date();
	Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE));
	calendar.setTime(end);
	calendar.add(Calendar.DAY_OF_MONTH, -DATA_RETENTION_DAYS);
	Date begin = calendar.getTime();
	return new SimpleEntry<>(begin, end);
    }

    /**
     * @param from
     * @param to
     * @return
     */
    static SimpleEntry<Date, Date> clampToRetention(Date from, Date to) {

	SimpleEntry<Date, Date> defaultExtent = getDefaultExtent();
	Date begin = from == null ? defaultExtent.getKey() : from;
	Date end = to == null ? defaultExtent.getValue() : to;

	if (begin.before(defaultExtent.getKey())) {
	    begin = defaultExtent.getKey();
	}
	if (end.after(defaultExtent.getValue())) {
	    end = defaultExtent.getValue();
	}
	if (end.before(begin)) {
	    return defaultExtent;
	}

	return new SimpleEntry<>(begin, end);
    }

    /**
     * @param query
     * @return
     */
    private List<PegelonlineEntity> fetchStations(String query) {

	List<PegelonlineEntity> entities = new ArrayList<>();
	Optional<String> response = downloader.downloadOptionalString(buildUrl(query));
	if (response.isEmpty()) {
	    return entities;
	}

	JSONArray items = new JSONArray(response.get());
	for (int i = 0; i < items.length(); i++) {
	    entities.add(new PegelonlineEntity(items.getJSONObject(i), EntityType.STATION));
	}

	return entities;
    }

    /**
     * @param object
     * @return
     */
    private Date parseTimestamp(JSONObject object) {

	String timestamp = object.optString(MEASUREMENT_TIMESTAMP, null);
	if (timestamp == null || timestamp.isEmpty()) {
	    return null;
	}

	return ISO8601DateTimeUtils.parseISO8601ToDate(timestamp).orElse(null);
    }

    /**
     * @param date
     * @return
     */
    private String formatApiDateTime(Date date) {

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX", Locale.US);
	sdf.setTimeZone(TimeZone.getTimeZone(TIME_ZONE));
	return sdf.format(date);
    }

    /**
     * @param value
     * @return
     */
    private String encodeQueryValue(String value) {

	return URLEncoder.encode(value, StandardCharsets.UTF_8);
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
