package eu.essi_lab.accessor.ththaiwater;

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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
 * Client of the public API of ThaiWater (https://www.thaiwater.net), the national water data portal of Thailand operated by the
 * Hydro-Informatics Institute (HII), aggregating the observations of RID, EGAT, the Bangkok Metropolitan Administration and other
 * agencies.<br>
 * The stations of each {@link ThaiWaterStationType} are listed by a service together with their latest observation. Observations
 * are read from:
 * <ul>
 * <li>public/waterlevel_graph: sub-daily values (10 minutes or hourly) of the river and canal water level stations, for windows of
 * at most one year (requests are split in windows of {@value #GRAPH_WINDOW_DAYS} days, since long windows are very slow)</li>
 * <li>public/waterlevel_graph_year: daily values (reading of 06:00 local time) of a calendar year of the river water level
 * stations, available since {@value #FIRST_YEAR}</li>
 * <li>public/flow_graph: 10 minutes values of the canal flow stations, for windows of at most about one month</li>
 * <li>analyst/dam_yearly_graph: daily values of a calendar year of the large reservoirs, for decades</li>
 * </ul>
 * All the times are given in Thailand local time (UTC+7, no daylight saving)
 *
 * @author boldrini
 */
public class ThaiWaterClient {

    public static final String DEFAULT_ENDPOINT = "https://api-v3.thaiwater.net/api/v1/thaiwater30/";

    static final String TELE_WATERLEVEL_LOAD = "public/waterlevel_load";
    static final String CANAL_LOAD = "public/canal_waterlevel";
    static final String FLOW_LOAD = "public/flow";
    static final String DAM_LOAD = "analyst/dam";

    static final String WATERLEVEL_GRAPH = "public/waterlevel_graph";
    static final String WATERLEVEL_YEAR_GRAPH = "public/waterlevel_graph_year";
    static final String FLOW_GRAPH = "public/flow_graph";
    static final String DAM_YEAR_GRAPH = "analyst/dam_yearly_graph";

    /**
     * Time zone of the ThaiWater times
     */
    static final ZoneId ZONE = ZoneId.of("Asia/Bangkok");

    /**
     * Number of days of each request of sub-daily values (the flow graph refuses windows longer than about one month)
     */
    static final int GRAPH_WINDOW_DAYS = 30;

    /**
     * The sub-daily values of the river water level stations are read for the observations of the last year (older observations
     * are read as daily values), or for any requested period not longer than {@link #SHORT_WINDOW}
     */
    static final long RECENT_WINDOW = TimeUnit.DAYS.toMillis(366);
    static final long SHORT_WINDOW = TimeUnit.DAYS.toMillis(31);

    /**
     * First year of the daily values of the river water level stations (start of the ThaiWater archive)
     */
    static final int FIRST_YEAR = 2012;

    /**
     * First year searched for the daily values of the reservoirs
     */
    static final int DAM_FIRST_YEAR = 1960;

    /**
     * First date searched for the observations of the canal stations, and width of the windows probed by the search
     */
    static final LocalDate GRAPH_FIRST_DATE = LocalDate.of(FIRST_YEAR, 1, 1);
    static final int PROBE_WINDOW_DAYS = 7;

    /**
     * Width of the window read to refresh the latest observation of the stations with sub-daily values
     */
    static final long LATEST_WINDOW = TimeUnit.DAYS.toMillis(2);

    /**
     * Maximum number of responses of the yearly services kept in memory
     */
    private static final int CACHE_SIZE = 32;

    // keys of the series
    public static final String SERIES_ID = "seriesId";
    public static final String STATION_TYPE = "stationType";
    public static final String STATION_ID = "stationId";
    public static final String STATION_CODE = "stationCode";
    public static final String STATION_NAME = "stationName";
    public static final String STATION_NAME_TH = "stationNameTh";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String AGENCY = "agency";
    public static final String AGENCY_SHORT_NAME = "agencyShortName";
    public static final String BASIN = "basin";
    public static final String PROVINCE = "province";
    public static final String DISTRICT = "district";
    public static final String RIVER = "river";
    public static final String PARAMETER = "parameter";
    public static final String LATEST_VALUE = "latestValue";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    static final String SERIES_ID_SEPARATOR = "|";

    private static final DateTimeFormatter LOCAL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter[] LOCAL_DATE_TIME_FORMATS = new DateTimeFormatter[] { //
	    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"), //
	    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") };

    private final String endpoint;
    private final Downloader downloader;

    private final Map<String, JSONObject> cache = new LinkedHashMap<String, JSONObject>(CACHE_SIZE, 0.75f, true) {

	private static final long serialVersionUID = 1L;

	@Override
	protected boolean removeEldestEntry(Map.Entry<String, JSONObject> eldest) {

	    return size() > CACHE_SIZE;
	}
    };

    /**
     * First year of the reservoirs found by the search of the first observation, used as hint for their other properties
     */
    private final HashMap<String, Integer> damFirstYears = new HashMap<>();

    /**
     *
     */
    public ThaiWaterClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public ThaiWaterClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 5);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 30);
    }

    /**
     * Retrieves the time series of the stations of the given types, one for each property having a latest observation
     *
     * @param types
     * @return the series sorted by station and parameter
     * @throws GSException
     */
    public List<ThaiWaterEntity> retrieveSeries(Collection<ThaiWaterStationType> types) throws GSException {

	List<ThaiWaterEntity> ret = new ArrayList<>();

	for (ThaiWaterStationType type : types) {

	    JSONArray entries = getLoadEntries(type);

	    int count = 0;
	    for (int i = 0; i < entries.length(); i++) {
		JSONObject entry = entries.optJSONObject(i);
		if (entry != null) {
		    List<ThaiWaterEntity> series = createSeries(entry, type);
		    ret.addAll(series);
		    count += series.size();
		}
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Retrieved {} ThaiWater series of {} stations ({})", count, entries.length(),
		    type.getCode());
	}

	ret.sort(Comparator.comparing(ThaiWaterEntity::getStationKey).thenComparing(ThaiWaterEntity::getSeriesId));

	return ret;
    }

    /**
     * @param type
     * @return the entries of the list of the latest observations of the given type
     * @throws GSException
     */
    private JSONArray getLoadEntries(ThaiWaterStationType type) throws GSException {

	JSONObject response = getJSON(getLoadPath(type)).orElse(new JSONObject());

	switch (type) {
	case TELE_WATERLEVEL:
	    JSONObject waterLevels = response.optJSONObject("waterlevel_data");
	    return waterLevels == null ? new JSONArray() : waterLevels.optJSONArray("data", new JSONArray());
	case DAM:
	    JSONObject dams = response.optJSONObject("data");
	    return dams == null ? new JSONArray() : dams.optJSONArray("dam_daily", new JSONArray());
	default:
	    return response.optJSONArray("data", new JSONArray());
	}
    }

    /**
     * @param type
     * @return
     */
    static String getLoadPath(ThaiWaterStationType type) {

	switch (type) {
	case CANAL:
	    return CANAL_LOAD;
	case FLOW:
	    return FLOW_LOAD;
	case DAM:
	    return DAM_LOAD;
	case TELE_WATERLEVEL:
	default:
	    return TELE_WATERLEVEL_LOAD;
	}
    }

    /**
     * @param entry an entry of the list of the latest observations
     * @param type
     * @return the series of the station, one for each property having a latest observation
     */
    static List<ThaiWaterEntity> createSeries(JSONObject entry, ThaiWaterStationType type) {

	List<ThaiWaterEntity> ret = new ArrayList<>();

	String stationKey;
	String prefix;
	String dateKey;

	switch (type) {
	case CANAL:
	    stationKey = "station";
	    prefix = "canal_";
	    dateKey = "canal_datetime";
	    break;
	case FLOW:
	    stationKey = "station";
	    prefix = "flow_";
	    dateKey = "flow_datetime";
	    break;
	case DAM:
	    stationKey = "dam";
	    prefix = "dam_";
	    dateKey = "dam_date";
	    break;
	case TELE_WATERLEVEL:
	default:
	    stationKey = "station";
	    prefix = "tele_station_";
	    dateKey = "waterlevel_datetime";
	    break;
	}

	JSONObject station = entry.optJSONObject(stationKey);
	if (station == null || !station.has("id")) {
	    return ret;
	}

	Optional<Date> latest = parseDate(entry.optString(dateKey, null));
	if (latest.isEmpty()) {
	    return ret;
	}

	String stationId = station.get("id").toString();

	JSONObject object = new JSONObject();
	object.put(STATION_TYPE, type.getCode());
	object.put(STATION_ID, stationId);
	object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(latest.get()));

	putString(object, STATION_CODE, station.optString(prefix + "oldcode", ""));

	JSONObject name = station.optJSONObject(prefix + "name");
	putString(object, STATION_NAME, getName(name));
	if (name != null) {
	    putString(object, STATION_NAME_TH, name.optString("th", ""));
	}

	Object lat = station.opt(type == ThaiWaterStationType.DAM ? "dam_lat" : prefix + "lat");
	Object lon = station.opt(type == ThaiWaterStationType.DAM ? "dam_long" : prefix + "long");
	if (lat instanceof Number && lon instanceof Number) {
	    object.put(LAT, lat);
	    object.put(LON, lon);
	}

	JSONObject agency = entry.optJSONObject("agency");
	if (agency != null) {
	    putString(object, AGENCY, getName(agency.optJSONObject("agency_name")));
	    JSONObject shortName = agency.optJSONObject("agency_shortname");
	    putString(object, AGENCY_SHORT_NAME, shortName == null ? "" : shortName.optString("en", ""));
	}

	JSONObject basin = entry.optJSONObject("basin");
	if (basin != null) {
	    putString(object, BASIN, getName(basin.optJSONObject("basin_name")));
	}

	JSONObject geocode = entry.optJSONObject("geocode");
	if (geocode != null) {
	    putString(object, PROVINCE, getName(geocode.optJSONObject("province_name")));
	    putString(object, DISTRICT, getName(geocode.optJSONObject("amphoe_name")));
	}

	putString(object, RIVER, entry.optString("river_name", ""));

	for (ThaiWaterParameter parameter : ThaiWaterParameter.values()) {

	    if (parameter.getStationType() != type) {
		continue;
	    }

	    Optional<BigDecimal> value = parseDecimal(entry.opt(parameter.getKey()));
	    if (value.isEmpty()) {
		continue;
	    }

	    // the reservoirs without level measurements report 0
	    if (parameter == ThaiWaterParameter.DAM_LEVEL && value.get().signum() == 0) {
		continue;
	    }

	    JSONObject series = new JSONObject(object.toString());
	    series.put(PARAMETER, parameter.name());
	    series.put(SERIES_ID, buildSeriesId(type, stationId, parameter));
	    series.put(LATEST_VALUE, value.get().toPlainString());

	    ret.add(new ThaiWaterEntity(series));
	}

	return ret;
    }

    /**
     * @param object
     * @param key
     * @param value
     */
    private static void putString(JSONObject object, String key, String value) {

	if (value != null && !value.trim().isEmpty()) {
	    object.put(key, value.trim());
	}
    }

    /**
     * @param name a multilingual name, e.g. {"th":"...","en":"Tha Chin Basin"}
     * @return the English name, or the Thai name if the English one is not available
     */
    static String getName(JSONObject name) {

	if (name == null) {
	    return "";
	}

	String en = name.optString("en", "").trim();
	return en.isEmpty() ? name.optString("th", "").trim() : en;
    }

    /**
     * Retrieves the time of the first observation of the series:
     * <ul>
     * <li>river water level stations: the first daily value, scanning the years since {@value #FIRST_YEAR}</li>
     * <li>reservoirs: the first daily value of the first year with values, found by a binary search on the years since
     * {@value #DAM_FIRST_YEAR}</li>
     * <li>canal stations: the first value of the first week with values, found by a binary search on the weeks since
     * {@value #FIRST_YEAR}</li>
     * </ul>
     * The binary searches assume that the series have no gaps longer than the probed periods
     *
     * @param series
     * @return
     * @throws GSException
     */
    public Optional<Date> retrieveFirstObservation(ThaiWaterEntity series) throws GSException {

	ThaiWaterParameter parameter = getParameter(series);

	Date end = ISO8601DateTimeUtils.parseISO8601ToDate(series.getString(END)).orElse(new Date());
	int endYear = toLocalDate(end).getYear();

	switch (parameter.getStationType()) {
	case TELE_WATERLEVEL:

	    for (int year = FIRST_YEAR; year <= endYear; year++) {
		TreeMap<Date, BigDecimal> values = readYear(series, parameter, year);
		if (!values.isEmpty()) {
		    return Optional.of(values.firstKey());
		}
	    }

	    // stations without daily values
	    TreeMap<Date, BigDecimal> recent = readGraph(series, parameter, new Date(end.getTime() - TimeUnit.DAYS.toMillis(30)), end);
	    return recent.isEmpty() ? Optional.empty() : Optional.of(recent.firstKey());

	case DAM:

	    Optional<Integer> firstYear = findFirstDamYear(series, parameter, endYear);
	    if (firstYear.isEmpty()) {
		return Optional.empty();
	    }

	    damFirstYears.putIfAbsent(series.getStationKey(), firstYear.get());
	    return Optional.of(readYear(series, parameter, firstYear.get()).firstKey());

	default:

	    LocalDate endDate = toLocalDate(end);
	    int high = (int) (ChronoUnit.DAYS.between(GRAPH_FIRST_DATE, endDate) / PROBE_WINDOW_DAYS);
	    if (high < 0) {
		return Optional.empty();
	    }

	    int low = 0;
	    TreeMap<Date, BigDecimal> found = readWeek(series, parameter, high);
	    if (found.isEmpty()) {
		return Optional.empty();
	    }

	    while (low < high) {
		int middle = (low + high) / 2;
		TreeMap<Date, BigDecimal> values = readWeek(series, parameter, middle);
		if (values.isEmpty()) {
		    low = middle + 1;
		} else {
		    high = middle;
		    found = values;
		}
	    }

	    return Optional.of(found.firstKey());
	}
    }

    /**
     * @param series
     * @param parameter
     * @param endYear
     * @return the first year with daily values of the reservoir
     * @throws GSException
     */
    private Optional<Integer> findFirstDamYear(ThaiWaterEntity series, ThaiWaterParameter parameter, int endYear)
	    throws GSException {

	// the properties of a reservoir usually start in the same year
	Integer hint = damFirstYears.get(series.getStationKey());
	if (hint != null && hint <= endYear && !readYear(series, parameter, hint).isEmpty()
		&& (hint == DAM_FIRST_YEAR || readYear(series, parameter, hint - 1).isEmpty())) {
	    return Optional.of(hint);
	}

	int low = DAM_FIRST_YEAR;
	int high = endYear;

	if (readYear(series, parameter, high).isEmpty()) {
	    return Optional.empty();
	}

	while (low < high) {
	    int middle = (low + high) / 2;
	    if (readYear(series, parameter, middle).isEmpty()) {
		low = middle + 1;
	    } else {
		high = middle;
	    }
	}

	return Optional.of(low);
    }

    /**
     * @param series
     * @param parameter
     * @param week index of the week since {@link #GRAPH_FIRST_DATE}
     * @return
     * @throws GSException
     */
    private TreeMap<Date, BigDecimal> readWeek(ThaiWaterEntity series, ThaiWaterParameter parameter, int week) throws GSException {

	LocalDate from = GRAPH_FIRST_DATE.plusDays((long) week * PROBE_WINDOW_DAYS);
	return readGraphDays(series, parameter, from, from.plusDays(PROBE_WINDOW_DAYS - 1));
    }

    /**
     * @param series
     * @return the time of the latest observation currently available, if any
     * @throws GSException
     */
    public Optional<Date> retrieveLatestObservation(ThaiWaterEntity series) throws GSException {

	ThaiWaterParameter parameter = getParameter(series);
	Date now = new Date();

	TreeMap<Date, BigDecimal> values = parameter.getStationType() == ThaiWaterStationType.DAM
		? readYear(series, parameter, toLocalDate(now).getYear())
		: readGraph(series, parameter, new Date(now.getTime() - LATEST_WINDOW), now);

	return values.isEmpty() ? Optional.empty() : Optional.of(values.lastKey());
    }

    /**
     * Retrieves the valid observations of the given period. The reservoirs have only daily values. The river water level stations
     * have sub-daily values, read for the observations of the last year or for short periods (see {@link #SHORT_WINDOW}), and
     * daily values, read for older observations of long periods and when sub-daily values are not available. The canal stations
     * have only sub-daily values
     *
     * @param series
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(ThaiWaterEntity series, Date begin, Date end) throws GSException {

	ThaiWaterParameter parameter = getParameter(series);

	switch (parameter.getStationType()) {
	case DAM:
	    return readYears(series, parameter, begin, end);

	case TELE_WATERLEVEL:

	    Date graphBegin = begin;
	    if (end.getTime() - begin.getTime() > SHORT_WINDOW) {
		Date recent = new Date(System.currentTimeMillis() - RECENT_WINDOW);
		graphBegin = recent.after(begin) ? recent : begin;
	    }

	    TreeMap<Date, BigDecimal> ret = new TreeMap<>();
	    if (begin.before(graphBegin)) {
		Date dailyEnd = graphBegin.after(end) ? end : new Date(graphBegin.getTime() - 1);
		ret.putAll(readYears(series, parameter, begin, dailyEnd));
	    }

	    if (!graphBegin.after(end)) {
		TreeMap<Date, BigDecimal> graph = readGraph(series, parameter, graphBegin, end);
		ret.putAll(graph.isEmpty() ? readYears(series, parameter, graphBegin, end) : graph);
	    }

	    return ret;

	default:
	    return readGraph(series, parameter, begin, end);
	}
    }

    /**
     * @param series
     * @param parameter
     * @param begin
     * @param end
     * @return the daily values of the period
     * @throws GSException
     */
    private TreeMap<Date, BigDecimal> readYears(ThaiWaterEntity series, ThaiWaterParameter parameter, Date begin, Date end)
	    throws GSException {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	int firstYear = Math.max(toLocalDate(begin).getYear(),
		parameter.getStationType() == ThaiWaterStationType.DAM ? DAM_FIRST_YEAR : FIRST_YEAR);

	for (int year = firstYear; year <= toLocalDate(end).getYear(); year++) {
	    ret.putAll(subMap(readYear(series, parameter, year), begin, end));
	}

	return ret;
    }

    /**
     * @param series
     * @param parameter
     * @param year
     * @return the daily values of the given year
     * @throws GSException
     */
    TreeMap<Date, BigDecimal> readYear(ThaiWaterEntity series, ThaiWaterParameter parameter, int year) throws GSException {

	String path = getYearPath(series, parameter, year);

	JSONObject response = cache.get(path);
	if (response == null) {
	    response = getJSON(path).orElse(new JSONObject());
	    cache.put(path, response);
	}

	return parseYear(response, parameter);
    }

    /**
     * @param series
     * @param parameter
     * @param year
     * @return
     */
    static String getYearPath(ThaiWaterEntity series, ThaiWaterParameter parameter, int year) {

	if (parameter.getStationType() == ThaiWaterStationType.DAM) {
	    return DAM_YEAR_GRAPH + "?data_type=" + parameter.getKey() + "&dam_id=" + series.getStationId() + "&year=" + year;
	}

	return WATERLEVEL_YEAR_GRAPH + "?station_type=" + ThaiWaterStationType.TELE_WATERLEVEL.getCode() + "&station_id="
		+ series.getStationId() + "&year=" + year;
    }

    /**
     * @param response the response of the yearly graph of a water level station or of a reservoir
     * @param parameter
     * @return
     */
    static TreeMap<Date, BigDecimal> parseYear(JSONObject response, ThaiWaterParameter parameter) {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	JSONObject data = response.optJSONObject("data");
	JSONArray graphData = data == null ? null : data.optJSONArray("graph_data");
	if (graphData == null) {
	    return ret;
	}

	boolean dam = parameter.getStationType() == ThaiWaterStationType.DAM;

	for (int i = 0; i < graphData.length(); i++) {
	    JSONObject year = graphData.optJSONObject(i);
	    if (year != null) {
		ret.putAll(parseRows(year.optJSONArray("data"), dam ? "date" : "datetime", dam ? "value" : parameter.getKey()));
	    }
	}

	return ret;
    }

    /**
     * @param series
     * @param parameter
     * @param begin
     * @param end
     * @return the sub-daily values of the period, read in windows of {@value #GRAPH_WINDOW_DAYS} days
     * @throws GSException
     */
    private TreeMap<Date, BigDecimal> readGraph(ThaiWaterEntity series, ThaiWaterParameter parameter, Date begin, Date end)
	    throws GSException {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	LocalDate last = toLocalDate(end);
	LocalDate from = toLocalDate(begin);

	while (!from.isAfter(last)) {

	    LocalDate to = from.plusDays(GRAPH_WINDOW_DAYS - 1);
	    if (to.isAfter(last)) {
		to = last;
	    }

	    ret.putAll(subMap(readGraphDays(series, parameter, from, to), begin, end));

	    from = to.plusDays(1);
	}

	return ret;
    }

    /**
     * @param series
     * @param parameter
     * @param from first local day
     * @param to last local day (included)
     * @return
     * @throws GSException
     */
    private TreeMap<Date, BigDecimal> readGraphDays(ThaiWaterEntity series, ThaiWaterParameter parameter, LocalDate from, LocalDate to)
	    throws GSException {

	Optional<JSONObject> response = getJSON(getGraphPath(series, parameter, from, to));

	return response.isPresent() ? parseGraph(response.get(), parameter) : new TreeMap<>();
    }

    /**
     * @param series
     * @param parameter
     * @param from
     * @param to
     * @return
     */
    static String getGraphPath(ThaiWaterEntity series, ThaiWaterParameter parameter, LocalDate from, LocalDate to) {

	String start = LOCAL_DATE_FORMAT.format(from);
	String end = LOCAL_DATE_FORMAT.format(to);

	if (parameter.getStationType() == ThaiWaterStationType.FLOW) {
	    return FLOW_GRAPH + "?station_id=" + series.getStationId() + "&date_start=" + start + "&date_end=" + end;
	}

	return WATERLEVEL_GRAPH + "?station_type=" + parameter.getStationType().getCode() + "&station_id=" + series.getStationId()
		+ "&start_date=" + start + "&end_date=" + end;
    }

    /**
     * @param response the response of the water level graph or of the flow graph
     * @param parameter
     * @return
     */
    static TreeMap<Date, BigDecimal> parseGraph(JSONObject response, ThaiWaterParameter parameter) {

	if (parameter.getStationType() == ThaiWaterStationType.FLOW) {
	    return parseRows(response.optJSONArray("data"), "flow_datetime", parameter.getGraphKey());
	}

	JSONObject data = response.optJSONObject("data");
	return parseRows(data == null ? null : data.optJSONArray("graph_data"), "datetime", parameter.getGraphKey());
    }

    /**
     * @param rows
     * @param dateKey
     * @param valueKey
     * @return the values of the rows, missing values excluded
     */
    static TreeMap<Date, BigDecimal> parseRows(JSONArray rows, String dateKey, String valueKey) {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	if (rows == null || valueKey == null) {
	    return ret;
	}

	for (int i = 0; i < rows.length(); i++) {

	    JSONObject row = rows.optJSONObject(i);
	    if (row == null) {
		continue;
	    }

	    Optional<BigDecimal> value = parseDecimal(row.opt(valueKey));
	    if (value.isEmpty()) {
		continue;
	    }

	    parseDate(row.optString(dateKey, null)).ifPresent(date -> ret.put(date, value.get()));
	}

	return ret;
    }

    /**
     * @param values
     * @param begin
     * @param end
     * @return
     */
    private static TreeMap<Date, BigDecimal> subMap(TreeMap<Date, BigDecimal> values, Date begin, Date end) {

	return new TreeMap<>(values.subMap(begin, true, end, true));
    }

    /**
     * @param type
     * @param stationId
     * @param parameter
     * @return
     */
    static String buildSeriesId(ThaiWaterStationType type, String stationId, ThaiWaterParameter parameter) {

	return type.getCode() + SERIES_ID_SEPARATOR + stationId + SERIES_ID_SEPARATOR + parameter.name();
    }

    /**
     * @param seriesId e.g. "tele_waterlevel|737|WATER_LEVEL"
     * @return the series identified by the given identifier, or null if not valid
     */
    public static ThaiWaterEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	String[] split = seriesId.split("\\" + SERIES_ID_SEPARATOR, -1);
	if (split.length != 3 || split[1].isEmpty()) {
	    return null;
	}

	Optional<ThaiWaterStationType> type = ThaiWaterStationType.decode(split[0]);
	Optional<ThaiWaterParameter> parameter = ThaiWaterParameter.decode(split[2]);
	if (type.isEmpty() || parameter.isEmpty() || parameter.get().getStationType() != type.get()) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, buildSeriesId(type.get(), split[1], parameter.get()));
	object.put(STATION_TYPE, type.get().getCode());
	object.put(STATION_ID, split[1]);
	object.put(PARAMETER, parameter.get().name());

	return new ThaiWaterEntity(object);
    }

    /**
     * @param series
     * @return
     * @throws GSException
     */
    private ThaiWaterParameter getParameter(ThaiWaterEntity series) throws GSException {

	return series.getParameter().orElseThrow(() -> createException("Invalid ThaiWater series " + series.getSeriesId(), null));
    }

    /**
     * @param text a local time, e.g. "2026-10-28 02:30", "2026-10-28 00:00:00", "2026-10-27" or "2026-01-01T00:00:00+07:00"
     * @return
     */
    static Optional<Date> parseDate(String text) {

	if (text == null || text.isBlank() || text.equals("null")) {
	    return Optional.empty();
	}

	String trimmed = text.trim();

	try {

	    if (trimmed.contains("T")) {
		return Optional.of(Date.from(OffsetDateTime.parse(trimmed).toInstant()));
	    }

	    if (trimmed.length() == 10) {
		return Optional.of(Date.from(LocalDate.parse(trimmed, LOCAL_DATE_FORMAT).atStartOfDay(ZONE).toInstant()));
	    }

	} catch (DateTimeParseException e) {

	    return ISO8601DateTimeUtils.parseISO8601ToDate(trimmed);
	}

	for (DateTimeFormatter format : LOCAL_DATE_TIME_FORMATS) {
	    try {
		return Optional.of(Date.from(LocalDateTime.parse(trimmed, format).atZone(ZONE).toInstant()));
	    } catch (DateTimeParseException e) {
		// next format
	    }
	}

	return Optional.empty();
    }

    /**
     * @param date
     * @return the local day of the given time
     */
    static LocalDate toLocalDate(Date date) {

	return date.toInstant().atZone(ZONE).toLocalDate();
    }

    /**
     * @param value a number, a string or null
     * @return
     */
    static Optional<BigDecimal> parseDecimal(Object value) {

	if (value == null || value == JSONObject.NULL) {
	    return Optional.empty();
	}

	try {
	    String text = value.toString().trim();
	    return text.isEmpty() ? Optional.empty() : Optional.of(new BigDecimal(text));
	} catch (NumberFormatException e) {
	    return Optional.empty();
	}
    }

    /**
     * @param path
     * @return the response, or empty if the service reports no data or an error (e.g. "limit date range")
     * @throws GSException
     */
    private Optional<JSONObject> getJSON(String path) throws GSException {

	String url = endpoint + path;

	try {

	    HttpResponse<InputStream> response = downloader.downloadResponse(url);

	    String text;
	    try (InputStream stream = response.body()) {
		text = stream == null ? "" : IOUtils.toString(stream, StandardCharsets.UTF_8);
	    }

	    if (response.statusCode() == 404) {
		return Optional.empty();
	    }

	    if (response.statusCode() != 200) {
		throw createException("ThaiWater " + path + " returned HTTP " + response.statusCode(), null);
	    }

	    if (text.isBlank()) {
		return Optional.empty();
	    }

	    // the lists of the latest observations have no result field
	    JSONObject object = new JSONObject(text);
	    if (object.has("result") && !"OK".equals(object.optString("result"))) {
		GSLoggerFactory.getLogger(getClass()).warn("ThaiWater {} returned {}", path, object.opt("data"));
		return Optional.empty();
	    }

	    return Optional.of(object);

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("ThaiWater " + path + " request failed", e);
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
     * @param endpoint e.g. "https://api-v3.thaiwater.net/api/v1/thaiwater30/" or "https://api-v3.thaiwater.net/api/v1/thaiwater30/public"
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

	// the services are published under both the "public" and the "analyst" paths
	if (normalized.endsWith("/public/")) {
	    normalized = normalized.substring(0, normalized.length() - "public/".length());
	}

	return normalized;
    }
}
