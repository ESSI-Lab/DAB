package eu.essi_lab.cfga.gui.extension;

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
import eu.essi_lab.cfga.gui.extension.directive.AddDirective;
import eu.essi_lab.cfga.gui.extension.directive.Directive.ConfirmationPolicy;
import eu.essi_lab.cfga.gui.extension.directive.EditDirective;
import eu.essi_lab.cfga.gui.extension.directive.RemoveDirective;
import eu.essi_lab.cfga.gui.extension.directive.ShowDirective;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class TabDescriptorBuilder {

    private final TabDescriptor tabDescriptor;

    /**
     *
     */
    private TabDescriptorBuilder() {

	tabDescriptor = new TabDescriptor();
    }

    /**
     *
     */
    private TabDescriptorBuilder(Class<? extends Setting> settingClass) {

	tabDescriptor = new TabDescriptor();
	tabDescriptor.setSettingClass(settingClass);
    }

    /**
     * @return
     */
    public static TabDescriptorBuilder get() {

	return new TabDescriptorBuilder();
    }

    /**
     * @return
     */
    public static TabDescriptorBuilder get(Class<? extends Setting> settingClass) {

	return new TabDescriptorBuilder(settingClass);
    }

    /**
     * @param settingClass
     * @return
     */
    public TabDescriptorBuilder withSettingClass(Class<? extends Setting> settingClass) {

	tabDescriptor.setSettingClass(settingClass);

	return this;
    }

    /**
     * @param directive
     * @return
     */
    public TabDescriptorBuilder withAddDirective(AddDirective directive) {

	tabDescriptor.getDirectiveManager().add(directive);

	return this;
    }

    /**
     * @param directiveName
     * @param settingClass
     * @return
     */
    public TabDescriptorBuilder withAddDirective(String directiveName, Class<? extends Setting> settingClass) {

	tabDescriptor.getDirectiveManager().add(new AddDirective(directiveName, settingClass));

	return this;
    }

    /**
     * @param directiveName
     * @param settingClass
     * @return
     */
    public TabDescriptorBuilder withAddDirective(String directiveName, String settingClass) {

	tabDescriptor.getDirectiveManager().add(new AddDirective(directiveName, settingClass));

	return this;
    }

    /**
     * @param directive
     * @return
     */
    public TabDescriptorBuilder withRemoveDirective(RemoveDirective directive) {

	tabDescriptor.getDirectiveManager().add(directive);

	return this;
    }

    /**
     * @param name
     * @param allowFullRemoval
     * @param settingClass
     * @return
     */
    public TabDescriptorBuilder withRemoveDirective(String name, boolean allowFullRemoval, Class<? extends Setting> settingClass) {

	tabDescriptor.getDirectiveManager().add(new RemoveDirective(name, allowFullRemoval, settingClass));

	return this;
    }

    /**
     * @param name
     * @param allowFullRemoval
     * @param settingClass
     * @return
     */
    public TabDescriptorBuilder withRemoveDirective(String name, boolean allowFullRemoval, String settingClass) {

	tabDescriptor.getDirectiveManager().add(new RemoveDirective(name, allowFullRemoval, settingClass));

	return this;
    }

    /**
     * @param name
     * @param policy
     * @return
     */
    public TabDescriptorBuilder withEditDirective(String name, ConfirmationPolicy policy) {

	tabDescriptor.getDirectiveManager().add(new EditDirective(name, policy));

	return this;
    }

    /**
     * @param description
     * @return
     */
    public TabDescriptorBuilder withShowDirective(String description) {

	return withShowDirective(description, null);
    }

    /**
     * @param direction
     * @return
     */
    public TabDescriptorBuilder  withShowDirective(SortDirection direction) {

	return withShowDirective(null, direction);
    }

    /**
     * @param description
     * @param direction
     * @return
     */
    public TabDescriptorBuilder withShowDirective(String description, SortDirection direction) {

	ShowDirective showDirective = new ShowDirective();

	if(description != null) {

	    showDirective.setDescription(description);
	}

	if (direction != null) {

	    showDirective.setSortDirection(direction);
	}

	tabDescriptor.getDirectiveManager().add(showDirective);

	return this;
    }

    /**
     * @param Component
     * @return
     */
    public TabDescriptorBuilder withComponent(Component Component) {

	tabDescriptor.setContent(Component);

	return this;
    }

    /**
     * @param descriptors
     * @param showColumnsHider
     * @return
     */
    public TabDescriptorBuilder withGridInfo(List<ColumnDescriptor> descriptors, boolean showColumnsHider) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, new ArrayList<>(), SelectionMode.NONE, showColumnsHider);
    }

    /**
     * @param descriptors
     * @return
     */
    public TabDescriptorBuilder withGridInfo(List<ColumnDescriptor> descriptors) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, new ArrayList<>(), SelectionMode.NONE, true);
    }

    /**
     * @param descriptors
     * @param selectionMode
     * @param showColumnsHider
     * @return
     */
    public TabDescriptorBuilder withGridInfo(List<ColumnDescriptor> descriptors, SelectionMode selectionMode, boolean showColumnsHider) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, new ArrayList<>(), selectionMode, showColumnsHider);
    }

    /**
     * @param descriptors
     * @param selectionMode
     * @return
     */
    public TabDescriptorBuilder withGridInfo(List<ColumnDescriptor> descriptors, SelectionMode selectionMode) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, new ArrayList<>(), selectionMode, true);
    }

    /**
     * @param pageSize
     * @param descriptors
     * @param showColumnsHider
     * @return
     */
    public TabDescriptorBuilder withGridInfo(int pageSize, List<ColumnDescriptor> descriptors, boolean showColumnsHider) {

	return withGridInfo(pageSize, descriptors, new ArrayList<>(), SelectionMode.NONE, showColumnsHider);
    }

    /**
     * @param pageSize
     * @param descriptors
     * @return
     */
    public TabDescriptorBuilder withGridInfo(int pageSize, List<ColumnDescriptor> descriptors) {

	return withGridInfo(pageSize, descriptors, new ArrayList<>(), SelectionMode.NONE, true);
    }

    /**
     * @param pageSize
     * @param descriptors
     * @param selectionMode
     * @param showColumnsHider
     * @return
     */
    public TabDescriptorBuilder withGridInfo(int pageSize, List<ColumnDescriptor> descriptors, SelectionMode selectionMode,
	    boolean showColumnsHider) {

	return withGridInfo(pageSize, descriptors, new ArrayList<>(), selectionMode, showColumnsHider);
    }

    /**
     * @param pageSize
     * @param descriptors
     * @param selectionMode
     * @return
     */
    public TabDescriptorBuilder withGridInfo(int pageSize, List<ColumnDescriptor> descriptors, SelectionMode selectionMode) {

	return withGridInfo(pageSize, descriptors, new ArrayList<>(), selectionMode, true);
    }

    /**
     * @param descriptors
     * @param items
     * @param showColumnsHider
     * @return
     */
    public TabDescriptorBuilder withGridInfo(List<ColumnDescriptor> descriptors, List<GridMenuItemHandler> items,
	    boolean showColumnsHider) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, items, SelectionMode.NONE, showColumnsHider);
    }

    /**
     * @param descriptors
     * @param items
     * @return
     */
    public TabDescriptorBuilder withGridInfo(List<ColumnDescriptor> descriptors, List<GridMenuItemHandler> items) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, items, SelectionMode.NONE, true);
    }

    /**
     * @param descriptors
     * @param items
     * @param selectionMode
     * @return
     */
    public TabDescriptorBuilder withGridInfo(//
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
    public TabDescriptorBuilder withGridInfo(//
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
    public TabDescriptorBuilder withGridInfo(//
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
    public TabDescriptorBuilder withGridInfo(//
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
    public TabDescriptorBuilder withGridInfo(//
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

	tabDescriptor.setGridInfo(gridInfo);

	return this;
    }

    /**
     *
     */
    public TabDescriptorBuilder reloadable() {

	tabDescriptor.setReloadable(true);

	return this;
    }

    /**
     * @param reloader
     * @return
     */
    public TabDescriptorBuilder reloadable(Runnable reloader) {

	tabDescriptor.setReloadable(true, reloader);

	return this;
    }

    /**
     * @return
     */
    public TabDescriptor build() {

	return tabDescriptor;
    }

}
