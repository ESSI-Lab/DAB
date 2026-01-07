/**
 * 
 */
package eu.essi_lab.lib.skos.finder.impl;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.lib.skos.finder.ConceptsQueryBuilder;

/**
 * @author Fabrizio
 */
public class DefaultConceptsQueryBuilder implements ConceptsQueryBuilder {

    @Override
    public String build(String searchTerm, List<String> sourceLangs) {

	String langFilter = sourceLangs.//
		stream().//
		map(lang -> "\"" + lang + "\"").//
		collect(Collectors.joining(","));

	String match = langFilter.isEmpty() ?

		String.format(//
			"FILTER(LCASE(STR(?label)) = \"%s\")", //
			searchTerm.toLowerCase().replace("\"", "\\\""))
		: String.format(//
			"FILTER(LANG(?label) IN (%s) && LCASE(STR(?label)) = \"%s\")", //
			langFilter, //
			searchTerm.toLowerCase().replace("\"", "\\\""));

	return String.format("""
		PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
		PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
		SELECT DISTINCT ?concept WHERE {
		    { ?concept skos:prefLabel ?label }
		    UNION { ?concept skos:altLabel ?label }
		    UNION { ?concept rdfs:label ?label }
		    UNION { ?concept skos:hiddenLabel ?label }
		    %s
		}
		""", match).trim();
    }
}
