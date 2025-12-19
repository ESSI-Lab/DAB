package eu.essi_lab.lib.net.protocols.impl;

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

import eu.essi_lab.lib.net.protocols.AbstractNetProtocol;
import eu.essi_lab.lib.net.protocols.NetProtocol;

public class WCSProtocol extends AbstractNetProtocol implements NetProtocol {

    @Override
    public String getDescription() {

	return "OGC Web Coverage Service Protocol";
    }

    @Override
    public String[] getURNs() {

	return new String[] {"http://www.opengis.net/def/serviceType/ogc/wcs", "urn:ogc:serviceType:WebCoverageService:HTTP", "urn:ogc:serviceType:WebCoverageService", "OGC:WCS" };
    }

    @Override
    public String getSrvType() {

	return "WCS";
    }

    @Override
    public String getSrvVersion() {

	return null;
    }

}
