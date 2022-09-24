package eu.essi_lab.cfga.scheduler;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.quartz.DateBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting.SchedulingGroup;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.configuration.GIProjectExecutionMode;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author Fabrizio
 */
public class JobRescheduler {

    public JobRescheduler() {
    }

    /**
     * @param setting
     * @param context
     * @return
     */
    public boolean isRescheduled(SchedulerWorkerSetting setting, JobExecutionContext context) {

	ExecutionMode executionMode = GIProjectExecutionMode.getMode();

	SchedulingGroup group = setting.getGroup();

	switch (executionMode) {

	//
	// Access nodes executes only asynch downloads jobs
	//
	case ACCESS:

	    switch (group) {
	    case ASYNCH_ACCESS:
		
		logJobExecuting(setting, executionMode, group);

		return false;
	    default:

		logJobRescheduled(setting, executionMode, group);

		rescheduleJob(setting, context);
		return true;
	    }

	    //
	    // Batch nodes executes all these jobs
	    //
	case BATCH:

	    switch (group) {
	    case HARVESTING:
	    case AUGMENTING:
	    case BULK_DOWNLOAD:
	    case DEFAULT:

		logJobExecuting(setting, executionMode, group);

		return false;

	    default:

		logJobRescheduled(setting, executionMode, group);

		rescheduleJob(setting, context);
		return true;
	    }

	    //
	    // Custom tasks are executed directly by the configuration node
	    //
	case CONFIGURATION:

	    switch (group) {
	    case CUSTOM_TASK:
		
		logJobExecuting(setting, executionMode, group);

		return false;
	    default:

		logJobRescheduled(setting, executionMode, group);

		rescheduleJob(setting, context);
		return true;
	    }

	    //
	    // This should never happen since in frontend execution mode the scheduler is not started
	    //
	case FRONTEND:

	    logJobRescheduled(setting, executionMode, group);

	    rescheduleJob(setting, context);
	    return true;
	//
	// in MIXED and LOCAL_PRODUCTION mode all jobs are executed
	//
	case MIXED:
	case LOCAL_PRODUCTION:
	default:

	    logJobExecuting(setting, executionMode, group);

	    return false;
	}
    }

    /**
     * @param setting
     * @param context
     */
    private void rescheduleJob(SchedulerWorkerSetting setting, JobExecutionContext context) {

	try {

	    GSLoggerFactory.getLogger(getClass()).info("Rescheduling of worker {} STARTED", setting.getName());

	    Scheduler scheduler = context.getScheduler();

	    JobKey jobKey = context.getJobDetail().getKey();
	    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);

	    GSLoggerFactory.getLogger(getClass()).info("Found " + triggers.size() + " triggers");

	    if (triggers.isEmpty()) {

		GSLoggerFactory.getLogger(getClass()).error("Unable to found triggers!");

	    } else {

		for (Trigger trigger : triggers) {

		    TriggerBuilder<? extends Trigger> triggerBuilder = trigger.getTriggerBuilder();

		    //
		    // rescheduled in 2 minutes
		    //
		    Trigger newTrigger = triggerBuilder.startAt(DateBuilder.nextGivenMinuteDate(new Date(), 2)).build();

		    Date date = scheduler.rescheduleJob(trigger.getKey(), newTrigger);

		    GSLoggerFactory.getLogger(getClass()).info("Job rescheduled: {}", ISO8601DateTimeUtils.getISO8601DateTime(date));
		}
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Rescheduling of worker {} ENDED", setting.getName());

	} catch (SchedulerException e) {

	    e.printStackTrace();

	    GSLoggerFactory.getLogger(getClass()).error("Rescheduling of worker {} FAILED", setting.getName());

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * @param setting
     * @param executionMode
     * @param group
     */
    private void logJobExecuting(Setting setting, ExecutionMode executionMode, SchedulingGroup group) {

	GSLoggerFactory.getLogger(getClass()).debug("Job '{}' is executable by this worker (execMode={}, schedulingGroup={})",
		setting.getName() + "_" + setting.getIdentifier(), executionMode, group.name());

    }

    /**
     * @param setting
     * @param executionMode
     * @param group
     */
    private void logJobRescheduled(Setting setting, ExecutionMode executionMode, SchedulingGroup group) {

	GSLoggerFactory.getLogger(getClass()).warn(
		"Job '{}' is NOT executable by this worker (execMode={}, schedulingGroup={}). Rescheduling",
		setting.getName() + "_" + setting.getIdentifier(), executionMode, group.name());

    }
}
