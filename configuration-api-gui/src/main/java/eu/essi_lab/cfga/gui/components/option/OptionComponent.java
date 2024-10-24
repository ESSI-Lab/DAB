package eu.essi_lab.cfga.gui.components.option;

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

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.Selectable.SelectionMode;
import eu.essi_lab.cfga.gui.components.OptionComponentFactory;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;

/**
 * <info>
 * <style>
 * #component {
 * font-family: Arial, Helvetica, sans-serif;
 * border-collapse: collapse;
 * width: 100%;
 * }
 * #component td, #component th {
 * border: 1px solid #ddd;
 * padding: 8px;
 * }
 * #component tr:nth-child(even){background-color: #f2f2f2;}
 * #component tr:hover {background-color: #ddd;}
 * #component th {
 * padding-top: 12px;
 * padding-bottom: 12px;
 * text-align: left;
 * background-color: #4CAF50;
 * color: white;
 * }
 * </style>
 * <table id='component'>
 * <tr>
 * <td>Property</td>
 * <td>To render</td>
 * <td>Rendering</td>
 * <td>Comment</td>
 * </tr>
 * <tr>
 * <td>enabled</td>
 * <td>yes</td>
 * <td>check box/switch button</td>
 * <td>def: true</td>
 * </tr>
 * <tr>
 * <td>canBeDisabled</td>
 * <td>no</td>
 * <td>if true, draws the above switch</td>
 * <td>def: true</td>
 * </tr>
 * <tr>
 * <td>visible</td>
 * <td>no</td>
 * <td>if false, hides the component</td>
 * <td>def: true</td>
 * </tr>
 * <tr>
 * <td>selectionMode</td>
 * <td>yes</td>
 * <td>if set, the values are rendered with a check box/switch button or with a radio button</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>description</td>
 * <td>yes</td>
 * <td>if present, shows the description in a not editable text field</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>advanced</td>
 * <td>yes</td>
 * <td>if true, the client put it in a "advanced" options accordion</td>
 * <td>def: false</td>
 * </tr>
 * <tr>
 * <td>editable</td>
 * <td>yes</td>
 * <td>if false, the client renders the text field/area not editable</td>
 * <td>def: true</td>
 * </tr>
 * <tr>
 * <td>mandatory</td>
 * <td>yes</td>
 * <td>if true, the client checks that a value is set</td>
 * <td>def: false</td>
 * </tr>
 * </table>
 * </info>
 */
@SuppressWarnings("serial")
public class OptionComponent extends VerticalLayout {

    private HasEnabled renderedOption;
    private Option<?> option;
    private ToggleButton toggle;

    /**
     * @param configuration
     * @param owner
     * @param option
     * @param forceReadonly
     */
    public OptionComponent(Configuration configuration, Setting owner, Option<?> option, boolean forceReadonly) {

	this.option = option;

	setId("option-component-" + option.getKey());
	setMargin(false);
	setSpacing(false);
	getStyle().set("padding", "0px");

	boolean visible = option.isVisible();

	if (!visible) {

	    return;
	}

	//
	// layouts
	//
	HorizontalLayout mainLayout = OptionComponentFactory.createOptionComponentMainLayout(option);

	VerticalLayout optionLayout = OptionComponentFactory.createOptionComponentLayout(option);

	mainLayout.add(optionLayout);

	add(mainLayout);

	//
	// enabled/disabled
	//
	boolean canBeDisabled = option.canBeDisabled();
	boolean value = option.isEnabled();
	boolean enabled = !forceReadonly;

	if (canBeDisabled) {

	    toggle = OptionComponentFactory.createOptionToggleButton(this, value, enabled);

	    mainLayout.add(toggle);
	}

	//
	// label
	//
	Label label = OptionComponentFactory.createOptionLabel(option.getLabel());
	optionLayout.add(label);

	//
	// description
	//

	Optional<String> description = option.getDescription();

	if (description.isPresent()) {

	    Label descriptionLabel = OptionComponentFactory.createOptionDescriptionLabel(description.get());

	    optionLayout.add(descriptionLabel);
	}

	//
	// values
	//
	SelectionMode selectionMode = option.getSelectionMode();

	switch (selectionMode) {
	case UNSET:

	    Component valueField = OptionComponentFactory.createOptionUnsetValueComponent(option, forceReadonly);

	    renderedOption = (HasEnabled) valueField;

	    optionLayout.add(valueField);

	    break;
	case MULTI:

	    if (Setting.class.isAssignableFrom(option.getValueClass())) {

		//
		// implementation of options with Setting as value is partial
		//

		// OptionSettingComponent component = ComponentFactory.createSettingOptionComponent(configuration,
		// owner, option);
		// renderedOption = component;
		// optionLayout.add(component);

	    } else {

		Component multiSelect = OptionComponentFactory.createOptionMultiSelectionComponent(option, forceReadonly);

		renderedOption = (HasEnabled) multiSelect;

		optionLayout.add(multiSelect);
	    }

	    break;
	case SINGLE:

	    if (Setting.class.isAssignableFrom(option.getValueClass())) {

		//
		// implementation of options with Setting as value is partial
		//

		// OptionSettingComponent component = ComponentFactory.createSettingOptionComponent(configuration,
		// owner, option);
		// renderedOption = component;
		// optionLayout.add(component);

	    } else {

		Component select = OptionComponentFactory.createOptionSingleSelectionComponent(option, forceReadonly);

		renderedOption = (HasEnabled) select;

		optionLayout.add(select);
	    }

	    break;
	}
    }

    /**
     * @param value
     * @param forceReadonly
     */
    public void onSettingToggleStateChanged(boolean value, boolean forceReadonly) {

	if (!value) {

	    onToggleStateChanged(false);

	    //
	    // if the option can be disabled, it has its own toggle button
	    // and its state can be set to enabled only with the toggle button
	    //
	} else if (value && !option.canBeDisabled()) {

	    onToggleStateChanged(true);
	}

	//
	// if the owner setting is disabled, the toggle button of this option
	// component must be disabled, it cannot be used
	// the toggle value must be set to false only if also the owner setting state is false
	//
	if (!value) {

	    if (toggle != null) {

		toggle.setEnabled(false);
		toggle.setValue(false);
	    }

	} else if (toggle != null) {

	    //
	    // if the value of the owner setting switch to true,
	    // this toggle can be used again, but only if the force read-only mode
	    // is not set
	    //
	    if (!forceReadonly) {

		toggle.setEnabled(true);
	    }
	}

    }

    /**
     * @param enabled
     */
    public void onToggleStateChanged(boolean enabled) {

	// GSLoggerFactory.getLogger(getClass()).debug("Updating state of option: " + option.getLabel());
	// GSLoggerFactory.getLogger(getClass()).debug("New state: " + (enabled ? "enabled" : "disabled"));

	renderedOption.setEnabled(enabled);
	option.setEnabled(enabled);
    }
}
