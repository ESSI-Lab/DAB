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
import java.util.ArrayList;
import java.util.List;

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
 * Handler for OGC STA ObservedProperties entity set.
 * Discovery by distinct UNIQUE_ATTRIBUTE_IDENTIFIER; maps ATTRIBUTE_TITLE to name.
 */
public class ObservedPropertiesHandler extends StreamingRequestHandler {

    private DatabaseExecutor executor;

    public ObservedPropertiesHandler() {
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
		writeObservedPropertiesResponse(output, webRequest);
	    } catch (Exception e) {
		throw new WebApplicationException("Error handling ObservedProperties request", e);
	    }
	};
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_JSON_TYPE;
    }

    public DiscoveryRequestTransformer getTransformer() {
	return new ObservedPropertiesTransformer();
    }

    private void writeObservedPropertiesResponse(OutputStream output, WebRequest webRequest) throws Exception {
	if (executor == null) {
	    output.write("{\"value\":[]}".getBytes(StandardCharsets.UTF_8));
	    return;
	}

	DiscoveryRequestTransformer transformer = getTransformer();
	DiscoveryMessage message = transformer.transform(webRequest);

	ResultSet<String> resultSet = executor.discoverDistinctStrings(message);
	List<JSONObject> observedProperties = new ArrayList<>();
	String baseUrl = STAJsonWriter.buildBaseUrl(webRequest.getServletRequest().getRequestURL().toString());
	STARequest staRequest = new STARequest(webRequest);
	String requestedId = staRequest.getEntityIdNormalized().orElse(null);

	if (resultSet != null) {
	    for (String result : resultSet.getResultsList()) {
		try {
		    GIResourceParser parser = new GIResourceParser(result);
		    String uniqueAttributeId = parser.getAttributeCode();
		    if (uniqueAttributeId == null || uniqueAttributeId.isEmpty()) {
			continue;
		    }
		    long id = STAResourceMapper.observedPropertyId(uniqueAttributeId);
		    if (requestedId != null && requestedId.matches("\\d+")) {
			if (id != Long.parseLong(requestedId)) {
			    continue;
			}
		    }
		    JSONObject op = STAResourceMapper.observedPropertyFromParser(parser, baseUrl);
		    if (op != null) {
			observedProperties.add(op);
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}

	if (staRequest.getEntityId().isPresent()) {
	    if (observedProperties.isEmpty()) {
		throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
	    }
	    output.write(observedProperties.get(0).toString().getBytes(StandardCharsets.UTF_8));
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
		nextLink = STAJsonWriter.buildNextLink(baseUrl, "ObservedProperties", token, webRequest.getQueryString());
	    }
	}

	String json = STAJsonWriter.collectionResponse(observedProperties, nextLink, count);
	output.write(json.getBytes(StandardCharsets.UTF_8));
    }
}
