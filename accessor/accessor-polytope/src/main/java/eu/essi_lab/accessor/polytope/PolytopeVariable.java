package eu.essi_lab.accessor.polytope;

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

public enum PolytopeVariable {

    /**
     * The values of variables are taken from the following:
     * https://confluence.ecmwf.int/display/ODBAPI/Examples
     * Amsterdam:
     * varno=39 => 2m temperature
     * varno=58 => 2m relative humidity
     */

    TEMPERATURE(39, "2m temperature", "Kelvin"), HUMIDITY(58, "2m rel. humidity", "percent");

    private Integer id;

    private String label;
   
    private String unit;

    public Integer getId() {
	return id;
    }

    public String getLabel() {
	return label;
    }
    
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    PolytopeVariable(Integer id, String label, String unit) {
	this.id = id;
	this.label = label;
	this.unit = unit;
    }

    public static PolytopeVariable decode(String id) {
	for (PolytopeVariable var : values()) {
	    if (var.getId().toString().equals(id)) {
		return var;
	    }
	}
	return null;
    }
}
