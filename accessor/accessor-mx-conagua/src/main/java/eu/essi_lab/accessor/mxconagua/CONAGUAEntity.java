package eu.essi_lab.accessor.mxconagua;

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
 * A CONAGUA time series: a station of the SIH hydrometric catalogue combined with one of the properties of its file, with the time
 * of its first and last valid value
 *
 * @author boldrini
 */
public class CONAGUAEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public CONAGUAEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public CONAGUAEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "CNDCA|DISCHARGE"
     */
    public String getSeriesId() {

	return getObject().optString(CONAGUAClient.SERIES_ID, null);
    }

    /**
     * @return the station code, e.g. "CNDCA"
     */
    public String getStationCode() {

	return getObject().optString(CONAGUAClient.STATION_CODE, null);
    }

    /**
     * @return
     */
    public String getStationName() {

	return getObject().optString(CONAGUAClient.STATION_NAME, getStationCode());
    }

    /**
     * @return
     */
    public Optional<CONAGUAProperty> getProperty() {

	return CONAGUAProperty.decode(getObject().optString(CONAGUAClient.PROPERTY, null));
    }

    /**
     * @return the unit as written by CONAGUA, e.g. "m³/s", or an empty string if not set
     */
    public String getUnit() {

	return getObject().optString(CONAGUAClient.UNIT, "");
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
