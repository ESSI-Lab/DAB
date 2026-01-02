package eu.essi_lab.accessor.polytope.metadata;

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

public enum PolytopeIonBeamMetadataMeteoTrackerVariable {

    /**
     * The values of variables are taken from the following:
     * https://confluence.ecmwf.int/display/ODBAPI/Examples
     * Amsterdam:
     * varno=39 => 2m temperature
     * varno=58 => 2m relative humidity
     */

    TEMPERATURE(39, "2m temperature", "air_temperature_near_surface", "Kelvin"), 
    HUMIDITY(58, "2m rel. humidity","relative_humidity_near_surface", "percent"),
    SOLAR_RADIATION_INDEX(25, "Solar Radiation Index", "solar_radiation_index", "units"), 
    PRESSURE(107, "Air Pressure", "air_pressure_near_surface", "Pa"), 
    DEW_POINT_TEMPERATURE(40, "Dew Point Temperature", "dew_point_temperature", "Kelvin"),
    POTENTIAL_TEMPERATURE(3, "Potential Temperature", "potential_temperature", "Kelvin"),
    VERTICAL_TEMPERATURE_GRADIENT(100, "Vertical Temperature Gradient", "vertical_temperature_gradient", "°C/100m");

    private Integer id;

    private String label;
    
    private String key;
   
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
    
    public String getKey() {
	return key;
    }

    public void setKey(String key) {
	this.key = key;
    }

    PolytopeIonBeamMetadataMeteoTrackerVariable(Integer id, String label, String key, String unit) {
	this.id = id;
	this.label = label;
	this.key = key;
	this.unit = unit;
    }

    public static PolytopeIonBeamMetadataMeteoTrackerVariable decode(String id) {
	for (PolytopeIonBeamMetadataMeteoTrackerVariable var : values()) {
	    if (var.getKey().toString().equals(id)) {
		return var;
	    }
	}
	return null;
    }
}
