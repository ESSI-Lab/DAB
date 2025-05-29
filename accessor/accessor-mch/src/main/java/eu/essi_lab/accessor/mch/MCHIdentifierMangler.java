package eu.essi_lab.accessor.mch;

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

import eu.essi_lab.lib.utils.KVPMangler;

/**
 * 
 * @author boldrini
 */
public class MCHIdentifierMangler extends KVPMangler {
    private static final String STATION_ID_KEY = "station";
    private static final String VARIABLE_NAME_KEY = "variable";
    private static final String RESOLUTION_KEY = "resolution"; // DAILY / DETAILED

    public MCHIdentifierMangler() {
	super(";");
    }

    public void setStationId(String stationId) {
	setParameter(STATION_ID_KEY, stationId);
    }

    public String getStationId() {
	return getParameterValue(STATION_ID_KEY);
    }

    public void setVariableName(String variableName) {
	setParameter(VARIABLE_NAME_KEY, variableName);
    }

    public String getVariableName() {
	return getParameterValue(VARIABLE_NAME_KEY);
    }
    
    public void setResolution(String resolution) {
	setParameter(RESOLUTION_KEY, resolution);
    }

    public String getResolution() {
	return getParameterValue(RESOLUTION_KEY);
    }

}
