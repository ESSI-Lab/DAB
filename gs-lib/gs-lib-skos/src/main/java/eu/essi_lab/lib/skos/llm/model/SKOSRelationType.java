package eu.essi_lab.lib.skos.llm.model;

/**
 * Mirrors the standard SKOS semantic relation vocabulary, plus an OTHER bucket for relations the model proposes that don't map cleanly onto
 * SKOS (e.g. "is a synonym in domain X", "commonly co-occurs with").
 * The {@code skosProperty} value is what a caller would use if they wanted to round-trip a result back into an actual SKOS graph (e.g.
 * skos:broader).
 */
public enum SKOSRelationType {

    BROADER("skos:broader"),//
    NARROWER("skos:narrower"),//
    RELATED("skos:related"),//
    EXACT_MATCH("skos:exactMatch"),//
    CLOSE_MATCH("skos:closeMatch"),//
    BROAD_MATCH("skos:broadMatch"),//
    NARROW_MATCH("skos:narrowMatch"),//
    RELATED_MATCH("skos:relatedMatch"),//

    CUSTOM(null),//

    /**
     * Non-SKOS relation; see {@link eu.essi_lab.lib.skos.llm.model.RelatedTerm#otherRelationLabel()}.
     */
    OTHER(null);

    /**
     *
     */
    private final String skosProperty;

    /**
     * @param skosProperty
     */
    SKOSRelationType(String skosProperty) {

        this.skosProperty = skosProperty;
    }

    /**
     * @return
     */
    public String skosProperty() {

        return skosProperty;
    }
}
