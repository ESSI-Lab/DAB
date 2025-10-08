/**
 * 
 */
package eu.essi_lab.lib.skoss;

import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public enum SKOSSemanticRelation implements LabeledEnum {

    /**
     * 
     */
    SEMANTIC_RELATION("skos:semanticRelation"),
    /**
     * 
     */
    RELATED("skos:related"),
    /**
     * 
     */
    RELATED_MATCH("skos:relatedMatch"),
    /**
    * 
    */
    BROADER_TRANSITIVE("skos:broaderTransitive"),
    /**
    * 
    */
    BROADER("skos:broader"),
    /**
    * 
    */
    BROAD_MATCH("skos:broadMatch"),
    /**
     * 
     */
    NARROWER_TRANSITIVE("skos:narrowerTransitive"),
    /**
     * 
     */
    NARROWER("skos:narrower"),
    /**
     * 
     */
    NARROW_MATCH("skos:narrowMatch"),
    /**
     * 
     */
    MAPPING_RELATION("skos:mappingRelation"),
    /**
     * 
     */
    CLOSE_MATCH("skos:closeMatch"),
    /**
     * 
     */
    EXACT_MATCH("skos:exactMatch");

    private String label;

    /**
     * @param label
     */
    private SKOSSemanticRelation(String label) {

	this.label = label;
    }

    @Override
    public String getLabel() {
	return label;
    }

}
