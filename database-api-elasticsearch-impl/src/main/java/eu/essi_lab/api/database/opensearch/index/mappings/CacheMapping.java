/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index.mappings;

import org.opensearch.client.opensearch._types.mapping.FieldType;

import eu.essi_lab.api.database.Database;

/**
 * @author Fabrizio
 */
public class CacheMapping extends IndexMapping {

    /**
     * 
     */
    private static final String CACHE_INDEX = Database.CACHE_FOLDER + "-index";

    /**
     * 
     */
    public static final String CACHED_ENTRY = "cachedData";

    /**
     * @param index
     */
    protected CacheMapping() {

	super(CACHE_INDEX);

	addProperty(CACHED_ENTRY, FieldType.Binary.jsonValue());
    }

    /**
     * @return
     */
    public static final CacheMapping get() {

	return new CacheMapping();
    }

}
