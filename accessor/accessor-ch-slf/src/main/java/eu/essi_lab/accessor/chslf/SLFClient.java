package eu.essi_lab.accessor.chslf;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * Client of the SLF Measurement API (https://measurement-api.slf.ch/docs). The API only publishes the last 1, 3 or 7
 * days of measurements; older data are distributed by SLF as yearly archives on EnviDat.
 *
 * @author boldrini
 */
public class SLFClient {

    public static final String DEFAULT_ENDPOINT = "https://measurement-api.slf.ch/public/api/";

    /**
     * The longest period (in days) the API can return
     */
    public static final int DATA_RETENTION_DAYS = 7;

    private static final int[] ALLOWED_PERIODS = new int[] { 1, 3, 7 };

    public static final String STATION_CODE = "code";
    public static final String STATION_LABEL = "label";
    public static final String STATION_LAT = "lat";
    public static final String STATION_LON = "lon";
    public static final String STATION_ELEVATION = "elevation";
    public static final String STATION_COUNTRY_CODE = "country_code";
    public static final String STATION_CANTON_CODE = "canton_code";
    public static final String STATION_TYPE = "type";

    public static final String MEASUREMENT_STATION_CODE = "station_code";
    public static final String MEASUREMENT_DATE = "measure_date";

    /**
     * Keys added to the station object to build the harvested metadata record
     */
    public static final String NETWORK = "network";
    public static final String PARAMETER = "parameter";
    public static final String TIMESERIES_ID = "timeseriesId";
    public static final String FROM = "from";
    public static final String TO = "to";

    private final String endpoint;
    private final Downloader downloader;

    public SLFClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public SLFClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
    }

    /**
     * @param network
     * @return
     */
    public List<JSONObject> retrieveStations(SLFNetwork network) {

	return toList(download(network.getStationsPath()));
    }

    /**
     * Uses the last 24 hours of measurements of all the stations to find which parameters each station is actually
     * measuring
     *
     * @param network
     * @return a map from station code to the codes of the parameters having at least one value, or empty if the
     *         request failed
     */
    public Optional<Map<String, Set<String>>> retrieveAvailableParameters(SLFNetwork network) {

	Optional<String> response = download(network.getBulkMeasurementsPath());
	if (response.isEmpty()) {
	    return Optional.empty();
	}

	Map<String, Set<String>> ret = new HashMap<>();

	for (JSONObject measurement : toList(response)) {

	    String stationCode = measurement.optString(MEASUREMENT_STATION_CODE, null);
	    if (stationCode == null) {
		continue;
	    }

	    Set<String> codes = ret.computeIfAbsent(stationCode, k -> new HashSet<>());

	    for (String key : measurement.keySet()) {
		if (!measurement.isNull(key) && !key.equals(MEASUREMENT_STATION_CODE) && !key.equals(MEASUREMENT_DATE)) {
		    codes.add(key);
		}
	    }
	}

	return Optional.of(ret);
    }

    /**
     * @param network
     * @param stationCode
     * @param periodInDays one of 1, 3 or 7
     * @return the measurements of the given station, sorted as returned by the API
     */
    public List<JSONObject> retrieveMeasurements(SLFNetwork network, String stationCode, int periodInDays) {

	String path = network.getStationMeasurementsPath(stationCode);

	if (path != null) {

	    return toList(download(path + "?period_in_days=" + periodInDays));
	}

	// no per station endpoint: the bulk endpoint is filtered
	List<JSONObject> ret = new ArrayList<>();
	for (JSONObject measurement : toList(download(network.getBulkMeasurementsPath() + "?period_in_days=" + periodInDays))) {
	    if (stationCode.equals(measurement.optString(MEASUREMENT_STATION_CODE, null))) {
		ret.add(measurement);
	    }
	}

	return ret;
    }

    /**
     * @param begin
     * @return the smallest period supported by the API covering the time from the given date to now
     */
    public static int getPeriodInDays(Date begin) {

	if (begin == null) {
	    return DATA_RETENTION_DAYS;
	}

	double days = (System.currentTimeMillis() - begin.getTime()) / (1000.0 * 60 * 60 * 24);

	for (int period : ALLOWED_PERIODS) {
	    if (days <= period) {
		return period;
	    }
	}

	return DATA_RETENTION_DAYS;
    }

    /**
     * @return the time window covered by the API, from {@link #DATA_RETENTION_DAYS} days ago to now
     */
    public static SimpleEntry<Date, Date> getDefaultExtent() {

	Date end = new Date();
	Date begin = new Date(end.getTime() - DATA_RETENTION_DAYS * 24l * 60 * 60 * 1000);
	return new SimpleEntry<>(begin, end);
    }

    /**
     * @param measureDate the API timestamp, in UTC (the daily snow endpoint omits the time zone designator)
     * @return
     */
    public static Optional<Date> parseDate(String measureDate) {

	if (measureDate == null || measureDate.isEmpty()) {
	    return Optional.empty();
	}

	String date = measureDate;
	if (date.contains("T") && !date.endsWith("Z") && !date.matches(".*[+-]\\d{2}:?\\d{2}$")) {
	    date = date + "Z";
	}

	return ISO8601DateTimeUtils.parseISO8601ToDate(date);
    }

    /**
     * @param network
     * @param stationCode
     * @param parameterCode
     * @return the identifier used as online resource name
     */
    public static String buildTimeseriesId(SLFNetwork network, String stationCode, String parameterCode) {

	return network.getId() + "/" + stationCode + "/" + parameterCode;
    }

    /**
     * @param timeseriesId
     * @return the parameter, with the station code as key, or null if the identifier is not valid
     */
    public static SimpleEntry<String, SLFParameter> parseTimeseriesId(String timeseriesId) {

	if (timeseriesId == null) {
	    return null;
	}

	String[] split = timeseriesId.split("/");
	if (split.length != 3) {
	    return null;
	}

	SLFNetwork network = SLFNetwork.fromId(split[0]);
	if (network == null) {
	    return null;
	}

	SLFParameter parameter = SLFParameter.decode(network, split[2]);
	if (parameter == null) {
	    return null;
	}

	return new SimpleEntry<>(split[1], parameter);
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
	    // error messages are returned as JSON objects, e.g. {"code":"NO_DATA", ...}
	    GSLoggerFactory.getLogger(getClass()).warn("Unexpected SLF response: {}", body.length() > 200 ? body.substring(0, 200) : body);
	    return ret;
	}

	JSONArray array = new JSONArray(body);
	for (int i = 0; i < array.length(); i++) {
	    ret.add(array.getJSONObject(i));
	}

	return ret;
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
