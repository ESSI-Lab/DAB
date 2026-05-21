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

import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.cfga.gs.setting.accessor.*;
import eu.essi_lab.cfga.gs.setting.augmenter.*;
import eu.essi_lab.cfga.gs.task.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.cfga.setting.scheduling.*;
import org.json.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public abstract class HarvestingSetting extends SchedulerWorkerSetting implements BrokeringSetting {

    private static final String CUSTOM_TASK_SETTING_IDENTIFIER = "customTaskSetting";

    /**
     *
     */
    public HarvestingSetting() {

	setCanBeRemoved(true);
	setCanBeDisabled(false);

	setName("Harvested/mixed accessor settings");

	setValidator(createValidator());

	// the class to configure when the job raises
	//
	setConfigurableType(initConfigurableType());

	//
	// the scheduling group
	//
	setGroup(SchedulingGroup.HARVESTING);

	//
	// Scheduling is disabled and set to run once
	//
	getScheduling().setRunOnce();
	getScheduling().setEnabled(false);

	//
	// Settings with available harvested accessors, only one can be chosen
	// Since they are folded, the user can see a list of accessors names.
	// When one element of the list is selected, the client shows all the setting content
	//
	Setting accessorsSetting = initAccessorsSetting();

	addSetting(accessorsSetting);

	//
	// Setting with available Augmenters, more than one can be chosen
	//
	Setting augmentersSetting = initAugmentersSetting();

	addSetting(augmentersSetting);

	//
	// Custom task setting
	//

	CustomTaskSetting customTaskSetting = new CustomTaskSetting();

	customTaskSetting.setDescription(
		"A customizable, schedulable task which executes the code provided by an implementation of the 'Task' interface. If "
			+ " enabled, it will be executed at harvesting end");

	customTaskSetting.setCanBeDisabled(true);

	customTaskSetting.setEditable(false);

	customTaskSetting.setCanBeRemoved(false);

	customTaskSetting.setIdentifier(CUSTOM_TASK_SETTING_IDENTIFIER);

	customTaskSetting.removeSetting("scheduling");

	customTaskSetting.removeOption("emailRecipients");

	customTaskSetting.setEnabled(false);

	addSetting(customTaskSetting);

	//
	// set the onClean function
	//
	setAfterCleanFunction(new HarvestingSettingAfterCleanFunction());
    }

    /**
     * @author Fabrizio
     */
    public static class HarvestingSettingAfterCleanFunction implements AfterCleanFunction {

	@Override
	public void afterClean(Setting setting) {

	    HarvestingSetting thisSetting = downCast(setting);

	    //
	    //
	    //

	    AccessorSetting selAccessorSetting = thisSetting.getSelectedAccessorSetting();

	    selAccessorSetting.setShowHeader(false);

	    thisSetting.setName(selAccessorSetting.getGSSourceSetting().getSourceLabel());

	    //
	    //
	    //

	    List<AugmenterSetting> augmenterSettings = thisSetting.getSelectedAugmenterSettings();

	    if (augmenterSettings.isEmpty()) {

		thisSetting.getAugmentersSetting().setEnabled(false);

		thisSetting.getAugmentersSetting().setShowHeader(false);
	    }
	}
    }

    /**
     * @param object
     */
    public HarvestingSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public HarvestingSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public Optional<CustomTaskSetting> getCustomTaskSetting() {

	Optional<Setting> setting = getSetting(CUSTOM_TASK_SETTING_IDENTIFIER);

	return setting.map(value -> SettingUtils.downCast(value, CustomTaskSetting.class));

    }

    /**
     * @param predicate
     */
    public void selectAccessorSetting(Predicate<AccessorSetting> predicate) {

	getAccessorsSetting().//
		select(s -> predicate.test(SettingUtils.downCast(s, AccessorSetting.class)));
    }

    /**
     * @return
     */
    public Setting getAccessorsSetting() {

	return getSetting(getAccessorsSettingIdentifier()).get();
    }

    /**
     * @return
     */
    public Setting getAugmentersSetting() {

	return getSetting(getAugmentersSettingIdentifier()).get();
    }

    /**
     * @return
     */
    public List<AugmenterSetting> getSelectedAugmenterSettings() {

	return getAugmentersSetting().//
		getSettings(AugmenterSetting.class, false).//
		stream().//
		filter(Setting::isSelected).//
		collect(Collectors.toList());
    }

    /**
     *
     */
    public void setObject(JSONObject object) {

	super.setObject(object);
    }

    /**
     * @return
     */
    @Override
    public String getWorkerName() {

	return getSelectedAccessorSetting().getGSSourceSetting().getSourceLabel();
    }

    /**
     *
     */
    protected abstract Setting initAugmentersSetting();

    /**
     *
     */
    protected abstract String getAugmentersSettingIdentifier();

    /**
     *
     */
    protected abstract Setting initAccessorsSetting();

    /**
     *
     */
    protected abstract String getAccessorsSettingIdentifier();

    /**
     * @return
     */
    protected abstract String initConfigurableType();

    /**
     * @param setting
     * @return
     */
    private static HarvestingSetting downCast(Setting setting) {

	Class<? extends HarvestingSetting> clazz = HarvestingSettingLoader.load().getClass();

	return SettingUtils.downCast(setting, clazz);
    }
}
