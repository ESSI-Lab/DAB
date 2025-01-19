/**
 * 
 */
package eu.essi_lab.cfga.source.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.cfg.DatabaseSource;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase.OpenSearchServiceType;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class OpenSearchSourceTest extends DatabaseSourceTest {

    private static StorageInfo INFO;

    static {

	INFO = new StorageInfo();
	INFO.setIdentifier("test");
	INFO.setName(Database.CONFIGURATION_FOLDER);
	INFO.setType(OpenSearchServiceType.OPEN_SEARCH_MANAGED.getProtocol());
	INFO.setUser("admin");
	INFO.setPassword("admin");
	INFO.setUri("http://localhost:9200");
    }

    @Test
    public void locationTest() throws Exception {

	DatabaseSource source = create();
	Assert.assertEquals("configuration\\test-config.json", source.getLocation());
    }

    /**
     * @return
     * @throws GSException
     */
    protected DatabaseSource create() throws GSException {

	return new DatabaseSource(storageInfo, Database.CONFIGURATION_FOLDER, "test-config");
    }

    /**
     * @param info
     */
    public OpenSearchSourceTest() {

	super(INFO);
    }

    @Before
    public void init() throws Exception {

	OpenSearchClient client = OpenSearchDatabase.createNoSSLContextClient(INFO);

	for (String index : IndexMapping.getMappings()) {

	    if (OpenSearchDatabase.checkIndex(client, index)) {

		DeleteIndexRequest indexRequest = new DeleteIndexRequest.Builder().//
			index(index).//
			build();

		client.indices().delete(indexRequest);
	    }
	}

	// recreate the indexes
	OpenSearchDatabase database = new OpenSearchDatabase();
	database.initialize(INFO);
    }
}
