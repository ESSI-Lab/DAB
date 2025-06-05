package eu.essi_lab.cfga.gui;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.util.Optional;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.vaadin.flow.component.UI;

import eu.essi_lab.cfga.ConfigurationSource;
import eu.essi_lab.cfga.ConfigurationSource.LockAcquisitionResult;
import eu.essi_lab.cfga.gui.components.EnabledGroupManager;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class LockManager extends UITask {

    private static final String LOCK_ACQUIRED_MESSAGE = "Write permission acquired. Any pending changes are lost";
    private static final String LOCK_REJECTED_MESSAGE = "Another user is online";
    private static final String LOCK_RELEASED_MESSAGE = "Write permission lost due to long inactivity";

    /**
     * 
     */
    private static final String LOCK_OWNER = UUID.randomUUID().toString();

    /**
     * 
     */
    private static final LockManager INSTANCE = new LockManager();

    /**
     * 
     * 
     */
    private static final long CHECK_PERIOD = 30; // seconds

    /**
     * After CHECK_PERIOD * 3 seconds, we can assume that the lock owner is offline
     */
    private static final long MAX_IDLE_TIME = CHECK_PERIOD * 3; // seconds

    private ConfigurationView view;
    private ConfigurationSource source;
    private boolean acquired;
    private boolean firstAcquiring;

    private LockManager() {

	firstAcquiring = true;

	Timer timer = new Timer();
	timer.schedule(//
		this, //
		TimeUnit.SECONDS.toMillis(CHECK_PERIOD), //
		TimeUnit.SECONDS.toMillis(CHECK_PERIOD));
    }

    /**
     * @return
     */
    public static LockManager getInstance() {

	return INSTANCE;
    }

    /**
     * @return
     */
    public boolean isAcquired() {

	return acquired;
    }

    /**
     * @param source
     */
    void setSource(ConfigurationSource source) {

	this.source = source;
    }

    /**
     * @param ui
     * @param view
     */
    void setUI(UI ui, ConfigurationView view) {

	super.setUI(ui);

	this.view = view;
    }

    @Override
    protected boolean executeTask() {

	checkLock();

	return true;
    }

    /**
     * If the tracker is not idle and the tab is open, first check the orphan lock existence.
     * If found, tried to remove it. If the removal succeeds or no orphan lock is found, tried to
     * acquire the lock
     */
    void checkLock() {

//	if (IdleTracker.getInstance().isIdle()) {
//
//	    GSLoggerFactory.getLogger(getClass()).debug("Lock checking skipped due to long inactivity");
//	    return;
//	}

	if (!SingleTabManager.getInstance().isTabOpen()) {

	    GSLoggerFactory.getLogger(getClass()).debug("Lock checking skipped since no tab is opened");
	    return;
	}

	GSLoggerFactory.getLogger(getClass()).debug("Lock checking STARTED");

	//
	//
	//

	boolean acquireLock = checkOrphanLock();
	if (acquireLock) {

	    tryAcquire();
	}

	//
	//
	//

	GSLoggerFactory.getLogger(getClass()).debug("Lock checking ENDED");
    }

    /**
     * Return true only if no orphan lock is found, or it has been correctly removed
     */
    private boolean checkOrphanLock() {

	GSLoggerFactory.getLogger(getClass()).debug("Orphan lock check STARTED");

	boolean tryAcquire = false;
	boolean orphanLockFound = false;

	try {
	    orphanLockFound = source.//
		    orphanLockFound(//
			    TimeUnit.SECONDS.toMillis(MAX_IDLE_TIME));

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Orphan lock existence check failed");
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	if (orphanLockFound) {

	    GSLoggerFactory.getLogger(getClass()).debug("Removing orphan lock STARTED");

	    try {
		boolean released = source.releaseLock();

		if (released) {

		    GSLoggerFactory.getLogger(getClass()).debug("Orphan lock removed");

		    tryAcquire = true;

		} else {

		    GSLoggerFactory.getLogger(getClass()).error("Orphan lock removal failed");
		}

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error("Orphan lock removal failed");
		GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    }

	    GSLoggerFactory.getLogger(getClass()).debug("Removing orphan lock ENDED");

	} else {

	    GSLoggerFactory.getLogger(getClass()).debug("No orphan orphan lock found");

	    tryAcquire = true;
	}

	GSLoggerFactory.getLogger(getClass()).debug("Orphan lock check ENDED");

	return tryAcquire;
    }

    /**
     * 
     */
    private void tryAcquire() {

	try {

	    LockAcquisitionResult result = source.acquireLock(LOCK_OWNER);

	    acquired = result != LockAcquisitionResult.REJECTED;

	    GSLoggerFactory.getLogger(getClass()).debug("Lock acquisition: {}", result);

	    updateLockAcquiredState(Optional.of(result), view);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).debug("Lock acquisition failed");
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }

    /**
     * 
     */
    void releaseLock() {

	GSLoggerFactory.getLogger(getClass()).debug("Lock releasing STARTED");

	try {
	    boolean released = source.releaseLock();

	    acquired = !released;

	    GSLoggerFactory.getLogger(getClass()).debug("Lock releasing: " + (released ? "succeeded" : "failed"));

	    updateLockAcquiredState(Optional.empty(), view);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	GSLoggerFactory.getLogger(getClass()).debug("Lock releasing ENDED");
    }

    /**
     * @param acquired
     * @param view
     */
    private void updateLockAcquiredState(Optional<LockAcquisitionResult> result, ConfigurationView view) {

	if (result.isPresent()) {

	    switch (result.get()) {
	    case SUCCEEDED:

		view.infoLabel.setText(LOCK_ACQUIRED_MESSAGE);

		view.infoLabel.getStyle().set("background-color", "white");
		view.infoLabel.getStyle().set("color", "green");
		view.infoLabel.getStyle().set("border", "1px solid green");

		EnabledGroupManager.getInstance().setEnabled(true);

		//
		// when the lock is acquired the save button must be disabled since there are
		// no pending changes to apply. If the user has just loaded the configuration page
		// of course there are no pending changes.
		// If the user reacquire a lost lost after a long inactivity, possible pending
		// changes MUST BE CLEARED and so the button must be disabled because in the meantime
		// another user may have changed the configuration, and the application of pending changes
		// could compromise the configuration integrity
		//
		if (!firstAcquiring) {

		    view.refresh();
		}
		
		view.getSaveButton().setEnabled(false);

		firstAcquiring = false;

	    case OWNED:

		// nothing to do in this case

		break;
	    case REJECTED:

		view.infoLabel.setText(LOCK_REJECTED_MESSAGE);

		view.infoLabel.getStyle().set("background-color", "white");
		view.infoLabel.getStyle().set("color", "red");
		view.infoLabel.getStyle().set("border", "1px solid red");

		EnabledGroupManager.getInstance().setEnabled(false);

		break;
	    }
	} else {

	    view.infoLabel.setText(LOCK_RELEASED_MESSAGE);

	    view.infoLabel.getStyle().set("background-color", "white");
	    view.infoLabel.getStyle().set("color", "lightgray");
	    view.infoLabel.getStyle().set("border", "1px solid lightgray");

	    EnabledGroupManager.getInstance().setEnabled(false);
	}
    }
}
