package eu.essi_lab.accessor.eurobis.ld;

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

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * Downloads a Marineinfo DCAT collection Turtle file, lists {@code dcat:dataset} IRIs, fetches each
 * {@code .json} document, and reports how often {@code "projects"} is non-null and non-empty.
 * <p>
 * Optional arguments: {@code [collectionUrl] [maxDatasets]} — default collection is EurOBIS
 * collection 619; {@code maxDatasets} limits how many datasets are checked (handy for a quick run).
 */
public final class MarineinfoDatasetProjectsSurvey {

    public static final String DEFAULT_COLLECTION_TTL = "https://marineinfo.org/id/collection/619.ttl";

    private static final Logger LOGGER = GSLoggerFactory.getLogger(MarineinfoDatasetProjectsSurvey.class);

    private static final String DCAT_NS = "http://www.w3.org/ns/dcat#";
    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    public static void main(String[] args) throws Exception {
	String collectionUrl = args.length > 0 ? args[0] : DEFAULT_COLLECTION_TTL;
	Integer maxDatasets = null;
	if (args.length > 1) {
	    maxDatasets = Integer.parseInt(args[1]);
	}

	HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT)
		.followRedirects(HttpClient.Redirect.NORMAL).build();

	System.out.println("Collection: " + collectionUrl);
	String turtle = httpGetString(client, collectionUrl);
	Set<String> datasetUris = extractDatasetUris(turtle);
	List<String> ordered = new ArrayList<>(datasetUris);
	if (maxDatasets != null) {
	    ordered = new ArrayList<>(ordered.subList(0, Math.min(maxDatasets, ordered.size())));
	}

	int totalToCheck = ordered.size();
	int withProjects = 0;
	int withoutProjects = 0;
	int failedJson = 0;

	int logEvery = Math.max(1, totalToCheck / 100);
	LOGGER.info("Fetching JSON for {} dataset(s); progress logged about every {} dataset(s) (~1%% steps).", totalToCheck,
		logEvery);

	for (int idx = 0; idx < totalToCheck; idx++) {
	    String dsUri = ordered.get(idx);
	    String jsonUrl = dsUri.endsWith(".json") ? dsUri : dsUri + ".json";
	    try {
		String body = httpGetString(client, jsonUrl);
		JSONObject json = new JSONObject(body);
		if (hasNonEmptyProjects(json)) {
		    withProjects++;
		    String hitMsg = String.format(Locale.US, "Dataset %s has non-empty \"projects\" (%s). %s",
			    datasetIdForLog(dsUri, json), jsonUrl, projectsSummary(json));
		    System.out.println(hitMsg);
		    LOGGER.info(hitMsg);
		} else {
		    withoutProjects++;
		}
	    } catch (Exception e) {
		failedJson++;
		LOGGER.warn("Skip (error): {} — {}", jsonUrl, e.getMessage());
	    }

	    int done = idx + 1;
	    boolean last = done == totalToCheck;
	    if (last || done % logEvery == 0) {
		int pct = totalToCheck == 0 ? 100 : (int) (100L * done / totalToCheck);
		int successful = withProjects + withoutProjects;
		double shareSoFar = successful > 0 ? (100.0 * withProjects / successful) : 0;
		LOGGER.info(
			"Progress: {}% ({}/{}) — non-empty projects so far: {} ({}% of successful fetches), failures: {}",
			pct, done, totalToCheck, withProjects, String.format(Locale.US, "%.2f", shareSoFar), failedJson);
	    }
	}

	int attempted = withProjects + withoutProjects;
	int totalListed = datasetUris.size();
	System.out.println("Datasets in catalog (distinct): " + totalListed);
	System.out.println("Datasets checked: " + attempted + " (failed fetches: " + failedJson + ")");
	if (attempted > 0) {
	    double pct = 100.0 * withProjects / attempted;
	    System.out.printf("With non-empty \"projects\": %d (%.2f%%)%n", withProjects, pct);
	    System.out.printf("With null / empty / missing \"projects\": %d (%.2f%%)%n", withoutProjects,
		    100.0 * withoutProjects / attempted);
	}
    }

    static Set<String> extractDatasetUris(String turtle) {
	Set<String> out = new LinkedHashSet<>();
	Model model = ModelFactory.createDefaultModel();
	model.read(new ByteArrayInputStream(turtle.getBytes(StandardCharsets.UTF_8)), null, "TTL");
	Property dataset = model.createProperty(DCAT_NS, "dataset");
	StmtIterator it = model.listStatements(null, dataset, (RDFNode) null);
	while (it.hasNext()) {
	    Statement s = it.next();
	    if (s.getObject().isResource()) {
		out.add(s.getObject().asResource().getURI());
	    }
	}
	model.close();
	return out;
    }

    static boolean hasNonEmptyProjects(JSONObject json) {
	if (!json.has("projects") || json.isNull("projects")) {
	    return false;
	}
	JSONArray arr = json.optJSONArray("projects");
	if (arr != null) {
	    return arr.length() > 0;
	}
	return json.optJSONObject("projects") != null;
    }

    static String datasetIdForLog(String datasetUri, JSONObject json) {
	JSONObject rec = json.optJSONObject("datasetrec");
	if (rec != null && !rec.isNull("DasID")) {
	    Object id = rec.opt("DasID");
	    if (id != null && !JSONObject.NULL.equals(id)) {
		return String.valueOf(id);
	    }
	}
	String uri = datasetUri.endsWith(".json") ? datasetUri.substring(0, datasetUri.length() - 5) : datasetUri;
	int slash = uri.lastIndexOf('/');
	return slash >= 0 ? uri.substring(slash + 1) : uri;
    }

    static String projectsSummary(JSONObject json) {
	JSONArray arr = json.optJSONArray("projects");
	if (arr == null) {
	    return "projects: (object)";
	}
	return "project count: " + arr.length();
    }

    private static String httpGetString(HttpClient client, String url) throws Exception {
	HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).timeout(TIMEOUT).GET().build();
	HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
	if (res.statusCode() < 200 || res.statusCode() >= 300) {
	    throw new IllegalStateException("HTTP " + res.statusCode());
	}
	return res.body();
    }

    private MarineinfoDatasetProjectsSurvey() {
    }
}
