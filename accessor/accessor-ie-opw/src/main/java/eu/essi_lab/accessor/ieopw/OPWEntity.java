package eu.essi_lab.accessor.ieopw;

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
 * An OPW time series: a station combined with one of its parameters, described by the entry of the station in the map layer of
 * the parameter
 *
 * @author boldrini
 */
public class OPWEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public OPWEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public OPWEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "01041|S"
     */
    public String getSeriesId() {

	return getObject().optString(OPWClient.SERIES_ID, null);
    }

    /**
     * @return the station number, e.g. "01041"
     */
    public String getStationNumber() {

	return getObject().optString(OPWClient.STATION_NO, null);
    }

    /**
     * @return
     */
    public String getStationName() {

	return getObject().optString(OPWClient.STATION_NAME, getStationNumber());
    }

    /**
     * @return
     */
    public Optional<OPWParameter> getParameter() {

	return OPWParameter.decode(getObject().optString(OPWClient.PARAMETER, null));
    }

    /**
     * @return the unit symbol, e.g. "m", or an empty string if not set
     */
    public String getUnit() {

	return getObject().optString(OPWClient.UNIT, "");
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
