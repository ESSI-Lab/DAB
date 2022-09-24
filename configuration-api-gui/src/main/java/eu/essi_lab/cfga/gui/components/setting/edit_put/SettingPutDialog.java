package eu.essi_lab.cfga.gui.components.setting.edit_put;

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

import java.util.Optional;

import com.vaadin.flow.component.Component;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gui.components.TabContainer;
import eu.essi_lab.cfga.gui.components.listener.ButtonChangeListener;
import eu.essi_lab.cfga.gui.components.setting.listener.SettingAddButtonConfirmationListener;
import eu.essi_lab.cfga.gui.extension.directive.AddDirective;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.validation.ValidationContext;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class SettingPutDialog extends SettingPutOrEditDialog {

    private Setting settingToAdd;
    private AddDirective addDirective;

    /**
     * @param configuration
     * @param tabContainer
     * @param addDirective
     */
    public SettingPutDialog(//
	    Configuration configuration, //
	    TabContainer tabContainer, //
	    AddDirective addDirective) {

	super(configuration, tabContainer, ValidationContext.put());
	
	this.addDirective = addDirective;
	this.settingToAdd = SettingUtils.create(addDirective.getSettingClass());

	setTitle(addDirective.getName());
	setConfirmText("Add");

	//
	// hides the header and opens expands the setting
	//
	this.settingToAdd.setShowHeader(false);

	this.foldedModeEnabled = this.settingToAdd.isFoldedModeEnabled();

	this.settingToAdd.enableFoldedMode(false);

	// SettingHelper.expand(settingToAdd);

	Component settingToAddComponent = createSettingToAddOrEditComponent(configuration, this.settingToAdd, dialogHeight);

	setContent(settingToAddComponent);
    }

    @Override
    protected Setting getSetting() {

	return this.settingToAdd;
    }

    @Override
    protected Optional<AddDirective> getDirective() {

	return Optional.of(addDirective);
    }

    @Override
    protected ButtonChangeListener getConfirmationListener() {
	return new SettingAddButtonConfirmationListener(//
		this, //
		configuration, //
		settingToAdd, //
		tabContainer, //
		foldedModeEnabled);
    }
}
