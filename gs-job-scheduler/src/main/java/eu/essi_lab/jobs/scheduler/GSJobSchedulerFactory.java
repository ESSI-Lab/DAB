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

import org.quartz.Scheduler;
import org.slf4j.Logger;

import eu.essi_lab.configuration.GSConfigurationManager;
import eu.essi_lab.configuration.sync.ConfigurationSync;
import eu.essi_lab.jobs.scheduler.quartz.QuartzJobBuilderMediator;
import eu.essi_lab.jobs.scheduler.quartz.QuartzSchedulerConfiguration;
import eu.essi_lab.jobs.scheduler.quartz.QuartzSchedulerMediator;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.exceptions.GSException;
public class GSJobSchedulerFactory {

    Logger logger = GSLoggerFactory.getLogger(GSJobSchedulerFactory.class);

    public IGSJobScheduler getGSJobScheduler() throws GSException {

	logger.debug("Scheduler");
	GSJobScheduler scheduler = new GSJobScheduler();
	logger.debug("Quartz mediator");
	QuartzSchedulerMediator quartzMediator = new QuartzSchedulerMediator();
	logger.debug("Quartz Scheduler");
	Scheduler quartzScheduler = QuartzSchedulerConfiguration.getInstance().getScheduler();
	quartzMediator.setScheduler(quartzScheduler);
	logger.debug("Builder");
	QuartzJobBuilderMediator builder = new QuartzJobBuilderMediator();
	logger.debug("Configuration sync");
	ConfigurationSync configurationSync = ConfigurationSync.getInstance();
	logger.debug("Configuration sync: " + configurationSync.getClass().getName());
	logger.debug("Configuration");
	GSConfiguration configuration = configurationSync.getClonedConfiguration();
	logger.debug("Manager");
	GSConfigurationManager reader = new GSConfigurationManager(configuration);
	builder.setConfigurationReader(reader);
	quartzMediator.setQuartzJobBuilderMediator(builder);
	scheduler.setMediator(quartzMediator);

	return scheduler;

    }
}
