package eu.essi_lab.accessor.esaca;

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
 * Observed properties published by the ACA Sentilo sensors, identified by the (Catalan) description of the sensor. The sensor type
 * codes are not used, since they are not consistent (e.g. "0014" is used both for river and total discharge, "9999" for several
 * properties)
 *
 * @author boldrini
 */
public enum ACAProperty {

    /**
     * River water level (stage) at a gauging station
     */
    RIVER_LEVEL("Nivell riu", "River water level"), //
    /**
     * River discharge at a gauging station
     */
    RIVER_DISCHARGE("Cabal riu", "River discharge"), //
    /**
     * Discharge of a canal
     */
    CANAL_DISCHARGE("Cabal canal", "Canal discharge"), //
    /**
     * Total discharge (river and canals)
     */
    TOTAL_DISCHARGE("Cabal total", "Total discharge"), //
    /**
     * Water level of a reservoir, above sea level
     */
    RESERVOIR_LEVEL("Nivell absolut", "Reservoir water level"), //
    /**
     * Volume stored in a reservoir
     */
    RESERVOIR_VOLUME("Volum embassat", "Reservoir stored volume"), //
    /**
     * Volume stored in a reservoir, as percentage of its capacity
     */
    RESERVOIR_PERCENTAGE("Percentatge volum embassat", "Reservoir stored volume percentage"), //
    /**
     * Groundwater level (piezometric head), above sea level
     */
    GROUNDWATER_LEVEL("Nivell piezomètric absolut", "Groundwater level"), //
    /**
     * Water level of a spring
     */
    SPRING_LEVEL("Nivell font", "Spring water level"), //
    /**
     * Water temperature
     */
    WATER_TEMPERATURE("Temperatura de l'aigua", "Water temperature"), //
    /**
     * Electrical conductivity of the water
     */
    CONDUCTIVITY("Conductivitat", "Electrical conductivity"), //
    /**
     * Precipitation intensity
     */
    PRECIPITATION_INTENSITY("Intensitat de precipitació", "Precipitation intensity");

    private final String description;
    private final String label;

    /**
     * @param description
     * @param label
     */
    private ACAProperty(String description, String label) {

	this.description = description;
	this.label = label;
    }

    /**
     * @return the Catalan description used by ACA, e.g. "Cabal riu"
     */
    public String getDescription() {

	return description;
    }

    /**
     * @return the English name, e.g. "River discharge"
     */
    public String getLabel() {

	return label;
    }

    /**
     * @param description the description of the sensor, e.g. "Cabal riu"
     * @return
     */
    public static Optional<ACAProperty> decode(String description) {

	if (description == null) {
	    return Optional.empty();
	}

	for (ACAProperty property : values()) {
	    if (property.description.equalsIgnoreCase(description.trim())) {
		return Optional.of(property);
	    }
	}

	return Optional.empty();
    }
}
