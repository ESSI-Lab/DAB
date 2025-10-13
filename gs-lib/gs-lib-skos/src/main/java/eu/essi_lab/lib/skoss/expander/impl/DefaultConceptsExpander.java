/**
 * 
 */
package eu.essi_lab.lib.skoss.expander.impl;

import java.util.List;

import eu.essi_lab.lib.skoss.SKOSResponse;
import eu.essi_lab.lib.skoss.SKOSSemanticRelation;
import eu.essi_lab.lib.skoss.expander.ConceptsExpander;
import eu.essi_lab.lib.skoss.expander.ExpansionLimit;

/**
 * @author Fabrizio
 */
public class DefaultConceptsExpander implements ConceptsExpander {

    @Override
    public SKOSResponse expand(//
	    List<String> concepts, //
	    List<String> ontologyUrls, //
	    List<String> sourceLangs, //
	    List<String> searchLangs, //
	    List<SKOSSemanticRelation> expansionRelations, //
	    ExpansionLevel targetLevel, //
	    ExpansionLimit limit) throws Exception {

	return null;
    }
}
