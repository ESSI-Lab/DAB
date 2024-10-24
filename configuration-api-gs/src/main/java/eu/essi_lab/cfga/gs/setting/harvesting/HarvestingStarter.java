package eu.essi_lab.cfga.gs.setting.harvesting;

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
import eu.essi_lab.cfga.gs.TaskStarter;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class HarvestingStarter extends TaskStarter {

    /**
     * 
     */
    private String settingId;

    @Override
    public void onClick(GridContextMenuItemClickEvent<HashMap<String, String>> event) {

	Optional<HashMap<String, String>> item = event.getItem();

	settingId = item.get().get("Setting id");

	super.onClick(event);
    }

    @Override
    public String getItemText() {

	return "Start harvesting";
    }

    /**
     * @return
     */
    @Override
    protected SchedulerWorkerSetting getSetting() {

	HarvestingSetting setting = ConfigurationWrapper.getHarvestingSettings().//
		stream().//
		filter(s -> s.getIdentifier().equals(settingId)).//
		findFirst().//
		get();

	return setting;
    }

    @Override
    protected String getDialogTitle(GridContextMenuItemClickEvent<HashMap<String, String>> event) {

	return "Start harvesting once";
    }

    @Override
    protected String getTextAreaText(GridContextMenuItemClickEvent<HashMap<String, String>> event) {

	String uri = ConfigurationWrapper.getDatabaseSetting().getDatabaseUri();

	Optional<HashMap<String, String>> item = event.getItem();

	String sourceName = item.get().get("Name");

	String text = "- Current database uri: " + uri + "\n";
	text += "- Selected source: " + sourceName + "\n\n";
	text += "- Click \"Start\"  to harvest now and once the selected source";

	return text;
    }

    /**
     * @return
     */
    protected String getStartTaskText() {

	return "- Harvesting started, please wait...";
    }

    /**
     * @return
     */
    protected String getEndTaskText() {

	return "- Harvesting ended";
    }

}
