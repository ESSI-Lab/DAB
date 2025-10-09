/**
 * 
 */
package eu.essi_lab.lib.skoss.expander.impl;

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

import java.util.List;

import eu.essi_lab.lib.skoss.SKOSSemanticRelation;
import eu.essi_lab.lib.skoss.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skoss.expander.ExpandConceptsQueryBuilder;

/**
 * @author Fabrizio
 */
public class DefaultExpandConceptsQueryBuilder implements ExpandConceptsQueryBuilder {

    /**
     * @param closeMatch
     */
    public DefaultExpandConceptsQueryBuilder() {

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

	return String.format("""
		PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
		SELECT DISTINCT ?pref ?alt ?expanded WHERE {
		    BIND(<%s> AS ?concept)

		    OPTIONAL { ?concept skos:prefLabel ?pref FILTER(LANG(?pref) IN (%s)) }
		    OPTIONAL { ?concept skos:altLabel ?alt FILTER(LANG(?alt) IN (%s)) }

		    %s
		}
		""", concept, labelsFilter, labelsFilter, expansionBlock).trim();
    }

    /**
     * @param conceptVar
     * @param relations
     * @return
     */
    protected String buildExpansionOptionalBlock(String conceptVar, List<SKOSSemanticRelation> relations) {

	StringBuilder sb = new StringBuilder();

	for (int i = 0; i < relations.size(); i++) {
	    SKOSSemanticRelation rel = relations.get(i);

	    sb.append("{ OPTIONAL { ?").//
		    append(conceptVar).//
		    append(" ").//
		    append(rel.getLabel()).//
		    append(" ?expanded } }");

	    if (i != relations.size() - 1) {
		sb.append(" UNION ");
	    }
	}

	return sb.toString();
    }
}
