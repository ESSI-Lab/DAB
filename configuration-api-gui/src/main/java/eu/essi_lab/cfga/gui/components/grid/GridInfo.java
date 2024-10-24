package eu.essi_lab.cfga.gui.components.grid;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

/**
 * @author Fabrizio
 */
public class GridInfo {

    /**
     * 
     */
    private static final int DEFAULT_PAGE_SIZE = 15;

    private int pageSize;

    private List<ColumnDescriptor> descriptors;

    private List<ContextMenuItem> items;

    /**
     * 
     */
    public GridInfo() {

	setPageSize(DEFAULT_PAGE_SIZE);

	descriptors = new ArrayList<>();

	items = new ArrayList<>();
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
     * @param item
     */
    public void addContextMenuItem(ContextMenuItem item) {

	items.add(item);
    }

    /**
     * @return the formatter
     */
    public List<ContextMenuItem> getContextMenuItems() {

	return items;
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

}
