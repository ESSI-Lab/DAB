package eu.essi_lab.accessor.derlpwasser;

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
 * Parameters published by the flood portal of Rhineland-Palatinate. The key of each parameter is the key of its measurements in
 * the response of the measurement site endpoint
 *
 * @author boldrini
 */
public enum RLPWasserParameter {

    /**
     * Water level (Wasserstand), in cm referred to the gauge datum (Pegelnullpunkt), or in m above sea level (m ü. NHN) at lake
     * sites
     */
    W("Wasserstand", "Water level"), //
    /**
     * Discharge (Abfluss), in m³/s
     */
    Q("Abfluss", "Discharge");

    private final String germanName;
    private final String label;

    /**
     * @param germanName
     * @param label
     */
    private RLPWasserParameter(String germanName, String label) {

	this.germanName = germanName;
	this.label = label;
    }

    /**
     * @return the name used by the provider, e.g. "Wasserstand"
     */
    public String getGermanName() {

	return germanName;
    }

    /**
     * @return the English name, e.g. "Water level"
     */
    public String getLabel() {

	return label;
    }

    /**
     * @param isSeaSite true for lake (See) sites, whose water level is referred to the sea level
     * @return the unit symbol used by the portal, e.g. "cm"
     */
    public String getUnit(boolean isSeaSite) {

	switch (this) {
	case W:
	    return isSeaSite ? "m ü. NHN" : "cm";
	case Q:
	default:
	    return "m³/s";
	}
    }

    /**
     * @param code the parameter code, e.g. "W"
     * @return
     */
    public static Optional<RLPWasserParameter> decode(String code) {

	if (code == null) {
	    return Optional.empty();
	}

	for (RLPWasserParameter parameter : values()) {
	    if (parameter.name().equalsIgnoreCase(code.trim())) {
		return Optional.of(parameter);
	    }
	}

	return Optional.empty();
    }
}
