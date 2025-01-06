package eu.essi_lab.accessor.polytope.metadata;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

public enum PolytopeIonBeamMetadataAcronetVariable {

    /**
     * The values of variables are taken from the following:
     * https://confluence.ecmwf.int/display/ODBAPI/Examples
     * Amsterdam:
     * varno=39 => 2m temperature
     * varno=58 => 2m relative humidity
     */
    
//    "wind_gust_direction": null,
//    "rain": 0.0,
//    "internal_temperature": 281.049999999999955,
//    "air_temperature_near_surface": 279.949999999999989,
//    "signal_strength": null,
//    "altitude": 593.750120304068105,
//    "air_pressure_near_surface": 95301.995999999999185,
//    "datetime": "2024-12-13T08:45:00Z",
//    "battery_level": 12.969999,
//    "relative_humidity_near_surface": 62.299999999999997,
//    "wind_direction_near_surface": 313.699999999999989,
//    "wind_speed_near_surface": 0.6173328,
//    "wind_gust": 0.8231104
    
    TEMPERATURE(39, "2m temperature", "air_temperature_near_surface", "Kelvin"), 
    TEMPERATURE_INTERNAL(39,"temperature (internal)", "internal_temperature", "Kelvin"),
    WIND_SPEED(112,"wind speed", "wind_speed_near_surface", "m/s"),
    WIND_DIRECTION(111,"wind direction", "wind_direction_near_surface", "Degrees"),
    GUSTING_WIND_SPEED(114,"gusting wind speed", "wind_gust", "m/s"),
    GUSTING_WIND_DIRECTION(113,"gusting wind direction", "wind_gust_direction", "Degrees"),
    RAIN(115,"Rain", "NIVOMETRO","mm"),
    PRESSURE(107, "Air Pressure", "air_pressure_near_surface", "Pa"),
    HUMIDITY(58, "2m rel. humidity","relative_humidity_near_surface", "percent");
    
    
    
//    TEMPERATURE_MIN("2m temperature (min)", "TERMOMETRO_MIN", "Kelvin"),
//    TEMPERATURE_MAX("2m temperature (max)", "TERMOMETRO_MAX", "Kelvin"),
//    SOIL_TEMPERATURE("soil temperature", "SOIL_TEMPERATURE", "Kelvin"),
//    HUMIDITY("2m rel. humidity", "IGROMETRO","percent"),
//    SOIL_HUMIDITY("soil rel. humidity", "IGROMETRO_SUOLO","percent"),
//    SOLAR_RADIATION_INDEX("Solar Radiation Index", "RADIOMETRO", "units"),
//    PRESSURE("Air Pressure", "BAROMETRO","Pa"),
//    SNOW("Snow cover", "NIVOMETRO","mm"),
//    PM10("Mass Concentration PM10", "PM10", "μg/m³"),    
//    SOLAR_RADIATION_INDEX(25, "Solar Radiation Index", "solar_radiation_index", "units"), 
//    PRESSURE(107, "Air Pressure", "air_pressure_near_surface", "Pa"), 
//    DEW_POINT_TEMPERATURE(40, "Dew Point Temperature", "dew_point_temperature", "Kelvin"),
//    POTENTIAL_TEMPERATURE(3, "Potential Temperature", "potential_temperature", "Kelvin"),
//    VERTICAL_TEMPERATURE_GRADIENT(100, "Vertical Temperature Gradient", "vertical_temperature_gradient", "°C/100m");
    
    
    

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

    PolytopeIonBeamMetadataAcronetVariable(Integer id, String label, String key, String unit) {
	this.id = id;
	this.label = label;
	this.key = key;
	this.unit = unit;
    }

    public static PolytopeIonBeamMetadataAcronetVariable decode(String id) {
	for (PolytopeIonBeamMetadataAcronetVariable var : values()) {
	    if (var.getKey().toString().equals(id)) {
		return var;
	    }
	}
	return null;
    }
}
