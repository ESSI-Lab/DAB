package eu.essi_lab.harvester.worker;

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

import java.util.*;

import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.AccessorFactory.LookupPolicy;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.lib.utils.*;

/**
 * @author Fabrizio
 */
public class HarvestedAccessorsSetting extends Setting {

    /**
     * 
     */
    final static String IDENTIFIER = "harvestedAccessorsSetting";

    /**
     * 
     */
    public HarvestedAccessorsSetting() {

	setCanBeDisabled(false);
	setEditable(false);
	setShowHeader(false);
	enableCompactMode(false);

	setName("Accessors");

	setIdentifier(IDENTIFIER);
	setSelectionMode(SelectionMode.SINGLE);

	@SuppressWarnings("rawtypes")
	List<IHarvestedAccessor> harvested = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);
	harvested.sort(Comparator.comparing(a -> a.getSetting().getName()));

	harvested.forEach(h -> {

	    Setting setting = h.getSetting().clone();
	    setting.setIdentifier(setting.getOption("accessorType", String.class).get().getValue());
	    addSetting(setting);
	});

	//
	// selects the first, because one must be selected
	//

	getSettings().//
		parallelStream().//
		min(Comparator.comparing(Setting::getName)).//
		get().//
		setSelected(true);

    }
}
