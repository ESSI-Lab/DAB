package eu.essi_lab.cfga.gui.components.setting.edit_put;

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
import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.gui.components.listener.*;
import eu.essi_lab.cfga.gui.components.setting.*;
import eu.essi_lab.cfga.gui.components.setting.listener.*;
import eu.essi_lab.cfga.gui.components.tabs.*;
import eu.essi_lab.cfga.gui.directive.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.cfga.setting.validation.*;

import java.util.*;

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

	Optional<EditDirective> editDirective = tabContent == null ? Optional.empty() : tabContent.getEditDirective();

	String title = editDirective.flatMap(EditDirective::getDescription).orElse("Edit setting");

	setHeader(title);
	setConfirmText("Apply changes");
	setCancelText("Discard changes");

	this.settingToEdit = SelectionUtils.resetAndSelect(setting, true);

	//
	// hides the header and opens expands the setting
	//

	this.foldedModeEnabled = this.settingToEdit.isFoldedModeEnabled();

	this.settingToEdit.enableFoldedMode(false);

	// SettingHelper.expand(settingToEdit);

	Component settingToAddComponent = createSettingToAddOrEditComponent(configuration, //
		this.settingToEdit, //
		dialogHeight, //
		editDirective.map(EditDirective::isTabView).orElse(false) //
	);

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
