package eu.essi_lab.accessor.copernicus.dataspace;

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

import eu.essi_lab.accessor.copernicus.dataspace.distributed.CopernicusDataspaceGranulesAccessor;
import eu.essi_lab.accessor.copernicus.dataspace.distributed.CopernicusDataspaceGranulesConnectorSetting;
import eu.essi_lab.accessor.copernicus.dataspace.harvested.CopernicusDataspaceAccessor;
import eu.essi_lab.accessor.copernicus.dataspace.harvested.CopernicusDataspaceConnectorSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;

public class CopernicusDataspaceMixedSettingFactory {

    /**
     * @return
     */
    public static AccessorSetting createMixedSetting() {

	AccessorSetting accessorSetting = AccessorSetting.createMixed( //
		"Copernicus Dataspace", //
		"https://catalogue.dataspace.copernicus.eu/resto/api/collections/describe.xml", //
		"Copernicus Dataspace Accessor", //
		CopernicusDataspaceAccessor.TYPE, //
		CopernicusDataspaceGranulesAccessor.TYPE, //
		"CopernicusDataspace", //
		new CopernicusDataspaceConnectorSetting(), //
		new CopernicusDataspaceGranulesConnectorSetting() //
	);//

	return accessorSetting;
    }
}
