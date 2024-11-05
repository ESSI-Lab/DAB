/**
 * 
 */
package eu.essi_lab.cfga.gui.components.grid.menuitem;

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

import java.util.HashMap;

import com.vaadin.flow.component.grid.contextmenu.GridContextMenu.GridContextMenuItemClickEvent;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gui.components.TabContainer;
import eu.essi_lab.cfga.gui.components.grid.ContextMenuItem;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class SettingsRemoveMenuItem extends ContextMenuItem {

    /**
     * 
     */
    public SettingsRemoveMenuItem() {

    }

    /**
     * @param withTopDivider
     * @param withBottomDivider
     */
    public SettingsRemoveMenuItem(boolean withTopDivider, boolean withBottomDivider) {

	super(withTopDivider, withBottomDivider);
    }

    @Override
    public void onClick(//
	    GridContextMenuItemClickEvent<HashMap<String, String>> event, //
	    TabContainer tabContainer, //
	    Configuration configuration, //
	    Setting setting, //
	    HashMap<String, Boolean> selection) {

	System.out.println(selection);
    }

    @Override
    public boolean withTopDivider() {

	return true;
    }

    @Override
    public String getItemText() {

	return "Remove selected settings";
    }

}
