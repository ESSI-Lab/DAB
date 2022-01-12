package eu.essi_lab.jobs.scheduler.quartz;

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

import static eu.essi_lab.jobs.scheduler.quartz.QuartzJobBuilderMediator.CONCRETE_JOB_KEY;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.quartz.DateBuilder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.ScheduleBuilder;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.quartz.spi.MutableTrigger;
import org.slf4j.Logger;

import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.configuration.GIProjectExecutionMode;
import eu.essi_lab.jobs.excution.GSJobExecutionPlanBuilder;
import eu.essi_lab.jobs.excution.GSJobExecutor;
import eu.essi_lab.jobs.scheduler.GSJobSchedulerFactory;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.BulkDownloadMessage;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
@DisallowConcurrentExecution
public class GSQuartzJob implements Job {

    Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private static final String MESSAGE_KEY = "MESSAGE";

    public enum JobType {
	HARVEST, ACCESS, BULK_DOWNLOAD, OTHER
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

	logger.debug("[JOB] Execute invoked ");

	JobDetail jobDetail = jobExecutionContext.getJobDetail();

	String job = (String) jobDetail.getJobDataMap().get(CONCRETE_JOB_KEY);

	JobDataMap jobDataMap = jobDetail.getJobDataMap();

	JobType type = identifyJob(jobDetail);

	ExecutionMode mode = new GIProjectExecutionMode().getMode();

	logger.debug("[JOB] Execution mode: {} Job Type: {} ", mode, type);

	switch (mode) {
	case ACCESS:
	    if (type == JobType.ACCESS) {
		break;
	    } else {
		String info = getNotAbleToExecuteMessage(mode, type);
		logger.error(info);
		rescheduleTask(jobExecutionContext, info);
	    }
	case BATCH:
	    if (type == JobType.HARVEST || type == JobType.BULK_DOWNLOAD || type == JobType.OTHER) {
		break;
	    } else {
		String info = getNotAbleToExecuteMessage(mode, type);
		logger.error(info);
		rescheduleTask(jobExecutionContext, info);
	    }
	case FRONTEND:
	    // this should not happen
	    String info = getNotAbleToExecuteMessage(mode, type);
	    logger.error(info);
	    rescheduleTask(jobExecutionContext, info);
	case MIXED:
	default:
	    break;
	}

	logger.debug("[JOB] Launching IGSJob: {}", job);

	GSJobExecutor executor = new GSJobExecutor();

	GSJobExecutionPlanBuilder builder = new GSJobExecutionPlanBuilder();

	builder.setFactory(new GSJobSchedulerFactory());

	executor.setBuilder(builder);

	Boolean recovering = jobExecutionContext.isRecovering();

	executor.execute(job, jobDataMap, jobExecutionContext.getTrigger().getKey(), recovering);

    }

    private void rescheduleTask(JobExecutionContext jobExecutionContext, String info) throws JobExecutionException {
	// SimpleScheduleBuilder schedule = SimpleScheduleBuilder //
	// .simpleSchedule() //
	// .withRepeatCount(0); //
	//
	// Trigger trigger = TriggerBuilder//
	// .newTrigger()//
	// .withIdentity(jobExecutionContext.getJobDetail().getKey().getName() + " (retried " +
	// ISO8601DateTimeUtils.getISO8601DateTime()
	// + ")")//
	// .withDescription("RetryTrigger because " + info) //
	// .startAt(DateBuilder.nextGivenMinuteDate(new Date(), 2))//
	// .withSchedule(//
	// schedule)//
	// .forJob(jobExecutionContext.getJobDetail().getKey())//
	// .build();
	try {
	    JobKey jobKey = jobExecutionContext.getJobDetail().getKey();
	    List<? extends Trigger> triggers = jobExecutionContext.getScheduler().getTriggersOfJob(jobKey);
	    logger.info("[JOB] Found " + triggers.size() + " triggers.");
	    if (triggers.isEmpty()) {
		logger.error("[JOB] No triggers found!");
	    }else {
		for (Trigger trigger : triggers) {
		    TriggerBuilder<? extends Trigger> triggerBuilder = trigger.getTriggerBuilder();
		    Trigger newTrigger = triggerBuilder.startAt(DateBuilder.nextGivenMinuteDate(new Date(), 2)).build(); // restarts in two minutes
		    jobExecutionContext.getScheduler().rescheduleJob(trigger.getKey(), newTrigger);
		}
	    }
	    logger.error("[JOB] Rescheduled job to restart in 2 minutes.");
	} catch (SchedulerException e) {
	    e.printStackTrace();
	    logger.error("[JOB] Error rescheduling the job: " + e.getMessage());
	} // schedule the trigger

	JobExecutionException jex = new JobExecutionException(info, false);
	throw jex;

    }

    private String getNotAbleToExecuteMessage(ExecutionMode mode, JobType type) {
	return "[JOB] Not able to execute " + type + " job on node type: " + mode;
    }

    private JobType identifyJob(JobDetail jobDetail) {
	JobType type = JobType.OTHER;
	JobDataMap jobDataMap = jobDetail.getJobDataMap();
	String name = jobDetail.getKey().getName();
	if (name.contains(GSConfiguration.HARVESTER_JOB_KEY)) {
	    type = JobType.HARVEST;
	} else {
	    Object message = jobDataMap.get(MESSAGE_KEY);
	    if (message != null) {
		if (message instanceof AccessMessage) {
		    type = JobType.ACCESS;
		} else if (message instanceof BulkDownloadMessage) {
		    type = JobType.BULK_DOWNLOAD;
		}
	    }
	}
	return type;
    }
}
