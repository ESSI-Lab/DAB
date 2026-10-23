package eu.essi_lab.cfga.gs;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTimeZone;
import org.quartz.ObjectAlreadyExistsException;

import eu.essi_lab.cfga.option.ISODateTime;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.scheduler.SchedulerUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * Periodically checks that the enabled worker settings scheduled to run indefinitely (custom tasks, harvesting and
 * augmenter workers) have a job in the scheduler, and schedules the missing ones.<br>
 * The job of such a setting is expected to always exist, so a missing job means that it has been lost (e.g. by a failed
 * rescheduling, or by a job canceled because of an outdated configuration). Settings run once or for a given number of
 * times are not checked, since their job is deleted by the scheduler when the last execution ends.<br>
 * The check should run on a single node, the one which schedules the workers (the configuration node)
 *
 * @author boldrini
 */
public class SchedulingChecker {

    /**
     * Interval between two checks
     */
    private static final long CHECK_INTERVAL_MS = ConfigurationWrapper.CONFIG_RELOAD_TIME_UNIT
	    .toMillis(ConfigurationWrapper.CONFIG_RELOAD_TIME * 2);

    private static final SchedulingChecker INSTANCE = new SchedulingChecker();

    private Timer timer;

    private SchedulingChecker() {
    }

    /**
     * @return
     */
    public static SchedulingChecker getInstance() {

	return INSTANCE;
    }

    /**
     * Starts the periodic check, the first one after {@link #CHECK_INTERVAL_MS}, so that the scheduler has completed
     * the possible recovery of the jobs
     */
    public synchronized void start() {

	if (timer != null) {

	    return;
	}

	GSLoggerFactory.getLogger(getClass()).info("Scheduling checker started, check interval: {} minutes",
		TimeUnit.MILLISECONDS.toMinutes(CHECK_INTERVAL_MS));

	timer = new Timer("SchedulingChecker", true);

	timer.schedule(new TimerTask() {

	    @Override
	    public void run() {

		try {

		    check(SchedulerFactory.getScheduler(ConfigurationWrapper.getSchedulerSetting()));

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(SchedulingChecker.class).error("Scheduling check failed: {}", e.getMessage(), e);
		}
	    }

	}, CHECK_INTERVAL_MS, CHECK_INTERVAL_MS);
    }

    /**
     * Schedules the enabled worker settings scheduled to run indefinitely which have no job in the given scheduler
     *
     * @param scheduler
     * @return the scheduled settings
     * @throws Exception
     */
    public List<SchedulerWorkerSetting> check(Scheduler scheduler) throws Exception {

	List<SchedulerWorkerSetting> settings = new ArrayList<>();
	settings.addAll(ConfigurationWrapper.getCustomTaskSettings());
	settings.addAll(ConfigurationWrapper.getHarvestingSettings());
	settings.addAll(ConfigurationWrapper.getAugmenterWorkerSettings());

	DateTimeZone timeZone = ConfigurationWrapper.getSchedulerSetting().getUserDateTimeZone();

	List<SchedulerWorkerSetting> scheduled = new ArrayList<>();

	for (SchedulerWorkerSetting setting : settings) {

	    if (!isExpectedToBeScheduled(setting.getScheduling(), timeZone)) {

		continue;
	    }

	    if (scheduler.getQuartzScheduler().checkExists(SchedulerUtils.createJobKey(setting))) {

		continue;
	    }

	    GSLoggerFactory.getLogger(getClass()).warn("Worker '{}' ({}) is enabled to run indefinitely, but it is not scheduled. Scheduling it",
		    setting.getName(), setting.getIdentifier());

	    try {

		scheduler.schedule(setting);

		scheduled.add(setting);

	    } catch (ObjectAlreadyExistsException ex) {

		// scheduled in the meantime, e.g. by a configuration change
		GSLoggerFactory.getLogger(getClass()).info("Worker '{}' already scheduled", setting.getName());

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error("Unable to schedule worker '{}': {}", setting.getName(), ex.getMessage(), ex);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Scheduling check ended. Checked settings: {}. Scheduled: {}", settings.size(),
		scheduled.size());

	return scheduled;
    }

    /**
     * @param scheduling
     * @param timeZone
     * @return <code>true</code> if the scheduling is enabled, set to run indefinitely, and not ended
     */
    private boolean isExpectedToBeScheduled(Scheduling scheduling, DateTimeZone timeZone) {

	if (!scheduling.isEnabled() || !scheduling.isRunIndefinitelySet()) {

	    return false;
	}

	Optional<ISODateTime> endTime = scheduling.getEndTime();

	return endTime.isEmpty() || ISO8601DateTimeUtils.toGMTDateTime(endTime.get().getValue(), timeZone).after(new Date());
    }
}
