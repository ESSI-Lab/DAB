package eu.essi_lab.accessor.lvlvgmc;

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

import java.util.Map;

import org.json.JSONObject;

import eu.essi_lab.lib.utils.JSONObjectWrapper;

/**
 * A LVGMC time series: a hydrological station combined with one of its parameters
 *
 * @author boldrini
 */
public class LVGMCEntity extends JSONObjectWrapper {

    /**
     * English names of the parameters published at the time of writing (the other parameters are named after the first part of
     * their English description)
     */
    static final Map<String, String> PROPERTY_NAMES = Map.of(//
	    "LIMEN", "Water level", //
	    "WTEMD", "Water temperature", //
	    "SEDUT", "Sea water temperature", //
	    "SEHL", "Sea level");

    /**
     * Unit names of the units published at the time of writing
     */
    static final Map<String, String> UNIT_NAMES = Map.of(//
	    "cm", "centimetres", //
	    "m", "metres", //
	    "°C", "degrees Celsius", //
	    "m3/s", "cubic metres per second");

    /**
     * @param source
     */
    public LVGMCEntity(String source) {

	super(source);
    }

    /**
     * @param object
     */
    public LVGMCEntity(JSONObject object) {

	super(object);
    }

    /**
     * @return the series identifier, e.g. "HD073548|LIMEN"
     */
    public String getSeriesId() {

	return getObject().optString(LVGMCClient.SERIES_ID, null);
    }

    /**
     * @return the station identifier, e.g. "HD073548"
     */
    public String getStationId() {

	return getObject().optString(LVGMCClient.STATION_ID, null);
    }

    /**
     * @return
     */
    public String getStationName() {

	String name = getString(LVGMCClient.NAME);
	return name.isEmpty() ? getStationId() : name;
    }

    /**
     * @return the parameter abbreviation, e.g. "LIMEN"
     */
    public String getAbbreviation() {

	return getObject().optString(LVGMCClient.ABBREVIATION, null);
    }

    /**
     * @return the English description of the parameter, e.g. "Water level, real, hourly average"
     */
    public String getEnglishDescription() {

	return getString(LVGMCClient.EN_DESCRIPTION);
    }

    /**
     * @return the Latvian description of the parameter, e.g. "Ūdens līmenis, faktiskais, stundas vidējais"
     */
    public String getLatvianDescription() {

	return getString(LVGMCClient.LV_DESCRIPTION);
    }

    /**
     * @return the unit, e.g. "cm"
     */
    public String getUnit() {

	return getString(LVGMCClient.MEASUREMENT_UNIT);
    }

    /**
     * @return the unit name, e.g. "centimetres"
     */
    public String getUnitName() {

	return UNIT_NAMES.getOrDefault(getUnit(), getUnit());
    }

    /**
     * @return the English name of the observed property, e.g. "Water level"
     */
    public String getPropertyName() {

	String abbreviation = getAbbreviation();
	if (abbreviation != null && PROPERTY_NAMES.containsKey(abbreviation)) {
	    return PROPERTY_NAMES.get(abbreviation);
	}

	String description = getEnglishDescription();
	if (!description.isEmpty()) {
	    return description.split(",")[0].trim();
	}

	return abbreviation == null ? "" : abbreviation;
    }

    /**
     * @return the ISO 8601 aggregation period of the values: "PT15M" for the means of the last 15 minutes of the hour (sea
     *         level), "PT1H" for the hourly averages
     */
    public String getAggregationPeriod() {

	return getEnglishDescription().toLowerCase().contains("15 minutes") ? "PT15M" : "PT1H";
    }

    /**
     * @return true for the water levels, which are given above the station zero (to obtain levels in the LAS-2000,5 height
     *         system the station zero height must be added)
     */
    public boolean isWaterLevel() {

	return "LIMEN".equals(getAbbreviation()) || getEnglishDescription().toLowerCase().startsWith("water level");
    }

    /**
     * @param key
     * @return the value, or an empty string if not set
     */
    public String getString(String key) {

	return getObject().optString(key, "").trim();
    }
}
