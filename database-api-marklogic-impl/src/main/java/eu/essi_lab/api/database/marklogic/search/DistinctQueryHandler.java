/**
 * 
 */
package eu.essi_lab.api.database.marklogic.search;

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

import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.Queryable;

/**
 * @author Fabrizio
 */
public class DistinctQueryHandler {

    /**
     * @param message
     * @param ctsSearch
     * @return
     */
    public static String handleCount(DiscoveryMessage message, MarkLogicDiscoveryBondHandler handler) {

	Queryable element = message.getDistinctValuesElement().get();

	String ctsSearchQuery = handler.getCTSSearchQuery(true);

	return "count(cts:element-values(fn:QName('" + CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI + "','" + element.getName()
		+ "'), (), ('eager'), " + ctsSearchQuery + "))";
    }

    /**
     * @param message
     * @param originalQuery
     * @param dataBase
     * @param count
     * @param start
     * @return
     */
    public static String handleSearch(DiscoveryMessage message, String originalQuery, MarkLogicDatabase dataBase, int count, int start) {

	Queryable queryable = message.getDistinctValuesElement().get();

	String letQuery = "let $query := " + originalQuery + "\n\n";

	String lexiconRead = "for $y in subsequence(cts:element-values(fn:QName('" + CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI + "','"
		+ queryable.getName() + "'), (), ('eager'), $query)," + start + "," + count + ")\n\n";

	String letSearch = "let $x := cts:search(doc()[gs:Dataset or gs:DatasetCollection or gs:Document or gs:Ontology or gs:Service or gs:Observation],\n";
	letSearch += "cts:and-query((\n";
	//
	//
	//
	letSearch += " $query,\n"; // unless problems occur, this is not required and without the query execution is much more fast
	//
	//
	//
	letSearch += "cts:element-range-query(fn:QName('" + CommonNameSpaceContext.GS_DATA_MODEL_SCHEMA_URI + "','" + queryable.getName()
		+ "'),'=',$y,('score-function=linear'),0.0)\n";
	letSearch += ")),('unfiltered','score-simple'),0)[1 to 1]\n\n";

	return letQuery + lexiconRead + letSearch;
    }
}
