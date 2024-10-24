package eu.essi_lab.accessor.polytope;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * @author roncella
 */
public class PolytopeMapper extends OriginalIdentifierMapper {

    private static final String POLYTOPE = "POLYTOPE";

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public PolytopeMapper() {
	// do nothing
    }

    public enum Resolution {
	HOURLY
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.POLYTOPE;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {
	/**
	 * AMSTERDAM STATION
	 * 25 columns (first column (0) empty)
	 * ;obstype@body;codetype@body;entryno@body;varno@body;date@hdr;time@hdr;stalt@hdr;statid@hdr;obstype@hdr;codetype@hdr;source@hdr;
	 * groupid@hdr;reportype@hdr;class@desc;type@desc;stream@desc;expver@desc;levtype@desc;andate@desc;antime@desc;lat@hdr;lon@hdr;obsvalue@body;stationid@hdr,
	 * column0: number of measures per day (cumulative)
	 * column4: varno (identifier of variable)
	 * column19: andate (date of measure)
	 * column20: antime (time of measure)
	 * column21: lat (station latitude)
	 * column22: lon (station longitude)
	 * column23: obsvalue (value of variable)
	 * column24: stationid (Name of station)
	 */

	/**
	 * METEOTRACKER
	 * 26 columns (first column (0) empty)
	 * codetype@body;entryno@body;varno@body;statid@hdr;obstype@hdr;codetype@hdr;source@hdr;stationid@hdr;groupid@hdr;reportype@hdr;class@desc;
	 * type@desc;stream@desc;expver@desc;levtype@desc;date@hdr;time@hdr;andate@desc;antime@desc;stalt@hdr;press@body;obsvalue@body;lat@hdr;lon@hdr;min@body
	 * column4: varno@body (identifier of variable)
	 * column19: andate@desc (date of measure)
	 * column20: antime@desc (time of measure)
	 * column24: lat@hdr (station latitude)
	 * column25: lon@hdr (station longitude)
	 * column21: stalt@hdr (station altitude)
	 * column23: obsvalue@body (value of variable)
	 * column9: stationid@hdr (Name of station)
	 */

	
	dataset.getPropertyHandler().setIsTimeseries(true);
	
	String originalMetadata = originalMD.getMetadata();
	Map<PolytopeVariable, PolytopeStation> mapStations = getMapStations(originalMetadata);

	if (!mapStations.isEmpty()) {

	    PolytopeStation station = mapStations.get(PolytopeVariable.TEMPERATURE);
	    PolytopeVariable variable = PolytopeVariable.TEMPERATURE;
	    String abbreviation = "K";
	    String buildingURL = "_TEMP.csv";
	    if (station == null) {
		station = mapStations.get(PolytopeVariable.HUMIDITY);
		variable = PolytopeVariable.HUMIDITY;
		buildingURL = "_HUM.csv";
		abbreviation = "%";
	    }

	    Resolution resolution = Resolution.HOURLY;

	    // TEMPORAL EXTENT
	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	    TemporalExtent extent = new TemporalExtent();
	    String startDate = station.getStartDateTime();
	    String endDate = station.getEndDateTime();
	    if (startDate != null && !startDate.isEmpty()) {

		extent.setBeginPosition(startDate);

		if (endDate != null && !endDate.isEmpty()) {

		    extent.setEndPosition(endDate);
		}

		/**
		 * CODE COMMENTED BELOW COULD BE USEFUL
		 * // if (dateTime.isPresent()) {
		 * // String beginTime = ISO8601DateTimeUtils.getISO8601DateTime(dateTime.get());
		 * // extent.setPosition(beginTime, startIndeterminate, false, true);
		 * // // Estimate of the data size
		 * // // only an estimate seems to be possible, as this odata service doesn't seem to support the
		 * /$count
		 * // // operator
		 * // double expectedValuesPerYears = 12.0; // 1 value every 5 minutes
		 * // double expectedValuesPerDay = expectedValuesPerHours * 24.0;
		 * // long expectedSize = TimeSeriesUtils.estimateSize(dateTime.get(), new Date(),
		 * expectedValuesPerDay);
		 * // GridSpatialRepresentation grid = new GridSpatialRepresentation();
		 * // grid.setNumberOfDimensions(1);
		 * // grid.setCellGeometryCode("point");
		 * // Dimension time = new Dimension();
		 * // time.setDimensionNameTypeCode("time");
		 * // try {
		 * // time.setDimensionSize(new BigInteger("" + expectedSize));
		 * // ExtensionHandler extensionHandler = dataset.getExtensionHandler();
		 * // extensionHandler.setDataSize(expectedSize);
		 * // } catch (Exception e) {
		 * // }
		 * // grid.addAxisDimension(time);
		 * // coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);
		 * // }
		 */

		coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);
	    }
	    if (!station.getName().equals("")) {
		coreMetadata.setTitle("Acquisitions of " + variable.getLabel() + " at station: " + station.getName());
		coreMetadata.setAbstract("This dataset contains " + variable.name().toLowerCase()
			+ " timeseries form I-CHANGE Citizen Observatory, acquired by a specific" + " observing station ("
			+ station.getName() + ") of the Amsterdam LL.");
	    } else {
		coreMetadata.setTitle("Acquisitions of " + variable.getLabel() + " at station: " + station.getStationCode());
		coreMetadata.setAbstract("This dataset contains " + variable.name().toLowerCase()
			+ " timeseries form I-CHANGE Citizen Observatory, acquired by a specific" + " observing station ("
			+ station.getName() + ") of the Amsterdam LL.");
	    }
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(POLYTOPE);

	    coreMetadata.getMIMetadata().getDataIdentification()
		    .setCitationPublicationDate(ISO8601DateTimeUtils.getISO8601Date(new Date()));

	    // if (!station.getState().equals("")) {
	    // coreMetadata.getMIMetadata().getDataIdentification().addKeyword("State: " + station.getState());
	    // }
	    // if (!station.getCountry().equals("")) {
	    // coreMetadata.getMIMetadata().getDataIdentification().addKeyword("Country: " + station.getCountry());
	    // }
	    // if (!station.getIcao().equals("")) {
	    // coreMetadata.getMIMetadata().getDataIdentification().addKeyword("ICAO: " + station.getIcao());
	    // }

	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getName());
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(station.getStationCode());
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(variable.toString().toLowerCase());

	    if (resolution.equals(Resolution.HOURLY))
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword("hourly");

	    ExtensionHandler handler = dataset.getExtensionHandler();
	    handler.setTimeUnits("h");
	    handler.setTimeResolution("1");
	    handler.setAttributeMissingValue("-9999");
	    handler.setAttributeUnitsAbbreviation(abbreviation);

	    //
	    // URL + variable
	    //
	    // String id = UUID.nameUUIDFromBytes((splittedStrings[0] + splittedStrings[11]).getBytes()).toString();

	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	    // bounding box
	    String lat = station.getMinLat();
	    String lon = station.getMinLon();

	    if (isDouble(lat) && isDouble(lon)) {

		double serieslat = Double.parseDouble(station.getMinLat());
		double serieslon = Double.parseDouble(station.getMinLon());

		coreMetadata.addBoundingBox(serieslat, serieslon, serieslat, serieslon);
	    }

	    // elevation
	    String minElevation = station.getMinElevation();
	    String maxElevation = station.getMaxElevation();
	    if (minElevation != null && !minElevation.equals("") && maxElevation != null && !maxElevation.equals("")) {
		VerticalExtent verticalExtent = new VerticalExtent();
		if (isDouble(minElevation)) {
		    verticalExtent.setMinimumValue(Double.parseDouble(minElevation));
		}
		if (isDouble(maxElevation)) {
		    verticalExtent.setMaximumValue(Double.parseDouble(maxElevation));
		}
		coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(verticalExtent);
	    }

	    // contact point
	    // ResponsibleParty creatorContact = new ResponsibleParty();
	    //
	    // creatorContact.setOrganisationName("Tripura University");
	    // creatorContact.setRoleCode("originator");
	    // creatorContact.setIndividualName("Anirban Guha");
	    //
	    // Contact contactcreatorContactInfo = new Contact();
	    // Address address = new Address();
	    // address.addElectronicMailAddress("anirbanguha@tripurauniv.in");
	    // contactcreatorContactInfo.setAddress(address);
	    // creatorContact.setContactInfo(contactcreatorContactInfo);
	    //
	    // ResponsibleParty creatorTechContact = new ResponsibleParty();
	    //
	    // creatorTechContact.setOrganisationName("Tripura University");
	    // creatorTechContact.setRoleCode("pointOfContact");
	    // creatorTechContact.setIndividualName("Anirban Guha");
	    //
	    // Contact contactcreatorTechContactInfo = new Contact();
	    // Address addresscreatorTechContact = new Address();
	    // addresscreatorTechContact.addElectronicMailAddress("anirbanguha@tripurauniv.in");
	    // contactcreatorTechContactInfo.setAddress(addresscreatorTechContact);
	    // creatorTechContact.setContactInfo(contactcreatorTechContactInfo);
	    //
	    //
	    //
	    // coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);
	    // coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(otherTechContact);
	    // coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorTechContact);
	    // coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(otherContact);

	    /**
	     * MIPLATFORM
	     **/

	    MIPlatform platform = new MIPlatform();

	    String platformIdentifier = "i-change-citizen-observatory-archive:" + station.getStationCode();

	    platform.setMDIdentifierCode(platformIdentifier);

	    String siteDescription = station.getName();

	    platform.setDescription(siteDescription);

	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(station.getName());
	    platform.setCitation(platformCitation);

	    coreMetadata.getMIMetadata().addMIPlatform(platform);

	    /**
	     * COVERAGEDescription
	     **/

	    CoverageDescription coverageDescription = new CoverageDescription();
	    String variableId = variable.toString() + "_" + resolution.toString();

	    coverageDescription.setAttributeIdentifier(variableId);
	    coverageDescription.setAttributeTitle(variable.getLabel());

	    String attributeDescription = variable.toString() + " Units: " + variable.getUnit() + " Resolution: " + resolution.toString();

	    coverageDescription.setAttributeDescription(attributeDescription);
	    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	    /**
	     * ONLINE
	     */
	    // https://i-change.s3.amazonaws.com/Ams01_TEMP.csv
	    // Online online = new Online();
	    // online.setProtocol(NetProtocols.HTTP.getCommonURN());
	    // String linkage = "https://i-change.s3.amazonaws.com/" + station.getName() + buildingURL;
	    // online.setLinkage(linkage);
	    // online.setName(variable + "@" + station.getName());
	    // online.setFunctionCode("download");
	    // online.setDescription(variable + " Station name: " + station.getName());
	    //
	    // coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);

	    String resourceIdentifier = generateCode(dataset, variable.getId() + ":" + station.getName());

	    coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

	    String linkage = "https://i-change.s3.amazonaws.com/" + station.getName() + buildingURL;
	    Online o = new Online();
	    o.setLinkage(linkage);
	    o.setFunctionCode("download");
	    o.setName(station.getName() + " - " + variable);
	    o.setIdentifier(variable.getId() + ":" + station.getName());
	    o.setProtocol(CommonNameSpaceContext.POLYTOPE);
	    o.setDescription(variable + " Station name: " + station.getName());
	    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(o);

	    coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);
		

	}

    }

    protected Map<PolytopeVariable, PolytopeStation> getMapStations(String originalMetadata) {

	Map<PolytopeVariable, PolytopeStation> mapStations = new HashMap<>();
	try {
	    // delimiter seems to be ; by default
	    Reader in = new StringReader(originalMetadata);
	    String d = ";";
	    char delimiter = d.charAt(0);
	    Iterable<CSVRecord> records = CSVFormat.RFC4180.withDelimiter(delimiter).withFirstRecordAsHeader().parse(in);
	    mapStations = readCSV(records);

	} catch (Exception e) {
	    // TODO: handle exception
	    logger.error(e.getMessage());
	    Reader reader = new StringReader(originalMetadata);
	    Iterable<CSVRecord> records = null;
	    try {
		records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(reader);
	    } catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }

	    mapStations = readCSV(records);

	}

	return mapStations;
    }

    protected Map<PolytopeVariable, PolytopeStation> readCSV(Iterable<CSVRecord> records) {

	Map<PolytopeVariable, PolytopeStation> mapStations = new HashMap<>();

	for (CSVRecord record : records) {

	    String stationName = record.get("stationid@hdr");
	    String lat = record.get("lat@hdr");
	    String lon = record.get("lon@hdr");
	    String date = record.get("andate@desc");
	    String time = record.get("antime@desc");
	    String dateTime = buildDate(date, time);
	    String varName = record.get("varno@body");
	    String alt = record.get("stalt@hdr");
	    PolytopeVariable pv = PolytopeVariable.decode(varName);

	    if (mapStations.isEmpty()) {
		PolytopeStation station = new PolytopeStation();
		station.setName(stationName);
		station.setStationCode(stationName);
		station.setMinLat(lat);
		station.setMaxLat(lat);
		station.setMinLon(lon);
		station.setMaxLon(lon);
		station.setStartDateTime(dateTime);
		station.setEndDateTime(dateTime);
		station.setMinElevation(alt);
		station.setMaxElevation(alt);

		mapStations.put(pv, station);

	    } else {
		PolytopeStation polStation = mapStations.get(pv);
		if (polStation != null) {
		    // already saved -- update it
		    polStation.setEndDateTime(dateTime);

		} else {
		    // this should never happen
		    // now one csv for variable
		    polStation = new PolytopeStation();
		    polStation.setName(stationName);
		    polStation.setStationCode(stationName);
		    polStation.setMinLat(lat);
		    polStation.setMinLon(lon);
		    polStation.setMaxLat(lat);
		    polStation.setMaxLon(lon);
		    polStation.setStartDateTime(dateTime);
		    polStation.setEndDateTime(dateTime);

		    mapStations.put(pv, polStation);
		}

	    }

	}

	return mapStations;
    }

    protected String buildDate(String date, String time) {

	Date initialDate = setTime(date, time);
	String dateTime = ISO8601DateTimeUtils.getISO8601DateTime(initialDate);
	return dateTime;
    }

    /**
     * @param date
     * @param time
     * @return
     *         startTime should be: 0000,0001,0002,....2100,2200,2300
     */
    public static Date setTime(String date, String time) {

	Optional<Date> startDateTime = null;

	try {
	    startDateTime = ISO8601DateTimeUtils.parseNotStandardToDate(date);
	} catch (ParseException e) {
	    GSLoggerFactory.getLogger(PolytopeMapper.class).error(e.getMessage());
	}
	if (startDateTime.isPresent()) {
	    Date begin = startDateTime.get();
	    if (time != null) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTime(begin);
		if (time.length() < 4) {
		    for (int i = 0; i <= 4 - time.length(); i++) {
			time = "0" + time;
		    }
		}
		int t = 0;
		switch (time) {
		case "0000":
		    t = 0;
		    break;
		case "0100":
		    t = 1;
		    break;
		case "0200":
		    t = 2;
		    break;
		case "0300":
		    t = 3;
		    break;
		case "0400":
		    t = 4;
		    break;
		case "0500":
		    t = 5;
		    break;
		case "0600":
		    t = 6;
		    break;
		case "0700":
		    t = 7;
		    break;
		case "0800":
		    t = 8;
		    break;
		case "0900":
		    t = 9;
		    break;
		case "1000":
		    t = 10;
		    break;
		case "1100":
		    t = 11;
		    break;
		case "1200":
		    t = 12;
		    break;
		case "1300":
		    t = 13;
		    break;
		case "1400":
		    t = 14;
		    break;
		case "1500":
		    t = 15;
		    break;
		case "1600":
		    t = 16;
		    break;
		case "1700":
		    t = 17;
		    break;
		case "1800":
		    t = 18;
		    break;
		case "1900":
		    t = 19;
		    break;
		case "2000":
		    t = 20;
		    break;
		case "2100":
		    t = 21;
		    break;
		case "2200":
		    t = 22;
		    break;
		case "2300":
		    t = 23;
		    break;

		default:
		    t = 0;
		    break;
		}

		cal.set(Calendar.HOUR_OF_DAY, t);

		return cal.getTime();
	    }

	    return begin;
	}

	return null;
    }

    protected boolean isDouble(String str) {
	try {
	    // check if it can be parsed as any double
	    Double.parseDouble(str);
	    return true;
	} catch (NumberFormatException e) {
	    return false;
	}
    }

    public static void main(String[] args) {
	TemporalExtent extent = new TemporalExtent();
	TimeIndeterminateValueType endTimeInderminate = TimeIndeterminateValueType.NOW;
	extent.setIndeterminateEndPosition(endTimeInderminate);
	TimeIndeterminateValueType startIndeterminate = TimeIndeterminateValueType.AFTER;
	extent.setIndeterminateBeginPosition(startIndeterminate);
	Calendar efd = Calendar.getInstance();
	efd.setTime(new Date());
	String value = ISO8601DateTimeUtils.getISO8601Date(efd.getTime());
	extent.setBeginPosition(value);

	Dataset dataset = new Dataset();
	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(extent);

	// TemporalExtent timeExt = dataset.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
	// String begin = timeExt.getBeginPosition();
	// String end = timeExt.getEndPosition();
	// TimeIndeterminateValueType indBegin = timeExt.getIndeterminateBeginPosition();
	// TimeIndeterminateValueType indEnd = timeExt.getIndeterminateEndPosition();

    }

}
