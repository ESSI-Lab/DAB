package eu.essi_lab.jobs.excution;

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

import org.slf4j.Logger;

import eu.essi_lab.jobs.GSJobValidationFailReason;
import eu.essi_lab.jobs.GSJobValidationResult;
import eu.essi_lab.jobs.configuration.IGSConfigurableJob;
import eu.essi_lab.jobs.scheduler.IGSJobScheduler;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
public class GSJobValidationHandler {

    private transient Logger logger = GSLoggerFactory.getLogger(GSJobValidationHandler.class);

    private IGSJobScheduler scheduler;

    public void handleFailure(IGSConfigurableJob job, GSJobValidationResult validationResult) throws GSException {

	GSJobValidationFailReason reason = validationResult.getReason();
	logger.debug("Handling job validation failure of job {} with reason {}", job.getId(), reason);

	boolean knownReason = false;

	switch (reason) {
	case TARGET_SOURCE_NOT_FOUND:
	case TARGET_SOURCE_NOT_MATCHING:
	case NO_CONFIGURABLE_KEY_FOUND:

	    knownReason = true;

	default:
	    if (!knownReason)
		logger.warn("IGSJob validation failed with unknown reason");

	    unscheduleJob(job);

	}

    }

    private void unscheduleJob(IGSConfigurableJob job) throws GSException {
	logger.debug("Unscheduling {}", job.getId());
	scheduler.unscheduleJob(job);
	logger.debug("Successfully unscheduled {}", job.getId());

    }

    public IGSJobScheduler getScheduler() {
	return scheduler;
    }

    public void setScheduler(IGSJobScheduler scheduler) {
	this.scheduler = scheduler;
    }
}
