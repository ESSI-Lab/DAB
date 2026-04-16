package eu.essi_lab.cfga.scheduler;

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

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting.SchedulingGroup;
import eu.essi_lab.configuration.ClusterType;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.lib.utils.GSLoggerFactory;

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
    @SuppressWarnings("incomplete-switch")
    public boolean isRescheduled(SchedulerWorkerSetting setting, JobExecutionContext context) {

	ExecutionMode executionMode = ExecutionMode.get();

	ClusterType clusterType = ClusterType.get();

	SchedulingGroup group = setting.getGroup();

	switch (executionMode) {

	//
	//
	//
	case BATCH:

	    switch (clusterType) {
	    case PRE_PRODUCTION:

		switch (group) {
		case HARVESTING:
		case AUGMENTING:
		case ASYNCH_ACCESS:
		case CUSTOM_TASK:
		case DEFAULT:

		    logJobExecuting(setting, executionMode, group);

		    return false;

		default:

		    logJobRescheduled(setting, executionMode, group);

		    rescheduleJob(setting, context);
		    return true;
		}

	    case PRODUCTION:

		switch (group) {
		case HARVESTING:
		case BULK_DOWNLOAD:
		case DEFAULT:

		    logJobExecuting(setting, executionMode, group);

		    return false;

		default:

		    logJobRescheduled(setting, executionMode, group);

		    rescheduleJob(setting, context);
		    return true;
		}
	    }

	    //
	    //
	    //
	case CONFIGURATION:

	    switch (group) {
	    case BULK_DOWNLOAD:

		logJobExecuting(setting, executionMode, group);

		return false;

	    case DEFAULT:
	    default:

		logJobRescheduled(setting, executionMode, group);

		rescheduleJob(setting, context);
		return true;
	    }

	    //
	    //
	    //
	case AUGMENTER:

	    switch (group) {
	    case AUGMENTING:
	    case ASYNCH_ACCESS:
	    case CUSTOM_TASK:

		logJobExecuting(setting, executionMode, group);

		return false;

	    default:

		logJobRescheduled(setting, executionMode, group);

		rescheduleJob(setting, context);
		return true;
	    }

	    //
	    // no job executed
	    //
	case INTENSIVE:
	case FRONTEND:
	case ACCESS:

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

	    GSLoggerFactory.getLogger(getClass()).info("Automatic rescheduling of worker {} STARTED", setting.getName());

	    Scheduler scheduler = context.getScheduler();

	    JobKey jobKey = context.getJobDetail().getKey();
	    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);

	    GSLoggerFactory.getLogger(getClass()).info("Found {} triggers", triggers.size());

	    if (triggers.isEmpty()) {

		GSLoggerFactory.getLogger(getClass()).error("Unable to found triggers!");

	    } else {

		for (Trigger trigger : triggers) {

		    @SuppressWarnings("unchecked")
		    TriggerBuilder<Trigger> triggerBuilder = (TriggerBuilder<Trigger>) trigger.getTriggerBuilder();

		    Trigger newTrigger = triggerBuilder//
			    // .withSchedule(SimpleScheduleBuilder.simpleSchedule()//
			    // .withMisfireHandlingInstructionIgnoreMisfires())//
			    .startAt(new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1)))//
			    .build();

		    GSLoggerFactory.getLogger(getClass()).info("Job rescheduled to be fired as soon as possible");

		    scheduler.rescheduleJob(trigger.getKey(), newTrigger);
		}
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Automatic rescheduling of worker {} ENDED", setting.getName());

	} catch (SchedulerException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Automatic rescheduling of worker {} FAILED", setting.getName());

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}
    }

    /**
     * @param setting
     * @param executionMode
     * @param group
     */
    private void logJobExecuting(SchedulerWorkerSetting setting, ExecutionMode executionMode, SchedulingGroup group) {

	SchedulerWorkerSetting downCast = (SchedulerWorkerSetting) SettingUtils.downCast(setting, setting.getSettingClass());

	GSLoggerFactory.getLogger(getClass()).debug("Worker '{}' is executable by this job (execMode={}, schedulingGroup={})",
		downCast.getWorkerName() + " (" + setting.getIdentifier() + ")", executionMode, group.name());

    }

    /**
     * @param setting
     * @param executionMode
     * @param group
     */
    private void logJobRescheduled(SchedulerWorkerSetting setting, ExecutionMode executionMode, SchedulingGroup group) {

	SchedulerWorkerSetting downCast = (SchedulerWorkerSetting) SettingUtils.downCast(setting, setting.getSettingClass());

	GSLoggerFactory.getLogger(getClass()).warn(
		"Worker '{}' is NOT executable by this job (execMode={}, schedulingGroup={}). Rescheduling",
		downCast.getWorkerName() + " (" + setting.getIdentifier() + ")", executionMode, group.name());

    }
}
