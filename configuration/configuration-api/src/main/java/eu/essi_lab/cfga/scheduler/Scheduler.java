/**
 * 
 */
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

import java.sql.SQLException;
import java.util.List;

import org.joda.time.DateTimeZone;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;

import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;

/**
 * @author Fabrizio
 */
public interface Scheduler {

    /**
     * @author Fabrizio
     */
    public enum JobEvent {

	/**
	 * 
	 */
	JOB_TO_BE_EXECUTED,
	/**
	 * 
	 */
	JOB_EXECUTED,
	/**
	 * 
	 */
	JOB_VETOED
    }

    @FunctionalInterface
    public interface JobEventListener {

	void eventOccurred(JobEvent event, JobExecutionContext context, JobExecutionException jobException);
    }

    /**
     * @throws SchedulerException
     */
    public void start() throws SchedulerException;

    /**
     * @param minutes
     * @throws SchedulerException
     */
    public void startDelayed(int minutes) throws SchedulerException;

    /**
     * @throws SchedulerException
     */
    public void shutdown() throws SchedulerException;

    /**
     * @param listener
     * @throws SchedulerException
     */
    public void addSchedulerListener(SchedulerListener listener) throws SchedulerException;

    /**
     * @param listener
     * @throws SchedulerException
     */
    public void removeSchedulerListener(SchedulerListener listener) throws SchedulerException;

    /**
     * Register a {@link JobEventListener} to the specified {@link JobEvent}
     * 
     * @param listener
     * @param event
     * @param name the listener name
     * @param autoRemove if <code>true</code> removes the listener after the event is occurred
     * @throws SchedulerException
     */
    public void addJobEventListener(JobEventListener listener, JobEvent event, String name, boolean autoRemove) throws SchedulerException;

    /**
     * Register a {@link JobEventListener} to the all the available {@link JobEvent}
     * 
     * @param listener
     * @param name the listener name
     * @throws SchedulerException
     */
    public void addJobEventListener(JobEventListener listener, String name) throws SchedulerException;

    /**
     * @param listenerName
     * @throws SchedulerException
     */
    public void removeJobListener(String listenerName) throws SchedulerException;

    /**
     * @param setting
     * @throws SchedulerException
     */
    public void schedule(SchedulerWorkerSetting setting) throws SchedulerException;

    /**
     * @param setting
     * @throws SchedulerException
     */
    public void pause(SchedulerWorkerSetting setting) throws SchedulerException;

    /**
     * @param setting
     * @throws SchedulerException
     */
    public void reschedule(SchedulerWorkerSetting setting) throws Exception;

    /**
     * @throws SchedulerException
     */
    public void unscheduleAll() throws Exception;

    /**
     * @return
     * @throws SchedulerException
     */
    public List<SchedulerWorkerSetting> listExecutingSettings() throws Exception;

    /**
     * Specific implementation for persistent scheduler should be NOT REQUIRED
     * since the implementation of <code>scheduler.getTriggerKeys</code> and <code>scheduler.getTrigger</code>
     * refers to the <i>getJobStore()</i> which, in case of a clustered DB, goes to the SQL table.<br>
     * Anyway the tests demonstrates the using of JobStore for SQL is very slow, so a
     * specific implementation is provided
     * 
     * @return
     * @throws SchedulerException
     */
    public List<SchedulerWorkerSetting> listScheduledSettings() throws Exception;

    /**
     * @param setting
     * @throws SchedulerException
     */
    public void unschedule(SchedulerWorkerSetting setting) throws SchedulerException;

    /**
     * @param status
     * @throws SQLException
     */
    public void setJobStatus(SchedulerJobStatus status) throws SQLException;

    /**
     * @return
     * @throws SQLException
     */
    public List<SchedulerJobStatus> getJobStatuslist() throws SQLException;

    /**
     * @return
     */
    public org.quartz.Scheduler getQuartzScheduler();

    /**
     * 
     */
    public void setUserDateTimeZone(DateTimeZone dateTimeZone);
}
