package eu.essi_lab.accessor.luage;

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
 * An AGE time series: a station of the inondations.lu portal combined with one of its parameters
 *
 * @author boldrini
 */
public class AGEEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public AGEEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public AGEEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "Hesperange|W"
     */
    public String getSeriesId() {

	return getObject().optString(AGEClient.SERIES_ID, null);
    }

    /**
     * @return the station code (the name of its JSON file), e.g. "Hesperange"
     */
    public String getStationCode() {

	return getObject().optString(AGEClient.CODE, null);
    }

    /**
     * @return
     */
    public String getStationName() {

	return getObject().optString(AGEClient.NAME, getStationCode());
    }

    /**
     * @return
     */
    public String getWaterCourse() {

	return getString(AGEClient.WATER_COURSE);
    }

    /**
     * @return the WISKI parameter type, e.g. "W"
     */
    public String getParameter() {

	return getString(AGEClient.PARAMETER);
    }

    /**
     * @return the unit symbol, e.g. "cm"
     */
    public String getUnit() {

	return getString(AGEClient.UNIT);
    }

    /**
     * @return the operator of the station, e.g. "Administration de la gestion de l'eau"
     */
    public String getOperator() {

	return getString(AGEClient.OPERATOR);
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
