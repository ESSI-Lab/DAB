package eu.essi_lab.cfga.gui.components;

import java.util.ArrayList;

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
import java.util.Optional;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gui.ConfigurationView;
import eu.essi_lab.cfga.gui.components.grid.GridComponent;
import eu.essi_lab.cfga.gui.components.grid.GridInfo;
import eu.essi_lab.cfga.gui.components.setting.SettingComponent;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabDescriptor;
import eu.essi_lab.cfga.gui.extension.directive.DirectiveManager;
import eu.essi_lab.cfga.gui.extension.directive.EditDirective;
import eu.essi_lab.cfga.gui.extension.directive.RemoveDirective;
import eu.essi_lab.cfga.gui.extension.directive.ShowDirective;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class TabContainer extends VerticalLayout {

    private GridComponent grid;
    private Optional<RemoveDirective> removeDirective;
    private Optional<EditDirective> editDirective;
    private TabDescriptor tabDescriptor;
    private ComponentInfo componentInfo;
    private Configuration configuration;
    private boolean rendered;
    private ConfigurationView view;
    private final List<Component> legends;

    /**
     *
     */
    public TabContainer() {

	legends = new ArrayList<>();
    }

    /**
     *
     */
    public void render() {

	render(false);
    }

    /**
     * @param refresh
     */
    public void render(boolean refresh) {

	rendered = true;

	boolean readOnly = componentInfo.isForceReadOnlySet();

	HorizontalLayout headerLayout = findHeader();

	if (tabDescriptor.isReloadable()) {

	    if (addReloadButton(headerLayout)) {

		CustomButton reloadButton = createReloadButton();

		headerLayout.add(reloadButton);
	    }
	}

	removeAllButHeader();

	if (tabDescriptor.getComponent().isPresent()) {

	    add(tabDescriptor.getComponent().get());

	    return;
	}

	List<Setting> settings = view.retrieveTabSettings(tabDescriptor);

	DirectiveManager directiveManager = tabDescriptor.getDirectiveManager();

	Optional<ShowDirective> showDirective = directiveManager.get(ShowDirective.class);

	showDirective.flatMap(ShowDirective::getSortDirection).ifPresent(dir -> {

	    switch (dir) {
	    case ASCENDING -> settings.sort(Comparator.comparing(Setting::getName));
	    case DESCENDING -> settings.sort((s1, s2) -> s2.getName().compareTo(s1.getName()));
	    }
	});

	if (tabDescriptor.getGridInfo().isPresent()) {

	    Optional<GridInfo> gridInfo = tabDescriptor.getGridInfo();

	    GridComponent gridComponent = new GridComponent(//
		    gridInfo.get(), //
		    settings, //
		    configuration, //
		    this, //
		    readOnly, //
		    refresh);

	    TabSheet tabSheet = new TabSheet();
	    tabSheet.getStyle().set("border-bottom", "1px solid #d3d3d39e");

	    if (tabDescriptor.getGridInfo().get().isShowColumnsHider()) {

		tabSheet.add("Columns", gridComponent.createColumnsHider());
	    }

	    if (!legends.isEmpty()) {

		tabSheet.add("Legend", gridComponent.createLegendsViewer(legends));
	    }

	    add(tabSheet);

	    add(gridComponent);

	    expand(gridComponent);

	} else {

	    for (int i = 0; i < settings.size(); i++) {

		Setting setting = settings.get(i);

		SettingComponent component = SettingComponentFactory.createSettingComponent(configuration, setting.getIdentifier(),
			readOnly, this);

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
    public boolean isRendered() {

	return rendered;
    }

    /**
     * @param rendered
     */
    public void setRendered(boolean rendered) {

	this.rendered = rendered;
    }

    /**
     * @param view
     * @param configuration
     * @param settings
     * @param componentInfo
     * @param tabDescriptor
     */
    public void init(ConfigurationView view, Configuration configuration, ComponentInfo componentInfo, TabDescriptor tabDescriptor) {

	this.view = view;
	this.configuration = configuration;
	this.componentInfo = componentInfo;
	this.tabDescriptor = tabDescriptor;
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
     * @param removeDirective
     */
    public void setRemoveDirective(Optional<RemoveDirective> removeDirective) {

	this.removeDirective = removeDirective;
    }

    /**
     * @return the removeDirective
     */
    public Optional<RemoveDirective> getRemoveDirective() {

	return removeDirective;
    }

    /**
     * @param editDirective
     */
    public void setEditDirective(Optional<EditDirective> editDirective) {

	this.editDirective = editDirective;
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

	    if (c.getId().isEmpty() || !c.getId().get().startsWith(ConfigurationViewFactory.TAB_HEADER_ID_PREFIX)) {

		remove(c);
	    }
	});
    }

    /**
     * @return
     */
    private CustomButton createReloadButton() {

	CustomButton reloadButton = new CustomButton("Reload", VaadinIcon.REFRESH.create());
	reloadButton.setId("reloadButton");
	reloadButton.setWidth(150, Unit.PIXELS);
	reloadButton.getStyle().set("margin-left", "15px");
	reloadButton.getStyle().set("border", "1px solid hsl(0deg 0% 81%");

	reloadButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {

	    if (tabDescriptor.getTabReloader().isPresent()) {

		tabDescriptor.getTabReloader().get().run();

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

	    if (c.getId().isPresent() && c.getId().get().startsWith(ConfigurationViewFactory.TAB_HEADER_ID_PREFIX)) {

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
