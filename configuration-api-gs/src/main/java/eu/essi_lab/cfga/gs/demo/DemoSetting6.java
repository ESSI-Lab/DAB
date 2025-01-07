package eu.essi_lab.cfga.gs.demo;

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

import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.AfterCleanFunction;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class DemoSetting6 extends Setting {

    public DemoSetting6() {

	setName("Demo setting 6");

	setCanBeRemoved(true);
	setEditable(true);
	setCanBeDisabled(true);

	setDescription("A setting with afterClean function which hides the header and adds a third option");

	//
	//
	//

	Option<String> option1 = StringOptionBuilder.get().//
		withKey("demo6Option1").//
		withLabel("Option 1").//
		withDescription("String option 1").//
		withValue("Value 1").//
		build();

	addOption(option1);

	//
	//
	//

	Option<String> option2 = StringOptionBuilder.get().//
		withKey("demo6Option2").//
		withLabel("Option 2").//
		withDescription("String option 2").//
		withValue("Value 2").//
		build();

	addOption(option2);

	//
	// set the component extension
	//
	setExtension(new DemoSetting6ComponentInfo());

	//
	// set the onClean function
	//
	setAfterCleanFunction(new Demo6AfterCleanFunction());
    }

    /**
     * @author Fabrizio
     */
    public static class Demo6AfterCleanFunction implements AfterCleanFunction {

	@Override
	public void afterClean(Setting setting) {

	    setting.setShowHeader(false);

	    Option<String> option3 = StringOptionBuilder.get().//
		    withKey("demo6Option3").//
		    withLabel("Option 3").//
		    withDescription("String option 3").//
		    withValue("Value 3").//
		    build();

	    setting.addOption(option3);
	}
    }

    /**
     * @author Fabrizio
     */
    public static class DemoSetting6ComponentInfo extends ComponentInfo {

	/**
	 * 
	 */
	public DemoSetting6ComponentInfo() {

	    setComponentName(DemoSetting6.class.getName());

	    setForceReadOnly(false);

	    TabInfo tabInfo = TabInfoBuilder.get().//
		    withIndex(5).//
		    withShowDirective("Demo setting 6").//
		    build();

	    setTabInfo(tabInfo);
	}
    }
}
