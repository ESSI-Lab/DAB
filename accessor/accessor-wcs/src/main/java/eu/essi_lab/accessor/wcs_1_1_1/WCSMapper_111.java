/**
 * 
 */
package eu.essi_lab.accessor.wcs_1_1_1;

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

import eu.essi_lab.accessor.wcs.WCSConnector;
import eu.essi_lab.accessor.wcs_1_1_0.WCSMapper_110;
import eu.essi_lab.lib.net.protocols.NetProtocols;

/**
 * @author Fabrizio
 */
public class WCSMapper_111 extends WCSMapper_110 {

    @Override
    protected String getWCSProtocol() {

	return NetProtocols.WCS_1_1_1.getCommonURN();
    }

    @Override
    protected String getVersion() {

	return "1.1.1";
    }
    
    @Override
    public String getSupportedOriginalMetadataSchema() {
	return WCSConnector.WCS_SCHEME + WCSConnector_111.class.getSimpleName();
    }
}
