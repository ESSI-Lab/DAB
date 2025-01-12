/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

/**
 * @author Fabrizio
 */
public class DataFolderMapping extends IndexMapping {

    public static final String DATA_FOLDER_INDEX = "data-folder-index";

    /**
     * 
     *  
     */
    protected DataFolderMapping() {

	super(DATA_FOLDER_INDEX);
    }

    /**
     * @return
     */
    public static final DataFolderMapping get() {

	return new DataFolderMapping();
    }
}
