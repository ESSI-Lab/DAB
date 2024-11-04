package eu.essi_lab.cfga.gs.setting.harvesting.menuitems;

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
import java.util.Optional;

import com.vaadin.flow.component.grid.contextmenu.GridContextMenu.GridContextMenuItemClickEvent;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gui.components.grid.ContextMenuItem;
import eu.essi_lab.cfga.gui.components.setting.edit_put.SettingEditDialog;

/**
 * @author Fabrizio
 */
public class HarvestingSettingEditorMenuItem implements ContextMenuItem {

    @Override
    public void onClick(GridContextMenuItemClickEvent<HashMap<String, String>> event, HashMap<String, Boolean> selected) {

	Optional<HashMap<String, String>> item = event.getItem();

	String settingId = item.get().get("identifier");

	HarvestingSetting setting = ConfigurationWrapper.getHarvestingSettings().//
		stream().//
		filter(s -> s.getIdentifier().equals(settingId)).//		
		findFirst().//
		get();

	new SettingEditDialog(ConfigurationWrapper.getConfiguration().get(), setting).open();
    }

    @Override
    public String getItemText() {

	return "Edit setting";
    }
}
