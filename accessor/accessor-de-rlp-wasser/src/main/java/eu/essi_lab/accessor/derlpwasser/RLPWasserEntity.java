package eu.essi_lab.accessor.derlpwasser;

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
 * A time series of the flood portal of Rhineland-Palatinate: a measurement site combined with one of its parameters, described by
 * the site entry of the portal configuration
 *
 * @author boldrini
 */
public class RLPWasserEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public RLPWasserEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public RLPWasserEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "26600128|W"
     */
    public String getSeriesId() {

	return getObject().optString(RLPWasserClient.SERIES_ID, null);
    }

    /**
     * @return the site number, e.g. "26600128"
     */
    public String getSiteNumber() {

	return getObject().optString(RLPWasserClient.NUMBER, null);
    }

    /**
     * @return
     */
    public String getSiteName() {

	return getObject().optString(RLPWasserClient.NAME, getSiteNumber());
    }

    /**
     * @return
     */
    public Optional<RLPWasserParameter> getParameter() {

	return RLPWasserParameter.decode(getObject().optString(RLPWasserClient.PARAMETER, null));
    }

    /**
     * @return the unit symbol, e.g. "cm", or an empty string if not set
     */
    public String getUnit() {

	return getObject().optString(RLPWasserClient.UNIT, "");
    }

    /**
     * @return true if the site is a lake (See), whose water level is referred to the sea level
     */
    public boolean isSeaSite() {

	return getObject().optBoolean(RLPWasserClient.IS_SEA_SITE, false);
    }

    /**
     * @return true if the site is operated by a municipality or a district
     */
    public boolean isMunicipal() {

	return RLPWasserClient.MUNICIPAL_TYPE.equals(getObject().optString(RLPWasserClient.TYPE, ""));
    }

    /**
     * @return true if the site is operated by an agency other than the water management of Rhineland-Palatinate
     */
    public boolean isExternal() {

	return getObject().optBoolean(RLPWasserClient.IS_EXTERNAL, false);
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
