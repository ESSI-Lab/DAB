package eu.essi_lab.accessor.esmitecoaforos;

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
 * A time series of the Anuario de Aforos: a station of a basin combined with one of its variables, with the description of the
 * station and the period of the available values
 *
 * @author boldrini
 */
public class MITECOAforosEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public MITECOAforosEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public MITECOAforosEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "EBRO|9001|RIVER_DISCHARGE_DAILY"
     */
    public String getSeriesId() {

	return getObject().optString(MITECOAforosClient.SERIES_ID, null);
    }

    /**
     * @return the basin folder of the archive, e.g. "EBRO"
     */
    public String getBasin() {

	return getObject().optString(MITECOAforosClient.BASIN, null);
    }

    /**
     * @return the station code, e.g. "9001"
     */
    public String getStationId() {

	return getObject().optString(MITECOAforosClient.STATION_ID, null);
    }

    /**
     * @return
     */
    public String getStationName() {

	String name = getString(MITECOAforosClient.STATION_NAME);
	return name.isEmpty() ? getStationId() : name;
    }

    /**
     * @return
     */
    public Optional<MITECOAforosVariable> getVariable() {

	return MITECOAforosVariable.decode(getObject().optString(MITECOAforosClient.VARIABLE, null));
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
