/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.ErrorCause;
import org.opensearch.client.opensearch._types.ErrorResponse;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchPhraseQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.CountRequest;
import org.opensearch.client.opensearch.core.DeleteByQueryRequest;
import org.opensearch.client.opensearch.core.DeleteByQueryResponse;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.DeleteResponse;
import org.opensearch.client.opensearch.core.ExistsRequest;
import org.opensearch.client.opensearch.core.GetRequest;
import org.opensearch.client.opensearch.core.GetResponse;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.opensearch.client.opensearch.core.search.SourceFilter;
import org.opensearch.client.opensearch.generic.OpenSearchGenericClient;
import org.opensearch.client.opensearch.generic.Requests;
import org.opensearch.client.opensearch.generic.Response;

import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.mappings.FolderRegistryMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.ViewsMapping;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.PerformanceLogger;
import eu.essi_lab.messages.bond.View.ViewVisibility;

/**
 * @author Fabrizio
 */
public class OpenSearchWrapper {

    private OpenSearchClient client;

    /**
     * @param client
     */
    public OpenSearchWrapper(OpenSearchClient client) {

	this.client = client;
    }

    /**
     * @param index
     * @param entryId
     * @return
     * @throws OpenSearchException
     * @throws IOException
     */
    public Optional<JSONObject> getSource(String index, String entryId) throws OpenSearchException, IOException {

	GetRequest getRequest = buildGetRequest(index, entryId);

	GetResponse<Object> response = client.get(getRequest, Object.class);

	if (response.found()) {

	    JSONObject source = ConversionUtils.toJSONObject(response.source());

	    decorateSource(source, index, entryId);

	    return Optional.of(source);
	}

	return Optional.empty();
    }

    /**
     * @param searchQuery
     * @param properties
     * @param start
     * @param size
     * @return
     * @throws Exception
     */
    public List<InputStream> searchBinaries(Query searchQuery, int start, int size) throws Exception {

	SearchResponse<Object> searchResponse = search(searchQuery, start, size);

	return ConversionUtils.toBinaryList(searchResponse);
    }

    /**
     * @param searchQuery
     * @param properties
     * @param start
     * @param size
     * @return
     * @throws Exception
     */
    public SearchResponse<Object> search(Query searchQuery, int start, int size) throws Exception {

	PerformanceLogger pl = new PerformanceLogger(//
		PerformanceLogger.PerformancePhase.OPENSEARCH_WRAPPER_SEARCH, //
		UUID.randomUUID().toString(), //
		Optional.empty());

	SearchResponse<Object> response = client.search(builder -> {

	    builder.query(searchQuery).//
		    from(start).//
		    size(size);

	    return builder;

	}, Object.class);

	pl.logPerformance(GSLoggerFactory.getLogger(getClass()));

	return response;
    }

    /**
     * Retrieves first 10 entries
     * 
     * @param searchQuery
     * @param key
     * @return
     * @throws Exception
     */
    public List<InputStream> searchBinaries(Query searchQuery) throws Exception {

	return searchBinaries(searchQuery, 0, 10);
    }

    /**
     * @param searchQuery
     * @param property
     * @return
     * @throws OpenSearchException
     * @throws IOException
     */
    public List<String> searchProperty(Query searchQuery, String property) throws OpenSearchException, IOException {

	return searchProperty(searchQuery, property, 0, 10);
    }

    /**
     * @param searchQuery
     * @param property
     * @param start
     * @param size
     * @return
     * @throws OpenSearchException
     * @throws IOException
     */
    public List<String> searchProperty(Query searchQuery, String property, int start, int size) throws OpenSearchException, IOException {

	SearchResponse<Object> response = client.search(builder -> {

	    builder.query(searchQuery).//
		    from(start).//
		    size(size).// includes only the given property
		    source(src -> src.filter(new SourceFilter.Builder().includes(property).//
			    build()));

	    return builder;

	}, Object.class);

	HitsMetadata<Object> hits = response.hits();
	List<Hit<Object>> hitsList = hits.hits();

	return hitsList.stream().//

		map(hit -> {

		    JSONObject source = ConversionUtils.toJSONObject(hit.source());
		    return decorateSource(source, hit.index(), hit.id());
		}).//

		map(source -> source.getString(property)).//

		collect(Collectors.toList());
    }

    /**
     * @param index
     * @param searchQuery
     * @return
     */
    public DeleteByQueryRequest buildDeleteByQueryRequest(String index, Query searchQuery) {

	return new DeleteByQueryRequest.Builder().//
	// allowNoIndices(true).//
	// index(IndexData.ALL_INDEXES).//
		index(index).//
		query(searchQuery).//
		build();
    }

    /**
     * @param request
     * @throws IOException
     * @throws OpenSearchException
     */
    public DeleteByQueryResponse deleteByQuery(DeleteByQueryRequest request) throws OpenSearchException, IOException {

	DeleteByQueryResponse response = client.deleteByQuery(request);

	synch();

	return response;
    }

    /**
     * @param index
     * @param entryId
     * @return
     * @throws OpenSearchException
     * @throws IOException
     */
    public boolean delete(String index, String entryId) throws OpenSearchException, IOException {

	DeleteRequest deleteRequest = new DeleteRequest.Builder().//
		index(index).//
		id(entryId).//
		build();

	DeleteResponse response = client.delete(deleteRequest);

	synch();

	return response.result() == Result.Deleted;
    }

    /**
     * @param index
     * @param entryId
     * @return
     * @throws IOException
     * @throws OpenSearchException
     */
    public boolean entryExists(String index, String entryId) throws OpenSearchException, IOException {

	ExistsRequest existsRequest = new ExistsRequest.Builder().//
		id(entryId).//
		index(index).build();

	return client.exists(existsRequest).value();
    }

    /**
     * @param client
     * @param indexData
     * @return
     * @throws IOException
     */
    public boolean storeWithGenericClient(IndexData indexData) throws IOException {

	OpenSearchGenericClient generic = client.generic();

	Response response = generic.execute(//
		Requests.builder().//
			endpoint(indexData.getIndex() + "/_doc/" + indexData.getEntryId()).//
			method("POST").//
			json(indexData.getData()).build());

	String bodyAsString = response.getBody().//
		get().//
		bodyAsString();

	JSONObject responseObject = new JSONObject(bodyAsString);

	return responseObject.getString("result").equalsIgnoreCase("created");
    }

    /**
     * {@link IndexRequest} works only with a <i>POJO</i> body;
     * using other types of body such as {@link String} of {@link JSONObject} do not works!
     * 
     * @param client
     * @param indexData
     * @return
     * @throws OpenSearchException
     * @throws IOException
     */
    public boolean storeWithOpenSearchClient(IndexData indexData) throws OpenSearchException, IOException {

	IndexRequest<JSONObject> indexRequest = indexData.getRequest();

	IndexResponse indexResponse = client.index(indexRequest);

	Result result = indexResponse.result();

	return result == Result.Created;
    }

    /**
     * Builds a query which searches all entries of the given <code>folder</code>.<br>
     * <b>Constraints</b>: databaseId = getDatabase().getIdentifier() AND folderName = getName()
     */
    public Query buildSearchEntriesQuery(OpenSearchFolder folder) {

	// MatchQuery databaseIdQuery = new MatchQuery.Builder().//
	// field(IndexData.DATABASE_ID).query(new FieldValue.Builder().//
	// stringValue(folder.getDatabase().getIdentifier()).//
	// build())
	// .//
	// build();
	//
	// MatchQuery folderNameQuery = new MatchQuery.Builder().//
	// field(IndexData.FOLDER_NAME).query(new FieldValue.Builder().//
	// stringValue(folder.getName()).//
	// build())
	// .//
	// build();

	MatchPhraseQuery databaseIdQuery = new MatchPhraseQuery.Builder().//
		field(IndexData.DATABASE_ID).query(folder.getDatabase().getIdentifier()).//
		build();

	MatchPhraseQuery folderNameQuery = new MatchPhraseQuery.Builder().//
		field(IndexData.FOLDER_NAME).query(folder.getName()).//
		build();

	List<Query> mustList = new ArrayList<>();

	mustList.add(databaseIdQuery.toQuery());
	mustList.add(folderNameQuery.toQuery());

	List<Query> shouldList = buildIndexesQuery();

	BoolQuery boolQuery = new BoolQuery.Builder().//
		must(mustList).//
		should(shouldList).//
		minimumShouldMatch("1").//
		build();

	return boolQuery.toQuery();
    }

    /**
     * @param databaseId
     * @param creator
     * @param owner
     * @param visibility
     * @return
     */
    public Query buildSearchViewsQuery(//
	    String databaseId, //
	    Optional<String> creator, //
	    Optional<String> owner, //
	    Optional<ViewVisibility> visibility) {

	MatchPhraseQuery databaseIdQuery = new MatchPhraseQuery.Builder().//
		field(IndexData.DATABASE_ID).//
		query(databaseId).//
		build();

	MatchPhraseQuery indexQuery = new MatchPhraseQuery.Builder().//
		field("_index").//
		query(ViewsMapping.get().getIndex()).//
		build();

	List<Query> mustList = new ArrayList<>();

	if (creator.isPresent()) {

	    MatchPhraseQuery creatorQuery = new MatchPhraseQuery.Builder().//
		    field(ViewsMapping.VIEW_CREATOR).//
		    query(creator.get()).//
		    build();

	    mustList.add(creatorQuery.toQuery());
	}

	if (owner.isPresent()) {

	    MatchPhraseQuery ownerQuery = new MatchPhraseQuery.Builder().//
		    field(ViewsMapping.VIEW_OWNER).//
		    query(owner.get()).//
		    build();

	    mustList.add(ownerQuery.toQuery());
	}

	if (visibility.isPresent()) {

	    MatchPhraseQuery visibilityQuery = new MatchPhraseQuery.Builder().//
		    field(ViewsMapping.VIEW_VISIBILITY).//
		    query(visibility.get().name()).//
		    build();
	    mustList.add(visibilityQuery.toQuery());
	}

	mustList.add(databaseIdQuery.toQuery());
	mustList.add(indexQuery.toQuery());

	List<Query> shouldList = buildIndexesQuery();

	BoolQuery boolQuery = new BoolQuery.Builder().//
		must(mustList).//
		should(shouldList).//
		minimumShouldMatch("1").//

		build();

	return boolQuery.toQuery();
    }

    /**
     * Builds a query which searches the entries with the given <code>property</code> and
     * <code>propertyValue</code>
     * 
     * @param databaseId
     * @param index
     * @param property
     * @param propertyValue
     * @return
     */
    public Query buildSearchQuery(String databaseId, String index, String property, String propertyValue) {

	return buildSearchQuery(databaseId, index, property, Arrays.asList(propertyValue));
    }

    /**
     * Builds a query which searches the entries with the given <code>property</code> and
     * matching one or more <code>propertyValues</code>
     * 
     * @param databaseId
     * @param index
     * @param property
     * @param propertyValues
     * @return
     */
    public Query buildSearchQuery(String databaseId, String index, String property, List<String> propertyValues) {

	MatchPhraseQuery databaseIdQuery = new MatchPhraseQuery.Builder().//
		field(IndexData.DATABASE_ID).query(databaseId).//
		build();

	List<Query> shouldList = new ArrayList<>();

	propertyValues.forEach(v -> {

	    MatchPhraseQuery propertyQuery = new MatchPhraseQuery.Builder().//
		    field(property).//
		    query(v).//
		    build();

	    shouldList.add(propertyQuery.toQuery());
	});

	MatchPhraseQuery indexQuery = new MatchPhraseQuery.Builder().//
		field("_index").//
		query(index).//
		build();

	List<Query> mustList = new ArrayList<>();

	mustList.add(databaseIdQuery.toQuery());
	mustList.add(indexQuery.toQuery());

	BoolQuery boolQuery = new BoolQuery.Builder().//
		should(shouldList).//
		must(mustList).//
		minimumShouldMatch("1").//
		build();

	return boolQuery.toQuery();
    }

    /**
     * Builds a query which searches the entries in the database with id <code>databaseId</code> and in the index
     * <code>index</code>
     * 
     * @param databaseId
     * @param index
     * @return
     */
    public Query buildSearchQuery(String databaseId, String index) {

	MatchPhraseQuery databaseIdQuery = new MatchPhraseQuery.Builder().//
		field(IndexData.DATABASE_ID).query(databaseId).//
		build();

	MatchPhraseQuery indexQuery = new MatchPhraseQuery.Builder().//
		field("_index").//
		query(index).//
		build();

	List<Query> mustList = new ArrayList<>();

	mustList.add(databaseIdQuery.toQuery());
	mustList.add(indexQuery.toQuery());

	BoolQuery boolQuery = new BoolQuery.Builder().//
		must(mustList).//
		build();

	return boolQuery.toQuery();
    }

    /**
     * @param databaseId
     * @return
     */
    public Query buildSearchRegistryQuery(String databaseId) {

	MatchPhraseQuery databaseIdQuery = new MatchPhraseQuery.Builder().//
		field(IndexData.DATABASE_ID).query(databaseId).//
		build();

	MatchPhraseQuery folderRegistryIndexQuery = new MatchPhraseQuery.Builder().//
		field("_index").query(FolderRegistryMapping.get().getIndex()).//
		build();

	List<Query> queryList = Arrays.asList(//
		databaseIdQuery.toQuery(), //
		folderRegistryIndexQuery.toQuery());

	BoolQuery boolQuery = new BoolQuery.Builder().must(queryList).build();

	return boolQuery.toQuery();
    }

    /**
     * @param index
     * @return
     * @throws OpenSearchException
     * @throws IOException
     */
    public int count(String index) throws OpenSearchException, IOException {

	CountRequest countRequest = new CountRequest.Builder().//
		index(index).build();

	return (int) client.count(countRequest).count();
    }

    /**
     * @param searchQuery
     * @return
     * @throws IOException
     * @throws OpenSearchException
     */
    public int count(Query searchQuery) throws OpenSearchException, IOException {

	SearchResponse<Object> searchResponse = client.search(builder -> {
	    builder.query(searchQuery);
	    return builder;

	}, Object.class);

	HitsMetadata<Object> hits = searchResponse.hits();
	return (int) hits.total().value();
    }

    /**
     * @throws IOException
     * @throws OpenSearchException
     */
    public void synch() throws OpenSearchException, IOException {

	client.indices().refresh();
    }

    /**
     * @param searchQuery
     * @param key
     * @return
     * @throws Exception
     */
    private Optional<JSONObject> searchSource(Query searchQuery, String key) throws Exception {

	SearchResponse<Object> searchResponse = client.search(s -> {
	    s.query(searchQuery);
	    return s;

	}, Object.class);

	HitsMetadata<Object> hits = searchResponse.hits();
	if (hits.total().value() == 0) {

	    return Optional.empty();
	}

	List<Hit<Object>> hitsList = hits.hits();
	Hit<Object> hit = hitsList.get(0);

	JSONObject source = ConversionUtils.toJSONObject(hit.source());

	decorateSource(source, hit.index(), hit.id());

	return Optional.of(source);
    }

    /**
     * Builds a query which searches the entry of the given <code>folder</code>
     * having the given <code>key</code>.<br>
     * <b>Constraints</b>: databaseId = getDatabase().getIdentifier() AND folderName = getName() AND key = key
     * 
     * @param key
     */
    private Query buildSearchQuery(OpenSearchFolder folder, String key) {

	// MatchQuery databaseIdQuery = new MatchQuery.Builder().//
	// field(IndexData.DATABASE_ID).query(new FieldValue.Builder().//
	// stringValue(folder.getDatabase().getIdentifier()).//
	// build())
	// .//
	// build();
	//
	// MatchQuery folderNameQuery = new MatchQuery.Builder().//
	// field(IndexData.FOLDER_NAME).query(new FieldValue.Builder().//
	// stringValue(folder.getName()).//
	// build())
	// .//
	// build();
	//
	// MatchQuery keyQuery = new MatchQuery.Builder().//
	// field(IndexData.ENTRY_NAME).query(new FieldValue.Builder().//
	// stringValue(key).//
	// build())
	// .//
	// build();

	MatchPhraseQuery databaseIdQuery = new MatchPhraseQuery.Builder().//
		field(IndexData.DATABASE_ID).query(folder.getDatabase().getIdentifier()).//
		build();

	MatchPhraseQuery folderNameQuery = new MatchPhraseQuery.Builder().//
		field(IndexData.FOLDER_NAME).query(folder.getName()).//
		build();

	MatchPhraseQuery keyQuery = new MatchPhraseQuery.Builder().//
		field(IndexData.ENTRY_NAME).query(key).//
		build();

	List<Query> mustList = new ArrayList<>();

	mustList.add(databaseIdQuery.toQuery());
	mustList.add(folderNameQuery.toQuery());
	mustList.add(keyQuery.toQuery());

	List<Query> shouldList = buildIndexesQuery();

	BoolQuery boolQuery = new BoolQuery.Builder().//
		must(mustList).//
		should(shouldList).//
		minimumShouldMatch("1").//
		build();

	return boolQuery.toQuery();
    }

    /**
     * @return
     */
    private List<Query> buildIndexesQuery() {

	List<String> indexes = IndexMapping.getIndexes();

	List<Query> queryList = new ArrayList<>();

	indexes.forEach(index -> {

	    queryList.add(new MatchPhraseQuery.Builder().//
		    field("_index").//
		    query(index).//
		    build().//
		    toQuery());

	});

	return queryList;
    }

    /**
     * @param ex
     * @return
     */
    @SuppressWarnings("unused")
    private boolean indexNotFound(OpenSearchException ex) {

	ErrorResponse response = ex.response();
	ErrorCause error = response.error();
	return error.type().equals("index_not_found_exception");
    }

    /**
     * @param source
     * @param _index
     * @param _id
     * @return
     */
    private JSONObject decorateSource(JSONObject source, String _index, String _id) {

	source.put(IndexData.INDEX, _index);
	source.put(IndexData.ENTRY_ID, _id);

	return source;
    }

    /**
     * @param index
     * @param entryId
     * @return
     */
    private GetRequest buildGetRequest(String index, String entryId) {

	return new GetRequest.Builder().//
		index(index).//
		id(entryId).//
		build();
    }
}
