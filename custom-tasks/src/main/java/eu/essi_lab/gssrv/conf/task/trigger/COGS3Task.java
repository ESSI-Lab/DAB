package eu.essi_lab.gssrv.conf.task.trigger;

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
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import org.quartz.JobExecutionContext;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Optional;

public class COGS3Task extends AbstractCustomTask {



    public enum COGS3TaskTaskOptions implements OptionsKey {
	SOURCE_URL, BUCKET_NAME, VAR_NAMES;
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {
	Instant start = Instant.now();
	log(status, "COGS3Task STARTED");
	TaskReport report = TaskReport.of(status);

	//CustomTaskSetting setting = retrieveSetting(context);
	//Optional<String> taskOptions = setting.getTaskOptions();

	//String bucketName = "s3-demo-geotiff"; // Default
	//String[] variables = {"shiwe", "2t", "2r", "utci"};

	Optional<EnumMap<COGS3Task.COGS3TaskTaskOptions, String>> taskOptions = readTaskOptions(context, COGS3Task.COGS3TaskTaskOptions.class);
	if (taskOptions.isEmpty()) {
	    cancel(status, report, "No options specified");
	    return;
	}
	String bucketName = taskOptions.get().get(COGS3TaskTaskOptions.BUCKET_NAME);
	if (bucketName == null && bucketName == null) {
	    cancel(status, report, "No bucket name option specified");
	    return;
	}

	String varName = taskOptions.get().get(COGS3TaskTaskOptions.VAR_NAMES);
	if (varName == null && varName == null) {
	    cancel(status, report, "No variable names option specified");
	    return;
	}

	String sourceURL = taskOptions.get().get(COGS3TaskTaskOptions.SOURCE_URL);
	if (sourceURL == null && sourceURL == null) {
	    cancel(status, report, "No Geotiff url option specified");
	    return;
	}
	String[] variables = varName.split(",");


	Optional<S3TransferWrapper> manager = getS3TransferManager();
	if (manager.isEmpty()) {
	    cancel(status, report, "S3 manager not present");
	    return;
	}

	COGS3SyncProcessor processor = new COGS3SyncProcessor(manager.get(), bucketName, variables, sourceURL);
	processor.setReport(report);

	// The exception is not rethrown, otherwise the summary e-mail is not sent: the ERROR phase is kept by the scheduler
	try {
	    processor.execute();
	    status.setPhase(JobPhase.COMPLETED);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("COGS3Task failed: " + e.getMessage(), e);
	    report.error("Task failed: " + TaskReport.message(e));
	    status.setErrorPhase();
	}

	log(status, "COGS3Task ENDED (duration " + TaskReport.elapsed(start) + ")");
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
	return "COG S3Task";
    }
}
