package eu.essi_lab.accessor.mxconagua;

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
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Client of the historical hydrometric database of the Sistema de Información Hidrológica (SIH) of the Comisión Nacional del Agua
 * (CONAGUA) of Mexico (https://sih.conagua.gob.mx/hidros.html).<br>
 * The stations are listed by a catalogue CSV file (ISO-8859-1 encoded), with code, name, coordinates, altitude, state,
 * municipality, hydrological region and basin. The whole period of record of each station is published as a CSV file (UTF-8
 * encoded) named after the station code, with a few header lines followed by the daily values of water level (Nivel) and discharge
 * (Gasto), e.g.:
 *
 * <pre>
 * Comisión Nacional del Agua
 * Subdirección General Técnica
 * Estación:
 * Clave: B01002
 * Estado: Baja California
 * Municipio: Tecate
 *
 * Fecha, Nivel(m), Gasto(m³/s)
 * 1960/01/01,,0.00
 * </pre>
 *
 * The files are updated weekly. The station files are parsed once and cached for a short time, since the same file provides the
 * series of both the properties
 *
 * @author boldrini
 */
public class CONAGUAClient {

    public static final String DEFAULT_ENDPOINT = "https://sih.conagua.gob.mx/basedatos/Hidros/";

    static final String CATALOGUE_FILE = "0_Catalogo de estaciones hidrometricas.csv";

    static final String STATION_FILE_EXTENSION = ".csv";

    /**
     * Header of the date column of the station files
     */
    static final String DATE_COLUMN = "Fecha";

    // keys of the station entries, from the catalogue
    public static final String STATION_CODE = "code";
    public static final String STATION_NAME = "name";
    public static final String LAT = "latitude";
    public static final String LON = "longitude";
    public static final String ALTITUDE = "altitude";
    public static final String STATE = "state";
    public static final String MUNICIPALITY = "municipality";
    public static final String HYDROLOGICAL_REGION = "hydrologicalRegion";
    public static final String BASIN = "basin";

    // keys added by the client and by the connector
    public static final String PROPERTY = "property";
    public static final String UNIT = "unit";
    public static final String SERIES_ID = "seriesId";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    private static final String SERIES_ID_SEPARATOR = "|";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * Maximum number of parsed station files kept in memory, and for how long
     */
    static final int CACHE_SIZE = 4;
    static final long CACHE_TIME = TimeUnit.MINUTES.toMillis(10);

    private static final LinkedHashMap<String, CachedData> CACHE = new LinkedHashMap<>(CACHE_SIZE, 0.75f, true);

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public CONAGUAClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public CONAGUAClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 5);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    /**
     * @return the stations of the catalogue having coordinates, sorted by code
     * @throws GSException
     */
    public List<JSONObject> retrieveStations() throws GSException {

	String text = get(CATALOGUE_FILE).orElseThrow(() -> createException("CONAGUA catalogue " + CATALOGUE_FILE + " not found", null));

	List<JSONObject> ret = parseCatalogue(text);

	ret.sort(Comparator.comparing(s -> s.getString(STATION_CODE)));

	return ret;
    }

    /**
     * @param text
     * @return the stations having code and coordinates
     */
    static List<JSONObject> parseCatalogue(String text) {

	List<JSONObject> ret = new ArrayList<>();

	String[] lines = text.split("\r?\n");
	if (lines.length == 0) {
	    return ret;
	}

	List<String> header = splitCSVLine(lines[0]);
	String[] keys = new String[header.size()];
	for (int i = 0; i < header.size(); i++) {
	    keys[i] = getCatalogueKey(header.get(i));
	}

	for (int i = 1; i < lines.length; i++) {

	    List<String> cells = splitCSVLine(lines[i]);

	    JSONObject station = new JSONObject();
	    for (int j = 0; j < cells.size() && j < keys.length; j++) {
		String value = cells.get(j).trim();
		if (keys[j] != null && !value.isEmpty()) {
		    station.put(keys[j], value);
		}
	    }

	    if (!station.has(STATION_CODE) || parseDecimal(station.optString(LAT, null)) == null
		    || parseDecimal(station.optString(LON, null)) == null) {
		continue;
	    }

	    ret.add(station);
	}

	return ret;
    }

    /**
     * @param column a column of the catalogue, e.g. "Nombre de la estación"
     * @return the key of the station entries, or null if the column is unknown
     */
    private static String getCatalogueKey(String column) {

	String name = column.trim().toLowerCase();

	if (name.startsWith("clave")) {
	    return STATION_CODE;
	}
	if (name.startsWith("nombre")) {
	    return STATION_NAME;
	}
	if (name.startsWith("latitud")) {
	    return LAT;
	}
	if (name.startsWith("longitud")) {
	    return LON;
	}
	if (name.startsWith("altitud")) {
	    return ALTITUDE;
	}
	if (name.startsWith("estado")) {
	    return STATE;
	}
	if (name.startsWith("municipio")) {
	    return MUNICIPALITY;
	}
	if (name.startsWith("r.h")) {
	    return HYDROLOGICAL_REGION;
	}
	if (name.startsWith("cuenca")) {
	    return BASIN;
	}

	return null;
    }

    /**
     * Daily values of a station file
     */
    static class StationData {

	/**
	 * The valid values of each property
	 */
	final Map<CONAGUAProperty, TreeMap<Date, BigDecimal>> values = new EnumMap<>(CONAGUAProperty.class);

	/**
	 * The unit of each property, as written in the column header (e.g. "m³/s")
	 */
	final Map<CONAGUAProperty, String> units = new EnumMap<>(CONAGUAProperty.class);

	/**
	 * @param property
	 * @return the valid values of the property, possibly empty
	 */
	TreeMap<Date, BigDecimal> getValues(CONAGUAProperty property) {

	    return values.getOrDefault(property, new TreeMap<>());
	}
    }

    /**
     * Creates the series of the given properties of a station, reading its file. Series without valid values are discarded
     *
     * @param station a station entry of the catalogue
     * @param properties
     * @return the series, empty if the station file is not available
     * @throws GSException
     */
    public List<CONAGUAEntity> retrieveSeries(JSONObject station, List<CONAGUAProperty> properties) throws GSException {

	List<CONAGUAEntity> ret = new ArrayList<>();

	Optional<StationData> data = retrieveStationData(station.getString(STATION_CODE));
	if (data.isEmpty()) {
	    return ret;
	}

	for (CONAGUAProperty property : properties) {
	    createSeries(station, property, data.get()).ifPresent(ret::add);
	}

	return ret;
    }

    /**
     * @param station
     * @param property
     * @param data
     * @return the series, or empty if the property has no valid values
     */
    static Optional<CONAGUAEntity> createSeries(JSONObject station, CONAGUAProperty property, StationData data) {

	TreeMap<Date, BigDecimal> values = data.getValues(property);
	if (values.isEmpty()) {
	    return Optional.empty();
	}

	JSONObject object = new JSONObject(station.toString());

	object.put(PROPERTY, property.name());
	object.put(SERIES_ID, buildSeriesId(station.getString(STATION_CODE), property));
	object.put(UNIT, data.units.getOrDefault(property, property.getUnitAbbreviation()));
	object.put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(values.firstKey()));
	object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(values.lastKey()));

	return Optional.of(new CONAGUAEntity(object));
    }

    /**
     * @param series
     * @return the time of the first and of the last valid value currently published, or empty if not available
     * @throws GSException
     */
    public Optional<Date[]> retrieveExtent(CONAGUAEntity series) throws GSException {

	Optional<CONAGUAProperty> property = series.getProperty();
	if (property.isEmpty()) {
	    return Optional.empty();
	}

	Optional<StationData> data = retrieveStationData(series.getStationCode());
	if (data.isEmpty() || data.get().getValues(property.get()).isEmpty()) {
	    return Optional.empty();
	}

	TreeMap<Date, BigDecimal> values = data.get().getValues(property.get());
	return Optional.of(new Date[] { values.firstKey(), values.lastKey() });
    }

    /**
     * @param series
     * @param begin
     * @param end
     * @return the valid values of the given period
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(CONAGUAEntity series, Date begin, Date end) throws GSException {

	Optional<CONAGUAProperty> property = series.getProperty();
	if (property.isEmpty()) {
	    throw createException("Unknown CONAGUA property of series " + series.getSeriesId(), null);
	}

	Optional<StationData> data = retrieveStationData(series.getStationCode());
	if (data.isEmpty()) {
	    return new TreeMap<>();
	}

	return subMap(data.get().getValues(property.get()), begin, end);
    }

    /**
     * @param stationCode
     * @return the parsed station file, possibly taken from the cache, or empty if the file is not available
     * @throws GSException
     */
    Optional<StationData> retrieveStationData(String stationCode) throws GSException {

	String path = stationCode + STATION_FILE_EXTENSION;
	String key = endpoint + path;

	synchronized (CACHE) {
	    CachedData cached = CACHE.get(key);
	    if (cached != null && System.currentTimeMillis() - cached.time < CACHE_TIME) {
		return Optional.of(cached.data);
	    }
	}

	Optional<String> text = get(path);
	if (text.isEmpty()) {
	    return Optional.empty();
	}

	StationData data = parseStationFile(text.get());

	synchronized (CACHE) {
	    CACHE.put(key, new CachedData(data));
	    Iterator<String> iterator = CACHE.keySet().iterator();
	    while (CACHE.size() > CACHE_SIZE && iterator.hasNext()) {
		iterator.next();
		iterator.remove();
	    }
	}

	return Optional.of(data);
    }

    /**
     * A parsed station file and the time it was downloaded
     */
    private static class CachedData {

	final StationData data;
	final long time;

	/**
	 * @param data
	 */
	CachedData(StationData data) {

	    this.data = data;
	    this.time = System.currentTimeMillis();
	}
    }

    /**
     * @param text
     * @return the valid values of the known properties. Empty or not numeric values are skipped
     */
    static StationData parseStationFile(String text) {

	StationData ret = new StationData();

	String[] lines = text.split("\r?\n");

	CONAGUAProperty[] columns = null;

	for (String line : lines) {

	    if (line.isBlank()) {
		continue;
	    }

	    List<String> cells = splitCSVLine(line);

	    if (columns == null) {

		if (cells.get(0).trim().equalsIgnoreCase(DATE_COLUMN)) {

		    columns = new CONAGUAProperty[cells.size()];

		    for (int i = 1; i < cells.size(); i++) {

			String header = cells.get(i).trim();
			int open = header.indexOf('(');
			String name = open < 0 ? header : header.substring(0, open).trim();

			Optional<CONAGUAProperty> property = CONAGUAProperty.decode(name);
			if (property.isEmpty()) {
			    GSLoggerFactory.getLogger(CONAGUAClient.class).warn("Unknown CONAGUA column {}", header);
			    continue;
			}

			columns[i] = property.get();
			ret.values.put(property.get(), new TreeMap<>());

			int close = header.indexOf(')', open + 1);
			if (open >= 0 && close > open + 1) {
			    ret.units.put(property.get(), header.substring(open + 1, close).trim());
			}
		    }
		}

		continue;
	    }

	    Optional<Date> date = parseDate(cells.get(0));
	    if (date.isEmpty()) {
		continue;
	    }

	    for (int i = 1; i < cells.size() && i < columns.length; i++) {

		BigDecimal value = parseDecimal(cells.get(i));
		if (columns[i] != null && value != null) {
		    ret.values.get(columns[i]).put(date.get(), value);
		}
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

	if (begin.after(end)) {
	    return new TreeMap<>();
	}

	return new TreeMap<>(values.subMap(begin, true, end, true));
    }

    /**
     * @param stationCode
     * @param property
     * @return e.g. "CNDCA|DISCHARGE"
     */
    static String buildSeriesId(String stationCode, CONAGUAProperty property) {

	return stationCode + SERIES_ID_SEPARATOR + property.name();
    }

    /**
     * @param seriesId
     * @return a minimal series with station code and property, or null if the identifier is not valid
     */
    public static CONAGUAEntity parseSeriesId(String seriesId) {

	int index = seriesId.lastIndexOf(SERIES_ID_SEPARATOR);
	if (index <= 0) {
	    return null;
	}

	String stationCode = seriesId.substring(0, index);
	Optional<CONAGUAProperty> property = CONAGUAProperty.decode(seriesId.substring(index + 1));
	if (property.isEmpty()) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(STATION_CODE, stationCode);
	object.put(PROPERTY, property.get().name());
	object.put(SERIES_ID, buildSeriesId(stationCode, property.get()));

	return new CONAGUAEntity(object);
    }

    /**
     * @param line
     * @return the cells of a comma separated line, with double quoted cells possibly containing commas
     */
    static List<String> splitCSVLine(String line) {

	List<String> ret = new ArrayList<>();

	StringBuilder cell = new StringBuilder();
	boolean quoted = false;

	for (int i = 0; i < line.length(); i++) {

	    char c = line.charAt(i);

	    if (c == '"') {
		if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
		    cell.append('"');
		    i++;
		} else {
		    quoted = !quoted;
		}
	    } else if (c == ',' && !quoted) {
		ret.add(cell.toString());
		cell.setLength(0);
	    } else {
		cell.append(c);
	    }
	}

	ret.add(cell.toString());

	return ret;
    }

    /**
     * @param text e.g. "1960/01/01"
     * @return the start of the day (UTC)
     */
    static Optional<Date> parseDate(String text) {

	if (text == null || text.isBlank()) {
	    return Optional.empty();
	}

	try {
	    LocalDate date = LocalDate.parse(text.trim(), DATE_FORMAT);
	    return Optional.of(Date.from(date.atStartOfDay(ZoneOffset.UTC).toInstant()));
	} catch (DateTimeParseException e) {
	    return Optional.empty();
	}
    }

    /**
     * @param text
     * @return
     */
    private static BigDecimal parseDecimal(String text) {

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
     * @param bytes
     * @return the text, decoded as UTF-8 (without byte order mark) if valid, otherwise as ISO-8859-1
     */
    static String decode(byte[] bytes) {

	try {

	    String text = StandardCharsets.UTF_8.newDecoder().//
		    onMalformedInput(CodingErrorAction.REPORT).//
		    onUnmappableCharacter(CodingErrorAction.REPORT).//
		    decode(ByteBuffer.wrap(bytes)).//
		    toString();

	    return text.startsWith("﻿") ? text.substring(1) : text;

	} catch (CharacterCodingException e) {

	    return new String(bytes, StandardCharsets.ISO_8859_1);
	}
    }

    /**
     * @param path
     * @return the decoded text, or empty if the file is not found
     * @throws GSException
     */
    private Optional<String> get(String path) throws GSException {

	String url = endpoint + path.replace(" ", "%20");

	try {

	    HttpResponse<InputStream> response = downloader.downloadResponse(url);

	    byte[] bytes;
	    try (InputStream stream = response.body()) {
		bytes = stream == null ? new byte[] {} : IOUtils.toByteArray(stream);
	    }

	    if (response.statusCode() == 404) {
		return Optional.empty();
	    }

	    if (response.statusCode() != 200) {
		// the site is protected by a web application firewall, which redirects (302) or refuses (403) the blocked clients
		throw createException("CONAGUA " + path + " returned HTTP " + response.statusCode()
			+ (response.statusCode() == 302 || response.statusCode() == 403 ? " (access blocked by the site firewall?)" : ""),
			null);
	    }

	    String text = decode(bytes);

	    // an HTML page is returned by the firewall to the blocked clients
	    if (text.trim().startsWith("<")) {
		throw createException("CONAGUA " + path + " returned an HTML page instead of a CSV file (access blocked?)", null);
	    }

	    return text.isBlank() ? Optional.empty() : Optional.of(text);

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("CONAGUA " + path + " request failed", e);
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
