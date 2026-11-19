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

/**
 * One related term proposed by the LLM for a given search term.
 *
 * @param term                the related term/label
 * @param relationType        the SKOS (or OTHER) relation to the search term
 * @param otherRelationLabel  free-text description of the relation, only populated
 *                            when relationType == OTHER (e.g. "co-occurs frequently")
 * @param confidence          model's self-reported confidence in [0.0, 1.0]
 * @param rationale           short explanation of why this term was proposed;
 *                            useful for audit/debugging and for filtering weak guesses
 */
public record RelatedTerm( //
        String term,//
        SKOSRelationType relationType,//
        String otherRelationLabel,//
        double confidence,//
        String rationale//
) {
    /**
     *
     * @param term
     * @param relationType
     * @param otherRelationLabel
     * @param confidence
     * @param rationale
     */
    public RelatedTerm {

        if (term == null || term.isBlank()) {

            throw new IllegalArgumentException("term must not be blank");
        }

        if (relationType == null) {

            throw new IllegalArgumentException("relationType must not be null");
        }

        if (confidence < 0.0 || confidence > 1.0) {

            throw new IllegalArgumentException("confidence must be in [0.0, 1.0], was " + confidence);
        }

        if (relationType == SKOSRelationType.OTHER && (otherRelationLabel == null || otherRelationLabel.isBlank())) {

            throw new IllegalArgumentException("otherRelationLabel is required when relationType == OTHER");
        }
    }
}
