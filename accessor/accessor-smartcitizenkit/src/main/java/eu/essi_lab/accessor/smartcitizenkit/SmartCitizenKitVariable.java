package eu.essi_lab.accessor.smartcitizenkit;

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

public enum SmartCitizenKitVariable {

    TEMPERATURE("2m temperature", "air_temperature_near_surface", "Kelvin"), HUMIDITY("2m rel. humidity", "relative_humidity_near_surface",
	    "percent"), SOLAR_RADIATION_INDEX("Solar Radiation Index", "solar_radiation_index", "units"), PRESSURE("Air Pressure",
		    "air_pressure_near_surface",
		    "Pa"), DEW_POINT_TEMPERATURE("Dew Point Temperature", "dew_point_temperature", "Kelvin"), VERTICAL_TEMPERATURE_GRADIENT(
			    "Vertical Temperature Gradient", "vertical_temperature_gradient", "°C/100m"),

    // HDX("Humidex", "°C"),
    SOUND("Sound", "myair", "km/h"), LIGHT("Solar Radiation Index", "myair", "#"), UVB("Ultraviolet B", "myair", "#"),
    // CO2("CO2", "ppm"),
    PM1("Mass Concentration PM1.0", "myair", "μg/m³"), PM25("Mass Concentration PM2.5", "myair", "μg/m³"), PM10("Mass Concentration PM10",
	    "myair",
	    "μg/m³"), PC03("Number of particulate concentration PC0.3", "myair", "#/cm³"), PC05("Number of particulate concentration PM0.5",
		    "myair", "#/cm³"), PC1("Number of particulate concentration PC1.0", "myair", "#/cm³"), PC25(
			    "Number of particulate concentration PC2.5", "myair", "#/cm³"), PC5("Number of particulate concentration PC5.0",
				    "myair", "#/cm³"), PC10("Number of particulate concentration PC10", "myair", "#/cm³");

    private String label;

    private String key;

    private String unit;

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

    SmartCitizenKitVariable(String label, String key, String unit) {

	this.label = label;
	this.key = key;
	this.unit = unit;
    }

    public static SmartCitizenKitVariable decode(String id) {
	for (SmartCitizenKitVariable var : values()) {
	    if (var.getKey().toString().equals(id)) {
		return var;
	    }
	}
	return null;
    }
}
