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
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.resource.stax.GIResourceParser;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.sta.ExpandSubRequest;
import eu.essi_lab.profiler.sta.LocationsTransformer;
import eu.essi_lab.profiler.sta.STAResourceMapper;
import eu.essi_lab.profiler.sta.STAJsonWriter;
import eu.essi_lab.profiler.sta.STARequest;
import eu.essi_lab.profiler.sta.ThingsTransformer;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.pdk.wrt.DiscoveryRequestTransformer;
import eu.essi_lab.request.executor.IDiscoveryStringExecutor;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

/**
 * Handler for OGC STA Locations entity set and Things(id)/Locations navigation.
 */
public class LocationsHandler extends StreamingRequestHandler {

    private IDiscoveryStringExecutor executor;
    private DatabaseExecutor databaseExecutor;

    public LocationsHandler() {
	try {
	    ServiceLoader<IDiscoveryStringExecutor> loader = ServiceLoader.load(IDiscoveryStringExecutor.class);
	    Iterator<IDiscoveryStringExecutor> it = loader.iterator();
	    executor = it.hasNext() ? it.next() : null;
	} catch (Exception e) {
	    executor = null;
	}
	try {
	    StorageInfo uri = ConfigurationWrapper.getStorageInfo();
	    databaseExecutor = DatabaseProviderFactory.getExecutor(uri);
	} catch (GSException e) {
	    databaseExecutor = null;
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
		writeLocationsResponse(output, webRequest);
	    } catch (Exception e) {
		e.printStackTrace();
		throw new WebApplicationException(e.getMessage());
	    }
	};
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_JSON_TYPE;
    }

    public DiscoveryRequestTransformer getTransformer() {
	return new LocationsTransformer();
    }

    private void writeLocationsResponse(OutputStream output, WebRequest webRequest) throws Exception {
	if (executor == null) {
	    output.write("{\"value\":[]}".getBytes(StandardCharsets.UTF_8));
	    return;
	}

	STARequest staRequest = new STARequest(webRequest);
	boolean isThingsLocations = staRequest.getEntitySet().orElse(null) == STARequest.EntitySet.Things
		&& "Locations".equals(staRequest.getNavigationProperty().orElse(null));
	if (isThingsLocations) {
	    String thingId = staRequest.getEntityIdNormalized().orElse(null);
	    if (thingId == null || thingId.isEmpty()) {
		throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Missing Thing id").build());
	    }
	}

	DiscoveryRequestTransformer transformer = getTransformer();
	DiscoveryMessage message = transformer.transform(webRequest);

	ResultSet<String> resultSet = new ResultSet<>();

	if (message.getPage()!=null && message.getPage().getSize()>0) {
	 resultSet =   executor.retrieveStrings(message);
	}
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
		    String description = parser.getPlatformDescription();
		    if (description == null) {
			description = "";
		    }
		    String locKey = id + "|" + lon + "|" + lat + (altitude != null ? "|" + altitude : "");
		    if (seenKeys.add(locKey)) {
			JSONObject loc = STAJsonWriter.location(id, lon, lat, altitude, name,description, baseUrl);
			addExpandedThings(loc, id, webRequest, baseUrl, staRequest);
			locations.add(loc);
		    }
		} catch (Exception e) {
		    if (!isThingsLocations) {
			e.printStackTrace();
		    }
		}
	    }
	}

	if (staRequest.getEntityId().isPresent() && !isThingsLocations) {
	    if (locations.isEmpty()) {
		throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
	    }
	    JSONObject singleLoc = locations.get(0);
	    addExpandedThings(singleLoc, singleLoc.getString("@iot.id"), webRequest,
		    STAJsonWriter.buildBaseUrl(webRequest.getServletRequest().getRequestURL().toString()), staRequest);
	    output.write(singleLoc.toString().getBytes(StandardCharsets.UTF_8));
	    return;
	}

	Integer count = null;

	    if (staRequest.getCount() != null&&staRequest.getCount()) {
		message.setPage(new Page(1,10));
		CountSet countSet = executor.count(message);
		count = countSet.getCount();
	    }


	String nextLink = null;
	int pageSize = staRequest.getTop() != null ? staRequest.getTop() : 100;
	boolean hasFullPage = locations.size() >= pageSize;
	if (hasFullPage && resultSet != null && resultSet.getSearchAfter().isPresent()) {
	    String token = resultSet.getSearchAfter().get().getValues()
		    .map(v -> v.isEmpty() ? null : v.get(0).toString())
		    .orElse(null);
	    if (token != null) {
		String entityPath = isThingsLocations
			? "Things(" + staRequest.getEntityIdNormalized().get() + ")/Locations"
			: "Locations";
		nextLink = STAJsonWriter.buildNextLink(baseUrl, entityPath, token, webRequest.getQueryString());
	    }
	}

	String json = STAJsonWriter.collectionResponse(locations, nextLink, count);
	output.write(json.getBytes(StandardCharsets.UTF_8));
    }

    private void addExpandedThings(JSONObject location, String locationId, WebRequest webRequest, String baseUrl,
	    STARequest staRequest) {
	boolean expandThings = staRequest.getExpandOptions().stream()
		.anyMatch(o -> "Things".equals(o.getProperty()));
	if (!expandThings || databaseExecutor == null) {
	    return;
	}
	int top = staRequest.getExpandOptions().stream()
		.filter(o -> "Things".equals(o.getProperty()))
		.map(STARequest.ExpandOption::getTop)
		.filter(java.util.Objects::nonNull)
		.findFirst()
		.orElse(100);
	try {
	    String path = "Locations(" + locationId + ")/Things";
	    String query = "$top=" + top;
	    ExpandSubRequest subReq = new ExpandSubRequest(webRequest, path, query);
	    DiscoveryRequestTransformer thingsTransformer = new ThingsTransformer();
	    DiscoveryMessage msg = thingsTransformer.transform(subReq);
	    ResultSet<String> thingsResult = databaseExecutor.discoverDistinctStrings(msg);
	    JSONArray things = new JSONArray();
	    if (thingsResult != null) {
		for (String result : thingsResult.getResultsList()) {
		    try {
			GIResourceParser parser = new GIResourceParser(result);
			JSONObject thing = STAResourceMapper.thingFromParser(parser, baseUrl);
			if (thing != null) {
			    things.put(thing);
			}
		    } catch (Exception e) {
			// skip malformed
		    }
		}
	    }
	    location.put("Things", things);
	} catch (Exception e) {
	    location.put("Things", new JSONArray());
	}
    }
}
