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
import eu.essi_lab.profiler.sta.ObservedPropertiesTransformer;
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
		    String id = parser.getOnlineId();
		    if (id == null) {
			continue;
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
		    if (endNow!=null&&endNow.equals("true")){
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
		    String platformId = parser.getUniquePlatformCode();
		    if (platformId != null) {
			properties.put("platformId", platformId);
		    }
		    JSONObject ds = STAJsonWriter.datastream(id, name, description,
			    "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement",
			    unitName, unitSymbol, null, lon, lat, phenomenonTime, properties, platformId, baseUrl);
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
	    output.write(datastreams.get(0).toString().getBytes(StandardCharsets.UTF_8));
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
			long id = observedPropertyId(uniqueAttributeId);
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

    private static long observedPropertyId(String uniqueAttributeId) {
	if (uniqueAttributeId == null || uniqueAttributeId.isEmpty()) {
	    return 0;
	}
	long h = 0;
	for (char c : uniqueAttributeId.toCharArray()) {
	    h = 31 * h + c;
	}
	return Math.abs(h);
    }
}
