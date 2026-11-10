package eu.essi_lab.lib.skos.llm.client;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

/**
 * Plain DTOs matching exactly the JSON Schema we hand to the LLM via {@code response_format}. Kept separate from the domain model
 * ({@code eu.essi_lab.lib.skos.llm.model}) so that a wire-format change (e.g. the model vendor tweaks field naming conventions) doesn't ripple into
 * the rest of the codebase — only {@code LlmResponseMapper} needs to change.
 */
public final class LlmWireFormat {

    private LlmWireFormat() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RelatedTermDto( //
	    String term,//
	    String relation_type, // one of the SKOSRelationType enum names, or "OTHER"
	    String other_relation_label, // populated only when relation_type == "OTHER"
	    double confidence,//
	    String rationale//
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExpansionDto(List<RelatedTermDto> related_terms) {
    }
}
