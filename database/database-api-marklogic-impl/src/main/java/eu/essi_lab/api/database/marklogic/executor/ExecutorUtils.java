/**
 * 
 */
package eu.essi_lab.api.database.marklogic.executor;

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

import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
public class ExecutorUtils {

    /**
     * @return
     */
    public static String getCountDeletedQuery() {

	String query = "xquery version \"1.0-ml\";\n";

	query += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\";\n";

	query += "xdmp:estimate(cts:search(doc(),cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','isDeleted'),'=','true',";

	query += "(\"score-function=linear\"),0.0),(\"unfiltered\", \"score-simple\"),0))\n";

	return query;
    }

    /**
     * @param count
     * @return
     */
    public static String getClearDeletedQuery() {

	String query = "xquery version \"1.0-ml\";\n";
	query += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\";\n";

	query += "for $doc in cts:search(doc()[gs:Dataset or gs:DatasetCollection or gs:Document or gs:Ontology or gs:Service or gs:Observation],\n";

	query += "cts:element-range-query(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','isDeleted'),'=','true',(\"score-function=linear\"),0.0) \n";

	query += ",(\"unfiltered\",\"score-simple\"),0)\n";

	query += "return xdmp:document-delete(fn:document-uri($doc))";

	return query;
    }

    /**
     * @param element
     * @param parsedQuery
     * @param start
     * @param count
     * @return
     */
    public static String buildIndexValuesQuery(MetadataElement element, String parsedQuery, int start, int count) {

	String fquery = "xquery version \"1.0-ml\";\n";
	fquery += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\";\n";
	fquery += "let $query := \n";
	fquery += parsedQuery + "\n";
	fquery += "return subsequence( cts:element-values(fn:QName('http://flora.eu/gi-suite/1.0/dataModel/schema','" + element.getName()
		+ "') ,(), (), ";
	fquery += "$query), " + start + ", " + count + ")";
	fquery += ",xdmp:query-trace(false());";

	return fquery;
    }

    /**
     * @param ctsSearchQuery
     * @param maxIdsCount
     * @return
     */
    public static String buildPartitionQuery(String ctsSearchQuery, int maxIdsCount) {

	String query = "xquery version \"1.0-ml\";\n";
	query += "import module namespace gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\" at \"/gs-modules/functions-module.xqy\";\n";
	query += "let $query := \n";
	query += ctsSearchQuery + "\n";
	// query += "return cts:search(doc()[gs:Dataset],$query,(\"unfiltered\",\"score-simple\"),0)[1 to " +
	// maxIdsCount
	// + "]//gs:fileId/text()";
	//
	query += "let $x := cts:search(doc()[gs:Dataset],$query,(\"unfiltered\",\"score-simple\"),0)[1 to " + maxIdsCount + "]\n";
	query += "return \n";
	query += "concat(\n";
	query += "	\"{\"\"\", \n";
	query += "	\"id\"\":\"\"\", $x//gs:fileId/text(),\n";
	query += "	\"\"\",\"\"box\"\":\"\"\", $x//gs:west/text() , \" \", $x//gs:south/text() ,\" \", $x//gs:east/text() ,\" \", $x//gs:north/text(),\"\",\n";
	query += "	\"\"\",\"\"polygon\"\":\"\"\", $x//*:satelliteScene/*:footprint/text() ,\"\"\"\",\n";
	query += "\"}\"  )\n";
	return query;
    }

}
