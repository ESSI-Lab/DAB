package eu.essi_lab.model.resource;

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

import java.util.UUID;

/**
 * @author Fabrizio
 */
public enum BNHSProperty {
    HYCOSID("HYCOSID", UUID.class, MetadataElement.HYCOS_IDENTIFIER,true), //
    STATION_ID("StationID", String.class, MetadataElement.STATION_IDENTIFIER,true), //
    COUNTRY("Country", String.class, MetadataElement.COUNTRY,true), //
    INSTITUTE("Institute", String.class, MetadataElement.ORGANISATION_NAME,true), //
    STATION_NAME("StationName", String.class, MetadataElement.PLATFORM_TITLE,true), //
    RIVER("River", String.class, MetadataElement.RIVER,true), //
    LAKE_STATION("Lake Station", String.class, MetadataElement.LAKE_STATION,true), //
    GRDC_ID("GRDC ID", String.class, MetadataElement.GRDC_ID,true), //
    GRDC_ARDB("GRDC ARDB", String.class, MetadataElement.GRDC_ARDB,true), //
    WMO_REGION("WMO Region", String.class, MetadataElement.WMO_REGION,true), //
    LATITUDE("Latitude", Double.class, MetadataElement.LATITUDE,false), //
    LONGITUDE("Longitude", Double.class, MetadataElement.LONGITUDE,false), //
    LATITUDE_OF_DISCHARGE("Latitude of discharge", Double.class, MetadataElement.LATITUDE_OF_DISCHARGE,true), //
    LONGITUDE_OF_DISCHARGE("Longitude of discharge", Double.class, MetadataElement.LONGITUDE_OF_DISCHARGE,true), //
    STATUS("Status", String.class, MetadataElement.STATUS,true), //
    DRAINAGE_AREA("Drainage Area", Double.class, MetadataElement.DRAINAGE_AREA,true), //
    EFFECTIVE_DRAINAGE_AREA("Effective Drainage Area", Double.class, MetadataElement.EFFECTIVE_DRAINAGE_AREA,true), //
    DRAINAGE_SHAPEFILE("Drainage Shapefile", String.class, MetadataElement.DRAINAGE_SHAPEFILE,true), //
    DATUM_NAME("Datum Name", String.class, MetadataElement.ALTITUDE_DATUM,true), //
    DATUM_ALTITUDE("Datum Altitude", Double.class, MetadataElement.ALTITUDE,false), //
    FLOW_TO_OCEAN("Flow to Ocean", String.class, MetadataElement.FLOW_TO_OCEAN,true), //
    DOWNSTREAM_HYCOS_STATION("Downstream HYCOS station", String.class, MetadataElement.DOWNSTREAM_HYCOS_STATION,true), //
    REGULATION("Regulation", String.class, MetadataElement.REGULATION,true), //
    REGULATION_START_DATE("Regulation Start Date", java.util.Date.class, MetadataElement.REGULATION_START_DATE,true), //
    REGULATION_END_DATE("Regulation End Date", java.util.Date.class, MetadataElement.REGULATION_END_DATE,true), //
    LAND_USE_CHANGE("Land Use Change", String.class, MetadataElement.LAND_USE_CHANGE,false), //
    SURFACE_COVER("Surface Cover", String.class, MetadataElement.SURFACE_COVER,true), //
    DATA_QUALITY_ICE("Data_quality_ice", String.class, MetadataElement.DATA_QUALITY_ICE,false), //
    DATA_QUALITY_OPEN("Data_quality_open", String.class, MetadataElement.DATA_QUALITY_OPEN,false), //
    DISCHARGE_AVAILABILITY("Discharge Availability", String.class, MetadataElement.DISCHARGE_AVAILABILITY,true), //
    WATER_LEVEL_AVAILABILITY("Water Level Availability", String.class, MetadataElement.WATER_LEVEL_AVAILABILITY,true), //
    WATER_TEMPERATURE_AVAILABILITY("Water Temperature Availability", String.class, MetadataElement.WATER_TEMPERATURE_AVAILABILITY,true), //
    ICE_ON_OFF_AVAILABILITY("Ice On / Off Dates Availability", String.class, MetadataElement.ICE_ON_OFF_AVAILABILITY,false), //
    ICE_THICKNESS_AVAILABILITY("Ice Thickness Availability", String.class, MetadataElement.ICE_THICKNESS_AVAILABILITY,true), //
    SNOW_DEPTH_AVAILABILITY("Snow Depth Availability", String.class, MetadataElement.SNOW_DEPTH_AVAILABILITY,true), //
    MEASUREMENT_METHOD_DISCHARGE("Measurement Method - Discharge", String.class, MetadataElement.MEASUREMENT_METHOD_DISCHARGE,true), //
    MEASUREMENT_METHOD_WATER_TEMPERATURE("Measurement Method - Water Temperature (computed vs. observed)", String.class,
	    MetadataElement.MEASUREMENT_METHOD_WATER_TEMPERATURE,true), //
    MEASUREMENT_METHOD_ICE_ON_OFF("Measurement Method - Ice On / Off Dates", String.class, MetadataElement.MEASUREMENT_METHOD_ICE_ON_OFF,true), //
    EQUIPMENT("Equipment", String.class, MetadataElement.INSTRUMENT_TITLE,true), //
    STATION_NOTES("Station Notes (INTERNAL USE ONLY from here on)", String.class, null,false), //
    START_YEAR("StartYear", String.class, null,false), //
    END_YEAR("EndYear", String.class, null,false), //
    N_YEARS("NYears", String.class, null,false), //
    REAL_TIME("RealTime", String.class, null,false), //
    WATER_TEMPERATURE_START_DATE("Water Temp start date", String.class, null,true), //
    WATER_TEMPERATURE_END_DATE("Water Temp end date", String.class, null,true), //
    SAMPLING_INTERVAL("Sampling interval (temporal frequency)", String.class, null,true), //
    SCRIPT_LINK_FOR_ARCHIVED_DATA("Script-link for Archived data", String.class, null,false), //
    NOTES_FOR_ARCHIVED_DATA("Notes for archived data", String.class, null,false), //
    SCRIPT_LINK_FOR_REAL_TIME_DATA("Script-link for Real-time data", String.class, null,false), //
    NOTES_FOR_REAL_TIME_DATA("Notes for real-time data", String.class, null,false), //
    SCRIPT_LINK_FOR_WATER_TEMPERATURE_DATA("Script-link for Water Temp data", String.class, null,true), //
    NOTES_WATER_TEMPERATURE("Notes for water temp data", String.class, null,true), //
    NOTES_ICE_ON_OFF("Notes for ice on/off dates and thickness", String.class, null,true), //
    NOTES_SNOW_DEPTH("Notes for snow depth", String.class, null,true);

    private MetadataElement element;

    public MetadataElement getElement() {
	return element;
    }

    /**
     * @param label
     */
    private BNHSProperty(String label, Class<?> type, MetadataElement element, boolean inWML) {

	this.label = label;

	this.type = type;

	this.element = element;

	this.inWML = inWML;
    }

    private String label;

    private Class<?> type;

    private boolean inWML = false;

    public boolean isInWML() {
	return inWML;
    }

    /**
     * @return
     */
    public String getLabel() {

	return label;
    }

    public Class<?> getType() {

	return type;
    }

    /**
     * @param label
     * @return
     */
    public static BNHSProperty decode(String label) {
	for (BNHSProperty c : values()) {
	    if (c.getLabel().equals(label) || c.name().equals(label)) {
		return c;
	    }
	}
	return null;
    }

}
