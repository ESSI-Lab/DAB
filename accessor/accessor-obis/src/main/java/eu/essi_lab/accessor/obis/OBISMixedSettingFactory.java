package eu.essi_lab.accessor.obis;

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

import eu.essi_lab.accessor.obis.distributed.OBISGranulesConnectorSetting;
import eu.essi_lab.accessor.obis.distributed.OBISMixedDistributedAccessor;
import eu.essi_lab.accessor.obis.harvested.OBISConnectorSetting;
import eu.essi_lab.accessor.obis.harvested.OBISMixedHarvestedAccessor;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;

/**
 * @author Fabrizio
 */
public class OBISMixedSettingFactory {

    /**
     * @return
     */
    public static AccessorSetting createMixedSetting() {

	AccessorSetting accessorSetting = AccessorSetting.createMixed( //
		"OBIS", //
		"http://api.iobis.org", //
		"OBIS Accessor", //
		OBISMixedHarvestedAccessor.TYPE, //
		OBISMixedDistributedAccessor.TYPE, //
		"OBIS",//
		new OBISConnectorSetting(), //
		new OBISGranulesConnectorSetting() //
	);//

	return accessorSetting;
    }
}
