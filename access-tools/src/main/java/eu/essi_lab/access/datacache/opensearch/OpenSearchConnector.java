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
import java.io.StringReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.json.JSONObject;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.Time;
import org.opensearch.client.opensearch._types.aggregations.Aggregate;
import org.opensearch.client.opensearch._types.aggregations.MaxAggregate;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.Query.Builder;
import org.opensearch.client.opensearch.core.DeleteByQueryResponse;
import org.opensearch.client.opensearch.core.ScrollRequest;
import org.opensearch.client.opensearch.core.ScrollResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
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
import eu.essi_lab.access.datacache.Polygon4326;
import eu.essi_lab.access.datacache.Response;
import eu.essi_lab.access.datacache.ResponseListener;
import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.access.datacache.StationsStatistics;
import eu.essi_lab.access.datacache.StatisticsRecord;
import eu.essi_lab.access.datacache.WKT;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;

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

    private OpenSearchClient client = null;
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
	    e.printStackTrace();
	}

	RestClientBuilder builder = RestClient.builder(httpHost)
		.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
		    @Override
		    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {

			try {
			    SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(null, (chain, authType) -> true).build();
			    return httpClientBuilder.setSSLContext(sslContext).setSSLHostnameVerifier((hostname1, session) -> true);

			} catch (Exception e) {
			    GSLoggerFactory.getLogger(getClass()).error(e);
			}

			return null;
		    }
		});

	if (username != null && !username.isEmpty() && //
		password != null && !password.isEmpty()) {

	    BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();

	    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

	    builder = builder
		    .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));

	}

	RestClient restClient = builder.build();

	OpenSearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

	return new OpenSearchClient(transport);

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
	T record = class1.newInstance();
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

    }

    public boolean checkIndexExistence(String index) throws IOException {
	ExistsRequest.Builder erb = new ExistsRequest.Builder();
	return client.indices().exists(erb.index(index).build()).value();
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
		org.opensearch.client.opensearch.core.BulkRequest.Builder request = new org.opensearch.client.opensearch.core.BulkRequest.Builder();
		List<BulkOperation> operations = new ArrayList<>();
		for (Object record : tmp) {
		    if (record instanceof DataRecord) {
			DataRecord dataRecord = (DataRecord) record;
			JSONObject json = getJSONObject(dataRecord);
			String id = "ID" + UUID.randomUUID().toString();
			String dataId = dataRecord.getDataIdentifier();
			if (dataId != null) {
			    id = dataId + "-" + dataRecord.getDate().getTime();
			}

			String finalId = id;

			BulkOperation.Builder bob = new BulkOperation.Builder();
			bob.index(idx -> idx.index(DataCacheIndex.VALUES.getIndex(databaseName)).id(finalId).document(json));
			operations.add(bob.build());

		    } else if (record instanceof StatisticsRecord) {
			StatisticsRecord statisticsRecord = (StatisticsRecord) record;
			JSONObject json = getJSONObject(statisticsRecord);
			String id = "ID" + UUID.randomUUID().toString();
			String dataId = statisticsRecord.getDataIdentifier();
			if (dataId != null) {
			    id = dataId + "-STAT-" + statisticsRecord.getDate().getTime();
			}

			String finalId = id;

			BulkOperation.Builder bob = new BulkOperation.Builder();
			bob.index(idx -> idx.index(DataCacheIndex.STATISTICS.getIndex(databaseName)).id(finalId).document(json));
			operations.add(bob.build());

		    } else if (record instanceof StationRecord) {
			StationRecord stationRecord = (StationRecord) record;
			JSONObject json = getJSONObject(stationRecord);
			String id = "ID" + UUID.randomUUID().toString();
			String metadataId = stationRecord.getMetadataIdentifier();
			if (metadataId != null) {
			    id = metadataId + "-STATION";
			}

			String finalId = id;

			BulkOperation.Builder bob = new BulkOperation.Builder();
			bob.index(idx -> idx.index(DataCacheIndex.STATIONS.getIndex(databaseName)).id(finalId).document(json));
			operations.add(bob.build());

		    }

		}
		request = request.operations(operations);
		flush(request.build());
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
    public void deleteBefore(Date date, String sourceIdentifier, String dataIdentifier) throws Exception {
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

	DeleteByQueryResponse response = client.deleteByQuery(dqrb.build());
	long deleted = response.deleted();
	GSLoggerFactory.getLogger(getClass()).info("Deleted records: {}", deleted);

    }

    @Override
    public Long count() throws Exception {
	org.opensearch.client.opensearch.core.CountResponse count = client
		.count(c -> c.index(DataCacheIndex.VALUES.getIndex(databaseName)));
	return count.count();
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

    @Override
    public void getRecords(ResponseListener listener, Date begin, Date end, String... dataIdentifiers) throws Exception {
	org.opensearch.client.opensearch.core.SearchRequest.Builder srb = new org.opensearch.client.opensearch.core.SearchRequest.Builder();
	srb.index(DataCacheIndex.VALUES.getIndex(databaseName));

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
	srb.query(q.build());
	srb.size(1000);
	// String[] include = new String[] { DATA_IDENTIFIER_PROPERTY, DATE_PROPERTY,
	// VALUE_PROPERTY };
	// String[] exclude = new String[] {};
	// sourceBuilder.fetchSource(include, exclude);
	srb.sort(s -> s
		.field(b -> b.field(DATA_IDENTIFIER_PROPERTY + ".keyword").order(org.opensearch.client.opensearch._types.SortOrder.Desc)));

	srb.scroll(t -> t.time("1m"));

	org.opensearch.client.opensearch.core.SearchResponse<JsonData> searchResponse = client.search(srb.build(), JsonData.class);

	String scrollId = searchResponse.scrollId();
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
	    // SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
	    ScrollRequest.Builder sr = new ScrollRequest.Builder();
	    sr.scroll(Time.of(t -> t.time("1m")));
	    ScrollResponse<JsonData> searchScrollResponse = client.scroll(sr.build(), JsonData.class);
	    scrollId = searchScrollResponse.scrollId();
	    hits = searchScrollResponse.hits().hits();
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

	//BoolQuery.Builder query = getQuery(bbox, allProperties, propertyValues);
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

	client.indices().delete(d -> d.index(DataCacheIndex.STATIONS.getIndex(databaseName)));
	indexInitialization();

    }

}
