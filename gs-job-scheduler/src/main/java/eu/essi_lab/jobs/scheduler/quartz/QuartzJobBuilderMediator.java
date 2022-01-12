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

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.essi_lab.configuration.ConfigurableKey;
import eu.essi_lab.configuration.IGSConfigurationReader;
import eu.essi_lab.jobs.configuration.IGSConfigurableJob;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
public class QuartzJobBuilderMediator {

    /**
     * IMPORTANT: changing the values of CONFIGURABLE_KEY and/or CONCRETE_JOB_KEY will require to reschedule all existing jobs.
     */
    public static final String CONFIGURABLE_KEY = "GLOBAL_CONFIGURABLE_KEY";
    public static final String CONCRETE_JOB_KEY = "JOB_CLASS_KEY";

    private Logger logger = LoggerFactory.getLogger(QuartzJobBuilderMediator.class);
    private IGSConfigurationReader reader;
    private static final String CANT_CREATE_JOB_GLOBAL_KEY = "CANT_CREATE_JOB_GLOBAL_KEY";

    public QuartzJobBuilderMediator() {
	//nothing to do here
    }

    public Job toQuartzJob(IGSConfigurableJob job, IGSConfigurable configurable, JobDataMap dataMap) throws GSException {

	logger.debug("Requested to create Quartz job of {} with id {} and group {}", job.getInstantiableClass(), job.getId(),
		job.getGroup());

	ConfigurableKey jobkey = getConfigurationReader().getConfigurableKey(job);

	if (jobkey == null)
	    throw GSException.createException(getClass(),
		    "Can't create global key for job " + job.getInstantiableClass() + " with id " + job.getId() + " and group " + job
			    .getGroup(), null, ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, CANT_CREATE_JOB_GLOBAL_KEY);

	String jobkeyString = jobkey.keyString();

	logger.trace("Global key of job {} with id {} and group {} is {}", job.getInstantiableClass(), job.getId(), job.getGroup(),
		jobkeyString);

	dataMap.put(CONCRETE_JOB_KEY, jobkeyString);

	if (configurable == null) {

	    logger.warn("Scheduling a batch job with no configurable {}", job.getId());

	} else {

	    ConfigurableKey key = getConfigurationReader().getConfigurableKey(configurable);

	    String kString = key.keyString();

	    logger.debug("Configurable key for job id {} is {}", job.getId(), kString);

	    dataMap.put(CONFIGURABLE_KEY, kString);
	}

	return new GSQuartzJob();
    }

    public void setConfigurationReader(IGSConfigurationReader confReader) {
	this.reader = confReader;
    }

    public IGSConfigurationReader getConfigurationReader() {
	return reader;
    }

}
