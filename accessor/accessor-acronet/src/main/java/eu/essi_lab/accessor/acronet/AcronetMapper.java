package eu.essi_lab.accessor.acronet;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

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
public class AcronetMapper extends OriginalIdentifierMapper {

    private static final String ACRONET = "ACRONET";

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public AcronetMapper() {
	// do nothing
    }

    public static OriginalMetadata create(JSONObject datasetInfo, String variableName) {
	OriginalMetadata originalMetadata = new OriginalMetadata();

	originalMetadata.setSchemeURI(CommonNameSpaceContext.ACRONET);

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("dataset-info", datasetInfo);
	jsonObject.put("var-name", variableName);

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
    private String retrieveVariableName(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).optString("var-name");
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
	return CommonNameSpaceContext.ACRONET;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {

	/**
	 * DATASET INFO
	 */
	// {
	// "id": "210330116_2",
	// "name": "Ex Asilo Mazza",
	// "lat": 44.19195,
	// "lng": 8.128267,
	// "mu": "°C"
	// },

	/**
	 * VARIABLE INFO: name of the variable.
	 * e.g. TEMPERATURA
	 */

	dataset.getPropertyHandler().setIsTimeseries(true);

	JSONObject datasetInfo = retrieveDatasetInfo(originalMD);

	String variableName = null; 
	String varName = retrieveVariableName(originalMD);
	AcronetVariable variable = AcronetVariable.decode(varName);
	if(variable != null) {
	    variableName = variable.getLabel();
	}

	String stationName = datasetInfo.optString("name");
	String stationId = datasetInfo.optString("id");
	
	String noSpaceName = stationName.replaceAll(" ", ":");

	String startDate = "2021-11-01T00:00:00Z";
	String endDate = datasetInfo.optString("last_reading_at");


	String variableId = null;
	String variableUUID = null;

	String variableDescription = variableName;
	String variableUnit = datasetInfo.optString("mu");
	if(variableUnit.contains("°C")) {
	    variableUnit = "K";
	}
	String sensorId = null; // sensorInfo.optString("id");
	// JSONObject measurementObj = sensorInfo.optJSONObject("measurement");
	// if (measurementObj != null) {
	// variableUUID = measurementObj.optString("uuid");
	// variableId = measurementObj.optString("id");
	// variableName = measurementObj.optString("name");
	// variableDescription = sensorInfo.optString("description");
	// }

	// SmartCitizenKitVariable variable = SmartCitizenKitVariable.decode(key);
	Double pointLon = datasetInfo.optDoubleObject("lng");
	Double pointLat = datasetInfo.optDoubleObject("lat");
	Double altitude = null;
	

	Resolution resolution = Resolution.HOURLY;

	// TEMPORAL EXTENT
	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	TemporalExtent extent = new TemporalExtent();

	if (startDate != null && !startDate.isEmpty()) {

	    extent.setBeginPosition(startDate);

	    if (endDate != null && !endDate.isEmpty()) {

		extent.setEndPosition(endDate);
	    } else {
		extent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
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

	coreMetadata.setTitle("Acquisitions of " + variableName + " through ACRONET device: " + stationId);
	coreMetadata.setAbstract(
		"This dataset contains " + variableDescription + " timeseries from I-CHANGE ACRONET: " + stationName);

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(ACRONET);

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

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(variableName);
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword(variable.getKey());
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword(variable.toString().toLowerCase());

	if (resolution.equals(Resolution.HOURLY))
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("hourly");

	ExtensionHandler handler = dataset.getExtensionHandler();
	handler.setTimeUnits("h");
	handler.setTimeResolution("1");
	handler.setAttributeMissingValue("-9999");
	handler.setAttributeUnitsAbbreviation(variableUnit);

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

	// Double[] bbox = multipointToBbox(coordinates);

	// bounding box (Multipoint)

	// bounding box
	if (pointLon != null && pointLat != null) {

	    coreMetadata.addBoundingBox(pointLat, pointLon, pointLat, pointLon);

	}
	// elevation

	if (altitude != null) {
	    VerticalExtent verticalExtent = new VerticalExtent();
	    verticalExtent.setMinimumValue(altitude);
	    verticalExtent.setMaximumValue(altitude);
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
//	if (authorsObj != null) {
//
//	    String authorName = authorsObj.optString("username");
//
//	    if (authorName != null) {
//		ResponsibleParty creatorContact = new ResponsibleParty();
//		creatorContact.setOrganisationName(authorName);
//		creatorContact.setRoleCode("originator");
//		coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);
//	    }
//	}



	/**
	 * MIPLATFORM
	 **/

	MIPlatform platform = new MIPlatform();

	
	
	String platformIdentifier = "i-change-acronet:" + noSpaceName;

	platform.setMDIdentifierCode(platformIdentifier);

	String siteDescription = noSpaceName;

	platform.setDescription(siteDescription);

	Citation platformCitation = new Citation();
	platformCitation.setTitle(noSpaceName);
	platform.setCitation(platformCitation);

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	/**
	 * COVERAGEDescription
	 **/

	CoverageDescription coverageDescription = new CoverageDescription();
	String varId = noSpaceName + ":" + varName;

	coverageDescription.setAttributeIdentifier(varId);
	coverageDescription.setAttributeTitle(variableName);

	String attributeDescription = variableDescription + " Units: " + variableUnit;

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

	String resourceIdentifier = generateCode(dataset, variableName + ":" + noSpaceName);

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

	    String linkage = AcronetConnector.BASE_URL;// +
	    // station.getName()
	    // +
	    // buildingURL;

	    Online o = new Online();
	    o.setLinkage(linkage);
	    o.setFunctionCode("download");
	    o.setName(stationId + ":" + varName + ":" + variableUnit);
	    o.setIdentifier(noSpaceName + ":" + variableDescription);
	    o.setProtocol(CommonNameSpaceContext.ACRONET);
	    o.setDescription(variableDescription + " Station name: " + stationName);
	    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(o);

	} catch (Exception e) {
	    // TODO: handle exception
	}

	coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);

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
