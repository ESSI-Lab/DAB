package eu.essi_lab.configuration.sync;

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

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import eu.essi_lab.api.configuration.storage.IGSConfigurationStorage;
import eu.essi_lab.configuration.reader.GSConfigurationReader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class ConfigurationSync implements IConfigurationSync, Observer {

    public static final String ERR_ID_NODBMANAGER = "nodbmanager";
    private static ConfigurationSync instance;
    private IGSConfigurationStorage manager;
    private GSConfiguration currentConf;

    private InputStream currentConfCis;

    private ScheduledExecutorService executor;
    private transient Logger logger = GSLoggerFactory.getLogger(this.getClass());
    private final Object lock = new Object();

    private List<GSSource> allsources = null;

    private Map<String, GSSource> mapsources = new HashMap<>();
    private final ConfigurationQueueSmart queue;

    private class UpdateRunnable implements Runnable {

	@Override
	public void run() {
	    try {

		updateConf();

	    } catch (GSException e) {

		DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));

	    }

	}

    }

    public void destroyExecutor() {

	logger.info("Destroying ConfigurationSync Executor");

	List<Runnable> runnables = executor.shutdownNow();

	logger.info("Destroy runnables number {}", runnables.size());

    }

    private ConfigurationSync() {

	executor = Executors.newSingleThreadScheduledExecutor();

	executor.scheduleAtFixedRate(new UpdateRunnable(), 0, 2, TimeUnit.MINUTES);

	queue = ConfigurationQueueSmart.getInstance();

    }

    @Override
    public void setUpdateFrequency(int frequency, TimeUnit unit) throws GSException {

	executor.shutdownNow();

	executor = Executors.newSingleThreadScheduledExecutor();

	executor.scheduleAtFixedRate(new UpdateRunnable(), 0, frequency, unit);

    }

    public static ConfigurationSync getInstance() {

	if (instance == null)
	    instance = new ConfigurationSync();

	return instance;
    }

    @Override
    public GSConfiguration getClonedConfiguration() throws GSException {

	logger.trace("Getting configuration");

	try {

	    if (currentConf == null) {
		logger.trace("Current is null");
		updateConf();
	    }

	} catch (GSException ex) {

	    ErrorInfo ei = new ErrorInfo();

	    ei.setUserErrorDescription("Can not read configuration");

	    ei.setSeverity(ErrorInfo.SEVERITY_FATAL);

	    ex.addInfo(ei);

	    throw ex;
	}

	logger.trace("Cloning");

	GSConfiguration clconf = cloneConf();

	logger.trace("Cloned done");

	return clconf;   
    }

    @Override
    public GSConfiguration getConfiguration() throws GSException {

	return currentConf;
    }

    private GSConfiguration cloneConf() throws GSException {

	return queue.get();
    }

    @Override
    public void setDBGISuiteConfiguration(IGSConfigurationStorage manager) {

	this.manager = manager;

    }

    @Override
    public IGSConfigurationStorage getDBGISuiteConfiguration() {

	return this.manager;

    }

    @Override
    public GSConfiguration fetchRemote() throws GSException {

	return readRemoteConfiguration();

    }

    private synchronized void updateConf() throws GSException {

	logger.trace("Update From Remote Configuration Started (synchronized)");

	GSConfiguration c = readRemoteConfiguration();

	assign(c);

	logger.trace("Update From Remote Configuration Completed");
    }

    private GSConfiguration readRemoteConfiguration() throws GSException {

	if (this.manager == null) {

	    GSException e = new GSException();

	    ErrorInfo ei = new ErrorInfo();

	    ei.setContextId(this.getClass().getName());
	    ei.setErrorDescription("No Configuration Connector was set");
	    ei.setErrorId(ERR_ID_NODBMANAGER);
	    ei.setErrorType(ErrorInfo.ERRORTYPE_CLIENT);
	    ei.setErrorCorrection("Set the DB Manager of " + this.getClass().getName());

	    e.addInfo(ei);

	    throw e;

	} else {
	    logger.debug("Using as manager: " + manager.getClass().getName());
	}

	GSConfiguration c = this.manager.read();

	logger.trace("Remote conf has been fetched from {}", this.manager.getStorageUri().getUri());

	return c;

    }

    private void assign(GSConfiguration conf) throws GSException {

	logger.trace("Assigning configuration");

	if (conf == null) {
	    logger.trace("Remote configuration is null, returning");
	    return;
	}

	if (currentConf != null) {

	    logger.trace("Remote [{}]: {}", conf.getTimeStamp(),
		    ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(new Date(conf.getTimeStamp())));

	    logger.trace("Local  [{}]: {}", currentConf.getTimeStamp(),
		    ISO8601DateTimeUtils.getISO8601DateTimeWithMilliseconds(new Date(currentConf.getTimeStamp())));

	    if (conf.getTimeStamp() > currentConf.getTimeStamp()) {

		logger.debug("Found more recent configuration");

		synchronized (lock) {

		    currentConfCis = conf.serializeToInputStream();

		    currentConfCis.mark(Integer.MAX_VALUE);

		    currentConf = conf;

		    updateCaches(currentConf);
		}
	    } else {
		logger.debug("Current configuration is more recent, I'm not assigning new one");
	    }
	} else {

	    logger.debug("No local configuration, assigning remote one with timestamp {}", conf.getTimeStamp());

	    currentConfCis = conf.serializeToInputStream();

	    currentConfCis.mark(Integer.MAX_VALUE);

	    currentConf = conf;

	    updateCaches(currentConf);
	}
    }

    private void updateCaches(GSConfiguration c) throws GSException {

	logger.trace("Updating caches");

	queue.setBaseConf(c);

	GSConfigurationReader r = new GSConfigurationReader(c);

	allsources = r.readInstantiableType(GSSource.class, new Deserializer(), false);

	mapsources = new HashMap<>();

	allsources.forEach(s -> mapsources.put(s.getUniqueIdentifier(), s));

	logger.trace("Caches updated");

    }

    @Override
    public void update(Observable o, Object arg) {

	logger.debug("Detected possible update of configuration");

	if (arg == null) {

	    logger.warn("Updated object is null");

	    return;
	}

	if (!(arg instanceof GSConfiguration)) {

	    logger.warn("Updated object is not GSConfiguration, but {}", arg.getClass().getName());

	    return;
	}

	try {
	    assign((GSConfiguration) arg);
	} catch (GSException e) {
	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));
	}

    }

    public List<GSSource> getAllsources() {
	return allsources;
    }

    public GSSource getSource(String id) {
	return mapsources.get(id);
    }
}
