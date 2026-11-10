package eu.essi_lab.lib.skos.llm.prompt;

import eu.essi_lab.lib.skos.llm.model.*;

import java.util.*;
import java.util.stream.*;

/**
 * Builds the two prompts sent per request. Kept separate from the client so prompt iteration doesn't touch HTTP/parsing code, and so it's
 * easy to unit-test the prompt text itself.
 */
public final class PromptBuilder {

    private PromptBuilder() {}

    /**
     *
     * @return
     */
    public static String buildSystemPrompt() {
	return """
                You are a terminology and ontology expert emulating the behaviour of a SKOS
                (Simple Knowledge Organization System) thesaurus lookup service.

                Given a search term, propose semantically related terms using the standard
                SKOS relation vocabulary:
                - BROADER: a more general concept than the search term
                - NARROWER: a more specific concept than the search term
                - RELATED: associatively related but neither broader nor narrower
                - EXACT_MATCH: a synonym or equivalent label in another vocabulary/language
                - CLOSE_MATCH: nearly equivalent but not precisely interchangeable
                - BROAD_MATCH / NARROW_MATCH / RELATED_MATCH: cross-vocabulary versions of the above
                - CUSTOM: a custom relation described in natural language by the user
                - OTHER: use only when the relation is real and useful but doesn't fit the above;
                  in that case you MUST fill in a short free-text label describing the relation
                  in "other_relation_label". For every other relation type, set
                  "other_relation_label" to null.

                Rules:
                1. Only propose terms you are reasonably confident are genuinely, commonly
                   recognized as related — do not invent implausible or overly obscure terms.
                2. Prefer well-established terminology over exotic or speculative associations.
                3. Give a calibrated confidence score in [0.0, 1.0] for every term: 1.0 means
                   "this relation is essentially definitional / universally agreed", lower
                   scores mean "plausible but debatable or context-dependent".
                4. Give a one-sentence rationale for every term.
                5. Respond with JSON only, matching the provided schema exactly. No prose,
                   no markdown fences, no commentary outside the JSON structure.
                """;
    }

    /**
     *
     * @param request
     * @return
     */
    public static String buildUserPrompt(TermExpansionRequest request) {

	StringBuilder sb = new StringBuilder();

	sb.append("Search term: \"").append(request.searchTerm()).append("\"\n");
	sb.append("Response language for term labels: ").append(request.language()).append("\n");

	if(request.customRelation() != null && !request.customRelation().isBlank()) {

	    sb.append("Propose related terms according to this custom relation (instead of the predefined SKOS relations): ")
		    .append(request.customRelation()).append("\n");
	}

	if (request.domainContext() != null && !request.domainContext().isBlank()) {

	    sb.append("Domain / vocabulary context (use this to disambiguate the term and scope results): ")
		    .append(request.domainContext()).append("\n");
	}

	List<SKOSRelationType> allowed = request.allowedRelations();

	if (allowed != null && !allowed.isEmpty()) {

	    String allowedNames = allowed.stream().map(Enum::name).collect(Collectors.joining(", "));
	    sb.append("Only propose terms using these relation types: ").append(allowedNames).append("\n");
	}

	sb.append("Return at most ").append(request.maxResults()).append(" related terms, ")
		.append("ranked with the most confident / most relevant first.\n");

	return sb.toString();
    }
}
