package eu.essi_lab.accessor.ltlhmt;

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
 * A LHMT time series: a hydrological station combined with one of its parameters
 *
 * @author boldrini
 */
public class LHMTEntity extends JSONObjectWrapper {

    /**
     * @param source
     */
    public LHMTEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public LHMTEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "nemajunu-vms|WATER_LEVEL"
     */
    public String getSeriesId() {

	return getObject().optString(LHMTClient.SERIES_ID, null);
    }

    /**
     * @return the station code, e.g. "nemajunu-vms"
     */
    public String getStationCode() {

	return getObject().optString(LHMTClient.CODE, null);
    }

    /**
     * @return
     */
    public String getStationName() {

	return getObject().optString(LHMTClient.NAME, getStationCode());
    }

    /**
     * @return
     */
    public String getWaterBody() {

	return getString(LHMTClient.WATER_BODY);
    }

    /**
     * @return
     */
    public Optional<LHMTParameter> getParameter() {

	return LHMTParameter.decode(getObject().optString(LHMTClient.PARAMETER, null));
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
