package eu.essi_lab.accessor.wcs_1_0_0_TDS;

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

import eu.essi_lab.accessor.wcs.WCSConnector;
import eu.essi_lab.accessor.wcs_1_0_0.WCSMapper_100;
import eu.essi_lab.lib.net.protocols.NetProtocols;

public class WCSMapper_100_TDS extends WCSMapper_100 {
    @Override
    protected String getWCSProtocol() {

	return NetProtocols.WCS_1_0_0_TDS.getCommonURN();
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return WCSConnector.WCS_SCHEME + WCSConnector_100_TDS.class.getSimpleName();
    }
}
