package eu.essi_lab.accessor.cmr;

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

import eu.essi_lab.accessor.cmr.cwic.distributed.CWICCMRGranulesAccessor;
import eu.essi_lab.accessor.cmr.cwic.distributed.CWICCMRGranulesConnectorSetting;
import eu.essi_lab.accessor.cmr.cwic.harvested.CWICCMROpensearchAccessor;
import eu.essi_lab.accessor.cmr.cwic.harvested.CWICCMROpensearchConnectorSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;

/**
 * @author Fabrizio
 */
public class CMRCWICMixedSettingFactory {

    /**
     * @return
     */
    public static AccessorSetting createMixedSetting() {

	AccessorSetting accessorSetting = AccessorSetting.createMixed( //
		"CMR CWIC", //
		"https://cmr.earthdata.nasa.gov/opensearch/collections.atom?", //
		"CEOS WGISS Integrated Catalog (CWIC)", //
		CWICCMROpensearchAccessor.TYPE, //
		CWICCMRGranulesAccessor.TYPE, //
		"CMRCWIC", //
		new CWICCMROpensearchConnectorSetting(), //
		new CWICCMRGranulesConnectorSetting() //
	);//

	return accessorSetting;
    }
}
