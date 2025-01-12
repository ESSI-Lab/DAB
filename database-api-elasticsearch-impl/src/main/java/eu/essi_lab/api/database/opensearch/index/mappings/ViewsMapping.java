/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

import eu.essi_lab.api.database.Database;

/**
 * @author Fabrizio
 */
public class ViewsMapping extends IndexMapping {

    public static final String VIEWS_INDEX = Database.VIEWS_FOLDER + "-index";

    /**
    * 
    */
    protected ViewsMapping() {

	super(VIEWS_INDEX);
    }

    /**
     * @return
     */
    public static final ViewsMapping get() {

	return new ViewsMapping();
    }
}
