package eu.essi_lab.accessor.datastream;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * Simple client for the DataStream OData API.
 * 
 * It covers the use cases required by the accessor:
 * <ul>
 * <li>listing datasets (Metadata)</li>
 * <li>listing locations for a given dataset (Locations)</li>
 * <li>discovering available CharacteristicName values for a given dataset/location</li>
 * <li>retrieving observations for a DOI/location/CharacteristicName and optional year range</li>
 * </ul>
 *
 * All requests are authenticated by sending the provided API key in the {@code x-api-key}
 * HTTP header.
 */
public class DataStreamClient {

    private static final long MIN_REQUEST_INTERVAL_MS = 1000;

    private final String endpoint;
    public static String apiKey;
    private final HttpClient httpClient;
    private final ReentrantLock requestLock = new ReentrantLock();
    private volatile long lastRequestTimeMillis = 0;

    public DataStreamClient(String endpoint) {
	this.endpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
	this.httpClient = HttpClient.newHttpClient();
    }

    public DataStreamClient(String endpoint, String apiKey) {
	this.endpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
	DataStreamClient.apiKey = apiKey;
	this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Represents a DataStream dataset (entry from the Metadata entity set).
     */
    public static class DatasetMetadata {
	public String id;
	public String doi;
	public String name;
	public JSONObject raw;
    }

    /**
     * Represents a monitoring location for a dataset.
     */
    public static class Location {
	public int id;
	public String doi;
	public String name;
	public Double latitude;
	public Double longitude;
	public String monitoringLocationType;
	public JSONObject raw;
    }

    /**
     * Represents a single observation.
     */
    public static class Observation {
	public String doi;
	public String characteristicName;
	public Double value;
	public String unit;
	public String activityStartDate;
	public String activityStartTime;
	public JSONObject raw;
    }

    /**
     * Simple value object representing the first and last observation dates and
     * result unit for a given series (DOI, location, CharacteristicName).
     */
    public static class ObservationDateRange {
	public String firstActivityStartDate;
	public String firstActivityStartTime;
	public String lastActivityStartDate;
	public String lastActivityStartTime;
	/** ResultUnit from Observations (e.g. "deg C"). */
	public String resultUnit;
    }

    public List<DatasetMetadata> listDatasets(int top) throws IOException, InterruptedException {
	String url = endpoint + "/Metadata";
	if (top > 0) {
	    //
	    // In this case we only retrieve a single page from the service and rely
	    // on $top to limit the number of returned entries server-side.
	    //
	    url += "?%24top=" + top;
	    List<DatasetMetadata> results = new ArrayList<>();
	    JSONObject page = executeGet(url);
	    JSONArray value = page.optJSONArray("value");
	    if (value != null) {
		for (int i = 0; i < value.length(); i++) {
		    JSONObject obj = value.getJSONObject(i);
		    DatasetMetadata md = new DatasetMetadata();
		    md.raw = obj;
		    md.id = obj.optString("Id", null);
		    md.doi = obj.optString("DOI", null);
		    md.name = obj.optString("DatasetName", null);
		    results.add(md);
		    if (results.size() >= top) {
			break;
		    }
		}
	    }
	    return results;
	}

	List<DatasetMetadata> results = new ArrayList<>();
	String next = url;
	while (next != null) {
	    JSONObject page = executeGet(next);
	    JSONArray value = page.optJSONArray("value");
	    if (value != null) {
		for (int i = 0; i < value.length(); i++) {
		    JSONObject obj = value.getJSONObject(i);
		    DatasetMetadata md = new DatasetMetadata();
		    md.raw = obj;
		    md.id = obj.optString("Id", null);
		    md.doi = obj.optString("DOI", null);
		    md.name = obj.optString("DatasetName", null);
		    results.add(md);
		}
	    }
	    next = page.optString("@odata.nextLink", null);
	}
	return results;
    }

    /**
     * Lists dataset collection identifiers only (Id, DOI, DatasetName) without full metadata,
     * to build a lightweight cache. Results are not sorted; sort by id in the caller if needed.
     *
     * @param maxCollections max number of collections to return; 0 or negative = no limit (paginate all)
     */
    public List<DatasetMetadata> listDatasetIdentifiers(int maxCollections) throws IOException, InterruptedException {
	String select = "%24select=Id,DOI,DatasetName";
	String url = endpoint + "/Metadata?" + select;
	if (maxCollections > 0) {
	    url += "&%24top=" + maxCollections;
	    List<DatasetMetadata> results = new ArrayList<>();
	    JSONObject page = executeGet(url);
	    JSONArray value = page.optJSONArray("value");
	    if (value != null) {
		for (int i = 0; i < value.length(); i++) {
		    JSONObject obj = value.getJSONObject(i);
		    DatasetMetadata md = new DatasetMetadata();
		    md.raw = null;
		    md.id = obj.optString("Id", null);
		    md.doi = obj.optString("DOI", null);
		    md.name = obj.optString("DatasetName", null);
		    results.add(md);
		}
	    }
	    return results;
	}
	List<DatasetMetadata> results = new ArrayList<>();
	String next = url;
	while (next != null) {
	    JSONObject page = executeGet(next);
	    JSONArray value = page.optJSONArray("value");
	    if (value != null) {
		for (int i = 0; i < value.length(); i++) {
		    JSONObject obj = value.getJSONObject(i);
		    DatasetMetadata md = new DatasetMetadata();
		    md.raw = null;
		    md.id = obj.optString("Id", null);
		    md.doi = obj.optString("DOI", null);
		    md.name = obj.optString("DatasetName", null);
		    results.add(md);
		}
	    }
	    next = page.optString("@odata.nextLink", null);
	}
	return results;
    }

    /**
     * Fetches full metadata for a single dataset collection by its DOI.
     */
    public DatasetMetadata getMetadataByDoi(String doi) throws IOException, InterruptedException {
	String filter = "DOI%20eq%20'" + URLEncoder.encode(doi, StandardCharsets.UTF_8) + "'";
	String url = endpoint + "/Metadata?%24filter=" + filter;
	JSONObject page = executeGet(url);
	JSONArray value = page != null ? page.optJSONArray("value") : null;
	if (value != null && value.length() > 0) {
	    JSONObject obj = value.getJSONObject(0);
	    DatasetMetadata md = new DatasetMetadata();
	    md.raw = obj;
	    md.id = obj.optString("Id", null);
	    md.doi = obj.optString("DOI", null);
	    md.name = obj.optString("DatasetName", null);
	    return md;
	}
	return null;
    }

    public List<Location> listLocationsByDoi(String doi) throws IOException, InterruptedException {
	List<Location> results = new ArrayList<>();

	String filter = "DOI%20eq%20'" + URLEncoder.encode(doi, StandardCharsets.UTF_8) + "'";
	String url = endpoint + "/Locations?%24filter=" + filter;
	String next = url;
	while (next != null) {
	    JSONObject page = executeGet(next);
	    JSONArray value = page.optJSONArray("value");
	    if (value != null) {
		for (int i = 0; i < value.length(); i++) {
		    JSONObject obj = value.getJSONObject(i);
		    Location loc = new Location();
		    loc.raw = obj;
		    loc.id = obj.optInt("Id");
		    loc.doi = obj.optString("DOI", null);
		    loc.name = obj.optString("Name", obj.optString("ID", null));
		    if (obj.has("Latitude") && !obj.isNull("Latitude")) {
			loc.latitude = obj.getDouble("Latitude");
		    }
		    if (obj.has("Longitude") && !obj.isNull("Longitude")) {
			loc.longitude = obj.getDouble("Longitude");
		    }
		    loc.monitoringLocationType = obj.optString("MonitoringLocationType", null);
		    results.add(loc);
		}
	    }
	    next = page.optString("@odata.nextLink", null);
	}

	return results;
    }

    /**
     * Returns the distinct list of CharacteristicName values for the given DOI and
     * location. Stops paginating once {@code maxCharacteristics} distinct names are
     * collected when {@code maxCharacteristics > 0}.
     *
     * @param maxCharacteristics max number of characteristics to consider; -1 or 0 = no limit
     */
    public Set<String> getCharacteristicNames(String doi, int locationId, int maxCharacteristics)
	    throws IOException, InterruptedException {
	Set<String> names = new HashSet<>();

	String filter = "DOI%20eq%20'" + URLEncoder.encode(doi, StandardCharsets.UTF_8) + "'%20and%20LocationId%20eq%20'"
		+ locationId + "'";
	String select = "%24select=CharacteristicName";
	
	String baseUrl = endpoint + "/Observations?%24filter=" + filter +  "&" + select+"&%24top=" + maxCharacteristics;

	//
	// When a positive maxCharacteristics is provided, we can simply rely on
	// $top to have the server return at most that many distinct
	// CharacteristicName values in a single page, avoiding pagination.
	//
	if (maxCharacteristics > 0) {
	    JSONObject page = executeGet(baseUrl);
	    JSONArray value = page.optJSONArray("value");
	    if (value != null) {
		for (int i = 0; i < value.length(); i++) {
		    JSONObject obj = value.getJSONObject(i);
		    String name = obj.optString("CharacteristicName", null);
		    if (name != null && !name.isEmpty()) {
			names.add(name);
		    }
		}
	    }
	    return names;
	}

	//
	// Unlimited case: follow all pages.
	//
	String next = baseUrl;
	while (next != null) {
	    JSONObject page = executeGet(next);
	    JSONArray value = page.optJSONArray("value");
	    if (value != null) {
		for (int i = 0; i < value.length(); i++) {
		    JSONObject obj = value.getJSONObject(i);
		    String name = obj.optString("CharacteristicName", null);
		    if (name != null && !name.isEmpty()) {
			names.add(name);
		    }
		}
	    }
	    next = page.optString("@odata.nextLink", null);
	}

	return names;
    }

    /**
     * Returns all distinct CharacteristicName values for the given DOI and location
     * (no limit; follows all pages).
     */
    public Set<String> getCharacteristicNames(String doi, int locationId) throws IOException, InterruptedException {
	return getCharacteristicNames(doi, locationId, -1);
    }

    /**
     * Returns distinct CharacteristicName values with their ResultUnit for the given DOI and location.
     * Same semantics as {@link #getCharacteristicNames(String, int, int)} but also extracts unit (first occurrence per name).
     *
     * @param maxCharacteristics max number of characteristics to consider; -1 or 0 = no limit
     * @return map from characteristic name to result unit (unit may be null if not present)
     */
    public Map<String, String> getCharacteristicNamesWithUnits(String doi, int locationId, int maxCharacteristics)
	    throws IOException, InterruptedException {
	Map<String, String> nameToUnit = new HashMap<>();

	String filter = "DOI%20eq%20'" + URLEncoder.encode(doi, StandardCharsets.UTF_8) + "'%20and%20LocationId%20eq%20'"
		+ locationId + "'";
	String select = "%24select=CharacteristicName,ResultUnit";
	String baseUrl = endpoint + "/Observations?%24filter=" + filter + "&" + select + "&%24top=" + maxCharacteristics;

	if (maxCharacteristics > 0) {
	    JSONObject page = executeGet(baseUrl);
	    JSONArray value = page.optJSONArray("value");
	    if (value != null) {
		for (int i = 0; i < value.length(); i++) {
		    JSONObject obj = value.getJSONObject(i);
		    String name = obj.optString("CharacteristicName", null);
		    if (name != null && !name.isEmpty() && !nameToUnit.containsKey(name)) {
			nameToUnit.put(name, obj.optString("ResultUnit", null));
		    }
		}
	    }
	    return nameToUnit;
	}

	String next = baseUrl;
	while (next != null) {
	    JSONObject page = executeGet(next);
	    JSONArray value = page.optJSONArray("value");
	    if (value != null) {
		for (int i = 0; i < value.length(); i++) {
		    JSONObject obj = value.getJSONObject(i);
		    String name = obj.optString("CharacteristicName", null);
		    if (name != null && !name.isEmpty() && !nameToUnit.containsKey(name)) {
			nameToUnit.put(name, obj.optString("ResultUnit", null));
		    }
		}
	    }
	    next = page.optString("@odata.nextLink", null);
	}
	return nameToUnit;
    }

    /**
     * Retrieves observations for the given DOI, location and CharacteristicName.
     *
     * The date range can be constrained using ActivityStartYear (inclusive
     * bounds).
     */
    public List<Observation> getObservations(String doi, int locationId, String characteristicName, Integer fromYear,
	    Integer toYear, int top) throws IOException, InterruptedException {

	StringBuilder filter = new StringBuilder();
	filter.append("DOI%20eq%20'").append(URLEncoder.encode(doi, StandardCharsets.UTF_8)).append("'");
	filter.append("%20and%20LocationId%20eq%20'").append(locationId).append("'");
	filter.append("%20and%20CharacteristicName%20eq%20'")
		.append(URLEncoder.encode(characteristicName, StandardCharsets.UTF_8)).append("'");
	if (fromYear != null) {
	    filter.append("%20and%20ActivityStartYear%20ge%20").append((fromYear));
	}
	if (toYear != null) {
	    filter.append("%20and%20ActivityStartYear%20le%20").append((toYear));
	}

	StringBuilder urlBuilder = new StringBuilder(endpoint).append("/Observations?%24filter=").append(filter.toString())
		.append("&%24select=DOI,CharacteristicName,ResultValue,ResultUnit,ActivityStartDate,ActivityStartTime");
	if (top > 0) {
	    urlBuilder.append("&%24top=").append(top);
	}

	List<Observation> results = new ArrayList<>();
	String next = urlBuilder.toString();
	while (next != null) {
	    JSONObject page = executeGet(next);
	    JSONArray value = page.optJSONArray("value");
	    if (value != null) {
		for (int i = 0; i < value.length(); i++) {
		    JSONObject obj = value.getJSONObject(i);
		    Observation obs = new Observation();
		    obs.raw = obj;
		    obs.doi = obj.optString("DOI", null);
		    obs.characteristicName = obj.optString("CharacteristicName", null);
		    if (obj.has("ResultValue") && !obj.isNull("ResultValue")) {
			obs.value = obj.getDouble("ResultValue");
		    }
		    obs.unit = obj.optString("ResultUnit", null);
		    obs.activityStartDate = obj.optString("ActivityStartDate", null);
		    obs.activityStartTime = obj.optString("ActivityStartTime", null);
		    results.add(obs);
		}
	    }
	    next = page.optString("@odata.nextLink", null);
	}

	return results;
    }

    /**
     * Retrieves the first and last observation dates (and times, when available)
     * for the given DOI, location and CharacteristicName.
     *
     * <p>
     * DataStream OData does not support {@code $orderby}, so this method issues
     * a single request with {@code $top} (see {@link #OBSERVATION_DATE_RANGE_TOP})
     * and computes the min/max of ActivityStartDate and ActivityStartTime on the
     * client.
     * </p>
     *
     * @return an {@link ObservationDateRange} with non-null fields when at least
     *         one observation exists for the series, or {@code null} when no
     *         observations are found.
     */
    public ObservationDateRange getObservationDateRange(String doi, int locationId, String characteristicName, int maxChars)
	    throws IOException, InterruptedException {

	// DataStream OData does not support $orderby, so we fetch one page with $top and
	// compute the min/max of ActivityStartDate and ActivityStartTime (same effective
	// limit as OBSERVATION_PAGE_SIZE * OBSERVATION_DATE_RANGE_MAX_PAGES).
	StringBuilder filter = new StringBuilder();
	filter.append("DOI%20eq%20'").append(URLEncoder.encode(doi, StandardCharsets.UTF_8)).append("'");
	filter.append("%20and%20LocationId%20eq%20'").append(locationId).append("'");
	filter.append("%20and%20CharacteristicName%20eq%20'")
		.append(URLEncoder.encode(characteristicName, StandardCharsets.UTF_8)).append("'");

	String select = "%24select=ActivityStartDate,ActivityStartTime,ResultUnit";
	String url = endpoint + "/Observations?%24filter=" + filter.toString() + "&" + select + "&%24top="
		+ maxChars;

	String minDate = null, minTime = null, maxDate = null, maxTime = null;
	String resultUnit = null;

	JSONObject page = executeGet(url);
	JSONArray value = page.optJSONArray("value");
	if (value != null) {
	    for (int i = 0; i < value.length(); i++) {
		JSONObject obj = value.getJSONObject(i);
		String d = obj.optString("ActivityStartDate", null);
		String t = obj.optString("ActivityStartTime", null);
		if (d == null || d.isEmpty()) {
		    continue;
		}
		if (resultUnit == null) {
		    resultUnit = obj.optString("ResultUnit", null);
		}
		if (minDate == null || compareDateTime(d, t, minDate, minTime) < 0) {
		    minDate = d;
		    minTime = t;
		}
		if (maxDate == null || compareDateTime(d, t, maxDate, maxTime) > 0) {
		    maxDate = d;
		    maxTime = t;
		}
	    }
	}

	if (minDate == null) {
	    return null;
	}
	ObservationDateRange range = new ObservationDateRange();
	range.firstActivityStartDate = minDate;
	range.firstActivityStartTime = minTime;
	range.lastActivityStartDate = maxDate;
	range.lastActivityStartTime = maxTime;
	range.resultUnit = resultUnit;
	return range;
    }

    /**
     * Compares (date1, time1) and (date2, time2) chronologically.
     *
     * @return negative if (date1, time1) is before (date2, time2), zero if equal,
     *         positive if after. Null or empty time is treated as start of day.
     */
    private static int compareDateTime(String date1, String time1, String date2, String time2) {
	int c = (date1 != null ? date1 : "").compareTo(date2 != null ? date2 : "");
	if (c != 0) {
	    return c;
	}
	String t1 = (time1 != null && !time1.isEmpty()) ? time1 : "00:00:00";
	String t2 = (time2 != null && !time2.isEmpty()) ? time2 : "00:00:00";
	return t1.compareTo(t2);
    }

    private JSONObject executeGet(String url) throws IOException, InterruptedException {
	requestLock.lock();
	
	Integer status = null;
	try {
	    long now = System.currentTimeMillis();
	    long elapsed = now - lastRequestTimeMillis;
	    if (elapsed < MIN_REQUEST_INTERVAL_MS && lastRequestTimeMillis > 0) {
		Thread.sleep(MIN_REQUEST_INTERVAL_MS - elapsed);
	    }
	    lastRequestTimeMillis = System.currentTimeMillis();

	    GSLoggerFactory.getLogger(getClass()).debug("DataStreamClient GET {}", url);

	    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
		    .header("Accept", "application/vnd.api+json")
		    .header("x-api-key", apiKey != null ? apiKey : "")
		    .GET().build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	    status = response.statusCode();
		if (status != 200) {
		throw new IOException("HTTP request failed with status code: " + response.statusCode());
	    }
	    return new JSONObject(response.body());

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error executing GET request " + url + " " + status, e);
	    if (e instanceof IOException) {
		throw (IOException) e;
	    }
	    if (e instanceof InterruptedException) {
		throw (InterruptedException) e;
	    }
	    throw new IOException("Error executing GET request " + url, e);
	} finally {
	    requestLock.unlock();
	}
    }
}

