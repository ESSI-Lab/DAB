/**
 * 
 */
package eu.essi_lab.gssrv.conf.task;

import java.util.ArrayList;
import java.util.Arrays;

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

import org.quartz.JobExecutionContext;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.Database.IdentifierType;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.HarvestingEmbeddedTask;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResourceSelector;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * This task must be embedded
 * 
 * @author Fabrizio
 */
public class ResourcesComparatorTask extends AbstractCustomTask implements HarvestingEmbeddedTask {

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Resources comparator task STARTED");

	Optional<String> taskOptions = readTaskOptions(context);

	if (!taskOptions.isPresent()) {

	    log(status, "Custom task options missing, unable to perform task");

	    return;
	}

	Database database = DatabaseFactory.get(ConfigurationWrapper.getStorageInfo());

	DatabaseFinder finder = DatabaseProviderFactory.getFinder(ConfigurationWrapper.getStorageInfo());

	GSSource gsSource = getSource().get();

	SourceStorageWorker worker = database.getWorker(gsSource.getUniqueIdentifier());

	DatabaseFolder data1Folder = null;
	DatabaseFolder data2Folder = null;

	if (worker.existsData1Folder()) {

	    data1Folder = worker.getData1Folder();
	}

	if (worker.existsData2Folder()) {

	    data2Folder = worker.getData2Folder();
	}

	if (data1Folder == null && data2Folder == null) {

	    GSLoggerFactory.getLogger(getClass()).error("Both data folders missing, exit!");
	}

	ArrayList<String> newRecords = new ArrayList<>();
	ArrayList<String> modifiedRecords = new ArrayList<>();
	ArrayList<String> deletedRecords = new ArrayList<>();

	HarvestingProperties properties = worker.getHarvestingProperties();

	//
	// first harvesting, only new records added
	//
	if (properties == null) {

	}

	//
	// incremental harvesting, only new records added
	//
	if (data1Folder != null && data2Folder == null) {

	    String fromDateStamp = properties.getStartHarvestingTimestamp();
	    String untilDateStamp = ISO8601DateTimeUtils.getISO8601DateTime();

	    long minTimeStamp = ISO8601DateTimeUtils.parseISO8601ToDate(fromDateStamp).get().getTime();
	    long maxTimeStamp = ISO8601DateTimeUtils.parseISO8601ToDate(untilDateStamp).get().getTime();

	    ResourcePropertyBond minTimeStampBond = BondFactory.createResourcePropertyBond(BondOperator.GREATER_OR_EQUAL,
		    ResourceProperty.RESOURCE_TIME_STAMP, String.valueOf(minTimeStamp));

	    ResourcePropertyBond maxTimeStampBond = BondFactory.createResourcePropertyBond(BondOperator.GREATER_OR_EQUAL,
		    ResourceProperty.RESOURCE_TIME_STAMP, String.valueOf(maxTimeStamp));

	    ResourcePropertyBond sourceIdBond = BondFactory.createSourceIdentifierBond(gsSource.getUniqueIdentifier());

	    LogicalBond andBond = BondFactory.createAndBond(minTimeStampBond, maxTimeStampBond, sourceIdBond);

	    DiscoveryMessage discoveryMessage = new DiscoveryMessage();
	    discoveryMessage.setExcludeResourceBinary(true);

	    ResourceSelector resourceSelector = new ResourceSelector();
	    resourceSelector.addIndex(MetadataElement.IDENTIFIER);

	    discoveryMessage.setResourceSelector(resourceSelector);
	    discoveryMessage.setSources(Arrays.asList(gsSource));
	    discoveryMessage.setUserBond(andBond);
	    discoveryMessage.setNormalizedBond(andBond);
	    discoveryMessage.setPermittedBond(andBond);
	    discoveryMessage.setIncludeDeleted(false);
	    discoveryMessage.setPage(new Page(0, Integer.MAX_VALUE));

	    ResultSet<GSResource> resultSet = finder.discover(discoveryMessage);
	    resultSet.getResultsList().forEach(res -> newRecords.add(res.getIndexesMetadata().read(MetadataElement.IDENTIFIER).get(0)));
	}

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
