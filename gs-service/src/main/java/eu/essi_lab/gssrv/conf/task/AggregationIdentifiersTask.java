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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.quartz.JobExecutionContext;
import org.w3c.dom.Document;

import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.scheduler.SchedulerUtils;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.harvester.worker.HarvestingSettingImpl;
import eu.essi_lab.indexes.IndexedElementsWriter;
import eu.essi_lab.indexes.IndexedMetadataElements;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.ResultsPriority;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.index.IndexedMetadataElement;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.request.executor.discover.ResourcePropertyConstraintAdder;

/**
 * @author Fabrizio
 */
public class AggregationIdentifiersTask extends AbstractCustomTask {

    /**
     * 
     */
    private static final int STEP = 50;

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Aggregation identifiers task STARTED");

	HarvestingSetting harvestingSetting = SettingUtils.downCast(SchedulerUtils.getSetting(context), HarvestingSettingImpl.class);

	Optional<CustomTaskSetting> customTaskSetting = harvestingSetting.getCustomTaskSetting();

	if (!customTaskSetting.isPresent()) {

	    log(status, "Custom task setting missing, unable to perform task");

	    return;
	}

	Optional<String> taskOptions = customTaskSetting.get().getTaskOptions();

	if (!taskOptions.isPresent()) {

	    log(status, "Required options not provided, unable to perform task");

	    return;
	}

	String targetSourceIdentifier = taskOptions.get();

	Optional<GSSource> targetSource = ConfigurationWrapper.//
		getAllSources().//
		stream().//
		filter(s -> s.getUniqueIdentifier().equals(targetSourceIdentifier)).//
		findFirst();

	if (!targetSource.isPresent()) {

	    log(status, "No source found with the provided source identifier " + targetSourceIdentifier + ". Unable to run task, exit");

	} else {

	    run(targetSource.get());
	}

	log(status, "Aggregation identifiers task ENDED");

    }

    /**
     * @param targetSourceIdentifier
     */
    private void run(GSSource targetSource) throws Exception {

	HashMap<String, List<String>> map = new HashMap<>();

	//
	//
	//

	StorageInfo databaseURI = ConfigurationWrapper.getStorageInfo();

	DatabaseReader dbReader = DatabaseProviderFactory.getReader(databaseURI);
	DatabaseFinder dbFinder = DatabaseProviderFactory.getFinder(databaseURI);

	SourceStorage sourceStorage = DatabaseProviderFactory.getSourceStorage(databaseURI);

	DatabaseFolder folder = sourceStorage.getDataFolder(targetSource.getUniqueIdentifier(), true).get();

	//
	// 1) Creates collection id to children ids map
	//

	DiscoveryMessage message = new DiscoveryMessage();
	message.setRequestId(UUID.randomUUID().toString());
	message.getResourceSelector().setIndexesPolicy(IndexesPolicy.NONE);
	message.getResourceSelector().setSubset(ResourceSubset.CORE);

	message.setSources(Arrays.asList(targetSource));
	message.setDataBaseURI(databaseURI);
	message.setResultsPriority(ResultsPriority.COLLECTION);

	ResourcePropertyConstraintAdder rpcah = new ResourcePropertyConstraintAdder(message);
	Bond bond = rpcah.addResourcePropertyConstraints(message.getRequestId(),
		BondFactory.createSourceIdentifierBond(targetSource.getUniqueIdentifier()), message.getResultsPriority());

	message.setNormalizedBond(bond);
	message.setPermittedBond(bond);
	message.setUserBond(bond);

	int collectionsCount = dbFinder.count(message).getCount();

	GSLoggerFactory.getLogger(getClass()).info("Collections count: " + collectionsCount);

	for (int i = 1; i < collectionsCount; i += STEP) {

	    message.setPage(new Page(i, i + STEP));

	    List<GSResource> collections = dbFinder.discover(message).getResultsList();

	    for (GSResource collection : collections) {

		MIMetadata miMetadata = collection.//
			getHarmonizedMetadata().//
			getCoreMetadata().//
			getMIMetadata();

		String collectionId = miMetadata.getFileIdentifier();

		XMLDocumentReader reader = new XMLDocumentReader(//
			miMetadata.asDocument(false));

		List<String> childrenIds = reader.evaluateTextContent(
			"//*:aggregationInfo/*:MD_AggregateInformation/*:aggregateDataSetIdentifier/*:MD_Identifier/*:code/*:CharacterString/text()");

		if (!childrenIds.isEmpty()) {

		    List<String> idsList = map.get(collectionId);
		    if (idsList == null) {
			idsList = new ArrayList<String>();
			map.put(collectionId, idsList);
		    }

		    idsList.addAll(childrenIds);
		}
	    }
	}

	//
	// 2)
	//

	message.setRequestId(UUID.randomUUID().toString());
	message.getResourceSelector().setIndexesPolicy(IndexesPolicy.ALL);
	message.getResourceSelector().setSubset(ResourceSubset.FULL);

	message.setResultsPriority(ResultsPriority.DATASET);

	rpcah = new ResourcePropertyConstraintAdder(message);
	Bond sourceIdBond = rpcah.addResourcePropertyConstraints(message.getRequestId(),
		BondFactory.createSourceIdentifierBond(targetSource.getUniqueIdentifier()), message.getResultsPriority());

	message.setNormalizedBond(sourceIdBond);
	message.setPermittedBond(sourceIdBond);
	message.setUserBond(sourceIdBond);
	DiscoveryCountResponse datasetCount = dbFinder.count(message);

	GSLoggerFactory.getLogger(getClass()).info("Dataset count: " + datasetCount.getCount());

	int collectionIndex = 0;

	ArrayList<String> missingDatasets = new ArrayList<>();

	for (String collectionId : map.keySet()) {

	    GSLoggerFactory.getLogger(getClass())
		    .info("Handling collection [" + (collectionIndex + 1) + "/" + collectionsCount + "] STARTED");

	    List<String> childrenIds = map.get(collectionId);

	    GSLoggerFactory.getLogger(getClass()).info("Collection childrens: " + childrenIds.size());

	    int childrenIndex = 0;

	    for (String id : childrenIds) {

		GSLoggerFactory.getLogger(getClass())
			.info("Handling child [" + (childrenIndex + 1) + "/" + childrenIds.size() + "] STARTED");

		SimpleValueBond idBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, id);

		LogicalBond andBond = BondFactory.createAndBond(Arrays.asList(sourceIdBond, idBond));

		message.setNormalizedBond(andBond);
		message.setPermittedBond(andBond);
		message.setUserBond(andBond);

		GSResource dataset = null;

		List<GSResource> resultsList = dbFinder.discover(message).getResultsList();

		if (resultsList.isEmpty()) {

		    GSLoggerFactory.getLogger(getClass()).info("Missing dataset: " + id);

		    missingDatasets.add(id);

		    GSLoggerFactory.getLogger(getClass())
			    .info("Handling child [" + (childrenIndex + 1) + "/" + childrenIds.size() + "] ENDED");

		    continue;

		} else {

		    dataset = resultsList.get(0);
		}

		dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().setParentIdentifier(collectionId);

		IndexedMetadataElement element = IndexedMetadataElements.//
			getIndexes().//
			stream().//
			filter(i -> i.getMetadataElement().isPresent() && i.getMetadataElement().get() == MetadataElement.PARENT_IDENTIFIER)
			.//
			findFirst().//
			get();

		IndexedElementsWriter.indexMetadataElements(dataset, Arrays.asList(element));

		//
		//
		//

		Document asDocument = dataset.asDocument(true);

		String key = dataset.getPrivateId();
		folder.replace(key, FolderEntry.of(asDocument), EntryType.GS_RESOURCE);

		GSLoggerFactory.getLogger(getClass()).info("Handling child [" + (childrenIndex + 1) + "/" + childrenIds.size() + "] ENDED");

		childrenIndex++;
	    }

	    GSLoggerFactory.getLogger(getClass())
		    .info("Handling collection [" + (collectionIndex + 1) + "/" + collectionsCount + "] ENDED");

	    collectionIndex++;
	}

	//
	//
	//

	if (!missingDatasets.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).info("Missing datasets count: " + missingDatasets.size());
	    GSLoggerFactory.getLogger(getClass()).info(missingDatasets.toString());

	    Optional<S3TransferWrapper> optS3TransferManager = getS3TransferManager();

	    if (optS3TransferManager.isPresent()) {

		GSLoggerFactory.getLogger(getClass()).info("Transfer of missing files log to s3 STARTED");

		S3TransferWrapper manager = optS3TransferManager.get();

		File tempFile = File.createTempFile("missingDatasets_" + targetSource.getUniqueIdentifier(), ".txt");

		String text = missingDatasets.toString().replace("[", "").replace("]", "").replace(",", "\n").replace(" ", "");

		Files.copy(IOStreamUtils.asStream(text), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

		manager.uploadFile(tempFile.getAbsolutePath(), "aggregationidslog", tempFile.getName());

		tempFile.delete();

		GSLoggerFactory.getLogger(getClass()).info("Transfer of missing files log to s3 ENDED");
	    }
	}
    }

    @Override
    public String getName() {

	return "Aggregation identifiers task";
    }

}
