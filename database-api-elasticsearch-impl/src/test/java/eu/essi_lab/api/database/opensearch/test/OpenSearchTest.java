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

   
    @Before
    public void before() throws GSException, OpenSearchException, IOException {

	OpenSearchClient client = OpenSearchDatabase.createNoSSLContextClient(OpenSearchDatabaseInitTest.createStorageInfo());

	for (String index : IndexMapping.getIndexes()) {

	    if (OpenSearchDatabase.checkIndex(client, index)) {

		DeleteIndexRequest indexRequest = new DeleteIndexRequest.Builder().//
			index(index).//
			build();

		client.indices().delete(indexRequest);
	    }
	}
    }
}
