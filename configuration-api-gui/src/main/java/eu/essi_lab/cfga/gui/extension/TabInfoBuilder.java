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
public class TabInfoBuilder {

    private TabInfo tabInfo;

    /**
    * 
    */
    public TabInfoBuilder() {

	tabInfo = new TabInfo();
    }

    /**
     * @return
     */
    public static TabInfoBuilder get() {

	return new TabInfoBuilder();
    }

    /**
     * @param index
     * @return
     */
    public TabInfoBuilder withIndex(int index) {

	tabInfo.setIndex(index);

	return this;
    }

    /**
     * @param directive
     * @return
     */
    public TabInfoBuilder withAddDirective(AddDirective directive) {

	tabInfo.getDirectiveManager().addAddDirective(directive);

	return this;
    }

    /**
     * @param directiveName
     * @param settingClass
     * @return
     */
    public TabInfoBuilder withAddDirective(String directiveName, Class<? extends Setting> settingClass) {

	tabInfo.getDirectiveManager().addAddDirective(new AddDirective(directiveName, settingClass));

	return this;
    }

    /**
     * @param directiveName
     * @param settingClass
     * @return
     */
    public TabInfoBuilder withAddDirective(String directiveName, String settingClass) {

	tabInfo.getDirectiveManager().addAddDirective(new AddDirective(directiveName, settingClass));

	return this;
    }

    /**
     * @param directive
     * @return
     */
    public TabInfoBuilder withRemoveDirective(RemoveDirective directive) {

	tabInfo.getDirectiveManager().addRemoveDirective(directive);

	return this;
    }

    /**
     * @param name
     * @param allowFullRemoval
     * @param settingClass
     * @return
     */
    public TabInfoBuilder withRemoveDirective(String name, boolean allowFullRemoval, Class<? extends Setting> settingClass) {

	tabInfo.getDirectiveManager().addRemoveDirective(new RemoveDirective(name, allowFullRemoval, settingClass));

	return this;
    }

    /**
     * @param name
     * @param allowFullRemoval
     * @param settingClass
     * @return
     */
    public TabInfoBuilder withRemoveDirective(String name, boolean allowFullRemoval, String settingClass) {

	tabInfo.getDirectiveManager().addRemoveDirective(new RemoveDirective(name, allowFullRemoval, settingClass));

	return this;
    }

    /**
     * @param name
     * @param policy
     * @return
     */
    public TabInfoBuilder withEditDirective(String name, ConfirmationPolicy policy) {

	tabInfo.getDirectiveManager().addEditDirective(new EditDirective(name, policy));

	return this;
    }

    /**
     * @param name
     * @param direction
     * @return
     */
    public TabInfoBuilder withShowDirective(String name, SortDirection direction) {

	tabInfo.getDirectiveManager().addShowDirective(new ShowDirective(name, direction));

	return this;
    }

    /**
     * @param name
     * @return
     */
    public TabInfoBuilder withShowDirective(String name) {

	tabInfo.getDirectiveManager().addShowDirective(new ShowDirective(name));

	return this;
    }

    /**
     * @param Component
     * @return
     */
    public TabInfoBuilder withComponent(Component Component) {

	tabInfo.setComponent(Component);

	return this;
    }

    /**
     * @param descriptors
     * @param showColumnsHider
     * @return
     */
    public TabInfoBuilder withGridInfo(List<ColumnDescriptor> descriptors, boolean showColumnsHider) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, new ArrayList<>(), SelectionMode.NONE, showColumnsHider);
    }

    /**
     * @param descriptors
     * @return
     */
    public TabInfoBuilder withGridInfo(List<ColumnDescriptor> descriptors) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, new ArrayList<>(), SelectionMode.NONE, true);
    }

    /**
     * @param descriptors
     * @param selectionMode
     * @param showColumnsHider
     * @return
     */
    public TabInfoBuilder withGridInfo(List<ColumnDescriptor> descriptors, SelectionMode selectionMode, boolean showColumnsHider) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, new ArrayList<>(), selectionMode, showColumnsHider);
    }

    /**
     * @param descriptors
     * @param selectionMode
     * @return
     */
    public TabInfoBuilder withGridInfo(List<ColumnDescriptor> descriptors, SelectionMode selectionMode) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, new ArrayList<>(), selectionMode, true);
    }

    /**
     * @param pageSize
     * @param descriptors
     * @param showColumnsHider
     * @return
     */
    public TabInfoBuilder withGridInfo(int pageSize, List<ColumnDescriptor> descriptors, boolean showColumnsHider) {

	return withGridInfo(pageSize, descriptors, new ArrayList<>(), SelectionMode.NONE, showColumnsHider);
    }

    /**
     * @param pageSize
     * @param descriptors
     * @return
     */
    public TabInfoBuilder withGridInfo(int pageSize, List<ColumnDescriptor> descriptors) {

	return withGridInfo(pageSize, descriptors, new ArrayList<>(), SelectionMode.NONE, true);
    }

    /**
     * @param pageSize
     * @param descriptors
     * @param selectionMode
     * @param showColumnsHider
     * @return
     */
    public TabInfoBuilder withGridInfo(int pageSize, List<ColumnDescriptor> descriptors, SelectionMode selectionMode,
	    boolean showColumnsHider) {

	return withGridInfo(pageSize, descriptors, new ArrayList<>(), selectionMode, showColumnsHider);
    }

    /**
     * @param pageSize
     * @param descriptors
     * @param selectionMode
     * @return
     */
    public TabInfoBuilder withGridInfo(int pageSize, List<ColumnDescriptor> descriptors, SelectionMode selectionMode) {

	return withGridInfo(pageSize, descriptors, new ArrayList<>(), selectionMode, true);
    }

    /**
     * @param descriptors
     * @param items
     * @param showColumnsHider
     * @return
     */
    public TabInfoBuilder withGridInfo(List<ColumnDescriptor> descriptors, List<GridMenuItemHandler> items, boolean showColumnsHider) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, items, SelectionMode.NONE, showColumnsHider);
    }

    /**
     * @param descriptors
     * @param items
     * @return
     */
    public TabInfoBuilder withGridInfo(List<ColumnDescriptor> descriptors, List<GridMenuItemHandler> items) {

	return withGridInfo(GridInfo.DEFAULT_PAGE_SIZE, descriptors, items, SelectionMode.NONE, true);
    }

    /**
     * @param descriptors
     * @param items
     * @param selectionMode
     * @return
     */
    public TabInfoBuilder withGridInfo(//
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
    public TabInfoBuilder withGridInfo(//
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
    public TabInfoBuilder withGridInfo(//
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
    public TabInfoBuilder withGridInfo(//
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
    public TabInfoBuilder withGridInfo(//
	    int pageSize, //
	    List<ColumnDescriptor> descriptors, //
	    List<GridMenuItemHandler> items, //
	    SelectionMode selectionMode, //
	    boolean showColumnsHider) {

	GridInfo gridInfo = new GridInfo();
	gridInfo.setPageSize(pageSize);
	gridInfo.setSelectionMode(selectionMode);
	gridInfo.setShowColumnsHider(showColumnsHider);

	descriptors.forEach(d -> gridInfo.addColumnDescriptor(d));
	items.forEach(i -> gridInfo.addGridMenuItemHandler(i));

	tabInfo.setGridInfo(gridInfo);

	return this;
    }

    /**
    * 
    */
    public TabInfoBuilder reloadable() {

	tabInfo.setReloadable(true);

	return this;
    }

    /**
     * @param reloader
     * @return
     */
    public TabInfoBuilder reloadable(Runnable reloader) {

	tabInfo.setReloadable(true, reloader);

	return this;
    }

    /**
     * @return
     */
    public TabInfo build() {

	return tabInfo;
    }

}
