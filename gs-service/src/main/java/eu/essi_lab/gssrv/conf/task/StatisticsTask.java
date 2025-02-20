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
import java.util.HashMap;
import java.util.Optional;

import org.quartz.JobExecutionContext;

import eu.essi_lab.access.availability.AvailabilityMonitor;
import eu.essi_lab.access.availability.DownloadInformation;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.profiler.semantic.SourceStatistics;
import eu.essi_lab.profiler.semantic.Stats;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * Gathers statistics info
 * 
 * @author boldrini
 */
public class StatisticsTask extends AbstractCustomTask {

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Statistics task STARTED");

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

	    GSLoggerFactory.getLogger(getClass()).info("No view specified by statistics task");

	    status.setPhase(JobPhase.CANCELED);
	    return;
	}

	GSLoggerFactory.getLogger(getClass()).info("Updating metrics for view {}", viewId);

	SourceStatistics sourceStats = null;
	try {
	    sourceStats = new SourceStatistics(null, Optional.of(viewId), ResourceProperty.SOURCE_ID);
	} catch (Exception e1) {
	    e1.printStackTrace();
	}
	HashMap<String, Stats> overallStats = sourceStats.getStatistics();
	HashMap<String, Integer> availability = new HashMap<String, Integer>();
	HashMap<String, Integer> datasets = new HashMap<String, Integer>();
	HashMap<String, Integer> platforms = new HashMap<String, Integer>();
	HashMap<String, Integer> variables = new HashMap<String, Integer>();

	HashMap<String, Double> coreMetadataCompleteness = new HashMap<String, Double>();
	HashMap<String, Double> fullMetadataCompleteness = new HashMap<String, Double>();
	GSLoggerFactory.getLogger(getClass()).info("source stats completed");

	PrometheusConfig config = PrometheusConfig.DEFAULT;
	PrometheusMeterRegistry registry = new PrometheusMeterRegistry(config);
	for (String source : overallStats.keySet()) {

	    GSSource s = ConfigurationWrapper.getSource(source);
	    Gauge.builder("source_info", () -> 1)//
		    .description("Metadata about each source.")//
		    .tags("source_id", source, "source_label", s.getLabel())//
		    .register(registry);
	    try {
		Stats stats = overallStats.get(source);

		DownloadInformation goodInfo = AvailabilityMonitor.getInstance().getLastDownloadDate(source);
		Date lastGoodDownload = goodInfo == null ? null : goodInfo.getDate();
		DownloadInformation badInfo = AvailabilityMonitor.getInstance().getLastDownloadDate(source);
		Date lastBadDownload = badInfo == null ? null : badInfo.getDate();

		Integer downloadAvailable = 0;
		if (lastGoodDownload != null) {
		    if (lastBadDownload == null || lastBadDownload.before(lastGoodDownload)) {
			downloadAvailable = 1;
		    }
		}
		availability.put(source, downloadAvailable);
		io.micrometer.core.instrument.Gauge.builder("download_availability", availability, g -> g.get(source))//
			.description("Download availability ")//
			.tag("source_id", source).//
			register(registry);

		datasets.put(source, Integer.parseInt(stats.getTimeSeriesCount()));
		io.micrometer.core.instrument.Gauge.builder("timeseries_total", datasets, g -> g.get(source))//
			.description("Total number of timeseries ")//
			.tag("source_id", source).//
			register(registry);

		platforms.put(source, Integer.parseInt(stats.getSiteCount()));
		io.micrometer.core.instrument.Gauge.builder("platforms_total", platforms, g -> g.get(source))//
			.description("Total number of platforms ")//
			.tag("source_id", source).//
			register(registry);

		variables.put(source, Integer.parseInt(stats.getAttributeCount()));
		io.micrometer.core.instrument.Gauge.builder("variables_total", variables, g -> g.get(source))//
			.description("Total number of variables ")//
			.tag("source_id", source).//
			register(registry);

		platforms.put(source, Integer.parseInt(stats.getSiteCount()));
		io.micrometer.core.instrument.Gauge.builder("platforms_total", platforms, g -> g.get(source))//
			.description("Total number of platforms ")//
			.tag("source_id", source).//
			register(registry);

		coreMetadataCompleteness.put(source, 95.);
		io.micrometer.core.instrument.Gauge.builder("core_metadata_completeness", coreMetadataCompleteness, g -> g.get(source))//
			.description("Core metadata availability percentage")//
			.tag("source_id", source).//
			register(registry);

		fullMetadataCompleteness.put(source, 70.);
		io.micrometer.core.instrument.Gauge.builder("full_metadata_completeness", fullMetadataCompleteness, g -> g.get(source))//
			.description("Full metadata availability percentage")//
			.tag("source_id", source).//
			register(registry);

		// String content = "<tr><td colspan='15'><br/>"//
		// + "Data provider: <b>" + source + "</b><br/>"//
		// + "#Platforms: " + stats.getSiteCount() + "<br/>"//
		// + "#Variables:" + stats.getAttributeCount() + "<br/>"//
		// + "#Timeseries:" + stats.getTimeSeriesCount() + "<br/>"//
		// + "Begin:" + stats.getBegin() + "<br/>"//
		// + "End:" + stats.getEnd() + "<br/>"//
		// + "BBOX(w,s,e,n): " + stats.getWest() + "," + stats.getSouth() + "," +
		// stats.getEast() +
		// ","
		// + stats.getNorth() + "<br/>" //
		// + "Altitude:" + stats.getMinimumAltitude() + "/" + stats.getMaximumAltitude() +
		// "<br/>"//
		// + "</td></tr>" + "" //
		// + "<tr>";
	    } catch (Exception e) {
		e.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }

	    if (ConfigurationWrapper.isJobCanceled(context)) {
		GSLoggerFactory.getLogger(getClass()).info("Statistics task CANCELED view id {} ", viewId);

		status.setPhase(JobPhase.CANCELED);
		return;
	    }

	}

	GSLoggerFactory.getLogger(getClass()).info("Updated metrics for view {}", viewId);

	Optional<S3TransferWrapper> optS3TransferManager = getS3TransferManager();

	if (optS3TransferManager.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).info("Transfer download stats to s3 STARTED");

	    S3TransferWrapper manager = optS3TransferManager.get();
	    manager.setACLPublicRead(true);

	    File tempFile = File.createTempFile(getClass().getSimpleName() + viewId, ".txt");

	    String text = registry.scrape();

	    Files.copy(IOStreamUtils.asStream(text), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

	    manager.uploadFile(tempFile.getAbsolutePath(), "dabreporting", "monitoring/" + viewId + ".txt");

	    tempFile.delete();

	    GSLoggerFactory.getLogger(getClass()).info("Transfer of statistics to s3 ENDED");
	}

	log(status, "Statistics task ENDED");
    }

    @Override
    public String getName() {

	return "Statistics task";
    }
}
