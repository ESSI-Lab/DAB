/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

/**
 * @author Fabrizio
 */
public class MiscMapping extends IndexMapping {

    public static final String MISC_INDEX = "misc-index";

    /**
     * 
     */
    protected MiscMapping() {

	super(MISC_INDEX);
    }

    /**
     * @return
     */
    public static final MiscMapping get() {

	return new MiscMapping();
    }

}
