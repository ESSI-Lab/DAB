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
import java.util.ArrayList;
import java.util.List;

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
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.sta.DatastreamsTransformer;
import eu.essi_lab.profiler.sta.ExpandSubRequest;
import eu.essi_lab.profiler.sta.STAJsonWriter;
import eu.essi_lab.profiler.sta.STARequest;
import eu.essi_lab.profiler.sta.ThingsTransformer;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

/**
 * Handler for OGC STA Things entity set.
 */
public class ThingsHandler extends StreamingRequestHandler {

    private DatabaseExecutor executor;

    public ThingsHandler() {
	try {
	    StorageInfo uri = ConfigurationWrapper.getStorageInfo();
	    executor = DatabaseProviderFactory.getExecutor(uri);
	} catch (GSException e) {
	    // executor may be null if DB not configured
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
		writeThingsResponse(output, webRequest);
	    } catch (Exception e) {
		throw new WebApplicationException("Error handling Things request", e);
	    }
	};
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_JSON_TYPE;
    }

    public DiscoveryRequestTransformer getTransformer() {
	return new ThingsTransformer();
    }

    private void writeThingsResponse(OutputStream output, WebRequest webRequest) throws Exception {
	if (executor == null) {
	    output.write("{\"value\":[]}".getBytes(StandardCharsets.UTF_8));
	    return;
	}

	DiscoveryRequestTransformer transformer = getTransformer();
	DiscoveryMessage message = transformer.transform(webRequest);

	ResultSet<String> resultSet = executor.discoverDistinctStrings(message);
	List<JSONObject> things = new ArrayList<>();
	String baseUrl = STAJsonWriter.buildBaseUrl(webRequest.getServletRequest().getRequestURL().toString());
	STARequest staRequest = new STARequest(webRequest);

	if (resultSet != null) {
	    for (String result : resultSet.getResultsList()) {
		try {
		    GIResourceParser parser = new GIResourceParser(result);
		    String id = parser.getUniquePlatformCode();
		    if (id == null) {
			continue;
		    }
		    String name = parser.getPlatformName();
		    if (name == null) {
			name = id;
		    }
		    JSONObject thing = STAJsonWriter.thing(id, name, "", baseUrl);
		    addExpandedDatastreams(thing, id, webRequest, baseUrl, staRequest);
		    addExpandedMultiDatastreams(thing, staRequest);
		    things.add(thing);
		} catch (XMLStreamException | IOException e) {
		    // skip malformed
		}
	    }
	}

	// Single object only for Things(id), not for Locations(id)/Things navigation
	boolean singleThing = staRequest.getEntitySet().orElse(null) == STARequest.EntitySet.Things
		&& staRequest.getEntityId().isPresent()
		&& !staRequest.getNavigationProperty().isPresent();
	if (singleThing) {
	    if (things.isEmpty()) {
		throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
	    }
	    JSONObject singleThingObj = things.get(0);
	    addExpandedDatastreams(singleThingObj, singleThingObj.getString("@iot.id"), webRequest, baseUrl, staRequest);
	    addExpandedMultiDatastreams(singleThingObj, staRequest);
	    output.write(singleThingObj.toString().getBytes(StandardCharsets.UTF_8));
	    return;
	}
	if (things.isEmpty() && staRequest.getEntityId().isPresent()) {
	    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
	}

	Integer count = null;
	if (resultSet != null && resultSet.getCountResponse() != null) {
	    count = resultSet.getCountResponse().getCount();
	}

	String nextLink = null;
	int pageSize = staRequest.getTop() != null ? staRequest.getTop() : 100;
	boolean hasFullPage = things.size() >= pageSize;
	if (hasFullPage && resultSet != null && resultSet.getSearchAfter().isPresent()) {
	    String token = resultSet.getSearchAfter().get().getValues()
		    .map(v -> v.isEmpty() ? null : v.get(0).toString())
		    .orElse(null);
	    if (token != null) {
		String entityPath = "Things";
		if (staRequest.getEntitySet().orElse(null) == STARequest.EntitySet.Locations
			&& staRequest.getEntityIdNormalized().isPresent()) {
		    entityPath = "Locations(" + staRequest.getEntityIdNormalized().get() + ")/Things";
		}
		nextLink = STAJsonWriter.buildNextLink(baseUrl, entityPath, token, webRequest.getQueryString());
	    }
	}

	String json = STAJsonWriter.collectionResponse(things, nextLink, count);
	output.write(json.getBytes(StandardCharsets.UTF_8));
    }

    private void addExpandedDatastreams(JSONObject thing, String thingId, WebRequest webRequest, String baseUrl,
	    STARequest staRequest) {
	boolean expandDatastreams = staRequest.getExpandOptions().stream()
		.anyMatch(o -> "Datastreams".equals(o.getProperty()));
	if (!expandDatastreams) {
	    return;
	}
	int top = staRequest.getExpandOptions().stream()
		.filter(o -> "Datastreams".equals(o.getProperty()))
		.map(STARequest.ExpandOption::getTop)
		.filter(java.util.Objects::nonNull)
		.findFirst()
		.orElse(100);
	try {
	    String path = "Things(" + thingId + ")/Datastreams";
	    String query = "$top=" + top;
	    ExpandSubRequest subReq = new ExpandSubRequest(webRequest, path, query);
	    DiscoveryRequestTransformer dsTransformer = new DatastreamsTransformer();
	    DiscoveryMessage msg = dsTransformer.transform(subReq);
	    ResultSet<String> dsResult = exec(msg);
	    JSONArray datastreams = new JSONArray();
	    if (dsResult != null) {
		for (String result : dsResult.getResultsList()) {
		    try {
			JSONObject ds = parseDatastreamFromResult(result, thingId, baseUrl);
			if (ds != null) {
			    datastreams.put(ds);
			}
		    } catch (Exception e) {
			// skip malformed
		    }
		}
	    }
	    thing.put("Datastreams", datastreams);
	} catch (Exception e) {
	    thing.put("Datastreams", new JSONArray());
	}
    }

    /**
     * Adds empty MultiDatastreams array when $expand=MultiDatastreams is requested.
     * MultiDatastreams are not supported at this time.
     */
    private void addExpandedMultiDatastreams(JSONObject thing, STARequest staRequest) {
	boolean expandMultiDatastreams = staRequest.getExpandOptions().stream()
		.anyMatch(o -> "MultiDatastreams".equals(o.getProperty()));
	if (expandMultiDatastreams) {
	    thing.put("MultiDatastreams", new JSONArray());
	}
    }

    private static JSONObject parseDatastreamFromResult(String result, String platformId, String baseUrl)
	    throws XMLStreamException, IOException {
	GIResourceParser parser = new GIResourceParser(result);
	String id = parser.getOnlineId();
	if (id == null) {
	    return null;
	}
	String platformName = parser.getPlatformName();
	String attributeName = parser.getAttributeName();
	String name = platformName != null && attributeName != null
		? platformName + " - " + attributeName
		: (attributeName != null ? attributeName : id);
	String description = parser.getAttributeDescription();
	if (description == null || description.isEmpty()) {
	    description = attributeName != null ? attributeName : "";
	}
	String phenomenonTime = "";
	String begin = parser.getTmpExtentBegin();
	String end = parser.getTmpExtentEnd();
	String endNow = parser.getTmpExtentEndNow();
	if (endNow != null && endNow.equals("true")) {
	    end = ISO8601DateTimeUtils.getISO8601DateTime();
	}
	if (begin != null && end != null) {
	    phenomenonTime = begin + "/" + end;
	} else if (begin != null) {
	    phenomenonTime = begin;
	} else if (end != null) {
	    phenomenonTime = end;
	}
	BigDecimal lon = null;
	BigDecimal lat = null;
	if (parser.getBBOX() != null) {
	    lon = parser.getBBOX().getBigDecimalWest();
	    lat = parser.getBBOX().getBigDecimalNorth();
	}
	String unitName = parser.getUnits();
	String unitSymbol = parser.getUnitsAbbreviation();
	if (unitSymbol == null || unitSymbol.isEmpty()) {
	    unitSymbol = unitName;
	}
	JSONObject properties = new JSONObject();
	properties.put("resultType", "Timeseries");
	if (platformId != null) {
	    properties.put("platformId", platformId);
	}
	return STAJsonWriter.datastream(id, name, description,
		"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement",
		unitName, unitSymbol, null, lon, lat, phenomenonTime, properties, platformId, baseUrl);
    }
}
