/**
 * 
 */
package eu.essi_lab.lib.skoss.concepts_expander.impl;

import java.util.List;

import eu.essi_lab.lib.skoss.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skoss.ExpandConceptsQueryBuilder;

/**
 * @author Fabrizio
 */
public class DefaultExpandConceptsQueryBuilder implements ExpandConceptsQueryBuilder {

    @Override
    public String build(//
	    String concept, //
	    List<String> searchLangs, //
	    List<String> expansionRelations, //
	    ExpansionLevel target, //
	    ExpansionLevel current) {

	String labelsFilter = String.join(",", searchLangs.stream().map(l -> "\"" + l + "\"").toArray(String[]::new));
	String expansionBlock = current.getValue() < target.getValue() ? buildExpansionOptionalBlock("concept", expansionRelations) : "";
	String closeMatchBlock = current.getValue() < target.getValue() ? "OPTIONAL { ?concept skos:closeMatch ?closeMatch }" : "";

	return String.format("""
		PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
		SELECT DISTINCT ?pref ?alt ?closeMatch ?expanded WHERE {
		    BIND(<%s> AS ?concept)

		    OPTIONAL { ?concept skos:prefLabel ?pref FILTER(LANG(?pref) IN (%s)) }
		    OPTIONAL { ?concept skos:altLabel ?alt FILTER(LANG(?alt) IN (%s)) }

		    %s
		    %s
		}
		""", concept, labelsFilter, labelsFilter, closeMatchBlock, expansionBlock);
    }

    /**
     * @param conceptVar
     * @param relations
     * @return
     */
    private String buildExpansionOptionalBlock(String conceptVar, List<String> relations) {

	StringBuilder sb = new StringBuilder();

	for (String rel : relations) {
	    sb.append("OPTIONAL { ?").append(conceptVar).append(" ").append(rel).append(" ?expanded } ");
	}

	return sb.toString();
    }
}
