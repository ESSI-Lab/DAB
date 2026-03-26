package eu.essi_lab.services.data_hub;

import com.fasterxml.jackson.databind.*;
import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.factory.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.services.impl.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.services.message.*;
import org.apache.avro.*;
import org.apache.avro.generic.*;
import org.apache.avro.io.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.*;
import org.json.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.*;
import java.time.*;
import java.util.*;
import java.util.function.*;

/**
 * @author Fabrizio
 */
public class DataHUBService extends AbstractManagedService {

    private Map<Integer, Schema> schemaCache;
    private ObjectMapper mapper;
    private boolean running;

    private static final String TOKEN_URL_KEY = "token.url";
    private static final String TOKEN_USER_KEY = "token.user";
    private static final String TOKEN_PWD_KEY = "token.pwd";

    private static final String SCHEMA_REGISTRY_URL_KEY = "shemaRegistryURL";

    private static final String BOOTSTRAP_SERVERS_KEY = "bootstrapServers";
    private static final String TOPIC_KEY = "topic";
    private static final String GROUP_ID_KEY = "groupId";

    private static final String KAFKA_USERNAME_KEY = "kafka.username";
    private static final String KAFKA_PASSWORD_KEY = "kafka.password";

    private static final String MAX_MESSAGES_KEY = "maxMessages";
    private static final String TEXT_FILTER_KEY = "textFilter";
    private static final String ENABLE_TEXT_FILTER_KEY = "enableTextFilter";

    private static final String SOURCE_ID_KEY = "dataHubSourceId";

    private static final List<String> TEST_IDS_ = List.of(
	    "urn:li:dataset:(urn:li:dataPlatform:metadata,abdam_pai_rischio_frana_uom_dx_sele_glt_gct,DEV)",//
	    "urn:li:dataset:(urn:li:dataPlatform:metadata,abdam_pai_rischio_frana_uom_dx_sele_glt_pzz,DEV)",//
	    "urn:li:dataset:(urn:li:dataPlatform:metadata,abdam_pai_rischio_frana_uom_dx_sele_glt_srg,DEV)");//

    /**
     *
     */
    private KafkaConsumer<byte[], byte[]> consumer;
    private String sourceId;

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

	Optional<String> bootstrapServers = getSetting().readKeyValue(BOOTSTRAP_SERVERS_KEY);

	if (bootstrapServers.isEmpty()) {

	    publish(MessageChannel.MessageLevel.ERROR, "Missing bootstrap server configuration");

	    running = false;
	}

	Optional<String> groupId = getSetting().readKeyValue(GROUP_ID_KEY);

	if (!(check(groupId))) {

	    publish(MessageChannel.MessageLevel.ERROR, "Missing group id");

	    running = false;
	}

	Optional<String> user = getSetting().readKeyValue(KAFKA_USERNAME_KEY);

	if (!(check(user))) {

	    publish(MessageChannel.MessageLevel.ERROR, "Missing user name");

	    running = false;
	}

	Optional<String> pwd = getSetting().readKeyValue(KAFKA_PASSWORD_KEY);

	if (!(check(pwd))) {

	    publish(MessageChannel.MessageLevel.ERROR, "Missing password");

	    running = false;
	}

	Optional<String> tokenUrl = getSetting().readKeyValue(TOKEN_URL_KEY);

	if (!(check(tokenUrl))) {

	    publish(MessageChannel.MessageLevel.ERROR, "Missing token url");

	    running = false;
	}

	Optional<String> tokenUser = getSetting().readKeyValue(TOKEN_USER_KEY);

	if (!(check(tokenUser))) {

	    publish(MessageChannel.MessageLevel.ERROR, "Missing token user name");

	    running = false;
	}

	Optional<String> tokenPwd = getSetting().readKeyValue(TOKEN_PWD_KEY);

	if (!(check(tokenPwd))) {

	    publish(MessageChannel.MessageLevel.ERROR, "Missing token password");

	    running = false;
	}

	Optional<String> schemaRegistryURL = getSetting().readKeyValue(SCHEMA_REGISTRY_URL_KEY);

	if (!(check(schemaRegistryURL))) {

	    publish(MessageChannel.MessageLevel.ERROR, "Missing schema registry url");

	    running = false;
	}

	Optional<String> topic = getSetting().readKeyValue(TOPIC_KEY);

	if (!(check(topic))) {

	    publish(MessageChannel.MessageLevel.ERROR, "Missing topic name");
	    running = false;
	}

	Optional<String> maxMessages = getSetting().readKeyValue(MAX_MESSAGES_KEY);

	if (!(check(maxMessages))) {

	    publish(MessageChannel.MessageLevel.ERROR, "Missing max messages");
	    running = false;
	}

	Optional<String> textFilter = getSetting().readKeyValue(TEXT_FILTER_KEY);

	if (!(check(textFilter))) {

	    publish(MessageChannel.MessageLevel.ERROR, "Missing text filter");

	    running = false;
	}

	Optional<String> optSourceId = getSetting().readKeyValue(SOURCE_ID_KEY);

	if (!(check(optSourceId))) {

	    publish(MessageChannel.MessageLevel.ERROR, "Missing source identifier");

	    running = false;

	} else {

	    sourceId = optSourceId.get();
	}

	boolean enableTextFilter = getSetting().readKeyValue(ENABLE_TEXT_FILTER_KEY).map(Boolean::parseBoolean).orElse(true);

	if (!running) {

	    return;
	}

	//
	// get token
	//

	String token = null;

	try {

	    token = String.valueOf(getAccessToken(tokenUrl.get(), tokenUser.get(), tokenPwd.get()));

	} catch (Exception e) {

	    publish(MessageChannel.MessageLevel.ERROR, "Unable to get access token: " + e.getMessage());

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    running = false;
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

	Database database = null;
	SourceStorageWorker worker = null;

	try {

	    database  = DatabaseFactory.get(ConfigurationWrapper.getStorageInfo());

	    worker = database.getWorker(sourceId);

	} catch (GSException e) {

	    publish(MessageChannel.MessageLevel.ERROR, "Unable to get database instance: " + e.getMessage());
	    GSLoggerFactory.getLogger(getClass()).error(e);

	    running = false;
	    return;
	}

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

		List<Map<String, Object>> rawMessages = new ArrayList<>();

		for (ConsumerRecord<byte[], byte[]> msg : records) {

		    byte[] raw = msg.value();

		    if (raw == null) {

			continue;
		    }

		    String text = new String(raw);

		    if (enableTextFilter && !text.contains(textFilter.get())) {

			continue;
		    }

		    rawMessages.addAll(decode(msg, raw, schemaRegistryURL.get(), token));
		}

		rawMessages.sort((a, b) -> Long.compare((Long) b.get("timestamp"), (Long) a.get("timestamp")));

		publish(MessageChannel.MessageLevel.INFO, "Total matching messages: " + rawMessages.size());

		rawMessages.stream(). //
			map(msg -> DecodedMessage.of(this, mapper, msg)).//
			filter(Objects::nonNull).//
			filter(messageFilter()).//
			forEach(this::handle);//
	    }
	}
    }

    /**
     * TO BE IMPLEMENTED: filter in only messages related to the DataHUB
     *
     * @return
     */
    private Predicate<DecodedMessage> messageFilter() {

	return (msg) -> true;
    }

    /**
     * TO BE IMPLEMENTED
     *
     * @param decodedMessage
     */
    private void handle(DecodedMessage decodedMessage) {

	String timeStamp = decodedMessage.timeStamp();
	String entityURN = decodedMessage.entityURN();
	ChangeType changeType = decodedMessage.type();

	switch (changeType) {
	case UPSERT -> {

	    GSLoggerFactory.getLogger(getClass()).debug("Upsert message");
	}
	case DELETE -> {

	    GSLoggerFactory.getLogger(getClass()).debug("Delete message");
	}
	}

	publish(MessageChannel.MessageLevel.INFO, "Message: " + timeStamp + "/" + entityURN + "/" + changeType);
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
	    ChangeType type) {

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

		String timeStamp = ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds();
		String entityURN = jsonObject.optString("entityUrn", "missing entityURN");
		String changeType = jsonObject.optString("changeType", "missing changeType");

		service.publish(MessageChannel.MessageLevel.INFO, "Message: " + timeStamp + "/" + entityURN + "/" + changeType);

		return new DecodedMessage(timeStamp, entityURN, ChangeType.valueOf(changeType));

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
    private List<Map<String, Object>> decode(ConsumerRecord<byte[], byte[]> msg, byte[] raw, String schemaRegistryURL, String token) {

	List<Map<String, Object>> decodedMessages = new ArrayList<>();

	try {

	    GenericRecord record = deserialize(raw, schemaRegistryURL, token);

	    String json = record.toString();
	    Map<String, Object> map = mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
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

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    publish(MessageChannel.MessageLevel.ERROR, "Error decoding message:" + e.getMessage());
	}

	return decodedMessages;
    }

    @Override
    public void stop() {

	if (consumer != null) {

	    synchronized (consumer) {

		consumer.close();
	    }
	}

	running = false;
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
}
