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
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Client of the real time hydrometric monitoring API of the Ufficio Dighe of the Provincia autonoma di Trento
 * (https://www.floods.it/public/DatiLive.php).<br>
 * The index (index.json) is a GeoJSON FeatureCollection with one Point per monitored series (water level or discharge of a site);
 * the values of each series are published as &lt;series id&gt;.json, with epoch times (UTC), and hold the last 7 days only (no date
 * parameters are supported, the file is the whole content available for the series).
 *
 * @author boldrini
 */
public class TrentinoFloodsClient {

    public static final String DEFAULT_ENDPOINT = "https://www.floods.it/api/v1/monitoring/";

    static final String INDEX = "index.json";

    /**
     * Path of the discontinued open data download page (data stopped in January 2024)
     */
    static final String OLD_OPENDATA_PATH = "/public/Opendata";

    /**
     * Width of the rolling window of the series files
     */
    public static final long WINDOW = TimeUnit.DAYS.toMillis(7);

    /**
     * Minimum interval between two requests: the server answers 429 after about 90 requests in a few seconds
     */
    static final long MIN_REQUEST_INTERVAL = TimeUnit.SECONDS.toMillis(1);

    private static long lastRequest;

    // members of the index features
    static final String INDEX_METADATA = "metadata";
    static final String INDEX_SERIES_ID = "series id";
    static final String INDEX_SITE = "site";
    static final String INDEX_WATERBODY = "water body";
    static final String INDEX_UOM = "uom";
    static final String INDEX_QUANTITY = "quantity";
    static final String INDEX_QUANTITY_TYPE = "quantity type";
    static final String INDEX_COLLECTION_METHOD = "collection method";
    static final String INDEX_AUTHOR = "author";

    // members of the series files
    static final String DATA_TIMESTAMP = "timestamp";
    static final String DATA_VALUE = "value";
    static final String DATA_TIME_FORMAT = "time_format";

    // keys of the entity
    public static final String SERIES_ID = "seriesId";
    public static final String SITE = "site";
    public static final String WATERBODY = "waterbody";
    public static final String UOM = "uom";
    public static final String QUANTITY = "quantity";
    public static final String QUANTITY_TYPE = "quantityType";
    public static final String COLLECTION_METHOD = "collectionMethod";
    public static final String AUTHOR = "author";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String BEGIN = "begin";
    public static final String END = "end";

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
	// after a burst of requests the server answers 429 for about one minute
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 6, TimeUnit.SECONDS, 20);
    }

    /**
     * Waits until {@link #MIN_REQUEST_INTERVAL} has elapsed since the previous request of any client of this JVM
     *
     * @throws InterruptedException
     */
    private static synchronized void throttle() throws InterruptedException {

	long wait = lastRequest + MIN_REQUEST_INTERVAL - System.currentTimeMillis();
	if (wait > 0) {
	    Thread.sleep(wait);
	}

	lastRequest = System.currentTimeMillis();
    }

    /**
     * Retrieves the series listed by the index
     *
     * @return the series sorted by site and series identifier
     * @throws GSException
     */
    public List<TrentinoFloodsEntity> retrieveSeries() throws GSException {

	String index = get(INDEX).orElseThrow(() -> createException("Series index not available", null));

	try {
	    return parseIndex(index);
	} catch (JSONException e) {
	    throw createException("Invalid series index", e);
	}
    }

    /**
     * @param index the GeoJSON of the series index
     * @return the series, sorted by site and series identifier
     */
    static List<TrentinoFloodsEntity> parseIndex(String index) {

	List<TrentinoFloodsEntity> ret = new ArrayList<>();

	JSONArray features = new JSONObject(index).optJSONArray("features");
	if (features == null) {
	    return ret;
	}

	for (int i = 0; i < features.length(); i++) {

	    JSONObject feature = features.optJSONObject(i);
	    JSONObject metadata = feature == null ? null
		    : Optional.ofNullable(feature.optJSONObject("properties")).map(p -> p.optJSONObject(INDEX_METADATA)).orElse(null);
	    if (metadata == null) {
		continue;
	    }

	    String seriesId = metadata.optString(INDEX_SERIES_ID, "").trim();
	    if (seriesId.isEmpty()) {
		continue;
	    }

	    JSONObject object = new JSONObject();
	    object.put(SERIES_ID, seriesId);

	    put(object, SITE, metadata.optString(INDEX_SITE));
	    put(object, WATERBODY, metadata.optString(INDEX_WATERBODY));
	    put(object, UOM, metadata.optString(INDEX_UOM));
	    put(object, QUANTITY, metadata.optString(INDEX_QUANTITY));
	    put(object, QUANTITY_TYPE, metadata.optString(INDEX_QUANTITY_TYPE));
	    put(object, COLLECTION_METHOD, metadata.optString(INDEX_COLLECTION_METHOD));
	    put(object, AUTHOR, metadata.optString(INDEX_AUTHOR));

	    // GeoJSON coordinates are longitude, latitude
	    JSONArray coordinates = Optional.ofNullable(feature.optJSONObject("geometry")).map(g -> g.optJSONArray("coordinates"))
		    .orElse(null);
	    if (coordinates != null && coordinates.length() >= 2) {
		try {
		    object.put(LON, coordinates.getBigDecimal(0));
		    object.put(LAT, coordinates.getBigDecimal(1));
		} catch (JSONException e) {
		    GSLoggerFactory.getLogger(TrentinoFloodsClient.class).warn("Invalid coordinates of series {}", seriesId);
		}
	    }

	    if (TrentinoFloodsProperty.decode(object.optString(QUANTITY)).isEmpty()) {
		GSLoggerFactory.getLogger(TrentinoFloodsClient.class).warn("Unknown quantity {} of series {}", object.optString(QUANTITY),
			seriesId);
	    }

	    ret.add(new TrentinoFloodsEntity(object));
	}

	ret.sort(Comparator.comparing(TrentinoFloodsEntity::getSite)
		.thenComparing((TrentinoFloodsEntity e) -> parseLong(e.getSeriesId()))
		.thenComparing(TrentinoFloodsEntity::getSeriesId));

	return ret;
    }

    /**
     * Downloads the file of the series and adds its temporal extent (first and last valid values) to the entity
     *
     * @param series
     * @return false if the series has no valid values
     * @throws GSException
     */
    public boolean addTemporalExtent(TrentinoFloodsEntity series) throws GSException {

	TreeMap<Date, BigDecimal> values = retrieveData(series);
	if (values.isEmpty()) {
	    return false;
	}

	series.getObject().put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(values.firstKey()));
	series.getObject().put(END, ISO8601DateTimeUtils.getISO8601DateTime(values.lastKey()));

	return true;
    }

    /**
     * @param series
     * @return the first and last times of the valid values currently published, or empty if none
     * @throws GSException
     */
    public Optional<SimpleEntry<Date, Date>> retrieveTemporalExtent(TrentinoFloodsEntity series) throws GSException {

	TreeMap<Date, BigDecimal> values = retrieveData(series);
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

	TreeMap<Date, BigDecimal> values = retrieveData(series);
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
    private TreeMap<Date, BigDecimal> retrieveData(TrentinoFloodsEntity series) throws GSException {

	Optional<String> json = get(series.getSeriesId() + ".json");
	if (json.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).warn("No data available for series {}", series.getSeriesId());
	    return new TreeMap<>();
	}

	try {
	    return parseData(json.get());
	} catch (JSONException e) {
	    throw createException("Invalid data of series " + series.getSeriesId(), e);
	}
    }

    /**
     * @param json the content of a series file
     * @return the valid values (missing and not numeric values are skipped)
     */
    static TreeMap<Date, BigDecimal> parseData(String json) {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	JSONArray features = new JSONObject(json).optJSONArray("features");
	if (features == null) {
	    return ret;
	}

	for (int i = 0; i < features.length(); i++) {

	    JSONObject properties = Optional.ofNullable(features.optJSONObject(i)).map(f -> f.optJSONObject("properties")).orElse(null);
	    JSONObject data = properties == null ? null : properties.optJSONObject("data");
	    if (data == null) {
		continue;
	    }

	    String timeFormat = Optional.ofNullable(properties.optJSONObject(INDEX_METADATA)).map(m -> m.optString(DATA_TIME_FORMAT))
		    .orElse("epoch");
	    if (!timeFormat.isEmpty() && !"epoch".equalsIgnoreCase(timeFormat)) {
		GSLoggerFactory.getLogger(TrentinoFloodsClient.class).warn("Unsupported time format {}", timeFormat);
		continue;
	    }

	    JSONArray timestamps = data.optJSONArray(DATA_TIMESTAMP);
	    JSONArray values = data.optJSONArray(DATA_VALUE);
	    if (timestamps == null || values == null) {
		continue;
	    }

	    for (int j = 0; j < timestamps.length() && j < values.length(); j++) {

		if (timestamps.isNull(j) || values.isNull(j)) {
		    continue;
		}

		try {
		    Date date = new Date(TimeUnit.SECONDS.toMillis(timestamps.getLong(j)));
		    ret.put(date, values.getBigDecimal(j));
		} catch (JSONException e) {
		    // not numeric value
		}
	    }
	}

	return ret;
    }

    /**
     * @param seriesId e.g. "32207"
     * @return an entity with the series, or null if the identifier is not valid
     */
    public static TrentinoFloodsEntity parseSeriesId(String seriesId) {

	if (seriesId == null || seriesId.isBlank() || seriesId.contains("/")) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, seriesId.trim());

	return new TrentinoFloodsEntity(object);
    }

    /**
     * @param path the file, relative to the endpoint
     * @return the response, or empty if the file does not exist
     * @throws GSException
     */
    private Optional<String> get(String path) throws GSException {

	String url = endpoint + path;

	try {

	    throttle();

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

	    String text = new String(bytes, StandardCharsets.UTF_8);

	    if (text.isBlank()) {
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
     * @param object
     * @param key
     * @param value
     */
    private static void put(JSONObject object, String key, String value) {

	if (value != null && !value.trim().isEmpty()) {
	    object.put(key, value.trim());
	}
    }

    /**
     * @param value
     * @return
     */
    private static long parseLong(String value) {

	try {
	    return Long.parseLong(value);
	} catch (NumberFormatException e) {
	    return Long.MAX_VALUE;
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

	if (endpoint == null || endpoint.isBlank()) {
	    return DEFAULT_ENDPOINT;
	}

	String normalized = endpoint.trim();

	// sources configured with the discontinued open data page are redirected to the monitoring API
	if (normalized.contains(OLD_OPENDATA_PATH)) {
	    return DEFAULT_ENDPOINT;
	}

	// the index itself can be given as endpoint
	int index = normalized.indexOf(INDEX);
	if (index >= 0) {
	    normalized = normalized.substring(0, index);
	}

	if (!normalized.endsWith("/")) {
	    normalized += "/";
	}

	return normalized;
    }
}
