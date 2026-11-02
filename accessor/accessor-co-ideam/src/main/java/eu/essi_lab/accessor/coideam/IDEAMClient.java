package eu.essi_lab.accessor.coideam;

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
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
 * Client of the raw observations of the automatic stations of the Instituto de Hidrología, Meteorología y Estudios Ambientales
 * (IDEAM) of Colombia, published on datos.gov.co through the Socrata Open Data API (SODA, https://dev.socrata.com/).<br>
 * Each dataset of {@link IDEAMDataset} has one row per observation, with the columns {@link #STATION_CODE}, {@link #SENSOR_CODE},
 * {@link #DATE}, {@link #VALUE} and the description of the station and of the sensor. A time series is identified by the dataset,
 * the station code and the sensor code.<br>
 * Observation times are floating timestamps in Colombian legal time (UTC-5, see {@link #TIME_ZONE}).<br>
 * The datasets are very large (tens to hundreds of millions of rows): aggregation queries over a whole dataset take minutes,
 * while queries filtered by station are fast
 *
 * @author boldrini
 */
public class IDEAMClient {

    public static final String DEFAULT_ENDPOINT = "https://www.datos.gov.co/";

    static final String RESOURCE_PATH = "resource/";

    /**
     * The national catalog of the IDEAM stations
     */
    static final String STATION_CATALOG = "hp9r-jxuu";

    /**
     * Maximum page size of SODA
     */
    static final int MAX_PAGE_SIZE = 50000;

    /**
     * Maximum number of station codes in a single request to the station catalog
     */
    static final int MAX_CATALOG_CODES = 50;

    /**
     * Time zone of the observation times
     */
    public static final ZoneId TIME_ZONE = ZoneId.of("America/Bogota");

    // columns of the observation datasets
    public static final String STATION_CODE = "codigoestacion";
    public static final String SENSOR_CODE = "codigosensor";
    public static final String DATE = "fechaobservacion";
    public static final String VALUE = "valorobservado";
    public static final String STATION_NAME = "nombreestacion";
    public static final String DEPARTMENT = "departamento";
    public static final String MUNICIPALITY = "municipio";
    public static final String HYDROGRAPHIC_ZONE = "zonahidrografica";
    public static final String LAT = "latitud";
    public static final String LON = "longitud";
    public static final String SENSOR_DESCRIPTION = "descripcionsensor";
    public static final String UNIT = "unidadmedida";

    // columns of the station catalog, added to the series with the "catalog_" prefix
    static final String CATALOG_CODE = "codigo";
    static final String CATALOG_PREFIX = "catalog_";
    public static final String CATEGORY = CATALOG_PREFIX + "categoria";
    public static final String TECHNOLOGY = CATALOG_PREFIX + "tecnologia";
    public static final String STATUS = CATALOG_PREFIX + "estado";
    public static final String ALTITUDE = CATALOG_PREFIX + "altitud";
    public static final String CATALOG_LAT = CATALOG_PREFIX + "latitud";
    public static final String CATALOG_LON = CATALOG_PREFIX + "longitud";
    public static final String INSTALLATION_DATE = CATALOG_PREFIX + "fecha_instalacion";
    public static final String SUSPENSION_DATE = CATALOG_PREFIX + "fecha_suspension";
    public static final String STREAM = CATALOG_PREFIX + "corriente";
    public static final String HYDROGRAPHIC_AREA = CATALOG_PREFIX + "area_hidrografica";
    public static final String HYDROGRAPHIC_SUBZONE = CATALOG_PREFIX + "subzona_hidrografica";
    public static final String OPERATOR = CATALOG_PREFIX + "entidad";

    static final String[] CATALOG_COLUMNS = new String[] { "categoria", "tecnologia", "estado", "altitud", "latitud", "longitud",
	    "fecha_instalacion", "fecha_suspension", "corriente", "area_hidrografica", "subzona_hidrografica", "entidad" };

    // keys added by the client
    public static final String SERIES_ID = "seriesId";
    public static final String DATASET = "dataset";
    public static final String BEGIN = "begin";
    public static final String END = "end";
    public static final String COUNT = "count";

    private static final String SERIES_ID_SEPARATOR = "|";

    private static final DateTimeFormatter FLOATING_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private final String endpoint;
    private final String appToken;
    private final Downloader downloader;

    /**
     *
     */
    public IDEAMClient() {

	this(DEFAULT_ENDPOINT, null);
    }

    /**
     * @param endpoint
     */
    public IDEAMClient(String endpoint) {

	this(endpoint, null);
    }

    /**
     * @param endpoint
     * @param appToken an optional Socrata application token, which raises the throttling limits
     */
    public IDEAMClient(String endpoint, String appToken) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.appToken = appToken == null || appToken.isBlank() ? null : appToken.trim();
	this.downloader = new Downloader();
	this.downloader.setConnectionTimeout(TimeUnit.MINUTES, 1);
	// aggregation queries over a whole dataset may take several minutes
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 15);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 2, TimeUnit.SECONDS, 30);
    }

    /**
     * Retrieves the time series of the given dataset with their begin, end and number of observations, by means of a single
     * aggregation query (paged). The query scans the whole dataset, so it takes minutes, unless it is restricted to some stations
     *
     * @param dataset
     * @param stations the codes of the stations, empty for all the stations
     * @return the series sorted by station and sensor
     * @throws GSException
     */
    public List<IDEAMEntity> retrieveSeries(IDEAMDataset dataset, List<String> stations) throws GSException {

	List<IDEAMEntity> ret = new ArrayList<>();

	for (int offset = 0;; offset += MAX_PAGE_SIZE) {

	    LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
	    parameters.put("$select", STATION_CODE + "," + SENSOR_CODE + ",min(" + DATE + ") as " + BEGIN + ",max(" + DATE + ") as "
		    + END + ",count(*) as " + COUNT);
	    if (!stations.isEmpty()) {
		parameters.put("$where",
			STATION_CODE + " in (" + stations.stream().map(IDEAMClient::quote).collect(Collectors.joining(",")) + ")");
	    }
	    parameters.put("$group", STATION_CODE + "," + SENSOR_CODE);
	    parameters.put("$order", STATION_CODE + "," + SENSOR_CODE);
	    parameters.put("$limit", String.valueOf(MAX_PAGE_SIZE));
	    parameters.put("$offset", String.valueOf(offset));

	    JSONArray page = query(dataset.getId(), parameters);

	    for (int i = 0; i < page.length(); i++) {
		createSeries(dataset, page.getJSONObject(i)).ifPresent(ret::add);
	    }

	    if (page.length() < MAX_PAGE_SIZE) {
		break;
	    }
	}

	ret.sort(Comparator.comparing(IDEAMEntity::getStationCode).thenComparing(IDEAMEntity::getSensorCode));

	return ret;
    }

    /**
     * @param dataset
     * @param row a row of the aggregation query, with station code, sensor code, begin, end and count
     * @return the series, or empty if the row is not valid
     */
    static Optional<IDEAMEntity> createSeries(IDEAMDataset dataset, JSONObject row) {

	String station = row.optString(STATION_CODE, "").trim();
	String sensor = row.optString(SENSOR_CODE, "").trim();

	Optional<Date> begin = parseDate(row.optString(BEGIN, null));
	Optional<Date> end = parseDate(row.optString(END, null));

	if (station.isEmpty() || sensor.isEmpty() || begin.isEmpty() || end.isEmpty()) {
	    return Optional.empty();
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, buildSeriesId(dataset, station, sensor));
	object.put(DATASET, dataset.name());
	object.put(STATION_CODE, station);
	object.put(SENSOR_CODE, sensor);
	object.put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(begin.get()));
	object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(end.get()));
	if (row.has(COUNT)) {
	    object.put(COUNT, row.optLong(COUNT));
	}

	return Optional.of(new IDEAMEntity(object));
    }

    /**
     * Retrieves the most recent row of the series
     *
     * @param series
     * @return the row, or empty if the series has no observations
     * @throws GSException
     */
    public Optional<JSONObject> retrieveLatestRow(IDEAMEntity series) throws GSException {

	IDEAMDataset dataset = getDataset(series);

	LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
	parameters.put("$where", getSeriesFilter(series));
	parameters.put("$order", DATE + " DESC");
	parameters.put("$limit", "1");

	JSONArray rows = query(dataset.getId(), parameters);

	return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getJSONObject(0));
    }

    /**
     * Completes the series with the description of the station and of the sensor given by its most recent row, and updates the end
     * of the series
     *
     * @param series
     * @return false if the series has no observations
     * @throws GSException
     */
    public boolean completeSeries(IDEAMEntity series) throws GSException {

	Optional<JSONObject> latest = retrieveLatestRow(series);
	if (latest.isEmpty()) {
	    return false;
	}

	addLatestRow(series, latest.get());

	return true;
    }

    /**
     * @param series
     * @param row the most recent row of the series
     */
    static void addLatestRow(IDEAMEntity series, JSONObject row) {

	JSONObject object = series.getObject();

	for (String key : new String[] { STATION_NAME, DEPARTMENT, MUNICIPALITY, HYDROGRAPHIC_ZONE, LAT, LON, SENSOR_DESCRIPTION, UNIT }) {
	    String value = row.optString(key, "").trim();
	    if (!value.isEmpty()) {
		object.put(key, value);
	    }
	}

	parseDate(row.optString(DATE, null)).ifPresent(d -> object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(d)));
    }

    /**
     * Completes the series with the entries of their stations in the national station catalog
     *
     * @param series
     * @throws GSException
     */
    public void addCatalogEntries(Collection<IDEAMEntity> series) throws GSException {

	List<String> codes = series.stream().map(IDEAMEntity::getStationCode).distinct().collect(Collectors.toList());

	HashMap<String, JSONObject> entries = new HashMap<>();

	for (int i = 0; i < codes.size(); i += MAX_CATALOG_CODES) {

	    List<String> subList = codes.subList(i, Math.min(i + MAX_CATALOG_CODES, codes.size()));

	    LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
	    parameters.put("$where", CATALOG_CODE + " in (" + subList.stream().map(IDEAMClient::quote).collect(Collectors.joining(","))
		    + ")");
	    parameters.put("$limit", String.valueOf(MAX_PAGE_SIZE));

	    JSONArray rows = query(STATION_CATALOG, parameters);
	    for (int j = 0; j < rows.length(); j++) {
		JSONObject row = rows.getJSONObject(j);
		// the catalog has duplicated rows
		entries.putIfAbsent(row.optString(CATALOG_CODE), row);
	    }
	}

	for (IDEAMEntity entity : series) {
	    addCatalogEntry(entity, entries.get(entity.getStationCode()));
	}
    }

    /**
     * @param series
     * @param entry the entry of the station in the catalog, possibly null
     */
    static void addCatalogEntry(IDEAMEntity series, JSONObject entry) {

	if (entry == null) {
	    return;
	}

	for (String column : CATALOG_COLUMNS) {
	    String value = entry.optString(column, "").trim();
	    if (!value.isEmpty()) {
		series.getObject().put(CATALOG_PREFIX + column, value);
	    }
	}
    }

    /**
     * Retrieves the observations of the given period, in pages of {@link #MAX_PAGE_SIZE} rows (each page starts after the last
     * observation of the previous one). Rows without a value are discarded
     *
     * @param series
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(IDEAMEntity series, Date begin, Date end) throws GSException {

	IDEAMDataset dataset = getDataset(series);

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	String from = formatDate(begin);
	String to = formatDate(end);
	String operator = ">=";

	while (true) {

	    LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
	    parameters.put("$select", DATE + "," + VALUE);
	    parameters.put("$where", getSeriesFilter(series) + " AND " + DATE + " " + operator + " " + quote(from) + " AND " + DATE + " <= "
		    + quote(to));
	    parameters.put("$order", DATE);
	    parameters.put("$limit", String.valueOf(MAX_PAGE_SIZE));

	    JSONArray rows = query(dataset.getId(), parameters);

	    String last = parseObservations(rows, ret);

	    if (rows.length() < MAX_PAGE_SIZE || last == null) {
		break;
	    }

	    from = last;
	    operator = ">";
	}

	return ret;
    }

    /**
     * @param rows
     * @param values the map where the valid values are added
     * @return the floating timestamp of the last row, or null if there are no rows
     */
    static String parseObservations(JSONArray rows, TreeMap<Date, BigDecimal> values) {

	String last = null;

	for (int i = 0; i < rows.length(); i++) {

	    JSONObject row = rows.getJSONObject(i);

	    String timestamp = row.optString(DATE, null);
	    Optional<Date> date = parseDate(timestamp);
	    if (date.isEmpty()) {
		continue;
	    }
	    last = timestamp;

	    BigDecimal value = parseDecimal(row.optString(VALUE, ""));
	    if (value != null) {
		values.put(date.get(), value);
	    }
	}

	return last;
    }

    /**
     * @param series
     * @return the SoQL filter of the series
     */
    static String getSeriesFilter(IDEAMEntity series) {

	return STATION_CODE + " = " + quote(series.getStationCode()) + " AND " + SENSOR_CODE + " = " + quote(series.getSensorCode());
    }

    /**
     * @param resource the Socrata identifier of the dataset
     * @param parameters the SoQL parameters
     * @return
     */
    String buildURL(String resource, Map<String, String> parameters) {

	StringBuilder builder = new StringBuilder(endpoint + RESOURCE_PATH + resource + ".json");

	String separator = "?";
	for (Entry<String, String> entry : parameters.entrySet()) {
	    builder.append(separator).append(entry.getKey()).append("=")
		    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8).replace("+", "%20"));
	    separator = "&";
	}

	return builder.toString();
    }

    /**
     * @param dataset
     * @param station
     * @param sensor
     * @return e.g. "NIVEL|0021209920|0230"
     */
    static String buildSeriesId(IDEAMDataset dataset, String station, String sensor) {

	return dataset.name() + SERIES_ID_SEPARATOR + station + SERIES_ID_SEPARATOR + sensor;
    }

    /**
     * @param seriesId
     * @return the series identified by the given identifier, or null if not valid
     */
    public static IDEAMEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	String[] split = seriesId.split("\\" + SERIES_ID_SEPARATOR, -1);
	if (split.length != 3 || split[1].isBlank() || split[2].isBlank()) {
	    return null;
	}

	Optional<IDEAMDataset> dataset = IDEAMDataset.decode(split[0]);
	if (dataset.isEmpty()) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, buildSeriesId(dataset.get(), split[1], split[2]));
	object.put(DATASET, dataset.get().name());
	object.put(STATION_CODE, split[1]);
	object.put(SENSOR_CODE, split[2]);

	return new IDEAMEntity(object);
    }

    /**
     * @param series
     * @return
     * @throws GSException
     */
    private IDEAMDataset getDataset(IDEAMEntity series) throws GSException {

	return series.getDataset().orElseThrow(() -> createException("Invalid IDEAM series " + series.getSeriesId(), null));
    }

    /**
     * @param text a floating timestamp in Colombian legal time, e.g. "2026-10-26T23:00:00.000"
     * @return
     */
    static Optional<Date> parseDate(String text) {

	if (text == null || text.isEmpty() || text.equals("null")) {
	    return Optional.empty();
	}

	try {
	    return Optional.of(Date.from(LocalDateTime.parse(text).atZone(TIME_ZONE).toInstant()));
	} catch (DateTimeParseException e) {
	    try {
		return Optional.of(Date.from(Instant.parse(text)));
	    } catch (DateTimeParseException ex) {
		return Optional.empty();
	    }
	}
    }

    /**
     * @param date
     * @return the date as floating timestamp in Colombian legal time, e.g. "2026-10-26T23:00:00.000"
     */
    static String formatDate(Date date) {

	return FLOATING_DATE_FORMAT.format(date.toInstant().atZone(TIME_ZONE).toLocalDateTime());
    }

    /**
     * @param value
     * @return the value as SoQL string literal
     */
    static String quote(String value) {

	return "'" + value.replace("'", "''") + "'";
    }

    /**
     * @param text
     * @return
     */
    private static BigDecimal parseDecimal(String text) {

	try {
	    return text == null || text.isBlank() ? null : new BigDecimal(text.trim());
	} catch (NumberFormatException e) {
	    return null;
	}
    }

    /**
     * @param resource
     * @param parameters
     * @return
     * @throws GSException
     */
    private JSONArray query(String resource, Map<String, String> parameters) throws GSException {

	String url = buildURL(resource, parameters);

	try {

	    HttpHeaders headers = appToken == null ? HttpHeaderUtils.buildEmpty() : HttpHeaderUtils.build("X-App-Token", appToken);

	    GSLoggerFactory.getLogger(getClass()).debug("Querying {}", url);

	    HttpResponse<InputStream> response = downloader.downloadResponse(url, headers);

	    String text;
	    try (InputStream stream = response.body()) {
		text = stream == null ? "" : IOUtils.toString(stream, StandardCharsets.UTF_8);
	    }

	    if (response.statusCode() != 200) {
		throw createException("IDEAM " + URI.create(url).getPath() + " returned HTTP " + response.statusCode() + ": "
			+ text.substring(0, Math.min(text.length(), 500)), null);
	    }

	    return new JSONArray(text);

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("IDEAM request " + url + " failed", e);
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

	// a dataset URL may be given, e.g. https://www.datos.gov.co/resource/bdmn-sqnh.json
	int index = normalized.indexOf("/" + RESOURCE_PATH);
	if (index > 0) {
	    normalized = normalized.substring(0, index);
	}

	if (!normalized.endsWith("/")) {
	    normalized += "/";
	}

	return normalized;
    }
}
