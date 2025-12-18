package eu.essi_lab.cfga.gui.components.tabs;

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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.details.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.tabs.*;
import com.vaadin.flow.component.textfield.*;
import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.gui.components.*;
import eu.essi_lab.cfga.gui.components.grid.*;
import eu.essi_lab.cfga.gui.components.setting.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.cfga.gui.dialog.*;
import eu.essi_lab.cfga.gui.directive.*;
import eu.essi_lab.cfga.setting.*;

import java.util.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public class TabContent extends VerticalLayout implements Renderable {

    /**
     *
     */
    public static final String TAB_HEADER_ID_PREFIX = "tabHeader";

    private GridComponent grid;
    private Optional<RemoveDirective> removeDirective;
    private Optional<EditDirective> editDirective;
    private TabDescriptor tabDesc;
    private TabContentDescriptor tabContentDesc;
    private Configuration configuration;
    private boolean rendered;
    private final List<Component> legends;

    /**
     *
     */
    public TabContent() {

	legends = new ArrayList<>();

	getStyle().set("padding", "0px");
	getStyle().set("padding-top", "5px");

	setMargin(false);
	setSpacing(false);

	// computes the height
	UI.getCurrent().getPage().retrieveExtendedClientDetails(receiver -> {

	    int screenHeight = receiver.getScreenHeight();
	    setHeight(screenHeight - ComponentFactory.MIN_HEIGHT_OFFSET, Unit.PIXELS);
	});
    }

    /**
     * @return
     */
    @Override
    public Component getComponent() {

	return this;
    }

    /**
     * @param configuration
     * @param tabContentDesc
     * @param tabDesc
     */
    public void init(//
	    Configuration configuration, //
	    TabContentDescriptor tabContentDesc, //
	    TabDescriptor tabDesc) {

	this.configuration = configuration;
	this.tabContentDesc = tabContentDesc;
	this.tabDesc = tabDesc;

	DirectiveManager directiveManager = tabContentDesc.getDirectiveManager();

	this.removeDirective = directiveManager.get(RemoveDirective.class);
	this.editDirective = directiveManager.get(EditDirective.class);

	//
	// Header
	//

	HorizontalLayout headerLayout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout(
		TabContent.TAB_HEADER_ID_PREFIX + "_" + tabContentDesc.getLabel());
	headerLayout.setWidthFull();
	headerLayout.setAlignItems(Alignment.BASELINE);

	add(headerLayout);

	//
	// Description
	//

	Optional<ShowDirective> showDirective = directiveManager.get(ShowDirective.class);

	if (showDirective.flatMap(ShowDirective::getDescription).isPresent()) {

	    VerticalLayout descLayout = ComponentFactory.createNoSpacingNoMarginVerticalLayout();
	    descLayout.getStyle().set("padding", "0px");
	    descLayout.setWidthFull();

	    final String desc = showDirective.flatMap(ShowDirective::getDescription).get();

	    HorizontalLayout descFieldLayout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout();
	    descFieldLayout.getStyle().set("padding", "0px");
	    descFieldLayout.setWidthFull();

	    TextField descField = new TextField();
	    descField.setWidthFull();
	    descField.setReadOnly(true);
	    descField.addClassName("text-field-no-border");

	    descField.getStyle().set("margin-left", "4px");
	    descField.getStyle().set("font-size", "14px");
	    descField.getStyle().set("color", "black");
	    descField.setValue(desc);

	    descFieldLayout.add(descField);

	    descLayout.add(descFieldLayout);

	    if (showDirective.get().withDescriptionSeparator()) {

		Div separator = ComponentFactory.createSeparator();
		separator.getStyle().set("width", "99%");
		separator.getStyle().set("margin-top", "3px");
		separator.getStyle().set("margin-left", "4px");

		descLayout.add(separator);
	    }

	    headerLayout.add(descLayout);

	    //
 	    // max description length is limited
 	    //

	    Optional<AddDirective> addDirective = directiveManager.get(AddDirective.class);

	    int maxDescLength = 300; // no add, no reload

	    if (addDirective.isPresent() && tabContentDesc.isReloadable()) { // add & reload

		descLayout.setMaxWidth("1070px");
		maxDescLength = 180;

	    } else if (addDirective.isPresent() && !tabContentDesc.isReloadable()) { // only add

		descLayout.setMaxWidth("1240px");
		descField.setMaxWidth("1200px");

		maxDescLength = 200;

	    } else if (addDirective.isEmpty() && tabContentDesc.isReloadable()) { // only reload

		descLayout.setMaxWidth("1165px");
		descField.setMaxWidth("1150px");

		maxDescLength = 190;
	    }

	    if (desc.length() > maxDescLength) {

		String shortDesc = desc.substring(0, maxDescLength - 5);

		descField.setValue(shortDesc);

		Button extraDescButton = new Button("...");
		extraDescButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
		extraDescButton.getStyle().set("margin-right", "21px");
		extraDescButton.setTooltipText("Click to see full description");
		extraDescButton.addClickListener(evt -> {

		    TextArea textArea = new TextArea();
		    textArea.addClassName("text-area-no-border");
		    textArea.getStyle().set("font-size", "14px");
		    textArea.getStyle().set("padding", "0px");

		    textArea.setWidth("485px");
		    textArea.setHeight("300px");
		    textArea.setReadOnly(true);
		    textArea.setValue(desc);

		    NotificationDialog dialog = NotificationDialog.getNotificationDialog("Description", desc);
		    dialog.setContent(textArea);

		    dialog.open();
		});

		descFieldLayout.add(extraDescButton);
	    }

	} else {

	    Div div = ComponentFactory.createDiv();
	    div.setWidthFull();

	    headerLayout.add(div);
	}

	//
	// ADD button
	//

	Optional<AddDirective> addDirective = directiveManager.get(AddDirective.class);

	addDirective.ifPresent(dir -> {

	    Button addButton = SettingComponentFactory.createSettingAddButton(configuration, this, dir);
	    headerLayout.add(addButton);
	});

	//
	// RELOAD button
	//

	if (tabContentDesc.isReloadable()) {

	    if (addReloadButton(headerLayout)) {

		CustomButton reloadButton = createReloadButton();

		headerLayout.add(reloadButton);
	    }
	}
    }

    /**
     * @param refresh
     */
    @Override
    public void render(boolean refresh) {

	setRendered(true);

	removeAllButHeader();

	//
	// rendering according to the content provided by the TabContentDescriptor
	//

	if (tabContentDesc.getContent().isPresent()) {

	    add(tabContentDesc.getContent().get());

	    return;
	}

	//
	// rendering according to the TabContentDescriptor setting class
	//

	List<Setting> settings = configuration.list(tabContentDesc.getSettingClass().get(), false).//
		stream().//
		map(s -> (Setting) s).//
		collect(Collectors.toList());

	DirectiveManager directiveManager = tabContentDesc.getDirectiveManager();

	Optional<ShowDirective> showDirective = directiveManager.get(ShowDirective.class);

	//
	// optional settings sorting
	//

	showDirective.flatMap(ShowDirective::getSortDirection).ifPresent(dir -> {

	    switch (dir) {
	    case ASCENDING -> settings.sort(Comparator.comparing(Setting::getName));
	    case DESCENDING -> settings.sort((s1, s2) -> s2.getName().compareTo(s1.getName()));
	    }
	});

	boolean readOnly = tabDesc.isForceReadOnlySet();

	//
	// grid component
	//

	if (tabContentDesc.getGridInfo().isPresent()) {

	    Optional<GridInfo> gridInfo = tabContentDesc.getGridInfo();

	    GridComponent gridComponent = new GridComponent(//
		    gridInfo.get(), //
		    settings, //
		    configuration, //
		    this, //
		    readOnly, //
		    refresh, tabDesc.getContentDescriptors().size() > 1);

	    TabSheet tabSheet = new TabSheet();
	    tabSheet.getStyle().set("border-bottom", "1px solid #d3d3d39e");
	    boolean addTabSheet = false;

	    if (tabContentDesc.getGridInfo().get().isShowColumnsHider()) {

		tabSheet.add("Columns", gridComponent.createColumnsHider());
		addTabSheet = true;
	    }

	    if (!legends.isEmpty()) {

		tabSheet.add("Legend", gridComponent.createLegendViewer(legends));
		addTabSheet = true;
	    }

	    if (addTabSheet) {

		add(tabSheet);
	    }

	    add(gridComponent);

	    expand(gridComponent);

	} else {

	    //
	    // settings list
	    //

	    for (int i = 0; i < settings.size(); i++) {

		Setting setting = settings.get(i);

		SettingComponent component = SettingComponentFactory.createSettingComponent(//
			configuration, //
			setting, //
			readOnly, //
			false,// forceHideHeader
			this); //

		if (component.getDetails().isPresent()) {

		    Details details = component.getDetails().get();

		    if (i == 0) {

			details.getStyle().set("margin-top", "15px");
		    }

		    this.add(details);

		} else {

		    this.add(component);
		}
	    }
	}
    }

    /**
     * @return the rendered
     */
    @Override
    public boolean isRendered() {

	return rendered;
    }

    /**
     * @param rendered
     */
    @Override
    public void setRendered(boolean rendered) {

	this.rendered = rendered;
    }

    /**
     *
     */
    public void add(Component... components) {

	super.add(components);

	if (components.length == 1 && components[0] instanceof GridComponent) {

	    this.grid = (GridComponent) components[0];
	}
    }

    /**
     * @param legend
     * @return
     */
    public void addLegend(Component legend) {

	if (legends.stream().map(lg -> lg.getId().get()).noneMatch(id -> id.equals(legend.getId().get()))) {

	    legends.add(legend);
	}
    }

    /**
     * @param component
     */
    public void addSettingComponent(SettingComponent component) {

	if (getGrid().isPresent()) {

	    GridComponent grid = getGrid().get();

	    grid.addSettingComponent(component);

	} else {

	    if (component.getDetails().isPresent()) {

		addComponentAtIndex(1, component.getDetails().get());

	    } else {

		addComponentAtIndex(1, component);
	    }
	}
    }

    /**
     * @param oldComponent
     * @param newComponent
     */
    public void replaceSettingComponent(SettingComponent oldComponent, SettingComponent newComponent) {

	if (getGrid().isPresent()) {

	    GridComponent grid = getGrid().get();

	    grid.replaceSettingComponent(oldComponent, newComponent);

	} else {

	    Component toRemove = oldComponent.getDetails().isPresent() ? oldComponent.getDetails().get() : oldComponent;
	    Component toAdd = newComponent.getDetails().isPresent() ? newComponent.getDetails().get() : newComponent;

	    int index = indexOf(toRemove);

	    addComponentAtIndex(index, toAdd);

	    toRemove.getElement().getStyle().set("display", "none");

	    remove(toRemove);

	    // int index = indexOf(oldComponent);
	    //
	    // addComponentAtIndex(index, newComponent);
	    //
	    // remove(oldComponent);
	}
    }

    /**
     * @param component
     * @param settingIdentifier
     */
    public void removeSettingComponent(SettingComponent component, String settingIdentifier) {

	if (getGrid().isPresent()) {

	    getGrid().get().removeSettingComponent(component, settingIdentifier);

	} else {

	    if (component.getDetails().isPresent()) {
		component.getDetails().get().getElement().getStyle().set("display", "none");
	    }

	    component.getStyle().set("display", "none");
	}
    }

    /**
     * Working only if a {@link #getGrid()} is present
     *
     * @param settingIdentifiers
     */
    public void removeSettingComponents(List<String> settingIdentifiers) {

	if (getGrid().isPresent()) {

	    getGrid().get().removeSettingComponents(settingIdentifiers);
	}
    }

    /**
     * @return the removeDirective
     */
    public Optional<RemoveDirective> getRemoveDirective() {

	return removeDirective;
    }

    /**
     * @return the editDirective
     */
    public Optional<EditDirective> getEditDirective() {

	return editDirective;
    }

    /**
     *
     */
    void removeAllButHeader() {

	getChildren().forEach(c -> {

	    if (c.getId().isEmpty() || !c.getId().get().startsWith(TAB_HEADER_ID_PREFIX)) {

		remove(c);
	    }
	});
    }

    /**
     * @return
     */
    private CustomButton createReloadButton() {

	CustomButton reloadButton = ComponentFactory.createReloadButton();

	reloadButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {

	    if (tabContentDesc.getTabReloader().isPresent()) {

		tabContentDesc.getTabReloader().get().run();

		removeAllButHeader();

		render(true);

	    } else {

		removeAllButHeader();

		render(true);
	    }
	});

	return reloadButton;
    }

    /**
     * @param headerLayout
     * @return
     */
    private boolean addReloadButton(HorizontalLayout headerLayout) {

	return headerLayout.//
		getChildren().//
		noneMatch(child -> child.getId().isPresent() && child.getId().get().equals("reloadButton"));
    }

    /**
     * @return
     */
    private HorizontalLayout findHeader() {

	HorizontalLayout[] out = new HorizontalLayout[1];

	getChildren().forEach(c -> {

	    if (c.getId().isPresent() && c.getId().get().startsWith(TAB_HEADER_ID_PREFIX)) {

		out[0] = (HorizontalLayout) c;
	    }
	});

	return out[0];
    }

    /**
     * @return
     */
    private Optional<GridComponent> getGrid() {

	return Optional.ofNullable(grid);
    }
}
