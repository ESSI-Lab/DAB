package eu.essi_lab.cfga.gs.setting;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting;

/**
 * @author Fabrizio
 */
public class SchedulerViewSetting extends SchedulerSetting implements Configurable<Setting> {

    public SchedulerViewSetting() {

	//
	// set the rendering extension
	//
	setExtension(new SchedulerSettingComponentInfo());
    }

    @Override
    public String getType() {

	return "SchedulerSystemSetting";
    }

    /**
     * @author Fabrizio
     */
    public static class SchedulerSettingComponentInfo extends ComponentInfo {

	/**
	 * 
	 */
	public SchedulerSettingComponentInfo() {

	    setComponentName(SchedulerSetting.class.getName());

	    TabInfo tabInfo = TabInfoBuilder.get().//
		    withIndex(TabIndex.SCHEDULER_VIEW_SETTING.getIndex()).//
		    withShowDirective("Scheduler").//
		    build();

	    setTabInfo(tabInfo);
	}
    }

    @Override
    public void configure(Setting setting) {

    }

    @Override
    public Setting getSetting() {

	return this;
    }

}
