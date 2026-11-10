package eu.essi_lab.accessor.czchmi;

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
 * A CHMI time series: the station metadata (keys of the CHMI metadata files) and the series type
 *
 * @author boldrini
 */
public class CHMIEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public CHMIEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public CHMIEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "0-203-1-001000|H"
     */
    public String getSeriesId() {

	return getObject().optString(CHMIClient.SERIES_ID, null);
    }

    /**
     * @return the WIGOS station identifier, e.g. "0-203-1-001000"
     */
    public String getStationId() {

	return getObject().optString(CHMIClient.OBJ_ID, null);
    }

    /**
     * @return
     */
    public String getStationName() {

	String name = getString(CHMIClient.STATION_NAME);
	return name.isEmpty() ? getStationId() : name;
    }

    /**
     * @return
     */
    public Optional<CHMIParameter> getParameter() {

	return CHMIParameter.decode(getObject().optString(CHMIClient.PARAMETER, null));
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	Object value = getObject().opt(key);
	if (value == null || value == JSONObject.NULL) {
	    return "";
	}

	return value.toString().trim();
    }
}
