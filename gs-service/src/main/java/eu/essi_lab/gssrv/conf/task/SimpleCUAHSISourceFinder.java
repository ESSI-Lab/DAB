package eu.essi_lab.gssrv.conf.task;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;

public class SimpleCUAHSISourceFinder extends SourceFinder {

    @Override
    public List<HarvestingSetting> getSources(String endpoint, String identifierPrefix) {
	List<HarvestingSetting> ret = new ArrayList<HarvestingSetting>();
	List<String> augmenterTypes = Arrays.asList(//
		"EasyAccessAugmenter", //
		"WHOSUnitsAugmenter", //
		"WHOSVariableAugmenter");
	HarvestingSetting harvestingSetting = createSetting(//
		"CUAHSIHISServer", //
		Optional.empty(), //
		"wof1", //
		"WOF test 1", //
		"https://hydroportal.cuahsi.org/woftest/cuahsi_1_1.asmx", //
		augmenterTypes);
	ret.add(harvestingSetting);
	HarvestingSetting harvestingSetting2 = createSetting(//
		"CUAHSIHISServer", //
		Optional.empty(), //
		"wof2", //
		"WOF test 2", //
		"https://hydroportal.cuahsi.org/woftest/cuahsi_1_1.asmx", //
		augmenterTypes);
	ret.add(harvestingSetting2);
	return ret;
    }

}
