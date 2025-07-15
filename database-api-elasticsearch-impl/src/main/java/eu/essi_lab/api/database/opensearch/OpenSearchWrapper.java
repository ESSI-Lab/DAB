/**
 * 
 */
package eu.essi_lab.api.database.opensearch;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.ErrorCause;
import org.opensearch.client.opensearch._types.ErrorResponse;
import org.opensearch.client.opensearch._types.FieldSort;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch._types.SortOptions;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.aggregations.Aggregation;
import org.opensearch.client.opensearch._types.aggregations.Buckets;
import org.opensearch.client.opensearch._types.aggregations.CardinalityAggregation;
import org.opensearch.client.opensearch._types.aggregations.MaxAggregation;
import org.opensearch.client.opensearch._types.aggregations.MinAggregation;
import org.opensearch.client.opensearch._types.aggregations.StringTermsAggregate;
import org.opensearch.client.opensearch._types.aggregations.StringTermsBucket;
import org.opensearch.client.opensearch._types.aggregations.TermsAggregation;
import org.opensearch.client.opensearch._types.aggregations.TopHitsAggregate;
import org.opensearch.client.opensearch._types.aggregations.TopHitsAggregation;
import org.opensearch.client.opensearch._types.aggregations.TopHitsAggregation.Builder;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
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
import org.opensearch.client.opensearch.core.MsearchRequest;
import org.opensearch.client.opensearch.core.MsearchResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.msearch.MultiSearchResponseItem;
import org.opensearch.client.opensearch.core.msearch.RequestItem;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.opensearch.client.opensearch.core.search.SourceFilter;
import org.opensearch.client.opensearch.core.search.TrackHits;
import org.opensearch.client.opensearch.generic.OpenSearchGenericClient;
import org.opensearch.client.opensearch.generic.Requests;
import org.opensearch.client.opensearch.generic.Response;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.ShapeFileMapping;
import eu.essi_lab.api.database.opensearch.query.OpenSearchQueryBuilder;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.messages.SortedFields;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.Queryable.ContentType;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class OpenSearchWrapper {

    private static final Integer MAX_DEFAULT_HITS = 10000;

    private OpenSearchClient client;

    private OpenSearchDatabase database;

    /**
     * @param database
     */
    public OpenSearchWrapper(OpenSearchDatabase database) {

	this.database = database;
	this.client = database.getClient();
    }

    /**
     * @return the database
     */
    public OpenSearchDatabase getDatabase() {

	return database;
    }

    /**
     * @param featureId
     * @return
     * @throws OpenSearchException
     * @throws IOException
     * @throws GSException
     */
    public Optional<JSONArray> getShapeFeatureCoordinates(String featureId) throws OpenSearchException, IOException, GSException {

	DatabaseFolder folder = database.getFolder(Database.SHAPE_FILES_FOLDER, true).get();

	String id = OpenSearchFolder.getEntryId(folder, featureId);

	Optional<JSONObject> source = getSource(ShapeFileMapping.get().getIndex(), id);

	if (source.isPresent()) {

	    return Optional.of(source.get().getJSONObject("shape").getJSONArray("coordinates"));
	}

	return Optional.empty();
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

	    JSONObject source = OpenSearchUtils.toJSONObject(response.source());

	    OpenSearchUtils.decorateSource(source, index, entryId);

	    return Optional.of(source);
	}

	return Optional.empty();
    }

    /**
     * @param searchQuery
     * @param start
     * @param size
     * @param tfTargets
     * @param maxItems
     * @return
     * @throws Exception
     */
    public SearchResponse<Object> count(Query searchQuery, DiscoveryMessage message) throws Exception {

	// PerformanceLogger pl = new PerformanceLogger(//
	// PerformanceLogger.PerformancePhase.OPENSEARCH_WRAPPER_SEARCH, //
	// UUID.randomUUID().toString(), //
	// Optional.empty());

	Optional<Queryable> element = message.getDistinctValuesElement();
	SearchResponse<Object> response = null;

	if (element.isPresent()) {

	    CardinalityAggregation agg = CardinalityAggregation.of(a -> a.field(

		    DataFolderMapping.toKeywordField(element.get().getName())));

	    Aggregation aggregation = Aggregation.of(a -> a.cardinality(agg));

	    response = client.search(builder -> {

		builder.aggregations(DataFolderMapping.toKeywordField(element.get().getName()), aggregation);

		builder.query(searchQuery).//
			index(DataFolderMapping.get().getIndex()).//
			size(0);

		if (OpenSearchDatabase.debugQueries) {

		    debugCountRequest(searchQuery, DataFolderMapping.toKeywordField(element.get().getName()), aggregation);
		}

		return builder;

	    }, Object.class);

	} else {

	    List<Queryable> targets = message.getTermFrequencyTargets();
	    int maxItems = message.getMaxFrequencyMapItems();

	    response = client.search(builder -> {

		targets.forEach(trg -> {

		    builder.aggregations(trg.getName(), agg -> agg.terms(t -> t.field(

			    DataFolderMapping.toKeywordField(trg.getName())).size(maxItems)));
		});

		builder.query(searchQuery).//
			trackTotalHits(new TrackHits.Builder().enabled(true).build()).//
			index(DataFolderMapping.get().getIndex()).//
			size(0);

		if (OpenSearchDatabase.debugQueries) {

		    debugCountRequest(searchQuery, targets, maxItems);
		}

		return builder;

	    }, Object.class);
	}

	// pl.logPerformance(GSLoggerFactory.getLogger(getClass()));

	return response;
    }

    /**
     * @param searchQuery
     * @param sourceFields
     * @param target
     * @param size
     * @return
     * @throws Exception
     */
    @SuppressWarnings("serial")
    public List<JSONObject> aggregateWithNestedAgg(//
	    Query searchQuery, //
	    List<String> sourceFields, //
	    Queryable target, //
	    int size, //
	    boolean excludeBinaries, boolean logQuery) throws Exception {

	String topHitsAggName = "top_hits_agg";
	String termsAggName = "terms_agg";

	Map<String, Aggregation> map = new HashMap<>();

	Builder topHitsBuilder = new TopHitsAggregation.Builder().//
		size(1);

	handleSourceFields(//
		topHitsBuilder, //
		null, //
		sourceFields, //
		excludeBinaries); // excluding resource binaries

	Aggregation topHitsAgg = new Aggregation.Builder().// takes the first result

		topHits(topHitsBuilder.//
			size(1).//
			build())
		.build();

	Aggregation termsAgg = new Aggregation.Builder().terms(//
		new TermsAggregation.Builder().//
			field(DataFolderMapping.toKeywordField(target.getName())).//
			size(size).//
			build())
		.//
		aggregations(new HashMap<>() {
		    {
			put(topHitsAggName, topHitsAgg);
		    }
		}).build();

	map.put(termsAggName, termsAgg);

	SearchRequest searchRequest = new SearchRequest.Builder().//
		index(DataFolderMapping.get().getIndex()).//
		query(searchQuery).//
		size(0).//
		aggregations(map).//
		build();

	if (OpenSearchDatabase.debugQueries && logQuery) {

	    GSLoggerFactory.getLogger(getClass()).debug("\n\n--- NESTED AGGREGATION ---\n");
	    GSLoggerFactory.getLogger(OpenSearchFinder.class).debug(OpenSearchUtils.toJSONObject(searchRequest).toString(3));
	}

	SearchResponse<Object> response = client.search(searchRequest, Object.class);

	Map<String, Aggregate> aggregations = response.aggregations();
	Aggregate termsAggregate = aggregations.get(termsAggName);

	StringTermsAggregate sterms = termsAggregate.sterms();

	Buckets<StringTermsBucket> buckets = sterms.buckets();

	List<StringTermsBucket> array = buckets.array();

	ArrayList<JSONObject> out = new ArrayList<>();

	for (StringTermsBucket stringTermsBucket : array) {

	    Map<String, Aggregate> topHits = stringTermsBucket.aggregations();

	    Aggregate aggregate = topHits.get(topHitsAggName);
	    TopHitsAggregate topHitsAggregate = aggregate.topHits();

	    HitsMetadata<JsonData> hits = topHitsAggregate.hits();
	    Hit<JsonData> hit = hits.hits().get(0);

	    JsonData source = hit.source();
	    JSONObject jsonObject = new JSONObject(source.toString());
	    out.add(jsonObject);
	}

	return out;
    }

    /**
     * @param searchQuery
     * @param target
     * @param size
     * @return
     * @throws Exception
     */
    public List<JSONObject> aggregateWithMultiSerch(Query searchQuery, Queryable target, int size) throws Exception {

	List<String> distValues = findDistinctValues(searchQuery, target, size);

	List<RequestItem> items = OpenSearchQueryBuilder.buildDistinctValuesItems(distValues, target);

	MsearchResponse<Object> msResponse = client.msearch(

		new MsearchRequest.Builder().//
			index(DataFolderMapping.get().getIndex()).//
			searches(items).//
			build(),

		Object.class);

	List<MultiSearchResponseItem<Object>> responses = msResponse.responses();

	return responses.//
		stream().//
		map(r -> OpenSearchUtils.toJSONObject(r.result().hits().hits().get(0).source())).//
		collect(Collectors.toList());

    }

    /**
     * <b>NOTE</b>: if <code>size</code> is greater than {@link #MAX_DEFAULT_HITS}, the request will fail and an
     * {@link OpenSearchException} will be thrown
     * 
     * @param index
     * @param searchQuery
     * @param fields
     * @param start
     * @param size
     * @param orderingProperty
     * @param sortOrder
     * @param searchAfter
     * @param requestCache
     * @param excludeResourceBinary
     * @return
     * @throws Exception
     */
    public SearchResponse<Object> search(//
	    String index, //
	    Query searchQuery, //
	    List<String> fields, //
	    int start, //
	    int size, //
	    Optional<SortedFields> sortedFields, //
	    Optional<SearchAfter> searchAfter, //
	    boolean requestCache, //
	    boolean excludeResourceBinary)

	    throws Exception {

	SearchResponse<Object> response = client.search(builder -> {

	    builder.query(searchQuery).//
		    index(index);

	    builder.size(size);

	    if (searchAfter.isPresent()) {

		Optional<List<Object>> values = searchAfter.get().getValues();

		if (values.isPresent()) {

		    List<FieldValue> myFields = getFieldValues(values);

		    builder.searchAfterVals(myFields);
		}

	    } else {

		builder.from(start);
	    }

	    if (sortedFields.isPresent()) {

		handleSort(builder, sortedFields.get());
	    }

	    handleSourceFields(null, builder, fields, excludeResourceBinary);

	    if (requestCache) {

		builder.requestCache(true);
	    }

	    if (OpenSearchDatabase.debugQueries) {

		debugSearchRequest(//
			searchQuery, //
			index, //
			size, //
			searchAfter, //
			start, //
			sortedFields, //
			fields, //
			excludeResourceBinary, //
			requestCache);//
	    }

	    return builder;

	}, Object.class);

	// pl.logPerformance(GSLoggerFactory.getLogger(getClass()));

	return response;
    }

    private List<FieldValue> getFieldValues(Optional<List<Object>> values) {
	ArrayList<FieldValue> ret = new ArrayList<FieldValue>();
	if (values.isEmpty()) {
	    return ret;
	}
	for (Object obj : values.get()) {
	    if (obj instanceof String) {
		String str = (String) obj;
		FieldValue fv = FieldValue.of(str);
		ret.add(fv);
	    } else if (obj instanceof Long) {
		Long l = (Long) obj;
		FieldValue fv = FieldValue.of(l);
		ret.add(fv);
	    } else if (obj instanceof Double) {
		Double d = (Double) obj;
		FieldValue fv = FieldValue.of(d);
		ret.add(fv);
	    }
	}
	return ret;
    }

    /**
     * <b>NOTE</b>: use it with care! It returns a maximum of {@link #MAX_DEFAULT_HITS} which are requested (and
     * optionally returned) in a single query.<br>
     * This method is actually used by {@link OpenSearchReader} to get the users and the views, so the limit of
     * {@link #MAX_DEFAULT_HITS} is not a problem
     * 
     * @param searchQuery
     * @param key
     * @return
     * @throws Exception
     */
    public List<InputStream> searchBinaries(String index, Query searchQuery) throws Exception {

	SearchResponse<Object> searchResponse = search(//
		index, //
		searchQuery, //
		Arrays.asList(), //
		0, //
		MAX_DEFAULT_HITS, //
		Optional.empty(), //
		Optional.empty(), //
		false, //
		false);

	return OpenSearchUtils.toBinaryList(searchResponse);
    }

    /**
     * <b>NOTE</b>: if <code>size</code> is greater than {@link #MAX_DEFAULT_HITS}, the request will fail and an
     * {@link OpenSearchException} will be thrown.<br>
     * This method is actually used by
     * {@link OpenSearchFolder#get(eu.essi_lab.api.database.Database.IdentifierType, String)} method with
     * a <code>size</code> of 1, so the limit of {@link #MAX_DEFAULT_HITS} is not a problem
     * 
     * @param index
     * @param searchQuery
     * @param start
     * @param size
     * @return
     * @throws Exception
     */
    public List<JSONObject> searchSources(String index, Query searchQuery, int start, int size) throws Exception {

	SearchResponse<Object> response = search(//
		index, //
		searchQuery, //
		Arrays.asList(), //
		start, //
		size, //
		Optional.empty(), //
		Optional.empty(), //
		false, //
		false);

	return OpenSearchUtils.toJSONSourcesList(response);
    }

    /**
     * <b>NOTE</b>: use it with care! It returns a maximum of {@link #MAX_DEFAULT_HITS} which are requested (and
     * optionally returned) in a single query.<br>
     * <b>WARNING</b>: this method is actually called by
     * {@link OpenSearchReader#getResources(eu.essi_lab.api.database.Database.IdentifierType, String)}
     * and {@link OpenSearchReader#getResources(String, eu.essi_lab.model.GSSource, boolean)} methods where ALL
     * resources should be searched and the limit of {@link #MAX_DEFAULT_HITS} could be a problem
     * 
     * @param index
     * @param searchQuery
     * @return
     * @throws Exception
     */
    public List<JSONObject> searchSources(String index, Query searchQuery) throws Exception {

	SearchResponse<Object> response = search(//
		index, //
		searchQuery, //
		Arrays.asList(), //
		0, //
		MAX_DEFAULT_HITS, //
		Optional.empty(), //
		Optional.empty(), //
		false, //
		false);

	return OpenSearchUtils.toJSONSourcesList(response);
    }

    /**
     * <b>NOTE</b>: use it with care! It returns a maximum of {@link #MAX_DEFAULT_HITS} which are requested (and
     * optionally returned) in a single query.<br>
     * The search request is performed excluding binaries and only the given <code>field</code> is searched and
     * returned.<br>
     * Actually this method is called by {@link FolderRegistry#getRegisteredFolders()},
     * {@link OpenSearchFolder#listKeys()} for non-data folders,
     * and by {@link OpenSearchReader#getViewIdentifiers(eu.essi_lab.api.database.GetViewIdentifiersRequest)}; in all
     * these cases the limitation of {@link #MAX_DEFAULT_HITS} is not a problem
     * 
     * @param searchQuery
     * @param field
     * @return
     * @throws Exception
     */
    public List<String> searchField(String index, Query searchQuery, String field) throws Exception {

	return searchField(index, searchQuery, field, 0, MAX_DEFAULT_HITS);
    }

    /**
     * <b>NOTE</b>: if <code>size</code> is greater than {@link #MAX_DEFAULT_HITS}, the request will fail and an
     * {@link OpenSearchException} will be thrown.<br>
     * The search request is performed excluding binaries and only the given <code>field</code> is searched and returned
     * 
     * @param searchQuery
     * @param field
     * @param start
     * @param size
     * @return
     * @throws Exception
     */
    public List<String> searchField(//
	    String index, //
	    Query searchQuery, //
	    String field, //
	    int start, //
	    int size) throws Exception {

	SearchResponse<Object> response = search(//
		index, //
		searchQuery, //
		Arrays.asList(field), //
		start, //
		size, //
		Optional.empty(), // sorted properties
		Optional.empty(), // search after
		false, // request cache
		true); // exclude binary

	return OpenSearchUtils.toFieldsList(response, field);
    }

    /**
     * See <a href=
     * "https://stackoverflow.com/questions/74823431/how-to-implement-nested-aggregations-using-opensearch-java-client">Nested
     * aggregations</a>
     * 
     * @param searchQuery
     * @param field
     * @param max
     * @return
     * @throws IOException
     * @throws OpenSearchException
     */
    public double findMinMaxValue(Query searchQuery, String field, boolean max) throws OpenSearchException, IOException {

	Map<String, Aggregation> map = new HashMap<>();

	String aggName = "minMaxAgg";

	Aggregation agg = max ? new Aggregation.Builder().max(new MaxAggregation.Builder().field(field).build()).build()
		: new Aggregation.Builder().min(new MinAggregation.Builder().field(field).build()).build();

	map.put(aggName, agg);

	SearchResponse<Object> response = client.search(builder -> {

	    builder.index(DataFolderMapping.get().getIndex()).//
		    query(searchQuery).//
		    size(0).//
		    aggregations(map);

	    if (OpenSearchDatabase.debugQueries) {

		debugMinMaxRequest(searchQuery, field, map);
	    }

	    return builder;

	}, Object.class);

	Map<String, Aggregate> aggregations = response.aggregations();

	if (max) {

	    return aggregations.get(aggName).max().value();
	}

	return aggregations.get(aggName).min().value();
    }

    /**
     * @param index
     * @param searchQuery
     * @return
     */
    public DeleteByQueryRequest buildDeleteByQueryRequest(String index, Query searchQuery) {

	return new DeleteByQueryRequest.Builder().//
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

    public boolean delete(String index, List<String> entries) throws OpenSearchException, IOException {

	List<BulkOperation> operations = new ArrayList<>();
	for (String id : entries) {
	    operations.add(BulkOperation.of(b -> b.delete(d -> d.index(index).id(id))));
	}

	BulkRequest bulkRequest = new BulkRequest.Builder().operations(operations).build();

	BulkResponse response = client.bulk(bulkRequest);

	synch();

	if (response.errors()) {

	    response.items().forEach(item -> {
		if (item.error() != null) {
		    GSLoggerFactory.getLogger(getClass()).error("Failed to delete document ID {}: {}", item.id(), item.error().reason());
		}
	    });
	    return false;
	} else {
	    return true;
	}

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
			json(indexData.getDataString()).build());

	String bodyAsString = response.getBody().//
		get().//
		bodyAsString();

	JSONObject responseObject = new JSONObject(bodyAsString);

	return responseObject.getString("result").equalsIgnoreCase("created");
    }

    /**
     * @param indexData
     * @return
     * @throws OpenSearchException
     * @throws IOException
     */
    public boolean storeWithOpenSearchClient(IndexData indexData) throws OpenSearchException, IOException {

	IndexRequest<Map<String, Object>> indexRequest = indexData.getIndexRequest();

	IndexResponse indexResponse = client.index(indexRequest);

	Result result = indexResponse.result();

	return result == Result.Created;
    }

    /**
     * @param searchQuery
     * @return
     * @throws IOException
     * @throws OpenSearchException
     */
    public int count(String index, Query searchQuery) throws OpenSearchException, IOException {

	CountRequest countRequest = new CountRequest.Builder().//
		query(searchQuery).//
		index(index).//
		build();

	return (int) client.count(countRequest).count();

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
     * @param field
     * @param aggregation
     */
    private void debugCountRequest(Query searchQuery, String field, Aggregation aggregation) {

	org.opensearch.client.opensearch.core.SearchRequest.Builder clone = new SearchRequest.Builder();

	clone.query(searchQuery).//
		index(DataFolderMapping.get().getIndex());

	clone.aggregations(DataFolderMapping.toKeywordField(field), aggregation);

	clone.size(0);

	JSONObject object = OpenSearchUtils.toJSONObject(clone.build());
	object.put("index", DataFolderMapping.get().getIndex());

	GSLoggerFactory.getLogger(getClass()).debug(object.toString(3));
    }

    /**
     * @param searchQuery
     * @param field
     * @param map
     */
    private void debugMinMaxRequest(Query searchQuery, String field, Map<String, Aggregation> map) {

	GSLoggerFactory.getLogger(getClass()).debug("\n\n--- MIN/MAX QUERY ---\n");

	org.opensearch.client.opensearch.core.SearchRequest.Builder clone = new SearchRequest.Builder();

	clone.index(DataFolderMapping.get().getIndex()).//
		query(searchQuery).//
		size(0).//
		aggregations(map);

	JSONObject object = OpenSearchUtils.toJSONObject(clone.build());
	object.put("index", DataFolderMapping.get().getIndex());

	GSLoggerFactory.getLogger(getClass()).debug(object.toString(3));
    }

    /**
     * @param searchQuery
     * @param maxItems
     */
    private void debugCountRequest(Query searchQuery, List<Queryable> targets, int maxItems) {

	org.opensearch.client.opensearch.core.SearchRequest.Builder clone = new SearchRequest.Builder();

	targets.forEach(trg -> {

	    clone.aggregations(trg.getName(), agg -> agg.terms(t -> t.field(

		    DataFolderMapping.toKeywordField(trg.getName())).size(maxItems)));
	});

	clone.query(searchQuery).//
		index(DataFolderMapping.get().getIndex());

	clone.size(0);

	clone.trackTotalHits(new TrackHits.Builder().enabled(true).build());

	JSONObject object = OpenSearchUtils.toJSONObject(clone.build());
	object.put("index", DataFolderMapping.get().getIndex());

	GSLoggerFactory.getLogger(getClass()).debug(object.toString(3));
    }

    /**
     * @param searchQuery
     * @param index
     * @param size
     * @param searchAfter
     * @param start
     * @param orderingProperty
     * @param sortOrder
     * @param fields
     * @param excludeResourceBinary
     * @param requestCache
     */
    private void debugSearchRequest(//
	    Query searchQuery, //
	    String index, //
	    Integer size, //
	    Optional<SearchAfter> searchAfter, //
	    Integer start, //
	    Optional<SortedFields> sortedFields, //
	    List<String> fields, //
	    boolean excludeResourceBinary, //
	    boolean requestCache) {

	org.opensearch.client.opensearch.core.SearchRequest.Builder clone = new SearchRequest.Builder();

	clone.query(searchQuery).//
		index(index);

	clone.size(size);

	if (searchAfter.isPresent()) {

	    List<FieldValue> myFields = getFieldValues(searchAfter.get().getValues());
	    clone.searchAfterVals(myFields);

	} else {

	    clone.from(start);
	}

	if (sortedFields.isPresent()) {

	    handleSort(clone, sortedFields.get());
	}

	handleSourceFields(null, clone, fields, excludeResourceBinary);

	if (requestCache) {

	    clone.requestCache(true);
	}

	JSONObject object = OpenSearchUtils.toJSONObject(clone.build());
	object.put("index", index);

	GSLoggerFactory.getLogger(getClass()).debug(object.toString(3));
    }

    /**
     * @param builder
     * @param orderingProperty
     * @param sortOrder
     */
    private void handleSort(//
	    org.opensearch.client.opensearch.core.SearchRequest.Builder builder, //
	    SortedFields sortedFields) {

	List<SortOptions> sortOptions = new ArrayList<SortOptions>();

	for (SimpleEntry<Queryable, eu.essi_lab.model.SortOrder> sortedField : sortedFields.getFields()) {

	    Queryable orderingProperty = sortedField.getKey();
	    eu.essi_lab.model.SortOrder sortOrder = sortedField.getValue();

	    ContentType contentType = orderingProperty.getContentType();

	    String field = contentType == ContentType.TEXTUAL ? DataFolderMapping.toKeywordField(orderingProperty.getName())
		    : orderingProperty.getName();

	    SortOptions sortOption = new SortOptions.Builder().//
		    field(new FieldSort.Builder().//
			    field(field).//
			    order(sortOrder == eu.essi_lab.model.SortOrder.ASCENDING ? SortOrder.Asc : SortOrder.Desc).build())
		    .//
		    build();

	    sortOptions.add(sortOption);
	}

	builder.sort(sortOptions);
    }

    /**
     * @param topHitsBuilder
     * @param searchBuilder
     * @param fields
     */
    private void handleSourceFields(//
	    org.opensearch.client.opensearch._types.aggregations.TopHitsAggregation.Builder topHitsBuilder, //
	    org.opensearch.client.opensearch.core.SearchRequest.Builder searchBuilder, //
	    List<String> fields, //
	    boolean excludeResourceBinary) {

	if (!fields.isEmpty()) {

	    ArrayList<String> fields_ = new ArrayList<String>(fields);

	    if (!fields.contains(ResourceProperty.TYPE.getName())) {

		fields_.add(ResourceProperty.TYPE.getName());
	    }

	    if (!fields.contains(IndexData.BINARY_PROPERTY)) {

		fields_.add(IndexData.BINARY_PROPERTY);
	    }

	    if (!fields.contains(DataFolderMapping.GS_RESOURCE) && !excludeResourceBinary) {

		fields_.add(DataFolderMapping.GS_RESOURCE);
	    }

	    if (topHitsBuilder != null) {

		topHitsBuilder.source(src -> src.filter(new SourceFilter.Builder().includes(fields_).//
			build()));

	    } else {

		searchBuilder.source(src -> src.filter(new SourceFilter.Builder().includes(fields_).//
			build()));
	    }
	} else {

	    //
	    // if no fields are specified, excludes all the *_keyword fields since they are
	    // not used to decorate the datasets by the ResourceDecorator.
	    // this exclusion has effect only if the target of the query are GSResources
	    //
	    List<String> toExclude = get_keywordsFields(ResourceProperty.listQueryables());
	    toExclude.addAll(get_keywordsFields(MetadataElement.listQueryables()));

	    // it excludes also the anyText field since it is usually ignored
	    toExclude.add(MetadataElement.ANY_TEXT.getName());
	    toExclude.add(IndexMapping.toKeywordField(MetadataElement.ANY_TEXT.getName()));

	    if (excludeResourceBinary) {

		toExclude.add(DataFolderMapping.GS_RESOURCE);
	    }

	    // it excludes also the data index fields since they are usually ignored
	    toExclude.add(IndexData.BINARY_DATA_TYPE);
	    toExclude.add(IndexData.DATA_TYPE);
	    toExclude.add(IndexData.DATABASE_ID);
	    toExclude.add(IndexData.ENTRY_NAME);
	    toExclude.add(IndexData.FOLDER_ID);
	    toExclude.add(IndexData.FOLDER_NAME);

	    if (topHitsBuilder != null) {

		topHitsBuilder.source(src -> src.filter(new SourceFilter.Builder().excludes(toExclude).//
			build()));
	    } else {

		searchBuilder.source(src -> src.filter(new SourceFilter.Builder().excludes(toExclude).//
			build()));
	    }
	}
    }

    /**
     * @param list
     * @return
     */
    private List<String> get_keywordsFields(List<Queryable> list) {

	return list.stream().//
		filter(rp -> rp.getContentType() == ContentType.TEXTUAL).//
		map(rp -> DataFolderMapping.toKeywordField(rp.getName())).//
		collect(Collectors.toList());

    }

    /**
     * @param target
     * @param size
     * @return
     * @throws IOException
     * @throws Exception
     */
    private List<String> findDistinctValues(Query searchQuery, Queryable target, int size) throws Exception {

	TermsAggregation termsAgg = TermsAggregation.of(//
		a -> a.field(DataFolderMapping.toKeywordField(target.getName())).//
			size(size));

	Aggregation agg = Aggregation.of(a -> a.terms(termsAgg));

	SearchResponse<Object> aggResponse = client.search(builder -> {

	    builder.aggregations(target.getName(), agg);

	    builder.query(searchQuery).//
		    index(DataFolderMapping.get().getIndex()).//
		    size(0);

	    return builder;

	}, Object.class);

	Map<String, Aggregate> aggregations = aggResponse.aggregations();
	Aggregate aggregate = aggregations.get(target.getName());

	StringTermsAggregate sterms = aggregate.sterms();

	return sterms.buckets().//
		array().//
		stream().//
		map(b -> b.key()).//
		collect(Collectors.toList());
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
