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
import eu.essi_lab.cfga.gui.components.SettingComponentFactory;
import eu.essi_lab.cfga.gui.components.TabContainer;
import eu.essi_lab.cfga.gui.components.listener.ButtonChangeListener;
import eu.essi_lab.cfga.gui.components.setting.SettingComponent;
import eu.essi_lab.cfga.gui.dialog.ConfirmationDialog;
import eu.essi_lab.cfga.gui.dialog.NotificationDialog;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class SettingRemoveButtonListener implements ButtonChangeListener {

    private SettingComponent component;
    private Configuration configuration;
    private TabContainer tabContainer;

    /**
     * @param configuration
     * @param tabContainer
     * @param component
     */
    public SettingRemoveButtonListener(Configuration configuration, TabContainer tabContainer, SettingComponent component) {

	this.configuration = configuration;
	this.tabContainer = tabContainer;
	this.component = component;
    }

    /**
     * @author Fabrizio
     */
    private class OnConfirmListener implements ButtonChangeListener {

	@Override
	public void handleEvent(ClickEvent<Button> event) {

	    Setting setting = component.getSetting();
	    boolean removed = configuration.remove(setting.getIdentifier());

	    if (!removed) {

		NotificationDialog.getErrorDialog("Setting not removed: " + setting.getIdentifier()).open();

		return;
	    }

	    tabContainer.removeSettingComponent(component, setting.getIdentifier());

	    GSLoggerFactory.getLogger(getClass()).debug("Removed setting: " + setting.getName());
	}
    }

    @Override
    public void handleEvent(ClickEvent<Button> event) {

	if (tabContainer.getRemoveDirective().isPresent()) {

	    boolean allowFullRemoval = tabContainer.getRemoveDirective().get().isAllowFullRemoval();

	    Class<? extends Setting> settingClass = tabContainer.getRemoveDirective().get().getSettingClass();

	    GSLoggerFactory.getLogger(getClass()).trace("Listing settings STARTED");
	    
	    int size = configuration.size(settingClass);

	    GSLoggerFactory.getLogger(getClass()).trace("Listing settings ENDED");

	    if (size == 1 && !allowFullRemoval) {

		NotificationDialog.getInfoDialog("Removal of all settings is not allowed").open();
		return;
	    }
	}

	ConfirmationDialog dialog = SettingComponentFactory.createSettingRemoveDialog(new OnConfirmListener());
	
	dialog.addToCloseAll();

	dialog.open();
    }
}
