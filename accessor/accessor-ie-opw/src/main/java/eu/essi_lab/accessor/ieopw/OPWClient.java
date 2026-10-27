package eu.essi_lab.accessor.ieopw;

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
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
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
 * Client of the Hydro-Data service of the Office of Public Works (OPW) of Ireland (https://waterlevel.ie/hydro-data/).<br>
 * A time series is identified by a station number and a station parameter (see {@link OPWParameter}). The stations of each
 * parameter are listed by a map layer, together with the time of the latest observation. Observations of the last week and of
 * the last month are published as JSON, while the whole period of record (15 minutes values) is published as a zipped CSV
 * file.<br>
 * Only stations with numbers between {@link #MIN_REPUBLISHABLE_STATION} and {@link #MAX_REPUBLISHABLE_STATION} are suitable for
 * republication (see https://waterlevel.ie/page/api/)
 *
 * @author boldrini
 */
public class OPWClient {

    public static final String DEFAULT_ENDPOINT = "https://waterlevel.ie/hydro-data/";

    static final String DATA_PATH = "data/internet/";
    static final String STATIONS_PATH = DATA_PATH + "stations/stations.json";

    static final String WEEK_FEED = "week.json";
    static final String MONTH_FEED = "month.json";

    /**
     * Width of the time window certainly covered by each feed (the week feed starts about 7 days ago, the month feed about 30 days
     * ago)
     */
    static final long WEEK_FEED_WINDOW = TimeUnit.DAYS.toMillis(6);
    static final long MONTH_FEED_WINDOW = TimeUnit.DAYS.toMillis(28);

    public static final int MIN_REPUBLISHABLE_STATION = 1;
    public static final int MAX_REPUBLISHABLE_STATION = 41000;

    /**
     * Quality codes of missing (-1, 255) and unusable (151) values
     */
    static final Set<Integer> INVALID_QUALITY_CODES = new HashSet<>(Arrays.asList(-1, 151, 255));

    /**
     * Maximum number of rows read from the beginning of an archive looking for the first valid value
     */
    static final int FIRST_OBSERVATION_MAX_ROWS = 100000;

    // keys of the map layer entries
    public static final String STATION_NO = "station_no";
    public static final String STATION_NAME = "station_name";
    public static final String LAT = "station_latitude";
    public static final String LON = "station_longitude";
    public static final String PARAMETER = "stationparameter_name";
    public static final String PARAMETER_LONG_NAME = "stationparameter_longname";
    public static final String UNIT = "ts_unitsymbol";
    public static final String TS_SHORT_NAME = "ts_shortname";
    public static final String LATEST_TIMESTAMP = "timestamp";
    public static final String CATCHMENT = "catchment_name";
    public static final String WATERBODY = "WTO_OBJECT";
    public static final String RIVER_BASIN_DISTRICT = "RBD";
    public static final String CATCHMENT_SIZE = "CATCHMENT_SIZE";
    public static final String GAUGE_DATUM = "GAUGE_DATUM";
    public static final String GAUGE_DATUM_UNIT = "station_gauge_datum_unit";
    public static final String STATION_CLASS = "Stn_Class";

    // keys added from the station list
    public static final String STATION_STATUS = "station_status";
    public static final String STATION_DESCRIPTION = "ObjectDescription";

    // keys added by the client and by the connector
    public static final String SERIES_ID = "seriesId";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    private static final String SERIES_ID_SEPARATOR = "|";

    private static final DateTimeFormatter ARCHIVE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	    .withZone(ZoneOffset.UTC);

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public OPWClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public OPWClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 5);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    /**
     * Retrieves the time series of the given parameters at the stations suitable for republication. Series without observations
     * are discarded
     *
     * @param parameters
     * @return the series sorted by station number and parameter
     * @throws GSException
     */
    public List<OPWEntity> retrieveSeries(Collection<OPWParameter> parameters) throws GSException {

	HashMap<String, JSONObject> stations = new HashMap<>();

	JSONArray stationList = new JSONArray(get(STATIONS_PATH));
	for (int i = 0; i < stationList.length(); i++) {
	    JSONObject station = stationList.getJSONObject(i);
	    stations.put(station.optString(STATION_NO), station);
	}

	List<OPWEntity> ret = new ArrayList<>();

	for (OPWParameter parameter : parameters) {

	    JSONArray layer = new JSONArray(get(DATA_PATH + "layers/" + parameter.getLayerId() + "/index.json"));

	    for (int i = 0; i < layer.length(); i++) {

		createSeries(layer.getJSONObject(i), parameter, stations.get(layer.getJSONObject(i).optString(STATION_NO))).//
			ifPresent(ret::add);
	    }
	}

	ret.sort(Comparator.comparing(OPWEntity::getStationNumber).thenComparing(OPWEntity::getSeriesId));

	return ret;
    }

    /**
     * @param layerEntry the entry of the station in the map layer of the parameter
     * @param parameter
     * @param station the station description, possibly null
     * @return the series, or empty if the station is not suitable for republication or the series has no observations
     */
    static Optional<OPWEntity> createSeries(JSONObject layerEntry, OPWParameter parameter, JSONObject station) {

	String stationNumber = layerEntry.optString(STATION_NO, "");
	if (!isRepublishable(stationNumber)) {
	    return Optional.empty();
	}

	Optional<Date> latest = parseDate(layerEntry.optString(LATEST_TIMESTAMP, null));
	if (latest.isEmpty()) {
	    return Optional.empty();
	}

	JSONObject object = new JSONObject(layerEntry.toString());

	object.put(PARAMETER, parameter.name());
	object.put(SERIES_ID, buildSeriesId(stationNumber, parameter));
	object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(latest.get()));

	if (station != null) {
	    for (String key : new String[] { STATION_STATUS, STATION_DESCRIPTION }) {
		String value = station.optString(key, "").trim();
		if (!value.isEmpty()) {
		    object.put(key, value);
		}
	    }
	}

	return Optional.of(new OPWEntity(object));
    }

    /**
     * @param stationNumber
     * @return true if the data of the station can be republished
     */
    public static boolean isRepublishable(String stationNumber) {

	if (stationNumber == null || stationNumber.isEmpty() || !stationNumber.chars().allMatch(Character::isDigit)) {
	    return false;
	}

	try {
	    int number = Integer.parseInt(stationNumber);
	    return number >= MIN_REPUBLISHABLE_STATION && number <= MAX_REPUBLISHABLE_STATION;
	} catch (NumberFormatException e) {
	    return false;
	}
    }

    /**
     * @param series
     * @return the time of the first valid observation of the series, read from the beginning of its archive (or from the most
     *         recent feeds if the archive is not available)
     * @throws GSException
     */
    public Optional<Date> retrieveFirstObservation(OPWEntity series) throws GSException {

	OPWParameter parameter = getParameter(series);

	Optional<Date> first = readArchive(series, parameter, null, null, FIRST_OBSERVATION_MAX_ROWS, true).values.keySet().stream()
		.findFirst();

	if (first.isPresent()) {
	    return first;
	}

	for (String feed : new String[] { MONTH_FEED, WEEK_FEED }) {
	    Optional<Observations> observations = readFeed(series, parameter, feed);
	    if (observations.isPresent() && !observations.get().values.isEmpty()) {
		return Optional.of(observations.get().values.firstKey());
	    }
	}

	return Optional.empty();
    }

    /**
     * Retrieves the valid observations of the given period. Recent periods are read from the week and month feeds, older ones from
     * the archive of the whole period of record, completed with the month feed if the period ends after the archive
     *
     * @param series
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(OPWEntity series, Date begin, Date end) throws GSException {

	OPWParameter parameter = getParameter(series);
	long now = System.currentTimeMillis();

	String[] feeds = new String[] { WEEK_FEED, MONTH_FEED };
	long[] windows = new long[] { WEEK_FEED_WINDOW, MONTH_FEED_WINDOW };

	for (int i = 0; i < feeds.length; i++) {

	    if (begin.getTime() < now - windows[i]) {
		continue;
	    }

	    Optional<Observations> observations = readFeed(series, parameter, feeds[i]);
	    if (observations.isPresent() && observations.get().covers(begin)) {
		return subMap(observations.get().values, begin, end);
	    }
	}

	Observations archive = readArchive(series, parameter, begin, end, Integer.MAX_VALUE, false);
	TreeMap<Date, BigDecimal> ret = archive.values;

	// the archive is updated daily: the most recent observations are taken from the month feed
	if (archive.complete && archive.last != null && archive.last.before(end)) {

	    readFeed(series, parameter, MONTH_FEED).ifPresent(month -> {
		Date from = archive.last.after(begin) ? new Date(archive.last.getTime() + 1) : begin;
		if (!from.after(end)) {
		    ret.putAll(subMap(month.values, from, end));
		}
	    });
	}

	return ret;
    }

    /**
     * Observations read from a feed or from an archive
     */
    static class Observations {

	/**
	 * The valid values
	 */
	final TreeMap<Date, BigDecimal> values = new TreeMap<>();

	/**
	 * Time of the first and of the last row (also of invalid values)
	 */
	Date first;
	Date last;

	/**
	 * True if all the rows were read
	 */
	boolean complete;

	/**
	 * @param date
	 * @return true if the rows start before the given date
	 */
	boolean covers(Date date) {

	    return first != null && !first.after(date);
	}

	/**
	 * @param date
	 * @param value
	 * @param qualityCode
	 */
	void addRow(Date date, BigDecimal value, Integer qualityCode) {

	    if (first == null) {
		first = date;
	    }
	    last = date;

	    if (value != null && (qualityCode == null || !INVALID_QUALITY_CODES.contains(qualityCode))) {
		values.put(date, value);
	    }
	}
    }

    /**
     * @param series
     * @param parameter
     * @param feed {@link #WEEK_FEED} or {@link #MONTH_FEED}
     * @return the observations of the feed, or empty if the feed is not available
     * @throws GSException
     */
    Optional<Observations> readFeed(OPWEntity series, OPWParameter parameter, String feed) throws GSException {

	String path = getStationPath(series, parameter) + feed;

	Optional<String> text = getOptional(path);
	if (text.isEmpty()) {
	    return Optional.empty();
	}

	try {

	    return Optional.of(parseFeed(new JSONArray(text.get())));

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).warn("Unable to parse OPW feed {}: {}", path, e.getMessage());
	    return Optional.empty();
	}
    }

    /**
     * @param feed
     * @return
     */
    static Observations parseFeed(JSONArray feed) {

	Observations ret = new Observations();

	if (feed.isEmpty()) {
	    return ret;
	}

	JSONArray data = feed.getJSONObject(0).optJSONArray("data");
	if (data == null) {
	    return ret;
	}

	for (int i = 0; i < data.length(); i++) {

	    JSONArray row = data.getJSONArray(i);

	    Optional<Date> date = parseDate(row.optString(0, null));
	    if (date.isEmpty()) {
		continue;
	    }

	    BigDecimal value = row.isNull(1) ? null : row.optBigDecimal(1, null);
	    Integer qualityCode = row.length() > 2 && !row.isNull(2) ? row.optInt(2) : null;

	    ret.addRow(date.get(), value, qualityCode);
	}

	ret.complete = true;

	return ret;
    }

    /**
     * Reads the archive of the whole period of record: a zipped CSV with comment lines starting with "#" and rows
     * "Timestamp;Value;Quality Code". The archive is streamed and the download is interrupted as soon as possible
     *
     * @param series
     * @param parameter
     * @param begin the begin of the period, or null
     * @param end the end of the period, or null
     * @param maxRows the maximum number of rows to read
     * @param firstValueOnly true to stop at the first valid value
     * @return
     * @throws GSException
     */
    private Observations readArchive(OPWEntity series, OPWParameter parameter, Date begin, Date end, int maxRows,
	    boolean firstValueOnly) throws GSException {

	String path = getStationPath(series, parameter) + parameter.getFileName() + "_complete.zip";

	try {

	    HttpResponse<InputStream> response = downloader.downloadResponse(endpoint + path);

	    try (InputStream stream = response.body()) {

		if (response.statusCode() != 200) {
		    GSLoggerFactory.getLogger(getClass()).warn("OPW archive {} returned HTTP {}", path, response.statusCode());
		    return new Observations();
		}

		ZipInputStream zip = new ZipInputStream(stream);
		ZipEntry entry = zip.getNextEntry();
		if (entry == null) {
		    return new Observations();
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(zip, StandardCharsets.UTF_8));

		return parseArchive(reader, begin, end, maxRows, firstValueOnly);
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("OPW archive " + path + " request failed", e);
	}
    }

    /**
     * @param reader
     * @param begin the begin of the period, or null
     * @param end the end of the period, or null
     * @param maxRows the maximum number of rows to read
     * @param firstValueOnly true to stop at the first valid value
     * @return
     * @throws Exception
     */
    static Observations parseArchive(BufferedReader reader, Date begin, Date end, int maxRows, boolean firstValueOnly)
	    throws Exception {

	Observations ret = new Observations();

	// rows are sorted by time and timestamps have a fixed format, so they can be compared as strings
	String beginText = begin == null ? null : formatDate(begin);
	String endText = end == null ? null : formatDate(end);

	int rows = 0;
	String line;

	while ((line = reader.readLine()) != null) {

	    if (line.isEmpty() || line.startsWith("#")) {
		continue;
	    }

	    if (++rows > maxRows) {
		return ret;
	    }

	    String[] split = line.split(";", -1);
	    String timestamp = split[0];

	    if (beginText != null && timestamp.compareTo(beginText) < 0) {
		continue;
	    }

	    if (endText != null && timestamp.compareTo(endText) > 0) {
		return ret;
	    }

	    Optional<Date> date = parseDate(timestamp);
	    if (date.isEmpty()) {
		continue;
	    }

	    BigDecimal value = split.length > 1 ? parseDecimal(split[1]) : null;
	    Integer qualityCode = split.length > 2 ? parseInteger(split[2]) : null;

	    ret.addRow(date.get(), value, qualityCode);

	    if (firstValueOnly && !ret.values.isEmpty()) {
		return ret;
	    }
	}

	ret.complete = true;

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
     * @param stationNumber
     * @param parameter
     * @return
     */
    static String buildSeriesId(String stationNumber, OPWParameter parameter) {

	return stationNumber + SERIES_ID_SEPARATOR + parameter.name();
    }

    /**
     * @param seriesId
     * @return the series identified by the given identifier, or null if not valid
     */
    public static OPWEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	int index = seriesId.indexOf(SERIES_ID_SEPARATOR);
	if (index <= 0) {
	    return null;
	}

	String stationNumber = seriesId.substring(0, index);
	Optional<OPWParameter> parameter = OPWParameter.decode(seriesId.substring(index + 1));
	if (parameter.isEmpty()) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, buildSeriesId(stationNumber, parameter.get()));
	object.put(STATION_NO, stationNumber);
	object.put(PARAMETER, parameter.get().name());

	return new OPWEntity(object);
    }

    /**
     * @param series
     * @param parameter
     * @return the path of the directory of the series, e.g. "data/internet/stations/0/01041/S/"
     */
    static String getStationPath(OPWEntity series, OPWParameter parameter) {

	return DATA_PATH + "stations/0/" + series.getStationNumber() + "/" + parameter.name() + "/";
    }

    /**
     * @param series
     * @return
     * @throws GSException
     */
    private OPWParameter getParameter(OPWEntity series) throws GSException {

	return series.getParameter().orElseThrow(() -> createException("Invalid OPW series " + series.getSeriesId(), null));
    }

    /**
     * @param text e.g. "2026-09-27T23:00:00.000Z"
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
     * @param date
     * @return the date in the format of the archives, e.g. "2026-09-27T23:00:00.000Z"
     */
    static String formatDate(Date date) {

	return ARCHIVE_DATE_FORMAT.format(date.toInstant());
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
     * @param text
     * @return
     */
    private static Integer parseInteger(String text) {

	try {
	    return text.isBlank() ? null : Integer.valueOf(text.trim());
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

	return getOptional(path).orElseThrow(() -> createException("OPW " + path + " not available", null));
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
		throw createException("OPW " + path + " returned HTTP " + response.statusCode(), null);
	    }

	    return text.isBlank() ? Optional.empty() : Optional.of(text);

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("OPW " + path + " request failed", e);
	}
    }

    /**
     * @param message
     * @param cause
     * @return
     */
    private GSException createException(String message, Exception cause) {

	GSLoggerFactory.getLogger(getClass()).error(message);

	return GSException.createException(//
		getClass(), //
		ErrorInfo.ERRORTYPE_SERVICE, //
		ErrorInfo.SEVERITY_ERROR, //
		message, //
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
