package eu.essi_lab.accessor.hrdhmz;

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

import eu.essi_lab.model.resource.InterpolationType;

/**
 * The data types published by the DHMZ hidroweb service. Each type is identified by the "tippod" parameter of the requests and
 * by the "Godine mjerenja ..." (years of measurement) row of the station description
 *
 * @author boldrini
 */
public enum DHMZProperty {

    /**
     * Daily water level, referred to the gauge zero of the station
     */
    WATER_LEVEL("5", "vodostaja", "vodostaj", "Water level", "cm", "centimetres", InterpolationType.AVERAGE), //
    /**
     * Daily discharge
     */
    DISCHARGE("100", "protoka", "protok", "Discharge", "m3/s", "cubic metres per second", InterpolationType.AVERAGE), //
    /**
     * Daily total discharge (e.g. river and canal, published only by a few stations)
     */
    TOTAL_DISCHARGE("120", "ukupnog protoka", "ukupni protok", "Total discharge", "m3/s", "cubic metres per second",
	    InterpolationType.AVERAGE), //
    /**
     * Daily water temperature
     */
    WATER_TEMPERATURE("200", "temperatura", "temperatura vode", "Water temperature", "°C", "degrees Celsius",
	    InterpolationType.AVERAGE), //
    /**
     * Daily suspended sediment concentration
     */
    SUSPENDED_SEDIMENT_CONCENTRATION("300", "koncentracija", "koncentracija suspendiranog nanosa", "Suspended sediment concentration",
	    "g/m3", "grams per cubic metre", InterpolationType.AVERAGE), //
    /**
     * Daily suspended sediment load (daily total)
     */
    SUSPENDED_SEDIMENT_LOAD("400", "pronosa", "pronos suspendiranog nanosa", "Suspended sediment load", "t", "tonnes",
	    InterpolationType.TOTAL);

    private final String typeCode;
    private final String yearsKey;
    private final String localName;
    private final String label;
    private final String unit;
    private final String unitName;
    private final InterpolationType interpolation;

    /**
     * @param typeCode
     * @param yearsKey
     * @param localName
     * @param label
     * @param unit
     * @param unitName
     * @param interpolation
     */
    private DHMZProperty(String typeCode, String yearsKey, String localName, String label, String unit, String unitName,
	    InterpolationType interpolation) {

	this.typeCode = typeCode;
	this.yearsKey = yearsKey;
	this.localName = localName;
	this.label = label;
	this.unit = unit;
	this.unitName = unitName;
	this.interpolation = interpolation;
    }

    /**
     * @return the value of the "tippod" request parameter, e.g. "5"
     */
    public String getTypeCode() {

	return typeCode;
    }

    /**
     * @return the key of the years of measurement in the station description, e.g. "vodostaja" for "Godine mjerenja vodostaja"
     */
    public String getYearsKey() {

	return yearsKey;
    }

    /**
     * @return the Croatian name, e.g. "vodostaj"
     */
    public String getLocalName() {

	return localName;
    }

    /**
     * @return the English name, e.g. "Water level"
     */
    public String getLabel() {

	return label;
    }

    /**
     * @return
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
    public InterpolationType getInterpolation() {

	return interpolation;
    }

    /**
     * @param name the property name, e.g. "WATER_LEVEL", or the type code, e.g. "5"
     * @return
     */
    public static Optional<DHMZProperty> decode(String name) {

	if (name == null) {
	    return Optional.empty();
	}

	for (DHMZProperty property : values()) {
	    if (property.name().equalsIgnoreCase(name.trim()) || property.getTypeCode().equals(name.trim())) {
		return Optional.of(property);
	    }
	}

	return Optional.empty();
    }
}
