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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.quartz.JobExecutionContext;

import eu.essi_lab.access.augmenter.AccessAugmenter;
import eu.essi_lab.access.augmenter.DataCacheAugmenter;
import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory;
import eu.essi_lab.access.datacache.StatisticsRecord;
import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.augmenter.ResourceAugmenter;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.gssrv.conf.task.collection.SourceCollectionCreator.PropertyResult;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.TaskListExecutor;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.messages.RequestMessage.IterationMode;
import eu.essi_lab.messages.ResourceSelector.IndexesPolicy;
import eu.essi_lab.messages.ResourceSelector.ResourceSubset;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.rsm.access.AccessQueryUtils;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.request.executor.IDiscoveryExecutor;

/**
 * Tests the download from all the sources of a given view. For each source only
 * one resource is tested, then the resource is updated with lastDownloadDate
 * info.
 * 
 * @author boldrini
 */
public class DownloadTestTask extends AbstractCustomTask {

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Data download test task STARTED");

	// SETTINGS RETRIEVAL
	CustomTaskSetting taskSettings = retrieveSetting(context);

	Optional<String> taskOptions = taskSettings.getTaskOptions();

	String viewId = null;
	if (taskOptions.isPresent()) {
	    String options = taskOptions.get();
	    if (options != null) {
		viewId = options;
	    }
	}

	if (viewId == null) {

	    GSLoggerFactory.getLogger(getClass()).info("No view specified by download test task");

	    status.setPhase(JobPhase.CANCELED);
	    return;
	}

	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);
	IDiscoveryExecutor executor = loader.iterator().next();

	DiscoveryMessage discoveryMessage = new DiscoveryMessage();

	discoveryMessage.setRequestId(UUID.randomUUID().toString());

	discoveryMessage.setPage(new Page(1, 1000));
	discoveryMessage.setIteratedWorkflow(IterationMode.FULL_RESPONSE);
	discoveryMessage.setSources(ConfigurationWrapper.getHarvestedSources());
	StorageInfo uri = ConfigurationWrapper.getDatabaseURI();
	discoveryMessage.setDataBaseURI(uri);

	Optional<View> view = WebRequestTransformer.findView(ConfigurationWrapper.getDatabaseURI(), viewId);
	WebRequestTransformer.setView(view.get().getId(), ConfigurationWrapper.getDatabaseURI(), discoveryMessage);
	discoveryMessage.setUserBond(BondFactory.createResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.IS_DOWNLOADABLE, "true"));
	discoveryMessage.setDistinctValuesElement(ResourceProperty.SOURCE_ID);
	ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);
	List<GSResource> resources = resultSet.getResultsList();

	boolean accessAugmenter = false;
	
	ResourceAugmenter augmenter;
	
	if (accessAugmenter) {
	    augmenter = new AccessAugmenter();    
	}else {
	    augmenter = new StationPortalAugmenter();
	    ((StationPortalAugmenter)augmenter).setView(viewId); 
	}
	 

	DatabaseWriter writer = DatabaseProviderFactory.getWriter(ConfigurationWrapper.getDatabaseURI());

	
	
	for (int i = 0; i < resources.size(); i++) {

	    GSResource resource = resources.get(i);

	    Optional<GSResource> augmented = augmenter.augment(resource);

	    if (augmented.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).error("Was not able to augment");
	    } else {
		GSResource a = augmented.get();
		Optional<String> lastDownload = a.getPropertyHandler().getLastDownloadDate();
		if (lastDownload.isEmpty()) {
		    GSLoggerFactory.getLogger(getClass()).error("Was not able to download");
		} else {
		    GSLoggerFactory.getLogger(getClass()).info("Was able to download resource from source {}",
			    a.getSource().getUniqueIdentifier());

		}
		writer.update(augmented.get());
	    }

	    // CHECKING CANCELED JOB

	    if (ConfigurationWrapper.isJobCanceled(context)) {
		GSLoggerFactory.getLogger(getClass()).info("Data download test task CANCELED view id {} ", viewId);

		status.setPhase(JobPhase.CANCELED);
		return;
	    }

	}

	log(status, "Data download test task ENDED");
    }

    @Override
    public String getName() {

	return "Data download test task";
    }
}
