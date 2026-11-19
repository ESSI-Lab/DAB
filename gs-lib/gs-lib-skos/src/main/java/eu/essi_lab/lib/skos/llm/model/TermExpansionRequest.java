package eu.essi_lab.lib.skos.llm.model;

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
