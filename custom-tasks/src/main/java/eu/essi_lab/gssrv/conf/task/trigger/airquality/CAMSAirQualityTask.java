package eu.essi_lab.gssrv.conf.task.trigger.airquality;

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

import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.OptionsKey;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.gssrv.conf.task.trigger.TaskReport;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import org.quartz.JobExecutionContext;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

/**
 * Publishes on S3 the COGs of the CAMS European air quality forecasts (see {@link CAMSAirQualityProcessor}).<br>
 * Task options (one per line, key=value):
 * <ul>
 * <li>BUCKET_NAME (required): target bucket</li>
 * <li>PREFIX: key prefix in the bucket, default "CAMS"</li>
 * <li>ADS_KEY: ADS personal access token; if missing, the ADS_KEY environment variable is used</li>
 * <li>ADS_URL: ADS API endpoint, default {@value ADSClient#DEFAULT_URL}</li>
 * <li>POLLUTANTS: comma separated list among pm25, pm10, no2, o3, so2; default all</li>
 * <li>MAX_LEAD_HOUR: last forecast hour, 0-96; default 96</li>
 * <li>RUN_DATE: run to download (yyyy-MM-dd); default the latest available 00 UTC run</li>
 * <li>AREA: optional sub-area as North,West,South,East; default the whole CAMS Europe domain</li>
 * <li>MASK_SEA: true to set the sea and ocean pixels to NoData; default false</li>
 * </ul>
 * The task should be scheduled once a day, after {@value CAMSAirQualityProcessor#RUN_AVAILABILITY_HOUR_UTC}:00 UTC
 */
public class CAMSAirQualityTask extends AbstractCustomTask {

    public enum CAMSAirQualityTaskOptions implements OptionsKey {
	BUCKET_NAME, PREFIX, ADS_KEY, ADS_URL, POLLUTANTS, MAX_LEAD_HOUR, RUN_DATE, AREA, MASK_SEA;
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	Instant start = Instant.now();
	log(status, "CAMSAirQualityTask STARTED");
	TaskReport report = TaskReport.of(status);

	Optional<EnumMap<CAMSAirQualityTaskOptions, String>> taskOptions = readTaskOptions(context, CAMSAirQualityTaskOptions.class);
	if (taskOptions.isEmpty()) {
	    cancel(status, report, "No options specified");
	    return;
	}
	EnumMap<CAMSAirQualityTaskOptions, String> options = taskOptions.get();

	String bucketName = options.get(CAMSAirQualityTaskOptions.BUCKET_NAME);
	if (bucketName == null) {
	    cancel(status, report, "No bucket name option specified");
	    return;
	}

	String adsKey = Optional.ofNullable(options.get(CAMSAirQualityTaskOptions.ADS_KEY)).orElse(System.getenv("ADS_KEY"));
	if (adsKey == null || adsKey.isBlank()) {
	    cancel(status, report, "No ADS key specified (ADS_KEY option or environment variable)");
	    return;
	}

	String adsUrl = options.getOrDefault(CAMSAirQualityTaskOptions.ADS_URL, ADSClient.DEFAULT_URL);
	String prefix = options.getOrDefault(CAMSAirQualityTaskOptions.PREFIX, "CAMS");

	List<CAMSPollutant> pollutants = options.containsKey(CAMSAirQualityTaskOptions.POLLUTANTS)
		? CAMSAirQualityProcessor.parsePollutants(options.get(CAMSAirQualityTaskOptions.POLLUTANTS))
		: List.of(CAMSPollutant.values());
	if (pollutants.isEmpty()) {
	    cancel(status, report, "No valid pollutant specified");
	    return;
	}

	int maxLeadHour = options.containsKey(CAMSAirQualityTaskOptions.MAX_LEAD_HOUR)
		? Integer.parseInt(options.get(CAMSAirQualityTaskOptions.MAX_LEAD_HOUR))
		: CAMSAirQualityProcessor.MAX_LEAD_HOUR;

	LocalDate runDate = options.containsKey(CAMSAirQualityTaskOptions.RUN_DATE)
		? LocalDate.parse(options.get(CAMSAirQualityTaskOptions.RUN_DATE))
		: null;

	Optional<S3TransferWrapper> manager = getS3TransferManager();
	if (manager.isEmpty()) {
	    cancel(status, report, "S3 manager not present");
	    return;
	}

	CAMSAirQualityProcessor processor = new CAMSAirQualityProcessor(new ADSClient(adsUrl, adsKey), manager.get(), bucketName, prefix,
		pollutants, runDate, maxLeadHour);

	if (options.containsKey(CAMSAirQualityTaskOptions.AREA)) {
	    processor.setArea(Arrays.stream(options.get(CAMSAirQualityTaskOptions.AREA).split(",")).map(String::trim)
		    .mapToDouble(Double::parseDouble).toArray());
	}

	processor.setMaskSea(Boolean.parseBoolean(options.get(CAMSAirQualityTaskOptions.MASK_SEA)));

	processor.setReport(report);

	// The exception is not rethrown, otherwise the summary e-mail is not sent: the ERROR phase is kept by the scheduler
	try {
	    processor.execute();
	    status.setPhase(JobPhase.COMPLETED);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("CAMSAirQualityTask failed: " + e.getMessage(), e);
	    report.error("Task failed: " + TaskReport.message(e));
	    status.setErrorPhase();
	}

	log(status, "CAMSAirQualityTask ENDED (duration " + TaskReport.elapsed(start) + ")");
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
	return "CAMS Air Quality COG Task";
    }
}
