package eu.essi_lab.harvester.worker;

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

import java.util.List;

import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.AccessorFactory.LookupPolicy;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;

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
	harvested.sort((a1, a2) -> a1.getSetting().getName().compareTo(a2.getSetting().getName()));

	harvested.forEach(h -> {

	    AccessorSetting setting = SettingUtils.downCast(h.getSetting(), AccessorSetting.class, true);
	    setting.setIdentifier(setting.getAccessorType());
	    addSetting(setting);
	});

	//
	// selects the first, because one must be selected
	//
	getSettings().//
		stream().//
		sorted((s1, s2) -> s1.getName().compareTo(s2.getName())).//
		findFirst().//
		get().//
		setSelected(true);

    }
}
