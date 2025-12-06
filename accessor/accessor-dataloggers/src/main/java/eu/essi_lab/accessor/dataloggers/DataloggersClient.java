package eu.essi_lab.accessor.dataloggers;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class DataloggersClient {

    private String endpoint;
    private HttpClient httpClient;
    private Map<String, Variable> variableCache;

    public DataloggersClient(String endpoint) {
	this.endpoint = endpoint;
	this.httpClient = HttpClient.newHttpClient();
	this.variableCache = new HashMap<>();
	initializeVariableCache();
    }

    private void initializeVariableCache() {

	try {
	    List<Variable> allVariables = getAllVariables();
	    for (Variable variable : allVariables) {
		if (variable.getVarCod() != null) {
		    variableCache.put(variable.getVarCod(), variable);
		}
	    }
	} catch (Exception e) {
	    System.err.println("Warning: Failed to initialize variable cache: " + e.getMessage());
	}
    }

    /**
     * Retrieves dataloggers from the API with pagination support.
     * 
     * @param page The page number (0-based)
     * @param size The page size
     * @return DataloggersResponse containing the dataloggers and pagination info
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the request is interrupted
     */
    public DataloggersResponse getDataloggers(Integer page, Integer size) throws IOException, InterruptedException {
	String url = endpoint + "/dataloggers";

	// Build query parameters
	StringBuilder queryParams = new StringBuilder();
	if (page != null) {
	    queryParams.append("page=").append(URLEncoder.encode(String.valueOf(page), StandardCharsets.UTF_8));
	}
	if (size != null) {
	    if (queryParams.length() > 0) {
		queryParams.append("&");
	    }
	    queryParams.append("size=").append(URLEncoder.encode(String.valueOf(size), StandardCharsets.UTF_8));
	}

	// Append query parameters to URL
	if (queryParams.length() > 0) {
	    url += "?" + queryParams.toString();
	}

	// Create POST request with query parameters (no body)
	HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).POST(HttpRequest.BodyPublishers.noBody()).build();

	HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

	if (response.statusCode() != 200) {
	    throw new IOException("HTTP request failed with status code: " + response.statusCode());
	}

	return parseResponse(response.body());
    }

    /**
     * Retrieves all dataloggers by automatically handling pagination.
     * 
     * @return List of all dataloggers
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the request is interrupted
     */
    public List<Datalogger> getAllDataloggers() throws IOException, InterruptedException {
	List<Datalogger> allDataloggers = new ArrayList<>();
	int page = 0;
	int size = 100; // Default page size
	boolean hasMore = true;

	while (hasMore) {
	    DataloggersResponse response = getDataloggers(page, size);
	    if (response.getContent() != null) {
		allDataloggers.addAll(response.getContent());
	    }
	    hasMore = response.getLast() != null && !response.getLast();
	    page++;
	}

	return allDataloggers;
    }

    private DataloggersResponse parseResponse(String jsonString) {
	JSONObject json = new JSONObject(jsonString);
	DataloggersResponse response = new DataloggersResponse();

	// Parse content array
	if (json.has("content") && !json.isNull("content")) {
	    JSONArray contentArray = json.getJSONArray("content");
	    List<Datalogger> dataloggers = new ArrayList<>();
	    for (int i = 0; i < contentArray.length(); i++) {
		JSONObject dataloggerJson = contentArray.getJSONObject(i);
		dataloggers.add(parseDatalogger(dataloggerJson));
	    }
	    response.setContent(dataloggers);
	}

	// Parse pageable
	if (json.has("pageable") && !json.isNull("pageable")) {
	    JSONObject pageableJson = json.getJSONObject("pageable");
	    Pageable pageable = new Pageable();
	    if (pageableJson.has("pageNumber")) {
		pageable.setPageNumber(pageableJson.getInt("pageNumber"));
	    }
	    if (pageableJson.has("pageSize")) {
		pageable.setPageSize(pageableJson.getInt("pageSize"));
	    }
	    response.setPageable(pageable);
	}

	// Parse pagination fields
	if (json.has("totalPages")) {
	    response.setTotalPages(json.getInt("totalPages"));
	}
	if (json.has("totalElements")) {
	    response.setTotalElements(json.getLong("totalElements"));
	}
	if (json.has("last")) {
	    response.setLast(json.getBoolean("last"));
	}
	if (json.has("first")) {
	    response.setFirst(json.getBoolean("first"));
	}

	return response;
    }

    private Datalogger parseDatalogger(JSONObject json) {
	Datalogger datalogger = new Datalogger();

	if (json.has("datalogger_id")) {
	    datalogger.setDataloggerId(json.getInt("datalogger_id"));
	}
	if (json.has("datalogger_cod")) {
	    datalogger.setDataloggerCod(json.getString("datalogger_cod"));
	}
	if (json.has("dataprovider_id")) {
	    datalogger.setDataproviderId(json.getInt("dataprovider_id"));
	}
	if (json.has("dataprovider_cod")) {
	    datalogger.setDataproviderCod(json.getString("dataprovider_cod"));
	}
	if (json.has("datalogger_location")) {
	    datalogger.setDataloggerLocation(json.getString("datalogger_location"));
	}
	if (json.has("datalogger_available_since") && !json.isNull("datalogger_available_since")) {
	    datalogger.setDataloggerAvailableSince(OffsetDateTime.parse(json.getString("datalogger_available_since")));
	}
	if (json.has("datalogger_available_until") && !json.isNull("datalogger_available_until")) {
	    datalogger.setDataloggerAvailableUntil(OffsetDateTime.parse(json.getString("datalogger_available_until")));
	}

	// Parse datastreams
	if (json.has("datastreams") && !json.isNull("datastreams")) {
	    JSONArray datastreamsArray = json.getJSONArray("datastreams");
	    List<Datastream> datastreams = new ArrayList<>();
	    for (int i = 0; i < datastreamsArray.length(); i++) {
		JSONObject datastreamJson = datastreamsArray.getJSONObject(i);
		datastreams.add(parseDatastream(datastreamJson));
	    }
	    datalogger.setDatastreams(datastreams);
	}

	return datalogger;
    }

    private Datastream parseDatastream(JSONObject json) {
	Datastream datastream = new Datastream();

	if (json.has("uom_id")) {
	    datastream.setUomId(json.getInt("uom_id"));
	}
	if (json.has("var_id")) {
	    datastream.setVarId(json.getInt("var_id"));
	}
	if (json.has("uom_cod")) {
	    datastream.setUomCod(json.getString("uom_cod"));
	}
	if (json.has("var_cod")) {
	    datastream.setVarCod(json.getString("var_cod"));
	}
	if (json.has("datalogger_id")) {
	    datastream.setDataloggerId(json.getInt("datalogger_id"));
	}
	if (json.has("datastream_id")) {
	    datastream.setDatastreamId(json.getInt("datastream_id"));
	}
	if (json.has("datalogger_cod")) {
	    datastream.setDataloggerCod(json.getString("datalogger_cod"));
	}
	if (json.has("tipologia_rete")) {
	    datastream.setTipologiaRete(json.getString("tipologia_rete"));
	}
	if (json.has("dataprovider_id")) {
	    datastream.setDataproviderId(json.getInt("dataprovider_id"));
	}
	if (json.has("datastream_step")) {
	    datastream.setDatastreamStep(json.getInt("datastream_step"));
	}
	if (json.has("dataprovider_cod")) {
	    datastream.setDataproviderCod(json.getString("dataprovider_cod"));
	}
	if (json.has("datalogger_location")) {
	    datastream.setDataloggerLocation(json.getString("datalogger_location"));
	}
	if (json.has("datastream_available_since") && !json.isNull("datastream_available_since")) {
	    datastream.setDatastreamAvailableSince(OffsetDateTime.parse(json.getString("datastream_available_since")));
	}
	if (json.has("datastream_available_until") && !json.isNull("datastream_available_until")) {
	    datastream.setDatastreamAvailableUntil(OffsetDateTime.parse(json.getString("datastream_available_until")));
	}

	return datastream;
    }

    /**
     * Retrieves variables from the API with pagination support.
     * 
     * @param page The page number (0-based)
     * @param size The page size
     * @return VariablesResponse containing the variables and pagination info
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the request is interrupted
     */
    public VariablesResponse getVariables(Integer page, Integer size) throws IOException, InterruptedException {
	String url = endpoint + "vars";

	// Build query parameters
	StringBuilder queryParams = new StringBuilder();
	if (page != null) {
	    queryParams.append("page=").append(URLEncoder.encode(String.valueOf(page), StandardCharsets.UTF_8));
	}
	if (size != null) {
	    if (queryParams.length() > 0) {
		queryParams.append("&");
	    }
	    queryParams.append("size=").append(URLEncoder.encode(String.valueOf(size), StandardCharsets.UTF_8));
	}

	// Append query parameters to URL
	if (queryParams.length() > 0) {
	    url += "?" + queryParams.toString();
	}

	// Create POST request with query parameters (no body)
	HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

	HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

	if (response.statusCode() != 200) {
	    throw new IOException("HTTP request failed with status code: " + response.statusCode());
	}

	return parseVariablesResponse(response.body());
    }

    /**
     * Retrieves all variables by automatically handling pagination.
     * 
     * @return List of all variables
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the request is interrupted
     */
    public List<Variable> getAllVariables() throws IOException, InterruptedException {
	List<Variable> allVariables = new ArrayList<>();
	int page = 0;
	int size = 100; // Default page size
	boolean hasMore = true;

	while (hasMore) {
	    VariablesResponse response = getVariables(page, size);
	    if (response.getContent() != null) {
		allVariables.addAll(response.getContent());
	    }
	    hasMore = response.getLast() != null && !response.getLast();
	    page++;
	}

	return allVariables;
    }

    /**
     * Retrieves variable information by variable code from the cache.
     * 
     * @param varCod The variable code
     * @return Variable object if found, null otherwise
     */
    public Variable getVariableByCode(String varCod) {
	if (variableCache.isEmpty()) {
	    initializeVariableCache();
	}
	return variableCache.get(varCod);
    }

    /**
     * Checks if a variable exists in the cache by code.
     * 
     * @param varCod The variable code
     * @return true if the variable exists, false otherwise
     */
    public boolean hasVariable(String varCod) {
	if (variableCache.isEmpty()) {
	    initializeVariableCache();
	}
	return variableCache.containsKey(varCod);
    }

    private VariablesResponse parseVariablesResponse(String jsonString) {
	JSONObject json = new JSONObject(jsonString);
	VariablesResponse response = new VariablesResponse();

	// Parse content array
	if (json.has("content") && !json.isNull("content")) {
	    JSONArray contentArray = json.getJSONArray("content");
	    List<Variable> variables = new ArrayList<>();
	    for (int i = 0; i < contentArray.length(); i++) {
		JSONObject variableJson = contentArray.getJSONObject(i);
		variables.add(parseVariable(variableJson));
	    }
	    response.setContent(variables);
	}

	// Parse pageable
	if (json.has("pageable") && !json.isNull("pageable")) {
	    JSONObject pageableJson = json.getJSONObject("pageable");
	    Pageable pageable = new Pageable();
	    if (pageableJson.has("pageNumber")) {
		pageable.setPageNumber(pageableJson.getInt("pageNumber"));
	    }
	    if (pageableJson.has("pageSize")) {
		pageable.setPageSize(pageableJson.getInt("pageSize"));
	    }
	    response.setPageable(pageable);
	}

	// Parse pagination fields
	if (json.has("totalPages")) {
	    response.setTotalPages(json.getInt("totalPages"));
	}
	if (json.has("totalElements")) {
	    response.setTotalElements(json.getLong("totalElements"));
	}
	if (json.has("last")) {
	    response.setLast(json.getBoolean("last"));
	}
	if (json.has("first")) {
	    response.setFirst(json.getBoolean("first"));
	}

	return response;
    }

    private Variable parseVariable(JSONObject json) {
	Variable variable = new Variable();

	if (json.has("var_id")) {
	    variable.setVarId(json.getInt("var_id"));
	}
	if (json.has("var_cod")) {
	    variable.setVarCod(json.getString("var_cod"));
	}
	if (json.has("uom_id")) {
	    variable.setUomId(json.getInt("uom_id"));
	}
	if (json.has("uom_cod")) {
	    variable.setUomCod(json.getString("uom_cod"));
	}

	return variable;
    }

    /**
     * Retrieves data from the API with filtering and pagination support.
     * 
     * @param varIds List of variable IDs to filter by (can be null)
     * @param dataloggerIds List of datalogger IDs to filter by (can be null)
     * @param datastreamIds List of datastream IDs to filter by (can be null)
     * @param startDate Start date in ISO format (can be null)
     * @param endDate End date in ISO format (can be null)
     * @param page The page number (0-based)
     * @param size The page size
     * @return DataResponse containing the features and pagination info
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the request is interrupted
     */
    public DataResponse getData(List<Integer> varIds, List<Integer> dataloggerIds, List<Integer> datastreamIds, String startDate,
	    String endDate, Integer page, Integer size) throws IOException, InterruptedException {
	String url = endpoint + "data";

	// Build query parameters for pagination
	StringBuilder queryParams = new StringBuilder();
	if (page != null) {
	    queryParams.append("page=").append(URLEncoder.encode(String.valueOf(page), StandardCharsets.UTF_8));
	}
	if (size != null) {
	    if (queryParams.length() > 0) {
		queryParams.append("&");
	    }
	    queryParams.append("size=").append(URLEncoder.encode(String.valueOf(size), StandardCharsets.UTF_8));
	}

	// Append query parameters to URL
	if (queryParams.length() > 0) {
	    url += "?" + queryParams.toString();
	}

	// Build form data
	StringBuilder formData = new StringBuilder();
	if (varIds != null && !varIds.isEmpty()) {
	    for (Integer varId : varIds) {
		if (formData.length() > 0) {
		    formData.append("&");
		}
		formData.append("var_ids=").append(URLEncoder.encode(String.valueOf(varId), StandardCharsets.UTF_8));
	    }
	}
	if (dataloggerIds != null && !dataloggerIds.isEmpty()) {
	    for (Integer dataloggerId : dataloggerIds) {
		if (formData.length() > 0) {
		    formData.append("&");
		}
		formData.append("datalogger_ids=").append(URLEncoder.encode(String.valueOf(dataloggerId), StandardCharsets.UTF_8));
	    }
	}
	if (datastreamIds != null && !datastreamIds.isEmpty()) {
	    for (Integer datastreamId : datastreamIds) {
		if (formData.length() > 0) {
		    formData.append("&");
		}
		formData.append("datastream_ids=").append(URLEncoder.encode(String.valueOf(datastreamId), StandardCharsets.UTF_8));
	    }
	}
	if (startDate != null && !startDate.isEmpty()) {
	    if (formData.length() > 0) {
		formData.append("&");
	    }
	    formData.append("start_date=").append(URLEncoder.encode(startDate, StandardCharsets.UTF_8));
	}
	if (endDate != null && !endDate.isEmpty()) {
	    if (formData.length() > 0) {
		formData.append("&");
	    }
	    formData.append("end_date=").append(URLEncoder.encode(endDate, StandardCharsets.UTF_8));
	}

	// Create POST request with form data
	HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/x-www-form-urlencoded")
		.POST(HttpRequest.BodyPublishers.ofString(formData.toString())).build();

	HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

	if (response.statusCode() != 200) {
	    throw new IOException("HTTP request failed with status code: " + response.statusCode());
	}

	return parseDataResponse(response.body());
    }

    private DataResponse parseDataResponse(String jsonString) {
	JSONObject json = new JSONObject(jsonString);
	DataResponse response = new DataResponse();

	// Parse content (FeatureCollection)
	if (json.has("content") && !json.isNull("content")) {
	    JSONObject contentJson = json.getJSONObject("content");
	    FeatureCollection featureCollection = parseFeatureCollection(contentJson);
	    response.setContent(featureCollection);
	}

	// Parse pageable
	if (json.has("pageable") && !json.isNull("pageable")) {
	    JSONObject pageableJson = json.getJSONObject("pageable");
	    Pageable pageable = new Pageable();
	    if (pageableJson.has("pageNumber")) {
		pageable.setPageNumber(pageableJson.getInt("pageNumber"));
	    }
	    if (pageableJson.has("pageSize")) {
		pageable.setPageSize(pageableJson.getInt("pageSize"));
	    }
	    response.setPageable(pageable);
	}

	// Parse pagination fields
	if (json.has("totalPages")) {
	    response.setTotalPages(json.getInt("totalPages"));
	}
	if (json.has("totalElements")) {
	    response.setTotalElements(json.getLong("totalElements"));
	}
	if (json.has("last")) {
	    response.setLast(json.getBoolean("last"));
	}
	if (json.has("first")) {
	    response.setFirst(json.getBoolean("first"));
	}

	return response;
    }

    private FeatureCollection parseFeatureCollection(JSONObject json) {
	FeatureCollection featureCollection = new FeatureCollection();

	if (json.has("type")) {
	    featureCollection.setType(json.getString("type"));
	}

	if (json.has("features") && !json.isNull("features")) {
	    JSONArray featuresArray = json.getJSONArray("features");
	    List<Feature> features = new ArrayList<>();
	    for (int i = 0; i < featuresArray.length(); i++) {
		JSONObject featureJson = featuresArray.getJSONObject(i);
		features.add(parseFeature(featureJson));
	    }
	    featureCollection.setFeatures(features);
	}

	return featureCollection;
    }

    private Feature parseFeature(JSONObject json) {
	Feature feature = new Feature();

	if (json.has("id")) {
	    feature.setId(json.getString("id"));
	}
	if (json.has("type")) {
	    feature.setType(json.getString("type"));
	}

	if (json.has("properties") && !json.isNull("properties")) {
	    JSONObject propertiesJson = json.getJSONObject("properties");
	    feature.setProperties(parseFeatureProperties(propertiesJson));
	}

	if (json.has("geometry") && !json.isNull("geometry")) {
	    JSONObject geometryJson = json.getJSONObject("geometry");
	    feature.setGeometry(parseGeometry(geometryJson));
	}

	return feature;
    }

    private FeatureProperties parseFeatureProperties(JSONObject json) {
	FeatureProperties properties = new FeatureProperties();

	if (json.has("id")) {
	    properties.setId(json.getString("id"));
	}
	if (json.has("date") && !json.isNull("date")) {
	    properties.setDate(OffsetDateTime.parse(json.getString("date")));
	}
	if (json.has("datastream_id")) {
	    properties.setDatastreamId(json.getInt("datastream_id"));
	}

	if (json.has("additionalAttributes") && !json.isNull("additionalAttributes")) {
	    JSONObject additionalAttributesJson = json.getJSONObject("additionalAttributes");
	    AdditionalAttributes additionalAttributes = new AdditionalAttributes();

	    if (additionalAttributesJson.has("measurement") && !additionalAttributesJson.isNull("measurement")) {
		JSONObject measurementJson = additionalAttributesJson.getJSONObject("measurement");
		additionalAttributes.setMeasurement(parseMeasurement(measurementJson));
	    }

	    properties.setAdditionalAttributes(additionalAttributes);
	}

	return properties;
    }

    private Measurement parseMeasurement(JSONObject json) {
	Measurement measurement = new Measurement();

	if (json.has("type")) {
	    measurement.setType(json.getString("type"));
	}
	if (json.has("value") && !json.isNull("value")) {
	    measurement.setValue(json.getDouble("value"));
	}
	if (json.has("unit")) {
	    measurement.setUnit(json.getString("unit"));
	}
	if (json.has("timestamp") && !json.isNull("timestamp")) {
	    measurement.setTimestamp(OffsetDateTime.parse(json.getString("timestamp")));
	}
	if (json.has("source")) {
	    measurement.setSource(json.getString("source"));
	}
	if (json.has("period")) {
	    measurement.setPeriod(json.getString("period"));
	}
	if (json.has("data_count")) {
	    measurement.setDataCount(json.getInt("data_count"));
	}
	if (json.has("parameter")) {
	    measurement.setParameter(json.getString("parameter"));
	}

	return measurement;
    }

    private Geometry parseGeometry(JSONObject json) {
	Geometry geometry = new Geometry();

	if (json.has("type")) {
	    geometry.setType(json.getString("type"));
	}

	if (json.has("coordinates") && !json.isNull("coordinates")) {
	    JSONArray coordinatesArray = json.getJSONArray("coordinates");
	    List<Double> coordinates = new ArrayList<>();
	    for (int i = 0; i < coordinatesArray.length(); i++) {
		coordinates.add(coordinatesArray.getDouble(i));
	    }
	    geometry.setCoordinates(coordinates);
	}

	return geometry;
    }

    public static void main(String[] args) {
	String endpoint = System.getProperty("dataloggersEndpoint");
	if (endpoint == null || endpoint.isEmpty()) {
	    System.err.println("Please set the dataloggersEndpoint system property");
	    return;
	}

	DataloggersClient client = new DataloggersClient(endpoint);

	try {
	    System.out.println("Retrieving dataloggers...");

	    // Example: Retrieve first page with 10 items

	    int page = 0;
	    int size = 2; // Default page size
	    boolean hasMore = true;
	    while (hasMore) {
		DataloggersResponse response = client.getDataloggers(page, size);
		System.out.println("Total pages: " + response.getTotalPages());
		System.out.println("Total elements: " + response.getTotalElements());
		System.out.println("Current page: " + (response.getPageable() != null ? response.getPageable().getPageNumber() : "N/A"));
		System.out.println("Page size: " + (response.getPageable() != null ? response.getPageable().getPageSize() : "N/A"));
		System.out.println();

		if (response.getContent() != null) {
		    for (Datalogger datalogger : response.getContent()) {
			System.out.println("Datalogger: " + datalogger);
			if (datalogger.getDatastreams() != null) {
			    System.out.println("  Datastreams:");
			    for (Datastream datastream : datalogger.getDatastreams()) {
				System.out.println("    - " + datastream);
				String varCode = datastream.getVarCod();
				System.out.println(client.getVariableByCode(varCode));
				System.out.println(datastream.getDatastreamAvailableSince());
				System.out.println(datastream.getDatastreamAvailableUntil());
			    }
			}
			System.out.println();
		    }
		}
		hasMore = response.getLast() != null && !response.getLast();
		page++;
		if (page > 3) {
		    break;
		}
	    }

	    List<Integer> varids = new ArrayList<Integer>();
	    varids.add(2);
	    List<Integer> dataloggerids = new ArrayList<Integer>();
	    dataloggerids.add(17);
	    List<Integer> datastreamids = new ArrayList<Integer>();
	    datastreamids.add(23);
	    DataResponse data = client.getData(varids, dataloggerids, datastreamids, "2025-01-01", "2025-02-01", 0, 2);
	    List<Feature> features = data.getContent().getFeatures();
	    for (Feature feature : features) {
		Measurement measurement = feature.getProperties().getAdditionalAttributes().getMeasurement();
		System.out.println(measurement);
	    }
	    // Example: Retrieve all dataloggers (uncomment to use)
	    // System.out.println("\nRetrieving all dataloggers...");
	    // System.out.println("Retrieved " + allDataloggers.size() + " dataloggers");

	} catch (Exception e) {
	    System.err.println("Error retrieving dataloggers: " + e.getMessage());
	    e.printStackTrace();
	}
    }
}
