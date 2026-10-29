package eu.essi_lab.accessor.ththaiwater;

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

/**
 * Observed properties published by ThaiWater, each one observed at stations of a given {@link ThaiWaterStationType}
 *
 * @author boldrini
 */
public enum ThaiWaterParameter {

    /**
     * River water level, referred to the mean sea level (MSL)
     */
    WATER_LEVEL(ThaiWaterStationType.TELE_WATERLEVEL, "waterlevel_msl", "value", "Water level", "m", "metres",
	    "water level (metres above mean sea level)"), //
    /**
     * River discharge, available at the stations with a rating curve
     */
    DISCHARGE(ThaiWaterStationType.TELE_WATERLEVEL, "discharge", "discharge", "Discharge", "m3/s", "cubic metres per second",
	    "discharge"), //
    /**
     * Canal water level, referred to the mean sea level (MSL)
     */
    CANAL_WATER_LEVEL(ThaiWaterStationType.CANAL, "canal_value", "value", "Water level", "m", "metres",
	    "canal water level (metres above mean sea level)"), //
    /**
     * Canal flow rate
     */
    CANAL_FLOW(ThaiWaterStationType.FLOW, "flow_value", "flow_value", "Discharge", "m3/s", "cubic metres per second",
	    "canal flow rate (discharge)"), //
    /**
     * Reservoir storage (daily)
     */
    DAM_STORAGE(ThaiWaterStationType.DAM, "dam_storage", null, "Reservoir storage", "Mm3", "million cubic metres",
	    "reservoir storage (total volume of water stored)"), //
    /**
     * Reservoir usable storage (daily), i.e. storage above the minimum operating level
     */
    DAM_USABLE_STORAGE(ThaiWaterStationType.DAM, "dam_uses_water", null, "Reservoir usable storage", "Mm3", "million cubic metres",
	    "reservoir usable storage (volume of water above the minimum storage)"), //
    /**
     * Reservoir inflow (daily volume)
     */
    DAM_INFLOW(ThaiWaterStationType.DAM, "dam_inflow", null, "Reservoir inflow", "Mm3", "million cubic metres",
	    "reservoir inflow (daily volume)"), //
    /**
     * Reservoir release (daily volume)
     */
    DAM_RELEASE(ThaiWaterStationType.DAM, "dam_released", null, "Reservoir release", "Mm3", "million cubic metres",
	    "reservoir release (daily volume of water released)"), //
    /**
     * Reservoir water level (daily), referred to the mean sea level (MSL)
     */
    DAM_LEVEL(ThaiWaterStationType.DAM, "dam_level", null, "Reservoir water level", "m", "metres",
	    "reservoir water level (metres above mean sea level)");

    private final ThaiWaterStationType stationType;
    private final String key;
    private final String graphKey;
    private final String label;
    private final String unit;
    private final String unitName;
    private final String description;

    /**
     * @param stationType
     * @param key
     * @param graphKey
     * @param label
     * @param unit
     * @param unitName
     * @param description
     */
    private ThaiWaterParameter(ThaiWaterStationType stationType, String key, String graphKey, String label, String unit,
	    String unitName, String description) {

	this.stationType = stationType;
	this.key = key;
	this.graphKey = graphKey;
	this.label = label;
	this.unit = unit;
	this.unitName = unitName;
	this.description = description;
    }

    /**
     * @return
     */
    public ThaiWaterStationType getStationType() {

	return stationType;
    }

    /**
     * @return the ThaiWater name of the property, used by the lists of the latest observations, by the daily values of the water
     *         level stations and as data type of the reservoir graphs, e.g. "waterlevel_msl" or "dam_storage"
     */
    public String getKey() {

	return key;
    }

    /**
     * @return the key of the values returned by the graph services (sub-daily observations), or null for the reservoirs
     */
    public String getGraphKey() {

	return graphKey;
    }

    /**
     * @return the English name of the property, e.g. "Water level"
     */
    public String getLabel() {

	return label;
    }

    /**
     * @return the unit abbreviation, e.g. "m3/s"
     */
    public String getUnit() {

	return unit;
    }

    /**
     * @return
     */
    public String getUnitName() {

	return unitName;
    }

    /**
     * @return
     */
    public String getDescription() {

	return description;
    }

    /**
     * @return true if the values are daily volumes (inflow and release of the reservoirs)
     */
    public boolean isDailyVolume() {

	return this == DAM_INFLOW || this == DAM_RELEASE;
    }

    /**
     * @param name
     * @return
     */
    public static Optional<ThaiWaterParameter> decode(String name) {

	if (name == null) {
	    return Optional.empty();
	}

	for (ThaiWaterParameter parameter : values()) {
	    if (parameter.name().equalsIgnoreCase(name.trim())) {
		return Optional.of(parameter);
	    }
	}

	return Optional.empty();
    }
}
