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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private final String endpoint;
    private final String apiKey;
    private final HttpClient httpClient;

    public DataStreamClient(String endpoint, String apiKey) {
	this.endpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
	this.apiKey = apiKey;
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
	String distinct = "%24distinct=true";

	String url = endpoint + "/Observations?%24filter=" + filter + "&" + distinct + "&" + select;
	String next = url;
	while (next != null) {
	    JSONObject page = executeGet(next);
	    JSONArray value = page.optJSONArray("value");
	    if (value != null) {
		for (int i = 0; i < value.length(); i++) {
		    JSONObject obj = value.getJSONObject(i);
		    String name = obj.optString("CharacteristicName", null);
		    if (name != null && !name.isEmpty()) {
			names.add(name);
			if (maxCharacteristics > 0 && names.size() >= maxCharacteristics) {
			    return names;
			}
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
	    filter.append("%20and%20ActivityStartYear%20ge%20").append(fromYear);
	}
	if (toYear != null) {
	    filter.append("%20and%20ActivityStartYear%20le%20").append(toYear);
	}

	StringBuilder urlBuilder = new StringBuilder(endpoint).append("/Observations?%24filter=").append(filter.toString())
		.append("%24%24select=DOI,CharacteristicName,ResultValue,ResultUnit,ActivityStartDate,ActivityStartTime");
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

    private JSONObject executeGet(String url) throws IOException, InterruptedException {
	GSLoggerFactory.getLogger(getClass()).debug("DataStreamClient GET {}", url);

	HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
		.header("Accept", "application/vnd.api+json")
		.header("x-api-key", apiKey != null ? apiKey : "")
		.GET().build();

	HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	if (response.statusCode() != 200) {
	    throw new IOException("HTTP request failed with status code: " + response.statusCode());
	}
	return new JSONObject(response.body());
    }
}

