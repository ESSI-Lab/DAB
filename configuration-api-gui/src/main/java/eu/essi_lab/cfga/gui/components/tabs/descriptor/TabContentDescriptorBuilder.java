package eu.essi_lab.cfga.gui.components.tabs.descriptor;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.grid.Grid.*;
import com.vaadin.flow.data.provider.*;
import eu.essi_lab.cfga.gui.components.grid.*;
import eu.essi_lab.cfga.gui.directive.*;
import eu.essi_lab.cfga.gui.directive.Directive.*;
import eu.essi_lab.cfga.setting.*;

import java.util.*;

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
     * @param directive
     * @return
     */
    public TabContentDescriptorBuilder withCustomAddDirective(CustomAddDirective directive) {

	descriptor.getDirectiveManager().add(directive);

	return this;
    }

    /**
     *
     * @param name
     * @param listener
     * @return
     */
    public TabContentDescriptorBuilder withCustomAddDirective(ComponentEventListener<ClickEvent<Button>> listener) {

	descriptor.getDirectiveManager().add(new  CustomAddDirective(listener));

	return this;
    }

    /**
     *
     * @param name
     * @param listener
     * @return
     */
    public TabContentDescriptorBuilder withCustomAddDirective(String name, ComponentEventListener<ClickEvent<Button>> listener) {

	descriptor.getDirectiveManager().add(new  CustomAddDirective(name, listener));

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
