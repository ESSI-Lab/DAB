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
 * Types of the ThaiWater stations, each one listed by its own service together with the latest observations. The code of the type
 * is part of the series identifiers, since the station identifiers are unique only within the same type
 *
 * @author boldrini
 */
public enum ThaiWaterStationType {

    /**
     * Telemetering water level stations of the rivers (HII, RID, EGAT, ...), listed by public/waterlevel_load
     */
    TELE_WATERLEVEL("tele_waterlevel", "Telemetering water level station"), //
    /**
     * Canal water level stations of the Bangkok Metropolitan Administration, listed by public/canal_waterlevel
     */
    CANAL("canal", "Canal water level station"), //
    /**
     * Canal flow stations of the Bangkok Metropolitan Administration, listed by public/flow
     */
    FLOW("flow", "Canal flow station"), //
    /**
     * Large reservoirs (RID and EGAT dams) with daily observations, listed by analyst/dam
     */
    DAM("dam", "Large reservoir");

    private final String code;
    private final String label;

    /**
     * @param code
     * @param label
     */
    private ThaiWaterStationType(String code, String label) {

	this.code = code;
	this.label = label;
    }

    /**
     * @return the code used in the series identifiers and in the settings, e.g. "tele_waterlevel"
     */
    public String getCode() {

	return code;
    }

    /**
     * @return
     */
    public String getLabel() {

	return label;
    }

    /**
     * @param code the code or the name of the type, e.g. "tele_waterlevel" or "TELE_WATERLEVEL"
     * @return
     */
    public static Optional<ThaiWaterStationType> decode(String code) {

	if (code == null) {
	    return Optional.empty();
	}

	for (ThaiWaterStationType type : values()) {
	    if (type.code.equalsIgnoreCase(code.trim()) || type.name().equalsIgnoreCase(code.trim())) {
		return Optional.of(type);
	    }
	}

	return Optional.empty();
    }
}
