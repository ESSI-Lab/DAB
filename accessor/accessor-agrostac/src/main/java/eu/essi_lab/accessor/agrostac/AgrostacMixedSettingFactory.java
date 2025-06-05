package eu.essi_lab.accessor.agrostac;

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

import eu.essi_lab.accessor.agrostac.distributed.AgrostacGranulesAccessor;
import eu.essi_lab.accessor.agrostac.distributed.AgrostacGranulesConnectorSetting;
import eu.essi_lab.accessor.agrostac.harvested.AgrostacAccessor;
import eu.essi_lab.accessor.agrostac.harvested.AgrostacConnectorSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;

public class AgrostacMixedSettingFactory {

    /**
     * @return
     */
    public static AccessorSetting createMixedSetting() {

	AccessorSetting accessorSetting = AccessorSetting.createMixed( //
		"Agrostac", //
		"https://agrostac-test.containers.wur.nl/agrostac/", //
		"v.1.0",
		"Agrostac Accessor", //
		AgrostacAccessor.TYPE, //
		AgrostacGranulesAccessor.TYPE, //
		"Agrostac", //
		new AgrostacConnectorSetting(), //
		new AgrostacGranulesConnectorSetting() //
	);//

	return accessorSetting;
    }
}
