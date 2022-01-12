package eu.essi_lab.jobs.listener;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;
import org.slf4j.Logger;

import eu.essi_lab.configuration.ConfigurationUtils;
import eu.essi_lab.jobs.GSJobStatus;
import eu.essi_lab.jobs.excution.GSJobExecutor;
import eu.essi_lab.jobs.report.GSJobEventCollectorFactory;
import eu.essi_lab.jobs.report.GSJobStatusCollector;
import eu.essi_lab.jobs.scheduler.GSJobSchedulerFactory;
import eu.essi_lab.jobs.scheduler.IGSJobScheduler;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.SharedRepositoryInfo;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.shared.messages.SharedContentReadResponse;
import eu.essi_lab.shared.model.SharedContent;
public class GSJobListener extends JobListenerSupport {

    public static final String GS_JOB_STATUS_EXECUTION_ID = "gs_execution_id";
    public static final String GS_JOB_STATUS_HINT_EXECUTION_ID = "gs_hint_execution_id";
    private static final String GSJOB_LISTENER_NAME = "GSJOB_LISTENER";
    private transient Logger logger = GSLoggerFactory.getLogger(GSJobListener.class);

    @Override
    public String getName() {
	return GSJOB_LISTENER_NAME;
    }

    public GSJobStatus createBaseJobStatus(JobExecutionContext context) {

	GSJobStatus status = new GSJobStatus();

	String name = context.getJobDetail().getKey().getName();

	status.setJobkey(name);

	String group = context.getJobDetail().getKey().getGroup();

	status.setJobtype(group);

	status.setRecovering(context.isRecovering());

	// the user of this library might have put an hint for the executionId to be used
	String executionId = (String) context.getJobDetail().getJobDataMap().get(GS_JOB_STATUS_HINT_EXECUTION_ID);

	logger.debug("Hint executionid {}", executionId);

	if (executionId == null) {

	    executionId = UUID.randomUUID().toString();
	}

	status.setExecutionid(executionId);

	return status;
    }

    GSJobStatus recoverExistingExecutionId(JobExecutionContext context, GSJobStatus gsJobStatus, GSJobStatusCollector collector) {

	String existingid = (String) context.getJobDetail().getJobDataMap().get(GS_JOB_STATUS_EXECUTION_ID);

	logger.debug("Found recovring job with job key {} and existing execution id {}", gsJobStatus.getJobkey(), existingid);

	if (existingid != null && !"".equals(existingid)) {

	    gsJobStatus.setExecutionid(existingid);

	    try {

		SharedContentReadResponse<GSJobStatus> response = collector.read(existingid);

		List<SharedContent<GSJobStatus>> contents = response.getContents();

		if (!contents.isEmpty()) {
		    gsJobStatus = contents.get(0).getContent();
		}

		gsJobStatus.setRecovering(true);

	    } catch (GSException e) {

		logger.warn("Error fetching existing GSJobStatus with execution id {}", existingid);

		DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));

	    }

	} else {
	    logger.warn("Unable to recover existing execution id for {}", gsJobStatus.getJobkey());

	    gsJobStatus.setStartDate(new Date());

	}

	return gsJobStatus;

    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {

	logger.trace("Job To be Executed invoked");

	GSJobStatus status = createBaseJobStatus(context);

	try {

	    GSJobStatusCollector collector = retriveCollector();

	    if (status.getRecovering()) {

		status = recoverExistingExecutionId(context, status, collector);

	    } else {

		context.getJobDetail().getJobDataMap().put(GS_JOB_STATUS_EXECUTION_ID, status.getExecutionid());

		status.setStartDate(new Date());

		IGSJobScheduler sch = new GSJobSchedulerFactory().getGSJobScheduler();

		sch.updateJobMap(context.getJobDetail(), context.getJobDetail().getJobDataMap());
	    }

	    collector.store(status);

	} catch (GSException e) {

	    logger.warn("Error fetching GSJobStatusCollector, can't store job start status of job group {} and name {}",
		    status.getJobtype(), status.getJobkey());

	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));

	}

    }

    GSJobStatusCollector retriveCollector() throws GSException {

	SharedRepositoryInfo repo = ConfigurationUtils.getSharedRepositoryInfo();

	return GSJobEventCollectorFactory.getGSJobStatusCollector(repo);

    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {

	logger.trace("Job Was Executed invoked");

	GSJobStatusCollector collector;
	SharedContentReadResponse<GSJobStatus> response;

	try {

	    collector = retriveCollector();

	    response = collector.read(context.getJobDetail().getJobDataMap().getString(GS_JOB_STATUS_EXECUTION_ID));

	} catch (GSException e) {

	    logger.warn("Error fetching existing status from GSJobStatusCollector, can't store job end status of job group {} and name {}",
		    context.getJobDetail().getKey().getGroup(), context.getJobDetail().getKey().getName());

	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));

	    return;

	}

	response.getContents().stream().map(shared -> shared.getContent()).collect(Collectors.toList()).stream().findFirst().ifPresent(
		status -> {

		    JobDataMap jobMap = context.getJobDetail().getJobDataMap();

		    String result = jobMap.getString(GSJobExecutor.JOB_EXECUTION_RESULT_ID);

		    String phase = jobMap.getString(GSJobExecutor.JOB_EXECUTION_LAST_PHASE_ID);

		    status.setLastPhase(phase);

		    status.setResult(result);

		    Optional.ofNullable(jobMap.get(GSJobExecutor.JOB_EXECUTION_EXCEPTION_ID)).ifPresent(o -> {

			GSException e = (GSException) o;

			status.setException(
				DefaultGSExceptionLogger.printToString(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e))));

		    });

		    status.setEndDate(new Date());

		    collector.store(status);

		});

    }

}
