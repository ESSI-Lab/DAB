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

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.configuration.Deserializer;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.exceptions.DefaultGSExceptionHandler;
import eu.essi_lab.model.exceptions.DefaultGSExceptionLogger;
import eu.essi_lab.model.exceptions.DefaultGSExceptionReader;
import eu.essi_lab.model.exceptions.GSException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import org.slf4j.Logger;
public class ConfigurationQueueOnTheFly {


    private transient Logger logger = GSLoggerFactory.getLogger(ConfigurationQueueOnTheFly.class);

    private GSConfiguration baseConf;

    private static ConfigurationQueueOnTheFly instance;

    private Object lock = new Object();

    private InputStream currentConfCis;

    private ConfigurationQueueOnTheFly() {
	// force singleton

    }

    public static ConfigurationQueueOnTheFly getInstance() {

	if (instance == null)
	    instance = new ConfigurationQueueOnTheFly();

	return instance;

    }

    public GSConfiguration get() throws GSException {

	Long start = new Date().getTime();
	logger.trace("Configuration queue get start");

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

    public void setBaseConf(GSConfiguration baseConf) {

	this.baseConf = baseConf;

	try {

	    synchronized (lock) {
		currentConfCis = this.baseConf.serializeToInputStream();

		currentConfCis.mark(Integer.MAX_VALUE);

	    }
	} catch (GSException e) {
	    logger.warn("Missed entry in configuration queue");

	    DefaultGSExceptionLogger.log(new DefaultGSExceptionHandler(new DefaultGSExceptionReader(e)));

	}

    }


}
