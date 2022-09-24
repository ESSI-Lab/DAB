package eu.essi_lab.cfga.gs.task;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.TaskStarter;
import eu.essi_lab.cfga.gs.setting.EmailSetting;

/**
 * @author Fabrizio
 */
public class CustomTaskStarter extends TaskStarter {

    /**
     * 
     */
    private static final long serialVersionUID = 1657058006141995600L;
    /**
     * 
     */
    private String customTaskName;

    @Override
    public void onClick(GridContextMenuItemClickEvent<HashMap<String, String>> event) {

	Optional<HashMap<String, String>> item = event.getItem();

	customTaskName = item.get().get("Name");

	super.onClick(event);
    }

    @Override
    public String getItemText() {

	return "Execute task";
    }

    @Override
    protected CustomTaskSetting getSetting() {

	CustomTaskSetting setting = ConfigurationWrapper.getCustomTaskSettings().//
		stream().//
		filter(s -> s.getTaskName().equals(customTaskName)).//
		findFirst().//
		get();

	return setting;
    }

    @Override
    protected String getDialogTitle(GridContextMenuItemClickEvent<HashMap<String, String>> event) {

	return "Execute custom task once";
    }

    @Override
    protected String getTextAreaText(GridContextMenuItemClickEvent<HashMap<String, String>> event) {

	final StringBuilder builder = new StringBuilder();
	builder.append("- Selected task: " + customTaskName + "\n\n");
	builder.append("- Click \"Start\"  to execute now and once the selected task");

	Optional<EmailSetting> optEmailSetting = ConfigurationWrapper.getSystemSettings().getEmailSetting();

	CustomTaskSetting setting = (CustomTaskSetting) getSetting();

	List<String> recipients = setting.getEmailRecipients();

	if (optEmailSetting.isPresent() && !recipients.isEmpty()) {

	    builder.append("\n\n- When the task is completed, a notification email will be send to: ");
	    recipients.forEach(r -> builder.append(r + ","));
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
