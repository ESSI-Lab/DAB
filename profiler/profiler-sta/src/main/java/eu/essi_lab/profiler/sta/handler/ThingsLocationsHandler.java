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

import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
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
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.sta.STAJsonWriter;
import eu.essi_lab.profiler.sta.STARequest;
import eu.essi_lab.profiler.sta.ThingsLocationsTransformer;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

/**
 * Handler for Things(id)/Locations. Returns an array of locations related to the thing.
 */
public class ThingsLocationsHandler extends StreamingRequestHandler {

    private DatabaseExecutor executor;

    public ThingsLocationsHandler() {
	try {
	    StorageInfo uri = ConfigurationWrapper.getStorageInfo();
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
		writeThingsLocationsResponse(output, webRequest);
	    } catch (Exception e) {
		throw new WebApplicationException("Error handling Things/Locations request", e);
	    }
	};
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_JSON_TYPE;
    }

    public DiscoveryRequestTransformer getTransformer() {
	return new ThingsLocationsTransformer();
    }

    private void writeThingsLocationsResponse(OutputStream output, WebRequest webRequest) throws Exception {
	if (executor == null) {
	    output.write("{\"value\":[]}".getBytes(StandardCharsets.UTF_8));
	    return;
	}

	STARequest staRequest = new STARequest(webRequest);
	String thingId = staRequest.getEntityIdNormalized().orElse(null);
	if (thingId == null || thingId.isEmpty()) {
	    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Missing Thing id").build());
	}

	DiscoveryRequestTransformer transformer = getTransformer();
	DiscoveryMessage message = transformer.transform(webRequest);

	ResultSet<String> resultSet = executor.discoverDistinctStrings(message);
	List<JSONObject> locations = new ArrayList<>();
	Set<String> seenKeys = new LinkedHashSet<>();
	String baseUrl = STAJsonWriter.buildBaseUrl(webRequest.getServletRequest().getRequestURL().toString());

	if (resultSet != null) {
	    for (String result : resultSet.getResultsList()) {
		try {
		    GIResourceParser parser = new GIResourceParser(result);
		    String id = parser.getUniquePlatformCode();
		    if (id == null) {
			continue;
		    }
		    GeographicBoundingBox bbox = parser.getBBOX();
		    BigDecimal lat = null;
		    BigDecimal lon = null;
		    if (bbox != null) {
			lon = parser.getBBOX().getBigDecimalWest();
			lat = parser.getBBOX().getBigDecimalNorth();
		    }
		    if (lon == null || lat == null) {
			continue;
		    }
		    String alt = parser.getAltitude();
		    BigDecimal altitude = null;
		    if (alt != null && !alt.isEmpty()) {
			altitude = new BigDecimal(alt.trim());
		    }
		    String name = parser.getPlatformName();
		    if (name == null) {
			name = id;
		    }
		    String locKey = id + "|" + lon + "|" + lat + (altitude != null ? "|" + altitude : "");
		    if (seenKeys.add(locKey)) {
			JSONObject loc = STAJsonWriter.location(id, lon, lat, altitude, name, baseUrl);
			locations.add(loc);
		    }
		} catch (Exception e) {
		    // skip malformed
		}
	    }
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
		String entityPath = "Things(" + thingId + ")/Locations";
		nextLink = STAJsonWriter.buildNextLink(baseUrl, entityPath, token, webRequest.getQueryString());
	    }
	}

	String json = STAJsonWriter.collectionResponse(locations, nextLink, count);
	output.write(json.getBytes(StandardCharsets.UTF_8));
    }
}
