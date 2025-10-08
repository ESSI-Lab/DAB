/**
 * 
 */
package eu.essi_lab.lib.skoss.concepts_finder.impl;

import java.util.List;

import eu.essi_lab.lib.skoss.FindConceptsQueryBuilder;

/**
 * @author Fabrizio
 */
public class DefaultFindConceptsQueryBuilder implements FindConceptsQueryBuilder {

    @Override
    public String build(String searchTerm, List<String> sourceLangs) {

	String langFilter = String.join(//
		",", //
		sourceLangs.//
			stream().//
			map(lang -> "\"" + lang + "\"").//
			toArray(String[]::new));

	String match = String.format(//
		"FILTER(LANG(?label) IN (%s) && LCASE(STR(?label)) = \"%s\")", //
		langFilter, //
		searchTerm.toLowerCase().replace("\"", "\\\""));

	return String.format("""
		PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
		SELECT DISTINCT ?concept WHERE {
		    { ?concept skos:prefLabel ?label }
		    UNION { ?concept skos:altLabel ?label }
		    UNION { ?concept skos:hiddenLabel ?label }
		    %s
		}
		""", match).trim();
    }
}
