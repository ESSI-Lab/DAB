package eu.essi_lab.accessor.datastream;

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

import eu.essi_lab.lib.utils.KVPMangler;

/**
 * Identifier mangler used to build stable identifiers and online resource names
 * for DataStream logical time series.
 *
 * The following components are encoded:
 * <ul>
 * <li>dataset DOI</li>
 * <li>location identifier</li>
 * <li>CharacteristicName</li>
 * </ul>
 */
public class DataStreamIdentifierMangler extends KVPMangler {

    private static final String DOI_KEY = "doi";
    private static final String LOCATION_KEY = "location";
    private static final String CHARACTERISTIC_KEY = "characteristic";

    public DataStreamIdentifierMangler() {
	super(";");
    }

    public void setDoi(String doi) {
	setParameter(DOI_KEY, doi);
    }

    public String getDoi() {
	return getParameterValue(DOI_KEY);
    }

    public void setLocationId(String locationId) {
	setParameter(LOCATION_KEY, locationId);
    }

    public String getLocationId() {
	return getParameterValue(LOCATION_KEY);
    }

    public void setCharacteristicName(String characteristicName) {
	setParameter(CHARACTERISTIC_KEY, characteristicName);
    }

    public String getCharacteristicName() {
	return getParameterValue(CHARACTERISTIC_KEY);
    }
}

