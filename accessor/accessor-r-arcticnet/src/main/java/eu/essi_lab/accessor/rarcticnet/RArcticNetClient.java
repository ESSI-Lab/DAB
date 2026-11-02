package eu.essi_lab.accessor.rarcticnet;

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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Client of the static archive of R-ArcticNet v4.0 (A Regional, Electronic, Hydrographic Data Network for the Arctic Region,
 * Water Systems Analysis Group, University of New Hampshire, https://www.r-arcticnet.sr.unh.edu/v4.0/).<br>
 * The archive is made of a few tab separated text files (see {@link RArcticNetDataset}): a site attributes table and a discharge
 * table (m3/s) for the monthly dataset of all the gauges and for the daily dataset of the Russian gauges. Missing values are
 * empty fields.<br>
 * Since the files are static (last modified in 2005 and 2024) and large (up to 17 MB), each file is downloaded only once and
 * cached in the temporary directory; the cached copy is revalidated with a conditional request (If-Modified-Since) at most
 * every {@link #CACHE_CHECK_INTERVAL} milliseconds.
 *
 * @author boldrini
 */
public class RArcticNetClient {

    public static final String DEFAULT_ENDPOINT = "https://www.r-arcticnet.sr.unh.edu/v4.0/";

    /**
     * Encoding of the text files (some station names include Windows-1252 characters)
     */
    static final Charset CHARSET = Charset.forName("windows-1252");

    /**
     * Minimum time between two checks of the same cached file
     */
    static final long CACHE_CHECK_INTERVAL = TimeUnit.HOURS.toMillis(6);

    // columns of the site attributes tables
    public static final String POINT_ID = "PointID";
    public static final String CODE = "Code";
    public static final String NAME = "Name";
    public static final String LAT = "Lat";
    public static final String LON = "Long";
    public static final String DRAINAGE_AREA = "DArea";
    public static final String EFFECTIVE_DRAINAGE_AREA = "DArea_effective";
    public static final String HYDROZONE = "Hydrozone";
    public static final String SOURCE = "Source";
    public static final String GAUGE_ALTITUDE = "Gauge_altitude";
    public static final String DISTANCE_TO_OUTLET = "DistanceToOutlet";
    public static final String NOTES = "Notes";

    // keys added by the client
    public static final String SERIES_ID = "seriesId";
    public static final String DATASET = "dataset";
    public static final String BEGIN = "begin";
    public static final String END = "end";
    public static final String VALUE_COUNT = "valueCount";

    private static final String SERIES_ID_SEPARATOR = "|";

    private static final Map<String, Long> CACHE_CHECKS = new ConcurrentHashMap<>();
    private static final Object CACHE_LOCK = new Object();

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public RArcticNetClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public RArcticNetClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 5);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    /**
     * Retrieves the discharge time series of the given datasets: one series per site and dataset, with begin and end given by the
     * first and the last non missing value. Sites without values are discarded
     *
     * @param datasets
     * @return the series sorted by PointID and dataset
     * @throws GSException
     */
    public List<RArcticNetEntity> retrieveSeries(Collection<RArcticNetDataset> datasets) throws GSException {

	List<RArcticNetEntity> ret = new ArrayList<>();

	for (RArcticNetDataset dataset : datasets) {

	    Map<String, JSONObject> sites = readSiteAttributes(dataset);

	    Map<String, Extent> extents;
	    try (BufferedReader reader = open(dataset.getDischargePath())) {
		extents = parseExtents(reader, dataset);
	    } catch (IOException e) {
		throw createException("Unable to read R-ArcticNet " + dataset.getDischargePath(), e);
	    }

	    for (JSONObject site : sites.values()) {

		createSeries(site, dataset, extents.get(getRowKey(site, dataset))).ifPresent(ret::add);
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Retrieved {} R-ArcticNet {} series of {} sites", //
		    ret.stream().filter(s -> s.getDataset().orElse(null) == dataset).count(), dataset.getLabel(), sites.size());
	}

	ret.sort(Comparator.comparing(RArcticNetEntity::getPointIdNumber).thenComparing(s -> s.getDataset().get()));

	return ret;
    }

    /**
     * @param site a row of the site attributes table
     * @param dataset
     * @param extent the extent of the values of the site, possibly null
     * @return the series, or empty if the site has no values
     */
    static Optional<RArcticNetEntity> createSeries(JSONObject site, RArcticNetDataset dataset, Extent extent) {

	String pointId = site.optString(POINT_ID, "");
	if (pointId.isEmpty() || extent == null || extent.count == 0) {
	    return Optional.empty();
	}

	JSONObject object = new JSONObject(site.toString());

	object.put(DATASET, dataset.name());
	object.put(SERIES_ID, buildSeriesId(pointId, dataset));
	object.put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(extent.first));
	object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(extent.last));
	object.put(VALUE_COUNT, extent.count);

	return Optional.of(new RArcticNetEntity(object));
    }

    /**
     * Retrieves the values of the given period (whole period of record if begin and end are null)
     *
     * @param series
     * @param begin the begin of the period, or null
     * @param end the end of the period, or null
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(RArcticNetEntity series, Date begin, Date end) throws GSException {

	RArcticNetDataset dataset = series.getDataset()
		.orElseThrow(() -> createException("Invalid R-ArcticNet series " + series.getSeriesId(), null));

	Optional<String> rowKey = resolveRowKey(series, dataset);
	if (rowKey.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).warn("R-ArcticNet site {} not found in the {} dataset", series.getPointId(),
		    dataset.getLabel());
	    return new TreeMap<>();
	}

	try (BufferedReader reader = open(dataset.getDischargePath())) {

	    return parseObservations(reader, dataset, rowKey.get(), begin, end);

	} catch (IOException e) {

	    throw createException("Unable to read R-ArcticNet " + dataset.getDischargePath(), e);
	}
    }

    /**
     * @param series
     * @param dataset
     * @return the key of the rows of the series in the discharge table: the PointID for the monthly dataset, the Code for the
     *         daily dataset (read from the site attributes if not available)
     * @throws GSException
     */
    private Optional<String> resolveRowKey(RArcticNetEntity series, RArcticNetDataset dataset) throws GSException {

	if (dataset == RArcticNetDataset.MONTHLY) {
	    return Optional.ofNullable(series.getPointId());
	}

	String code = series.getString(CODE);
	if (!code.isEmpty()) {
	    return Optional.of(code);
	}

	return Optional.ofNullable(readSiteAttributes(dataset).get(series.getPointId())).map(site -> getRowKey(site, dataset));
    }

    /**
     * @param dataset
     * @return the rows of the site attributes table by PointID
     * @throws GSException
     */
    private Map<String, JSONObject> readSiteAttributes(RArcticNetDataset dataset) throws GSException {

	try (BufferedReader reader = open(dataset.getSiteAttributesPath())) {

	    return parseSiteAttributes(reader);

	} catch (IOException e) {

	    throw createException("Unable to read R-ArcticNet " + dataset.getSiteAttributesPath(), e);
	}
    }

    /**
     * @param site
     * @param dataset
     * @return the key of the rows of the site in the discharge table of the dataset
     */
    static String getRowKey(JSONObject site, RArcticNetDataset dataset) {

	return dataset == RArcticNetDataset.MONTHLY ? site.optString(POINT_ID, "") : site.optString(CODE, "");
    }

    /**
     * Parses a site attributes table: tab separated, with a header line and text fields possibly enclosed in quotes
     *
     * @param reader
     * @return the rows by PointID, as JSON objects with the non empty fields
     * @throws IOException
     */
    static Map<String, JSONObject> parseSiteAttributes(BufferedReader reader) throws IOException {

	Map<String, JSONObject> ret = new LinkedHashMap<>();

	String line = reader.readLine();
	if (line == null) {
	    return ret;
	}

	String[] header = split(line);

	while ((line = reader.readLine()) != null) {

	    if (line.isBlank()) {
		continue;
	    }

	    String[] fields = split(line);

	    JSONObject site = new JSONObject();
	    for (int i = 0; i < Math.min(header.length, fields.length); i++) {
		if (!fields[i].isEmpty()) {
		    site.put(header[i], fields[i]);
		}
	    }

	    String pointId = site.optString(POINT_ID, "");
	    if (!pointId.isEmpty()) {
		ret.put(pointId, site);
	    }
	}

	return ret;
    }

    /**
     * Extent of the non missing values of a series
     */
    static class Extent {

	Date first;
	Date last;
	int count;

	/**
	 * @param date
	 */
	void add(Date date) {

	    if (first == null || date.before(first)) {
		first = date;
	    }
	    if (last == null || date.after(last)) {
		last = date;
	    }
	    count++;
	}
    }

    /**
     * Consumer of the values of a discharge table
     */
    @FunctionalInterface
    interface ValueConsumer {

	/**
	 * @param rowKey
	 * @param date
	 * @param value
	 */
	void accept(String rowKey, Date date, BigDecimal value);
    }

    /**
     * Reads the whole discharge table, computing the extent of the values of each series (rows are not strictly sorted)
     *
     * @param reader
     * @param dataset
     * @return the extents by row key (see {@link #getRowKey(JSONObject, RArcticNetDataset)})
     * @throws IOException
     */
    static Map<String, Extent> parseExtents(BufferedReader reader, RArcticNetDataset dataset) throws IOException {

	HashMap<String, Extent> ret = new HashMap<>();

	String line;
	while ((line = reader.readLine()) != null) {

	    parseRow(line, dataset, (key, date, value) -> ret.computeIfAbsent(key, k -> new Extent()).add(date));
	}

	return ret;
    }

    /**
     * Reads the values of a series from the discharge table
     *
     * @param reader
     * @param dataset
     * @param rowKey the key of the rows of the series
     * @param begin the begin of the period, or null
     * @param end the end of the period, or null
     * @return
     * @throws IOException
     */
    static TreeMap<Date, BigDecimal> parseObservations(BufferedReader reader, RArcticNetDataset dataset, String rowKey, Date begin,
	    Date end) throws IOException {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	String line;
	while ((line = reader.readLine()) != null) {

	    // fast check, before splitting the line
	    if (!line.startsWith(rowKey)) {
		continue;
	    }

	    parseRow(line, dataset, (key, date, value) -> {

		if (key.equals(rowKey) && (begin == null || !date.before(begin)) && (end == null || !date.after(end))) {
		    ret.put(date, value);
		}
	    });
	}

	return ret;
    }

    /**
     * Parses a row of a discharge table. Monthly rows are "PointID Code Year Jan ... Dec Annual", daily rows are
     * "Code Year Day Jan ... Dec". Empty fields (missing values), the header line, the annual value and the days not existing in a
     * month (e.g. February 30) are discarded. Monthly values are timestamped at the first day of the month, daily values at the
     * beginning of the day (UTC)
     *
     * @param line
     * @param dataset
     * @param consumer
     */
    static void parseRow(String line, RArcticNetDataset dataset, ValueConsumer consumer) {

	String[] fields = line.split("\t", -1);

	boolean monthly = dataset == RArcticNetDataset.MONTHLY;
	if (fields.length < 4) {
	    return;
	}

	String key = fields[0].trim();

	Optional<Integer> year = parseInteger(fields[monthly ? 2 : 1]);
	if (key.isEmpty() || year.isEmpty()) {
	    return;
	}

	int day = 1;
	if (!monthly) {
	    Optional<Integer> d = parseInteger(fields[2]);
	    if (d.isEmpty()) {
		return;
	    }
	    day = d.get();
	}

	for (int month = 1; month <= 12; month++) {

	    int index = 2 + month;
	    if (index >= fields.length) {
		break;
	    }

	    BigDecimal value = parseDecimal(fields[index]);
	    if (value == null) {
		continue;
	    }

	    if (day < 1 || day > LocalDate.of(year.get(), month, 1).lengthOfMonth()) {
		continue;
	    }

	    Date date = Date.from(LocalDate.of(year.get(), month, day).atStartOfDay(ZoneOffset.UTC).toInstant());

	    consumer.accept(key, date, value);
	}
    }

    /**
     * @param line
     * @return the tab separated fields, trimmed and without the enclosing quotes
     */
    static String[] split(String line) {

	String[] ret = line.split("\t", -1);

	for (int i = 0; i < ret.length; i++) {

	    String field = ret[i].trim();
	    if (field.length() >= 2 && field.startsWith("\"") && field.endsWith("\"")) {
		field = field.substring(1, field.length() - 1).trim();
	    }
	    ret[i] = field;
	}

	return ret;
    }

    /**
     * @param pointId
     * @param dataset
     * @return e.g. "5855|DAILY"
     */
    static String buildSeriesId(String pointId, RArcticNetDataset dataset) {

	return pointId + SERIES_ID_SEPARATOR + dataset.name();
    }

    /**
     * @param seriesId
     * @return the series identified by the given identifier, or null if not valid
     */
    public static RArcticNetEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	int index = seriesId.indexOf(SERIES_ID_SEPARATOR);
	if (index <= 0) {
	    return null;
	}

	String pointId = seriesId.substring(0, index);
	Optional<RArcticNetDataset> dataset = RArcticNetDataset.decode(seriesId.substring(index + 1));
	if (dataset.isEmpty() || !pointId.chars().allMatch(Character::isDigit)) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, buildSeriesId(pointId, dataset.get()));
	object.put(POINT_ID, pointId);
	object.put(DATASET, dataset.get().name());

	return new RArcticNetEntity(object);
    }

    /**
     * @param text e.g. "1908" or "1908.00"
     * @return
     */
    private static Optional<Integer> parseInteger(String text) {

	try {
	    String trimmed = text.trim();
	    if (trimmed.isEmpty()) {
		return Optional.empty();
	    }
	    return Optional.of(new BigDecimal(trimmed).intValueExact());
	} catch (NumberFormatException | ArithmeticException e) {
	    return Optional.empty();
	}
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
     * @param path
     * @return a reader of the cached copy of the given file
     * @throws GSException
     * @throws IOException
     */
    private BufferedReader open(String path) throws GSException, IOException {

	return Files.newBufferedReader(getFile(path).toPath(), CHARSET);
    }

    /**
     * Returns the cached copy of the given file, downloading it if not cached yet or if modified on the server
     *
     * @param path
     * @return
     * @throws GSException
     */
    File getFile(String path) throws GSException {

	String url = endpoint + path;
	File file = getCacheFile(url);

	synchronized (CACHE_LOCK) {

	    Long lastCheck = CACHE_CHECKS.get(url);
	    if (file.exists() && lastCheck != null && System.currentTimeMillis() - lastCheck < CACHE_CHECK_INTERVAL) {
		return file;
	    }

	    try {

		HttpHeaders headers = file.exists() ? HttpHeaderUtils.build("If-Modified-Since", formatHttpDate(file.lastModified()))
			: HttpHeaderUtils.buildEmpty();

		HttpResponse<InputStream> response = downloader.downloadResponse(url, headers);

		try (InputStream stream = response.body()) {

		    int status = response.statusCode();

		    if (status == 304 && file.exists()) {

			GSLoggerFactory.getLogger(getClass()).debug("R-ArcticNet {} not modified", path);

		    } else if (status == 200 && stream != null) {

			GSLoggerFactory.getLogger(getClass()).info("Downloading R-ArcticNet {}", path);

			Path tmp = Files.createTempFile(file.toPath().getParent(), "r-arcticnet", ".tmp");
			try {
			    Files.copy(stream, tmp, StandardCopyOption.REPLACE_EXISTING);
			    Files.move(tmp, file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} finally {
			    Files.deleteIfExists(tmp);
			}

			response.headers().firstValue("Last-Modified").flatMap(RArcticNetClient::parseHttpDate)
				.ifPresent(file::setLastModified);

			GSLoggerFactory.getLogger(getClass()).info("Downloaded R-ArcticNet {} ({} bytes)", path, file.length());

		    } else if (file.exists()) {

			GSLoggerFactory.getLogger(getClass()).warn("R-ArcticNet {} returned HTTP {}, using the cached copy", path, status);

		    } else {

			throw createException("R-ArcticNet " + path + " returned HTTP " + status, null);
		    }
		}

		CACHE_CHECKS.put(url, System.currentTimeMillis());

		return file;

	    } catch (GSException e) {

		throw e;

	    } catch (Exception e) {

		if (file.exists()) {
		    GSLoggerFactory.getLogger(getClass()).warn("R-ArcticNet {} request failed, using the cached copy: {}", path,
			    e.getMessage());
		    return file;
		}

		GSLoggerFactory.getLogger(getClass()).error(e);
		throw createException("R-ArcticNet " + path + " request failed", e);
	    }
	}
    }

    /**
     * @param url
     * @return the cache file of the given URL, in the temporary directory
     */
    static File getCacheFile(String url) {

	String hash;
	try {
	    hash = StringUtils.hashSHA1messageDigest(url);
	} catch (Exception e) {
	    hash = String.valueOf(url.hashCode() & 0x7fffffff);
	}

	return new File(System.getProperty("java.io.tmpdir"), "r-arcticnet-" + hash + ".txt");
    }

    /**
     * @param millis
     * @return e.g. "Wed, 27 Jul 2005 20:16:57 GMT"
     */
    static String formatHttpDate(long millis) {

	return DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(new Date(millis).toInstant(), ZoneOffset.UTC));
    }

    /**
     * @param text e.g. "Wed, 27 Jul 2005 20:16:57 GMT"
     * @return
     */
    static Optional<Long> parseHttpDate(String text) {

	try {
	    return Optional.of(ZonedDateTime.parse(text, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli());
	} catch (Exception e) {
	    return Optional.empty();
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
