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

/**
 * Observed properties published by the station files of the SIH hydrometric database, each one identified by the name of its
 * column (e.g. "Gasto" in the column "Gasto(m³/s)")
 *
 * @author boldrini
 */
public enum CONAGUAProperty {

    /**
     * Daily water level (nivel)
     */
    LEVEL("Nivel", "Water level", "m", "metres"), //
    /**
     * Daily discharge (gasto)
     */
    DISCHARGE("Gasto", "Discharge", "m3/s", "cubic metres per second");

    private final String column;
    private final String label;
    private final String unitAbbreviation;
    private final String unitName;

    /**
     * @param column
     * @param label
     * @param unitAbbreviation
     * @param unitName
     */
    private CONAGUAProperty(String column, String label, String unitAbbreviation, String unitName) {

	this.column = column;
	this.label = label;
	this.unitAbbreviation = unitAbbreviation;
	this.unitName = unitName;
    }

    /**
     * @return the Spanish name used in the column header, e.g. "Gasto"
     */
    public String getColumn() {

	return column;
    }

    /**
     * @return the English name, e.g. "Discharge"
     */
    public String getLabel() {

	return label;
    }

    /**
     * @return the unit abbreviation used when the column header has no unit, e.g. "m3/s"
     */
    public String getUnitAbbreviation() {

	return unitAbbreviation;
    }

    /**
     * @return
     */
    public String getUnitName() {

	return unitName;
    }

    /**
     * @param code the property name (e.g. "DISCHARGE") or the column name (e.g. "Gasto")
     * @return
     */
    public static Optional<CONAGUAProperty> decode(String code) {

	if (code == null) {
	    return Optional.empty();
	}

	for (CONAGUAProperty property : values()) {
	    if (property.name().equalsIgnoreCase(code.trim()) || property.getColumn().equalsIgnoreCase(code.trim())) {
		return Optional.of(property);
	    }
	}

	return Optional.empty();
    }
}
