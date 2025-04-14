/**
 * 
 */
package eu.essi_lab.api.database.opensearch.test;

import java.io.IOException;

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

    /**
     * @param args
     * @throws OpenSearchException
     * @throws GSException
     * @throws IOException
     */
    public static void main(String[] args) throws OpenSearchException, GSException, IOException {

	before();
	
	System.exit(0);
    }

    @Before
    public static void before() throws GSException, OpenSearchException, IOException {

	System.setProperty("initIndexes", "true");

	OpenSearchClient client = OpenSearchDatabase.createNoSSLContextClient(OpenSearchDatabase.createLocalServiceInfo());

	for (String index : IndexMapping.getIndexes(false)) {

	    if (OpenSearchDatabase.checkIndex(client, index)) {

		DeleteIndexRequest indexRequest = new DeleteIndexRequest.Builder().//
			index(index).//
			build();

		client.indices().delete(indexRequest);
	    }
	}
    }
}
