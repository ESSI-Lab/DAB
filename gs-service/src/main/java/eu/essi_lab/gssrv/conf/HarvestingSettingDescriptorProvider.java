package eu.essi_lab.gssrv.conf;

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

import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.data.provider.*;
import eu.essi_lab.cfga.gs.setting.harvesting.*;
import eu.essi_lab.cfga.gs.setting.menuitems.*;
import eu.essi_lab.cfga.gui.components.grid.*;
import eu.essi_lab.cfga.gui.components.grid.menuitem.*;
import eu.essi_lab.cfga.gui.components.grid.renderer.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.configuration.*;
import org.json.*;

import java.util.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public class HarvestingSettingDescriptorProvider {

    private final TabContentDescriptor descriptor;

    /**
     *
     */
    public HarvestingSettingDescriptorProvider() {

	descriptor = TabContentDescriptorBuilder.get(HarvestingSettingLoader.load().getSettingClass()).//

		withLabel("Harvesting").//
		withShowDirective("Manage DAB harvested sources. Click \"Reload\" to" + " update the scheduler information",
		SortDirection.ASCENDING).//

		withAddDirective(//
		"ADD",//
		"Add harvested/mixed accessor", //
		"eu.essi_lab.harvester.worker.HarvestingSettingImpl", //
		true).// tab view

		withRemoveDirective("REMOVE", //
		"Remove accessor", //
		true, // allow full removal
		"eu.essi_lab.harvester.worker.HarvestingSettingImpl").//

		withEditDirective("EDIT",  //
		"Edit harvested/mixed accessor",//
		true).// tab view

		withGridInfo(Arrays.asList(//

		ColumnDescriptor.createPositionalDescriptor(), //

		ColumnDescriptor.create("Name", 400, true, true, Setting::getName), //

		ColumnDescriptor.create("Type", 150, true, true, this::getSelectedAccessorType), //

		ColumnDescriptor.create("Source id", 200, true, true, this::getSourceId), //

		ColumnDescriptor.create("Setting id", 200, true, true, Setting::getIdentifier), //

		ColumnDescriptor.create("Comment", 150, true, true, this::getComment), //

		ColumnDescriptor.create("Deployment", 150, true, true, this::getDeployment), //

		ColumnDescriptor.create("Host", 150, true, true, (s) -> SchedulerSupport.getInstance().getJobHostName(s)), //

		ColumnDescriptor.create("Repeat count", 150, true, true, (s) -> SchedulerSupport.getInstance().getRepeatCount(s)), //

		ColumnDescriptor.create("Repeat interval", 150, true, true, (s) -> SchedulerSupport.getInstance().getRepeatInterval(s)),

		ColumnDescriptor.create("Status", 100, true, true, (s) -> SchedulerSupport.getInstance().getJobPhase(s), //

			Comparator.comparing(item -> item.get("Status")), //

			new JobPhaseColumnRenderer()), //

		ColumnDescriptor.create("Fired time", 150, true, true, (s) -> SchedulerSupport.getInstance().getFiredTime(s)), //

		ColumnDescriptor.create("End time", 150, true, true, (s) -> SchedulerSupport.getInstance().getEndTime(s)), //

		ColumnDescriptor.create("El. time (HH:mm:ss)", 170, true, true, (s) -> SchedulerSupport.getInstance().getElapsedTime(s)), //

		ColumnDescriptor.create("Next fire time", 150, true, true, (s) -> SchedulerSupport.getInstance().getNextFireTime(s)), //

		ColumnDescriptor.create("Info", true, true, false, (s) -> SchedulerSupport.getInstance().getAllMessages(s))//

	), getItemsList(), Grid.SelectionMode.MULTI).//

		reloadable(() -> SchedulerSupport.getInstance().update(),  //

		//
		// auto-reload only in non-production mode. this is useful if a harvesting is started when the
		// scheduling is initially disabled and HarvestingStarter enables it
		//
		ExecutionMode.get() == ExecutionMode.MIXED || ExecutionMode.get() == ExecutionMode.LOCAL_PRODUCTION).//

		build();
    }

    /**
     * @return
     */
    public TabContentDescriptor get() {

	return descriptor;
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

	list.add(new HarvestPreviewStarter());

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

		filter(key -> isJSONObject(object, key) && object.getJSONObject(key).has("accessorType")).findFirst().get();
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
