/**
 * 
 */
package eu.essi_lab.lib.skoss;

import java.util.List;

/**
 * @author Fabrizio
 */
@FunctionalInterface
public interface FindConceptsQueryBuilder {

    /**
     * @param searchTerm
     * @param sourceLangs
     * @return
     */
    String build(String searchTerm, List<String> sourceLangs);
}
