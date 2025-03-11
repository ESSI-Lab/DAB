/**
 * 
 */
package eu.essi_lab.api.database.opensearch.datafolder.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;

import eu.essi_lab.api.database.Database.OpenSearchServiceType;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.query.OpenSearchQueryBuilder;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.model.SortOrder;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.resource.RankingStrategy;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class SortByResourceTimeStampExternalTestIT {

    @Test
    public void test() throws Exception {

	StorageInfo osStorageInfo = new StorageInfo(System.getProperty("dbUrl"));
	osStorageInfo.setName("production");
	osStorageInfo.setUser(System.getProperty("dbUser"));
	osStorageInfo.setPassword(System.getProperty("dbPassword"));
	osStorageInfo.setIdentifier("production");
	osStorageInfo.setType(OpenSearchServiceType.OPEN_SEARCH_MANAGED.getProtocol());

	OpenSearchDatabase db = new OpenSearchDatabase();
	db.initialize(osStorageInfo);

	OpenSearchWrapper wrapper = new OpenSearchWrapper(db.getClient());

	HashMap<String, String> map = new HashMap<String, String>();
	map.put("UUID-2dc3a01b-934e-4c3d-9311-527ac93ec058", "data-1");

	OpenSearchQueryBuilder builder = new OpenSearchQueryBuilder(//
		wrapper, //
		new RankingStrategy(), //
		map, //
		false);

	Query sourceIdQuery = builder
		.buildSourceIdQuery(BondFactory.createSourceIdentifierBond("UUID-2dc3a01b-934e-4c3d-9311-527ac93ec058"));

	Query query = sourceIdQuery;

	SearchResponse<Object> response = wrapper.search(//
		DataFolderMapping.get().getIndex(), //
		query, //
		Arrays.asList(), //
		0, // start
		10, // size
		Optional.of(ResourceProperty.RESOURCE_TIME_STAMP), // orderingProperty
		Optional.of(SortOrder.ASCENDING), // orderingDirection
		Optional.empty(), // search after
		false); // cache

	List<Hit<Object>> hits = response.hits().hits();
	Assert.assertEquals(10, hits.size());
    }
}
