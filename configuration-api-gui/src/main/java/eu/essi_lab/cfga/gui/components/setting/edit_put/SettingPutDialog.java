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
public class SettingPutDialog extends SettingPutOrEditDialog {

    private final Setting settingToAdd;
    private final AddDirective addDirective;

    /**
     * @param configuration
     * @param tabContent
     * @param addDirective
     */
    public SettingPutDialog(//
	    Configuration configuration, //
	    TabContent tabContent, //
	    AddDirective addDirective) {

	super(configuration, tabContent, ValidationContext.put());
	
	this.addDirective = addDirective;
	this.settingToAdd = SettingUtils.create(addDirective.getSettingClass());

	setHeader(addDirective.getDescription().orElse("Add setting"));
	setConfirmText("ADD");

	//
	// hides the header and opens expands the setting
	//

	this.foldedModeEnabled = this.settingToAdd.isFoldedModeEnabled();

	this.settingToAdd.enableFoldedMode(false);

	// SettingHelper.expand(settingToAdd);

	Component settingToAddComponent = createSettingToAddOrEditComponent(//
		configuration,//
		this.settingToAdd,//
		dialogHeight);

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
		tabContent, //
		foldedModeEnabled);
    }
}
