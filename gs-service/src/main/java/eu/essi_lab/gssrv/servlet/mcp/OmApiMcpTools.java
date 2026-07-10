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

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

/**
 * MCP tools wrapping the DAB O&amp;M REST API ({@code om-api}).
 */
public final class OmApiMcpTools {

    private static final String COMMON_QUERY_PROPERTIES = """
	      "token": { "type": "string", "description": "DAB user token (path parameter)" },
	      "view": { "type": "string", "description": "DAB view identifier (path parameter)" },
	      "feature": { "type": "string", "description": "Global feature identifier" },
	      "localFeatureIdentifier": { "type": "string", "description": "Feature identifier from the data provider" },
	      "featureName": { "type": "string", "description": "Feature name" },
	      "observationIdentifier": { "type": "string", "description": "Observation / timeseries identifier" },
	      "beginPosition": { "type": "string", "description": "Temporal interval begin (ISO8601)" },
	      "endPosition": { "type": "string", "description": "Temporal interval end (ISO8601)" },
	      "west": { "type": "number", "description": "Bounding box west longitude" },
	      "south": { "type": "number", "description": "Bounding box south latitude" },
	      "east": { "type": "number", "description": "Bounding box east longitude" },
	      "north": { "type": "number", "description": "Bounding box north latitude" },
	      "spatialRelation": { "type": "string", "description": "Spatial relation: contains or intersects" },
	      "predefinedSearchArea": { "type": "string", "description": "Predefined spatial extent layer id" },
	      "observedProperty": { "type": "string", "description": "Observed property free text or ontology URI" },
	      "ontology": { "type": "string", "description": "Ontology for observed property expansion: whos or his-central" },
	      "timeInterpolation": { "type": "string", "description": "Time-axis interpolation (MAX, MIN, AVERAGE, ...)" },
	      "intendedObservationSpacing": { "type": "string", "description": "Expected spacing between observations (ISO8601 duration)" },
	      "aggregationDuration": { "type": "string", "description": "Aggregation duration of values (ISO8601 duration)" },
	      "country": { "type": "string", "description": "Country code (ISO3)" },
	      "provider": { "type": "string", "description": "Data provider / source identifier" },
	      "limit": { "type": "integer", "description": "Maximum number of records returned" },
	      "resumptionToken": { "type": "string", "description": "Pagination token from a previous response" }
	    """;

    private OmApiMcpTools() {
    }

    public static List<SyncToolSpecification> toolSpecifications(McpJsonMapper jsonMapper, ObjectMapper objectMapper) {

	return List.of(//
		searchFeaturesTool(jsonMapper), //
		searchObservationsTool(jsonMapper), //
		getObservationTool(jsonMapper), //
		listQueryPropertiesTool(jsonMapper, objectMapper));
    }

    private static SyncToolSpecification searchFeaturesTool(McpJsonMapper jsonMapper) {

	String inputSchema = """
		{
		  "type": "object",
		  "required": ["token", "view"],
		  "properties": {
		%s
		  }
		}
		""".formatted(COMMON_QUERY_PROPERTIES);

	return SyncToolSpecification.builder()//
		.tool(McpSchema.Tool.builder()//
			.name("om_search_features")//
			.title("Search O&M features (metadata)")//
			.description("Searches DAB monitoring features (stations/sites) via the O&M API. "
				+ "Returns OM-JSON/GeoJSON metadata only (no observation values). "
				+ "Uses the same query constraints as GET .../om-api/features.")//
			.inputSchema(jsonMapper, inputSchema)//
			.build())//
		.callHandler((exchange, request) -> executeTool("om_search_features", request, () -> OmApiMcpSupport.invoke("features",
			OmApiMcpSupport.toArgumentMap(request.arguments()))))//
		.build();
    }

    private static SyncToolSpecification searchObservationsTool(McpJsonMapper jsonMapper) {

	String inputSchema = """
		{
		  "type": "object",
		  "required": ["token", "view"],
		  "properties": {
		%s
		  }
		}
		""".formatted(COMMON_QUERY_PROPERTIES);

	return SyncToolSpecification.builder()//
		.tool(McpSchema.Tool.builder()//
			.name("om_search_observations")//
			.title("Search O&M observations (metadata)")//
			.description("Searches DAB observation timeseries metadata via the O&M API. "
				+ "Returns metadata only (no measurement values). "
				+ "Use om_get_observation to download values for a specific timeseries id. "
				+ "Uses the same query constraints as GET .../om-api/observations.")//
			.inputSchema(jsonMapper, inputSchema)//
			.build())//
		.callHandler((exchange, request) -> executeTool("om_search_observations", request,
			() -> OmApiMcpSupport.invoke("observations", OmApiMcpSupport.toArgumentMap(request.arguments()))))//
		.build();
    }

    private static SyncToolSpecification getObservationTool(McpJsonMapper jsonMapper) {

	String inputSchema = """
		{
		  "type": "object",
		  "required": ["token", "view", "observationIdentifier", "beginPosition", "endPosition"],
		  "properties": {
		    "token": { "type": "string", "description": "DAB user token (path parameter)" },
		    "view": { "type": "string", "description": "DAB view identifier (path parameter)" },
		    "observationIdentifier": { "type": "string", "description": "Observation / timeseries id from om_search_observations" },
		    "beginPosition": { "type": "string", "description": "Temporal interval begin (ISO8601)" },
		    "endPosition": { "type": "string", "description": "Temporal interval end (ISO8601)" },
		    "format": { "type": "string", "description": "Response format (e.g. JSON, CSV, WaterML 1.0, WaterML 2.0, NetCDF). Default: JSON." }
		  }
		}
		""";

	return SyncToolSpecification.builder()//
		.tool(McpSchema.Tool.builder()//
			.name("om_get_observation")//
			.title("Get O&M observation values")//
			.description("Downloads measurement values for a single observation timeseries via the O&M API. "
				+ "Pass the id returned by om_search_observations together with a temporal range. "
				+ "Equivalent to GET .../om-api/observations?observationIdentifier=...&includeData=true.")//
			.inputSchema(jsonMapper, inputSchema)//
			.build())//
		.callHandler((exchange, request) -> executeTool("om_get_observation", request,
			() -> OmApiMcpSupport.invokeObservationData(OmApiMcpSupport.toArgumentMap(request.arguments()))))//
		.build();
    }

    private static SyncToolSpecification listQueryPropertiesTool(McpJsonMapper jsonMapper, ObjectMapper objectMapper) {

	String inputSchema = """
		{
		  "type": "object",
		  "required": ["token", "view"],
		  "properties": {
		%s,
		    "property": {
		      "type": "string",
		      "description": "Facet to list (e.g. predefinedSearchArea, timeInterpolation, country, provider, observedPropertyURI). Omit to list available property names."
		    }
		  }
		}
		""".formatted(COMMON_QUERY_PROPERTIES);

	return SyncToolSpecification.builder()//
		.tool(McpSchema.Tool.builder()//
			.name("om_list_query_properties")//
			.title("List O&M query property values")//
			.description("Lists allowed values for O&M search fields (countries, providers, predefined search areas, "
				+ "time interpolation modes, etc.) via GET .../om-api/properties. "
				+ "Accepts the same spatial/temporal/feature filters to scope facet counts.")//
			.inputSchema(jsonMapper, inputSchema)//
			.build())//
		.callHandler((exchange, request) -> {

		    Map<String, Object> args = OmApiMcpSupport.toArgumentMap(request.arguments());
		    try {
			OmApiMcpSupport.requiredString(args, "token");
			OmApiMcpSupport.requiredString(args, "view");
			if (OmApiMcpSupport.optionalString(args, "property") == null) {
			    return success(OmApiMcpSupport.listAvailableQueryPropertiesJson(objectMapper));
			}
			return success(OmApiMcpSupport.invoke("properties", args));
		    } catch (Exception e) {
			OmApiMcpSupport.logToolError("om_list_query_properties", e);
			return error(e.getMessage());
		    }
		})//
		.build();
    }

    private static CallToolResult executeTool(String toolName, CallToolRequest request, ToolAction action) {

	try {
	    return success(action.run());
	} catch (Exception e) {
	    OmApiMcpSupport.logToolError(toolName, e);
	    return error(e.getMessage());
	}
    }

    private static CallToolResult success(String json) {

	return CallToolResult.builder()//
		.content(List.of(new TextContent(json)))//
		.isError(false)//
		.build();
    }

    private static CallToolResult error(String message) {

	String text = message != null ? message : "unknown error";
	return CallToolResult.builder()//
		.content(List.of(new TextContent(text)))//
		.isError(true)//
		.build();
    }

    @FunctionalInterface
    private interface ToolAction {
	String run() throws Exception;
    }
}
