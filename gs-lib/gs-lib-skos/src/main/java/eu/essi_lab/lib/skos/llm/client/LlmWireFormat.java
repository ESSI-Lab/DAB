package eu.essi_lab.lib.skos.llm.client;

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
