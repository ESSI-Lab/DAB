package eu.essi_lab.accessor.siarso;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.lib.utils.JSONObjectWrapper;

/**
 * An ARSO time series: a station of the XML of the latest data combined with one of the columns of its tables
 *
 * @author boldrini
 */
public class ARSOEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public ARSOEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public ARSOEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "1060|Vodostaj"
     */
    public String getSeriesId() {

	return getObject().optString(ARSOClient.SERIES_ID, null);
    }

    /**
     * @return the station code, e.g. "1060"
     */
    public String getStationId() {

	return getObject().optString(ARSOClient.STATION_ID, null);
    }

    /**
     * @return the station name, e.g. "Gornja Radgona I (Mura)"
     */
    public String getStationName() {

	String location = getString(ARSOClient.LOCATION);
	String river = getString(ARSOClient.RIVER);

	if (location.isEmpty()) {
	    return river.isEmpty() ? getStationId() : river;
	}

	return river.isEmpty() ? location : location + " (" + river + ")";
    }

    /**
     * @return the name of the table column (in Slovenian), e.g. "Vodostaj"
     */
    public String getParameterName() {

	return getObject().optString(ARSOClient.PARAMETER, null);
    }

    /**
     * @return
     */
    public Optional<ARSOParameter> getParameter() {

	return ARSOParameter.fromColumnName(getParameterName());
    }

    /**
     * @return the unit, e.g. "cm", or an empty string if not set
     */
    public String getUnit() {

	return getString(ARSOClient.UNIT);
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
