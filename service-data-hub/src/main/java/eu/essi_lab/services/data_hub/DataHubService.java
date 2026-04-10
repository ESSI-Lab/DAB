package eu.essi_lab.services.data_hub;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import com.fasterxml.jackson.databind.*;
import eu.essi_lab.accessor.datahub.*;
import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.factory.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.identifierdecorator.*;
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

import static eu.essi_lab.accessor.datahub.DatahubMapper.*;

/**
 * @author Fabrizio
 */
public class DataHubService extends AbstractManagedService {

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
    private static final String KAFKA_POLL_TIMEOUT_SECONDS_KEY = "kafka.pollTimeoutSeconds";

    private static final String KAFKA_MAX_PENDING_PROCESSES_KEY = "kafka.maxPendingProcesses";
    private static final String KAFKA_MAX_CONCURRENT_PROCESSES_KEY = "kafka.maxConcurrentProcesses";

    private static final String DATA_HUB_SOURCE_ID_KEY = "dataHub.sourceId";
    private static final String DATA_HUB_SOURCE_LABEL_KEY = "dataHub.sourceLabel";
    private static final String DATA_HUB_RECORDS_FILTER_KEY = "dataHub.recordsFilter";

    private static final String MAX_STOP_WAIT_SECONDS_KEY = "maxStopWaitSeconds";

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
    private static final int DEFAULT_MAX_STOP_WAIT_SECONDS = 30;

    /**
     *
     */
    private static final int DEFAULT_POLL_TIMEOUT_SECONDS = 1;

    private boolean running;
    /**
     *
     */
    private KafkaConsumer<byte[], byte[]> consumer;
    private String sourceId;
    private DatabaseFolder targetFolder;
    private String serviceUrl;
    private String sourceLabel;
    private ExecutorService executor;
    private OffsetTracker tracker;
    private Decoder decoder;
    private int maxStopWaitSeconds;
    private String tokenUser;
    private String tokenPwd;

    /**
     *
     */
    public DataHubService() {

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

	Optional<String> optTokenUser = getSetting().readKeyValue(TOKEN_USER_KEY);

	if (!(check(optTokenUser))) {

	    error("Missing token user name");

	} else {

	    tokenUser = optTokenUser.get();
	}

	Optional<String> optTokenPwd = getSetting().readKeyValue(TOKEN_PWD_KEY);

	if (!(check(optTokenPwd))) {

	    error("Missing token password");

	} else {

	    tokenPwd = optTokenPwd.get();
	}

	Optional<String> schemaRegistryURL = getSetting().readKeyValue(SCHEMA_REGISTRY_URL_KEY);

	if (!(check(schemaRegistryURL))) {

	    error("Missing schema registry url");
	}

	Optional<String> topic = getSetting().readKeyValue(KAFKA_TOPIC_KEY);

	if (!(check(topic))) {

	    error("Missing topic name");
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
	//
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

	int pollTimeoutSeconds = getSetting(). //
		readKeyValue(KAFKA_POLL_TIMEOUT_SECONDS_KEY).//
		map(Integer::parseInt).//
		orElse(DEFAULT_POLL_TIMEOUT_SECONDS);//

	maxStopWaitSeconds = getSetting(). //
		readKeyValue(MAX_STOP_WAIT_SECONDS_KEY).//
		map(Integer::parseInt).//
		orElse(DEFAULT_MAX_STOP_WAIT_SECONDS);

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

	    ConsumerRecords<byte[], byte[]> records = poll(pollTimeoutSeconds);

	    for (ConsumerRecord<byte[], byte[]> record : records) {

		try {

		    inFlight.acquire();

		} catch (InterruptedException e) {
		    Thread.currentThread().interrupt();
		    error("Loop thread interrupted: " + e.getMessage(), e);

		    return;
		}

		executor.submit(() -> {

		    try {

			byte[] raw = record.value();

			Optional<DecodedRecord> optRecord;

			if (raw != null) {

			    optRecord = decoder.decode(record, schemaRegistryURL.get()). //
				    map(rec -> DecodedRecord.of(this, decoder.getMapper(), rec)).//
				    filter(checkRecord(recordsFilter.get()));

			} else {

			    optRecord = Optional.empty();
			}

			optRecord.ifPresent(this::process);

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

		if (!executor.awaitTermination(maxStopWaitSeconds, TimeUnit.SECONDS)) {

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

	    SourceStorage sourceStorage = DatabaseProviderFactory.getSourceStorage(ConfigurationWrapper.getStorageInfo());

	    sourceStorage.harvestingStarted(getSource(), HarvestingStrategy.SELECTIVE, false, false);
	    sourceStorage.harvestingEnded(getSource(), HarvestingStrategy.SELECTIVE);

	    SourceStorageWorker worker = database.getWorker(sourceId);

	    boolean data1Folder = worker.existsData1Folder();
	    boolean data2Folder = worker.existsData2Folder();

	    if (data1Folder && data2Folder) {

		error("Both data-1 and data-2 folders exist");
		return Optional.empty();
	    }

	    if (!data1Folder) {

		error("data-1 folder missing");
		return Optional.empty();
	    }

	    return Optional.of(worker.getData1Folder());

	} catch (Exception e) {

	    error("Unable to retrieve target folder: " + e.getMessage(), e);

	    return Optional.empty();
	}
    }

    /**
     * Filters in only messages related to the DataHUB
     *
     * @return
     */
    private Predicate<DecodedRecord> checkRecord(String recordsFilter) {

	return (record) -> {

	    if (record.type() == ChangeType.DELETE) {

		return true;
	    }

	    Optional<JSONObject> optValue = record.optAspectValue();

	    if (optValue.isPresent()) {

		JSONObject aspectValue = optValue.get();

		JSONObject customProperties = aspectValue.optJSONObject("customProperties");

		if (customProperties != null) {

		    return customProperties.toString().contains(recordsFilter);
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

		GSLoggerFactory.getLogger(getClass()).info("Processing UPSERT record STARTED: {}", entityURN);

		DatahubConnector connector = new DatahubConnector();

		String token = getAccessToken();

		String jsonEntity = connector.fetch(serviceUrl, token, entityURN);

		OriginalMetadata original = new OriginalMetadata();
		original.setMetadata(jsonEntity);
		original.setSchemeURI(DATAHUB_NS_URI);

		DatahubMapper mapper = new DatahubMapper();

		GSResource resource = mapper.map(original, getSource());

		String originalId = IdentifierDecorator.generatePersistentIdentifier( //
			resource.getOriginalId().get(), //
			getSource().getUniqueIdentifier());//

		resource.setPrivateId(StringUtils.URLEncodeUTF8(originalId));
		resource.setOriginalId(originalId);
		resource.setPublicId(originalId);

		IndexedElementsWriter.write(resource);

		Document doc = resource.asDocument(true);

		DatabaseFolder.UpsertType upsertResult = targetFolder.upsert( //
			entityURN,  //
			DatabaseFolder.FolderEntry.of(doc),   //
			DatabaseFolder.EntryType.GS_RESOURCE);//

		publish(MessageChannel.MessageLevel.INFO, "Record: " + timeStamp + "/" + entityURN + "/" + upsertResult);

		GSLoggerFactory.getLogger(getClass()).info("Processing UPSERT record ENDED: {}/{}", entityURN, upsertResult);
	    }
	    case DELETE -> {

		GSLoggerFactory.getLogger(getClass()).info("Processing DELETE record STARTED: {}", entityURN);

		String decodedURN = StringUtils.URLEncodeUTF8(entityURN);

		boolean removed = targetFolder.remove(decodedURN);

		if (removed) {

		    publish(MessageChannel.MessageLevel.INFO, "Record: " + timeStamp + "/" + entityURN + "/REMOVED");

		} else {

		    publish(MessageChannel.MessageLevel.ERROR, "Unable to remove record: " + entityURN);
		}

		GSLoggerFactory.getLogger(getClass()).info("Processing DELETE record ENDED: {}", entityURN);
	    }
	    }

	} catch (Exception e) {

	    error("Unable to process record: " + e.getMessage(), e, true);
	}
    }

    /**
     * @return
     */
    private GSSource getSource() {

	GSSource source = new GSSource();
	source.setEndpoint(serviceUrl);
	source.setUniqueIdentifier(sourceId);
	source.setLabel(sourceLabel);

	return source;
    }

    /**
     * @return
     */
    private ConsumerRecords<byte[], byte[]> poll(int timeout) {

	synchronized (consumer) {

	    return consumer.poll(Duration.ofMillis(TimeUnit.SECONDS.toMillis(timeout)));
	}
    }

    /**
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    String getAccessToken() throws IOException, InterruptedException {

	HttpResponse<String> response;
	ObjectMapper mapper = new ObjectMapper();

	try (HttpClient client = HttpClient.newHttpClient()) {

	    Map<String, Object> payload = new HashMap<>();
	    payload.put("user", tokenUser);
	    payload.put("psw", tokenPwd);
	    payload.put("app_to_use", "TUTTE");

	    String body = mapper.writeValueAsString(payload);

	    String tokenUrl = serviceUrl + "/ext-login";

	    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(tokenUrl)).header("Content-Type", "application/json")
		    .POST(HttpRequest.BodyPublishers.ofString(body)).build();

	    response = client.send(request, HttpResponse.BodyHandlers.ofString());
	}

	JsonNode json = mapper.readTree(response.body());

	return json.get("access_token").asText();
    }

    /**
     *
     * @param user
     * @param pwd
     * @param url
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static String getAccessToken(String user, String pwd, String url) throws IOException, InterruptedException {

	HttpResponse<String> response;
	ObjectMapper mapper = new ObjectMapper();

	try (HttpClient client = HttpClient.newHttpClient()) {

	    Map<String, Object> payload = new HashMap<>();
	    payload.put("user", user);
	    payload.put("psw", pwd);
	    payload.put("app_to_use", "TUTTE");

	    String body = mapper.writeValueAsString(payload);

	    String tokenUrl = url + "/ext-login";

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

	} else {

	    GSLoggerFactory.getLogger(getClass()).error(message);
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
