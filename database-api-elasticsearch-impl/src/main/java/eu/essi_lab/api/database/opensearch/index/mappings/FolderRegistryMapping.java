/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;

/**
 * @author Fabrizio
 */
public class FolderRegistryMapping extends IndexMapping {

    /**
     * 
     */
    private static final String FOLDER_REGISTRY_INDEX = "folder-registry-index";

    /**
     * 
     */
    public static final String ENTRY_POSTFIX = "_registered-folder";

    /**
     * 
     */
    protected FolderRegistryMapping() {

	super(FOLDER_REGISTRY_INDEX);
    }

    /**
     * @param folder
     * @return
     */
    public static String getEntryId(DatabaseFolder folder) {

	return OpenSearchFolder.getFolderId(folder) + FolderRegistryMapping.ENTRY_POSTFIX;
    }

    /**
     * @param folder
     * @return
     */
    public static String getEntryName(DatabaseFolder folder) {

	return folder.getName() + FolderRegistryMapping.ENTRY_POSTFIX;
    }

    /**
     * @return
     */
    public static final FolderRegistryMapping get() {

	return new FolderRegistryMapping();
    }
}
