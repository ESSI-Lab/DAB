package eu.essi_lab.lib.skos.llm.model;

import java.util.*;

/**
 * Final, domain-shaped result of a term expansion — this is the type your downstream code should consume instead of the old SPARQL-result
 * DTO.
 *
 * @param searchTerm the original input term
 * @param relatedTerms the expanded/related terms
 * @param modelUsed the OpenRouter model identifier that produced this result, kept for traceability/audit since results are now
 * probabilistic
 */
public record TermExpansionResult( //
	String searchTerm,//
	List<RelatedTerm> relatedTerms,//
	String modelUsed//
) {
}
