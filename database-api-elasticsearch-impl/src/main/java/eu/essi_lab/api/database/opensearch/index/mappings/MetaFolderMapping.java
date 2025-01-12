/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

import org.opensearch.client.opensearch._types.mapping.FieldType;

/**
 * @author Fabrizio
 */
public class MetaFolderMapping extends IndexMapping {

    /**
     * 
     */
    public static final String META_FOLDER_INDEX = "meta-folder-index";

    //
    // meta-folder-index properties
    //
    public static final String SOURCE_ID = "sourceId";
    public static final String DATA_FOLDER = "dataFolder";
    public static final String HARVESTING_PROPERTIES = "harvestingProperties";
    public static final String INDEX_DOC = "indexDoc";
    public static final String ERRORS_REPORT = "errorsReport";
    public static final String WARN_REPORT = "warnReport";

    /**
     * @return
     */
    public static final MetaFolderMapping get() {

	return new MetaFolderMapping();
    }

    /**
     * 
     */
    private MetaFolderMapping() {

	super(META_FOLDER_INDEX);

	// mandatory
	addProperty(SOURCE_ID, FieldType.Text.jsonValue());

	// optional, only one of them can be set
	addProperty(HARVESTING_PROPERTIES, FieldType.Binary.jsonValue());
	addProperty(ERRORS_REPORT, FieldType.Binary.jsonValue());
	addProperty(WARN_REPORT, FieldType.Binary.jsonValue());
	addProperty(INDEX_DOC, FieldType.Binary.jsonValue());
	// set only when the index doc is stored
	addProperty(DATA_FOLDER, FieldType.Text.jsonValue()); // data-1 or data-2
    }
}
