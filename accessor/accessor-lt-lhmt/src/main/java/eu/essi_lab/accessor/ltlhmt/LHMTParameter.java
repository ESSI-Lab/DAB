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

/**
 * Hydrological parameters published by the Meteo LT API. The measured (hourly) observations are available for the last 30 days,
 * while the historical (daily mean) observations are available from 2000 until the end of the last consolidated year
 *
 * @author boldrini
 */
public enum LHMTParameter {

    /**
     * Hourly water level, cm
     */
    WATER_LEVEL(LHMTClient.MEASURED, "waterLevel", "Water level", "centimetres", "cm"), //
    /**
     * Hourly water temperature, not measured with ice cover
     */
    WATER_TEMPERATURE(LHMTClient.MEASURED, "waterTemperature", "Water temperature", "degrees Celsius", "°C"), //
    /**
     * Daily mean water level, cm
     */
    WATER_LEVEL_DAILY(LHMTClient.HISTORICAL, "waterLevel", "Water level", "centimetres", "cm"), //
    /**
     * Daily mean discharge
     */
    DISCHARGE_DAILY(LHMTClient.HISTORICAL, "waterDischarge", "Discharge", "cubic metres per second", "m3/s");

    private final String observationType;
    private final String key;
    private final String label;
    private final String unitName;
    private final String unitAbbreviation;

    /**
     * @param observationType
     * @param key
     * @param label
     * @param unitName
     * @param unitAbbreviation
     */
    private LHMTParameter(String observationType, String key, String label, String unitName, String unitAbbreviation) {

	this.observationType = observationType;
	this.key = key;
	this.label = label;
	this.unitName = unitName;
	this.unitAbbreviation = unitAbbreviation;
    }

    /**
     * @return {@link LHMTClient#MEASURED} or {@link LHMTClient#HISTORICAL}
     */
    public String getObservationType() {

	return observationType;
    }

    /**
     * @return true if the observations are daily means
     */
    public boolean isDaily() {

	return observationType.equals(LHMTClient.HISTORICAL);
    }

    /**
     * @return the key of the value in the observations, e.g. "waterLevel"
     */
    public String getKey() {

	return key;
    }

    /**
     * @return
     */
    public String getLabel() {

	return label;
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
    public String getUnitAbbreviation() {

	return unitAbbreviation;
    }

    /**
     * @return the ISO 8601 resolution of the observations
     */
    public String getResolution() {

	return isDaily() ? "P1D" : "PT1H";
    }

    /**
     * @param code e.g. "WATER_LEVEL"
     * @return
     */
    public static Optional<LHMTParameter> decode(String code) {

	if (code == null) {
	    return Optional.empty();
	}

	for (LHMTParameter parameter : values()) {
	    if (parameter.name().equalsIgnoreCase(code.trim())) {
		return Optional.of(parameter);
	    }
	}

	return Optional.empty();
    }
}
