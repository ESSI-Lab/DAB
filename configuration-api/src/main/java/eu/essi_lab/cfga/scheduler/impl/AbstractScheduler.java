package eu.essi_lab.cfga.scheduler.impl;

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

import java.sql.DriverManager;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTimeZone;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.listeners.JobListenerSupport;

import eu.essi_lab.cfga.option.ISODateTime;
import eu.essi_lab.cfga.scheduler.SchedulerUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author Fabrizio
 */
public abstract class AbstractScheduler implements eu.essi_lab.cfga.scheduler.Scheduler {

    protected Scheduler scheduler;
    protected DateTimeZone dateTimeZone;

    /**
     * 
     */
    public AbstractScheduler() {
    }

    /**
     * @param quartzScheduler
     */
    public AbstractScheduler(Scheduler quartzScheduler) {

	scheduler = quartzScheduler;
    }

    /**
     * @throws SchedulerException
     */
    @Override
    public synchronized void start() throws SchedulerException {

	if (!scheduler.isStarted() && !scheduler.isShutdown()) {

	    scheduler.start();
	}
    }

    /**
     * @throws SchedulerException
     */
    @Override
    public synchronized void shutdown() throws SchedulerException {

	scheduler.shutdown();
    }

    /**
     * @param setting
     * @throws SchedulerException
     */
    @Override
    public synchronized void schedule(SchedulerWorkerSetting setting) throws SchedulerException {

	GSLoggerFactory.getLogger(getClass()).info("Scheduling of worker {} STARTED", setting.getName());

	logScheduling(setting.getScheduling());

	JobDetail jobDetail = SchedulerUtils.createJob(setting);

	Trigger trigger = buildTrigger(setting, jobDetail);

	scheduler.scheduleJob(jobDetail, trigger);

	GSLoggerFactory.getLogger(getClass()).info("Scheduling of worker {} ENDED", setting.getName());
    }

    /**
     * @param setting
     * @throws SchedulerException
     */
    @Override
    public synchronized void reschedule(SchedulerWorkerSetting setting) throws Exception {

	GSLoggerFactory.getLogger(getClass()).info("Rescheduling of worker {} STARTED", setting.getName());

	JobDetail jobDetail = SchedulerUtils.createJob(setting);

	List<SchedulerWorkerSetting> settings = listScheduledSettings();

	boolean isScheduled = settings.//
		stream().//
		map(s -> SchedulerUtils.createJob(s)).//
		filter(j -> j.getKey().equals(jobDetail.getKey())).//
		findFirst().//
		isPresent();
	//
	// enters here if the worker is not scheduled at all because scheduling was disabled
	//
	if (!isScheduled) {

	    GSLoggerFactory.getLogger(getClass()).info("Worker not scheduled (scheduling was disabled), adding new job");

	    schedule(setting);
	    return;
	}

	GSLoggerFactory.getLogger(getClass()).info("Unscheduling of current job STARTED");

	Trigger trigger = buildTrigger(setting, jobDetail);

	boolean unscheduled = scheduler.unscheduleJob(trigger.getKey());

	if (!unscheduled) {

	    GSLoggerFactory.getLogger(getClass()).error("Unscheduling of worker {} failed, trigger not found", setting.getName());
	    return;
	}

	GSLoggerFactory.getLogger(getClass()).info("Unscheduling of current job ENDED");

	schedule(setting);

	GSLoggerFactory.getLogger(getClass()).info("Rescheduling of worker {} ENDED", setting.getName());
    }

    /**
     * @param setting
     * @throws SchedulerException
     */
    @Override
    public synchronized void pause(SchedulerWorkerSetting setting) throws SchedulerException {

	GSLoggerFactory.getLogger(getClass()).info("Pausing of worker {} STARTED", setting.getName());

	scheduler.pauseJob(SchedulerUtils.createJobKey(setting));

	GSLoggerFactory.getLogger(getClass()).info("Pausing of worker {} ENDED", setting.getName());
    }

    /**
     * @param setting
     * @throws SchedulerException
     */
    @Override
    public synchronized void unschedule(SchedulerWorkerSetting setting) throws SchedulerException {

	GSLoggerFactory.getLogger(getClass()).info("Unscheduling of worker {} STARTED", setting.getName());

	GSLoggerFactory.getLogger(getClass()).info("With worker identifier {}", setting.getIdentifier());
	GSLoggerFactory.getLogger(getClass()).info("With worker group {}", setting.getGroup().getLabel());

	boolean unscheduled = scheduler.unscheduleJob(SchedulerUtils.createTriggerKey(setting));

	GSLoggerFactory.getLogger(getClass()).info("Unscheduling: " + (unscheduled ? " SUCCEEDED" : "FAILED"));

	GSLoggerFactory.getLogger(getClass()).info("Unscheduling of worker {} ENDED", setting.getName());
    }

    /**
     * @throws SchedulerException
     */
    @Override
    public synchronized void unscheduleAll() throws Exception {

	List<SchedulerWorkerSetting> schedulingSettings = listScheduledSettings();
	for (SchedulerWorkerSetting schedulingSetting : schedulingSettings) {
	    unschedule(schedulingSetting);
	}
    }

    /**
     * @param listener
     * @throws SchedulerException
     */
    @Override
    public synchronized void addSchedulerListener(SchedulerListener listener) throws SchedulerException {

	scheduler.getListenerManager().addSchedulerListener(listener);
    }

    /**
     * @param listener
     * @throws SchedulerException
     */
    @Override
    public synchronized void removeSchedulerListener(SchedulerListener listener) throws SchedulerException {

	scheduler.getListenerManager().removeSchedulerListener(listener);
    }

    /**
     * @param listener
     * @param name
     * @throws SchedulerException
     */
    @Override
    public synchronized void addJobEventListener(JobEventListener listener, String name) throws SchedulerException {

	scheduler.getListenerManager().addJobListener(new JobListenerSupport() {

	    public void jobToBeExecuted(JobExecutionContext context) {

		listener.eventOccurred(JobEvent.JOB_TO_BE_EXECUTED, context, null);
	    }

	    public void jobExecutionVetoed(JobExecutionContext context) {

		listener.eventOccurred(JobEvent.JOB_VETOED, context, null);
	    }

	    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {

		listener.eventOccurred(JobEvent.JOB_EXECUTED, context, null);
	    }

	    public String getName() {

		return name;
	    }
	});
    }

    /**
     * @throws SchedulerException
     */
    @Override
    public void addJobEventListener(JobEventListener listener, JobEvent event, String name, boolean autoRemove) throws SchedulerException {

	scheduler.getListenerManager().addJobListener(new JobListenerSupport() {

	    public synchronized void jobToBeExecuted(JobExecutionContext context) {

		if (event == JobEvent.JOB_TO_BE_EXECUTED) {

		    notifyEvent(context, listener, event, name, autoRemove);
		}
	    }

	    public void jobExecutionVetoed(JobExecutionContext context) {

		if (event == JobEvent.JOB_VETOED) {

		    notifyEvent(context, listener, event, name, autoRemove);
		}
	    }

	    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {

		if (event == JobEvent.JOB_EXECUTED) {

		    notifyEvent(context, listener, event, name, autoRemove);
		}
	    }

	    public String getName() {

		return name;
	    }
	});
    }

    /**
     * @param listenerName
     * @throws SchedulerException
     */
    @Override
    public synchronized void removeJobListener(String listenerName) throws SchedulerException {

	scheduler.getListenerManager().removeJobListener(listenerName);
    }

    /**
     * @return
     */
    @Override
    public synchronized Scheduler getQuartzScheduler() {

	return scheduler;
    }

    /**
     * 
     */
    @Override
    public void setUserDateTimeZone(DateTimeZone dateTimeZone) {

	this.dateTimeZone = dateTimeZone;
    }

    /**
     * @param setting
     * @param jobDetail
     * @return
     */
    protected Trigger buildTrigger(SchedulerWorkerSetting setting, JobDetail jobDetail) {

	SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.//

		simpleSchedule().//

		withMisfireHandlingInstructionFireNow();

	if (!setting.getScheduling().isRunOnceSet()) {

	    int repeatInterval = setting.getScheduling().getRepeatInterval();
	    TimeUnit repeatIntervalUnit = setting.getScheduling().getRepeatIntervalUnit();
	    long repeatIntervalMillis = repeatIntervalUnit.toMillis(repeatInterval);

	    scheduleBuilder = scheduleBuilder.withIntervalInMilliseconds(repeatIntervalMillis);

	    Optional<Integer> repeatCount = setting.getScheduling().getRepeatCount();

	    if (repeatCount.isPresent()) {

		scheduleBuilder = scheduleBuilder.withRepeatCount(repeatCount.get());

	    } else {

		scheduleBuilder = scheduleBuilder.repeatForever();
	    }
	}

	TriggerBuilder<SimpleTrigger> triggerBuilder = //
		SchedulerUtils.createTriggerBuilder(setting, jobDetail).//
			withSchedule(scheduleBuilder);

	Optional<ISODateTime> optStartTime = setting.getScheduling().getStartTime();

	if (optStartTime.isPresent()) {

	    String startTimeValue = setting.getScheduling().getStartTime().get().getValue();

	    Date startTime = ISO8601DateTimeUtils.toGMTDateTime(startTimeValue, dateTimeZone);

	    triggerBuilder = triggerBuilder.startAt(startTime);

	} else {

	    triggerBuilder = triggerBuilder.startNow();
	}

	Optional<ISODateTime> optEndTime = setting.getScheduling().getEndTime();

	if (optEndTime.isPresent()) {

	    String endTimeValue = setting.getScheduling().getEndTime().get().getValue();

	    Date endTime = ISO8601DateTimeUtils.toGMTDateTime(endTimeValue, dateTimeZone);

	    triggerBuilder = triggerBuilder.endAt(endTime);
	}

	Trigger trigger = triggerBuilder.build();

	return trigger;
    }

    /**
     * @param jobDetail
     * @return
     * @throws SchedulerException
     */
    protected Optional<JobExecutionContext> findContext(JobDetail jobDetail) throws SchedulerException {

	return scheduler.//
		getCurrentlyExecutingJobs().//
		stream().//
		filter(c -> c.getJobDetail().getKey().getName().equals(jobDetail.getKey().getName())).//
		findFirst();
    }

    public static void main(String[] args) throws Exception {

	// ?useSSL=false

	String url = "jdbc:mysql://hydrodb-cluster.cluster-cuj9mnzczk9c.us-east-1.rds.amazonaws.com:3306?useSSL=false";

	DriverManager.getConnection(url, "essihydro", "ciardi25");
    }

    /**
     * @param scheduling
     */
    private void logScheduling(Scheduling scheduling) {

	GSLoggerFactory.getLogger(getClass()).info("With repeat count {}", scheduling.getRepeatCount().map(r -> r.toString()).orElse("-"));
	GSLoggerFactory.getLogger(getClass()).info("With repeat interval {}", scheduling.getRepeatInterval());
	GSLoggerFactory.getLogger(getClass()).info("With repeat interval time unit {}", scheduling.getRepeatIntervalUnit());
	GSLoggerFactory.getLogger(getClass()).info("With start time {}", scheduling.getStartTime().map(t -> t.toString()+" "+dateTimeZone).orElse("-"));
	GSLoggerFactory.getLogger(getClass()).info("With end time {}", scheduling.getEndTime().map(t -> t.toString()+" "+dateTimeZone).orElse("-"));
    }

    /**
     * @param context
     * @param listener
     * @param event
     * @param autoRemove
     */
    private void notifyEvent(JobExecutionContext context, JobEventListener listener, JobEvent event, String name, boolean autoRemove) {

	GSLoggerFactory.getLogger(getClass()).debug("Registered JobEvent " + event + " occurred");

	GSLoggerFactory.getLogger(getClass()).debug("Notifying event STARTED");

	listener.eventOccurred(event, context, null);

	GSLoggerFactory.getLogger(getClass()).debug("Notifying event ENDED");

	if (autoRemove) {

	    GSLoggerFactory.getLogger(getClass()).debug("Autoremoving listener STARTED");

	    try {
		scheduler.getListenerManager().removeJobListener(name);
	    } catch (SchedulerException e) {
		e.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }

	    GSLoggerFactory.getLogger(getClass()).debug("Autoremoving listener ENDED");
	}
    }
}
