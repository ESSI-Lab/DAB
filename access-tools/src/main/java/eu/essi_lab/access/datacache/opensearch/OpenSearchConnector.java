package eu.essi_lab.access.datacache.opensearch;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.xml.datatype.Duration;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.Conflicts;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.aggregations.Aggregation;
import org.opensearch.client.opensearch._types.aggregations.CompositeAggregate;
import org.opensearch.client.opensearch._types.aggregations.CompositeAggregationSource;
import org.opensearch.client.opensearch._types.aggregations.CompositeBucket;
import org.opensearch.client.opensearch._types.aggregations.MaxAggregate;
import org.opensearch.client.opensearch._types.aggregations.StringTermsBucket;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.Query.Builder;
import org.opensearch.client.opensearch.core.DeleteByQueryResponse;
import org.opensearch.client.opensearch.core.ScrollRequest;
import org.opensearch.client.opensearch.core.ScrollResponse;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.tasks.GetTasksResponse;
import org.opensearch.client.opensearch.tasks.Status;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.RangeQueryBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.essi_lab.access.datacache.BBOX;
import eu.essi_lab.access.datacache.BBOX3857;
import eu.essi_lab.access.datacache.BBOX4326;
import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory.DataConnectorType;
import eu.essi_lab.access.datacache.DataRecord;
import eu.essi_lab.access.datacache.LatitudeLongitude;
import eu.essi_lab.access.datacache.Polygon4326;
import eu.essi_lab.access.datacache.Response;
import eu.essi_lab.access.datacache.SeriesCacheInfo;
import eu.essi_lab.access.datacache.SeriesStatus;
import eu.essi_lab.access.datacache.SeriesStatusPage;
import eu.essi_lab.access.datacache.SeriesStatusRecord;
import eu.essi_lab.access.datacache.ResponseListener;
import eu.essi_lab.access.datacache.PropertyCacheStats;
import eu.essi_lab.access.datacache.SourceCacheStats;
import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.access.datacache.StationsStatistics;
import eu.essi_lab.access.datacache.StatisticsRecord;
import eu.essi_lab.access.datacache.WKT;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.JsonFileBuffer;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;

/**
 * @author boldrini
 */
public class OpenSearchConnector extends DataCacheConnector {

    /**
     * Precision threshold of the dataset counts of the cache statistics (used by the data reports): the maximum supported
     * by OpenSearch, so that the counts are close to exact up to 40000 datasets per group (the default threshold is 3000)
     */
    private static final int REPORT_PRECISION_THRESHOLD = 40000;

    private static final String DATE_PROPERTY = "date";
    private static final String ACTIVE_PROPERTY = "active";
    private static final String DATA_IDENTIFIER_PROPERTY = "dataIdentifier";
    private static final String SOURCE_IDENTIFIER_PROPERTY = "sourceIdentifier";
    private static final String THEME_CATEGORY_PROPERTY = "themeCategory";
    private static final String PLATFORM_IDENTIFIER_PROPERTY = "platformIdentifier";
    private static final String NEXT_RECORD_EXPECTED_TIME_PROPERTY = "nextRecordExpectedTime";
    private static final int DATA_IDENTIFIERS_PAGE_SIZE = 1000;
    private static final int MAX_REPORT_CONCEPTS = 2000;

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

    public OpenSearchClient client = null;
    private String databaseName = null;

    public enum DataCacheIndex {
	VALUES("values", DataRecord.class), STATISTICS("statistics", StatisticsRecord.class), STATIONS("stations", StationRecord.class),
	SERIES_STATUS("series-status", SeriesStatusRecord.class);

	private String suffix;

	public String getSuffix() {
	    return suffix;
	}

	public void setSuffix(String suffix) {
	    this.suffix = suffix;
	}

	public Class<?> getRecordType() {
	    return clazz;
	}

	public void setRecordType(Class<?> clazz) {
	    this.clazz = clazz;
	}

	private Class<?> clazz;

	private DataCacheIndex(String suffix, Class<?> clazz) {
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

	dataRecordbuffer = new JsonFileBuffer<DataRecord>(DataRecord.class);
	statisticsRecordbuffer = new JsonFileBuffer<StatisticsRecord>(StatisticsRecord.class);
	stationRecordbuffer = new JsonFileBuffer<StationRecord>(StationRecord.class);

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
	    client.ping();
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
		    CreateIndexRequest.Builder cirBuilder = new CreateIndexRequest.Builder();
		    cirBuilder = cirBuilder.index(indexName);

		    // optional mapping
		    GSLoggerFactory.getLogger(getClass()).info("Configuring mapping of index {}", indexName);
		    JSONObject mapping = getOSMapping(index.getRecordType());
		    // Obtain the JsonpMapper from the client's transport
		    JsonpMapper mapper = client._transport().jsonpMapper();

		    // Create a JsonParser from the JSON string
		    JsonParser parser = mapper.jsonProvider().createParser(new StringReader(mapping.toString()));

		    // Deserialize the JSON into a TypeMapping object
		    TypeMapping map = TypeMapping._DESERIALIZER.deserialize(parser, mapper);

		    cirBuilder = cirBuilder.mappings(map);
		    GSLoggerFactory.getLogger(getClass()).info("Opensearch creating index {}", indexName);
		    client.indices().create(cirBuilder.build());
		    GSLoggerFactory.getLogger(getClass()).info("Opensearch creating index SUCCESS {} ", indexName);
		}
	    } catch (IOException e) {
		e.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).error("Opensearch creating index ERROR {}", indexName);
		throw new Exception("OpenSearch error creating index");
	    }

	}

    }

    protected OpenSearchClient createClient(URL uri, String username, String password) {

	HttpHost httpHost = null;
	try {
	    httpHost = new HttpHost(uri.getHost(), uri.getPort(), uri.toURI().getScheme());
	} catch (URISyntaxException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	RestClientBuilder builder = RestClient.builder(httpHost);

	if (username != null && !username.isEmpty() && //
		password != null && !password.isEmpty()) {

	    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();

	    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

	    builder
		    .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

	}

	RestClient restClient = builder.build();

	OpenSearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

	return new OpenSearchClient(transport);

    }

    public JSONObject getOSMapping(Class<?> clazz) {
	JSONObject ret = new JSONObject();
	JSONObject properties = new JSONObject();

	Field[] allFields = clazz.getDeclaredFields();
	for (Field field : allFields) {
	    JSONObject typeObject = new JSONObject();
	    Class<?> javaType = field.getType();
	    String osType = null;
	    String format = null;
	    if (javaType.equals(Date.class)) {
		osType = "date";
		format = "epoch_millis||strict_date_optional_time";
	    } else if (javaType.equals(LatitudeLongitude.class)) {
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
		if (format != null) {
		    typeObject.put("format", format);
		}
		String javaName = field.getName();
		properties.put(javaName, typeObject);
	    }
	}
	ret.put("properties", properties);
	return ret;
    }

    private <T> List<T> parseRecords(List<Hit<JsonData>> hits, Class<T> class1) throws Exception {
	List<T> ret = new ArrayList<>();
	for (Hit<JsonData> hit : hits) {
	    T record = parseRecord(hit, class1);

	    ret.add(record);
	}
	return ret;

    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    private <T> T parseRecord(Hit<JsonData> hit, Class<T> class1) throws Exception {
	Map<String, Object> map = objectMapper.convertValue(hit.source().to(Map.class), Map.class);

	Field[] allFields = class1.getDeclaredFields();
	T record = class1.getDeclaredConstructor().newInstance();
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
		    JsonValue jsonData = hit.source().toJson();
		    if (jsonData != null) {
			JSONObject hitJson = new JSONObject(jsonData.toString());
			Long l = hitJson.optLongObject(javaName, null);
			if (l != null) {
			    Date date = new Date(l);
			    field.set(record, date);
			}
		    }

		} else if (javaType.equals(LatitudeLongitude.class)) {
		    String wkt;

		    if (value instanceof Map<?, ?>) {
			Map<String, String> m = (Map<String, String>) value;
			wkt = m.get("string");
		    } else {
			wkt = value.toString();
		    }
		    WKT w = new WKT(wkt);
		    if (w.getObjectName().equals("POINT")) {
			LatitudeLongitude latLon = new LatitudeLongitude(w.getNumbers().get(1), w.getNumbers().get(0));
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
		    if (value instanceof Map) {
			Map<?, ?> newMap = (Map<?, ?>) value;
			Object stringValue = newMap.get("string");
			if (stringValue instanceof String) {
			    try {
				BigDecimal decimal = new BigDecimal((String) stringValue);
				field.set(record, decimal);
			    } catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid number format in map: " + stringValue, e);
			    }
			}
		    }

		    // BigDecimal decimal = new BigDecimal(value.toString());
		    // field.set(record, decimal);
		} else if (javaType.equals(Duration.class)) {
		    Duration duration = ISO8601DateTimeUtils.getDuration(value.toString());
		    field.set(record, duration);
		} else if (javaType.equals(Integer.class)) {
		    field.set(record, Integer.parseInt(value.toString()));
		} else if (javaType.equals(Long.class)) {
		    field.set(record, Long.parseLong(value.toString()));
		} else {
		    if (value instanceof Map) {
			Map<?, ?> newMap = (Map<?, ?>) value;
			Object stringValue = newMap.get("string");
			if (stringValue instanceof String) {
			    field.set(record, stringValue);
			}
		    } else {
			field.set(record, value.toString());
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
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
	    } else if (javaType.equals(LatitudeLongitude.class)) {
		LatitudeLongitude date = (LatitudeLongitude) value;
		BigDecimal lat = date.getLatitude();
		BigDecimal lon = date.getLongitude();
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

    /**
     * Stops the periodic flush and retention jobs and releases the client. A bulk request already in progress is
     * allowed to complete; records still in the buffers are NOT written, so callers should wait for
     * {@link #countRecordsInDataBuffer()} to reach 0 before closing.
     */
    public void close() throws Exception {
	if (futureTask != null) {
	    futureTask.cancel(false);
	}

	scheduledExecutorService.shutdown();
	if (!scheduledExecutorService.awaitTermination(2, TimeUnit.MINUTES)) {
	    GSLoggerFactory.getLogger(getClass()).warn("OS connector executor did not terminate in time, forcing shutdown");
	    scheduledExecutorService.shutdownNow();
	}

	if (client != null) {
	    client._transport().close();
	}
    }

    public boolean checkIndexExistence(String index) throws IOException {
	ExistsRequest.Builder erb = new ExistsRequest.Builder();
	return client.indices().exists(erb.index(index).build()).value();
    }

    private JsonFileBuffer<DataRecord> dataRecordbuffer;
    private JsonFileBuffer<StatisticsRecord> statisticsRecordbuffer;
    private JsonFileBuffer<StationRecord> stationRecordbuffer;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> futureTask = null;
    private boolean flushInProgress = false;

    /**
     * Max time of a single run of the write task, so that a continuously filled buffer does not keep the run going
     * forever (e.g. preventing a timely close)
     */
    private static final long MAX_WRITE_RUN_MS = 30 * 1000;
    private Runnable writeTask = new Runnable() {

	@Override
	public void run() {
	    try {
		if (Thread.interrupted()) {
		    GSLoggerFactory.getLogger(getClass()).error("Thread interrupted");
		    return;
		}

		OperationCreator dataCreator = new OperationCreator() {

		    @Override
		    public BulkOperation createOperation(Object object) {
			DataRecord dataRecord = (DataRecord) object;
			JSONObject json = getJSONObject(dataRecord);
			String id = "ID" + UUID.randomUUID().toString();
			String dataId = dataRecord.getDataIdentifier();
			if (dataId != null) {
			    id = dataId + "-" + dataRecord.getDate().getTime();
			}
			String finalId = id;
			BulkOperation.Builder bob = new BulkOperation.Builder();
			bob.index(idx -> idx.index(DataCacheIndex.VALUES.getIndex(databaseName)).id(finalId).document(json.toMap()));
			return bob.build();
		    }
		};

		OperationCreator statisticsCreator = new OperationCreator() {

		    @Override
		    public BulkOperation createOperation(Object object) {
			StatisticsRecord statisticsRecord = (StatisticsRecord) object;
			JSONObject json = getJSONObject(statisticsRecord);
			String id = "ID" + UUID.randomUUID().toString();
			String dataId = statisticsRecord.getDataIdentifier();
			if (dataId != null) {
			    id = dataId + "-STAT-" + statisticsRecord.getDate().getTime();
			}

			String finalId = id;

			BulkOperation.Builder bob = new BulkOperation.Builder();
			bob.index(idx -> idx.index(DataCacheIndex.STATISTICS.getIndex(databaseName)).id(finalId).document(json.toMap()));
			return bob.build();
		    }
		};

		OperationCreator stationCreator = new OperationCreator() {

		    @Override
		    public BulkOperation createOperation(Object object) {
			StationRecord stationRecord = (StationRecord) object;
			JSONObject json = getJSONObject(stationRecord);
			String id = "ID" + UUID.randomUUID().toString();
			String metadataId = stationRecord.getMetadataIdentifier();
			if (metadataId != null) {
			    id = metadataId + "-STATION";
			}

			String finalId = id;

			BulkOperation.Builder bob = new BulkOperation.Builder();
			bob.index(idx -> idx.index(DataCacheIndex.STATIONS.getIndex(databaseName)).id(finalId).document(json.toMap()));
			return bob.build();
		    }
		};

		// bulks are written until the buffers are empty, so that the throughput is not limited to one bulk per run.
		// the buffers are written in rounds, one bulk each, so that a busy buffer does not delay the others
		long start = System.currentTimeMillis();
		boolean written = true;
		while (written && System.currentTimeMillis() - start < MAX_WRITE_RUN_MS) {

		    if (Thread.currentThread().isInterrupted()) {
			GSLoggerFactory.getLogger(getClass()).error("Thread interrupted");
			return;
		    }

		    int data = writeRecords(dataRecordbuffer, dataCreator);
		    int statistics = writeRecords(statisticsRecordbuffer, statisticsCreator);
		    int stations = writeRecords(stationRecordbuffer, stationCreator);

		    if (data < 0 || statistics < 0 || stations < 0) {
			// retried at the next run
			return;
		    }

		    written = data + statistics + stations > 0;
		}

	    } catch (Exception e) {
		e.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }

	}

	/**
	 * Writes a bulk of at most {@link #maxBulkSize} records of the given buffer
	 *
	 * @return the number of records written, or -1 if the bulk failed
	 */
	private int writeRecords(JsonFileBuffer<?> dataRecordbuffer, OperationCreator operationCreator) throws IOException {
	    List<?> tmp = dataRecordbuffer.poll(maxBulkSize);

	    if (tmp.isEmpty()) {
		return 0;
	    }

	    flushInProgress = true;
	    org.opensearch.client.opensearch.core.BulkRequest.Builder request = new org.opensearch.client.opensearch.core.BulkRequest.Builder();
	    List<BulkOperation> operations = new ArrayList<>();
	    for (Object record : tmp) {
		BulkOperation operation = operationCreator.createOperation(record);
		operations.add(operation);
	    }
	    request = request.operations(operations);
	    boolean success = flush(request.build());
	    flushInProgress = false;
	    return success ? tmp.size() : -1;
	}
    };

    @Override
    public void write(List<DataRecord> records) throws IOException {

	for (DataRecord record : records) {
	    write(record);
	}

    }

    @Override
    public void write(DataRecord record) throws IOException {
	dataRecordbuffer.add(record);
    }

    @Override
    public void writeStatistics(StatisticsRecord record) throws Exception {

	statisticsRecordbuffer.add(record);

    }

    @Override
    public void writeStation(StationRecord record) throws Exception {
	stationRecordbuffer.add(record);

    }

    public boolean flush(org.opensearch.client.opensearch.core.BulkRequest request) {
	List<String> failedItems = new ArrayList<>();
	int size = request.operations().size();
	long start = System.currentTimeMillis();
	GSLoggerFactory.getLogger(getClass()).info("Writing to OS {} records", size);
	HashMap<String, String> items = new HashMap<>();

	Boolean success = null;
	IOException unexpectedError = null;
	String failureMessage = null;
	int maxTry = 10;
	org.opensearch.client.opensearch.core.BulkResponse indexResponse = null;
	while (indexResponse == null && maxTry-- > 0) {
	    try {
		GSLoggerFactory.getLogger(getClass()).info("Bulk request started", size);

		indexResponse = client.bulk(request);
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
	    boolean errorsEncountered = indexResponse.errors();

	    if (errorsEncountered) {
		success = false;
		indexResponse.items().forEach(item -> {
		    if (item.error() != null) {
			GSLoggerFactory.getLogger(getClass()).error("Opensearch index failure: {} {} {}", item.error().reason(), item.id(),
				item);
		    }
		});
	    } else {
		success = true;
	    }

	}
	if (!success) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to index document(s)");
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
    public void deleteFromActiveStationsBefore(Date date, String sourceIdentifier) throws Exception {
	org.opensearch.client.opensearch.core.DeleteByQueryRequest.Builder dbqrb = new org.opensearch.client.opensearch.core.DeleteByQueryRequest.Builder();
	dbqrb.index(DataCacheIndex.VALUES.getIndex(databaseName));
	Query.Builder queryBuilder = new Query.Builder();
	BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
	if (date != null) {
	    String property = DATE_PROPERTY;
	    RangeQueryBuilder timeBuilder = QueryBuilders.rangeQuery(property).lt(date.getTime());
	    boolQueryBuilder = boolQueryBuilder.must(t -> t.range(r -> r.field(property).lt(JsonData.of(date.getTime()))));
	}

	if (sourceIdentifier != null) {
	    boolQueryBuilder = boolQueryBuilder
		    .must(t -> t.term(r -> r.field(SOURCE_IDENTIFIER_PROPERTY + ".keyword").value(FieldValue.of(false))));
	}
	boolQueryBuilder = boolQueryBuilder.mustNot(t -> t.term(q -> q.field(ACTIVE_PROPERTY + ".keyword").value(FieldValue.of(false))));

	queryBuilder = (Builder) queryBuilder.bool(boolQueryBuilder.build());
	dbqrb.query(queryBuilder.build());

	DeleteByQueryResponse response = client.deleteByQuery(dbqrb.build());
	long deleted = response.deleted();
	GSLoggerFactory.getLogger(getClass()).info("Deleted records: {}", deleted);

    }

    @Override
    public long deleteBefore(Date date, String sourceIdentifier, String dataIdentifier) throws Exception {
	GSLoggerFactory.getLogger(getClass()).info("Deleting records from source: {} data record: {}", sourceIdentifier, dataIdentifier);
	org.opensearch.client.opensearch.core.DeleteByQueryRequest.Builder dqrb = new org.opensearch.client.opensearch.core.DeleteByQueryRequest.Builder();
	dqrb = dqrb.index(DataCacheIndex.VALUES.getIndex(databaseName));

	Query.Builder qb = new Query.Builder();

	BoolQuery.Builder bqb = new BoolQuery.Builder();

	if (date != null) {
	    String property = DATE_PROPERTY;
	    bqb = bqb.must(t -> t.range(r -> r.field(property).lt(JsonData.of(date.getTime()))));
	}
	if (sourceIdentifier != null) {
	    bqb = bqb.must(f -> f.term(q -> q.field(SOURCE_IDENTIFIER_PROPERTY + ".keyword").value(FieldValue.of(sourceIdentifier))));
	}
	if (dataIdentifier != null) {
	    bqb = bqb.must(q -> q.term(t -> t.field(DATA_IDENTIFIER_PROPERTY + ".keyword").value(FieldValue.of(dataIdentifier))));
	}

	qb = (Builder) qb.bool(bqb.build());
	dqrb = dqrb.query(qb.build());

	long deleted = deleteByQueryTask(dqrb);
	GSLoggerFactory.getLogger(getClass()).info("Deleted records: {}", deleted);

	return deleted;
    }

    @Override
    public long deleteStatistics(String sourceIdentifier) throws Exception {

	if (sourceIdentifier == null) {
	    throw new IllegalArgumentException("missing source identifier");
	}

	org.opensearch.client.opensearch.core.DeleteByQueryRequest.Builder dqrb = new org.opensearch.client.opensearch.core.DeleteByQueryRequest.Builder();
	dqrb.index(DataCacheIndex.STATISTICS.getIndex(databaseName));
	dqrb.query(q -> q.term(t -> t.field("sourceId.keyword").value(FieldValue.of(sourceIdentifier))));

	long deleted = deleteByQueryTask(dqrb);
	GSLoggerFactory.getLogger(getClass()).info("Deleted statistics of source {}: {}", sourceIdentifier, deleted);

	return deleted;
    }

    @Override
    public long deleteSeriesStatus(String sourceIdentifier) throws Exception {

	if (sourceIdentifier == null) {
	    throw new IllegalArgumentException("missing source identifier");
	}

	org.opensearch.client.opensearch.core.DeleteByQueryRequest.Builder dqrb = new org.opensearch.client.opensearch.core.DeleteByQueryRequest.Builder();
	dqrb.index(DataCacheIndex.SERIES_STATUS.getIndex(databaseName));
	dqrb.query(q -> q.term(t -> t.field(STATUS_SOURCE_FIELD).value(FieldValue.of(sourceIdentifier))));

	long deleted = deleteByQueryTask(dqrb);
	GSLoggerFactory.getLogger(getClass()).info("Deleted series statuses of source {}: {}", sourceIdentifier, deleted);

	return deleted;
    }

    /**
     * Executes the given delete by query as a background task, waiting for its completion. The deletion of many records
     * can take longer than the client read timeout and the cluster cancels the deletion when the client disconnects, so
     * the deletion is started as a background task and then polled
     *
     * @param dqrb
     * @return the number of deleted records
     * @throws Exception
     */
    private long deleteByQueryTask(org.opensearch.client.opensearch.core.DeleteByQueryRequest.Builder dqrb) throws Exception {

	dqrb.waitForCompletion(false).conflicts(Conflicts.Proceed);

	DeleteByQueryResponse response = client.deleteByQuery(dqrb.build());
	String taskId = response.task();
	GSLoggerFactory.getLogger(getClass()).info("Delete task started: {}", taskId);

	return waitForDeleteTask(taskId);
    }

    /**
     * Waits for the completion of the given delete by query task, logging its progress
     *
     * @param taskId
     * @return the number of deleted records
     * @throws Exception
     */
    private long waitForDeleteTask(String taskId) throws Exception {

	while (true) {

	    GetTasksResponse taskResponse = client.tasks().get(g -> g.taskId(taskId));

	    if (taskResponse.completed()) {

		if (taskResponse.error() != null) {
		    throw new IOException("Delete task " + taskId + " failed: " + taskResponse.error().reason());
		}

		Status status = taskResponse.response() != null ? taskResponse.response() : taskResponse.task().status();

		return status != null ? status.deleted() : 0;
	    }

	    Status status = taskResponse.task().status();
	    if (status != null) {
		GSLoggerFactory.getLogger(getClass()).info("Delete task {}: deleted {}/{}", taskId, status.deleted(), status.total());
	    }

	    Thread.sleep(10000);
	}
    }

    @Override
    public Long count() throws Exception {
	org.opensearch.client.opensearch.core.CountResponse count = client
		.count(c -> c.index(DataCacheIndex.VALUES.getIndex(databaseName)));
	return count.count();
    }

    @Override
    public Map<String, SourceCacheStats> getCacheStatsPerSource(List<String> selectedSourceIds) throws IOException {
	SearchResponse<Void> response = client.search(s -> s.index(DataCacheIndex.VALUES.getIndex(databaseName)).size(0)
		.query(q -> q.terms(t -> t.field("sourceIdentifier.keyword")
			.terms(tq -> tq.value(selectedSourceIds.stream().map(FieldValue::of).toList()))))
		.aggregations("sources",
			a -> a.terms(t -> t.field("sourceIdentifier.keyword").size(selectedSourceIds.size()))
				.aggregations("unique_datasets", sa -> sa.cardinality(ca -> ca.field("dataIdentifier.keyword").precisionThreshold(REPORT_PRECISION_THRESHOLD)))
				.aggregations("record_count", sa -> sa.valueCount(vc -> vc.field("sourceIdentifier.keyword")))
				.aggregations("oldest_insert", sa -> sa.min(m -> m.field("timestamp")))
				.aggregations("newest_insert", sa -> sa.max(m -> m.field("timestamp")))
				.aggregations("avg_insert", sa -> sa.avg(a2 -> a2.field("timestamp")))),
		Void.class);

	Map<String, SourceCacheStats> statsMap = new HashMap<>();
	Aggregate sourcesAgg = response.aggregations().get("sources");

	if (sourcesAgg.isSterms()) {
	    for (StringTermsBucket bucket : sourcesAgg.sterms().buckets().array()) {
		String sourceId = bucket.key();

		Long datasetCount = bucket.aggregations().get("unique_datasets").cardinality().value();
		long recordCount = (long) bucket.aggregations().get("record_count").valueCount().value();
		double oldest = bucket.aggregations().get("oldest_insert").min().value();
		double newest = bucket.aggregations().get("newest_insert").max().value();
		double avg = bucket.aggregations().get("avg_insert").avg().value();

		SourceCacheStats stats = new SourceCacheStats();
		stats.setSourceId(sourceId);
		stats.setUniqueDatasetCount(datasetCount);
		stats.setRecordCount(recordCount);
		stats.setOldestInsert(new Date((long) oldest));
		stats.setNewestInsert(new Date((long) newest));
		stats.setAverageAgeHours(java.time.Duration.between(Instant.ofEpochMilli((long) avg), Instant.now()).toHours());

		statsMap.put(sourceId, stats);
	    }
	}

	return statsMap;
    }

    @Override
    public List<PropertyCacheStats> getCacheStatsPerObservedProperty(String sourceId) throws IOException {
	SearchResponse<Void> response = client.search(s -> s.index(DataCacheIndex.VALUES.getIndex(databaseName)).size(0)
		.query(q -> q.term(t -> t.field(SOURCE_IDENTIFIER_PROPERTY + ".keyword").value(FieldValue.of(sourceId))))
		.aggregations("uris",
			a -> a.terms(t -> t.field("observedPropertyURI.keyword").size(1000).missing(FieldValue.of("")))
				.aggregations("names", na -> na
					.terms(t -> t.field("observedProperty.keyword").size(1000).missing(FieldValue.of("")))
					.aggregations("unique_datasets",
						sa -> sa.cardinality(ca -> ca.field(DATA_IDENTIFIER_PROPERTY + ".keyword").precisionThreshold(REPORT_PRECISION_THRESHOLD)))
					.aggregations("newest_observation", sa -> sa.max(m -> m.field(DATE_PROPERTY)))
					.aggregations("newest_insert", sa -> sa.max(m -> m.field("timestamp"))))),
		Void.class);

	List<PropertyCacheStats> ret = new ArrayList<>();
	Aggregate urisAgg = response.aggregations().get("uris");

	if (urisAgg.isSterms()) {
	    for (StringTermsBucket uriBucket : urisAgg.sterms().buckets().array()) {
		Aggregate namesAgg = uriBucket.aggregations().get("names");
		if (!namesAgg.isSterms()) {
		    continue;
		}
		for (StringTermsBucket bucket : namesAgg.sterms().buckets().array()) {
		    double newestObservation = bucket.aggregations().get("newest_observation").max().value();
		    double newestInsert = bucket.aggregations().get("newest_insert").max().value();

		    PropertyCacheStats stats = new PropertyCacheStats();
		    stats.setObservedPropertyURI(uriBucket.key());
		    stats.setObservedProperty(bucket.key());
		    stats.setUniqueDatasetCount(bucket.aggregations().get("unique_datasets").cardinality().value());
		    stats.setRecordCount(bucket.docCount());
		    if (Double.isFinite(newestObservation)) {
			stats.setNewestObservation(new Date((long) newestObservation));
		    }
		    if (Double.isFinite(newestInsert)) {
			stats.setNewestInsert(new Date((long) newestInsert));
		    }

		    ret.add(stats);
		}
	    }
	}

	return ret;
    }

    @Override
    public Date getFirstDate(String dataIdentifier) throws Exception {
	org.opensearch.client.opensearch.core.SearchRequest.Builder srb = new org.opensearch.client.opensearch.core.SearchRequest.Builder();
	srb = srb.index(DataCacheIndex.VALUES.getIndex(databaseName));

	BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
	boolQueryBuilder.must(q -> q.term(t -> t.field(DATA_IDENTIFIER_PROPERTY + ".keyword").value(FieldValue.of(dataIdentifier))));

	srb.aggregations("agg", a -> a.min(m -> m.field(DATE_PROPERTY)));

	Query.Builder qb = new Query.Builder();

	qb.bool(boolQueryBuilder.build());
	srb.query(qb.build());
	org.opensearch.client.opensearch.core.SearchResponse<JsonData> searchResponse = client.search(srb.build(), JsonData.class);
	Map<String, Aggregate> aggregations = searchResponse.aggregations();
	Aggregate aggregationResult = aggregations.values().iterator().next();
	if (aggregationResult.isMin()) {
	    Double d = aggregationResult.min().value();
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
	org.opensearch.client.opensearch.core.SearchRequest.Builder srb = new org.opensearch.client.opensearch.core.SearchRequest.Builder();
	srb = srb.index(DataCacheIndex.VALUES.getIndex(databaseName));
	BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

	boolQueryBuilder = boolQueryBuilder
		.must(t -> t.term(q -> q.field(DATA_IDENTIFIER_PROPERTY + ".keyword").value(FieldValue.of(dataIdentifier))));

	srb.aggregations("agg", a -> a.max(m -> m.field(DATE_PROPERTY)));

	Query.Builder qb = new Query.Builder();

	qb.bool(boolQueryBuilder.build());
	srb.query(qb.build());

	org.opensearch.client.opensearch.core.SearchResponse<JsonData> searchResponse = client.search(srb.build(), JsonData.class);
	Map<String, Aggregate> aggregations = searchResponse.aggregations();
	Aggregate aggregationResult = aggregations.values().iterator().next();
	if (aggregationResult.isMax()) {
	    MaxAggregate max = aggregationResult.max();
	    Double d = max.value();
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

	BoolQuery.Builder query = new BoolQuery.Builder();

	if (necessaryProperties != null) {
	    addProperties(query, true, necessaryProperties.toArray(new SimpleEntry[] {}));
	}
	if (sufficientProperties != null) {
	    addProperties(query, false, sufficientProperties.toArray(new SimpleEntry[] {}));
	    query.minimumShouldMatch("1");
	}

	if (begin != null) {
	    String property = DATE_PROPERTY;
	    query.must(t -> t.range(f -> f.field(property).gte(JsonData.of(begin.getTime()))));
	}
	if (end != null) {
	    String property = DATE_PROPERTY;

	    query.must(f -> f.range(q -> q.field(property).lte(JsonData.of(end.getTime()))));
	}
	org.opensearch.client.opensearch.core.SearchRequest.Builder srb = new org.opensearch.client.opensearch.core.SearchRequest.Builder();
	srb.index(DataCacheIndex.VALUES.getIndex(databaseName));
	BoolQuery qq = query.build();
	srb.query(q -> q.bool(qq));
	srb.size(maxRecords);

	if (ascendOrder) {
	    srb.sort(s -> s.field(f -> f.field(DATE_PROPERTY).order(org.opensearch.client.opensearch._types.SortOrder.Asc)));
	} else {
	    srb.sort(s -> s.field(f -> f.field(DATE_PROPERTY).order(org.opensearch.client.opensearch._types.SortOrder.Desc)));
	}

	org.opensearch.client.opensearch.core.SearchResponse<JsonData> searchResponse = client.search(srb.build(), JsonData.class);

	List<Hit<JsonData>> hits = searchResponse.hits().hits();
	List<DataRecord> records = parseRecords(hits, DataRecord.class);

	long total = searchResponse.hits().total().value();
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
	    org.opensearch.client.opensearch.core.SearchRequest.Builder srb = new org.opensearch.client.opensearch.core.SearchRequest.Builder();
	    srb = srb.index(DataCacheIndex.VALUES.getIndex(databaseName));
	    Query.Builder qb = new Query.Builder();
	    BoolQuery.Builder qbq = new BoolQuery.Builder();
	    qb = (Builder) qb.bool(qbq.build());
	    if (dataIdentifiers.length > 0) {
		qbq = qbq.must(f -> f.term(q -> q.field(DATA_IDENTIFIER_PROPERTY + ".keyword").value(FieldValue.of(dataIdentifier))));

	    }

	    srb = srb.size(maxRecords).query(qb.build())
		    .sort(s -> s.field(f -> f.field(DATE_PROPERTY).order(org.opensearch.client.opensearch._types.SortOrder.Desc)));

	    org.opensearch.client.opensearch.core.SearchResponse<JsonData> searchResponse = client.search(srb.build(), JsonData.class);

	    List<Hit<JsonData>> hits = searchResponse.hits().hits();
	    List<DataRecord> records = parseRecords(hits, DataRecord.class);
	    long total = searchResponse.hits().total().value();
	    long i = 0;
	    boolean completed = records.size() == total;
	    Response response = new Response(records, i, total, completed);
	    response.setCompleted(true);
	    response.setTotal(hits.size());
	    listener.recordsReturned(response);
	}

    }

    private org.opensearch.client.opensearch.core.SearchRequest.Builder createBaseSearchRequest(Date begin, Date end,
	    String... dataIdentifiers) {
	Query.Builder q = new Query.Builder();
	BoolQuery.Builder bq = new BoolQuery.Builder();

	if (dataIdentifiers.length > 0) {
	    for (String dataIdentifier : dataIdentifiers) {
		bq.must(f -> f.term(t -> t.field(DATA_IDENTIFIER_PROPERTY + ".keyword").value(FieldValue.of(dataIdentifier))));
	    }
	}
	if (begin != null) {
	    String property = DATE_PROPERTY;
	    bq.must(t -> t.range(a -> a.field(property).gte(JsonData.of(begin.getTime()))));
	}
	if (end != null) {
	    String property = DATE_PROPERTY;
	    bq.must(t -> t.range(a -> a.field(property).lte(JsonData.of(end.getTime()))));
	}
	q.bool(bq.build());
	return new org.opensearch.client.opensearch.core.SearchRequest.Builder()//
		.index(DataCacheIndex.VALUES.getIndex(databaseName))//
		.size(1000)//
		.query(q.build()).sort(s -> s.field(b -> b.field(DATA_IDENTIFIER_PROPERTY + ".keyword")//
			.order(org.opensearch.client.opensearch._types.SortOrder.Desc)))//
		.sort(s -> s.field(f -> f.field(DATE_PROPERTY)//
			.order(org.opensearch.client.opensearch._types.SortOrder.Asc)))//

	;
    }

    @Override
    public void getRecords(ResponseListener listener, Date begin, Date end, String... dataIdentifiers) throws Exception {
	org.opensearch.client.opensearch.core.SearchRequest.Builder srb = createBaseSearchRequest(begin, end, dataIdentifiers);

	// First search

	org.opensearch.client.opensearch.core.SearchResponse<JsonData> searchResponse = client.search(srb.build(), JsonData.class);

	List<Hit<JsonData>> hits = searchResponse.hits().hits();
	List<DataRecord> records = parseRecords(hits, DataRecord.class);
	long total = searchResponse.hits().total().value();
	long i = 0;
	boolean completed = records.size() == total;
	Response response = new Response(records, i, total, completed);
	listener.recordsReturned(response);
	i += records.size();
	if (completed) {
	    return;
	}

	while (!completed) {
	    List<String> sortValues = searchResponse.hits().hits().getLast().sort();

	    org.opensearch.client.opensearch.core.SearchRequest.Builder nextRequest = createBaseSearchRequest(begin, end, dataIdentifiers)
		    .searchAfter(sortValues);

	    searchResponse = client.search(nextRequest.build(), JsonData.class);

	    hits = searchResponse.hits().hits();
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
	org.opensearch.client.opensearch.core.SearchRequest.Builder srb = new org.opensearch.client.opensearch.core.SearchRequest.Builder();
	srb.index(DataCacheIndex.VALUES.getIndex(databaseName));
	Query.Builder q = new Query.Builder();
	BoolQuery.Builder bq = new BoolQuery.Builder();
	if (sourceIdentifier != null) {
	    bq.must(f -> f.term(a -> a.field(SOURCE_IDENTIFIER_PROPERTY + ".keyword").value(FieldValue.of(sourceIdentifier))));
	}
	bq.must(f -> f
		.range(s -> s.field(NEXT_RECORD_EXPECTED_TIME_PROPERTY).lt(JsonData.of(new Date(new Date().getTime() + 20000).getTime()))));

	q.bool(bq.build());
	srb.query(q.build());
	srb.size(10000);
	srb.sort(s -> s.field(f -> f.field(NEXT_RECORD_EXPECTED_TIME_PROPERTY)));

	srb.source(
		f -> f.filter(a -> a.includes(DATA_IDENTIFIER_PROPERTY, NEXT_RECORD_EXPECTED_TIME_PROPERTY).excludes(new ArrayList<>())));

	org.opensearch.client.opensearch.core.SearchResponse<JsonData> searchResponse = client.search(srb.build(), JsonData.class);

	List<Hit<JsonData>> hits = searchResponse.hits().hits();
	List<DataRecord> records = parseRecords(hits, DataRecord.class);
	List<SimpleEntry<String, Date>> ret = new ArrayList<>();
	for (DataRecord dataRecord : records) {
	    ret.add(new SimpleEntry<>(dataRecord.getDataIdentifier(), dataRecord.getNextRecordExpectedTime()));
	}
	return ret;
    }

    @Override
    public SimpleEntry<String, Date> getNextExpectedRecord(String sourceIdentifier) throws Exception {
	org.opensearch.client.opensearch.core.SearchRequest.Builder srb = new org.opensearch.client.opensearch.core.SearchRequest.Builder();
	srb.index(DataCacheIndex.VALUES.getIndex(databaseName));
	Query.Builder q = new Query.Builder();
	BoolQuery.Builder bqb = new BoolQuery.Builder();

	if (sourceIdentifier != null) {
	    bqb.must(f -> f.term(s -> s.field(SOURCE_IDENTIFIER_PROPERTY + ".keyword").value(FieldValue.of(sourceIdentifier))));
	}

	bqb.must(f -> f.range(r -> r.field(NEXT_RECORD_EXPECTED_TIME_PROPERTY).gt(JsonData.of(new Date().getTime()))));

	q.bool(bqb.build());
	srb.query(q.build());
	srb.size(1);

	srb.source(
		f -> f.filter(i -> i.includes(DATA_IDENTIFIER_PROPERTY, NEXT_RECORD_EXPECTED_TIME_PROPERTY).excludes(new ArrayList<>())));

	srb.sort(f -> f
		.field(s -> s.field(NEXT_RECORD_EXPECTED_TIME_PROPERTY).order(org.opensearch.client.opensearch._types.SortOrder.Asc)));

	org.opensearch.client.opensearch.core.SearchResponse<JsonData> searchResponse = client.search(srb.build(), JsonData.class);

	List<Hit<JsonData>> hits = searchResponse.hits().hits();
	List<DataRecord> records = parseRecords(hits, DataRecord.class);
	if (records.isEmpty()) {
	    return null;
	}
	DataRecord dataRecord = records.get(0);
	return new SimpleEntry<>(dataRecord.getDataIdentifier(), dataRecord.getNextRecordExpectedTime());
    }

    @Override
    public void forEachDataIdentifiersPage(String sourceIdentifier, PageConsumer<List<String>> consumer) throws Exception {

	Query query = Query.of(q -> q.term(t -> t.field(SOURCE_IDENTIFIER_PROPERTY + ".keyword").value(FieldValue.of(sourceIdentifier))));

	forEachSeriesBucketPage(DataCacheIndex.VALUES, DATA_IDENTIFIER_PROPERTY, query, Map.of(),
		buckets -> consumer.accept(buckets.stream().map(b -> b.key().get("id").to(String.class)).toList()));
    }

    @Override
    public Map<String, SeriesCacheInfo> getSeriesCacheInfo(String sourceIdentifier, Collection<String> dataIdentifiers)
	    throws Exception {

	Map<String, SeriesCacheInfo> ret = new HashMap<>();

	if (dataIdentifiers.isEmpty()) {
	    return ret;
	}

	List<FieldValue> ids = dataIdentifiers.stream().map(FieldValue::of).toList();

	// values summary, per series

	Query valuesQuery = Query.of(q -> q.bool(b -> b //
		.must(m -> m.term(t -> t.field(SOURCE_IDENTIFIER_PROPERTY + ".keyword").value(FieldValue.of(sourceIdentifier))))
		.must(m -> m.terms(t -> t.field(DATA_IDENTIFIER_PROPERTY + ".keyword").terms(v -> v.value(ids))))));

	Map<String, Aggregation> valuesAggregations = new HashMap<>();
	valuesAggregations.put("first", Aggregation.of(a -> a.min(m -> m.field(DATE_PROPERTY))));
	valuesAggregations.put("last", Aggregation.of(a -> a.max(m -> m.field(DATE_PROPERTY))));
	valuesAggregations.put("next", Aggregation.of(a -> a.max(m -> m.field(NEXT_RECORD_EXPECTED_TIME_PROPERTY))));
	// the max of a field without values is returned as 0 by the client, so its presence is checked separately
	valuesAggregations.put("nextCount", Aggregation.of(a -> a.valueCount(v -> v.field(NEXT_RECORD_EXPECTED_TIME_PROPERTY))));
	valuesAggregations.put("insert", Aggregation.of(a -> a.max(m -> m.field("timestamp"))));
	valuesAggregations.put("uri", Aggregation.of(a -> a.terms(t -> t.field("observedPropertyURI.keyword").size(1))));
	valuesAggregations.put("name", Aggregation.of(a -> a.terms(t -> t.field("observedProperty.keyword").size(1))));

	forEachSeriesBucketPage(DataCacheIndex.VALUES, DATA_IDENTIFIER_PROPERTY, valuesQuery, valuesAggregations, buckets -> {

	    for (CompositeBucket bucket : buckets) {

		String dataId = bucket.key().get("id").to(String.class);
		SeriesCacheInfo info = ret.computeIfAbsent(dataId, k -> new SeriesCacheInfo());
		info.setDataIdentifier(dataId);
		info.setValuesCount(bucket.docCount());
		info.setFirstObservation(toDate(bucket.aggregations().get("first").min().value()));
		info.setLastObservation(toDate(bucket.aggregations().get("last").max().value()));
		if (bucket.aggregations().get("nextCount").valueCount().value() > 0) {
		    info.setNextExpected(toDate(bucket.aggregations().get("next").max().value()));
		}
		info.setLastInsert(toDate(bucket.aggregations().get("insert").max().value()));
		info.setObservedPropertyURI(firstTerm(bucket.aggregations().get("uri")));
		info.setObservedProperty(firstTerm(bucket.aggregations().get("name")));
	    }
	    return true;
	});

	// last update attempt, per series

	Query statisticsQuery = Query.of(q -> q.bool(b -> b //
		.must(m -> m.term(t -> t.field("sourceId.keyword").value(FieldValue.of(sourceIdentifier))))
		.must(m -> m.terms(t -> t.field("dataIdentifier.keyword").terms(v -> v.value(ids))))));

	Map<String, Aggregation> statisticsAggregations = new HashMap<>();
	statisticsAggregations.put("latest", Aggregation.of(a -> a.topHits(th -> th.size(1)
		.sort(so -> so.field(f -> f.field(DATE_PROPERTY).order(org.opensearch.client.opensearch._types.SortOrder.Desc))))));

	forEachSeriesBucketPage(DataCacheIndex.STATISTICS, "dataIdentifier", statisticsQuery, statisticsAggregations, buckets -> {

	    for (CompositeBucket bucket : buckets) {

		List<Hit<JsonData>> hits = bucket.aggregations().get("latest").topHits().hits().hits();
		if (hits.isEmpty() || hits.get(0).source() == null) {
		    continue;
		}

		String dataId = bucket.key().get("id").to(String.class);
		JSONObject latest = new JSONObject(hits.get(0).source().toJson().toString());

		SeriesCacheInfo info = ret.computeIfAbsent(dataId, k -> new SeriesCacheInfo());
		info.setDataIdentifier(dataId);
		if (latest.has("date")) {
		    info.setLastUpdate(new Date(latest.getLong("date")));
		}
		if (latest.has("harvestedRecords")) {
		    info.setLastUpdateValues(latest.getInt("harvestedRecords"));
		}
		String error = latest.optString("error", null);
		if (error != null && !error.isBlank()) {
		    info.setLastUpdateError(error);
		}
	    }
	    return true;
	});

	return ret;
    }

    /**
     * Iterates, by composite aggregation, the pages of buckets of the documents matching the given query, grouped by the
     * given series identifier field
     */
    private void forEachSeriesBucketPage(DataCacheIndex index, String idField, Query query, Map<String, Aggregation> subAggregations,
	    PageConsumer<List<CompositeBucket>> consumer) throws Exception {

	Map<String, String> after = null;

	do {

	    Map<String, String> currentAfter = after;

	    SearchResponse<Void> response = client.search(s -> s.index(index.getIndex(databaseName)).size(0).query(query)
		    .aggregations("series", a -> a.composite(c -> {
			c.size(DATA_IDENTIFIERS_PAGE_SIZE)
				.sources(Map.of("id", CompositeAggregationSource.of(cs -> cs.terms(t -> t.field(idField + ".keyword")))));
			if (currentAfter != null) {
			    c.after(currentAfter);
			}
			return c;
		    }).aggregations(subAggregations)), Void.class);

	    CompositeAggregate aggregate = response.aggregations().get("series").composite();

	    if (!aggregate.buckets().array().isEmpty() && !consumer.accept(aggregate.buckets().array())) {
		return;
	    }

	    after = null;
	    if (!aggregate.buckets().array().isEmpty() && aggregate.afterKey() != null && !aggregate.afterKey().isEmpty()) {
		after = new HashMap<>();
		for (Map.Entry<String, JsonData> entry : aggregate.afterKey().entrySet()) {
		    after.put(entry.getKey(), entry.getValue().to(String.class));
		}
	    }

	} while (after != null);
    }

    private static Date toDate(double value) {

	return Double.isFinite(value) ? new Date((long) value) : null;
    }

    private static String firstTerm(Aggregate aggregate) {

	if (aggregate.isSterms() && !aggregate.sterms().buckets().array().isEmpty()) {
	    String key = aggregate.sterms().buckets().array().get(0).key();
	    return key == null || key.isBlank() ? null : key;
	}
	return null;
    }

    @Override
    public long deleteData(String sourceIdentifier, String dataIdentifier) throws Exception {

	if (dataIdentifier == null) {
	    throw new IllegalArgumentException("missing data identifier");
	}

	long deleted = deleteBefore(null, sourceIdentifier, dataIdentifier);

	BoolQuery.Builder bqb = new BoolQuery.Builder();
	if (sourceIdentifier != null) {
	    bqb.must(f -> f.term(t -> t.field(SOURCE_IDENTIFIER_PROPERTY + ".keyword").value(FieldValue.of(sourceIdentifier))));
	}
	bqb.must(f -> f.term(t -> t.field(DATA_IDENTIFIER_PROPERTY + ".keyword").value(FieldValue.of(dataIdentifier))));
	BoolQuery query = bqb.build();

	DeleteByQueryResponse response = client
		.deleteByQuery(d -> d.index(DataCacheIndex.STATIONS.getIndex(databaseName)).query(q -> q.bool(query)));
	GSLoggerFactory.getLogger(getClass()).info("Deleted stations of data {}: {}", dataIdentifier, response.deleted());

	if (sourceIdentifier != null) {
	    client.delete(d -> d.index(DataCacheIndex.SERIES_STATUS.getIndex(databaseName)).id(seriesStatusId(sourceIdentifier, dataIdentifier)));
	}

	return deleted;
    }

    //
    // SERIES STATUS
    //

    private static final String STATUS_FIELD = "status.keyword";
    private static final String STATUS_SOURCE_FIELD = "sourceIdentifier.keyword";
    private static final String STATUS_DATA_FIELD = "dataIdentifier.keyword";

    /**
     * Effective status severity (see {@link SeriesStatus#effective(SeriesStatus, Date, Date, long)})
     */
    private static final String SEVERITY_SCRIPT = """
	    String s = doc['status.keyword'].value;
	    if (s == 'ERROR') { return 0; }
	    if (s == 'MISSING') { return 4; }
	    if (s == 'NOT_IN_METADATA') { return 6; }
	    if (doc['nextExpected'].size() > 0 && doc['nextExpected'].value.toInstant().toEpochMilli() < params.overdue) { return 1; }
	    if (s == 'INCOMPLETE') { return 5; }
	    if (s == 'HISTORICAL') { return 7; }
	    if (s == 'NOT_SCHEDULED') { return 3; }
	    if (doc['lastObservation'].size() == 0 || doc['lastObservation'].value.toInstant().toEpochMilli() < params.stale) { return 2; }
	    return 8;
	    """;

    private static String seriesStatusId(String sourceIdentifier, String dataIdentifier) {

	return UUID.nameUUIDFromBytes((sourceIdentifier + "\n" + dataIdentifier).getBytes(StandardCharsets.UTF_8)).toString();
    }

    private static SeriesStatusRecord toStatusRecord(JsonData source) {

	return SeriesStatusRecord.fromJSON(new JSONObject(source.toJson().toString()));
    }

    @Override
    public void writeSeriesStatus(List<SeriesStatusRecord> records) throws Exception {

	if (records.isEmpty()) {
	    return;
	}

	String index = DataCacheIndex.SERIES_STATUS.getIndex(databaseName);

	List<BulkOperation> operations = new ArrayList<>();
	for (SeriesStatusRecord record : records) {
	    Map<String, Object> document = record.toJSON().toMap();
	    String id = seriesStatusId(record.getSourceIdentifier(), record.getDataIdentifier());
	    operations.add(BulkOperation.of(b -> b.index(i -> i.index(index).id(id).document(document))));
	}

	org.opensearch.client.opensearch.core.BulkResponse response = client.bulk(b -> b.operations(operations));
	if (response.errors()) {
	    response.items().stream().filter(i -> i.error() != null).findFirst().ifPresent(i -> GSLoggerFactory.getLogger(getClass())
		    .error("Error writing series status {}: {}", i.id(), i.error().reason()));
	}
    }

    @Override
    public Map<String, SeriesStatusRecord> getSeriesStatus(String sourceIdentifier, Collection<String> dataIdentifiers)
	    throws Exception {

	Map<String, SeriesStatusRecord> ret = new HashMap<>();
	List<String> ids = new ArrayList<>(dataIdentifiers);

	for (int i = 0; i < ids.size(); i += DATA_IDENTIFIERS_PAGE_SIZE) {

	    List<String> docIds = ids.subList(i, Math.min(ids.size(), i + DATA_IDENTIFIERS_PAGE_SIZE)).stream()
		    .map(id -> seriesStatusId(sourceIdentifier, id)).toList();

	    org.opensearch.client.opensearch.core.MgetResponse<JsonData> response = client
		    .mget(m -> m.index(DataCacheIndex.SERIES_STATUS.getIndex(databaseName)).ids(docIds), JsonData.class);

	    for (org.opensearch.client.opensearch.core.mget.MultiGetResponseItem<JsonData> item : response.docs()) {
		if (item.isResult() && item.result().found() && item.result().source() != null) {
		    SeriesStatusRecord record = toStatusRecord(item.result().source());
		    ret.put(record.getDataIdentifier(), record);
		}
	    }
	}

	return ret;
    }

    @Override
    public List<SeriesStatusRecord> getSeriesToAugment(String sourceIdentifier, Date attemptedBefore, int size) throws Exception {

	List<FieldValue> statuses = List.of(FieldValue.of(SeriesStatus.MISSING.name()), FieldValue.of(SeriesStatus.INCOMPLETE.name()),
		FieldValue.of(SeriesStatus.NOT_SCHEDULED.name()));

	SearchResponse<JsonData> response = client.search(s -> s.index(DataCacheIndex.SERIES_STATUS.getIndex(databaseName)).size(size)
		.query(q -> q.bool(b -> b //
			.must(m -> m.term(t -> t.field(STATUS_SOURCE_FIELD).value(FieldValue.of(sourceIdentifier))))
			.must(m -> m.terms(t -> t.field(STATUS_FIELD).terms(v -> v.value(statuses))))
			.should(sh -> sh.bool(nb -> nb.mustNot(mn -> mn.exists(e -> e.field("attempted")))))
			.should(sh -> sh.range(r -> r.field("attempted").lt(JsonData.of(attemptedBefore.getTime()))))
			.minimumShouldMatch("1"))),
		JsonData.class);

	return response.hits().hits().stream().filter(h -> h.source() != null).map(h -> toStatusRecord(h.source())).toList();
    }

    @Override
    public void forEachSeriesStatusPage(String sourceIdentifier, SeriesStatus status, PageConsumer<List<SeriesStatusRecord>> consumer)
	    throws Exception {

	List<String> searchAfter = null;

	while (true) {

	    List<String> currentAfter = searchAfter;

	    SearchResponse<JsonData> response = client.search(s -> {
		s.index(DataCacheIndex.SERIES_STATUS.getIndex(databaseName)).size(DATA_IDENTIFIERS_PAGE_SIZE)
			.query(q -> q.bool(b -> b //
				.must(m -> m.term(t -> t.field(STATUS_SOURCE_FIELD).value(FieldValue.of(sourceIdentifier))))
				.must(m -> m.term(t -> t.field(STATUS_FIELD).value(FieldValue.of(status.name()))))))
			.sort(so -> so.field(f -> f.field(STATUS_DATA_FIELD).order(org.opensearch.client.opensearch._types.SortOrder.Asc)));
		if (currentAfter != null) {
		    s.searchAfter(currentAfter);
		}
		return s;
	    }, JsonData.class);

	    List<Hit<JsonData>> hits = response.hits().hits();
	    if (hits.isEmpty()) {
		return;
	    }

	    List<SeriesStatusRecord> page = hits.stream().filter(h -> h.source() != null).map(h -> toStatusRecord(h.source())).toList();
	    if (!consumer.accept(page)) {
		return;
	    }

	    searchAfter = hits.get(hits.size() - 1).sort();
	}
    }

    @Override
    public long deleteSeriesStatusNotInPass(String sourceIdentifier, String passId) throws Exception {

	DeleteByQueryResponse response = client.deleteByQuery(d -> d.index(DataCacheIndex.SERIES_STATUS.getIndex(databaseName))
		.query(q -> q.bool(b -> b //
			.must(m -> m.term(t -> t.field(STATUS_SOURCE_FIELD).value(FieldValue.of(sourceIdentifier))))
			.mustNot(m -> m.term(t -> t.field("passId.keyword").value(FieldValue.of(passId)))))));

	return response.deleted() == null ? 0 : response.deleted();
    }

    /**
     * @return the query of the series having the given effective status (see
     *         {@link SeriesStatus#effective(SeriesStatus, Date, Date, long)})
     */
    private static Query effectiveStatusQuery(SeriesStatus status, long now) {

	long overdueLimit = now - SeriesStatus.OVERDUE_MS;
	long staleLimit = now - SeriesStatus.STALE_MS;

	Query overdue = Query.of(q -> q.range(r -> r.field("nextExpected").lt(JsonData.of(overdueLimit))));
	Query notTimeIndependent = Query.of(q -> q.bool(b -> b.mustNot(m -> m.terms(t -> t.field(STATUS_FIELD)
		.terms(v -> v.value(List.of(FieldValue.of("ERROR"), FieldValue.of("MISSING"), FieldValue.of("NOT_IN_METADATA"))))))));

	return switch (status) {
	case ERROR, MISSING, NOT_IN_METADATA -> Query.of(q -> q.term(t -> t.field(STATUS_FIELD).value(FieldValue.of(status.name()))));
	case OVERDUE -> Query.of(q -> q.bool(b -> b.must(notTimeIndependent).must(overdue)));
	case INCOMPLETE, HISTORICAL, NOT_SCHEDULED -> Query.of(q -> q.bool(b -> b //
		.must(m -> m.term(t -> t.field(STATUS_FIELD).value(FieldValue.of(status.name())))).mustNot(overdue)));
	case STALE -> Query.of(q -> q.bool(b -> b //
		.must(m -> m.term(t -> t.field(STATUS_FIELD).value(FieldValue.of(SeriesStatus.OK.name())))).mustNot(overdue)
		.should(sh -> sh.range(r -> r.field("lastObservation").lt(JsonData.of(staleLimit))))
		.should(sh -> sh.bool(nb -> nb.mustNot(mn -> mn.exists(e -> e.field("lastObservation"))))).minimumShouldMatch("1")));
	case OK -> Query.of(q -> q.bool(b -> b //
		.must(m -> m.term(t -> t.field(STATUS_FIELD).value(FieldValue.of(SeriesStatus.OK.name())))).mustNot(overdue)
		.must(m -> m.range(r -> r.field("lastObservation").gte(JsonData.of(staleLimit))))));
	};
    }

    @Override
    public SeriesStatusPage searchSeriesStatus(String sourceIdentifier, String conceptKey, SeriesStatus status, String text,
	    String cursor, int size) throws Exception {

	SeriesStatusPage ret = new SeriesStatusPage();
	long now = System.currentTimeMillis();
	ret.setNow(now);

	String index = DataCacheIndex.SERIES_STATUS.getIndex(databaseName);
	Query sourceQuery = Query.of(q -> q.term(t -> t.field(STATUS_SOURCE_FIELD).value(FieldValue.of(sourceIdentifier))));

	// query: source, concept and text. the status filter is applied as post filter, so that the counts per status
	// match the other filters

	BoolQuery.Builder bqb = new BoolQuery.Builder().must(sourceQuery);
	if (conceptKey != null && !conceptKey.isBlank()) {
	    bqb.must(m -> m.term(t -> t.field("conceptKey.keyword").value(FieldValue.of(conceptKey))));
	}
	if (text != null && !text.isBlank()) {
	    String pattern = "*" + text.trim().replace("\\", "\\\\").replace("*", "\\*").replace("?", "\\?") + "*";
	    BoolQuery.Builder textQuery = new BoolQuery.Builder().minimumShouldMatch("1");
	    for (String field : List.of("dataIdentifier", "title", "platform", "observedProperty")) {
		textQuery.should(sh -> sh.wildcard(w -> w.field(field + ".keyword").value(pattern).caseInsensitive(true)));
	    }
	    BoolQuery textBool = textQuery.build();
	    bqb.must(m -> m.bool(textBool));
	}
	BoolQuery query = bqb.build();

	Map<String, Query> statusFilters = new LinkedHashMap<>();
	for (SeriesStatus s : SeriesStatus.values()) {
	    statusFilters.put(s.name(), effectiveStatusQuery(s, now));
	}

	List<String> searchAfter = cursor == null || cursor.isBlank() ? null : new JSONArray(cursor).toList().stream().map(String::valueOf).toList();

	SearchResponse<JsonData> response = client.search(s -> {
	    s.index(index).size(size).trackTotalHits(t -> t.enabled(true)).query(q -> q.bool(query))
		    .aggregations("statuses", a -> a.filters(f -> f.filters(b -> b.keyed(statusFilters))))
		    .aggregations("computed", a -> a.max(m -> m.field("computed")))
		    .aggregations("computedCount", a -> a.valueCount(v -> v.field("computed")))
		    .sort(so -> so.script(sc -> sc.type(org.opensearch.client.opensearch._types.ScriptSortType.Number)
			    .order(org.opensearch.client.opensearch._types.SortOrder.Asc)
			    .script(scr -> scr.inline(i -> i.source(SEVERITY_SCRIPT).params("overdue", JsonData.of(now - SeriesStatus.OVERDUE_MS))
				    .params("stale", JsonData.of(now - SeriesStatus.STALE_MS))))))
		    .sort(so -> so.field(f -> f.field("lastObservation").order(org.opensearch.client.opensearch._types.SortOrder.Asc)
			    .missing(FieldValue.of("_first"))))
		    .sort(so -> so.field(f -> f.field(STATUS_DATA_FIELD).order(org.opensearch.client.opensearch._types.SortOrder.Asc)));
	    if (status != null) {
		s.postFilter(statusFilters.get(status.name()));
	    }
	    if (searchAfter != null) {
		s.searchAfter(searchAfter);
	    }
	    return s;
	}, JsonData.class);

	List<Hit<JsonData>> hits = response.hits().hits();
	for (Hit<JsonData> hit : hits) {
	    if (hit.source() != null) {
		ret.getRecords().add(toStatusRecord(hit.source()));
	    }
	}

	ret.setTotal(response.hits().total() == null ? hits.size() : response.hits().total().value());
	if (hits.size() == size) {
	    ret.setNextCursor(new JSONArray(hits.get(hits.size() - 1).sort()).toString());
	}

	Map<String, org.opensearch.client.opensearch._types.aggregations.FiltersBucket> statusBuckets = response.aggregations().get("statuses")
		.filters().buckets().keyed();
	for (SeriesStatus s : SeriesStatus.values()) {
	    org.opensearch.client.opensearch._types.aggregations.FiltersBucket bucket = statusBuckets.get(s.name());
	    ret.getCounts().put(s, bucket == null ? 0l : bucket.docCount());
	}

	// the max of a field without values is returned as 0 by the client
	if (response.aggregations().get("computedCount").valueCount().value() > 0) {
	    ret.setComputed(toDate(response.aggregations().get("computed").max().value()));
	}

	// concepts of the source

	Query fine = Query.of(q -> q.bool(b -> b.should(effectiveStatusQuery(SeriesStatus.OK, now))
		.should(effectiveStatusQuery(SeriesStatus.HISTORICAL, now)).minimumShouldMatch("1")));

	SearchResponse<Void> conceptsResponse = client.search(s -> s.index(index).size(0).query(sourceQuery)
		.aggregations("concepts", a -> a.terms(t -> t.field("conceptKey.keyword").size(MAX_REPORT_CONCEPTS))
			.aggregations("uri", sa -> sa.terms(t -> t.field("observedPropertyURI.keyword").size(1)))
			.aggregations("name", sa -> sa.terms(t -> t.field("observedProperty.keyword").size(1)))
			.aggregations("fine", sa -> sa.filter(fine))),
		Void.class);

	Aggregate conceptsAggregate = conceptsResponse.aggregations().get("concepts");
	if (conceptsAggregate.isSterms()) {
	    for (StringTermsBucket bucket : conceptsAggregate.sterms().buckets().array()) {
		long fineCount = bucket.aggregations().get("fine").filter().docCount();
		ret.getConcepts().add(new SeriesStatusPage.ConceptCount(bucket.key(), firstTerm(bucket.aggregations().get("uri")),
			firstTerm(bucket.aggregations().get("name")), bucket.docCount(), bucket.docCount() - fineCount));
	    }
	}

	return ret;
    }

    @Override
    public void deleteStations(String sourceIdentifier) throws Exception {
	deleteStations(sourceIdentifier, null);
    }

    @Override
    public void deleteStations(String sourceIdentifier, String theme) throws Exception {

	org.opensearch.client.opensearch.core.DeleteByQueryRequest.Builder db = new org.opensearch.client.opensearch.core.DeleteByQueryRequest.Builder();
	db = db.index(DataCacheIndex.STATIONS.getIndex(databaseName));

	Query.Builder qb = new Query.Builder();
	BoolQuery.Builder bqb = new BoolQuery.Builder();

	if (sourceIdentifier != null) {
	    bqb.must(f -> f.term(q -> q.field(SOURCE_IDENTIFIER_PROPERTY + ".keyword").value(FieldValue.of(sourceIdentifier))));
	}
	if (theme != null) {
	    bqb.must(f -> f.term(q -> q.field(THEME_CATEGORY_PROPERTY + ".keyword").value(FieldValue.of(theme))));
	}
	qb.bool(bqb.build());
	db.query(qb.build());

	DeleteByQueryResponse response = client.deleteByQuery(db.build());
	long deleted = response.deleted();
	GSLoggerFactory.getLogger(getClass()).info("Deleted records: {}", deleted);

    }

    @Override
    public void deleteStation(String stationId) throws Exception {
	if (stationId == null) {
	    throw new RuntimeException("missing station id");
	}
	org.opensearch.client.opensearch.core.DeleteByQueryRequest.Builder dbrb = new org.opensearch.client.opensearch.core.DeleteByQueryRequest.Builder();

	dbrb.index(DataCacheIndex.STATIONS.getIndex(databaseName));
	dbrb.query(q -> q
		.bool(b -> b.must(t -> t.term(f -> f.field(PLATFORM_IDENTIFIER_PROPERTY + ".keyword").value(FieldValue.of(stationId))))));
	DeleteByQueryResponse response = client.deleteByQuery(dbrb.build());
	long deleted = response.deleted().longValue();
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
	org.opensearch.client.opensearch.core.SearchRequest.Builder srb = new org.opensearch.client.opensearch.core.SearchRequest.Builder();
	srb.index(DataCacheIndex.STATIONS.getIndex(databaseName));

	BoolQuery stationQuery = getStationsQuery(null, bbox, allProperties, neededProperties, propertyValues);

	srb.query(b -> b.bool(stationQuery));

	if (offset != null) {
	    srb.from(offset);
	} else {
	    srb.from(0);
	}

	if (maxRecords != null) {
	    srb.size(maxRecords);
	} else {
	    srb.size(10000);
	}

	org.opensearch.client.opensearch.core.SearchResponse<JsonData> searchResponse = client.search(srb.build(), JsonData.class);

	List<Hit<JsonData>> hits = searchResponse.hits().hits();
	List<StationRecord> records = parseRecords(hits, StationRecord.class);
	return records;

    }

    @Override
    public void getStationsWithProperties(ResponseListener<StationRecord> listener, Date lastHarvesting, BBOX bbox, Integer maxRecords,
	    boolean allProperties, SimpleEntry<String, String>... propertyValues) throws Exception {
	org.opensearch.client.opensearch.core.SearchRequest.Builder srb = new org.opensearch.client.opensearch.core.SearchRequest.Builder();
	srb.index(DataCacheIndex.STATIONS.getIndex(databaseName));
	//
	BoolQuery sourceBuilder = getStationsQuery(lastHarvesting, bbox, allProperties, propertyValues);
	srb.query(q -> q.bool(sourceBuilder));

	if (maxRecords != null) {
	    srb.size(maxRecords);
	} else {
	    srb.size(1000);
	}
	srb.scroll(t -> t.time("1m"));

	org.opensearch.client.opensearch.core.SearchResponse<JsonData> searchResponse = client.search(srb.build(), JsonData.class);

	String scrollId = searchResponse.scrollId();
	List<Hit<JsonData>> hits = searchResponse.hits().hits();
	List<StationRecord> records = parseRecords(hits, StationRecord.class);
	long total = searchResponse.hits().total().value();
	long i = 0;
	boolean completed = records.size() == total;
	Response<StationRecord> response = new Response<StationRecord>(records, i, total, completed);
	listener.recordsReturned(response);
	i += records.size();
	if (completed) {
	    return;
	}

	while (!completed) {

	    ScrollRequest.Builder sssb = new ScrollRequest.Builder();
	    sssb.scrollId(scrollId).scroll(t -> t.time("1m"));
	    ScrollResponse<JsonData> searchScrollResponse = client.scroll(sssb.build(), JsonData.class);
	    scrollId = searchScrollResponse.scrollId();
	    hits = searchScrollResponse.hits().hits();
	    records = parseRecords(hits, StationRecord.class);
	    if (records.isEmpty()) {
		completed = true;
	    }
	    response = new Response<StationRecord>(records, i, total, completed);
	    listener.recordsReturned(response);
	    i += records.size();
	}

    }

    private BoolQuery getStationsQuery(Date lastHarvesting, BBOX bbox, boolean allProperties,
	    SimpleEntry<String, String>... propertyValues) {
	return getStationsQuery(lastHarvesting, bbox, allProperties, null, propertyValues);
    }

    private BoolQuery getStationsQuery(Date lastHarvesting, BBOX bbox, boolean allProperties, List<String> neededProperties,
	    SimpleEntry<String, String>... propertyValues) {

	BoolQuery.Builder bqb = new BoolQuery.Builder();
	addProperties(bqb, allProperties, propertyValues);
	if (neededProperties != null && !neededProperties.isEmpty()) {
	    for (String neededProperty : neededProperties) {
		bqb.must(e -> e.exists(q -> q.field(neededProperty)));

	    }
	}
	addBBOX(bqb, bbox);

	if (lastHarvesting != null) {
	    bqb.must(f -> f.range(r -> r.field("lastHarvesting").gt(JsonData.of(ISO8601DateTimeUtils.getISO8601DateTime(lastHarvesting)))));
	}

	SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
	if (!allProperties && propertyValues != null && propertyValues.length > 0) {
	    bqb.minimumShouldMatch("1");
	}
	return bqb.build();
    }

    private void addBBOX(BoolQuery.Builder query, BBOX bbox) {
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
	    query.mustNot(f -> f.range(q -> q.field(WEST_PROPERTY).gt(JsonData.of(searchMaxx))));
	    query.mustNot(f -> f.range(q -> q.field(EAST_PROPERTY).lt(JsonData.of(searchMinx))));
	    query.mustNot(f -> f.range(q -> q.field(SOUTH_PROPERTY).gt(JsonData.of(searchMaxy))));
	    query.mustNot(f -> f.range(q -> q.field(NORTH_PROPERTY).lt(JsonData.of(searchMiny))));
	    break;

	case "EPSG:3857":
	default:
	    query.mustNot(f -> f.range(q -> q.field(MINX3857_PROPERTY).gt(JsonData.of(searchMaxx))));
	    query.mustNot(f -> f.range(q -> q.field(MAXX3857_PROPERTY).lt(JsonData.of(searchMinx))));
	    query.mustNot(f -> f.range(q -> q.field(MINY3857_PROPERTY).gt(JsonData.of(searchMaxy))));
	    query.mustNot(f -> f.range(q -> q.field(MAXY3857_PROPERTY).lt(JsonData.of(searchMiny))));

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

	// BoolQuery.Builder query = getQuery(bbox, allProperties, propertyValues);
	BoolQuery boolQuery = getQuery(bbox, allProperties, propertyValues).build();

	SimpleEntry<Long, StationRecord> stationSouth = getStation(boolQuery, SOUTH_PROPERTY,
		org.opensearch.client.opensearch._types.SortOrder.Asc);
	SimpleEntry<Long, StationRecord> stationNorth = getStation(boolQuery, NORTH_PROPERTY,
		org.opensearch.client.opensearch._types.SortOrder.Desc);
	SimpleEntry<Long, StationRecord> stationEast = getStation(boolQuery, EAST_PROPERTY,
		org.opensearch.client.opensearch._types.SortOrder.Desc);
	SimpleEntry<Long, StationRecord> stationWest = getStation(boolQuery, WEST_PROPERTY,
		org.opensearch.client.opensearch._types.SortOrder.Asc);

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

    private SimpleEntry<Long, StationRecord> getStation(BoolQuery boolQuery, String sortProperty,
	    org.opensearch.client.opensearch._types.SortOrder order) throws Exception {
	org.opensearch.client.opensearch.core.SearchRequest.Builder srb = new org.opensearch.client.opensearch.core.SearchRequest.Builder();
	srb.index(DataCacheIndex.STATIONS.getIndex(databaseName));
	Query.Builder qb = new Query.Builder();
	qb.bool(boolQuery);
	srb.query(qb.build());
	srb.size(1);
	srb.sort(f -> f.field(fs -> fs.field(sortProperty).order(order)));

	org.opensearch.client.opensearch.core.SearchResponse<JsonData> searchResponse = client.search(srb.build(), JsonData.class);
	List<Hit<JsonData>> hits = searchResponse.hits().hits();
	List<StationRecord> records = parseRecords(hits, StationRecord.class);

	long total = searchResponse.hits().total().value();
	if (records.isEmpty()) {
	    return new SimpleEntry<>(0l, null);
	}
	return new SimpleEntry<>(total, records.get(0));

    }

    private BoolQuery.Builder getQuery(BBOX bbox, boolean allProperties, SimpleEntry<String, String>[] propertyValues) {
	BoolQuery.Builder query = new BoolQuery.Builder();
	addProperties(query, allProperties, propertyValues);
	addBBOX(query, bbox);
	return query;

    }

    private void addProperties(BoolQuery.Builder query, boolean allProperties, SimpleEntry<String, String>... propertyValues) {
	if (propertyValues != null) {
	    for (SimpleEntry<String, String> propertyValue : propertyValues) {
		if (allProperties) {
		    query.must(
			    q -> q.term(t -> t.field(propertyValue.getKey() + ".keyword").value(FieldValue.of(propertyValue.getValue()))));
		} else {
		    query.should(
			    f -> f.term(s -> s.field(propertyValue.getKey() + ".keyword").value(FieldValue.of(propertyValue.getValue()))));
		}
	    }
	}

    }

    @Override
    public int countRecordsInDataBuffer() {
	try {
	    return dataRecordbuffer.sizeEstimate();
	} catch (IOException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e);
	    return -1;
	}
    }

    @Override
    public void clearStations() throws Exception {

	client.indices().delete(d -> d.index(DataCacheIndex.STATIONS.getIndex(databaseName)));
	indexInitialization();

    }

}
