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

import java.util.List;
import java.util.Optional;

import org.quartz.JobExecutionContext;

import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;

/**
 * Tests source connectivity by executing a listRecords request using each
 * source harvested accessor.
 */
public class SourceConnectivityTestTask extends AbstractCustomTask {

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Source connectivity test task STARTED");

	Optional<String> taskOptions = readTaskOptions(context);
	String viewId = null;
	if (taskOptions.isPresent()) {
	    String options = taskOptions.get();
	    if (options != null) {
		viewId = options;
	    }
	}

	if (viewId == null) {
	    GSLoggerFactory.getLogger(getClass()).info("No view specified by source connectivity test task");
	    status.setPhase(JobPhase.CANCELED);
	    return;
	}

	Optional<View> optView = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId);
	if (optView.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).info("View '{}' not found for source connectivity test task", viewId);
	    status.setPhase(JobPhase.CANCELED);
	    return;
	}

	List<GSSource> viewSources = ConfigurationWrapper.getViewSources(optView.get());
	SourceStorage sourceStorage = DatabaseProviderFactory.getSourceStorage(ConfigurationWrapper.getStorageInfo());

	GSLoggerFactory.getLogger(getClass()).info("Testing connectivity of {} sources for view {}", viewSources.size(), viewId);

	for (GSSource source : viewSources) {
		GSLoggerFactory.getLogger(getClass()).info("Testing connectivity for source '{}'", source.getLabel());

	    boolean sourceUp = false;

	    try {
		Optional<HarvestingSetting> optHarvestingSetting = ConfigurationWrapper.getHarvestingSettings(source.getUniqueIdentifier());
		if (optHarvestingSetting.isPresent()) {
		    @SuppressWarnings("rawtypes")
		    IHarvestedAccessor accessor = AccessorFactory
			    .getConfiguredHarvestedAccessor(optHarvestingSetting.get().getSelectedAccessorSetting());

		    ListRecordsRequest request = new ListRecordsRequest();
		    request.setHarvestingProperties(sourceStorage.retrieveHarvestingProperties(source));

		    ListRecordsResponse<?> response = accessor.listRecords(request);
		    sourceUp = response != null && !response.getRecordsAsList().isEmpty();
		} else {
		    GSLoggerFactory.getLogger(getClass()).warn(
			    "No harvesting setting found for source '{}' ({}), storing sourceUp=0",
			    source.getLabel(),
			    source.getUniqueIdentifier());
		}
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error(
			"Connectivity test failed for source '{}' ({})",
			source.getLabel(),
			source.getUniqueIdentifier(),
			e);
	    }

	    HarvestingProperties properties = sourceStorage.retrieveHarvestingProperties(source);
	    properties.setSourceUp(sourceUp);
	    sourceStorage.storeHarvestingProperties(source, properties);

	    GSLoggerFactory.getLogger(getClass()).info("Source '{}' ({}) connectivity status: {}",
		    source.getLabel(),
		    source.getUniqueIdentifier(),
		    sourceUp ? 1 : 0);

	    if (ConfigurationWrapper.isJobCanceled(context)) {
		GSLoggerFactory.getLogger(getClass()).info("Source connectivity test task CANCELED view id {} ", viewId);
		status.setPhase(JobPhase.CANCELED);
		return;
	    }
	}

	log(status, "Source connectivity test task ENDED");
    }

    @Override
    public String getName() {
	return "Source connectivity test task";
    }
}
