/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

import java.io.IOException;

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

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.MatchQuery;
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
import org.w3c.dom.Node;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.IndexData.DataType;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;

/**
 * @author Fabrizio
 */
public class OpenSearchFolder implements DatabaseFolder {

    private String name;
    private OpenSearchDatabase database;
    private boolean forceRemoveByQuery;
    private boolean forceExistsBySearchQuery;
    private boolean forceCountBySearchQuery;
    private boolean forceGetBinaryBySearchQuery;

    /**
     * @param database
     * @param name
     */
    public OpenSearchFolder(OpenSearchDatabase database, String name) {

	this.database = database;
	this.name = name;
    }

    @Override
    public String getName() {

	return name;
    }

    @Override
    public boolean store(String key, FolderEntry entry, EntryType type) throws Exception {

	OpenSearchClient client = database.getClient();

	IndexData indexData = IndexData.of(this, key, entry, type);

	boolean stored = storeWithGenericClient(client, indexData);

	synch();

	return stored;
    }

    @Override
    public boolean replace(String key, FolderEntry entry, EntryType type) throws Exception {

	if (exists(key)) {

	    if (remove(key)) {
		return store(key, entry, type);
	    }
	}

	return false;
    }

    @Override
    public Node get(String key) throws Exception {

	JSONObject source = _getSource(key);

	SourceWrapper wrapper = new SourceWrapper(source);

	if (wrapper.getDataType() == DataType.BINARY) {

	    throw new Exception("Resource with key " + key + " is not a document, use 'getBinary'");
	}

	return IndexData.toDocument(IndexData.toStream(source));
    }

    @Override
    public InputStream getBinary(String key) throws Exception {

	return IndexData.toStream(_getSource(key));
    }

    @Override
    public boolean remove(String key) throws Exception {

	OpenSearchClient client = database.getClient();

	Optional<String> index = IndexData.detectIndex(this);

	String id = getEntryId(this, key);

	boolean deleted = false;

	if (index.isPresent() && !forceRemoveByQuery) {

	    DeleteRequest deleteRequest = new DeleteRequest.Builder().//
		    index(index.get()).//
		    id(id).//
		    build();

	    DeleteResponse response = client.delete(deleteRequest);

	    deleted = response.result() == Result.Deleted;

	} else {

	    DeleteByQueryRequest deleteRequest = new DeleteByQueryRequest.Builder().//
		    allowNoIndices(true).//
		    index(IndexData.ALL_INDEXES).//
		    query(buildSearchQuery(key)).//
		    build();

	    DeleteByQueryResponse response = client.deleteByQuery(deleteRequest);

	    deleted = response.deleted() == 1; // 0 otherwise
	}

	synch();
	
	return deleted;
    }

    @Override
    public boolean exists(String key) throws Exception {

	OpenSearchClient client = database.getClient();

	Optional<String> index = IndexData.detectIndex(this);

	String id = getEntryId(this, key);

	boolean exists = false;

	if (index.isPresent() && !forceExistsBySearchQuery) {

	    ExistsRequest existsRequest = new ExistsRequest.Builder().//
		    id(id).//
		    index(index.get()).build();

	    exists = client.exists(existsRequest).value();

	} else {

	    Query searchQuery = buildSearchQuery(key);

	    exists = searchBinarySource(client, searchQuery, key) != null;
	}

	return exists;
    }

    @Override
    public String[] listKeys() throws Exception {

	OpenSearchClient client = database.getClient();

	Query searchQuery = buildSearchFolderEntriesQuery();

	SearchResponse<Object> response = client.search(s -> {

	    s.query(searchQuery).// includes only the key
		    source(src -> src.filter(new SourceFilter.Builder().includes(IndexData.ENTRY_NAME).build()));

	    return s;

	}, Object.class);

	HitsMetadata<Object> hits = response.hits();
	List<Hit<Object>> hitsList = hits.hits();

	return hitsList.stream().//
		map(hit -> IndexData.toJSONObject(hit.source()).getString(IndexData.ENTRY_NAME)).//
		collect(Collectors.toList()).//
		toArray(new String[] {});//
    }

    @Override
    public int size() throws Exception {

	OpenSearchClient client = database.getClient();

	Optional<String> index = IndexData.detectIndex(this);

	long count = 0;

	if (index.isPresent() && !forceCountBySearchQuery) {

	    CountRequest countRequest = new CountRequest.Builder().//
		    index(index.get()).build();

	    count = client.count(countRequest).count();

	} else {

	    Query searchQuery = buildSearchFolderEntriesQuery();
	    count = count(client, searchQuery);
	}

	return (int) count;
    }

    @Override
    public void clear() throws Exception {

	OpenSearchClient client = database.getClient();

	String index = IndexData.detectIndex(this).orElse(IndexData.ALL_INDEXES);

	DeleteByQueryRequest deleteRequest = new DeleteByQueryRequest.Builder().//
		allowNoIndices(true).//
		index(index).//
		query(buildSearchFolderEntriesQuery()).//
		build();

	client.deleteByQuery(deleteRequest);
	
	synch();
    }

    /**
     * @return
     */
    @Override
    public Database getDatabase() {

	return database;
    }

    /**
     * The folder is formed by the database id followed by '_' and the folder name.<br>
     * E.g.: database id = 'test'; folder name = 'acronet'; folder id = 'test_acronet'.
     * 
     * @param folder
     */
    public static String getFolderId(DatabaseFolder folder) {

	return folder.getDatabase().getIdentifier() + "_" + folder.getName();
    }

    /**
     * The resource id, used as source id for the stored items, is formed by the folder id followed by '_' and
     * the resource key.<br>
     * E.g.: E.g.: database id = 'test'; folder name = 'acronet'; resource key = 'key'; resource key =
     * 'test_acronet_key'.
     * 
     * @param key
     * @return
     */
    public static String getEntryId(DatabaseFolder folder, String resourceKey) {

	return getFolderId(folder) + "_" + resourceKey;
    }

    /**
     * @param key
     * @return
     * @throws Exception
     */
    public SourceWrapper getSourceWrapper(String key) throws Exception {

	return new SourceWrapper(_getSource(key));
    }

    /**
     * @param key
     * @return
     * @throws Exception
     */
    private JSONObject _getSource(String key) throws Exception {

	OpenSearchClient client = database.getClient();

	Optional<String> index = IndexData.detectIndex(this);

	String id = getEntryId(this, key);

	JSONObject source = null;

	if (index.isPresent() && !forceGetBinaryBySearchQuery) {

	    GetRequest getRequest = new GetRequest.Builder().//
		    index(index.get()).//
		    id(id).//
		    build();

	    GetResponse<Object> response = client.get(getRequest, Object.class);

	    source = IndexData.toJSONObject(response.source());

	    if (source != null) {

		source.put(IndexData.INDEX, index.get());
	    }

	} else {

	    Query searchQuery = buildSearchQuery(key);

	    source = searchBinarySource(client, searchQuery, key);
	}

	if (source == null) {

	    throw new Exception("Resource with key '" + key + "' not found");
	}

	source.put(IndexData.ENTRY_ID, id);

	return source;
    }

    /**
     * For test purpose
     * 
     * @param force
     */
    public void forceGetBinaryBySearchQuery(boolean force) {

	this.forceGetBinaryBySearchQuery = force;
    }

    /**
     * For test purpose
     * 
     * @param force
     */
    public void forceCountBySearchQuery(boolean force) {

	this.forceCountBySearchQuery = force;
    }

    /**
     * For test purpose
     * 
     * @param force
     */
    public void forceExistsBySearchQuery(boolean force) {

	this.forceExistsBySearchQuery = force;
    }

    /**
     * For test purpose
     * 
     * @param force
     */
    public void forceRemoveByQuery(boolean force) {

	this.forceRemoveByQuery = force;
    }

    /**
     * @throws IOException
     * @throws OpenSearchException
     */
    private void synch() throws OpenSearchException, IOException {
    
        database.getClient().indices().refresh();
    }

    /**
     * @param client
     * @param indexData
     * @return
     * @throws IOException
     */
    private boolean storeWithGenericClient(OpenSearchClient client, IndexData indexData) throws IOException {

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
     * IndexRequest works only with a POJO body;
     * using other types of body such as String of JSONObject do not works
     * 
     * @param client
     * @param indexData
     * @return
     * @throws OpenSearchException
     * @throws IOException
     */
    @SuppressWarnings("unused")
    private boolean storeWithOpenSearchClient(OpenSearchClient client, IndexData indexData) throws OpenSearchException, IOException {

	IndexRequest<JSONObject> indexRequest = indexData.getRequest();

	IndexResponse indexResponse = client.index(indexRequest);

	Result result = indexResponse.result();

	return result == Result.Created;
    }

    /**
     * @param client
     * @param searchQuery
     * @param key
     * @return
     * @throws Exception
     */
    private int count(OpenSearchClient client, Query searchQuery) throws Exception {

	SearchResponse<Object> searchResponse = client.search(s -> {
	    s.query(searchQuery);
	    return s;

	}, Object.class);

	HitsMetadata<Object> hits = searchResponse.hits();
	return (int) hits.total().value();
    }

    /**
     * @param client
     * @param key
     * @return
     * @throws Exception
     */
    private JSONObject searchBinarySource(OpenSearchClient client, Query searchQuery, String key) throws Exception {

	SearchResponse<Object> searchResponse = client.search(s -> {
	    s.query(searchQuery);
	    return s;

	}, Object.class);

	HitsMetadata<Object> hits = searchResponse.hits();
	if (hits.total().value() == 0) {

	    return null;
	}

	List<Hit<Object>> hitsList = hits.hits();
	Hit<Object> hit = hitsList.get(0);

	JSONObject source = IndexData.toJSONObject(hit.source());

	source.put(IndexData.INDEX, hit.index());

	return source;
    }

    /**
     * This query searches all records of this folder.<br>
     * databaseId = getDatabase().getIdentifier() && folderName = getName()
     */
    private Query buildSearchFolderEntriesQuery() {

	MatchQuery databaseIdQuery = new MatchQuery.Builder().//
		field(IndexData.DATABASE_ID).query(new FieldValue.Builder().//
			stringValue(getDatabase().getIdentifier()).//
			build())
		.//
		build();

	MatchQuery folderNameQuery = new MatchQuery.Builder().//
		field(IndexData.FOLDER_NAME).query(new FieldValue.Builder().//
			stringValue(getName()).//
			build())
		.//
		build();

	List<Query> queryList = Arrays.asList(//
		databaseIdQuery.toQuery(), //
		folderNameQuery.toQuery());

	BoolQuery boolQuery = new BoolQuery.Builder().must(queryList).build();

	return boolQuery.toQuery();
    }

    /**
     * This query searches all records of this folder having the given <code>key</code>.<br>
     * databaseId = getDatabase().getIdentifier() && folderName = getName() && key = key
     * 
     * @param key
     */
    private Query buildSearchQuery(String key) {

	MatchQuery databaseIdQuery = new MatchQuery.Builder().//
		field(IndexData.DATABASE_ID).query(new FieldValue.Builder().//
			stringValue(getDatabase().getIdentifier()).//
			build())
		.//
		build();

	MatchQuery folderNameQuery = new MatchQuery.Builder().//
		field(IndexData.FOLDER_NAME).query(new FieldValue.Builder().//
			stringValue(getName()).//
			build())
		.//
		build();

	MatchQuery keyQuery = new MatchQuery.Builder().//
		field(IndexData.ENTRY_NAME).query(new FieldValue.Builder().//
			stringValue(key).//
			build())
		.//
		build();

	List<Query> queryList = Arrays.asList(//
		databaseIdQuery.toQuery(), //
		folderNameQuery.toQuery(), //
		keyQuery.toQuery());

	BoolQuery boolQuery = new BoolQuery.Builder().must(queryList).build();

	return boolQuery.toQuery();
    }
}
