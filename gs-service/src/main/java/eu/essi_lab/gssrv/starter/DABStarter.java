package eu.essi_lab.gssrv.starter;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.api.database.cfg.*;
import eu.essi_lab.api.database.factory.*;
import eu.essi_lab.augmenter.worker.*;
import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.check.*;
import eu.essi_lab.cfga.check.CheckResponse.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.DefaultConfiguration.*;
import eu.essi_lab.cfga.gs.demo.*;
import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.cfga.gs.setting.SystemSetting.*;
import eu.essi_lab.cfga.gs.setting.database.*;
import eu.essi_lab.cfga.gs.setting.harvesting.*;
import eu.essi_lab.cfga.gs.setting.ratelimiter.*;
import eu.essi_lab.cfga.gs.setting.ratelimiter.RateLimiterSetting.*;
import eu.essi_lab.cfga.patch.*;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting.*;
import eu.essi_lab.cfga.source.*;
import eu.essi_lab.configuration.*;
import eu.essi_lab.gssrv.conf.task.*;
import eu.essi_lab.gssrv.health.*;
import eu.essi_lab.gssrv.servlet.*;
import eu.essi_lab.harvester.*;
import eu.essi_lab.jaxb.common.*;
import eu.essi_lab.jaxb.wms.extension.*;
import eu.essi_lab.lib.net.downloader.*;
import eu.essi_lab.lib.net.s3.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.model.resource.*;
import eu.essi_lab.profiler.esri.feature.*;
import eu.essi_lab.profiler.esri.feature.query.*;
import eu.essi_lab.profiler.wms.extent.*;
import eu.essi_lab.profiler.wms.extent.map.*;
import eu.essi_lab.request.executor.schedule.*;
import eu.essi_lab.shared.driver.es.stats.*;
import org.quartz.*;

import javax.ws.rs.ext.*;
import javax.xml.bind.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import static eu.essi_lab.cfga.ConfigurationChangeListener.ConfigurationChangeEvent.*;

/**
 * @author Fabrizio
 */
public class DABStarter implements ConfigurationChangeListener {

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
    private SchedulerViewSetting schedulerSetting;

    /**
     *
     */
    public DABStarter() {

	GSLoggerFactory.getLogger(DABStarter.class).info("Retrieving execution mode");

	mode = ExecutionMode.get();

	GSLoggerFactory.getLogger(DABStarter.class).info("DAB is starting in execution mode {}", mode);
    }

    /**
     * @throws GSException
     */
    public void start() throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Cluster: {}", ClusterType.get().getLabel());

	//
	// see GIP-235
	//
	RuntimeDelegate.setInstance(new org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl());

	logJVMOptions();

	initLocale();

	initContext();

	initConfig();

	if (JVMOption.isEnabled(JVMOption.CHECK_CONFIG)) {

	    GSLoggerFactory.getLogger(DABStarter.class).info("Configuration check STARTED");

	    CheckResponse similarityCheckResponse = checkConfig();

	    GSLoggerFactory.getLogger(DABStarter.class).info("Configuration check ENDED");

	    if (similarityCheckResponse.getCheckResult() == CheckResult.CHECK_FAILED) {

		switch (mode) {
		case CONFIGURATION:
		case LOCAL_PRODUCTION:
		case MIXED:

		    GSLoggerFactory.getLogger(ConfigurationUtils.class).warn("Configuration fix STARTED");

		    ConfigurationUtils.backup(configuration);

		    if (similarityCheckResponse.getCheckResult() == CheckResult.CHECK_FAILED) {

			ConfigurationUtils.fix(configuration, similarityCheckResponse.getSettings(), false, false);
		    }

		    ConfigurationUtils.flush(configuration);

		    GSLoggerFactory.getLogger(ConfigurationUtils.class).warn("Configuration fix ENDED");

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
	}

	applySystemSettings();

	initTrustStore();

	healthCheckTest();

	switch (mode) {
	case BATCH:

	    startSchedulerDelayed();
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

	if (JVMOption.isEnabled(JVMOption.INIT_CACHES)) {

	    initCaches();
	}

	initDatabase();
    }

    /**
     *
     */
    private void logJVMOptions() {

	JVMOption.log();
    }

    @Override
    public void configurationChanged(ConfigurationChangeEvent event) {

	if (event.getEventType() == CONFIGURATION_AUTO_RELOADED) {

	    switch (mode) {
	    case BATCH, AUGMENTER, BULK -> {

		if (!schedulerSetting.equals(ConfigurationWrapper.getSchedulerSetting())) {

		    GSLoggerFactory.getLogger(DABStarter.class).info("Detected scheduler setting changes");
		    GSLoggerFactory.getLogger(DABStarter.class).info("Updating scheduler STARTED");

		    schedulerSetting = ConfigurationWrapper.getSchedulerSetting();

		    SchedulerFactory.getScheduler(schedulerSetting, true);

		    GSLoggerFactory.getLogger(DABStarter.class).info("Updating scheduler ENDED");
		}
	    }
	    }
	}
    }

    /**
     * @throws GSException
     */
    private void initConfig() throws GSException {

	try {

	    GSLoggerFactory.getLogger(DABStarter.class).info("Initializing configuration STARTED");

	    //
	    // 1) Retrieves the configuration.url parameter
	    //

	    Optional<String> optConfigURL = JVMOption.getStringValue(JVMOption.CONFIGURATION_URL);

	    String configURL;

	    if (optConfigURL.isEmpty() || optConfigURL.get().isEmpty()) {

		GSLoggerFactory.getLogger(DABStarter.class).warn("Configuration URL not found, using fallback URL: local FS temp");
		configURL = "file:temp";

	    } else {

		configURL = optConfigURL.get();
	    }

	    String[] split = configURL.split("!");

	    GSLoggerFactory.getLogger(DABStarter.class).info("Configuration URL: {}", configURL);

	    //
	    // 2) Creates the source
	    //

	    Configuration configuration = null;

	    String newConfigName;

	    ConfigurationSource source = null;

	    if (DatabaseSourceUrl.check(configURL)) {

		GSLoggerFactory.getLogger(getClass()).info("Found database URL");
		//
		// -Dconfiguration.url=xdbc://user:password@hostname:8000,8004/dbName/folder/
		// -Dconfiguration.url=osm://awsaccesskey:awssecretkey@https:productionhost/prod/prodConfig
		//

		String startupUri = split[0];

		source = DatabaseSource.of(startupUri);

	    } else if (S3Source.check(configURL)) {

		GSLoggerFactory.getLogger(getClass()).info("Found S3 URL");

		//
		// -Dconfiguration.url=s3://awsaccesskey:awssecretkey@bucket/config.json
		// Note! For MINIO the s3Endpoint parameter must be specified
		// E.g. -Ds3Endpoint=http://my-minio-hostname:9000
		//
		String startupUri = split[0];

		Optional<String> s3Endpoint = JVMOption.getStringValue(JVMOption.S3_ENDPOINT);

		if (s3Endpoint.isPresent()) {

		    source = S3Source.of(startupUri, s3Endpoint.get());

		} else {

		    source = S3Source.of(startupUri);
		}

	    } else {

		String configFileName = "gs-configuration";

		if (configURL.startsWith("file:temp")) {

		    GSLoggerFactory.getLogger(getClass()).info("Found local temp file");

		    //
		    // -Dconfiguration.url=file:temp
		    // -Dconfiguration.url=file:temp demo
		    //

		    GSLoggerFactory.getLogger(DABStarter.class).info("Local FS configuration on the temp user directory");

		    source = new FileSource(configFileName);

		} else if (configURL.startsWith("file://")) {

		    GSLoggerFactory.getLogger(getClass()).info("Found local file");

		    //
		    // -Dconfiguration.url=file://path/preprodenvconf/
		    // -Dconfiguration.url=file://path/preprodenvconf!demo
		    //
		    GSLoggerFactory.getLogger(DABStarter.class).info("Local FS configuration on given path");

		    String path = split[0].replace("file://", "");
		    path = path + File.separator + configFileName + ".json";

		    source = new FileSource(new File(path));
		} else {
		    GSLoggerFactory.getLogger(DABStarter.class).error("Unrecognized config URL: {}", configURL);
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

		configuration = switch (newConfigName) {
		    case "default" -> {

			GSLoggerFactory.getLogger(DABStarter.class).info("Creating and flushing new default configuration");

			yield new DefaultConfiguration(source, ConfigurationWrapper.CONFIG_RELOAD_TIME_UNIT,
				ConfigurationWrapper.CONFIG_RELOAD_TIME);
		    }
		    case "demo" -> {

			GSLoggerFactory.getLogger(DABStarter.class).info("Creating and flushing new demo configuration");

			yield new DemoConfiguration(source, ConfigurationWrapper.CONFIG_RELOAD_TIME_UNIT,
				ConfigurationWrapper.CONFIG_RELOAD_TIME);
		    }
		    default -> configuration;
		};

		SelectionUtils.deepClean(configuration);

		SelectionUtils.deepAfterClean(configuration);

		configuration.flush();

	    } else {

		GSLoggerFactory.getLogger(DABStarter.class).info("Creating configuration from existing source");
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
	    // set volatile DB according to the java option
	    //
	    if (JVMOption.isEnabled(JVMOption.FORCE_VOLATILE_DB)) {

		DatabaseSetting databaseSetting = configuration.get(//
			SingletonSettingsId.DATABASE_SETTING.getLabel(), //
			DatabaseSetting.class).get();

		databaseSetting.setVolatile(true);

		boolean replaced = configuration.replace(databaseSetting);

		if (!replaced && !configuration.contains(databaseSetting)) {

		    throw GSException.createException(//
			    ServletListener.class, //
			    "Database setting not replaced", //
			    null, //
			    null, //
			    ErrorInfo.ERRORTYPE_INTERNAL, //
			    ErrorInfo.SEVERITY_FATAL, //
			    "DatabaseSettingNotReplacedError");
		}

		// flushing config after changes is necessary to set it in SYNCH state
		configuration.flush();
	    }

	    //
	    //
	    //
	    if (mode == ExecutionMode.LOCAL_PRODUCTION) {

		GSLoggerFactory.getLogger(DABStarter.class).info("Creating local config with VOLATILE job store STARTED");

		// pause the autoreload to the current config instance, probably not required...
		configuration.pauseAutoreload();

		//
		// disables email delivery of reports
		//

		SystemSetting systemSetting = configuration.get(//
			SingletonSettingsId.SYSTEM_SETTING.getLabel(), //
			SystemSetting.class).get();

		systemSetting = SettingUtils.downCast(//
			SelectionUtils.resetAndSelect(systemSetting, false), SystemSetting.class);

		systemSetting.enableHarvestingReportEmail(false);
		systemSetting.enableErrorLogsReportEmail(false);
		systemSetting.enableAugmentationReportMail(false);
		systemSetting.enableDownloadReportMail(false);

		//
		// enables multiple tabs
		//

		systemSetting.putKeyValue(SystemSetting.KeyValueOptionKeys.MULTIPLE_CONFIGURATION_TABS.getLabel(), "true");

		SelectionUtils.deepClean(systemSetting);

		boolean replaced = configuration.replace(systemSetting);

		if (!replaced && !configuration.contains(systemSetting)) {

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
			SingletonSettingsId.RATE_LIMITER_SETTING.getLabel(), //
			RateLimiterSetting.class).get();

		rateLimiterSetting.setComputationType(ComputationType.DISABLED);

		replaced = configuration.replace(rateLimiterSetting);

		if (!replaced && !configuration.contains(rateLimiterSetting)) {

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
			SingletonSettingsId.SCHEDULER_SETTING.getLabel(), //
			SchedulerViewSetting.class).get();

		JobStoreType jobStoreType = schedulerSetting.getJobStoreType();

		if (jobStoreType == JobStoreType.PERSISTENT) {

		    schedulerSetting.setJobStoreType(JobStoreType.VOLATILE);

		    replaced = configuration.replace(schedulerSetting);

		    if (!replaced && !configuration.contains(schedulerSetting)) {

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

		Optional<File> path = JVMOption.getStringValue(JVMOption.LOCAL_PROD_CONFIG_PATH).map(File::new);

		configuration = path.isPresent() ? //
			FileSource.switchSource(configuration, path.get()) : //
			FileSource.switchSource(configuration);

		GSLoggerFactory.getLogger(DABStarter.class).info("Creating local config with VOLATILE job store ENDED");
	    }

	    // ------------------------------------------------------------------
	    //
	    // - Setting.COMPACT_MODE = false patch
	    //

	    GSLoggerFactory.getLogger(getClass()).debug("Setting.COMPACT_MODE = false patch STARTED");

	    RemovePropertyPatch patch = RemovePropertyPatch.of(configuration, Setting.COMPACT_MODE, false);

	    patch.patch();

	    GSLoggerFactory.getLogger(getClass()).debug("Setting.COMPACT_MODE = false patch ENDED");

	    //
	    //
	    // ---------------------------------------------------------------

	    DABStarter.configuration = configuration;

	    ConfigurationWrapper.setConfiguration(configuration);

	    schedulerSetting = ConfigurationWrapper.getSchedulerSetting();

	    configuration.addChangeEventListener(this);

	    GSLoggerFactory.getLogger(DABStarter.class).info("Initializing configuration ENDED");

	} catch (

		GSException gsex) {

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

	// ---------------------------------
	//
	// - RegisteredEditableSettingMethod
	//
	// if one or more registered settings are not compliant with the editable
	// definition,
	// it means that they are bad implemented and they must be manually fixed
	//
	RegisteredEditableMethod regEditSettingMethod = new RegisteredEditableMethod();

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
	// if some referenced classes cannot be instantiated, it probably means that
	// they have been
	// removed or there is some implementation issues. in the first case, fixing the
	// config with
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
	// if some editable settings in the configuration are not compliant with the
	// editable definition,
	// since the RegisteredEditableSettingMethod is successful (so they are
	// compliant with the editable definition)
	// it means that some options and/or settings are changed and they can be fixed
	//

	ConfigurationEditableMethod configEditSettingMethod = new ConfigurationEditableMethod();

	CheckResponse configEditCheck = configEditSettingMethod.check(configuration);

	// ---------------------------------
	//
	// - SimilarityCheckMethod
	//
	// this method is configured to fail only in the following cases:
	// - one or more options have been removed from the target setting (so
	// serialized settings have more options
	// than the corresponding Java setting)
	// - one or more options have been added to the target setting (so serialized
	// settings have less options
	// than the corresponding Java setting)
	// - one or more settings have been removed from the target setting (so
	// serialized settings have more settings
	// than the corresponding Java setting)
	// - one or more options have been added to the target setting (so serialized
	// settings have less options
	// than the corresponding Java setting)

	SimilarityMethod similarityCheckMethod = new SimilarityMethod();

	// in case a validator is added to a Setting (or removed from a Setting)
	similarityCheckMethod.getExclusions().remove(Setting.VALIDATOR);

	similarityCheckMethod.getExclusions().remove(Setting.SHOW_HEADER);

	CheckResponse similarityCheckResponse = similarityCheckMethod.check(configuration);

	if (configEditCheck.getCheckResult() == CheckResult.CHECK_FAILED || //
		similarityCheckResponse.getCheckResult() == CheckResult.CHECK_FAILED) {

	    //
	    // this case should never happen, since the similarity check is more completed
	    // than the
	    // editable check and includes it. so, if the editable check fails, the
	    // similarity check also fails, while
	    // it can happen that the similarity check fails and the editable settings
	    // don't.
	    //
	    // if the reset setting (Java setting) has LESS options and/or settings than
	    // the serialized setting, the editable check fails because this would prevent
	    // to select
	    // the missing info from the serialized setting to the reset setting.
	    //
	    // if the reset setting (Java setting) has MORE options and/or settings than
	    // the target (serialized) setting, the editable check do NOT fail while the
	    // similarity check would FAIL
	    // anyway
	    //
	    if (similarityCheckResponse.getCheckResult() == CheckResult.CHECK_SUCCESSFUL) {

		GSLoggerFactory.getLogger(DABStarter.class)
			.warn("ConfigEditableSetting check failed while SimilarityCheck do not failed !!!");
	    }
	}

	//
	// note that this method returns only the response of the {@link
	// SimilarityCheckMethod} so the
	// settings that will be eventually fixed are the ones returned by that check.
	// This is fine,
	// since they also include the settings returned by the
	// ConfigEditableSettingMethod
	//

	return similarityCheckResponse;
    }

    /**
     *
     */
    private void initTrustStore() {

	Optional<String> trustStoreName = ConfigurationWrapper. //
		getSystemSettings().//
		readKeyValue(KeyValueOptionKeys.TRUST_STORE_NAME.getLabel());

	Optional<String> trustStorePwd = ConfigurationWrapper. //
		getSystemSettings().//
		readKeyValue(KeyValueOptionKeys.TRUST_STORE_PWD.getLabel());

	Optional<String> trustStore = ConfigurationWrapper. //
		getSystemSettings().//
		readKeyValue(KeyValueOptionKeys.TRUST_STORE.getLabel());

	if (trustStore.isPresent() && trustStorePwd.isPresent() && trustStoreName.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).info("Trust store init STARTED");

	    File target = null;

	    File trustFile = new File(trustStore.get());

	    if (trustFile.exists()) {

		target = trustFile;

		GSLoggerFactory.getLogger(getClass()).info("Loading trust store from file system: {}", target.getAbsolutePath());

	    } else {

		Optional<S3TransferWrapper> wrapper = ConfigurationWrapper.getS3TransferWrapper();

		if (wrapper.isPresent()) {

		    GSLoggerFactory.getLogger(getClass()).info("Loading trust store S3 bucket: {}", trustStore.get());

		    target = new File(FileUtils.getTempDir(), trustStoreName.get());

		    boolean downloaded = wrapper.get().download( //
			    trustStore.get(), //
			    trustStoreName.get(), //
			    target);

		    if (!downloaded) {

			target = null;

			GSLoggerFactory.getLogger(getClass()).error("Error occurred. Unable to download trust store from S3");
		    }

		} else {

		    GSLoggerFactory.getLogger(getClass()).warn("Unable to init trust store, S3 transfer wrapper is not configured");
		}
	    }

	    if (target != null) {

		System.setProperty("dab.net.ssl.trustStore", target.getAbsolutePath());
		System.setProperty("dab.net.ssl.trustStoreType", Downloader.DEFAULT_KEY_STORE_TYPE);
		System.setProperty("dab.net.ssl.trustStorePassword", trustStorePwd.get());

		GSLoggerFactory.getLogger(getClass()).info("Trust store init ENDED");
	    }

	} else {

	    GSLoggerFactory.getLogger(getClass()).info("Trust store credentials missing");
	}
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
    private void initContext() throws GSException {

	try {

	    GSLoggerFactory.getLogger(DABStarter.class).debug("Context initialization STARTED");

	    CommonContext.createMarshaller(true);
	    CommonContext.createUnmarshaller();
	    new Dataset();

	    JAXBWMS.getInstance().getMarshaller();

	    // this is done here to guarantee that is done in the right thread
	    // in some cases we notice that this call is firstly done by threads with a context class loader
	    // devoid of the necessary classes to load
	    ConfigurableLoader.load();

	    GSLoggerFactory.getLogger(DABStarter.class).debug("Context initialization ENDED");

	} catch (JAXBException e) {

	    GSLoggerFactory.getLogger(DABStarter.class).error("Fatal error on startup, context could not be initialized", e);

	    throw GSException.createException(//
		    this.getClass(), //
		    e.getMessage(), //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    "ContextInitError", //
		    e);
	}
    }

    /**
     * @throws GSException
     */
    private void healthCheckTest() throws GSException {

	if (JVMOption.isEnabled(JVMOption.SKIP_HEALTH_CHECK)) {

	    return;
	}

	GSLoggerFactory.getLogger(DABStarter.class).info("Health check at startup");

	HealthCheck checker = new HealthCheck();

	boolean healthy = checker.isHealthy(true);

	GSLoggerFactory.getLogger(DABStarter.class).info("Health check at startup result {}", healthy);

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
	    WMSGetMapHandler.getCachedLayer(WMSLayer.TRIGGER_MONITORING_POINTS);
	    WMSGetMapHandler.getCachedLayer(WMSLayer.ICHANGE_MONITORING_POINTS);
	    CachedCollections.getInstance()
		    .prepare(new FeatureLayer1StationsArcticRequest(), "whos-arctic", new FeatureLayer1StationsArctic());
	    break;
	case BATCH:
	case CONFIGURATION:
	case AUGMENTER:
	case INTENSIVE:
	case MIXED:
	    break;
	case LOCAL_PRODUCTION:
	    // all activated.. used for testing
	    WMSGetMapHandler.getCachedLayer(WMSLayer.EMOD_PACE_BATHYMETRY);
	    WMSGetMapHandler.getCachedLayer(WMSLayer.EMOD_PACE_BIOLOGY);
	    WMSGetMapHandler.getCachedLayer(WMSLayer.EMOD_PACE_CHEMISTRY);
	    WMSGetMapHandler.getCachedLayer(WMSLayer.EMOD_PACE_METEOROLOGY);
	    WMSGetMapHandler.getCachedLayer(WMSLayer.EMOD_PACE_OCEANOGRAPHY_NRT);
	    WMSGetMapHandler.getCachedLayer(WMSLayer.EMOD_PACE_PHYSICS);
	    WMSGetMapHandler.getCachedLayer(WMSLayer.TRIGGER_MONITORING_POINTS);
	    WMSGetMapHandler.getCachedLayer(WMSLayer.ICHANGE_MONITORING_POINTS);
	    CachedCollections.getInstance()
		    .prepare(new FeatureLayer1StationsArcticRequest(), "whos-arctic", new FeatureLayer1StationsArctic());
	default:
	}
    }

    /**
     * @throws GSException
     */
    private void initDatabase() throws GSException {

	DatabaseFactory.get(ConfigurationWrapper.getDatabaseSetting().asStorageInfo());
    }

    /**
     *
     */
    private void startSchedulerDelayed() {

	Optional<Properties> keyValueOptions = ConfigurationWrapper.getSystemSettings().getKeyValueOptions();

	int schedulerStartDelay = DEFAULT_SCHEDULER_START_DELAY;

	if (keyValueOptions.isPresent()) {

	    schedulerStartDelay = Integer.parseInt(keyValueOptions.get()
		    .getProperty(KeyValueOptionKeys.SCHEDULER_START_DELAY.getLabel(), String.valueOf(DEFAULT_SCHEDULER_START_DELAY)));
	}

	GSLoggerFactory.getLogger(DABStarter.class).info("Scheduler will start in {} minutes", schedulerStartDelay);

	new Timer().schedule(new TimerTask() {

	    @Override
	    public void run() {

		try {

		    GSLoggerFactory.getLogger(DABStarter.class).info("Delayed scheduler start STARTED");

		    GSLoggerFactory.getLogger(DABStarter.class).info("JobStore type: {}", schedulerSetting.getJobStoreType());

		    Scheduler scheduler = SchedulerFactory.getScheduler(schedulerSetting, false);

		    scheduler.start();

		    GSLoggerFactory.getLogger(DABStarter.class).info("Delayed scheduler start ENDED");

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

		    DABStarter.schedulerStartError = true;
		}
	    }

	}, TimeUnit.MINUTES.toMillis(schedulerStartDelay));
    }

    /**
     * @throws GSException
     */
    private void startScheduler() throws GSException {

	GSLoggerFactory.getLogger(DABStarter.class).info("Starting scheduler STARTED");

	GSLoggerFactory.getLogger(DABStarter.class).info("JobStore type: {}", schedulerSetting.getJobStoreType());

	Scheduler scheduler = SchedulerFactory.getScheduler(schedulerSetting, false);

	try {

	    scheduler.start();

	    //
	    // for these exec modes also starts the SchedulerSupport
	    //
	    if (mode == ExecutionMode.CONFIGURATION || mode == ExecutionMode.MIXED || mode == ExecutionMode.LOCAL_PRODUCTION) {

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

	GSLoggerFactory.getLogger(DABStarter.class).info("Starting scheduler ENDED");
    }
}
