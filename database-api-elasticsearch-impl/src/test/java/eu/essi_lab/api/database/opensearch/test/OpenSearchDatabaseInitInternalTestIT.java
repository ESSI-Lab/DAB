/**
 *
 */
package eu.essi_lab.api.database.opensearch.test;

import eu.essi_lab.model.StorageInfo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch.indices.ExistsRequest;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class OpenSearchDatabaseInitInternalTestIT {

    public OpenSearchDatabase createDataBase() throws GSException {
	//	return OpenSearchDatabase.createLocalService();
	OpenSearchDatabase database = new OpenSearchDatabase();

	database.initialize(initStorage());

	return database;

    }

    private StorageInfo initStorage() {
	//	return OpenSearchDatabase.createLocalServiceInfo();
	StorageInfo esStorage = new StorageInfo(System.getProperty("es.host"));

	esStorage.setUser(System.getProperty("es.user"));

	esStorage.setPassword(System.getProperty("es.password"));

	esStorage.setName("test");
	esStorage.setIdentifier("test");

	esStorage.setType(Database.OpenSearchServiceType.OPEN_SEARCH_LOCAL.getProtocol());
	return esStorage;

    }

    @Test
    public void databaseProviderTest() throws GSException {

	Database database = createDataBase();
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

	OpenSearchDatabase database = createDataBase();

	//
	//
	//

	for (IndexMapping mapping : IndexMapping.getMappings()) {

	    ExistsRequest existsIndexRequest = new ExistsRequest.Builder().index(mapping.getIndex()).build();

	    boolean created = database.getClient().indices().exists(existsIndexRequest).value();

	    Assert.assertTrue(created);
	}
    }
}
