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

public enum PolytopeIonBeamMetadataSmartKitVariable {

        
//	    -	"air_pressure_near_surface": 99.629999999999996,
//	    -    "carbon_dioxide": 363.0,
//	    -    "equivalent_carbon_dioxide": null,
//	    -    "relative_humidity_near_surface": 48.490000000000002,
//	    -    "ambient_light": 4.0,
//	    -    "noise_dBA": 38.340000000000003,
//	    -    "particulate_matter_1": 15.0,
//	    -    "particulate_matter_10": 22.0,
//	    -    "particulate_matter_2_5": 21.0,
//	    -   "air_temperature_near_surface": 29.91,
//	    -   "total_volatile_organic_compounds": null,
//    
    TEMPERATURE(39, "Air Temperature", "air_temperature_near_surface", "Air temperature is a measure of how hot or cold the air is. It is the most commonly measured weather parameter. Air temperature is dependent on the amount and strength of the sunlight hitting the earth, and atmospheric conditions, such as cloud cover and humidity, which trap heat.", "Kelvin"), 
    PRESSURE(107, "Barometric Pressure", "air_pressure_near_surface", "Barometric pressure is the pressure within the atmosphere of Earth. In most circumstances atmospheric pressure is closely approximated by the hydrostatic pressure caused by the weight of air above the measurement point.", "kPa"),
    HUMIDITY(58, "Relative Humidity","relative_humidity_near_surface","Relative humidity is a measure of the amount of moisture in the air relative to the total amount of moisture the air can hold. For instance, if the relative humidity was 50%, then the air is only half saturated with moisture", "percent"),
    CO2(200, "Carbon dioxide (CO2)", "carbon_dioxide", "Carbon dioxide is a colorless gas produced by the combustion of all carbon-based fuels, such as methane (natural gas), petroleum distillates (gasoline, diesel, kerosene, propane), coal, wood and generic organic matter. Carbon dioxide is the most significant long-lived greenhouse gas in Earth's atmosphere.", "ppm"),
    eCO2(201, "Equivalent Carbon dioxide (eCO2)", "equivalent_carbon_dioxide", "Equivalent CO2 is the concentration of CO2 that would cause the same level of radiative forcing as a given type and concentration of greenhouse gas. Examples of such greenhouse gases are methane, perfluorocarbons, and nitrous oxide. CO2 is primarily a by-product of human metabolism and is constantly being emitted into the indoor environment by building occupants. CO2 may come from combustion sources as well. Associations of higher indoor carbon dioxide concentrations with impaired work performance and increased health symptoms have been attributed to correlation of indoor CO2 with concentrations of other indoor air pollutants that are also influenced by rates of outdoor-air ventilation.", "ppm"),
    NOISE(300, "Noise Level", "noise_dBA", "dB's measure sound pressure difference between the average local pressure and the pressure in the sound wave. A quiet library is below 40dB, your house is around 50dB and a diesel truck in your street 90dB.", "dBA"), 
    LIGHT(301, "Ambient Light", "ambient_light", "Lux is a measure of how much light is spread over a given area. A full moon clear night is around 1 lux, inside an office building you usually have 400 lux and a bright day can be more than 20000 lux.", "lux"),
    TVOC(302, "TVOC", "total_volatile_organic_compounds", "Total volatile organic compounds is a grouping of a wide range of organic chemical compounds to simplify reporting when these are present in ambient air or emissions. Many substances, such as natural gas, could be classified as volatile organic compounds (VOCs).", "ppb"),
    PM10(303, "PM 10", "particulate_matter_10", "PM stands for particulate matter: the term for a mixture of solid particles and liquid droplets found in the air. Some particles, such as dust, dirt, soot, or smoke, are large or dark enough to be seen with the naked eye.", "μg/m³"),
    PM1(304, "PM 1", "particulate_matter_1", "PM stands for particulate matter: the term for a mixture of solid particles and liquid droplets found in the air. Some particles, such as dust, dirt, soot, or smoke, are large or dark enough to be seen with the naked eye.", "μg/m³"),
    PM25(305, "PM 2.5", "particulate_matter_2_5", "PM stands for particulate matter: the term for a mixture of solid particles and liquid droplets found in the air. Some particles, such as dust, dirt, soot, or smoke, are large or dark enough to be seen with the naked eye.", "μg/m³");
      
    
    

    private Integer id;

    private String label;
    
    private String key;
    
    private String description;
   
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
    
    public String getDescription() {
	return description;
    }

    public void setDescription(String descr) {
	this.description = descr;
    }

    PolytopeIonBeamMetadataSmartKitVariable(Integer id, String label, String key, String description, String unit) {
	this.id = id;
	this.label = label;
	this.key = key;
	this.description = description;
	this.unit = unit;
    }

    public static PolytopeIonBeamMetadataSmartKitVariable decode(String id) {
	for (PolytopeIonBeamMetadataSmartKitVariable var : values()) {
	    if (var.getKey().toString().equals(id)) {
		return var;
	    }
	}
	return null;
    }
}
