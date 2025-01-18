/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

/**
 * @author Fabrizio
 */
public class FoldersMapping extends IndexMapping {

    /**
     * 
     */
    public static final String FOLDERS_INDEX = "folders-index";

    /**
     * 
     */
    protected FoldersMapping() {

	super(FOLDERS_INDEX);
    }

    /**
     * @return
     */
    public static final FoldersMapping get() {

	return new FoldersMapping();
    }
}
