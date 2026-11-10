package eu.essi_lab.lib.skos.llm.service;

import eu.essi_lab.lib.skos.llm.client.*;
import eu.essi_lab.lib.skos.llm.model.*;
import eu.essi_lab.lib.skos.llm.prompt.*;

import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
 * LLM-backed replacement for the previous SPARQL/SKOS-ontology-querying component. Same conceptual contract — term in, related terms out —
 * but the related terms now come from a language model instead of a curated ontology graph.
 * <p>Thread-safe and cheap to reuse: hold one instance per configured
 * model/API key, don't create a new one per request.
 */
public class SKOSTermExpansionService implements AutoCloseable {

    private static final Logger log = Logger.getLogger(SKOSTermExpansionService.class.getName());
    private static final int MAX_RETRIES = 2;

    private final OpenRouterClient client;
    private final OpenRouterConfig config;

    /**
     * @param config
     */
    public SKOSTermExpansionService(OpenRouterConfig config) {
	this.config = config;
	this.client = new OpenRouterClient(config);
    }

    /**
     * Convenience overload matching the simplest old-component call pattern: term in, results out.
     */
    public TermExpansionResult expand(String searchTerm) {

	return expand(TermExpansionRequest.simple(searchTerm));
    }

    /**
     * @param request
     * @return
     */
    public TermExpansionResult expand(TermExpansionRequest request) {

	String systemPrompt = PromptBuilder.buildSystemPrompt();
	String userPrompt = PromptBuilder.buildUserPrompt(request);

	IOException lastIoFailure = null;

	for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {

	    try {

		LlmWireFormat.ExpansionDto dto = client.requestTermExpansion(systemPrompt, userPrompt);
		return toDomainResult(request.searchTerm(), dto);

	    } catch (OpenRouterApiException e) {
		// 429/5xx are worth a retry; 4xx auth/validation errors are not.
		if (e.httpStatusCode() == 429 || e.httpStatusCode() >= 500) {
		    log.log(Level.WARNING, "Retryable OpenRouter error on attempt " + attempt, e);
		    continue;
		}
		throw new TermExpansionException("Non-retryable OpenRouter error for term '" + request.searchTerm() + "'", e);

	    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
		// Model returned something that didn't match the schema despite the constraint;
		// rare with strict JSON schema mode, but not impossible on some providers.
		throw new TermExpansionException(
			"OpenRouter model '" + config.model() + "' returned malformed JSON for term '" + request.searchTerm() + "'", e);
	    } catch (IOException e) {

		lastIoFailure = e;
		log.log(Level.WARNING, "IO error contacting OpenRouter on attempt " + attempt, e);

	    } catch (InterruptedException e) {

		Thread.currentThread().interrupt();
		throw new TermExpansionException("Interrupted while contacting OpenRouter", e);

	    }
	}

	throw new TermExpansionException("Exhausted retries contacting OpenRouter for term '" + request.searchTerm() + "'", lastIoFailure);
    }

    /**
     * @param searchTerm
     * @param dto
     * @return
     */
    private TermExpansionResult toDomainResult(String searchTerm, LlmWireFormat.ExpansionDto dto) {

	List<RelatedTerm> terms = dto.related_terms().stream().map(this::toDomainRelatedTerm).toList();

	return new TermExpansionResult(searchTerm, terms, config.model());
    }

    /**
     * @param dto
     * @return
     */
    private RelatedTerm toDomainRelatedTerm(LlmWireFormat.RelatedTermDto dto) {

	SKOSRelationType type;

	try {
	    type = SKOSRelationType.valueOf(dto.relation_type());
	} catch (IllegalArgumentException e) {
	    log.warning("Unrecognized relation_type '" + dto.relation_type() + "' from model; defaulting to OTHER");
	    type = SKOSRelationType.OTHER;
	}

	String otherLabel = dto.other_relation_label();

	if (type == SKOSRelationType.OTHER && (otherLabel == null || otherLabel.isBlank())) {
	    otherLabel = "unspecified";
	}

	return new RelatedTerm(dto.term(), type, otherLabel, dto.confidence(), dto.rationale());
    }

    @Override
    public void close() {
	client.close();
    }
}
