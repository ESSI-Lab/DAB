/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

import java.io.IOException;

import org.opensearch.client.opensearch._types.OpenSearchException;

import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.mappings.FolderRegistryMapping;

/**
 * @author Fabrizio
 */
public class FolderRegistry {

    private OpenSearchClientWrapper wrapper;

    /**
     * @param database
     * @return
     */
    public static FolderRegistry get(OpenSearchDatabase database) {

	return new FolderRegistry(database);
    }

    /**
     * @return
     */
    private FolderRegistry(OpenSearchDatabase database) {

	this.wrapper = new OpenSearchClientWrapper(database.getClient());
    }

    /**
     * @param folder
     * @throws IOException
     */
    public boolean register(OpenSearchFolder folder) throws IOException {

	IndexData indexData = IndexData.of(folder);

	boolean stored = wrapper.storeWithGenericClient(indexData);

	wrapper.synch();

	return stored;
    }

    /**
     * @param folder
     * @return
     * @throws IOException
     * @throws OpenSearchException
     */
    public boolean deregister(OpenSearchFolder folder) throws OpenSearchException, IOException {

	String index = FolderRegistryMapping.get().getIndex();

	String id = FolderRegistryMapping.getEntryId(folder);

	boolean deleted = wrapper.delete(index, id);

	wrapper.synch();

	return deleted;
    }

    /**
     * @param folder
     * @return
     * @throws IOException
     * @throws OpenSearchException
     */
    public boolean isRegistered(OpenSearchFolder folder) throws OpenSearchException, IOException {

	String index = FolderRegistryMapping.get().getIndex();

	String id = FolderRegistryMapping.getEntryId(folder);

	return wrapper.entryExists(index, id);
    }
}
