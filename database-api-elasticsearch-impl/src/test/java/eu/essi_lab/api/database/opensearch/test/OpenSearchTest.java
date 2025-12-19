/**
 *
 */
package eu.essi_lab.api.database.opensearch.test;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;
import java.io.IOException;

import java.util.UUID;
import org.junit.Before;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;

import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class OpenSearchTest {

    private GSLoggerFactory.GSLogger logger = GSLoggerFactory.getLogger(OpenSearchTest.class);

    /**
     * @param args
     * @throws OpenSearchException
     * @throws GSException
     * @throws IOException
     */
    public static void main(String[] args) throws OpenSearchException, GSException, IOException {

	new OpenSearchTest().before();

	System.exit(0);
    }

    //    public static final StorageInfo ES_STORAGE = new StorageInfo(System.getProperty("es.host"));

    @Before
    public void before() throws GSException, IOException {

	System.setProperty("initIndexes", "true");

	StorageInfo es = initStorage();

	logger.debug("Executing Before {}", es.getUri());

	OpenSearchClient client = OpenSearchDatabase.createClient(es);
	for (String index : IndexMapping.getIndexes(false)) {

	    if (DataFolderMapping.checkIndex(client, index)) {

		DeleteIndexRequest indexRequest = new DeleteIndexRequest.Builder().//
			index(index).//
			build();

		client.indices().delete(indexRequest);
	    }
	}
	logger.debug("Completed Before");
    }

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

	esStorage.setName(UUID.randomUUID().toString());

	esStorage.setType(Database.OpenSearchServiceType.OPEN_SEARCH_LOCAL.getProtocol());
	return esStorage;

    }
}
