/**
 * 
 */
package eu.essi_lab.lib.skoss;

import java.util.List;

/**
 * @author Fabrizio
 */
@FunctionalInterface
public interface FedXConceptsQueryExecutor {

    /**
     * @param engine
     * @param queryBuilder
     * @param searchTerm
     * @param ontologyUrls
     * @param sourceLangs
     * @return
     * @throws Exception
     */
    List<String> execute(//
	    FedXEngine engine, //
	    FindConceptsQueryBuilder queryBuilder, //
	    String searchTerm, //
	    List<String> ontologyUrls, //
	    List<String> sourceLangs) throws Exception;
}
