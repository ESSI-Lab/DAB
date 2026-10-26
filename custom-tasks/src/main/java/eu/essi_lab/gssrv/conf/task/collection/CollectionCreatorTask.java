package eu.essi_lab.gssrv.conf.task.collection;

import java.nio.charset.StandardCharsets;
import java.util.EnumMap;

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

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.quartz.JobExecutionContext;
import org.w3c.dom.Document;

import eu.essi_lab.api.database.SourceStorageProvider;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.SourceStorageProvider;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractEmbeddedTask;
import eu.essi_lab.cfga.gs.task.OptionsKey;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.wis.WISUtils;

/**
 * @author boldrini
 */
public class CollectionCreatorTask extends AbstractEmbeddedTask {

    public enum CollectionCreatorTaskOptions implements OptionsKey {
	SOURCE_ID, VIEW_ID;
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Collection creator task STARTED");

	Optional<EnumMap<CollectionCreatorTaskOptions, String>> taskOptions = readTaskOptions(context, CollectionCreatorTaskOptions.class);
	if (taskOptions.isEmpty() || taskOptions.get().isEmpty()) {
	    GSLoggerFactory.getLogger(getClass())
		    .error("No options specified. Options should be new line separated and in the form key=value");
	    return;
	}

	String sourceId = taskOptions.get().get(CollectionCreatorTaskOptions.SOURCE_ID);
	if (sourceId == null) {
	    GSLoggerFactory.getLogger(getClass()).error("No source id option specified");
	    return;
	}

	String viewId = taskOptions.get().get(CollectionCreatorTaskOptions.VIEW_ID);
	if (viewId == null) {
	    viewId = WISUtils.getViewId().orElse(null);
	}
	if (viewId == null) {
	    GSLoggerFactory.getLogger(getClass()).error("No view id option specified, neither in the task nor in the WIS2 settings");
	    return;
	}

	Optional<View> view = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId);
	if (view.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).error("View not found");
	    return;
	}
	String hostname = WISUtils.getHostname();
	String centreId = WISUtils.getCentreId();
	String generatedBy = WISUtils.getGeneratedBy();

	String[] splits = sourceId.split(";");

	for (String split : splits) {

	    Optional<GSSource> targetSource = ConfigurationWrapper.//
		    getAllSources().//
		    stream().//
		    filter(s -> s.getUniqueIdentifier().equals(split)).//
		    findFirst();

	    if (!targetSource.isPresent()) {

		log(status, "No source found with the provided source identifier " + split + ". Unable to run task, exit");

	    } else {

		run(hostname, centreId, generatedBy, targetSource.get().getUniqueIdentifier(), view.get());
	    }
	}

	log(status, "Collection creator task ENDED");
    }

    /**
     * @param targetSourceIdentifier
     */
    public void run(String hostname, String centreId, String generatedBy, String sourceId, View view) throws Exception {

	StorageInfo databaseURI = ConfigurationWrapper.getStorageInfo();
	run(hostname, centreId, generatedBy, sourceId, databaseURI, view);
    }

    /**
     * @param targetSourceIdentifier
     */
    public void run(String hostname, String centreId, String generatedBy, String sourceId, StorageInfo databaseURI, View view) throws Exception {

	SourceStorageProvider sourceStorage = DatabaseProviderFactory.getSourceStorage(databaseURI);

	DatabaseFolder folder = sourceStorage.getDataFolder(sourceId, true).get();

	List<DatasetCollection> datasets = new SourceCollectionCreator(centreId).getCollections(sourceId, view.getSourceDeployment());

	// List<DatasetCollection> children = new ParameterCollectionCreator().getCollections(sourceId,
	// view.getSourceDeployment());
	//
	// datasets.addAll(children);

	GSLoggerFactory.getLogger(getClass()).info("Number of generated collections: {}", datasets.size());

	for (DatasetCollection dataset : datasets) {
	    System.out.println(dataset.getHarmonizedMetadata().getCoreMetadata().getTitle());
	}

	// the records of this source currently published, keyed as in the folder; those not generated again are deleted
	Optional<String> optionalView = Optional.of(view.getId());
	ResultSet<GSResource> resultSet = WISUtils.getMetadataItems(null, optionalView, Optional.of(sourceId));
	HashMap<String, GSResource> toDelete = new HashMap<>();
	for (GSResource result : resultSet.getResultsList()) {
	    Optional<String> originalId = result.getOriginalId();
	    if (originalId.isPresent()) {
		toDelete.put(originalId.get(), result);
	    }
	}

	for (DatasetCollection dataset : datasets) {

	    Optional<String> topic = dataset.getExtensionHandler().getWISTopicHierarchy();
	    if (topic.isPresent()) {
		IndexedElementsWriter.write(dataset);
		Document asDocument = dataset.asDocument(true);
		String key = dataset.getOriginalId().get();
		boolean alreadyPublished = folder.exists(key);
		toDelete.remove(key);
		if (alreadyPublished) {
		    folder.replace(key, FolderEntry.of(asDocument), EntryType.GS_RESOURCE);
		} else {
		    folder.store(key, FolderEntry.of(asDocument), EntryType.GS_RESOURCE);
		}

		// send WIS notification message
		String fileIdentifier = dataset.getHarmonizedMetadata().getCoreMetadata().getIdentifier();
		String wnmId = UUID.randomUUID().toString();
		GeographicBoundingBox bbox = dataset.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
		String dataId = fileIdentifier;

		String url = WISUtils.getRecordUrl(WISUtils.getServiceUrl(view.getId()), fileIdentifier);

		// the WCMP2 record is encoded from the stored document, the same way the item endpoint serves it, so that
		// the integrity checksum matches the content available at the canonical link
		String wcmp2 = WISUtils.getWCMP2Record(GSResource.create(asDocument), view.getId());

		// WIS2 GDC: "canonical" for a new record, "update" to have an already published record reingested
		Link link = new Link(alreadyPublished ? "update" : "canonical", "application/geo+json", url);
		link.setLength(wcmp2.getBytes(StandardCharsets.UTF_8).length);
		WISNotificationMessage wnm = new WISNotificationMessage(wnmId, bbox, dataId, link, generatedBy);
		wnm.setIntegrity(wcmp2);
		System.out.println(topic.get());
		System.out.println(wnm.getJSONObject().toString(3));
		System.out.println(wcmp2);
		WIS2Publisher.publish(topic.get(), wnm);

	    } else {
		GSLoggerFactory.getLogger(getClass()).error("Topic not present!");
	    }

	}
	for (String key : toDelete.keySet()) {

	    GSResource deleted = toDelete.get(key);

	    if (!folder.remove(key)) {
		GSLoggerFactory.getLogger(getClass()).warn("Unable to remove collection {}", key);
		continue;
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Removed collection {}", key);

	    // send WIS deletion notification message
	    Optional<String> topic = deleted.getExtensionHandler().getWISTopicHierarchy();
	    if (topic.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).error("Topic not present for removed collection {}", key);
		continue;
	    }

	    String fileIdentifier = deleted.getHarmonizedMetadata().getCoreMetadata().getIdentifier();
	    GeographicBoundingBox bbox = deleted.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
	    String url = WISUtils.getRecordUrl(WISUtils.getServiceUrl(view.getId()), fileIdentifier);

	    Link link = new Link("deletion", "application/geo+json", url);
	    WISNotificationMessage wnm = new WISNotificationMessage(UUID.randomUUID().toString(), bbox, fileIdentifier, link, generatedBy);
	    System.out.println(topic.get());
	    System.out.println(wnm.getJSONObject().toString(3));

	    WIS2Publisher.publish(topic.get(), wnm);
	}
    }

    @Override
    public String getName() {

	return "Collection creator task";
    }

    @Override
    public ExecutionStage getExecutionStage() {

	return ExecutionStage.AFTER_HARVESTING_END;
    }

}
