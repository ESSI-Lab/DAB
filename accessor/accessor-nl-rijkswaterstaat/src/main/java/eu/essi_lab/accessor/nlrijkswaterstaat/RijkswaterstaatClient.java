package eu.essi_lab.accessor.nlrijkswaterstaat;

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
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;

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
 * Client of the Rijkswaterstaat WaterWebservices (DDAPI 2.0).<br>
 * A time series is identified by a location code, a process type and the AQUO metadata codes listed in {@link #AQUO_KEYS}.
 * Codes describing how a value was obtained (instrument, method, ...) are not part of the identifier, so that series are not split
 * every time the measuring equipment changes.
 *
 * @author boldrini
 */
public class RijkswaterstaatClient {

    public static final String DEFAULT_ENDPOINT = "https://ddapi20-waterwebservices.rijkswaterstaat.nl/";

    static final String CATALOG_PATH = "METADATASERVICES/OphalenCatalogus";
    static final String LATEST_OBSERVATIONS_PATH = "ONLINEWAARNEMINGENSERVICES/OphalenLaatsteWaarnemingen";
    static final String CHECK_OBSERVATIONS_PATH = "ONLINEWAARNEMINGENSERVICES/CheckWaarnemingenAanwezig";
    static final String OBSERVATIONS_PATH = "ONLINEWAARNEMINGENSERVICES/OphalenWaarnemingen";

    /**
     * AQUO metadata elements identifying the observed property of a series
     */
    public static final String[] AQUO_KEYS = new String[] { //
	    "Compartiment", //
	    "Grootheid", //
	    "Eenheid", //
	    "Hoedanigheid", //
	    "Parameter", //
	    "BioTaxon", //
	    "Orgaan", //
	    "Typering", //
	    "Groepering" };

    public static final String MEASUREMENT_PROCESS_TYPE = "meting";

    public static final String SERIES_ID = "seriesId";
    public static final String LOCATION_CODE = "locationCode";
    public static final String LOCATION_NAME = "locationName";
    public static final String LOCATION_DESCRIPTION = "locationDescription";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String PROPERTY_DESCRIPTION = "propertyDescription";
    public static final String PROCESS_TYPE = "ProcesType";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    /**
     * Lower bound of the search of the first observation of a series (the oldest Dutch water level records are from the 18th
     * century)
     */
    public static final Date EARLIEST_DATE = ISO8601DateTimeUtils.parseISO8601ToDate("1700-01-01T00:00:00Z").get();

    /**
     * Value used by Rijkswaterstaat for missing observations
     */
    static final double MISSING_VALUE = 999999999;

    /**
     * Maximum number of AQUO metadata sent in a single request of latest observations
     */
    static final int LATEST_OBSERVATIONS_BATCH_SIZE = 100;

    /**
     * Initial time window of the observation requests; a window exceeding the maximum number of observations per request is split
     */
    static final long OBSERVATIONS_WINDOW = TimeUnit.DAYS.toMillis(366);
    private static final long MIN_OBSERVATIONS_WINDOW = TimeUnit.HOURS.toMillis(1);

    /**
     * Width of the interval downloaded to find the exact time of the first observation
     */
    static final long EXACT_SEARCH_WINDOW = TimeUnit.DAYS.toMillis(1);

    private static final String SERIES_ID_SEPARATOR = "|";
    private static final String TOO_MANY_OBSERVATIONS = "overschreden";

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public RijkswaterstaatClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public RijkswaterstaatClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 5);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    /**
     * Retrieves from the catalogue all the time series (location and observed property) of the given process types
     *
     * @param processTypes e.g. {@link #MEASUREMENT_PROCESS_TYPE}
     * @return the series sorted by location code
     * @throws GSException
     */
    public List<RijkswaterstaatEntity> retrieveSeries(Collection<String> processTypes) throws GSException {

	JSONObject filter = new JSONObject();
	filter.put("Compartimenten", true);
	filter.put("Grootheden", true);
	filter.put("Eenheden", true);
	filter.put("Hoedanigheden", true);
	filter.put("Parameters", true);
	filter.put("BioTaxon", true);
	filter.put("Organen", true);
	filter.put("Typeringen", true);
	filter.put("Groeperingen", true);
	filter.put("ProcesTypes", true);

	JSONObject request = new JSONObject();
	request.put("CatalogusFilter", filter);

	JSONObject catalog = post(CATALOG_PATH, request).//
		orElseThrow(() -> createException("Empty Rijkswaterstaat catalogue", null));

	if (!catalog.optBoolean("Succesvol", false)) {
	    throw createException("Rijkswaterstaat catalogue error: " + catalog.optString("Foutmelding"), null);
	}

	HashMap<Integer, JSONObject> metadata = new HashMap<>();
	JSONArray metadataList = catalog.getJSONArray("AquoMetadataLijst");
	for (int i = 0; i < metadataList.length(); i++) {
	    JSONObject md = metadataList.getJSONObject(i);
	    metadata.put(md.getInt("AquoMetadata_MessageID"), md);
	}

	HashMap<Integer, JSONObject> locations = new HashMap<>();
	JSONArray locationList = catalog.getJSONArray("LocatieLijst");
	for (int i = 0; i < locationList.length(); i++) {
	    JSONObject location = locationList.getJSONObject(i);
	    locations.put(location.getInt("Locatie_MessageID"), location);
	}

	List<RijkswaterstaatEntity> ret = new ArrayList<>();
	JSONArray links = catalog.getJSONArray("AquoMetadataLocatieLijst");
	for (int i = 0; i < links.length(); i++) {

	    JSONObject link = links.getJSONObject(i);
	    JSONObject md = metadata.get(link.getInt("AquoMetaData_MessageID"));
	    JSONObject location = locations.get(link.getInt("Locatie_MessageID"));

	    if (md == null || location == null || !processTypes.contains(md.optString(PROCESS_TYPE))) {
		continue;
	    }

	    ret.add(createSeries(location, md));
	}

	ret.sort(Comparator.comparing(RijkswaterstaatEntity::getLocationCode).thenComparing(RijkswaterstaatEntity::getSeriesId));

	return ret;
    }

    /**
     * Retrieves the latest observation of each sub series of the given series. A series can be composed of several sub series (e.g.
     * different measuring instruments over time), each having its own latest observation.
     *
     * @param series series of the same location
     * @return for each series id with observations, the earliest and the latest of the latest observation times of its sub series
     * @throws GSException
     */
    public Map<String, SimpleEntry<Date, Date>> retrieveLatestObservations(List<RijkswaterstaatEntity> series) throws GSException {

	HashMap<String, SimpleEntry<Date, Date>> ret = new HashMap<>();

	for (int start = 0; start < series.size(); start += LATEST_OBSERVATIONS_BATCH_SIZE) {

	    List<RijkswaterstaatEntity> batch = series.subList(start, Math.min(series.size(), start + LATEST_OBSERVATIONS_BATCH_SIZE));

	    JSONArray locationList = new JSONArray();
	    batch.stream().map(RijkswaterstaatEntity::getLocationCode).distinct()
		    .forEach(code -> locationList.put(new JSONObject().put("Code", code)));

	    JSONArray metadataList = new JSONArray();
	    for (RijkswaterstaatEntity entity : batch) {
		metadataList.put(new JSONObject().put("AquoMetadata", createAquoMetadataRequest(entity)));
	    }

	    JSONObject request = new JSONObject();
	    request.put("LocatieLijst", locationList);
	    request.put("AquoPlusWaarnemingMetadataLijst", metadataList);

	    Optional<JSONObject> response = post(LATEST_OBSERVATIONS_PATH, request);
	    if (response.isEmpty()) {
		continue;
	    }

	    JSONArray observations = response.get().optJSONArray("WaarnemingenLijst");
	    if (observations == null) {
		GSLoggerFactory.getLogger(getClass()).warn("Unable to retrieve latest observations: {}",
			response.get().optString("Foutmelding"));
		continue;
	    }

	    for (int i = 0; i < observations.length(); i++) {

		JSONObject observation = observations.getJSONObject(i);
		String seriesId = buildSeriesId(observation.getJSONObject("Locatie").getString("Code"),
			observation.getJSONObject("AquoMetadata"));

		JSONArray measurements = observation.optJSONArray("MetingenLijst");
		if (measurements == null) {
		    continue;
		}

		for (int j = 0; j < measurements.length(); j++) {

		    Optional<Date> time = ISO8601DateTimeUtils.parseISO8601ToDate(measurements.getJSONObject(j).optString("Tijdstip"));
		    if (time.isEmpty()) {
			continue;
		    }

		    SimpleEntry<Date, Date> extent = ret.get(seriesId);
		    if (extent == null) {
			ret.put(seriesId, new SimpleEntry<>(time.get(), time.get()));
		    } else if (time.get().before(extent.getKey())) {
			ret.put(seriesId, new SimpleEntry<>(time.get(), extent.getValue()));
		    } else if (time.get().after(extent.getValue())) {
			extent.setValue(time.get());
		    }
		}
	    }
	}

	return ret;
    }

    /**
     * @param series
     * @param begin
     * @param end
     * @return true if the series has at least one observation in the given period
     * @throws GSException
     */
    public boolean hasObservations(RijkswaterstaatEntity series, Date begin, Date end) throws GSException {

	JSONObject request = new JSONObject();
	request.put("LocatieLijst", new JSONArray().put(new JSONObject().put("Code", series.getLocationCode())));
	request.put("AquoMetadataLijst", new JSONArray().put(createAquoMetadataRequest(series)));
	request.put("Periode", createPeriod(begin, end));

	Optional<JSONObject> response = post(CHECK_OBSERVATIONS_PATH, request);

	return response.isPresent() && "true".equals(response.get().optString("WaarnemingenAanwezig"));
    }

    /**
     * Approximates the time of the first observation of a series by binary search in the interval from {@link #EARLIEST_DATE} to a
     * time at which the series is known to have an observation
     *
     * @param series
     * @param knownObservation time of an existing observation of the series
     * @param maxRequests maximum number of availability requests; 0 to return the known observation time
     * @return a time before (or equal to) which no observation exists, and that precedes the first observation by at most
     *         (knownObservation - {@link #EARLIEST_DATE}) / 2^maxRequests
     * @throws GSException
     */
    public Date estimateFirstObservation(RijkswaterstaatEntity series, Date knownObservation, int maxRequests) throws GSException {

	SimpleEntry<Date, Date> interval = narrowFirstObservation(series, knownObservation, maxRequests, 1);
	return interval.getKey();
    }

    /**
     * Finds the exact time of the first observation of a series: the binary search goes on until the interval containing the first
     * observation is shorter than {@link #EXACT_SEARCH_WINDOW}, then the observations of the interval are downloaded (about 18
     * availability requests and one observations request for a series starting after {@link #EARLIEST_DATE})
     *
     * @param series
     * @param knownObservation time of an existing observation of the series
     * @return the time of the first observation, or the lower bound of the last interval if its values are all missing
     * @throws GSException
     */
    public Date findFirstObservation(RijkswaterstaatEntity series, Date knownObservation) throws GSException {

	SimpleEntry<Date, Date> interval = narrowFirstObservation(series, knownObservation, Integer.MAX_VALUE, EXACT_SEARCH_WINDOW);

	TreeMap<Date, BigDecimal> values = retrieveObservations(series, interval.getKey(), interval.getValue());
	if (values.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).warn("No valid values of series {} in [{}/{}]", series.getSeriesId(),
		    interval.getKey(), interval.getValue());
	    return interval.getKey();
	}

	return values.firstKey();
    }

    /**
     * @param series
     * @param knownObservation
     * @param maxRequests
     * @param minWidth
     * @return
     * @throws GSException
     */
    private SimpleEntry<Date, Date> narrowFirstObservation(RijkswaterstaatEntity series, Date knownObservation, int maxRequests,
	    long minWidth) throws GSException {

	GSException[] error = new GSException[1];

	SimpleEntry<Date, Date> ret = narrowFirstObservation(EARLIEST_DATE, knownObservation, maxRequests, minWidth, (begin, end) -> {
	    try {
		return error[0] == null && hasObservations(series, begin, end);
	    } catch (GSException e) {
		error[0] = e;
		return false;
	    }
	});

	if (error[0] != null) {
	    throw error[0];
	}

	return ret;
    }

    /**
     * @param earliest
     * @param knownObservation
     * @param maxRequests
     * @param hasObservations tells if there is at least one observation in the given closed interval
     * @return
     */
    static Date estimateFirstObservation(Date earliest, Date knownObservation, int maxRequests,
	    BiPredicate<Date, Date> hasObservations) {

	return narrowFirstObservation(earliest, knownObservation, maxRequests, 1, hasObservations).getKey();
    }

    /**
     * Binary search of the first observation
     *
     * @param earliest
     * @param knownObservation
     * @param maxRequests maximum number of availability requests
     * @param minWidth the search stops when the interval is not longer than this width, in milliseconds
     * @param hasObservations tells if there is at least one observation in the given closed interval
     * @return an interval [lower, upper] containing the first observation, with no observations before lower
     */
    static SimpleEntry<Date, Date> narrowFirstObservation(Date earliest, Date knownObservation, int maxRequests, long minWidth,
	    BiPredicate<Date, Date> hasObservations) {

	if (!knownObservation.after(earliest) || maxRequests <= 0) {
	    return new SimpleEntry<>(knownObservation, knownObservation);
	}

	// invariant: no observations before lower, at least one observation in [lower, upper]
	long lower = earliest.getTime();
	long upper = knownObservation.getTime();

	for (int i = 0; i < maxRequests && upper - lower > Math.max(1, minWidth); i++) {

	    long middle = lower + (upper - lower) / 2;

	    if (hasObservations.test(new Date(lower), new Date(middle))) {
		upper = middle;
	    } else {
		lower = middle;
	    }
	}

	return new SimpleEntry<>(new Date(lower), new Date(upper));
    }

    /**
     * Retrieves the observations of a series. Missing values are skipped; when sub series have observations at the same time, the first
     * one is kept.
     *
     * @param series
     * @param begin
     * @param end
     * @return the observation values sorted by time
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(RijkswaterstaatEntity series, Date begin, Date end) throws GSException {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	Deque<SimpleEntry<Long, Long>> windows = new ArrayDeque<>();
	for (long start = begin.getTime(); start <= end.getTime(); start += OBSERVATIONS_WINDOW + 1) {
	    windows.add(new SimpleEntry<>(start, Math.min(end.getTime(), start + OBSERVATIONS_WINDOW)));
	}

	while (!windows.isEmpty()) {

	    SimpleEntry<Long, Long> window = windows.poll();

	    JSONObject request = new JSONObject();
	    request.put("Locatie", new JSONObject().put("Code", series.getLocationCode()));
	    request.put("AquoPlusWaarnemingMetadata", new JSONObject().put("AquoMetadata", createAquoMetadataRequest(series)));
	    request.put("Periode", createPeriod(new Date(window.getKey()), new Date(window.getValue())));

	    Optional<JSONObject> response = post(OBSERVATIONS_PATH, request);
	    if (response.isEmpty()) {
		continue;
	    }

	    if (!response.get().optBoolean("Succesvol", false)) {

		String message = response.get().optString("Foutmelding");
		long length = window.getValue() - window.getKey();

		if (message.contains(TOO_MANY_OBSERVATIONS) && length > MIN_OBSERVATIONS_WINDOW) {

		    long middle = window.getKey() + length / 2;
		    windows.addFirst(new SimpleEntry<>(middle + 1, window.getValue()));
		    windows.addFirst(new SimpleEntry<>(window.getKey(), middle));
		    continue;
		}

		throw createException("Rijkswaterstaat observations error: " + message, null);
	    }

	    JSONArray observations = response.get().optJSONArray("WaarnemingenLijst");
	    if (observations == null) {
		continue;
	    }

	    for (int i = 0; i < observations.length(); i++) {

		JSONArray measurements = observations.getJSONObject(i).optJSONArray("MetingenLijst");
		if (measurements == null) {
		    continue;
		}

		for (int j = 0; j < measurements.length(); j++) {

		    JSONObject measurement = measurements.getJSONObject(j);
		    JSONObject value = measurement.optJSONObject("Meetwaarde");
		    Optional<Date> time = ISO8601DateTimeUtils.parseISO8601ToDate(measurement.optString("Tijdstip"));

		    if (value == null || time.isEmpty() || !value.has("Waarde_Numeriek") || value.isNull("Waarde_Numeriek")) {
			continue;
		    }

		    double number = value.getDouble("Waarde_Numeriek");
		    if (number >= MISSING_VALUE || Double.isNaN(number)) {
			continue;
		    }

		    ret.putIfAbsent(time.get(), BigDecimal.valueOf(number));
		}
	    }
	}

	return ret;
    }

    /**
     * @param location a location of the catalogue
     * @param metadata an AQUO metadata of the catalogue
     * @return
     */
    static RijkswaterstaatEntity createSeries(JSONObject location, JSONObject metadata) {

	JSONObject object = new JSONObject();

	String locationCode = location.getString("Code");
	object.put(SERIES_ID, buildSeriesId(locationCode, metadata));
	object.put(LOCATION_CODE, locationCode);
	object.put(LOCATION_NAME, location.optString("Naam", locationCode));
	object.put(LOCATION_DESCRIPTION, location.optString("Omschrijving", ""));
	if (location.has("Lat") && location.has("Lon")) {
	    object.put(LAT, location.getDouble("Lat"));
	    object.put(LON, location.getDouble("Lon"));
	}

	for (String key : AQUO_KEYS) {
	    JSONObject element = metadata.optJSONObject(key);
	    if (element != null) {
		object.put(key, new JSONObject().//
			put("Code", element.optString("Code", "")).//
			put("Omschrijving", element.optString("Omschrijving", "")));
	    }
	}

	object.put(PROCESS_TYPE, metadata.optString(PROCESS_TYPE, ""));
	object.put(PROPERTY_DESCRIPTION, metadata.optString("Parameter_Wat_Omschrijving", ""));

	return new RijkswaterstaatEntity(object);
    }

    /**
     * @param locationCode
     * @param metadata an AQUO metadata object of the catalogue or of an observation
     * @return the series identifier: location code, process type and AQUO codes separated by {@value #SERIES_ID_SEPARATOR}
     */
    static String buildSeriesId(String locationCode, JSONObject metadata) {

	StringBuilder builder = new StringBuilder(locationCode);
	builder.append(SERIES_ID_SEPARATOR).append(metadata.optString(PROCESS_TYPE, ""));

	for (String key : AQUO_KEYS) {
	    JSONObject element = metadata.optJSONObject(key);
	    builder.append(SERIES_ID_SEPARATOR).append(element == null ? "" : element.optString("Code", ""));
	}

	return builder.toString();
    }

    /**
     * @param seriesId
     * @return a series with location code, process type and AQUO codes, or null if the identifier is not valid
     */
    public static RijkswaterstaatEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	String[] parts = seriesId.split("\\" + SERIES_ID_SEPARATOR, -1);
	if (parts.length != AQUO_KEYS.length + 2 || parts[0].isEmpty()) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, seriesId);
	object.put(LOCATION_CODE, parts[0]);
	object.put(PROCESS_TYPE, parts[1]);

	for (int i = 0; i < AQUO_KEYS.length; i++) {
	    object.put(AQUO_KEYS[i], new JSONObject().put("Code", parts[i + 2]));
	}

	return new RijkswaterstaatEntity(object);
    }

    /**
     * @param series
     * @return the AQUO metadata of a request, with the non empty codes and the process type
     */
    static JSONObject createAquoMetadataRequest(RijkswaterstaatEntity series) {

	JSONObject ret = new JSONObject();

	for (String key : AQUO_KEYS) {
	    String code = series.getCode(key);
	    if (!code.isEmpty()) {
		ret.put(key, new JSONObject().put("Code", code));
	    }
	}

	String processType = series.getProcessType();
	if (!processType.isEmpty()) {
	    ret.put(PROCESS_TYPE, processType);
	}

	return ret;
    }

    /**
     * @param begin
     * @param end
     * @return
     */
    static JSONObject createPeriod(Date begin, Date end) {

	JSONObject ret = new JSONObject();
	ret.put("Begindatumtijd", formatDate(begin));
	ret.put("Einddatumtijd", formatDate(end));
	return ret;
    }

    /**
     * @param date
     * @return the date in the format expected by the API, e.g. 2026-10-26T00:00:00.000+00:00
     */
    static String formatDate(Date date) {

	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+00:00'", Locale.US);
	format.setTimeZone(TimeZone.getTimeZone("UTC"));
	return format.format(date);
    }

    /**
     * @param path
     * @param body
     * @return the JSON response (also for errors reported by the API), or empty if there is no content
     * @throws GSException
     */
    private Optional<JSONObject> post(String path, JSONObject body) throws GSException {

	try {

	    HttpRequest request = HttpRequestUtils.build(//
		    MethodWithBody.POST, //
		    endpoint + path, //
		    body.toString(), //
		    HttpHeaderUtils.build("Content-Type", "application/json"));

	    HttpResponse<InputStream> response = downloader.downloadResponse(request);

	    String text;
	    try (InputStream stream = response.body()) {
		text = stream == null ? "" : IOUtils.toString(stream, StandardCharsets.UTF_8);
	    }

	    if (response.statusCode() == 204 || text.isBlank()) {
		return Optional.empty();
	    }

	    if (response.statusCode() != 200 && response.statusCode() != 400) {
		throw createException("Rijkswaterstaat " + path + " returned HTTP " + response.statusCode(), null);
	    }

	    return Optional.of(new JSONObject(text));

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("Rijkswaterstaat " + path + " request failed", e);
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
