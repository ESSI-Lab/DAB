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

import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.UUID;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;

import eu.essi_lab.configuration.GIProjectExecutionMode;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.configuration.option.GSConfOptionDBURI;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
public class QuartzSchedulerConfiguration {

    public static final java.lang.String ERR_ID_QUARTZ_SCHEDULER_CONFIGURATION = "ERR_ID_QUARTZ_SCHEDULER_CONFIGURATION";
    public static final String TEST_JOB_GROUP = "testJobGroup";
    StorageUri currentConf;
    private static QuartzSchedulerConfiguration instance;
    private transient Logger logger = GSLoggerFactory.getLogger(this.getClass());
    private StdSchedulerFactory factory;
    private static final java.lang.String ERR_ID_QUARTZ_SCHEDULER_INSTANTIATION = "ERR_ID_QUARTZ_SCHEDULER_INSTANTIATION";

    private QuartzSchedulerConfiguration() {

	factory = new StdSchedulerFactory();
    }

    public static QuartzSchedulerConfiguration getInstance() {
	if (instance == null)
	    instance = new QuartzSchedulerConfiguration();

	return instance;
    }

    public Scheduler getScheduler() throws GSException {
	try {

	    if (currentConf == null) {
		logger.warn("Current Scheduler is storing jobs in memory, every thing will be lost on service reboot. Change the "
			+ "configuration to use a permanent job store.");
	    }

	    return factory.getScheduler();
	} catch (SchedulerException e) {
	    logger.error("Get scheduler failed", e);

	    throw GSException.createException(this.getClass(), "Error thrwon by Quartz Scheduler", null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, ERR_ID_QUARTZ_SCHEDULER_INSTANTIATION, e);
	}
    }

    void update(StorageUri schedulerInfo) throws GSException {

	if (schedulerInfo == null)
	    return;

	logger.trace("Update method was invoked");

	if (currentConf != null && schedulerInfo.toString().equals(currentConf.toString())) {

	    logger.trace("Incoming conf is the same as existing one, returning");

	    return;

	}

	try {
	    logger.debug("Update Scheduler Configuration");

	    Iterator<Scheduler> it = factory.getAllSchedulers().iterator();

	    if (it.hasNext()) {

		Scheduler existing = it.next();

		if (logger.isDebugEnabled())
		    logger.debug("Found existing scheduler {}", printSchedulerInfo(existing));

		synchronized (existing) {

		    if (logger.isDebugEnabled())
			logger.debug("Shutting down existing scheduler {}", printSchedulerInfo(existing));

		    existing.shutdown();

		}

	    }

	    Properties props = new QuartzPropertiesLoader().loadDefaultProperties();

	    /** START Porperties Update **/
	    logger.debug("Default Properites loaded");

	    /** JobStore Properties for JDBC, MySQL **/
	    props.setProperty("org.quartz.jobStore.dataSource", "myDS");
	    props.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
	    props.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
	    props.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
	    props.setProperty("org.quartz.jobStore.isClustered", "true");

	    /** DataSource Properties for MySQL **/
	    props.setProperty("org.quartz.dataSource.myDS.user", schedulerInfo.getUser());
	    props.setProperty("org.quartz.dataSource.myDS.password", schedulerInfo.getPassword());

	    String mySQLDatabaseName = schedulerInfo.getStorageName();
	    String mySQLDatabaseURI = schedulerInfo.getUri();

	    String quartzDSURL = mySQLDatabaseURI + "/" + mySQLDatabaseName;

	    logger.info("Setting DataBase URL to {}", quartzDSURL);
	    props.setProperty("org.quartz.dataSource.myDS.URL", quartzDSURL);

	    /** END Porperties Update **/

	    logger.debug("Initializng factory");

	    factory.initialize(props);

	    logger.debug("Getting scheduler");
	    Scheduler scheduler = factory.getScheduler();

	    if (logger.isDebugEnabled()) {
		logger.debug("Obtained scheduler {}", printSchedulerInfo(scheduler));
		logger.debug("Starting scheduler {}", printSchedulerInfo(scheduler));
	    }

	    new QuartzSchedulerStarter(new GIProjectExecutionMode().getMode()).startScheduler(scheduler);

	    if (!checkScheduler(scheduler))
		throw GSException.createException(this.getClass(), "Couldn't Start Scheduler with conf --> " + schedulerInfo.toString(),
			null, "The provided DataBase Information does not seem to work, please check.", ErrorInfo.ERRORTYPE_INTERNAL,
			ErrorInfo.SEVERITY_ERROR, ERR_ID_QUARTZ_SCHEDULER_CONFIGURATION);

	    currentConf = schedulerInfo;

	} catch (SchedulerException e) {
	    logger.error("Udpate scheduler failed", e);

	    throw GSException.createException(this.getClass(), "Error thrwon by Quartz Scheduler", null, null, ErrorInfo.ERRORTYPE_INTERNAL,
		    ErrorInfo.SEVERITY_ERROR, ERR_ID_QUARTZ_SCHEDULER_CONFIGURATION, e);

	}
    }

    private boolean checkScheduler(Scheduler scheduler) {

	SimpleScheduleBuilder schBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(1);
	TriggerKey triggerKey = new TriggerKey(UUID.randomUUID().toString(), TEST_JOB_GROUP);

	Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).startAt(new Date(new Date().getTime() + 2000L)).withSchedule(
		schBuilder.withRepeatCount(1)).build();

	JobKey jobKey = new JobKey(UUID.randomUUID().toString(), TEST_JOB_GROUP);

	JobDetail testJobDetail = JobBuilder.newJob(SchedulerTestJob.class).withIdentity(jobKey).build();

	try {
	    scheduler.scheduleJob(testJobDetail, trigger);

	    return true;
	} catch (SchedulerException e) {
	    logger.warn("Check scheduler failed", e);
	    return false;
	}

    }

    void update(GSConfOptionDBURI gsConfOptionDBURI) throws GSException {

	if (gsConfOptionDBURI == null && currentConf == null)
	    return;

	if (gsConfOptionDBURI != null) {

	    StorageUri schedulerInfo = gsConfOptionDBURI.getValue();

	    update(schedulerInfo);
	}

    }

    private String printSchedulerInfo(Scheduler sched) throws SchedulerException {

	return "[name: " + sched.getSchedulerName() + "; instanceId: " + sched.getSchedulerInstanceId() + "; object: " + sched + "]";

    }

}
