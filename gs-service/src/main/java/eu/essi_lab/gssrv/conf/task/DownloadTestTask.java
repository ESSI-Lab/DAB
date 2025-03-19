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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;

import org.quartz.JobExecutionContext;

import eu.essi_lab.access.augmenter.AccessAugmenter;
import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.augmenter.ResourceAugmenter;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.RequestMessage.IterationMode;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ResourceProperty;
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

	Optional<String> taskOptions = readTaskOptions(context);

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
	StorageInfo uri = ConfigurationWrapper.getStorageInfo();
	discoveryMessage.setDataBaseURI(uri);

	Optional<View> view = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId);
	WebRequestTransformer.setView(view.get().getId(), ConfigurationWrapper.getStorageInfo(), discoveryMessage);
	discoveryMessage.setUserBond(BondFactory.createResourcePropertyBond(BondOperator.EQUAL, ResourceProperty.IS_DOWNLOADABLE, "true"));
	discoveryMessage.setDistinctValuesElement(ResourceProperty.SOURCE_ID);
	ResultSet<GSResource> resultSet = executor.retrieve(discoveryMessage);
	List<GSResource> resources = resultSet.getResultsList();

	boolean accessAugmenter = false;

	ResourceAugmenter augmenter;

	if (accessAugmenter) {
	    augmenter = new AccessAugmenter();
	} else {
	    augmenter = new StationPortalAugmenter();
	    ((StationPortalAugmenter) augmenter).setView(viewId);
	}

	DatabaseWriter writer = DatabaseProviderFactory.getWriter(ConfigurationWrapper.getStorageInfo());

	GSLoggerFactory.getLogger(getClass()).info("Found {} sources", resources.size());

	Optional<S3TransferWrapper> optS3TransferManager = getS3TransferManager();

	for (int i = 0; i < resources.size(); i++) {

	    GSResource resource = resources.get(i);

	    String sourceId = resource.getSource().getUniqueIdentifier();
	    GSLoggerFactory.getLogger(getClass()).info("At source {} ({})of {} sources. Resource id: {}", (i + 1),
		    resource.getSource().getLabel(), resources.size(), resource.getPublicId());
	    Optional<GSResource> augmented = augmenter.augment(resource);
	    String result = "bad";
	    if (augmented.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).error("Was not able to augment");
	    } else {
		GSResource a = augmented.get();
		Optional<String> lastDownload = a.getPropertyHandler().getLastDownloadDate();
		Optional<String> lastFailedDownload = a.getPropertyHandler().getLastFailedDownloadDate();
		if (lastDownload.isEmpty()) {
		    GSLoggerFactory.getLogger(getClass()).error("Was not able to download");
		} else {
		    Date downloadDate = ISO8601DateTimeUtils.parseISO8601ToDate(lastDownload.get()).get();
		    Date failedDate = null;
		    if (lastFailedDownload.isPresent()) {
			failedDate = ISO8601DateTimeUtils.parseISO8601ToDate(lastFailedDownload.get()).get();
		    }
		    if (failedDate == null || failedDate.before(downloadDate)) {
			result = "good";
			GSLoggerFactory.getLogger(getClass()).info("Was able to download resource from source {}",
				a.getSource().getUniqueIdentifier());
		    }

		}
		writer.update(augmented.get());
	    }

	    if (optS3TransferManager.isPresent()) {

		GSLoggerFactory.getLogger(getClass()).info("Transfer download stats to s3 STARTED");

		S3TransferWrapper manager = optS3TransferManager.get();
		manager.setACLPublicRead(true);

		File tempFile = File.createTempFile(getClass().getSimpleName() + sourceId + result, ".txt");

		String platformId = "unknown";
		if (resource.getExtensionHandler().getUniquePlatformIdentifier().isPresent()) {
		    platformId = resource.getExtensionHandler().getUniquePlatformIdentifier().get();
		}

		String text = ISO8601DateTimeUtils.getISO8601DateTime() + "\n" + platformId;

		Files.copy(IOStreamUtils.asStream(text), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

		manager.uploadFile(tempFile.getAbsolutePath(), "dabreporting", "download-availability/" + sourceId + "-" + result + ".txt");

		tempFile.delete();

		GSLoggerFactory.getLogger(getClass()).info("Transfer of data availability test to s3 ENDED");
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
