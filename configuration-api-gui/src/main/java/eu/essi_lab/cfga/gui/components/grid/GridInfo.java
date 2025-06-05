package eu.essi_lab.cfga.gui.components.grid;

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
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;

/**
 * @author Fabrizio
 */
public class GridInfo {

    /**
     * 
     */
    public static final int DEFAULT_PAGE_SIZE = 15;

    private int pageSize;

    private List<ColumnDescriptor> descriptors;

    private List<GridMenuItemHandler> handlers;

    private Grid.SelectionMode selectionMode;

    private boolean showColumnsHider;

    /**
     * 
     */
    public GridInfo() {

	setPageSize(DEFAULT_PAGE_SIZE);

	setSelectionMode(SelectionMode.NONE);

	descriptors = new ArrayList<>();

	handlers = new ArrayList<>();
    }

    /**
     * @return
     */
    public Grid.SelectionMode getSelectionMode() {

	return selectionMode;
    }

    /**
     * @param selectionMode
     */
    public void setSelectionMode(Grid.SelectionMode selectionMode) {

	this.selectionMode = selectionMode;
    }

    /**
     * @param descriptor
     */
    public void addColumnDescriptor(ColumnDescriptor descriptor) {

	descriptors.add(descriptor);
    }

    /**
     * @return
     */
    public List<ColumnDescriptor> getColumnsDescriptors() {

	return descriptors;
    }

    /**
     * @param handler
     */
    public void addGridMenuItemHandler(GridMenuItemHandler handler) {

	handlers.add(handler);
    }

    /**
     * @return
     */
    public List<GridMenuItemHandler> getGridMenuItemHandlers() {

	return handlers;
    }

    /**
     * @return
     */
    public int getPageSize() {

	return pageSize;
    }

    /**
     * @param pageSize
     */
    public void setPageSize(int pageSize) {

	this.pageSize = pageSize;
    }

    /**
     * @return
     */
    public boolean isShowColumnsHider() {

	return showColumnsHider;
    }

    /**
     * @param showColumnsHider
     */
    public void setShowColumnsHider(boolean showColumnsHider) {

	this.showColumnsHider = showColumnsHider;
    }

}
