package eu.essi_lab.cfga.gui.components.tabs.descriptor;

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

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.data.provider.SortDirection;

import eu.essi_lab.cfga.gui.components.grid.ColumnDescriptor;
import eu.essi_lab.cfga.gui.components.grid.GridInfo;
import eu.essi_lab.cfga.gui.components.grid.GridMenuItemHandler;
import eu.essi_lab.cfga.gui.directive.AddDirective;
import eu.essi_lab.cfga.gui.directive.Directive.ConfirmationPolicy;
import eu.essi_lab.cfga.gui.directive.EditDirective;
import eu.essi_lab.cfga.gui.directive.RemoveDirective;
import eu.essi_lab.cfga.gui.directive.ShowDirective;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class TabContentDescriptorBuilder {

    private final TabContentDescriptor descriptor;

    /**
     *
     */
    private TabContentDescriptorBuilder() {

	descriptor = new TabContentDescriptor();
    }

    /**
     *
     */
    private TabContentDescriptorBuilder(Class<? extends Setting> settingClass) {

	descriptor = new TabContentDescriptor();
	descriptor.setSettingClass(settingClass);
    }

    /**
     * @return
     */
    public static TabContentDescriptorBuilder get() {

	return new TabContentDescriptorBuilder();
    }

    /**
     * @return
     */
    public static TabContentDescriptorBuilder get(Class<? extends Setting> settingClass) {

	return new TabContentDescriptorBuilder(settingClass);
    }

    /**
     * @param label
     * @return
     */
    public TabContentDescriptorBuilder withLabel(String label) {

	descriptor.setLabel(label);

	return this;
    }

    /**
     * @param settingClass
     * @return
     */
    public TabContentDescriptorBuilder withSettingClass(Class<? extends Setting> settingClass) {

	descriptor.setSettingClass(settingClass);

	return this;
    }

    /**
     * @param directive
     * @return
     */
    public TabContentDescriptorBuilder withAddDirective(AddDirective directive) {

	descriptor.getDirectiveManager().add(directive);

	return this;
    }

    /**
     * @param directiveName
     * @param settingClass
     * @return
     */
    public TabContentDescriptorBuilder withAddDirective(String directiveName, Class<? extends Setting> settingClass) {

	descriptor.getDirectiveManager().add(new AddDirective(directiveName, settingClass));

	return this;
    }

    /**
     * @param directiveName
     * @param settingClass
     * @return
     */
    public TabContentDescriptorBuilder withAddDirective(String directiveName, String settingClass) {

	descriptor.getDirectiveManager().add(new AddDirective(directiveName, settingClass));

	return this;
    }

    /**
     * @param directive
     * @return
     */
    public TabContentDescriptorBuilder withRemoveDirective(RemoveDirective directive) {

	descriptor.getDirectiveManager().add(directive);

	return this;
    }

    /**
     * @param name
     * @param allowFullRemoval
     * @param settingClass
     * @return
     */
    public TabContentDescriptorBuilder withRemoveDirective(String name, boolean allowFullRemoval, Class<? extends Setting> settingClass) {

	descriptor.getDirectiveManager().add(new RemoveDirective(name, allowFullRemoval, settingClass));

	return this;
    }

    /**
     * @param name
     * @param allowFullRemoval
     * @param settingClass
     * @return
     */
    public TabContentDescriptorBuilder withRemoveDirective(String name, boolean allowFullRemoval, String settingClass) {

	descriptor.getDirectiveManager().add(new RemoveDirective(name, allowFullRemoval, settingClass));

	return this;
    }

    /**
     *
     * @param name
     * @return
     */
    public TabContentDescriptorBuilder withEditDirective(String name) {

	descriptor.getDirectiveManager().add(new EditDirective(name));

	return this;
    }

    /**
     * @param name
     * @param policy
     * @return
     */
    public TabContentDescriptorBuilder withEditDirective(String name, ConfirmationPolicy policy) {

	descriptor.getDirectiveManager().add(new EditDirective(name, policy));

	return this;
    }

    /**
     * @param description
     * @return
     */
    public TabContentDescriptorBuilder withShowDirective(String description) {

	return withShowDirective(description, null, true);
    }

    /**
     * @param description
     * @param showDescriptionSeparator
     * @return
     */
    public TabContentDescriptorBuilder withShowDirective(String description, boolean showDescriptionSeparator) {

	return withShowDirective(description, null, showDescriptionSeparator);
    }

    /**
     * @param description
     * @param direction
     * @return
     */
    public TabContentDescriptorBuilder withShowDirective(String description, SortDirection direction) {

	return withShowDirective(description, direction, true);
    }

    /**
     * @param direction
     * @return
     */
    public TabContentDescriptorBuilder withShowDirective(SortDirection direction) {

	return withShowDirective(null, direction, true);
    }

    /**
     * @param description
     * @param direction
     * @return
     */
    public TabContentDescriptorBuilder withShowDirective(String description, SortDirection direction, boolean showDescriptionSeparator) {

	ShowDirective showDirective = new ShowDirective();

	if (description != null) {

	    showDirective.setDescription(description);
	    showDirective.showDescriptionSeparator(showDescriptionSeparator);
	}

	if (direction != null) {

	    showDirective.setSortDirection(direction);
	}

	descriptor.getDirectiveManager().add(showDirective);

	return this;
    }

    /**
     * @param Component
     * @return
     */
    public TabContentDescriptorBuilder withComponent(Component Component) {

	descriptor.setContent(Component);

	return this;
    }

    /**
     * @param descriptors
     * @param showColumnsHider
     * @return
     */
    public TabContentDescriptorBuilder withGridInfo(List<ColumnDescriptor> descriptors, boolean showColumnsHider) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, new ArrayList<>(), SelectionMode.NONE, showColumnsHider);
    }

    /**
     * @param descriptors
     * @return
     */
    public TabContentDescriptorBuilder withGridInfo(List<ColumnDescriptor> descriptors) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, new ArrayList<>(), SelectionMode.NONE, true);
    }

    /**
     * @param descriptors
     * @param selectionMode
     * @param showColumnsHider
     * @return
     */
    public TabContentDescriptorBuilder withGridInfo(List<ColumnDescriptor> descriptors, SelectionMode selectionMode,
	    boolean showColumnsHider) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, new ArrayList<>(), selectionMode, showColumnsHider);
    }

    /**
     * @param descriptors
     * @param selectionMode
     * @return
     */
    public TabContentDescriptorBuilder withGridInfo(List<ColumnDescriptor> descriptors, SelectionMode selectionMode) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, new ArrayList<>(), selectionMode, true);
    }

    /**
     * @param pageSize
     * @param descriptors
     * @param showColumnsHider
     * @return
     */
    public TabContentDescriptorBuilder withGridInfo(int pageSize, List<ColumnDescriptor> descriptors, boolean showColumnsHider) {

	return withGridInfo(pageSize, descriptors, new ArrayList<>(), SelectionMode.NONE, showColumnsHider);
    }

    /**
     * @param pageSize
     * @param descriptors
     * @return
     */
    public TabContentDescriptorBuilder withGridInfo(int pageSize, List<ColumnDescriptor> descriptors) {

	return withGridInfo(pageSize, descriptors, new ArrayList<>(), SelectionMode.NONE, true);
    }

    /**
     * @param pageSize
     * @param descriptors
     * @param selectionMode
     * @param showColumnsHider
     * @return
     */
    public TabContentDescriptorBuilder withGridInfo(int pageSize, List<ColumnDescriptor> descriptors, SelectionMode selectionMode,
	    boolean showColumnsHider) {

	return withGridInfo(pageSize, descriptors, new ArrayList<>(), selectionMode, showColumnsHider);
    }

    /**
     * @param pageSize
     * @param descriptors
     * @param selectionMode
     * @return
     */
    public TabContentDescriptorBuilder withGridInfo(int pageSize, List<ColumnDescriptor> descriptors, SelectionMode selectionMode) {

	return withGridInfo(pageSize, descriptors, new ArrayList<>(), selectionMode, true);
    }

    /**
     * @param descriptors
     * @param items
     * @param showColumnsHider
     * @return
     */
    public TabContentDescriptorBuilder withGridInfo(List<ColumnDescriptor> descriptors, List<GridMenuItemHandler> items,
	    boolean showColumnsHider) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, items, SelectionMode.NONE, showColumnsHider);
    }

    /**
     * @param descriptors
     * @param items
     * @return
     */
    public TabContentDescriptorBuilder withGridInfo(List<ColumnDescriptor> descriptors, List<GridMenuItemHandler> items) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, items, SelectionMode.NONE, true);
    }

    /**
     * @param descriptors
     * @param items
     * @param selectionMode
     * @return
     */
    public TabContentDescriptorBuilder withGridInfo(//
	    List<ColumnDescriptor> descriptors, //
	    List<GridMenuItemHandler> items, //
	    SelectionMode selectionMode, //
	    boolean showColumnsHider) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, items, selectionMode, showColumnsHider);
    }

    /**
     * @param descriptors
     * @param items
     * @param selectionMode
     * @return
     */
    public TabContentDescriptorBuilder withGridInfo(//
	    List<ColumnDescriptor> descriptors, //
	    List<GridMenuItemHandler> items, //
	    SelectionMode selectionMode) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, items, selectionMode, true);
    }

    /**
     * @param pageSize
     * @param descriptors
     * @param items
     * @param showColumnsHider
     * @return
     */
    public TabContentDescriptorBuilder withGridInfo(//
	    int pageSize, //
	    List<ColumnDescriptor> descriptors, //
	    List<GridMenuItemHandler> items, //
	    boolean showColumnsHider) {

	return withGridInfo(pageSize, descriptors, items, SelectionMode.NONE, showColumnsHider);
    }

    /**
     * @param pageSize
     * @param descriptors
     * @param items
     * @return
     */
    public TabContentDescriptorBuilder withGridInfo(//
	    int pageSize, //
	    List<ColumnDescriptor> descriptors, //
	    List<GridMenuItemHandler> items) {

	return withGridInfo(pageSize, descriptors, items, SelectionMode.NONE, true);
    }

    /**
     * @param pageSize
     * @param descriptors
     * @param items
     * @param selectionMode
     * @return
     */
    public TabContentDescriptorBuilder withGridInfo(//
	    int pageSize, //
	    List<ColumnDescriptor> descriptors, //
	    List<GridMenuItemHandler> items, //
	    SelectionMode selectionMode, //
	    boolean showColumnsHider) {

	GridInfo gridInfo = new GridInfo();
	gridInfo.setPageSize(pageSize);
	gridInfo.setSelectionMode(selectionMode);
	gridInfo.setShowColumnsHider(showColumnsHider);

	descriptors.forEach(gridInfo::addColumnDescriptor);
	items.forEach(gridInfo::addGridMenuItemHandler);

	descriptor.setGridInfo(gridInfo);

	return this;
    }

    /**
     *
     */
    public TabContentDescriptorBuilder reloadable() {

	descriptor.setReloadable(true);

	return this;
    }

    /**
     * @param reloader
     * @return
     */
    public TabContentDescriptorBuilder reloadable(Runnable reloader) {

	descriptor.setReloadable(true, reloader);

	return this;
    }

    /**
     * @return
     */
    public TabContentDescriptor build() {

	return descriptor;
    }

}
