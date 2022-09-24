package eu.essi_lab.gssrv.starter;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ext.RuntimeDelegate;
import javax.xml.bind.JAXBException;

import org.quartz.SchedulerException;

import eu.essi_lab.augmenter.worker.AugmentationReportsHandler;
import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.ConfigurationSource;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.checker.ConfigurationChecker;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.DefaultConfiguration.MainSettingsIdentifier;
import eu.essi_lab.cfga.gs.DefaultPreProdConfiguration;
import eu.essi_lab.cfga.gs.DefaultProdConfiguration;
import eu.essi_lab.cfga.gs.demo.DemoConfiguration;
import eu.essi_lab.cfga.gs.setting.SchedulerViewSetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.SchedulerSupport;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting.JobStoreType;
import eu.essi_lab.cfga.source.FileSource;
import eu.essi_lab.cfga.source.MarkLogicSource;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.configuration.GIProjectExecutionMode;
import eu.essi_lab.gssrv.health.HealthCheck;
import eu.essi_lab.gssrv.servlet.ServletListener;
import eu.essi_lab.harvester.HarvestingReportsHandler;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.shared.driver.es.stats.ElasticsearchInfoPublisher;

/**
 * @author Fabrizio
 */
public class GISuiteStarter {

    private static final String JAXB_INIT_ERR_ID = "ERR_ID_JAXB_INIT";
    private static final String CONF_INITIALIZATION_ALIEN_ERR_ID = "CONF_INITIALIZATION_ALIEN_ERROR";
    private static final String CONF_CHECK_FAILED_ERROR_ID = "CONF_CHECK_FAILED_ERROR_ERROR";

    private static final String STARTUP_HEALTH_CHECK_FAILED_ERR_ID = "STARTUP_HEALTH_CHECK_FAILED_ERR_ID";
    private static final String SCHEDULER_START_ERR_ID = "SCHEDULER_START_ERR_ID";
    private static final String CONF_FILE_MISSING_OR_EMPTY_ERR_ID = "CONF_FILE_MISSING_OR_EMPTY_ERR_ID";

    /**
     * Reload every 5 minutes
     */
    private static final int CONFIG_RELOAD_TIME = 300;

    /**
     * 
     */
    public static Configuration configuration;

    /**
     * 
     */
    private final ExecutionMode mode;

    /**
     * 
     */
    public GISuiteStarter() {

	GSLoggerFactory.getLogger(GISuiteStarter.class).info("Retrieving execution mode");

	mode = GIProjectExecutionMode.getMode();

	GSLoggerFactory.getLogger(GISuiteStarter.class).info("GI-suite is starting in execution mode {}", mode);
    }

    /**
     * @param confConnector
     * @throws GSException
     */
    public void start() throws GSException {

	//
	// see GIP-235
	//
	RuntimeDelegate.setInstance(new org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl());

	initConfig();

	checkConfig();

	applySystemSettings();

	initLocale();

	initJaxb();

	checkServiceLoader();

	healthCheckTest();

	if (mode != ExecutionMode.FRONTEND) {

	    startScheduler();
	}
    }

    /**
     * @throws GSException
     */
    private void initConfig() throws GSException {

	try {

	    GSLoggerFactory.getLogger(GISuiteStarter.class).info("Initializing configuration STARTED");

	    //
	    // 1) Retrieves the configuration.url parameter
	    //

	    String configURL = System.getProperty("configuration.url");
	    if (configURL == null) {
		configURL = System.getenv("configuration.url");
	    }

	    if (configURL == null || configURL.isEmpty()) {

		GSLoggerFactory.getLogger(GISuiteStarter.class).warn("Configuration URL not found, using fallback URL: local FS temp");
		configURL = "file:temp";
	    }

	    String[] split = configURL.split("!");

	    GSLoggerFactory.getLogger(GISuiteStarter.class).info("Configuration URL: {}", configURL);

	    //
	    // 2) Creates the source
	    //

	    Configuration configuration = null;

	    String newConfigName = null;

	    ConfigurationSource source = null;

	    String configFileName = "gs-configuration";

	    if (configURL.startsWith("xdbc:")) {

		//
		// xdbc://user:password@hostname:8000,8004/dbName/folder/
		//

		source = new MarkLogicSource(split[0], configFileName);

	    } else {

		if (configURL.startsWith("file:temp")) {

		    //
		    // -Dconfiguration.url=file:temp
		    // -Dconfiguration.url=file:temp demo
		    //

		    GSLoggerFactory.getLogger(GISuiteStarter.class).info("Local FS configuration on the temp user directory");

		    source = new FileSource(configFileName);

		} else if (configURL.startsWith("file://")) {

		    //
		    // -Dconfiguration.url=file://path/preprodenvconf/
		    // -Dconfiguration.url=file://path/preprodenvconf!demo
		    //
		    GSLoggerFactory.getLogger(GISuiteStarter.class).info("Local FS configuration on given path");

		    String path = split[0].replace("file://", "");
		    path = path + File.separator + configFileName + ".json";

		    source = new FileSource(new File(path));
		}
	    }

	    //
	    // 3) determines the configuration name to use in case of missing source
	    // xdbc://user:password@hostname:8000,8004/dbName/folder/!default-prod
	    //
	    newConfigName = split.length == 1 ? "default" : split[1];

	    if (source.isEmptyOrMissing()) {

		if (mode != ExecutionMode.MIXED) {

		    throw GSException.createException(//
			    ServletListener.class, //
			    "Configuration file empty or missing", //
			    null, //
			    null, //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_FATAL, //
			    CONF_FILE_MISSING_OR_EMPTY_ERR_ID);
		}

		switch (newConfigName) {

		case "default":

		    GSLoggerFactory.getLogger(GISuiteStarter.class).info("Creating and flushing new default configuration");

		    configuration = new DefaultConfiguration(source, TimeUnit.SECONDS, CONFIG_RELOAD_TIME);

		    break;

		case "default-prod":

		    GSLoggerFactory.getLogger(GISuiteStarter.class).info("Creating and flushing new default PROD configuration");

		    configuration = new DefaultProdConfiguration(source, TimeUnit.SECONDS, CONFIG_RELOAD_TIME);

		    break;

		case "default-preprod":

		    GSLoggerFactory.getLogger(GISuiteStarter.class).info("Creating and flushing new default PRE-PROD configuration");

		    configuration = new DefaultPreProdConfiguration(source, TimeUnit.SECONDS, CONFIG_RELOAD_TIME);

		    break;

		case "demo":

		    GSLoggerFactory.getLogger(GISuiteStarter.class).info("Creating and flushing new demo configuration");

		    configuration = new DemoConfiguration(source, TimeUnit.SECONDS, CONFIG_RELOAD_TIME);

		    break;
		}

		SelectionUtils.deepClean(configuration);

		SelectionUtils.deepAfterClean(configuration);

		configuration.flush();

	    } else {

		GSLoggerFactory.getLogger(GISuiteStarter.class).info("Creating configuration from existing source");
		configuration = new Configuration(source, TimeUnit.SECONDS, CONFIG_RELOAD_TIME);
	    }

	    //
	    // in execution mode CONFIGURATION and MIXED there is no need to autoreload
	    // since there is only one node, no shared configuration (also in LOCAL_PRODUCTION mode
	    // but this is done in the next rows...)
	    //
	    if (mode == ExecutionMode.CONFIGURATION || mode == ExecutionMode.MIXED) {

		configuration.pauseAutoreload();
	    }

	    //
	    //
	    //
	    if (mode == ExecutionMode.LOCAL_PRODUCTION) {

		GSLoggerFactory.getLogger(GISuiteStarter.class).info("Creating local config with VOLATILE job store STARTED");

		// pause the autoreload to the current config instance, probably not required...
		configuration.pauseAutoreload();

		SchedulerViewSetting schedulerSetting = configuration.get(//
			MainSettingsIdentifier.SCHEDULER.getLabel(), //
			SchedulerViewSetting.class).get();

		JobStoreType jobStoreType = schedulerSetting.getJobStoreType();

		if (jobStoreType == JobStoreType.PERSISTENT) {

		    schedulerSetting.setJobStoreType(JobStoreType.VOLATILE);

		    boolean replaced = configuration.replace(schedulerSetting);

		    if (!replaced) {

			throw GSException.createException(//
				ServletListener.class, //
				"Scheduler setting not replaced", //
				null, //
				null, //
				ErrorInfo.ERRORTYPE_INTERNAL, //
				ErrorInfo.SEVERITY_FATAL, //
				CONF_INITIALIZATION_ALIEN_ERR_ID);
		    }
		}

		configuration = FileSource.switchSource(configuration);

		GSLoggerFactory.getLogger(GISuiteStarter.class).info("Creating local config with VOLATILE job store ENDED");
	    }

	    GISuiteStarter.configuration = configuration;

	    ConfigurationWrapper.setConfiguration(configuration);

	    GSLoggerFactory.getLogger(GISuiteStarter.class).info("Initializing configuration ENDED");

	} catch (Exception throwable) {

	    throw GSException.createException(//
		    ServletListener.class, //
		    throwable.getMessage(), //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    CONF_INITIALIZATION_ALIEN_ERR_ID, //
		    throwable);
	}
    }

    /**
     * @throws GSException
     */
    private void checkConfig() throws GSException {

	GSLoggerFactory.getLogger(GISuiteStarter.class).info("Configuration check STARTED");

	ConfigurationChecker checker = new ConfigurationChecker();

	List<String> errors = checker.check(GISuiteStarter.configuration);

	if (!errors.isEmpty()) {

	    GSLoggerFactory.getLogger(GISuiteStarter.class).error("Configuration errors detected:");

	    errors.forEach(error -> {

		GSLoggerFactory.getLogger(GISuiteStarter.class).error(error);
	    });

	    throw GSException.createException(//
		    ServletListener.class, //
		    "Configuration errors detected: " + errors, //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    CONF_CHECK_FAILED_ERROR_ID //
	    );
	}

	GSLoggerFactory.getLogger(GISuiteStarter.class).info("Configuration check ENDED");
    }

    /**
     * 
     */
    private void applySystemSettings() {

	GSLoggerFactory.getLogger(getClass()).info("Applying system settings STARTED");

	// get the system settings
	SystemSetting systemSetting = ConfigurationWrapper.getSystemSettings();

	//
	// enables the publishing of runtime info to the database
	//
	if (systemSetting.areStatisticsEnabled()) {

	    GSLoggerFactory.getLogger(getClass()).info("Enabling statistics gathering");

	    ElasticsearchInfoPublisher.enable();
	}

	//
	// enables the email reporting during harvesting
	//
	if (systemSetting.isHarvestingReportMailEnabled()) {

	    GSLoggerFactory.getLogger(getClass()).info("Enabling harvesting e-mail sending");

	    HarvestingReportsHandler.enable();
	}

	//
	// enables the email reporting during augmentation
	//
	if (systemSetting.isAugmentationReportMailEnabled()) {

	    GSLoggerFactory.getLogger(getClass()).info("Enabling augmentation e-mail sending");

	    AugmentationReportsHandler.enable();
	}

	GSLoggerFactory.getLogger(getClass()).info("Applying system settings ENDED");
    }

    /**
     * 
     */
    private void initLocale() {
	// set the English locale
	Locale.setDefault(Locale.ENGLISH);
	// set time zone
	ISO8601DateTimeUtils.setGISuiteDefaultTimeZone();
    }

    /**
     * @throws GSException
     */
    private void initJaxb() throws GSException {
	// --------------------------------------
	//
	// this will initialize the JAXB contexts
	//
	try {

	    GSLoggerFactory.getLogger(GISuiteStarter.class).debug("JAXB initialization STARTED");

	    CommonContext.createMarshaller(true);
	    CommonContext.createUnmarshaller();
	    new Dataset();

	    GSLoggerFactory.getLogger(GISuiteStarter.class).debug("JAXB initialization ENDED");

	} catch (JAXBException e) {

	    GSLoggerFactory.getLogger(GISuiteStarter.class).error("Fatal error on startup, JAXB could not be initialized", e);

	    throw GSException.createException(//
		    this.getClass(), //
		    e.getMessage(), //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    JAXB_INIT_ERR_ID, //
		    e);
	}
    }

    /**
     * @throws GSException
     */
    private void checkServiceLoader() throws GSException {

	GSLoggerFactory.getLogger(GISuiteStarter.class).debug("Service loader check STARTED");
	new ServiceLoaderChecker();
	GSLoggerFactory.getLogger(GISuiteStarter.class).debug("Service loader check ENDED");
    }

    /**
     * @throws GSException
     */
    private void healthCheckTest() throws GSException {

	String skipHealthCheck = System.getProperty("skip.healthcheck");
	if (skipHealthCheck == null) {
	    skipHealthCheck = System.getenv("skip.healthcheck");
	}

	if (skipHealthCheck != null && skipHealthCheck.equals("true")) {

	    GSLoggerFactory.getLogger(GISuiteStarter.class).info("Skipping health check according to system variable 'skip.healthcheck'");
	    return;
	}

	try {

	    GSLoggerFactory.getLogger(GISuiteStarter.class).info("Health check at startup");

	    HealthCheck checker = new HealthCheck();

	    boolean healthy = checker.isHealthy(true);

	    GSLoggerFactory.getLogger(GISuiteStarter.class).info("Health check at startup result {}", healthy);

	    if (healthy) {

		HealthCheck.startCheckPassed = true;

	    } else {

		throw GSException.createException(//
			getClass(), //
			"Startup health check FAILED", //
			null, //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_FATAL, //
			STARTUP_HEALTH_CHECK_FAILED_ERR_ID);
	    }

	} catch (GSException e) {

	    throw e;

	} catch (Exception throwable) {

	    throw GSException.createException(//
		    ServletListener.class, //
		    throwable.getMessage(), //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    CONF_INITIALIZATION_ALIEN_ERR_ID, //
		    throwable);
	}
    }

    /**
     * @throws SchedulerException
     */
    private void startScheduler() throws GSException {

	GSLoggerFactory.getLogger(GISuiteStarter.class).info("Starting scheduler STARTED");

	SchedulerViewSetting schedulerSetting = ConfigurationWrapper.getSchedulerSetting();

	GSLoggerFactory.getLogger(GISuiteStarter.class).info("JobStore type: {}", schedulerSetting.getJobStoreType());

	Scheduler scheduler = SchedulerFactory.getScheduler(schedulerSetting);

	try {
	    scheduler.start();

	    //
	    // for these exec modes also starts the SchedulerSupport
	    //
	    if (mode == ExecutionMode.CONFIGURATION || mode == ExecutionMode.MIXED) {

		SchedulerSupport.getInstance().updateDelayed();
	    }

	} catch (SchedulerException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    ServletListener.class, //
		    e.getMessage(), //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    SCHEDULER_START_ERR_ID, //
		    e);
	}

	GSLoggerFactory.getLogger(GISuiteStarter.class).info("Starting scheduler ENDED");
    }
}
