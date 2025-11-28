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

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import com.vaadin.flow.component.grid.contextmenu.GridContextMenu.GridContextMenuItemClickEvent;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gui.components.tabs.TabContent;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public abstract class GridMenuItemHandler {

    private boolean withTopDivider;
    private boolean withBottomDivider;
    private final String id;

    /**
     * 
     */
    public GridMenuItemHandler() {

	id = UUID.randomUUID().toString();
    }

    /**
     * @param withTopDivider
     */
    public GridMenuItemHandler(boolean withTopDivider, boolean withBottomDivider) {

	this();

	setTopDivider(withTopDivider);
	setBottomDivider(withBottomDivider);
    }

    /**
     * @param event
     */
    public abstract void onClick(//
	    GridContextMenuItemClickEvent<HashMap<String, String>> event, //
	    TabContent tabContent, //
	    Configuration configuration, //
	    Optional<Setting> setting, //
	    HashMap<String, Boolean> selected);

    /**
     * @return
     */
    public abstract String getItemText();

    /**
     * @return
     */
    public boolean isEnabled(//
	    HashMap<String, String> eventItem, //
	    TabContent tabContent, //
	    Configuration configuration, //
	    Setting setting, //
	    HashMap<String, Boolean> selection) {

	return true;
    }

    /**
     * @return
     */
    public boolean isContextual() {

	return true;
    }

    /**
     * @return
     */
    public boolean withBottomDivider() {

	return withBottomDivider;
    }

    /**
     * @param withBottomDivider
     */
    public void setBottomDivider(boolean withBottomDivider) {

	this.withBottomDivider = withBottomDivider;
    }

    /**
     * @param withTopDivider
     */
    public void setTopDivider(boolean withTopDivider) {

	this.withTopDivider = withTopDivider;
    }

    /**
     * @return
     */
    public boolean withTopDivider() {

	return withTopDivider;
    }

    /**
     * @return
     */
    final String getIdentifier() {

	return id;
    }
}
