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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.opensearch.client.opensearch.core.msearch.MultiSearchResponseItem;
import org.opensearch.client.opensearch.core.msearch.RequestItem;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.opensearch.client.opensearch.core.search.SourceFilter;
import org.opensearch.client.opensearch.core.search.TrackHits;
import org.opensearch.client.opensearch.generic.OpenSearchGenericClient;
import org.opensearch.client.opensearch.generic.Requests;
import org.opensearch.client.opensearch.generic.Response;

import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.mappings.DataFolderMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.IndexMapping;
import eu.essi_lab.api.database.opensearch.query.OpenSearchQueryBuilder;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.Queryable.ContentType;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class OpenSearchWrapper {

    private static final Integer MAX_DEFAULT_HITS = 10000;

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

		return builder;

	    }, Object.class);
	}

	// pl.logPerformance(GSLoggerFactory.getLogger(getClass()));

	return response;
    }

    /**
     * @param searchQuery
     * @param target
     * @param size
     * @return
     * @throws Exception
     */
    @SuppressWarnings("serial")
    public List<JSONObject> aggregateWithNestedAgg(//
	    Query searchQuery, //
	    List<Queryable> sourceFields, //
	    Queryable target, //
	    int size) throws Exception {

	String topHitsAggName = "top_hits_agg";
	String termsAggName = "terms_agg";

	Map<String, Aggregation> map = new HashMap<>();

	Builder topHitsBuilder = new TopHitsAggregation.Builder().//
		size(1);

	handleSourceFields(//
		topHitsBuilder, //
		null, //
		sourceFields.stream().map(q -> q.getName()).collect(Collectors.toList()));

	Aggregation topHitsAgg = new Aggregation.Builder().// takes the first result

		topHits(new TopHitsAggregation.Builder().//
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
	    Optional<Queryable> orderingProperty, //
	    Optional<eu.essi_lab.model.SortOrder> sortOrder, //
	    Optional<SearchAfter> searchAfter, //
	    boolean requestCache, //
	    boolean excludeResourceBinary)

	    throws Exception {

	SearchResponse<Object> response = client.search(builder -> {

	    builder.query(searchQuery).//
		    index(index);

	    builder.size(size);

	    if (searchAfter.isPresent()) {

		searchAfter.get().getDoubleValue().ifPresent(val -> builder.searchAfterVals(FieldValue.of(val)));
		searchAfter.get().getLongValue().ifPresent(val -> builder.searchAfterVals(FieldValue.of(val)));
		searchAfter.get().getStringValue().ifPresent(val -> builder.searchAfterVals(FieldValue.of(val)));

	    } else {

		builder.from(start);
	    }

	    if (orderingProperty.isPresent() && sortOrder.isPresent()) {

		handleSort(builder, orderingProperty.get(), sortOrder.get());
	    }

	    handleSourceFields(null, builder, fields, excludeResourceBinary);

	    if (requestCache) {

		builder.requestCache(true);
	    }

	    return builder;

	}, Object.class);

	// pl.logPerformance(GSLoggerFactory.getLogger(getClass()));

	return response;
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
     * resources should be searched and the limit of {@link #MAX_DEFAULT_HITS} is a problem
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
		Optional.empty(), // ordering property
		Optional.empty(), // sort order
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

	    builder.query(searchQuery).//
		    aggregations(map);

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
     * @param builder
     * @param orderingProperty
     * @param sortOrder
     */
    private void handleSort(//
	    org.opensearch.client.opensearch.core.SearchRequest.Builder builder, //
	    Queryable orderingProperty, eu.essi_lab.model.SortOrder sortOrder) {

	ContentType contentType = orderingProperty.getContentType();
	String field = contentType == ContentType.TEXTUAL ? DataFolderMapping.toKeywordField(orderingProperty.getName())
		: orderingProperty.getName();

	builder.sort(new SortOptions.Builder().//
		field(new FieldSort.Builder().//
			field(field).//
			order(sortOrder == eu.essi_lab.model.SortOrder.ASCENDING ? SortOrder.Asc : SortOrder.Desc).build())
		.//
		build());
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
     * @param topHitsBuilder
     * @param searchBuilder
     * @param fields
     */
    private void handleSourceFields(//
	    org.opensearch.client.opensearch._types.aggregations.TopHitsAggregation.Builder topHitsBuilder, //
	    org.opensearch.client.opensearch.core.SearchRequest.Builder searchBuilder, //
	    List<String> fields) {

	handleSourceFields(topHitsBuilder, searchBuilder, fields, false);
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
