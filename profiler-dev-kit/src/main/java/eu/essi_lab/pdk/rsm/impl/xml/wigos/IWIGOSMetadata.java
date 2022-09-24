package eu.essi_lab.pdk.rsm.impl.xml.wigos;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.math.BigInteger;
import java.util.Date;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import eu.floraresearch.wigos._1_0.gmd.MDMetadataType;
import eu.floraresearch.wigos._1_0.gml._3_2_1.VerticalDatumType;
import eu.floraresearch.wigos._1_0.main.ControlCheckReportType;
import eu.floraresearch.wigos._1_0.main.LogType.LogEntry;
import eu.floraresearch.wigos._1_0.main.MaintenanceReportType;

public interface IWIGOSMetadata {

    // HEADER
    void setHeaderInformation(Date fileDate, String contactSurname, String contactName, String contactTitle, String organizationShort,
	    String contactEmail, String organizationURL);

    // CATEGORY 1: OBSERVED VARIABLE
    /**
     * Sets the Observed variable - measurand (1-01). M* (Phase I)
     * 
     * @param observationVariableCode a code from the code list at:
     *        https://docs.google.com/spreadsheets/d/19ZGKKJrpvK-2OsO0TTrewWWC1mCEGgk35xeFN1jxm74/edit#gid=0
     */
    void setObservedVariable(String name, String code);

    /**
     * Sets the feature of interest. Apparently missing from WIGOS.
     * 
     * @param foiCode
     */
    void setFeatureOfInterest(String foiCode);

    /**
     * Sets the Measurement unit (1-02). C* (Phase I) Mandatory for variables that are measured, as opposed to
     * classified
     * 
     * @param uomCode the unit of measure from the code list at:
     *        https://docs.google.com/spreadsheets/d/129h1yGJZ5m16xETpp-qGEFKBWqAG146RgrWHx-LYcjA/edit#gid=0
     */
    void setMeasurementUnit(String uomCode);

    /**
     * Sets the Spatial extent (1-04). M* (Phase I)
     * 
     * @param text
     */
    void setSpatialExtent(String text);

    /**
     * Sets the Representativeness (1-05). O (Phase II)
     * 
     * @param representativenessCode a code from http://test.wmocodes.info/wmdr/Representativeness
     */
    void setRepresentativeness(String representativenessCode);

    // CATEGORY 2: PURPOSE OF OBSERVATION
    /**
     * Sets the Application area (2-01). M* (Phase I)
     * 
     * @param applicationAreaCode code from http://test.wmocodes.info/wmdr/ApplicationArea
     */
    void setApplicationArea(String applicationAreaCode);

    /**
     * Sets the Programme / network affiliation (2-02). M (Phase I)
     * 
     * @param programCode a code from
     *        https://docs.google.com/spreadsheets/d/1ol4dLqU4w_DkPUrkKqyYJu7nR9jXKkvfiXddvuT-CMI/edit?usp=sharing
     */
    void addProgramme(String programCode);

    // CATEGORY 3: STATION/PLATFORM
    /**
     * Sets the Region of origin of data (3-01). C* (Phase I) Mandatory for fixed land-based stations, optional for
     * mobile stations
     * 
     * @param regionCode a code from http://test.wmocodes.info/wmdr/WMORegion
     */
    void setRegionOfOrigin(String regionCode);

    /**
     * Sets the Territory of origin of data (3-02). C* (Phase I) Mandatory for fixed land-based stations, optional for
     * mobile stations
     * 
     * @param territoryCode a code from http://test.wmocodes.info/wmdr/TerritoryName
     * @param validPeriodBegin optional valid period
     * @param validPeriodEnd optional valid period
     */
    void setTerritoryOfOrigin(String territoryCode, String validPeriodBegin, String validPeriodEnd);

    /**
     * Sets the Station / platform name (3-03). M (Phase I)
     */
    void setStationOrPlatformName(String name);

    /**
     * Sets the Station / platform description. Apparently not required by WIGOS.
     * 
     * @param description
     */
    void setStationOrPlatformDescription(String description);

    /**
     * Sets the Station / platform type (3-04). M* (Phase II)
     * 
     * @param facilityCode a code from http://test.wmocodes.info/wmdr/FacilityType
     */
    void setStationOrPlatformType(String facilityCode);

    /**
     * Sets the Station / platform model (3-05). M*# (Phase III)
     */
    void setStationOrPlatformModel();

    /**
     * Sets the Station / platform unique indentifier (3-06). M* (Phase I)
     * A globally unique identifier assigned by WMO for a station.
     */
    void setStationOrPlatformIdentifier(String id);

    /**
     * Sets the Station / platform unique established date. Apparently not required by WIGOS.
     */
    void setStationOrPlatformDateEstablished(XMLGregorianCalendar date);

    /**
     * Sets the geospatial location of the observing station / platform (3-07). M* (Phase I)
     * 
     * @param geopositioningMethod a code from http://test.wmocodes.info/wmdr/GeopositioningMethod
     * @param crs
     * @param elevation
     * @param longitude
     * @param latitude
     * @param validTimeBegin the valid time begin for this instrument position (optional)
     * @param validTimeEnd the valid time end for this instrument position (optional)
     */
    void addStationGeospatialLocation(Double latitude, Double longitude, Double elevation, String crs, String geopositioningMethod,
	    String validTimeBegin, String validTimeEnd);

    /**
     * Sets the data communication method (3-08). O (Phase II)
     * 
     * @param communicationMethodCode a code from http://test.wmocodes.info/wmdr/DataCommunicationMethod
     */
    void setDataCommunicationMethod(String communicationMethodCode);

    /**
     * Sets the station operating status (3-09). M (Phase I)
     */
    void setStationOperatingStatus();

    /**
     * Sets the surface cover (4-01). C # (Phase III) For hydrologic observations, specifying a nilReason value is
     * acceptable
     * 
     * @param codespace a codespace, such as http://codes.wmo.int/wmdr/SurfaceCoverIGBP
     * @param surfaceCoverCode a code from http://test.wmocodes.info/wmdr/SurfaceCoverIGBP,
     *        http://test.wmocodes.info/wmdr/SurfaceCoverUMD, http://test.wmocodes.info/wmdr/SurfaceCoverLAI,
     *        http://test.wmocodes.info/wmdr/SurfaceCoverNPP, http://test.wmocodes.info/wmdr/SurfaceCoverPFT,
     *        http://test.wmocodes.info/wmdr/SurfaceCoverLCCS
     *        @param surfaceCoverClassificationCode a code from http://test.wmocodes.info/wmdr/SurfaceCoverClassification surface cover classification scheme (4-02). C # (Phase III) For hydrologic observations, specifying a
     * nilReason value is acceptable
     */
    void setSurfaceCover(String codespace, String surfaceCoverCode,String surfaceCoverClassificationCode);

    /**
     * Sets the topography or bathymetry (4-03). C # (Phase III) For hydrologic observations, specifying a nilReason
     * value is acceptable
     * 
     * @param localTopographyCode a code from
     *        https://docs.google.com/spreadsheets/d/1nMYF4SOQy7AbSOMFWHI3IThpFusWK6FE2G1gePH9JWg/edit#gid=0
     * @param relativeElevationCode a code from
     *        https://docs.google.com/spreadsheets/d/19duCIxqkTAlKwItYwV07v2AiI2ZIdGmellnTfuuLIMQ/edit?usp=sharing
     * @param topographicContextCode a code from
     *        https://docs.google.com/spreadsheets/d/1SY7MHwcpruSA41YYjw3Gax0F9AfS4oNJnUJRzeeA7Oc/edit#gid=0
     * @param altitudeOrDepthCode a code from
     *        https://docs.google.com/spreadsheets/d/1gOXh7H_Ihl6dLboVo1rfbIoqFwVGenIwYjx_kgR18WM/edit#gid=0
     * @param beginPosition
     * @param endPosition
     */
    void setTopographyOrBathymetry(String beginPosition, String endPosition, String localTopographyCode, String relativeElevationCode,
	    String topographicContextCode, String altitudeOrDepthCode);

    /**
     * Sets the events at observing facility (4-04). O (Phase II)
     */
    void addEventsAtObservingFacility(LogEntry logEntry);

    /**
     * Sets the site information (4-05). O (Phase II)
     */
    void setSiteInformation();

    /**
     * Sets the surface roughness (4-06). O (Phase III)
     * 
     * @param surfaceRoughnessCode a code from http://test.wmocodes.info/wmdr/SurfaceRoughnessDavenport
     * @param beginPosition
     * @param endPosition
     */
    void setSurfaceRoughness(String surfaceRoughnessCode, String beginPosition, String endPosition);

    /**
     * Sets the climate zone (4-07). O (Phase III)
     * 
     * @param climateZoneCode a code from http://test.wmocodes.info/wmdr/ClimateZone
     * @param beginPosition
     * @param endPosition
     */
    void setClimateZone(String climateZoneCode, String beginPosition, String endPosition);

    // CATEGORY 5: INSTRUMENTS AND METHODS OF OBSERVATION
    /**
     * Sets the source of observation (5-01). M (Phase I)
     * 
     * @param sourceOfObservationCode a code from http://test.wmocodes.info/wmdr/SourceOfObservation
     */
    void setSourceOfObservation(String sourceOfObservationCode);

    /**
     * Sets the measurement / observing method (5-02). M # (Phase I)
     * 
     * @param measurementMethodCode a code from
     *        https://docs.google.com/spreadsheets/d/1Bf6o3Lbkai1uttpwUoOh1jGRjzukUq4b9iYmfH-VjD8/edit#gid=0
     */
    void setMeasurementMethod(String measurementMethodCode);

    /**
     * InstrumentSpecifications (5-03). C* # (Phase I) Mandatory for instrumental observations
     */
    void setInstrumentSpecifications(String observableRange, String specifiedAbsoluteUncertainty, String specifiedRelativeUncertainty,
	    String driftPerUnitTime, String specificationLink);

    /**
     * Instrument Operating Status (5-04). O (Phase III)
     * 
     * @param operatingStatusCode an operating status from the list at
     *        http://test.wmocodes.info/wmdr/InstrumentOperatingStatus
     */
    void addInstrumentOperatingStatus(String operatingStatusCode, String beginPosition, String endPosition);

    /**
     * Vertical Distance Of Sensor (5-05). C* (Phase I) Mandatory for instrumental observations and if proximity of
     * reference surface impacts on
     * observation
     * 
     * @param localReferenceSurfaceCode a code from
     *        https://docs.google.com/spreadsheets/d/1ik1LcgEZUbWjkVudWFqi7Ro-d3jiZ5HCCpMQpAaaNL8/edit#gid=0
     */
    void setVerticalDistanceOfSensor(String uom, double value, String localReferenceSurfaceCode);

    /**
     * Configuration Of Instrumentation (5-06). C # (Phase III) Mandatory for instrumental observations and if
     * prescribed by best practice / A nilReason
     * = ”not applicable” is acceptable for space-based observations
     */
    void setConfigurationOfInstrumentation(String configuration);

    /**
     * Instrument Control Schedule (5-07). C (Phase III) Mandatory for instrumental observations
     * 
     * @param controlSchedule
     */
    void setInstrumentControlSchedule(String controlSchedule);

    /**
     * Instrument Control Result(5-08). C# (Phase III)
     */
    void addInstrumentControlResult(ControlCheckReportType controlCheck);

    /**
     * Instrument Model Serial Number (5-09). C # (Phase III)
     */
    void setInstrumentModelSerialNumber(String manufacturer, String modelNumber, String serialNumber, String firmwareVersion);

    /**
     * Instrument Routine Maintenance (5-10). C # (Phase III) A nilReason = ”not applicable” is acceptable for
     * space-based observations
     * 
     * @param maintenanceSchedule
     */
    void setInstrumentRoutineMaintenance(String maintenanceSchedule);

    /**
     * Maintenance Party (5-11). O (Phase II)
     * Maintenance Activity (5-13). O (Phase III)
     */
    void addMaintenanceReport(MaintenanceReportType maintenance);

    /**
     * Instrument Geospatial Location (5-12). C* (Phase II) Mandatory for instrumental observations and if different from station/platform / A
     * nilReason = ”not applicable” is acceptable for space-based observations
     * 
     * @param geopositioningMethod a code from http://test.wmocodes.info/wmdr/GeopositioningMethod
     * @param crs
     * @param elevation
     * @param longitude
     * @param latitude
     * @param validTimeBegin the valid time begin for this instrument position (optional)
     * @param validTimeEnd the valid time end for this instrument position (optional)
     */
    void addInstrumentGeospatialLocation(Double latitude, Double longitude, Double elevation, String crs, String geopositioningMethod,
	    String validTimeBegin, String validTimeEnd);

    /**
     * Status Of Observation (5-14). O (Phase III)
     */
    void setStatusOfObservation(Boolean officialStatus);

    /**
     * Exposure Of Instruments (5-15). C # (Phase II) A nilReason = ”not applicable” is acceptable for space-based
     * observations
     * 
     * @param exposureTypeCode a code from http://test.wmocodes.info/wmdr/Exposure
     */
    void setExposureOfInstruments(String exposureTypeCode);

    // CATEGORY 6: SAMPLING
    /**
     * Sampling Procedures (6-01). O (Phase III)
     * 
     * @param samplingProcedureDescription
     * @param samplingProcedureCode a code from
     *        https://docs.google.com/spreadsheets/d/1MgJAjo7fIWJoFxlaGYRSAahBH5T6XTaOmfzxKUzKVNY/edit#gid=0
     */
    void setSamplingProcedures(String samplingProcedureCode, String samplingProcedureDescription);

    /**
     * Sample Treatment (6-02). O (Phase III)
     * 
     * @param sampleTreatmentCode a code from
     *        https://docs.google.com/spreadsheets/d/1hjNk_1tf0nwh2wtKySZlbl4l38ast-Nz1bZY8IInL-g/edit?usp=sharing
     */
    void setSampleTreatment(String sampleTreatmentCode);

    /**
     * Sampling Strategy (6-03). O* (Phase I)
     * 
     * @param strategyCode code from http://test.wmocodes.info/wmdr/SamplingStrategy
     */
    void setSamplingStrategy(String strategyCode);

    /**
     * Sampling Time Period (6-04). M # (Phase III)
     */
    void setSamplingTimePeriod(Duration duration);

    /**
     * Spatial Sampling Resolution (6-05). M # (Phase II) A nilReason = ”not applicable” is acceptable for space-based
     * observations
     * 
     * @param uom
     * @param v
     */
    void setSpatialSamplingResolution(String uom, double v, String details);

    /**
     * Temporal Sampling Resolution (6-06). M (Phase III) A nilReason = ”not applicable” is acceptable for space-based
     * observations
     */
    void setTemporalSamplingResolution(Duration duration);

    /**
     * Diurnal Base Time (6-07). C # (Phase I) Mandatory for observations collected during 24-hour period(s) / A
     * nilReason = ”not
     * applicable” is acceptable for space-based observations
     */
    void setDiurnalBaseTime(XMLGregorianCalendar diurnalBaseTime);

    /**
     * Schedule Of Observation (6-08). M # (Phase I) A nilReason = ”not applicable” is acceptable for space-based
     * observations
     */
    void setScheduleOfObservation(int startMonth, int endMonth, int startWeekDay, int endWeekDay, int startHour, int endHour,
	    int startMinute, int endMinute);

    // CATEGORY 7: DATA PROCESSING AND REPORTING
    /**
     * Data Processing Methods And Algorithms (7-01). O (Phase III)
     */
    void setDataProcessingMethodsAndAlgorithms(String dataProcessing);

    /**
     * Processing Analysis Centre (7-02). O (Phase II)
     */
    void setProcessingAnalysisCentre(String processingCentre);

    /**
     * Temporal Reporting Period (7-03). M* (Phase I)
     * 
     * @param timeStampMeaningCode a code from
     *        https://docs.google.com/spreadsheets/d/1SrYr3H4efEO6DpHbI1OSg8NgpswE8IlcvxmssIKOdHU/edit#gid=0
     */
    void setTemporalReportingPeriod(Duration duration, String timeStampMeaningCode);

    /**
     * Spatial Reporting Interval (7-04). C* (Phase I) Mandatory for remote-sensing observations and mobile platforms
     * in general
     * 
     * @param uom
     * @param v
     */
    void setSpatialReportingInterval(String uom, double v);

    /**
     * Software Processor And Version (7-05). O (Phase III)
     * 
     * @param softwareDetails
     * @param softwareURL
     */
    void setSoftwareProcessorAndVersion(String softwareDetails, String softwareURL);

    /**
     * Level Of Data (7-06). O (Phase II)
     * 
     * @param levelOfDataCode a code from http://test.wmocodes.info/wmdr/LevelOfData
     */
    void setLevelOfData(String levelOfDataCode);

    /**
     * Data Format (7-07). M (Phase III)
     * 
     * @param dataFormatCode a code from http://test.wmocodes.info/wmdr/DataFormat
     */
    void setDataFormat(String dataFormatCode);

    /**
     * Version Of Data Format (7-08). M (Phase III)
     */
    void setVersionOfDataFormat(String dataFormatVersion);

    /**
     * Aggregation Period (7-09). M (Phase II)
     */
    void setAggregationPeriod(Duration duration);

    /**
     * Reference Time (7-10). M (Phase II)
     * 
     * @param referenceTimeSourceCode a code from http://test.wmocodes.info/wmdr/ReferenceTime
     */
    void setReferenceTime(String referenceTimeSourceCode);

    /**
     * Reference Datum (7-11). C (Phase I) Mandatory for stations/platforms that report a derived observation value
     * that depends on a local datum
     */
    void setReferenceDatum(VerticalDatumType verticalDatum);

    /**
     * Numerical Resolution (7-12). O (Phase III)
     * 
     * @param v
     */
    void setNumericalResolution(BigInteger v);

    /**
     * Latency (7-13). M (Phase III)
     */
    void setTimeliness(Duration duration);

    // CATEGORY 8: DATA QUALITY
    /**
     * Uncertainty Of Measurement (8-01). C* # (Phase II) Mandatory for variables that are measured, as opposed to
     * classified
     */
    void setUncertaintyOfMeasurement();

    /**
     * Procedure Used To Estimate Uncertainty (8-02). C* # (Phase II) Mandatory for variables that are measured, as
     * opposed to classified
     */
    void setProcedureUsedToEstimateUncertainty();

    /**
     * Quality Flag (8-03). M # (Phase II)
     */
    void setQualityFlag();

    /**
     * Quality Flagging System (8-04). M # (Phase II)
     */
    void setQualityFlaggingSystem();

    /**
     * Traceability (8-05). C* # (Phase II) Mandatory for variables that are measured, as opposed to classified
     */
    void setTraceability();

    // CATEGORY 9: OWNERSHIP AND DATA POLICY
    /**
     * Supervising Organization (9-01). M (Phase II)
     */
    void setSupervisingOrganization();

    /**
     * Data Policy Use Constrainst (9-02). M* (Phase I)
     * 
     * @param dataPolicyCode a code from http://test.wmocodes.info/wmdr/DataPolicy
     */
    void setDataPolicyUseConstrainst(String dataPolicyCode);

    // CATEGORY 10: CONTACT
    /**
     * Facility Contact (10-01). M (Phase I)
     */
    void setFacilityContact(String individualName, String organizationShort, String phone, String streetAddress,
	    String city, String state, String zip, String isoCountry, String email);

    void setPhenomenontTemporalExtent(String beginPosition, String endPosition);

    void setObservationDataUrlArchive(String url);
    

    public void setMDMetadata(MDMetadataType metadata);

}
