package eu.essi_lab.access.datacache.opensearch;

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
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.Duration;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.json.JSONObject;
import org.opensearch.action.DocWriteResponse;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.bulk.BulkItemResponse;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.search.SearchScrollRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.core.CountRequest;
import org.opensearch.client.core.CountResponse;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.RangeQueryBuilder;
import org.opensearch.index.reindex.BulkByScrollResponse;
import org.opensearch.index.reindex.DeleteByQueryRequest;
import org.opensearch.search.SearchHit;
import org.opensearch.search.aggregations.Aggregation;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.Aggregations;
import org.opensearch.search.aggregations.metrics.MaxAggregationBuilder;
import org.opensearch.search.aggregations.metrics.MinAggregationBuilder;
import org.opensearch.search.aggregations.metrics.ParsedMax;
import org.opensearch.search.aggregations.metrics.ParsedMin;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.FieldSortBuilder;
import org.opensearch.search.sort.SortOrder;

import eu.essi_lab.access.datacache.BBOX;
import eu.essi_lab.access.datacache.BBOX3857;
import eu.essi_lab.access.datacache.BBOX4326;
import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory.DataConnectorType;
import eu.essi_lab.access.datacache.DataRecord;
import eu.essi_lab.access.datacache.Polygon4326;
import eu.essi_lab.access.datacache.Response;
import eu.essi_lab.access.datacache.ResponseListener;
import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.access.datacache.StationsStatistics;
import eu.essi_lab.access.datacache.StatisticsRecord;
import eu.essi_lab.access.datacache.WKT;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class OpenSearchConnector extends DataCacheConnector {

    private static final String DATE_PROPERTY = "date";
    private static final String ACTIVE_PROPERTY = "active";
    private static final String DATA_IDENTIFIER_PROPERTY = "dataIdentifier";
    private static final String SOURCE_IDENTIFIER_PROPERTY = "sourceIdentifier";
    private static final String THEME_CATEGORY_PROPERTY = "themeCategory";
    private static final String PLATFORM_IDENTIFIER_PROPERTY = "platformIdentifier";
    private static final String NEXT_RECORD_EXPECTED_TIME_PROPERTY = "nextRecordExpectedTime";

    private static final String SOUTH_PROPERTY = "south";
    private static final String EAST_PROPERTY = "east";
    private static final String NORTH_PROPERTY = "north";
    private static final String WEST_PROPERTY = "west";

    private static final String MINX3857_PROPERTY = "minx3857";
    private static final String MAXX3857_PROPERTY = "maxx3857";
    private static final String MINY3857_PROPERTY = "miny3857";
    private static final String MAXY3857_PROPERTY = "maxy3857";

    private Integer maxBulkSize = null;

    private Integer cachedDays = null;

    private RestHighLevelClient client = null;
    private String databaseName = null;

    public enum DataCacheIndex {
	VALUES("values", DataRecord.class), STATISTICS("statistics", StatisticsRecord.class), STATIONS("stations", StationRecord.class);

	private String suffix;

	public String getSuffix() {
	    return suffix;
	}

	public void setSuffix(String suffix) {
	    this.suffix = suffix;
	}

	public Class getRecordType() {
	    return clazz;
	}

	public void setRecordType(Class clazz) {
	    this.clazz = clazz;
	}

	private Class clazz;

	private DataCacheIndex(String suffix, Class clazz) {
	    this.suffix = suffix;
	    this.clazz = clazz;
	}

	public String getIndex(String databaseName) {
	    return databaseName + "-" + suffix;
	}
    }

    @Override
    public boolean supports(DataConnectorType type) {
	switch (type) {
	case OPEN_SEARCH_DOCKERHUB_1_3:
	    return true;
	default:
	    return false;
	}
    }

    /**
     * Constructor needed by service loader
     */
    public OpenSearchConnector() {

    }

    @Override
    public void initialize(URL endpoint, String username, String password, String databaseName) throws Exception {

	// DataConnectorType type

	//
	// DatabaseSetting without config folder
	//

	// FLUSH_INTERVAL_MS:9999
	// MAX_BULK_SIZE:234
	// CACHED_DAYS:43

	if (client != null) {
	    throw new Exception("Already initialized");
	}

	for (int i = 0; i < databaseName.length(); i++) {
	    char c = databaseName.charAt(i);
	    if (i == 0) {
		if (Character.isAlphabetic(c) && Character.isLowerCase(c)) {
		    continue;
		} else {
		    throw new Exception("Invalid OpenSearch index name: " + databaseName + " Offending character at position: " + i);
		}
	    } else {
		if (Character.isDigit(c)) {
		    continue;
		} else if (Character.isAlphabetic(c) && Character.isLowerCase(c)) {
		    continue;
		} else if (c == '-') {
		    continue;
		} else {
		    throw new Exception("Invalid OpenSearch index name: " + databaseName + " Offending character at position: " + i);
		}
	    }
	}

	client = createClient(endpoint, username, password);
	try {
	    client.ping(RequestOptions.DEFAULT);
	} catch (IOException e) {
	    e.printStackTrace();
	    throw new Exception("OpenSearch remote service connection issue");
	}

	this.databaseName = databaseName;

	indexInitialization();

	configure(MAX_BULK_SIZE, DEFAULT_MAX_BULK_SIZE.toString());

	configure(FLUSH_INTERVAL_MS, DEFAULT_FLUSH_INTERVAL_MS.toString());

	configure(CACHED_DAYS, DEFAULT_CACHED_DAYS.toString());

	scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

	    @Override
	    public void run() {
		if (cachedDays != null && cachedDays > 0) {
		    Calendar calendar = Calendar.getInstance();
		    calendar.setTime(new Date());
		    calendar.add(Calendar.DATE, -cachedDays);
		    try {
			deleteFromActiveStationsBefore(calendar.getTime(), null);
		    } catch (Exception e) {
			// TODO send mail
			e.printStackTrace();
		    }
		}

	    }
	}, 0, 1, TimeUnit.HOURS);

	GSLoggerFactory.getLogger(getClass()).info("OS connector initialized: {}", databaseName);

    }

    private void indexInitialization() throws Exception {
	for (DataCacheIndex index : DataCacheIndex.values()) {

	    String indexName = index.getIndex(databaseName);

	    GSLoggerFactory.getLogger(getClass()).info("Elastic search checking index {}", indexName);
	    try {
		boolean exists = checkIndexExistence(indexName);
		if (!exists) {
		    CreateIndexRequest cir = new CreateIndexRequest(indexName);
		    // optional mapping
		    GSLoggerFactory.getLogger(getClass()).info("Configuring mapping of index {}", indexName);
		    JSONObject mapping = getOSMapping(index.getRecordType());
		    cir.mapping(mapping.toString(), XContentType.JSON);
		    GSLoggerFactory.getLogger(getClass()).info("Opensearch creating index {}", indexName);
		    client.indices().create(cir, RequestOptions.DEFAULT);
		    GSLoggerFactory.getLogger(getClass()).info("Opensearch creating index SUCCESS {} ", indexName);
		}
	    } catch (IOException e) {
		e.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).error("Opensearch creating index ERROR {}", indexName);
		throw new Exception("OpenSearch error creating index");
	    }

	}

    }

    protected RestHighLevelClient createClient(URL endpoint, String username, String password) {
	CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

	if (username != null && password != null) {
	    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
	}

	RestClientBuilder builder = RestClient.builder(new HttpHost(endpoint.getHost(), endpoint.getPort(), endpoint.getProtocol()))
		.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
		    @Override
		    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
			return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
		    }
		});

	return new RestHighLevelClient(builder);
    }

    public JSONObject getOSMapping(Class clazz) {
	JSONObject ret = new JSONObject();
	JSONObject properties = new JSONObject();

	Field[] allFields = clazz.getDeclaredFields();
	for (Field field : allFields) {
	    JSONObject typeObject = new JSONObject();
	    Class<?> javaType = field.getType();
	    String osType = null;
	    if (javaType.equals(Date.class)) {
		osType = "date";
	    } else if (javaType.equals(SimpleEntry.class)) {
		osType = "geo_point";
	    } else if (javaType.equals(BBOX4326.class)) {
		osType = "geo_shape";
	    } else if (javaType.equals(BBOX3857.class)) {
		osType = "geo_shape";
	    } else if (javaType.equals(Polygon4326.class)) {
		osType = "geo_shape";
	    } else if (javaType.equals(Boolean.class)) {
		osType = "boolean";
	    } else if (javaType.equals(Double.class)) {
		osType = "double";
	    } else if (javaType.equals(Float.class)) {
		osType = "float";
	    } else if (javaType.equals(BigDecimal.class)) {
		osType = "double";
	    } else if (javaType.equals(Integer.class)) {
		osType = "integer";
	    } else if (javaType.equals(Long.class)) {
		osType = "long";
	    } else if (javaType.equals(Duration.class)) {
	    } else if (javaType.equals(String.class)) {
	    } else {
		throw new RuntimeException("Unsupported Java type: " + javaType);
	    }
	    if (osType != null) {
		typeObject.put("type", osType);
		String javaName = field.getName();
		properties.put(javaName, typeObject);
	    }
	}
	ret.put("properties", properties);
	return ret;
    }

    private <T> List<T> parseRecords(SearchHit[] hits, Class<T> clazz) throws Exception {
	List<T> ret = new ArrayList<>();
	for (SearchHit hit : hits) {
	    T record = parseRecord(hit, clazz);

	    ret.add(record);
	}
	return ret;

    }

    private <T> T parseRecord(SearchHit hit, Class<T> clazz) throws Exception {
	Map<String, Object> map = hit.getSourceAsMap();
	Field[] allFields = clazz.getDeclaredFields();
	T record = clazz.newInstance();
	for (Field field : allFields) {
	    try {
		field.setAccessible(true);
		Class<?> javaType = field.getType();
		String javaName = field.getName();
		if (!map.containsKey(javaName)) {
		    continue;
		}
		Object value = map.get(javaName);
		if (value == null) {
		    continue;
		}
		if (javaType.equals(Date.class)) {
		    long l = Long.parseLong(value.toString());
		    Date date = new Date(l);
		    field.set(record, date);
		} else if (javaType.equals(SimpleEntry.class)) {
		    String wkt = value.toString();
		    WKT w = new WKT(wkt);
		    if (w.getObjectName().equals("POINT")) {
			SimpleEntry<BigDecimal, BigDecimal> latLon = new SimpleEntry<>(w.getNumbers().get(1), w.getNumbers().get(0));
			field.set(record, latLon);
		    } else {
			throw new RuntimeException("ERROR MAPPING OS");
		    }
		} else if (javaType.equals(BBOX4326.class)) {
		    String wkt = value.toString();
		    BBOX4326 bb = new BBOX4326(wkt);
		    field.set(record, bb);
		} else if (javaType.equals(BBOX3857.class)) {
		    String wkt = value.toString();
		    BBOX3857 bb = new BBOX3857(wkt);
		    field.set(record, bb);
		} else if (javaType.equals(Polygon4326.class)) {
		    String wkt = value.toString();
		    Polygon4326 bb = new Polygon4326(wkt);
		    field.set(record, bb);
		} else if (javaType.equals(BigDecimal.class)) {
		    BigDecimal decimal = new BigDecimal(value.toString());
		    field.set(record, decimal);
		} else if (javaType.equals(Duration.class)) {
		    Duration duration = ISO8601DateTimeUtils.getDuration(value.toString());
		    field.set(record, duration);
		} else if (javaType.equals(Integer.class)) {
		    field.set(record, Integer.parseInt(value.toString()));
		} else if (javaType.equals(Long.class)) {
		    field.set(record, Long.parseLong(value.toString()));
		} else {
		    field.set(record, value.toString());
		}
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error("[DATA-CACHE] deserialization error for data record field {}", field.getName());
	    }
	}
	return record;
    }

    public JSONObject getJSONObject(Object object) {
	JSONObject ret = new JSONObject();
	Field[] allFields = object.getClass().getDeclaredFields();
	for (Field field : allFields) {
	    field.setAccessible(true);
	    Class<?> javaType = field.getType();
	    String javaName = field.getName();
	    Object value;
	    try {
		value = field.get(object);
	    } catch (IllegalArgumentException | IllegalAccessException e) {
		e.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).error("Unexpected error");
		throw new RuntimeException("Unexpected error");
	    }
	    if (value == null) {
		continue;
	    }
	    if (javaType.equals(Date.class)) {
		Date date = (Date) value;
		ret.put(javaName, date.getTime());
	    } else if (javaType.equals(SimpleEntry.class)) {
		SimpleEntry date = (SimpleEntry) value;
		Object lat = date.getKey();
		Object lon = date.getKey();
		ret.put(javaName, "POINT (" + lon.toString() + " " + lat.toString() + ")");
	    } else if (javaType.equals(BBOX4326.class)) {
		BBOX4326 bbox = (BBOX4326) value;
		ret.put(javaName, bbox.getWkt());
	    } else if (javaType.equals(BBOX3857.class)) {
		BBOX3857 bbox = (BBOX3857) value;
		ret.put(javaName, bbox.getWkt());
	    } else if (javaType.equals(Polygon4326.class)) {
		Polygon4326 polygon = (Polygon4326) value;
		ret.put(javaName, polygon.getWkt());
	    } else if (javaType.equals(BigDecimal.class)) {
		BigDecimal decimal = (BigDecimal) value;
		ret.put(javaName, decimal.toString());
	    } else if (javaType.equals(Duration.class)) {
		Duration duration = (Duration) value;
		ret.put(javaName, duration.toString());
	    } else {
		ret.put(javaName, value.toString());
	    }

	}
	return ret;
    }

    public void close() throws Exception {
	if (futureTask != null) {
	    futureTask.cancel(true);
	}
	client.close();
    }

    public boolean checkIndexExistence(String index) throws IOException {
	org.opensearch.client.indices.GetIndexRequest request = new org.opensearch.client.indices.GetIndexRequest(index);
	RequestOptions options = RequestOptions.DEFAULT;
	return client.indices().exists(request, options);
    }

    private List<Object> buffer = new ArrayList<>();
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> futureTask = null;
    private boolean flushInProgress = false;
    private Runnable writeTask = new Runnable() {

	@Override
	public void run() {
	    if (Thread.interrupted()) {
		return;
	    }

	    List<Object> tmp = new ArrayList<>();

	    synchronized (buffer) {

		if (buffer.size() < maxBulkSize) {
		    tmp.addAll(buffer);
		    buffer.clear();
		} else {
		    List<Object> staged = buffer.subList(0, maxBulkSize);
		    List<Object> posponed = buffer.subList(maxBulkSize, buffer.size());
		    tmp.addAll(staged);
		    buffer = posponed;
		}
	    }

	    if (!tmp.isEmpty()) {
		flushInProgress = true;
		BulkRequest request = new BulkRequest();
		for (Object record : tmp) {
		    if (record instanceof DataRecord) {
			DataRecord dataRecord = (DataRecord) record;
			JSONObject json = getJSONObject(dataRecord);
			String id = "ID" + UUID.randomUUID().toString();
			String dataId = dataRecord.getDataIdentifier();
			if (dataId != null) {
			    id = dataId + "-" + dataRecord.getDate().getTime();
			}
			request.add(new IndexRequest(DataCacheIndex.VALUES.getIndex(databaseName)).id(id).source(json.toString(),
				XContentType.JSON));
		    } else if (record instanceof StatisticsRecord) {
			StatisticsRecord statisticsRecord = (StatisticsRecord) record;
			JSONObject json = getJSONObject(statisticsRecord);
			String id = "ID" + UUID.randomUUID().toString();
			String dataId = statisticsRecord.getDataIdentifier();
			if (dataId != null) {
			    id = dataId + "-STAT-" + statisticsRecord.getDate().getTime();
			}
			request.add(new IndexRequest(DataCacheIndex.STATISTICS.getIndex(databaseName)).id(id).source(json.toString(),
				XContentType.JSON));

		    } else if (record instanceof StationRecord) {
			StationRecord stationRecord = (StationRecord) record;
			JSONObject json = getJSONObject(stationRecord);
			String id = "ID" + UUID.randomUUID().toString();
			String metadataId = stationRecord.getMetadataIdentifier();
			if (metadataId != null) {
			    id = metadataId + "-STATION";
			}
			request.add(new IndexRequest(DataCacheIndex.STATIONS.getIndex(databaseName)).id(id).source(json.toString(),
				XContentType.JSON));
		    }

		}
		flush(request);
		flushInProgress = false;
	    }

	}
    };

    @Override
    public void write(DataRecord record) {
	synchronized (buffer) {
	    buffer.add(record);
	    if (buffer.size() > maxBulkSize && buffer.size() % 100 == 0) {
		GSLoggerFactory.getLogger(getClass()).warn("Quite a lot data records are being ingested at the same time {}",
			buffer.size());
	    }
	}
    }

    @Override
    public void writeStatistics(StatisticsRecord record) throws Exception {
	synchronized (buffer) {
	    buffer.add(record);
	    if (buffer.size() > maxBulkSize && buffer.size() % 100 == 0) {
		GSLoggerFactory.getLogger(getClass()).warn("Quite a lot statistics records are being ingested at the same time {}",
			buffer.size());
	    }
	}
    }

    @Override
    public void write(List<DataRecord> records) {
	synchronized (this.buffer) {
	    this.buffer.addAll(records);
	    if (this.buffer.size() > maxBulkSize) {
		GSLoggerFactory.getLogger(getClass()).warn("Quite a lot data records are being ingested at the same time {}",
			records.size());
	    }
	}
    }

    public boolean flush(BulkRequest request) {
	List<String> failedItems = new ArrayList<>();
	int size = request.numberOfActions();
	long start = System.currentTimeMillis();
	GSLoggerFactory.getLogger(getClass()).info("Writing to OS {} records", size);
	HashMap<String, String> items = new HashMap<>();

	Boolean success = null;
	IOException unexpectedError = null;
	String failureMessage = null;
	int maxTry = 10;
	BulkResponse indexResponse = null;
	while (indexResponse == null && maxTry-- > 0) {
	    try {
		GSLoggerFactory.getLogger(getClass()).info("Bulk request started", size);
		indexResponse = client.bulk(request, RequestOptions.DEFAULT);
		GSLoggerFactory.getLogger(getClass()).info("Bulk request completed", size);
	    } catch (IOException e) {
		unexpectedError = e;
		GSLoggerFactory.getLogger(getClass()).error("Opensearch unexpected errors (sleep a bit before retry): {} try: {}",
			e.getMessage(), maxTry);
		try {
		    Thread.sleep(120000);
		} catch (InterruptedException e1) {
		    e1.printStackTrace();
		}
	    }
	}
	if (indexResponse == null) {
	    GSLoggerFactory.getLogger(getClass()).error("Opensearch unexpected error: {}", unexpectedError.getMessage());
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
		    String itemId = bulkItemResponse.getId();
		    String item = items.get(itemId);
		    failedItems.add(item);
		    GSLoggerFactory.getLogger(getClass()).error("Opensearch index failure: {} {} {}", reason, itemId, item);
		} else {
		    if (response.getResult() == DocWriteResponse.Result.CREATED) {
			// GSLoggerFactory.getLogger(getClass()).trace("Elasticsearch document created {} {}", index,
			// response.getId());
		    } else if (response.getResult() == DocWriteResponse.Result.UPDATED) {
			// GSLoggerFactory.getLogger(getClass()).trace("Elasticsearch document updated {} {}", index,
			// response.getId());
		    }
		}
	    }
	    if (indexResponse.hasFailures()) {
		String message = indexResponse.buildFailureMessage();
		GSLoggerFactory.getLogger(getClass()).error("Opensearch index with failures: {}", message);
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
	    GSLoggerFactory.getLogger(getClass()).error("Unable to index document(s) {}", failureMessage);
	} else {
	    long time = System.currentTimeMillis() - start;
	    double speed = size / (time / 1000.0);
	    GSLoggerFactory.getLogger(getClass()).info("Indexed {} records. Speed (dr/s): {} ", size, speed);
	}
	return success;

    }

    @Override
    public void configure(String key, String value) {

	switch (key) {
	case FLUSH_INTERVAL_MS:
	    long flushIntervalMs = Long.parseLong(value);
	    if (futureTask != null) {
		futureTask.cancel(true);
	    }
	    futureTask = scheduledExecutorService.scheduleAtFixedRate(writeTask, 0, flushIntervalMs, TimeUnit.MILLISECONDS);
	    break;
	case MAX_BULK_SIZE:
	    maxBulkSize = Integer.parseInt(value);
	    break;
	case CACHED_DAYS:
	    cachedDays = Integer.parseInt(value);
	    break;
	default:
	    break;
	}

    }

    @Override
    public void waitForFlush() {
	while (!buffer.isEmpty() || flushInProgress) {
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}

    }

    @Override
    public void deleteFromActiveStationsBefore(Date date, String sourceIdentifier) throws Exception {
	DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(DataCacheIndex.VALUES.getIndex(databaseName));
	BoolQueryBuilder query = QueryBuilders.boolQuery();
	if (date != null) {
	    String property = DATE_PROPERTY;
	    RangeQueryBuilder timeBuilder = QueryBuilders.rangeQuery(property).lt(date.getTime());
	    query.must(timeBuilder);
	}
	if (sourceIdentifier != null) {
	    query.must(QueryBuilders.termQuery(SOURCE_IDENTIFIER_PROPERTY + ".keyword", sourceIdentifier));
	}
	query.mustNot(QueryBuilders.termQuery(ACTIVE_PROPERTY + ".keyword", "false"));
	deleteByQueryRequest.setQuery(query);
	BulkByScrollResponse response = client.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
	long deleted = response.getDeleted();
	GSLoggerFactory.getLogger(getClass()).info("Deleted records: {}", deleted);

    }

    @Override
    public void deleteBefore(Date date, String sourceIdentifier, String dataIdentifier) throws Exception {
	GSLoggerFactory.getLogger(getClass()).info("Deleting records from source: {} data record: {}", sourceIdentifier, dataIdentifier);
	DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(DataCacheIndex.VALUES.getIndex(databaseName));
	BoolQueryBuilder query = QueryBuilders.boolQuery();
	if (date != null) {
	    String property = DATE_PROPERTY;
	    RangeQueryBuilder timeBuilder = QueryBuilders.rangeQuery(property).lt(date.getTime());
	    query.must(timeBuilder);
	}
	if (sourceIdentifier != null) {
	    query.must(QueryBuilders.termQuery(SOURCE_IDENTIFIER_PROPERTY + ".keyword", sourceIdentifier));
	}
	if (dataIdentifier != null) {
	    query.must(QueryBuilders.termQuery(DATA_IDENTIFIER_PROPERTY + ".keyword", dataIdentifier));
	}
	deleteByQueryRequest.setQuery(query);
	BulkByScrollResponse response = client.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
	long deleted = response.getDeleted();
	GSLoggerFactory.getLogger(getClass()).info("Deleted records: {}", deleted);

    }

    @Override
    public Long count() throws Exception {
	CountRequest countRequest = new CountRequest(DataCacheIndex.VALUES.getIndex(databaseName));
	RequestOptions options = RequestOptions.DEFAULT;
	CountResponse count = client.count(countRequest, options);
	return count.getCount();
    }

    @Override
    public Date getFirstDate(String dataIdentifier) throws Exception {
	SearchRequest searchRequest = new SearchRequest(DataCacheIndex.VALUES.getIndex(databaseName));
	SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
	BoolQueryBuilder query = QueryBuilders.boolQuery();
	query.must(QueryBuilders.termQuery(DATA_IDENTIFIER_PROPERTY + ".keyword", dataIdentifier));
	sourceBuilder.query(query);
	MinAggregationBuilder aggregation = AggregationBuilders.min("agg").field(DATE_PROPERTY);
	sourceBuilder.aggregation(aggregation);
	searchRequest.source(sourceBuilder);
	SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
	Aggregations aggregations = searchResponse.getAggregations();
	Aggregation aggregationResult = aggregations.asList().get(0);
	if (aggregationResult instanceof ParsedMin) {
	    ParsedMin max = (ParsedMin) aggregationResult;
	    Double d = max.getValue();
	    if (Double.isFinite(d)) {
		return new Date(d.longValue());
	    } else {
		return null;
	    }
	}
	return null;
    }

    @Override
    public Date getLastDate(String dataIdentifier) throws Exception {
	SearchRequest searchRequest = new SearchRequest(DataCacheIndex.VALUES.getIndex(databaseName));
	SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
	BoolQueryBuilder query = QueryBuilders.boolQuery();
	query.must(QueryBuilders.termQuery(DATA_IDENTIFIER_PROPERTY + ".keyword", dataIdentifier));
	sourceBuilder.query(query);
	MaxAggregationBuilder aggregation = AggregationBuilders.max("agg").field(DATE_PROPERTY);
	sourceBuilder.aggregation(aggregation);
	searchRequest.source(sourceBuilder);
	SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
	Aggregations aggregations = searchResponse.getAggregations();
	Aggregation aggregationResult = aggregations.asList().get(0);
	if (aggregationResult instanceof ParsedMax) {
	    ParsedMax max = (ParsedMax) aggregationResult;
	    Double d = max.getValue();
	    if (Double.isFinite(d)) {
		return new Date(d.longValue());
	    } else {
		return null;
	    }
	}
	return null;
    }

    @Override
    public List<DataRecord> getLastRecords(Integer maxRecords, String... dataIdentifiers) throws Exception {

	List<SimpleEntry<String, String>> propertyValues = new ArrayList<>();
	for (String dataIdentifier : dataIdentifiers) {
	    SimpleEntry<String, String> property = new SimpleEntry<String, String>(DATA_IDENTIFIER_PROPERTY, dataIdentifier);
	    propertyValues.add(property);
	}

	Response<DataRecord> ret = getRecordsWithProperties(maxRecords, null, null, false, null, propertyValues);
	return ret.getRecords();

    }

    @Override
    public Response<DataRecord> getRecordsWithProperties(Integer maxRecords, Date begin, Date end, boolean ascendOrder,
	    List<SimpleEntry<String, String>> necessaryProperties, List<SimpleEntry<String, String>> sufficientProperties)
	    throws Exception {

	SearchRequest searchRequest = new SearchRequest(DataCacheIndex.VALUES.getIndex(databaseName));
	SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
	BoolQueryBuilder query = QueryBuilders.boolQuery();

	if (necessaryProperties != null) {
	    addProperties(query, true, necessaryProperties.toArray(new SimpleEntry[] {}));
	}
	if (sufficientProperties != null) {
	    addProperties(query, false, sufficientProperties.toArray(new SimpleEntry[] {}));
	    query.minimumShouldMatch(1);
	}

	if (begin != null) {
	    String property = DATE_PROPERTY;
	    RangeQueryBuilder timeBuilder = QueryBuilders.rangeQuery(property).gte(begin.getTime());
	    query.must(timeBuilder);
	}
	if (end != null) {
	    String property = DATE_PROPERTY;
	    RangeQueryBuilder timeBuilder = QueryBuilders.rangeQuery(property).lte(end.getTime());
	    query.must(timeBuilder);
	}

	sourceBuilder.query(query);
	sourceBuilder.size(maxRecords);

	if (ascendOrder) {
	    sourceBuilder.sort(DATE_PROPERTY, SortOrder.ASC);
	} else {
	    sourceBuilder.sort(DATE_PROPERTY, SortOrder.DESC);
	}
	searchRequest.source(sourceBuilder);
	SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

	SearchHit[] hits = searchResponse.getHits().getHits();
	List<DataRecord> records = parseRecords(hits, DataRecord.class);

	long total = searchResponse.getHits().getTotalHits().value;
	Response<DataRecord> ret = new Response<>(records, 0, total, records.size() == total ? true : false);
	return ret;
    }

    @Override
    public List<DataRecord> getRecords(Date begin, Date end, String... dataIdentifiers) throws Exception {

	List<DataRecord> ret = new ArrayList<>();

	ResponseListener<DataRecord> listener = new ResponseListener<DataRecord>() {

	    boolean completed = false;

	    @Override
	    public boolean isCompleted() {
		return completed;
	    }

	    @Override
	    public void recordsReturned(Response<DataRecord> response) {
		ret.addAll(response.getRecords());
		if (response.isCompleted()) {
		    completed = true;
		}

	    }
	};
	getRecords(listener, begin, end, dataIdentifiers);

	while (!listener.isCompleted()) {
	    try {
		Thread.sleep(1000);
	    } catch (Exception e) {
		if (Thread.interrupted()) {
		    return ret;
		}
	    }
	}

	return ret;

    }

    @Override
    public void getLastRecords(ResponseListener listener, Integer maxRecords, String... dataIdentifiers) throws Exception {

	for (String dataIdentifier : dataIdentifiers) {

	    SearchRequest searchRequest = new SearchRequest(DataCacheIndex.VALUES.getIndex(databaseName));
	    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
	    BoolQueryBuilder query = QueryBuilders.boolQuery();
	    if (dataIdentifiers.length > 0) {
		query.must(QueryBuilders.termsQuery(DATA_IDENTIFIER_PROPERTY + ".keyword", dataIdentifier));
	    }
	    sourceBuilder.query(query);
	    sourceBuilder.size(maxRecords);
	    // String[] include = new String[] { DATA_IDENTIFIER_PROPERTY, DATE_PROPERTY, VALUE_PROPERTY };
	    // String[] exclude = new String[] {};
	    // sourceBuilder.fetchSource(include, exclude);
	    // sourceBuilder.sort(new FieldSortBuilder(DATA_IDENTIFIER_PROPERTY + ".keyword"));
	    sourceBuilder.sort(DATE_PROPERTY, SortOrder.DESC);
	    searchRequest.source(sourceBuilder);
	    SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

	    SearchHit[] hits = searchResponse.getHits().getHits();
	    List<DataRecord> records = parseRecords(hits, DataRecord.class);
	    long total = searchResponse.getHits().getTotalHits().value;
	    long i = 0;
	    boolean completed = records.size() == total;
	    Response response = new Response(records, i, total, completed);
	    response.setCompleted(true);
	    response.setTotal(hits.length);
	    listener.recordsReturned(response);
	}

    }

    @Override
    public void getRecords(ResponseListener listener, Date begin, Date end, String... dataIdentifiers) throws Exception {
	SearchRequest searchRequest = new SearchRequest(DataCacheIndex.VALUES.getIndex(databaseName));
	SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
	BoolQueryBuilder query = QueryBuilders.boolQuery();
	if (dataIdentifiers.length > 0) {
	    query.must(QueryBuilders.termsQuery(DATA_IDENTIFIER_PROPERTY + ".keyword", dataIdentifiers));
	}
	if (begin != null) {
	    String property = DATE_PROPERTY;
	    RangeQueryBuilder timeBuilder = QueryBuilders.rangeQuery(property).gte(begin.getTime());
	    query.must(timeBuilder);
	}
	if (end != null) {
	    String property = DATE_PROPERTY;
	    RangeQueryBuilder timeBuilder = QueryBuilders.rangeQuery(property).lte(end.getTime());
	    query.must(timeBuilder);
	}
	sourceBuilder.query(query);
	sourceBuilder.size(1000);
	// String[] include = new String[] { DATA_IDENTIFIER_PROPERTY, DATE_PROPERTY, VALUE_PROPERTY };
	// String[] exclude = new String[] {};
	// sourceBuilder.fetchSource(include, exclude);
	sourceBuilder.sort(new FieldSortBuilder(DATA_IDENTIFIER_PROPERTY + ".keyword"));
	sourceBuilder.sort(DATE_PROPERTY, SortOrder.DESC);
	searchRequest.scroll(TimeValue.timeValueMinutes(1L));
	searchRequest.source(sourceBuilder);
	SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

	String scrollId = searchResponse.getScrollId();
	SearchHit[] hits = searchResponse.getHits().getHits();
	List<DataRecord> records = parseRecords(hits, DataRecord.class);
	long total = searchResponse.getHits().getTotalHits().value;
	long i = 0;
	boolean completed = records.size() == total;
	Response response = new Response(records, i, total, completed);
	listener.recordsReturned(response);
	i += records.size();
	if (completed) {
	    return;
	}

	while (!completed) {
	    SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
	    scrollRequest.scroll(TimeValue.timeValueMinutes(1L));
	    SearchResponse searchScrollResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
	    scrollId = searchScrollResponse.getScrollId();
	    hits = searchScrollResponse.getHits().getHits();
	    records = parseRecords(hits, DataRecord.class);
	    if (records.isEmpty()) {
		completed = true;
	    }
	    response = new Response(records, i, total, completed);
	    listener.recordsReturned(response);
	    i += records.size();
	}

    }

    @Override
    public List<SimpleEntry<String, Date>> getExpectedAvailableRecords(String sourceIdentifier) throws Exception {
	SearchRequest searchRequest = new SearchRequest(DataCacheIndex.VALUES.getIndex(databaseName));
	SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
	BoolQueryBuilder query = QueryBuilders.boolQuery();
	if (sourceIdentifier != null) {
	    query.must(QueryBuilders.termQuery(SOURCE_IDENTIFIER_PROPERTY + ".keyword", sourceIdentifier));
	}
	RangeQueryBuilder timeBuilder = QueryBuilders.rangeQuery(NEXT_RECORD_EXPECTED_TIME_PROPERTY)
		.lt(new Date(new Date().getTime() + 20000).getTime());
	query.must(timeBuilder);

	sourceBuilder.query(query);
	sourceBuilder.size(10000);
	String[] include = new String[] { DATA_IDENTIFIER_PROPERTY, NEXT_RECORD_EXPECTED_TIME_PROPERTY };
	String[] exclude = new String[] {};
	sourceBuilder.fetchSource(include, exclude);
	sourceBuilder.sort(new FieldSortBuilder(NEXT_RECORD_EXPECTED_TIME_PROPERTY));
	searchRequest.source(sourceBuilder);
	SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

	SearchHit[] hits = searchResponse.getHits().getHits();
	List<DataRecord> records = parseRecords(hits, DataRecord.class);
	List<SimpleEntry<String, Date>> ret = new ArrayList<>();
	for (DataRecord dataRecord : records) {
	    ret.add(new SimpleEntry<>(dataRecord.getDataIdentifier(), dataRecord.getNextRecordExpectedTime()));
	}
	return ret;
    }

    @Override
    public SimpleEntry<String, Date> getNextExpectedRecord(String sourceIdentifier) throws Exception {
	SearchRequest searchRequest = new SearchRequest(DataCacheIndex.VALUES.getIndex(databaseName));
	SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
	BoolQueryBuilder query = QueryBuilders.boolQuery();
	if (sourceIdentifier != null) {
	    query.must(QueryBuilders.termQuery(SOURCE_IDENTIFIER_PROPERTY + ".keyword", sourceIdentifier));
	}
	RangeQueryBuilder timeBuilder = QueryBuilders.rangeQuery(NEXT_RECORD_EXPECTED_TIME_PROPERTY).gt(new Date().getTime());
	query.must(timeBuilder);

	sourceBuilder.query(query);
	sourceBuilder.size(1);
	String[] include = new String[] { DATA_IDENTIFIER_PROPERTY, NEXT_RECORD_EXPECTED_TIME_PROPERTY };
	String[] exclude = new String[] {};
	sourceBuilder.fetchSource(include, exclude);
	sourceBuilder.sort(new FieldSortBuilder(NEXT_RECORD_EXPECTED_TIME_PROPERTY));
	sourceBuilder.sort(NEXT_RECORD_EXPECTED_TIME_PROPERTY, SortOrder.ASC);

	searchRequest.source(sourceBuilder);
	SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

	SearchHit[] hits = searchResponse.getHits().getHits();
	List<DataRecord> records = parseRecords(hits, DataRecord.class);
	List<SimpleEntry<String, Date>> ret = new ArrayList<>();
	if (records.isEmpty()) {
	    return null;
	}
	DataRecord dataRecord = records.get(0);
	return new SimpleEntry<>(dataRecord.getDataIdentifier(), dataRecord.getNextRecordExpectedTime());
    }

    @Override
    public void deleteStations(String sourceIdentifier) throws Exception {
	deleteStations(sourceIdentifier, null);
    }

    @Override
    public void deleteStations(String sourceIdentifier, String theme) throws Exception {
	DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(DataCacheIndex.STATIONS.getIndex(databaseName));
	BoolQueryBuilder query = QueryBuilders.boolQuery();
	if (sourceIdentifier != null) {
	    query.must(QueryBuilders.termQuery(SOURCE_IDENTIFIER_PROPERTY + ".keyword", sourceIdentifier));
	}
	if (theme != null) {
	    query.must(QueryBuilders.termQuery(THEME_CATEGORY_PROPERTY + ".keyword", theme));
	}
	deleteByQueryRequest.setQuery(query);
	BulkByScrollResponse response = client.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
	long deleted = response.getDeleted();
	GSLoggerFactory.getLogger(getClass()).info("Deleted records: {}", deleted);

    }

    @Override
    public void deleteStation(String stationId) throws Exception {
	if (stationId==null) {
	    throw new RuntimeException("missing station id");
	}
	DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(DataCacheIndex.STATIONS.getIndex(databaseName));
	BoolQueryBuilder query = QueryBuilders.boolQuery();
	query.must(QueryBuilders.termQuery(PLATFORM_IDENTIFIER_PROPERTY+ ".keyword", stationId));
	deleteByQueryRequest.setQuery(query);
	BulkByScrollResponse response = client.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
	long deleted = response.getDeleted();
	GSLoggerFactory.getLogger(getClass()).info("Deleted records: {}", deleted);

    }

    @Override
    public List<StationRecord> getStationsWithProperties(BBOX bbox, Integer offset, Integer maxRecords, boolean allProperties,
	    SimpleEntry<String, String>... propertyValue) throws Exception {
	return getStationsWithProperties(bbox, offset, maxRecords, allProperties, null, propertyValue);
    }

    @Override
    public List<StationRecord> getStationsWithProperties(BBOX bbox, Integer offset, Integer maxRecords, boolean allProperties,
	    List<String> neededProperties, SimpleEntry<String, String>... propertyValues) throws Exception {

	SearchRequest searchRequest = new SearchRequest(DataCacheIndex.STATIONS.getIndex(databaseName));

	SearchSourceBuilder sourceBuilder = getStationsQuery(null, bbox, allProperties, neededProperties, propertyValues);

	if (offset != null) {
	    sourceBuilder.from(offset);
	} else {
	    sourceBuilder.from(0);
	}

	if (maxRecords != null) {
	    sourceBuilder.size(maxRecords);
	} else {
	    sourceBuilder.size(10000);
	}

	searchRequest.source(sourceBuilder);
	SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

	SearchHit[] hits = searchResponse.getHits().getHits();
	List<StationRecord> records = parseRecords(hits, StationRecord.class);
	return records;

    }

    @Override
    public void getStationsWithProperties(ResponseListener<StationRecord> listener, Date lastHarvesting, BBOX bbox, Integer maxRecords,
	    boolean allProperties, SimpleEntry<String, String>... propertyValues) throws Exception {
	SearchRequest searchRequest = new SearchRequest(DataCacheIndex.STATIONS.getIndex(databaseName));

	SearchSourceBuilder sourceBuilder = getStationsQuery(lastHarvesting, bbox, allProperties, propertyValues);

	if (maxRecords != null) {
	    sourceBuilder.size(maxRecords);
	} else {
	    sourceBuilder.size(1000);
	}
	searchRequest.scroll(TimeValue.timeValueMinutes(1L));

	searchRequest.source(sourceBuilder);
	SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

	String scrollId = searchResponse.getScrollId();
	SearchHit[] hits = searchResponse.getHits().getHits();
	List<StationRecord> records = parseRecords(hits, StationRecord.class);
	long total = searchResponse.getHits().getTotalHits().value;
	long i = 0;
	boolean completed = records.size() == total;
	Response<StationRecord> response = new Response<StationRecord>(records, i, total, completed);
	listener.recordsReturned(response);
	i += records.size();
	if (completed) {
	    return;
	}

	while (!completed) {
	    SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
	    scrollRequest.scroll(TimeValue.timeValueMinutes(1L));
	    SearchResponse searchScrollResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
	    scrollId = searchScrollResponse.getScrollId();
	    hits = searchScrollResponse.getHits().getHits();
	    records = parseRecords(hits, StationRecord.class);
	    if (records.isEmpty()) {
		completed = true;
	    }
	    response = new Response<StationRecord>(records, i, total, completed);
	    listener.recordsReturned(response);
	    i += records.size();
	}

    }

    private SearchSourceBuilder getStationsQuery(Date lastHarvesting, BBOX bbox, boolean allProperties,
	    SimpleEntry<String, String>... propertyValues) {
	return getStationsQuery(lastHarvesting, bbox, allProperties, null, propertyValues);
    }

    private SearchSourceBuilder getStationsQuery(Date lastHarvesting, BBOX bbox, boolean allProperties, List<String> neededProperties,
	    SimpleEntry<String, String>... propertyValues) {

	BoolQueryBuilder query = QueryBuilders.boolQuery();
	addProperties(query, allProperties, propertyValues);
	if (neededProperties != null && !neededProperties.isEmpty()) {
	    for (String neededProperty : neededProperties) {
		query.must(QueryBuilders.existsQuery(neededProperty));

	    }
	}
	addBBOX(query, bbox);

	if (lastHarvesting != null) {
	    query.must(QueryBuilders.rangeQuery("lastHarvesting").gt(ISO8601DateTimeUtils.getISO8601DateTime(lastHarvesting)));
	}

	SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
	if (!allProperties && propertyValues != null && propertyValues.length > 0) {
	    query.minimumShouldMatch(1);
	}
	sourceBuilder.query(query);
	return sourceBuilder;
    }

    private void addBBOX(BoolQueryBuilder query, BBOX bbox) {
	if (bbox == null) {
	    return;
	}
	BigDecimal searchMinx = bbox.getMinx();
	BigDecimal searchMaxx = bbox.getMaxx();
	BigDecimal searchMiny = bbox.getMiny();
	BigDecimal searchMaxy = bbox.getMaxy();

	switch (bbox.getCrs()) {
	case "EPSG:4326":
	case "CRS:84":
	    query.mustNot(QueryBuilders.rangeQuery(WEST_PROPERTY).gt(searchMaxx));
	    query.mustNot(QueryBuilders.rangeQuery(EAST_PROPERTY).lt(searchMinx));
	    query.mustNot(QueryBuilders.rangeQuery(SOUTH_PROPERTY).gt(searchMaxy));
	    query.mustNot(QueryBuilders.rangeQuery(NORTH_PROPERTY).lt(searchMiny));
	    break;

	case "EPSG:3857":
	default:
	    query.mustNot(QueryBuilders.rangeQuery(MINX3857_PROPERTY).gt(searchMaxx));
	    query.mustNot(QueryBuilders.rangeQuery(MAXX3857_PROPERTY).lt(searchMinx));
	    query.mustNot(QueryBuilders.rangeQuery(MINY3857_PROPERTY).gt(searchMaxy));
	    query.mustNot(QueryBuilders.rangeQuery(MAXY3857_PROPERTY).lt(searchMiny));

	    break;
	}

    }

    /**
     * @param bbox
     * @param maxRecords
     * @param allProperties or at least one property
     * @param propertyValue
     * @return
     * @throws Exception
     */
    public StationsStatistics getStationStatisticsWithProperties(BBOX bbox, boolean allProperties,
	    SimpleEntry<String, String>... propertyValues) throws Exception {

	BoolQueryBuilder query = getQuery(bbox, allProperties, propertyValues);

	SimpleEntry<Long, StationRecord> stationSouth = getStation(query, SOUTH_PROPERTY, SortOrder.ASC);
	SimpleEntry<Long, StationRecord> stationNorth = getStation(query, NORTH_PROPERTY, SortOrder.DESC);
	SimpleEntry<Long, StationRecord> stationEast = getStation(query, EAST_PROPERTY, SortOrder.DESC);
	SimpleEntry<Long, StationRecord> stationWest = getStation(query, WEST_PROPERTY, SortOrder.ASC);

	if (stationSouth.getKey().equals(0l)) {
	    return new StationsStatistics(null, null, 0);
	}

	BBOX3857 bbox3857 = new BBOX3857(stationWest.getValue().getBbox3857().getMinx(), //
		stationSouth.getValue().getBbox3857().getMiny(), //
		stationEast.getValue().getBbox3857().getMaxx(), //
		stationNorth.getValue().getBbox3857().getMaxy() //
	);
	BBOX4326 bbox4326 = new BBOX4326(stationSouth.getValue().getBbox4326().getSouth(), //
		stationNorth.getValue().getBbox4326().getNorth(), //
		stationWest.getValue().getBbox4326().getWest(), //
		stationEast.getValue().getBbox4326().getEast()//
	);
	StationsStatistics ret = new StationsStatistics(bbox3857, bbox4326, stationSouth.getKey());
	return ret;

    }

    private SimpleEntry<Long, StationRecord> getStation(BoolQueryBuilder query, String sortProperty, SortOrder order) throws Exception {
	SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
	sourceBuilder.query(query);
	sourceBuilder.size(1);
	sourceBuilder.sort(sortProperty, order);
	SearchRequest searchRequest = new SearchRequest(DataCacheIndex.STATIONS.getIndex(databaseName));
	searchRequest.source(sourceBuilder);
	SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
	SearchHit[] hits = searchResponse.getHits().getHits();
	List<StationRecord> records = parseRecords(hits, StationRecord.class);

	long total = searchResponse.getHits().getTotalHits().value;
	if (records.isEmpty()) {
	    return new SimpleEntry<>(0l, null);
	}
	return new SimpleEntry<>(total, records.get(0));

    }

    private BoolQueryBuilder getQuery(BBOX bbox, boolean allProperties, SimpleEntry<String, String>[] propertyValues) {
	BoolQueryBuilder query = QueryBuilders.boolQuery();
	addProperties(query, allProperties, propertyValues);
	addBBOX(query, bbox);
	return query;

    }

    private void addProperties(BoolQueryBuilder query, boolean allProperties, SimpleEntry<String, String>... propertyValues) {
	if (propertyValues != null) {
	    for (SimpleEntry<String, String> propertyValue : propertyValues) {
		if (allProperties) {
		    query.must(QueryBuilders.termsQuery(propertyValue.getKey() + ".keyword", propertyValue.getValue()));
		} else {
		    query.should(QueryBuilders.termsQuery(propertyValue.getKey() + ".keyword", propertyValue.getValue()));
		}
	    }
	}

    }

    @Override
    public void writeStation(StationRecord record) throws Exception {
	synchronized (buffer) {
	    buffer.add(record);
	    if (buffer.size() > maxBulkSize && buffer.size() % 100 == 0) {
		GSLoggerFactory.getLogger(getClass()).warn("Quite a lot records are being ingested at the same time {}", buffer.size());
	    }
	}

    }

    @Override
    protected void clearStations() throws Exception {

	DeleteIndexRequest dir = new DeleteIndexRequest(DataCacheIndex.STATIONS.getIndex(databaseName));
	// delete also the index
	client.indices().delete(dir, RequestOptions.DEFAULT);
	indexInitialization();

    }

}
