package eu.essi_lab.accessor.siarso;

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
 * Quantities published by the tables of the ARSO automatic hydrological stations, identified by the (Slovenian) name of the
 * table column, e.g. "Vodostaj" for the column "Vodostaj [cm]". Columns not listed here are harvested as well, using the
 * Slovenian name as label
 *
 * @author boldrini
 */
public enum ARSOParameter {

    /**
     * Water level, referred to the gauge zero of the station
     */
    WATER_LEVEL("Vodostaj", "Water level", ARSOClient.WATER_LEVEL_TAG), //
    /**
     * Discharge
     */
    DISCHARGE("Pretok", "Discharge", ARSOClient.DISCHARGE_TAG), //
    /**
     * Water temperature
     */
    WATER_TEMPERATURE("Temperatura vode", "Water temperature", ARSOClient.WATER_TEMPERATURE_TAG), //
    /**
     * Electrical conductivity of the water (karst springs)
     */
    CONDUCTIVITY("Elektroprevodnost vode", "Water electrical conductivity", null), //
    /**
     * Sea level (published as "vodostaj" in the XML of the latest data)
     */
    SEA_LEVEL("Gladina morja", "Sea level", ARSOClient.WATER_LEVEL_TAG), //
    /**
     * Sea water temperature (published as "temp_vode" in the XML of the latest data)
     */
    SEA_TEMPERATURE("Temperatura morja", "Sea water temperature", ARSOClient.WATER_TEMPERATURE_TAG), //
    /**
     * Significant wave height (mean height of the highest third of the waves)
     */
    WAVE_HEIGHT("Višina valov", "Significant wave height", ARSOClient.WAVE_HEIGHT_TAG), //
    /**
     * Wave period
     */
    WAVE_PERIOD("Perioda valovanja", "Wave period", null), //
    /**
     * Speed of the sea current 2 m below the surface
     */
    SEA_CURRENT_2M("Hitrost morskega toka 2m pod glad.", "Sea current speed 2 m below the surface", null), //
    /**
     * Speed of the sea current 10 m below the surface
     */
    SEA_CURRENT_10M("Hitrost morskega toka 10m pod glad.", "Sea current speed 10 m below the surface", null);

    private final String columnName;
    private final String label;
    private final String latestTag;

    /**
     * @param columnName
     * @param label
     * @param latestTag
     */
    private ARSOParameter(String columnName, String label, String latestTag) {

	this.columnName = columnName;
	this.label = label;
	this.latestTag = latestTag;
    }

    /**
     * @return the name of the table column, without the unit, e.g. "Vodostaj"
     */
    public String getColumnName() {

	return columnName;
    }

    /**
     * @return the English name, e.g. "Water level"
     */
    public String getLabel() {

	return label;
    }

    /**
     * @return the element of the XML of the latest data reporting the same quantity, if any
     */
    public Optional<String> getLatestTag() {

	return Optional.ofNullable(latestTag);
    }

    /**
     * @param columnName the name of the table column, e.g. "Vodostaj"
     * @return
     */
    public static Optional<ARSOParameter> fromColumnName(String columnName) {

	if (columnName == null) {
	    return Optional.empty();
	}

	for (ARSOParameter parameter : values()) {
	    if (parameter.columnName.equalsIgnoreCase(columnName.trim())) {
		return Optional.of(parameter);
	    }
	}

	return Optional.empty();
    }

    /**
     * @param code the enum name, e.g. "WATER_LEVEL"
     * @return
     */
    public static Optional<ARSOParameter> decode(String code) {

	if (code == null) {
	    return Optional.empty();
	}

	for (ARSOParameter parameter : values()) {
	    if (parameter.name().equalsIgnoreCase(code.trim())) {
		return Optional.of(parameter);
	    }
	}

	return Optional.empty();
    }
}
