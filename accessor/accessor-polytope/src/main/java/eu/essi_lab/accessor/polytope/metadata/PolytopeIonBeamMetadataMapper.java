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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
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
public class PolytopeIonBeamMetadataMapper extends OriginalIdentifierMapper {

    private static final String POLYTOPE_IONBEAM = "POLYTOPE_IONBEAM";

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public PolytopeIonBeamMetadataMapper() {
	// do nothing
    }

    public static OriginalMetadata create(JSONObject datasetInfo, JSONObject sensorInfo) {
	OriginalMetadata originalMetadata = new OriginalMetadata();

	originalMetadata.setSchemeURI(CommonNameSpaceContext.POLYTOPE_IONBEAM);

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("dataset-info", datasetInfo);
	jsonObject.put("sensor-info", sensorInfo);

	originalMetadata.setMetadata(jsonObject.toString(4));

	return originalMetadata;

    }

    /**
     * @param metadata
     * @return
     */
    private JSONObject retrieveDatasetInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getJSONObject("dataset-info");
    }

    /**
     * @param sensor metadata
     * @return
     */
    private JSONObject retrieveSensorInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getJSONObject("sensor-info");
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
	return CommonNameSpaceContext.POLYTOPE_IONBEAM;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {

	//
	// DATASET INFO
	// {
	// "external_id": "62ab72c11d8e11061d32002a",
	// "platform": "meteotracker",
	// "location_point": "POINT (8.65711845 44.3934191)",
	// "location_bbox": "POLYGON ((8.675219 44.386031, 8.675219 44.4008072, 8.6390179 44.4008072, 8.6390179
	// 44.386031, 8.675219 44.386031))",
	// "location_hull": "POLYGON ((8.6391694 44.386031, 8.6391037 44.3860898, 8.6390179 44.3863223, 8.6391489
	// 44.3864548, 8.6393916 44.3866083, 8.6636556 44.3988833, 8.6672147 44.4002499, 8.6676504 44.4003547, 8.6690215
	// 44.4005383, 8.6708526 44.4007344, 8.6736574 44.4008072, 8.6742229 44.4007635, 8.6746885 44.4006371, 8.6750503
	// 44.4005098, 8.675219 44.400353, 8.6750815 44.4002186, 8.6655395 44.3914033, 8.665203 44.3911424, 8.664728
	// 44.3910255, 8.6391694 44.386031))",
	// "start_time": "2022-06-16T18:13:15Z",
	// "stop_time": "2022-06-16T18:21:09Z",
	// "authors": [
	// {
	// "name": "genova_living_lab_1",
	// "description": null,
	// "url": null
	// },
	// {
	// "name": "meteotracker",
	// "description": null,
	// "url": null
	// }
	// ],
	// "sensors": [
	// {
	// "name": "meteotracker Sensor",
	// "description": "A placeholder sensor that is likely composed of multiple physical devices.",
	// "url": null,
	// "properties": [
	// {
	// "key": "relative_humidity_near_surface",
	// "name": "relative_humidity_near_surface",
	// "unit": "%",
	// "description": null,
	// "url": null
	// },
	//
	// {
	// "key": "altitude",
	// "name": "altitude",
	// "unit": "m",
	// "description": "The altitude of the observation, referenced to WGS84 (EPSG: 4326)",
	// "url": null
	// }
	// ]
	// }
	// ]
	// }

	JSONObject datasetInfo = retrieveDatasetInfo(originalMD);

	JSONObject sensorInfo = retrieveSensorInfo(originalMD);

	String stationName = datasetInfo.optString("stationName");

	String stationId = datasetInfo.optString("external_id");

	String startDate = datasetInfo.optString("start_time");
	String endDate = datasetInfo.optString("stop_time");

	JSONArray authors = datasetInfo.optJSONArray("authors");

	String location_bbox = datasetInfo.optString("location_bbox");
	String location_hull = datasetInfo.optString("location_hull");
	String platformName = datasetInfo.optString("platform");

	String key = sensorInfo.optString("key");
	PolytopeIonBeamMetadataVariable variable = PolytopeIonBeamMetadataVariable.decode(key);

	Double pointLon = datasetInfo.optDouble("longitude");
	Double pointLat = datasetInfo.optDouble("latitude");
	Double altitude = datasetInfo.optDouble("altitude");

	Resolution resolution = Resolution.HOURLY;

	// TEMPORAL EXTENT
	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	TemporalExtent extent = new TemporalExtent();

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

	coreMetadata.setTitle("Acquisitions of " + variable.getLabel() + " through MeteoTracker mobile weather station: " + stationId);
	coreMetadata.setAbstract("This dataset contains " + variable.getLabel()
		+ " timeseries from I-CHANGE Citizen Observatory, acquired by a specific observing mobile weather station (" + stationId
		+ " ).");

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(POLYTOPE_IONBEAM);

	coreMetadata.getMIMetadata().getDataIdentification().setCitationPublicationDate(ISO8601DateTimeUtils.getISO8601Date(new Date()));

	// if (!station.getState().equals("")) {
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword("State: " + station.getState());
	// }
	// if (!station.getCountry().equals("")) {
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword("Country: " + station.getCountry());
	// }
	// if (!station.getIcao().equals("")) {
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword("ICAO: " + station.getIcao());
	// }

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(variable.getLabel());
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(variable.getKey());
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(variable.toString().toLowerCase());

	if (resolution.equals(Resolution.HOURLY))
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("hourly");

	ExtensionHandler handler = dataset.getExtensionHandler();
	handler.setTimeUnits("h");
	handler.setTimeResolution("1");
	handler.setAttributeMissingValue("-9999");
	handler.setAttributeUnitsAbbreviation(variable.getUnit());

	//
	// URL + variable
	//
	// String id = UUID.nameUUIDFromBytes((splittedStrings[0] + splittedStrings[11]).getBytes()).toString();

	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	// bounding box (Multipoint)
	// "POLYGON ((8.6391694 44.386031, 8.6391037 44.3860898, 8.6390179 44.3863223, 8.6391489
	// 44.3864548, 8.6393916 44.3866083, 8.6636556 44.3988833, 8.6672147 44.4002499, 8.6676504 44.4003547, 8.6690215
	// 44.4005383, 8.6708526 44.4007344, 8.6736574 44.4008072, 8.6742229 44.4007635, 8.6746885 44.4006371, 8.6750503
	// 44.4005098, 8.675219 44.400353, 8.6750815 44.4002186, 8.6655395 44.3914033, 8.665203 44.3911424, 8.664728
	// 44.3910255, 8.6391694 44.386031))",

	if (platformName.toLowerCase().contains("meteotracker")) {
	    // meteotracker use-case
	    dataset.getPropertyHandler().setIsTrajectory(true);
	} else {
	    dataset.getPropertyHandler().setIsTimeseries(true);
	}

	List<List<Double>> multiPoints = new ArrayList<List<Double>>();
	Double minLon = null;
	Double minLat = null;
	Double maxLon = null;
	Double maxLat = null;
	Double minAlt = null;
	Double maxAlt = null;
	String[] coordinates;
	location_hull = location_hull.replace("POLYGON ((", "");
	location_hull = location_hull.replace("POINT ((", "");
	location_hull = location_hull.replace("))", "");
	coordinates = location_hull.split(", ");
	for (int i = 0; i < coordinates.length; i++) {
	    String[] splittedCoord = coordinates[i].split(" ");
	    Double lonDouble = Double.valueOf(splittedCoord[0]);
	    Double latDouble = Double.valueOf(splittedCoord[1]);
	    Double altDouble = null;
	    List<Double> lat_lon_alt = new ArrayList<>();
	    lat_lon_alt.add(latDouble);
	    lat_lon_alt.add(lonDouble);
	    lat_lon_alt.add(altDouble);
	    multiPoints.add(lat_lon_alt);

	    if (minLat == null)
		minLat = latDouble;
	    if (minLon == null)
		minLon = lonDouble;
	    if (maxLat == null)
		maxLat = latDouble;
	    if (maxLon == null)
		maxLon = lonDouble;
	    if (minAlt == null)
		minAlt = altDouble;
	    if (maxAlt == null)
		maxAlt = altDouble;

	    if (minLat != null && minLat > latDouble) {
		minLat = latDouble;
	    }
	    if (minLon != null && minLon > lonDouble) {
		minLon = lonDouble;
	    }
	    if (maxLat != null && maxLat < latDouble) {
		maxLat = latDouble;
	    }
	    if (maxLon != null && maxLon < lonDouble) {
		maxLon = lonDouble;
	    }
	    if (minAlt != null && minAlt > altDouble) {
		minAlt = altDouble;
	    }
	    if (maxAlt != null && maxAlt < altDouble) {
		maxAlt = altDouble;
	    }

	}

	// Double[] bbox = multipointToBbox(coordinates);

	// bounding box (Multipoint)

	if (multiPoints != null && multiPoints.size() > 0) {
	    BoundingPolygon myPolygon = new BoundingPolygon();

	    myPolygon.setMultiPoints(multiPoints);

	    coreMetadata.getMIMetadata().getDataIdentification().addBoundingPolygon(myPolygon);

	}
	// bounding box
	if (minLon != null && minLat != null && maxLon != null && maxLat != null) {

	    coreMetadata.addBoundingBox(maxLat, minLon, minLat, maxLon);

	}
	// elevation

	if (minAlt != null && maxAlt != null) {
	    VerticalExtent verticalExtent = new VerticalExtent();
	    verticalExtent.setMinimumValue(minAlt);
	    verticalExtent.setMaximumValue(maxAlt);
	    coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(verticalExtent);
	}

	// elevation
	// String minElevation = station.getMinElevation();
	// String maxElevation = station.getMaxElevation();
	// if (minElevation != null && !minElevation.equals("") && maxElevation != null && !maxElevation.equals("")) {
	// VerticalExtent verticalExtent = new VerticalExtent();
	// if (isDouble(minElevation)) {
	// verticalExtent.setMinimumValue(Double.parseDouble(minElevation));
	// }
	// if (isDouble(maxElevation)) {
	// verticalExtent.setMaximumValue(Double.parseDouble(maxElevation));
	// }
	// coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(verticalExtent);
	// }

	// contact point
	if (authors != null) {
	    String owner = null;
	    for (int k = 0; k < authors.length(); k++) {
		JSONObject authorObj = authors.optJSONObject(k);
		if (authorObj != null) {
		    String authorName = authorObj.optString("name");
		    if (!authorName.contains("meteotracker")) {
			owner = authorName;
			break;
		    }
		}
	    }
	    if (owner != null) {
		ResponsibleParty creatorContact = new ResponsibleParty();
		creatorContact.setOrganisationName(owner);
		creatorContact.setRoleCode("originator");
		// creatorContact.setIndividualName("Anirban Guha");
		coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);
	    }
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

	/**
	 * MIPLATFORM
	 **/

	MIPlatform platform = new MIPlatform();

	String platformIdentifier = "i-change-citizen-observatory-ionbeam:" + stationId;

	platform.setMDIdentifierCode(platformIdentifier);

	String siteDescription = stationId;

	platform.setDescription(siteDescription);

	Citation platformCitation = new Citation();
	platformCitation.setTitle(stationId);
	platform.setCitation(platformCitation);

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	/**
	 * COVERAGEDescription
	 **/

	CoverageDescription coverageDescription = new CoverageDescription();
	String variableId = "IONBEAM:" + variable.name();

	coverageDescription.setAttributeIdentifier(variableId);
	coverageDescription.setAttributeTitle(variable.getLabel());

	String attributeDescription = variable.getLabel() + " Units: " + variable.getUnit();

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

	String resourceIdentifier = generateCode(dataset, variable + ":" + stationId);

	coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

	// https://ionbeam-dev.ecmwf.int/api/v1/retrieve?project=public&platform=meteotracker&observation_variable=air_temperature_near_surface&
	// datetime=2022-06-16T18%3A13%3A15%2B00%3A00&filter=select+%2A+from+result+where+source_id+%3D+%2762ab72c11d8e11061d32002a%27%3B&format=csv

	/**
	 * Linkage url to be parametized:
	 * project={public, i-change}
	 * platform = {meteotracker, acronet, smart}
	 * observation_variable = platform.getKey()
	 */
	try {
	    startDate = startDate.replace("Z", "+00:00");
	    String linkage = PolytopeIonBeamMetadataConnector.BASE_URL
		    + "retrieve?project=public&platform=meteotracker&observation_variable=" + variable.getKey() + "&datetime="
		    + URLEncoder.encode(startDate, "UTF-8") + "&filter=select+*+from+result+where+source_id+%3D+%27"
		    +  stationId + "%27%3B&format=json";// + station.getName() +
	    // buildingURL;

	    Online o = new Online();
	    o.setLinkage(linkage);
	    o.setFunctionCode("download");
	    o.setName(stationId + ":" + variable.getKey());
	    o.setIdentifier(stationId + ":" + variableId);
	    o.setProtocol(CommonNameSpaceContext.POLYTOPE_IONBEAM);
	    o.setDescription(variable.getLabel() + " Station name: " + stationId);
	    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(o);
	
	} catch (Exception e) {
	    // TODO: handle exception
	}

	coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);

    }

    private Double[] multipointToBbox(String[] coordinates) {
	Double[] bbox = new Double[4];
	Double maxlat = null;
	Double minlat = null;
	Double maxlon = null;
	Double minlon = null;
	try {
	    for (int i = 0; i < coordinates.length; i++) {
		if (i % 2 == 0) {
		    String value = coordinates[i].replaceAll("\\[", "").replaceAll("\\]", "");
		    Double val = Double.valueOf(value);
		    if (maxlon == null) {
			maxlon = val;
			minlon = val;
		    }
		    if (val > maxlon) {
			maxlon = val;
		    }
		    if (val < minlon) {
			minlon = val;
		    }

		} else {
		    String value = coordinates[i].replaceAll("\\[", "").replaceAll("\\]", "");
		    Double val = Double.valueOf(value);
		    if (maxlat == null) {
			maxlat = val;
			minlat = val;
		    }
		    if (val > maxlat) {
			maxlat = val;
		    }
		    if (val < minlat) {
			minlat = val;
		    }
		}
	    }
	    if (maxlat != null && maxlon != null) {
		bbox[0] = minlon;
		bbox[1] = minlat;
		bbox[2] = maxlon;
		bbox[3] = maxlat;
		return bbox;
	    }

	    return new Double[0];
	} catch (Exception e) {
	    logger.warn("Exception converting multipoint to bounding box", e);
	    return new Double[0];
	}
    }

    protected Map<PolytopeIonBeamMetadataVariable, PolytopeIonBeamMetadataStation> getMapStations(String originalMetadata) {

	Map<PolytopeIonBeamMetadataVariable, PolytopeIonBeamMetadataStation> mapStations = new HashMap<>();
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

    protected Map<PolytopeIonBeamMetadataVariable, PolytopeIonBeamMetadataStation> readCSV(Iterable<CSVRecord> records) {

	Map<PolytopeIonBeamMetadataVariable, PolytopeIonBeamMetadataStation> mapStations = new HashMap<>();

	for (CSVRecord record : records) {

	    String stationName = record.get("stationid@hdr");
	    String lat = record.get("lat@hdr");
	    String lon = record.get("lon@hdr");
	    String date = record.get("andate@desc");
	    String time = record.get("antime@desc");
	    String dateTime = buildDate(date, time);
	    String varName = record.get("varno@body");
	    String alt = record.get("stalt@hdr");
	    PolytopeIonBeamMetadataVariable pv = PolytopeIonBeamMetadataVariable.decode(varName);

	    if (mapStations.isEmpty()) {
		PolytopeIonBeamMetadataStation station = new PolytopeIonBeamMetadataStation();
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
		PolytopeIonBeamMetadataStation polStation = mapStations.get(pv);
		if (polStation != null) {
		    // already saved -- update it
		    polStation.setEndDateTime(dateTime);

		} else {
		    // this should never happen
		    // now one csv for variable
		    polStation = new PolytopeIonBeamMetadataStation();
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
	    GSLoggerFactory.getLogger(PolytopeIonBeamMetadataMapper.class).error(e.getMessage());
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