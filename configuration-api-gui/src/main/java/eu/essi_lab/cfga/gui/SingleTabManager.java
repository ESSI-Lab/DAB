package eu.essi_lab.cfga.gui;

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

import java.util.Timer;
import java.util.concurrent.TimeUnit;

import com.vaadin.flow.component.UI;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class SingleTabManager extends UITask {

    private boolean open;

    /**
     * The heartbeat interval of 30 seconds, as defined in the web.xml file
     */
    private static final int HEARTBEAT_INTERVAL = 30;

    /**
     * The delay after the last heartbeat after that we can assume that the tab is closed
     */
    private static final long LAST_HEARTBEAT_INTERVAL_DELAY = HEARTBEAT_INTERVAL + 15;

    /**
     * 
     */
    private static final long CHECK_PERIOD = 15;

    /**
     * 
     */
    private static final SingleTabManager INSTANCE = new SingleTabManager();

    /**
     * @return
     */
    public static SingleTabManager getInstance() {

	return INSTANCE;
    }

    /**
     * 
     */
    private SingleTabManager() {

	Timer timer = new Timer();
	timer.scheduleAtFixedRate(this, //
		TimeUnit.SECONDS.toMillis(CHECK_PERIOD), //
		TimeUnit.SECONDS.toMillis(CHECK_PERIOD));
    }

    /**
     * @return
     */
    public synchronized boolean isTabOpen() {

	return open;
    }

    /**
     * 
     */
    public synchronized void setTabOpen(boolean set) {

	this.open = set;
    }

    /**
     * @param ui
     */
    @Override
    public synchronized void setUI(UI ui) {

	super.setUI(ui);
    }

    @Override
    public void run() {

	if (getUI() == null || !getUI().isAttached() || getUI().isClosing() || !getUI().isEnabled() || !getUI().isVisible()) {

	    this.open = false;

//	    GSLoggerFactory.getLogger(getClass()).info("----- UI no longer active -----");

	    return;
	}

	getUI().access(() -> {

	    synchronized (SHARED_LOCK) {

		executeTask();
	    }
	});
    }

    /**
     * 
     */
    public String getRemainingSessionTime() {

	long lastHeartbeatTimestamp = getUI().getInternals().getLastHeartbeatTimestamp();

	long currentTimeMillis = System.currentTimeMillis();

	long elapsedTimeFromLastHeartbeat = TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis - lastHeartbeatTimestamp);

	long remaining = LAST_HEARTBEAT_INTERVAL_DELAY - elapsedTimeFromLastHeartbeat;

	return String.valueOf(remaining);
    }

    @Override
    protected boolean executeTask() {

	if (!this.open) {

	    return false;
	}

	long lastHeartbeatTimestamp = getUI().getInternals().getLastHeartbeatTimestamp();

	long currentTimeMillis = System.currentTimeMillis();

	long elapsedTimeFromLastHeartbeat = TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis - lastHeartbeatTimestamp);

	if (elapsedTimeFromLastHeartbeat > TimeUnit.SECONDS.toMillis(LAST_HEARTBEAT_INTERVAL_DELAY)) {

	    GSLoggerFactory.getLogger(getClass()).info("----- Tab expired -----");
	    GSLoggerFactory.getLogger(getClass()).info("Elapsed time from last heartbeat {} seconds ", elapsedTimeFromLastHeartbeat);

	    this.open = false;

	    IdleTracker.getInstance().notifyTabClosed();

	    System.gc();

	    //
	    // this is the case when the user closes the tab when he's still
	    // the lock owner. this way we allow another user to obtain the lock
	    //
	    // if (LockManager.getInstance().isAcquired()) {
	    //
	    // LockManager.getInstance().releaseLock();
	    // }
	}

	//
	// no need to push also in case the lock is released since
	// changes to a hidden UI has no sense
	//
	return false;
    }
}
