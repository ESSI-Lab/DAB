package eu.essi_lab.gssrv.servlet;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.ext.RuntimeDelegate;

import org.quartz.SchedulerException;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting;
import eu.essi_lab.gssrv.starter.GIPStarter;
import eu.essi_lab.lib.utils.Chronometer;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class ServletListener implements ServletContextListener {

    public void contextInitialized(final ServletContextEvent sce) {

	// TMP dir check
	String tmpDir = System.getProperty("java.io.tmpdir");
	File tmpDirFile = new File(tmpDir);
	if (!tmpDirFile.exists()) {
	    GSLoggerFactory.getLogger(getClass()).error("Creating Java TMP dir as it was not found: {}", tmpDir);
	    boolean success = tmpDirFile.mkdir();
	    if (!success) {
		GSLoggerFactory.getLogger(getClass()).error("Java TMP dir not found and unable to create it: {}", tmpDir);
		System.exit(1);
	    }
	}

	//
	// see GIP-235
	//
	RuntimeDelegate.setInstance(new org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl());

	Chronometer chronometer = new Chronometer();
	chronometer.start();

	GSLoggerFactory.getLogger(ServletListener.class).info("DAB initialization STARTED");

	try {

	    getStarter().start();

	} catch (GSException e) {

	    GSLoggerFactory.getLogger(ServletListener.class).error("Error starting DAB");

	    e.log();

	    System.exit(1);
	}

	GSLoggerFactory.getLogger(ServletListener.class).info("DAB initialization ENDED");
	GSLoggerFactory.getLogger(ServletListener.class).info("DAB initialization time: {}", chronometer.formatElapsedTime());
    }

    public void contextDestroyed(ServletContextEvent sce) {

	GSLoggerFactory.getLogger(ServletListener.class).info("Context destroyng STARTED");

	try {
	    StorageInfo uri = ConfigurationWrapper.getStorageInfo();

	    GSLoggerFactory.getLogger(getClass()).info("Releasing database resources");

	    Database provider = DatabaseFactory.get(uri);
	    if (provider != null) {
		provider.release();
	    }

	    SchedulerSetting schedulerSetting = ConfigurationWrapper.getSchedulerSetting();
	    SchedulerFactory.getScheduler(schedulerSetting, false).shutdown();

	} catch (GSException e) {

	    e.log();

	    System.exit(1);

	} catch (SchedulerException e) {

	    e.printStackTrace();

	    System.exit(1);
	}

	GSLoggerFactory.getLogger(ServletListener.class).info("Context destroyng ENDED");
    }

    private GIPStarter getStarter() {

	return new GIPStarter();
    }
}
