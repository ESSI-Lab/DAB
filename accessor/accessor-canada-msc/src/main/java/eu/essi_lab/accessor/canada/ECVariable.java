package eu.essi_lab.accessor.canada;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

public enum ECVariable {

    /**
     * The values of variables are taken from the following README:
     * http://dd.weather.gc.ca/hydrometric/doc/hydrometric_README.txt
     */

    WATER_LEVEL("Water Level", "m"), //
    DISCHARGE("Discharge", "cms");

    String label;
    String unitAbbreviation;

    ECVariable(String label, String unitAbbreviation) {
	this.label = label;
	this.unitAbbreviation = unitAbbreviation;
    }

    public String getLabel() {
	return label;
    }

    public void setLabel(String label) {
	this.label = label;
    }

    public String getUnitAbbreviation() {
	return unitAbbreviation;
    }

    public void setUnitAbbreviation(String abbreviation) {
	this.unitAbbreviation = abbreviation;
    }

    @Override
    public String toString() {
	return label;
    }

    public static ECVariable decode(String variable) {
	for (ECVariable ret : ECVariable.values()) {
	    if (ret.toString().toLowerCase().contains(variable.toLowerCase())
		    || variable.toLowerCase().contains(ret.toString().toLowerCase())) {
		return ret;
	    }
	}
	return null;
    }

}
