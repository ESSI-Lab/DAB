package eu.essi_lab.services.data_hub.test;

import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.util.*;

/**
 * @author Fabrizio
 */
public class DataHubTestClient {

    private static final String JSON_INPUT_FOLDER_PATH = "D:/Desktop/test";

    private static final String CREATE_ENDPOINT_BASE;
    private static final String DELETE_ENDPOINT_BASE;
    private static final String TOKEN;

    static {

	CREATE_ENDPOINT_BASE = System.getenv("createEndpoint");
	DELETE_ENDPOINT_BASE = System.getenv("deleteEndpoint");
	TOKEN = System.getenv("token");

    }

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    /**
     * @param input
     * @param sep
     * @return
     */
    private static Map<String, Object> flattenJson(Map<String, Object> input, String sep) {
	Map<String, Object> result = new HashMap<>();
	flatten("", input, result, sep);
	return result;
    }

    /**
     * @param prefix
     * @param value
     * @param out
     * @param sep
     */
    private static void flatten(String prefix, Object value, Map<String, Object> out, String sep) {
	if (value instanceof Map<?, ?> map) {
	    for (var entry : map.entrySet()) {
		String key = prefix.isEmpty() ? entry.getKey().toString() : prefix + sep + entry.getKey();
		flatten(key, entry.getValue(), out, sep);
	    }
	} else if (value instanceof List<?> list) {
	    try {
		out.put(prefix, MAPPER.writeValueAsString(list));
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	} else if (value != null) {
	    out.put(prefix, value);
	}
    }

    /**
     * @param file
     * @param sep
     * @return
     * @throws IOException
     */
    private static Map<String, Object> flattenJsonFromFile(Path file, String sep) throws IOException {
	Map<String, Object> json = MAPPER.readValue(file.toFile(), new TypeReference<>() {
	});
	return flattenJson(json, sep);
    }

    /**
     * @param flatJson
     * @return
     */
    private static Map<String, Object> preparePayload(Map<String, Object> flatJson) {

	String urn = (String) flatJson.get("identifier");
	String title = (String) flatJson.getOrDefault("title", "Unnamed dataset");
	String abstractTxt = (String) flatJson.getOrDefault("abstract", "No description available");

	Map<String, Object> aspect = Map.of("__type", "DatasetProperties", "name", title, "description", abstractTxt, "customProperties",
		flatJson);

	Map<String, Object> entity = Map.of("entityType", "dataset", "entityUrn", urn, "aspect", aspect);

	return Map.of("payload", List.of(entity), "urn", urn);
    }

    /**
     * @param url
     * @param token
     * @param payload
     * @return
     * @throws Exception
     */
    private static boolean post(String url, String token, Object payload) throws Exception {

	String body = MAPPER.writeValueAsString(payload);

	HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("accept", "application/json")
		.header("Content-Type", "application/json").header("Authorization", "Bearer " + token)
		.POST(HttpRequest.BodyPublishers.ofString(body)).build();

	HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

	return response.statusCode() == 200 || response.statusCode() == 201;
    }

    /**
     * @throws Exception
     */
    private static void upsert() throws Exception {

	int success = 0;
	List<String> failed = new ArrayList<>();

	try (var files = Files.list(Paths.get(JSON_INPUT_FOLDER_PATH))) {

	    for (Path file : files.filter(f -> f.toString().endsWith(".json")).toList()) {

		try {
		    Map<String, Object> flat = flattenJsonFromFile(file, ":::");
		    Map<String, Object> prepared = preparePayload(flat);

		    boolean ok = post(CREATE_ENDPOINT_BASE, TOKEN, prepared.get("payload"));

		    String urn = (String) prepared.get("urn");

		    if (ok) {
			success++;
			System.out.println("Published entity: " + urn);
		    } else {
			failed.add(file.getFileName().toString());
			System.out.println("Failed: " + file);
		    }

		} catch (Exception e) {
		    failed.add(file.getFileName().toString());
		    System.out.println("Exception: " + file + " -> " + e.getMessage());
		}
	    }
	}

	System.out.println("\n====================");
	System.out.println("Success: " + success);
	System.out.println("Failed: " + failed.size());
    }

    /**
     * @param urns
     * @throws Exception
     */
    private static void delete(List<String> urns) throws Exception {

	int success = 0;
	List<String> failed = new ArrayList<>();

	for (String urn : urns) {

	    String url = DELETE_ENDPOINT_BASE + urn;

	    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("accept", "*/*")
		    .header("Authorization", "Bearer " + TOKEN).DELETE().build();

	    HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

	    if (response.statusCode() == 200 || response.statusCode() == 204) {
		success++;
		System.out.println("Deleted entity: " + urn);
	    } else {
		failed.add(urn);
		System.out.println("Failed: " + urn);
	    }
	}

	System.out.println("\n====================");
	System.out.println("Deleted: " + success);
	System.out.println("Failed: " + failed.size());
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

	// UPSERT
	upsert();

 	List<String> urns = List.of(
		"urn:li:dataset:(urn:li:dataPlatform:metadata,test_id_4,DEV)");

//	delete(urns);
    }
}
