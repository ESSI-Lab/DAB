package eu.essi_lab.services.data_hub;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.*;
import eu.essi_lab.accessor.datahub.*;
import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.factory.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.indexes.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.model.resource.*;
import eu.essi_lab.services.impl.*;
import eu.essi_lab.services.message.*;
import org.apache.avro.*;
import org.apache.avro.generic.*;
import org.apache.avro.io.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.*;
import org.json.*;
import org.w3c.dom.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * @author Fabrizio
 */
public class DataHUBService extends AbstractManagedService {

    private final Map<Integer, Schema> schemaCache;
    private final ObjectMapper mapper;
    private boolean running;

    private static final String SCHEMA_REGISTRY_URL_KEY = "shemaRegistryURL";
    private static final String SERVICE_URL_KEY = "serviceURL";

    private static final String TOKEN_USER_KEY = "token.user";
    private static final String TOKEN_PWD_KEY = "token.pwd";

    private static final String KAFKA_BOOTSTRAP_SERVERS_KEY = "kafka.bootstrapServers";
    private static final String KAFKA_TOPIC_KEY = "kafka.topic";
    private static final String KAFKA_GROUP_ID_KEY = "kafka.groupId";
    private static final String KAFKA_USERNAME_KEY = "kafka.username";
    private static final String KAFKA_PASSWORD_KEY = "kafka.password";
    private static final String KAFKA_MAX_MESSAGES_KEY = "kafka.maxMessages";

    private static final String TEST_FILTER_KEY = "test.filter";
    private static final String TEST_FILTER_ENABLED_KEY = "test.filter.enabled";

    private static final String DATA_HUB_SOURCE_ID_KEY = "dataHub.sourceId";
    private static final String DATA_HUB_SOURCE_LABEL_KEY = "dataHub.sourceLabel";
    private static final String DATA_HUB_RECORDS_FILTER_KEY = "dataHub.recordsFilter";

    private static final String THREAD_POOL_SIZE_KEY = "threadPoolSize";

    private static final List<String> TEST_IDS_ = List.of(
	    "urn:li:dataset:(urn:li:dataPlatform:metadata,abdam_pai_rischio_frana_uom_dx_sele_glt_gct,DEV)",//
	    "urn:li:dataset:(urn:li:dataPlatform:metadata,abdam_pai_rischio_frana_uom_dx_sele_glt_pzz,DEV)",//
	    "urn:li:dataset:(urn:li:dataPlatform:metadata,abdam_pai_rischio_frana_uom_dx_sele_glt_srg,DEV)");//

    /**
     *
     */
    private KafkaConsumer<byte[], byte[]> consumer;
    private String sourceId;
    private DatabaseFolder targetFolder;
    private String serviceUrl;
    private String token;
    private String sourceLabel;

    /**
     *
     */
    private static final int DEFAULT_THREAD_POOL_SIZE = 10;
    private ExecutorService executor;

    /**
     *
     */
    public DataHUBService() {

	schemaCache = new HashMap<>();
	mapper = new ObjectMapper();
    }

    @Override
    public void start() {

	running = true;

	//
	// read key-values
	//

	Optional<String> bootstrapServers = getSetting().readKeyValue(KAFKA_BOOTSTRAP_SERVERS_KEY);

	if (bootstrapServers.isEmpty()) {

	    error("Missing bootstrap server configuration");
	}

	Optional<String> groupId = getSetting().readKeyValue(KAFKA_GROUP_ID_KEY);

	if (!(check(groupId))) {

	    error("Missing group id");
	}

	Optional<String> user = getSetting().readKeyValue(KAFKA_USERNAME_KEY);

	if (!(check(user))) {

	    error("Missing user name");
	}

	Optional<String> pwd = getSetting().readKeyValue(KAFKA_PASSWORD_KEY);

	if (!(check(pwd))) {

	    error("Missing password");
	}

	Optional<String> optServiceUrl = getSetting().readKeyValue(SERVICE_URL_KEY);

	if (!(check(optServiceUrl))) {

	    error("Missing token url");

	} else {

	    serviceUrl = optServiceUrl.get();
	}

	Optional<String> tokenUser = getSetting().readKeyValue(TOKEN_USER_KEY);

	if (!(check(tokenUser))) {

	    error("Missing token user name");
	}

	Optional<String> tokenPwd = getSetting().readKeyValue(TOKEN_PWD_KEY);

	if (!(check(tokenPwd))) {

	    error("Missing token password");
	}

	Optional<String> schemaRegistryURL = getSetting().readKeyValue(SCHEMA_REGISTRY_URL_KEY);

	if (!(check(schemaRegistryURL))) {

	    error("Missing schema registry url");
	}

	Optional<String> topic = getSetting().readKeyValue(KAFKA_TOPIC_KEY);

	if (!(check(topic))) {

	    error("Missing topic name");
	}

	Optional<String> maxMessages = getSetting().readKeyValue(KAFKA_MAX_MESSAGES_KEY);

	if (!(check(maxMessages))) {

	    error("Missing max messages");
	}

	boolean useTestFilter = getSetting(). //
		readKeyValue(TEST_FILTER_ENABLED_KEY).//
		map(Boolean::parseBoolean).//
		orElse(true);//

	Optional<String> testFilter = getSetting().readKeyValue(TEST_FILTER_KEY);

	if (useTestFilter && !(check(testFilter))) {

	    error("Missing required test filter");
	}

	Optional<String> recordsFilter = getSetting().readKeyValue(DATA_HUB_RECORDS_FILTER_KEY);

	if (!(check(recordsFilter))) {

	    error("Missing records filter");
	}

	Optional<String> optSourceId = getSetting().readKeyValue(DATA_HUB_SOURCE_ID_KEY);

	if (!(check(optSourceId))) {

	    error("Missing source identifier");

	} else {

	    sourceId = optSourceId.get();
	}

	Optional<String> optSourceLabel = getSetting().readKeyValue(DATA_HUB_SOURCE_LABEL_KEY);

	if (!(check(optSourceLabel))) {

	    error("Missing source label");

	} else {

	    sourceLabel = optSourceLabel.get();
	}

	int threadPoolSize = getSetting(). //
		readKeyValue(THREAD_POOL_SIZE_KEY).//
		map(Integer::parseInt).//
		orElse(DEFAULT_THREAD_POOL_SIZE);//

	ThreadFactory factory = Thread.ofPlatform().//
		name(getClass().getSimpleName()).//
		factory();

	executor = Executors.newFixedThreadPool(threadPoolSize, factory);

	if (!running) {

	    return;
	}

	//
	// get target folder
	//

	try {

	    Database database = DatabaseFactory.get(ConfigurationWrapper.getStorageInfo());

	    SourceStorageWorker worker = database.getWorker(sourceId);

	    boolean data1Folder = worker.existsData1Folder();
	    boolean data2Folder = worker.existsData2Folder();

	    if (data1Folder && data2Folder) {

		error("Both data1 and data2 folders exist");
		return;
	    }

	    if (!data1Folder && !data2Folder) {

		error("Both data1 and data2 folders missing");
		return;
	    }

	    targetFolder = data1Folder ? worker.getData1Folder() : worker.getData2Folder();

	} catch (GSException e) {

	    error("Unable to get database instance: " + e.getMessage(), e);
	    return;
	}

	//
	// get token
	//

	try {

	    token = String.valueOf(getAccessToken(serviceUrl, tokenUser.get(), tokenPwd.get()));

	} catch (Exception e) {

	    error("Unable to get access token: " + e.getMessage(), e);

	    return;
	}

	//
	// init consumer
	//

	Properties props = new Properties();

	props.put("bootstrap.servers", bootstrapServers.get());
	props.put("group.id", groupId.get());
	props.put("enable.auto.commit", "false");
	props.put("auto.offset.reset", "latest");

	props.put("security.protocol", "SASL_SSL");
	props.put("sasl.mechanism", "SCRAM-SHA-512");

	props.put("sasl.jaas.config",
		"org.apache.kafka.common.security.scram.ScramLoginModule required " + "username=\"" + user.get() + "\" " + "password=\""
			+ pwd.get() + "\";");

	props.put("key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

	props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

	consumer = new KafkaConsumer<>(props);
	
	while (running) {

	    List<PartitionInfo> partitions = partitions(topic.get());

	    for (PartitionInfo partition : partitions) {

		preparePoll(topic.get(), partition, Integer.parseInt(maxMessages.get()));

		GSLoggerFactory.getLogger(getClass()).debug("Polling STARTED");

		ConsumerRecords<byte[], byte[]> records = poll();

		GSLoggerFactory.getLogger(getClass()).debug("Polling ENDED");

		if (records.isEmpty()) {

		    GSLoggerFactory.getLogger(getClass()).trace("No records found");

		    continue;
		}

		List<Map<String, Object>> rawDecodedMessages = new ArrayList<>();

		for (ConsumerRecord<byte[], byte[]> msg : records) {

		    byte[] raw = msg.value();

		    if (raw == null) {

			continue;
		    }

		    String text = new String(raw);

		    if (useTestFilter && !text.contains(testFilter.get())) {

			continue;
		    }

		    rawDecodedMessages.addAll(decode(msg, schemaRegistryURL.get(), token));
		}

		rawDecodedMessages.sort((a, b) -> Long.compare((Long) b.get("timestamp"), (Long) a.get("timestamp")));

		publish(MessageChannel.MessageLevel.INFO, "Total matching messages: " + rawDecodedMessages.size());

		if (rawDecodedMessages.isEmpty()) {

		    commit();

		} else {

		    List<DecodedMessage> list = rawDecodedMessages.stream(). //

			    map(msg -> DecodedMessage.of(this, mapper, msg)).//
			    filter(Objects::nonNull).//
			    filter(isDataHUBMessage(recordsFilter.get())). //
			    toList();

		    StreamUtils.asynchConsume(list, this::process, executor);//

		    commit();
		}
	    }
	}
    }

    @Override
    public void stop() {

	if (consumer != null) {

	    synchronized (consumer) {

		consumer.close();
	    }
	}

	if (executor != null) {

	    executor.close();
	}

	running = false;
    }

    /**
     * Filters in only messages related to the DataHUB
     *
     * @return
     */
    private Predicate<DecodedMessage> isDataHUBMessage(String recordsFilter) {

	return (msg) -> {

	    Optional<JSONObject> optValue = msg.optAspectValue();

	    if (optValue.isPresent()) {

		JSONObject aspectValue = optValue.get();

		JSONObject customProperties = aspectValue.optJSONObject("customProperties");

		if (customProperties != null) {

		    return customProperties.has(recordsFilter);
		}
	    }

	    return false;
	};
    }

    /**
     * @param decodedMessage
     */
    private void process(DecodedMessage decodedMessage) {

	String timeStamp = decodedMessage.timeStamp();
	String entityURN = decodedMessage.entityURN();
	ChangeType changeType = decodedMessage.type();

	try {

	    switch (changeType) {
	    case UPSERT -> {

		GSLoggerFactory.getLogger(getClass()).info("Handling UPSERT record: {}", entityURN);

		DatahubConnector connector = new DatahubConnector();

		String jsonEntity = connector.fetch(serviceUrl, token, entityURN);

		OriginalMetadata original = new OriginalMetadata();
		original.setMetadata(jsonEntity);

		DatahubMapper mapper = new DatahubMapper();

		GSSource source = new GSSource();
		source.setEndpoint(serviceUrl);
		source.setUniqueIdentifier(sourceId);
		source.setLabel(sourceLabel);

		GSResource resource = mapper.map(original, source);

		resource.setPrivateId(StringUtils.URLEncodeUTF8(resource.getOriginalId().get()));

		IndexedElementsWriter.write(resource);

		Document doc = resource.asDocument(true);

		boolean stored = targetFolder.store(entityURN, DatabaseFolder.FolderEntry.of(doc), DatabaseFolder.EntryType.GS_RESOURCE);

		if (!stored) {

		    boolean replaced = targetFolder.replace(entityURN, DatabaseFolder.FolderEntry.of(doc),
			    DatabaseFolder.EntryType.GS_RESOURCE);

		    if (replaced) {

			GSLoggerFactory.getLogger(getClass()).debug("Modified record: " + entityURN);

		    } else {

			error("Unable to add/replace record: " + entityURN);
		    }

		} else {

		    GSLoggerFactory.getLogger(getClass()).debug("New record: " + entityURN);
		}

	    }
	    case DELETE -> {

		GSLoggerFactory.getLogger(getClass()).info("Handling DELETE record: {}", entityURN);

		targetFolder.remove(entityURN);

	    }
	    }

	    commit();

	} catch (Exception e) {

	    error("Unable to handle record: " + e.getMessage(), e);
	}
    }

    /**
     * @param topic
     * @return
     */
    private List<PartitionInfo> partitions(String topic) {

	synchronized (consumer) {

	    return consumer.partitionsFor(topic);
	}
    }

    /**
     * @param topic
     * @param partition
     * @param maxMessages
     */
    private void preparePoll(String topic, PartitionInfo partition, int maxMessages) {

	synchronized (consumer) {

	    TopicPartition tp = new TopicPartition(topic, partition.partition());

	    consumer.assign(List.of(tp));

	    consumer.seekToEnd(List.of(tp));

	    long high = consumer.position(tp);

	    long start = Math.max(0, high - maxMessages);

	    consumer.seek(tp, start);
	}
    }

    /**
     * @return
     */
    private ConsumerRecords<byte[], byte[]> poll() {

	synchronized (consumer) {

	    return consumer.poll(Duration.ofMillis(Long.MAX_VALUE));
	}
    }

    /**
     *
     */
    private void commit() {

	synchronized (consumer) {

	    try {

		consumer.commitSync();

	    } catch (Exception e) {

		error("Unable to commit: " + e.getMessage(), e);
	    }
	}
    }

    /**
     * @author Fabrizio
     */
    private enum ChangeType {

	UPSERT, DELETE;
    }

    /**
     * @param timeStamp
     * @param entityURN
     * @author Fabrizio
     */
    private record DecodedMessage(//
	    String timeStamp,//
	    String entityURN, //
	    JSONObject aspect,//
	    ChangeType type) {

	/**
	 * @return
	 */
	public Optional<JSONObject> optAspectValue() {

	    return Optional.ofNullable(aspect);
	}

	/**
	 * @param service
	 * @param mapper
	 * @param decodedMessage
	 * @return
	 */
	static DecodedMessage of(DataHUBService service, ObjectMapper mapper, Map<String, Object> decodedMessage) {

	    try {

		Object data = decodedMessage.get("data");

		String json = mapper.writeValueAsString(data);

		JSONObject jsonObject = new JSONObject(json);

		JSONObject aspect = jsonObject.optJSONObject("aspect");

		JSONObject aspectValue = null;

		if (aspect != null) {

		    aspectValue = aspect.optJSONObject("value");
		}

		String timeStamp = ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds();
		String entityURN = jsonObject.optString("entityUrn", "missing entityURN");
		String changeType = jsonObject.optString("changeType", "missing changeType");

		service.publish(MessageChannel.MessageLevel.INFO, "Message: " + timeStamp + "/" + entityURN + "/" + changeType);

		return new DecodedMessage(timeStamp, entityURN, aspectValue, ChangeType.valueOf(changeType));

	    } catch (JsonProcessingException e) {

		service.publish(MessageChannel.MessageLevel.ERROR, "Error serializing data: " + e.getMessage());

		GSLoggerFactory.getLogger(DataHUBService.class).error(e);
	    }

	    return null;
	}
    }

    /**
     * @param msg
     * @param raw
     * @param schemaRegistryURL
     * @param token
     * @return
     */
    private List<Map<String, Object>> decode(ConsumerRecord<byte[], byte[]> msg, String schemaRegistryURL, String token) {

	List<Map<String, Object>> decodedMessages = new ArrayList<>();

	try {

	    GenericRecord record = deserialize(msg.value(), schemaRegistryURL, token);

	    String json = record.toString();
	    Map<String, Object> map = mapper.readValue(json, new TypeReference<Map<String, Object>>() {
	    });

	    if (map.containsKey("aspect")) {

		Object aspectObj = map.get("aspect");

		if (aspectObj instanceof Map) {

		    @SuppressWarnings("unchecked")
		    Map<String, Object> aspect = (Map<String, Object>) aspectObj;

		    Object value = aspect.get("value");

		    if (value instanceof byte[] bytes) {

			String str = new String(bytes);

			try {

			    aspect.put("value", mapper.readValue(str, Object.class));

			} catch (Exception e) {

			    aspect.put("value", str);
			}
		    }

		    if (value instanceof String str) {

			try {
			    aspect.put("value", mapper.readValue(str, Object.class));
			} catch (Exception ignored) {
			}
		    }
		}
	    }

	    Map<String, Object> output = new HashMap<>();

	    output.put("timestamp", msg.timestamp());
	    output.put("data", map);

	    decodedMessages.add(output);

	} catch (Exception e) {

	    error("Error decoding message:" + e.getMessage(), e);
	}

	return decodedMessages;
    }

    /**
     * @param tokenUrl
     * @param tokenUser
     * @param tokenPwd
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private String getAccessToken(String tokenUrl, String tokenUser, String tokenPwd) throws IOException, InterruptedException {

	HttpResponse<String> response;

	try (HttpClient client = HttpClient.newHttpClient()) {

	    Map<String, Object> payload = new HashMap<>();
	    payload.put("user", tokenUser);
	    payload.put("psw", tokenPwd);
	    payload.put("app_to_use", "TUTTE");

	    String body = mapper.writeValueAsString(payload);

	    tokenUrl += "/ext-login";

	    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(tokenUrl)).header("Content-Type", "application/json")
		    .POST(HttpRequest.BodyPublishers.ofString(body)).build();

	    response = client.send(request, HttpResponse.BodyHandlers.ofString());
	}

	JsonNode json = mapper.readTree(response.body());

	return json.get("access_token").asText();

    }

    /**
     * @param schemaRegistryUrl
     * @param schemaId
     * @param token
     * @return
     * @throws Exception
     */
    private Schema getSchema(String schemaRegistryUrl, int schemaId, String token) throws Exception {

	if (!schemaCache.containsKey(schemaId)) {

	    HttpClient client = HttpClient.newHttpClient();

	    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(schemaRegistryUrl + "/schemas/ids/" + schemaId))
		    .header("Authorization", "Bearer " + token).GET().build();

	    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

	    JsonNode json = mapper.readTree(response.body());

	    String schemaStr = json.get("schema").asText();

	    Schema schema = new Schema.Parser().parse(schemaStr);

	    schemaCache.put(schemaId, schema);
	}

	return schemaCache.get(schemaId);
    }

    /**
     * @param raw
     * @param schemaRegistryUrl
     * @param token
     * @return
     * @throws Exception
     */
    private GenericRecord deserialize(byte[] raw, String schemaRegistryUrl, String token) throws Exception {

	ByteBuffer buffer = ByteBuffer.wrap(raw);

	buffer.get(); // magic byte
	int schemaId = buffer.getInt();

	Schema schema = getSchema(schemaRegistryUrl, schemaId, token);

	byte[] avroBytes = new byte[buffer.remaining()];
	buffer.get(avroBytes);

	BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(avroBytes, null);

	GenericDatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);

	return reader.read(null, decoder);
    }

    /**
     * @param optional
     * @return
     */
    private boolean check(Optional<String> optional) {

	return optional.isPresent() && !optional.get().isEmpty();
    }

    /**
     * @param message
     * @param ex
     */
    private void error(String message) {

	error(message, null);
    }

    /**
     * @param message
     * @param ex
     */
    private void error(String message, Exception ex) {

	publish(MessageChannel.MessageLevel.ERROR, message);

	if (ex != null) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	}

	running = false;
    }
}
