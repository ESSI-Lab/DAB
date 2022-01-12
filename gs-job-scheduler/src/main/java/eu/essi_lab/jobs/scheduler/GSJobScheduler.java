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
import eu.essi_lab.jobs.configuration.IGSConfigurableJob;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.exceptions.GSException;
public class GSJobScheduler implements IGSJobScheduler {

    private IGSJobScheduler mediator;

    public GSJobScheduler() {
	//nothing to do here
    }

    public void setMediator(IGSJobScheduler mediator) {
	this.mediator = mediator;
    }

    @Override
    public Date scheduleJob(GSUser user, IGSConfigurableJob job, IGSConfigurable configurable, Map<String, Object> dataMap)
	    throws GSException {

	return mediator.scheduleJob(user, job, configurable, dataMap);

    }

    @Override
    public boolean isScheduled(IGSConfigurableJob job) throws GSException {

	return this.mediator.isScheduled(job);

    }

    @Override
    public void unscheduleJob(IGSConfigurableJob job) throws GSException {
	this.mediator.unscheduleJob(job);
    }

    @Override
    public void unscheduleJob(TriggerKey triggerKey) throws GSException {
	this.mediator.unscheduleJob(triggerKey);
    }

    @Override
    public boolean hasSameSchedule(IGSConfigurableJob job, Date startDate, GS_JOB_INTERVAL_PERIOD period, int interval) throws GSException {
	return mediator.hasSameSchedule(job, startDate, period, interval);
    }

    @Override
    public GSJobResultSet getJobs(GSJobRequest request) throws GSException {
	return null;
    }

    @Override
    public void start(ExecutionMode mode) throws GSException {
	mediator.start(mode);
    }

    @Override
    public void stop() throws GSException {
	mediator.stop();
    }

    @Override
    public void updateJobMap(JobDetail detail, JobDataMap newDataMap) {
	mediator.updateJobMap(detail, newDataMap);
    }

}
