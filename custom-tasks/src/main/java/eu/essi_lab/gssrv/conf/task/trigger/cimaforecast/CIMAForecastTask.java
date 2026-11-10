package eu.essi_lab.gssrv.conf.task.trigger.cimaforecast;

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

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.OptionsKey;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.gssrv.conf.task.trigger.TaskReport;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.gssrv.conf.task.trigger.cimaforecast.CIMAForecastProcessor.DownloadMode;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import org.quartz.JobExecutionContext;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

/**
 * Publishes on S3 the COGs of the CIMA WRF forecasts of UTCI, AT, WBGT and WCT (see {@link CIMAForecastProcessor}).<br>
 * The repository credentials are read from the DAB credentials (cimaWrfUser, cimaWrfPassword).<br>
 * Task options (one per line, key=value):
 * <ul>
 * <li>BUCKET_NAME (required): target bucket</li>
 * <li>PREFIX: key prefix in the bucket, default "CIMA-FORECAST"</li>
 * <li>INDICES: comma separated list among utci, at, wbgt, wct; default all</li>
 * <li>RUN: run to process, 00 or 12; default the latest available run</li>
 * <li>RUN_DATE: day of the run (yyyy-MM-dd), default today (UTC); used with RUN</li>
 * <li>MASK_SEA: false to keep the sea pixels; default true</li>
 * <li>MAX_LEAD_HOUR: last forecast hour, 1-48; default 48 (the run time, hour 0, is never published)</li>
 * <li>DOWNLOAD_MODE: remote (HTTP range requests, with fallback to full) or full (whole file download); default remote</li>
 * </ul>
 * The task should be scheduled every 12 hours, after the publication of the runs (00 UTC run after 03:15 UTC, 12 UTC run after 15:15
 * UTC), e.g. at 05:30 and 17:30 UTC
 */
public class CIMAForecastTask extends AbstractCustomTask {

    public enum CIMAForecastTaskOptions implements OptionsKey {
	BUCKET_NAME, PREFIX, INDICES, RUN, RUN_DATE, MASK_SEA, MAX_LEAD_HOUR, DOWNLOAD_MODE;
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	Instant start = Instant.now();
	log(status, "CIMAForecastTask STARTED");
	TaskReport report = TaskReport.of(status);

	Optional<EnumMap<CIMAForecastTaskOptions, String>> taskOptions = readTaskOptions(context, CIMAForecastTaskOptions.class);
	if (taskOptions.isEmpty()) {
	    cancel(status, report, "No options specified");
	    return;
	}
	EnumMap<CIMAForecastTaskOptions, String> options = taskOptions.get();

	String bucketName = options.get(CIMAForecastTaskOptions.BUCKET_NAME);
	if (bucketName == null) {
	    cancel(status, report, "No bucket name option specified");
	    return;
	}

	Optional<String> user = ConfigurationWrapper.getCredentialsSetting().getCimaWrfUser();
	Optional<String> password = ConfigurationWrapper.getCredentialsSetting().getCimaWrfPassword();
	if (user.isEmpty() || password.isEmpty()) {
	    cancel(status, report, "CIMA WRF credentials (cimaWrfUser, cimaWrfPassword) not configured");
	    return;
	}

	List<String> indices = options.containsKey(CIMAForecastTaskOptions.INDICES) //
		? Arrays.stream(options.get(CIMAForecastTaskOptions.INDICES).split(",")).map(s -> s.trim().toLowerCase())
		.filter(CIMAForecastProcessor.INDICES::contains).toList() //
		: CIMAForecastProcessor.INDICES;
	if (indices.isEmpty()) {
	    cancel(status, report, "No valid index specified");
	    return;
	}

	Optional<S3TransferWrapper> manager = getS3TransferManager();
	if (manager.isEmpty()) {
	    cancel(status, report, "S3 manager not present");
	    return;
	}

	CIMAForecastProcessor processor = new CIMAForecastProcessor(user.get(), password.get(), manager.get(), bucketName,
		options.getOrDefault(CIMAForecastTaskOptions.PREFIX, "CIMA-FORECAST"), indices);

	if (options.containsKey(CIMAForecastTaskOptions.RUN) || options.containsKey(CIMAForecastTaskOptions.RUN_DATE)) {

	    LocalDate day = options.containsKey(CIMAForecastTaskOptions.RUN_DATE)
		    ? LocalDate.parse(options.get(CIMAForecastTaskOptions.RUN_DATE))
		    : LocalDate.now(ZoneOffset.UTC);
	    int hour = Integer.parseInt(options.getOrDefault(CIMAForecastTaskOptions.RUN, "00"));

	    Instant run = day.atTime(hour, 0).toInstant(ZoneOffset.UTC);
	    processor.setRun(run);
	}

	if (options.containsKey(CIMAForecastTaskOptions.MASK_SEA)) {
	    processor.setMaskSea(Boolean.parseBoolean(options.get(CIMAForecastTaskOptions.MASK_SEA)));
	}
	if (options.containsKey(CIMAForecastTaskOptions.MAX_LEAD_HOUR)) {
	    processor.setMaxLeadHour(Integer.parseInt(options.get(CIMAForecastTaskOptions.MAX_LEAD_HOUR)));
	}
	if (options.containsKey(CIMAForecastTaskOptions.DOWNLOAD_MODE)) {
	    processor.setDownloadMode(DownloadMode.valueOf(options.get(CIMAForecastTaskOptions.DOWNLOAD_MODE).toUpperCase()));
	}

	processor.setReport(report);

	// The exception is not rethrown, otherwise the summary e-mail is not sent: the ERROR phase is kept by the scheduler
	try {
	    processor.execute();
	    status.setPhase(JobPhase.COMPLETED);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("CIMAForecastTask failed: " + e.getMessage(), e);
	    report.error("Task failed: " + TaskReport.message(e));
	    status.setErrorPhase();
	}

	log(status, "CIMAForecastTask ENDED (duration " + TaskReport.elapsed(start) + ")");
    }

    /**
     * Invalid configuration: the task is not executed
     */
    private void cancel(SchedulerJobStatus status, TaskReport report, String message) {

	GSLoggerFactory.getLogger(getClass()).error(message);
	report.error(message);
	status.setPhase(JobPhase.CANCELED);
    }

    @Override
    public String getName() {
	return "CIMA WRF Forecast COG Task";
    }
}
