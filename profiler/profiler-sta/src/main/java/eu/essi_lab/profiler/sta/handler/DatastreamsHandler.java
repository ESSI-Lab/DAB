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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.resource.stax.GIResourceParser;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.sta.DatastreamsTransformer;
import eu.essi_lab.profiler.sta.ExpandSubRequest;
import eu.essi_lab.profiler.sta.ObservedPropertiesTransformer;
import eu.essi_lab.profiler.sta.STAResourceMapper;
import eu.essi_lab.profiler.sta.STAJsonWriter;
import eu.essi_lab.profiler.sta.STARequest;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

/**
 * Handler for OGC STA Datastreams entity set (timeseries records).
 * Also handles ObservedProperties(id)/Datastreams navigation (filters by UNIQUE_ATTRIBUTE_IDENTIFIER).
 */
public class DatastreamsHandler extends StreamingRequestHandler {

    private DatabaseExecutor executor;

    public DatastreamsHandler() {
	try {
	    eu.essi_lab.model.StorageInfo uri = ConfigurationWrapper.getStorageInfo();
	    executor = DatabaseProviderFactory.getExecutor(uri);
	} catch (GSException e) {
	}
    }

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
		writeDatastreamsResponse(output, webRequest);
	    } catch (Exception e) {
		throw new WebApplicationException("Error handling Datastreams request", e);
	    }
	};
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_JSON_TYPE;
    }

    public DiscoveryRequestTransformer getTransformer() {
	return new DatastreamsTransformer();
    }

    protected void writeDatastreamsResponse(OutputStream output, WebRequest webRequest) throws Exception {
	STARequest staRequest = new STARequest(webRequest);
	if (staRequest.getEntitySet().orElse(null) == STARequest.EntitySet.ObservedProperties
		&& "Datastreams".equals(staRequest.getNavigationProperty().orElse(null))) {
	    String entityId = staRequest.getEntityIdNormalized().orElse(null);
	    if (entityId != null && entityId.matches("\\d+")) {
		String attributeCode = resolveObservedPropertyIdToAttributeCode(webRequest, entityId);
		if (attributeCode == null) {
		    output.write("{\"value\":[]}".getBytes(StandardCharsets.UTF_8));
		    return;
		}
		webRequest.getServletRequest().setAttribute(DatastreamsTransformer.ATTR_OBSERVED_PROPERTY_CODE,
			attributeCode);
	    }
	}

	DiscoveryRequestTransformer transformer = getTransformer();
	DiscoveryMessage message = transformer.transform(webRequest);

	ResultSet<String> resultSet = exec(message);
	List<JSONObject> datastreams = new ArrayList<>();
	String baseUrl = STAJsonWriter.buildBaseUrl(webRequest.getServletRequest().getRequestURL().toString());

	if (resultSet != null) {
	    for (String result : resultSet.getResultsList()) {
		try {
		    GIResourceParser parser = new GIResourceParser(result);
		    JSONObject ds = STAResourceMapper.datastreamFromParser(parser, baseUrl);
		    if (ds == null) {
			continue;
		    }
		    String id = ds.getString("@iot.id");
		    String platformId = ds.getJSONObject("properties").optString("platformId", null);
		    String begin = parser.getTmpExtentBegin();
		    String end = parser.getTmpExtentEnd();
		    String endNow = parser.getTmpExtentEndNow();
		    addExpandedObservedProperty(ds, parser, baseUrl, staRequest);
		    addExpandedObservations(ds, id, platformId, begin, end, endNow, webRequest, message, baseUrl, staRequest);
		    datastreams.add(ds);
		} catch (XMLStreamException | IOException e) {
		}
	    }
	}

	if (staRequest.getEntitySet().orElse(null) == STARequest.EntitySet.Datastreams
		&& staRequest.getEntityId().isPresent()) {
	    if (datastreams.isEmpty()) {
		throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
	    }
	    JSONObject singleDs = datastreams.get(0);
	    String dsId = singleDs.getString("@iot.id");
	    String platformId = singleDs.getJSONObject("properties").optString("platformId", null);
	    String begin = null;
	    String end = null;
	    String endNow = null;
	    String pt = singleDs.optString("phenomenonTime", "");
	    if (pt.contains("/")) {
		int slash = pt.indexOf('/');
		begin = pt.substring(0, slash).trim();
		end = pt.substring(slash + 1).trim();
	    } else if (!pt.isEmpty()) {
		begin = pt;
		end = pt;
	    }
	    addExpandedObservations(singleDs, dsId, platformId, begin, end, endNow, webRequest, message, baseUrl, staRequest);
	    output.write(singleDs.toString().getBytes(StandardCharsets.UTF_8));
	    return;
	}

	Integer count = null;
	if (resultSet != null && resultSet.getCountResponse() != null) {
	    count = resultSet.getCountResponse().getCount();
	}

	String nextLink = null;
	int pageSize = staRequest.getTop() != null ? staRequest.getTop() : 100;
	boolean hasFullPage = datastreams.size() >= pageSize;
	if (hasFullPage && resultSet != null && resultSet.getSearchAfter().isPresent()) {
	    String token = resultSet.getSearchAfter().get().getValues()
		    .map(v -> v.isEmpty() ? null : v.get(0).toString())
		    .orElse(null);
	    if (token != null) {
		String entityPath = "Datastreams";
		if (staRequest.getEntitySet().orElse(null) == STARequest.EntitySet.Things
			&& staRequest.getEntityIdNormalized().isPresent()) {
		    entityPath = "Things(" + staRequest.getEntityIdNormalized().get() + ")/Datastreams";
		} else if (staRequest.getEntitySet().orElse(null) == STARequest.EntitySet.ObservedProperties
			&& staRequest.getEntityIdNormalized().isPresent()
			&& "Datastreams".equals(staRequest.getNavigationProperty().orElse(null))) {
		    entityPath = "ObservedProperties(" + staRequest.getEntityIdNormalized().get() + ")/Datastreams";
		}
		nextLink = STAJsonWriter.buildNextLink(baseUrl, entityPath, token, webRequest.getQueryString());
	    }
	}

	String json = STAJsonWriter.collectionResponse(datastreams, nextLink, count);
	output.write(json.getBytes(StandardCharsets.UTF_8));
    }

    private String resolveObservedPropertyIdToAttributeCode(WebRequest webRequest, String numericId) {
	if (executor == null) {
	    return null;
	}
	try {
	    long targetId = Long.parseLong(numericId);
	    DiscoveryRequestTransformer transformer = new ObservedPropertiesTransformer();
	    DiscoveryMessage message = transformer.transform(webRequest);
	    ResultSet<String> resultSet = executor.discoverDistinctStrings(message);
	    if (resultSet == null) {
		return null;
	    }
	    for (String result : resultSet.getResultsList()) {
		try {
		    GIResourceParser parser = new GIResourceParser(result);
		    String uniqueAttributeId = parser.getAttributeCode();
		    if (uniqueAttributeId != null && !uniqueAttributeId.isEmpty()) {
			long id = STAResourceMapper.observedPropertyId(uniqueAttributeId);
			if (id == targetId) {
			    return uniqueAttributeId;
			}
		    }
		} catch (Exception e) {
		    // skip
		}
	    }
	} catch (Exception e) {
	    // ignore
	}
	return null;
    }

    private void addExpandedObservedProperty(JSONObject datastream, GIResourceParser parser, String baseUrl,
	    STARequest staRequest) {
	boolean expandOp = staRequest.getExpandOptions().stream()
		.anyMatch(o -> "ObservedProperty".equals(o.getProperty()));
	if (!expandOp) {
	    return;
	}
	JSONObject op = STAResourceMapper.observedPropertyFromParser(parser, baseUrl);
	if (op != null) {
	    datastream.put("ObservedProperty", op);
	}
    }

    private void addExpandedObservations(JSONObject datastream, String datastreamId, String platformId,
	    String beginStr, String endStr, String endNowStr, WebRequest webRequest, DiscoveryMessage message,
	    String baseUrl, STARequest staRequest) {
	boolean expandObs = staRequest.getExpandOptions().stream()
		.anyMatch(o -> "Observations".equals(o.getProperty()));
	if (!expandObs) {
	    return;
	}
	STARequest.ExpandOption obsOpt = staRequest.getExpandOptions().stream()
		.filter(o -> "Observations".equals(o.getProperty()))
		.findFirst()
		.orElse(null);
	if (obsOpt == null) {
	    return;
	}
	int top = obsOpt.getTop() != null ? obsOpt.getTop() : 100;
	String filter = obsOpt.getFilter();
	String orderBy = obsOpt.getOrderBy();

	java.util.Date windowBeginDate;
	java.util.Date windowEndDate;
	String[] range = STARequest.ExpandOption.parsePhenomenonTimeRange(filter);
	if (range != null && range.length == 2) {
	    Optional<java.util.Date> geOpt = ISO8601DateTimeUtils.parseISO8601ToDate(range[0]);
	    Optional<java.util.Date> leOpt = ISO8601DateTimeUtils.parseISO8601ToDate(range[1]);
	    if (geOpt.isPresent() && leOpt.isPresent()) {
		windowBeginDate = geOpt.get();
		windowEndDate = leOpt.get();
	    } else {
		windowBeginDate = null;
		windowEndDate = null;
	    }
	} else {
	    if (endNowStr != null && "true".equals(endNowStr)) {
		endStr = ISO8601DateTimeUtils.getISO8601DateTime();
	    }
	    if (beginStr == null || endStr == null) {
		datastream.put("Observations", new JSONArray());
		return;
	    }
	    Optional<java.util.Date> beginOpt = ISO8601DateTimeUtils.parseISO8601ToDate(beginStr);
	    Optional<java.util.Date> endOpt = ISO8601DateTimeUtils.parseISO8601ToDate(endStr);
	    if (!beginOpt.isPresent() || !endOpt.isPresent()) {
		datastream.put("Observations", new JSONArray());
		return;
	    }
	    ZonedDateTime phenomenonStart = ZonedDateTime.ofInstant(beginOpt.get().toInstant(), ZoneOffset.UTC);
	    ZonedDateTime phenomenonEnd = ZonedDateTime.ofInstant(endOpt.get().toInstant(), ZoneOffset.UTC);
	    ZonedDateTime windowEnd = phenomenonEnd;
	    ZonedDateTime windowBegin = phenomenonEnd.minusMonths(2);
	    if (windowBegin.isBefore(phenomenonStart)) {
		windowBegin = phenomenonStart;
	    }
	    windowBeginDate = java.util.Date.from(windowBegin.toInstant());
	    windowEndDate = java.util.Date.from(windowEnd.toInstant());
	}
	if (windowBeginDate == null || windowEndDate == null) {
	    datastream.put("Observations", new JSONArray());
	    return;
	}

	try {
	    DatastreamsObservationsHandler dsHandler = new DatastreamsObservationsHandler();
	    List<JSONObject> obs = dsHandler.fetchObservationsForDatastream(
		    webRequest, message, datastreamId, platformId, windowBeginDate, windowEndDate, baseUrl);

	    boolean orderDesc = orderBy != null && orderBy.toLowerCase().contains("phenomenontime")
		    && orderBy.toLowerCase().contains("desc");
	    if (orderDesc) {
		obs.sort(Comparator.comparing((JSONObject o) -> o.optString("phenomenonTime", ""))
			.reversed());
	    }
	    if (obs.size() > top) {
		obs = obs.subList(0, top);
	    }
	    JSONArray arr = new JSONArray();
	    for (JSONObject o : obs) {
		arr.put(o);
	    }
	    datastream.put("Observations", arr);
	} catch (Exception e) {
	    datastream.put("Observations", new JSONArray());
	}
    }
}
