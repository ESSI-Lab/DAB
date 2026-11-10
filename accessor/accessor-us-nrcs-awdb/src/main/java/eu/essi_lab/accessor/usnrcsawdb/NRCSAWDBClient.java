package eu.essi_lab.accessor.usnrcsawdb;

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

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * Client of the USDA NRCS Air and Water Database (AWDB) REST API (https://wcc.sc.egov.usda.gov/awdbRestApi/swagger-ui/index.html)
 *
 * @author boldrini
 */
public class NRCSAWDBClient {

    public static final String DEFAULT_ENDPOINT = "https://wcc.sc.egov.usda.gov/awdbRestApi/services/v1/";

    public static final String STATION_TRIPLET = "stationTriplet";
    public static final String STATION_ID = "stationId";
    public static final String STATION_NAME = "name";
    public static final String STATION_STATE_CODE = "stateCode";
    public static final String STATION_NETWORK_CODE = "networkCode";
    public static final String STATION_COUNTY_NAME = "countyName";
    public static final String STATION_HUC = "huc";
    public static final String STATION_ELEVATION = "elevation";
    public static final String STATION_LATITUDE = "latitude";
    public static final String STATION_LONGITUDE = "longitude";
    public static final String STATION_DATA_TIME_ZONE = "dataTimeZone";
    public static final String STATION_OPERATOR = "operator";
    public static final String STATION_ELEMENTS = "stationElements";

    public static final String ELEMENT_CODE = "elementCode";
    public static final String ELEMENT_ORDINAL = "ordinal";
    public static final String ELEMENT_HEIGHT_DEPTH = "heightDepth";
    public static final String ELEMENT_DURATION = "durationName";
    public static final String ELEMENT_STORED_UNIT = "storedUnitCode";
    public static final String ELEMENT_BEGIN_DATE = "beginDate";
    public static final String ELEMENT_END_DATE = "endDate";

    public static final String REFERENCE_NAME = "name";
    public static final String REFERENCE_DESCRIPTION = "description";
    public static final String REFERENCE_FUNCTION_CODE = "functionCode";
    public static final String REFERENCE_METRIC_UNIT = "metricUnitCode";

    /**
     * Keys added to the station object to build the harvested metadata record
     */
    public static final String RECORD_STATION_ELEMENT = "stationElement";
    public static final String RECORD_ELEMENT = "element";
    public static final String RECORD_NETWORK_NAME = "networkName";
    public static final String RECORD_TIMESERIES_ID = "timeseriesId";
    public static final String RECORD_COUNTRY_CODE = "countryCode";

    /**
     * Durations, from the finest to the coarsest
     */
    public static final List<String> DURATIONS = List.of("HOURLY", "DAILY", "SEMIMONTHLY", "MONTHLY", "SEASONAL", "WATER_YEAR",
	    "CALENDAR_YEAR");

    private static final DateTimeFormatter API_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static Map<String, JSONObject> elementsCache;

    private final String endpoint;
    private final Downloader downloader;

    public NRCSAWDBClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public NRCSAWDBClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
    }

    /**
     * @return the element reference data (names, descriptions and units), by element code
     */
    public synchronized Map<String, JSONObject> retrieveElements() {

	if (elementsCache == null || elementsCache.isEmpty()) {
	    elementsCache = retrieveReferenceList("elements", "code");
	}

	return elementsCache;
    }

    /**
     * @return the network names, by network code
     */
    public Map<String, String> retrieveNetworkNames() {

	Map<String, String> ret = new HashMap<>();
	for (JSONObject network : retrieveReferenceList("networks", "code").values()) {
	    ret.put(network.getString("code"), network.optString("name", network.getString("code")));
	}

	return ret;
    }

    /**
     * @return the ISO 3166 alpha 2 country codes, by state code (the AWDB also includes Canadian provinces)
     */
    public Map<String, String> retrieveStateCountries() {

	Map<String, String> ret = new HashMap<>();
	for (JSONObject state : retrieveReferenceList("states", "code").values()) {
	    String countryCode = state.optString("countryCode", null);
	    if (countryCode != null && !countryCode.isEmpty()) {
		ret.put(state.getString("code"), countryCode);
	    }
	}

	return ret;
    }

    /**
     * @param networkCode
     * @return the active stations of the given network, without the station elements
     */
    public List<JSONObject> retrieveStations(String networkCode) {

	return toList(download("stations?stationTriplets=" + encode("*:*:" + networkCode)));
    }

    /**
     * @param triplets
     * @param elementCodes
     * @param durations
     * @return the stations with the active station elements matching the given element codes and durations
     */
    public List<JSONObject> retrieveStationElements(List<String> triplets, List<String> elementCodes, List<String> durations) {

	String query = "stations?returnStationElements=true&stationTriplets=" + encode(String.join(",", triplets));

	if (!elementCodes.isEmpty()) {
	    query += "&elements=" + encode(String.join(",", elementCodes));
	}
	if (!durations.isEmpty()) {
	    query += "&durations=" + encode(String.join(",", durations));
	}

	return toList(download(query));
    }

    /**
     * @param timeseriesId
     * @return the station with only the station element identified by the given timeseries id, if any
     */
    public Optional<JSONObject> retrieveStationElement(NRCSAWDBTimeseriesId timeseriesId) {

	String query = "stations?returnStationElements=true&activeOnly=false&stationTriplets=" + encode(timeseriesId.getTriplet())
		+ "&elements=" + encode(timeseriesId.getElementString()) + "&durations=" + encode(timeseriesId.getDuration());

	for (JSONObject station : toList(download(query))) {

	    JSONArray elements = station.optJSONArray(STATION_ELEMENTS);
	    if (elements == null) {
		continue;
	    }

	    for (int i = 0; i < elements.length(); i++) {
		JSONObject element = elements.getJSONObject(i);
		if (timeseriesId.matches(element)) {
		    station.put(STATION_ELEMENTS, new JSONArray().put(element));
		    return Optional.of(station);
		}
	    }
	}

	return Optional.empty();
    }

    /**
     * @param timeseriesId
     * @param begin in UTC
     * @param end in UTC
     * @param timeZoneHours the station data time zone
     * @return the data of the given timeseries, as returned by the API (stationElement and values), or empty
     */
    public Optional<JSONObject> retrieveData(NRCSAWDBTimeseriesId timeseriesId, Date begin, Date end, double timeZoneHours) {

	ZoneOffset offset = getOffset(timeZoneHours);

	String beginDate = API_DATE_TIME.format(LocalDateTime.ofInstant(begin.toInstant(), offset));
	String endDate = API_DATE_TIME.format(LocalDateTime.ofInstant(end.toInstant(), offset));

	String query = "data?stationTriplets=" + encode(timeseriesId.getTriplet()) + //
		"&elements=" + encode(timeseriesId.getElementString()) + //
		"&duration=" + encode(timeseriesId.getDuration()) + //
		"&beginDate=" + encode(beginDate) + //
		"&endDate=" + encode(endDate);

	for (JSONObject station : toList(download(query))) {

	    JSONArray data = station.optJSONArray("data");
	    if (data == null) {
		continue;
	    }

	    for (int i = 0; i < data.length(); i++) {
		JSONObject entry = data.getJSONObject(i);
		JSONObject element = entry.optJSONObject(RECORD_STATION_ELEMENT);
		if (element != null && timeseriesId.matches(element)) {
		    return Optional.of(entry);
		}
	    }
	}

	return Optional.empty();
    }

    /**
     * Parses the timestamp of a data value. Values of manual measurements (e.g. snow courses) carry the actual
     * collection date; aggregated values only carry year, month and month part.
     *
     * @param value a value of the data response
     * @param timeZoneHours the station data time zone
     * @return
     */
    public static Optional<Date> parseValueDate(JSONObject value, double timeZoneHours) {

	ZoneOffset offset = getOffset(timeZoneHours);

	String date = value.optString("collectionDate", null);
	if (date == null || date.isEmpty()) {
	    date = value.optString("date", null);
	}

	try {
	    if (date != null && !date.isEmpty()) {
		if (date.length() == 10) {
		    return Optional.of(Date.from(LocalDate.parse(date).atStartOfDay().toInstant(offset)));
		}
		return Optional.of(Date.from(LocalDateTime.parse(date, API_DATE_TIME).toInstant(offset)));
	    }

	    if (value.has("year") && value.has("month")) {
		int day = "2".equals(value.optString("monthPart")) ? 16 : 1;
		return Optional.of(Date.from(LocalDate.of(value.getInt("year"), value.getInt("month"), day).atStartOfDay().toInstant(offset)));
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(NRCSAWDBClient.class).warn("Unable to parse AWDB date {}: {}", value, e.getMessage());
	}

	return Optional.empty();
    }

    /**
     * @param date an AWDB date, e.g. "1983-10-01 00:00"; dates in year 2100 mean that the station element is active
     * @param timeZoneHours
     * @return
     */
    public static Optional<Date> parseApiDate(String date, double timeZoneHours) {

	if (date == null || date.isEmpty()) {
	    return Optional.empty();
	}

	try {
	    return Optional.of(Date.from(LocalDateTime.parse(date, API_DATE_TIME).toInstant(getOffset(timeZoneHours))));
	} catch (Exception e) {
	    return Optional.empty();
	}
    }

    /**
     * @param value
     * @return
     */
    public static Optional<BigDecimal> parseValue(JSONObject value) {

	if (!value.has("value") || value.isNull("value")) {
	    return Optional.empty();
	}

	try {
	    return Optional.of(new BigDecimal(value.get("value").toString()));
	} catch (NumberFormatException e) {
	    return Optional.empty();
	}
    }

    /**
     * @param timeZoneHours
     * @return
     */
    static ZoneOffset getOffset(double timeZoneHours) {

	return ZoneOffset.ofTotalSeconds((int) Math.round(timeZoneHours * 3600));
    }

    /**
     * @param list
     * @param key
     * @return
     */
    private Map<String, JSONObject> retrieveReferenceList(String list, String key) {

	Map<String, JSONObject> ret = new HashMap<>();

	Optional<String> response = download("reference-data?referenceLists=" + list);
	if (response.isEmpty()) {
	    return ret;
	}

	JSONArray array = new JSONObject(response.get()).optJSONArray(list);
	if (array == null) {
	    return ret;
	}

	for (int i = 0; i < array.length(); i++) {
	    JSONObject item = array.getJSONObject(i);
	    ret.put(item.getString(key), item);
	}

	return ret;
    }

    /**
     * @param path
     * @return
     */
    private Optional<String> download(String path) {

	String url = endpoint + path;

	GSLoggerFactory.getLogger(getClass()).trace("Downloading {}", url);

	return downloader.downloadOptionalString(url);
    }

    /**
     * @param response
     * @return
     */
    private List<JSONObject> toList(Optional<String> response) {

	List<JSONObject> ret = new ArrayList<>();
	if (response.isEmpty()) {
	    return ret;
	}

	String body = response.get().trim();
	if (!body.startsWith("[")) {
	    // errors are returned as JSON objects, e.g. when too many station elements are requested
	    GSLoggerFactory.getLogger(getClass()).warn("Unexpected AWDB response: {}", body.length() > 300 ? body.substring(0, 300) : body);
	    return ret;
	}

	JSONArray array = new JSONArray(body);
	for (int i = 0; i < array.length(); i++) {
	    ret.add(array.getJSONObject(i));
	}

	return ret;
    }

    /**
     * @param value
     * @return
     */
    private static String encode(String value) {

	return URLEncoder.encode(value, StandardCharsets.UTF_8);
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
