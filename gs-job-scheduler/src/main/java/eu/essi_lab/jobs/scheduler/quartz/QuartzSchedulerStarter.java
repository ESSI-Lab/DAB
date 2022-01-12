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

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.jobs.listener.GSJobListener;
import eu.essi_lab.lib.utils.GSLoggerFactory;
public class QuartzSchedulerStarter {

    private final ExecutionMode executionMode;

    public QuartzSchedulerStarter(ExecutionMode mode) {

	executionMode = mode;

    }

    public void startScheduler(Scheduler scheduler) throws SchedulerException {

	GSLoggerFactory.getLogger(getClass()).info("Requested scheduler start with execution mode {}", executionMode);

	if (executionMode.equals(ExecutionMode.FRONTEND)) {

	    GSLoggerFactory.getLogger(getClass()).debug("Execution mode is {}, skipping scheduler start", executionMode);

	    return;
	}

	GSLoggerFactory.getLogger(getClass()).debug("Execution mode is {}, executing start", executionMode);

	scheduler.getListenerManager().addJobListener(new GSJobListener(),
		jobKey -> jobKey.getGroup() != null && !jobKey.getGroup().equals(QuartzSchedulerConfiguration.TEST_JOB_GROUP));

	scheduler.start();
    }

    public void stopScheduler(Scheduler scheduler) throws SchedulerException {
        scheduler.shutdown();
    }

}
