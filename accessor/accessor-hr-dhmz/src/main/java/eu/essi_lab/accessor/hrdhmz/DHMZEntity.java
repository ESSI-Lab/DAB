package eu.essi_lab.accessor.hrdhmz;

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
 * A DHMZ time series: a station combined with one of its data types, either the daily values of the archive or the real time
 * (automatic station) water levels of the last days
 *
 * @author boldrini
 */
public class DHMZEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public DHMZEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public DHMZEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "384|DISCHARGE" or "384|WATER_LEVEL|RT"
     */
    public String getSeriesId() {

	return getObject().optString(DHMZClient.SERIES_ID, null);
    }

    /**
     * @return the internal station key used by the requests, e.g. "384"
     */
    public String getStationKey() {

	return getObject().optString(DHMZClient.STATION_KEY, null);
    }

    /**
     * @return the official station code, e.g. "5001"
     */
    public String getStationCode() {

	return getObject().optString(DHMZClient.STATION_CODE, getStationKey());
    }

    /**
     * @return
     */
    public String getStationName() {

	return getObject().optString(DHMZClient.STATION_NAME, getStationCode());
    }

    /**
     * @return
     */
    public Optional<DHMZProperty> getProperty() {

	return DHMZProperty.decode(getObject().optString(DHMZClient.PROPERTY, null));
    }

    /**
     * @return true if this is the real time series of an automatic station
     */
    public boolean isRealTime() {

	return getObject().optBoolean(DHMZClient.REAL_TIME, false);
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
