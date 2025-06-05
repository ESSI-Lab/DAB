package eu.essi_lab.cfga.gui.components.setting;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.Selectable.SelectionMode;
import eu.essi_lab.cfga.gui.components.ComponentFactory;
import eu.essi_lab.cfga.gui.components.SettingComponentFactory;
import eu.essi_lab.cfga.gui.components.TabContainer;
import eu.essi_lab.cfga.gui.components.option.OptionComponent;
import eu.essi_lab.cfga.gui.components.option.OptionComponentLayout;
import eu.essi_lab.cfga.gui.components.setting.group.CheckComponentsHandler;
import eu.essi_lab.cfga.gui.components.setting.group.RadioComponentsHandler;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;
import eu.essi_lab.lib.utils.GSLoggerFactory;

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
 * <td>canBeReset</td>
 * <td>yes</td>
 * <td>if true, adds a button to reset the setting</td>
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
 * <td>if set, the children are rendered with a check box/switch button or with a radio button</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>selected</td>
 * <td>yes</td>
 * <td>selected according to the multi selection mode</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>foldable</td>
 * <td>yes</td>
 * <td>if true, only the setting name is shown and the content is put inside a accordion</td>
 * <td>def: false</td>
 * </tr>
 * <tr>
 * <td>name</td>
 * <td>yes</td>
 * <td>shows the setting name in a not editable text field</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>description</td>
 * <td>yes</td>
 * <td>if present, shows the description in a not editable text field</td>
 * <td></td>
 * </tr>
 * </table>
 * </info>
 */
@SuppressWarnings("serial")
public class SettingComponent extends Div {

    /**
     * 
     */
    private static final Integer LEVEL_LEFT_PADDING = 10;

    private HashMap<String, RadioComponentsHandler> radioMap;
    private HashMap<String, CheckComponentsHandler> checkMap;

    private HashMap<Setting, Integer> levelMap;
    private HashMap<Setting, List<Setting>> childToParentsMap;

    private HashMap<String, List<Component>> settingNameToComponentsMap;
    private HashMap<String, List<Component>> settingNameToToggleAndOptionsMap;

    private boolean forceReadonly;
    private Setting setting;
    private String settingIdentifier;

    private Configuration configuration;
    private Details details;
    private TabContainer tabContainer;

    private boolean forceHideLabel;

    /**
     * @param configuration
     * @param setting
     * @param forceReadonly
     * @param forceHideLabel
     * @param comparator
     * @param tabContainer
     */
    public SettingComponent(//
	    Configuration configuration, //
	    Setting setting, //
	    boolean forceReadonly, //
	    boolean forceHideLabel, //
	    Comparator<Setting> comparator, //
	    TabContainer tabContainer) {

	this.configuration = configuration;
	this.setting = setting;
	this.forceReadonly = forceReadonly;
	this.tabContainer = tabContainer;
	this.forceHideLabel = forceHideLabel;

	init(comparator);
    }

    /**
     * @param configuration
     * @param setting
     * @param forceReadonly
     * @param forceHideLabel
     * @param comparator
     * @param tabContainer
     */
    public SettingComponent(//
	    Configuration configuration, //
	    String settingIdentifier, //
	    boolean forceReadonly, //
	    boolean forceHideLabel, //
	    Comparator<Setting> comparator, //
	    TabContainer tabContainer) {

	this.configuration = configuration;
	this.settingIdentifier = settingIdentifier;
	this.forceReadonly = forceReadonly;
	this.tabContainer = tabContainer;
	this.forceHideLabel = forceHideLabel;

	init(comparator);
    }

    /**
     * @param comparator
     */
    private void init(Comparator<Setting> comparator) {

	setId("setting-component-" + getSetting().getName());

	setHeightFull();
	setWidthFull();

	radioMap = new HashMap<>();
	checkMap = new HashMap<>();
	levelMap = new HashMap<>();
	childToParentsMap = new HashMap<>();
	settingNameToComponentsMap = new HashMap<>();
	settingNameToToggleAndOptionsMap = new HashMap<>();

	if (comparator == null) {

	    //
	    // this default comparator put the scheduling first
	    //
	    comparator = (s1, s2) -> {

		int compareTo = s1.getName().compareTo(s2.getName());
		if (isScheduling(s1)) {
		    return -1;
		}
		if (isScheduling(s2)) {
		    return 1;
		}
		return compareTo;
	    };
	}

	deepRenderSetting(null, getSetting(), configuration, comparator);

	addRadioMultiSelectionComponents();
	addCheckMultiSelectionComponents();
    }

    /**
     * @param enabled
     */
    @SuppressWarnings("unchecked")
    public void onToggleStateChanged(ValueChangeEvent<?> event) {

	boolean enabled = (Boolean) event.getValue();

	//
	// updates the state of the setting with given setting id
	//
	ComponentValueChangeEvent<Component, ?> compEvent = (ComponentValueChangeEvent<Component, ?>) event;

	Component source = compEvent.getSource();

	String settingId = source.getElement().getAttribute("settingid");

	ArrayList<Setting> list = new ArrayList<>();

	SettingUtils.deepFind(getSetting(), s -> s.getIdentifier().equals(settingId), list);

	Setting settingToUpdate = list.get(0);

	GSLoggerFactory.getLogger(getClass()).debug("Updating state of setting: " + settingToUpdate.getName());
	GSLoggerFactory.getLogger(getClass()).debug("New state: " + (enabled ? "enabled" : "disabled"));

	settingToUpdate.setEnabled(enabled);

	//
	// updates the sub components
	//
	onToggleStateChanged(settingToUpdate, enabled);
    }

    /**
     * @return
     */
    public Setting getSetting() {

	if (this.setting != null) {

	    return setting;
	}

	return configuration.get(settingIdentifier).get();
    }

    /**
     * @param details
     */
    public void setDetails(Details details) {

	this.details = details;
    }

    /**
     * @return
     */
    public Optional<Details> getDetails() {

	return Optional.ofNullable(details);
    }

    /**
     * @param setting
     * @param enabled
     */
    private void onToggleStateChanged(Setting setting, boolean enabled) {

	List<Component> list = settingNameToToggleAndOptionsMap.get(setting.getName());

	for (Component comp : list) {

	    if (comp instanceof OptionComponent) {

		OptionComponent optionComp = (OptionComponent) comp;
		optionComp.onSettingToggleStateChanged(enabled, forceReadonly);

	    } else {

		//
		// This is the toggle button of a child setting
		//
		ToggleButton toggle = (ToggleButton) comp;

		//
		// if the parent setting value is false, also the state of the toggle button
		// of the children must be set to false
		//
		if (!enabled) {

		    toggle.setValue(false);
		}

		//
		// if the force readonly mode is enabled, the toggle button
		// cannot be used so it must be disabled. this is the only case
		// where a toggle button of a setting component can be disabled
		//
		if (forceReadonly) {

		    toggle.setEnabled(false);
		}
	    }
	}

    }

    /**
     * @param parent
     * @param setting
     * @param configuration
     * @param comparator
     */
    private void deepRenderSetting(//
	    Setting parent, //
	    Setting setting, //
	    Configuration configuration, //
	    Comparator<Setting> comparator) {

	renderSetting(parent, setting, configuration);

	setting.//
		getSettings().//
		stream().//
		sorted(comparator).//
		forEach(s -> deepRenderSetting(setting, s, configuration, comparator));
    }

    /**
     * @param parent
     * @param setting
     * @param configuration
     */
    private void renderSetting(//
	    Setting parent, //
	    Setting setting, //
	    Configuration configuration) {

	boolean visible = setting.isVisible();

	if (!visible) {

	    return;
	}

	setId("setting-comp-" + setting.getIdentifier());

	updateChildToParentsMap(parent, setting);

	//
	// layouts
	//

	int paddingLeft = computePaddingLeft(parent, setting);

	VerticalLayout mainLayout = SettingComponentFactory.createSettingMainLayout(//
		parent, //
		setting, "main-layout_" + setting.getName(), //
		paddingLeft);

	add(mainLayout);

	HorizontalLayout headerLayout = SettingComponentFactory.createSettingHeaderLayout(setting);
	if (!setting.isEditable()) {
	    // headerLayout = SettingComponentFactory.createSettingHeaderLayoutWithBottomMargin(setting);
	}

	// hides the header
	if (!setting.isShowHeaderSet()) {

	    headerLayout.getStyle().set("display", "none");
	}

	HorizontalLayout descriptionLayout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout();

	descriptionLayout.setWidthFull();
	descriptionLayout.setAlignItems(Alignment.BASELINE);

	mainLayout.add(headerLayout);
	mainLayout.add(descriptionLayout);

	//
	// get the multi selection of its parent (if exists)
	//
	SelectionMode selectionMode = parent != null ? parent.getSelectionMode() : SelectionMode.UNSET;

	switch (selectionMode) {
	case UNSET:
	    //
	    // name is added to the header if the setting is not folded
	    // if the setting is folded, the name is visible in the details component
	    //
	    if (!setting.isFoldedModeEnabled() && !forceHideLabel) {

		Label label = handleLabel(parent, setting, headerLayout);

		updateSettingToComponentsMap(setting, label);

	    } else {
		//
		// if the setting is folded, since there is no label, in order to place
		// the buttons on the right, an empty div is added instead of the label
		//
		if (setting.canBeDisabled() || setting.canBeRemoved() || setting.isEditable()) {

		    Div div = ComponentFactory.createDiv();
		    div.setWidthFull();

		    headerLayout.add(div);

		    updateSettingToComponentsMap(setting, div);
		}
	    }

	    break;
	case MULTI:

	    if (isScheduling(setting)) {

		handleLabel(parent, setting, headerLayout);

		break;
	    }

	    CheckComponentsHandler checkBoxGroupHandler = checkMap.get(parent.getName());

	    if (checkBoxGroupHandler == null) {

		mainLayout.getStyle().set("margin-bottom", "5px");

		checkBoxGroupHandler = SettingComponentFactory.createCheckComponentsHandler(parent, forceReadonly);

		checkMap.put(parent.getName(), checkBoxGroupHandler);

		Component toAdd = checkBoxGroupHandler.getGroupComponent();

		if (parent.isFoldedModeEnabled()) {

		    toAdd = ComponentFactory.createDetails(parent.getName(), checkBoxGroupHandler.getGroupComponent());
		}

		headerLayout.add(toAdd);

		updateSettingToComponentsMap(parent, toAdd);

		updateCheckGroup(parent, setting, toAdd, selectionMode);
	    }

	    break;
	case SINGLE:

	    if (isScheduling(setting)) {

		handleLabel(parent, setting, headerLayout);

		break;
	    }

	    RadioComponentsHandler radioButtonGroupHandler = radioMap.get(parent.getName());

	    if (radioButtonGroupHandler == null) {

		mainLayout.getStyle().set("margin-bottom", "5px");

		radioButtonGroupHandler = SettingComponentFactory.createRadioComponentsHandler(parent, forceReadonly);

		radioMap.put(parent.getName(), radioButtonGroupHandler);

		Component toAdd = radioButtonGroupHandler.getGroupComponent();

		if (parent.isFoldedModeEnabled()) {

		    toAdd = ComponentFactory.createDetails(parent.getName(), radioButtonGroupHandler.getGroupComponent());
		}

		headerLayout.add(toAdd);

		updateSettingToComponentsMap(parent, toAdd);

		//
		// here instead of the parent as first argument, this setting is used. for the
		// check the parent is used instead....to be better tested
		//
		updateRadioGroup(setting, setting, toAdd, selectionMode);
	    }

	    break;
	}

	//
	// description
	//
	handleDescription(parent, setting, descriptionLayout, selectionMode);

	//
	// reset button
	//
	handleEditButton(parent, setting, headerLayout, selectionMode);

	//
	// remove button
	//
	handleRemoveButton(parent, setting, tabContainer, headerLayout, selectionMode);

	//
	// disable button
	//
	Optional<ToggleButton> toggle = handleToggleButton(parent, setting, headerLayout, selectionMode);

	//
	// options
	//
	Optional<OptionComponentLayout> optionLayout = handleOptions(parent, setting, mainLayout, configuration, selectionMode);

	if (toggle.isPresent()) {

	    List<Component> list = settingNameToToggleAndOptionsMap.get(setting.getName());
	    if (list == null) {
		list = new ArrayList<>();
		settingNameToToggleAndOptionsMap.put(setting.getName(), list);
	    }

	    list.add(toggle.get());

	    if (optionLayout.isPresent()) {

		List<OptionComponent> optionComponents = optionLayout.get().getOptionComponents();

		for (OptionComponent optionComponent : optionComponents) {
		    list.add(optionComponent);
		}
	    }

	    onToggleStateChanged(setting, toggle.get().getValue());
	}
    }

    /**
     * @param parent
     * @param setting
     * @param headerLayout
     */
    private Label handleLabel(Setting parent, Setting setting, HorizontalLayout headerLayout) {

	Label label = SettingComponentFactory.createSettingNameLabel(setting, parent);

	headerLayout.add(label);

	return label;
    }

    /**
     * @param parent
     * @param setting
     * @param descriptionLayout
     * @param multiSelectionMode
     */
    private void handleDescription(Setting parent, Setting setting, HorizontalLayout descriptionLayout, SelectionMode multiSelectionMode) {

	Component component = null;

	Optional<String> description = setting.getDescription();

	if (!description.isPresent() && !hasVisibleOptions(setting) && multiSelectionMode != SelectionMode.UNSET) {

	    //
	    // if the setting is empty, no description nor options and the setting is an item of check
	    // or radio (reset and disable buttons are not shown for such items)
	    // in order to be rendered, an empty div is added replacing the description
	    //
	    component = ComponentFactory.createDiv();
	    component.setId("empty-setting-div-for-" + setting.getName());

	} else if (description.isPresent()) {

	    component = SettingComponentFactory.createSettingDescriptionArea(description.get());
	}

	if (component != null) {

	    descriptionLayout.add(component);

	    if (!isScheduling(setting)) {

		updateSettingToComponentsMap(setting, descriptionLayout);

		updateRadioGroup(parent, setting, null, multiSelectionMode);
		updateCheckGroup(parent, setting, null, multiSelectionMode);
	    }
	}
    }

    /**
     * @param parent
     * @param setting
     * @param headerLayout
     * @param multiSelectionMode
     */
    private void handleEditButton(Setting parent, Setting setting, HorizontalLayout headerLayout, SelectionMode multiSelectionMode) {

	boolean isEditable = setting.isEditable();

	if (isEditable) {

	    Button button = SettingComponentFactory.createSettingEditButton(configuration, setting, this, tabContainer);

	    updateSettingToComponentsMap(setting, button);

	    boolean radioUpdated = !isScheduling(setting) && updateRadioGroup(parent, setting, button, multiSelectionMode);
	    boolean checkUpdated = !isScheduling(setting) && updateCheckGroup(parent, setting, button, multiSelectionMode);

	    if (!radioUpdated && !checkUpdated) {

		headerLayout.add(button);
	    }
	}
    }

    /**
     * @param parent
     * @param setting
     * @param headerLayout
     * @param multiSelectionMode
     */
    private void handleRemoveButton(//
	    Setting parent,//
	    Setting setting,//
	    TabContainer tabContainer,//
	    HorizontalLayout headerLayout,//
	    SelectionMode multiSelectionMode) {

	boolean canBeRemoved = setting.canBeRemoved();

	if (canBeRemoved) {

	    Button button = SettingComponentFactory.createSettingRemoveButton(configuration, tabContainer, this);

	    updateSettingToComponentsMap(setting, button);

	    boolean radioUpdated = !isScheduling(setting) && updateRadioGroup(parent, setting, button, multiSelectionMode);
	    boolean checkUpdated = !isScheduling(setting) && updateCheckGroup(parent, setting, button, multiSelectionMode);

	    if (!radioUpdated && !checkUpdated) {

		headerLayout.add(button);
	    }
	}
    }

    /**
     * @param parent
     * @param setting
     * @param headerLayout
     * @param multiSelectionMode
     */
    private Optional<ToggleButton> handleToggleButton(//
	    Setting parent, //
	    Setting setting, //
	    HorizontalLayout headerLayout, //
	    SelectionMode multiSelectionMode) {

	boolean canBeDisabled = setting.canBeDisabled();

	if (canBeDisabled) {

	    boolean enabled = !forceReadonly;

	    ToggleButton toggle = SettingComponentFactory.createSettingDisableEnableButton(this, setting.isEnabled(), enabled);

	    //
	    //
	    // set as attribute the setting identifier. the listener for the button state changes is added
	    // to the SettingComponent, which will have several listeners attached, one for each toggle
	    // button of the children settings. When the button state changes, it is possible to retrieve the
	    // setting id property from the toggle button which is the source of the event, and searching for
	    // the child setting having that id in order to update the setting state according to the button value
	    //
	    //
	    toggle.getElement().setAttribute("settingid", setting.getIdentifier());

	    updateSettingToComponentsMap(setting, toggle);

	    boolean radioUpdated = !isScheduling(setting) && updateRadioGroup(parent, setting, toggle, multiSelectionMode);
	    boolean checkUpdated = !isScheduling(setting) && updateCheckGroup(parent, setting, toggle, multiSelectionMode);

	    if (!radioUpdated && !checkUpdated) {
		
		toggle.getStyle().set("margin-left", "5px");

		headerLayout.add(toggle);
	    }

	    return Optional.of(toggle);
	}

	return Optional.empty();
    }

    /**
     * @param parent
     * @param setting
     * @param mainLayout
     * @param configuration
     * @param multiSelectionMode
     */
    private Optional<OptionComponentLayout> handleOptions(//
	    Setting parent, //
	    Setting setting, //
	    VerticalLayout mainLayout, //
	    Configuration configuration, //
	    SelectionMode multiSelectionMode) {

	List<Option<?>> options = setting.getOptions();

	long visibileOptions = options.//
		stream().//
		filter(o -> o.isVisible()).//
		count();

	if (!options.isEmpty() && visibileOptions > 0) {

	    OptionComponentLayout optionLayout = SettingComponentFactory.createSettingOptionsComponent(configuration, setting,
		    forceReadonly);

	    Component component = optionLayout;

	    Optional<Details> details = optionLayout.getDetails();

	    if (details.isPresent()) {

		component = details.get();
	    }

	    mainLayout.add(component);

	    if (!isScheduling(setting)) {

		updateRadioGroup(parent, setting, null, multiSelectionMode);
		updateCheckGroup(parent, setting, null, multiSelectionMode);

		updateSettingToComponentsMap(setting, component);
	    }

	    return Optional.of(optionLayout);
	}

	return Optional.empty();
    }

    /**
     * @param setting
     * @return
     */
    private boolean hasVisibleOptions(Setting setting) {

	List<Option<?>> options = setting.getOptions();

	long visibileOptions = options.stream().filter(o -> o.isVisible()).count();

	return !options.isEmpty() && visibileOptions > 0;
    }

    /**
     * 
     */
    private void addRadioMultiSelectionComponents() {

	Collection<RadioComponentsHandler> values = radioMap.values();
	// Accessors Setting -> RadioButtonGroupHandler 1
	// Augmenters Setting -> RadioButtonGroupHandler 2

	for (RadioComponentsHandler group : values) {

	    List<String> items = group.getItems();

	    //
	    // -- Accessors Setting
	    //
	    // OAI PMH Accessor
	    // GBIF Accessor
	    // FDEDEO Accessor

	    //
	    // -- Augmenters Setting
	    //
	    // Augmenter 1
	    // Augmenter 2
	    // Augmenter 3

	    for (String item : items) {

		List<Component> parentComponents = settingNameToComponentsMap.get(item);
		group.addComponents(item, parentComponents);

		List<String> children = getChildrenOfParent(item);

		for (String child : children) {

		    List<Component> list = settingNameToComponentsMap.get(child);
		    group.addComponents(item, list);
		}
	    }

	    group.applySelection();
	}
    }

    /**
     * 
     */
    private void addCheckMultiSelectionComponents() {

	Collection<CheckComponentsHandler> values = checkMap.values();
	// Accessors Setting -> RadioButtonGroupHandler 1
	// Augmenters Setting -> RadioButtonGroupHandler 2

	for (CheckComponentsHandler group : values) {

	    List<String> items = group.getItems();

	    //
	    // -- Accessors Setting
	    //
	    // OAI PMH Accessor
	    // GBIF Accessor
	    // FDEDEO Accessor

	    //
	    // -- Augmenters Setting
	    //
	    // Augmenter 1
	    // Augmenter 2
	    // Augmenter 3

	    for (String item : items) {

		List<Component> parentComponents = settingNameToComponentsMap.get(item);
		group.addComponents(item, parentComponents);

		List<String> children = getChildrenOfParent(item);

		for (String child : children) {

		    List<Component> list = settingNameToComponentsMap.get(child);
		    group.addComponents(item, list);
		}
	    }

	    group.applySelection();
	}
    }

    /**
     * @param parent
     * @param setting
     */
    private boolean updateCheckGroup(Setting parent, Setting setting, Component component, SelectionMode multiSelectionMode) {

	if (parent != null && checkMap.get(parent.getName()) != null && multiSelectionMode == SelectionMode.MULTI) {

	    checkMap.get(parent.getName()).addItem(setting.getName());
	    return true;
	}

	return false;
    }

    /***
     * @param parent
     * @param setting
     * @param component one of: reset button, disable button
     * @return
     */
    private boolean updateRadioGroup(Setting parent, Setting setting, Component component, SelectionMode multiSelectionMode) {

	if (parent != null && radioMap.get(parent.getName()) != null && multiSelectionMode == SelectionMode.SINGLE) {

	    radioMap.get(parent.getName()).addItem(setting.getName());
	    return true;
	}

	return false;
    }

    /**
     * @param setting
     * @param component
     */
    private void updateSettingToComponentsMap(Setting setting, Component component) {

	List<Component> list = settingNameToComponentsMap.get(setting.getName());
	if (list == null) {
	    list = new ArrayList<>();
	    settingNameToComponentsMap.put(setting.getName(), list);
	}
	list.add(component);
    }

    /**
     * @param parent
     * @param setting
     */
    private void updateChildToParentsMap(Setting parent, Setting setting) {

	if (parent != null) {

	    List<Setting> list = childToParentsMap.get(setting);
	    if (list == null) {
		list = new ArrayList<>();
		childToParentsMap.put(setting, list);
	    }

	    List<Setting> superParents = childToParentsMap.get(parent);
	    if (superParents != null) {
		list.addAll(superParents);
	    }

	    list.add(parent);
	}
    }

    /**
     * @param parentSettingName
     * @return
     */
    private List<String> getChildrenOfParent(String parentSettingName) {

	return childToParentsMap.//
		keySet().//
		stream().//
		filter(child -> getParentNamesOfChild(child).contains(parentSettingName)).//
		map(s -> s.getName()).//
		collect(Collectors.toList());
    }

    /**
     * @param childSetting
     * @return
     */
    private List<String> getParentNamesOfChild(Setting childSetting) {

	return childToParentsMap.get(childSetting).stream().map(s -> s.getName()).collect(Collectors.toList());
    }

    /**
     * @param parent
     * @param setting
     * @return
     */
    private int computePaddingLeft(Setting parent, Setting setting) {

	if (parent == null) {

	    levelMap.put(setting, -1);

	} else if (parent.getSelectionMode() != SelectionMode.UNSET) {

	    Integer level = levelMap.getOrDefault(parent, -1);
	    levelMap.put(setting, ++level);
	}

	Integer level = levelMap.getOrDefault(setting, 0);

	return (level) * LEVEL_LEFT_PADDING;
    }

    /**
     * @param setting
     * @return
     */
    private boolean isScheduling(Setting setting) {

	return setting.getSettingClass().equals(Scheduling.class);
    }

}
