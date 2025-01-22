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
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class OpenSearchDatabaseInitTest {

    @Test
    public void databaseProviderTest() throws GSException {

	Database database = OpenSearchDatabase.createLocalService();
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

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

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
