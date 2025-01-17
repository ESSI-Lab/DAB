/**
 * 
 */
package eu.essi_lab.cfga.source.test;

import org.junit.Before;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;

import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase.OpenSearchServiceType;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.model.StorageInfo;

/**
 * @author Fabrizio
 */
public class OpenSearchSourceTest extends DatabaseSourceTest {

    private static StorageInfo INFO;

    static {

	INFO = new StorageInfo();
	// the identifier is set same as name
	INFO.setName("testDb");
	INFO.setType(OpenSearchServiceType.OPEN_SEARCH_MANAGED.getProtocol());
	INFO.setUser("admin");
	INFO.setPassword("admin");
	INFO.setUri("http://localhost:9200");
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
    }
}
