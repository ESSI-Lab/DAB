package eu.essi_lab.accessor.dkvanda;

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
 * A VanDa time series: an examination (examination type and parameter) made at a measurement point of a station, described by
 * the station list
 *
 * @author boldrini
 */
public class VanDaEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public VanDaEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public VanDaEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "21006844|1|25|1233"
     */
    public String getSeriesId() {

	return getObject().optString(VanDaClient.SERIES_ID, null);
    }

    /**
     * @return the 8 digits station identifier, e.g. "21006844"
     */
    public String getStationId() {

	return getObject().optString(VanDaClient.STATION_ID, null);
    }

    /**
     * @return
     */
    public String getStationName() {

	return getObject().optString(VanDaClient.STATION_NAME, getStationId());
    }

    /**
     * @return
     */
    public int getMeasurementPointNumber() {

	return getObject().optInt(VanDaClient.MEASUREMENT_POINT_NUMBER, 1);
    }

    /**
     * @return the examination type code (stancode list 1101), e.g. 25 for "Vandstand"
     */
    public int getExaminationTypeCode() {

	return getObject().optInt(VanDaClient.EXAMINATION_TYPE_SC, -1);
    }

    /**
     * @return the parameter code (stancode list 1008), e.g. 1233 for "Vandstand"
     */
    public int getParameterCode() {

	return getObject().optInt(VanDaClient.PARAMETER_SC, -1);
    }

    /**
     * @return the unit, e.g. "cm", or an empty string if not set
     */
    public String getUnit() {

	return getString(VanDaClient.UNIT);
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
