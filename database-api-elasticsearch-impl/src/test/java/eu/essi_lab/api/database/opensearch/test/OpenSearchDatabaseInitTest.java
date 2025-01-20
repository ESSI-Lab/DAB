/**
 * 
 */
package eu.essi_lab.api.database.opensearch.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch.indices.ExistsRequest;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.Database.OpenSearchServiceType;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class OpenSearchDatabaseInitTest {

    /**
     * @return
     */
    public static StorageInfo createStorageInfo() {

	StorageInfo storageInfo = new StorageInfo();
	// the identifier is set same as name
	storageInfo.setName("testDb");
	storageInfo.setType(OpenSearchServiceType.OPEN_SEARCH_MANAGED.getProtocol());
	storageInfo.setUser("admin");
	storageInfo.setPassword("admin");
	storageInfo.setUri("http://localhost:9200");

	return storageInfo;
    }

    /**
     * @return
     * @throws GSException
     */
    public static OpenSearchDatabase create() throws GSException {

	StorageInfo storageInfo = createStorageInfo();

	OpenSearchDatabase database = (OpenSearchDatabase) DatabaseFactory.get(storageInfo);

	database.initializeIndexes();

	return database;
    }

    @Test
    public void databaseProviderTest() throws GSException {

	Database database = create();
	assertNotNull(database);

	String identifier = database.getIdentifier();
	assertEquals("testDb", identifier);
    }

    /**
     * @param index
     * @throws OpenSearchException
     * @throws IOException
     * @throws GSException
     */
    @Test
    public void tesIndexesCreation() throws OpenSearchException, IOException, GSException {

	OpenSearchDatabase database = create();

	//
	//
	//

	for (IndexMapping mapping : IndexMapping.MAPPINGS) {

	    ExistsRequest existsIndexRequest = new ExistsRequest.Builder().index(mapping.getIndex()).build();

	    boolean created = database.getClient().indices().exists(existsIndexRequest).value();

	    Assert.assertTrue(created);
	}
    }
}
