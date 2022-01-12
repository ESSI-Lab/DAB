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

import eu.essi_lab.model.exceptions.ErrorInfo;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.GSException;
class ConfigurationQueue {

    List<GSConfiguration> list = new ArrayList<>();

    private transient Logger logger = GSLoggerFactory.getLogger(ConfigurationQueue.class);

    private GSConfiguration baseConf;
    private static final int INITIAL_QUEUE_SIZE = 10;
    private static final int MAX_QUEUE_SIZE = 50;
    private static ConfigurationQueue instance;

    private final AddNewConf addTask = new AddNewConf();
    private ExecutorService executor = Executors.newFixedThreadPool(5);

    private Object lock = new Object();

    private InputStream currentConfCis;

    private ConfigurationQueue() {
	// force singleton
    }

    public static ConfigurationQueue getInstance() {

	if (instance == null)
	    instance = new ConfigurationQueue();

	return instance;

    }

    public GSConfiguration get() throws GSException {

	if (list.size() > 0) {

	    GSConfiguration c;

	    synchronized (lock) {
		c = list.remove(0);
	    }
	    executor.submit(addTask);

	    return c;
	}

	executor.submit(addTask);

	synchronized (lock) {
	    try {
		currentConfCis.reset();
	    } catch (IOException e) {
		logger.error("Can't reset configuration stream", e);
	    }

	    return new Deserializer().deserialize(currentConfCis, GSConfiguration.class);
	}

    }

    private class AddNewConf implements Runnable {

	@Override
	public void run() {

	    synchronized (lock) {
		try {

		    logger.trace("Adding new conf STARTED");

		    currentConfCis.reset();

		    list.add(new Deserializer().deserialize(currentConfCis, GSConfiguration.class));

		    if (list.size() < MAX_QUEUE_SIZE) {

			logger.trace("List size < MAX_QUEUE_SIZE");

			currentConfCis.reset();

			list.add(new Deserializer().deserialize(currentConfCis, GSConfiguration.class));
		    }

		    logger.trace("Adding new conf ENDED");

		} catch (GSException e) {

		    logger.warn("Can't refill configuration queue");

		    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));

		} catch (IOException e) {
		    logger.error("Can't reset configuration stream", e);
		}
	    }
	}
    }

    public void setBaseConf(GSConfiguration baseConf) {

	this.baseConf = baseConf;

	try {

	    synchronized (lock) {
		currentConfCis = this.baseConf.serializeToInputStream();

		currentConfCis.mark(Integer.MAX_VALUE);

		list = new ArrayList<>();

		for (int i = 0; i < INITIAL_QUEUE_SIZE; i++) {

		    currentConfCis.reset();

		    list.add(new Deserializer().deserialize(currentConfCis, GSConfiguration.class));
		}
	    }
	} catch (GSException e) {
	    logger.warn("Missed entry in configuration queue");

	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));
	} catch (IOException e) {
	    logger.error("Can't reset configuration stream", e);

	}

    }
}
