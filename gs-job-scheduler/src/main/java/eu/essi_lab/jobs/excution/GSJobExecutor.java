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

import java.util.Iterator;
import java.util.Optional;

import org.quartz.JobDataMap;
import org.quartz.TriggerKey;
import org.slf4j.Logger;

import eu.essi_lab.configuration.ConfigurableKey;
import eu.essi_lab.configuration.GSConfigurationManager;
import eu.essi_lab.configuration.IGSConfigurationReader;
import eu.essi_lab.configuration.sync.ConfigurationSync;
import eu.essi_lab.jobs.IGSJob;
import eu.essi_lab.jobs.configuration.IGSConfigurableJob;
import eu.essi_lab.jobs.scheduler.GSJobSchedulerFactory;
import eu.essi_lab.jobs.scheduler.IGSJobScheduler;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.GSException;
public class GSJobExecutor {

    public static final String JOB_EXECUTION_EXCEPTION_ID = "JOB_EXECUTION_EXCEPTION_ID";
    public static final String JOB_EXECUTION_RESULT_ID = "JOB_EXECUTION_RESULT_ID";
    public static final String JOB_EXECUTION_LAST_PHASE_ID = "JOB_EXECUTION_LAST_PHASE_ID";

    private transient Logger logger = GSLoggerFactory.getLogger(GSJobExecutor.class);
    private GSJobExecutionPlanBuilder builder;

    /**
     * Triggers the execution of the provided jobclass with provided job data map
     *
     * @param jobclass
     * @param map
     * @param tkey
     */
    public void execute(String jobclass, JobDataMap map, TriggerKey tkey, Boolean recovering) {

	logger.info("Requested execution of job {}", jobclass);

	Optional<IGSConfigurableJob> optionalJob = instantiate(jobclass);

	optionalJob.ifPresent(job -> {

	    Iterator<GSJobExecutionPhase> iterator = builder.getDefaultPlan(job);

	    boolean gotonext = true;

	    GSJobExecutionPhaseResult result = null;

	    GSJobExecutionPhase nextPhase = null;

	    while (iterator.hasNext() && gotonext) {

		nextPhase = iterator.next();

		logger.debug("Execution of phase {} of job {}", nextPhase, job.getId());

		result = nextPhase.doPhase(job, map, recovering);

		logger.debug("Result of phase {} of job {} is {}", nextPhase, job.getId(), result);

		switch (result) {
		case STOP:
		    logger.trace("Detected STOP");

		    gotonext = false;

		    break;
		case PASS:
		    logger.trace("Detected PASS");
		    break;

		default:
		    logger.warn("Found an uncatched phase result executing phase {} of job {}", nextPhase, job.getId());

		}
	    }

	    doFinalizeJobExecution(nextPhase, result, map);

	});

	if (!optionalJob.isPresent()) {

	    logger.info("Unscheduling zombie job {}", tkey);

	    try {

		getScheduler().unscheduleJob(tkey);

	    } catch (GSException e) {

		DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));
	    }

	}
    }

    IGSJobScheduler getScheduler() throws GSException {
	return new GSJobSchedulerFactory().getGSJobScheduler();
    }

    IGSConfigurationReader getConfigurationReader() throws GSException {
	return new GSConfigurationManager(getConfigurationSync());
    }

    ConfigurationSync getConfigurationSync() {
	return ConfigurationSync.getInstance();
    }

    Optional<IGSConfigurableJob> instantiate(String jobclass) {

	try {

	    logger.debug("Instantiating {}", jobclass);

	    IGSConfigurationReader reader = getConfigurationReader();

	    IGSConfigurable configurable = reader.readComponent(new ConfigurableKey(jobclass));

	    if (configurable != null) {

		logger.trace("Found non null configurable for {}", jobclass);

		return Optional.of((IGSConfigurableJob) configurable);

	    }

	    logger.trace("No configurable found for {}", jobclass);

	} catch (GSException e) {

	    logger.warn("Uninstantiable job {}", jobclass);

	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));

	}

	return Optional.empty();

    }

    void doFinalizeJobExecution(GSJobExecutionPhase lastPhase, GSJobExecutionPhaseResult result, JobDataMap map) {

	/**
	 * This is still empty but we might want to implement here, e.g. writing metrics/statistics
	 */

	map.put(JOB_EXECUTION_LAST_PHASE_ID, lastPhase.getClass().getName());
	map.put(JOB_EXECUTION_RESULT_ID, result.toString());

    }

    /**
     * Stops the execution of the job
     */
    public void stop() {
	//TODO
    }

    public GSJobExecutionPlanBuilder getBuilder() {
	return builder;
    }

    public void setBuilder(GSJobExecutionPlanBuilder builder) {
	this.builder = builder;
    }
}
