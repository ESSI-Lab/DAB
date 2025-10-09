/**
 * 
 */
package eu.essi_lab.lib.skoss.finder;

import java.util.List;

/**
 * @author Fabrizio
 */
@FunctionalInterface
public interface ConceptsQueryExecutor {

    /**
     * @param queryBuilder
     * @param searchTerm
     * @param ontologyUrls
     * @param sourceLangs
     * @return
     * @throws Exception
     */
    List<String> execute(//
	    ConceptsQueryBuilder queryBuilder, //
	    String searchTerm, //
	    List<String> ontologyUrls, //
	    List<String> sourceLangs) throws Exception;
}
