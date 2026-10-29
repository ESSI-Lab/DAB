package eu.essi_lab.accessor.czchmi;

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
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Client of the CHMI hydrology open data (https://opendata.chmi.cz/hydrology/), a static file tree of JSON files:
 * <ul>
 * <li>now/data/{objID}.json: operational (not validated) 10 minutes data of the current day</li>
 * <li>recent/data/{yyyyMMdd}_{objID}.json: operational data of each previous day of the current year</li>
 * <li>historical/data/{directory}/H_{objID}_{code}_{yyyy}.json: validated data, one file per year</li>
 * <li>now/metadata/meta1.json, now/metadata/meta3.json, historical/metadata/meta1.json: station tables</li>
 * </ul>
 *
 * @author boldrini
 */
public class CHMIClient {

    public static final String DEFAULT_ENDPOINT = "https://opendata.chmi.cz/hydrology/";

    static final String NOW_DATA = "now/data/";
    static final String RECENT_DATA = "recent/data/";
    static final String HISTORICAL_DATA = "historical/data/";

    static final String NOW_STATIONS = "now/metadata/meta1.json";
    static final String NOW_STATION_DETAILS = "now/metadata/meta3.json";
    static final String HISTORICAL_STATIONS = "historical/metadata/meta1.json";

    // keys of the station tables
    public static final String OBJ_ID = "objID";
    public static final String DBC = "DBC";
    public static final String STATION_NAME = "STATION_NAME";
    public static final String STREAM_NAME = "STREAM_NAME";
    public static final String LAT = "GEOGR1";
    public static final String LON = "GEOGR2";
    public static final String ELEVATION = "WGS_ELE";
    public static final String CATCHMENT_AREA = "PLO_STA";
    public static final String BASIN_CODE = "HLGP4";
    public static final String OPERATOR = "PROVOZOVATEL";
    public static final String RIVER_KM = "RICKM";
    public static final String LOCATION_DESCRIPTION = "POPIS_UP";
    public static final String WEB_PAGE = "WWW_CHMI";

    /**
     * Station table keys kept in the harvested records
     */
    static final String[] STATION_KEYS = new String[] { OBJ_ID, DBC, STATION_NAME, STREAM_NAME, LAT, LON, ELEVATION, CATCHMENT_AREA,
	    BASIN_CODE, OPERATOR, RIVER_KM, LOCATION_DESCRIPTION, WEB_PAGE };

    // keys added by the client
    public static final String OPERATIONAL = "operational";
    public static final String SERIES_ID = "seriesId";
    public static final String PARAMETER = "parameter";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    private static final String SERIES_ID_SEPARATOR = "|";

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final Pattern RECENT_FILE = Pattern.compile("href=\"(\\d{8})_([^\"]+)\\.json\"");

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public CHMIClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public CHMIClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 5);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    /**
     * What is published for each station: the station tables, the years of the yearly files of each historical dataset and the
     * first day of the recent operational files
     */
    public static class Inventory {

	final TreeMap<String, JSONObject> stations = new TreeMap<>();
	final Map<CHMIProduct, Map<String, int[]>> years = new EnumMap<>(CHMIProduct.class);
	final Map<String, LocalDate> recentBegins = new HashMap<>();

	/**
	 * @return the stations, sorted by identifier
	 */
	public TreeMap<String, JSONObject> getStations() {

	    return stations;
	}

	/**
	 * @param product
	 * @param stationId
	 * @return the first and the last year of the files of the station, or null if none
	 */
	public int[] getYears(CHMIProduct product, String stationId) {

	    return years.getOrDefault(product, new HashMap<>()).get(stationId);
	}

	/**
	 * @param stationId
	 * @return the day of the first recent file of the station, or null if none
	 */
	public LocalDate getRecentBegin(String stationId) {

	    return recentBegins.get(stationId);
	}
    }

    /**
     * Downloads the station tables and the directory listings needed to harvest the given series types (about 10 requests, 20-50
     * MB of listings)
     *
     * @param parameters
     * @return
     * @throws GSException
     */
    public Inventory retrieveInventory(Collection<CHMIParameter> parameters) throws GSException {

	Inventory ret = new Inventory();

	for (JSONObject row : parseTable(new JSONObject(get(HISTORICAL_STATIONS)))) {
	    ret.stations.put(row.getString(OBJ_ID), selectStationKeys(row, new JSONObject()));
	}

	for (JSONObject row : parseTable(new JSONObject(get(NOW_STATIONS)))) {
	    String id = row.getString(OBJ_ID);
	    JSONObject station = selectStationKeys(row, ret.stations.getOrDefault(id, new JSONObject()));
	    station.put(OPERATIONAL, true);
	    ret.stations.put(id, station);
	}

	// additional details (elevation, operator, ...) of the operational stations
	getOptional(NOW_STATION_DETAILS).ifPresent(text -> {
	    for (JSONObject row : parseTable(new JSONObject(text))) {
		JSONObject station = ret.stations.get(row.optString(OBJ_ID));
		if (station != null) {
		    selectStationKeys(row, station);
		}
	    }
	});

	Set<CHMIProduct> products = new HashSet<>();
	for (CHMIParameter parameter : parameters) {
	    products.add(parameter.getProduct());
	    parameter.getHistoricalCounterpart().ifPresent(p -> products.add(p.getProduct()));
	}
	if (products.contains(CHMIProduct.HOURLY)) {
	    // the daily files tell which hourly series are available
	    products.add(CHMIProduct.DAILY);
	}

	for (CHMIProduct product : CHMIProduct.values()) {

	    if (!products.contains(product)) {
		continue;
	    }

	    if (product.isHistorical()) {
		ret.years.put(product, parseListing(get(HISTORICAL_DATA + product.getDirectory() + "/"), product));
	    } else {
		ret.recentBegins.putAll(parseRecentListing(get(RECENT_DATA)));
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} CHMI stations", ret.stations.size());

	return ret;
    }

    /**
     * Retrieves the series of the given station: the files of the current day and the latest yearly files of the datasets with a
     * variable set of series are read (at most 4 requests)
     *
     * @param inventory
     * @param stationId
     * @param parameters
     * @return
     * @throws GSException
     */
    public List<CHMIEntity> retrieveSeries(Inventory inventory, String stationId, Collection<CHMIParameter> parameters)
	    throws GSException {

	List<CHMIEntity> ret = new ArrayList<>();

	JSONObject station = inventory.stations.get(stationId);
	if (station == null) {
	    return ret;
	}

	// historical series already included in the operational series
	Set<CHMIParameter> included = new HashSet<>();

	List<CHMIParameter> operational = filter(parameters, CHMIProduct.OPERATIONAL);

	if (station.optBoolean(OPERATIONAL) && !operational.isEmpty()) {

	    Map<String, TreeMap<Date, BigDecimal>> today = readOperational(NOW_DATA + stationId + ".json");
	    Map<String, TreeMap<Date, BigDecimal>> yesterday = null;

	    for (CHMIParameter parameter : operational) {

		TreeMap<Date, BigDecimal> values = today.get(parameter.name());

		if (values == null || values.isEmpty()) {
		    // just after midnight
		    if (yesterday == null) {
			yesterday = readOperational(getRecentPath(stationId, LocalDate.now(ZoneOffset.UTC).minusDays(1)));
		    }
		    values = yesterday.get(parameter.name());
		}

		if (values == null || values.isEmpty()) {
		    continue;
		}

		Date begin = null;

		Optional<CHMIParameter> counterpart = parameter.getHistoricalCounterpart();
		if (counterpart.isPresent()) {
		    int[] years = inventory.getYears(counterpart.get().getProduct(), stationId);
		    if (years != null) {
			begin = getYearBegin(years[0]);
			included.add(counterpart.get());
		    }
		}

		LocalDate recentBegin = inventory.getRecentBegin(stationId);
		if (begin == null && recentBegin != null) {
		    begin = getDayBegin(recentBegin);
		}

		if (begin == null) {
		    begin = values.firstKey();
		}

		ret.add(createSeries(station, parameter, begin, values.lastKey()));
	    }
	}

	Map<String, TreeMap<Date, BigDecimal>> daily = null;

	for (CHMIProduct product : CHMIProduct.values()) {

	    if (!product.isHistorical()) {
		continue;
	    }

	    List<CHMIParameter> wanted = filter(parameters, product);
	    wanted.removeAll(included);

	    boolean dailyNeeded = product == CHMIProduct.DAILY && !filter(parameters, CHMIProduct.HOURLY).isEmpty();
	    if (wanted.isEmpty() && !dailyNeeded) {
		continue;
	    }

	    int[] years = inventory.getYears(product, stationId);
	    if (years == null) {
		continue;
	    }

	    if (product.isProbed()) {

		Map<String, TreeMap<Date, BigDecimal>> latest = readHistorical(product, stationId, years[1]);
		if (product == CHMIProduct.DAILY) {
		    daily = latest;
		}

		for (CHMIParameter parameter : wanted) {
		    TreeMap<Date, BigDecimal> values = latest.get(parameter.name());
		    if (values != null && !values.isEmpty()) {
			Date begin = years[0] == years[1] ? values.firstKey() : getYearBegin(years[0]);
			ret.add(createSeries(station, parameter, begin, values.lastKey()));
		    }
		}

		continue;
	    }

	    for (CHMIParameter parameter : wanted) {

		if (product == CHMIProduct.HOURLY && daily != null) {
		    CHMIParameter dailyParameter = parameter == CHMIParameter.HH ? CHMIParameter.HD : CHMIParameter.QD;
		    if (!daily.containsKey(dailyParameter.name())) {
			continue;
		    }
		}

		ret.add(createSeries(station, parameter, getYearBegin(years[0]), getYearEnd(years[1])));
	    }
	}

	return ret;
    }

    /**
     * @param station
     * @param parameter
     * @param begin
     * @param end
     * @return
     */
    static CHMIEntity createSeries(JSONObject station, CHMIParameter parameter, Date begin, Date end) {

	JSONObject object = new JSONObject(station.toString());

	object.put(PARAMETER, parameter.name());
	object.put(SERIES_ID, buildSeriesId(station.getString(OBJ_ID), parameter));
	object.put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(begin));
	object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(end));

	return new CHMIEntity(object);
    }

    /**
     * Retrieves the values of the given period. Historical series are read from the yearly files; operational series from the
     * validated yearly files of the historical counterpart (if any), then from the daily recent files following the last validated
     * value and from the file of the current day
     *
     * @param series
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(CHMIEntity series, Date begin, Date end) throws GSException {

	CHMIParameter parameter = getParameter(series);
	String stationId = series.getStationId();

	if (parameter.getProduct().isHistorical()) {
	    return readArchive(parameter, stationId, begin, end);
	}

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	LocalDate today = LocalDate.now(ZoneOffset.UTC);
	LocalDate from = toDay(begin);

	Optional<CHMIParameter> counterpart = parameter.getHistoricalCounterpart();
	if (counterpart.isPresent() && from.getYear() < today.getYear()) {

	    Date archiveEnd = new Date(Math.min(end.getTime(), getYearBegin(today.getYear()).getTime() - 1));
	    ret.putAll(readArchive(counterpart.get(), stationId, begin, archiveEnd));

	    if (!ret.isEmpty()) {
		from = toDay(ret.lastKey()).plusDays(1);
	    }
	}

	LocalDate to = toDay(end);
	if (!to.isBefore(today)) {
	    to = today.minusDays(1);
	}

	for (LocalDate day : getRecentDays(stationId, from, to)) {
	    TreeMap<Date, BigDecimal> values = readOperational(getRecentPath(stationId, day)).get(parameter.name());
	    if (values != null) {
		ret.putAll(subMap(values, begin, end));
	    }
	}

	if (!toDay(end).isBefore(today)) {
	    TreeMap<Date, BigDecimal> values = readOperational(NOW_DATA + stationId + ".json").get(parameter.name());
	    if (values != null) {
		ret.putAll(subMap(values, begin, end));
	    }
	}

	return ret;
    }

    /**
     * @param series
     * @return the time of the latest value of an operational series, read from the file of the current day
     * @throws GSException
     */
    public Optional<Date> retrieveLatestObservation(CHMIEntity series) throws GSException {

	CHMIParameter parameter = getParameter(series);
	if (parameter.getProduct().isHistorical()) {
	    return Optional.empty();
	}

	TreeMap<Date, BigDecimal> values = readOperational(NOW_DATA + series.getStationId() + ".json").get(parameter.name());

	return values == null || values.isEmpty() ? Optional.empty() : Optional.of(values.lastKey());
    }

    /**
     * Recent files are published for each day from a station specific first day up to yesterday: if the first requested day is not
     * available, the first available day is found with a binary search
     *
     * @param stationId
     * @param from
     * @param to
     * @return the days of the available recent files of the period
     * @throws GSException
     */
    private List<LocalDate> getRecentDays(String stationId, LocalDate from, LocalDate to) throws GSException {

	List<LocalDate> ret = new ArrayList<>();

	if (from.isAfter(to)) {
	    return ret;
	}

	LocalDate first = from;

	if (getOptional(getRecentPath(stationId, from)).isEmpty()) {

	    if (getOptional(getRecentPath(stationId, to)).isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).debug("No recent files of {} between {} and {}", stationId, from, to);
		return ret;
	    }

	    LocalDate missing = from;
	    LocalDate available = to;

	    while (missing.plusDays(1).isBefore(available)) {
		LocalDate middle = missing.plusDays((available.toEpochDay() - missing.toEpochDay()) / 2);
		if (getOptional(getRecentPath(stationId, middle)).isPresent()) {
		    available = middle;
		} else {
		    missing = middle;
		}
	    }

	    first = available;
	}

	for (LocalDate day = first; !day.isAfter(to); day = day.plusDays(1)) {
	    ret.add(day);
	}

	return ret;
    }

    /**
     * @param parameter a historical series type
     * @param stationId
     * @param begin
     * @param end
     * @return the values of the period, read from the yearly files
     * @throws GSException
     */
    private TreeMap<Date, BigDecimal> readArchive(CHMIParameter parameter, String stationId, Date begin, Date end) throws GSException {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	for (int year = toDay(begin).getYear(); year <= toDay(end).getYear(); year++) {
	    TreeMap<Date, BigDecimal> values = readHistorical(parameter.getProduct(), stationId, year).get(parameter.name());
	    if (values != null) {
		ret.putAll(subMap(values, begin, end));
	    }
	}

	return ret;
    }

    /**
     * @param path the path of a now or recent file
     * @return the values of each series of the file, empty if not available
     * @throws GSException
     */
    private Map<String, TreeMap<Date, BigDecimal>> readOperational(String path) throws GSException {

	Optional<String> text = getOptional(path);
	if (text.isEmpty()) {
	    return new HashMap<>();
	}

	try {
	    return parseOperational(new JSONObject(text.get()));
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Unable to parse CHMI file {}: {}", path, e.getMessage());
	    return new HashMap<>();
	}
    }

    /**
     * @param product
     * @param stationId
     * @param year
     * @return the values of each series of the yearly file, empty if not available
     * @throws GSException
     */
    private Map<String, TreeMap<Date, BigDecimal>> readHistorical(CHMIProduct product, String stationId, int year) throws GSException {

	String path = getHistoricalPath(product, stationId, year);

	Optional<String> text = getOptional(path);
	if (text.isEmpty()) {
	    return new HashMap<>();
	}

	try {
	    return parseHistorical(new JSONObject(text.get()));
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Unable to parse CHMI file {}: {}", path, e.getMessage());
	    return new HashMap<>();
	}
    }

    /**
     * @param object a now or recent file: {"objList":[{"objID":..,"tsList":[{"tsConID":"H","unit":"CM","tsData":[{"dt":..,"value":..}]}]}]}
     * @return the values of each series (by tsConID), without missing values
     */
    static Map<String, TreeMap<Date, BigDecimal>> parseOperational(JSONObject object) {

	Map<String, TreeMap<Date, BigDecimal>> ret = new HashMap<>();

	JSONArray objects = object.optJSONArray("objList");
	if (objects == null) {
	    return ret;
	}

	for (int i = 0; i < objects.length(); i++) {

	    JSONArray tsList = objects.getJSONObject(i).optJSONArray("tsList");
	    if (tsList == null) {
		continue;
	    }

	    for (int j = 0; j < tsList.length(); j++) {

		JSONObject ts = tsList.getJSONObject(j);
		TreeMap<Date, BigDecimal> values = ret.computeIfAbsent(ts.optString("tsConID"), k -> new TreeMap<>());

		JSONArray data = ts.optJSONArray("tsData");
		if (data == null) {
		    continue;
		}

		for (int k = 0; k < data.length(); k++) {
		    JSONObject value = data.getJSONObject(k);
		    addValue(values, value.optString("dt", null), value.isNull("value") ? null : value.optBigDecimal("value", null));
		}
	    }
	}

	return ret;
    }

    /**
     * @param object a historical file: {"objID":..,"tsList":[{"tsConID":"QD","unit":"M3_S","tsData":{"type":"DataCollection",
     *        "data":{"header":"DT,VAL","values":[[dt,value]]}}}]}
     * @return the values of each series (by tsConID), without missing values
     */
    static Map<String, TreeMap<Date, BigDecimal>> parseHistorical(JSONObject object) {

	Map<String, TreeMap<Date, BigDecimal>> ret = new HashMap<>();

	JSONArray tsList = object.optJSONArray("tsList");
	if (tsList == null) {
	    return ret;
	}

	for (int j = 0; j < tsList.length(); j++) {

	    JSONObject ts = tsList.getJSONObject(j);
	    TreeMap<Date, BigDecimal> values = ret.computeIfAbsent(ts.optString("tsConID"), k -> new TreeMap<>());

	    JSONObject tsData = ts.optJSONObject("tsData");
	    JSONArray rows = tsData == null || tsData.optJSONObject("data") == null ? null
		    : tsData.getJSONObject("data").optJSONArray("values");
	    if (rows == null) {
		continue;
	    }

	    for (int k = 0; k < rows.length(); k++) {
		JSONArray row = rows.getJSONArray(k);
		addValue(values, row.optString(0, null), row.length() < 2 || row.isNull(1) ? null : row.optBigDecimal(1, null));
	    }
	}

	return ret;
    }

    /**
     * @param values
     * @param date
     * @param value
     */
    private static void addValue(TreeMap<Date, BigDecimal> values, String date, BigDecimal value) {

	if (value == null) {
	    return;
	}

	parseDate(date).ifPresent(d -> values.put(d, value));
    }

    /**
     * @param object a metadata table: {"data":{"type":"DataCollection","data":{"header":"objID,DBC,...","values":[[..]]}}}
     * @return the rows, as objects with the header keys (null values are omitted)
     */
    static List<JSONObject> parseTable(JSONObject object) {

	List<JSONObject> ret = new ArrayList<>();

	JSONObject table = object.getJSONObject("data").getJSONObject("data");
	String[] header = table.getString("header").split(",");
	JSONArray values = table.getJSONArray("values");

	for (int i = 0; i < values.length(); i++) {

	    JSONArray row = values.getJSONArray(i);
	    JSONObject entry = new JSONObject();
	    for (int j = 0; j < header.length && j < row.length(); j++) {
		if (!row.isNull(j)) {
		    entry.put(header[j].trim(), row.get(j));
		}
	    }

	    if (entry.has(OBJ_ID)) {
		ret.add(entry);
	    }
	}

	return ret;
    }

    /**
     * @param row
     * @param target
     * @return the target, with the non empty station keys of the row
     */
    private static JSONObject selectStationKeys(JSONObject row, JSONObject target) {

	for (String key : STATION_KEYS) {
	    Object value = row.opt(key);
	    if (value != null && !value.toString().trim().isEmpty()) {
		target.put(key, value);
	    }
	}

	return target;
    }

    /**
     * @param html the listing of a historical directory
     * @param product
     * @return the first and last year of the files of each station
     */
    static Map<String, int[]> parseListing(String html, CHMIProduct product) {

	Map<String, int[]> ret = new HashMap<>();

	Pattern pattern = Pattern.compile("href=\"H_([^\"]+)_" + product.getCode() + "_(\\d{4})\\.json\"");
	Matcher matcher = pattern.matcher(html);

	while (matcher.find()) {
	    int year = Integer.parseInt(matcher.group(2));
	    int[] years = ret.computeIfAbsent(matcher.group(1), k -> new int[] { year, year });
	    years[0] = Math.min(years[0], year);
	    years[1] = Math.max(years[1], year);
	}

	return ret;
    }

    /**
     * @param html the listing of the recent directory
     * @return the day of the first file of each station
     */
    static Map<String, LocalDate> parseRecentListing(String html) {

	Map<String, LocalDate> ret = new HashMap<>();

	Matcher matcher = RECENT_FILE.matcher(html);

	while (matcher.find()) {
	    try {
		LocalDate day = LocalDate.parse(matcher.group(1), DAY_FORMAT);
		ret.merge(matcher.group(2), day, (a, b) -> a.isBefore(b) ? a : b);
	    } catch (DateTimeParseException e) {
		// not a recent data file
	    }
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
     * @param parameters
     * @param product
     * @return the given series types of the given dataset
     */
    private static List<CHMIParameter> filter(Collection<CHMIParameter> parameters, CHMIProduct product) {

	List<CHMIParameter> ret = new ArrayList<>();
	for (CHMIParameter parameter : parameters) {
	    if (parameter.getProduct() == product && !ret.contains(parameter)) {
		ret.add(parameter);
	    }
	}

	return ret;
    }

    /**
     * @param stationId
     * @param parameter
     * @return
     */
    static String buildSeriesId(String stationId, CHMIParameter parameter) {

	return stationId + SERIES_ID_SEPARATOR + parameter.name();
    }

    /**
     * @param seriesId
     * @return the series identified by the given identifier, or null if not valid
     */
    public static CHMIEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	int index = seriesId.lastIndexOf(SERIES_ID_SEPARATOR);
	if (index <= 0) {
	    return null;
	}

	String stationId = seriesId.substring(0, index);
	Optional<CHMIParameter> parameter = CHMIParameter.decode(seriesId.substring(index + 1));
	if (parameter.isEmpty()) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, buildSeriesId(stationId, parameter.get()));
	object.put(OBJ_ID, stationId);
	object.put(PARAMETER, parameter.get().name());

	return new CHMIEntity(object);
    }

    /**
     * @param stationId
     * @param day
     * @return e.g. "recent/data/20260101_0-203-1-001000.json"
     */
    static String getRecentPath(String stationId, LocalDate day) {

	return RECENT_DATA + DAY_FORMAT.format(day) + "_" + stationId + ".json";
    }

    /**
     * @param product
     * @param stationId
     * @param year
     * @return e.g. "historical/data/daily/H_0-203-1-001000_DQ_2025.json"
     */
    static String getHistoricalPath(CHMIProduct product, String stationId, int year) {

	return HISTORICAL_DATA + product.getDirectory() + "/H_" + stationId + "_" + product.getCode() + "_" + year + ".json";
    }

    /**
     * @param series
     * @return
     * @throws GSException
     */
    private CHMIParameter getParameter(CHMIEntity series) throws GSException {

	return series.getParameter().orElseThrow(() -> createException("Invalid CHMI series " + series.getSeriesId(), null));
    }

    /**
     * @param year
     * @return
     */
    static Date getYearBegin(int year) {

	return getDayBegin(LocalDate.of(year, 1, 1));
    }

    /**
     * @param year
     * @return the last second of the year
     */
    static Date getYearEnd(int year) {

	return new Date(getYearBegin(year + 1).getTime() - 1000);
    }

    /**
     * @param day
     * @return
     */
    static Date getDayBegin(LocalDate day) {

	return Date.from(day.atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    /**
     * @param date
     * @return the UTC day of the date
     */
    static LocalDate toDay(Date date) {

	return date.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
    }

    /**
     * @param text e.g. "2026-10-27T00:10:00Z"
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

	return getOptional(path).orElseThrow(() -> createException("CHMI " + path + " not available", null));
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
		throw createException("CHMI " + path + " returned HTTP " + response.statusCode(), null);
	    }

	    return text.isBlank() ? Optional.empty() : Optional.of(text);

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("CHMI " + path + " request failed", e);
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
