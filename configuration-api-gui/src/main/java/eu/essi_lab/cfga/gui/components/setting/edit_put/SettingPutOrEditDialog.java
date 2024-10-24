package eu.essi_lab.cfga.gui.components.setting.edit_put;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Unit;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gui.components.SettingComponentFactory;
import eu.essi_lab.cfga.gui.components.TabContainer;
import eu.essi_lab.cfga.gui.components.listener.ButtonChangeListener;
import eu.essi_lab.cfga.gui.dialog.ConfirmationDialog;
import eu.essi_lab.cfga.gui.extension.directive.Directive;
import eu.essi_lab.cfga.gui.extension.directive.Directive.ConfirmationPolicy;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.ValidationResponse.ValidationResult;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public abstract class SettingPutOrEditDialog extends ConfirmationDialog {

    protected int dialogHeight;
    protected int dialogWidth;
    protected Configuration configuration;
    protected TabContainer tabContainer;
    protected boolean foldedModeEnabled;

    /**
     * @param configuration
     * @param tabContainer
     * @param context
     */
    public SettingPutOrEditDialog(Configuration configuration, TabContainer tabContainer, ValidationContext context) {
	
	addToCloseAll();

	this.configuration = configuration;
	this.tabContainer = tabContainer;
	setCloseOnConfirm(false);

	dialogHeight = 800;
	dialogWidth = 800;

	setWidth(dialogWidth, Unit.PIXELS);

	//
	// Content
	//

	getContentLayout().getStyle().set("border", "1px solid lightgray");
	getContentLayout().getStyle().set("padding", "10px");
	getContentLayout().getStyle().set("border-radius", "3px");

	//
	// OnConfirm listener
	//
	setOnConfirmListener(e -> {

	    Optional<ValidationResponse> optional = getSetting().validate(configuration, context);

	    ValidationResponse validationResponse = null;

	    if (optional.isPresent()) {

		validationResponse = optional.get();
	    }

	    ButtonChangeListener listener = getConfirmationListener();

	    SettingConfirmationDialog dialog = new SettingConfirmationDialog(validationResponse, listener);

	    //
	    // in case of validation errors, the dialog is always opened in order to show the errors
	    //
	    if (validationResponse != null && validationResponse.getResult() == ValidationResult.VALIDATION_FAILED) {

		dialog.open();
		return;
	    }

	    Optional<? extends Directive> directive = this.getDirective();

	    if (directive.isPresent()) {

		ConfirmationPolicy policy = directive.get().getConfirmationPolicy();
		switch (policy) {
		case ALWAYS:
		    dialog.open();
		    break;
		case NEVER:
		    listener.handleEvent(null);
		    break;
		case ON_WARNINGS:

		    if (validationResponse == null || validationResponse.getWarnings().isEmpty()) {

			listener.handleEvent(null);

		    } else if (!validationResponse.getWarnings().isEmpty()) {

			dialog.open();
		    }

		    break;
		}
	    } else {

		//
		// if no directive is present, default behavior is no confirmation
		//
		listener.handleEvent(null);
	    }
	});
    }

    /**
     * @param configuration
     * @param setting
     * @param height
     * @return
     */
    protected Component createSettingToAddOrEditComponent(Configuration configuration, Setting setting, int height) {

	// Comparator<Setting> comparator = (s1, s2) -> s1.getName().compareTo(s2.getName());

	int componentHeight = height - 150;

	Component component = SettingComponentFactory.createSettingComponent(configuration, setting, false);

	((HasSize) component).setWidthFull();
	((HasSize) component).setHeight(componentHeight, Unit.PIXELS);
	((HasSize) component).setMaxHeight(componentHeight, Unit.PIXELS);

	component.getElement().getStyle().set("overflow-y", "auto");
	component.getElement().getStyle().set("padding-right", "10px");
	component.getElement().getStyle().set("padding-left", "5px");

	return component;
    }

    /**
     * @return
     */
    protected abstract Setting getSetting();

    /**
     * @return
     */
    protected abstract Optional<? extends Directive> getDirective();

    /**
     * @return
     */
    protected abstract ButtonChangeListener getConfirmationListener();

}
