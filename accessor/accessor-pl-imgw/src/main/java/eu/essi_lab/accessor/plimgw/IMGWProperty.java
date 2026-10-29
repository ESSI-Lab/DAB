package eu.essi_lab.accessor.plimgw;

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

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Observed properties published by IMGW-PIB, both by the real time API (latest observation) and by the archive of validated
 * daily data (where the properties are columns of the CSV files, in the order of this enumeration)
 *
 * @author boldrini
 */
public enum IMGWProperty {

    /**
     * Water level, referred to the gauge zero of the station
     */
    H("stan_wody", "stan_wody_data_pomiaru", "Stan wody", "Water level", "cm", "centimetres", "9999"), //
    /**
     * Discharge (flow)
     */
    Q("przeplyw", "przeplyw_data", "Przepływ", "Discharge", "m3/s", "cubic metres per second", "99999.999"), //
    /**
     * Water temperature
     */
    T("temperatura_wody", "temperatura_wody_data_pomiaru", "Temperatura wody", "Water temperature", "°C", "degrees Celsius",
	    "99.9");

    private final String valueKey;
    private final String dateKey;
    private final String polishName;
    private final String label;
    private final String unit;
    private final String unitName;
    private final BigDecimal missingValue;

    /**
     * @param valueKey
     * @param dateKey
     * @param polishName
     * @param label
     * @param unit
     * @param unitName
     * @param missingValue
     */
    private IMGWProperty(String valueKey, String dateKey, String polishName, String label, String unit, String unitName,
	    String missingValue) {

	this.valueKey = valueKey;
	this.dateKey = dateKey;
	this.polishName = polishName;
	this.label = label;
	this.unit = unit;
	this.unitName = unitName;
	this.missingValue = new BigDecimal(missingValue);
    }

    /**
     * @return the key of the latest value in the real time API, e.g. "stan_wody"
     */
    public String getValueKey() {

	return valueKey;
    }

    /**
     * @return the key of the time of the latest value in the real time API, e.g. "stan_wody_data_pomiaru"
     */
    public String getDateKey() {

	return dateKey;
    }

    /**
     * @return the IMGW name of the property, e.g. "Stan wody"
     */
    public String getPolishName() {

	return polishName;
    }

    /**
     * @return the English name, e.g. "Water level"
     */
    public String getLabel() {

	return label;
    }

    /**
     * @return the unit abbreviation, e.g. "cm"
     */
    public String getUnit() {

	return unit;
    }

    /**
     * @return e.g. "centimetres"
     */
    public String getUnitName() {

	return unitName;
    }

    /**
     * @param value
     * @return true if the value is the code used by the archive for missing data (e.g. 9999 for water level)
     */
    public boolean isMissing(BigDecimal value) {

	return value == null || value.compareTo(missingValue) == 0;
    }

    /**
     * @param code e.g. "H"
     * @return
     */
    public static Optional<IMGWProperty> decode(String code) {

	if (code == null) {
	    return Optional.empty();
	}

	for (IMGWProperty property : values()) {
	    if (property.name().equalsIgnoreCase(code.trim())) {
		return Optional.of(property);
	    }
	}

	return Optional.empty();
    }
}
