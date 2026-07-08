package eu.essi_lab.gssrv.servlet.mcp;

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

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.pdk.handler.StreamingRequestHandler;
import eu.essi_lab.profiler.om.FeaturesHandler;
import eu.essi_lab.profiler.om.OMHandler;
import eu.essi_lab.profiler.om.PropertiesHandler;
import jakarta.ws.rs.core.StreamingOutput;

/**
 * Invokes the DAB O&amp;M REST API ({@code om-api}) handlers in-process for MCP tools.
 */
final class OmApiMcpSupport {

    static final String OM_API_BASE = WebRequest.GS_SERVICE_BASE_PATH + WebRequest.SERVICES_PATH + "essi";

    /** Query parameter names accepted by features, observations and properties (OpenAPI / {@link eu.essi_lab.profiler.om.OMRequest}). */
    static final Set<String> OM_QUERY_PARAMETERS = Set.of(//
	    "feature", //
	    "localFeatureIdentifier", //
	    "featureName", //
	    "observationIdentifier", //
	    "beginPosition", //
	    "endPosition", //
	    "west", //
	    "south", //
	    "east", //
	    "north", //
	    "spatialRelation", //
	    "predefinedSearchArea", //
	    "predefinedLayer", //
	    "observedProperty", //
	    "ontology", //
	    "timeInterpolation", //
	    "intendedObservationSpacing", //
	    "aggregationDuration", //
	    "country", //
	    "provider", //
	    "limit", //
	    "resumptionToken", //
	    "property");

    static final List<String> LISTABLE_QUERY_PROPERTIES = List.of(//
	    "country", //
	    "feature", //
	    "observation", //
	    "observedProperty", //
	    "observedPropertyURI", //
	    "intendedObservationSpacing", //
	    "aggregationDuration", //
	    "timeInterpolation", //
	    "provider", //
	    "format", //
	    "ontology", //
	    "predefinedSearchArea");

    private OmApiMcpSupport() {
    }

    static String invoke(String endpoint, Map<String, Object> arguments) throws Exception {

	String token = requiredString(arguments, "token");
	String view = requiredString(arguments, "view");

	String query = buildQueryString(arguments);
	String url = "http://localhost" + OM_API_BASE + "/token/" + urlEncodePathSegment(token) + "/view/"
		+ urlEncodePathSegment(view) + "/om-api/" + endpoint + (query.isEmpty() ? "" : "?" + query);

	WebRequest webRequest = WebRequest.createGET(url);
	StreamingRequestHandler handler = handlerFor(endpoint);

	ByteArrayOutputStream output = new ByteArrayOutputStream();
	StreamingOutput streaming = handler.getStreamingResponse(webRequest);
	streaming.write(output);
	return output.toString(StandardCharsets.UTF_8);
    }

    static String listAvailableQueryPropertiesJson(ObjectMapper mapper) throws Exception {

	Map<String, Object> payload = new LinkedHashMap<>(2);
	payload.put("properties", LISTABLE_QUERY_PROPERTIES);
	payload.put("usage",
		"Call om_list_query_properties with token, view and property set to one of the listed names to retrieve facet values.");
	return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
    }

    private static StreamingRequestHandler handlerFor(String endpoint) {

	return switch (endpoint) {
	case "features" -> new FeaturesHandler();
	case "observations" -> new OMHandler();
	case "properties" -> new PropertiesHandler();
	default -> throw new IllegalArgumentException("Unknown OM API endpoint: " + endpoint);
	};
    }

    private static String buildQueryString(Map<String, Object> arguments) {

	List<String> pairs = new ArrayList<>();
	for (String key : OM_QUERY_PARAMETERS) {
	    Object value = arguments.get(key);
	    if (value == null) {
		continue;
	    }
	    String text = stringify(value);
	    if (text.isEmpty()) {
		continue;
	    }
	    pairs.add(urlEncodeQueryParam(key) + "=" + urlEncodeQueryParam(text));
	}
	return pairs.stream().collect(Collectors.joining("&"));
    }

    static String requiredString(Map<String, Object> arguments, String key) {

	Object value = arguments != null ? arguments.get(key) : null;
	if (value == null || stringify(value).isBlank()) {
	    throw new IllegalArgumentException("Missing required argument: " + key);
	}
	return stringify(value);
    }

    static String optionalString(Map<String, Object> arguments, String key) {

	if (arguments == null) {
	    return null;
	}
	Object value = arguments.get(key);
	if (value == null) {
	    return null;
	}
	String text = stringify(value);
	return text.isBlank() ? null : text;
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> toArgumentMap(Object arguments) {

	if (arguments == null) {
	    return Map.of();
	}
	if (arguments instanceof Map<?, ?> map) {
	    return (Map<String, Object>) map;
	}
	throw new IllegalArgumentException("Tool arguments must be a JSON object");
    }

    private static String stringify(Object value) {

	if (value instanceof Number number) {
	    double d = number.doubleValue();
	    if (d == Math.rint(d)) {
		return String.valueOf((long) d);
	    }
	    return number.toString();
	}
	return value.toString().trim();
    }

    private static String urlEncodePathSegment(String value) {

	return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static String urlEncodeQueryParam(String value) {

	return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    static void logToolError(String toolName, Exception e) {

	GSLoggerFactory.getLogger(OmApiMcpSupport.class).error("MCP tool {} failed", toolName, e);
    }
}
