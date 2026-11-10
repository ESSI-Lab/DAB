package eu.essi_lab.accessor.nonvemagasin;

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
 * Weekly properties published by the NVE reservoir statistics (Magasinstatistikk), each one identified by the name of the field
 * of the API
 *
 * @author boldrini
 */
public enum NVEMagasinProperty {

    /**
     * Filling ratio of the reservoirs (fraction of the capacity, 0-1)
     */
    FILLING_RATIO("fyllingsgrad", "Reservoir filling ratio", "dimensionless (fraction of the reservoir capacity, 0-1)", "1"), //
    /**
     * Energy stored in the reservoirs
     */
    FILLING("fylling_TWh", "Reservoir filling (stored energy)", "terawatt hours", "TWh"), //
    /**
     * Capacity of the reservoirs, expressed as energy
     */
    CAPACITY("kapasitet_TWh", "Reservoir capacity (energy)", "terawatt hours", "TWh");

    private final String field;
    private final String label;
    private final String unitName;
    private final String unitAbbreviation;

    /**
     * @param field
     * @param label
     * @param unitName
     * @param unitAbbreviation
     */
    private NVEMagasinProperty(String field, String label, String unitName, String unitAbbreviation) {

	this.field = field;
	this.label = label;
	this.unitName = unitName;
	this.unitAbbreviation = unitAbbreviation;
    }

    /**
     * @return the name of the field of the API, e.g. "fyllingsgrad"
     */
    public String getField() {

	return field;
    }

    /**
     * @return the English name of the property
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
     * @param code the field of the API (e.g. "fyllingsgrad") or the name of the constant (e.g. "FILLING_RATIO")
     * @return
     */
    public static Optional<NVEMagasinProperty> decode(String code) {

	if (code == null) {
	    return Optional.empty();
	}

	for (NVEMagasinProperty property : values()) {
	    if (property.field.equalsIgnoreCase(code.trim()) || property.name().equalsIgnoreCase(code.trim())) {
		return Optional.of(property);
	    }
	}

	return Optional.empty();
    }
}
