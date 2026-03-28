package eu.essi_lab.services.data_hub;

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
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.*;
import org.json.*;
import org.w3c.dom.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * @author Fabrizio
 */
public class DataHUBService extends AbstractManagedService {

    private static final String SCHEMA_REGISTRY_URL_KEY = "shemaRegistryURL";
    private static final String SERVICE_URL_KEY = "serviceURL";
    private static final String TOKEN_USER_KEY = "token.user";
    private static final String TOKEN_PWD_KEY = "token.pwd";
    private static final String KAFKA_BOOTSTRAP_SERVERS_KEY = "kafka.bootstrapServers";
    private static final String KAFKA_TOPIC_KEY = "kafka.topic";
    private static final String KAFKA_GROUP_ID_KEY = "kafka.groupId";
    private static final String KAFKA_USERNAME_KEY = "kafka.username";
    private static final String KAFKA_PASSWORD_KEY = "kafka.password";
    private static final String KAFKA_MAX_POLL_RECORDS_KEY = "kafka.maxPollRecords";

    private static final String KAFKA_MAX_PENDING_PROCESSES_KEY = "kafka.maxPendingProcesses";
    private static final String KAFKA_MAX_CONCURRENT_PROCESSES_KEY = "kafka.maxConcurrentProcesses";

    private static final String TEST_FILTER_KEY = "test.filter";
    private static final String TEST_FILTER_ENABLED_KEY = "test.filter.enabled";
    private static final String DATA_HUB_SOURCE_ID_KEY = "dataHub.sourceId";
    private static final String DATA_HUB_SOURCE_LABEL_KEY = "dataHub.sourceLabel";
    private static final String DATA_HUB_RECORDS_FILTER_KEY = "dataHub.recordsFilter";

    /**
     * Concurrency control
     */
    private static final int DEFAULT_MAX_CONCURRENT_PROCESSES = 10;

    /**
     * Backpressure control
     */
    private static final int DEFAULT_MAX_PENDING_PROCESSES = 50;

    /**
     * Overrides default 500
     */
    private static final int DEFAULT_MAX_POLL_RECORDS = 100;

    /**
     *
     */
    private static final int MAX_STOP_WAIT_SECONDS = 30;


    private boolean running;
    /**
     *
     */
    private KafkaConsumer<byte[], byte[]> consumer;
    private String sourceId;
    private DatabaseFolder targetFolder;
    private String serviceUrl;
    private String token;
    private String sourceLabel;
    private ExecutorService executor;
    private OffsetTracker tracker;
    private Decoder decoder;

    /**
     *
     */
    public DataHUBService() {

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

	//
	// these properties control the
	//

	int maxPollRecords = getSetting(). //
		readKeyValue(KAFKA_MAX_POLL_RECORDS_KEY).//
		map(Integer::parseInt).//
		orElse(DEFAULT_MAX_POLL_RECORDS);//

	int maxConcurrentProcesses = getSetting(). //
		readKeyValue(KAFKA_MAX_CONCURRENT_PROCESSES_KEY).//
		map(Integer::parseInt).//
		orElse(DEFAULT_MAX_CONCURRENT_PROCESSES);//

	int maxPendingProcesses = getSetting(). //
		readKeyValue(KAFKA_MAX_PENDING_PROCESSES_KEY).//
		map(Integer::parseInt).//
		orElse(DEFAULT_MAX_PENDING_PROCESSES);//

	//
	// init executor
	//

	ThreadFactory factory = Thread.ofPlatform().//
		name(getClass().getSimpleName()).//
		factory();

	executor = Executors.newFixedThreadPool(maxConcurrentProcesses, factory);

	//
	// get target folder
	//

	getTargetFolder().ifPresent(f -> targetFolder = f);

	//
	// get token
	//

	try {

	    token = String.valueOf(getAccessToken(serviceUrl, tokenUser.get(), tokenPwd.get()));

	} catch (Exception e) {

	    error("Unable to get access token: " + e.getMessage(), e);
	}

	if (!running) {

	    return;
	}

	//
	// init consumer
	//

	Properties props = new Properties();

	props.put("bootstrap.servers", bootstrapServers.get());
	props.put("group.id", groupId.get());
	props.put("max.poll.records", String.valueOf(maxPollRecords));

	props.put("enable.auto.commit", "false");
	props.put("auto.offset.reset", "latest");
	props.put("security.protocol", "SASL_SSL");
	props.put("sasl.mechanism", "SCRAM-SHA-512");

	props.put("sasl.jaas.config",
		"org.apache.kafka.common.security.scram.ScramLoginModule required " + "username=\"" + user.get() + "\" " + "password=\""
			+ pwd.get() + "\";");

	props.put("key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

	props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

	decoder = new Decoder(this);

	consumer = new KafkaConsumer<>(props);

	tracker = new OffsetTracker();

	consumer.subscribe(List.of(topic.get()), new ConsumerRebalanceListener() {

	    @Override
	    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {

		tracker.onPartitionAssigned(partitions);
	    }

	    @Override
	    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {

		commitSync();
	    }

	    @Override
	    public void onPartitionsLost(Collection<TopicPartition> partitions) {
	    }
	});

	Semaphore inFlight = new Semaphore(maxPendingProcesses); // backpressure

	while (running) {

	    GSLoggerFactory.getLogger(getClass()).info("Polling STARTED");

	    ConsumerRecords<byte[], byte[]> records = poll();

	    GSLoggerFactory.getLogger(getClass()).info("Polling ENDED");

	    GSLoggerFactory.getLogger(getClass()).info("Records found: {}", records.count());

	    for (ConsumerRecord<byte[], byte[]> record : records) {

		try {

		    inFlight.acquire();

		} catch (InterruptedException e) {
		    Thread.currentThread().interrupt();
		    return;
		}

		executor.submit(() -> {

		    try {

			byte[] raw = record.value();

			Optional<DecodedRecord> optRecord;

			if (raw != null) {

			    //			    optRecord = decoder.decode(record, schemaRegistryURL.get(), token). //
			    //				    map(rec -> DecodedRecord.of(this, decoder.getMapper(), rec)).//
			    //				    filter(test(recordsFilter.get()));

			    optRecord = decoder.decode(record, schemaRegistryURL.get(), token). //

				    map(rec -> DecodedRecord.of(DataHUBService.this, decoder.getMapper(), rec));

			} else {

			    optRecord = Optional.empty();
			}

			optRecord.ifPresent(this::fakeProcess);

		    } finally {

			tracker.markProcessed(record);

			inFlight.release();
		    }
		});
	    }
	}

	commitAsync();
    }

    @Override
    public void stop() {

	if (executor != null) {

	    executor.shutdown();

	    try {

		if (!executor.awaitTermination(MAX_STOP_WAIT_SECONDS, TimeUnit.SECONDS)) {

		    executor.shutdownNow();
		}
	    } catch (Exception e) {

		error("Unable to await executor termination: " + e.getMessage(), e);
	    }
	}

	if (consumer != null) {

	    synchronized (consumer) {

		try {

		    commitSync();

		} catch (Exception e) {

		    error("Unable to commit synch: " + e.getMessage(), e);

		} finally {

		    consumer.close();
		}
	    }
	}

	running = false;
    }

    /**
     * @return
     */
    private Optional<DatabaseFolder> getTargetFolder() {

	try {

	    Database database = DatabaseFactory.get(ConfigurationWrapper.getStorageInfo());

	    SourceStorageWorker worker = database.getWorker(sourceId);

	    boolean data1Folder = worker.existsData1Folder();
	    boolean data2Folder = worker.existsData2Folder();

	    if (data1Folder && data2Folder) {

		error("Both data1 and data2 folders exist");
		return Optional.empty();
	    }

	    if (!data1Folder && !data2Folder) {

		error("Both data1 and data2 folders missing");
		return Optional.empty();
	    }

	    return Optional.of(data1Folder ? worker.getData1Folder() : worker.getData2Folder());

	} catch (GSException e) {

	    error("Unable to get database instance: " + e.getMessage(), e);
	    return Optional.empty();
	}
    }

    /**
     * Filters in only messages related to the DataHUB
     *
     * @return
     */
    private Predicate<DecodedRecord> test(String recordsFilter) {

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
     * @param decodedRecord
     */
    private void fakeProcess(DecodedRecord decodedRecord) {

	String timeStamp = decodedRecord.timeStamp();
	String entityURN = decodedRecord.entityURN();
	ChangeType changeType = decodedRecord.type();

	try {

	    publish(MessageChannel.MessageLevel.INFO, timeStamp + "/" + entityURN + "/" + changeType);

	    GSLoggerFactory.getLogger(getClass()).info(timeStamp + "/" + entityURN + "/" + changeType);

	} catch (Exception e) {

	    error("Unable to process record: " + e.getMessage(), e, true);
	}
    }

    /**
     * @param decodedRecord
     */
    private void process(DecodedRecord decodedRecord) {

	String timeStamp = decodedRecord.timeStamp();
	String entityURN = decodedRecord.entityURN();
	ChangeType changeType = decodedRecord.type();

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

	} catch (Exception e) {

	    error("Unable to process record: " + e.getMessage(), e, true);
	}
    }

    /**
     * @return
     */
    private ConsumerRecords<byte[], byte[]> poll() {

	synchronized (consumer) {

	    return consumer.poll(Duration.ofMillis(TimeUnit.SECONDS.toMillis(1)));
	}
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
	ObjectMapper mapper = new ObjectMapper();

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
     *
     */
    private void commitAsync() {

	Map<TopicPartition, OffsetAndMetadata> offsets = tracker.buildOffsets();

	if (!offsets.isEmpty()) {

	    synchronized (consumer) {

		consumer.commitAsync(offsets, (offsets1, exception) -> {

		    if (exception != null) {

			error(exception.getMessage(), exception);
		    }
		});
	    }
	}
    }

    /**
     *
     */
    private void commitSync() {

	Map<TopicPartition, OffsetAndMetadata> offsets = tracker.buildOffsets();

	if (!offsets.isEmpty()) {

	    synchronized (consumer) {

		try {

		    consumer.commitSync(offsets);

		} catch (Exception e) {

		    error(e.getMessage(), e);
		}
	    }
	}
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
     * @param running
     */
    void error(String message, Exception ex, boolean running) {

	publish(MessageChannel.MessageLevel.ERROR, message);

	if (ex != null) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	}

	this.running = running;
    }

    /**
     * @param message
     * @param running
     */
    private void error(String message, boolean running) {

	error(message, null);
    }

    /**
     * @param message
     * @param ex
     */
    void error(String message, Exception ex) {

	error(message, ex, false);
    }

    /**
     * @param message
     * @param ex
     */
    private void error(String message) {

	error(message, null, true);
    }
}
