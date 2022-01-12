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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;

import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.jobs.GSJobRequest;
import eu.essi_lab.jobs.GSJobResultSet;
import eu.essi_lab.jobs.GSJobStatus;
import eu.essi_lab.jobs.GSJobUtils;
import eu.essi_lab.jobs.IGSJob;
import eu.essi_lab.jobs.configuration.GS_JOBS_CONSTANTS;
import eu.essi_lab.jobs.configuration.IGSConfigurableJob;
import eu.essi_lab.jobs.report.GSJobStatusCollector;
import eu.essi_lab.jobs.scheduler.GSJobSchedulerFactory;
import eu.essi_lab.jobs.scheduler.GS_JOB_INTERVAL_PERIOD;
import eu.essi_lab.jobs.scheduler.IGSJobScheduler;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
public class QuartzSchedulerMediator implements IGSJobScheduler {

    private static final String ERROR_DESCRIPTION = "Error thrwon by Quartz Scheduler";

    private static final String ERR_ID_QUARTZ_SCHEDULER_SCHEDULING = "ERR_ID_QUARTZ_SCHEDULER_SCHEDULING";

    private QuartzJobBuilderMediator builder;

    private Scheduler scheduler;

    private transient Logger logger = GSLoggerFactory.getLogger(this.getClass());
    private static final String ERR_ID_QUARTZ_SCHEDULER_EXIST_SCHEDULE = "ERR_ID_QUARTZ_SCHEDULER_EXIST_SCHEDULE";
    private static final String ERR_ID_QUARTZ_SCHEDULER_UNSCHEDULING = "ERR_ID_QUARTZ_SCHEDULER_UNSCHEDULING";
    private final Object lockObj = new Object();
    private static final String ERR_ID_QUARTZ_SCHEDULER_START = "ERR_ID_QUARTZ_SCHEDULER_START";
    private static final String ERR_ID_QUARTZ_SCHEDULER_STOP = "ERR_ID_QUARTZ_SCHEDULER_STOP";

    public void setScheduler(Scheduler sch) {

	scheduler = sch;

    }

    @Override
    public Date scheduleJob(GSUser user, IGSConfigurableJob job, IGSConfigurable configurable, Map<String, Object> map) throws GSException {

	try {

	    JobDataMap dataMap = doPrepareDataMap(job, map);

	    return doScheduleJob(job, configurable, dataMap, user, scheduler);

	} catch (SchedulerException se) {

	    throw GSException.createException(this.getClass(), ERROR_DESCRIPTION, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, ERR_ID_QUARTZ_SCHEDULER_SCHEDULING, se);
	}
    }

    QuartzSchedulingComparator getQuartzSchedulingComparator() {
	return new QuartzSchedulingComparator();
    }

    @Override
    public boolean hasSameSchedule(IGSConfigurableJob job, Date startDate, GS_JOB_INTERVAL_PERIOD period, int interval) throws GSException {

	JobKey jk = getJobKey(job);

	logger.debug("Checking same schedule of job {} with start {}, period {}, interval {}", jk, startDate, period, interval);

	List<? extends Trigger> triggers;

	synchronized (scheduler) {

	    try {
		triggers = scheduler.getTriggersOfJob(jk);
	    } catch (SchedulerException e) {
		throw GSException.createException(this.getClass(), ERROR_DESCRIPTION, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
			ErrorInfo.SEVERITY_ERROR, ERR_ID_QUARTZ_SCHEDULER_SCHEDULING, e);
	    }
	}

	if (triggers.isEmpty()) {
	    logger.warn("No triggers found for {}, returning false", jk);
	    return false;
	}

	if (triggers.size() > 1) {
	    logger.warn("More than trigger found for {}", jk);
	}

	Optional<? extends Trigger> optional = triggers.stream().filter(trigger -> {

	    logger.trace("Checking trigger {}", trigger);

	    boolean result = getQuartzSchedulingComparator().hasSameSchedule(job, trigger);

	    logger.trace("Result of trigger {} is {}", trigger, result);

	    return result;

	}).findFirst();

	boolean found = optional.isPresent();

	logger.debug("{} schedule of job {} with start {}, period {}, interval {}", found ? "Found same" : "Found different", jk, startDate,
		period, interval);

	return found;

    }

    @Override
    public GSJobResultSet getJobs(GSJobRequest request) throws GSException {

	return null;
    }

    @Override
    public void start(ExecutionMode mode) throws GSException {
	try {

	    new QuartzSchedulerStarter(mode).startScheduler(scheduler);

	} catch (SchedulerException e) {

	    logger.error("Error starting Quartz scheduler");

	    throw GSException.createException(this.getClass(), "Error thrown by Quartz Scheduler", null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, ERR_ID_QUARTZ_SCHEDULER_START, e);
	}
    }

    @Override
    public void stop() throws GSException {

	logger.debug("Stopping scheduler {}", scheduler);

	try {

	    new QuartzSchedulerStarter(null).stopScheduler(scheduler);

	} catch (SchedulerException e) {

	    logger.error("Error stopping Quartz scheduler");

	    throw GSException.createException(this.getClass(), "Error thrown by Quartz Scheduler", null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, ERR_ID_QUARTZ_SCHEDULER_STOP, e);
	}
    }

    @Override
    public boolean isScheduled(IGSConfigurableJob job) throws GSException {

	try {

	    synchronized (scheduler) {
		return scheduler.checkExists(getJobKey(job));
	    }

	} catch (SchedulerException e) {

	    throw GSException.createException(this.getClass(), ERROR_DESCRIPTION, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, ERR_ID_QUARTZ_SCHEDULER_EXIST_SCHEDULE, e);
	}

    }

    @Override
    public void unscheduleJob(IGSConfigurableJob job) throws GSException {
	unscheduleJob(getTriggerKey(job));

    }

    @Override
    public void unscheduleJob(TriggerKey key) throws GSException {
	logger.debug("Unscheduling trigger key {}", key);

	try {

	    synchronized (scheduler) {
		scheduler.unscheduleJob(key);
	    }

	} catch (SchedulerException e) {

	    throw GSException.createException(this.getClass(), ERROR_DESCRIPTION, null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, ERR_ID_QUARTZ_SCHEDULER_UNSCHEDULING, e);
	}

    }

    private TriggerKey getTriggerKey(IGSJob job) {
	return new TriggerKey(job.getId(), job.getGroup());
    }

    JobDataMap doPrepareDataMap(IGSJob job, Map<String, Object> map) {

	JobDataMap dataMap = new JobDataMap(map);

	dataMap.put(GS_JOBS_CONSTANTS.JOBKEY_KEY.toString(), job.getId());

	return dataMap;
    }

    private Date doScheduleJob(IGSConfigurableJob gsjob, IGSConfigurable configurable, JobDataMap dataMap, GSUser user, Scheduler scheduler)
	    throws SchedulerException, GSException {

	Job qJob;
	synchronized (lockObj) {
	    qJob = builder.toQuartzJob(gsjob, configurable, dataMap);
	}

	JobKey jobKey = getJobKey(gsjob);
	JobDetail job = JobBuilder.//
		newJob(qJob.getClass()).//
		withIdentity(jobKey).//
		usingJobData(dataMap).//
		requestRecovery(true).//
		build();

	Optional<SimpleScheduleBuilder> schBuilder = createBuilder(gsjob);

	Date startDate = GSJobUtils.nextStartDate(gsjob);

	Trigger trigger;

	TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.//
		newTrigger().//
		withIdentity(getTriggerKey(gsjob)).//
		startAt(startDate);

	if (schBuilder.isPresent()) {
	    // additionally, schedule to repeat the job forever
	    trigger = triggerBuilder.//
		    withSchedule(schBuilder.get().repeatForever()).//
		    build();
	} else {
	    trigger = triggerBuilder.//
		    build();
	}

	GSLoggerFactory.getLogger(getClass()).info("Going to schedule a job");

	String[] keys = job.getJobDataMap().getKeys();
	for (String key : keys) {
	    Object obj = job.getJobDataMap().get(key);
	    if (obj instanceof RequestMessage) {
		RequestMessage message = (RequestMessage) obj;
		List<GSSource> sources = message.getSources();
		List<GSSource> newSources = new ArrayList<GSSource>();
		for (GSSource source : sources) {
		    GSSource gss = new GSSource();
		    gss.setBrokeringStrategy(source.getBrokeringStrategy());
		    gss.setEndpoint(source.getEndpoint());
		    gss.setLabel(source.getLabel());
		    gss.setOrderingDirection(source.getOrderingDirection());
		    gss.setOrderingProperty(source.getOrderingProperty());
		    gss.setResultsPriority(source.getResultsPriority());
		    gss.setUniqueIdentifier(source.getUniqueIdentifier());
		    gss.setVersion(source.getVersion());
		    if (source.getBrokeringStrategy().equals(BrokeringStrategy.HARVESTED)) {
			// for harvested sources is useless to have the source accessor
			// a cloned gs source is sufficient
			newSources.add(gss);
		    } else {
			newSources.add(source);
		    }
		}
		sources.clear();
		sources.addAll(newSources);
	    }
	}

	Date ret = scheduler.scheduleJob(job, trigger);

	return ret;
    }

    private void updateJobMap(Scheduler scheduler, JobDetail detail, JobDataMap newDataMap) throws SchedulerException {

	JobDetail newDetail = detail.getJobBuilder().withIdentity(detail.getKey()).usingJobData(newDataMap).build();

	scheduler.addJob(newDetail, true, true);

    }

    @Override
    public void updateJobMap(JobDetail detail, JobDataMap newDataMap) {

	try {
	    updateJobMap(scheduler, detail, newDataMap);
	} catch (SchedulerException e) {
	    logger.warn("Unable to update job data map of {}", detail.getKey(), e);
	}

    }

    /**
     * @param gsjob
     * @return
     */
    private Optional<SimpleScheduleBuilder> createBuilder(IGSJob gsjob) {

	GS_JOB_INTERVAL_PERIOD period = gsjob.getIntervalPeriod();

	if (period == null)
	    return Optional.empty();

	SimpleScheduleBuilder schBuilder = SimpleScheduleBuilder.simpleSchedule();

	switch (period) {

	case MONTHS:
	    schBuilder.withIntervalInHours(gsjob.getInterval() * 24 * 30);
	    break;
	case WEEKS:
	    schBuilder.withIntervalInHours(gsjob.getInterval() * 24 * 7);
	    break;
	case DAYS:
	    schBuilder.withIntervalInHours(gsjob.getInterval() * 24);
	    break;
	case HOURS:
	    schBuilder.withIntervalInHours(gsjob.getInterval());
	    break;
	case MINUTES:
	    schBuilder.withIntervalInMinutes(gsjob.getInterval());
	    break;
	case SECONDS:
	    schBuilder.withIntervalInSeconds(gsjob.getInterval());
	    break;
	default:
	    break;
	}

	return Optional.of(schBuilder);
    }

    private JobKey getJobKey(IGSJob gsjob) {

	return new JobKey(gsjob.getId(), gsjob.getGroup());
    }

    public void setQuartzJobBuilderMediator(QuartzJobBuilderMediator builderMediator) {

	builder = builderMediator;
    }
}
