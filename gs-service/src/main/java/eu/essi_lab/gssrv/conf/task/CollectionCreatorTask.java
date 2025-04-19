package eu.essi_lab.gssrv.conf.task;

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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.json.JSONObject;
import org.quartz.JobExecutionContext;
import org.w3c.dom.Document;

import eu.essi_lab.access.augmenter.DataCacheAugmenter;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.HarvestingEmbeddedTask;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.gssrv.conf.task.collection.ParameterCollectionCreator;
import eu.essi_lab.gssrv.conf.task.collection.SourceCollectionCreator;
import eu.essi_lab.lib.mqtt.hive.MQTTPublisherHive;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.profiler.wis.WISUtils;

/**
 * @author boldrini
 */
public class CollectionCreatorTask extends AbstractCustomTask implements HarvestingEmbeddedTask {

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Collection creator task STARTED");

	Optional<String> taskOptions = readTaskOptions(context);

	if (!taskOptions.isPresent()) {

	    log(status, "Custom task options missing, unable to perform task");

	    return;
	}

	try {

	    SystemSetting systemSettings = ConfigurationWrapper.getSystemSettings();

	    Optional<Properties> keyValueOption = systemSettings.getKeyValueOptions();

	    if (keyValueOption.isPresent()) {

		String host = keyValueOption.get().getProperty("mqttBrokerHost");
		String port = keyValueOption.get().getProperty("mqttBrokerPort");
		String user = keyValueOption.get().getProperty("mqttBrokerUser");
		String pwd = keyValueOption.get().getProperty("mqttBrokerPwd");

		if (host == null || port == null || user == null || pwd == null) {

		    GSLoggerFactory.getLogger(getClass()).error("MQTT options not found!");

		} else {

		    client = new MQTTPublisherHive(host, Integer.valueOf(port), user, pwd);
		}
	    } else {

		GSLoggerFactory.getLogger(getClass()).error("Key-value pair options not found!");

	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(DataCacheAugmenter.class).error(e);
	    throw e;
	}

	String targetSourceIdentifiers = taskOptions.get().trim();

	String[] splits = targetSourceIdentifiers.split("\n");

	for (String split : splits) {

	    Optional<GSSource> targetSource = ConfigurationWrapper.//
		    getAllSources().//
		    stream().//
		    filter(s -> s.getUniqueIdentifier().equals(split)).//
		    findFirst();

	    if (!targetSource.isPresent()) {

		log(status, "No source found with the provided source identifier " + split + ". Unable to run task, exit");

	    } else {

		run(targetSource.get().getUniqueIdentifier());
	    }
	}
	
	log(status, "Collection creator task ENDED");
    }

    /**
     * @param targetSourceIdentifier
     */
    public void run(String sourceId) throws Exception {

	StorageInfo databaseURI = ConfigurationWrapper.getStorageInfo();
	run(sourceId, databaseURI);
    }

    /**
     * @param targetSourceIdentifier
     */
    public void run(String sourceId, StorageInfo databaseURI) throws Exception {

	SourceStorage sourceStorage = DatabaseProviderFactory.getSourceStorage(databaseURI);

	DatabaseFolder folder = sourceStorage.getDataFolder(sourceId, true).get();

	List<DatasetCollection> datasets = new SourceCollectionCreator().getCollections(sourceId);

	List<DatasetCollection> children = new ParameterCollectionCreator().getCollections(sourceId);

	datasets.addAll(children);

	Optional<String> optionalView = Optional.of("gs-view-source(" + sourceId + ")");
	ResultSet<GSResource> resultSet = WISUtils.getMetadataItems(null, optionalView);
	HashSet<String> toDelete = new HashSet();
	for (GSResource result : resultSet.getResultsList()) {
	    toDelete.add(result.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getFileIdentifier());
	}

	for (DatasetCollection dataset : datasets) {

	    Optional<String> topic = dataset.getExtensionHandler().getWISTopicHierarchy();
	    if (topic.isPresent()) {
		JSONObject feature = WISUtils.mapFeature(dataset);
		String msg = feature.toString();
		if (client == null) {
		    GSLoggerFactory.getLogger(getClass()).info("MQTT broker not configured");
		} else {
		    client.publish(topic.get(), msg, true);
		}
	    } else {
		GSLoggerFactory.getLogger(getClass()).error("Topic not present!");
	    }

	    Document asDocument = dataset.asDocument(true);
	    String key = dataset.getOriginalId().get();
	    if (folder.exists(key)) {
		toDelete.remove(key);
		folder.replace(key, FolderEntry.of(asDocument), EntryType.GS_RESOURCE);
	    } else {
		folder.store(key, FolderEntry.of(asDocument), EntryType.GS_RESOURCE);
	    }

	}

	for (String id : toDelete) {
	    folder.remove(id);
	}

    }

    private static MQTTPublisherHive client;

    @Override
    public String getName() {

	return "Collection creator task";
    }

    @Override
    public ExecutionStage getExecutionStage() {

	return ExecutionStage.AFTER_HARVESTING_END;
    }

}
