/**
 * 
 */
package eu.essi_lab.lib.skoss.expander.impl;

import java.util.List;

import eu.essi_lab.lib.skoss.SKOSSemanticRelation;
import eu.essi_lab.lib.skoss.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skoss.expander.ExpandConceptsQueryBuilder;

/**
 * @author Fabrizio
 */
public class CloseMatchExpandConceptsQueryBuilder implements ExpandConceptsQueryBuilder{

    /**
     * @author Fabrizio
     */

    private boolean closeMatch;

    /**
     * @param closeMatch
     */
    public CloseMatchExpandConceptsQueryBuilder() {

    }

    /**
     * @param closeMatch
     */
    public CloseMatchExpandConceptsQueryBuilder(boolean closeMatch) {

	selectCloseMatch(closeMatch);
    }

    /**
     * @return
     */
    public boolean isCloseMatchSelected() {

	return closeMatch;
    }

    /**
     * @param closeMatch
     */
    public void selectCloseMatch(boolean closeMatch) {

	this.closeMatch = closeMatch;
    }

    @Override
    public String build(//
	    String concept, //
	    List<String> searchLangs, //
	    List<SKOSSemanticRelation> expansionRelations, //
	    ExpansionLevel target, //
	    ExpansionLevel current) {

	String labelsFilter = String.join(",", searchLangs.stream().map(l -> "\"" + l + "\"").toArray(String[]::new));
	String expansionBlock = current.getValue() < target.getValue() ? buildExpansionOptionalBlock("concept", expansionRelations) : "";
	String closeMatchBlock = current.getValue() < target.getValue() ? "OPTIONAL { ?concept skos:closeMatch ?closeMatch }" : "";

	if (closeMatch) {

	    return String.format("""
	    	PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
	    	SELECT DISTINCT ?pref ?alt ?closeMatch ?expanded WHERE {
	    	    BIND(<%s> AS ?concept)

	    	    OPTIONAL { ?concept skos:prefLabel ?pref FILTER(LANG(?pref) IN (%s)) }
	    	    OPTIONAL { ?concept skos:altLabel ?alt FILTER(LANG(?alt) IN (%s)) }

	    	    %s
	    	    %s
	    	}
	    	""", concept, labelsFilter, labelsFilter, closeMatchBlock, expansionBlock).trim();
	}

	return String.format("""
		PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
		SELECT DISTINCT ?pref ?alt ?expanded WHERE {
		    BIND(<%s> AS ?concept)

		    OPTIONAL { ?concept skos:prefLabel ?pref FILTER(LANG(?pref) IN (%s)) }
		    OPTIONAL { ?concept skos:altLabel ?alt FILTER(LANG(?alt) IN (%s)) }

		    %s
		    %s
		}
		""", concept, labelsFilter, labelsFilter, closeMatchBlock, expansionBlock).trim();
    }

    /**
     * @param conceptVar
     * @param relations
     * @return
     */
    protected String buildExpansionOptionalBlock(String conceptVar, List<SKOSSemanticRelation> relations) {

	StringBuilder sb = new StringBuilder();

	for (SKOSSemanticRelation rel : relations) {

	    sb.append("OPTIONAL { ?").//
		    append(conceptVar).//
		    append(" ").//
		    append(rel.getLabel()).//
		    append(" ?expanded } ");
	}

	return sb.toString();
    }

}
