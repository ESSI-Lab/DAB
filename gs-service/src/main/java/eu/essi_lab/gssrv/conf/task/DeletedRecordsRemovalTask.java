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

import org.quartz.JobExecutionContext;

import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.model.StorageInfo;

/**
 * @author Fabrizio
 */
public class DeletedRecordsRemovalTask extends AbstractCustomTask {

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Deleted records removal task STARTED");

	StorageInfo databaseURI = ConfigurationWrapper.getStorageInfo();

	DatabaseExecutor executor = DatabaseProviderFactory.getExecutor(databaseURI);

	log(status, "Count deleted records STARTED");

	int count = executor.countDeletedRecords();

	log(status, "Found " + count + " deleted records to remove");

	log(status, "Count deleted records ENDED");

	if (count > 0) {

	    log(status, "Removing deleted records STARTED");

	    executor.clearDeletedRecords();

	    log(status, "Removing deleted records ENDED");
	}

	log(status, "Deleted records removal task ENDED");
    }

    @Override
    public String getName() {

	return "Deleted records removal";
    }

}
