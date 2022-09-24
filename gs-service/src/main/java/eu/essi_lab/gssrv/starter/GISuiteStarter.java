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

import eu.essi_lab.api.configuration.storage.GSConfigurationStorageFactory;
import eu.essi_lab.api.configuration.storage.IGSConfigurationStorage;
import eu.essi_lab.authentication.configuration.OAuthConfigurable;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.configuration.GIProjectExecutionMode;
import eu.essi_lab.configuration.reader.GSConfigurationReader;
import eu.essi_lab.configuration.sync.ConfigurationSync;
import eu.essi_lab.gssrv.health.StatusHealthCheck;
import eu.essi_lab.gssrv.servlet.ServletListener;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jobs.configuration.scheduler.InstantiableSchedulerInfo;
import eu.essi_lab.jobs.scheduler.GSJobSchedulerFactory;
import eu.essi_lab.jobs.scheduler.quartz.QuartzSchedulerStarter;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.GSInitConfiguration;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.configuration.IGSConfigurableComposed;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.configuration.option.GSConfOption;
import eu.essi_lab.model.configuration.option.GSConfOptionString;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.bind.JAXBException;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;

public class GISuiteStarter {

    private static final String ERR_ID_QUARTZ_SCHEDULER = "ERR_ID_QUARTZ_SCHEDULER";

    private transient Logger logger = GSLoggerFactory.getLogger(GISuiteStarter.class);
    private static final String ERR_ID_JAXB_INIT = "ERR_ID_JAXB_INIT";
    private static final String CONF_INITIALIZATION_ALIEN_ERRID = "CONF_INITIALIZATION_ALIEN_ERRID";

    private final ExecutionMode mode;
    private static final String STARTUP_HEALTH_CHECK_FAILED = "STARTUP_HEALTH_CHECK_FAILED";

    public GISuiteStarter() {

	logger.info("Retrieving execution mode");

	mode = new GIProjectExecutionMode().getMode();

	logger.info("GI-suite is starting in execution mode {}", mode);
    }
    public void start(IGSConfigurationStorage confConnector) throws GSException {

	initLocale();

	initJaxb();

	checkserviceLoader();

	if (confConnector == null) {
	    startScheduler(false);
	} else {

	    ConfigurationSync.getInstance().setDBGISuiteConfiguration(confConnector);

	    logger.info("Initializing configuration");

	    try {

		initializeConfiguration(ConfigurationSync.getInstance().getClonedConfiguration());

		logger.info("Completed configuration initialization");

		startScheduler(true);

		logger.info("Health check at startup"); 

		StatusHealthCheck checker = new StatusHealthCheck();

		boolean healthy = checker.isHealthy(true);

		logger.info("Health check at startup result {}", healthy);

		if (healthy) {
		    StatusHealthCheck.START_CHECK_PASSED = true;
		} else {
		    logger.error("Health check failed. Is the DAB initialized?");
		}

	    } catch (GSException e) {
		GSLoggerFactory.getLogger(ServletListener.class).error("Error initializing configuration");

		DefaultGSExceptionHandler handler = new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e));

		DefaultGSExceptionLogger.log(handler);

		System.exit(1);

	    } catch (Exception throwable) {

		GSLoggerFactory.getLogger(ServletListener.class).error("Error initializing configuration");

		DefaultGSExceptionHandler handler = new DefaultGSExceptionHandler(new DefaultGSExceptionReader(//
			GSException.createException(//
				ServletListener.class, //
				null, //
				null, //
				null, //
				ErrorInfo.ERRORTYPE_INTERNAL, //
				ErrorInfo.SEVERITY_FATAL, //
				CONF_INITIALIZATION_ALIEN_ERRID, //
				throwable)));

		DefaultGSExceptionLogger.log(handler);

		System.exit(1);
	    }
	}
    }

    private void initJaxb() throws GSException {
	// --------------------------------------
	//
	// this will initialize the JAXB contexts
	//
	try {

	    logger.debug("JAXB initialization STARTED");

	    CommonContext.createMarshaller(true);
	    CommonContext.createUnmarshaller();
	    new Dataset();

	    logger.debug("JAXB initialization ENDED");

	} catch (JAXBException e) {

	    logger.error("Fatal error starting gi-suite, JAXB could not be initialized", e);

	    throw GSException.createException(//
		    this.getClass(), //
		    "Error thrown by CommonContext", //
		    null, //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_FATAL, //
		    ERR_ID_JAXB_INIT, //
		    e);
	}
    }

    private void checkserviceLoader() throws GSException {
	// --------------------------------------
	//
	// this checks service loaders
	//
	logger.debug("Service loader check");
	new ServiceLoaderChecker();
    }

    private void initLocale() {
	// set the English locale
	Locale.setDefault(Locale.ENGLISH);
	// set time zone
	ISO8601DateTimeUtils.setGISuiteDefaultTimeZone();
    }

    private void startScheduler(Boolean confExists) throws GSException {
	// --------------------------------------
	//
	// this will initialize the Quartz Scheduler
	//
	logger.debug("Quartz Scheduler init");

	if (confExists)

	    try {

		new GSJobSchedulerFactory().getGSJobScheduler().start(mode);

	    } catch (GSException se) {

		logger.error("Fatal error starting gi-suite, Quartz scheduler could not be started");

		throw GSException.createException(//
			getClass(), //
			"Fatal error starting gi-suite, Quartz scheduler could not be started", //
			null, //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_FATAL, //
			ERR_ID_QUARTZ_SCHEDULER, se);
	    }

	else {
	    try {
		new QuartzSchedulerStarter(mode).startScheduler(new StdSchedulerFactory().getScheduler());
	    } catch (SchedulerException e) {

		logger.error("Fatal error starting gi-suite, Quartz scheduler could not be started", e);

		throw GSException.createException(//
			getClass(), //
			"Failed to start default quartz scheduler", //
			null, //
			null, //
			ErrorInfo.ERRORTYPE_INTERNAL, //
			ErrorInfo.SEVERITY_FATAL, //
			ERR_ID_QUARTZ_SCHEDULER, //
			e);
	    }
	}

    }

    public StorageUri applyInitRequest(GSInitConfiguration initRequest) throws GSException {

	StorageUri url = new StorageUri(initRequest.getUrl());

	if (!initRequest.getUseExisting()) {

	    GSConfiguration conf = GSConfiguration.createDefaultConfiguration();

	    GSConfOption<String> rootOpt = new GSConfOptionString();
	    rootOpt.setKey(GSConfiguration.GS_ROOT_USER_OPTION_KEY);
	    rootOpt.setValue(initRequest.getRootUser());
	    conf.setOption(rootOpt);

	    Map<String, GSConfOption<?>> supportedOAuth = conf.getConfigurableComponents().get(OAuthConfigurable.AUTH_MAIN_COMPONENT_KEY)
		    .getSupportedOptions();

	    for (GSConfOption opt : supportedOAuth.values()) {

		logger.debug("Checking if requested OAuth {} matches {}", initRequest.getOauthProviderName(), opt.getKey());

		if (opt.getKey().toLowerCase().contains(initRequest.getOauthProviderName().toLowerCase())) {

		    opt.setValue(true);
		    conf.getConfigurableComponents().get(OAuthConfigurable.AUTH_MAIN_COMPONENT_KEY).setOption(opt);

		    IGSConfigurable oauth = ((IGSConfigurableComposed) conf.getConfigurableComponents()
			    .get(OAuthConfigurable.AUTH_MAIN_COMPONENT_KEY)).getConfigurableComponents().values().iterator().next();

		    Map<String, GSConfOption<?>> pOptions = oauth.getSupportedOptions();

		    for (GSConfOption pOpts : pOptions.values()) {

			if (pOpts.getKey().toLowerCase().contains("secret")) {

			    pOpts.setValue(initRequest.getOauthProviderSecret());
			    oauth.setOption(pOpts);
			}

			if (pOpts.getKey().toLowerCase().contains("client_id")) {

			    pOpts.setValue(initRequest.getOauthProviderId());
			    oauth.setOption(pOpts);
			}
		    }
		}
	    }

	    Long cm = System.currentTimeMillis();

	    logger.debug("Setting initialization timestamp to {}", cm);

	    conf.setTimeStamp(cm);

	    if (isLocal(url))
		return GSConfigurationStorageFactory.storeConfigurationToRemote(ConfigurationLookup.toAbsolutePath(url), conf);

	    return GSConfigurationStorageFactory.storeConfigurationToRemote(url, conf);
	}

	return url;

    }

    private boolean isLocal(StorageUri url) {

	String lc = url.getUri().toLowerCase();

	return (lc.contains("file://") || ConfigurationLookup.isLocalRelative(url));

    }

    public void initializeConfiguration(GSConfiguration configuration) throws GSException {

	logger.trace("Looking for remote scheduler");

	// TODO write tests for the business logic below
	List<InstantiableSchedulerInfo> list = new GSConfigurationReader(configuration)
		.readInstantiableType(InstantiableSchedulerInfo.class, new Deserializer());

	if (!list.isEmpty()) {

	    logger.trace("Found non-empty list of remote schedulers");

	    InstantiableSchedulerInfo scheduler = list.get(0);

	    logger.trace("Invoking onFlush on first");

	    scheduler.getSchedulerInfoConfiguration().onFlush();

	    logger.trace("onFlush on {} completed", scheduler.getComponentId());

	}

	logger.trace("Invoking onStartUp on entire configuration");

	configuration.onStartUp();

    }
}
