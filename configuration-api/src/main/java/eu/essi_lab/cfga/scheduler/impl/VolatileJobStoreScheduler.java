package eu.essi_lab.cfga.scheduler.impl;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.scheduler.SchedulerUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class VolatileJobStoreScheduler extends AbstractScheduler {

    /**
     * 
     */
    private List<SchedulerJobStatus> jobStatusList;

    /**
     * @param quartzScheduler
     */
    public VolatileJobStoreScheduler() {

	this(createDefaultScheduler());
    }

    /**
     * @param quartzScheduler
     */
    public VolatileJobStoreScheduler(Scheduler quartzScheduler) {

	super(quartzScheduler);

	jobStatusList = new ArrayList<>();
    }

    @Override
    public synchronized List<SchedulerWorkerSetting> listScheduledSettings() throws Exception {

	ArrayList<SchedulerWorkerSetting> out = new ArrayList<>();

	for (TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup())) {

	    Trigger trigger = scheduler.getTrigger(triggerKey);
	    // trigger can be null if the meantime it was unscheduled
	    if (trigger != null) {
		JobDataMap jobDataMap = trigger.getJobDataMap();

		SchedulerWorkerSetting setting = new SchedulerWorkerSetting(jobDataMap.getString("setting"));

		setting.setNextFireTime(trigger.getNextFireTime());

		out.add(setting);
	    }
	}

	return out;
    }

    /**
     * @return
     * @throws SchedulerException
     */
    @Override
    public synchronized List<SchedulerWorkerSetting> listExecutingSettings() throws Exception {

	List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();

	return executingJobs.//
		stream().//
		map(context -> {

		    Date fireTime = context.getFireTime();

		    SchedulerWorkerSetting setting = SchedulerUtils.getSetting(context);

		    setting.setFiredTime(fireTime);

		    return setting;
		}).//
		collect(Collectors.toList());
    }

    /**
     * @return
     */
    private static org.quartz.Scheduler createDefaultScheduler() {

	try {
	    return StdSchedulerFactory.getDefaultScheduler();
	} catch (SchedulerException e) {

	    GSLoggerFactory.getLogger(VolatileJobStoreScheduler.class).error(e.getMessage(), e);
	}

	return null;
    }

    @Override
    public void setJobStatus(SchedulerJobStatus status) {

	Optional<SchedulerJobStatus> oldStatus = jobStatusList.stream().filter(s -> s.getJobIdentifier().equals(status.getJobIdentifier()))
		.findFirst();

	if (oldStatus.isPresent()) {
	    jobStatusList.remove(oldStatus.get());
	}

	jobStatusList.add(status);
    }

    @Override
    public List<SchedulerJobStatus> getJobStatuslist() {

	return jobStatusList;
    }
}
