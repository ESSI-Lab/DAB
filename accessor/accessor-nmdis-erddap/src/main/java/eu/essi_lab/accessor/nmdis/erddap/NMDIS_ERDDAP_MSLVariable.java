package eu.essi_lab.accessor.nmdis.erddap;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

public enum NMDIS_ERDDAP_MSLVariable {

    SLEV("water surface height above a specific datum", "m") //
    ;

    NMDIS_ERDDAP_MSLVariable(String name, String units) {
	this.name = name;
	this.units = units;
    }

    private String name;
    private String units;

    public String getName() {
	return name;
    }

    public String getUnits() {
	return units;
    }

    public static NMDIS_ERDDAP_MSLVariable decode(String name) {
	for (NMDIS_ERDDAP_MSLVariable variable : values()) {
	    if (variable.getName().equals(name)) {
		return variable;
	    }
	}
	return valueOf(name);
    }
    

}
