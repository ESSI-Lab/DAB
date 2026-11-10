package eu.essi_lab.accessor.ththaiwater;

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
 * A ThaiWater time series: a station (or reservoir) combined with one of its observed properties, described by the entry of the
 * station in the list of the latest observations
 *
 * @author boldrini
 */
public class ThaiWaterEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public ThaiWaterEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public ThaiWaterEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "tele_waterlevel|737|WATER_LEVEL"
     */
    public String getSeriesId() {

	return getObject().optString(ThaiWaterClient.SERIES_ID, null);
    }

    /**
     * @return the ThaiWater identifier of the station, e.g. "737"
     */
    public String getStationId() {

	return getObject().optString(ThaiWaterClient.STATION_ID, null);
    }

    /**
     * @return the type of the station and its identifier, unique among all the stations, e.g. "tele_waterlevel|737"
     */
    public String getStationKey() {

	return getStationType().map(ThaiWaterStationType::getCode).orElse("") + ThaiWaterClient.SERIES_ID_SEPARATOR + getStationId();
    }

    /**
     * @return the station code of the agency, e.g. "C.36", or an empty string if not set
     */
    public String getStationCode() {

	return getString(ThaiWaterClient.STATION_CODE);
    }

    /**
     * @return the station name, possibly including the station code, e.g. "Tha Chedi Temple (TTC06)"
     */
    public String getStationName() {

	String name = getString(ThaiWaterClient.STATION_NAME);
	if (name.isEmpty()) {
	    name = getStationCode().isEmpty() ? getStationId() : getStationCode();
	}

	String code = getStationCode();
	if (!code.isEmpty() && !name.contains(code)) {
	    name += " (" + code + ")";
	}

	return name;
    }

    /**
     * @return
     */
    public Optional<ThaiWaterStationType> getStationType() {

	return ThaiWaterStationType.decode(getObject().optString(ThaiWaterClient.STATION_TYPE, null));
    }

    /**
     * @return
     */
    public Optional<ThaiWaterParameter> getParameter() {

	return ThaiWaterParameter.decode(getObject().optString(ThaiWaterClient.PARAMETER, null));
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
