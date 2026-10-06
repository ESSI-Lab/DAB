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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.HostNamePropertyUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.shared.driver.es.stats.ElasticsearchClient;
import eu.essi_lab.shared.driver.es.stats.ElasticsearchHarvestingPublisher;
import jakarta.jws.WebService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Provides the harvesting executions overlapping a time window, read from the {@code {dbName}-harvests} index of the "DAB
 * statistics gathering" database (see {@link ElasticsearchHarvestingPublisher}).<br>
 * Each execution is returned with a status key:
 * <ul>
 * <li><code>running</code>: result RUNNING, started less than <code>staleHours</code> ago</li>
 * <li><code>stale</code>: result RUNNING, started more than <code>staleHours</code> ago; most likely the process died and
 * the harvesting has not been continued yet</li>
 * <li><code>interrupted</code>: the process died and the harvesting has been continued by another execution</li>
 * <li><code>completed</code>, <code>consolidated</code>, <code>failed</code>, <code>canceled</code>: final results</li>
 * </ul>
 *
 * @author boldrini
 */
@WebService
@Path("/")
public class HarvestStatusService {

    private static final Logger LOGGER = GSLoggerFactory.getLogger(HarvestStatusService.class);

    private static final long DEFAULT_HOURS = 24;
    private static final long DEFAULT_STALE_HOURS = 24;

    /**
     * Maximum number of returned executions (default OpenSearch max result window)
     */
    private static final int MAX_RESULTS = 10000;

    /**
     * Status keys, in the order used for the summary
     */
    private static final String[] STATUS_KEYS = { "running", "stale", "interrupted", "completed", "consolidated", "failed",
	    "canceled", "unknown" };

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("/harvest-status")
    public Response harvestStatus(//
	    @QueryParam("hours") Long hoursParam, //
	    @QueryParam("from") String fromParam, //
	    @QueryParam("to") String toParam, //
	    @QueryParam("sourceId") String sourceIdParam, //
	    @QueryParam("staleHours") Long staleHoursParam) {

	long requestStartMs = System.currentTimeMillis();

	ElasticsearchClient client = null;

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

	    long staleHours = staleHoursParam != null && staleHoursParam > 0 ? staleHoursParam : DEFAULT_STALE_HOURS;

	    JSONObject output = new JSONObject();

	    JSONObject window = new JSONObject();
	    window.put("hours", hours);
	    window.put("startMs", windowStartMs);
	    window.put("endMs", windowEndMs);
	    window.put("startIso", ISO8601DateTimeUtils.getISO8601DateTime(new Date(windowStartMs)));
	    window.put("endIso", ISO8601DateTimeUtils.getISO8601DateTime(new Date(windowEndMs)));
	    window.put("staleHours", staleHours);
	    output.put("window", window);

	    //
	    // search
	    //

	    Optional<ElasticsearchClient> optClient = ElasticsearchHarvestingPublisher.createClient();

	    if (optClient.isEmpty()) {

		return Response.ok(errorResponse("Harvesting statistics not available: the DAB statistics gathering is disabled").toString(),
			MediaType.APPLICATION_JSON).build();
	    }

	    client = optClient.get();

	    JSONObject response;

	    long searchStartMs = System.currentTimeMillis();

	    try {

		response = client.search(ElasticsearchHarvestingPublisher.HARVESTS_INDEX,
			buildQuery(window.getString("startIso"), window.getString("endIso"), sourceIdParam));

	    } catch (Exception ex) {

		// most likely the index does not exist yet, since no harvesting ended after the statistics were enabled
		LOGGER.warn("harvest-status search failed: {}", ex.getMessage());

		response = new JSONObject().put("hits", new JSONObject().put("hits", new JSONArray()));
	    }

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

		JSONObject harvest = toHarvest(hitsArray.getJSONObject(i).getJSONObject("_source"), nowMs, staleHours);

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

	} finally {

	    ElasticsearchHarvestingPublisher.close(client);
	}
    }

    /**
     * Executions overlapping the window: started before the window end, and ended (or interrupted) after the window start,
     * or still running
     *
     * @param startIso
     * @param endIso
     * @param sourceId
     * @return
     */
    private JSONObject buildQuery(String startIso, String endIso, String sourceId) {

	JSONArray should = new JSONArray();
	should.put(range("endDate", "gte", startIso));
	should.put(range("interruptionDetectedDate", "gte", startIso));
	should.put(new JSONObject().put("term", new JSONObject().put("result", "RUNNING")));

	JSONArray filter = new JSONArray();
	filter.put(range("startDate", "lte", endIso));
	filter.put(new JSONObject().put("bool", new JSONObject().put("should", should).put("minimum_should_match", 1)));

	if (sourceId != null && !sourceId.isBlank()) {

	    filter.put(new JSONObject().put("term", new JSONObject().put("sourceId", sourceId.trim())));
	}

	JSONObject body = new JSONObject();
	body.put("size", MAX_RESULTS);
	body.put("track_total_hits", true);
	body.put("sort", new JSONArray().put(new JSONObject().put("startDate", "asc")));
	body.put("query", new JSONObject().put("bool", new JSONObject().put("filter", filter)));

	return body;
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
     * @param staleHours
     * @return the given document with the computed fields <code>statusKey</code>, <code>startMs</code>,
     *         <code>endMs</code> and <code>awsTaskLogsUrl</code>
     */
    private JSONObject toHarvest(JSONObject doc, long nowMs, long staleHours) {

	String result = doc.optString("result", "");

	long startMs = toMillis(doc.optString("startDate", null)).orElse(nowMs);

	String statusKey;
	Optional<Long> endMs;

	switch (result) {
	case "RUNNING" -> {
	    statusKey = nowMs - startMs > TimeUnit.HOURS.toMillis(staleHours) ? "stale" : "running";
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
