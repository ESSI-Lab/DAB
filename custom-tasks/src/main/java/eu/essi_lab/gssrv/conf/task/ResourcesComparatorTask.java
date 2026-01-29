/**
 *
 */
package eu.essi_lab.gssrv.conf.task;

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

import com.beust.jcommander.internal.Lists;
import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.Database.IdentifierType;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting.KeyValueOptionKeys;
import eu.essi_lab.cfga.gs.task.AbstractEmbeddedTask;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.kafka.client.KafkaClient;
import eu.essi_lab.lib.kafka.client.KafkaClient.SaslMechanism;
import eu.essi_lab.lib.mqtt.hive.MQTTPublisherHive;
import eu.essi_lab.lib.net.publisher.MessagePublisher;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.HarvestingStrategy;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.GSResourceComparator;
import eu.essi_lab.model.resource.GSResourceComparator.ComparisonResponse;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.json.JSONObject;
import org.quartz.JobExecutionContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This task must be embedded
 *
 * @author Fabrizio
 */
public class ResourcesComparatorTask extends AbstractEmbeddedTask {

    /**
     *
     */
    private static final int PAGE_SIZE = 1000;
    private List<String> newRecords;
    private List<String> deletedRecords;
    private DatabaseFolder data1Folder;
    private DatabaseFolder data2Folder;

    /**
     *
     */
    private static final List<Queryable> DEFAULT_COMPARISON_PROPERTIES = Lists.newArrayList();

    static {
	DEFAULT_COMPARISON_PROPERTIES.add(MetadataElement.TITLE);
	DEFAULT_COMPARISON_PROPERTIES.add(MetadataElement.ABSTRACT);
	DEFAULT_COMPARISON_PROPERTIES.add(MetadataElement.TEMP_EXTENT_BEGIN);
	DEFAULT_COMPARISON_PROPERTIES.add(MetadataElement.TEMP_EXTENT_END);
	DEFAULT_COMPARISON_PROPERTIES.add(MetadataElement.BOUNDING_BOX);
	DEFAULT_COMPARISON_PROPERTIES.add(MetadataElement.KEYWORD);
	DEFAULT_COMPARISON_PROPERTIES.add(MetadataElement.ONLINE_LINKAGE);
    }

    /**
     *
     */
    private List<Queryable> comparisonProperties = DEFAULT_COMPARISON_PROPERTIES;
    private Exception e;

    /**
     *
     */
    public ResourcesComparatorTask() {

	newRecords = Lists.newArrayList();
	deletedRecords = Lists.newArrayList();
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Resources comparator task STARTED");

	Optional<String> taskOptions = readTaskOptions(context);

	//
	// reading target comparison properties to override default ones
	//

	//
	// Supported values:
	//
	// Keyword
	// Topic category
	// Online link
	// Spatial extent
	// Title
	// Abstract
	// Distribution format
	// Temporal extent begin
	// Temporal extent end
	// Online protocol
	//
	taskOptions.ifPresent(s -> //
		comparisonProperties = Arrays.stream(s.trim().strip().split("\n")).//
		map(v -> MetadataElement.optFromReadableName(v.trim().strip()).orElse(null)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList()));

	GSLoggerFactory.getLogger(getClass()).info("Selected comparison properties: {}", //
		comparisonProperties.stream().//
			map(p -> p.getReadableName().orElse(p.getName())).//
			collect(Collectors.toList()));

	HashMap<String, List<String>> modifiedRecords = new HashMap<>();

	Database database = DatabaseFactory.get(ConfigurationWrapper.getStorageInfo());

	DatabaseFinder finder = DatabaseProviderFactory.getFinder(ConfigurationWrapper.getStorageInfo());

	GSSource gsSource = getSource().get();

	SourceStorageWorker worker = database.getWorker(gsSource.getUniqueIdentifier());

	Optional<Boolean> consFolderSurvives = worker.consolidatedFolderSurvives();

	if (consFolderSurvives.isEmpty() || !consFolderSurvives.get()) {

	    HarvestingStrategy strategy = worker.getStrategy();

	    //
	    //
	    //

	    if (worker.existsData1Folder()) {

		data1Folder = worker.getData1Folder();
	    }

	    if (worker.existsData2Folder()) {

		data2Folder = worker.getData2Folder();
	    }

	    if (data1Folder == null && data2Folder == null) {

		GSLoggerFactory.getLogger(getClass()).error("Both data folders missing, exit!");
		log(status, "Both data folders missing, exit!");

		return;
	    }

	    switch (strategy) {
	    case FULL:

		if (data1Folder != null && data2Folder == null) {

		    //
		    // first full/selective harvesting, only new records added in the data-1 folder
		    //

		    newRecords.addAll(data1Folder.listIdentifiers(IdentifierType.PUBLIC));

		} else {

		    //
		    // successive harvesting, comparing data-1 and data-2 folders
		    //

		    List<String> currIds = Lists.newArrayList();
		    List<String> prevIds = Lists.newArrayList();

		    if (worker.isData1WritingFolder()) {

			currIds.addAll(data1Folder.listIdentifiers(IdentifierType.PUBLIC));
			prevIds.addAll(data2Folder.listIdentifiers(IdentifierType.PUBLIC));

		    } else {

			currIds.addAll(data2Folder.listIdentifiers(IdentifierType.PUBLIC));
			prevIds.addAll(data1Folder.listIdentifiers(IdentifierType.PUBLIC));
		    }

		    deletedRecords = prevIds.//
			    stream().//
			    filter(id -> !currIds.contains(id)).//
			    collect(Collectors.toList());

		    newRecords = currIds.stream().//
			    filter(id -> !prevIds.contains(id)).//
			    collect(Collectors.toList());

		    List<String> commonIds = currIds.stream().//
			    filter(prevIds::contains).//
			    toList();

		    for (String id : commonIds) {

			try {

			    Optional<GSResource> opt1 = data1Folder.get(IdentifierType.PUBLIC, id);
			    Optional<GSResource> opt2 = data2Folder.get(IdentifierType.PUBLIC, id);

			    if (opt1.isEmpty()) {

				throw new Exception("Resource " + id + " not found in data-1 folder");
			    }

			    if (opt2.isEmpty()) {

				throw new Exception("Resource " + id + " not found in data-2 folder");
			    }

			    ComparisonResponse response = GSResourceComparator.compare(comparisonProperties, opt1.get(), opt2.get());

			    if (!response.getProperties().isEmpty()) {

				response.getProperties().forEach(prop -> {

				    List<String> list = modifiedRecords.computeIfAbsent(prop.getName(), k -> new ArrayList<>());

				    list.add(id);
				});
			    }

			} catch (Exception ex) {

			    GSLoggerFactory.getLogger(getClass()).error(ex);
			    throw ex;
			}
		    }
		}

		break;

	    case SELECTIVE:

		//
		// successive selective harvesting
		//

		//
		// 1) searching for new records. if the returned records are in the ListRecordsRequest modified list,
		// they will be removed
		// from the new records list
		//

		String startTimeStamp = worker.getStartTimeStamp();
		String untilDateStamp = ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds();

		ResourcePropertyBond minTimeStampBond = BondFactory.createResourcePropertyBond(BondOperator.GREATER_OR_EQUAL,
			ResourceProperty.RESOURCE_TIME_STAMP, String.valueOf(startTimeStamp));

		ResourcePropertyBond maxTimeStampBond = BondFactory.createResourcePropertyBond(BondOperator.LESS,
			ResourceProperty.RESOURCE_TIME_STAMP, String.valueOf(untilDateStamp));

		ResourcePropertyBond sourceIdBond = BondFactory.createSourceIdentifierBond(gsSource.getUniqueIdentifier());

		LogicalBond andBond = BondFactory.createAndBond(minTimeStampBond, maxTimeStampBond, sourceIdBond);

		DiscoveryMessage discoveryMessage = new DiscoveryMessage();
		discoveryMessage.setExcludeResourceBinary(true);
		discoveryMessage.setUseCachedSourcesDataFolderMap(false);

		ResourceSelector resourceSelector = new ResourceSelector();
		resourceSelector.addIndex(MetadataElement.IDENTIFIER);

		discoveryMessage.setResourceSelector(resourceSelector);
		discoveryMessage.setSources(List.of(gsSource));
		discoveryMessage.setUserBond(andBond);
		discoveryMessage.setNormalizedBond(andBond);
		discoveryMessage.setPermittedBond(andBond);
		discoveryMessage.setIncludeDeleted(false);
		discoveryMessage.setPage(new Page(1, PAGE_SIZE));

		ResultSet<GSResource> resultSet = finder.discover(discoveryMessage);
		resultSet.//
			getResultsList().//
			forEach(res -> newRecords.add(res.getIndexesMetadata().read(MetadataElement.IDENTIFIER.getName()).get(0)));

		//
		// 2) deleted records
		//

		getListRecordsRequest().getIncrementalDeletedResources().forEach(res -> deletedRecords.add(res.getOriginalId().get()));

		//
		// 3) modified records
		//

		//
		// these are the previous records, now replaced by the ones in the writing folder
		// with the same original ids
		//
		List<GSResource> modifiedResources = getListRecordsRequest().//
			getIncrementalModifiedResources().//
			stream().//
			// deleted records can be found also in the modified records list, they must be discarded
				filter(res -> !deletedRecords.contains(res.getOriginalId().get())).//
				filter(res -> res.getOriginalId().isPresent()).//
				collect(Collectors.toList());

		DatabaseFolder writingFolder = worker.getWritingFolder();

		for (GSResource modified : modifiedResources) {

		    // modified records are also new (because of the resource time stamp), so here they are removed from
		    // the new records list
		    newRecords.remove(modified.getPublicId());

		    GSResource incoming = writingFolder.get(IdentifierType.PUBLIC, modified.getPublicId()).get();

		    ComparisonResponse response = GSResourceComparator.compare(comparisonProperties, incoming, modified);

		    // at least one change is expected!
		    if (!response.getProperties().isEmpty()) {

			response.getProperties().forEach(prop -> {

			    List<String> list = modifiedRecords.computeIfAbsent(prop.getName(), k -> new ArrayList<>());

			    list.add(modified.getPublicId());
			});
		    }
		}
	    }
	} else {

	    log(status, "Consolidated folder survived, nothing is changed");
	}

	log(status, "New records: " + newRecords.size());
	log(status, "Modified records: " + modifiedRecords.values().stream().flatMap(Collection::stream).distinct().count());
	log(status, "Deleted records: " + deletedRecords.size());

	Optional<MessagePublisher> client = createClient();

	if (client.isPresent()) {

	    if (!newRecords.isEmpty()) {

		String topic = buildTopic(gsSource, "added", client.get());
		String message = buildMessage(newRecords, gsSource.getUniqueIdentifier(), "added", Optional.empty());

		client.get().publish(topic, message);
	    }

	    if (!deletedRecords.isEmpty()) {

		String topic = buildTopic(gsSource, "deleted", client.get());
		String message = buildMessage(deletedRecords, gsSource.getUniqueIdentifier(), "deleted", Optional.empty());

		client.get().publish(topic, message);
	    }

	    if (!modifiedRecords.isEmpty()) {

		for (String property : modifiedRecords.keySet()) {

		    List<String> idsList = modifiedRecords.get(property);

		    String topic = buildTopic(gsSource, property, true, client.get());
		    String message = buildMessage(idsList, gsSource.getUniqueIdentifier(), "modified", Optional.of(property));

		    client.get().publish(topic, message);
		}
	    }
	}

	log(status, "Resources comparator task ENDED");
    }

    @Override
    public String getName() {

	return "Resources comparator task";
    }

    /**
     * @param source
     * @param topic
     * @param client
     * @return
     */
    private String buildTopic(GSSource source, String topic, MessagePublisher client) {

	return buildTopic(source, topic, false, client);

    }

    /**
     * @param source
     * @param topic
     * @param modified
     * @param client
     * @return
     */
    private String buildTopic(GSSource source, String topic, boolean modified, MessagePublisher client) {

	if (client instanceof KafkaClient) {

	    topic = modified ? "modified_" + topic : topic;

	    return "dab_" + source.getUniqueIdentifier() + "_" + topic;
	}

	topic = modified ? "modified/" + topic : topic;

	return "dab/" + source.getUniqueIdentifier() + "/" + topic;
    }

    /**
     * @param list
     * @param sourceId
     * @param event
     * @param property
     * @return
     */
    private String buildMessage(List<String> list, String sourceId, String event, Optional<String> property) {

	return list.stream().map(id -> {

	    JSONObject object = new JSONObject();
	    object.put("metadataId", id);
	    object.put("sourceId", sourceId);
	    object.put("event", event); // added, modified, deleted
	    property.ifPresent(v -> object.put("property", v)); // title, abstract, ...

	    return object.toString(3);

	}).collect(Collectors.joining(",", "[", "]"));
    }

    /**
     * @return
     * @throws Exception
     */
    private Optional<MessagePublisher> createClient() throws Exception {

	try {

	    SystemSetting systemSettings = ConfigurationWrapper.getSystemSettings();

	    Optional<Properties> keyValueOption = systemSettings.getKeyValueOptions();

	    if (keyValueOption.isPresent()) {

		String mqttHost = keyValueOption.get().getProperty(KeyValueOptionKeys.MQTT_BROKER_HOST.getLabel());
		String mqttPort = keyValueOption.get().getProperty(KeyValueOptionKeys.MQTT_BROKER_PORT.getLabel());
		String mqttUser = keyValueOption.get().getProperty(KeyValueOptionKeys.MQTT_BROKER_USER.getLabel());
		String mqttPwd = keyValueOption.get().getProperty(KeyValueOptionKeys.MQTT_BROKER_PWD.getLabel());

		if (mqttHost == null || mqttPort == null || mqttUser == null || mqttPwd == null) {

		    GSLoggerFactory.getLogger(getClass()).warn("MQTT options not found!");

		} else {

		    GSLoggerFactory.getLogger(getClass()).info("MQTT client created");

		    return Optional.of(new MQTTPublisherHive(mqttHost, Integer.parseInt(mqttPort), mqttUser, mqttPwd));
		}

		String kafkaHost = keyValueOption.get().getProperty(KeyValueOptionKeys.KAFKA_BROKER_HOST.getLabel());
		String kafkaPort = keyValueOption.get().getProperty(KeyValueOptionKeys.KAFKA_BROKER_PORT.getLabel());

		if (kafkaHost == null || kafkaPort == null) {

		    GSLoggerFactory.getLogger(getClass()).warn("Kafka options not found!");

		} else {

		    GSLoggerFactory.getLogger(getClass()).info("Kafka client created");

		    KafkaClient client = new KafkaClient(kafkaHost, Integer.parseInt(kafkaPort));

		    //
		    // security
		    //

		    Optional<SecurityProtocol> securityProtocol = Optional.ofNullable(
				    keyValueOption.get().getProperty(KeyValueOptionKeys.KAFKA_BROKER_SECURITY_PROTOCOL.getLabel()))
			    .map(SecurityProtocol::forName);

		    Optional<SaslMechanism> saslMechanism = Optional.ofNullable(
				    keyValueOption.get().getProperty(KeyValueOptionKeys.KAFKA_BROKER_SASL_MECHANISM.getLabel()))
			    .flatMap(SaslMechanism::of);

		    Optional<String> kafkaUser = Optional.ofNullable(
			    keyValueOption.get().getProperty(KeyValueOptionKeys.KAFKA_BROKER_USER.getLabel()));

		    Optional<String> kafkaPwd = Optional.ofNullable(
			    keyValueOption.get().getProperty(KeyValueOptionKeys.KAFKA_BROKER_PWD.getLabel()));

		    if (securityProtocol.isPresent() && saslMechanism.isPresent() && kafkaUser.isPresent() && kafkaPwd.isPresent()) {

			client.setSecurity(securityProtocol.get(), saslMechanism.get(), kafkaUser.get(), kafkaPwd.get());
		    }

		    //
		    // request timeout
		    //

		    Optional<String> reqTimeout = Optional.ofNullable(
			    keyValueOption.get().getProperty(KeyValueOptionKeys.KAFKA_BROKER_REQUEST_TIMEOUT.getLabel()));

		    reqTimeout.ifPresent(timeout -> client.setRequestTimeoutMls(Integer.parseInt(timeout)));

		    //
		    //
		    //

		    return Optional.of(client);

		}

	    } else {

		GSLoggerFactory.getLogger(getClass()).warn("Key-value pair options not found!");
	    }

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(ResourcesComparatorTask.class).error(e);

	    throw ex;
	}

	return Optional.empty();
    }

    @Override
    public ExecutionStage getExecutionStage() {

	return ExecutionStage.BEFORE_HARVESTING_END;
    }

}
