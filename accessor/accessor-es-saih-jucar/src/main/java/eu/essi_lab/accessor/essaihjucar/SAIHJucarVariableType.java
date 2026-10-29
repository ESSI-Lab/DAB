package eu.essi_lab.accessor.essaihjucar;

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
 * Types of the variables published by the SAIH Júcar web site, each one with the map page (category) listing them and the unit
 * used by the provider
 *
 * @author boldrini
 */
public enum SAIHJucarVariableType {

    /**
     * River discharge measured at a gauging station (aforo)
     */
    RIVER_DISCHARGE(SAIHJucarCategory.AFOROS, "River discharge", "m³/s"), //
    /**
     * Water level of a reservoir, above sea level (cota de embalse)
     */
    RESERVOIR_LEVEL(SAIHJucarCategory.EMBALSES, "Reservoir water level", "m s.n.m."), //
    /**
     * Volume stored in a reservoir, estimated from the water level (volumen de embalse)
     */
    RESERVOIR_VOLUME(SAIHJucarCategory.EMBALSES, "Reservoir volume", "hm³"), //
    /**
     * Inflow of a reservoir, computed by balance (caudal recibido)
     */
    RESERVOIR_INFLOW(SAIHJucarCategory.EMBALSES, "Reservoir inflow", "m³/s"), //
    /**
     * Total outflow of a reservoir (caudal total de salida)
     */
    RESERVOIR_OUTFLOW(SAIHJucarCategory.EMBALSES, "Reservoir total outflow", "m³/s"), //
    /**
     * Outflow of a reservoir to the river (caudal salida río)
     */
    RESERVOIR_RIVER_OUTFLOW(SAIHJucarCategory.EMBALSES, "Reservoir outflow to river", "m³/s"), //
    /**
     * Five minutes rainfall intensity (intensidad de lluvia calculada)
     */
    PRECIPITATION_INTENSITY(SAIHJucarCategory.LLUVIAS, "Precipitation intensity", "mm/h");

    /**
     * Map pages of the SAIH Júcar web site
     */
    public enum SAIHJucarCategory {

	/**
	 * River gauging stations (mapa-aforos)
	 */
	AFOROS,
	/**
	 * Reservoirs (mapa-embalses)
	 */
	EMBALSES,
	/**
	 * Rain gauges (mapa-lluvias)
	 */
	LLUVIAS;

	/**
	 * @param name
	 * @return
	 */
	public static Optional<SAIHJucarCategory> decode(String name) {

	    if (name == null) {
		return Optional.empty();
	    }

	    for (SAIHJucarCategory category : values()) {
		if (category.name().equalsIgnoreCase(name.trim())) {
		    return Optional.of(category);
		}
	    }

	    return Optional.empty();
	}
    }

    private final SAIHJucarCategory category;
    private final String label;
    private final String unit;

    /**
     * @param category
     * @param label
     * @param unit
     */
    private SAIHJucarVariableType(SAIHJucarCategory category, String label, String unit) {

	this.category = category;
	this.label = label;
	this.unit = unit;
    }

    /**
     * @return the map page listing the variables of this type
     */
    public SAIHJucarCategory getCategory() {

	return category;
    }

    /**
     * @return the English name of the observed property
     */
    public String getLabel() {

	return label;
    }

    /**
     * @return the unit used by the provider, e.g. "m³/s"
     */
    public String getUnit() {

	return unit;
    }

    /**
     * @param name
     * @return
     */
    public static Optional<SAIHJucarVariableType> decode(String name) {

	if (name == null) {
	    return Optional.empty();
	}

	for (SAIHJucarVariableType type : values()) {
	    if (type.name().equalsIgnoreCase(name.trim())) {
		return Optional.of(type);
	    }
	}

	return Optional.empty();
    }
}
