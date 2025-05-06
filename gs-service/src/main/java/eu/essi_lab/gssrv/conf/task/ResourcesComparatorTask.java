/**
 * 
 */
package eu.essi_lab.gssrv.conf.task;

import java.util.Arrays;
import java.util.List;

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

import java.util.Optional;
import java.util.stream.Collectors;

import org.quartz.JobExecutionContext;

import com.beust.jcommander.internal.Lists;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.Database.IdentifierType;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractEmbeddedTask;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
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
    {
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

	List<String> modifiedRecords = Lists.newArrayList();

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

		    data1Folder.listIdentifiers(IdentifierType.ORIGINAL).forEach(id -> newRecords.add(id));

		} else {

		    //
		    // successive harvesting, comparing data-1 and data-2 folders
		    //

		    List<String> currIds = Lists.newArrayList();
		    List<String> prevIds = Lists.newArrayList();

		    if (worker.isData1WritingFolder()) {

			currIds.addAll(data1Folder.listIdentifiers(IdentifierType.ORIGINAL));
			prevIds.addAll(data2Folder.listIdentifiers(IdentifierType.ORIGINAL));

		    } else {

			currIds.addAll(data2Folder.listIdentifiers(IdentifierType.ORIGINAL));
			prevIds.addAll(data1Folder.listIdentifiers(IdentifierType.ORIGINAL));
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

			    Optional<GSResource> opt1 = data1Folder.get(IdentifierType.ORIGINAL, id);
			    Optional<GSResource> opt2 = data2Folder.get(IdentifierType.ORIGINAL, id);

			    if (opt1.isEmpty()) {

				throw new Exception("Resource " + id + " not found in data-1 folder");
			    }

			    if (opt2.isEmpty()) {

				throw new Exception("Resource " + id + " not found in data-2 folder");
			    }

			    ComparisonResponse response = GSResourceComparator.compare(COMPARISON_PROPERTIES, opt1.get(), opt2.get());

			    if (!response.getProperties().isEmpty()) {

				modifiedRecords.add(id);
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
		resourceSelector.addIndex(ResourceProperty.ORIGINAL_ID);

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
			forEach(res -> newRecords.add(res.getIndexesMetadata().read(ResourceProperty.ORIGINAL_ID.getName()).get(0)));

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
			filter(res -> res.getOriginalId().isPresent()).//
			collect(Collectors.toList());

		DatabaseFolder writingFolder = worker.getWritingFolder();

		for (GSResource modified : modifiedResources) {

		    // modified records are also new (because of the resource time stamp), so here they are removed from
		    // the new records list
		    newRecords.remove(modified.getOriginalId().get());

		    GSResource incoming = writingFolder.get(IdentifierType.ORIGINAL, modified.getOriginalId().get()).get();

		    ComparisonResponse response = GSResourceComparator.compare(COMPARISON_PROPERTIES, incoming, modified);

		    // at least one change is expected!
		    if (!response.getProperties().isEmpty()) {

			modifiedRecords.add(modified.getOriginalId().get());
		    }
		}
	    }
	} else {

	    log(status, "Consolidated folder survived, nothing is changed");
	}

	log(status, "New records: " + newRecords.size());
	log(status, "Modified records: " + modifiedRecords.size());
	log(status, "Deleted records: " + deletedRecords.size());

	log(status, "Resources comparator task ENDED");
    }

    @Override
    public String getName() {

	return "Resources comparator task";
    }

    @Override
    public ExecutionStage getExecutionStage() {

	return ExecutionStage.BEFORE_HARVESTING_END;
    }

}
