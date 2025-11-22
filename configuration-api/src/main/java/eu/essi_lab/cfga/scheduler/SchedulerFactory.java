/**
 *
 */
package eu.essi_lab.cfga.scheduler;

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

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import eu.essi_lab.cfga.scheduler.impl.MySQLConnectionManager;
import eu.essi_lab.cfga.scheduler.impl.PersistentJobStoreScheduler;
import eu.essi_lab.cfga.scheduler.impl.VolatileJobStoreScheduler;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting.JobStoreType;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class SchedulerFactory {

    private static final StdSchedulerFactory FACTORY = new StdSchedulerFactory();

    private static final String AUGMENTER_BATCH_THREAD_COUNT = "20";

    /**
     *
     */
    private static Scheduler scheduler;

    /**
     * @param setting
     * @return
     */
    public synchronized static Scheduler getScheduler(SchedulerSetting setting) {

	return getScheduler(setting, true);
    }

    /**
     * @param setting
     * @param start
     * @return
     */
    public synchronized static Scheduler getScheduler(SchedulerSetting setting, boolean start) {

	return switch (setting.getJobStoreType()) {
	    case VOLATILE -> getVolatileScheduler(setting, start);
	    case PERSISTENT -> getPersistentScheduler(setting, start);
	};
    }

    /**
     * @return
     */
    public synchronized static Scheduler getVolatileScheduler() {

	return getVolatileScheduler(new SchedulerSetting());
    }

    /**
     * @param setting
     * @return
     */
    public synchronized static Scheduler getVolatileScheduler(SchedulerSetting setting) {

	return getVolatileScheduler(setting, true);
    }

    /**
     * @param setting
     * @param start
     * @return
     */
    public synchronized static Scheduler getVolatileScheduler(SchedulerSetting setting, boolean start) {

	try {

	    if (scheduler == null) {

		GSLoggerFactory.getLogger(SchedulerFactory.class).info("Creating new volatile scheduler");

		scheduler = createVolatileScheduler();

		scheduler.start();

	    } else if (scheduler instanceof PersistentJobStoreScheduler) {

		GSLoggerFactory.getLogger(SchedulerFactory.class).info("Shutting down persistent scheduler");

		scheduler.shutdown();

		GSLoggerFactory.getLogger(SchedulerFactory.class).info("Creating new volatile scheduler");

		scheduler = createVolatileScheduler();

		scheduler.start();
	    }
	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(SchedulerFactory.class).error(ex.getMessage(), ex);

	    throw new RuntimeException(ex.getMessage());
	}

	GSLoggerFactory.getLogger(Scheduler.class).debug("User date time zone: {}", setting.getUserDateTimeZone());

	scheduler.setUserDateTimeZone(setting.getUserDateTimeZone());

	return scheduler;
    }

    /**
     * @param setting
     * @param start
     * @return
     */
    public synchronized static Scheduler getPersistentScheduler(SchedulerSetting setting) {

	return getPersistentScheduler(setting, true);
    }

    /**
     * @param setting
     * @return
     * @throws SQLException
     */
    public synchronized static Scheduler getPersistentScheduler(SchedulerSetting setting, boolean start) {

	try {

	    switch (scheduler) {
	    case null -> {

		GSLoggerFactory.getLogger(SchedulerFactory.class).info("Creating new persistent scheduler");

		scheduler = createPersistentScheduler(setting);

		if (start) {
		    scheduler.start();
		}
	    }
	    case VolatileJobStoreScheduler volatileJobStoreScheduler -> {

		GSLoggerFactory.getLogger(SchedulerFactory.class).info("Shutting down volatile scheduler");

		scheduler.shutdown();

		GSLoggerFactory.getLogger(SchedulerFactory.class).info("Creating new persistent scheduler");

		scheduler = createPersistentScheduler(setting);

		if (start) {
		    scheduler.start();
		}
	    }
	    case PersistentJobStoreScheduler sch -> {

		SchedulerSetting schSetting = sch.getSetting();

		if (!schSetting.equals(setting)) {

		    GSLoggerFactory.getLogger(SchedulerFactory.class).info("Shutting down persistent scheduler");

		    scheduler.shutdown();

		    GSLoggerFactory.getLogger(SchedulerFactory.class).info("Updating persistent scheduler with new settings");

		    scheduler = createPersistentScheduler(setting);

		    if (start) {
			scheduler.start();
		    }
		}
	    }
	    default -> {
	    }
	    }
	} catch (SchedulerException ex) {

	    GSLoggerFactory.getLogger(SchedulerFactory.class).error(ex.getMessage(), ex);

	    throw new RuntimeException(ex.getMessage());
	}

	GSLoggerFactory.getLogger(Scheduler.class).debug("User date time zone: {}", setting.getUserDateTimeZone());

	scheduler.setUserDateTimeZone(setting.getUserDateTimeZone());

	return scheduler;
    }

    /**
     * @return
     * @throws SchedulerException
     */
    private static VolatileJobStoreScheduler createVolatileScheduler() throws SchedulerException {

	org.quartz.Scheduler quartzScheduler = createQuartzScheduler(null);

	return new VolatileJobStoreScheduler(quartzScheduler);
    }

    /**
     * @param setting
     * @return
     * @throws SchedulerException
     * @throws SQLException
     */
    private static PersistentJobStoreScheduler createPersistentScheduler(SchedulerSetting setting) throws SchedulerException {

	org.quartz.Scheduler quartzScheduler = createQuartzScheduler(setting);

	return new PersistentJobStoreScheduler(quartzScheduler, setting);
    }

    /**
     * @param setting
     * @return
     * @throws SchedulerException
     */
    private static org.quartz.Scheduler createQuartzScheduler(SchedulerSetting setting) throws SchedulerException {

	JobStoreType jobStoreType = setting == null ? JobStoreType.VOLATILE : setting.getJobStoreType();

	Properties properties = loadProperties(jobStoreType);

	switch (jobStoreType) {
	case VOLATILE:

	    // any other options can be set here
	    break;

	case PERSISTENT:

	    String mySQLDatabaseURI = setting.getSQLDatabaseUri();
	    String mySQLDatabaseName = setting.getSQLDatabaseName();
	    String user = setting.getSQLDatabaseUser();
	    String password = setting.getSQLDatabasePassword();

	    String quartzDSURL = MySQLConnectionManager.createConnectionURL(mySQLDatabaseURI, mySQLDatabaseName);

	    properties.setProperty("org.quartz.dataSource.myDS.user", user);
	    properties.setProperty("org.quartz.dataSource.myDS.password", password);
	    properties.setProperty("org.quartz.dataSource.myDS.URL", quartzDSURL);

	    String slotsCout = String.valueOf(setting.getSlotsCout());
	    GSLoggerFactory.getLogger(SchedulerFactory.class).debug("Default thread pool count set to: {}", slotsCout);

	    if (ExecutionMode.get() == ExecutionMode.AUGMENTER) {

		GSLoggerFactory.getLogger(SchedulerFactory.class)
			.debug("Thread pool count for augmenter execution mode set to {}", AUGMENTER_BATCH_THREAD_COUNT);

		slotsCout = AUGMENTER_BATCH_THREAD_COUNT;
	    }

	    properties.setProperty("org.quartz.threadPool.threadCount", slotsCout);

	    break;
	}

	FACTORY.initialize(properties);

	return FACTORY.getScheduler();
    }

    /**
     * @param jobStoreType
     * @return
     */
    private static Properties loadProperties(JobStoreType jobStoreType) {

	InputStream stream = switch (jobStoreType) {
	    case VOLATILE -> SchedulerFactory.class.getClassLoader().getResourceAsStream("ram-quartz.properties");
	    case PERSISTENT -> SchedulerFactory.class.getClassLoader().getResourceAsStream("jdbc-quartz.properties");
	};

	Properties properties = new Properties();
	try {
	    properties.load(stream);
	} catch (IOException e) {
	    GSLoggerFactory.getLogger(SchedulerFactory.class).error(e.getMessage(), e);
	}

	return properties;
    }
}
