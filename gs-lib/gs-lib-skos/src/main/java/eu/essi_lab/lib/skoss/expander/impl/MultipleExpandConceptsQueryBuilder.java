package eu.essi_lab.lib.skoss.expander.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import eu.essi_lab.lib.skoss.SKOSSemanticRelation;
import eu.essi_lab.lib.skoss.expander.ConceptsExpander.ExpansionLevel;

public class MultipleExpandConceptsQueryBuilder extends DefaultExpandConceptsQueryBuilder {
    @Override
    public String build(//
	    Collection<String> concepts, //
	    List<String> searchLangs, //
	    List<SKOSSemanticRelation> expansionRelations, //
	    ExpansionLevel target, //
	    ExpansionLevel current) {

	String labelsFilter = String.join(",", searchLangs.stream().map(l -> "\"" + l + "\"").toArray(String[]::new));
	String expansionBlock = current.getValue() < target.getValue() ? buildExpansionOptionalBlock("concept", expansionRelations) : "";

	String values = "";

	Iterator<String> iterator = concepts.iterator();
	while (iterator.hasNext()) {
	    String value = (String) iterator.next();
	    values += "<" + value + ">\n";
	}

	return String.format("""
		PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
		SELECT DISTINCT ?concept ?pref ?alt ?expanded WHERE {
		    VALUES ?concept {
		  %s
		}

		    OPTIONAL { ?concept skos:prefLabel ?pref FILTER(LANG(?pref) IN (%s)) }
		    OPTIONAL { ?concept skos:altLabel ?alt FILTER(LANG(?alt) IN (%s)) }

		    %s
		}
		""", values, labelsFilter, labelsFilter, expansionBlock).trim();
    }
}
