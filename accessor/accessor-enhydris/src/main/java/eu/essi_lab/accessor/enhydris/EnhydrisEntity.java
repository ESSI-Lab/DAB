package eu.essi_lab.accessor.enhydris;

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

import org.json.JSONObject;

import eu.essi_lab.lib.utils.JSONObjectWrapper;

/**
 * An Enhydris time series: a station combined with one of its time series groups (a variable) and one of the time series of the
 * group (e.g. the initial or the checked one)
 *
 * @author boldrini
 */
public class EnhydrisEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public EnhydrisEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public EnhydrisEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "2022/1244/10846"
     */
    public String getSeriesId() {

	return getObject().optString(EnhydrisClient.SERIES_ID, null);
    }

    /**
     * @return
     */
    public int getStationId() {

	return getObject().getInt(EnhydrisClient.STATION_ID);
    }

    /**
     * @return
     */
    public int getGroupId() {

	return getObject().getInt(EnhydrisClient.GROUP_ID);
    }

    /**
     * @return
     */
    public int getTimeseriesId() {

	return getObject().getInt(EnhydrisClient.TIMESERIES_ID);
    }

    /**
     * @return e.g. "Ευρώτας - Eurotas"
     */
    public String getStationName() {

	return getObject().optString(EnhydrisClient.STATION_NAME, String.valueOf(getStationId()));
    }

    /**
     * @return the name of the time series group, e.g. "Mean stage"
     */
    public String getGroupName() {

	return getString(EnhydrisClient.GROUP_NAME);
    }

    /**
     * @return the english description of the variable, e.g. "Stage"
     */
    public String getVariable() {

	return getString(EnhydrisClient.VARIABLE);
    }

    /**
     * @return the unit symbol, e.g. "m"
     */
    public String getUnit() {

	return getString(EnhydrisClient.UNIT);
    }

    /**
     * @return e.g. "Initial"
     */
    public String getTimeseriesType() {

	return getString(EnhydrisClient.TIMESERIES_TYPE);
    }

    /**
     * @return e.g. "Mean" for an aggregated time series
     */
    public String getTimeseriesName() {

	return getString(EnhydrisClient.TIMESERIES_NAME);
    }

    /**
     * @return e.g. "10min"
     */
    public String getTimeStep() {

	return getString(EnhydrisClient.TIME_STEP);
    }

    /**
     * @return the name of the station owner
     */
    public String getOwner() {

	return getString(EnhydrisClient.OWNER);
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
