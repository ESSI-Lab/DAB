package eu.essi_lab.cfga.gs.setting.harvesting;

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

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.vaadin.flow.data.provider.SortDirection;

import eu.essi_lab.cfga.gs.GSTabIndex;
import eu.essi_lab.cfga.gs.setting.BrokeringSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.cfga.gs.setting.menuitems.HarvestingInfoItemHandler;
import eu.essi_lab.cfga.gs.setting.menuitems.HarvestingStatsItemHandler;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.gui.components.grid.ColumnDescriptor;
import eu.essi_lab.cfga.gui.components.grid.GridMenuItemHandler;
import eu.essi_lab.cfga.gui.components.grid.menuitem.SettingEditItemHandler;
import eu.essi_lab.cfga.gui.components.grid.menuitem.SettingsRemoveItemHandler;
import eu.essi_lab.cfga.gui.components.grid.renderer.JobPhaseColumnRenderer;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabDescriptor;
import eu.essi_lab.cfga.gui.extension.TabDescriptorBuilder;
import eu.essi_lab.cfga.gui.extension.directive.Directive.ConfirmationPolicy;
import eu.essi_lab.cfga.setting.AfterCleanFunction;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.configuration.ExecutionMode;

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

	    TabDescriptor tabDescriptor = TabDescriptorBuilder.get().//
		    withIndex(GSTabIndex.HARVESTING.getIndex()).//
		    withShowDirective("Harvesting", "With this tab you can handle the DAB brokered sources. Click \"Reload\" to"
		    + "update the scheduler information",SortDirection.ASCENDING).//

		    withAddDirective(//
			    "Add harvested/mixed accessor", //
			    "eu.essi_lab.harvester.worker.HarvestingSettingImpl")
		    .//
		    withRemoveDirective("Remove accessor", true, "eu.essi_lab.harvester.worker.HarvestingSettingImpl").//
		    withEditDirective("Edit accessor", ConfirmationPolicy.ON_WARNINGS).//

		    withGridInfo(Arrays.asList(//

			    ColumnDescriptor.createPositionalDescriptor(), //

			    ColumnDescriptor.create("Name", 400, true, true, Setting::getName), //

			    ColumnDescriptor.create("Type", 150, true, true, this::getSelectedAccessorType), //

			    ColumnDescriptor.create("Source id", 200, true, true, this::getSourceId), //

			    ColumnDescriptor.create("Setting id", 200, true, true, Setting::getIdentifier), //

			    ColumnDescriptor.create("Comment", 150, true, true, this::getComment), //

			    ColumnDescriptor.create("Deployment", 150, true, true, this::getDeployment), //

			    ColumnDescriptor.create("Repeat count", 150, true, true,
				    (s) -> SchedulerSupport.getInstance().getRepeatCount(s)), //

			    ColumnDescriptor.create("Repeat interval", 150, true, true,
				    (s) -> SchedulerSupport.getInstance().getRepeatInterval(s)), //

			    ColumnDescriptor.create("Status", 100, true, true, (s) -> SchedulerSupport.getInstance().getJobPhase(s), //

				    Comparator.comparing(item -> item.get("Status")), //

				    new JobPhaseColumnRenderer()), //

			    ColumnDescriptor.create("Fired time", 150, true, true, (s) -> SchedulerSupport.getInstance().getFiredTime(s)), //

			    ColumnDescriptor.create("End time", 150, true, true, (s) -> SchedulerSupport.getInstance().getEndTime(s)), //

			    ColumnDescriptor.create("El. time (HH:mm:ss)", 170, true, true,
				    (s) -> SchedulerSupport.getInstance().getElapsedTime(s)), //

			    ColumnDescriptor.create("Next fire time", 150, true, true,
				    (s) -> SchedulerSupport.getInstance().getNextFireTime(s)), //

			    // ColumnDescriptor.create("Size", true, true, (s) ->
			    // SchedulerSupport.getInstance().getSize(s),
			    //
			    // (o1, o2) -> {
			    //
			    // String size1 = o1.get("Size");
			    // String size2 = o2.get("Size");
			    //
			    // return Integer.valueOf(SchedulerJobStatus.parse(size1))
			    // .compareTo(Integer.valueOf(SchedulerJobStatus.parse(size2)));
			    // }),

			    ColumnDescriptor.create("Info", true, true, false, (s) -> SchedulerSupport.getInstance().getAllMessages(s))//

		    ), getItemsList(), com.vaadin.flow.component.grid.Grid.SelectionMode.MULTI).//

		    reloadable(() -> SchedulerSupport.getInstance().update()).//

		    build();

	    setTabDescriptor(tabDescriptor);
	}

	/**
	 * @return
	 */
	private List<GridMenuItemHandler> getItemsList() {

	    ArrayList<GridMenuItemHandler> list = new ArrayList<>();

	    list.add(new SettingEditItemHandler());
	    list.add(new HarvestingInfoItemHandler());

	    if (ExecutionMode.get() == ExecutionMode.MIXED || //
		    ExecutionMode.get() == ExecutionMode.LOCAL_PRODUCTION) {

		list.add(new HarvestingStarter());
	    }

	    list.add(new SettingsRemoveItemHandler(true, true));

	    list.add(new HarvestingStatsItemHandler());

	    return list;
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getSourceId(Setting setting) {

	    return getValue(setting, "identifier");
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getDeployment(Setting setting) {

	    return getValue(setting, "sourceDeployment");
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getComment(Setting setting) {

	    return getValue(setting, "sourceComment");
	}

	/**
	 * @param setting
	 * @param field
	 * @return
	 */
	private String getValue(Setting setting, String field) {

	    JSONObject object = setting.getObject().getJSONObject("harvestedAccessorsSetting");

	    JSONObject sourceDeployment = object.keySet().//
		    stream().//
		    filter(key -> isJSONObject(object, key) && object.getJSONObject(key).has("sourceSetting")).//
		    map(key -> object.getJSONObject(key).getJSONObject("sourceSetting").getJSONObject(field)).//
		    findFirst().//
		    get();

	    if (sourceDeployment.has("values")) {

		return sourceDeployment.getJSONArray("values").toList().stream().map(Object::toString).collect(Collectors.joining(","));
	    }

	    return "";
	}

	/**
	 * @param setting
	 * @return
	 */
	private String getSelectedAccessorType(Setting setting) {

	    JSONObject object = setting.getObject().getJSONObject("harvestedAccessorsSetting");

	    return object.keySet().stream().

		    filter(key -> isJSONObject(object, key) && object.getJSONObject(key).has("accessorType")).findFirst()
		    .get();
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
