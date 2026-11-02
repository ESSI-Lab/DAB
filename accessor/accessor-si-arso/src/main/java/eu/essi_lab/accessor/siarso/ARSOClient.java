package eu.essi_lab.accessor.siarso;

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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Client of the data of the automatic hydrological stations of the Slovenian Environment Agency (Agencija Republike Slovenije za
 * okolje, ARSO).<br>
 * The stations are listed by the XML of the latest data (https://www.arso.gov.si/xml/vode/hidro_podatki_zadnji.xml), with
 * coordinates, gauge zero, flood thresholds and the latest water level, discharge and water temperature. The observations of each
 * station are published by the tables of the ARSO web pages (https://www.arso.gov.si/vode/podatki/amp/), only for a rolling
 * window: the last day (10 minutes values), the last 7 days (30 minutes values) and the last 30 days (hourly values). The 7 and
 * 30 days tables are updated once per day. A time series is identified by a station code and by the name of a table column
 * (see {@link ARSOParameter}).<br>
 * The hydrological archive (https://vode.arso.gov.si/hidarhiv/) is not available (it reports to be temporarily unavailable),
 * hence older observations can't be retrieved.<br>
 * All the times are in CET (UTC+1, without daylight saving time)
 *
 * @author boldrini
 */
public class ARSOClient {

    public static final String DEFAULT_ENDPOINT = "https://www.arso.gov.si/";

    static final String STATIONS_PATH = "xml/vode/hidro_podatki_zadnji.xml";
    static final String TABLES_PATH = "vode/podatki/amp/";

    /**
     * Windows (in days) of the published tables, from the finest to the coarsest resolution
     */
    static final int[] TABLE_WINDOWS = new int[] { 1, 7, 30 };

    /**
     * Width of the rolling window of the available observations
     */
    public static final long ROLLING_WINDOW = TimeUnit.DAYS.toMillis(30);

    /**
     * The time zone of the XML element "datum_cet" and of the tables
     */
    static final ZoneOffset CET = ZoneOffset.ofHours(1);

    // attributes of the station elements
    public static final String STATION_ID = "sifra";
    public static final String LON = "wgs84_dolzina";
    public static final String LAT = "wgs84_sirina";
    public static final String GAUGE_ZERO = "kota_0";

    // child elements of the station elements
    public static final String RIVER = "reka";
    public static final String LOCATION = "merilno_mesto";
    public static final String SHORT_NAME = "ime_kratko";
    public static final String DATE_CET = "datum_cet";

    public static final String WATER_LEVEL_TAG = "vodostaj";
    public static final String DISCHARGE_TAG = "pretok";
    public static final String WATER_TEMPERATURE_TAG = "temp_vode";
    public static final String WAVE_HEIGHT_TAG = "znacilna_visina_valov";

    /**
     * Flood thresholds (first, second and third high water discharge or level)
     */
    public static final List<String> THRESHOLD_TAGS = Arrays.asList(//
	    "prvi_vv_pretok", "drugi_vv_pretok", "tretji_vv_pretok", //
	    "prvi_vv_vodostaj", "drugi_vv_vodostaj", "tretji_vv_vodostaj");

    static final List<String> VALUE_TAGS = Arrays.asList(WATER_LEVEL_TAG, DISCHARGE_TAG, WATER_TEMPERATURE_TAG, WAVE_HEIGHT_TAG);

    /**
     * Time of the latest values reported by the XML, per element (only used at harvesting time)
     */
    static final String LATEST = "latest";

    // keys added by the client and by the connector
    public static final String PARAMETER = "parameter";
    public static final String UNIT = "unit";
    public static final String SERIES_ID = "seriesId";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    private static final String SERIES_ID_SEPARATOR = "|";

    private static final DateTimeFormatter XML_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TABLE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final Pattern TABLE_PATTERN = Pattern.compile("<table class=\"podatki\">(.*?)</table>", Pattern.DOTALL);
    private static final Pattern HEADER_PATTERN = Pattern.compile("<th>(.*?)</th>", Pattern.DOTALL);
    private static final Pattern ROW_PATTERN = Pattern.compile("<tr>(.*?)</tr>", Pattern.DOTALL);
    private static final Pattern CELL_PATTERN = Pattern.compile("<td>(.*?)</td>", Pattern.DOTALL);
    private static final Pattern COLUMN_PATTERN = Pattern.compile("^(.*?)\\s*\\[(.*)\\]$");
    private static final Pattern ENTITY_PATTERN = Pattern.compile("&#(x?)([0-9a-fA-F]+);");

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public ARSOClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public ARSOClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 2);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    /**
     * @return the stations listed by the XML of the latest data, sorted as in the XML
     * @throws GSException
     */
    public List<JSONObject> retrieveStations() throws GSException {

	String path = STATIONS_PATH;
	String xml = getOptional(path).orElseThrow(() -> createException("ARSO " + path + " not available", null));

	try {

	    return parseStations(xml);

	} catch (Exception e) {

	    throw createException("Unable to parse ARSO " + path, e);
	}
    }

    /**
     * @param xml the XML of the latest data
     * @return the stations, with the static elements (names, flood thresholds) and the time of the latest values
     * @throws Exception
     */
    static List<JSONObject> parseStations(String xml) throws Exception {

	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
	factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

	Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

	List<JSONObject> ret = new ArrayList<>();

	NodeList stations = document.getElementsByTagName("postaja");
	for (int i = 0; i < stations.getLength(); i++) {

	    Element element = (Element) stations.item(i);

	    String id = element.getAttribute(STATION_ID).trim();
	    if (id.isEmpty()) {
		continue;
	    }

	    JSONObject station = new JSONObject();
	    station.put(STATION_ID, id);

	    for (String attribute : new String[] { LON, LAT, GAUGE_ZERO }) {
		String value = element.getAttribute(attribute).trim();
		if (!value.isEmpty()) {
		    station.put(attribute, value);
		}
	    }

	    JSONObject latest = new JSONObject();
	    Optional<Date> date = Optional.empty();

	    NodeList children = element.getChildNodes();
	    for (int j = 0; j < children.getLength(); j++) {

		Node child = children.item(j);
		if (child.getNodeType() != Node.ELEMENT_NODE) {
		    continue;
		}

		String name = child.getNodeName();
		String value = child.getTextContent().trim();
		if (value.isEmpty()) {
		    continue;
		}

		if (name.equals(RIVER) || name.equals(LOCATION) || name.equals(SHORT_NAME) || THRESHOLD_TAGS.contains(name)) {
		    station.put(name, value);
		} else if (name.equals(DATE_CET)) {
		    date = parseXMLDate(value);
		} else if (VALUE_TAGS.contains(name) && parseDecimal(value) != null) {
		    latest.put(name, "");
		}
	    }

	    if (date.isPresent()) {
		for (String tag : latest.keySet()) {
		    latest.put(tag, ISO8601DateTimeUtils.getISO8601DateTime(date.get()));
		}
		station.put(LATEST, latest);
	    }

	    ret.add(station);
	}

	return ret;
    }

    /**
     * Retrieves the time series of a station, one for each column of its 30 days table having at least one valid value. The
     * begin and the end of each series are the first and the last valid values of the table, the end being updated with the
     * time of the latest value reported by the XML of the latest data
     *
     * @param station a station returned by {@link #retrieveStations()}
     * @param parameters the parameters to retrieve, empty for all (including the columns not listed by {@link ARSOParameter})
     * @return the series, empty if the station table is not available
     * @throws GSException
     */
    public List<ARSOEntity> retrieveSeries(JSONObject station, Collection<ARSOParameter> parameters) throws GSException {

	Optional<Table> table = readTable(station.getString(STATION_ID), TABLE_WINDOWS[TABLE_WINDOWS.length - 1]);
	if (table.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).warn("Table of ARSO station {} not available", station.optString(STATION_ID));
	    return new ArrayList<>();
	}

	return createSeries(station, table.get(), parameters);
    }

    /**
     * @param station
     * @param table
     * @param parameters the parameters to retrieve, empty for all
     * @return
     */
    static List<ARSOEntity> createSeries(JSONObject station, Table table, Collection<ARSOParameter> parameters) {

	List<ARSOEntity> ret = new ArrayList<>();

	JSONObject latest = station.optJSONObject(LATEST);

	for (int i = 0; i < table.columns.size(); i++) {

	    String column = table.columns.get(i);
	    Optional<ARSOParameter> parameter = ARSOParameter.fromColumnName(column);

	    if (!parameters.isEmpty() && (parameter.isEmpty() || !parameters.contains(parameter.get()))) {
		continue;
	    }

	    TreeMap<Date, BigDecimal> values = table.getValues(i);
	    if (values.isEmpty()) {
		continue;
	    }

	    Date end = values.lastKey();

	    if (latest != null && parameter.isPresent() && parameter.get().getLatestTag().isPresent()) {
		Optional<Date> latestDate = ISO8601DateTimeUtils.parseISO8601ToDate(latest.optString(parameter.get().getLatestTag().get()));
		if (latestDate.isPresent() && latestDate.get().after(end)) {
		    end = latestDate.get();
		}
	    }

	    JSONObject object = new JSONObject(station.toString());
	    object.remove(LATEST);

	    object.put(PARAMETER, column);
	    object.put(UNIT, table.units.get(i));
	    object.put(SERIES_ID, buildSeriesId(station.getString(STATION_ID), column));
	    object.put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(values.firstKey()));
	    object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(end));

	    ret.add(new ARSOEntity(object));
	}

	return ret;
    }

    /**
     * Retrieves the valid observations of the given period, merging the tables of the last day, of the last 7 days and of the last
     * 30 days (the finest resolution is used where available). Observations older than the rolling window are not available
     *
     * @param series
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(ARSOEntity series, Date begin, Date end) throws GSException {

	long now = System.currentTimeMillis();

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	for (int window : TABLE_WINDOWS) {

	    // the table doesn't reach the requested period
	    if (end.getTime() < now - TimeUnit.DAYS.toMillis(window)) {
		continue;
	    }

	    Optional<Table> table = readTable(series.getStationId(), window);
	    if (table.isEmpty()) {
		continue;
	    }

	    int index = table.get().columns.indexOf(series.getParameterName());
	    if (index >= 0) {
		for (Entry<Date, BigDecimal> entry : table.get().getValues(index).entrySet()) {
		    ret.putIfAbsent(entry.getKey(), entry.getValue());
		}
	    }

	    if (table.get().covers(begin)) {
		break;
	    }
	}

	return new TreeMap<>(ret.subMap(begin, true, end, true));
    }

    /**
     * @param series
     * @return the time of the latest valid observation, read from the table of the last day (or from the table of the last 7 days)
     * @throws GSException
     */
    public Optional<Date> retrieveLatestObservation(ARSOEntity series) throws GSException {

	for (int window : new int[] { TABLE_WINDOWS[0], TABLE_WINDOWS[1] }) {

	    Optional<Table> table = readTable(series.getStationId(), window);
	    if (table.isPresent()) {
		int index = table.get().columns.indexOf(series.getParameterName());
		if (index >= 0) {
		    TreeMap<Date, BigDecimal> values = table.get().getValues(index);
		    if (!values.isEmpty()) {
			return Optional.of(values.lastKey());
		    }
		}
	    }
	}

	return Optional.empty();
    }

    /**
     * A table of observations of a station
     */
    static class Table {

	/**
	 * The column names (without the date column and without the units), e.g. "Vodostaj"
	 */
	final List<String> columns = new ArrayList<>();

	/**
	 * The units of the columns, e.g. "cm"
	 */
	final List<String> units = new ArrayList<>();

	/**
	 * The rows, sorted by time
	 */
	final TreeMap<Date, String[]> rows = new TreeMap<>();

	/**
	 * @param column
	 * @return the valid values of the given column
	 */
	TreeMap<Date, BigDecimal> getValues(int column) {

	    TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	    for (Entry<Date, String[]> row : rows.entrySet()) {
		if (column < row.getValue().length) {
		    BigDecimal value = parseDecimal(row.getValue()[column]);
		    if (value != null) {
			ret.put(row.getKey(), value);
		    }
		}
	    }

	    return ret;
	}

	/**
	 * @param date
	 * @return true if the rows start before the given date
	 */
	boolean covers(Date date) {

	    return !rows.isEmpty() && !rows.firstKey().after(date);
	}
    }

    /**
     * @param stationId
     * @param window the window of the table in days (1, 7 or 30)
     * @return the table, or empty if not available
     * @throws GSException
     */
    Optional<Table> readTable(String stationId, int window) throws GSException {

	String path = getTablePath(stationId, window);

	Optional<String> html = getOptional(path);
	if (html.isEmpty()) {
	    return Optional.empty();
	}

	Optional<Table> table = parseTable(html.get());
	if (table.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).warn("Unable to parse ARSO table {}", path);
	}

	return table;
    }

    /**
     * @param html a page of the ARSO web site with a table of class "podatki", whose first column is the date
     * @return
     */
    static Optional<Table> parseTable(String html) {

	Matcher tableMatcher = TABLE_PATTERN.matcher(html);
	if (!tableMatcher.find()) {
	    return Optional.empty();
	}

	String content = tableMatcher.group(1);

	Table ret = new Table();

	Matcher headerMatcher = HEADER_PATTERN.matcher(content);
	boolean first = true;
	while (headerMatcher.find()) {

	    if (first) {
		// the date column
		first = false;
		continue;
	    }

	    String header = unescape(headerMatcher.group(1));
	    Matcher columnMatcher = COLUMN_PATTERN.matcher(header);
	    if (columnMatcher.matches()) {
		ret.columns.add(columnMatcher.group(1).trim());
		ret.units.add(columnMatcher.group(2).trim());
	    } else {
		ret.columns.add(header);
		ret.units.add("");
	    }
	}

	if (ret.columns.isEmpty()) {
	    return Optional.empty();
	}

	Matcher rowMatcher = ROW_PATTERN.matcher(content);
	while (rowMatcher.find()) {

	    List<String> cells = new ArrayList<>();
	    Matcher cellMatcher = CELL_PATTERN.matcher(rowMatcher.group(1));
	    while (cellMatcher.find()) {
		cells.add(unescape(cellMatcher.group(1)));
	    }

	    if (cells.size() < 2) {
		continue;
	    }

	    Optional<Date> date = parseTableDate(cells.get(0));
	    if (date.isPresent()) {
		ret.rows.put(date.get(), cells.subList(1, cells.size()).toArray(new String[0]));
	    }
	}

	return Optional.of(ret);
    }

    /**
     * @param stationId
     * @param column
     * @return e.g. "1060|Vodostaj"
     */
    static String buildSeriesId(String stationId, String column) {

	return stationId + SERIES_ID_SEPARATOR + column;
    }

    /**
     * @param seriesId
     * @return the series identified by the given identifier, or null if not valid
     */
    public static ARSOEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	int index = seriesId.indexOf(SERIES_ID_SEPARATOR);
	if (index <= 0 || index == seriesId.length() - 1) {
	    return null;
	}

	String stationId = seriesId.substring(0, index);
	String column = seriesId.substring(index + 1);

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, buildSeriesId(stationId, column));
	object.put(STATION_ID, stationId);
	object.put(PARAMETER, column);

	return new ARSOEntity(object);
    }

    /**
     * @param stationId
     * @param window
     * @return e.g. "vode/podatki/amp/H1060_t_30.html"
     */
    static String getTablePath(String stationId, int window) {

	return TABLES_PATH + "H" + stationId + "_t_" + window + ".html";
    }

    /**
     * @param text e.g. "2026-10-27 15:00" (CET)
     * @return
     */
    static Optional<Date> parseXMLDate(String text) {

	return parseDate(text, XML_DATE_FORMAT);
    }

    /**
     * @param text e.g. "27.10.2026 15:30" (CET)
     * @return
     */
    static Optional<Date> parseTableDate(String text) {

	return parseDate(text, TABLE_DATE_FORMAT);
    }

    /**
     * @param text
     * @param formatter
     * @return
     */
    private static Optional<Date> parseDate(String text, DateTimeFormatter formatter) {

	if (text == null || text.isBlank()) {
	    return Optional.empty();
	}

	try {
	    return Optional.of(Date.from(LocalDateTime.parse(text.trim(), formatter).toInstant(CET)));
	} catch (DateTimeParseException e) {
	    return Optional.empty();
	}
    }

    /**
     * @param text
     * @return the value, or null if missing ("-") or not valid
     */
    static BigDecimal parseDecimal(String text) {

	if (text == null || text.isBlank()) {
	    return null;
	}

	try {
	    return new BigDecimal(text.trim());
	} catch (NumberFormatException e) {
	    return null;
	}
    }

    /**
     * @param text
     * @return the text with the numeric character references and the most common entities decoded
     */
    static String unescape(String text) {

	String ret = text.replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"");

	Matcher matcher = ENTITY_PATTERN.matcher(ret);
	StringBuilder builder = new StringBuilder();
	while (matcher.find()) {
	    int codePoint = Integer.parseInt(matcher.group(2), matcher.group(1).isEmpty() ? 10 : 16);
	    matcher.appendReplacement(builder, Matcher.quoteReplacement(new String(Character.toChars(codePoint))));
	}
	matcher.appendTail(builder);

	return builder.toString().replace("&amp;", "&").trim();
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
		throw createException("ARSO " + path + " returned HTTP " + response.statusCode(), null);
	    }

	    return text.isBlank() ? Optional.empty() : Optional.of(text);

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("ARSO " + path + " request failed", e);
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

	// the endpoint of the XML (or of a page) of the ARSO web site is reduced to the web site
	int index = normalized.indexOf("/xml/");
	if (index < 0) {
	    index = normalized.indexOf("/vode/");
	}
	if (index >= 0) {
	    normalized = normalized.substring(0, index);
	}

	if (!normalized.endsWith("/")) {
	    normalized += "/";
	}

	return normalized;
    }
}
