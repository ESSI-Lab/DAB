package eu.essi_lab.cfga.gs.task;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.TaskStarter;
import eu.essi_lab.cfga.gs.setting.EmailSetting;
import eu.essi_lab.cfga.gui.components.tabs.TabContent;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class CustomTaskStarter extends TaskStarter {

    /**
     *
     */
    private CustomTaskSetting customTaskSetting;

    /**
     *
     */
    public CustomTaskStarter() {

    }

    /**
     * @param withTopDivider
     * @param withBottomDivider
     */
    public CustomTaskStarter(boolean withTopDivider, boolean withBottomDivider) {

	super(withTopDivider, withBottomDivider);
    }

    @Override
    public void onClick(//
	    GridContextMenuItemClickEvent<HashMap<String, String>> event, //
	    TabContent tabContent, //
	    Configuration configuration, //
	    Optional<Setting> setting, //
	    HashMap<String, Boolean> selection) {

	customTaskSetting = SettingUtils.downCast(setting.get(), CustomTaskSetting.class);

	super.onClick(event, tabContent, configuration, setting, selection);
    }

    @Override
    public String getItemText() {

	return "Execute task";
    }

    @Override
    protected CustomTaskSetting getSetting() {

	return customTaskSetting;
    }

    @Override
    protected String getDialogTitle(GridContextMenuItemClickEvent<HashMap<String, String>> event) {

	return "Execute custom task once";
    }

    @Override
    protected String getTextAreaText(GridContextMenuItemClickEvent<HashMap<String, String>> event) {

	final StringBuilder builder = new StringBuilder();
	builder.append("- Selected task name: ").append(event.getItem().get().get("Name")).append("\n\n");
	String description = event.getItem().get().get("Description");
	if (description != null && !description.isEmpty()) {

	    builder.append("- Selected task decription: ").append(event.getItem().get().get("Description")).append("\n\n");
	}

	builder.append("- Click \"Start\"  to execute now and once the selected task");

	Optional<EmailSetting> optEmailSetting = ConfigurationWrapper.getSystemSettings().getEmailSetting();

	CustomTaskSetting setting = getSetting();

	List<String> recipients = setting.getEmailRecipients();

	if (optEmailSetting.isPresent() && !recipients.isEmpty()) {

	    builder.append("\n\n- When the task is completed, a notification email will be send to: ");
	    recipients.forEach(r -> builder.append(r).append(","));
	    builder.deleteCharAt(builder.length() - 1);
	    builder.append("\n");

	} else if (!setting.getEmailRecipients().isEmpty()) {

	    builder.append("\n! The notification email to the provided recipients cannot be send, since ");
	    builder.append(" the system email settings are not configured !\n\n");
	}

	return builder.toString();
    }

    /**
     * @return
     */
    @Override
    protected String getStartTaskText() {

	return "- Task execution started, please wait...";
    }

    /**
     * @return
     */
    @Override
    protected String getEndTaskText() {

	return "- Task execution ended";
    }
}
