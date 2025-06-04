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

/**
 * @author Fabrizio
 */
public class DemoSetting5 extends Setting {

    public DemoSetting5() {

	setName("Demo setting 5");
	setShowHeader(false);

	setCanBeRemoved(true);
	setEditable(true);
	setCanBeDisabled(true);

	setDescription("A setting with hidden header");

	//
	//
	//

	Option<String> option1 = StringOptionBuilder.get().//
		withKey(UUID.randomUUID().toString()).//
		withLabel("Option 1").//
		withDescription("String option 1").//
		withValue("Value 1").//
		build();

	addOption(option1);

	//
	//
	//

	Option<String> option2 = StringOptionBuilder.get().//
		withKey(UUID.randomUUID().toString()).//
		withLabel("Option 2").//
		withDescription("String option 2").//
		withValue("Value 2").//
		build();

	addOption(option2);

	//
	// set the component extension
	//
	setExtension(new DemoSetting5ComponentInfo());
    }

    /**
     * @author Fabrizio
     */
    public static class DemoSetting5ComponentInfo extends ComponentInfo {

	/**
	 * 
	 */
	public DemoSetting5ComponentInfo() {

	    setComponentName(DemoSetting5.class.getName());

	    setForceReadOnly(false);

	    TabInfo tabInfo = TabInfoBuilder.get().//
		    withIndex(4).//
		    withShowDirective("Demo setting 5").//
		    build();

	    setTabInfo(tabInfo);
	}
    }
}
