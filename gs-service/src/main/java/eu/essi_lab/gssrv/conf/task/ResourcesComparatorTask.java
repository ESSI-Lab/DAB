/**
 * 
 */
package eu.essi_lab.gssrv.conf.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.quartz.JobExecutionContext;

import com.beust.jcommander.internal.Lists;

import eu.essi_lab.access.augmenter.DataCacheAugmenter;
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
import eu.essi_lab.lib.mqtt.hive.MQTTPublisherHive;
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

    private static final List<Queryable> COMPARISON_PROPERTIES = Lists.newArrayList();
    static {
	COMPARISON_PROPERTIES.add(MetadataElement.TITLE);
	COMPARISON_PROPERTIES.add(MetadataElement.ABSTRACT);
	COMPARISON_PROPERTIES.add(MetadataElement.TEMP_EXTENT_BEGIN);
	COMPARISON_PROPERTIES.add(MetadataElement.TEMP_EXTENT_END);
	COMPARISON_PROPERTIES.add(MetadataElement.BOUNDING_BOX);
	COMPARISON_PROPERTIES.add(MetadataElement.KEYWORD);
    }

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

	if (taskOptions.isPresent()) {

	    //
	    // reading target comparison properties to override default ones
	    //
	}

	HashMap<String, List<String>> modifiedRecords = new HashMap<>();

	Database database = DatabaseFactory.get(ConfigurationWrapper.getStorageInfo());

	DatabaseFinder finder = DatabaseProviderFactory.getFinder(ConfigurationWrapper.getStorageInfo());

	GSSource gsSource = getSource().get();

	SourceStorageWorker worker = database.getWorker(gsSource.getUniqueIdentifier());

	Optional<Boolean> consFolderSurvives = worker.consolidatedFolderSurvives();

	if (consFolderSurvives.isEmpty() || (consFolderSurvives.isPresent() && consFolderSurvives.get() == false)) {

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

		    data1Folder.listIdentifiers(IdentifierType.PUBLIC).forEach(id -> newRecords.add(id));

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
			    filter(id -> prevIds.contains(id)).//
			    collect(Collectors.toList());

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

			    ComparisonResponse response = GSResourceComparator.compare(COMPARISON_PROPERTIES, opt1.get(), opt2.get());

			    if (!response.getProperties().isEmpty()) {

				response.getProperties().forEach(prop -> {

				    List<String> list = modifiedRecords.get(prop.getName());
				    if (list == null) {
					list = new ArrayList<>();
					modifiedRecords.put(prop.getName(), list);
				    }

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
		discoveryMessage.setSources(Arrays.asList(gsSource));
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
		    newRecords.remove(modified.getOriginalId().get());

		    GSResource incoming = writingFolder.get(IdentifierType.PUBLIC, modified.getPublicId()).get();

		    ComparisonResponse response = GSResourceComparator.compare(COMPARISON_PROPERTIES, incoming, modified);

		    // at least one change is expected!
		    if (!response.getProperties().isEmpty()) {

			response.getProperties().forEach(prop -> {

			    List<String> list = modifiedRecords.get(prop.getName());
			    if (list == null) {
				list = new ArrayList<>();
				modifiedRecords.put(prop.getName(), list);
			    }

			    list.add(modified.getPublicId());
			});
		    }
		}
	    }
	} else

	{

	    log(status, "Consolidated folder survived, nothing is changed");
	}

	log(status, "New records: " + newRecords.size());
	log(status, "Modified records: " + modifiedRecords.values().stream().flatMap(l -> l.stream()).distinct().count());
	log(status, "Deleted records: " + deletedRecords.size());

	Optional<MQTTPublisherHive> client = createClient();

	if (client.isPresent()) {

	    if (!newRecords.isEmpty()) {

		String topic = buildTopic(gsSource, "added");
		String message = buildMessage(newRecords);

		client.get().publish(topic, message);
	    }

	    if (!deletedRecords.isEmpty()) {

		String topic = buildTopic(gsSource, "deleted");
		String message = buildMessage(deletedRecords);

		client.get().publish(topic, message);
	    }

	    if (!modifiedRecords.isEmpty()) {

		for (String property : modifiedRecords.keySet()) {

		    List<String> idsList = modifiedRecords.get(property);

		    String topic = buildTopic(gsSource, "modified/" + property);
		    String message = buildMessage(idsList);

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
     * @return
     */
    private String buildTopic(GSSource source, String topic) {

	return "dab/" + source.getUniqueIdentifier() + "/" + topic;
    }

    /**
     * @param list
     * @return
     */
    private String buildMessage(List<String> list) {

	return list.stream().map(id -> "\"" + id + "\"").collect(Collectors.joining(",", "[", "]"));
    }

    /**
     * @return
     * @throws Exception
     */
    private Optional<MQTTPublisherHive> createClient() throws Exception {

	try {

	    SystemSetting systemSettings = ConfigurationWrapper.getSystemSettings();

	    Optional<Properties> keyValueOption = systemSettings.getKeyValueOptions();

	    if (keyValueOption.isPresent()) {

		String host = keyValueOption.get().getProperty(KeyValueOptionKeys.MQTT_BROKER_HOST.getLabel());
		String port = keyValueOption.get().getProperty(KeyValueOptionKeys.MQTT_BROKER_PORT.getLabel());
		String user = keyValueOption.get().getProperty(KeyValueOptionKeys.MQTT_BROKER_USER.getLabel());
		String pwd = keyValueOption.get().getProperty(KeyValueOptionKeys.MQTT_BROKER_PWD.getLabel());

		if (host == null || port == null || user == null || pwd == null) {

		    GSLoggerFactory.getLogger(getClass()).warn("MQTT options not found!");

		} else {

		    return Optional.of(new MQTTPublisherHive(host, Integer.valueOf(port), user, pwd));
		}
	    } else {

		GSLoggerFactory.getLogger(getClass()).warn("Key-value pair options not found!");
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(DataCacheAugmenter.class).error(e);

	    throw e;
	}

	return Optional.empty();
    }

    @Override
    public ExecutionStage getExecutionStage() {

	return ExecutionStage.BEFORE_HARVESTING_END;
    }

}
