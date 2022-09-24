package eu.essi_lab.cfga.gui.components.setting.listener;

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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gui.components.SettingComponentFactory;
import eu.essi_lab.cfga.gui.components.TabContainer;
import eu.essi_lab.cfga.gui.components.listener.ButtonChangeListener;
import eu.essi_lab.cfga.gui.components.setting.SettingComponent;
import eu.essi_lab.cfga.gui.components.setting.edit_put.SettingPutDialog;
import eu.essi_lab.cfga.gui.dialog.NotificationDialog;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class SettingAddButtonConfirmationListener implements ButtonChangeListener {

    private Setting settingToAdd;
    private boolean foldedModeEnabled;
    private Configuration configuration;
    private TabContainer tabContainer;
    private SettingPutDialog dialog;

    /**
     * @param dialog
     * @param configuration
     * @param settingToAdd
     * @param tabContainer
     * @param foldedModeEnabled
     */
    public SettingAddButtonConfirmationListener(//
	    SettingPutDialog dialog, //
	    Configuration configuration, //
	    Setting settingToAdd, //
	    TabContainer tabContainer, //
	    boolean foldedModeEnabled) {

	this.dialog = dialog;
	this.configuration = configuration;
	this.settingToAdd = settingToAdd;
	this.tabContainer = tabContainer;
	this.foldedModeEnabled = foldedModeEnabled;
    }

    @Override
    public void handleEvent(ClickEvent<Button> event) {

	// clean the setting
	//
	SelectionUtils.deepClean(settingToAdd);

	SelectionUtils.deepAfterClean(settingToAdd);

	//
	// shows the header end set folded and collapse mode
	//
	settingToAdd.setShowHeader(true);

	settingToAdd.enableFoldedMode(this.foldedModeEnabled);

	// SettingHelper.collapse(settingToAdd);

	//
	// put it in the configuration
	//
	boolean put = configuration.put(settingToAdd);

	if (!put) {

	    NotificationDialog.getErrorDialog("Setting not put: " + settingToAdd.getIdentifier()).open();

	    return;
	}

	SettingComponent addedSettingComponent = SettingComponentFactory.createSettingComponent(//
		configuration, //
		settingToAdd, //
		true, //
		this.tabContainer);

	tabContainer.addSettingComponent(addedSettingComponent);

	// System.out.println(configuration);

	dialog.close();
    }

}
