package eu.essi_lab.accessor.plimgw;

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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Client of the public data service of the Institute of Meteorology and Water Management (IMGW-PIB) of Poland
 * (https://danepubliczne.imgw.pl/).<br>
 * A time series is identified by a station code and an observed property (see {@link IMGWProperty}). Two sources are combined:
 * <ul>
 * <li>the real time API (api/data/hydro), listing the stations with coordinates and only the <b>latest</b> observation of each
 * property</li>
 * <li>the archive of validated data (data/dane_pomiarowo_obserwacyjne/dane_hydrologiczne/), with daily values from 1951,
 * organized in hydrological years (from 1 November of the previous year to 31 October) and published with a delay of about one
 * year. Each file (one per month, or one per hydrological year since 2023) contains all the stations</li>
 * </ul>
 * The archive of monthly characteristics (one small file per hydrological year) is used to find the first and the last month with
 * observations of each series. Since characteristics are computed only for months without gaps, the first day of the first
 * month and the last day of the last month are actual observation days (a partially observed month before them is not
 * detected).<br>
 * The hydro-back API of https://hydro.imgw.pl (recent days of observations) is not used, since it refuses requests not coming
 * from the web application.
 *
 * @author boldrini
 */
public class IMGWClient {

    public static final String DEFAULT_ENDPOINT = "https://danepubliczne.imgw.pl/";

    static final String STATIONS_PATH = "api/data/hydro";
    static final String STATION_PATH = STATIONS_PATH + "/id/";

    static final String ARCHIVE_PATH = "data/dane_pomiarowo_obserwacyjne/dane_hydrologiczne/";
    static final String MONTHLY_PATH = ARCHIVE_PATH + "miesieczne/";
    static final String DAILY_PATH = ARCHIVE_PATH + "dobowe/";

    /**
     * Maximum number of hydrological years read from the archive of daily values in a single request (the daily files of a
     * hydrological year, containing all the stations, are about 1.8 MB zipped). Longer windows are reduced to their most recent
     * part
     */
    public static final int MAX_ARCHIVE_YEARS = 20;

    /**
     * Encoding of the archive files
     */
    static final Charset ARCHIVE_CHARSET = Charset.forName("windows-1250");

    // keys of the real time API
    public static final String STATION_ID = "id_stacji";
    public static final String STATION_NAME = "stacja";
    public static final String RIVER = "rzeka";
    public static final String VOIVODESHIP = "wojewodztwo";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String FOUNDATION_YEAR = "rok_zalozenia_stacji";
    public static final String GAUGE_ZERO = "rzedna_zerawodowskazu";
    public static final String RIVER_KM = "kilometr_biegu_rzeki";
    public static final String ALARM_LEVEL = "stan_alarmowy";
    public static final String WARNING_LEVEL = "stan_ostrzegawczy";

    // keys added by the client
    public static final String PROPERTY = "property";
    public static final String SERIES_ID = "seriesId";
    public static final String BEGIN = "begin";
    public static final String END = "end";
    public static final String ARCHIVE_BEGIN = "archiveBegin";
    public static final String ARCHIVE_END = "archiveEnd";
    public static final String LATEST = "latest";

    private static final String SERIES_ID_SEPARATOR = "|";

    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Pattern YEAR_DIRECTORY = Pattern.compile("href=\"(\\d{4})/\"");
    private static final Pattern DAILY_FILE = Pattern.compile("href=\"(codz_(\\d{4})(?:_(\\d{2}))?\\.zip)\"");

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public IMGWClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public IMGWClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 5);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    /**
     * Retrieves the time series of the given properties at the stations of the real time API, with the period covered by the
     * archive and the latest observation. Series without observations are discarded
     *
     * @param properties
     * @param stations the codes of the stations to retrieve, empty for all
     * @return the series sorted by station code and property
     * @throws GSException
     */
    public List<IMGWEntity> retrieveSeries(Collection<IMGWProperty> properties, Collection<String> stations) throws GSException {

	JSONArray list = retrieveStations();

	ArchiveIndex index = retrieveArchiveIndex();

	List<IMGWEntity> ret = new ArrayList<>();

	for (int i = 0; i < list.length(); i++) {

	    JSONObject station = list.getJSONObject(i);
	    if (!stations.isEmpty() && !stations.contains(station.optString(STATION_ID))) {
		continue;
	    }

	    for (IMGWProperty property : properties) {
		createSeries(station, property, index).ifPresent(ret::add);
	    }
	}

	ret.sort(Comparator.comparing(IMGWEntity::getStationCode).thenComparing(e -> e.getProperty().map(Enum::ordinal).orElse(-1)));

	return ret;
    }

    /**
     * @param station the station entry of the real time API
     * @param property
     * @param index
     * @return the series, or empty if the station has no observations of the property
     */
    static Optional<IMGWEntity> createSeries(JSONObject station, IMGWProperty property, ArchiveIndex index) {

	String code = station.optString(STATION_ID, "").trim();
	if (code.isEmpty()) {
	    return Optional.empty();
	}

	Optional<LocalDate[]> archive = index.getPeriod(code, property);
	Optional<Date> latest = getLatestTime(station, property);

	if (archive.isEmpty() && latest.isEmpty()) {
	    return Optional.empty();
	}

	JSONObject object = new JSONObject(station.toString());

	object.put(PROPERTY, property.name());
	object.put(SERIES_ID, buildSeriesId(code, property));

	Date begin = null;
	Date end = null;

	if (archive.isPresent()) {
	    begin = toDate(archive.get()[0]);
	    end = toDate(archive.get()[1]);
	    object.put(ARCHIVE_BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(begin));
	    object.put(ARCHIVE_END, ISO8601DateTimeUtils.getISO8601DateTime(end));
	}

	if (latest.isPresent()) {
	    object.put(LATEST, ISO8601DateTimeUtils.getISO8601DateTime(latest.get()));
	    begin = begin == null || latest.get().before(begin) ? latest.get() : begin;
	    end = end == null || latest.get().after(end) ? latest.get() : end;
	}

	object.put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(begin));
	object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(end));

	return Optional.of(new IMGWEntity(object));
    }

    /**
     * @return the stations of the real time API, with their latest observations
     * @throws GSException
     */
    public JSONArray retrieveStations() throws GSException {

	return new JSONArray(get(STATIONS_PATH));
    }

    /**
     * @param stationCode
     * @return the station entry of the real time API, with its latest observations
     * @throws GSException
     */
    public Optional<JSONObject> retrieveStation(String stationCode) throws GSException {

	Optional<String> text = getOptional(STATION_PATH + stationCode);
	if (text.isEmpty()) {
	    return Optional.empty();
	}

	JSONArray array = new JSONArray(text.get());
	for (int i = 0; i < array.length(); i++) {
	    if (array.getJSONObject(i).optString(STATION_ID).equals(stationCode)) {
		return Optional.of(array.getJSONObject(i));
	    }
	}

	return Optional.empty();
    }

    /**
     * @param station the station entry of the real time API
     * @param property
     * @return the time of the latest valid observation of the property
     */
    static Optional<Date> getLatestTime(JSONObject station, IMGWProperty property) {

	if (getLatestValue(station, property).isEmpty()) {
	    return Optional.empty();
	}

	return parseAPIDate(station.optString(property.getDateKey(), null));
    }

    /**
     * @param station the station entry of the real time API
     * @param property
     * @return the latest value of the property
     */
    static Optional<BigDecimal> getLatestValue(JSONObject station, IMGWProperty property) {

	if (station.isNull(property.getValueKey())) {
	    return Optional.empty();
	}

	return Optional.ofNullable(parseDecimal(station.optString(property.getValueKey(), "")));
    }

    /**
     * Period of the observations of each series in the archive, at monthly resolution
     */
    static class ArchiveIndex {

	private final HashMap<String, LocalDate[]> periods = new HashMap<>();

	/**
	 * @param code
	 * @param property
	 * @return the first and the last day of observation
	 */
	Optional<LocalDate[]> getPeriod(String code, IMGWProperty property) {

	    return Optional.ofNullable(periods.get(code + SERIES_ID_SEPARATOR + property.name()));
	}

	/**
	 * @param row a row of the monthly characteristics
	 */
	void add(ArchiveRow row) {

	    LocalDate first = row.getMonth();
	    LocalDate last = first.withDayOfMonth(first.lengthOfMonth());

	    for (IMGWProperty property : IMGWProperty.values()) {

		if (row.getValue(property) == null) {
		    continue;
		}

		LocalDate[] period = periods.computeIfAbsent(row.code + SERIES_ID_SEPARATOR + property.name(),
			k -> new LocalDate[] { first, last });

		if (first.isBefore(period[0])) {
		    period[0] = first;
		}
		if (last.isAfter(period[1])) {
		    period[1] = last;
		}
	    }
	}

	/**
	 * @return the number of series
	 */
	int size() {

	    return periods.size();
	}
    }

    /**
     * A row of the archive files: daily values ("codz") or monthly characteristics ("mies"), with fields station code, station
     * name, river, hydrological year, month in the hydrological year, day (or extreme indicator), water level, discharge, water
     * temperature, calendar month
     */
    static class ArchiveRow {

	String code;
	int hydrologicalYear;
	int calendarMonth;
	int day;
	final BigDecimal[] values = new BigDecimal[IMGWProperty.values().length];

	/**
	 * @param property
	 * @return the value, null if missing
	 */
	BigDecimal getValue(IMGWProperty property) {

	    return values[property.ordinal()];
	}

	/**
	 * @return the first day of the calendar month of the row
	 */
	LocalDate getMonth() {

	    return LocalDate.of(getCalendarYear(hydrologicalYear, calendarMonth), calendarMonth, 1);
	}

	/**
	 * @return the day of a daily row
	 */
	LocalDate getDate() {

	    return getMonth().withDayOfMonth(day);
	}
    }

    /**
     * Parses a row of the archive. The format changed over time: fields separated by commas or semicolons, possibly quoted, with
     * missing values as special codes (e.g. 9999), empty or "NULL". The fields are read from the end, since names might contain
     * the separator
     *
     * @param line
     * @return the row, or empty if not valid
     */
    static Optional<ArchiveRow> parseRow(String line) {

	if (line == null || line.isBlank()) {
	    return Optional.empty();
	}

	String[] split = line.split(line.indexOf(';') >= 0 ? ";" : ",", -1);
	int n = split.length;
	if (n < 10) {
	    return Optional.empty();
	}

	try {

	    ArchiveRow row = new ArchiveRow();
	    row.code = clean(split[0]);
	    row.hydrologicalYear = Integer.parseInt(clean(split[n - 7]));
	    row.day = Integer.parseInt(clean(split[n - 5]));
	    row.calendarMonth = Integer.parseInt(clean(split[n - 1]));

	    if (row.code.isEmpty() || row.calendarMonth < 1 || row.calendarMonth > 12) {
		return Optional.empty();
	    }

	    for (IMGWProperty property : IMGWProperty.values()) {
		BigDecimal value = parseDecimal(clean(split[n - 4 + property.ordinal()]));
		row.values[property.ordinal()] = property.isMissing(value) ? null : value;
	    }

	    return Optional.of(row);

	} catch (NumberFormatException e) {

	    return Optional.empty();
	}
    }

    /**
     * Reads the monthly characteristics of all the hydrological years of the archive (one file of a few hundreds KB per year)
     *
     * @return
     * @throws GSException
     */
    public ArchiveIndex retrieveArchiveIndex() throws GSException {

	ArchiveIndex ret = new ArchiveIndex();

	TreeSet<Integer> years = parseYears(get(MONTHLY_PATH));

	GSLoggerFactory.getLogger(getClass()).info("Reading IMGW monthly characteristics of {} hydrological years", years.size());

	for (Integer year : years) {

	    readArchiveFile(MONTHLY_PATH + year + "/mies_" + year + ".zip", reader -> {
		String line;
		while ((line = reader.readLine()) != null) {
		    parseRow(line).ifPresent(ret::add);
		}
	    });
	}

	GSLoggerFactory.getLogger(getClass()).info("Found {} IMGW series in the archive", ret.size());

	return ret;
    }

    /**
     * Retrieves the valid observations of the given period: daily values from the archive (at most {@link #MAX_ARCHIVE_YEARS}
     * hydrological years, the most recent ones) and the latest observation from the real time API
     *
     * @param series
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(IMGWEntity series, Date begin, Date end) throws GSException {

	IMGWProperty property = getProperty(series);
	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	// if the period of the series in the archive is unknown (not even known to be null), the whole archive is considered
	Date archiveBegin = series.getDate(ARCHIVE_BEGIN).orElse(begin);
	Optional<Date> archiveEnd = series.getDate(ARCHIVE_END);
	if (!series.getObject().has(ARCHIVE_END)) {
	    archiveEnd = retrieveArchiveEnd();
	}

	if (archiveEnd.isPresent() && !begin.after(archiveEnd.get()) && !end.before(archiveBegin)) {

	    LocalDate from = toLocalDate(begin.after(archiveBegin) ? begin : archiveBegin);
	    LocalDate to = toLocalDate(end.before(archiveEnd.get()) ? end : archiveEnd.get());

	    int firstYear = getHydrologicalYear(from);
	    int lastYear = getHydrologicalYear(to);

	    if (lastYear - firstYear + 1 > MAX_ARCHIVE_YEARS) {
		firstYear = lastYear - MAX_ARCHIVE_YEARS + 1;
		GSLoggerFactory.getLogger(getClass()).warn("Requested period too long, reading the archive from hydrological year {}",
			firstYear);
	    }

	    for (int year = firstYear; year <= lastYear; year++) {
		readDailyValues(series.getStationCode(), property, year, begin, end, ret);
	    }
	}

	Optional<JSONObject> station = retrieveStation(series.getStationCode());
	if (station.isPresent()) {

	    Optional<Date> latest = getLatestTime(station.get(), property);
	    if (latest.isPresent() && !latest.get().before(begin) && !latest.get().after(end)) {
		ret.put(latest.get(), getLatestValue(station.get(), property).get());
	    }
	}

	return ret;
    }

    /**
     * @return the last day of the last hydrological year of the archive of daily values
     * @throws GSException
     */
    Optional<Date> retrieveArchiveEnd() throws GSException {

	TreeSet<Integer> years = parseYears(get(DAILY_PATH));
	if (years.isEmpty()) {
	    return Optional.empty();
	}

	return Optional.of(toDate(LocalDate.of(years.last(), 10, 31)));
    }

    /**
     * @param stationCode
     * @param property
     * @param hydrologicalYear
     * @param begin
     * @param end
     * @param values the map where the valid values are added
     * @throws GSException
     */
    private void readDailyValues(String stationCode, IMGWProperty property, int hydrologicalYear, Date begin, Date end,
	    TreeMap<Date, BigDecimal> values) throws GSException {

	String directory = DAILY_PATH + hydrologicalYear + "/";

	Optional<String> listing = getOptional(directory);
	if (listing.isEmpty()) {
	    return;
	}

	for (String file : parseDailyFiles(listing.get(), hydrologicalYear, toLocalDate(begin), toLocalDate(end))) {

	    readArchiveFile(directory + file, reader -> parseDailyValues(reader, stationCode, property, begin, end, values));
	}
    }

    /**
     * @param reader
     * @param stationCode
     * @param property
     * @param begin
     * @param end
     * @param values the map where the valid values of the station between begin and end are added
     * @throws Exception
     */
    static void parseDailyValues(BufferedReader reader, String stationCode, IMGWProperty property, Date begin, Date end,
	    TreeMap<Date, BigDecimal> values) throws Exception {

	String line;
	while ((line = reader.readLine()) != null) {

	    // quick check before parsing
	    if (!line.contains(stationCode)) {
		continue;
	    }

	    Optional<ArchiveRow> row = parseRow(line);
	    if (row.isEmpty() || !row.get().code.equals(stationCode)) {
		continue;
	    }

	    BigDecimal value = row.get().getValue(property);
	    if (value == null) {
		continue;
	    }

	    Date date;
	    try {
		date = toDate(row.get().getDate());
	    } catch (Exception e) {
		continue;
	    }

	    if (!date.before(begin) && !date.after(end)) {
		values.put(date, value);
	    }
	}
    }

    /**
     * @param listing the HTML listing of the directory of a hydrological year
     * @param hydrologicalYear
     * @param from
     * @param to
     * @return the daily files of the hydrological year (one per month, or a single one for the whole year) overlapping the period
     */
    static List<String> parseDailyFiles(String listing, int hydrologicalYear, LocalDate from, LocalDate to) {

	List<String> ret = new ArrayList<>();

	Matcher matcher = DAILY_FILE.matcher(listing);
	while (matcher.find()) {

	    if (Integer.parseInt(matcher.group(2)) != hydrologicalYear || ret.contains(matcher.group(1))) {
		continue;
	    }

	    LocalDate first;
	    LocalDate last;

	    if (matcher.group(3) == null) {
		first = LocalDate.of(hydrologicalYear - 1, 11, 1);
		last = LocalDate.of(hydrologicalYear, 10, 31);
	    } else {
		int calendarMonth = (Integer.parseInt(matcher.group(3)) + 9) % 12 + 1;
		first = LocalDate.of(getCalendarYear(hydrologicalYear, calendarMonth), calendarMonth, 1);
		last = first.withDayOfMonth(first.lengthOfMonth());
	    }

	    if (!last.isBefore(from) && !first.isAfter(to)) {
		ret.add(matcher.group(1));
	    }
	}

	ret.sort(String::compareTo);

	return ret;
    }

    /**
     * @param listing the HTML listing of a directory
     * @return the years of the subdirectories
     */
    static TreeSet<Integer> parseYears(String listing) {

	TreeSet<Integer> ret = new TreeSet<>();

	Matcher matcher = YEAR_DIRECTORY.matcher(listing);
	while (matcher.find()) {
	    ret.add(Integer.parseInt(matcher.group(1)));
	}

	return ret;
    }

    /**
     * Reader of the CSV file of a zipped archive file
     */
    @FunctionalInterface
    interface ArchiveReader {

	/**
	 * @param reader
	 * @throws Exception
	 */
	void read(BufferedReader reader) throws Exception;
    }

    /**
     * @param path
     * @param archiveReader
     * @throws GSException
     */
    private void readArchiveFile(String path, ArchiveReader archiveReader) throws GSException {

	try {

	    HttpResponse<InputStream> response = downloader.downloadResponse(endpoint + path);

	    try (InputStream stream = response.body()) {

		if (response.statusCode() != 200) {
		    GSLoggerFactory.getLogger(getClass()).warn("IMGW archive {} returned HTTP {}", path, response.statusCode());
		    return;
		}

		ZipInputStream zip = new ZipInputStream(stream);
		ZipEntry entry = zip.getNextEntry();
		if (entry == null) {
		    return;
		}

		archiveReader.read(new BufferedReader(new InputStreamReader(zip, ARCHIVE_CHARSET)));
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("IMGW archive " + path + " request failed", e);
	}
    }

    /**
     * @param stationCode
     * @param property
     * @return
     */
    static String buildSeriesId(String stationCode, IMGWProperty property) {

	return stationCode + SERIES_ID_SEPARATOR + property.name();
    }

    /**
     * @param seriesId
     * @return the series identified by the given identifier, or null if not valid
     */
    public static IMGWEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	int index = seriesId.indexOf(SERIES_ID_SEPARATOR);
	if (index <= 0) {
	    return null;
	}

	String stationCode = seriesId.substring(0, index);
	Optional<IMGWProperty> property = IMGWProperty.decode(seriesId.substring(index + 1));
	if (property.isEmpty()) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, buildSeriesId(stationCode, property.get()));
	object.put(STATION_ID, stationCode);
	object.put(PROPERTY, property.get().name());

	return new IMGWEntity(object);
    }

    /**
     * @param series
     * @return
     * @throws GSException
     */
    private IMGWProperty getProperty(IMGWEntity series) throws GSException {

	return series.getProperty().orElseThrow(() -> createException("Invalid IMGW series " + series.getSeriesId(), null));
    }

    /**
     * @param hydrologicalYear
     * @param calendarMonth
     * @return the calendar year of the month (November and December belong to the following hydrological year)
     */
    static int getCalendarYear(int hydrologicalYear, int calendarMonth) {

	return calendarMonth >= 11 ? hydrologicalYear - 1 : hydrologicalYear;
    }

    /**
     * @param date
     * @return the hydrological year of the date
     */
    static int getHydrologicalYear(LocalDate date) {

	return date.getMonthValue() >= 11 ? date.getYear() + 1 : date.getYear();
    }

    /**
     * @param date
     * @return the date at 00:00 UTC
     */
    static Date toDate(LocalDate date) {

	return Date.from(date.atStartOfDay().toInstant(ZoneOffset.UTC));
    }

    /**
     * @param date
     * @return the UTC day of the date
     */
    static LocalDate toLocalDate(Date date) {

	return date.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
    }

    /**
     * @param text e.g. "2026-10-27 14:20:00" (UTC)
     * @return
     */
    static Optional<Date> parseAPIDate(String text) {

	if (text == null || text.isBlank() || text.equals("null")) {
	    return Optional.empty();
	}

	try {
	    return Optional.of(Date.from(LocalDateTime.parse(text.trim(), API_DATE_FORMAT).toInstant(ZoneOffset.UTC)));
	} catch (DateTimeParseException e) {
	    return ISO8601DateTimeUtils.parseISO8601ToDate(text);
	}
    }

    /**
     * @param text
     * @return
     */
    private static String clean(String text) {

	return text.replace("\"", "").trim();
    }

    /**
     * @param text
     * @return
     */
    private static BigDecimal parseDecimal(String text) {

	try {
	    return text == null || text.isBlank() || text.equalsIgnoreCase("NULL") ? null : new BigDecimal(text.trim());
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

	return getOptional(path).orElseThrow(() -> createException("IMGW " + path + " not available", null));
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
		throw createException("IMGW " + path + " returned HTTP " + response.statusCode(), null);
	    }

	    return text.isBlank() ? Optional.empty() : Optional.of(text);

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("IMGW " + path + " request failed", e);
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
