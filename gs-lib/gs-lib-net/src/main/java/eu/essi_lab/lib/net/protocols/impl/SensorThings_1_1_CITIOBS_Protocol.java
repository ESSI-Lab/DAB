package eu.essi_lab.lib.net.protocols.impl;

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

import eu.essi_lab.lib.net.protocols.AbstractNetProtocol;
import eu.essi_lab.lib.net.protocols.NetProtocol;

/**
 * @author Fabrizio
 */
public class SensorThings_1_1_CITIOBS_Protocol extends AbstractNetProtocol implements NetProtocol {

    @Override
    public String getDescription() {

	return "SensorThings 1.1 Protocol - CITIOBS Profile";
    }

    @Override
    public String[] getURNs() {

	return new String[] { "urn:www:opengis:net:spec:iot_sensing:1.1:CITIOBS" };
    }

    @Override
    public String getSrvType() {

	return "SensorThings";
    }

    @Override
    public String getSrvVersion() {

	return "1.1";
    }
}
