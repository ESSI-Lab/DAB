package eu.essi_lab.cfga.gs.task;

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

import java.util.concurrent.TimeUnit;

import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;

/**
 * @author Fabrizio
 */
public class DefaultCustomTask extends AbstractCustomTask {

    /**
     * 
     */
    private static final long SLEEP_TIME = TimeUnit.SECONDS.toMillis(15);

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Default custom task STARTED");

	log(status, "This task does nothing and runs for 15 seconds");

	Thread.sleep(SLEEP_TIME);

	log(status, "Default custom task ENDED");
    }

    @Override
    public String getName() {

	return getTaskName();
    }

    /**
     * @return
     */
    public static String getTaskName() {

	return "Default custom task";
    }
}
