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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.skos.SKOSClient;
import eu.essi_lab.lib.skos.SKOSConcept;
import eu.essi_lab.lib.skos.SKOSResponse;
import eu.essi_lab.lib.skos.SKOSSemanticRelation;
import eu.essi_lab.lib.skos.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skos.expander.ExpansionLimit;
import eu.essi_lab.lib.skos.expander.ExpansionLimit.LimitTarget;
import eu.essi_lab.lib.skos.expander.impl.DefaultConceptsExpander;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceTemplateSpecification;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.ResourceTemplate;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

/**
 * MCP {@link eu.essi_lab.lib.skos.SKOSClient}-backed JSON resources for the HIS-Central
 * hydro ontology (dynamic SPARQL).
 */
public final class HydroOntologyMcpSpecifications {

    /** HIS-Central ontology service (SKOS queries via {@link SKOSClient}). */
    public static final String SPARQL_ENDPOINT = "https://his-central-ontology.geodab.eu/hydro-ontology/sparql";

    /**
     * Static resource URIs expose metadata and template patterns (no SKOS lookup).
     */
    public static final String RESOURCE_METADATA_URI = "dab://ontology/his-central/hydro";

    /** Prefix resolved when reading {@code dab://ontology/his-central/hydro/terms/{term}} */
    static final String TERMS_URI_PREFIX = RESOURCE_METADATA_URI + "/terms/";

    /**
     * URI template advertised to MCP clients ({@code term} = free-text vocabulary search term).
     */
    public static final String TERMS_URI_TEMPLATE = TERMS_URI_PREFIX + "{term}";

    private HydroOntologyMcpSpecifications() {
    }

    /**
     * MCP static resource describing the ontology service and template usage (JSON body).
     */
    public static SyncResourceSpecification hydroOntologyMetadataResource(ObjectMapper mapper) {

	Resource resource = Resource.builder().//
		uri(RESOURCE_METADATA_URI).//
		name("his-central-hydro-ontology-info").//
		title("HIS-Central hydro ontology").//
		description("Metadata and MCP resource template URIs for the HIS-Central hydro SKOS ontology (SPARQL: " + SPARQL_ENDPOINT
			+ "). Use dab://ontology/his-central/hydro/terms/{term} with a UTF-8 search term to retrieve concepts as JSON.").//
		mimeType("application/json").//
		build();

	return new SyncResourceSpecification(resource, (exchange, request) -> {

	    try {

		Map<String, Object> payload = LinkedHashMap.newLinkedHashMap(4);
		payload.put("title", "HIS-Central hydro ontology");
		payload.put("sparqlEndpoint", SPARQL_ENDPOINT);
		payload.put("resourceTemplateTerms", TERMS_URI_TEMPLATE);
		payload.put("usage",
			"Hydrate MCP resources/read with dab://ontology/his-central/hydro/terms/<term> "
				+ "where <term> is a Unicode search string (concept labels/terms). Returned JSON aggregates SKOS concepts.");

		String json = mapper.writeValueAsString(payload);
		return new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "application/json", json)));

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(HydroOntologyMcpSpecifications.class).error("hydro MCP metadata resource", e);

		try {

		    String json = mapper.writeValueAsString(Map.of("error", e.getMessage()));

		    return new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "application/json", json)));

		} catch (Exception e2) {

		    return new ReadResourceResult(
			    List.of(new TextResourceContents(request.uri(), "text/plain;charset=UTF-8", "Error: " + e.getMessage())));
		}
	    }
	});
    }

    /**
     * MCP resource template: {@value #TERMS_URI_TEMPLATE} resolves to JSON aggregating matched concepts.
     */
    public static SyncResourceTemplateSpecification hydroOntologyTermsResourceTemplate(ObjectMapper mapper) {

	ResourceTemplate template = ResourceTemplate.builder().//
		uriTemplate(TERMS_URI_TEMPLATE).//
		name("his-central-hydro-ontology-terms-json").//
		title("HIS-Central hydro ontology — SKOS concepts (JSON)").//
		description(
			"Dynamically queries " + SPARQL_ENDPOINT + " via SKOSClient (term search + semantic expansion); response is application/json.")
		.//
		mimeType("application/json").//
		build();

	return new SyncResourceTemplateSpecification(template, (exchange, request) -> {

	    String resolvedUri = request.uri();

	    try {

		String term = extractEncodedTerm(resolvedUri);
		if (term == null || term.isBlank()) {
		    return jsonError(mapper, resolvedUri, "Missing or empty {term}: use dab://ontology/his-central/hydro/terms/<term>");
		}

		SKOSClient client = configureSkosClient();
		client.setSearchValue(SKOSClient.SearchTarget.TERMS, term);
		SKOSResponse response = client.search();

		List<Map<String, Object>> concepts = response.getAggregatedResults().stream()//
			.map(HydroOntologyMcpSpecifications::conceptToJSON).collect(Collectors.toList());

		Map<String, Object> payload = LinkedHashMap.newLinkedHashMap(4);
		payload.put("sparqlEndpoint", SPARQL_ENDPOINT);
		payload.put("searchTerm", term);
		payload.put("concepts", concepts);
		payload.put("conceptCount", concepts.size());

		String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
		return new ReadResourceResult(List.of(new TextResourceContents(resolvedUri, "application/json", json)));

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(HydroOntologyMcpSpecifications.class).error("hydro MCP terms template: {}", resolvedUri, e);

		try {
		    return jsonError(mapper, resolvedUri, e.getMessage());

		} catch (Exception e2) {
		    return new ReadResourceResult(List
			    .of(new TextResourceContents(resolvedUri, "text/plain;charset=UTF-8", "Error: " + e.getMessage())));
		}
	    }
	});
    }

    private static ReadResourceResult jsonError(ObjectMapper mapper, String uri, String message) throws Exception {

	String json = mapper.writeValueAsString(Map.of("error", message != null ? message : "unknown", "sparqlEndpoint", SPARQL_ENDPOINT));
	return new ReadResourceResult(List.of(new TextResourceContents(uri, "application/json", json)));
    }

    /**
     * Resolves UTF-8 path segment(s) after {@value #TERMS_URI_PREFIX}.
     */
    static String extractEncodedTerm(String resourceUri) {

	if (resourceUri == null || !resourceUri.startsWith(TERMS_URI_PREFIX)) {

	    return "";
	}

	String raw = resourceUri.substring(TERMS_URI_PREFIX.length());
	if (raw.isEmpty()) {

	    return "";
	}

	return URLDecoder.decode(raw, StandardCharsets.UTF_8);
    }

    static SKOSClient configureSkosClient() {

	SKOSClient client = new SKOSClient();

	client.setOntologyUrls(Arrays.asList(SPARQL_ENDPOINT));
	client.setSearchLangs(Arrays.asList("it", "en"));
	client.setSourceLangs(Arrays.asList("it", "en"));

	client.setExpansionLevel(ExpansionLevel.MEDIUM);
	client.setExpansionsRelations(Arrays.asList(SKOSSemanticRelation.NARROWER));
	client.setExpansionLimit(ExpansionLimit.of(LimitTarget.CONCEPTS, 250));

	DefaultConceptsExpander expander = new DefaultConceptsExpander();
	expander.setExcludeNoPrefConcepts(false);
	expander.getQueryBuilder().setIncludeNoLanguageConcepts(true);
	client.setExpander(expander);

	return client;
    }

    static Map<String, Object> conceptToJSON(SKOSConcept c) {

	Map<String, Object> row = LinkedHashMap.newLinkedHashMap(8);
	row.put("uri", c.getConceptURI());
	row.put("prefLabel", c.getPref().orElse(null));
	row.put("altLabels", sortedCopy(c.getAlt()));
	row.put("expandedConceptUris", sortedCopy(c.getExpanded()));
	row.put("expandedFromConceptUris", sortedCopy(c.getExpandedFrom()));

	c.getLevel().ifPresent(lv -> row.put("expansionLevel", lv.name()));

	return row;
    }

    private static List<String> sortedCopy(java.util.Set<String> s) {

	ArrayList<String> list = new ArrayList<>(s);
	list.sort(String::compareTo);
	return list;
    }
}
