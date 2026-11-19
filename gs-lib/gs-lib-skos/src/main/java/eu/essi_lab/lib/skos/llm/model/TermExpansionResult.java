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
