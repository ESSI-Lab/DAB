package eu.essi_lab.cfga.gui.extension;

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

import java.util.Optional;

import com.vaadin.flow.component.Component;

import eu.essi_lab.cfga.gui.components.grid.GridInfo;
import eu.essi_lab.cfga.gui.extension.directive.DirectiveManager;

/**
 * @author Fabrizio
 */
public class TabDescriptor {

    private int index;
    private DirectiveManager directiveManager;
    private GridInfo gridInfo;
    private boolean reloadable;
    private Runnable tabReloadHandler;
    private Component component;

    /**
     * 
     */
    public TabDescriptor() {

	directiveManager = new DirectiveManager();
    }

    /**
     * @param index
     * @param label
     */
    public TabDescriptor(int index) {

	this.index = index;
    }

    /**
     * @return the directiveManager
     */
    public DirectiveManager getDirectiveManager() {

	return directiveManager;
    }

    /**
     * @return
     */
    public int getIndex() {

	return index;
    }

    /**
     * @param index
     */
    public void setIndex(int index) {

	this.index = index;
    }

    /**
     * @param gridInfo
     */
    public void setGridInfo(GridInfo gridInfo) {

	this.gridInfo = gridInfo;
    }

    /**
     * @return
     */
    public Optional<GridInfo> getGridInfo() {

	return Optional.ofNullable(gridInfo);
    }

    /**
     * @return
     */
    public boolean isReloadable() {

	return reloadable;
    }

    /**
     * @param reloadable
     */
    public void setReloadable(boolean reloadable) {

	this.reloadable = reloadable;
    }

    /**
     * @param reloadable
     * @param reloader
     */
    public void setReloadable(boolean reloadable, Runnable reloader) {

	this.reloadable = reloadable;
	this.tabReloadHandler = reloader;
    }

    /**
     * @return
     */
    public Optional<Runnable> getTabReloader() {

	return Optional.ofNullable(tabReloadHandler);
    }

    
    
    /**
     * @return the component
     */
    public Optional<Component> getComponent() {
	
        return Optional.ofNullable(component);
    }

    /**
     * @param component 
     */
    public void setComponent(Component component) {
	
        this.component = component;
    }

    /**
     * 
     */
    @Override
    public String toString() {

	return "Tab#" + getIndex();
    }

    @Override
    public int hashCode() {

	return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {

	return this.hashCode() == o.hashCode();
    }
}
