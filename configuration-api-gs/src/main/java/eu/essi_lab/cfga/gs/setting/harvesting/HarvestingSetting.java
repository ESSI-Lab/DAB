package eu.essi_lab.cfga.gs.setting.harvesting;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.vaadin.flow.data.provider.SortDirection;

import eu.essi_lab.cfga.gs.setting.BrokeringSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.menuitems.HarvestingSettingEditorMenuItem;
import eu.essi_lab.cfga.gs.setting.harvesting.menuitems.HarvestingStatsProviderMenuItem;
import eu.essi_lab.cfga.gs.setting.menuitems.RowValuesFormatterMenuItem;
import eu.essi_lab.cfga.gui.components.grid.ColumnDescriptor;
import eu.essi_lab.cfga.gui.components.grid.ContextMenuItem;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.cfga.gui.extension.directive.Directive.ConfirmationPolicy;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.setting.AfterCleanFunction;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.configuration.GIProjectExecutionMode;

/**
 * @author Fabrizio
 */
public abstract class HarvestingSetting extends SchedulerWorkerSetting implements BrokeringSetting {

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
	// set the component extension
	//
	setExtension(new HarvestingSettingComponentInfo());

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

	    AccessorSetting selecteAccessorSetting = thisSetting.getSelectedAccessorSetting();

	    selecteAccessorSetting.setShowHeader(false);

	    thisSetting.setName(selecteAccessorSetting.getGSSourceSetting().getSourceLabel());

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
     * @author Fabrizio
     */
    public static class HarvestingSettingComponentInfo extends ComponentInfo {

	/**
	 * 
	 */
	public HarvestingSettingComponentInfo() {

	    setComponentName(HarvestingSetting.class.getName());

	    TabInfo tabInfo = TabInfoBuilder.get().//
		    withIndex(2).//
		    withShowDirective("Harvesting", SortDirection.ASCENDING).//

		    withAddDirective(//
			    "Add harvested/mixed accessor", //
			    "eu.essi_lab.harvester.worker.HarvestingSettingImpl")
		    .//
		    withRemoveDirective("Remove accessor", false, "eu.essi_lab.harvester.worker.HarvestingSettingImpl").//
		    withEditDirective("Edit accessor", ConfirmationPolicy.ON_WARNINGS).//

		    withGridInfo(Arrays.asList(//

			    ColumnDescriptor.createPositionalDescriptor(), //

			    ColumnDescriptor.create("Name", true, true, (s) -> s.getName()), //

			    ColumnDescriptor.create("Type", 150, true, true, (s) -> getSelectedAccessorType(s)), //

			    ColumnDescriptor.create("Id", 150, true, true, (s) -> getId(s)), //

			    ColumnDescriptor.create("Comment", 150, true, true, (s) -> getComment(s)), //

			    ColumnDescriptor.create("Repeat (D-h)", 130, true, true,
				    (s) -> SchedulerSupport.getInstance().getRepeatInterval(s)), //

			    ColumnDescriptor.create("Status", 100, true, true, (s) -> SchedulerSupport.getInstance().getJobPhase(s)), //

			    ColumnDescriptor.create("Fired time", 150, true, true, (s) -> SchedulerSupport.getInstance().getFiredTime(s)), //

			    ColumnDescriptor.create("End time", 150, true, true, (s) -> SchedulerSupport.getInstance().getEndTime(s)), //

			    ColumnDescriptor.create("El. time (HH:mm:ss)", 170, true, true,
				    (s) -> SchedulerSupport.getInstance().getElapsedTime(s)), //

			    ColumnDescriptor.create("Next fire time", 150, true, true,
				    (s) -> SchedulerSupport.getInstance().getNextFireTime(s)), //

			    ColumnDescriptor.create("Size", 70, true, true, (s) -> SchedulerSupport.getInstance().getSize(s),

				    (o1, o2) -> {

					String size1 = o1.get("Size");
					String size2 = o2.get("Size");

					return Integer.valueOf(SchedulerJobStatus.parse(size1))
						.compareTo(Integer.valueOf(SchedulerJobStatus.parse(size2)));
				    }), //

			    ColumnDescriptor.create("Info", true, true, false, (s) -> SchedulerSupport.getInstance().getAllMessages(s))//

		    ), getItemsList()).//

		    reloadable(() -> SchedulerSupport.getInstance().update()).//

		    build();

	    setTabInfo(tabInfo);
	}

	/**
	 * @return
	 */
	private List<ContextMenuItem> getItemsList() {

	    ArrayList<ContextMenuItem> list = new ArrayList<>();

	    list.add(new HarvestingSettingEditorMenuItem());
	    list.add(new RowValuesFormatterMenuItem());
	    list.add(new HarvestingStatsProviderMenuItem());

	    if (GIProjectExecutionMode.getMode() == ExecutionMode.MIXED || //
		    GIProjectExecutionMode.getMode() == ExecutionMode.LOCAL_PRODUCTION) {

		list.add(new HarvestingStarter());
	    }

	    return list;
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getId(Setting setting) {

	    JSONObject object = setting.getObject().getJSONObject("harvestedAccessorsSetting");

	    return object.keySet().//
		    stream().//
		    filter(key -> isJSONObject(object, key) && object.getJSONObject(key).has("sourceSetting")).//
		    map(key -> object.getJSONObject(key).getJSONObject("sourceSetting").getJSONObject("identifier")).//
		    findFirst().//
		    get().getJSONArray("values").getString(0);
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getComment(Setting setting) {

	    JSONObject object = setting.getObject().getJSONObject("harvestedAccessorsSetting");

	    JSONObject sourceComment = object.keySet().//
		    stream().//
		    filter(key -> isJSONObject(object, key) && object.getJSONObject(key).has("sourceSetting")).//
		    map(key -> object.getJSONObject(key).getJSONObject("sourceSetting").getJSONObject("sourceComment")).//
		    findFirst().//
		    get();

	    if (sourceComment.has("values")) {

		return sourceComment.getJSONArray("values").getString(0);
	    }

	    return "";
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getSelectedAccessorType(Setting setting) {

	    JSONObject object = setting.getObject().getJSONObject("harvestedAccessorsSetting");

	    String accessorType = object.keySet().stream().

		    filter(key -> isJSONObject(object, key) && object.getJSONObject(key).has("accessorType")).map(key -> key).findFirst()
		    .get();

	    return accessorType;
	}

	/**
	 * @param object
	 * @param key
	 * @return
	 */
	private static boolean isJSONObject(JSONObject object, String key) {

	    try {
		object.getJSONObject(key);
	    } catch (org.json.JSONException ex) {
		return false;
	    }

	    return true;
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
		filter(s -> s.isSelected()).//
		collect(Collectors.toList());
    }

    /**
     * 
     */
    public void setObject(JSONObject object) {

	super.setObject(object);
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

	HarvestingSetting harvSetting = SettingUtils.downCast(setting, clazz);

	return harvSetting;
    }
}
