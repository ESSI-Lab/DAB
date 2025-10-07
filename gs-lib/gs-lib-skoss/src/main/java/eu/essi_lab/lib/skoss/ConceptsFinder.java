/**
 * 
 */
package eu.essi_lab.lib.skoss;

import java.util.List;

/**
 * @author Fabrizio
 */
@FunctionalInterface
public interface ConceptsFinder {

    /**
     * @param searchTerm
     * @param ontologyUrls
     * @param sourceLangs
     * @return
     * @throws Exception
     */
    List<String> find(String searchTerm, List<String> ontologyUrls, List<String> sourceLangs) throws Exception;
}
