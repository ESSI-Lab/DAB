package eu.essi_lab.accessor.nlrijkswaterstaat;

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
 * A Rijkswaterstaat time series: a location combined with the AQUO metadata identifying one observed property
 *
 * @author boldrini
 */
public class RijkswaterstaatEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public RijkswaterstaatEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public RijkswaterstaatEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return
     */
    public String getSeriesId() {

	return getObject().optString(RijkswaterstaatClient.SERIES_ID, null);
    }

    /**
     * @return
     */
    public String getLocationCode() {

	return getObject().optString(RijkswaterstaatClient.LOCATION_CODE, null);
    }

    /**
     * @param aquoKey one of {@link RijkswaterstaatClient#AQUO_KEYS}
     * @return the code, or an empty string if not set
     */
    public String getCode(String aquoKey) {

	JSONObject element = getObject().optJSONObject(aquoKey);
	return element == null ? "" : element.optString("Code", "");
    }

    /**
     * @param aquoKey one of {@link RijkswaterstaatClient#AQUO_KEYS}
     * @return the description, or an empty string if not set
     */
    public String getDescription(String aquoKey) {

	JSONObject element = getObject().optJSONObject(aquoKey);
	return element == null ? "" : element.optString("Omschrijving", "");
    }

    /**
     * @return
     */
    public String getProcessType() {

	return getObject().optString(RijkswaterstaatClient.PROCESS_TYPE, "");
    }
}
