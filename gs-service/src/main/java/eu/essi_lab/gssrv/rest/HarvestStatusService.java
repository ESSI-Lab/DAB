package eu.essi_lab.gssrv.rest;

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

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opensearch.client.ResponseException;
import org.slf4j.Logger;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.gssrv.conf.AdminAuthorization;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.HostNamePropertyUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.shared.driver.es.stats.ElasticsearchClient;
import eu.essi_lab.shared.driver.es.stats.ElasticsearchHarvestingPublisher;
import jakarta.jws.WebService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Provides the harvesting executions overlapping a time window, read from the {@code {dbName}-harvests} index of the "DAB
 * statistics gathering" database (see {@link ElasticsearchHarvestingPublisher}).<br>
 * Each execution is returned with a status key:
 * <ul>
 * <li><code>running</code>: result RUNNING, started less than {@value #STALE_HOURS} hours ago</li>
 * <li><code>stale</code>: result RUNNING, started more than {@value #STALE_HOURS} hours ago; most likely the process died and
 * the harvesting has not been continued yet</li>
 * <li><code>interrupted</code>: the process died and the harvesting has been continued by another execution</li>
 * <li><code>completed</code>, <code>consolidated</code>, <code>failed</code>, <code>canceled</code>: final results</li>
 * </ul>
 * The optional <code>source</code> parameter filters the executions by source: it matches (case insensitive) the source
 * identifiers containing it, and the source labels containing words starting with it.<br>
 * All the operations are restricted to the DAB administrators, authorized with the same rule of the configurator (see
 * {@link AdminAuthorization}): the user must be logged in with the configured OAuth 2.0 provider. When the user is not
 * logged in, the response (status 401) provides the <code>provider</code> to use for the login.
 *
 * @author boldrini
 */
@WebService
@Path("/")
public class HarvestStatusService {

    private static final Logger LOGGER = GSLoggerFactory.getLogger(HarvestStatusService.class);

    private static final long DEFAULT_HOURS = 24;
    private static final long STALE_HOURS = 24;

    /**
     * Maximum number of returned executions (default OpenSearch max result window)
     */
    private static final int MAX_RESULTS = 10000;

    /**
     * Status keys, in the order used for the summary
     */
    /**
     * Maximum number of executions deleted with a single request
     */
    private static final int MAX_DELETIONS = 1000;

    private static final String[] STATUS_KEYS = { "running", "stale", "interrupted", "completed", "consolidated", "failed",
	    "canceled", "unknown" };

    /**
     * @param request
     * @return the executions overlapping the requested time window
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/harvest-status")
    public Response harvestStatus(@Context HttpServletRequest httpRequest, HarvestStatusRequest request) {

	Optional<Response> denied = checkAdmin(httpRequest);

	if (denied.isPresent()) {

	    return denied.get();
	}

	return search(request.getHours(), request.getFrom(), request.getTo(), request.getSource(),
		AdminAuthorization.findEmail(httpRequest).orElse(null));
    }

    /**
     * Deletes the given executions, provided that they are still marked as running: they are the executions whose
     * process died without being continued, or whose end has not been recorded. Executions with a final result are never
     * deleted
     *
     * @param request
     * @return the number of deleted executions
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/harvest-status/delete")
    public Response deleteHarvests(@Context HttpServletRequest httpRequest, HarvestDeleteRequest request) {

	Optional<Response> denied = checkAdmin(httpRequest);

	if (denied.isPresent()) {

	    return denied.get();
	}

	List<String> ids = request.getHarvestingIds() == null ? List.of()
		: request.getHarvestingIds().stream().filter(id -> id != null && !id.isBlank()).distinct().toList();

	if (ids.isEmpty() || ids.size() > MAX_DELETIONS) {

	    return Response.status(Status.BAD_REQUEST)
		    .entity(errorResponse("Between 1 and " + MAX_DELETIONS + " harvesting identifiers are required").toString()).build();
	}

	Optional<ElasticsearchClient> client = ElasticsearchHarvestingPublisher.createClient();

	if (client.isEmpty()) {

	    return Response.serverError()
		    .entity(errorResponse("Harvesting statistics not available: the DAB statistics gathering is disabled").toString()).build();
	}

	JSONArray filter = new JSONArray();
	filter.put(new JSONObject().put("ids", new JSONObject().put("values", new JSONArray(ids))));
	filter.put(new JSONObject().put("term", new JSONObject().put("result", "RUNNING")));

	JSONObject query = new JSONObject().put("bool", new JSONObject().put("filter", filter));

	try {

	    long deleted = client.get().deleteByQuery(ElasticsearchHarvestingPublisher.HARVESTS_INDEX, query);

	    LOGGER.warn("harvest-status: {} deleted {} of the {} requested running harvests: {}",
		    AdminAuthorization.findEmail(httpRequest).orElse("administrator"), deleted, ids.size(), ids);

	    JSONObject output = new JSONObject();
	    output.put("status", "success");
	    output.put("requested", ids.size());
	    output.put("deleted", deleted);

	    return Response.ok(output.toString(), MediaType.APPLICATION_JSON).build();

	} catch (Exception ex) {

	    LOGGER.error("harvest-status deletion FAILED: {}", ex.getMessage(), ex);

	    return Response.serverError().entity(errorResponse("Deletion failed: " + ex.getMessage()).toString()).build();
	}
    }

    /**
     * @param httpRequest
     * @return an error response if the user is not logged in (401), or is not an administrator (403)
     */
    private Optional<Response> checkAdmin(HttpServletRequest httpRequest) {

	switch (AdminAuthorization.check(httpRequest)) {
	case AUTHORIZED:
	    return Optional.empty();

	case NOT_AUTHENTICATED: {

	    JSONObject error = errorResponse("Login required");
	    error.put("provider", ConfigurationWrapper.getOAuthSetting().getSelectedProvider().getProviderName());

	    return Optional.of(Response.status(Status.UNAUTHORIZED).entity(error.toString()).type(MediaType.APPLICATION_JSON).build());
	}

	case NOT_ADMIN: {

	    String email = AdminAuthorization.findEmail(httpRequest).orElse("");

	    LOGGER.warn("harvest-status: access denied to non administrator {}", email);

	    JSONObject error = errorResponse("The account " + email + " is not an administrator");
	    error.put("provider", ConfigurationWrapper.getOAuthSetting().getSelectedProvider().getProviderName());

	    return Optional.of(Response.status(Status.FORBIDDEN).entity(error.toString()).type(MediaType.APPLICATION_JSON).build());
	}

	default:
	    return Optional.of(Response.serverError().entity(errorResponse("Unable to identify the user").toString())
		    .type(MediaType.APPLICATION_JSON).build());
	}
    }

    /**
     * @param hoursParam
     * @param fromParam
     * @param toParam
     * @param sourceParam
     * @param user the e-mail of the logged administrator, if any
     * @return
     */
    private Response search(Long hoursParam, String fromParam, String toParam, String sourceParam, String user) {

	long requestStartMs = System.currentTimeMillis();

	try {

	    //
	    // time window
	    //

	    long nowMs = System.currentTimeMillis();
	    long hours = hoursParam != null && hoursParam > 0 ? hoursParam : DEFAULT_HOURS;
	    long windowEndMs = nowMs;
	    long windowStartMs = windowEndMs - TimeUnit.HOURS.toMillis(hours);

	    if (fromParam != null && !fromParam.isBlank() && toParam != null && !toParam.isBlank()) {

		Optional<Date> fromDate = ISO8601DateTimeUtils.parseISO8601ToDate(fromParam.trim());
		Optional<Date> toDate = ISO8601DateTimeUtils.parseISO8601ToDate(toParam.trim());

		if (fromDate.isPresent() && toDate.isPresent() && fromDate.get().before(toDate.get())) {

		    windowStartMs = fromDate.get().getTime();
		    windowEndMs = toDate.get().getTime();
		    hours = Math.max(1, (windowEndMs - windowStartMs) / TimeUnit.HOURS.toMillis(1));
		}
	    }

	    JSONObject output = new JSONObject();

	    JSONObject window = new JSONObject();
	    window.put("hours", hours);
	    window.put("startMs", windowStartMs);
	    window.put("endMs", windowEndMs);
	    window.put("startIso", ISO8601DateTimeUtils.getISO8601DateTime(new Date(windowStartMs)));
	    window.put("endIso", ISO8601DateTimeUtils.getISO8601DateTime(new Date(windowEndMs)));
	    window.put("staleHours", STALE_HOURS);
	    output.put("window", window);

	    //
	    // search
	    //

	    Optional<ElasticsearchClient> optClient = ElasticsearchHarvestingPublisher.createClient();

	    if (optClient.isEmpty()) {

		return Response.ok(errorResponse("Harvesting statistics not available: the DAB statistics gathering is disabled").toString(),
			MediaType.APPLICATION_JSON).build();
	    }

	    ElasticsearchClient client = optClient.get();

	    JSONObject response;

	    long searchStartMs = System.currentTimeMillis();

	    JSONObject query = buildQuery(window.getString("startIso"), window.getString("endIso"), sourceParam);

	    LOGGER.info("harvest-status searching index {}-{}: {}", client.getDbName(), ElasticsearchHarvestingPublisher.HARVESTS_INDEX,
		    query);

	    try {

		response = client.search(ElasticsearchHarvestingPublisher.HARVESTS_INDEX, query);

	    } catch (ResponseException ex) {

		if (ex.getResponse().getStatusLine().getStatusCode() != 404) {

		    LOGGER.error("harvest-status search failed: {}", ex.getMessage(), ex);

		    return Response.serverError().entity(errorResponse("Search of the harvests index failed: " + ex.getMessage()).toString())
			    .build();
		}

		// the index does not exist yet, since no harvesting started after the statistics were enabled
		LOGGER.warn("harvest-status: index {}-{} not found", client.getDbName(), ElasticsearchHarvestingPublisher.HARVESTS_INDEX);

		response = new JSONObject().put("hits", new JSONObject().put("hits", new JSONArray()));
	    }

	    LOGGER.info("harvest-status found {} harvests",
		    response.getJSONObject("hits").optJSONObject("total") != null
			    ? response.getJSONObject("hits").getJSONObject("total").opt("value")
			    : response.getJSONObject("hits").getJSONArray("hits").length());

	    long searchMs = System.currentTimeMillis() - searchStartMs;

	    //
	    // harvests
	    //

	    JSONObject hits = response.getJSONObject("hits");
	    JSONArray hitsArray = hits.getJSONArray("hits");

	    Map<String, Integer> counts = new LinkedHashMap<>();
	    for (String key : STATUS_KEYS) {
		counts.put(key, 0);
	    }

	    JSONArray harvests = new JSONArray();

	    for (int i = 0; i < hitsArray.length(); i++) {

		JSONObject harvest = toHarvest(hitsArray.getJSONObject(i).getJSONObject("_source"), nowMs);

		counts.merge(harvest.getString("statusKey"), 1, Integer::sum);

		harvests.put(harvest);
	    }

	    long total = hits.optJSONObject("total") != null ? hits.getJSONObject("total").optLong("value", harvests.length())
		    : harvests.length();

	    JSONObject summary = new JSONObject();
	    summary.put("total", harvests.length());
	    summary.put("truncated", total > harvests.length());
	    counts.forEach(summary::put);

	    output.put("status", "success");
	    output.put("user", user);
	    output.put("summary", summary);
	    output.put("harvests", harvests);

	    JSONObject timing = new JSONObject();
	    timing.put("searchMs", searchMs);
	    timing.put("totalMs", System.currentTimeMillis() - requestStartMs);
	    output.put("timing", timing);

	    LOGGER.info("harvest-status returned {} harvests in {} ms", harvests.length(), timing.getLong("totalMs"));

	    return Response.ok(output.toString(), MediaType.APPLICATION_JSON).build();

	} catch (Exception e) {

	    LOGGER.error("harvest-status FAILED: {}", e.getMessage(), e);

	    return Response.serverError().entity(errorResponse(e.getMessage()).toString()).build();
	}
    }

    /**
     * Executions overlapping the window: started before the window end, and ended (or interrupted) after the window start,
     * or still running
     *
     * @param startIso
     * @param endIso
     * @param source optional source filter
     * @return
     */
    private JSONObject buildQuery(String startIso, String endIso, String source) {

	JSONArray should = new JSONArray();
	should.put(range("endDate", "gte", startIso));
	should.put(range("interruptionDetectedDate", "gte", startIso));
	should.put(new JSONObject().put("term", new JSONObject().put("result", "RUNNING")));

	JSONArray filter = new JSONArray();
	filter.put(range("startDate", "lte", endIso));
	filter.put(new JSONObject().put("bool", new JSONObject().put("should", should).put("minimum_should_match", 1)));

	if (source != null && !source.isBlank()) {

	    filter.put(sourceFilter(source.trim()));
	}

	JSONObject body = new JSONObject();
	body.put("size", MAX_RESULTS);
	body.put("track_total_hits", true);
	body.put("sort", new JSONArray().put(new JSONObject().put("startDate", "asc")));
	body.put("query", new JSONObject().put("bool", new JSONObject().put("filter", filter)));

	return body;
    }

    /**
     * @param source
     * @return a filter matching the source identifiers containing <code>source</code>, or the source labels with words
     *         starting with <code>source</code> (case insensitive)
     */
    private JSONObject sourceFilter(String source) {

	String escaped = source.replace("\\", "\\\\").replace("*", "\\*").replace("?", "\\?");

	JSONObject wildcard = new JSONObject().put("value", "*" + escaped + "*").put("case_insensitive", true);

	JSONArray should = new JSONArray();
	should.put(new JSONObject().put("wildcard", new JSONObject().put("sourceId", wildcard)));
	should.put(new JSONObject().put("match_phrase_prefix", new JSONObject().put("sourceLabel", source)));

	return new JSONObject().put("bool", new JSONObject().put("should", should).put("minimum_should_match", 1));
    }

    /**
     * @param field
     * @param operator
     * @param value
     * @return
     */
    private JSONObject range(String field, String operator, String value) {

	return new JSONObject().put("range", new JSONObject().put(field, new JSONObject().put(operator, value)));
    }

    /**
     * @param doc
     * @param nowMs
     * @return the given document with the computed fields <code>statusKey</code>, <code>startMs</code>,
     *         <code>endMs</code> and <code>awsTaskLogsUrl</code>
     */
    private JSONObject toHarvest(JSONObject doc, long nowMs) {

	String result = doc.optString("result", "");

	long startMs = toMillis(doc.optString("startDate", null)).orElse(nowMs);

	String statusKey;
	Optional<Long> endMs;

	switch (result) {
	case "RUNNING" -> {
	    statusKey = nowMs - startMs > TimeUnit.HOURS.toMillis(STALE_HOURS) ? "stale" : "running";
	    endMs = Optional.of(nowMs);
	}
	case "INTERRUPTED" -> {
	    // the actual interruption time is unknown: the bar ends when the interruption has been detected
	    statusKey = "interrupted";
	    endMs = toMillis(doc.optString("interruptionDetectedDate", null));
	}
	case "COMPLETED" -> {
	    statusKey = "completed";
	    endMs = toMillis(doc.optString("endDate", null));
	}
	case "CONSOLIDATED_FOLDER_SURVIVED" -> {
	    statusKey = "consolidated";
	    endMs = toMillis(doc.optString("endDate", null));
	}
	case "FAILED" -> {
	    statusKey = "failed";
	    endMs = toMillis(doc.optString("endDate", null));
	}
	case "CANCELED" -> {
	    statusKey = "canceled";
	    endMs = toMillis(doc.optString("endDate", null));
	}
	default -> {
	    statusKey = "unknown";
	    endMs = toMillis(doc.optString("endDate", null));
	}
	}

	doc.put("statusKey", statusKey);
	doc.put("startMs", startMs);
	doc.put("endMs", Math.max(startMs, endMs.orElse(startMs)));

	String awsTaskId = doc.optString("awsTaskId", "");

	if (!awsTaskId.isBlank()) {

	    HostNamePropertyUtils.getEcsTaskLogsUrl(awsTaskId).ifPresent(url -> doc.put("awsTaskLogsUrl", url));
	}

	return doc;
    }

    /**
     * @param iso
     * @return
     */
    private Optional<Long> toMillis(String iso) {

	if (iso == null || iso.isBlank()) {

	    return Optional.empty();
	}

	return ISO8601DateTimeUtils.parseISO8601ToDate(iso).map(Date::getTime);
    }

    /**
     * @param message
     * @return
     */
    private JSONObject errorResponse(String message) {

	JSONObject ret = new JSONObject();
	ret.put("status", "error");
	ret.put("message", message == null ? "Unknown error" : message);

	return ret;
    }
}
