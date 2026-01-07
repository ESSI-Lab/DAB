package eu.essi_lab.cfga.gs.demo;

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

import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class DemoSetting1 extends Setting {

    public DemoSetting1() {

	setName("Demo setting 1");

	setDescription("A 'folded' setting with compacted options and advanced options");

	// setCanBeReset(true);
	enableFoldedMode(true);
	enableCompactMode(true);

	int length = 10;

	for (int i = 0; i < length; i++) {

	    Option<String> option = StringOptionBuilder.//
		    get().//
		    withKey("opt" + i).//
		    withDescription("Description of option " + i).//
		    withLabel("Option " + i).//
		    build();

	    option.setEnabled(i % 2 == 0);

	    if (i > 4) {
		option.setAdvanced(true);
		option.setDescription("Advanced option " + i);
	    }

	    addOption(option);
	}
    }

    /**
     * @author Fabrizio
     */
    public static class DemoSetting1TabDescriptor extends TabDescriptor {

	/**
	 *
	 */
	public DemoSetting1TabDescriptor() {

	    setLabel("Demo setting 1");

	    setForceReadOnly(false);

	    TabContentDescriptor descriptor = TabContentDescriptorBuilder.get(DemoSetting1.class).//
		    build();

	    setIndex(0);
	    addContentDescriptor(descriptor);
	}
    }
}
