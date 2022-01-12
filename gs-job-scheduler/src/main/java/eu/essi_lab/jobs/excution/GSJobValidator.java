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

import org.quartz.JobDataMap;
import org.slf4j.Logger;

import eu.essi_lab.jobs.GSJobValidationResult;
import eu.essi_lab.jobs.configuration.IGSConfigurableJob;
import eu.essi_lab.jobs.scheduler.quartz.GSQuartzJob;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
public class GSJobValidator implements GSJobExecutionPhase {

    private transient Logger logger = GSLoggerFactory.getLogger(GSJobValidator.class);
    private GSJobValidationHandler handler;
    private static final String UNKNOWN_GSJOB_VALIDATION_FAILURE_HANDLING_EXECPTION_ERR_ID = "UNKNOWN_GSJOB_VALIDATION_FAILURE_HANDLING_EXECPTION_ERR_ID";

    @Override
    public GSJobExecutionPhaseResult doPhase(IGSConfigurableJob job, JobDataMap map, Boolean recovering) {

	logger.info("Start validation of {}", job.getId());

	GSJobValidationResult validationResult = job.isValid(map);
	GSException gse;

	if (validationResult.isValid()) {

	    logger.debug("Validation passed");

	    return GSJobExecutionPhaseResult.PASS;

	}

	logger.debug("Validation failed with reason {}", validationResult.getReason());

	try {

	    handler.handleFailure(job, validationResult);

	    return GSJobExecutionPhaseResult.STOP;

	} catch (GSException e) {

	    logger.warn("Exception handling validation failure of job {} with reason {}", job.getId(), validationResult.getReason());

	    gse = e;

	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));
	} catch (Exception e) {

	    logger.error("Alien Exception executing job {}", job.getId());

	    gse = GSException.createException(GSQuartzJob.class, "Unknown error during validation failure handling", null,
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, UNKNOWN_GSJOB_VALIDATION_FAILURE_HANDLING_EXECPTION_ERR_ID, e);

	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(gse)));

	}

	map.put(GSJobExecutor.JOB_EXECUTION_EXCEPTION_ID, gse);

	return GSJobExecutionPhaseResult.STOP;
    }

    @Override
    public String toString() {
	return "GS Job Validation Phase";
    }

    public void setHandler(GSJobValidationHandler handler) {
	this.handler = handler;
    }

    public GSJobValidationHandler getHandler() {
	return this.handler;
    }
}
