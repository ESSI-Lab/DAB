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

import eu.essi_lab.cfga.gui.dialog.NotificationDialog;

/**
 * @author Fabrizio
 */
public class IdleTracker {

    private static final IdleTracker INSTANCE = new IdleTracker();

    // private static NotificationDialog dialog;

    private Timer timer;
    private boolean idle;

    /**
     * 
     * 
     */
    private static final long MAX_IDLE_PERIOD = 180; // 3 minutes

    private UI ui;

    private ConfigurationView view;

    private static NotificationDialog dialog;

    /**
     * @return
     */
    public static IdleTracker getInstance() {

	return INSTANCE;
    }

    /**
     * 
     */
    private IdleTracker() {

	init();
    }

    /**
     * @param view
     */
    public void setView(ConfigurationView view) {

	this.view = view;
    }

    /**
     * @return
     */
    public boolean isIdle() {

	synchronized (UITask.SHARED_LOCK) {

	    return this.idle;
	}
    }

    /**
     * 
     */
    public void reset() {

	synchronized (UITask.SHARED_LOCK) {

	    this.idle = false;

	    if (timer != null) {
		timer.cancel();
	    }

	    init();
	}
    }
    
    /**
     * 
     */
    public void notifyTabClosed() {

	if (dialog != null) {

	    dialog.close();
	}
    }

    /**
     * @param ui
     */
    void setUI(UI ui) {

	this.ui = ui;
    }

    /**
     * @author Fabrizio
     */
    private class IdleTrackerTask extends UITask {

	@Override
	protected boolean executeTask() {
	    
	    if(view == null){
		
		return false;
	    }

	    if (!SingleTabManager.getInstance().isTabOpen()) {

		return false;
	    }

//	    if(dialog != null && dialog.isOpened()){
//		
//		return false;
//	    }
	    
	    if (!IdleTracker.this.idle) {

		IdleTracker.this.idle = true;
		
		view.logoutButton.click();
		
//		view.getLogOutButtonListener().handleEvent(null);

//		if (dialog == null) {
//
//		    dialog = initDialog();
//
//		    dialog.open();
//
//		} else {
//
//		    dialog.close();
//
//		    dialog = initDialog();
//
//		    dialog.open();
//		}
//
//		view.refresh();
//		view.getSaveButton().setEnabled(false);

		return false;
	    }

	    return false;
	}
    }

    /**
     * 
     */
    private void init() {

	timer = new Timer();

	//
	// a task instance can be scheduled only once, so a new task instance is required when a new timer is created
	//
	IdleTrackerTask task = new IdleTrackerTask();
	task.setUI(this.ui);

	timer.schedule(task, TimeUnit.SECONDS.toMillis(MAX_IDLE_PERIOD), TimeUnit.SECONDS.toMillis(MAX_IDLE_PERIOD));
    }

    /**
     * @return
     */
    private NotificationDialog initDialog() {

	// dialog.addDialogCloseActionListener(new ComponentEventListener<Dialog.DialogCloseActionEvent>() {
	//
	// @Override
	// public void onComponentEvent(Dialog.DialogCloseActionEvent event) {
	//
	// synchronized (UITask.SHARED_LOCK) {
	//
	// view.refresh();
	// view.getSaveButton().setEnabled(false);
	// }
	// }
	// });
	//
	// dialog.setOnCancelListener(new ButtonChangeListener() {
	//
	// @Override
	// public void handleEvent(ClickEvent<Button> event) {
	//
	// synchronized (UITask.SHARED_LOCK) {
	//
	// view.refresh();
	// view.getSaveButton().setEnabled(false);
	// }
	// }
	// });
	//
	// dialog.setOnCloseButtonListener(new ComponentEventListener<ClickEvent<Button>>() {
	//
	// @Override
	// public void onComponentEvent(ClickEvent<Button> event) {
	//
	// synchronized (UITask.SHARED_LOCK) {
	//
	// view.refresh();
	// view.getSaveButton().setEnabled(false);
	// }
	// }
	// });

	return NotificationDialog.getWarningDialog("Session expired due to long inactivity", 400);
    }
}
