package eu.essi_lab.gssrv.starter;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ext.RuntimeDelegate;
import javax.xml.bind.JAXBException;

import org.quartz.SchedulerException;

import eu.essi_lab.augmenter.worker.AugmentationReportsHandler;
import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.ConfigurationSource;
import eu.essi_lab.cfga.ConfigurationUtils;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.checker.CheckResponse;
import eu.essi_lab.cfga.checker.CheckResponse.CheckResult;
import eu.essi_lab.cfga.checker.ConfigEditableSettingMethod;
import eu.essi_lab.cfga.checker.ReferencedClassesMethod;
import eu.essi_lab.cfga.checker.RegisteredEditableSettingMethod;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.DefaultConfiguration.MainSettingsIdentifier;
import eu.essi_lab.cfga.gs.SimilarityCheckMethod;
import eu.essi_lab.cfga.gs.demo.DemoConfiguration;
import eu.essi_lab.cfga.gs.setting.SchedulerViewSetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.SchedulerSupport;
import eu.essi_lab.cfga.gs.setting.ratelimiter.RateLimiterSetting;
import eu.essi_lab.cfga.gs.setting.ratelimiter.RateLimiterSetting.ComputationType;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting.JobStoreType;
import eu.essi_lab.cfga.source.FileSource;
import eu.essi_lab.cfga.source.MarkLogicSource;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.gssrv.conf.task.ErrorLogsPublisherTask;
import eu.essi_lab.gssrv.health.HealthCheck;
import eu.essi_lab.gssrv.servlet.ServletListener;
import eu.essi_lab.harvester.HarvestingReportsHandler;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.profiler.esri.feature.FeatureLayer1StationsArctic;
import eu.essi_lab.profiler.esri.feature.query.CachedCollections;
import eu.essi_lab.profiler.wms.extent.WMSLayer;
import eu.essi_lab.profiler.wms.extent.map.WMSGetMapHandler;
import eu.essi_lab.request.executor.schedule.DownloadReportsHandler;
import eu.essi_lab.shared.driver.es.stats.ElasticsearchInfoPublisher;

/**
 * @author Fabrizio
 */
public class GIPStarter {

    /**
    * 
    */
    public static Configuration configuration;

    /**
    * 
    */
    public static boolean schedulerStartError;

    /**
     * In minutes
     */
    private static final int DEFAULT_SCHEDULER_START_DELAY = 15;

    /**
     * 
     */
    private final ExecutionMode mode;

    /**
     * 
     */
    public GIPStarter() {

	GSLoggerFactory.getLogger(GIPStarter.class).info("Retrieving execution mode");

	mode = ExecutionMode.get();

	GSLoggerFactory.getLogger(GIPStarter.class).info("GI-suite is starting in execution mode {}", mode);

	if (ExecutionMode.skipAuthorization()) {
	    GSLoggerFactory.getLogger(GIPStarter.class).info("Auhtorization skipped by administrator");
	} else {
	    GSLoggerFactory.getLogger(GIPStarter.class).info("Auhtorization activated by administrator");
	}

    }

    /**
     * @param confConnector
     * @throws GSException
     */
    @SuppressWarnings("incomplete-switch")
    public void start() throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Cluster: {}", getCluster());

	//
	// see GIP-235
	//
	RuntimeDelegate.setInstance(new org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl());

	initLocale();

	initJaxb();

	initConfig();

	CheckResponse similarityCheckResponse = checkConfig();

	if (similarityCheckResponse.getCheckResult() == CheckResult.CHECK_FAILED) {

	    switch (mode) {
	    case CONFIGURATION:
	    case LOCAL_PRODUCTION:
	    case MIXED:

		ConfigurationUtils.fix(configuration, similarityCheckResponse);
		break;
	    default:
		throw GSException.createException(//
			ConfigurationUtils.class, //
			"Configuration issues found, but this node is not empowered to fix the configuration, exit!", //
			null, //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_FATAL, //
			"NodeNotEmpoweredToFixConfiguarionError");

	    }
	}

	applySystemSettings();

	healthCheckTest();

	switch (mode) {
	case BATCH:

	    startSchedulerLate();
	    break;

	case AUGMENTER:
	case BULK:
	case CONFIGURATION:
	case LOCAL_PRODUCTION:
	case MIXED:

	    startScheduler();
	    break;

	case FRONTEND:
	case ACCESS:
	case INTENSIVE:
	    break;
	}

	initCaches();
    }

    /**
     * @return
     */
    public static String getCluster() {

	String cluster = System.getProperty("cluster");
	if (cluster == null) {
	    cluster = System.getenv("cluster");
	}
	return cluster != null ? cluster : "Local";
    }

    /**
     * @throws GSException
     */
    private void initConfig() throws GSException {

	try {

	    GSLoggerFactory.getLogger(GIPStarter.class).info("Initializing configuration STARTED");

	    //
	    // 1) Retrieves the configuration.url parameter
	    //

	    String configURL = System.getProperty("configuration.url");
	    if (configURL == null) {
		configURL = System.getenv("configuration.url");
	    }

	    if (configURL == null || configURL.isEmpty()) {

		GSLoggerFactory.getLogger(GIPStarter.class).warn("Configuration URL not found, using fallback URL: local FS temp");
		configURL = "file:temp";
	    }

	    String[] split = configURL.split("!");

	    GSLoggerFactory.getLogger(GIPStarter.class).info("Configuration URL: {}", configURL);

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

		    GSLoggerFactory.getLogger(GIPStarter.class).info("Local FS configuration on the temp user directory");

		    source = new FileSource(configFileName);

		} else if (configURL.startsWith("file://")) {

		    //
		    // -Dconfiguration.url=file://path/preprodenvconf/
		    // -Dconfiguration.url=file://path/preprodenvconf!demo
		    //
		    GSLoggerFactory.getLogger(GIPStarter.class).info("Local FS configuration on given path");

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

		// if (mode != ExecutionMode.MIXED) {
		//
		// throw GSException.createException(//
		// ServletListener.class, //
		// "Configuration file empty or missing", //
		// null, //
		// null, //
		// ErrorInfo.ERRORTYPE_INTERNAL, //
		// ErrorInfo.SEVERITY_FATAL, //
		// CONF_FILE_MISSING_OR_EMPTY_ERR_ID);
		// }

		switch (newConfigName) {

		case "default":

		    GSLoggerFactory.getLogger(GIPStarter.class).info("Creating and flushing new default configuration");

		    configuration = new DefaultConfiguration(source, ConfigurationWrapper.CONFIG_RELOAD_TIME_UNIT,
			    ConfigurationWrapper.CONFIG_RELOAD_TIME);

		    break;

		case "demo":

		    GSLoggerFactory.getLogger(GIPStarter.class).info("Creating and flushing new demo configuration");

		    configuration = new DemoConfiguration(source, ConfigurationWrapper.CONFIG_RELOAD_TIME_UNIT,
			    ConfigurationWrapper.CONFIG_RELOAD_TIME);

		    break;
		}

		SelectionUtils.deepClean(configuration);

		SelectionUtils.deepAfterClean(configuration);

		configuration.flush();

	    } else {

		GSLoggerFactory.getLogger(GIPStarter.class).info("Creating configuration from existing source");
		configuration = new Configuration(source, ConfigurationWrapper.CONFIG_RELOAD_TIME_UNIT,
			ConfigurationWrapper.CONFIG_RELOAD_TIME);
	    }

	    //
	    // in execution mode CONFIGURATION and MIXED there is no need to autoreload
	    // since there is only one node, no shared configuration (also in
	    // LOCAL_PRODUCTION mode
	    // but this is done in the next rows...)
	    //
	    if (mode == ExecutionMode.CONFIGURATION || mode == ExecutionMode.MIXED) {

		configuration.pauseAutoreload();
	    }

	    //
	    //
	    //
	    if (mode == ExecutionMode.LOCAL_PRODUCTION) {

		GSLoggerFactory.getLogger(GIPStarter.class).info("Creating local config with VOLATILE job store STARTED");

		// pause the autoreload to the current config instance, probably not required...
		configuration.pauseAutoreload();

		//
		// disables harvesting and error logs email reports
		//

		SystemSetting systemSetting = configuration.get(//
			MainSettingsIdentifier.SYSTEM_SETTINGS.getLabel(), //
			SystemSetting.class).get();

		systemSetting.enableHarvestingReportEmail(false);
		systemSetting.enableErrorLogsReportEmail(false);
		systemSetting.enableAugmentationReportMail(false);

		boolean replaced = configuration.replace(systemSetting);

		if (!replaced) {

		    throw GSException.createException(//
			    ServletListener.class, //
			    "System setting not replaced", //
			    null, //
			    null, //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_FATAL, //
			    "SystemSettingNotReplacedError");
		}

		//
		// disables the rate limiter setting
		//

		RateLimiterSetting rateLimiterSetting = configuration.get(//
			MainSettingsIdentifier.RATE_LIMITER_SETTINGS.getLabel(), //
			RateLimiterSetting.class).get();

		rateLimiterSetting.setComputationType(ComputationType.DISABLED);

		replaced = configuration.replace(rateLimiterSetting);

		if (!replaced) {

		    throw GSException.createException(//
			    ServletListener.class, //
			    "Rate limiter setting not replaced", //
			    null, //
			    null, //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_FATAL, //
			    "RateLimiterSettingNotReplacedError");
		}

		//
		// set volatile scheduler
		//

		SchedulerViewSetting schedulerSetting = configuration.get(//
			MainSettingsIdentifier.SCHEDULER.getLabel(), //
			SchedulerViewSetting.class).get();

		JobStoreType jobStoreType = schedulerSetting.getJobStoreType();

		if (jobStoreType == JobStoreType.PERSISTENT) {

		    schedulerSetting.setJobStoreType(JobStoreType.VOLATILE);

		    replaced = configuration.replace(schedulerSetting);

		    if (!replaced) {

			throw GSException.createException(//
				ServletListener.class, //
				"Scheduler setting not replaced", //
				null, //
				null, //
				ErrorInfo.ERRORTYPE_INTERNAL, //
				ErrorInfo.SEVERITY_FATAL, //
				"SchedulerSettingNotReplacedError");
		    }
		}

		configuration = FileSource.switchSource(configuration);

		GSLoggerFactory.getLogger(GIPStarter.class).info("Creating local config with VOLATILE job store ENDED");
	    }

	    GIPStarter.configuration = configuration;

	    ConfigurationWrapper.setConfiguration(configuration);

	    GSLoggerFactory.getLogger(GIPStarter.class).info("Initializing configuration ENDED");

	} catch (GSException gsex) {

	    throw gsex;

	} catch (Exception throwable) {

	    throw GSException.createException(//
		    ServletListener.class, //
		    throwable.getMessage(), //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    "ConfigInitError", //
		    throwable);
	}
    }

    /**
     * @throws GSException
     */
    private CheckResponse checkConfig() throws GSException {

	GSLoggerFactory.getLogger(GIPStarter.class).info("Configuration check STARTED");

	// ---------------------------------
	//
	// - RegisteredEditableSettingMethod
	//
	// if one or more registered settings are not compliant with the editable definition,
	// it means that they are bad implemented and they must be manually fixed
	//
	RegisteredEditableSettingMethod regEditSettingMethod = new RegisteredEditableSettingMethod();

	CheckResponse regEditCheck = regEditSettingMethod.check();

	if (regEditCheck.getCheckResult() == CheckResult.CHECK_FAILED) {

	    throw GSException.createException(//
		    ServletListener.class, //
		    "Registered editable settings check failed: " + regEditCheck.getMessages(), //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    "RegisteredEditableSettingCheckError");
	}

	// ---------------------------------
	//
	// - ReferencedClassesMethod
	//
	// if some referenced classes cannot be instantiated, it probably means that they have been
	// removed or there is some implementation issues. in the first case, fixing the config with
	// new settings could help, but in the latter case the issue cannot be fixed.
	// so here, we throw an exception
	//

	ReferencedClassesMethod referencedClassesMethod = new ReferencedClassesMethod();

	CheckResponse refClassCheck = referencedClassesMethod.check(configuration);

	if (refClassCheck.getCheckResult() == CheckResult.CHECK_FAILED) {

	    throw GSException.createException(//
		    ServletListener.class, //
		    "Referenced classes check failed: " + refClassCheck.getMessages(), //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    "ReferencedClassesCheckError");
	}

	// ---------------------------------
	//
	// - ConfigEditableSettingMethod
	//
	// if some editable settings in the configuration are not compliant with the editable definition,
	// since the RegisteredEditableSettingMethod is successfull (so they are compliant with the editable definition)
	// it means that some options and/or settings are changed and they can be fixed
	//

	ConfigEditableSettingMethod configEditSettingMethod = new ConfigEditableSettingMethod();

	CheckResponse configEditCheck = configEditSettingMethod.check(configuration);

	// ---------------------------------
	//
	// - SimilarityCheckMethod
	//
	// this method is configured to fail only in the following cases:
	// - one or more options have been removed from the target setting (so serialized settings have more options
	// than the corresponding Java setting)
	// - one or more options have been added to the target setting (so serialized settings have less options
	// than the corresponding Java setting)
	// - one or more settings have been removed from the target setting (so serialized settings have more settings
	// than the corresponding Java setting)
	// - one or more options have been added to the target setting (so serialized settings have less options
	// than the corresponding Java setting)

	SimilarityCheckMethod similarityCheckMethod = new SimilarityCheckMethod();

	CheckResponse similarityCheckResponse = similarityCheckMethod.check(configuration);

	if (configEditCheck.getCheckResult() == CheckResult.CHECK_FAILED || //
		similarityCheckResponse.getCheckResult() == CheckResult.CHECK_FAILED) {

	    //
	    // this case should never happen, since the similarity check is more completed than the
	    // editable check and includes it. so, if the editable check fails, the similarity check also fails, while
	    // it can happen that the similarity check fails and the editable settings don't.
	    //
	    // if the reset setting (Java setting) has LESS options and/or settings than
	    // the serialized setting, the editable check fails because this would prevent to select
	    // the missing info from the serialized setting to the reset setting.
	    //
	    // if the reset setting (Java setting) has MORE options and/or settings than
	    // the target (serialized) setting, the editable check do NOT fail while the similarity check would FAIL
	    // anyway
	    //
	    if (similarityCheckResponse.getCheckResult() == CheckResult.CHECK_SUCCESSFUL) {

		GSLoggerFactory.getLogger(GIPStarter.class)
			.warn("ConfigEditableSetting check failed while SimilarityCheck do not failed !!!");
	    }
	}

	GSLoggerFactory.getLogger(GIPStarter.class).info("Configuration check ENDED");

	//
	// note that this method returns only the response of the {@link SimilarityCheckMethod} so the
	// settings that will be eventually fixed are the ones returned by that check. This is fine,
	// since they also include the settings returned by the ConfigEditableSettingMethod
	//

	return similarityCheckResponse;
    }

    /**
     * 
     */
    private void applySystemSettings() {

	GSLoggerFactory.getLogger(getClass()).info("Applying system settings STARTED");

	//
	// get the system settings
	//
	SystemSetting systemSetting = ConfigurationWrapper.getSystemSettings();

	//
	// enables the publishing of error logs (the related custom task must be
	// enabled)
	//

	if (systemSetting.isErrorLogsReportEnabled()) {

	    GSLoggerFactory.getLogger(getClass()).info("Setting error log listener");

	    GSLoggerFactory.setErrorLogListener(new ErrorLogsPublisherTask());
	}

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

	//
	// enables the email reporting during bulk downloads

	if (systemSetting.isDownloadReportMailEnabled()) {

	    GSLoggerFactory.getLogger(getClass()).info("Enabling bulk download e-mail sending");

	    DownloadReportsHandler.enable();
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

	    GSLoggerFactory.getLogger(GIPStarter.class).debug("JAXB initialization STARTED");

	    CommonContext.createMarshaller(true);
	    CommonContext.createUnmarshaller();
	    new Dataset();

	    GSLoggerFactory.getLogger(GIPStarter.class).debug("JAXB initialization ENDED");

	} catch (JAXBException e) {

	    GSLoggerFactory.getLogger(GIPStarter.class).error("Fatal error on startup, JAXB could not be initialized", e);

	    throw GSException.createException(//
		    this.getClass(), //
		    e.getMessage(), //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    "JAXBInitError", //
		    e);
	}
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

	    GSLoggerFactory.getLogger(GIPStarter.class).info("Skipping health check according to system variable 'skip.healthcheck'");
	    return;
	}

	GSLoggerFactory.getLogger(GIPStarter.class).info("Health check at startup");

	HealthCheck checker = new HealthCheck();

	boolean healthy = checker.isHealthy(true);

	GSLoggerFactory.getLogger(GIPStarter.class).info("Health check at startup result {}", healthy);

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
		    "HealthCheckStartupError");
	}

    }

    /**
     * 
     */
    private void initCaches() {

	// init caches, but only for specific production nodes
	ExecutionMode executionMode = ExecutionMode.get();
	switch (executionMode) {
	case FRONTEND:
	    WMSGetMapHandler.getCachedLayer(WMSLayer.EMOD_PACE_BATHYMETRY);
	    WMSGetMapHandler.getCachedLayer(WMSLayer.EMOD_PACE_BIOLOGY);
	    WMSGetMapHandler.getCachedLayer(WMSLayer.EMOD_PACE_CHEMISTRY);
	    WMSGetMapHandler.getCachedLayer(WMSLayer.EMOD_PACE_METEOROLOGY);
	    WMSGetMapHandler.getCachedLayer(WMSLayer.EMOD_PACE_OCEANOGRAPHY_NRT);
	    WMSGetMapHandler.getCachedLayer(WMSLayer.EMOD_PACE_PHYSICS);
	    break;
	case ACCESS:
	    WMSGetMapHandler.getCachedLayer(WMSLayer.ICHANGE_MONITORING_POINTS);
	    CachedCollections.getInstance().prepare(new FeatureLayer1StationsArcticRequest(), "whos-arctic",
		    new FeatureLayer1StationsArctic());
	    break;
	case BATCH:
	case CONFIGURATION:
	case AUGMENTER:
	case INTENSIVE:
	case MIXED:
	case LOCAL_PRODUCTION:
	default:
	}
    }

    /**
     * @throws GSException
     */
    private void startSchedulerLate() throws GSException {

	Optional<Properties> keyValueOptions = ConfigurationWrapper.getSystemSettings().getKeyValueOptions();

	int schedulerStartDelay = DEFAULT_SCHEDULER_START_DELAY;

	if (keyValueOptions.isPresent()) {

	    schedulerStartDelay = Integer
		    .valueOf(keyValueOptions.get().getProperty("schedulerStartDelay", String.valueOf(DEFAULT_SCHEDULER_START_DELAY)));
	}

	GSLoggerFactory.getLogger(GIPStarter.class).info("Scheduler will start in {} minutes", schedulerStartDelay);

	new Timer().schedule(new TimerTask() {

	    @Override
	    public void run() {

		try {

		    GSLoggerFactory.getLogger(GIPStarter.class).info("Delayed scheduler start STARTED");

		    SchedulerViewSetting schedulerSetting = ConfigurationWrapper.getSchedulerSetting();

		    GSLoggerFactory.getLogger(GIPStarter.class).info("JobStore type: {}", schedulerSetting.getJobStoreType());

		    Scheduler scheduler = SchedulerFactory.getScheduler(schedulerSetting, false);

		    scheduler.start();

		    GSLoggerFactory.getLogger(GIPStarter.class).info("Delayed scheduler start ENDED");

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

		    GIPStarter.schedulerStartError = true;
		}
	    }

	}, TimeUnit.MINUTES.toMillis(schedulerStartDelay));
    }

    /**
     * @throws GSException
     */
    private void startScheduler() throws GSException {

	GSLoggerFactory.getLogger(GIPStarter.class).info("Starting scheduler STARTED");

	SchedulerViewSetting schedulerSetting = ConfigurationWrapper.getSchedulerSetting();

	GSLoggerFactory.getLogger(GIPStarter.class).info("JobStore type: {}", schedulerSetting.getJobStoreType());

	Scheduler scheduler = SchedulerFactory.getScheduler(schedulerSetting, false);

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
		    "SchedulerStartError", //
		    e);
	}

	GSLoggerFactory.getLogger(GIPStarter.class).info("Starting scheduler ENDED");
    }
}
