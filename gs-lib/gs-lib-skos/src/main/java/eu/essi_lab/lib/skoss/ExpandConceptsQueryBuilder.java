/**
 * 
 */
package eu.essi_lab.lib.skoss;

import java.util.List;

import eu.essi_lab.lib.skoss.ConceptsExpander.ExpansionLevel;

/**
 * @author Fabrizio
 */
@FunctionalInterface
public interface ExpandConceptsQueryBuilder {

    /**
     * @param concept
     * @param searchLangs
     * @param expansionRelations
     * @param target
     * @param current
     * @return
     */
    String build(//
	    String concept, //
	    List<String> searchLangs, //
	    List<SKOSSemanticRelation> expansionRelations, //
	    ExpansionLevel target, //
	    ExpansionLevel current);
}
