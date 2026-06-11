/**
 * 
 */
package eu.essi_lab.accessor.trigger;

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

import eu.essi_lab.accessor.trigger.TRIGGERConnector.TRIGGER_VARIABLES;
import eu.essi_lab.iso.datamodel.classes.*;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.*;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.chrono.MinguoDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import eu.essi_lab.accessor.trigger.AggregatedTRIGGERConnector.AGGREGATED_TRIGGER_VARIABLES;

/**
 * @author Roberto
 */
public class AggregatedTRIGGERMapper extends OriginalIdentifierMapper {

    private static DateTimeFormatter formatter;

    /**
     * {
     * "nPoints": 650,
     * "nPhotos": 0,
     * "offsetTZ": "+0100",
     * "_id": "65a780ffbf31550fb0984ce3",
     * "T0": {
     * "maxVal": 10.8,
     * "minVal": 6.4,
     * "avgVal": 7.7
     * },
     * "H": {
     * "maxVal": 87,
     * "minVal": 71,
     * "avgVal": 77
     * },
     * "a": {
     * "maxVal": 147,
     * "minVal": 9,
     * "avgVal": 55
     * },
     * "P": {
     * "maxVal": 1007,
     * "minVal": 990,
     * "avgVal": 1001
     * },
     * "td": {
     * "maxVal": 7.5,
     * "minVal": 2.9,
     * "avgVal": 4.1
     * },
     * "HDX": {
     * "maxVal": 10.5,
     * "minVal": 5.1,
     * "avgVal": 6.6
     * },
     * "i": {
     * "maxVal": -15,
     * "minVal": -0.7,
     * "avgVal": 4.2
     * },
     * "L": {
     * "maxVal": 0,
     * "minVal": 0,
     * "avgVal": 0
     * },
     * "bt": {},
     * "tp": {
     * "maxVal": 283.9,
     * "minVal": 279.9,
     * "avgVal": 280.8
     * },
     * "CO2": {},
     * "m1": {},
     * "m2": {},
     * "m4": {},
     * "m10": {},
     * "n0": {},
     * "n1": {},
     * "n2": {},
     * "n4": {},
     * "n10": {},
     * "tps": {},
     * "EAQ": {},
     * "FAQ": {},
     * "O3": {},
     * "from": "Arenzano",
     * "startTime": "2024-01-17T07:25:46.000Z",
     * "by": "genova_living_lab_1",
     * "to": "Genova",
     * "endTime": "2024-01-17T12:19:58.000Z"
     * }
     */

    public AggregatedTRIGGERMapper() {
	this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    }

    /**
     * @param datasetInfo
     * @param variableName
     * @return
     */
    static OriginalMetadata create(JSONObject datasetInfo, String variableName, String queryPath, List<TRIGGERTimePosition> position) {

	OriginalMetadata originalMetadata = new OriginalMetadata();

	originalMetadata.setSchemeURI(CommonNameSpaceContext.AGGREGATED_TRIGGER);
	List<LocalDateTime> dates = position.stream().map(TRIGGERTimePosition::getDateTime).toList();
	var minDate = Collections.min(dates);
	var maxDate = Collections.max(dates);

	JSONObject jsonObject = new JSONObject();
	JSONObject locationTimeObject = new JSONObject();
	JSONArray timeSeries = new JSONArray();
	if (position != null) {
	    for (TRIGGERTimePosition tpos : position) {
		JSONObject pointObject = new JSONObject();
		JSONObject locationObject = new JSONObject();
		LocalDateTime time = tpos.getDateTime();
		Double lon = tpos.getLongitude();
		Double lat = tpos.getLatitude();
		String timeString = time.format(formatter);
		locationObject.put("latitude", lat);
		locationObject.put("longitude", lon);
		pointObject.put("location", locationObject);
		pointObject.put("time", timeString);
		timeSeries.put(pointObject);

	    }
	    jsonObject.put("time-series", timeSeries);
	}

	jsonObject.put("dataset-info", datasetInfo);
	jsonObject.put("begin-date", minDate);
	jsonObject.put("end-date", maxDate);
	// jsonObject.put("sensor-info", sensorInfo);
	// jsonObject.put("var-type", variableType);
	jsonObject.put("var-name", variableName);
	jsonObject.put("query-path", queryPath);
	// jsonObject.put("interpolation-type", interpolationType);

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
     * @param metadata
     * @return
     */
    private String retrieveVariableInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getString("var-name");
    }
    
    /**
     * @param metadata
     * @return
     */
    private String retrieveQueryPath(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getString("query-path");
    }

    /**
     * @param metadata
     * @return
     */
    private String retrieveStartDate(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getString("begin-date");
    }

    /**
     * @param metadata
     * @return
     */
    private String retrieveEndDate(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getString("end-date");
    }

    /**
     * @param metadata
     * @return
     */
    private JSONArray retrieveTimeSeries(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).optJSONArray("time-series");
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	String metadata = resource.getOriginalMetadata().getMetadata();
	JSONObject object = new JSONObject(metadata);

	if (object.has("_id")) {
	    return object.getString("_id");
	}

	return null;
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;

    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {

	try {

	    String metadata = originalMD.getMetadata();

	    JSONObject object = retrieveDatasetInfo(originalMD);

	    String variable = retrieveVariableInfo(originalMD);

	    // String interp = retrieveInterpolationCode(originalMD);
	    
	    String queryPath = retrieveQueryPath(originalMD);

	    String startDate = retrieveStartDate(originalMD);
	    String endDate = retrieveEndDate(originalMD);

	    JSONArray timeSeries = retrieveTimeSeries(originalMD);

	    String units = null;

	    // JSONObject altitude = object.optJSONObject(METEOTRACKER_VARIABLES.a.name());
	    // if (altitude != null) {
	    // minAlt = altitude.optBigDecimal("minVal", null);
	    // maxAlt = altitude.optBigDecimal("maxVal", null);
	    // avgAlt = altitude.optBigDecimal("avgVal", null);
	    // }

	    if (object != null && !object.isEmpty()) {

		String userId = object.optString("userId");

		AGGREGATED_TRIGGER_VARIABLES var = AGGREGATED_TRIGGER_VARIABLES.decode(variable);
		if (var != null) {

		    units = var.getUnits();
		    if (units.contains("°C")) {
			units = units.replace("°C", "K");
		    }

		    Double minLon = null;
		    Double minLat = null;
		    Double maxLon = null;
		    Double maxLat = null;
		    Double minAlt = null;
		    Double maxAlt = null;
		    List<List<Double>> multiPoints = new ArrayList<List<Double>>();
		    if (timeSeries != null) {

			dataset.getPropertyHandler().setIsTrajectory(true);
			

			for (int j=0; j <  timeSeries.length() ; j++) {
			    JSONObject tsObj = timeSeries.getJSONObject(j);
			    JSONObject locObj = tsObj.optJSONObject("location");
			    
			    
			    Double lonDouble = locObj.optDouble("longitude");
			    Double latDouble = locObj.optDouble("latitude");
			    // Double latDouble = Double.valueOf(lat);
			    // Double lonDouble = Double.valueOf(lon);
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

			}
		    }

		    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

		    coreMetadata.setTitle("Acquisitions of " + var.getLabel() + " through TRIGGER API device identifier: " + userId);
		    coreMetadata.setAbstract(
			    "This dataset contains " + var.getLabel() + " timeseries from TRIGGER API, acquired by a specific device.");

		    // Map<PolytopeVariable, PolytopeStation> mapStations = getMapStations(originalMetadata);
		    //
		    // if (!mapStations.isEmpty()) {
		    //
		    // PolytopeStation station = mapStations.get(PolytopeVariable.TEMPERATURE);
		    // PolytopeVariable variable = PolytopeVariable.TEMPERATURE;
		    // String abbreviation = "K";
		    // String buildingURL = "_TEMP.csv";
		    // if (station == null) {
		    // station = mapStations.get(PolytopeVariable.HUMIDITY);
		    // variable = PolytopeVariable.HUMIDITY;
		    // buildingURL = "_HUM.csv";
		    // abbreviation = "%";
		    // }

		    // TEMPORAL EXTENT

		    TemporalExtent extent = new TemporalExtent();
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
			 * // // only an estimate seems to be possible, as this odata service doesn't seem to support
			 * the
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

		    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("TRIGGER");

		    coreMetadata.getMIMetadata().getDataIdentification()
			    .setCitationPublicationDate(ISO8601DateTimeUtils.getISO8601Date(new Date()));

		    // if (!station.getState().equals("")) {
		    // coreMetadata.getMIMetadata().getDataIdentification().addKeyword("State: " + station.getState());
		    // }
		    // if (!station.getCountry().equals("")) {
		    // coreMetadata.getMIMetadata().getDataIdentification().addKeyword("Country: " +
		    // station.getCountry());
		    // }
		    // if (!station.getIcao().equals("")) {
		    // coreMetadata.getMIMetadata().getDataIdentification().addKeyword("ICAO: " + station.getIcao());
		    // }

		    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(variable);
		    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(var.getLabel());
		    // coreMetadata.getMIMetadata().getDataIdentification().addKeyword(interp);
		    // coreMetadata.getMIMetadata().getDataIdentification().addKeyword(variable.toString().toLowerCase());

		    ExtensionHandler handler = dataset.getExtensionHandler();
		    handler.setTimeResolutionDuration8601("PT1H");
		    handler.setAttributeMissingValue("-9999");
		    handler.setAttributeUnitsAbbreviation(var.getUnits());

		    //
		    // URL + variable
		    //
		    // String id = UUID.nameUUIDFromBytes((splittedStrings[0] +
		    // splittedStrings[11]).getBytes()).toString();

		    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

		    // bounding box (Multipoint)

		    // List<List<Double>> multiPoint = station.getMultiPoint();
		    //
		    if (multiPoints != null && multiPoints.size() > 0) {
			BoundingPolygon myPolygon = new BoundingPolygon();

			myPolygon.setMultiPoints(multiPoints);

			coreMetadata.getMIMetadata().getDataIdentification().addBoundingPolygon(myPolygon);

		    }

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

		    // contact point
		    ResponsibleParty creatorContact = new ResponsibleParty();

		    creatorContact.setOrganisationName("UNIBO");
		    creatorContact.setRoleCode("originator");
		    // creatorContact.setIndividualName("Anirban Guha");

		    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);

		    /**
		     * MIPLATFORM
		     **/

		    MIPlatform platform = new MIPlatform();

		    String platformIdentifier = "trigger-unibo:" + userId;

		    platform.setMDIdentifierCode(platformIdentifier);

		    String siteDescription = userId;

		    platform.setDescription(siteDescription);

		    Citation platformCitation = new Citation();
		    platformCitation.setTitle(userId);
		    platform.setCitation(platformCitation);

		    coreMetadata.getMIMetadata().addMIPlatform(platform);

		    // /**
		    // * COVERAGEDescription
		    // **/
		    //
		    CoverageDescription coverageDescription = new CoverageDescription();
		    String variableId = var.name();

		    coverageDescription.setAttributeIdentifier(variableId);
		    coverageDescription.setAttributeTitle(var.getLabel());

		    String attributeDescription = var.getLabel() + " Units: " + units;

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

		    String resourceIdentifier = generateCode(dataset, variable + ":" + userId);

		    coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

		    String linkage = TRIGGERConnector.BASE_URL + queryPath + "select=year,month,day,hour,minute,second,userId," +  variable.toLowerCase() +"&where=userId=" + userId;// +
														    // station.getName()
														    // +
		    // buildingURL;
		    Online o = new Online();
		    o.setLinkage(linkage);
		    o.setFunctionCode("download");
		    o.setName(userId + ":" + variable);
		    o.setIdentifier(userId + ":" + variableId);
		    o.setProtocol(CommonNameSpaceContext.TRIGGER);
		    o.setDescription(var.getLabel() + " Station name: " + userId);
		    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(o);

		    coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);
		}
	    }

	} catch (

	Exception e) {
	    GSLoggerFactory.getLogger(getClass()).info("Error mapping" + e);

	}
    }


    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.AGGREGATED_TRIGGER;
    }

}
