package eu.essi_lab.lib.skos.llm.model;

import java.util.*;

/**
 * Input to the expansion service. Mirrors what a SPARQL-driven component would need to be told: the term itself, plus optional context that
 * helps disambiguate it (the way choosing a specific SKOS scheme would have).
 *
 * @param searchTerm the term to expand
 * @param domainContext optional free-text hint about the domain/ontology scope, e.g. "agricultural science, FAO AGROVOC-like vocabulary"
 * @param language BCP-47 language tag for the term and expected results (e.g. "en", "it") or natural language description of the languages
 * (e.g. "italian and german")
 * @param allowedRelations if non-empty, restricts which relation types the model may propose; empty/null means "all relation types are
 * allowed"
 * @param maxResults cap on number of related terms requested
 */
public record TermExpansionRequest( //
	String searchTerm,//
	String domainContext,//
	String customRelation,//
	String language,//
	List<SKOSRelationType> allowedRelations,//
	int maxResults//
) {

    /**
     *
     */
    private static final int DEFAULT_MAX_RESULTS = 15;

    /**
     * @param searchTerm
     * @param domainContext
     * @param language
     * @param allowedRelations
     * @param maxResults
     */
    public TermExpansionRequest {

	if (searchTerm == null || searchTerm.isBlank()) {

	    throw new IllegalArgumentException("searchTerm must not be blank");
	}

	if (maxResults <= 0) {

	    maxResults = DEFAULT_MAX_RESULTS;
	}

	if (language == null || language.isBlank()) {

	    language = "en";
	}
    }

    /**
     * Convenience factory for the full case; all parameters but custom relation
     *
     * @param searchTerm
     * @param domainContext
     * @param language
     * @param allowedRelations
     * @param maxResults
     * @return
     */
    public static TermExpansionRequest full( //
	    String searchTerm,//
	    String domainContext,//
	    String language,//
	    List<SKOSRelationType> allowedRelations,//
	    int maxResults//
    ) {

	return new TermExpansionRequest(searchTerm, domainContext, null, language, allowedRelations, maxResults);
    }

    /**
     * Convenience factory for the common case: just a term, defaults for everything else
     *
     * @param searchTerm
     * @return
     */
    public static TermExpansionRequest simple(String searchTerm) {

	return new TermExpansionRequest(searchTerm, null, null, "en", List.of(), DEFAULT_MAX_RESULTS);
    }

    /**
     * Convenience factory for the custom relation case
     *
     * @param searchTerm
     * @param customRelation
     * @param language
     * @param maxResults
     * @return
     */
    public static TermExpansionRequest custom(String searchTerm, String customRelation, String language, int maxResults) {

	return new TermExpansionRequest(searchTerm, null, customRelation, language, List.of(SKOSRelationType.CUSTOM), maxResults);
    }
}
