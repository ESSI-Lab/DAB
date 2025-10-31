package eu.essi_lab.cfga.scheduler;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.lib.servlet.RequestManager;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
@DisallowConcurrentExecution
public class SchedulerJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

	SchedulerWorkerSetting setting = SchedulerUtils.getSetting(context);

	//
	//
	//

	@SuppressWarnings("rawtypes")
	SchedulerWorker worker = null;

	try {
	    worker = setting.createConfigurable();
	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	    throw new JobExecutionException(ex);
	}

	//
	//
	//

	SchedulerJobStatus status = new SchedulerJobStatus(context, setting.getIdentifier(), worker.getClass().getName());

	if (isPersistentScheduler(context)) {

	    JobRescheduler rescheduler = new JobRescheduler();

	    if (rescheduler.isRescheduled(setting, context)) {

		status.setPhase(JobPhase.RESCHEDULED);

		storeJobStatus(context, worker, status);

		return;
	    }
	}

	//
	//
	//

	status.setPhase(JobPhase.RUNNING);

	storeJobStatus(context, worker, status);

	//
	//
	//

	SchedulerWorkerSetting downCast = (SchedulerWorkerSetting) SettingUtils.downCast(setting, setting.getSettingClass());

	GSLoggerFactory.getLogger(getClass()).debug("Worker name: {}", downCast.getWorkerName());
	GSLoggerFactory.getLogger(getClass()).debug("Worker class: {}", worker.getClass().getName());

	try {

	    GSLoggerFactory.getLogger(getClass()).debug("Job execution STARTED");

	    RequestManager.getInstance().updateThreadName(worker.getClass(), downCast.getWorkerName());

	    worker.doJob(context, status);

	    status.setEndTime();

	    if (status.getPhase() == JobPhase.RUNNING) {

		status.setPhase(JobPhase.COMPLETED);
	    }

	    GSLoggerFactory.getLogger(getClass()).debug("Job execution ENDED");

	} catch (Exception e) {

	    if (e instanceof GSException) {

		((GSException) e).log();

	    } else {

		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }

	    status.setEndTime();
	    status.setPhase(JobPhase.ERROR);

	    throw new JobExecutionException(e);

	} finally {

	    storeJobStatus(context, worker, status);
	}
    }

    /**
     * @param context
     * @return
     */
    private boolean isPersistentScheduler(JobExecutionContext context) {

	try {
	    return context.getScheduler().getMetaData().isJobStoreSupportsPersistence();
	} catch (SchedulerException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	// conservative response
	return true;
    }

    /**
     * @param context
     * @param worker
     * @param status
     */
    private void storeJobStatus(JobExecutionContext context, SchedulerWorker<?> worker, SchedulerJobStatus status) {

	GSLoggerFactory.getLogger(getClass()).debug("Storing job status STARTED");

	try {

	    SchedulerUtils.putStatus(context, status);

	    worker.storeJobStatus(status);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to store status");

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	GSLoggerFactory.getLogger(getClass()).debug("Storing job status ENDED");
    }
}
