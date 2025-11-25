/**
 * 
 */
package eu.essi_lab.cfga.gs.setting.menuitems;

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
import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.grid.contextmenu.GridContextMenu.GridContextMenuItemClickEvent;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.cfga.gui.components.TabContent;
import eu.essi_lab.cfga.gui.components.grid.GridMenuItemHandler;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class ProfilerStateOnlineItemHandler extends GridMenuItemHandler {

    /**
     * 
     */
    public ProfilerStateOnlineItemHandler() {
    }

    /**
     * @param withTopDivider
     * @param withBottomDivider
     */
    public ProfilerStateOnlineItemHandler(boolean withTopDivider, boolean withBottomDivider) {

	super(withTopDivider, withBottomDivider);
    }

    @Override
    public void onClick(//
	    GridContextMenuItemClickEvent<HashMap<String, String>> event, //
	    TabContent tabContent, //
	    Configuration configuration, //
	    Optional<Setting> setting, //
	    HashMap<String, Boolean> selection) {

	List<ProfilerSetting> settings = configuration.//
		list(ProfilerSetting.class, false).//
		stream().//
		filter(s -> selection.containsKey(s.getIdentifier()) && selection.get(s.getIdentifier())).//

		map(s -> SelectionUtils.resetAndSelect(s, false)).//

		map(s -> (ProfilerSetting) SettingUtils.downCast(s, s.getSettingClass())).//

		peek(s -> {

		    s.setOnline(online());
		    SelectionUtils.deepClean(s);

		}).//

		toList();

	settings.forEach(configuration::replace);

	tabContent.render(true);
    }

    /**
     * @return
     */
    protected boolean online() {

	return true;
    }

    @Override
    public String getItemText() {

	return "Turn online selected profilers";
    }

    @Override
    public boolean isEnabled(//
	    HashMap<String, String> eventItem, //
	    TabContent tabContent, //
	    Configuration configuration, //
	    Setting setting, //
	    HashMap<String, Boolean> selection) {

	return selection.values().stream().anyMatch(v -> v == true);
    }
}
