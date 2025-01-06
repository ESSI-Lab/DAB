package eu.essi_lab.lib.net.protocols.impl;

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

import eu.essi_lab.lib.net.protocols.AbstractNetProtocol;
import eu.essi_lab.lib.net.protocols.NetProtocol;

public class WMTS_1_0_0Protocol extends AbstractNetProtocol implements NetProtocol {

    @Override
    public String getDescription() {

	return "OGC Web Map Tile Service 1.0.0 Protocol";
    }

    @Override
    public String[] getURNs() {

	return new String[] { "urn:ogc:serviceType:WebMapTileService:1.0.0:HTTP", "OGC:WMTS-1.0.0-http-get",
		"OGC:WMTS-1.0.0-http-get-map" };
    }

    @Override
    public String getSrvType() {

	return "WMTS";
    }

    @Override
    public String getSrvVersion() {

	return "1.0.0";
    }

}
