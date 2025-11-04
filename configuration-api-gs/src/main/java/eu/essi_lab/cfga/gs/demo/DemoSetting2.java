package eu.essi_lab.cfga.gs.demo;

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

import java.util.UUID;

import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;

/**
 * This setting is intended to be used to test the client functionalities
 * 
 * @author Fabrizio
 */
public class DemoSetting2 extends SchedulerWorkerSetting {

    private final Setting rootNoRenderableSetting;
    private final Setting settingChild1;
    private final Setting settingChild2;

    /**
     * 
     */
    public DemoSetting2() {

	setEditable(false);
	enableCompactMode(false);

	setName("Setting demo");

	//
	// the scheduling group
	//
	setGroup(SchedulingGroup.DEFAULT);

	//
	// Scheduling is disabled and set to run once
	//
	getScheduling().setRunOnce();
	getScheduling().setEnabled(false);

	//
	// Options
	//
	Option<String> mainSettingOption = StringOptionBuilder.get().//
		required().//
		withKey("mainSettingOption").//
		withLabel("The main setting option").//
		withValue("Some value").//
		build();

	addOption(mainSettingOption);

	//
	//
	//
	rootNoRenderableSetting = createNoRenderableMultiSelectItem();

	addSetting(rootNoRenderableSetting);

	settingChild1 = new Setting();
	settingChild1.setCanBeDisabled(true);
	settingChild1.setEditable(true);
	settingChild1.enableCompactMode(false);

	settingChild1.setName("Setting child 1");
	settingChild1.setDescription("Description of setting child 1");
	settingChild1.setIdentifier("settingChild1");
	settingChild1.setSelectionMode(SelectionMode.SINGLE);

	addSetting(settingChild1);

	addSettingsToChild(settingChild1);

	//
	//
	//

	settingChild2 = new Setting();
	settingChild2.setCanBeDisabled(true);
	settingChild2.setEditable(true);
	settingChild2.enableCompactMode(false);

	settingChild2.setName("Setting child 2");
	settingChild2.setDescription("Description of setting child 2");
	settingChild2.setIdentifier("settingChild2");

	Option<String> setting2Option = StringOptionBuilder.//
		get().//
		withDescription("Option description").//
		withKey("setting2Option").//
		withLabel("Setting 2 option").//
		build();

	settingChild2.addOption(setting2Option);

	addSetting(settingChild2);

	//
	// set the component extension
	//
	setExtension(new DemoSetting2ComponentInfo());
    }

    /**
     * @param settingChild1
     */
    private void addSettingsToChild(Setting settingChild1) {

	for (int i = 0; i < 10; i++) {

	    Setting setting = new Setting();
	    setting.setName("Child1 setting #" + i);
	    setting.setDescription("Setting description");
	    setting.enableCompactMode(false);

	    Option<String> option = StringOptionBuilder.//
		    get().//
		    withDescription("Option description").//
		    withKey(UUID.randomUUID().toString()).//
		    withDescription("Description of option #" + i).//
		    withLabel("Option #" + i).//
		    build();

	    setting.addOption(option);

	    settingChild1.addSetting(setting);
	}
    }

    /**
     * A setting which is an item of a parent multi select setting, must provide in order to be shown
     * at least a description or/and one or more options. Both the rootSetting and its first child
     * are empty, no description nor options (reset and disable buttons are not shown for items
     * of a parent multi select setting) so in order to be rendered, an empty div is added replacing the description
     * 
     * @return
     */
    private Setting createNoRenderableMultiSelectItem() {

	Setting rootNoRenderableSetting = new Setting();

	rootNoRenderableSetting.setName("Root setting with no renderable properties");
	rootNoRenderableSetting.setEditable(false);
	rootNoRenderableSetting.setCanBeDisabled(false);
	rootNoRenderableSetting.setSelectionMode(SelectionMode.SINGLE);

	Setting noRenderableSetting = new Setting();
	noRenderableSetting.setName("Setting with no renderable properties");
	// noRenderableSetting.setDescription("TEST TEST TEST");
	noRenderableSetting.setIdentifier("noRenderableSetting");
	noRenderableSetting.setSelected(true);
	noRenderableSetting.setEditable(false);
	noRenderableSetting.setCanBeDisabled(false);
	noRenderableSetting.enableCompactMode(false);

	rootNoRenderableSetting.addSetting(noRenderableSetting);

	//
	// ---
	//

	Setting renderableSetting = new Setting();
	renderableSetting.setIdentifier("renderableSetting");
	renderableSetting.setName("Renderable setting");
	renderableSetting.setDescription("This setting has at least a description to be rendered");
	renderableSetting.setEditable(false);
	renderableSetting.setCanBeDisabled(false);
	renderableSetting.enableCompactMode(false);

	rootNoRenderableSetting.addSetting(renderableSetting);

	return rootNoRenderableSetting;
    }

    /**
     * @author Fabrizio
     */
    public static class DemoSetting2ComponentInfo extends ComponentInfo {

	/**
	 * 
	 */
	public DemoSetting2ComponentInfo() {

	    setComponentName(DemoSetting2.class.getName());

	    setForceReadOnly(false);

	    TabInfo tabInfo = TabInfoBuilder.get().//
		    withIndex(1).//
		    withShowDirective("Demo setting 2").//
		    build();

	    setTabInfo(tabInfo);
	}
    }

    /**
     * 
     */
    public void set_Radio_Radio_Mode() {

	setName("Multi setting test - RADIO - RADIO");

	setSelectionMode(SelectionMode.SINGLE);
	settingChild1.setSelected(true);

	settingChild1.setSelectionMode(SelectionMode.SINGLE);
	settingChild1.getSettings().getFirst().setSelected(true);
    }

    /**
     * 
     */
    public void set_Radio_Check_Mode() {

	setName("Multi setting test - RADIO - CHECK");
	setSelectionMode(SelectionMode.SINGLE);

	//
	//
	//
	settingChild1.setSelectionMode(SelectionMode.MULTI);

	//
	//
	//
	rootNoRenderableSetting.setSelected(true);
	rootNoRenderableSetting.getSettings().getFirst().setSelected(true);
    }

    /**
     * 
     */
    public void set_Check_Check_Mode() {

	setName("Multi setting test - CHECK - CHECK");

	setSelectionMode(SelectionMode.MULTI);
	settingChild1.setSelectionMode(SelectionMode.MULTI);
    }

    /**
     * 
     */
    public void set_Check_Radio_Mode() {

	setName("Multi setting test - CHECK - RADIO");

	setSelectionMode(SelectionMode.MULTI);

	settingChild1.setSelectionMode(SelectionMode.SINGLE);
	settingChild1.getSettings().getFirst().setSelected(true);
    }

}
