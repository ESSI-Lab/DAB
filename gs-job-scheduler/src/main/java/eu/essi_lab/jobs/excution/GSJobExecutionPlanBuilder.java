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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;

import eu.essi_lab.jobs.IGSJob;
import eu.essi_lab.jobs.scheduler.GSJobSchedulerFactory;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.GSException;
public class GSJobExecutionPlanBuilder {

    private transient Logger logger = GSLoggerFactory.getLogger(GSJobExecutionPlanBuilder.class);

    private GSJobSchedulerFactory factory;

    public GSJobExecutionPlanBuilder() {
    }

    public Iterator<GSJobExecutionPhase> getDefaultPlan(IGSJob job) {
	List<GSJobExecutionPhase> phases = new ArrayList<>();

	if (job != null) {

	    GSJobExecutionPhase validator = new GSJobValidator();

	    GSJobValidationHandler handler = new GSJobValidationHandler();

	    try {

		handler.setScheduler(factory.getGSJobScheduler());

		((GSJobValidator) validator).setHandler(handler);

		phases.add(validator);

	    } catch (GSException e) {
		logger.warn(
			"Unable to obtain scheduler for the GSJobValidationHandler (see exception below), returning a plan with no validaiton phase");

		DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));
	    }

	    phases.add(new GSJobRunner());
	}
	return phases.iterator();
    }

    public GSJobSchedulerFactory getFactory() {
	return factory;
    }

    public void setFactory(GSJobSchedulerFactory factory) {
	this.factory = factory;
    }
}
