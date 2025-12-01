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

import java.util.Comparator;
import java.util.List;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.Details.OpenedChangeEvent;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.Selectable.SelectionMode;
import eu.essi_lab.cfga.gui.components.listener.ButtonChangeListener;
import eu.essi_lab.cfga.gui.components.option.OptionComponent;
import eu.essi_lab.cfga.gui.components.option.OptionComponentLayout;
import eu.essi_lab.cfga.gui.components.setting.SettingComponent;
import eu.essi_lab.cfga.gui.components.setting.edit_put.SettingEditDialog;
import eu.essi_lab.cfga.gui.components.setting.edit_put.SettingPutDialog;
import eu.essi_lab.cfga.gui.components.setting.group.CheckComponentsHandler;
import eu.essi_lab.cfga.gui.components.setting.group.RadioComponentsHandler;
import eu.essi_lab.cfga.gui.components.setting.listener.SettingRemoveButtonListener;
import eu.essi_lab.cfga.gui.components.setting.listener.SettingToggleButtonListener;
import eu.essi_lab.cfga.gui.components.tabs.*;
import eu.essi_lab.cfga.gui.dialog.ConfirmationDialog;
import eu.essi_lab.cfga.gui.directive.AddDirective;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class SettingComponentFactory {

    /**
     * @param settingComponent
     * @param value
     * @param enabled
     * @return
     */
    public static ToggleButton createSettingDisableEnableButton(SettingComponent settingComponent, boolean value, boolean enabled) {

	ToggleButton toggle = ComponentFactory.createToggleButton(value, enabled);
	// toggle.getStyle().set("margin-top", "-5px");
	toggle.setHeightFull();

	toggle.addValueChangeListener(new SettingToggleButtonListener(settingComponent));

	return toggle;
    }

    /**
     * @param parent
     * @param setting
     * @param id
     * @param paddingLeft
     * @return
     */
    public static VerticalLayout createSettingMainLayout(Setting parent, Setting setting, String id, int paddingLeft) {

	VerticalLayout mainLayout = ComponentFactory.createNoSpacingNoMarginVerticalLayout(id);

	mainLayout.getStyle().set("padding", "0px");
	mainLayout.getStyle().set("padding-left", paddingLeft + "px");

	mainLayout.setWidthFull();

	return mainLayout;
    }

    /**
     * @param setting
     * @return
     */
    public static HorizontalLayout createSettingHeaderLayout(Setting setting) {

	HorizontalLayout headerLayout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout("header-layout_" + setting.getName());
	headerLayout.setAlignItems(Alignment.BASELINE);
	headerLayout.setWidthFull();
	// headerLayout.getStyle().set("margin-bottom", "3px");

	return headerLayout;
    }

    /**
     * @param setting
     * @return
     */
    public static HorizontalLayout createSettingHeaderLayoutWithBottomMargin(Setting setting) {

	HorizontalLayout headerLayout = createSettingHeaderLayout(setting);
	headerLayout.getStyle().set("margin-bottom", "3px");

	return headerLayout;
    }

    /**
     * @param configuration
     * @param setting
     * @return
     */
    public static OptionComponentLayout createSettingOptionsComponent(Configuration configuration, Setting setting, boolean forceReadonly) {

	OptionComponentLayout mainLayout = new OptionComponentLayout("main-options-layout-for-setting-" + setting.getName());
	mainLayout.setSizeFull();
	mainLayout.getStyle().set("padding", "0px");

	List<Option<?>> advancedOptions = setting.//
		getOptions().//
		stream().//
		filter(Option::isAdvanced).//
		toList();

	VerticalLayout optionsLayout = ComponentFactory.createNoSpacingNoMarginVerticalLayout(
		"options-layout-for-setting-" + setting.getName());
	optionsLayout.setSizeFull();
	optionsLayout.getStyle().set("padding", "0px");

	setting.//
		getOptions().//
		stream().//
		filter(o -> !o.isAdvanced()).//
		forEach(o -> {

	    OptionComponent optionComponent = new OptionComponent(configuration, setting, o, forceReadonly);

	    optionsLayout.add(optionComponent);
	    mainLayout.getOptionComponents().add(optionComponent);
	});

	if (setting.isCompactModeEnabled()) {

	    Details details = createSettingCompactModeComponent(optionsLayout);

	    if (advancedOptions.isEmpty()) {

		mainLayout.setCompactModeDetails(details);
		// return details;

	    } else {

		mainLayout.add(details);
	    }
	} else {

	    mainLayout.add(optionsLayout);
	}

	if (!advancedOptions.isEmpty()) {

	    VerticalLayout advLayout = ComponentFactory.createNoSpacingNoMarginVerticalLayout(
		    "advanced-options-layout-for-setting-" + setting.getName());
	    advLayout.setSizeFull();
	    advLayout.getStyle().set("padding", "0px");

	    advancedOptions.forEach(o -> {

		OptionComponent optionComponent = new OptionComponent(configuration, setting, o, forceReadonly);

		advLayout.add(optionComponent);
		mainLayout.getOptionComponents().add(optionComponent);
	    });

	    Details accordion = OptionComponentFactory.createAdvancedOptionsDetails(advLayout);
	    mainLayout.add(accordion);
	}

	return mainLayout;
    }

    /**
     * @param configuration
     * @param setting
     * @param forceReadonly
     * @return
     */
    public static SettingComponent createSettingComponent(//
	    Configuration configuration, //
	    String settingIdentifier, //
	    boolean forceReadonly) {

	return createSettingComponentWithOptionalFoldedMode(configuration, settingIdentifier, forceReadonly, false, null, null);
    }

    /**
     * @param configuration
     * @param setting
     * @param forceReadonly
     * @return
     */
    public static SettingComponent createSettingComponent(//
	    Configuration configuration, //
	    Setting setting, //
	    boolean forceReadonly) {

	return createSettingComponentWithOptionalFoldedMode(configuration, setting, forceReadonly, false, null, null);
    }

    /**
     * @param configuration
     * @param setting
     * @param forceReadonly
     * @param tabContent
     * @return
     */
    public static SettingComponent createSettingComponent(//
	    Configuration configuration, //
	    String settingIdentifier, //
	    boolean forceReadonly, //
	    TabContent tabContent) {

	return createSettingComponentWithOptionalFoldedMode(configuration, settingIdentifier, forceReadonly, false, null, tabContent);
    }

    /**
     * @param configuration
     * @param setting
     * @param forceReadonly
     * @param tabContent
     * @return
     */
    public static SettingComponent createSettingComponent(//
	    Configuration configuration, //
	    Setting setting, //
	    boolean forceReadonly, //
	    TabContent tabContent) {

	return createSettingComponentWithOptionalFoldedMode(configuration, setting, forceReadonly, false, null, tabContent);
    }

    /**
     * @param configuration
     * @param setting
     * @param forceReadonly
     * @param tabContent
     * @return
     */
    public static SettingComponent createSettingComponentForcingHideLabel(//
	    Configuration configuration, //
	    String settingIdentifier, //
	    boolean forceReadonly, //
	    TabContent tabContent) {

	return createSettingComponentWithOptionalFoldedMode(configuration, settingIdentifier, forceReadonly, true, null, tabContent);
    }

    /**
     * @param configuration
     * @param setting
     * @param forceReadonly
     * @param comparator
     * @param tabContent
     * @return
     */
    private static SettingComponent createSettingComponent(//
	    Configuration configuration, //
	    String settingIdentifier, //
	    boolean forceReadonly, //
	    boolean forceHideLabel, //

	    Comparator<Setting> comparator, //
	    TabContent tabContent) {

	SettingComponent settingComponent = new SettingComponent(configuration, settingIdentifier, forceReadonly, forceHideLabel,
		comparator, tabContent);

	settingComponent.getStyle().set("background-color", "white");
	settingComponent.getStyle().set("padding", "4px");
	settingComponent.getStyle().set("border-radius", "5px");
	settingComponent.getStyle().set("margin-top", "0px");

	return settingComponent;
    }

    /**
     * @param configuration
     * @param setting
     * @param forceReadonly
     * @param comparator
     * @param tabContent
     * @return
     */
    private static SettingComponent createSettingComponent(//
	    Configuration configuration, //
	    Setting setting, //
	    boolean forceReadonly, //
	    boolean forceHideLabel, //

	    Comparator<Setting> comparator, //
	    TabContent tabContent) {

	SettingComponent settingComponent = new SettingComponent(configuration, setting, forceReadonly, forceHideLabel, comparator,
		tabContent);

	settingComponent.getStyle().set("background-color", "white");
	settingComponent.getStyle().set("padding", "4px");
	settingComponent.getStyle().set("border-radius", "px");
	settingComponent.getStyle().set("margin-top", "0px");

	return settingComponent;
    }

    /**
     * @param configuration
     * @param setting
     * @param forceReadonly
     * @param forceHideLabel
     * @param comparator
     * @param tabContent
     * @return
     */
    private static SettingComponent createSettingComponentWithOptionalFoldedMode(//
	    Configuration configuration, //
	    String settingIdentifier, //
	    boolean forceReadonly, //
	    boolean forceHideLabel, //
	    Comparator<Setting> comparator, //
	    TabContent tabContent) {

	SettingComponent settingComponent = createSettingComponent(configuration, settingIdentifier, forceReadonly, forceHideLabel,
		comparator, tabContent);

	Setting setting = configuration.get(settingIdentifier).get();

	if (setting.isFoldedModeEnabled()) {

	    Details details = ComponentFactory.createDetails(setting.getName(), settingComponent);

	    settingComponent.setDetails(details);
	}

	return settingComponent;
    }

    /**
     * @param configuration
     * @param setting
     * @param forceReadonly
     * @param forceHideLabel
     * @param comparator
     * @param tabContent
     * @return
     */
    private static SettingComponent createSettingComponentWithOptionalFoldedMode(//
	    Configuration configuration, //
	    Setting setting, //
	    boolean forceReadonly, //
	    boolean forceHideLabel, //
	    Comparator<Setting> comparator, //
	    TabContent tabContent) {

	SettingComponent settingComponent = createSettingComponent(configuration, setting, forceReadonly, forceHideLabel, comparator,
		tabContent);

	if (setting.isFoldedModeEnabled()) {

	    Details details = ComponentFactory.createDetails(setting.getName(), settingComponent);

	    settingComponent.setDetails(details);
	}

	return settingComponent;
    }

    /**
     * @param content
     * @return
     */
    @SuppressWarnings("serial")
    public static Details createSettingCompactModeComponent(Component content) {

	Details details = ComponentFactory.createDetails("View options", content);
	details.addThemeVariants(DetailsVariant.SMALL);

	details.addOpenedChangeListener((ComponentEventListener<OpenedChangeEvent>) event -> {

	    boolean opened = event.isOpened();
	    if (opened) {
		details.setSummaryText("Hide options");
	    } else {
		details.setSummaryText("View options");
	    }
	});

	return details;
    }

    /**
     * @param content
     * @return
     */
    public static Details createSettingFoldedModeComponent(Setting setting, SettingComponent component) {

	Details details = ComponentFactory.createDetails(setting.getName(), component);

	component.setDetails(details);

	return details;
    }

    /**
     * @param description
     * @return
     */
    public static TextArea createSettingDescriptionArea(String description) {

	TextArea textArea = new TextArea();

	textArea.setReadOnly(true);
	textArea.setValue(description);
	textArea.setWidthFull();
	textArea.getStyle().set("font-size", "15px");
	textArea.getStyle().set("border-radius", "0px");
	textArea.getStyle().set("background-color", "rgb(158 158 158 / 26%)");

	textArea.addClassName("text-area-no-border");

	//	textArea.getStyle().set("color", "gray");
	//	textArea.getStyle().set("margin-bottom", "5px");

	return textArea;
    }

    /**
     * @param settingName
     * @param parent
     * @return
     */
    public static Label createSettingNameLabel(Setting setting, Setting parent) {

	String name = parent != null ? "[ " + setting.getName() + " ]" : setting.getName();

	Label label = ComponentFactory.createLabel(name);
	label.getStyle().set("font-weight", "bold");
	label.getStyle().set("margin-top", "10px");

	if (!setting.isCompactModeEnabled() || //
		(setting.isCompactModeEnabled() && setting.getOptions().isEmpty()) || //
		(setting.getSelectionMode() != SelectionMode.UNSET)) {

	    label.getStyle().set("border-bottom", "1px solid lightgray");
	}

	if (parent == null) {

	    label.getStyle().set("font-size", "15px");
	    label.getStyle().set("background-color", "#3c8df5");
	    label.getStyle().set("padding", "5px");
	    label.getStyle().set("padding-bottom", "3px");
	    label.getStyle().set("padding-top", "1px");
	    label.getStyle().set("border-radius", "0px");
	    label.getStyle().set("color", "white");
	    label.getStyle().set("opacity", "0.9");

	} else {

	    label.getStyle().set("padding-bottom", "5px");
	}

	return label;
    }

    /**
     * @return
     */
    public static Button createSettingResetButton() {

	Button button = new Button("Reset");
	button.addThemeVariants(ButtonVariant.LUMO_SMALL);
	// button.setWidth("100px");
	// button.setHeight("30px");

	// button.getStyle().set("margin-top", "-6px");
	button.getStyle().set("margin-left", "3px");

	return button;
    }

    /**
     * @param configuration
     * @param setting
     * @param tabContent
     * @return
     */
    public static Button createSettingEditButton(//
	    Configuration configuration, //
	    Setting setting, //
	    SettingComponent currentSettingComponent, //
	    TabContent tabContent) {

	ConfigurationViewButton button = new ConfigurationViewButton("EDIT", VaadinIcon.EDIT.create());
	button.addThemeVariants(ButtonVariant.LUMO_SMALL);
	button.setWidth(100, Unit.PIXELS);
	button.getStyle().set("margin-left", "3px");

	//
	//
	//

	button.addEnabledStyle("color", "black");
	button.addEnabledStyle("background-color", "rgb(255 252 0 / 41%)");
	button.addEnabledStyle("border", "1px solid lightgray");

	//
	//
	//

	button.addClickListener(e -> new SettingEditDialog(configuration, setting, currentSettingComponent, tabContent).open());

	EnabledGroupManager.getInstance().add(button);

	return button;
    }

    /**
     * @param configuration
     * @param tabContent
     * @param addDirective
     * @return
     */
    public static Button createSettingAddButton(//
	    Configuration configuration, //
	    TabContent tabContent, //
	    AddDirective addDirective) {

	ConfigurationViewButton button = new ConfigurationViewButton("ADD", VaadinIcon.PLUS_SQUARE_O.create());
	button.setWidth(100, Unit.PIXELS);
	button.addThemeVariants(ButtonVariant.LUMO_SMALL);
	button.getStyle().set("margin-left", "3px");

	//
	//
	//
	button.addEnabledStyle("color", "white");
	button.addEnabledStyle("background-color", "#008ab7");
	button.addEnabledStyle("border", "none");

	//
	//
	//

	button.addClickListener(e -> new SettingPutDialog(configuration, tabContent, addDirective).open());

	EnabledGroupManager.getInstance().add(button);

	return button;
    }

    /**
     * @param configuration
     * @param tabContent
     * @param settingComponent
     * @return
     */
    public static Button createSettingRemoveButton(//
	    Configuration configuration,//
	    TabContent tabContent,//
	    SettingComponent settingComponent) {

	ConfigurationViewButton button = new ConfigurationViewButton("REMOVE", VaadinIcon.MINUS_SQUARE_O.create());
	button.addThemeVariants(ButtonVariant.LUMO_SMALL);
	button.setWidth(150, Unit.PIXELS);
	button.getStyle().set("margin-left", "3px");

	//
	//
	//

	button.addEnabledStyle("color", "red");
	button.addEnabledStyle("border", "none");
	button.addEnabledStyle("background-color", "rgb(240 240 240)");
	button.addEnabledStyle("border", "1px solid lightgray");

	//
	//
	//

	button.addClickListener(new SettingRemoveButtonListener(configuration, tabContent, settingComponent));

	EnabledGroupManager.getInstance().add(button);

	return button;
    }

    /**
     * @param onConfirmListener
     * @return
     */
    public static ConfirmationDialog createSettingRemoveDialog(ButtonChangeListener onConfirmListener) {

	ConfirmationDialog dialog = new ConfirmationDialog("Are you sure you want to remove this setting?", onConfirmListener);

	dialog.addToCloseAll();

	return dialog;
    }

    /**
     * @param setting
     * @param forceReadOnly
     * @return
     */
    public static RadioComponentsHandler createRadioComponentsHandler(Setting setting, boolean forceReadOnly) {

	RadioComponentsHandler handler = new RadioComponentsHandler();

	handler.getGroupComponent().setEnabled(!forceReadOnly);

	handler.setGroupSetting(setting);

	return handler;
    }

    /**
     * @param setting
     * @param forceReadOnly
     * @return
     */
    public static CheckComponentsHandler createCheckComponentsHandler(Setting setting, boolean forceReadOnly) {

	CheckComponentsHandler handler = new CheckComponentsHandler();

	handler.getGroupComponent().setEnabled(!forceReadOnly);

	handler.setGroupSetting(setting);

	return handler;
    }

}
