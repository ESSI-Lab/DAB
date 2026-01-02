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

import java.util.TimerTask;

import com.vaadin.flow.component.UI;

/**
 * @author Fabrizio
 */
public abstract class UITask extends TimerTask {

    static final Object SHARED_LOCK = new Object();

    private UI ui;

    /**
     * @param ui
     */
    void setUI(UI ui) {

	this.ui = ui;
    }

    /**
     * @return the ui
     */
    UI getUI() {

	return ui;
    }

    @Override
    public void run() {

	if (ui == null || !ui.isAttached() || ui.isClosing() || !ui.isEnabled() || !ui.isVisible()) {
	    return;
	}
	
	synchronized (SHARED_LOCK) {

	    executePreTask();
	}
	
	ui.access(() -> {
	    
	    boolean push;

	    synchronized (SHARED_LOCK) {
		
		push = executeTask();
	    }
	    
	    if (push) {

		ui.push();
	    }
	});

    }

    /**
     * Method executed the the timer tread of this task, just before the UI access command
     */
    protected void executePreTask() {

    }

    /**
     * Task asynchronously executed inside the UI access command.<br>
     * This task is executed only if the given UI is still attached, not closing, enabled and visible
     * 
     * @return <code>true</code> if the UI need a push
     */
    protected abstract boolean executeTask();

}
