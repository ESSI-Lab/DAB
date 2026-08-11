package eu.essi_lab.gssrv.servlet.mcp;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.ontology.OntologySetting;
import eu.essi_lab.lib.skos.SKOSClient;
import eu.essi_lab.lib.skos.SKOSClient.SearchTarget;
import eu.essi_lab.lib.skos.SKOSConcept;
import eu.essi_lab.lib.skos.SKOSResponse;
import eu.essi_lab.lib.skos.SKOSSemanticRelation;
import eu.essi_lab.lib.skos.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skos.expander.ExpansionLimit;
import eu.essi_lab.lib.skos.expander.ExpansionLimit.LimitTarget;
import eu.essi_lab.lib.skos.expander.impl.DefaultConceptsExpander;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.request.executor.impl.discover.QueryInitializer;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncResourceTemplateSpecification;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.ResourceTemplate;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;

/**
 * MCP resource template listing observed-property URIs indexed in a DAB view, enriched with
 * SKOS labels and direct broader/narrower relations when resolvable.
 */
public final class ViewObservedPropertiesMcpSpecifications {

    /** View id pre-warmed at startup ({@link eu.essi_lab.gssrv.starter.DABStarter}). */
    public static final String STARTUP_WARMUP_VIEW_ID = "his-central";

    public static final String VIEW_URI_PREFIX = "dab://view/";

    static final String OBSERVED_PROPERTIES_SUFFIX = "/observed-properties";

    /** URI template ({@code viewId} = DAB view identifier, URL-encoded when it contains reserved characters). */
    public static final String VIEW_OBSERVED_PROPERTIES_URI_TEMPLATE = VIEW_URI_PREFIX + "{viewId}" + OBSERVED_PROPERTIES_SUFFIX;

    private static final int SKOS_RELATION_LIMIT = 100;

    private static final ObjectMapper CACHE_MAPPER = new ObjectMapper();

    private ViewObservedPropertiesMcpSpecifications() {
    }

    /**
     * Builds and stores the observed-properties payload for {@code viewId}. Safe to call from a background thread.
     *
     * @param viewId the DAB view identifier to warm
     */
    public static void warmCache(String viewId) {

	if (viewId == null || viewId.isBlank()) {
	    return;
	}

	if (ViewObservedPropertiesMcpCache.get(viewId).isPresent()) {
	    GSLoggerFactory.getLogger(ViewObservedPropertiesMcpSpecifications.class).info(
		    "MCP observed-properties cache already populated for view {}", viewId);
	    return;
	}

	try {
	    Optional<View> view = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId);
	    if (view.isEmpty()) {
		GSLoggerFactory.getLogger(ViewObservedPropertiesMcpSpecifications.class).warn(
			"MCP observed-properties cache warmup skipped: view not found ({})", viewId);
		return;
	    }

	    GSLoggerFactory.getLogger(ViewObservedPropertiesMcpSpecifications.class).info(
		    "MCP observed-properties cache warmup STARTED for view {}", viewId);

	    String json = buildPayloadJson(CACHE_MAPPER, viewId, view.get());
	    ViewObservedPropertiesMcpCache.put(viewId, json);

	    GSLoggerFactory.getLogger(ViewObservedPropertiesMcpSpecifications.class).info(
		    "MCP observed-properties cache warmup ENDED for view {} ({} bytes)", viewId, json.length());

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(ViewObservedPropertiesMcpSpecifications.class).error(
		    "MCP observed-properties cache warmup failed for view {}", viewId, e);
	}
    }

    /**
     * Schedules {@link #warmCache(String)} on a daemon thread so startup is not blocked.
     *
     * @param viewId the DAB view identifier to warm
     */
    public static void warmCacheAsync(String viewId) {

	if (viewId == null || viewId.isBlank()) {
	    return;
	}

	Thread warmup = new Thread(() -> warmCache(viewId), "mcp-observed-properties-warmup-" + viewId);
	warmup.setDaemon(true);
	warmup.start();
    }

    public static SyncResourceTemplateSpecification viewObservedPropertiesResourceTemplate(ObjectMapper mapper) {

	ResourceTemplate template = ResourceTemplate.builder().//
		uriTemplate(VIEW_OBSERVED_PROPERTIES_URI_TEMPLATE).//
		name("dab-view-observed-properties-json").//
		title("DAB view — observed property URIs (JSON)").//
		description("Returns distinct observedPropertyURI values indexed for the given DAB view, with SKOS preferred and "
			+ "alternate labels and direct broader/narrower concept URIs when available from configured ontologies.")//
		.mimeType("application/json").//
		build();

	return new SyncResourceTemplateSpecification(template, (context, request) -> {

	    String resolvedUri = request.uri();

	    try {

		String viewId = extractViewId(resolvedUri);
		if (viewId == null || viewId.isBlank()) {
		    return jsonError(mapper, resolvedUri,
			    "Missing or empty {viewId}: use dab://view/<viewId>/observed-properties (URL-encode the view id when needed)");
		}

		Optional<String> cached = ViewObservedPropertiesMcpCache.get(viewId);
		if (cached.isPresent()) {
		    return new ReadResourceResult(
			    List.of(new TextResourceContents(resolvedUri, "application/json", cached.get())));
		}

		Optional<View> view = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId);
		if (view.isEmpty()) {
		    return jsonError(mapper, resolvedUri, "View not found: " + viewId);
		}

		String json = buildPayloadJson(mapper, viewId, view.get());
		ViewObservedPropertiesMcpCache.put(viewId, json);
		return new ReadResourceResult(List.of(new TextResourceContents(resolvedUri, "application/json", json)));

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(ViewObservedPropertiesMcpSpecifications.class).error("view observed-properties MCP template: {}",
			resolvedUri, e);

		try {
		    return jsonError(mapper, resolvedUri, e.getMessage());
		} catch (Exception e2) {
		    return new ReadResourceResult(
			    List.of(new TextResourceContents(resolvedUri, "text/plain;charset=UTF-8", "Error: " + e.getMessage())));
		}
	    }
	});
    }

    static String extractViewId(String resourceUri) {

	if (resourceUri == null || !resourceUri.startsWith(VIEW_URI_PREFIX)) {
	    return "";
	}

	String remainder = resourceUri.substring(VIEW_URI_PREFIX.length());
	if (!remainder.endsWith(OBSERVED_PROPERTIES_SUFFIX)) {
	    return "";
	}

	String raw = remainder.substring(0, remainder.length() - OBSERVED_PROPERTIES_SUFFIX.length());
	if (raw.endsWith("/")) {
	    raw = raw.substring(0, raw.length() - 1);
	}
	if (raw.isEmpty()) {
	    return "";
	}

	return URLDecoder.decode(raw, StandardCharsets.UTF_8);
    }

    private static String buildPayloadJson(ObjectMapper mapper, String viewId, View view) throws Exception {

	List<TermFrequencyItem> terms = collectObservedPropertyUris(view);
	List<String> ontologyUrls = enabledOntologyEndpoints();
	Map<String, Map<String, Object>> semanticsCache = new HashMap<>();

	List<Map<String, Object>> properties = new ArrayList<>(terms.size());
	for (TermFrequencyItem term : terms) {
	    properties.add(observedPropertyToJson(term, ontologyUrls, semanticsCache));
	}

	Map<String, Object> payload = LinkedHashMap.newLinkedHashMap(6);
	payload.put("viewId", viewId);
	payload.put("viewLabel", view.getLabel());
	payload.put("observedProperties", properties);
	payload.put("observedPropertyCount", properties.size());
	payload.put("ontologyEndpoints", ontologyUrls);
	payload.put("usage",
		"Each entry aggregates dataset index counts for observedPropertyURI within the resolved view bond.");

	return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
    }

    private static List<TermFrequencyItem> collectObservedPropertyUris(View view) throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();
	message.setDataBaseURI(ConfigurationWrapper.getStorageInfo());
	message.setView(view);
	message.setSources(ConfigurationWrapper.getViewSources(view));
	message.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	message.getResourceSelector().addIndex(MetadataElement.OBSERVED_PROPERTY_URI);
	message.setPage(new Page(1000));

	new QueryInitializer().initializeQuery(message);

	DatabaseExecutor executor = DatabaseProviderFactory.getExecutor(ConfigurationWrapper.getStorageInfo());
	List<TermFrequencyItem> out = new ArrayList<>();
	String resumption = null;
	for (int pageIdx = 0; pageIdx < 100_000; pageIdx++) {
	    ResultSet<TermFrequencyItem> page = executor.getIndexValues(message, MetadataElement.OBSERVED_PROPERTY_URI, 0, resumption);
	    if (page == null) {
		break;
	    }
	    if (page.getResultsList() != null) {
		out.addAll(page.getResultsList());
	    }
	    Optional<SearchAfter> searchAfter = page.getSearchAfter();
	    if (searchAfter.isEmpty()) {
		break;
	    }
	    Optional<List<Object>> values = searchAfter.get().getValues();
	    if (values.isEmpty() || values.get().isEmpty()) {
		break;
	    }
	    resumption = values.get().get(0).toString();
	}
	return out;
    }

    private static List<String> enabledOntologyEndpoints() {

	return ConfigurationWrapper.getOntologySettings().stream()//
		.filter(s -> s.getOntologyAvailability() == OntologySetting.Availability.ENABLED)//
		.map(OntologySetting::getOntologyEndpoint)//
		.collect(Collectors.toList());
    }

    private static Map<String, Object> observedPropertyToJson(TermFrequencyItem term, List<String> ontologyUrls,
	    Map<String, Map<String, Object>> semanticsCache) {

	String uri = term.getTerm();
	Map<String, Object> row = LinkedHashMap.newLinkedHashMap(8);
	row.put("uri", uri);
	row.put("recordCount", term.getFreq());

	if (ontologyUrls.isEmpty() || uri == null || uri.isBlank()) {
	    return row;
	}

	Map<String, Object> semantics = semanticsCache.computeIfAbsent(uri, u -> resolveSemantics(u, ontologyUrls));
	row.putAll(semantics);
	return row;
    }

    private static Map<String, Object> resolveSemantics(String uri, List<String> ontologyUrls) {

	Map<String, Object> semantics = LinkedHashMap.newLinkedHashMap(6);

	try {
	    SKOSConcept concept = lookupConcept(uri, ontologyUrls);
	    if (concept != null) {
		Map<String, String> prefLabels = LinkedHashMap.newLinkedHashMap(2);
		prefLabels.put("en", resolvePreferredLabel(uri, ontologyUrls, "en"));
		prefLabels.put("it", resolvePreferredLabel(uri, ontologyUrls, "it"));
		semantics.put("prefLabels", prefLabels);
		semantics.put("altLabels", sortedCopy(concept.getAlt()));
	    }

	    Set<String> broader = lookupRelatedConceptUris(uri, ontologyUrls, SKOSSemanticRelation.BROADER);
	    Set<String> narrower = lookupRelatedConceptUris(uri, ontologyUrls, SKOSSemanticRelation.NARROWER);
	    if (!broader.isEmpty()) {
		semantics.put("broaderConceptUris", sortedCopy(broader));
	    }
	    if (!narrower.isEmpty()) {
		semantics.put("narrowerConceptUris", sortedCopy(narrower));
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(ViewObservedPropertiesMcpSpecifications.class).warn("SKOS enrichment failed for {}: {}", uri,
		    e.getMessage());
	}

	return semantics;
    }

    private static SKOSConcept lookupConcept(String uri, List<String> ontologyUrls) throws Exception {

	SKOSClient client = configureSkosClient(ontologyUrls);
	client.setSearchValue(SearchTarget.CONCEPTS, uri);
	client.setExpansionLevel(ExpansionLevel.NONE);
	client.setExpansionLimit(ExpansionLimit.of(LimitTarget.CONCEPTS, 1));

	SKOSResponse response = client.search();
	for (SKOSConcept concept : response.getResults()) {
	    if (uri.equals(concept.getConceptURI())) {
		return concept;
	    }
	}
	return response.getResults().isEmpty() ? null : response.getResults().get(0);
    }

    private static Set<String> lookupRelatedConceptUris(String uri, List<String> ontologyUrls, SKOSSemanticRelation relation)
	    throws Exception {

	SKOSClient client = configureSkosClient(ontologyUrls);
	client.setSearchValue(SearchTarget.CONCEPTS, uri);
	client.setExpansionLevel(ExpansionLevel.LOW);
	client.setExpansionsRelations(List.of(relation));
	client.setExpansionLimit(ExpansionLimit.of(LimitTarget.CONCEPTS, SKOS_RELATION_LIMIT));

	SKOSResponse response = client.search();
	for (SKOSConcept concept : response.getResults()) {
	    if (uri.equals(concept.getConceptURI())) {
		return new TreeSet<>(concept.getExpanded());
	    }
	}
	return Set.of();
    }

    private static String resolvePreferredLabel(String conceptUri, List<String> ontologyUrls, String language) throws Exception {

	SKOSClient client = configureSkosClient(ontologyUrls);
	client.setSearchValue(SearchTarget.CONCEPTS, conceptUri);
	client.setSearchLangs(List.of(language));
	client.setSourceLangs(List.of(language));
	client.setExpansionLevel(ExpansionLevel.NONE);
	client.setExpansionLimit(ExpansionLimit.of(LimitTarget.CONCEPTS, 1));

	SKOSResponse response = client.search();
	for (SKOSConcept concept : response.getResults()) {
	    if (conceptUri.equals(concept.getConceptURI()) && concept.getPref().isPresent()) {
		return concept.getPref().get();
	    }
	}
	List<String> prefLabels = response.getPrefLabels();
	return prefLabels.isEmpty() ? "" : prefLabels.get(0);
    }

    private static SKOSClient configureSkosClient(List<String> ontologyUrls) {

	SKOSClient client = new SKOSClient();
	client.setOntologyUrls(ontologyUrls);
	client.setSearchLangs(Arrays.asList("it", "en"));
	client.setSourceLangs(Arrays.asList("it", "en"));

	DefaultConceptsExpander expander = new DefaultConceptsExpander();
	expander.setExcludeNoPrefConcepts(false);
	expander.getQueryBuilder().setIncludeNoLanguageConcepts(true);
	client.setExpander(expander);

	return client;
    }

    private static List<String> sortedCopy(Set<String> values) {

	ArrayList<String> list = new ArrayList<>(values);
	list.sort(String::compareTo);
	return list;
    }

    private static ReadResourceResult jsonError(ObjectMapper mapper, String uri, String message) throws Exception {

	String json = mapper.writeValueAsString(Map.of("error", message != null ? message : "unknown"));
	return new ReadResourceResult(List.of(new TextResourceContents(uri, "application/json", json)));
    }
}
