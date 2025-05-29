/**
 * 
 */
package eu.essi_lab.accessor.trigger;

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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.trigger.TRIGGERConnector.TRIGGER_VARIABLES;
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

/**
 * @author Roberto
 */
public class TRIGGERMapper extends OriginalIdentifierMapper {

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

    public TRIGGERMapper() {
	this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    }

    /**
     * @param datasetInfo
     * @param sensorInfo
     * @return
     */
    static OriginalMetadata create(TRIGGERDevice datasetInfo, String variableName, String queryPath, List<TRIGGERTimePosition> position) {

	OriginalMetadata originalMetadata = new OriginalMetadata();

	originalMetadata.setSchemeURI(CommonNameSpaceContext.TRIGGER);
	LocalDateTime beginDate = datasetInfo.getBeginDate();
	LocalDateTime endDate = datasetInfo.getEndDate();
	String formattedStartString = beginDate.format(formatter);
	String formattedEndString = endDate.format(formatter);
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

	jsonObject.put("dataset-info", datasetInfo.getJSONObject());
	jsonObject.put("begin-date", formattedStartString);
	jsonObject.put("end-date", formattedEndString);
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

	    String originalMetadata = originalMD.getMetadata();
	    
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

		String deviceId = object.optString("deviceId");

		TRIGGER_VARIABLES var = TRIGGER_VARIABLES.decode(variable);
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

		    coreMetadata.setTitle("Acquisitions of " + var.getLabel() + " through TRIGGER API device identifier: " + deviceId);
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
		    handler.setTimeUnits("h");
		    handler.setTimeResolution("1");
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

		    String platformIdentifier = "trigger-unibo:" + deviceId;

		    platform.setMDIdentifierCode(platformIdentifier);

		    String siteDescription = deviceId;

		    platform.setDescription(siteDescription);

		    Citation platformCitation = new Citation();
		    platformCitation.setTitle(deviceId);
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

		    String resourceIdentifier = generateCode(dataset, variable + ":" + deviceId);

		    coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

		    String linkage = TRIGGERConnector.BASE_URL + queryPath + "deviceId=" + deviceId;// +
														    // station.getName()
														    // +
		    // buildingURL;
		    Online o = new Online();
		    o.setLinkage(linkage);
		    o.setFunctionCode("download");
		    o.setName(deviceId + ":" + variable);
		    o.setIdentifier(deviceId + ":" + variableId);
		    o.setProtocol(CommonNameSpaceContext.TRIGGER);
		    o.setDescription(var.getLabel() + " Station name: " + deviceId);
		    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(o);

		    coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);
		}
	    }

	    // String metadata = originalMD.getMetadata();
	    //
	    // JSONObject object = retrieveDatasetInfo(originalMD);
	    //
	    // String variable = retrieveVariableInfo(originalMD);
	    //
	    // String interp = retrieveInterpolationCode(originalMD);
	    //
	    // Dataset dataset = new Dataset();
	    //
	    // dataset.setSource(source);
	    //
	    // MIMetadata md_Metadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	    // DataIdentification dataId = md_Metadata.getDataIdentification();
	    //
	    // String id = "";
	    // String sid = "";
	    // String doi = "";
	    //
	    // String collection_id = "";
	    // String tags = "";
	    // // String longName = "HealthSites - ";
	    // String title = "Title not provided";
	    // String abstrakt = null;
	    // String timeStamp = null;
	    //
	    // String startDate = null;
	    // String endDate = null;
	    //
	    // Double eastBoundLongitude = null;
	    // Double northBoundLatitude = null;
	    // Double southBoundLatitude = null;
	    // Double westBoundLongitude = null;
	    // String language = null;
	    //
	    // // doi: 10.15493/SAEON.EGAGASINI.10000056 -->
	    // // https://catalogue.saeon.ac.za/records/10.15493/SAEON.EGAGASINI.10000056
	    //
	    // // identifier
	    // id = object.optString("_id");
	    //
	    // // sid
	    // sid = object.optString("_id");
	    //
	    // // collection_id
	    // collection_id = object.optString("collection_id");
	    //
	    // // tags
	    // tags = object.optString("tags");
	    //
	    // // timestamp
	    // timeStamp = object.optString("timestamp");
	    //
	    // // metadata (JSONArray)
	    // JSONArray metadataArray = object.optJSONArray("metadata");
	    // JSONObject elements = null;
	    // JSONObject metadataObject = null;
	    // if (metadataArray == null) {
	    // metadataArray = object.optJSONArray("metadata_records");
	    // }
	    // if (metadataArray != null && metadataArray.length() > 0) {
	    //
	    // for (int z = 0; z < metadataArray.length(); z++) {
	    //
	    // elements = metadataArray.optJSONObject(z);
	    //
	    // if (elements != null) {
	    // String schema = elements.optString("schema_id");
	    // if (schema != null && schema.toLowerCase().contains("iso19115")) {
	    // continue;
	    // }
	    // metadataObject = elements.optJSONObject("metadata");
	    // }
	    //
	    // if (metadataObject == null) {
	    // return null;
	    // }
	    //
	    // String publisherName = metadataObject.optString("publisher");
	    // String schemaVersion = metadataObject.optString("schemaVersion");
	    // String publicationYear = metadataObject.optString("publicationYear");
	    //
	    // // DATES
	    // JSONArray datesArray = metadataObject.optJSONArray("dates");
	    // if (datesArray != null && datesArray.length() > 0) {
	    // String dateString = datesArray.getJSONObject(0).optString("date");
	    // // should be something like this: 2018-05-31/2018-06-13
	    // String[] splittedDate = dateString.split("/");
	    // if (splittedDate.length > 1) {
	    // startDate = splittedDate[0];
	    // endDate = splittedDate[1];
	    // } else {
	    // startDate = dateString;
	    // }
	    // }
	    // // TITLE
	    // JSONArray titleArray = metadataObject.optJSONArray("titles");
	    // if (titleArray != null && titleArray.length() > 0) {
	    // title = titleArray.getJSONObject(0).optString("title");
	    // }
	    //
	    // // CREATORS
	    // HashMap<String, String> creatorsMap = new HashMap<String, String>();
	    // JSONArray creatorsArray = metadataObject.optJSONArray("creators");
	    // if (creatorsArray != null && creatorsArray.length() > 0) {
	    // for (int j = 0; j < creatorsArray.length(); j++) {
	    // JSONObject creatorObject = creatorsArray.getJSONObject(j);
	    // String creatorName = creatorObject.optString("name");
	    // String affiliation = null;
	    // JSONArray affilitianArray = creatorObject.optJSONArray("affiliation");
	    // if (affilitianArray != null && affilitianArray.length() > 0) {
	    // affiliation = affilitianArray.getJSONObject(0).optString("affiliation");
	    // }
	    // creatorsMap.put(creatorName, affiliation);
	    // }
	    // }
	    //
	    // // language
	    // language = metadataObject.optString("language");
	    // language = (language != null) ? language : "en";
	    // md_Metadata.setLanguage(language);
	    //
	    // // SUBJECTS (keywords)
	    // List<String> keywordList = new ArrayList<String>();
	    // JSONArray keywordsArray = metadataObject.optJSONArray("subjects");
	    // if (keywordsArray != null && keywordsArray.length() > 0) {
	    // for (int i = 0; i < keywordsArray.length(); i++) {
	    // String key = keywordsArray.getJSONObject(i).optString("subject");
	    // keywordList.add(key);
	    // }
	    // }
	    //
	    // // rightsList
	    // JSONArray rightsArray = metadataObject.optJSONArray("rightsList");
	    // String rights = null;
	    // String rightsURI = null;
	    // if (rightsArray != null && rightsArray.length() > 0) {
	    // rights = rightsArray.getJSONObject(0).optString("rights");
	    // rightsURI = rightsArray.getJSONObject(0).optString("rightsURI");
	    // }
	    //
	    // // publisher
	    // String publisher = metadataObject.optString("publisher");
	    // if (publisher != null && !publisher.isEmpty()) {
	    // ResponsibleParty party = new ResponsibleParty();
	    // party.setRoleCode("publisher");
	    // party.setOrganisationName(publisher);
	    // md_Metadata.getDataIdentification().addPointOfContact(party);
	    // md_Metadata.addContact(party);
	    // }
	    //
	    // // CONTRIBUTORS
	    // HashMap<String, String> contributorsMap = new HashMap<String, String>();
	    // JSONArray contributorsArray = metadataObject.optJSONArray("contributors");
	    // if (contributorsArray != null && contributorsArray.length() > 0) {
	    // for (int j = 0; j < contributorsArray.length(); j++) {
	    // JSONObject creatorObject = contributorsArray.getJSONObject(j);
	    // String contributorName = creatorObject.optString("name");
	    // String contributorType = creatorObject.optString("contributorType");
	    // String affiliation = null;
	    // JSONArray affilitianArray = creatorObject.optJSONArray("affiliation");
	    // if (affilitianArray != null && affilitianArray.length() > 0) {
	    // affiliation = affilitianArray.getJSONObject(0).optString("affiliation");
	    // }
	    // contributorsMap.put(contributorName + ":" + contributorType, affiliation);
	    // }
	    // }
	    //
	    // // ABSTRACT (descriptions)
	    // JSONArray abstarctArray = metadataObject.optJSONArray("descriptions");
	    // if (abstarctArray != null && abstarctArray.length() > 0) {
	    // abstrakt = abstarctArray.getJSONObject(0).optString("description");
	    // }
	    //
	    // // BBOX (geoLocations)
	    //
	    // JSONArray bboxArray = metadataObject.optJSONArray("geoLocations");
	    // if (bboxArray != null && bboxArray.length() > 0) {
	    // JSONObject bboxObject = bboxArray.getJSONObject(0).optJSONObject("geoLocationBox");
	    // if (bboxObject != null) {
	    // eastBoundLongitude = bboxObject.optDouble("eastBoundLongitude");
	    // northBoundLatitude = bboxObject.optDouble("northBoundLatitude");
	    // westBoundLongitude = bboxObject.optDouble("westBoundLongitude");
	    // southBoundLatitude = bboxObject.optDouble("southBoundLatitude");
	    // } else {
	    // // lat-long case
	    // JSONObject latLonObject = bboxArray.getJSONObject(0).optJSONObject("geoLocationPoint");
	    // if (latLonObject != null) {
	    // eastBoundLongitude = latLonObject.optDouble("pointLongitude");
	    // northBoundLatitude = latLonObject.optDouble("pointLatitude");
	    // westBoundLongitude = latLonObject.optDouble("pointLongitude");
	    // southBoundLatitude = latLonObject.optDouble("pointLatitude");
	    // }
	    // }
	    // }
	    //
	    // // ONLINES (immutableResource)
	    // JSONObject immutableResourceObj = metadataObject.optJSONObject("immutableResource");
	    // if (immutableResourceObj != null && immutableResourceObj.length() > 0) {
	    // String resourceDesc = immutableResourceObj.optString("resourceDescription");
	    // String resourceName = immutableResourceObj.optString("resourceName");
	    // JSONObject resourceDownload = immutableResourceObj.optJSONObject("resourceDownload");
	    // if (resourceDownload != null) {
	    // String onlineURL = resourceDownload.optString("downloadURL");
	    // if (onlineURL != null) {
	    // Online o = new Online();
	    // o.setLinkage(onlineURL);
	    // if (resourceName != null) {
	    // o.setName(resourceName);
	    // }
	    // if (resourceDesc != null) {
	    // o.setDescription(resourceDesc);
	    // }
	    // o.setFunctionCode("download");
	    // o.setProtocol("HTTP");
	    //
	    // o.setDescriptionGmxAnchor(AccessType.DIRECT_ACCESS.getDescriptionAnchor());
	    //
	    // md_Metadata.getDistribution().addDistributionOnline(o);
	    // }
	    // }
	    // }
	    //
	    // // ONLINES (linkedResources)
	    // JSONArray linkedResourcesArray = metadataObject.optJSONArray("linkedResources");
	    // if (linkedResourcesArray != null && linkedResourcesArray.length() > 0) {
	    // for (int j = 0; j < linkedResourcesArray.length(); j++) {
	    // Online o = new Online();
	    // String onlineURL = linkedResourcesArray.getJSONObject(j).optString("resourceURL");
	    // String onlineName = linkedResourcesArray.getJSONObject(j).optString("resourceName");
	    // String onlineDescription = linkedResourcesArray.getJSONObject(j).optString("resourceDescription");
	    // if (onlineURL != null) {
	    // o.setLinkage(onlineURL);
	    // if (onlineDescription != null) {
	    // o.setDescription(onlineDescription);
	    // }
	    // if (onlineName != null) {
	    // o.setName(onlineName);
	    // }
	    //
	    // o.setFunctionCode("download");
	    // o.setProtocol("HTTP");
	    //
	    // o.setDescriptionGmxAnchor(AccessType.DIRECT_ACCESS.getDescriptionAnchor());
	    //
	    // md_Metadata.getDistribution().addDistributionOnline(o);
	    // }
	    // }
	    // }
	    //
	    // // DOI online
	    // if (doi != null && !doi.isEmpty()) {
	    // Online o = new Online();
	    // o.setLinkage("https://doi.org/" + doi);
	    // o.setFunctionCode("download");
	    // o.setProtocol("HTTP");
	    // o.setDescription("DOI reference");
	    // o.setDescriptionGmxAnchor(AccessType.DIRECT_ACCESS.getDescriptionAnchor());
	    // md_Metadata.getDistribution().addDistributionOnline(o);
	    // } else {
	    // Online o = new Online();
	    // o.setLinkage("https://catalogue.saeon.ac.za/records/" + id);
	    // o.setFunctionCode("download");
	    // o.setProtocol("HTTP");
	    // o.setDescription("DOI reference");
	    // o.setDescriptionGmxAnchor(AccessType.DIRECT_ACCESS.getDescriptionAnchor());
	    // md_Metadata.getDistribution().addDistributionOnline(o);
	    // }
	    //
	    // if (title == null || title.isEmpty()) {
	    // title = "None";
	    // }
	    //
	    // dataId.setCitationTitle(title);
	    // dataId.setAbstract(abstrakt);
	    //
	    // // TEMPORAL EXTENT
	    // if (startDate != null) {
	    // TemporalExtent timeExtent = new TemporalExtent();
	    // timeExtent.setId(UUID.randomUUID().toString().substring(0, 6));
	    // timeExtent.setBeginPosition(startDate);
	    // if (endDate != null) {
	    // timeExtent.setEndPosition(endDate);
	    // }
	    // dataId.addTemporalExtent(timeExtent);
	    // }
	    //
	    // try {
	    // if (timeStamp != null) {
	    // Date iso8601 = ISO8601DateTimeUtils.parseISO8601ToDate(timeStamp).get();
	    // md_Metadata.setDateStampAsDateTime(XMLGregorianCalendarUtils.createGregorianCalendar(iso8601));
	    // }
	    // } catch (Exception e) {
	    // // TODO: handle exception
	    // }
	    //
	    // // KEYWORDS
	    // Keywords md_Keywords = new Keywords();
	    //
	    // md_Keywords.addKeyword("SAEON");
	    // // md_Keywords.addKeyword("HEALTH");
	    //
	    // for (String k : keywordList) {
	    // md_Keywords.addKeyword(k);
	    // }
	    //
	    // dataId.addKeywords(md_Keywords);
	    //
	    // if (!creatorsMap.isEmpty()) {
	    // // TODO: affiliation field should be more structured
	    // for (Map.Entry<String, String> entry : creatorsMap.entrySet()) {
	    // ResponsibleParty creatorsContact = new ResponsibleParty();
	    // String creatorName = entry.getKey();
	    // String affiliation = entry.getValue();
	    // creatorsContact.setRoleCode("originator");
	    // creatorsContact.setIndividualName(creatorName);
	    // dataId.addPointOfContact(creatorsContact);
	    // md_Metadata.addContact(creatorsContact);
	    // }
	    // }
	    //
	    // if (!contributorsMap.isEmpty()) {
	    // // TODO: affiliation field should be more structured
	    // for (Map.Entry<String, String> entry : contributorsMap.entrySet()) {
	    // ResponsibleParty contributorContact = new ResponsibleParty();
	    // String nameAndType = entry.getKey();
	    // String affiliation = entry.getValue();
	    // String[] splittedNameType = nameAndType.split(":");
	    // String contributorName = splittedNameType[0];
	    // String type = splittedNameType[1];
	    // contributorContact.setRoleCode(type);
	    // contributorContact.setIndividualName(contributorName);
	    // dataId.addPointOfContact(contributorContact);
	    // md_Metadata.addContact(contributorContact);
	    // }
	    // }
	    // //
	    // // if (attributes.has("addr_street")) {
	    // // addr = attributes.getString("addr_street");
	    // // }
	    // //
	    // // if (attributes.has("addr_city")) {
	    // // city = attributes.getString("addr_city");
	    // // }
	    //
	    // // if (attributes.has("provincia")) {
	    // // state = attributes.getString("provincia");
	    // // }
	    // //
	    // // if (attributes.has("addr_postcode")) {
	    // // postalCode = attributes.getString("addr_postcode");
	    // // }
	    // //
	    // // if (attributes.has("email")) {
	    // // email = attributes.getString("email");
	    // // }
	    // //
	    // // if (attributes.has("tel")) {
	    // // tel = attributes.getString("tel");
	    // // }
	    // //
	    // // if (attributes.has("fax")) {
	    // // fax = attributes.getString("fax");
	    // // }
	    //
	    // // contact.setRoleCode("pointOfContact");
	    // //
	    // // Contact contactInfo = new Contact();
	    // // Address address = new Address();
	    // //
	    // // if (city != null) {
	    // // address.setCity(city);
	    // // }
	    // // if (addr != null) {
	    // // address.addDeliveryPoint(addr);
	    // // }
	    // //
	    // // if (postalCode != null) {
	    // // address.setPostalCode(postalCode);
	    // // }
	    // //
	    // // if (state != null) {
	    // // address.setAdministrativeArea(state);
	    // // }
	    // //
	    // // if (email != null) {
	    // // address.addElectronicMailAddress(email);
	    // // }
	    // //
	    // // if (tel != null) {
	    // // contactInfo.addPhoneVoice(tel);
	    // // }
	    // //
	    // // if (fax != null) {
	    // // contactInfo.addPhoneFax(fax);
	    // // }
	    //
	    // // contactInfo.setAddress(address);
	    // // contact.setContactInfo(contactInfo);
	    // // dataId.addPointOfContact(contact);
	    //
	    // //
	    // // BBOX
	    // //
	    // if (eastBoundLongitude != null && westBoundLongitude != null && northBoundLatitude != null
	    // && southBoundLatitude != null) {
	    // dataId.addGeographicBoundingBox(northBoundLatitude, westBoundLongitude, southBoundLatitude,
	    // eastBoundLongitude);
	    // }
	    //
	    // // LEGAL CONSTRAINTS
	    // if (rights != null && rightsURI != null) {
	    // LegalConstraints rc = new LegalConstraints();
	    // rc.addUseLimitation(rights + " - " + rightsURI);
	    // dataId.addLegalConstraints(rc);
	    // }
	    // }
	    // }
	    //
	    // return dataset;

	} catch (

	Exception e) {
	    GSLoggerFactory.getLogger(getClass()).info("Error mapping" + e);

	}
    }


    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.TRIGGER;
    }

}
