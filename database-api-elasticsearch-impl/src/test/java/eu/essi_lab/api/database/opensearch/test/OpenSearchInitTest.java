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
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase.OpenSearchServiceType;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class OpenSearchInitTest {

    @Test
    public void databaseProviderTest() throws GSException {

	StorageInfo storageInfo = new StorageInfo();
	storageInfo.setIdentifier("test");
	storageInfo.setType(OpenSearchServiceType.OPEN_SEARCH_MANAGED.getType());
	storageInfo.setUser("admin");
	storageInfo.setPassword("admin");
	storageInfo.setUri("http://localhost:9200");

	Database database = DatabaseFactory.get(storageInfo);
	assertNotNull(database);

	String identifier = database.getIdentifier();
	assertEquals("test", identifier);
    }

    /**
     * @param index
     * @throws OpenSearchException
     * @throws IOException
     * @throws GSException
     */
    @Test
    public void tesIndexesCreation() throws OpenSearchException, IOException, GSException {

	StorageInfo storageInfo = new StorageInfo();
	storageInfo.setIdentifier("test");
	storageInfo.setType(OpenSearchServiceType.OPEN_SEARCH_MANAGED.getType());
	storageInfo.setUser("admin");
	storageInfo.setPassword("admin");
	storageInfo.setUri("http://localhost:9200");

	//
	//
	//

	OpenSearchDatabase database = new OpenSearchDatabase();
	database.initialize(storageInfo);

	//
	//
	//

	for (String index : OpenSearchDatabase.INDEXES_LIST) {

	    ExistsRequest existsIndexRequest = new ExistsRequest.Builder().index(index).build();

	    boolean created = database.getClient().indices().exists(existsIndexRequest).value();

	    Assert.assertTrue(created);
	}
    }
}
