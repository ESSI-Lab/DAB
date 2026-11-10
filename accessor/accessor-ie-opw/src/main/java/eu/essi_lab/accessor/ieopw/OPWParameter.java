package eu.essi_lab.accessor.ieopw;

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
 * Station parameters published by the OPW Hydro-Data service, each one with the map layer listing the stations where it is
 * observed
 *
 * @author boldrini
 */
public enum OPWParameter {

    /**
     * Water level, referred to the datum of the station (e.g. Malin Head OSGM15)
     */
    S("10", "Waterlevel", "Water level"), //
    /**
     * Discharge (flow)
     */
    Q("30", "Discharge", "Discharge"), //
    /**
     * Water temperature
     */
    TWater("20", "WaterTemperature", "Water temperature"), //
    /**
     * Precipitation (24 hours moving total)
     */
    P("40", "Rainfall", "Precipitation");

    private final String layerId;
    private final String fileName;
    private final String label;

    /**
     * @param layerId
     * @param fileName
     * @param label
     */
    private OPWParameter(String layerId, String fileName, String label) {

	this.layerId = layerId;
	this.fileName = fileName;
	this.label = label;
    }

    /**
     * @return the identifier of the map layer listing the stations of this parameter
     */
    public String getLayerId() {

	return layerId;
    }

    /**
     * @return the base name of the downloadable files, e.g. "Waterlevel" for Waterlevel_complete.zip
     */
    public String getFileName() {

	return fileName;
    }

    /**
     * @return
     */
    public String getLabel() {

	return label;
    }

    /**
     * @param code the station parameter code, e.g. "S"
     * @return
     */
    public static Optional<OPWParameter> decode(String code) {

	if (code == null) {
	    return Optional.empty();
	}

	for (OPWParameter parameter : values()) {
	    if (parameter.name().equalsIgnoreCase(code.trim())) {
		return Optional.of(parameter);
	    }
	}

	return Optional.empty();
    }
}
