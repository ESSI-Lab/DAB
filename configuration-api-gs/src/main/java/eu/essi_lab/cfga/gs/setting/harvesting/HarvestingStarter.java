package eu.essi_lab.cfga.gs.setting.harvesting;

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
import java.util.Optional;

import com.vaadin.flow.component.grid.contextmenu.GridContextMenu.GridContextMenuItemClickEvent;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.TaskStarter;
import eu.essi_lab.cfga.gui.components.tabs.TabContent;
import eu.essi_lab.cfga.gui.dialog.*;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.lib.utils.*;
import org.jspecify.annotations.*;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class HarvestingStarter extends TaskStarter {

    /**
     *
     */
    private HarvestingSetting harvSetting;

    /**
     *
     */
    public HarvestingStarter() {

    }

    /**
     * @param withTopDivider
     * @param withBottomDivider
     */
    public HarvestingStarter(boolean withTopDivider, boolean withBottomDivider) {

	super(withTopDivider, withBottomDivider);
    }

    @Override
    public void onClick(//
	    GridContextMenuItemClickEvent<HashMap<String, String>> event, //
	    TabContent tabContent, //
	    Configuration configuration, //
	    Optional<Setting> setting, //
	    HashMap<String, Boolean> selection) {

	harvSetting = SettingUtils.downCast(setting.get(), HarvestingSettingLoader.load().getClass());

	boolean runOnceSet = harvSetting.getScheduling().isRunOnceSet();
	boolean enabled = harvSetting.getScheduling().isEnabled();

	if (!runOnceSet || !enabled) {

	    String message = getMessage(runOnceSet, enabled);

	    ConfirmationDialog dialog = new ConfirmationDialog(message, (evt) -> {

		harvSetting.getScheduling().setEnabled(true);
		harvSetting.getScheduling().setRunOnce();

		configuration.replace(harvSetting);

		try {

		    configuration.flush();

		    super.onClick(event, tabContent, configuration, setting, selection);

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e);

		    NotificationDialog.getErrorDialog("Unable to flush configuration after scheduling change: " + e.getMessage()).open();
		}
	    });

	    dialog.setHeader("Scheduling change required");
	    dialog.setConfirmText("Proceed");
	    dialog.setCancelText("Cancel");
	    dialog.open();

	} else {

	    super.onClick(event, tabContent, configuration, setting, selection);
	}
    }

    /**
     * @param runOnceSet
     * @param enabled
     * @return
     */
    private @NonNull String getMessage(boolean runOnceSet, boolean enabled) {

	Optional<Integer> repeatCount = harvSetting.getScheduling().getRepeatCount();

	boolean indefinitely = harvSetting.getScheduling().isRunIndefinitelySet();

	String currentRun = runOnceSet ? ".\n" : " and set to run " + (indefinitely ? "indefinitely.\n" : repeatCount.get() + " times.\n");

	String message = "Scheduling is " + (enabled ? "enabled" : "disabled") + currentRun;

	message += "To proceed, scheduling will " + (!enabled ? " be enabled and set to run once" : " be set to run once");

	return message;
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

	return harvSetting;
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
