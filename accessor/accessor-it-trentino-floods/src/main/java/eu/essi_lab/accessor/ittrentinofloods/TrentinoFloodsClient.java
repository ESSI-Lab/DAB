package eu.essi_lab.accessor.ittrentinofloods;

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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
 * Client of the open data of the hydrometric sensors of the Ufficio Dighe of the Provincia autonoma di Trento
 * (https://www.floods.it/public/Opendata/), published on https://dati.trentino.it as the datasets "Anagrafica Sensori Ufficio Dighe"
 * (sensor registry, Dataset=0), "Rilevamento Sensori Idrometrici - Livello" (Dataset=1) and "Rilevamento Sensori Idrometrici -
 * Portata (Calcolata)" (Dataset=2).<br>
 * Each sensor is published as a CSV file with the hourly values of the last 7 days only (no date parameters are supported, the file
 * is the whole content available for the sensor). Times are local times, flagged as solar time (UTC+1) or summer time (UTC+2).
 *
 * @author boldrini
 */
public class TrentinoFloodsClient {

    public static final String DEFAULT_ENDPOINT = "https://www.floods.it/public/Opendata/";

    static final String DOWNLOAD_PATH = "ODdownload.php";

    static final String REGISTRY_DATASET = "0";
    static final String LEVEL_DATASET = "1";
    static final String DISCHARGE_DATASET = "2";

    /**
     * Width of the rolling window of the sensor files
     */
    public static final long WINDOW = TimeUnit.DAYS.toMillis(7);

    /**
     * Response returned (with HTTP 200) for unknown datasets or sensors
     */
    static final String NOT_EXISTING = "Il file non esiste";

    // columns of the sensor registry
    static final String REGISTRY_SENSOR_ID = "IdSensore";
    static final String REGISTRY_DESCRIPTION = "Descrizione";
    static final String REGISTRY_TYPE = "Tipologia";
    static final String REGISTRY_LAT = "Latitudine WGS";
    // sic
    static final String REGISTRY_LON = "Logitudine WGS";
    static final String REGISTRY_X = "X WGS";
    static final String REGISTRY_Y = "Y WGS";
    static final String REGISTRY_BASIN = "Bacino";
    static final String REGISTRY_WATERBODY = "Corpo Idrico";
    static final String REGISTRY_URL = "Url";

    // columns of the sensor files
    static final String DATA_DATE = "Data/Ora";
    static final String DATA_VALUE = "Valore";
    static final String DATA_VALIDATED = "Validato";
    static final String DATA_SOLAR_TIME = "OraSolare";

    // keys of the entity
    public static final String SENSOR_ID = "sensorId";
    public static final String DATASET = "dataset";
    public static final String STATION_NAME = "stationName";
    public static final String TYPE = "type";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String UTM_X = "utmX";
    public static final String UTM_Y = "utmY";
    public static final String BASIN = "basin";
    public static final String WATERBODY = "waterbody";
    public static final String URL = "url";
    public static final String SERIES_ID = "seriesId";
    public static final String BEGIN = "begin";
    public static final String END = "end";
    public static final String VALIDATED = "validated";

    private static final String SERIES_ID_SEPARATOR = "|";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public TrentinoFloodsClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public TrentinoFloodsClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 2);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    /**
     * Retrieves the time series (one per sensor) listed by the sensor registry
     *
     * @return the series sorted by sensor identifier
     * @throws GSException
     */
    public List<TrentinoFloodsEntity> retrieveSeries() throws GSException {

	String registry = get(REGISTRY_DATASET, null).orElseThrow(() -> createException("Sensor registry not available", null));

	return parseRegistry(registry);
    }

    /**
     * @param registry the CSV of the sensor registry
     * @return the series of the water level and discharge sensors, sorted by sensor identifier
     */
    static List<TrentinoFloodsEntity> parseRegistry(String registry) {

	List<TrentinoFloodsEntity> ret = new ArrayList<>();

	List<Map<String, String>> rows = parseCSV(registry);

	for (Map<String, String> row : rows) {

	    String sensorId = row.getOrDefault(REGISTRY_SENSOR_ID, "");
	    if (sensorId.isEmpty()) {
		continue;
	    }

	    String type = row.getOrDefault(REGISTRY_TYPE, "");
	    String dataset = getDataset(row.getOrDefault(REGISTRY_URL, ""), type);
	    if (TrentinoFloodsProperty.decode(dataset).isEmpty()) {
		GSLoggerFactory.getLogger(TrentinoFloodsClient.class).warn("Unknown type {} of sensor {}", type, sensorId);
		continue;
	    }

	    JSONObject object = new JSONObject();
	    object.put(SENSOR_ID, sensorId);
	    object.put(DATASET, dataset);
	    object.put(SERIES_ID, buildSeriesId(dataset, sensorId));
	    object.put(TYPE, type);

	    put(object, STATION_NAME, row.get(REGISTRY_DESCRIPTION));
	    put(object, BASIN, row.get(REGISTRY_BASIN));
	    put(object, WATERBODY, row.get(REGISTRY_WATERBODY));
	    put(object, URL, row.get(REGISTRY_URL));
	    put(object, UTM_X, row.get(REGISTRY_X));
	    put(object, UTM_Y, row.get(REGISTRY_Y));

	    try {
		object.put(LAT, new BigDecimal(row.getOrDefault(REGISTRY_LAT, "")));
		object.put(LON, new BigDecimal(row.getOrDefault(REGISTRY_LON, "")));
	    } catch (NumberFormatException e) {
		GSLoggerFactory.getLogger(TrentinoFloodsClient.class).warn("Invalid coordinates of sensor {}", sensorId);
	    }

	    ret.add(new TrentinoFloodsEntity(object));
	}

	ret.sort(Comparator.comparing((TrentinoFloodsEntity e) -> parseInt(e.getSensorId())).thenComparing(TrentinoFloodsEntity::getSensorId));

	return ret;
    }

    /**
     * @param url the download URL listed by the registry
     * @param type the sensor type, e.g. "Idrometro-Livello (m)"
     * @return the dataset of the sensor
     */
    static String getDataset(String url, String type) {

	int index = url.indexOf("Dataset=");
	if (index >= 0) {
	    String dataset = url.substring(index + "Dataset=".length()).split("&")[0].trim();
	    if (!dataset.isEmpty()) {
		return dataset;
	    }
	}

	String lower = type.toLowerCase();
	if (lower.contains("livello")) {
	    return LEVEL_DATASET;
	}
	if (lower.contains("portata")) {
	    return DISCHARGE_DATASET;
	}

	return "";
    }

    /**
     * Downloads the file of the series and adds its temporal extent (first and last valid values) to the entity
     *
     * @param series
     * @return false if the series has no valid values
     * @throws GSException
     */
    public boolean addTemporalExtent(TrentinoFloodsEntity series) throws GSException {

	SensorData data = retrieveData(series);
	if (data.values.isEmpty()) {
	    return false;
	}

	series.getObject().put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(data.values.firstKey()));
	series.getObject().put(END, ISO8601DateTimeUtils.getISO8601DateTime(data.values.lastKey()));
	series.getObject().put(VALIDATED, data.validated);

	return true;
    }

    /**
     * @param series
     * @return the first and last times of the valid values currently published, or empty if none
     * @throws GSException
     */
    public Optional<SimpleEntry<Date, Date>> retrieveTemporalExtent(TrentinoFloodsEntity series) throws GSException {

	TreeMap<Date, BigDecimal> values = retrieveData(series).values;
	if (values.isEmpty()) {
	    return Optional.empty();
	}

	return Optional.of(new SimpleEntry<>(values.firstKey(), values.lastKey()));
    }

    /**
     * @param series
     * @param begin
     * @param end
     * @return the valid values between begin and end (included)
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(TrentinoFloodsEntity series, Date begin, Date end) throws GSException {

	TreeMap<Date, BigDecimal> values = retrieveData(series).values;
	if (values.isEmpty()) {
	    return values;
	}

	return new TreeMap<>(values.subMap(begin, true, end, true));
    }

    /**
     * @param series
     * @return
     * @throws GSException
     */
    private SensorData retrieveData(TrentinoFloodsEntity series) throws GSException {

	Optional<String> csv = get(series.getDataset(), series.getSensorId());
	if (csv.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).warn("No data available for series {}", series.getSeriesId());
	    return new SensorData();
	}

	return parseData(csv.get());
    }

    /**
     * Values of a sensor file
     */
    static class SensorData {

	final TreeMap<Date, BigDecimal> values = new TreeMap<>();

	/**
	 * True if all the valid values are flagged as validated
	 */
	boolean validated = true;
    }

    /**
     * @param csv the content of a sensor file
     * @return the valid values (missing and not numeric values are skipped)
     */
    static SensorData parseData(String csv) {

	SensorData ret = new SensorData();

	for (Map<String, String> row : parseCSV(csv)) {

	    String value = row.getOrDefault(DATA_VALUE, "").replace(",", ".");
	    if (value.isEmpty()) {
		continue;
	    }

	    BigDecimal decimal;
	    try {
		decimal = new BigDecimal(value);
	    } catch (NumberFormatException e) {
		continue;
	    }

	    Optional<Date> date = parseDate(row.getOrDefault(DATA_DATE, ""), row.getOrDefault(DATA_SOLAR_TIME, "SI"));
	    if (date.isEmpty()) {
		continue;
	    }

	    ret.values.put(date.get(), decimal);
	    if (!"SI".equalsIgnoreCase(row.getOrDefault(DATA_VALIDATED, ""))) {
		ret.validated = false;
	    }
	}

	return ret;
    }

    /**
     * @param date e.g. "04/01/2024 13:00"
     * @param solarTime "SI" for solar time (UTC+1), "NO" for summer time (UTC+2)
     * @return
     */
    static Optional<Date> parseDate(String date, String solarTime) {

	try {
	    LocalDateTime local = LocalDateTime.parse(date.trim(), DATE_FORMAT);
	    ZoneOffset offset = "NO".equalsIgnoreCase(solarTime.trim()) ? ZoneOffset.ofHours(2) : ZoneOffset.ofHours(1);
	    return Optional.of(Date.from(local.toInstant(offset)));
	} catch (DateTimeParseException e) {
	    return Optional.empty();
	}
    }

    /**
     * @param csv a comma separated file with a header row
     * @return the rows, as maps from the column names to the (trimmed) values
     */
    static List<Map<String, String>> parseCSV(String csv) {

	List<Map<String, String>> ret = new ArrayList<>();

	String[] lines = csv.replace("﻿", "").split("\\r?\\n");
	if (lines.length == 0) {
	    return ret;
	}

	String[] header = lines[0].split(",", -1);

	for (int i = 1; i < lines.length; i++) {

	    if (lines[i].isBlank()) {
		continue;
	    }

	    String[] fields = lines[i].split(",", -1);

	    Map<String, String> row = new HashMap<>();
	    for (int j = 0; j < header.length && j < fields.length; j++) {
		row.put(header[j].trim(), fields[j].trim());
	    }
	    ret.add(row);
	}

	return ret;
    }

    /**
     * @param dataset
     * @param sensorId
     * @return e.g. "1|5"
     */
    public static String buildSeriesId(String dataset, String sensorId) {

	return dataset + SERIES_ID_SEPARATOR + sensorId;
    }

    /**
     * @param seriesId e.g. "1|5"
     * @return an entity with the dataset and sensor of the series, or null if the identifier is not valid
     */
    public static TrentinoFloodsEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	String[] split = seriesId.split("\\" + SERIES_ID_SEPARATOR);
	if (split.length != 2 || split[1].trim().isEmpty() || TrentinoFloodsProperty.decode(split[0].trim()).isEmpty()) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(DATASET, split[0].trim());
	object.put(SENSOR_ID, split[1].trim());
	object.put(SERIES_ID, buildSeriesId(split[0].trim(), split[1].trim()));

	return new TrentinoFloodsEntity(object);
    }

    /**
     * @param dataset
     * @param sensorId the sensor, or null for the registry
     * @return the response, or empty if the file does not exist
     * @throws GSException
     */
    private Optional<String> get(String dataset, String sensorId) throws GSException {

	String url = endpoint + DOWNLOAD_PATH + "?Dataset=" + dataset + (sensorId == null ? "" : "&Sensore=" + sensorId);

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
		throw createException(url + " returned HTTP " + response.statusCode(), null);
	    }

	    String text = decode(bytes);

	    if (text.isBlank() || text.trim().startsWith(NOT_EXISTING)) {
		return Optional.empty();
	    }

	    return Optional.of(text);

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException(url + " request failed", e);
	}
    }

    /**
     * @param bytes
     * @return the text, decoded as UTF-8 or as Windows-1252 if not valid UTF-8
     */
    static String decode(byte[] bytes) {

	String text = new String(bytes, StandardCharsets.UTF_8);
	if (text.contains("�")) {
	    text = new String(bytes, Charset.forName("windows-1252"));
	}

	return text;
    }

    /**
     * @param object
     * @param key
     * @param value
     */
    private static void put(JSONObject object, String key, String value) {

	if (value != null && !value.isEmpty()) {
	    object.put(key, value);
	}
    }

    /**
     * @param value
     * @return
     */
    private static int parseInt(String value) {

	try {
	    return Integer.parseInt(value);
	} catch (NumberFormatException e) {
	    return Integer.MAX_VALUE;
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

	// the download page itself can be given as endpoint
	int index = normalized.indexOf(DOWNLOAD_PATH);
	if (index >= 0) {
	    normalized = normalized.substring(0, index);
	}

	if (!normalized.endsWith("/")) {
	    normalized += "/";
	}

	return normalized;
    }
}
