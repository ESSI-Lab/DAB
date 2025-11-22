package eu.essi_lab.profiler.oaipmh.profile.mapper.wigos;

import java.io.ByteArrayInputStream;

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

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.utils.whos.HydroOntology;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
import eu.essi_lab.lib.net.utils.whos.WHOSOntology;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;
import eu.essi_lab.profiler.oaipmh.profile.mapper.wigos.WMORegionMapper.WMORegion;
import eu.essi_lab.wigos._1_0.gmd.MDMetadataType;
import eu.essi_lab.wigos._1_0.main.ObjectFactory;

public class WIGOS_MAPPER extends DiscoveryResultSetMapper<Element> {

    // CATEGORY 1: OBSERVED VARIABLE
    /**
     * Sets the Observed variable - measurand (1-01). M* (Phase I)
     * 
     * @param observationVariableCode a code from the code list at:
     *        https://docs.google.com/spreadsheets/d/19ZGKKJrpvK-2OsO0TTrewWWC1mCEGgk35xeFN1jxm74/edit#gid=0
     */
    // setObservedVariable(String observationVariableCode);

    /**
     * Sets the Measurement unit (1-02). C* (Phase I) Mandatory for variables that are measured, as opposed to
     * classified
     * 
     * @param uomCode the unit of measure from the code list at:
     *        https://docs.google.com/spreadsheets/d/129h1yGJZ5m16xETpp-qGEFKBWqAG146RgrWHx-LYcjA/edit#gid=0
     */
    // setMeasurementUnit(String uomCode);

    /**
     * Sets the Temporal extent (1-03). M* (Phase I)
     * 
     * @param beginPosition
     * @param endPosition
     */
    // setTemporalExtent(String beginPosition, String endPosition);

    /**
     * Sets the Spatial extent (1-04). M* (Phase I)
     * 
     * @param latitude
     * @param longitude
     * @param elevation
     */
    // setSpatialExtent(Double latitude, Double longitude, Double elevation);
    /**
     * Sets the Representativeness (1-05). O (Phase II)
     */
    // setRepresentativeness();
    // CATEGORY 2: PURPOSE OF OBSERVATION
    /**
     * Sets the Application area (2-01). M* (Phase I)
     */
    // setApplicationArea();
    /**
     * Sets the Programme / network affiliation (2-02). M (Phase I)
     */
    // setProgramme();

    // CATEGORY 3: STATION/PLATFORM
    /**
     * Sets the Region of origin of data (3-01). C* (Phase I) Mandatory for fixed land-based stations, optional for
     * mobile stations
     */
    // setRegionOfOrigin();
    /**
     * Sets the Territory of origin of data (3-02). C* (Phase I) Mandatory for fixed land-based stations, optional
     * for
     * mobile stations
     */
    // setTerritoryOfOrigin();
    /**
     * Sets the Station / platform (3-03). M (Phase I)
     */
    // setStationOrPlatformName();
    /**
     * Sets the Station / platform type (3-04). M* (Phase II)
     */
    // setStationOrPlatformType();
    /**
     * Sets the Station / platform type (3-05). M*# (Phase III)
     */
    // setStationOrPlatformModel();
    /**
     * Sets the Station / platform unique indentifier (3-06). M* (Phase I)
     */
    // setStationOrPlatformIdentifier();
    /**
     * Sets the geospatial location of the observing station / platform (3-07). M* (Phase I)
     */
    // setGeospatialLocation();
    /**
     * Sets the data communication method (3-08). O (Phase II)
     */
    // setDataCommunicationMethod()
    /**
     * Sets the station operating status (3-09). M (Phase I)
     */
    // setStationOperatingStatus()

    // CATEGORY 4: ENVIRONMENT

    /**
     * Sets the surface cover (4-01). C # (Phase III) For hydrologic observations, specifying a nilReason value is
     * acceptable
     */
    // setSurfaceCover()
    /**
     * Sets the surface cover classification scheme (4-02). C # (Phase III) For hydrologic observations, specifying
     * a
     * nilReason value is acceptable
     */
    // setSurfaceCoverClassificationScheme()
    /**
     * Sets the topography or bathymetry (4-03). C # (Phase III) For hydrologic observations, specifying a nilReason
     * value is acceptable
     */
    // setTopographyOrBathymetry()
    /**
     * Sets the events at observing facility (4-04). O (Phase II)
     */
    // setEventsAtObservingFacility()
    /**
     * Sets the site information (4-05). O (Phase II)
     */
    // setSiteInformation()
    /**
     * Sets the surface roughness (4-06). O (Phase III)
     */
    // setSurfaceRoughness()
    /**
     * Sets the climate zone (4-07). O (Phase III)
     */
    // setClimateZone()
    // CATEGORY 5: INSTRUMENTS AND METHODS OF OBSERVATION
    /**
     * Sets the source of observation (5-01). M (Phase I)
     */
    // setSourceOfObservation()
    /**
     * Sets the measurement / observing method (5-02). M # (Phase I)
     */
    // setMeasurementMethod()
    /**
     * (5-03). C* # (Phase I) Mandatory for instrumental observations
     */
    // setInstrumentSpecifications()
    /**
     * (5-04). O (Phase III)
     */
    // setInstrumentOperatingStatus()
    /**
     * (5-05). C* (Phase I) Mandatory for instrumental observations and if proximity of reference surface impacts on
     * observation
     */
    // setVerticalDistanceOfSensor()
    /**
     * (5-06). C # (Phase III) Mandatory for instrumental observations and if prescribed by best practice / A
     * nilReason
     * = ”not applicable” is acceptable for space-based observations
     */
    // setConfigurationOfInstrumentation()
    /**
     * (5-07). C (Phase III) Mandatory for instrumental observations
     */
    // setInstrumentControlSchedule()
    /**
     * (5-08). C# (Phase III)
     */
    // setInstrumentControlResult()

    /**
     * (5-09). C # (Phase III)
     */
    // setInstrumentModelSerialNumber()
    /**
     * (5-10). C # (Phase III) A nilReason = ”not applicable” is acceptable for space-based observations
     */
    // setInstrumentRoutineMaintenance()
    /**
     * (5-11). O (Phase II)
     */
    // setMaintenanceParty() ;

    /**
     * (5-12). C* (Phase II) Mandatory for instrumental observations and if different from station/platform / A
     * nilReason = ”not applicable” is acceptable for space-based observations
     */
    // setInstrumentGeospatialLocation() ;

    /**
     * (5-13). O (Phase III)
     */
    // setMaintenanceActivity() ;

    /**
     * (5-14). O (Phase III)
     */
    // setStatusOfObservation() ;

    /**
     * (5-15). C # (Phase II) A nilReason = ”not applicable” is acceptable for space-based observations
     */
    // setExposureOfInstruments() ;

    // CATEGORY 6: SAMPLING
    /**
     * (6-01). O (Phase III)
     */
    // setSamplingProcedures() ;

    /**
     * (6-02). O (Phase III)
     */
    // setSampleTreatment() ;

    /**
     * (6-03). O* (Phase I)
     */
    // setSamplingStrategy() ;

    /**
     * (6-04). M # (Phase III)
     */
    // setSamplingTimePeriod() ;

    /**
     * (6-05). M # (Phase II) A nilReason = ”not applicable” is acceptable for space-based observations
     */
    // setSpatialSamplingResolution() ;

    /**
     * (6-06). M (Phase III) A nilReason = ”not applicable” is acceptable for space-based observations
     */
    // setTemporalSamplingResolution() ;

    /**
     * (6-07). C # (Phase I) Mandatory for observations collected during 24-hour period(s) / A nilReason = ”not
     * applicable” is acceptable for space-based observations
     */
    // setDirurnalBaseTime() ;

    /**
     * (6-08). M # (Phase I) A nilReason = ”not applicable” is acceptable for space-based observations
     */
    // setScheduleOfObservation() ;

    // CATEGORY 7: DATA PROCESSING AND REPORTING
    /**
     * (7-01). O (Phase III)
     */
    // setDataProcessingMethodsAndAlgorithms() ;

    /**
     * (7-02). O (Phase II)
     */
    // setProcessingAnalysisCentre() ;

    /**
     * (7-03). M* (Phase I)
     */
    // setTemporalReportingPeriod() ;

    /**
     * (7-04). C* (Phase I) Mandatory for remote-sensing observations and mobile platforms in general
     */
    // setSpatialReportingInterval() ;

    /**
     * (7-05). O (Phase III)
     */
    // setSoftwareProcessorAndVersion() ;

    /**
     * (7-06). O (Phase II)
     */
    // setLevelOfData() ;

    /**
     * (7-07). M (Phase III)
     */
    // setDataFormat() ;

    /**
     * (7-08). M (Phase III)
     */
    // setVersionOfDataFormat() ;

    /**
     * (7-09). M (Phase II)
     */
    // setAggregationPeriod() ;

    /**
     * (7-10). M (Phase II)
     */
    // setReferenceTime() ;

    /**
     * (7-11). C (Phase I) Mandatory for stations/platforms that report a derived observation value that depends on
     * a
     * local datum
     */
    // setReferenceDatum() ;

    /**
     * (7-12). O (Phase III)
     */
    // setNumericalResolution() ;

    /**
     * (7-13). M (Phase III)
     */
    // setLatency() ;

    // CATEGORY 8: DATA QUALITY
    /**
     * (8-01). C* # (Phase II) Mandatory for variables that are measured, as opposed to classified
     */
    // setUncertaintyOfMeasurement() ;

    /**
     * (8-02). C* # (Phase II) Mandatory for variables that are measured, as opposed to classified
     */
    // setProcedureUsedToEstimateUncertainty() ;

    /**
     * (8-03). M # (Phase II)
     */
    // setQualityFlag() ;

    /**
     * (8-04). M # (Phase II)
     */
    // setQualityFlaggingSystem() ;

    /**
     * (8-05). C* # (Phase II) Mandatory for variables that are measured, as opposed to classified
     */
    // setTraceability() ;

    // CATEGORY 9: OWNERSHIP AND DATA POLICY
    /**
     * (9-01). M (Phase II)
     */
    // setSupervisingOrganization() ;

    /**
     * (9-02). M* (Phase I)
     */
    // setDataPolicyUseConstrainst() ;

    // CATEGORY 10: CONTACT
    /**
     * (10-01). M (Phase I)
     */
    // setContact() ;

    // Map of lowercase name -> Country
    private static final Map<String, Country> NAME_TO_COUNTRY = new HashMap<>();
    private static final Pattern COUNTRY_PATTERN;

    static {
	Set<String> allNames = new HashSet<>();

	for (Country country : Country.values()) {
	    String shortName = country.getShortName().toLowerCase();
	    String officialName = country.getOfficialName().toLowerCase();

	    NAME_TO_COUNTRY.put(shortName, country);
	    NAME_TO_COUNTRY.put(officialName, country);

	    allNames.add(Pattern.quote(shortName));
	    allNames.add(Pattern.quote(officialName));
	}

	// Build a single regex pattern like: (argentina|brazil|the republic of paraguay|italy)
	COUNTRY_PATTERN = Pattern.compile("\\b(" + String.join("|", allNames) + ")\\b", Pattern.CASE_INSENSITIVE);
    }

    @Override
    public Element map(DiscoveryMessage message, GSResource resource) throws GSException {

	try {
	    MIMetadata iso = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	    ExtensionHandler extensionHandler = resource.getExtensionHandler();

	    HydroOntology ontology = new WHOSOntology();

	    WIGOSMetadata record = new WIGOSMetadata();

	    record = addHeader(record);

	    String name = iso.getCoverageDescription().getAttributeTitle();

	    // south africa --- > River discharge
	    // argentina ------> ???
	    /*
	     * WMO FACILITY TYPE: https://codes.wmo.int/wmdr/_FacilityType
	     */

	    record.setStationOrPlatformType("lakeRiverFixed");

	    String beginPosition = iso.getDataIdentification().getTemporalExtent().getBeginPosition();
	    String endPosition = iso.getDataIdentification().getTemporalExtent().getEndPosition();

	    Optional<String> countryCode = resource.getExtensionHandler().getCountryISO3();
	    String isoC = null;
	    String cCode = null;
	    if (countryCode.isPresent()) {
		isoC = countryCode.get();
		cCode = isoC;
		record.setTerritoryOfOrigin(cCode, beginPosition, endPosition);
	    } else {
		// TODO: IMPLEMENT: UNDERSTAND THE COUNTRY NAME
		GSLoggerFactory.getLogger(getClass()).error("NO VALID COUNTRY NAME!!!");
		String label = resource.getSource().getLabel().toLowerCase();

		if (label.contains("hmfs")) {
		    cCode = Country.ARGENTINA.getISO3();
		} else {
		    Matcher matcher = COUNTRY_PATTERN.matcher(label.toLowerCase());

		    if (matcher.find()) {
			String matchedName = matcher.group(1).toLowerCase();
			Country matchedCountry = NAME_TO_COUNTRY.get(matchedName);
			cCode = matchedCountry != null ? matchedCountry.getISO3() : null;
		    }
		}

		if (cCode != null) {
		    record.setTerritoryOfOrigin(cCode, beginPosition, endPosition);
		}
	    }

	    /*
	     * WMO REGION: https://codes.wmo.int/wmdr/_WMORegion
	     * one of the following: africa, antarctica, asia, europe, inapplicable, northCentralAmericaCaribbean,
	     * southAmerica, southWestPacific, unknown
	     */
	    WMORegion region = WMORegionMapper.getRegion(cCode);
	    Country country = Country.decode(cCode);
	    String isoCode = null;
	    if (country != null) {
		isoCode = country.getNumericCode();
	    }
	    int regionCode = 0;

	    if (region != null) {
		record.setRegionOfOrigin(region.getDescription());
		regionCode = region.getCode();
	    } else {
		record.setRegionOfOrigin("unknown");
	    }
	    // boolean isAfrica = cCode.toLowerCase().contains("zaf");
	    // if (isAfrica) {
	    // record.setRegionOfOrigin("africa");
	    // } else {
	    // record.setRegionOfOrigin("southAmerica");
	    // }

	    // record.setSurfaceCover(null, "not applicable");
	    // record.setTopographyOrBathymetry(null, null, "not applicable", "not applicable", "not applicable", "not
	    // applicable");

	    /*
	     * WMO SourceOfObservation: https://codes.wmo.int/wmdr/_SourceOfObservation
	     * one of the following: automaticReading, humanObservation, inapplicable, manualReading, unknown
	     */
	    record.setSourceOfObservation("automaticReading");

	    /*
	     * WMO Observing Method: https://codes.wmo.int/wmdr/_ObservingMethodAtmosphere or
	     * https://codes.wmo.int/wmdr/_ObservingMethodTerrestrial or
	     * https://codes.wmo.int/wmdr/_ObservingMethodOcean
	     * TODO: understand which type of observingMethod is needed and then find the relative code. E.g.
	     * Pluviograph code: 170
	     * Volumetric method code: 375
	     */
	    record.setMeasurementMethod("unknown");

	    record.setDataFormat("xml");
	    record.setVersionOfDataFormat("WaterML 2.0");

	    String description = iso.getDataIdentification().getAbstract();



	    String providerId = resource.getSource().getUniqueIdentifier();
	    String fileIdentifier = iso.getFileIdentifier();
	    MIPlatform platform = iso.getMIPlatform();
	    String siteName = null;
	    if (platform != null) {
		siteName = platform.getCitation().getTitle();
		record.setStationOrPlatformName(siteName);
	    } else {
		GSLoggerFactory.getLogger(getClass()).warn("Problem mapping site");
	    }
	    
	    
	    List<String> orgDesc = extensionHandler.getOriginatorOrganisationDescriptions();
	    List<String> orgId = extensionHandler.getOriginatorOrganisationIdentifiers();
	    
	    /*
	     * SET setStationOrPlatformDescription - site information
	     */
	    String toSplit = "with data from station";
	    if (description != null && description.contains(toSplit)) {
		String splittedString = description.split(toSplit)[1];
		description = "Station" + splittedString;
	    } else if(siteName != null) {
		description = orgDesc.isEmpty() ? "Station " + siteName + " located in " + country.getOfficialName() : "Station " + siteName + " located in " + country.getOfficialName() + " - " + orgDesc.get(0);
	    }
	    record.setStationOrPlatformDescription(description, beginPosition, endPosition);

	    String codeId = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatform().getMDIdentifierCode();

	    String[] splittedString = codeId.split("/");
	    String stationId = "";
	    if (splittedString.length == 1) {
		splittedString = codeId.split(":");
	    }
	    stationId = splittedString[splittedString.length - 1];
	    String initialId = "0-21016-";
	    initialId += regionCode + isoCode + "0-" + stationId;
	    // if (isAfrica) {
	    // String[] splittedString = codeId.split("/");
	    // String stationId = splittedString[splittedString.length - 1];
	    // initialId += "17100-" + stationId;
	    // } else {
	    // String[] splittedString = codeId.split(":");
	    // String stationId = splittedString[splittedString.length - 1];
	    // initialId += "30320-" + stationId;
	    // }

	    record.setStationOrPlatformIdentifier(initialId);

	    // commented now for South Africa use-case
	    // Optional<String> identifier = extensionHandler.getUniquePlatformIdentifier();
	    // if (identifier.isPresent()) {
	    // System.out.println("UNIQUE_PLATFORM_IDENTIFIER: " + identifier.get());
	    // record.setStationOrPlatformIdentifier(identifier.get());
	    // }

	    String title = iso.getDataIdentification().getCitationTitle();

	    Online online = iso.getDistribution().getDistributionOnline();
	    if (online != null) {
		record.setObservationDataUrlArchive(online.getLinkage());
	    }

	    record.addProgramme("WHYCOS");
	    record.setStationOperatingStatus(beginPosition, endPosition);
	    record.setReferenceTime("unknown");
	    record.setTimeliness(null);
	    record.setSamplingTimePeriod(null);

	    /*
	     * DURATION : create Duration -> e.g. PT1H, P2Y3M5D
	     */
	    // Obtain a DatatypeFactory instance
	    Optional<String> timeResDuration = extensionHandler.getTimeResolutionDuration8601();
	    DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
	    // Create a Duration representing 1 hour
	    Duration duration;
	    if (timeResDuration.isPresent()) {
		duration = datatypeFactory.newDuration(timeResDuration.get());
	    } else {
		// sinc it is mandatory -bet on PT1H
		duration = datatypeFactory.newDuration("PT1H");
	    }

	    /*
	     * WMO Meaning of time stamp: https://codes.wmo.int/wmdr/_TimeStampMeaning
	     * one of the following: beginning, end, inapplicable, middle, unknown
	     */
	    record.setTemporalReportingPeriod(duration, "beginning");

	    /*
	     * SET setDeploymentValidPeriod
	     */
	    record.setDeploymentValidPeriod(beginPosition, endPosition);

	    /*
	     * SET setStationOrPlatformDateEstablished
	     */

	    int startMonth = 1;
	    int endMonth = 1;
	    int startWeekDay = 1;
	    int endWeekDay = 1;
	    int startHour = 1;
	    int endHour = 1;
	    int startMinute = 1;
	    int endMinute = 1;
	    // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	    // Date parsedDate = sdf.parse(beginPosition);
	    Optional<Date> optDate = ISO8601DateTimeUtils.parseISO8601ToDate(beginPosition);
	    if (optDate.isPresent()) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(optDate.get());
		DatatypeFactory df = DatatypeFactory.newInstance();
		XMLGregorianCalendar xmlGregorianCalendar = df.newXMLGregorianCalendar(calendar);
		record.setStationOrPlatformDateEstablished(xmlGregorianCalendar);
		
		Optional<Date> optEndDate = ISO8601DateTimeUtils.parseISO8601ToDate(endPosition);
		if(optEndDate.isPresent()) {
		    GregorianCalendar end_calendar = new GregorianCalendar();
		    end_calendar.setTime(optEndDate.get());
		    endMonth = end_calendar.get(Calendar.MONTH) + 1;
		    endWeekDay = end_calendar.get(Calendar.DAY_OF_WEEK);
		    endMonth = end_calendar.get(Calendar.MONTH) + 1;
		    }
		// Extract fields
		startMonth = 1 ;//calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based
		endMonth = 12; //startMonth; // same, unless you want a duration
		startWeekDay = 1; //calendar.get(Calendar.DAY_OF_WEEK); // Sunday = 1 ... Saturday = 7
		endWeekDay = 7; //startWeekDay;
		startHour = 0; //calendar.get(Calendar.HOUR_OF_DAY);
		endHour = 23; //startHour;
		startMinute = 0; //calendar.get(Calendar.MINUTE);
		endMinute = 59; //startMinute;
	    }

	    /*
	     * SET setApplicationArea
	     */
	    record.setApplicationArea("unknown");

	    /*
	     * SET Schedule: mandatory
	     */
	    record.setScheduleOfObservation(startMonth, endMonth, startWeekDay, endWeekDay, startHour, endHour, startMinute, endMinute);
	    // Duration duration = getDuration(beginPosition);
	    // record.setTemporalReportingPeriod(duration , beginPosition);
	    record.setPhenomenontTemporalExtent(beginPosition, endPosition);
	    GeographicBoundingBox bbox = iso.getDataIdentification().getGeographicBoundingBox();
	    if (bbox != null && bbox.getNorth() != null) {
		Double north = bbox.getNorth();
		Double south = bbox.getSouth();
		Double east = bbox.getEast();
		Double west = bbox.getWest();

		VerticalExtent ve = iso.getDataIdentification().getVerticalExtent();
		Double altitude = null;
		if (ve != null) {
		    altitude = ve.getMaximumValue();
		}
		if (altitude == null)
		    altitude = 0.0;
		try {
		    record.addStationGeospatialLocation(north, east, altitude, null, null, beginPosition, endPosition);
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).warn("Problem with geospatial location");
		}

	    }
	    List<eu.essi_lab.iso.datamodel.classes.ResponsibleParty> responsibles = iso.getDataIdentification()
		    .getCitationResponsibleParties();
	    if (!responsibles.isEmpty()) {
		eu.essi_lab.iso.datamodel.classes.ResponsibleParty sourceResponsible = responsibles.iterator().next();
		String individualName = sourceResponsible.getIndividualName();
		String organizationShort = sourceResponsible.getOrganisationName();
		String phone = null;
		try {
		    phone = sourceResponsible.getContact().getPhoneVoices().next();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		String streetAddress = null;
		try {
		    streetAddress = sourceResponsible.getContact().getAddress().getDeliveryPoint();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		String city = null;
		try {
		    city = sourceResponsible.getContact().getAddress().getCity();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		String state = null;
		try {
		    state = sourceResponsible.getContact().getAddress().getAdministrativeArea();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		String zip = null;
		try {
		    zip = sourceResponsible.getContact().getAddress().getPostalCode();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		String isoCountry = null;
		try {
		    isoCountry = sourceResponsible.getContact().getAddress().getCountry();
		} catch (Exception e) {
		    e.printStackTrace();
		}
		String email = null;
		try {
		    email = sourceResponsible.getContact().getAddress().getElectronicMailAddress();
		} catch (Exception e) {
		    e.printStackTrace();
		}

		record.setFacilityContact(individualName, organizationShort, phone, streetAddress, city, state, zip, isoCountry, email,
			beginPosition, endPosition);
	    } else {
		// other methods for responsible party
		
		if (!orgDesc.isEmpty())
		    record.setFacilityContact(null, orgDesc.get(0), null, null, null, null, null, isoC, null, beginPosition, endPosition);
	    }
	    try {

		// TODO: to uncomment the following line we need to be sure that MDMetadata are valid respecting ISO
		// 19115 specification

		// record.setMDMetadata(convertMetadata(resource.getHarmonizedMetadata().getCoreMetadata().getReadOnlyMDMetadata()));

		Optional<String> variableUnits = extensionHandler.getAttributeUnitsURI();

		Optional<String> labelUnits = extensionHandler.getAttributeUnits();
		Optional<String> units = extensionHandler.getAttributeUnitsAbbreviation();
		if(variableUnits.isPresent()){
		    String measure = variableUnits.get();
		    measure = measure.contains("m3_s-1") ? "http://codes.wmo.int/wmdr/unit/m3.s-1": measure;
		    record.setMeasurementUnit(measure);
		} else if (units.isPresent()) {
		    record.setMeasurementUnit(units.get());
		} else {
		    // TODO: decode the unit of measure
		    // extensionHandler.getAttributeUnits()
		    // if (isAfrica) {
		    // record.setMeasurementUnit("m³/s");
		    // } else {
		    // record.setMeasurementUnit("mm");
		    // }
		    // record.setMeasurementUnit("mm");
		    GSLoggerFactory.getLogger(getClass()).error("NO VALID UNITS OF MEASURE CODE!!!");
		}

		Optional<String> riverBasin = extensionHandler.getRiverBasin();

		Optional<String> optionalVariableCode = extensionHandler.getObservedPropertyURI();
		String code = null;
		HashSet<String> uris = new HashSet<>();
		if (optionalVariableCode.isPresent()) { // http://hydro.geodab.eu/hydro-ontology/concept/33 -> null
							// value
		    List<SKOSConcept> concepts = ontology.findConcepts(optionalVariableCode.get(), true, false);
		    for (SKOSConcept concept : concepts) {
			if (concept != null) {
			    GSLoggerFactory.getLogger(getClass()).info("CONCEPT URI: " + concept.getURI());
			    uris.add(concept.getURI());
			}
		    }
		}

		/*
		 * WMO OBSERVED PROPERTIES:
		 * https://codes.wmo.int/wmdr/_ObservedVariableAtmosphere
		 * https://codes.wmo.int/wmdr/_ObservedVariableTerrestrial
		 * https://codes.wmo.int/wmdr/_ObservedVariableOcean
		 */
		// TODO: implement code depending on variable
		for (String s : uris) {
		    if (s.contains("codes.wmo.int")) {
			code = s;
			break;
		    }

		}

		if (code != null) {
		    record.setObservedVariable(name, code);
		} else {
		    // TODO found another way to add the relative code
		    // record.setObservedVariable(name,
		    // code);//http://codes.wmo.int/wmdr/ObservedVariableTerrestrial/171
		    // record.setObservedVariable(name, "http://codes.wmo.int/wmdr/ObservedVariableAtmosphere/213");

		    if (name.toLowerCase().contains("precipitation")) {
			record.setObservedVariable(name, "http://codes.wmo.int/wmdr/ObservedVariableAtmosphere/210");
		    } else if (name.toLowerCase().contains("height")) {
			record.setObservedVariable(name, "http://codes.wmo.int/wmdr/ObservedVariableTerrestrial/172");
		    } else if (name.toLowerCase().contains("discharge")) {
			record.setObservedVariable(name, "http://codes.wmo.int/wmdr/ObservedVariableTerrestrial/171");
		    } else if (name.toLowerCase().contains("temperature")) {
			record.setObservedVariable(name, "http://codes.wmo.int/wmdr/ObservedVariableAtmosphere/12166");
		    } else if (name.toLowerCase().contains("temperature")) {
			record.setObservedVariable(name, "http://codes.wmo.int/wmdr/ObservedVariableAtmosphere/12166");
		    } else if (name.toLowerCase().contains("humidity")) {
			record.setObservedVariable(name, "http://codes.wmo.int/wmdr/ObservedVariableAtmosphere/12249");
		    } else if (name.toLowerCase().contains("pressure")) {
			record.setObservedVariable(name, "http://codes.wmo.int/wmdr/ObservedVariableAtmosphere/216");
		    }

		    GSLoggerFactory.getLogger(getClass()).error("NO VALID OBSERVED PROPERTIES CODE!!!");

		}

		// resultTime seems to be mandatory for OSCAR
		record.setResultTime(null);
		record.setDatGenarationValidPeriod(beginPosition, endPosition);

		record.setObservationGeometryType("point");

		// it seems that wmdr:observation should be inside the facility>ObservingFacility
		record.setObservationInFacility();

	    } catch (Exception e) {
		e.printStackTrace();
	    }

	    Document doc = WIGOSMetadata.getBuilder().newDocument();
	    ObjectFactory of = new ObjectFactory();
	    WIGOSMetadata.getMarshaller().marshal(of.createWIGOSMetadataRecord(record.getRecord()), doc);
	    Element ret = doc.getDocumentElement();
	    return ret;
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public String mapStations(List<GSResource> resources, String identifier) throws GSException {

	try {

	    String[] splittedString = identifier.split("/");
	    String stationId = splittedString[splittedString.length - 1];

	    String minDate = null;
	    String maxDate = null;
	    Date in = null;
	    Date end = null;

	    String title = null;
	    Set<String> setTitle = new HashSet<String>();
	    Element wigosElem = null;
	    List<Node> observationList = new ArrayList<Node>();
	    int k = 0;
	    for (GSResource resource : resources) {
		MIMetadata iso = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
		String t = iso.getDataIdentification().getCitationTitle();
		GSLoggerFactory.getLogger(getClass()).info("TITLE: " + t);
		String beginPosition = iso.getDataIdentification().getTemporalExtent().getBeginPosition();
		String endPosition = iso.getDataIdentification().getTemporalExtent().getEndPosition();
		Optional<Date> startDate = ISO8601DateTimeUtils.parseISO8601ToDate(beginPosition);
		Optional<Date> endDate = ISO8601DateTimeUtils.parseISO8601ToDate(endPosition);
		if (startDate.isPresent()) {

		    if (minDate == null) {
			minDate = beginPosition;
			in = startDate.get();
		    } else {

			if (startDate.get().before(in)) {
			    minDate = beginPosition;
			    in = startDate.get();
			}

		    }
		}
		if (endDate.isPresent()) {

		    if (maxDate == null) {
			maxDate = endPosition;
			end = endDate.get();
		    } else {

			if (endDate.get().after(end)) {
			    maxDate = endPosition;
			    end = endDate.get();
			}

		    }
		}
		if (!setTitle.contains(t)) {
		    setTitle.add(t);
		    Element element = map(null, resource);
		    if (k == 0) {
			wigosElem = element;
			k++;
		    } else {
			String doc1 = XMLDocumentReader.asString(element);
			doc1 = doc1.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
			XMLDocumentReader xdoc = new XMLDocumentReader(doc1);
			Node node = xdoc.evaluateNode("//*:ObservingFacility/*:observation");
			observationList.add(node);
		    }

		} else {
		    GSLoggerFactory.getLogger(getClass()).info("DUPLICATED FOUND!!!");
		    continue;
		}
	    }

	    String doc1 = XMLDocumentReader.asString(wigosElem);

	    XMLDocumentReader xmlRequest = new XMLDocumentReader(doc1);

	    XMLDocumentWriter writer = new XMLDocumentWriter(xmlRequest);

	    writer.setText("//*:ResponsibleParty/*:validPeriod/*:TimePeriod/*:beginPosition", minDate);
	    writer.setText("//*:GeospatialLocation/*:validPeriod/*:TimePeriod/*:beginPosition", minDate);
	    writer.setText("//*:Territory/*:validPeriod/*:TimePeriod/*:beginPosition", minDate);
	    writer.setText("//*:GeospatialLocation/*:validPeriod/*:TimePeriod/*:endPosition", maxDate);
	    writer.setText("//*:Territory/*:validPeriod/*:TimePeriod/*:endPosition", maxDate);
	    
	    if(end != null) {
		//Calendar now = Calendar.getInstance();
	        Calendar oneYearAgo = Calendar.getInstance();
	        oneYearAgo.add(Calendar.MONTH, -12);
	        if(end.before(oneYearAgo.getTime())){
	            writer.setText("//*:ReportingStatus/*:reportingStatus/@*:href", "http://codes.wmo.int/wmdr/ReportingStatus/unknown");
	            GSLoggerFactory.getLogger(getClass()).info("NOT OPERATIONAL");
	            
	        }
	        
	    }
	    for (Node n : observationList) {
		String xpath = "//*:ObservingFacility";
		writer.addNode(xpath, n);
	    }
	    // Document doc = WIGOSMetadata.getBuilder().newDocument();
	    // ObjectFactory of = new ObjectFactory();
	    // WIGOSMetadata.getMarshaller().marshal(of.createWIGOSMetadataRecord(record.getRecord()), doc);
	    // Element ret = doc.getDocumentElement();
	    // DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    // factory.setNamespaceAware(true);
	    // DocumentBuilder builder = factory.newDocumentBuilder();
	    // Document doc = builder.parse(new ByteArrayInputStream(xmlRequest.asString().getBytes("UTF-8")));
	    return xmlRequest.asString();

	    // return (Element) xmlRequest.asString()
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    private static MDMetadataType convertMetadata(MDMetadata md_Metadata) {
	try {
	    InputStream stream = md_Metadata.asStream();
	    Unmarshaller u = WIGOSMetadata.getUnmarshaller();
	    Object obj = u.unmarshal(stream);
	    if (obj instanceof JAXBElement) {
		JAXBElement je = (JAXBElement) obj;
		obj = je.getValue();
	    }
	    if (obj instanceof MDMetadataType) {
		MDMetadataType metadata = (MDMetadataType) obj;
		return metadata;

	    }
	} catch (Exception e) {
	}
	return null;
    }

    // private static CIResponsiblePartyType convertParty(IResponsibleParty sourceResponsible) {
    // InputStream stream = sourceResponsible.asStream();
    //
    // try {
    // Unmarshaller u = JAXBUtils.getInstance().createUnmarshaller(ResponsibleParty.class);
    // Object obj = u.unmarshal(stream);
    // if (obj instanceof JAXBElement) {
    // JAXBElement je = (JAXBElement) obj;
    // obj = je.getValue();
    // }
    // if (obj instanceof CIResponsiblePartyType) {
    // CIResponsiblePartyType cirpt = (CIResponsiblePartyType) obj;
    // return cirpt;
    //
    // }
    // } catch (Exception e) {
    // }
    // return null;
    // }
    //
    // private static CodeWithAuthorityType getCodeWithAuthority(String authority, String code) {
    // CodeWithAuthorityType ret = new CodeWithAuthorityType();
    // ret.setCodeSpace(authority);
    // ret.setValue(code);
    // return ret;
    // }
    //
    // private static StringOrRefType getString(String str) {
    // StringOrRefType ret = new StringOrRefType();
    // ret.setValue(str);
    // return ret;
    // }
    //
    // private static CodeType getCodeType(String str) {
    // CodeType code = new CodeType();
    // code.setValue(str);
    // return code;
    // }

    @Override
    public MappingSchema getMappingSchema() {

	MappingSchema ret = new MappingSchema();
	ret.setName("WIGOS");
	ret.setUri(CommonNameSpaceContext.WIGOS);
	ret.setVersion("2017");
	return ret;
    }

    @Override
    public Provider getProvider() {
	return new ESSILabProvider();
    }

    /**
     * @param time
     * @return
     */
    protected Duration getDuration(String time) {

	DatatypeFactory datatypeFactory = null;
	try {
	    datatypeFactory = DatatypeFactory.newInstance();
	} catch (javax.xml.datatype.DatatypeConfigurationException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	    return null;
	}

	return datatypeFactory.newDuration(time.trim());

    }

    private WIGOSMetadata addHeader(WIGOSMetadata metadata) {
	Calendar c = new GregorianCalendar();
	c.set(2016, 3, 28, 0, 0, 0);
	Date d = new Date(c.getTimeInMillis());

	String contactSurname = "";
	String contactName = "";
	String contactTitle = "";
	String organizationShort = "World Meteorological Organization WMO and Federal Office for Meteorology and Climatology MeteoSwiss";
	String contactEmail = "oscar@wmo.int";
	String organizationURL = "https://oscar.wmo.int/surface";
	metadata.setHeaderInformation(d, null, null, null, organizationShort, contactEmail, organizationURL);
	// String email = "https://oscar.wmo.int/surface/#/feedback";
	// metadata.getRecord().getHeaderInformation().getHeader().getRecordOwner().getCIResponsibleParty().getContactInfo().getCIContact().getAddress().getCIAddress().getElectronicMailAddress().add(e
	// );
	return metadata;

    }

}
