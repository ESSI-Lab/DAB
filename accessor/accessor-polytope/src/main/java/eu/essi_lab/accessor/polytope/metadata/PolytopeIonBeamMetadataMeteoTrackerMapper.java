package eu.essi_lab.accessor.polytope.metadata;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
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
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * @author roncella
 */
public class PolytopeIonBeamMetadataMeteoTrackerMapper extends PolytopeIonBeamMetadataMapper {

    private static final String POLYTOPE_METEOTRACKER = "POLYTOPE METEOTRACKER";

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public PolytopeIonBeamMetadataMeteoTrackerMapper() {
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
	return CommonNameSpaceContext.POLYTOPE_METEOTRACKER;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {

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

	dataset.getPropertyHandler().setIsTrajectory(true);

	String originalMetadata = originalMD.getMetadata();
	Map<PolytopeIonBeamMetadataVariable, PolytopeIonBeamMetadataStation> mapStations = getMapStations(originalMetadata);

	if (!mapStations.isEmpty()) {

	    PolytopeIonBeamMetadataStation station = mapStations.get(PolytopeIonBeamMetadataVariable.TEMPERATURE);
	    PolytopeIonBeamMetadataVariable variable = PolytopeIonBeamMetadataVariable.TEMPERATURE;
	    String abbreviation = "K";
	    String buildingURL = "_TEMP.csv";
	    if (station == null) {
		station = mapStations.get(PolytopeIonBeamMetadataVariable.HUMIDITY);
		variable = PolytopeIonBeamMetadataVariable.HUMIDITY;
		buildingURL = "_HUM.csv";
		abbreviation = "%";
	    }

	    // TEMPORAL EXTENT
	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	    TemporalExtent extent = new TemporalExtent();

	    String startDate = station.getStartDateTime();
	    String endDate = station.getEndDateTime();
	    if (startDate != null && !startDate.isEmpty()) {

		extent.setBeginPosition(startDate);

		// Date begin = setTime(startDate, station.getStartTime());
		//
		// if (begin != null) {
		// String stringStart = ISO8601DateTimeUtils.getISO8601DateTime(begin);

		// }

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
		coreMetadata.setTitle(
			"Acquisitions of " + variable.getLabel() + " through MeteoTracker mobile weather station: " + station.getName());
		coreMetadata.setAbstract("This dataset contains " + variable.name().toLowerCase()
			+ " timeseries form I-CHANGE Citizen Observatory, acquired by a specific observing mobile weather station ("
			+ station.getName() + ") of the MeteoTracker.");
	    } else {
		coreMetadata.setTitle("Acquisitions of " + variable.getLabel() + " through MeteoTracker mobile weather station: "
			+ station.getStationCode());
		coreMetadata.setAbstract("This dataset contains " + variable.name().toLowerCase()
			+ " timeseries form I-CHANGE Citizen Observatory, acquired by a specific observing mobile weather station ("
			+ station.getName() + ") of the MeteoTracker.");
	    }
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(POLYTOPE_METEOTRACKER);

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

	    // bounding box (Multipoint)

	    List<List<Double>> multiPoint = station.getMultiPoint();

	    if (multiPoint != null && multiPoint.size() > 0) {

		BoundingPolygon myPolygon = new BoundingPolygon();

		myPolygon.setMultiPoints(multiPoint);

		coreMetadata.getMIMetadata().getDataIdentification().addBoundingPolygon(myPolygon);

	    }

	    String minLon = station.getMinLon();
	    String maxLon = station.getMaxLon();
	    String minLat = station.getMinLat();
	    String maxLat = station.getMaxLat();
	    if (minLon != null && !minLon.isEmpty() && minLat != null && !minLat.isEmpty() && maxLon != null && !maxLon.isEmpty()
		    && maxLat != null && !maxLat.isEmpty()) {

		coreMetadata.addBoundingBox(Double.parseDouble(maxLat), Double.parseDouble(minLon), Double.parseDouble(minLat),
			Double.parseDouble(maxLon));

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
	    // contactcreatorTechContactInfo.setAddress(addresscreatorTechContact); //
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
	    String variableId = variable.toString();

	    coverageDescription.setAttributeIdentifier(variableId);
	    coverageDescription.setAttributeTitle(variable.getLabel());

	    String attributeDescription = variable.toString() + " Units: " + variable.getUnit();

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

	    String linkage = "https://i-change.s3.amazonaws.com/meteotracker_" + station.getName() + buildingURL;
	    Online o = new Online();
	    o.setLinkage(linkage);
	    o.setFunctionCode("download");
	    o.setName(station.getName() + " - " + variable);
	    o.setIdentifier(variable.getId() + ":" + station.getName());
	    o.setProtocol(CommonNameSpaceContext.POLYTOPE_METEOTRACKER);
	    o.setDescription(variable + " Station name: " + station.getName());
	    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(o);

	    coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);
	}

    }

    protected Map<PolytopeIonBeamMetadataVariable, PolytopeIonBeamMetadataStation> readCSV(Iterable<CSVRecord> records) {

	Map<PolytopeIonBeamMetadataVariable, PolytopeIonBeamMetadataStation> mapStations = new HashMap<>();
	Double minLon = null;
	Double minLat = null;
	Double maxLon = null;
	Double maxLat = null;
	Double minAlt = null;
	Double maxAlt = null;
	String updatedDateTime = null;
	List<List<Double>> multiPoints = new ArrayList<List<Double>>();
	for (CSVRecord record : records) {
	    String stationName = record.get("stationid@hdr");
	    String lat = record.get("lat@hdr");
	    String lon = record.get("lon@hdr");
	    String date = record.get("andate@desc");
	    String time = record.get("antime@desc");
	    String varName = record.get("varno@body");
	    String timeDelay = record.get("min@body");
	    String alt = record.get("stalt@hdr");
	    String dateTime = buildDate(date, time);
	    Double latDouble = Double.valueOf(lat);
	    Double lonDouble = Double.valueOf(lon);
	    Double altDouble = Double.valueOf(alt);

	    List<Double> lat_lon_alt = new ArrayList<>();
	    lat_lon_alt.add(latDouble);
	    lat_lon_alt.add(lonDouble);
	    lat_lon_alt.add(altDouble);
	    multiPoints.add(lat_lon_alt);
	    PolytopeIonBeamMetadataVariable pv = PolytopeIonBeamMetadataVariable.decode(varName);

	    if (mapStations.isEmpty()) {
		minLat = latDouble;
		minLon = lonDouble;
		maxLat = latDouble;
		maxLon = lonDouble;
		minAlt = altDouble;
		maxAlt = altDouble;
		PolytopeIonBeamMetadataStation station = new PolytopeIonBeamMetadataStation();
		station.setName(stationName);
		station.setStationCode(stationName);
		station.setMultiPoint(multiPoints);
		station.setMinLat(lat);
		station.setMaxLat(lat);
		station.setMinLon(lon);
		station.setMaxLon(lon);
		updatedDateTime = updateDateTime(dateTime, timeDelay.trim());
		station.setStartDateTime(updatedDateTime);
		station.setEndDateTime(updatedDateTime);
		station.setMinElevation(alt);
		station.setMaxElevation(alt);
		mapStations.put(pv, station);

	    } else {
		PolytopeIonBeamMetadataStation polStation = mapStations.get(pv);
		if (polStation != null) {
		    // already saved -- update it
		    updatedDateTime = updateDateTime(dateTime, timeDelay.trim());
		    polStation.setEndDateTime(updatedDateTime);
		    polStation.setMultiPoint(multiPoints);
		    if (minLat != null && minLat > latDouble) {
			minLat = latDouble;
			polStation.setMinLat(lat);
		    }
		    if (minLon != null && minLon > lonDouble) {
			minLon = lonDouble;
			polStation.setMinLon(lon);
		    }
		    if (maxLat != null && maxLat < latDouble) {
			maxLat = latDouble;
			polStation.setMaxLat(lat);
		    }
		    if (maxLon != null && maxLon < lonDouble) {
			maxLon = lonDouble;
			polStation.setMaxLon(lon);
		    }
		    if (minAlt != null && minAlt > altDouble) {
			minAlt = altDouble;
			polStation.setMinElevation(alt);
		    }
		    if (maxAlt != null && maxAlt < altDouble) {
			maxAlt = altDouble;
			polStation.setMaxElevation(alt);
		    }

		} else {
		    // this should never happen
		    // now one csv for variable
		    polStation = new PolytopeIonBeamMetadataStation();
		    polStation.setName(stationName);
		    polStation.setStationCode(stationName);
		    polStation.setMinLat(lat);
		    polStation.setMaxLat(lat);
		    polStation.setMinLon(lon);
		    polStation.setMaxLon(lon);
		    polStation.setStartDateTime(dateTime);
		    polStation.setEndDateTime(dateTime);

		    mapStations.put(pv, polStation);
		}

	    }

	}

	return mapStations;
    }

    public static String updateDateTime(String updatedDateTime, String delay) {
	Optional<Date> d = ISO8601DateTimeUtils.parseISO8601ToDate(updatedDateTime);
	Date newDate = null;
	if (d.isPresent()) {
	    BigDecimal minutes = new BigDecimal(delay);
	    newDate = updateDateTime(d.get(), minutes);	    
	}
	return (newDate == null) ? updatedDateTime : ISO8601DateTimeUtils.getISO8601DateTime(newDate);
    }

    public static Date updateDateTime(Date initialDateTime, BigDecimal minutes) {
	long distTimeInMs = Math.round(minutes.doubleValue() * 60000);
	return new Date(initialDateTime.getTime() + distTimeInMs);
    }

    public static void main(String[] args) {

	String minutes = "38.60";

	double t = Double.parseDouble(minutes);
	Date date = new Date();
	long distTimeInMs = Math.round(t * 60000);
	Date newDate = new Date(date.getTime() + distTimeInMs);
	System.out.println(newDate.toString());
	newDate = new Date(newDate.getTime() + distTimeInMs);
	System.out.println(newDate.toString());
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
