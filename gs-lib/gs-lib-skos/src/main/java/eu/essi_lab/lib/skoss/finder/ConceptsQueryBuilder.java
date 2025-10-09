/**
 * 
 */
package eu.essi_lab.lib.skoss.finder;

import java.util.List;

/**
 * @author Fabrizio
 */
@FunctionalInterface
public interface ConceptsQueryBuilder {

    /**
     * @param searchTerm
     * @param sourceLangs
     * @return
     */
    String build(String searchTerm, List<String> sourceLangs);
}
