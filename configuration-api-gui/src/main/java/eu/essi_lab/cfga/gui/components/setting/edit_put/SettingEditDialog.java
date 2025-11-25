package eu.essi_lab.cfga.gui.components.setting.edit_put;

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

import com.vaadin.flow.component.Component;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gui.components.TabContent;
import eu.essi_lab.cfga.gui.components.listener.ButtonChangeListener;
import eu.essi_lab.cfga.gui.components.setting.SettingComponent;
import eu.essi_lab.cfga.gui.components.setting.listener.SettingEditButtonConfirmationListener;
import eu.essi_lab.cfga.gui.extension.directive.EditDirective;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.validation.ValidationContext;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class SettingEditDialog extends SettingPutOrEditDialog {

    private final Setting settingToEdit;
    private final SettingComponent currentSettingComponent;

    /**
     * @param configuration
     * @param setting
     */
    public SettingEditDialog(//
	    Configuration configuration, //
	    Setting setting) {//

	this(configuration, setting, null, null);
    }

    /**
     * @param configuration
     * @param setting
     * @param tabContent
     */
    public SettingEditDialog(//
	    Configuration configuration, //
	    Setting setting, //
	    SettingComponent currentSettingComponent, //
	    TabContent tabContent) {//

	super(configuration, tabContent, ValidationContext.edit());

	this.currentSettingComponent = currentSettingComponent;
	
	Optional<EditDirective> editDirective = tabContent == null? Optional.empty() : tabContent.getEditDirective();
	String title = "Edit setting";
	
	if(editDirective.isPresent()){
	    
	    title = editDirective.get().getName();	    
	}

	setTitle(title);
	setConfirmText("Apply changes");
	setCancelText("Discard changes");

	this.settingToEdit = SelectionUtils.resetAndSelect(setting, true);

	//
	// hides the header and opens expands the setting
	//
	this.settingToEdit.setShowHeader(false);

	this.foldedModeEnabled = this.settingToEdit.isFoldedModeEnabled();

	this.settingToEdit.enableFoldedMode(false);

	// SettingHelper.expand(settingToEdit);

	Component settingToAddComponent = createSettingToAddOrEditComponent(configuration, this.settingToEdit, dialogHeight);

	setContent(settingToAddComponent);
    }

    @Override
    protected Setting getSetting() {

	return this.settingToEdit;
    }

    @Override
    protected ButtonChangeListener getConfirmationListener() {

	return new SettingEditButtonConfirmationListener(//
		this, //
		configuration, //
		settingToEdit, //
		currentSettingComponent, //
		tabContent, //
		foldedModeEnabled);
    }

    @Override
    protected Optional<EditDirective> getDirective() {

	return tabContent == null ? Optional.empty() : tabContent.getEditDirective();
    }
}
