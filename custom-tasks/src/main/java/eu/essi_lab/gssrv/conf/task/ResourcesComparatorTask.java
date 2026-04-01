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

import com.google.common.collect.*;
import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.Database.*;
import eu.essi_lab.api.database.factory.*;
import eu.essi_lab.api.database.opensearch.*;
import eu.essi_lab.api.database.opensearch.index.mappings.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.cfga.gs.setting.SystemSetting.*;
import eu.essi_lab.cfga.gs.task.*;
import eu.essi_lab.cfga.scheduler.*;
import eu.essi_lab.lib.kafka.client.*;
import eu.essi_lab.lib.kafka.client.KafkaPublisher.*;
import eu.essi_lab.lib.mqtt.hive.*;
import eu.essi_lab.lib.net.publisher.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.*;
import eu.essi_lab.messages.bond.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.resource.*;
import eu.essi_lab.model.resource.GSResourceComparator.*;
import org.apache.kafka.common.security.auth.*;
import org.json.*;
import org.opensearch.client.json.*;
import org.opensearch.client.opensearch.*;
import org.opensearch.client.opensearch._types.*;
import org.opensearch.client.opensearch._types.aggregations.*;
import org.opensearch.client.opensearch.core.*;
import org.quartz.*;

import java.util.*;
import java.util.stream.*;

/**
 * This task must be embedded
 *
 * @author Fabrizio
 */
public class ResourcesComparatorTask extends AbstractEmbeddedTask {

    /**
     * default page size of the discovery requests to get new records after selective harvesting
     */
    private static final int DEFAULT_DISCOVERY_PAGE_SIZE = 1000;

    /**
     * default maximum number of fields to aggregate in the comparison aggregation method
     */
    private static final int DEFAULT_MAX_AGGREGATION_FIELDS = 3;

    /**
     * default page size of aggregation requests in the comparison aggregation method
     */
    private static final int DEFAULT_AGGREGATION_PAGE_SIZE = 1000;

    /**
     * default max. number of values for each multi-value field that are included in the comparison aggregation method
     */
    private static final int DEFAULT_MAX_VALUES_PER_FIELD = 10;

    private List<String> newRecords;
    private List<String> deletedRecords;
    private DatabaseFolder data1Folder;
    private DatabaseFolder data2Folder;

    /**
     *
     */
    private static final List<String> DEFAULT_COMPARISON_PROPERTIES = new ArrayList<>();

    static {
	DEFAULT_COMPARISON_PROPERTIES.add(MetadataElement.TITLE.getName());
	DEFAULT_COMPARISON_PROPERTIES.add(MetadataElement.ABSTRACT.getName());
	DEFAULT_COMPARISON_PROPERTIES.add(MetadataElement.TEMP_EXTENT_BEGIN.getName());
	DEFAULT_COMPARISON_PROPERTIES.add(MetadataElement.TEMP_EXTENT_END.getName());
	DEFAULT_COMPARISON_PROPERTIES.add(MetadataElement.BOUNDING_BOX.getName());
	DEFAULT_COMPARISON_PROPERTIES.add(MetadataElement.KEYWORD.getName());
	DEFAULT_COMPARISON_PROPERTIES.add(MetadataElement.ONLINE_LINKAGE.getName());
    }

    /**
     *
     */
    public ResourcesComparatorTask() {

	newRecords = new ArrayList<>();
	deletedRecords = new ArrayList<>();
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Resources comparator task STARTED");

	int discoveryPageSize = DEFAULT_DISCOVERY_PAGE_SIZE;

	List<String> comparisonFields = DEFAULT_COMPARISON_PROPERTIES;

	int maxAggFields = DEFAULT_MAX_AGGREGATION_FIELDS;

	int aggregationPageSize = DEFAULT_AGGREGATION_PAGE_SIZE;

	int maxValuesPerField = DEFAULT_MAX_VALUES_PER_FIELD;

	Optional<String> taskOptions = readTaskOptions(context);

	if (taskOptions.isPresent()) {

	    Properties properties = new Properties();
	    properties.load(IOStreamUtils.asStream(taskOptions.get()));

	    String comparisonProp = properties.getProperty("comparisonProp", null);

	    if (comparisonProp != null) {

		comparisonFields = Arrays.asList(comparisonProp.split(","));
	    }

	    discoveryPageSize = Integer.parseInt(properties.getProperty("discoveryPageSize", String.valueOf(DEFAULT_DISCOVERY_PAGE_SIZE)));

	    maxAggFields = Integer.parseInt(properties.getProperty("maxAggFields", String.valueOf(DEFAULT_MAX_AGGREGATION_FIELDS)));

	    aggregationPageSize = Integer.parseInt(
		    properties.getProperty("aggregationPageSize", String.valueOf(DEFAULT_AGGREGATION_PAGE_SIZE)));

	    maxValuesPerField = Integer.parseInt(properties.getProperty("maxValuesPerField", String.valueOf(DEFAULT_MAX_VALUES_PER_FIELD)));
	}

	GSLoggerFactory.getLogger(getClass()).info("Selected comparison fields: {}", //
		comparisonFields);

	List<Queryable> queryables = comparisonFields.stream().//
		map(v -> (Queryable) MetadataElement.optFromName(v.trim().strip()).orElse(null)).//
		filter(Objects::nonNull).//
		toList();

	Map<String, List<String>> modifiedRecords = new HashMap<>();

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

		    List<String> currIds = new ArrayList<>();
		    List<String> prevIds = new ArrayList<>();

		    if (worker.isData1WritingFolder()) {

			currIds.addAll(data1Folder.listIdentifiers(IdentifierType.PUBLIC));
			prevIds.addAll(data2Folder.listIdentifiers(IdentifierType.PUBLIC));

		    } else {

			currIds.addAll(data2Folder.listIdentifiers(IdentifierType.PUBLIC));
			prevIds.addAll(data1Folder.listIdentifiers(IdentifierType.PUBLIC));
		    }

		    //
		    // deleted
		    //

		    deletedRecords = prevIds.//
			    parallelStream().//
			    filter(id -> !currIds.contains(id)).//
			    collect(Collectors.toList());
		    //
		    // new
		    //

		    newRecords = currIds.//
			    parallelStream().//
			    filter(id -> !prevIds.contains(id)).//
			    collect(Collectors.toList());

		    //
		    // common
		    //

		    comparisonFields = comparisonFields.stream().map(f -> {

			if (f.equals(MetadataElement.BOUNDING_BOX.getName())) {

			    return DataFolderMapping.toHashField(MetadataElement.BOUNDING_BOX.getName());
			}

			return f;

		    }).toList();

		    List<List<String>> partition = Lists.partition(comparisonFields, maxAggFields);

		    for (List<String> fields : partition) {

			Map<String, List<String>> result = compare(//
				((OpenSearchDatabase) finder.getDatabase()).getClient(), //
				fields, //
				gsSource.getUniqueIdentifier(),//
				aggregationPageSize, //
				maxValuesPerField);//

			result.keySet().forEach(key -> modifiedRecords.put(key, result.get(key)));
		    }
		}

		break;

	    case SELECTIVE:

		//
		// successive selective harvesting
		//

		//
		// 1) searching for new records. if the returned records are in the ListRecordsRequest modified list,
		// they will be removed from the new records list
		//

		Optional<SearchAfter> searchAfter = Optional.empty();

		do {

		    String startTimeStamp = worker.getStartTimeStamp();
		    String untilDateStamp = ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds();

		    ResourcePropertyBond minTimeStampBond = BondFactory.createResourcePropertyBond(BondOperator.GREATER_OR_EQUAL,
			    ResourceProperty.RESOURCE_TIME_STAMP, String.valueOf(startTimeStamp));

		    ResourcePropertyBond maxTimeStampBond = BondFactory.createResourcePropertyBond(BondOperator.LESS,
			    ResourceProperty.RESOURCE_TIME_STAMP, untilDateStamp);

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
		    discoveryMessage.setPage(new Page(1, discoveryPageSize));

		    searchAfter.ifPresent(discoveryMessage::setSearchAfter);

		    ResultSet<GSResource> resultSet = finder.discover(discoveryMessage);

		    resultSet.//
			    getResultsList().//
			    forEach(res -> newRecords.add(res.getIndexesMetadata().read(MetadataElement.IDENTIFIER.getName()).getFirst()));

		    searchAfter = resultSet.getSearchAfter();

		} while (searchAfter.isPresent());

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

		    ComparisonResponse response = GSResourceComparator.compare(queryables, incoming, modified);

		    // at least one change is expected!
		    if (!response.getProperties().isEmpty()) {

			for (Queryable prop : response.getProperties()) {

			    List<String> list = modifiedRecords.computeIfAbsent(prop.getName(), k -> new ArrayList<>());

			    list.add(modified.getPublicId());
			}
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

	if (client instanceof KafkaPublisher) {

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

		    KafkaPublisher client = new KafkaPublisher(kafkaHost, Integer.parseInt(kafkaPort));

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

	    GSLoggerFactory.getLogger(ResourcesComparatorTask.class).error(ex);

	    throw ex;
	}

	return Optional.empty();
    }

    @Override
    public ExecutionStage getExecutionStage() {

	return ExecutionStage.BEFORE_HARVESTING_END;
    }

    /**
     * @param client
     * @param targetFields
     * @param sourceId
     * @param aggregationPageSize
     * @return
     * @throws Exception
     */
    private Map<String, List<String>> compare( //
	    OpenSearchClient client, //
	    List<String> targetFields, //
	    String sourceId, //
	    int aggregationPageSize, //
	    int maxValuesPerField)//
	    throws Exception {

	Map<String, String> afterKey = null;

	Map<String, List<String>> out = new HashMap<>();

	targetFields.forEach(field -> out.put(field, new ArrayList<>()));

	while (true) {

	    Map<String, String> finalAfterKey = afterKey;

	    Map<String, Aggregation> subAggs = new HashMap<>();

	    for (String field : targetFields) {

		subAggs.put(field, //
			Aggregation.of(a -> a //
				.terms(t -> t.field(IndexMapping.toKeywordField("dataFolder")).size(2)) //
				.aggregations("values", // maximum n values for property
					v -> v.terms(tt -> tt //
						.field(field.equals(IndexMapping.toHashField(MetadataElement.BOUNDING_BOX.getName()))
							? field
							: IndexMapping.toKeywordField(field)) //
						.size(maxValuesPerField))))); //
	    }

	    SearchRequest.Builder builder = new SearchRequest.Builder();

	    SearchRequest request = builder.index(DataFolderMapping.get().getIndex()).size(0) //

		    .query(q -> q.term(t -> t.field(IndexMapping.toKeywordField("sourceId")).value(FieldValue.of(sourceId)))) //

		    .aggregations("by_fileId", a -> //

			    a.composite(c -> {

					c.size(aggregationPageSize);//

					CompositeAggregationSource fileIdSource = new CompositeAggregationSource.Builder()//

						.terms(t -> t.field(IndexMapping.toKeywordField("fileId"))).build();//

					//noinspection unchecked
					c.sources(Map.of("fileId", fileIdSource));//

					if (finalAfterKey != null) {

					    c.after(finalAfterKey);
					}

					return c;
				    })

				    .aggregations(subAggs)

		    ).build();

	    SearchResponse<Void> response = client.search(request, Void.class);

	    if (OpenSearchDatabase.debugQueries()) {

		JSONObject reqObject = OpenSearchUtils.toJSONObject(request);
		GSLoggerFactory.getLogger(getClass()).debug(reqObject.toString(3));

		JSONObject resObject = OpenSearchUtils.toJSONObject(response);
		GSLoggerFactory.getLogger(getClass()).debug(resObject.toString(3));
	    }

	    CompositeAggregate composite = response.aggregations().get("by_fileId").composite();

	    for (CompositeBucket bucket : composite.buckets().array()) {

		String fileId = bucket.key().get("fileId").toString();

		Map<String, Aggregate> aggs = bucket.aggregations();

		for (String field : targetFields) {

		    Aggregate agg = aggs.get(field);

		    if (agg == null || !agg.isSterms()) {

			continue;
		    }

		    StringTermsAggregate byFolder = agg.sterms();

		    Set<String> values1 = new HashSet<>();
		    Set<String> values2 = new HashSet<>();

		    for (StringTermsBucket folderBucket : byFolder.buckets().array()) {

			String folder = folderBucket.key();

			StringTermsAggregate values = folderBucket.aggregations().get("values").sterms();

			Set<String> targetSet = "data-1".equals(folder) ? values1 : values2;

			for (StringTermsBucket v : values.buckets().array()) {

			    targetSet.add(v.key());
			}
		    }

		    if (!values1.equals(values2)) {

			out.get(field).add(fileId);
		    }
		}
	    }

	    if (composite.buckets().array().size() < DEFAULT_DISCOVERY_PAGE_SIZE) {

		break;
	    }

	    Map<String, JsonData> rawAfterKey = composite.afterKey();

	    if (rawAfterKey != null) {

		afterKey = new HashMap<>();

		for (Map.Entry<String, JsonData> e : rawAfterKey.entrySet()) {

		    afterKey.put(e.getKey(), e.getValue().toString());
		}

	    } else {

		afterKey = null;
	    }

	    if (afterKey == null) {

		break;
	    }
	}

	return out;
    }

}
