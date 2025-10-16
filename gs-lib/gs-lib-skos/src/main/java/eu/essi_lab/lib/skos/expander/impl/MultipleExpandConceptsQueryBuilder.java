package eu.essi_lab.lib.skos.expander.impl;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import eu.essi_lab.lib.skos.SKOSSemanticRelation;
import eu.essi_lab.lib.skos.expander.ConceptsExpander.ExpansionLevel;

public class MultipleExpandConceptsQueryBuilder extends DefaultExpandConceptsQueryBuilder {
    @Override
    public String build(//
	    Collection<String> concepts, //
	    List<String> searchLangs, //
	    List<SKOSSemanticRelation> relations, //
	    ExpansionLevel target, //
	    ExpansionLevel current) {

	String labelsFilter = String.join(",", searchLangs.stream().map(l -> "\"" + l + "\"").toArray(String[]::new));
	String expansionBlock = current.getValue() < target.getValue() ? buildExpansionOptionalBlock("concept", relations) : "";

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
