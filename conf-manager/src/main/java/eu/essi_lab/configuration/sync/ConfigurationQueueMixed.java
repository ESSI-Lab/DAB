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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.GSException;
class ConfigurationQueueMixed {

    List<GSConfiguration> list = new ArrayList<>();
    ConfigurationQueueOnTheFly onthefly = null;
    private transient Logger logger = GSLoggerFactory.getLogger(ConfigurationQueueMixed.class);
    private GSConfiguration baseConf;
    private static final int INITIAL_QUEUE_SIZE = 10;
    private static final int MAX_QUEUE_SIZE = 50;
    private static ConfigurationQueueMixed instance;
    private final AddNewConf addTask = new AddNewConf();
    private final RestoreQueueTask restore = new RestoreQueueTask();
    private ExecutorService executor = Executors.newFixedThreadPool(1);
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Object lock = new Object();
    private InputStream currentConfCis;
    private Integer watingtasks = 0;

    private ConfigurationQueueMixed() {
	// force singleton
    }

    public static ConfigurationQueueMixed getInstance() {

	if (instance == null)
	    instance = new ConfigurationQueueMixed();

	return instance;

    }

    private Optional<GSConfiguration> doGetIfListNotEmpty() {

	fillQueueIfPossible();

	synchronized (lock) {
	    if (!list.isEmpty()) {

		GSConfiguration c;

		c = list.remove(0);
		logger.trace("Conf from queue (new list size {})", list.size());

		return Optional.of(c);
	    }
	}

	return Optional.empty();
    }

    public GSConfiguration get() throws GSException {

	if (onthefly != null) {
	    onthefly.setBaseConf(this.baseConf);
	    return onthefly.get();
	}

	Long start = new Date().getTime();

	Optional<GSConfiguration> opt = doGetIfListNotEmpty();

	if (!opt.isPresent()) {

	    logger.trace("Empty configuration queue, generating new clone");

	    GSConfiguration c = null;

	    synchronized (lock) {
		try {
		    currentConfCis.reset();
		    c = new Deserializer().deserialize(currentConfCis, GSConfiguration.class);
		} catch (IOException e) {
		    logger.warn("IOException resetting current configuration stream", e);
		}

	    }

	    logger.trace("Configuration queue get end {}", (new Date().getTime() - start));

	    return c;

	}

	logger.trace("(from queue) Configuration queue get end {}", (new Date().getTime() - start));

	return opt.get();

    }

    private synchronized void fillQueueIfPossible() {

	Integer wt = getWatingtasks();

	logger.trace("{} waiting tasks", wt);

	if (wt > 9) {
	    logger.trace("Too many wating tasks, skip");
	    onthefly = ConfigurationQueueOnTheFly.getInstance();

	    scheduler.schedule(restore, 1L, TimeUnit.MINUTES);
	    return;
	}

	increaseWatingtasks();

	executor.submit(addTask);

    }

    public synchronized Integer getWatingtasks() {
	return watingtasks;
    }

    public synchronized void increaseWatingtasks() {
	this.watingtasks++;
    }

    public synchronized void decreaseWatingtasks() {
	this.watingtasks--;
    }

    private class RestoreQueueTask implements Runnable {

	@Override
	public void run() {

	    logger.trace("Stting on the fly to null");

	    onthefly = null;
	}
    }

    private class AddNewConf implements Runnable {

	@Override
	public void run() {

	    synchronized (lock) {
		try {

		    logger.trace("Adding new conf STARTED");

		    int s = list.size();

		    if (s < MAX_QUEUE_SIZE) {

			logger.trace("List size {} < MAX_QUEUE_SIZE {}", s, MAX_QUEUE_SIZE);

			currentConfCis.reset();

			logger.trace("stream reset done");

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

	    decreaseWatingtasks();
	}
    }

    public void setBaseConf(GSConfiguration baseConf) {

	this.baseConf = baseConf;

	if (onthefly != null) {

	    onthefly.setBaseConf(this.baseConf);

	}

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
