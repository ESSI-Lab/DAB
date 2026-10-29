package eu.essi_lab.accessor.coideam;

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
 * An IDEAM time series: the observations of a sensor of a station in one of the datasets of {@link IDEAMDataset}, described by
 * the most recent row of the series (station name, location, sensor description, unit), by the extent of the series and by the
 * entry of the station in the national station catalog
 *
 * @author boldrini
 */
public class IDEAMEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public IDEAMEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public IDEAMEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "NIVEL|0021209920|0230"
     */
    public String getSeriesId() {

	return getObject().optString(IDEAMClient.SERIES_ID, null);
    }

    /**
     * @return
     */
    public Optional<IDEAMDataset> getDataset() {

	return IDEAMDataset.decode(getObject().optString(IDEAMClient.DATASET, null));
    }

    /**
     * @return the station code, e.g. "0021209920"
     */
    public String getStationCode() {

	return getObject().optString(IDEAMClient.STATION_CODE, null);
    }

    /**
     * @return the sensor code, e.g. "0230"
     */
    public String getSensorCode() {

	return getObject().optString(IDEAMClient.SENSOR_CODE, null);
    }

    /**
     * @return the station name, e.g. "SANTA ROSITA [21209920]"
     */
    public String getStationName() {

	String name = getString(IDEAMClient.STATION_NAME);
	return name.isEmpty() ? getStationCode() : name;
    }

    /**
     * @return the unit, e.g. "mt", or an empty string if not set
     */
    public String getUnit() {

	return getString(IDEAMClient.UNIT);
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
