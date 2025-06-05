package eu.essi_lab.accessor.waf.trigger;

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


public enum TRIGGERWAFVariable {

    TEMPERATURE("2m temperature (2T)", "2t", "This parameter is the temperature of air at 2m above the surface of land, sea or in-land waters. 2m temperature is calculated by interpolating between the lowest model level and the Earth's surface, taking account of the atmospheric conditions.", "°C"),
    HUMIDITY("2m relative humidity (2R)", "2r", "The ratio of the partial pressure of water vapour to the equilibrium vapour pressure of water at the same temperature near the surface. Note that the specific height level above ground might vary from one centre to another.","%"),
    WBGT("Wet-bulb globe temperature (WBGT)", "wbgt", "The wet bulb globe temperature is a measure of environmental heat as it affects humans. It is derived from air temperature, dew point temperature, wind speed and mean radiant temperature.", "°C"),
    UTCI("Universal thermal climate index (UTCI)","utci","The Universal Thermal Climate Index (UTCI) is defined as the air temperature of a reference outdoor environment that would elicit in the human body the same physiological response (sweat production, shivering, skin wettedness,skin blood flow and rectal, mean skin and face temperatures) as the actual environment.","°C"),
    MRT("Mean radiant temperature (MRT)","mrt","The mean radiant temperature is the temperature of a uniform, black enclosure that exchanges the same amount of heat by radiation with the occupant as the actual surroundings","°C"),
    WCT("Wind chill temperature (WCT)","wct","The wind chill factor describes the cooling sensation felt by the human body and is based on the rate of heat loss from exposed skin caused by the effects of wind and cold. It is derived from air temperature and wind speed.", "°C"),
      
    RAGWEED_POLLEN("Ragweed pollen","cams_eu_ragweed_pollen_web","Ragweed is a species native to the American continent but has become an invasive species in Europe, causing allergies in many people. The blossom starts in August and its highest pollen season is in September.", "grains/m³"),
    PM2_5("Particulate matter 2.5 (PM2.5)","cams_eu_particulate_matter_2.5um_web","These refer to airborne particles with diameters less than 2.5 micrometers (PM2.5).", "µg/m³"),
    PM10("Particulate matter 10 (PM10)","cams_eu_particulate_matter_10um_web","These refer to airborne particles with diameters less than 10 micrometers (PM10).", "µg/m³"),
    OZONE("Ozone","cams_eu_ozone_web","A reactive gas composed of three oxygen atoms, ozone is found in both the Earth's upper atmosphere and at ground level.", "µg/m³"),
    NO2("Nitrogen dioxide (NO2)","cams_eu_nitrogen_dioxide_web","A significant air pollutant produced mainly from vehicle emissions and industrial processes.", "µg/m³"),
    MUGWORT_POLLEN("Mugwort pollen","cams_eu_mugwort_pollen_web","The AUX provides forecasts for various pollen types, measured in grains per cubic meter.", "grains/m³"),
    GRASS_POLLEN("Grass pollen","cams_eu_grass_pollen_web","The AUX provides forecasts for various pollen types, measured in grains per cubic meter.", "grains/m³"),
    BIRCH_POLLEN("Birch pollen","cams_eu_birch_pollen_web","The AUX provides forecasts for various pollen types, measured in grains per cubic meter.", "grains/m³");
    

    
    //    TEMPERATURE_MIN("2m temperature (min)", "TERMOMETRO_MIN", "Kelvin"),
//    TEMPERATURE_MAX("2m temperature (max)", "TERMOMETRO_MAX", "Kelvin"),
//    TEMPERATURE_INTERNAL("temperature (internal)", "TERMOMETRO_INTERNA", "Kelvin"),
//    SOIL_TEMPERATURE("soil temperature", "SOIL_TEMPERATURE", "Kelvin");
   
//    SOIL_HUMIDITY("soil rel. humidity", "IGROMETRO_SUOLO","percent"),
//    WIND_SPEED("wind speed", "ANEMOMETRO", "m/s"),
//    WIND_DIRECTION("wind direction", "DIREZIONEVENTO", "Degrees"),
//    GUSTING_WIND_SPEED("gusting wind speed", "ANEMOMETRO_RAFFICA", "m/s"),
//    GUSTING_WIND_DIRECTION("gusting wind direction", "DIREZIONEVENTO_RAFFICA", "Degrees"),
//    SOLAR_RADIATION_INDEX("Solar Radiation Index", "RADIOMETRO", "units"),
//    PRESSURE("Air Pressure", "BAROMETRO","Pa"),
//    SNOW("Snow cover", "NIVOMETRO","mm"),
//    PM10("Mass Concentration PM10", "PM10", "μg/m³");
    
    
    private String label;

    private String key;
    
    private String description;

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
    
    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    TRIGGERWAFVariable(String label, String key, String description, String unit) {

	this.label = label;
	this.key = key;
	this.description = description;
	this.unit = unit;
    }

    public static TRIGGERWAFVariable decode(String id) {
	for (TRIGGERWAFVariable var : values()) {
	    if (var.getKey().toString().equals(id)) {
		return var;
	    }
	}
	return null;
    }
    
}
