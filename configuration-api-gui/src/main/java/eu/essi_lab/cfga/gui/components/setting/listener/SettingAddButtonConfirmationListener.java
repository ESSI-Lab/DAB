package eu.essi_lab.cfga.gui.components.setting.listener;

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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.Selector;
import eu.essi_lab.cfga.gui.components.SettingComponentFactory;
import eu.essi_lab.cfga.gui.components.tabs.TabContent;
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
    private final boolean foldedModeEnabled;
    private final Configuration configuration;
    private final TabContent tabContent;
    private final SettingPutDialog dialog;

    /**
     * @param dialog
     * @param configuration
     * @param settingToAdd
     * @param tabContent
     * @param foldedModeEnabled
     */
    public SettingAddButtonConfirmationListener(//
	    SettingPutDialog dialog, //
	    Configuration configuration, //
	    Setting settingToAdd, //
	    TabContent tabContent, //
	    boolean foldedModeEnabled) {

	this.dialog = dialog;
	this.configuration = configuration;
	this.settingToAdd = settingToAdd;
	this.tabContent = tabContent;
	this.foldedModeEnabled = foldedModeEnabled;
    }

    @Override
    public void handleEvent(ClickEvent<Button> event) {

	//
	// in case of a selector, retrieves the selected setting
	// 
	if (Selector.class.isAssignableFrom(settingToAdd.getSettingClass())) {

	    @SuppressWarnings("rawtypes")
	    Selector selector = (Selector)settingToAdd;
	    
	    settingToAdd = (Setting) selector.getSelectedSettings().getFirst();
	}

	//
	// clean the setting
	//
	SelectionUtils.deepClean(settingToAdd);

	SelectionUtils.deepAfterClean(settingToAdd);

	//
	// enables the header end set folded and collapse mode
	//
	settingToAdd.setForceHideheader(false);

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
		this.tabContent);

	tabContent.addSettingComponent(addedSettingComponent);

	dialog.close();
    }

}
