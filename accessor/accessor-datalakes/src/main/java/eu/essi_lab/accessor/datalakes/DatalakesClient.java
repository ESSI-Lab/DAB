package eu.essi_lab.accessor.datalakes;

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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * HTTP client for the Datalakes REST API ({@code https://api.datalakes-eawag.ch}).
 */
public class DatalakesClient {

    public static final String DEFAULT_ENDPOINT = "https://api.datalakes-eawag.ch";

    public static final String PAYLOAD_TIME_SERIES = "TIME_SERIES";
    public static final String PAYLOAD_GRID = "GRID";

    private final String endpoint;
    private final String apiKey;
    private final HttpClient httpClient;

    public DatalakesClient(String endpoint) {
	this(endpoint, null);
    }

    public DatalakesClient(String endpoint, String apiKey) {
	this.endpoint = normalizeEndpoint(endpoint);
	this.apiKey = apiKey;
	this.httpClient = HttpClient.newHttpClient();
    }

    public static String normalizeEndpoint(String endpoint) {
	if (endpoint == null || endpoint.isEmpty()) {
	    return DEFAULT_ENDPOINT;
	}
	String normalized = endpoint.trim();
	while (normalized.endsWith("/")) {
	    normalized = normalized.substring(0, normalized.length() - 1);
	}
	return normalized;
    }

    public List<JSONObject> listDatasets() throws IOException, InterruptedException {
	JSONArray array = executeGetArray("/datasets");
	List<JSONObject> datasets = new ArrayList<>();
	if (array != null) {
	    for (int i = 0; i < array.length(); i++) {
		datasets.add(array.getJSONObject(i));
	    }
	}
	return datasets;
    }

    public JSONObject getDataset(int datasetId) throws IOException, InterruptedException {
	return executeGetObject("/datasets/" + datasetId);
    }

    public List<JSONObject> getDatasetParameters(int datasetId) throws IOException, InterruptedException {
	JSONArray array = executeGetArray("/datasetparameters/" + datasetId);
	List<JSONObject> parameters = new ArrayList<>();
	if (array != null) {
	    for (int i = 0; i < array.length(); i++) {
		parameters.add(array.getJSONObject(i));
	    }
	}
	return parameters;
    }

    public JSONObject getSelectionTables() throws IOException, InterruptedException {
	return executeGetObject("/selectiontables");
    }

    public List<JSONObject> listFiles(int datasetId) throws IOException, InterruptedException {
	JSONArray array = executeGetArray("/files/?datasets_id=" + datasetId);
	List<JSONObject> files = new ArrayList<>();
	if (array != null) {
	    for (int i = 0; i < array.length(); i++) {
		files.add(array.getJSONObject(i));
	    }
	}
	return files;
    }

    public JSONObject downloadJsonFile(int fileId) throws IOException, InterruptedException {
	String body = executeGetRaw("/download/" + fileId);
	return new JSONObject(body);
    }

    /**
     * Returns {@link #PAYLOAD_TIME_SERIES} when the dataset has no grid ({@code z}) axes,
     * otherwise {@link #PAYLOAD_GRID}.
     */
    public static String resolvePayloadType(List<JSONObject> datasetParameters) {
	if (datasetParameters == null || datasetParameters.isEmpty()) {
	    return PAYLOAD_GRID;
	}
	for (JSONObject parameter : datasetParameters) {
	    String axis = parameter.optString("axis", "");
	    if (axis.startsWith("z")) {
		return PAYLOAD_GRID;
	    }
	}
	return PAYLOAD_TIME_SERIES;
    }

    public static boolean isInternalDataset(JSONObject dataset) {
	return "internal".equalsIgnoreCase(dataset.optString("datasource", ""));
    }

    public static boolean isObservableParameter(JSONObject datasetParameter) {
	String axis = datasetParameter.optString("axis", "");
	if (!axis.startsWith("y")) {
	    return false;
	}
	String parseParameter = datasetParameter.optString("parseparameter", "").toLowerCase();
	return !parseParameter.endsWith("_qual") && !parseParameter.contains("qual");
    }

    public static boolean isTimeSeriesRecord(String payloadType, JSONObject dataset) {
	return PAYLOAD_TIME_SERIES.equals(payloadType) && isInternalDataset(dataset);
    }

    public static JSONObject findLookupEntry(JSONArray table, int id) {
	if (table == null) {
	    return null;
	}
	for (int i = 0; i < table.length(); i++) {
	    JSONObject entry = table.getJSONObject(i);
	    if (entry.optInt("id") == id) {
		return entry;
	    }
	}
	return null;
    }

    public static boolean overlaps(Date begin, Date end, JSONObject file) {
	if (begin == null || end == null) {
	    return true;
	}
	String minDateTime = file.optString("mindatetime", null);
	String maxDateTime = file.optString("maxdatetime", null);
	if (minDateTime == null || minDateTime.isEmpty() || maxDateTime == null || maxDateTime.isEmpty()) {
	    return true;
	}
	long fileBegin = parseIso8601Millis(minDateTime);
	long fileEnd = parseIso8601Millis(maxDateTime);
	return fileBegin <= end.getTime() && fileEnd >= begin.getTime();
    }

    public static long parseIso8601Millis(String iso8601) {
	return ISO8601DateTimeUtils.parseISO8601ToDate(iso8601).map(Date::getTime).orElse(0L);
    }

    private JSONObject executeGetObject(String path) throws IOException, InterruptedException {
	String body = executeGetRaw(path);
	return new JSONObject(body);
    }

    private JSONArray executeGetArray(String path) throws IOException, InterruptedException {
	String body = executeGetRaw(path);
	return new JSONArray(body);
    }

    private String executeGetRaw(String path) throws IOException, InterruptedException {
	String url = endpoint + path;
	GSLoggerFactory.getLogger(getClass()).debug("DatalakesClient GET {}", url);

	HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).header("Accept", "application/json").GET();
	if (apiKey != null && !apiKey.isEmpty()) {
	    builder.header("api_key", apiKey);
	}

	HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	if (response.statusCode() != 200) {
	    throw new IOException("HTTP request failed with status code " + response.statusCode() + " for " + url);
	}
	return response.body();
    }
}
