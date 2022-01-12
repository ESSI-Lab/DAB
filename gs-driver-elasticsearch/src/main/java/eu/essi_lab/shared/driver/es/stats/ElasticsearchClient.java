package eu.essi_lab.shared.driver.es.stats;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.TaskOperationFailure;
import org.elasticsearch.action.admin.cluster.node.tasks.list.ListTasksRequest;
import org.elasticsearch.action.admin.cluster.node.tasks.list.ListTasksResponse;
import org.elasticsearch.action.admin.cluster.repositories.get.GetRepositoriesRequest;
import org.elasticsearch.action.admin.cluster.repositories.get.GetRepositoriesResponse;
import org.elasticsearch.action.admin.cluster.snapshots.get.GetSnapshotsRequest;
import org.elasticsearch.action.admin.cluster.snapshots.get.GetSnapshotsResponse;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotRequest;
import org.elasticsearch.action.admin.cluster.snapshots.status.SnapshotIndexShardStatus;
import org.elasticsearch.action.admin.cluster.snapshots.status.SnapshotIndexStatus;
import org.elasticsearch.action.admin.cluster.snapshots.status.SnapshotStatus;
import org.elasticsearch.action.admin.cluster.snapshots.status.SnapshotsStatusRequest;
import org.elasticsearch.action.admin.cluster.snapshots.status.SnapshotsStatusResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.tasks.CancelTasksRequest;
import org.elasticsearch.cluster.metadata.RepositoryMetadata;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.geometry.Geometry;
import org.elasticsearch.geometry.Rectangle;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.ScrollableHitSource;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.snapshots.SnapshotId;
import org.elasticsearch.snapshots.SnapshotInfo;
import org.elasticsearch.snapshots.SnapshotShardFailure;
import org.elasticsearch.snapshots.SnapshotState;
import org.elasticsearch.tasks.TaskInfo;
import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.stats.ComputationResult;
import eu.essi_lab.messages.stats.RegionsManager;
import eu.essi_lab.messages.stats.ResponseItem;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsMessage.GroupByPeriod;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.shared.driver.es.connector.aws.AWSRequestSigningApacheInterceptor;

public class ElasticsearchClient {

    // private static String serviceName = "es";
    // private static String region = "us-west-1";
    // private static String aesEndpoint = ""; // e.g. https://search-mydomain.us-west-1.es.amazonaws.com
    // private static String index = "my-index";
    // private static String type = "_doc";
    // private static String id = "1";

    private static final String STATS_COMPUTING_STARTED = "STATS_COMPUTING_STARTED";
    private static final String STATS_COMPUTING_ENDED = "STATS_COMPUTING_ENDED";
    private static HashMap<String, RestHighLevelClient> clients = new HashMap<>();
    private RestHighLevelClient client = null;
    private String dbName;

    public String getDbName() {
	return dbName;
    }

    public void setDbName(String dbName) {
	this.dbName = dbName;
    }

    public ElasticsearchClient(String amazonHost) {
	// pasword is in the system properties:
	// aws.accessKeyId // aws.secretKey

	String key = "AWS" + amazonHost;

	client = clients.get(key);

	if (client == null) {

	    AWS4Signer signer = new AWS4Signer();
	    signer.setServiceName("es");
	    signer.setRegionName("us-east-1");
	    AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
	    HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor("es", signer, credentialsProvider);
	    client = new RestHighLevelClient(RestClient.builder(HttpHost.create(amazonHost))
		    .setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
	    clients.put(key, client);

	}

    }

    public ElasticsearchClient(String endpoint, String username, String password) {

	String key = endpoint;

	client = clients.get(key);

	if (client == null) {

	    AWS4Signer signer = new AWS4Signer();
	    signer.setServiceName("es");
	    signer.setRegionName("us-east-1");
	    AWSCredentials credentials = new BasicAWSCredentials(username, password);
	    AWSCredentialsProvider credentialsProvier = new AWSStaticCredentialsProvider(credentials);
	    HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor("es", signer, credentialsProvier);
	    client = new RestHighLevelClient(RestClient.builder(HttpHost.create(endpoint))
		    .setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
	    clients.put(key, client);

	}

    }
    public StatisticsResponse compute(StatisticsMessage message) throws GSException {

	GSLoggerFactory.getLogger(getClass()).trace(STATS_COMPUTING_STARTED);

	Queryable frequencyTarget = null;
	Optional<List<Queryable>> frequencyTargets = message.getFrequencyTargets();
	if (frequencyTargets.isPresent()) {
	    List<Queryable> targets = frequencyTargets.get();
	    if (!targets.isEmpty()) {
		frequencyTarget = targets.get(0);
	    }
	}

	Queryable countDistinctTarget = null;
	Optional<Queryable> groupByTarget = message.getGroupByTarget();
	Optional<List<Queryable>> countDistinctTargets = message.getCountDistinctTargets();
	if (countDistinctTargets.isPresent()) {
	    List<Queryable> targets = countDistinctTargets.get();
	    if (!targets.isEmpty()) {
		countDistinctTarget = targets.get(0);
	    }
	}
	Optional<GroupByPeriod> groupByPeriod = message.getGroupByPeriod();

	StatisticsResponse ret = new StatisticsResponse();

	try {

	    BoolQueryBuilder query = QueryBuilders.boolQuery();
	    Bond bond = message.getNormalizedBond();
	    Set<Bond> bonds = new HashSet<>();
	    if (bond instanceof LogicalBond) {
		LogicalBond logicalBond = (LogicalBond) bond;
		if (logicalBond.getLogicalOperator().equals(LogicalOperator.AND)) {
		    for (Bond b : logicalBond.getOperands()) {
			bonds.add(b);
		    }
		} else {
		    bonds.add(logicalBond);
		}
	    } else {
		bonds.add(bond);
	    }
	    for (Bond b : bonds) {
		if (b != null) {
		    if (b instanceof LogicalBond) {
			LogicalBond lb = (LogicalBond) b;
			if (lb.getLogicalOperator().equals(LogicalOperator.NOT)) {
			    Bond fo = lb.getFirstOperand();
			    if (fo instanceof RuntimeInfoElementBond) {
				RuntimeInfoElementBond rfo = (RuntimeInfoElementBond) fo;
				BondOperator negatedOperator = BondOperator.negate(rfo.getOperator());
				if (negatedOperator != null) {
				    rfo.setOperator(negatedOperator);
				    b = rfo;
				}
			    }
			}
		    }

		    if (b instanceof RuntimeInfoElementBond) {
			RuntimeInfoElementBond rieb = (RuntimeInfoElementBond) b;
			RuntimeInfoElement property = rieb.getProperty();
			String value = rieb.getPropertyValue();
			switch (property) {
			case CHRONOMETER_TIME_STAMP:
			case ACCESS_MESSAGE_TIME_STAMP:
			case BULK_DOWNLOAD_MESSAGE_TIME_STAMP:
			case DISCOVERY_MESSAGE_TIME_STAMP:
			case RESULT_SET_TIME_STAMP:
			case WEB_REQUEST_TIME_STAMP:
			    property = RuntimeInfoElement.CHRONOMETER_TIME_STAMP;
			    break;
			case WEB_REQUEST_TIME_STAMP_MILLIS:
			case CHRONOMETER_TIME_STAMP_MILLIS:
			case ACCESS_MESSAGE_TIME_STAMP_MILLIS:
			case BULK_DOWNLOAD_MESSAGE_TIME_STAMP_MILLIS:
			case DISCOVERY_MESSAGE_TIME_STAMP_MILLIS:
			case PROFILER_TIME_STAMP_MILLIS:
			case RESULT_SET_TIME_STAMP_MILLIS:
			    property = RuntimeInfoElement.CHRONOMETER_TIME_STAMP;
			    value = ISO8601DateTimeUtils.getISO8601DateTime(new Date(Long.parseLong(value)));
			    break;
			default:
			    break;
			}
			BondOperator operator = rieb.getOperator();
			QueryBuilder qb = null;
			boolean unexpected = false;
			switch (operator) {
			case LESS:
			    qb = QueryBuilders.rangeQuery(property.getName()).lt(value);
			    break;
			case LESS_OR_EQUAL:
			    qb = QueryBuilders.rangeQuery(property.getName()).lte(value);
			    break;
			case GREATER:
			    qb = QueryBuilders.rangeQuery(property.getName()).gt(value);
			    break;
			case GREATER_OR_EQUAL:
			    qb = QueryBuilders.rangeQuery(property.getName()).gte(value);
			    break;
			case EQUAL:
			    qb = QueryBuilders.termQuery(property.getName() + ".keyword", value);
			    break;
			default:
			    unexpected = true;
			    break;
			}
			if (!unexpected) {
			    if (qb != null) {
				query.must(qb);
			    }
			}
		    }
		}
	    }
	    SearchRequest searchRequest = new SearchRequest(dbName + "-request");

	    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

	    searchRequest.source(sourceBuilder);

	    sourceBuilder.query(query);
	    sourceBuilder.from(0);
	    sourceBuilder.size(0);
	    sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

	    if (frequencyTarget != null) {
		if (frequencyTarget.equals(RuntimeInfoElement.DISCOVERY_MESSAGE_BBOX)) {
		    return computeBBOX(frequencyTarget.getName(), searchRequest, sourceBuilder, query);
		}
		TermsAggregationBuilder aggregation = AggregationBuilders.terms("by_queryable")
			.field(frequencyTarget.getName() + ".keyword");
		Optional<Integer> optionalMax = message.getMaxFrequencyItems();
		if (optionalMax.isPresent()) {
		    aggregation.size(optionalMax.get());
		} else {
		    aggregation.size(10);
		}
		sourceBuilder.aggregation(aggregation);
	    } else if (groupByPeriod.isPresent() && countDistinctTarget != null) {

		if (countDistinctTarget instanceof RuntimeInfoElement) {
		    RuntimeInfoElement rie = (RuntimeInfoElement) countDistinctTarget;
		    switch (rie) {
		    case CHRONOMETER_TIME_STAMP:
		    case ACCESS_MESSAGE_TIME_STAMP:
		    case BULK_DOWNLOAD_MESSAGE_TIME_STAMP:
		    case DISCOVERY_MESSAGE_TIME_STAMP:
		    case RESULT_SET_TIME_STAMP:
		    case WEB_REQUEST_TIME_STAMP:
			countDistinctTarget = RuntimeInfoElement.CHRONOMETER_TIME_STAMP;
			break;
		    case WEB_REQUEST_TIME_STAMP_MILLIS:
		    case CHRONOMETER_TIME_STAMP_MILLIS:
		    case ACCESS_MESSAGE_TIME_STAMP_MILLIS:
		    case BULK_DOWNLOAD_MESSAGE_TIME_STAMP_MILLIS:
		    case DISCOVERY_MESSAGE_TIME_STAMP_MILLIS:
		    case PROFILER_TIME_STAMP_MILLIS:
		    case RESULT_SET_TIME_STAMP_MILLIS:
			countDistinctTarget = RuntimeInfoElement.CHRONOMETER_TIME_STAMP;
			break;
		    default:
			break;
		    }

		}

		DateHistogramInterval dhi = new DateHistogramInterval(groupByPeriod.get().getInterval());
		DateHistogramAggregationBuilder aggregation = AggregationBuilders.dateHistogram("date_histogram")
			.field(countDistinctTarget.getName()).calendarInterval(dhi);
		sourceBuilder.aggregation(aggregation);
	    }
	    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

	    int itemsCount = 0;
	    RestStatus status = searchResponse.status();

	    TimeValue took = searchResponse.getTook();
	    System.out.println(status.toString() + " took " + took.toString());

	    Aggregations aggregations = searchResponse.getAggregations();
	    Aggregation aggregationResult = aggregations.asList().get(0);
	    if (aggregationResult instanceof ParsedStringTerms) {
		ResponseItem responseItem = new ResponseItem();
		ret.getItems().add(responseItem);
		itemsCount++;
		ComputationResult frequencyResult = new ComputationResult();
		frequencyResult.setTarget("");
		responseItem.addFrequency(frequencyResult);
		ParsedStringTerms pst = (ParsedStringTerms) aggregationResult;
		List<? extends Bucket> buckets = pst.getBuckets();
		String frequencyValue = "";
		for (Bucket bucket : buckets) {
		    frequencyValue += URLEncoder.encode(bucket.getKeyAsString(), "UTF-8") + ComputationResult.FREQUENCY_ITEM_SEP
			    + bucket.getDocCount() + " ";
		}
		frequencyValue = frequencyValue.trim();
		frequencyResult.setValue(frequencyValue);
	    } else if (aggregationResult instanceof ParsedDateHistogram) {
		ParsedDateHistogram pdh = (ParsedDateHistogram) aggregationResult;
		List<? extends org.elasticsearch.search.aggregations.bucket.histogram.Histogram.Bucket> buckets = pdh.getBuckets();
		for (org.elasticsearch.search.aggregations.bucket.histogram.Histogram.Bucket bucket : buckets) {
		    ResponseItem responseItem = new ResponseItem();
		    ret.getItems().add(responseItem);
		    itemsCount++;
		    ComputationResult countDistinct = new ComputationResult();
		    responseItem.addCountDistinct(countDistinct);
		    Optional<Date> date = ISO8601DateTimeUtils.parseISO8601ToDate(bucket.getKeyAsString());
		    String start = ISO8601DateTimeUtils.getISO8601DateTime(date.get());
		    String end = ISO8601DateTimeUtils.getISO8601DateTime(new Date(date.get().getTime() + groupByPeriod.get().getPeriod()));
		    responseItem.setGroupedBy(start + ResponseItem.ITEMS_RANGE_SEPARATOR + end);
		    countDistinct.setValue("" + bucket.getDocCount());
		}

	    }

	    ret.setItemsCount(itemsCount);

	    GSLoggerFactory.getLogger(getClass()).trace(STATS_COMPUTING_ENDED);

	    return ret;
	} catch (

	Exception e) {
	    e.printStackTrace();
	    throw new GSException();
	}

    }

    private StatisticsResponse computeBBOX(String targetName, SearchRequest searchRequest, SearchSourceBuilder sourceBuilder,
	    BoolQueryBuilder query) throws IOException {

	JSONArray regions = RegionsManager.getRegions();
	String value = "";
	for (int i = 0; i < regions.length(); i++) {
	    JSONObject bbox = regions.getJSONObject(i);
	    Double south = Double.parseDouble(bbox.get("south").toString());
	    Double west = Double.parseDouble(bbox.get("west").toString());
	    Double north = Double.parseDouble(bbox.get("north").toString());
	    Double east = Double.parseDouble(bbox.get("east").toString());
	    Geometry shape = new Rectangle(west, east, north, south);
	    BoolQueryBuilder query2 = QueryBuilders.boolQuery();
	    query2.must(query);
	    query2.must(QueryBuilders.geoWithinQuery(RuntimeInfoElement.DISCOVERY_MESSAGE_SHAPE.getName(), shape));
	    sourceBuilder.query(query2);
	    try {
		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		long v = searchResponse.getHits().getTotalHits().value;
		value += south + "," + west + "," + north + "," + east + "ITEMSEP" + v + " ";
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	value = value.trim();
	StatisticsResponse ret = new StatisticsResponse();
	ret.setItemsCount(1);
	ResponseItem responseItem = new ResponseItem();
	ret.getItems().add(responseItem);
	ComputationResult result = new ComputationResult();
	result.setTarget(targetName);
	result.setValue(value);
	responseItem.addFrequency(result);
	return ret;

    }

    public void deleteBefore(String time) throws IOException {
	DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(dbName);
	String property = RuntimeInfoElement.CHRONOMETER_TIME_STAMP.getName();
	RangeQueryBuilder timeBuilder = QueryBuilders.rangeQuery(property).lt(time);
	BoolQueryBuilder query = QueryBuilders.boolQuery();
	query.must(timeBuilder);
	deleteByQueryRequest.setQuery(query);
	BulkByScrollResponse response = client.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
	long deleted = response.getDeleted();
	System.out.println("Deleted: " + deleted);
    }

    public void deleteAfter(String time) throws IOException {
	DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(dbName);
	String property = RuntimeInfoElement.CHRONOMETER_TIME_STAMP.getName();
	RangeQueryBuilder timeBuilder = QueryBuilders.rangeQuery(property).gt(time);
	BoolQueryBuilder query = QueryBuilders.boolQuery();
	query.must(timeBuilder);
	deleteByQueryRequest.setQuery(query);
	BulkByScrollResponse response = client.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
	long deleted = response.getDeleted();
	System.out.println("Deleted: " + deleted);
    }

    public void delete(String... indexes) throws IOException {
	for (String index : indexes) {
	    String dbIndex = getDbName().toLowerCase() + "-" + index;
	    GSLoggerFactory.getLogger(getClass()).info("Elastic search deleting index {}", dbIndex);
	    try {
		boolean exists = checkIndexExistence(dbIndex);
		if (exists) {
		    DeleteIndexRequest cir = new DeleteIndexRequest(dbIndex);
		    GSLoggerFactory.getLogger(getClass()).info("Elasticsearch deleting index {}", index);
		    client.indices().delete(cir, RequestOptions.DEFAULT);
		    GSLoggerFactory.getLogger(getClass()).info("Elasticsearch deleting index SUCCESS {} ", index);
		}
	    } catch (IOException e) {
		e.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).error("Elasticsearch deleting index ERROR {}", index);
		throw e;
	    }
	}

    }

    public synchronized void init(String... indexes) throws IOException {

	for (String index : indexes) {
	    String dbIndex = getDbName().toLowerCase() + "-" + index;
	    GSLoggerFactory.getLogger(getClass()).info("Elastic search checking index {}", dbIndex);
	    try {
		boolean exists = checkIndexExistence(dbIndex);
		if (!exists) {
		    CreateIndexRequest cir = new CreateIndexRequest(dbIndex);
		    InputStream mappingStream = ElasticsearchClient.class.getClassLoader()
			    .getResourceAsStream("es-mappings/" + index + "-mapping.json");
		    if (mappingStream != null) {
			// optional mapping
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(mappingStream, baos);
			mappingStream.close();
			baos.close();
			String str = new String(baos.toByteArray());
			cir.mapping(str, XContentType.JSON);
		    }
		    GSLoggerFactory.getLogger(getClass()).info("Elasticsearch creating index {}", index);
		    client.indices().create(cir, RequestOptions.DEFAULT);
		    GSLoggerFactory.getLogger(getClass()).info("Elasticsearch creating index SUCCESS {} ", index);
		}
	    } catch (IOException e) {
		e.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).error("Elasticsearch creating index ERROR {}", index);
		throw e;
	    }

	}

    }

    public boolean checkIndexExistence(String index) throws IOException {
	org.elasticsearch.client.indices.GetIndexRequest request = new org.elasticsearch.client.indices.GetIndexRequest(index);
	RequestOptions options = RequestOptions.DEFAULT;
	return client.indices().exists(request, options);
    }

    /**
     * @param index
     * @param jsonMap it's a json map id to String JSON
     * @return
     */
    public boolean write(String index, JSONObject... content) {
	HashMap<String, String> items = new HashMap<>();
	for (int i = 0; i < content.length; i++) {
	    String itemId = content[i].getString("runtimeId");
	    String item = content[i].toString();
	    items.put(itemId, item);
	}
	return write(index, items);
    }

    public boolean write(String index, HashMap<String, String> items) {
	if (dbName != null) {
	    index = dbName + "-" + index;
	}
	BulkRequest request = new BulkRequest();
	List<String> failedItems = new ArrayList<>();
	Set<Entry<String, String>> entries = items.entrySet();
	for (Entry<String, String> entry : entries) {
	    String itemId = entry.getKey();
	    String item = entry.getValue();
	    items.put(itemId, item);
	    request.add(new IndexRequest(index).id(itemId).source(item, XContentType.JSON));
	}
	Boolean success = null;
	IOException unexpectedError = null;
	String failureMessage = null;
	int maxTry = 10;
	BulkResponse indexResponse = null;
	while (indexResponse == null && maxTry-- > 0) {
	    try {
		indexResponse = client.bulk(request, RequestOptions.DEFAULT);
	    } catch (IOException e) {
		unexpectedError = e;
		GSLoggerFactory.getLogger(getClass()).error("Elasticsearch unexpected errors (sleep a bit before retry): {} try: {}",
			e.getMessage(), maxTry);
		try {
		    Thread.sleep(120000);
		} catch (InterruptedException e1) {
		    e1.printStackTrace();
		}
	    }
	}
	if (indexResponse == null) {
	    GSLoggerFactory.getLogger(getClass()).error("Elasticsearch unexpected error: {}", unexpectedError.getMessage());
	    failureMessage = unexpectedError.getMessage() + "\n\n";
	    for (String item : items.values()) {
		failureMessage += item + "\n\n";
	    }
	    success = false;
	} else {
	    Iterator<BulkItemResponse> it = indexResponse.iterator();
	    while (it.hasNext()) {
		BulkItemResponse bulkItemResponse = (BulkItemResponse) it.next();
		DocWriteResponse response = bulkItemResponse.getResponse();
		if (bulkItemResponse.isFailed()) {
		    String reason = bulkItemResponse.getFailureMessage();
		    String itemId = response.getId();
		    String item = items.get(itemId);
		    failedItems.add(item);
		    GSLoggerFactory.getLogger(getClass()).error("Elasticsearch index failure: {} {} {}", reason, itemId, item);
		} else {
		    if (response.getResult() == DocWriteResponse.Result.CREATED) {
			GSLoggerFactory.getLogger(getClass()).trace("Elasticsearch document created {} {}", index, response.getId());
		    } else if (response.getResult() == DocWriteResponse.Result.UPDATED) {
			GSLoggerFactory.getLogger(getClass()).trace("Elasticsearch document updated {} {}", index, response.getId());
		    }
		}
	    }
	    if (indexResponse.hasFailures()) {
		String message = indexResponse.buildFailureMessage();
		GSLoggerFactory.getLogger(getClass()).error("Elasticsearch index with failures: {}", message);
		failureMessage = message + "\n\n";
		for (String f : failedItems) {
		    failureMessage += f + "\n\n";
		}
		success = false;
	    } else {
		success = true;
	    }
	}
	if (!success) {
	    GSMailSenderElasticsearch.sendEmail(
		    GSMailSenderElasticsearch.MAIL_REPORT_STATISTICS + GSMailSenderElasticsearch.MAIL_ERROR_SUBJECT,
		    "Unable to index document(s) \n\n" + failureMessage);
	}
	return success;

    }

    public void close() throws IOException {
	client.close();

    }

    public JSONObject getDocument(String index, String id) {
	if (dbName != null) {
	    index = dbName + "-" + index;
	}
	GetRequest request = new GetRequest(index, id);
	GetResponse getResponse;
	try {
	    getResponse = client.get(request, RequestOptions.DEFAULT);
	} catch (IOException e) {
	    e.printStackTrace();
	    return null;
	}
	String source = getResponse.getSourceAsString();
	JSONObject ret = new JSONObject(source);
	return ret;
    }

    public void updateMessageType(String index, String id) {
	if (dbName != null) {
	    index = dbName + "-" + index;
	}
	UpdateRequest request = new UpdateRequest(index, id);
	Map<String, Object> parameters = new HashMap<>();
	Script inline = new Script(ScriptType.INLINE, "painless", updateScript, parameters);

	request.script(inline);
	try {
	    UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);
	    GSLoggerFactory.getLogger(getClass()).info("Elasticsearch update result: {}", updateResponse.getResult().toString());
	} catch (IOException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error("Elasticsearch error during update");
	}

    }

    String updateScript = "if (ctx._source.DISCOVERY_MESSAGE_PAGE_SIZE != null && ctx._source.MESSAGE_TYPE == null) { ctx._source.MESSAGE_TYPE = 'DiscoveryMessage' }";

    public void updateMessageType(String index) {
	if (dbName != null) {
	    index = dbName + "-" + index;
	}
	UpdateByQueryRequest request = new UpdateByQueryRequest(index);
	// request.setQuery(new TermQueryBuilder("runtimeId", "b9051c5c-9f09-4624-ad9d-d8a60565ce89"));
	// request.setMaxDocs(100);

	request.setScript(new Script(ScriptType.INLINE, "painless", updateScript, Collections.emptyMap()));

	Map<String, Object> parameters = new HashMap<>();
	Script inline = new Script(ScriptType.INLINE, "painless", updateScript, parameters);

	request.setScript(inline);

	// ActionListener<BulkByScrollResponse> listener = new ActionListener<BulkByScrollResponse>() {
	// @Override
	// public void onResponse(BulkByScrollResponse bulkResponse) {
	// TimeValue timeTaken = bulkResponse.getTook();
	// boolean timedOut = bulkResponse.isTimedOut();
	// long totalDocs = bulkResponse.getTotal();
	// long updatedDocs = bulkResponse.getUpdated();
	// long deletedDocs = bulkResponse.getDeleted();
	// long batches = bulkResponse.getBatches();
	// long noops = bulkResponse.getNoops();
	// long versionConflicts = bulkResponse.getVersionConflicts();
	// long bulkRetries = bulkResponse.getBulkRetries();
	// long searchRetries = bulkResponse.getSearchRetries();
	// TimeValue throttledMillis = bulkResponse.getStatus().getThrottled();
	// TimeValue throttledUntilMillis = bulkResponse.getStatus().getThrottledUntil();
	// List<ScrollableHitSource.SearchFailure> searchFailures = bulkResponse.getSearchFailures();
	// List<BulkItemResponse.Failure> bulkFailures = bulkResponse.getBulkFailures();
	// System.out.println("Ended");
	// }
	//
	// @Override
	// public void onFailure(Exception e) {
	// GSLoggerFactory.getLogger(getClass()).error("Unexpected error during batch update");
	// e.printStackTrace();
	// }
	// };
	//
	// client.updateByQueryAsync(request, RequestOptions.DEFAULT, listener);

	try {
	    BulkByScrollResponse bulkResponse = client.updateByQuery(request, RequestOptions.DEFAULT);
	    TimeValue timeTaken = bulkResponse.getTook();
	    boolean timedOut = bulkResponse.isTimedOut();
	    long totalDocs = bulkResponse.getTotal();
	    long updatedDocs = bulkResponse.getUpdated();
	    long deletedDocs = bulkResponse.getDeleted();
	    long batches = bulkResponse.getBatches();
	    long noops = bulkResponse.getNoops();
	    long versionConflicts = bulkResponse.getVersionConflicts();
	    long bulkRetries = bulkResponse.getBulkRetries();
	    long searchRetries = bulkResponse.getSearchRetries();
	    TimeValue throttledMillis = bulkResponse.getStatus().getThrottled();
	    TimeValue throttledUntilMillis = bulkResponse.getStatus().getThrottledUntil();
	    List<ScrollableHitSource.SearchFailure> searchFailures = bulkResponse.getSearchFailures();
	    List<BulkItemResponse.Failure> bulkFailures = bulkResponse.getBulkFailures();
	    System.out.println("Ended time " + timeTaken.seconds() + " total " + totalDocs + " updated " + updatedDocs + " noops " + noops);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	GSLoggerFactory.getLogger(getClass()).info("Elasticsearch batch update submitted");

    }

    public void showTasks() {

	ListTasksRequest request = new ListTasksRequest();
	try {

	    ListTasksResponse response = client.tasks().list(request, RequestOptions.DEFAULT);
	    List<TaskInfo> tasks = response.getTasks();
	    for (TaskInfo task : tasks) {
		System.out.println(task.getTaskId());
		System.out.println(task.getDescription());
		System.out.println(task.getAction());
		if (task.getAction().contains("update")) {
		    System.out.println();
		}
		System.out.println(new Date(task.getStartTime()));
		System.out.println(task.getType());
		System.out.println(task.getStatus());
		System.out.println("\n");
	    }

	    List<TaskOperationFailure> taskFailures = response.getTaskFailures();
	    for (TaskOperationFailure taskFailure : taskFailures) {
		System.out.println("failure: " + taskFailure.getTaskId());
	    }

	} catch (IOException e) {
	    e.printStackTrace();
	}

	GSLoggerFactory.getLogger(getClass()).info("Elasticsearch task view ended");

    }

    public void cancelTask(String taskId) {
	CancelTasksRequest byTaskIdRequest = new org.elasticsearch.client.tasks.CancelTasksRequest.Builder()
		.withTaskId(new org.elasticsearch.client.tasks.TaskId(taskId)).withWaitForCompletion(true).build();

	try {
	    client.tasks().cancel(byTaskIdRequest, RequestOptions.DEFAULT);
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

    public static void main(String[] args) throws Exception {
	ElasticsearchClient client = new ElasticsearchClient(System.getProperty("amazonHost"));
	client.setIndexes("production-_request-backup", "production-request", "production-request-restored");

	// client.delete("requests-new");
	// client.deleteBefore("2021-07-01T00:00:00Z");

	// long count = client.count();
	// System.out.println("Total count: " + count);
	//
	boolean includeHarvest = false;
	client.downloadGEOSSRequests(includeHarvest,new File("/home/boldrini/requests.json"));
	System.out.println("Include harvest: " + includeHarvest);

	// client.listSnapshots(ISO8601DateTimeUtils.parseISO8601("2021-07-29"));
	// client.listSnapshots(null);

	// client.recoveryInformation("production","_request-backup");
	// client.countGEOSS();

//	long count = client.countGEOSS();
//	System.out.println(count);

//	client.printKeywords(2020);

	// client.restoreSnapshot("cs-automated", "2021-07-29t22-48-13.81b2d4bc-26cc-43d6-b5d5-a43232a65b76",
	// "production", "request");
	client.close();
    }

    String[] indexes = null;

    private void setIndexes(String... indexes) {
	this.indexes = indexes;

    }

    private void recoveryInformation(String dbName, String index) throws IOException {
	Request request = new Request("GET", "/" + dbName + "-" + index + "/_recovery");
	Response response = client.getLowLevelClient().performRequest(request);
	String responseBody = EntityUtils.toString(response.getEntity());
	System.out.println(responseBody);
    }

    public void restoreSnapshot(String repositoryName, String snapshotName, String dbName, String index) throws IOException {
	RestoreSnapshotRequest request = new RestoreSnapshotRequest(repositoryName, snapshotName);
	request.indices(dbName + "-" + index);
	request.renamePattern(dbName + "-(.+)");
	request.renameReplacement(dbName + "-$1-backup");
	request.waitForCompletion(false);
	client.snapshot().restore(request, RequestOptions.DEFAULT);
    }

    public void listSnapshots(Date minDate) throws Exception {
	GetRepositoriesRequest repoRequest = new GetRepositoriesRequest();
	String[] repositories = new String[] {};
	repoRequest.repositories(repositories);
	GetRepositoriesResponse repoResponse = this.client.snapshot().getRepository(repoRequest, RequestOptions.DEFAULT);
	List<RepositoryMetadata> repos = repoResponse.repositories();

	List<String> details = new ArrayList<String>();

	for (RepositoryMetadata repo : repos) {
	    System.out.println(repo.name());
	    GetSnapshotsRequest request = new GetSnapshotsRequest();
	    request.repository(repo.name());
	    GetSnapshotsResponse response = client.snapshot().get(request, RequestOptions.DEFAULT);
	    List<SnapshotInfo> snapshotsInfos = response.getSnapshots();

	    for (SnapshotInfo snapshotsInfo : snapshotsInfos) {
		SnapshotInfo snapshotInfo = snapshotsInfo;
		RestStatus restStatus = snapshotInfo.status();
		SnapshotId snapshotId = snapshotInfo.snapshotId();
		SnapshotState snapshotState = snapshotInfo.state();
		List<SnapshotShardFailure> snapshotShardFailures = snapshotInfo.shardFailures();
		long startTime = snapshotInfo.startTime();
		long endTime = snapshotInfo.endTime();
		Date endDate = new Date(endTime);
		if (minDate == null || minDate.before(endDate)) {
		    details.add(snapshotId.getName());
		    System.out.println(snapshotId.getName() + " " + snapshotId.getUUID());
		    listSnapshotDetails(repo.name(), snapshotId.getName());
		}
	    }

	}

    }

    public void listSnapshotDetails(String repositoryName, String snapshotName) throws IOException {
	SnapshotsStatusRequest statusRequest = new SnapshotsStatusRequest();
	statusRequest.repository(repositoryName);
	statusRequest.snapshots(new String[] { snapshotName });
	SnapshotsStatusResponse statusResponse = client.snapshot().status(statusRequest, RequestOptions.DEFAULT);
	List<SnapshotStatus> snapshotStatusesResponse = statusResponse.getSnapshots();
	for (SnapshotStatus snapshotStatus : snapshotStatusesResponse) {
	    System.out.println("Snapshot " + snapshotName);
	    Map<String, SnapshotIndexStatus> indices = snapshotStatus.getIndices();
	    Set<Entry<String, SnapshotIndexStatus>> entries = indices.entrySet();
	    for (Entry<String, SnapshotIndexStatus> entry : entries) {
		String id = entry.getKey();

		SnapshotIndexStatus value = entry.getValue();

		Map<Integer, SnapshotIndexShardStatus> shards = value.getShards();
		long size = 0;
		long count = 0;
		for (Entry<Integer, SnapshotIndexShardStatus> shardEntry : shards.entrySet()) {
		    SnapshotIndexShardStatus shardStatus = shardEntry.getValue();
		    size += shardStatus.getStats().getTotalSize();
		    count += shardStatus.getStats().getTotalFileCount();
		}
		System.out.println("Snapshot " + id + " " + value.getIndex() + " size: " + size + " count: " + count);
	    }
	    System.out.println();
	}

    }

    public void downloadGEOSSRequests(boolean includeHarvest, File file) throws Exception {
	BoolQueryBuilder builder = getGEOSSQuery(includeHarvest);
	String[] indices = new String[] { dbName + "-request" };
	if (indexes != null) {
	    indices = indexes;
	}
	SearchRequest searchRequest = new SearchRequest(indices);
	searchRequest.scroll(TimeValue.timeValueDays(1L)); 
	SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//	int index = 0;
//	int pageSize = 100;
//	searchSourceBuilder.from(index);
	searchSourceBuilder.size(100);
	searchSourceBuilder.query(builder);
	searchRequest.source(searchSourceBuilder);
	RequestOptions options = RequestOptions.DEFAULT;
	SearchResponse response = client.search(searchRequest, options);
	String scrollId = response.getScrollId();
	
	
	
	SearchHit[] hits = null;
	FileOutputStream fos = new FileOutputStream(file);
	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	int index = 0;
	do {
	    
	    SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId); 
	    scrollRequest.scroll(TimeValue.timeValueSeconds(30));
	    SearchResponse searchScrollResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
	    scrollId = searchScrollResponse.getScrollId();  
	    SearchHits searchHits = searchScrollResponse.getHits();
	    hits = searchHits.getHits();
	    for (SearchHit hit : hits) {
		String str = hit.getSourceAsString();
		bw.write(str);
		bw.newLine();
		index++;
	    }	    
	    System.out.println(index);

	} while (hits != null && hits.length > 0);
	bw.close();
	System.out.println("end (" + index + ")");

    }

    public long countGEOSS(boolean includeHarvest) throws Exception {
	BoolQueryBuilder builder = getGEOSSQuery(includeHarvest);
	return count(builder);
    }

    private BoolQueryBuilder getGEOSSQuery(boolean includeHarvest) {
	BoolQueryBuilder builder = QueryBuilders.boolQuery();

	if (!includeHarvest) {
	    builder.must(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), "opensearch"));
	}

	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), "cite-csw-ri"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), "www.blue-cloud.org"));

	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), "gwps"));

	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.PROFILER_NAME.getName(), "THREDDSProfiler"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.PROFILER_NAME.getName(), "BNHSProfiler"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.PROFILER_NAME.getName(), "HydroServerProfiler"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.PROFILER_NAME.getName(), "SOSProfiler"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.PROFILER_NAME.getName(), "ESRIProfiler"));

	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), "plata"));

	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), "preprod"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), "seadatanet"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), "whos"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), "emodnet"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), "argo"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), "wekeo"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), "icos"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), "elixir-ena"));

	builder.mustNot(QueryBuilders.queryStringQuery("wsdl").defaultField(RuntimeInfoElement.WEB_REQUEST_QUERY_STRING.getName()));

	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_BASE_URI.getName(), "whos.geodab.eu"));

	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REFERER.getName(), "alerta.ina.gob.ar"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REFERER.getName(), "whos.geodab.eu"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REFERER.getName(), "wmo.maps.arcgis.com"));

	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), "bnhs"));

	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.VIEW_ID.getName(), "whos-plata"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.VIEW_ID.getName(), "preprod"));

	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.DISCOVERY_MESSAGE_GS_USER_EMAIL.getName(), "whos"));

	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), "sos-tahmo-proxy"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), "gwis"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), "eurobis"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), "odip"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REQUEST_PATH.getName(), "blue-cloud"));

	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_QUERY_STRING.getName(), "ROOT"));

	if (!includeHarvest) {
	    builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_QUERY_STRING.getName(),
		    "satellitescene_collection_prefix_ChinaGEO_CSES-01"));
	    builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_QUERY_STRING.getName(), "china%20geoss"));
	}

	// builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_QUERY_STRING.getName(),
	// "targetIds"));
	//
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_HOST.getName(), "localhost"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.RUNTIME_CONTEXT.getName(), "https://geoss.devel.esaportal.eu"));

	builder.mustNot(QueryBuilders.prefixQuery(RuntimeInfoElement.WEB_REQUEST_QUERY_STRING.getName(), "targetid"));

	builder.mustNot(QueryBuilders.queryStringQuery("localhost").defaultField(RuntimeInfoElement.WEB_REQUEST_QUERY_STRING.getName()));

	//
	// CNR
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_X_FORWARDER_FOR.getName(), "149.139.19.2"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_X_FORWARDER_FOR.getName(), "149.139.19.84"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_X_FORWARDER_FOR.getName(), "149.139.19.85"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_X_FORWARDER_FOR.getName(), "149.139.19.86"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_X_FORWARDER_FOR.getName(), "149.139.19.89"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_X_FORWARDER_FOR.getName(), "149.139.19.108"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_X_FORWARDER_FOR.getName(), "149.139.19.212"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_X_FORWARDER_FOR.getName(), "93.57.245.45"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_HOST.getName(), "149.139.19.2"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_HOST.getName(), "149.139.19.84"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_HOST.getName(), "149.139.19.85"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_HOST.getName(), "149.139.19.86"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_HOST.getName(), "149.139.19.89"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_HOST.getName(), "149.139.19.108"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_HOST.getName(), "149.139.19.212"));

	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_ADDRESS.getName(), "127.0.0.1"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_HOST.getName(), "127.0.0.1"));

	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.MESSAGE_TYPE.getName(), "AccessMessage"));

	// amazon load balancer
	// builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_ADDRESS.getName(),
	// "10.0.2.53"));
	// builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_ADDRESS.getName(),
	// "10.0.2.152"));
	// builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_ADDRESS.getName(),
	// "10.0.0.152"));
	// builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_HOST.getName(),
	// "10.0.2.157"));
	// builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_HOST.getName(),
	// "10.0.2.53"));
	// builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_HOST.getName(),
	// "10.0.2.152"));
	// builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_HOST.getName(),
	// "10.0.0.113"));
	// builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_HOST.getName(),
	// "10.0.0.152"));
	// builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_HOST.getName(),
	// "10.0.0.69"));
	// builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_HOST.getName(),
	// "10.0.2.87"));
	// builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_HOST.getName(),
	// "10.0.0.192"));

	// polacchi
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_X_FORWARDER_FOR.getName(), "78.11.115.164"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_X_FORWARDER_FOR.getName(), "78.11.115.162"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_X_FORWARDER_FOR.getName(), "83.18.202.10"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_X_FORWARDER_FOR.getName(), "89.73.128.61"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_X_FORWARDER_FOR.getName(), "89.64.75.240"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_X_FORWARDER_FOR.getName(), "89.79.41.237"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_X_FORWARDER_FOR.getName(), "178.73.6.42"));
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_X_FORWARDER_FOR.getName(), "178.73.6.43"));

	// greci
	builder.mustNot(QueryBuilders.matchPhraseQuery(RuntimeInfoElement.WEB_REQUEST_REMOTE_HOST.getName(), "195.251.203.238"));
	return builder;
    }

    public long count() throws Exception {
	return count(null);
    }

    public long count(QueryBuilder builder) throws Exception {
	String[] indices = new String[] { dbName + "-request" };
	if (indexes != null) {
	    indices = indexes;
	}
	CountRequest countRequest = new CountRequest(indices);
	if (builder != null) {
	    countRequest.query(builder);
	    GSLoggerFactory.getLogger(getClass()).info(builder.toString());
	}
	RequestOptions options = RequestOptions.DEFAULT;
	CountResponse count = client.count(countRequest, options);

	return count.getCount();

    }

}
