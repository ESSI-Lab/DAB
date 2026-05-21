package eu.essi_lab.accessor.hiscentral.test;

import eu.essi_lab.accessor.hiscentral.umbria.HISCentralUmbriaConnector;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class HISCentralUmbriaExternalTestIT {

    private static final String API_URL = "https://dati.regione.umbria.it/api/3/action/datastore_search";
    private static final String RESOURCE_ID = "50dbe5f8-5339-435f-9161-fe250669a97d";

    public static void main(String[] args) throws Exception {

	// 1️⃣ Recupero tutti gli ID stazione
	Set<String> stationIds = getAllStationIds();

	System.out.println("Totale stazioni trovate: " + stationIds.size());

	// 2️⃣ Per ogni stazione verifico i dati
	Map<String, StationResult> results = new HashMap<>();

	for (String stationId : stationIds) {
	    String response = callCKANForStation(stationId);
	    StationResult stationResult = hasValidFlowValues(response,  stationId);

	    results.put(stationId, stationResult);
	}

	// 3️⃣ Output finale
	System.out.println("\n=== RISULTATI ===");

	results.values().forEach(r -> {
	    System.out.println(
		    r.getStationId() + " - " +
			    r.getStationName() + " -> " +
			    (r.hasValidData() ? "VALIDA" : "NON VALIDA")
	    );
	});
    }

    // ================================
    // 🔹 STEP 1: Recupero ID stazioni
    // ================================
    public static Set<String> getAllStationIds() throws Exception {

	Set<String> ids = new HashSet<>();

	int limit = 1000;
	int offset = 0;
	boolean hasMore = true;

	while (hasMore) {

	    JSONObject payload = new JSONObject();
	    payload.put("resource_id", RESOURCE_ID);
	    payload.put("limit", limit);
	    payload.put("offset", offset);

	    JSONArray fields = new JSONArray();
	    fields.put("ID_STAZIONE");
	    payload.put("fields", fields);

	    String response = sendPost(payload);

	    JSONObject root = new JSONObject(response);
	    JSONArray records = root.getJSONObject("result").getJSONArray("records");

	    for (int i = 0; i < records.length(); i++) {
		String id = records.getJSONObject(i).optString("ID_STAZIONE", null);
		if (id != null) {
		    ids.add(id);
		}
	    }

	    // paginazione
	    if (records.length() < limit) {
		hasMore = false;
	    } else {
		offset += limit;
	    }
	}

	return ids;
    }

    // ======================================
    // 🔹 STEP 2: Chiamata per singola stazione
    // ======================================
    public static String callCKANForStation(String stationId) throws Exception {

	JSONObject payload = new JSONObject();
	payload.put("resource_id", RESOURCE_ID);

	JSONObject filters = new JSONObject();
	filters.put("ID_STAZIONE", stationId);

	payload.put("filters", filters);
	payload.put("limit", 400);
	payload.put("sort", "ANNO asc, MESE asc, GIORNO asc");

	return sendPost(payload);
    }

    // ======================================
    // 🔹 STEP 3: Verifica valori validi
    // ======================================
    public static StationResult hasValidFlowValues(String json, String stationId) {


	boolean valid = false;
	JSONObject root = new JSONObject(json);
	JSONArray records = root.getJSONObject("result").getJSONArray("records");

	String stationName = null;
	for (int i = 0; i < records.length(); i++) {
	    JSONObject record = records.getJSONObject(i);

	    // nome stazione (lo prendo una volta sola)
	    if (stationName == null) {
		stationName = record.optString("NOME_STAZIONE", "UNKNOWN");
	    }
	    String flow = record.optString("AVGDAY", "NA");
	    String flow1 = record.optString("MINDAY", "NA");
	    String flow2 = record.optString("MAXDAY", "NA");

	    if (!flow.equalsIgnoreCase("NA") && !flow.isEmpty()) {
		valid=true;
	    }

	    if (!flow1.equalsIgnoreCase("NA") && !flow1.isEmpty()) {
		valid=true;
	    }

	    if (!flow2.equalsIgnoreCase("NA") && !flow2.isEmpty()) {
		valid=true;
	    }
	}
	return new StationResult(stationId, stationName, valid);
    }

    // ======================================
    // 🔹 Utility HTTP POST
    // ======================================
    public static String sendPost(JSONObject payload) throws Exception {

	HashMap<String, String> map = new HashMap<String, String>();
	map.put("accept", "text/plain");
	map.put("Content-Type", "application/json");

	HttpRequest request = HttpRequestUtils.build(HttpRequestUtils.MethodWithBody.POST, API_URL, payload.toString(),
		HttpHeaderUtils.build(map));

	HttpResponse<InputStream> response = new Downloader().downloadResponse(request);
	int statusCode = response.statusCode();

	InputStream input = response.body();

	ByteArrayOutputStream output = new ByteArrayOutputStream();

	IOUtils.copy(input, output);

	String result = new String(output.toByteArray());

	if(input != null) {
	    input.close();
	}
	return result;
    }

    public static class StationResult {
	private final String stationId;
	private final String stationName;
	private final boolean hasValidData;

	public StationResult(String stationId, String stationName, boolean hasValidData) {
	    this.stationId = stationId;
	    this.stationName = stationName;
	    this.hasValidData = hasValidData;
	}

	public String getStationId() { return stationId; }
	public String getStationName() { return stationName; }
	public boolean hasValidData() { return hasValidData; }
    }
}



