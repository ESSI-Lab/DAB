package eu.essi_lab.lib.skos.llm.model;

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
