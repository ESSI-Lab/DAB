package eu.essi_lab.accessor.acronet;

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

public enum AcronetVariable {
    
//    [
//     "PLUVIOMETRO",
//     "IDROMETRO",
//     "IDROMETRO_PORTATA",
//     "IDROMETRO_VELOCITA",
//     "NIVOMETRO",
//     "TERMOMETRO",
//     "IGROMETRO",
//    "DIREZIONEVENTO",
//  	"ANEMOMETRO",
//     "BAROMETRO",
//     "RADIOMETRO",
//     "INVASO",
//     "TURBINATA",
//     "PLUVIOMETRO_OTT",
//     "PORTATA_PRESA",
//     "PORTATA_ENTRANTE_STIMATA",
//     "BATTERIA",
//     "OZONO",
//     "PM10",
//     "IGROMETRO_SUOLO",
//     "IGROMETRO_SUOLO_10",
//     "IGROMETRO_SUOLO_20",
//     "IGROMETRO_SUOLO_40",
//     "CO",
//     "NO2",
//     "C6H6",
//     "TERMOMETRO_INTERNA",
//     "TEMPERATURA_OZONO",
//     "DIREZIONEVENTO_RAFFICA",
//     "ANEMOMETRO_RAFFICA",
//     "TERMOMETRO_MIN",
//     "TERMOMETRO_MAX",
//     "FUEL_TEMPERATURE",
//     "FUEL_MOISTURE",
//     "SOIL_TEMPERATURE",
//     "IONIZING_RADIATION",
// ]

    TEMPERATURE("2m temperature", "TERMOMETRO", "Kelvin"),
    TEMPERATURE_MIN("2m temperature (min)", "TERMOMETRO_MIN", "Kelvin"),
    TEMPERATURE_MAX("2m temperature (max)", "TERMOMETRO_MAX", "Kelvin"),
    TEMPERATURE_INTERNAL("temperature (internal)", "TERMOMETRO_INTERNA", "Kelvin"),
    SOIL_TEMPERATURE("soil temperature", "SOIL_TEMPERATURE", "Kelvin"),
    HUMIDITY("2m rel. humidity", "IGROMETRO","percent"),
    SOIL_HUMIDITY("soil rel. humidity", "IGROMETRO_SUOLO","percent"),
    WIND_SPEED("wind speed", "ANEMOMETRO", "m/s"),
    WIND_DIRECTION("wind direction", "DIREZIONEVENTO", "Degrees"),
    GUSTING_WIND_SPEED("gusting wind speed", "ANEMOMETRO_RAFFICA", "m/s"),
    GUSTING_WIND_DIRECTION("gusting wind direction", "DIREZIONEVENTO_RAFFICA", "Degrees"),
    SOLAR_RADIATION_INDEX("Solar Radiation Index", "RADIOMETRO", "units"),
    PRESSURE("Air Pressure", "BAROMETRO","Pa"),
    SNOW("Snow cover", "NIVOMETRO","mm"),
    PM10("Mass Concentration PM10", "PM10", "μg/m³");
    
    
//    DEW_POINT_TEMPERATURE("Dew Point Temperature", "dew_point_temperature", "Kelvin"), VERTICAL_TEMPERATURE_GRADIENT(
//			    "Vertical Temperature Gradient", "vertical_temperature_gradient", "°C/100m"),
//
//    // HDX("Humidex", "°C"),
//    SOUND("Sound", "myair", "km/h"), LIGHT("Solar Radiation Index", "myair", "#"), UVB("Ultraviolet B", "myair", "#"),
//    // CO2("CO2", "ppm"),
//    PM1("Mass Concentration PM1.0", "myair", "μg/m³"), PM25("Mass Concentration PM2.5", "myair", "μg/m³"), PC03("Number of particulate concentration PC0.3", "myair", "#/cm³"), PC05("Number of particulate concentration PM0.5",
//		    "myair", "#/cm³"), PC1("Number of particulate concentration PC1.0", "myair", "#/cm³"), PC25(
//			    "Number of particulate concentration PC2.5", "myair", "#/cm³"), PC5("Number of particulate concentration PC5.0",
//				    "myair", "#/cm³"), PC10("Number of particulate concentration PC10", "myair", "#/cm³");

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

    AcronetVariable(String label, String key, String unit) {

	this.label = label;
	this.key = key;
	this.unit = unit;
    }

    public static AcronetVariable decode(String id) {
	for (AcronetVariable var : values()) {
	    if (var.getKey().toString().equals(id)) {
		return var;
	    }
	}
	return null;
    }
    
    
    public static void main(String[] args) {
	AcronetVariable var = AcronetVariable.decode("TEMPERATURE");
	System.out.println(var);
    }
}
