package eu.essi_lab.accessor.atehyd;

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
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Client of the eHYD portal of the Austrian Hydrographic Service (https://ehyd.gv.at), published by the Federal Ministry of
 * Agriculture and Forestry, Climate and Environmental Protection, Regions and Water Management (BML).<br>
 * eHYD publishes the checked archive data of the hydrographic yearbooks (typically up to 2-3 years ago) as CSV files, one file per
 * station and product (see {@link EHYDProduct}). The station list of each category (owf = surface water, gw = groundwater) is a
 * GeoJSON (EPSG:3857) listing for each station its files, with number, size and first/last year. Files are encoded in ISO-8859-1,
 * start with a header describing the station and the series and continue after the "Werte:" line with the rows
 * "dd.MM.yyyy HH:mm:ss;value", where gaps are reported as "Lücke". Times are in MEZ (UTC+1, no daylight saving time).<br>
 * The file server supports HTTP range requests, so the begin and the end of a series are read from the first and from the last
 * bytes of its file, without downloading it
 *
 * @author boldrini
 */
public class EHYDClient {

    public static final String DEFAULT_ENDPOINT = "https://ehyd.gv.at/services/";

    static final String STATIONS_PATH = "Messstellen/json?filter=";
    static final String INFO_PATH = "Messstellen/info?hzbnr=";
    static final String FILES_PATH = "MessstellenExtraData/";

    /**
     * Number of bytes read from the beginning of a file to get its header and its first value
     */
    static final int HEAD_BYTES = 16384;

    /**
     * Number of bytes read from the end of a file to get its last value
     */
    static final int TAIL_BYTES = 4096;

    /**
     * Times of the eHYD files are in MEZ (Central European Time, without daylight saving time)
     */
    static final ZoneOffset TIME_ZONE = ZoneOffset.ofHours(1);

    static final String GAP = "Lücke";
    static final String VALUES_LINE = "Werte:";

    private static final Pattern VALUE_PATTERN = Pattern
	    .compile("^(\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}:\\d{2})\\s*;\\s*([^;]*)");

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    // keys of the station list
    static final String FEATURES = "features";
    static final String PROPERTIES = "properties";
    static final String GEOMETRY = "geometry";
    static final String STATION_HZBNR = "hzbnr01";
    static final String FILES = "fjson";
    static final String FILE_TYPE = "ftyp";
    static final String FILE_FOUND = "ffound";

    // keys of the series
    public static final String SERIES_ID = "seriesId";
    public static final String HZBNR = "hzbnr";
    public static final String PRODUCT = "product";
    public static final String FILE_NUMBER = "filenr";
    public static final String FILE_NAME = "file";
    public static final String FILE_SIZE = "fsize";
    public static final String FIRST_YEAR = "filefrom";
    public static final String LAST_YEAR = "fileto";
    public static final String LAT = "lat";
    public static final String LON = "lon";

    // keys read from the file header
    public static final String STATION_NAME = "stationName";
    public static final String WATERBODY = "waterbody";
    public static final String OFFICE = "office";
    public static final String OPERATOR = "operator";
    public static final String CATCHMENT_AREA = "catchmentArea";
    public static final String GAUGE_DATUM = "gaugeDatum";
    public static final String UNIT = "unit";
    public static final String EXPORT_SERIES = "exportSeries";
    public static final String DBMS_NUMBER = "dbmsNumber";
    public static final String FEDERAL_STATE = "federalState";
    public static final String GROUNDWATER_BODY = "groundwaterBody";
    public static final String AQUIFER = "aquifer";

    // keys added by the client
    public static final String BEGIN = "begin";
    public static final String END = "end";

    /**
     * Header fields of the files and corresponding keys of the series
     */
    static final Map<String, String> HEADER_FIELDS = new LinkedHashMap<>();
    static {
	HEADER_FIELDS.put("Messstelle", STATION_NAME);
	HEADER_FIELDS.put("DBMS-Nummer", DBMS_NUMBER);
	HEADER_FIELDS.put("Gewässer", WATERBODY);
	HEADER_FIELDS.put("Dienststelle", OFFICE);
	HEADER_FIELDS.put("Messstellenbetreiber", OPERATOR);
	HEADER_FIELDS.put("orogr.Einzugsgebiet [km²]", CATCHMENT_AREA);
	HEADER_FIELDS.put("Bundesland", FEDERAL_STATE);
	HEADER_FIELDS.put("Grundwasserkörper", GROUNDWATER_BODY);
	HEADER_FIELDS.put("PorenGW-Gebiet", AQUIFER);
	HEADER_FIELDS.put("Exportzeitreihe", EXPORT_SERIES);
	HEADER_FIELDS.put("Einheit", UNIT);
    }

    /**
     * Header section listing the gauge datum (height of the gauge zero, m above the Adriatic sea) since a given date
     */
    static final String GAUGE_DATUM_SECTION = "Pegelnullpunkt";

    private static final String SERIES_ID_SEPARATOR = "|";

    private static final double EARTH_RADIUS = 6378137.0;

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public EHYDClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public EHYDClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setConnectionTimeout(TimeUnit.SECONDS, 30);
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 3);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    /**
     * Retrieves the time series of the given products from the station lists of their categories (one request per category)
     *
     * @param products
     * @return the series sorted by HZB number and product, without begin and end (see {@link #completeSeries(EHYDEntity)})
     * @throws GSException
     */
    public List<EHYDEntity> retrieveSeries(Collection<EHYDProduct> products) throws GSException {

	Set<String> categories = new LinkedHashSet<>();
	products.forEach(p -> categories.add(p.getCategory()));

	List<EHYDEntity> ret = new ArrayList<>();

	for (String category : categories) {

	    JSONObject stations = new JSONObject(get(STATIONS_PATH + category));
	    ret.addAll(parseStations(stations, products));
	}

	ret.sort(Comparator.comparing(EHYDEntity::getHZBNumber).thenComparing(EHYDEntity::getSeriesId));

	return ret;
    }

    /**
     * @param stations the station list (GeoJSON in EPSG:3857)
     * @param products
     * @return the series of the given products listed by the station list
     */
    static List<EHYDEntity> parseStations(JSONObject stations, Collection<EHYDProduct> products) {

	LinkedHashMap<String, EHYDEntity> ret = new LinkedHashMap<>();

	JSONArray features = stations.optJSONArray(FEATURES);
	if (features == null) {
	    return new ArrayList<>();
	}

	for (int i = 0; i < features.length(); i++) {

	    JSONObject feature = features.getJSONObject(i);
	    JSONObject properties = feature.optJSONObject(PROPERTIES);
	    if (properties == null) {
		continue;
	    }

	    String hzbnr = properties.optString(STATION_HZBNR, "").trim();
	    if (hzbnr.isEmpty()) {
		continue;
	    }

	    double[] latLon = getLatLon(feature.optJSONObject(GEOMETRY));

	    JSONArray files;
	    try {
		files = new JSONArray(properties.optString(FILES, "[]"));
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(EHYDClient.class).warn("Invalid file list of eHYD station {}", hzbnr);
		continue;
	    }

	    for (int j = 0; j < files.length(); j++) {

		JSONObject file = files.getJSONObject(j);

		Optional<EHYDProduct> product = EHYDProduct.decode(file.optString(FILE_TYPE, null));
		if (product.isEmpty() || !products.contains(product.get()) || file.optInt(FILE_FOUND, 1) != 1) {
		    continue;
		}

		JSONObject object = new JSONObject();
		object.put(SERIES_ID, buildSeriesId(hzbnr, product.get()));
		object.put(HZBNR, hzbnr);
		object.put(PRODUCT, product.get().getId());
		object.put(FILE_NUMBER, file.optInt(FILE_NUMBER));
		object.put(FILE_NAME, file.optString(FILE_NAME));
		object.put(FILE_SIZE, file.optLong(FILE_SIZE));
		object.put(FIRST_YEAR, file.optInt(FIRST_YEAR));
		object.put(LAST_YEAR, file.optInt(LAST_YEAR));

		if (latLon != null) {
		    object.put(LAT, latLon[0]);
		    object.put(LON, latLon[1]);
		}

		ret.put(object.getString(SERIES_ID), new EHYDEntity(object));
	    }
	}

	return new ArrayList<>(ret.values());
    }

    /**
     * @param geometry a point in EPSG:3857
     * @return the WGS84 latitude and longitude, or null if not available
     */
    static double[] getLatLon(JSONObject geometry) {

	if (geometry == null) {
	    return null;
	}

	JSONArray coordinates = geometry.optJSONArray("coordinates");
	if (coordinates == null || coordinates.length() < 2) {
	    return null;
	}

	double x = coordinates.optDouble(0);
	double y = coordinates.optDouble(1);
	if (Double.isNaN(x) || Double.isNaN(y)) {
	    return null;
	}

	double lon = Math.toDegrees(x / EARTH_RADIUS);
	double lat = Math.toDegrees(2 * Math.atan(Math.exp(y / EARTH_RADIUS)) - Math.PI / 2);

	return new double[] { round(lat), round(lon) };
    }

    /**
     * @param value
     * @return the value rounded to 6 decimals
     */
    private static double round(double value) {

	return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Completes the series with the header of its file and with the time of its first and of its last valid value. The first
     * {@link #HEAD_BYTES} and the last {@link #TAIL_BYTES} of the file are downloaded (only if a value is not found there the whole
     * file is downloaded)
     *
     * @param series
     * @return false if the series has no valid values
     * @throws GSException
     */
    public boolean completeSeries(EHYDEntity series) throws GSException {

	EHYDProduct product = getProduct(series);
	String path = getFilePath(series.getHZBNumber(), product, series.getFileNumber());

	RangeResponse head = getRange(path, "bytes=0-" + (HEAD_BYTES - 1));

	Observations observations = parse(new BufferedReader(new StringReader(head.text)), null, null, true);
	putHeader(series, observations);

	Optional<Date> first = observations.getFirst();
	Optional<Date> last = head.complete ? observations.getLast() : Optional.empty();

	if (!head.complete && first.isPresent()) {

	    RangeResponse tail = getRange(path, "bytes=-" + TAIL_BYTES);
	    last = parseTail(tail.text).getLast();
	}

	if (first.isEmpty() || last.isEmpty()) {

	    if (head.complete) {
		return false;
	    }

	    // e.g. long gaps at the beginning or at the end of the series
	    Observations all = readFile(path, null, null);
	    first = all.getFirst();
	    last = all.getLast();
	}

	if (first.isEmpty() || last.isEmpty()) {
	    return false;
	}

	series.getObject().put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(first.get()));
	series.getObject().put(END, ISO8601DateTimeUtils.getISO8601DateTime(last.get()));

	return true;
    }

    /**
     * @param series
     * @param observations
     */
    private static void putHeader(EHYDEntity series, Observations observations) {

	for (Map.Entry<String, String> entry : observations.header.entrySet()) {
	    series.getObject().put(entry.getKey(), entry.getValue());
	}
    }

    /**
     * Retrieves the time of the current last valid value of the series (one small request to resolve the file of the series and
     * one to read the end of the file)
     *
     * @param series
     * @return
     * @throws GSException
     */
    public Optional<Date> retrieveLastObservation(EHYDEntity series) throws GSException {

	EHYDProduct product = getProduct(series);
	String path = getFilePath(series.getHZBNumber(), product, retrieveFileNumber(series));

	RangeResponse tail = getRange(path, "bytes=-" + TAIL_BYTES);

	return parseTail(tail.text).getLast();
    }

    /**
     * Retrieves the valid values of the given period. eHYD files contain the whole series (at most about 1.5 MB for 50 years of
     * daily values), so a single request is made, whatever the length of the period
     *
     * @param series
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(EHYDEntity series, Date begin, Date end) throws GSException {

	EHYDProduct product = getProduct(series);
	String path = getFilePath(series.getHZBNumber(), product, retrieveFileNumber(series));

	return readFile(path, begin, end).values;
    }

    /**
     * The numbers of the files of a station depend on the available products, so they are resolved with the station info
     *
     * @param series
     * @return the current number of the file of the series, or the harvested one if not found
     * @throws GSException
     */
    Integer retrieveFileNumber(EHYDEntity series) throws GSException {

	EHYDProduct product = getProduct(series);

	Optional<String> info = getOptional(INFO_PATH + series.getHZBNumber());
	if (info.isPresent()) {
	    try {
		Optional<Integer> number = findFileNumber(new JSONArray(info.get()), product.getFileName(series.getHZBNumber()));
		if (number.isPresent()) {
		    return number.get();
		}
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).warn("Unable to parse eHYD info of station {}: {}", series.getHZBNumber(),
			e.getMessage());
	    }
	}

	if (series.getFileNumber() == null) {
	    throw createException("eHYD file of series " + series.getSeriesId() + " not found", null);
	}

	return series.getFileNumber();
    }

    /**
     * @param info the station info
     * @param fileName e.g. "W-Tagesmittel-201582.csv"
     * @return the number of the file with the given name
     */
    static Optional<Integer> findFileNumber(Object info, String fileName) {

	if (info instanceof JSONArray) {

	    JSONArray array = (JSONArray) info;
	    for (int i = 0; i < array.length(); i++) {
		Optional<Integer> ret = findFileNumber(array.get(i), fileName);
		if (ret.isPresent()) {
		    return ret;
		}
	    }

	} else if (info instanceof JSONObject) {

	    JSONObject object = (JSONObject) info;
	    if (fileName.equalsIgnoreCase(object.optString("fileName", "")) && object.has("fileNr")) {
		return Optional.of(object.optInt("fileNr"));
	    }

	    for (String key : object.keySet()) {
		Optional<Integer> ret = findFileNumber(object.get(key), fileName);
		if (ret.isPresent()) {
		    return ret;
		}
	    }
	}

	return Optional.empty();
    }

    /**
     * Values and header read from a file, or from a part of it
     */
    static class Observations {

	/**
	 * The valid values
	 */
	final TreeMap<Date, BigDecimal> values = new TreeMap<>();

	/**
	 * The header fields, by series key
	 */
	final Map<String, String> header = new HashMap<>();

	/**
	 * @return the time of the first valid value
	 */
	Optional<Date> getFirst() {

	    return values.isEmpty() ? Optional.empty() : Optional.of(values.firstKey());
	}

	/**
	 * @return the time of the last valid value
	 */
	Optional<Date> getLast() {

	    return values.isEmpty() ? Optional.empty() : Optional.of(values.lastKey());
	}
    }

    /**
     * Parses a file, or its beginning
     *
     * @param reader
     * @param begin the begin of the period, or null
     * @param end the end of the period, or null (rows are sorted by time, reading stops after the end)
     * @param header true if the reader starts at the beginning of the file
     * @return
     * @throws GSException
     */
    static Observations parse(BufferedReader reader, Date begin, Date end, boolean header) throws GSException {

	Observations ret = new Observations();

	boolean inHeader = header;
	String section = null;

	try {

	    String line;
	    while ((line = reader.readLine()) != null) {

		if (inHeader) {

		    if (line.trim().equals(VALUES_LINE)) {
			inHeader = false;
		    } else {
			section = parseHeaderLine(line, section, ret.header);
		    }
		    continue;
		}

		Matcher matcher = VALUE_PATTERN.matcher(line);
		if (!matcher.find()) {
		    continue;
		}

		Optional<Date> date = parseDate(matcher.group(1));
		if (date.isEmpty()) {
		    continue;
		}

		if (begin != null && date.get().before(begin)) {
		    continue;
		}

		if (end != null && date.get().after(end)) {
		    break;
		}

		parseValue(matcher.group(2)).ifPresent(value -> ret.values.put(date.get(), value));
	    }

	} catch (Exception e) {

	    throw GSException.createException(//
		    EHYDClient.class, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "Unable to read eHYD file: " + e.getMessage(), //
		    e);
	}

	return ret;
    }

    /**
     * @param text the last bytes of a file (the first line, possibly truncated, is discarded)
     * @return
     * @throws GSException
     */
    static Observations parseTail(String text) throws GSException {

	int index = text.indexOf('\n');
	String lines = index < 0 ? "" : text.substring(index + 1);

	return parse(new BufferedReader(new StringReader(lines)), null, null, false);
    }

    /**
     * Header lines are "Key:   ;value", or a section title "Key:" followed by indented rows "   dd.MM.yyyy   ;value"
     *
     * @param line
     * @param section the current section
     * @param header
     * @return the new current section
     */
    private static String parseHeaderLine(String line, String section, Map<String, String> header) {

	if (line.isBlank()) {
	    return section;
	}

	int separator = line.indexOf(';');

	if (Character.isWhitespace(line.charAt(0))) {

	    // a row of the current section, the last one is the current value
	    if (GAUGE_DATUM_SECTION.equals(section) && separator > 0
		    && line.substring(0, separator).trim().matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
		parseValue(line.substring(separator + 1)).ifPresent(v -> header.put(GAUGE_DATUM, v.toPlainString()));
	    }
	    return section;
	}

	String key = (separator < 0 ? line : line.substring(0, separator)).trim();
	if (key.endsWith(":")) {
	    key = key.substring(0, key.length() - 1).trim();
	}

	if (separator < 0) {
	    return key;
	}

	String field = HEADER_FIELDS.get(key);
	String value = line.substring(separator + 1).trim();

	if (field != null && !value.isEmpty() && !header.containsKey(field)) {
	    if (field.equals(UNIT)) {
		value = value.replace("[", "").replace("]", "").trim();
	    }
	    header.put(field, value);
	}

	return key;
    }

    /**
     * @param text e.g. "  1,01", "01014,52" or "Lücke"
     * @return the value, or empty if missing
     */
    static Optional<BigDecimal> parseValue(String text) {

	if (text == null) {
	    return Optional.empty();
	}

	String value = text.trim();
	if (value.isEmpty() || value.startsWith(GAP)) {
	    return Optional.empty();
	}

	try {
	    return Optional.of(new BigDecimal(value.replace(',', '.')).stripTrailingZeros());
	} catch (NumberFormatException e) {
	    return Optional.empty();
	}
    }

    /**
     * @param text e.g. "01.01.1971 00:00:00" (MEZ)
     * @return
     */
    static Optional<Date> parseDate(String text) {

	try {
	    return Optional.of(Date.from(LocalDateTime.parse(text.trim(), DATE_FORMAT).toInstant(TIME_ZONE)));
	} catch (DateTimeParseException e) {
	    return Optional.empty();
	}
    }

    /**
     * Reads a whole file, streaming it
     *
     * @param path
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    private Observations readFile(String path, Date begin, Date end) throws GSException {

	try {

	    HttpResponse<InputStream> response = downloader.downloadResponse(endpoint + path);

	    try (InputStream stream = response.body()) {

		if (response.statusCode() != 200) {
		    throw createException("eHYD " + path + " returned HTTP " + response.statusCode(), null);
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.ISO_8859_1));
		return parse(reader, begin, end, true);
	    }

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("eHYD " + path + " request failed", e);
	}
    }

    /**
     * A part of a file
     */
    static class RangeResponse {

	String text;

	/**
	 * True if the whole file was returned
	 */
	boolean complete;
    }

    /**
     * @param path
     * @param range e.g. "bytes=0-16383" or "bytes=-4096"
     * @return
     * @throws GSException
     */
    private RangeResponse getRange(String path, String range) throws GSException {

	try {

	    HttpResponse<InputStream> response = downloader.downloadResponse(endpoint + path, HttpHeaderUtils.build("Range", range));

	    RangeResponse ret = new RangeResponse();

	    try (InputStream stream = response.body()) {
		ret.text = stream == null ? "" : IOUtils.toString(stream, StandardCharsets.ISO_8859_1);
	    }

	    if (response.statusCode() == 200) {

		ret.complete = true;

	    } else if (response.statusCode() == 206) {

		// e.g. "bytes 0-16383/522705"
		String contentRange = response.headers().firstValue("Content-Range").orElse("");
		ret.complete = isWholeFile(contentRange);

	    } else {

		throw createException("eHYD " + path + " returned HTTP " + response.statusCode(), null);
	    }

	    return ret;

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("eHYD " + path + " request failed", e);
	}
    }

    /**
     * @param contentRange e.g. "bytes 0-16383/522705"
     * @return true if the range covers the whole file
     */
    static boolean isWholeFile(String contentRange) {

	Matcher matcher = Pattern.compile("bytes (\\d+)-(\\d+)/(\\d+)").matcher(contentRange);
	if (!matcher.find()) {
	    return false;
	}

	return Long.parseLong(matcher.group(1)) == 0 && Long.parseLong(matcher.group(2)) + 1 >= Long.parseLong(matcher.group(3));
    }

    /**
     * @param hzbnr
     * @param product
     * @param fileNumber
     * @return e.g. "MessstellenExtraData/owf?id=201582&file=2"
     */
    static String getFilePath(String hzbnr, EHYDProduct product, Integer fileNumber) {

	return FILES_PATH + product.getCategory() + "?id=" + hzbnr + "&file=" + fileNumber;
    }

    /**
     * @param hzbnr
     * @param product
     * @return
     */
    static String buildSeriesId(String hzbnr, EHYDProduct product) {

	return hzbnr + SERIES_ID_SEPARATOR + product.getId();
    }

    /**
     * @param seriesId
     * @return the series identified by the given identifier, or null if not valid
     */
    public static EHYDEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	int index = seriesId.indexOf(SERIES_ID_SEPARATOR);
	if (index <= 0) {
	    return null;
	}

	String hzbnr = seriesId.substring(0, index);
	Optional<EHYDProduct> product = EHYDProduct.decode(seriesId.substring(index + 1));
	if (product.isEmpty()) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, buildSeriesId(hzbnr, product.get()));
	object.put(HZBNR, hzbnr);
	object.put(PRODUCT, product.get().getId());

	return new EHYDEntity(object);
    }

    /**
     * @param series
     * @return
     * @throws GSException
     */
    private EHYDProduct getProduct(EHYDEntity series) throws GSException {

	return series.getProduct().orElseThrow(() -> createException("Invalid eHYD series " + series.getSeriesId(), null));
    }

    /**
     * @param path
     * @return
     * @throws GSException
     */
    private String get(String path) throws GSException {

	return getOptional(path).orElseThrow(() -> createException("eHYD " + path + " not available", null));
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
		throw createException("eHYD " + path + " returned HTTP " + response.statusCode(), null);
	    }

	    return text.isBlank() ? Optional.empty() : Optional.of(text);

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("eHYD " + path + " request failed", e);
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

	// the portal address is accepted as well
	if (normalized.matches("https?://(www\\.)?ehyd\\.gv\\.at/(eHYD/)?")) {
	    normalized = normalized.replaceAll("(eHYD/)?$", "") + "services/";
	}

	return normalized;
    }
}
