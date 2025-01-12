/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

import eu.essi_lab.api.database.Database;

/**
 * @author Fabrizio
 */
public class AugmentersMapping extends IndexMapping {

    public static final String AUGMENTERS_INDEX = Database.AUGMENTERS_FOLDER + "-index";

    /**
     * @param index
     */
    protected AugmentersMapping() {

	super(AUGMENTERS_INDEX);
    }

    /**
     * @return
     */
    public static final AugmentersMapping get() {

	return new AugmentersMapping();
    }
}
