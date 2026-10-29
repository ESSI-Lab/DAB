package eu.essi_lab.accessor.adhi;

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
 * An ADHI time series: a station of the station list combined with one of the discharge series of the dataset (see
 * {@link ADHIVariable})
 *
 * @author boldrini
 */
public class ADHIEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public ADHIEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public ADHIEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "ADHI_1:MONTHLY_MEAN"
     */
    public String getSeriesId() {

	return getObject().optString(ADHIClient.SERIES_ID, null);
    }

    /**
     * @return the ADHI station identifier, e.g. "ADHI_1"
     */
    public String getStationId() {

	return getObject().optString(ADHIClient.STATION_ID, null);
    }

    /**
     * @return the native code of the station, e.g. "CMQ0001"
     */
    public String getStationCode() {

	return getString(ADHIClient.STATION_CODE);
    }

    /**
     * @return
     */
    public String getStationName() {

	String name = getString(ADHIClient.STATION_NAME);
	return name.isEmpty() ? getStationId() : name;
    }

    /**
     * @return
     */
    public Optional<ADHIVariable> getVariable() {

	return ADHIVariable.decode(getObject().optString(ADHIClient.VARIABLE, null));
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
