/**
 *
 */
package eu.essi_lab.cfga.source.test;

import java.io.IOException;

import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.api.database.cfg.DatabaseSource;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class OpenSearchSourceInternalTestIT extends DatabaseSourceTest {

    public static final StorageInfo INFO = new StorageInfo(System.getProperty("es.host"));

    //    private static StorageInfo INFO;
    //    static {
    //
    //	INFO = new StorageInfo();
    //	INFO.setIdentifier("test");
    //	INFO.setName(Database.CONFIGURATION_FOLDER);
    //	INFO.setType(OpenSearchServiceType.OPEN_SEARCH_MANAGED.getProtocol());
    //	INFO.setUser("admin");
    //	INFO.setPassword("admin");
    //	INFO.setUri("http://localhost:9200");
    //    }

    @Before
    public void clear() throws GSException, OpenSearchException, IOException {

	System.setProperty("initIndexes", "true");

	INFO.setUser(System.getProperty("es.user"));

	INFO.setPassword(System.getProperty("es.password"));
	INFO.setIdentifier("test");
	INFO.setName(Database.CONFIGURATION_FOLDER);

	INFO.setType(Database.OpenSearchServiceType.OPEN_SEARCH_LOCAL.getProtocol());

	OpenSearchClient client = OpenSearchDatabase.createNoSSLContextClient(INFO);

	for (String index : IndexMapping.getIndexes(false)) {

	    if (IndexMapping.checkIndex(client, index)) {

		DeleteIndexRequest indexRequest = new DeleteIndexRequest.Builder().//
			index(index).//
			build();

		client.indices().delete(indexRequest);
	    }
	}
	OpenSearchDatabase database = (OpenSearchDatabase) DatabaseFactory.get(INFO);
	IndexMapping.initializeIndexes(client);
    }

    @Test
    public void locationTest() throws Exception {

	DatabaseSource source = create();
	Assert.assertEquals("test_configuration\\test-config.json", source.getLocation());
    }

    /**
     * @return
     * @throws GSException
     */
    protected DatabaseSource create() throws Exception {

	return DatabaseSource.of(DatabaseImpl.OPENSEARCH, storageInfo, "test-config");
    }

    /**
     * @param info
     */
    public OpenSearchSourceInternalTestIT() {

	super(INFO);
    }

}
