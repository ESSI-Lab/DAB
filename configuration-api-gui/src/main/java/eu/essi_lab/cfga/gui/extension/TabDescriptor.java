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

import com.vaadin.flow.component.*;
import eu.essi_lab.cfga.gui.components.grid.*;
import eu.essi_lab.cfga.gui.extension.directive.*;
import eu.essi_lab.cfga.setting.*;

import java.util.*;

/**
 *
 * @author Fabrizio
 *
 */
public class TabDescriptor {

    private DirectiveManager directiveManager;
    private GridInfo gridInfo;
    private boolean reloadable;
    private Runnable tabReloadHandler;
    private Component content;
    private String label;
    private Class<? extends Setting> settingClass;

    /**
     *
     */
    TabDescriptor() {

	directiveManager = new DirectiveManager();
    }

    /**
     *
     * @return
     */
    public String getLabel() {

	return label;
    }

    /**
     *
     * @param label
     */
    public void setLabel(String label) {

	this.label = label;
    }

    /**
     * @return the directiveManager
     */
    public DirectiveManager getDirectiveManager() {

	return directiveManager;
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
     *
     * @return
     */
    public Optional<Class<? extends Setting>> getSettingClass() {

	return Optional.ofNullable(settingClass);
    }

    /**
     *
     * @param settingClass
     */
    public void setSettingClass(Class<? extends Setting> settingClass) {

	this.settingClass = settingClass;
    }

    /**
     * @return the component
     */
    public Optional<Component> getContent() {

	return Optional.ofNullable(content);
    }

    /**
     * @param content
     */
    public void setContent(Component content) {

	this.content = content;
    }
}
