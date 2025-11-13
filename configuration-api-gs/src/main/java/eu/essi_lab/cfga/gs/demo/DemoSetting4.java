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

import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabDescriptor;
import eu.essi_lab.cfga.gui.extension.TabDescriptorBuilder;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class DemoSetting4 extends Setting {

    /**
     * @param i 
     * 
     */
    public DemoSetting4(int i) {

	setEditable(false);
	enableCompactMode(false);

	setCanBeRemoved(true);

	setName("Setting demo 4 - #" + i);

	setDescription("A setting that can be removed from the configuration");

	//
	// set the component extension
	//
	setExtension(new DemoSetting4ComponentInfo());
    }

    /**
     * @author Fabrizio
     */
    public static class DemoSetting4ComponentInfo extends ComponentInfo {

	/**
	 * 
	 */
	public DemoSetting4ComponentInfo() {

	    setComponentName(DemoSetting4.class.getName());
	   
	    setForceReadOnly(false);

	    TabDescriptor tabDescriptor = TabDescriptorBuilder.get().//
		    withIndex(3).//
		    withShowDirective("Demo setting 4").//
		    build();

	    setTabInfo(tabDescriptor);
	}
    }
}
