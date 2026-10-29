package eu.essi_lab.accessor.hrdhmz;

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
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Client of the hidroweb service of the Croatian Meteorological and Hydrological Service (Državni hidrometeorološki zavod, DHMZ),
 * https://hidro.dhz.hr/ .<br>
 * All the requests are sent to a single script with form parameters ("funkc" selects the function). The responses are Python
 * dictionary literals (single quoted strings) and are parsed leniently.<br>
 * The station list ("markeri") describes each station with an HTML tooltip listing the years of measurement of each data type
 * (see {@link DHMZProperty}) and, for automatic stations, the latest observation. Daily values are published one year per
 * request ("dnevnipodaci"), the real time water levels of automatic stations for the last 15 days ("podaciautom"). The times of
 * the real time values are in CET (UTC+1), as stated by the service
 *
 * @author boldrini
 */
public class DHMZClient {

    public static final String DEFAULT_ENDPOINT = "https://hidro.dhz.hr/hidroweb/skripte/hisbaza.py";

    static final String SCRIPT_PATH = "hidroweb/skripte/hisbaza.py";

    /**
     * Value of the "stip" parameter returning the complete station list (active and historical stations)
     */
    static final String ALL_STATIONS_LIST_TYPE = "5";

    /**
     * Days of real time values kept by the service
     */
    public static final int REAL_TIME_DAYS = 15;

    /**
     * Time zone of the real time values and of the station descriptions
     */
    static final ZoneOffset CET = ZoneOffset.ofHours(1);

    /**
     * Pause between consecutive requests of the same download, to be gentle with the service
     */
    static final long REQUEST_PAUSE = 200;

    // keys of the station list entries
    static final String KOD = "kod";
    static final String SIFRA = "sifra";
    static final String TOOLTIP = "ttip";
    static final String LATITUDE = "gsirina";
    static final String LONGITUDE = "gduzina";
    static final String AUTOMATIC_FLAG = "autom";

    // keys of the series
    public static final String SERIES_ID = "seriesId";
    public static final String STATION_KEY = "stationKey";
    public static final String STATION_CODE = "stationCode";
    public static final String STATION_NAME = "stationName";
    public static final String RIVER = "river";
    public static final String BASIN = "basin";
    public static final String SUB_BASIN = "subBasin";
    public static final String STATION_TYPE = "stationType";
    public static final String OPERATION_BEGIN = "operationBegin";
    public static final String OPERATION_END = "operationEnd";
    public static final String GAUGE_ZERO = "gaugeZero";
    public static final String AUTOMATIC = "automatic";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String LATEST_TIME = "latestTime";
    public static final String LATEST_VALUE = "latestValue";
    public static final String PROPERTY = "property";
    public static final String REAL_TIME = "realTime";
    public static final String YEARS = "years";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    static final String SERIES_ID_SEPARATOR = "|";
    static final String REAL_TIME_SUFFIX = "RT";

    private static final Pattern NAME_PATTERN = Pattern.compile("Postaja: <b>([^<]*)</b>");
    private static final Pattern RIVER_PATTERN = Pattern.compile("Vodotok: <b>([^<]*)</b>");
    private static final Pattern BASIN_PATTERN = Pattern.compile("Vodotok: <b>[^<]*</b><br>\\s*<span[^>]*><b>(.*?)</b></span>");
    private static final Pattern OPERATION_BEGIN_PATTERN = Pattern.compile("Početak rada: <b>([^<]*)</b>");
    private static final Pattern OPERATION_END_PATTERN = Pattern.compile("Kraj rada: <b>([^<]*)</b>");
    private static final Pattern GAUGE_ZERO_PATTERN = Pattern.compile("Kota nule vodokaza: <b>([^<]*)</b>");
    private static final Pattern LATEST_PATTERN = Pattern.compile("Zadnji podatak: </b></span>([^&<]*)&nbsp;<b>([^<]*)</b>");
    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d{4})(?:\\s*-\\s*(\\d{4}))?");

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("d. M. yyyy.");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("d. M. yyyy. H:mm");

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public DHMZClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public DHMZClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 3);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    /**
     * Retrieves the stations with a single request
     *
     * @return the station list entries, without duplicates, sorted by station key
     * @throws GSException
     */
    public List<JSONObject> retrieveStations() throws GSException {

	JSONObject response = post("funkc=markeri&kkor=0&stip=" + ALL_STATIONS_LIST_TYPE);

	return parseStations(response);
    }

    /**
     * @param response
     * @return
     */
    static List<JSONObject> parseStations(JSONObject response) {

	JSONArray stations = response.optJSONArray("postaje");
	if (stations == null) {
	    return new ArrayList<>();
	}

	TreeMap<Integer, JSONObject> ret = new TreeMap<>();
	for (int i = 0; i < stations.length(); i++) {
	    JSONObject station = stations.getJSONObject(i);
	    Optional<Integer> key = parseInteger(station.optString(KOD, ""));
	    key.ifPresent(k -> ret.putIfAbsent(k, station));
	}

	return new ArrayList<>(ret.values());
    }

    /**
     * Creates the series of a station: one daily series for each data type with years of measurement and, for the automatic
     * stations with a recent latest observation, the real time water level series
     *
     * @param station the station list entry
     * @param properties the data types to harvest
     * @param realTime true to create the real time series
     * @param now
     * @return
     */
    static List<DHMZEntity> createSeries(JSONObject station, Collection<DHMZProperty> properties, boolean realTime, Date now) {

	List<DHMZEntity> ret = new ArrayList<>();

	JSONObject description = parseStation(station);
	if (description == null) {
	    return ret;
	}

	String tooltip = station.optString(TOOLTIP, "");

	for (DHMZProperty property : properties) {

	    Optional<String> yearsText = find(tooltip,
		    Pattern.compile("Godine mjerenja " + Pattern.quote(property.getYearsKey()) + ":(?:</td><td[^>]*>)?\\s*<b>([^<]*)</b>"));
	    if (yearsText.isEmpty()) {
		continue;
	    }

	    TreeSet<Integer> years = parseYears(yearsText.get());
	    if (years.isEmpty()) {
		continue;
	    }

	    JSONObject object = new JSONObject(description.toString());
	    object.put(PROPERTY, property.name());
	    object.put(REAL_TIME, false);
	    object.put(SERIES_ID, buildSeriesId(description.getString(STATION_KEY), property, false));
	    object.put(YEARS, yearsText.get().trim());

	    // begin of the first year (or start of operation, if later in the same year)
	    LocalDate begin = LocalDate.of(years.first(), 1, 1);
	    Optional<LocalDate> operationBegin = parseDay(description.optString(OPERATION_BEGIN, null));
	    if (operationBegin.isPresent() && operationBegin.get().getYear() == years.first() && operationBegin.get().isAfter(begin)) {
		begin = operationBegin.get();
	    }

	    // last day of the last year (or end of operation, if earlier in the same year)
	    LocalDate end = LocalDate.of(years.last(), 12, 31);
	    Optional<LocalDate> operationEnd = parseDay(description.optString(OPERATION_END, null));
	    if (operationEnd.isPresent() && operationEnd.get().getYear() == years.last() && operationEnd.get().isBefore(end)
		    && !operationEnd.get().isBefore(begin)) {
		end = operationEnd.get();
	    }

	    object.put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(Date.from(begin.atStartOfDay().toInstant(ZoneOffset.UTC))));
	    object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(Date.from(end.atStartOfDay().toInstant(ZoneOffset.UTC))));

	    ret.add(new DHMZEntity(object));
	}

	if (realTime && description.optBoolean(AUTOMATIC, false) && description.has(LATEST_TIME)) {

	    Date latest = ISO8601DateTimeUtils.parseISO8601ToDate(description.getString(LATEST_TIME)).get();
	    Date windowBegin = getRealTimeWindowBegin(now);

	    if (latest.after(windowBegin)) {

		JSONObject object = new JSONObject(description.toString());
		object.put(PROPERTY, DHMZProperty.WATER_LEVEL.name());
		object.put(REAL_TIME, true);
		object.put(SERIES_ID, buildSeriesId(description.getString(STATION_KEY), DHMZProperty.WATER_LEVEL, true));
		object.put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(windowBegin));
		object.put(END, description.getString(LATEST_TIME));

		ret.add(new DHMZEntity(object));
	    }
	}

	return ret;
    }

    /**
     * @param station the station list entry
     * @return the station description extracted from the tooltip, or null if the station has no key
     */
    static JSONObject parseStation(JSONObject station) {

	String key = station.optString(KOD, "").trim();
	if (key.isEmpty()) {
	    return null;
	}

	String code = station.optString(SIFRA, "").trim();
	String tooltip = station.optString(TOOLTIP, "");

	JSONObject ret = new JSONObject();
	ret.put(STATION_KEY, key);
	ret.put(STATION_CODE, code.isEmpty() ? key : code);

	String name = find(tooltip, NAME_PATTERN).orElse(code).trim();
	// e.g. "MURSKO SREDIŠĆE, 5165"
	if (!code.isEmpty() && name.endsWith(", " + code)) {
	    name = name.substring(0, name.length() - code.length() - 2).trim();
	}
	ret.put(STATION_NAME, name);

	putIfNotEmpty(ret, RIVER, find(tooltip, RIVER_PATTERN));

	find(tooltip, BASIN_PATTERN).ifPresent(basin -> {
	    String[] split = basin.split("<br>");
	    putIfNotEmpty(ret, BASIN, Optional.of(split[0]));
	    if (split.length > 1) {
		putIfNotEmpty(ret, SUB_BASIN, Optional.of(split[1]));
	    }
	});

	find(tooltip, OPERATION_BEGIN_PATTERN).flatMap(DHMZClient::parseDay).ifPresent(d -> ret.put(OPERATION_BEGIN, d.toString()));
	find(tooltip, OPERATION_END_PATTERN).flatMap(DHMZClient::parseDay).ifPresent(d -> ret.put(OPERATION_END, d.toString()));
	find(tooltip, GAUGE_ZERO_PATTERN).flatMap(z -> parseDecimal(z.replace(",", "."))).ifPresent(z -> ret.put(GAUGE_ZERO, z.toPlainString()));

	if (tooltip.contains("Limnigrafska postaja")) {
	    ret.put(STATION_TYPE, "Limnigrafska postaja");
	} else if (tooltip.contains("Vodokazna postaja")) {
	    ret.put(STATION_TYPE, "Vodokazna postaja");
	}

	boolean automatic = tooltip.contains("Automatska dojava") || station.optString(AUTOMATIC_FLAG, "").equals("t");
	ret.put(AUTOMATIC, automatic);

	Matcher latest = LATEST_PATTERN.matcher(tooltip);
	if (latest.find()) {
	    parseTime(latest.group(1)).ifPresent(t -> ret.put(LATEST_TIME, ISO8601DateTimeUtils.getISO8601DateTime(t)));
	    parseDecimal(latest.group(2)).ifPresent(v -> ret.put(LATEST_VALUE, v.toPlainString()));
	}

	Optional<BigDecimal> lat = parseDecimal(station.optString(LATITUDE, ""));
	Optional<BigDecimal> lon = parseDecimal(station.optString(LONGITUDE, ""));
	if (lat.isPresent() && lon.isPresent() && lat.get().signum() != 0 && lon.get().signum() != 0) {
	    ret.put(LAT, lat.get());
	    ret.put(LON, lon.get());
	}

	return ret;
    }

    /**
     * @param now
     * @return the begin of the window of the real time values (midnight CET, {@link #REAL_TIME_DAYS} days ago)
     */
    static Date getRealTimeWindowBegin(Date now) {

	LocalDate today = now.toInstant().atOffset(CET).toLocalDate();
	return Date.from(today.minusDays(REAL_TIME_DAYS).atStartOfDay().toInstant(CET));
    }

    /**
     * @param text e.g. "1923-1944, 1949-1991, 1998-2025" or "1976-1979 1981-2024"
     * @return
     */
    static TreeSet<Integer> parseYears(String text) {

	TreeSet<Integer> ret = new TreeSet<>();
	if (text == null) {
	    return ret;
	}

	Matcher matcher = YEAR_PATTERN.matcher(text);
	while (matcher.find()) {
	    int from = Integer.parseInt(matcher.group(1));
	    int to = matcher.group(2) == null ? from : Integer.parseInt(matcher.group(2));
	    for (int year = from; year <= to; year++) {
		ret.add(year);
	    }
	}

	return ret;
    }

    /**
     * Retrieves the years with daily values of a series
     *
     * @param stationKey
     * @param property
     * @return
     * @throws GSException
     */
    public TreeSet<Integer> retrieveYears(String stationKey, DHMZProperty property) throws GSException {

	JSONObject response = post("funkc=godinepost&kpost=" + encode(stationKey) + "&tippod=" + property.getTypeCode());

	return parseYearList(response);
    }

    /**
     * @param response
     * @return the years, excluding the entry of the real time values ("dojava")
     */
    static TreeSet<Integer> parseYearList(JSONObject response) {

	TreeSet<Integer> ret = new TreeSet<>();

	JSONArray years = response.optJSONArray("godinepostaje");
	if (years == null) {
	    return ret;
	}

	for (int i = 0; i < years.length(); i++) {
	    JSONObject year = years.getJSONObject(i);
	    if (year.optString("gs", "").equals(year.optString("gd", null))) {
		parseInteger(year.optString("gd", "")).ifPresent(ret::add);
	    }
	}

	return ret;
    }

    /**
     * @param stationKey
     * @param property
     * @param year
     * @return the valid daily values of the year (dates at midnight UTC)
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveDailyValues(String stationKey, DHMZProperty property, int year) throws GSException {

	JSONObject response = post(
		"funkc=dnevnipodaci&kpost=" + encode(stationKey) + "&godpod=" + year + "&tippod=" + property.getTypeCode());

	return parseDailyValues(response, year);
    }

    /**
     * @param response
     * @param year
     * @return the valid values, or an empty map if the response is about another year (the service returns the latest year when
     *         the requested one is not available)
     */
    static TreeMap<Date, BigDecimal> parseDailyValues(JSONObject response, int year) {

	if (!response.optString("godina", "").equals(String.valueOf(year))) {
	    return new TreeMap<>();
	}

	return parseValues(response, 0);
    }

    /**
     * @param stationKey
     * @return the valid real time water levels of the last {@link #REAL_TIME_DAYS} days
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveRealTimeValues(String stationKey) throws GSException {

	JSONObject response = post("funkc=podaciautom&kpost=" + encode(stationKey) + "&godpod=0&tippod="
		+ DHMZProperty.WATER_LEVEL.getTypeCode());

	return parseRealTimeValues(response);
    }

    /**
     * @param response
     * @return the valid values, with times converted from CET to UTC
     */
    static TreeMap<Date, BigDecimal> parseRealTimeValues(JSONObject response) {

	return parseValues(response, -TimeUnit.HOURS.toMillis(1));
    }

    /**
     * @param response
     * @param offset the offset added to the times
     * @return the values, excluding the "dry" ("psuho") and "missing" ("pnema") markers
     */
    private static TreeMap<Date, BigDecimal> parseValues(JSONObject response, long offset) {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	JSONArray values = response.optJSONArray("dpodaci");
	if (values == null) {
	    return ret;
	}

	Optional<BigDecimal> dry = parseDecimal(response.optString("psuho", ""));
	Optional<BigDecimal> missing = parseDecimal(response.optString("pnema", ""));

	for (int i = 0; i < values.length(); i++) {

	    JSONObject value = values.getJSONObject(i);

	    Optional<Long> term = parseLong(value.optString("term", ""));
	    Optional<BigDecimal> point = parseDecimal(value.optString("point", ""));
	    if (term.isEmpty() || point.isEmpty()) {
		continue;
	    }

	    if (isMarker(point.get(), dry) || isMarker(point.get(), missing)) {
		continue;
	    }

	    ret.put(new Date(term.get() + offset), point.get());
	}

	return ret;
    }

    /**
     * @param value
     * @param marker
     * @return
     */
    private static boolean isMarker(BigDecimal value, Optional<BigDecimal> marker) {

	return marker.isPresent() && marker.get().compareTo(value) == 0;
    }

    /**
     * Retrieves the observations of the given period. Daily series are read one year per request (only the years with data);
     * real time series with a single request
     *
     * @param series
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(DHMZEntity series, Date begin, Date end) throws GSException {

	DHMZProperty property = series.getProperty()
		.orElseThrow(() -> createException("Invalid DHMZ series " + series.getSeriesId(), null));

	if (series.isRealTime()) {
	    return new TreeMap<>(retrieveRealTimeValues(series.getStationKey()).subMap(begin, true, end, true));
	}

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	int firstYear = begin.toInstant().atOffset(ZoneOffset.UTC).getYear();
	int lastYear = end.toInstant().atOffset(ZoneOffset.UTC).getYear();

	TreeSet<Integer> years = retrieveYears(series.getStationKey(), property);

	for (Integer year : years.subSet(firstYear, true, lastYear, true)) {

	    pause();

	    ret.putAll(retrieveDailyValues(series.getStationKey(), property, year).subMap(begin, true, end, true));
	}

	return ret;
    }

    /**
     * Retrieves the latest observations of the automatic stations with a single request
     *
     * @return the time of the latest observation by station key
     * @throws GSException
     */
    public Map<String, Date> retrieveLatestObservations() throws GSException {

	return parseLatestObservations(post("funkc=zadnjipodaci&kkor=0"));
    }

    /**
     * @param response
     * @return
     */
    static Map<String, Date> parseLatestObservations(JSONObject response) {

	Map<String, Date> ret = new HashMap<>();

	JSONArray stations = response.optJSONArray("postaje");
	if (stations == null) {
	    return ret;
	}

	for (int i = 0; i < stations.length(); i++) {
	    JSONObject station = stations.getJSONObject(i);
	    parseTime(station.optString("zterm", "")).ifPresent(t -> ret.put(station.optString(KOD, ""), t));
	}

	return ret;
    }

    /**
     * @param stationKey
     * @param property
     * @param realTime
     * @return e.g. "384|DISCHARGE" or "384|WATER_LEVEL|RT"
     */
    static String buildSeriesId(String stationKey, DHMZProperty property, boolean realTime) {

	String id = stationKey + SERIES_ID_SEPARATOR + property.name();
	return realTime ? id + SERIES_ID_SEPARATOR + REAL_TIME_SUFFIX : id;
    }

    /**
     * @param seriesId
     * @return the series identified by the given identifier, or null if not valid
     */
    public static DHMZEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	String[] split = seriesId.split(Pattern.quote(SERIES_ID_SEPARATOR));
	if (split.length < 2 || split.length > 3 || split[0].isEmpty()) {
	    return null;
	}

	Optional<DHMZProperty> property = DHMZProperty.decode(split[1]);
	if (property.isEmpty()) {
	    return null;
	}

	boolean realTime = split.length == 3;
	if (realTime && (!split[2].equals(REAL_TIME_SUFFIX) || property.get() != DHMZProperty.WATER_LEVEL)) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, buildSeriesId(split[0], property.get(), realTime));
	object.put(STATION_KEY, split[0]);
	object.put(PROPERTY, property.get().name());
	object.put(REAL_TIME, realTime);

	return new DHMZEntity(object);
    }

    /**
     * @param text e.g. "30. 06. 1948." or "---"
     * @return
     */
    static Optional<LocalDate> parseDay(String text) {

	if (text == null) {
	    return Optional.empty();
	}

	try {
	    return Optional.of(LocalDate.parse(text.trim(), DAY_FORMAT));
	} catch (DateTimeParseException e) {
	    try {
		return Optional.of(LocalDate.parse(text.trim()));
	    } catch (DateTimeParseException ex) {
		return Optional.empty();
	    }
	}
    }

    /**
     * @param text a CET time, e.g. "27. 10. 2026. 20:00"
     * @return
     */
    static Optional<Date> parseTime(String text) {

	if (text == null) {
	    return Optional.empty();
	}

	try {
	    return Optional.of(Date.from(LocalDateTime.parse(text.trim(), TIME_FORMAT).toInstant(CET)));
	} catch (DateTimeParseException e) {
	    return Optional.empty();
	}
    }

    /**
     * Parses a response: a Python dictionary literal, e.g. {'success': 'true', 'dpodaci': [{'term': '1577836800000', 'point':
     * '255'}]}. The JSON parser accepts single quoted strings
     *
     * @param text
     * @return
     */
    static JSONObject parseResponse(String text) {

	return new JSONObject(text.trim());
    }

    /**
     * @param pattern
     * @param text
     * @return the first group of the first match, if not empty
     */
    private static Optional<String> find(String text, Pattern pattern) {

	Matcher matcher = pattern.matcher(text);
	if (matcher.find() && !matcher.group(1).trim().isEmpty()) {
	    return Optional.of(matcher.group(1).trim());
	}

	return Optional.empty();
    }

    /**
     * @param object
     * @param key
     * @param value
     */
    private static void putIfNotEmpty(JSONObject object, String key, Optional<String> value) {

	value.map(String::trim).filter(v -> !v.isEmpty() && !v.equals("---")).ifPresent(v -> object.put(key, v));
    }

    /**
     * @param text
     * @return
     */
    private static Optional<BigDecimal> parseDecimal(String text) {

	try {
	    return text == null || text.isBlank() ? Optional.empty() : Optional.of(new BigDecimal(text.trim()));
	} catch (NumberFormatException e) {
	    return Optional.empty();
	}
    }

    /**
     * @param text
     * @return
     */
    private static Optional<Integer> parseInteger(String text) {

	try {
	    return text == null || text.isBlank() ? Optional.empty() : Optional.of(Integer.valueOf(text.trim()));
	} catch (NumberFormatException e) {
	    return Optional.empty();
	}
    }

    /**
     * @param text
     * @return
     */
    private static Optional<Long> parseLong(String text) {

	try {
	    return text == null || text.isBlank() ? Optional.empty() : Optional.of(Long.valueOf(text.trim()));
	} catch (NumberFormatException e) {
	    return Optional.empty();
	}
    }

    /**
     * @param value
     * @return
     */
    private static String encode(String value) {

	return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     *
     */
    private static void pause() {

	try {
	    Thread.sleep(REQUEST_PAUSE);
	} catch (InterruptedException e) {
	    Thread.currentThread().interrupt();
	}
    }

    /**
     * @param form the form parameters, e.g. "funkc=markeri&kkor=0&stip=5"
     * @return
     * @throws GSException
     */
    private JSONObject post(String form) throws GSException {

	try {

	    HttpRequest request = HttpRequestUtils.build(//
		    MethodWithBody.POST, //
		    endpoint, //
		    form, //
		    HttpHeaderUtils.build("Content-Type", "application/x-www-form-urlencoded"));

	    HttpResponse<InputStream> response = downloader.downloadResponse(request);

	    String text;
	    try (InputStream stream = response.body()) {
		text = stream == null ? "" : IOUtils.toString(stream, StandardCharsets.UTF_8);
	    }

	    if (response.statusCode() != 200) {
		throw createException("DHMZ request " + form + " returned HTTP " + response.statusCode(), null);
	    }

	    if (text.isBlank()) {
		throw createException("DHMZ request " + form + " returned no content", null);
	    }

	    return parseResponse(text);

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("DHMZ request " + form + " failed", e);
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
     * @param endpoint e.g. "https://hidro.dhz.hr/" or "https://hidro.dhz.hr/hidroweb/skripte/hisbaza.py"
     * @return the URL of the script
     */
    static String normalizeEndpoint(String endpoint) {

	if (endpoint == null || endpoint.isBlank()) {
	    return DEFAULT_ENDPOINT;
	}

	String normalized = endpoint.trim();

	int query = normalized.indexOf('?');
	if (query >= 0) {
	    normalized = normalized.substring(0, query);
	}

	if (normalized.endsWith(".py")) {
	    return normalized;
	}

	if (!normalized.endsWith("/")) {
	    normalized += "/";
	}

	if (normalized.endsWith("hidroweb/skripte/")) {
	    return normalized + "hisbaza.py";
	}

	if (normalized.endsWith("hidroweb/")) {
	    return normalized + "skripte/hisbaza.py";
	}

	return normalized + SCRIPT_PATH;
    }
}
