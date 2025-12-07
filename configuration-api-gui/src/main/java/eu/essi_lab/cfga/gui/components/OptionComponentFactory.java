package eu.essi_lab.cfga.gui.components;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gui.components.option.OptionComponent;
import eu.essi_lab.cfga.gui.components.option.OptionDoubleField;
import eu.essi_lab.cfga.gui.components.option.OptionIntegerField;
import eu.essi_lab.cfga.gui.components.option.OptionTextArea;
import eu.essi_lab.cfga.gui.components.option.OptionTextField;
import eu.essi_lab.cfga.gui.components.option.listener.OptionToggleButtonListener;
import eu.essi_lab.cfga.gui.components.option.listener.OptionValueChangeListener;
import eu.essi_lab.cfga.gui.components.option.listener.OptionValuesLoaderListener;
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.ISODateTime;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class OptionComponentFactory {

    /**
     *
     */
    private static final int NUMERIC_FIELD_WIDTH = 200;

    /**
     *
     */
    private static final int BOOLEAN_FIELD_WIDTH = 100;

    /**
     * @param option
     * @return
     */
    public static HorizontalLayout createOptionComponentMainLayout(Option<?> option) {

	HorizontalLayout mainLayout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout("main-option-layout-" + option.getKey());
	mainLayout.getStyle().set("padding", "0px");
	mainLayout.setWidthFull();

	return mainLayout;
    }

    /**
     * @param option
     * @return
     */
    public static VerticalLayout createOptionComponentLayout(Option<?> option) {

	VerticalLayout optionLayout = ComponentFactory.createNoSpacingNoMarginVerticalLayout("option-layout-" + option.getKey());
	optionLayout.setAlignItems(Alignment.BASELINE);
	optionLayout.getStyle().set("padding", "0px");

	return optionLayout;
    }

    /**
     * @param description
     * @return
     */
    public static Label createOptionDescriptionLabel(String description) {

	Label label = ComponentFactory.createLabel(description);
	label.getStyle().set("font-size", "14px");
	label.getStyle().set("color", "gray");

	return label;
    }

    /**
     * @param text
     * @return
     */
    public static Label createOptionLabel(String text) {

	Label label = ComponentFactory.createLabel(text);
	label.getStyle().set("font-weight", "bold");
	label.getStyle().set("font-size", "13px");

	return label;
    }

    /**
     * @param value
     * @param enabled
     * @return
     */
    public static ToggleButton createOptionToggleButton(OptionComponent optionComponent, boolean value, boolean enabled) {

	ToggleButton toggle = ComponentFactory.createToggleButton(value, enabled);

	toggle.addValueChangeListener(new OptionToggleButtonListener(optionComponent));

	toggle.setHeightFull();

	return toggle;
    }

    /**
     * @param option
     * @param forceReadonly
     * @return
     */
    public static Component createOptionUnsetValueComponent(Option<?> option, boolean forceReadonly) {

	// GSLoggerFactory.getLogger(ComponentFactory.class).debug("Initialing component for option: " +
	// option.getKey());

	Optional<?> optionalValue = option.getOptionalValue();

	//
	// ISODateTime
	//
	if (option.isValueOf(ISODateTime.class)) {

	    // GSLoggerFactory.getLogger(ComponentFactory.class).debug("ISODateTime option value");

	    if (UI.getCurrent() == null) {
		return new Label();
	    }

	    if (!option.isEditable() || forceReadonly) {

		TextField textField = new TextField();
		textField.setId("option-text-field-for-"+option.getKey());
		textField.getStyle().set("font-size","14px");
		textField.setReadOnly(true);

		optionalValue.ifPresent(o -> textField.setValue(o.toString()));

		if (option.canBeDisabled()) {

		    textField.setEnabled(option.isEnabled());
		}

		textField.addValueChangeListener(new OptionValueChangeListener(option));

		return textField;
	    }

	    DateTimePicker picker = new DateTimePicker();
	    picker.setDatePlaceholder("Date");
	    picker.setTimePlaceholder("Time");
	    picker.setAutoOpen(true);

	    if (option.canBeDisabled()) {

		picker.setEnabled(option.isEnabled());
	    }

	    if (optionalValue.isPresent()) {

		// GSLoggerFactory.getLogger(ComponentFactory.class).debug("Value: " + value);

		LocalDateTime value = ISODateTime.asLocalDateTime(optionalValue.get().toString());
		picker.setValue(value);
	    }

	    picker.addValueChangeListener(new OptionValueChangeListener(option));

	    return picker;
	}

	//
	// Integer
	//
	if (option.isValueOf(Integer.class)) {

	    OptionIntegerField integerField = new OptionIntegerField(option, forceReadonly);

	    integerField.setWidth(NUMERIC_FIELD_WIDTH, Unit.PIXELS);

	    return integerField;
	}

	//
	// Double
	//
	if (option.isValueOf(Double.class)) {

	    OptionDoubleField doubleField = new OptionDoubleField(option, forceReadonly);

	    doubleField.setWidth(NUMERIC_FIELD_WIDTH, Unit.PIXELS);

	    return doubleField;
	}

	//
	// String
	//
	if (option.isTextAreaEnabled()) {

	    OptionTextArea textArea = new OptionTextArea(option, forceReadonly);
	    textArea.setWidthFull();

	    return textArea;
	}

	//
 	// Boolean choice
 	//
	if (option.isValueOf(BooleanChoice.class)) {

	    return createBooleanChoiceComponent(option, forceReadonly);
	}

	OptionTextField textField = new OptionTextField(option, forceReadonly);
	textField.setWidthFull();

	return textField;
    }

    /**
     * @param option
     * @param singleSelect
     * @param multiSelect
     * @param forceReadonly
     * @return
     */
    private static Component createOptionWithLoaderLayout(//
	    Option<?> option, //
	    Select<String> singleSelect, //
	    MultiSelectComboBox<String> multiSelect, //
	    boolean forceReadonly) {

	HorizontalLayout layout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout();
	layout.setWidthFull();

	if (singleSelect != null) {

	    layout.add(singleSelect);

	} else {

	    layout.add(multiSelect);
	}

	Button button = new Button(new Icon(VaadinIcon.REFRESH));
	button.addThemeVariants(ButtonVariant.LUMO_SMALL);
	button.getStyle().set("margin-left", "5px");
	button.getElement().setAttribute("title", "Click to load values");

	layout.add(button);

	OptionValuesLoaderListener listener;

	if (singleSelect != null) {

	    listener = new OptionValuesLoaderListener(option, singleSelect);

	} else {

	    listener = new OptionValuesLoaderListener(option, multiSelect);
	}

	button.addClickListener(listener);

	return layout;
    }

    /**
     * @param option
     * @param forceReadOnly
     * @return
     */
    public static Component createOptionMultiSelectionComponent(Option<?> option, boolean forceReadOnly) {

	MultiSelectComboBox<String> select = new MultiSelectComboBox<>();
	select.getStyle().set("font-size","14px");

	if (!option.getValueClass().equals(Integer.class) && !option.getValueClass().equals(Double.class)) {

	    select.setWidthFull();

	} else {

	    select.setWidth(NUMERIC_FIELD_WIDTH, Unit.PIXELS);
	}

	select.setRequired(option.isRequired());
	select.setRequiredIndicatorVisible(option.isRequired());
	select.setErrorMessage("A value is required");

	// GSLoggerFactory.getLogger(ComponentFactory.class).debug("Initialing multi select for option: " +
	// option.getKey());

	List<String> values = StringValuesReader.readValues(option);

	// GSLoggerFactory.getLogger(ComponentFactory.class).debug("Values: " + values);

	select.setItems(values);

	List<String> selectedValues = StringValuesReader.readSelectedValues(option);

	// GSLoggerFactory.getLogger(ComponentFactory.class).debug("Selected values: " + selectedValues);

	if (!selectedValues.isEmpty()) {

	    select.select(selectedValues);
	}

	if (option.canBeDisabled()) {

	    select.setEnabled(option.isEnabled());
	}

	if (!option.isEditable() || forceReadOnly) {

	    select.setReadOnly(true);
	}

	select.addValueChangeListener(new OptionValueChangeListener(option));

	if (option.getLoader().isPresent()) {

	    return createOptionWithLoaderLayout(option, null, select, forceReadOnly);
	}

	return select;
    }

    /**
     * @param option
     * @param forceReadOnly
     * @return
     */
    public static Component createOptionSingleSelectionComponent(Option<?> option, boolean forceReadOnly) {

	// GSLoggerFactory.getLogger(ComponentFactory.class).debug("Initialing select for option: " + option.getKey());

	if (!option.isValueOf(BooleanChoice.class)) {

	    Select<String> select = new Select<>();
	    select.getStyle().set("font-size","14px");

	    if (!option.getValueClass().equals(Integer.class) && !option.getValueClass().equals(Double.class)) {

		select.setWidthFull();

	    } else {

		select.setWidth(NUMERIC_FIELD_WIDTH, Unit.PIXELS);
	    }

	    List<String> values = StringValuesReader.readValues(option);

	    // GSLoggerFactory.getLogger(ComponentFactory.class).debug("Values: " + values);

	    select.setItems(values);

	    Optional<String> selectedValue = StringValuesReader.readSelectedValue(option);

	    // GSLoggerFactory.getLogger(ComponentFactory.class).debug("Selected value: " + selectedValue);

	    selectedValue.ifPresent(select::setValue);

	    if (option.canBeDisabled()) {

		select.setEnabled(option.isEnabled());
	    }

	    if (!option.isEditable() || forceReadOnly) {

		select.setReadOnly(true);
	    }

	    select.addValueChangeListener(new OptionValueChangeListener(option));

	    if (option.getLoader().isPresent()) {

		return createOptionWithLoaderLayout(option, select, null, forceReadOnly);
	    }

	    return select;
	}

	return createBooleanChoiceComponent(option, forceReadOnly);
    }

    /**
     * @param option
     * @param forceReadOnly
     * @return
     */
    private static Component createBooleanChoiceComponent(Option<?> option, boolean forceReadOnly) {

	VerticalLayout verticalLayout = ComponentFactory.createNoSpacingNoMarginVerticalLayout();

	verticalLayout.getStyle().set("margin-left", "-15px");
	verticalLayout.getStyle().set("padding-top", "0px");
	verticalLayout.getStyle().set("padding-bottom", "0px");

	HorizontalLayout horizontalLayout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout();

	Checkbox checkbox = new Checkbox();

	checkbox.addClassName("booleanChoice");

	checkbox.getStyle().set("margin-left", "-5px");

	Optional<String> selectedValue = StringValuesReader.readSelectedValue(option);

	selectedValue.ifPresent(v -> checkbox.setValue(v.equals("Yes")));

	if (option.canBeDisabled()) {

	    checkbox.setEnabled(option.isEnabled());
	}

	if (!option.isEditable() || forceReadOnly) {

	    checkbox.setReadOnly(true);
	    checkbox.setEnabled(false);
	}

	checkbox.addValueChangeListener(new OptionValueChangeListener(option));

	Label label = OptionComponentFactory.createOptionLabel(option.getLabel());

	label.getStyle().set("margin-top", "3px");
	label.getStyle().set("margin-left", "3px");

	Optional<String> description = option.getDescription();

	horizontalLayout.add(checkbox);

	horizontalLayout.add(label);

	if (description.isPresent()) {

	    Label descriptionLabel = OptionComponentFactory.createOptionDescriptionLabel(description.get());

	    verticalLayout.add(horizontalLayout);

	    verticalLayout.add(descriptionLabel);

	    return verticalLayout;
	}

	return horizontalLayout;
    }

    /**
     * @param content
     * @return
     */
    public static Details createAdvancedOptionsDetails(Component content) {

	Details details = ComponentFactory.createDetails(content, "View advanced options", "Hide advanced options");
	details.addThemeVariants(DetailsVariant.SMALL);

	return details;
    }

}
