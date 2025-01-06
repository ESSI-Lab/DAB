package eu.essi_lab.gssrv.conf.task;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.SchedulerViewSetting;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.scheduler.impl.PersistentJobStoreScheduler;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting.JobStoreType;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class SchedulerJobDataChecker extends AbstractCustomTask {

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Scheduler job data checker STARTED");

	CustomTaskSetting taskSetting = retrieveSetting(context);

	//
	//
	//

	SchedulerViewSetting schedulerSetting = ConfigurationWrapper.getSchedulerSetting();

	if (schedulerSetting.getJobStoreType() == JobStoreType.VOLATILE) {

	    log(status, "Volatile scheduler configured, persistent scheduler required. Unable to perform task");

	    return;
	}

	PersistentJobStoreScheduler scheduler = (PersistentJobStoreScheduler) SchedulerFactory.getPersistentScheduler(schedulerSetting);

	try {
	    List<SimpleEntry<String, String>> checkJobData = scheduler.checkJobData();

	    if (checkJobData.isEmpty()) {

		log(status, "No errors found");

	    } else {

		log(status, "Errors found: " + checkJobData.size());

		checkJobData.forEach(e -> log(status, "Job with error: " + e.getKey() + ", table: " + e.getValue()));
	    }

	} catch (Throwable e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    log(status, "Error occurred: " + e.getMessage());
	}

	log(status, "Scheduler job data checker ENDED");
    }

    @Override
    public String getName() {

	return "Scheduler job data checker";
    }

}
