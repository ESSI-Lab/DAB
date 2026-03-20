package eu.essi_lab.cfga.gui.components.setting;

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

import com.vaadin.flow.component.AbstractField.*;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.HasValue.*;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.details.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.tabs.*;
import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.Selectable.*;
import eu.essi_lab.cfga.gui.components.*;
import eu.essi_lab.cfga.gui.components.option.*;
import eu.essi_lab.cfga.gui.components.setting.group.*;
import eu.essi_lab.cfga.gui.components.tabs.*;
import eu.essi_lab.cfga.option.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.cfga.setting.scheduling.*;
import eu.essi_lab.lib.utils.*;

import java.util.*;
import java.util.stream.*;

/**
 * <info>
 * <style>
 * #component { font-family: Arial, Helvetica, sans-serif; border-collapse: collapse; width: 100%; } #component td, #component th { border:
 * 1px solid #ddd; padding: 8px; } #component tr:nth-child(even){background-color: #f2f2f2;} #component tr:hover {background-color: #ddd;}
 * #component th { padding-top: 12px; padding-bottom: 12px; text-align: left; background-color: #4CAF50; color: white; }
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

    private HashMap<String, Integer> levelMap;
    private HashMap<String, List<String>> childToParentsMap;

    private HashMap<String, List<Component>> settingNameToComponentsMap;
    private HashMap<String, List<Component>> settingNameToSwitchAndOptionsMap;

    private final boolean forceReadonly;
    private Setting setting;

    private final Configuration configuration;
    private Details details;
    private final TabContent tabContent;

    private final boolean forceHideHeader;
    private final boolean tabView;

    private TabSheet tabSheet;

    /**
     * @param configuration
     * @param setting
     * @param forceReadonly
     * @param forceHideHeader
     * @param comparator
     * @param tabContent
     */
    public SettingComponent(//
	    Configuration configuration, //
	    Setting setting, //
	    boolean forceReadonly, //
	    boolean forceHideHeader, //
	    Comparator<Setting> comparator, //
	    TabContent tabContent) {

	this(configuration, setting, forceReadonly, forceHideHeader, comparator, tabContent, false);
    }

    /**
     * @param configuration
     * @param setting
     * @param forceReadonly
     * @param forceHideHeader
     * @param comparator
     * @param tabContent
     * @param tabView
     */
    public SettingComponent(//
	    Configuration configuration, //
	    Setting setting, //
	    boolean forceReadonly, //
	    boolean forceHideHeader, //
	    Comparator<Setting> comparator, //
	    TabContent tabContent, //
	    boolean tabView) {

	this.configuration = configuration;
	this.setting = setting;
	this.forceReadonly = forceReadonly;
	this.tabContent = tabContent;
	this.forceHideHeader = forceHideHeader;
	this.tabView = tabView;

	if (setting.isFoldedModeEnabled()) {

	    this.details = ComponentFactory.createDetails(setting.getName(), this);
	    this.details.setId("setting-component-" + getSetting().getName());
	}

	init(comparator, tabView);
    }

    /**
     * @param comparator
     * @param tabView
     */
    private void init(Comparator<Setting> comparator, boolean tabView) {

	setId("setting-component-" + getSetting().getName());

	setWidthFull();

	getStyle().set("background-color", "white");
	getStyle().set("border-radius", "5px");
	getStyle().set("margin-top", "0px");

	radioMap = new HashMap<>();
	checkMap = new HashMap<>();
	levelMap = new HashMap<>();
	childToParentsMap = new HashMap<>();
	settingNameToComponentsMap = new HashMap<>();
	settingNameToSwitchAndOptionsMap = new HashMap<>();

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

	if (tabView) {

	    this.tabSheet = new TabSheet();
	    this.tabSheet.getStyle().set("padding", "0px");
	    this.tabSheet.getStyle().set("margin-left", "-20px");

	    add(tabSheet);
	}

	deepRenderSetting(null, getSetting(), configuration, comparator);

	radioMap.values().forEach(GroupComponentsHandler::setItems);
	checkMap.values().forEach(GroupComponentsHandler::setItems);

	addRadioMultiSelectionComponents();
	addCheckMultiSelectionComponents();

	radioMap.values().forEach(GroupComponentsHandler::scrollInToView);
    }

    /**
     * @param enabled
     */
    public void onSwitchStateChanged(ValueChangeEvent<?> event) {

	boolean enabled = (Boolean) event.getValue();

	//
	// updates the state of the setting with given setting id
	//
	ComponentValueChangeEvent<Component, ?> compEvent = (ComponentValueChangeEvent<Component, ?>) event;

	Component source = compEvent.getSource();

	String settingId = source.getElement().getAttribute("settingid");

	ArrayList<Setting> list = new ArrayList<>();

	SettingUtils.deepFind(getSetting(), s -> s.getIdentifier().equals(settingId), list);

	Setting settingToUpdate = list.getFirst();

	GSLoggerFactory.getLogger(getClass()).debug("Updating state of setting: {}", settingToUpdate.getName());
	GSLoggerFactory.getLogger(getClass()).debug("New state: {}", enabled ? "enabled" : "disabled");

	settingToUpdate.setEnabled(enabled);

	//
	// updates the sub components
	//
	onSwitchStateChanged(settingToUpdate, enabled);
    }

    /**
     * @return
     */
    public Setting getSetting() {

	return setting;
    }

    /**
     * @return
     */
    public Optional<Details> getDetails() {

	return Optional.ofNullable(details);
    }

    /**
     * @param settingName
     * @return
     */
    public List<OptionTextField> getOptionTextFields(String settingName) {

	List<Component> list = settingNameToComponentsMap.get(settingName);

	if (list != null && !list.isEmpty()) {

	    OptionComponentLayout layout = (OptionComponentLayout) list.getFirst();

	    return layout.//
		    getOptionComponents().//
		    stream().//
		    map(oc -> (OptionTextField) oc.getOptionLayout().getChildren().filter(c -> c instanceof OptionTextField).findFirst()
		    .orElse(null)).//
		    filter(Objects::nonNull).//
		    collect(Collectors.toList());
	}

	return new ArrayList<>();
    }

    /**
     * @return
     */
    public Optional<RadioComponentsHandler> getRadioHandler() {

	return radioMap.values().stream().findFirst();
    }

    /**
     * @return
     */
    public Optional<CheckComponentsHandler> getCheckHandler() {

	return checkMap.values().stream().findFirst();
    }

    /**
     * @param setting
     * @param enabled
     */
    private void onSwitchStateChanged(Setting setting, boolean enabled) {

	List<Component> list = settingNameToSwitchAndOptionsMap.get(setting.getName());

	for (Component comp : list) {

	    if (comp instanceof OptionComponent optionComp) {

		optionComp.onSettingSwitchStateChanged(enabled, forceReadonly);

	    } else {

		//
		// This is the switch button of a child setting
		//
		Switch switch_ = (Switch) comp;

		//
		// if the parent setting value is false, also the state of the switch button
		// of the children must be set to false
		//
		if (!enabled) {

		    switch_.setValue(false);
		}

		//
		// if the force readonly mode is enabled, the switch button
		// cannot be used so it must be disabled. this is the only case
		// where a toggle button of a setting component can be disabled
		//
		if (forceReadonly) {

		    switch_.setEnabled(false);
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

	if (tabView) {

	    List<String> parents = childToParentsMap.get(setting.getName());

	    if (parents != null && parents.size() == 1) {

		tabSheet.add(setting.getName(), mainLayout);

	    } else if (parents != null) {

		int tabCount = tabSheet.getTabCount();

		for (int i = 0; i < tabCount; i++) {

		    if (parents.contains(tabSheet.getTabAt(i).getLabel())) {

			((VerticalLayout) tabSheet.getComponent(tabSheet.getTabAt(i))).add(mainLayout);
		    }
		}
	    } else if (!setting.getOptions().isEmpty()) {

		tabSheet.add("Options", mainLayout);
	    }

	} else {

	    add(mainLayout);
	}

	HorizontalLayout headerLayout = SettingComponentFactory.createSettingHeaderLayout(setting);
	headerLayout.setId("header-layout_" + setting.getName());

	//
	// forced to hide the whole header by the put/edit dialogs
	// the header is hidden only in the root setting
	//
	if (forceHideHeader && parent == null) {

	    headerLayout.getStyle().set("display", "none");
	}

	//
	// if the name is hidden, an empty div is added instead of the label
	//
	if (!setting.isShowHeaderSet()) {

	    Div div = ComponentFactory.createDiv();

	    headerLayout.add(div);
	}

	HorizontalLayout descriptionLayout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout();

	descriptionLayout.setWidthFull();
	descriptionLayout.setAlignItems(Alignment.BASELINE);

	mainLayout.add(headerLayout);
	mainLayout.add(descriptionLayout);

	//
	// get selection mode of its parent (if exists)
	//
	SelectionMode selectionMode = parent != null ? parent.getSelectionMode() : SelectionMode.UNSET;

	switch (selectionMode) {
	case UNSET:

	    String tabLabel = "";

	    if (tabView) {

		int tabCount = tabSheet.getTabCount();
		tabLabel = tabCount > 0 ? tabSheet.getTabAt(tabCount - 1).getLabel() : "";
	    }

	    //
	    // name is added to the header if the setting is not folded
	    // if the setting is folded, the name is visible in the details component
	    //
	    if (!setting.isFoldedModeEnabled() && !tabLabel.equals(setting.getName())) {

		Span label = handleLabel(parent, setting, headerLayout);

		if (label != null) {

		    updateSettingToComponentsMap(setting, label);
		}

	    } else {
		//
		// if the setting is folded, since there is no label, in order to place
		// the buttons on the right, an empty div is added instead of the label
		//
		if (setting.canBeDisabled() || setting.canBeRemoved() || setting.isEditable()) {

		    Div div = ComponentFactory.createDiv();

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
	// edit button
	//
	handleEditButton(parent, setting, headerLayout, selectionMode);

	//
	// remove button
	//
	handleRemoveButton(parent, setting, tabContent, headerLayout, selectionMode);

	//
	// disable button
	//
	Optional<Switch> switch_ = handleSwith(parent, setting, headerLayout, selectionMode);

	//
	// options
	//
	Optional<OptionComponentLayout> optionLayout = handleOptions(parent, setting, mainLayout, configuration, selectionMode);

	if (switch_.isPresent()) {

	    List<Component> list = settingNameToSwitchAndOptionsMap.computeIfAbsent(setting.getName(), k -> new ArrayList<>());

	    list.add(switch_.get());

	    if (optionLayout.isPresent()) {

		List<OptionComponent> optionComponents = optionLayout.get().getOptionComponents();

		list.addAll(optionComponents);
	    }

	    onSwitchStateChanged(setting, switch_.get().getValue());
	}
    }

    /**
     * @param parent
     * @param setting
     * @param headerLayout
     */
    private Span handleLabel(Setting parent, Setting setting, HorizontalLayout headerLayout) {

	if (setting.isShowHeaderSet()) {

	    Span label = SettingComponentFactory.createSettingNameSpan(setting, parent);

	    headerLayout.add(label);

	    return label;
	}

	return null;
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

	if (description.isEmpty() && !hasVisibleOptions(setting) && multiSelectionMode != SelectionMode.UNSET) {

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

	    Button button = SettingComponentFactory.createSettingEditButton(configuration, setting, this, tabContent);

	    if (setting.isShowHeaderSet()) {

		button.getStyle().set("margin-left", "3px");
	    }

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
	    Setting parent, //
	    Setting setting, //
	    TabContent tabContent, //
	    HorizontalLayout headerLayout, //
	    SelectionMode multiSelectionMode) {

	boolean canBeRemoved = setting.canBeRemoved();

	if (canBeRemoved) {

	    Button button = SettingComponentFactory.createSettingRemoveButton(configuration, tabContent, this);

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
    private Optional<Switch> handleSwith(//
	    Setting parent, //
	    Setting setting, //
	    HorizontalLayout headerLayout, //
	    SelectionMode multiSelectionMode) {

	boolean canBeDisabled = setting.canBeDisabled();

	if (canBeDisabled) {

	    boolean enabled = !forceReadonly;

	    Switch switch_ = SettingComponentFactory.createSettingSwitch(this, setting.isEnabled(), enabled);

	    //
	    //
	    // set as attribute the setting identifier. the listener for the button state changes is added
	    // to the SettingComponent, which will have several listeners attached, one for each switch_
	    // button of the children settings. When the button state changes, it is possible to retrieve the
	    // setting id property from the switch_ button which is the source of the event, and searching for
	    // the child setting having that id in order to update the setting state according to the button value
	    //
	    //
	    switch_.getElement().setAttribute("settingid", setting.getIdentifier());

	    updateSettingToComponentsMap(setting, switch_);

	    boolean radioUpdated = !isScheduling(setting) && updateRadioGroup(parent, setting, switch_, multiSelectionMode);
	    boolean checkUpdated = !isScheduling(setting) && updateCheckGroup(parent, setting, switch_, multiSelectionMode);

	    if (!radioUpdated && !checkUpdated) {

		switch_.getStyle().set("margin-left", "5px");

		if (forceHideHeader && parent == null) {

		    headerLayout.removeAll();

		    Div div = ComponentFactory.createDiv();

		    headerLayout.add(div);
		    headerLayout.getStyle().set("display", "");
		}

		headerLayout.add(switch_);
	    }

	    return Optional.of(switch_);
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
		filter(ConfigurationObject::isVisible).//
		count();

	if (!options.isEmpty() && visibileOptions > 0) {

	    OptionComponentLayout optionLayout = SettingComponentFactory.createSettingOptionsComponent(//
		    configuration, //
		    setting,//
		    forceReadonly);//

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

	long visibileOptions = options.stream().filter(ConfigurationObject::isVisible).count();

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

	List<Component> list = settingNameToComponentsMap.computeIfAbsent(setting.getName(), k -> new ArrayList<>());
	list.add(component);
    }

    /**
     * @param parent
     * @param setting
     */
    private void updateChildToParentsMap(Setting parent, Setting setting) {

	if (parent != null) {

	    List<String> list = childToParentsMap.computeIfAbsent(setting.getName(), k -> new ArrayList<>());

	    List<String> superParents = childToParentsMap.get(parent.getName());

	    if (superParents != null) {

		list.addAll(superParents);
	    }

	    list.add(parent.getName());
	}
    }

    /**
     * @param parentSettingName
     * @return
     */
    private List<String> getChildrenOfParent(String parentSettingName) {

	return childToParentsMap.//
		keySet().//
		parallelStream().//
		filter(child -> getParentNamesOfChild(child).contains(parentSettingName)).//
		toList();
    }

    /**
     * @param childSetting
     * @return
     */
    private List<String> getParentNamesOfChild(String childSetting) {

	return childToParentsMap.get(childSetting);
    }

    /**
     * @param parent
     * @param setting
     * @return
     */
    private int computePaddingLeft(Setting parent, Setting setting) {

	if (parent == null) {

	    levelMap.put(setting.getIdentifier(), -1);

	} else if (parent.getSelectionMode() != SelectionMode.UNSET) {

	    Integer level = levelMap.getOrDefault(parent.getIdentifier(), -1);
	    levelMap.put(setting.getIdentifier(), ++level);
	}

	Integer level = levelMap.getOrDefault(setting.getIdentifier(), 0);

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
