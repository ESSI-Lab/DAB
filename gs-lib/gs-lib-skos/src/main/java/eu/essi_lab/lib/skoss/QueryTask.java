/**
 * 
 */
package eu.essi_lab.lib.skoss;

import java.util.List;

/**
 * @author Fabrizio
 */
public interface QueryTask {

    /**
     * @param endpointUrl
     * @param query
     * @param concepts
     * @return
     */
    SKOSResponse query(String endpointUrl, String query, List<SKOSConcept> concepts);
}
