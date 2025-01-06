package eu.essi_lab.cfga.gui.components.setting.listener;

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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gui.components.SettingComponentFactory;
import eu.essi_lab.cfga.gui.components.TabContainer;
import eu.essi_lab.cfga.gui.components.listener.ButtonChangeListener;
import eu.essi_lab.cfga.gui.components.setting.SettingComponent;
import eu.essi_lab.cfga.gui.components.setting.edit_put.SettingEditDialog;
import eu.essi_lab.cfga.gui.dialog.NotificationDialog;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class SettingEditButtonConfirmationListener implements ButtonChangeListener {

    private Configuration configuration;
    private Setting settingToEdit;
    private boolean foldedModeEnabled;
    private SettingComponent currentSettingComponent;
    private TabContainer tabContainer;
    private SettingEditDialog dialog;

    /**
     * @param dialog
     * @param configuration
     * @param settingToEdit
     * @param currentSettingComponent
     * @param tabContainer
     * @param foldedModeEnabled
     */
    public SettingEditButtonConfirmationListener(//
	    SettingEditDialog dialog, //
	    Configuration configuration, //
	    Setting settingToEdit, //
	    SettingComponent currentSettingComponent, //
	    TabContainer tabContainer, //
	    boolean foldedModeEnabled) {

	this.dialog = dialog;
	this.configuration = configuration;
	this.settingToEdit = settingToEdit;
	this.currentSettingComponent = currentSettingComponent;
	this.tabContainer = tabContainer;
	this.foldedModeEnabled = foldedModeEnabled;
    }

    @Override
    public void handleEvent(ClickEvent<Button> event) {

	//
	// clean the setting
	//
	SelectionUtils.deepClean(settingToEdit);

	SelectionUtils.deepAfterClean(settingToEdit);

	//
	// shows the header end set folded and collapse mode
	//
	settingToEdit.setShowHeader(true);

	settingToEdit.enableFoldedMode(this.foldedModeEnabled);

	// SettingHelper.collapse(settingToEdit);

	if (configuration.contains(settingToEdit)) {

	    NotificationDialog.getWarningDialog("No changes to apply").open();

	    return;
	}

	//
	// replaces the setting in the configuration
	//
	boolean replaced = configuration.replace(settingToEdit);

	if (!replaced) {

	    NotificationDialog.getErrorDialog("Setting not replaced: " + settingToEdit.getIdentifier()).open();

	    return;
	}

	//
	//
	//

	if (currentSettingComponent != null) {

	    SettingComponent editedSettingComponent = SettingComponentFactory.createSettingComponent(//
		    configuration, //
		    settingToEdit, //
		    true, //
		    this.tabContainer);

	    tabContainer.replaceSettingComponent(currentSettingComponent, editedSettingComponent);
	}

	dialog.close();
    }
}
