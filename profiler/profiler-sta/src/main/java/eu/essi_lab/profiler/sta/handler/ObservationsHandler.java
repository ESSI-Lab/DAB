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

package eu.essi_lab.profiler.sta.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.resource.stax.GIResourceParser;
import org.json.JSONObject;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.sta.ObservationsWithDataTransformer;
import eu.essi_lab.profiler.sta.STAJsonWriter;
import eu.essi_lab.profiler.sta.STARequest;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

/**
 * Handler for OGC STA Observations entity set.
 * Retrieves real observation values by fetching datastreams in blocks and delegating
 * to DatastreamsObservationsHandler logic for each datastream (first 2-month chunk per datastream).
 */
public class ObservationsHandler extends StreamingRequestHandler {

    private static final int PAGINATION_MONTHS = 2;

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage vm = new ValidationMessage();
	vm.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return vm;
    }

    @Override
    public StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException {
	return output -> {
	    try {
		writeObservationsResponse(output, webRequest);
	    } catch (Exception e) {
		throw new WebApplicationException("Error handling Observations request", e);
	    }
	};
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_JSON_TYPE;
    }

    public DiscoveryRequestTransformer getTransformer() {
	return new ObservationsWithDataTransformer();
    }

    private void writeObservationsResponse(OutputStream output, WebRequest webRequest) throws Exception {
	DiscoveryRequestTransformer transformer = getTransformer();
	DiscoveryMessage message = transformer.transform(webRequest);

	ResultSet<String> resultSet = exec(message);
	List<JSONObject> observations = new ArrayList<>();
	String baseUrl = STAJsonWriter.buildBaseUrl(webRequest.getServletRequest().getRequestURL().toString());

	if (resultSet != null) {
	    for (String result : resultSet.getResultsList()) {
		try {
		    GIResourceParser parser = new GIResourceParser(result);
		    String datastreamId = parser.getOnlineId();
		    if (datastreamId == null) {
			continue;
		    }
		    String platformId = parser.getUniquePlatformCode();
		    String beginStr = parser.getTmpExtentBegin();
		    String endStr = parser.getTmpExtentEnd();
		    String endNow = parser.getTmpExtentEndNow();
		    if (endNow != null && "true".equals(endNow)) {
			endStr = ISO8601DateTimeUtils.getISO8601DateTime();
		    }
		    if (beginStr == null || endStr == null) {
			continue;
		    }
		    Optional<java.util.Date> beginOpt = ISO8601DateTimeUtils.parseISO8601ToDate(beginStr);
		    Optional<java.util.Date> endOpt = ISO8601DateTimeUtils.parseISO8601ToDate(endStr);
		    if (!beginOpt.isPresent() || !endOpt.isPresent()) {
			continue;
		    }
		    ZonedDateTime phenomenonEnd = ZonedDateTime.ofInstant(endOpt.get().toInstant(), ZoneOffset.UTC);
		    ZonedDateTime phenomenonStart = ZonedDateTime.ofInstant(beginOpt.get().toInstant(), ZoneOffset.UTC);
		    ZonedDateTime windowEnd = phenomenonEnd;
		    ZonedDateTime windowBegin = phenomenonEnd.minusMonths(PAGINATION_MONTHS);
		    if (windowBegin.isBefore(phenomenonStart)) {
			windowBegin = phenomenonStart;
		    }
		    java.util.Date windowBeginDate = java.util.Date.from(windowBegin.toInstant());
		    java.util.Date windowEndDate = java.util.Date.from(windowEnd.toInstant());

		    DatastreamsHandler dsHandler = new DatastreamsHandler();
		    List<JSONObject> obs = dsHandler.fetchObservationsForDatastream(
			    webRequest, message, datastreamId, platformId, windowBeginDate, windowEndDate, baseUrl);
		    observations.addAll(obs);
		} catch (XMLStreamException | IOException e) {
		    // skip malformed
		}
	    }
	}

	STARequest staRequest = new STARequest(webRequest);
	if (staRequest.getEntityId().isPresent()) {
	    if (observations.isEmpty()) {
		throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
	    }
	    output.write(STAJsonWriter.collectionResponse(observations, null, null).getBytes(StandardCharsets.UTF_8));
	    return;
	}

	Integer count = null;
	if (resultSet != null && resultSet.getCountResponse() != null) {
	    count = resultSet.getCountResponse().getCount();
	}

	String nextLink = null;
	if (resultSet != null && resultSet.getSearchAfter().isPresent()) {
	    String token = resultSet.getSearchAfter().get().getValues()
		    .map(v -> v.isEmpty() ? null : v.get(0).toString())
		    .orElse(null);
	    if (token != null) {
		nextLink = STAJsonWriter.buildNextLink(baseUrl, "Observations", token, webRequest.getQueryString());
	    }
	}

	String json = STAJsonWriter.collectionResponse(observations, nextLink, count);
	output.write(json.getBytes(StandardCharsets.UTF_8));
    }
}
