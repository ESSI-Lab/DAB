package eu.essi_lab.jobs.scheduler;

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
import java.util.Map;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.TriggerKey;

import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.jobs.GSJobRequest;
import eu.essi_lab.jobs.GSJobResultSet;
import eu.essi_lab.jobs.IGSJob;
import eu.essi_lab.jobs.configuration.IGSConfigurableJob;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.exceptions.GSException;
public interface IGSJobScheduler {

    Date scheduleJob(GSUser user, IGSConfigurableJob job, IGSConfigurable configurable, Map<String, Object> jobDataMap) throws GSException;

    boolean isScheduled(IGSConfigurableJob job) throws GSException;

    void unscheduleJob(IGSConfigurableJob job) throws GSException;

    void unscheduleJob(TriggerKey triggerKey) throws GSException;

    /**
     * Checks if the provided {@link IGSJob} has the same schedule as the one defined by the provided startDate, period and interval. This
     * method assumes that the provided {@link IGSJob} is scheduled, i.e. {@link #isScheduled(IGSConfigurableJob)} returns true.
     *
     * @param job
     * @param startDate
     * @param period
     * @param interval
     * @return
     * @throws GSException
     */
    boolean hasSameSchedule(IGSConfigurableJob job, Date startDate, GS_JOB_INTERVAL_PERIOD period, int interval) throws GSException;

    /**
     * Retrieves the {@link eu.essi_lab.jobs.GSJobStatus}es of the {@link IGSJob}s matching the constraints defined in the provided {@link
     * GSJobRequest}
     *
     * @throws GSException
     */
    GSJobResultSet getJobs(GSJobRequest request) throws GSException;

    void start(ExecutionMode mode) throws GSException;

    void stop() throws GSException;

    void updateJobMap(JobDetail detail, JobDataMap newDataMap);

}
