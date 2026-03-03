package eu.essi_lab.cfga.gui.components.setting.listener;

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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.*;
import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.gui.components.listener.*;
import eu.essi_lab.cfga.gui.components.setting.*;
import eu.essi_lab.cfga.gui.components.tabs.*;
import eu.essi_lab.cfga.gui.dialog.*;
import eu.essi_lab.cfga.gui.directive.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.lib.utils.*;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class SettingRemoveButtonListener implements ButtonChangeListener {

    private final SettingComponent component;
    private final Configuration configuration;
    private final TabContent tabContent;

    /**
     * @param configuration
     * @param tabContent
     * @param component
     */
    public SettingRemoveButtonListener(Configuration configuration, TabContent tabContent, SettingComponent component) {

	this.configuration = configuration;
	this.tabContent = tabContent;
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

	    tabContent.removeSettingComponent(component, setting.getIdentifier());

	    GSLoggerFactory.getLogger(getClass()).debug("Removed setting: {}", setting.getName());
	}
    }

    @Override
    public void handleEvent(ClickEvent<Button> event) {

	if (tabContent.getRemoveDirective().isPresent()) {

	    boolean allowFullRemoval = tabContent.getRemoveDirective().get().isFullRemovalAllowed();

	    Class<? extends Setting> settingClass = tabContent.getRemoveDirective().get().getSettingClass();

	    GSLoggerFactory.getLogger(getClass()).trace("Listing settings STARTED");
	    
	    int size = configuration.size(settingClass);

	    GSLoggerFactory.getLogger(getClass()).trace("Listing settings ENDED");

	    if (size == 1 && !allowFullRemoval) {

		NotificationDialog.getInfoDialog("Removal of all settings is not allowed").open();
		return;
	    }
	}

	String desc = tabContent.getRemoveDirective().flatMap(RemoveDirective::getDescription).orElse("Removal confirmation");

	ConfirmationDialog dialog = createSettingRemoveDialog(desc, new OnConfirmListener());
	
	dialog.addToCloseAll();

	dialog.open();
    }

    /**
     *
     * @param title
     * @param onConfirmListener
     * @return
     */
    private ConfirmationDialog createSettingRemoveDialog(String title, ButtonChangeListener onConfirmListener) {

	ConfirmationDialog dialog = new ConfirmationDialog("Are you sure you want to remove this setting?", onConfirmListener);
	dialog.setTitle(title);

	dialog.addToCloseAll();

	return dialog;
    }
}
