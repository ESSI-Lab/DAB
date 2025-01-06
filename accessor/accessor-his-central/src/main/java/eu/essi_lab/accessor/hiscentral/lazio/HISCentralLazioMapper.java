package eu.essi_lab.accessor.hiscentral.lazio;

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

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import eu.essi_lab.accessor.hiscentral.utils.HISCentralUtils;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * @author Roberto
 */
public class HISCentralLazioMapper extends FileIdentifierMapper {

    private SimpleDateFormat iso8601Format;
    
    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.HISCENTRAL_LAZIO_NS_URI;
    }

    public HISCentralLazioMapper() {
	//2010-06-01T00:00:00
	//2023-01-16T09:30:00+01:00
	this.iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	iso8601Format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    
    public static void setIndeterminatePosition(GSResource gsResource) {
	setIndeterminatePosition(gsResource, TimeUnit.DAYS.toMillis(30));
    }

    /**
     * @param datasetInfo
     * @param sensorInfo
     * @return
     */
    static OriginalMetadata create(JSONObject datasetInfo, JSONObject sensorInfo) {

	OriginalMetadata originalMetadata = new OriginalMetadata();

	originalMetadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_LAZIO_NS_URI);

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
     * @param metadata
     * @return
     */
    private JSONObject retrieveSensorInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getJSONObject("sensor-info");
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {

	JSONObject datasetInfo = retrieveDatasetInfo(originalMD);

	JSONObject sensorInfo = retrieveSensorInfo(originalMD);

	//
	// DATASET INFO
//	stationName": "Liri a Isola Liri",
//        "stationId": 247100,
//        "customStationId": null,
//        "basin": null,
//        "river": null,
//        "alertZone": "Lazi-G",
//        "time": "2022-12-20T12:15:00+01:00",
//        "stateId": 0,
//        "registerName": null,
//        "address": "",
//        "locality": "Ponte \"alfredo Barbati\"",
//        "city": "Isola del Liri",
//        "province": "Frosinone",
//        "prov": "FR",
//        "region": "Lazio",
//        "country": "Italia",
//        "longitude": 13.564722,
//        "latitude": 41.676944,
//        "altitude": 220,
//        "gmt": 60



	String stationName = datasetInfo.optString("stationName");
	String stationId = datasetInfo.optString("stationId");
	
	

	Double pointLon = datasetInfo.optDouble("longitude");
	Double pointLat = datasetInfo.optDouble("latitude");
	Double altitude = datasetInfo.optDouble("altitude");
	
	String locality = datasetInfo.optString("locality");
	String city = datasetInfo.optString("city");
	String province = datasetInfo.optString("prov");
	String region = datasetInfo.optString("region");
	String country = datasetInfo.optString("country");

	String alertZone = datasetInfo.optString("alertZone");
	

	//
	// MEASURE INFO
//	"elementName": "Flow Rate",
//	    "elementId": 42705,
//	    "customElementId": null,
//	    "stationName": "Liri a Isola Liri",
//	    "stationId": 247100,
//	    "customStationId": null,
//	    "basin": null,
//	    "river": null,
//	    "startDate": "2010-06-01T00:00:00",
//	    "endDate": null,
//	    "decimals": 2,
//	    "measUnit": "m3/s",
//	    "time": "2023-01-16T09:30:00+01:00",
//	    "value": 18.18,
//	    "trend": -0.88,
//	    "stateId": 0,
//	    "quantityOrgId": 1,
//	    "quantityId": 12,
//	    "stateDescr": "",
//	    "dtr": 900,
//	    "isVirtual": true


	String timeSeriesId = sensorInfo.optString("elementId");
	String measureName = sensorInfo.optString("elementName");
	String tempExtenBegin = sensorInfo.optString("startDate");
	String tempExtenEnd = sensorInfo.optString("endDate");

	String measureUnits = sensorInfo.optString("measUnit");


	//
	// String stationName = sensorInfo.getString("name");
	//
	// String statisticalFunction = "";
	// if (sensorInfo.getJSONObject("observedProperty").has("statisticalFunction")) {
	//
	// statisticalFunction = sensorInfo.getJSONObject("observedProperty").getString("statisticalFunction");
	//
	// if (statisticalFunction.equals("sum")) {
	// dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.TOTAL);
	// } else {
	// dataset.getExtensionHandler().setTimeInterpolation(statisticalFunction);
	// }
	// }

	// String uom = sensorInfo.getJSONObject("observedProperty").getString("uom");
	// String basePhenomenon = sensorInfo.getJSONObject("observedProperty").getString("basePhenomenon");

	// String intendedObservationSpacing = sensorInfo.getString("intendedObservationSpacing");
	//
	// String aggregationTimePeriod = sensorInfo.get("aggregationTimePeriod").toString();

	//
	// intendedObservationSpacing = "P15M"
	//
	// 15 -> timeResolution
	// M -> timeUnits
	//
	// String timeUnits = intendedObservationSpacing.substring(1, intendedObservationSpacing.length() - 1);
	// String timeResolution = intendedObservationSpacing.substring(intendedObservationSpacing.length() - 1);
	//
	// dataset.getExtensionHandler().setTimeUnits(timeUnits);
	// dataset.getExtensionHandler().setTimeResolution(timeResolution);
	//
	// if (aggregationTimePeriod != null && aggregationTimePeriod.equals("0")) {
	// dataset.getExtensionHandler().setTimeSupport(timeUnits);
	// }

	//
	//
	//

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	coreMetadata.getMIMetadata().setLanguage("Italian");
	coreMetadata.getMIMetadata().setCharacterSetCode("utf8");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	coreMetadata.addDistributionFormat("WaterML 1.1");

	coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(stationName + " (" + locality + ")" + " - " + measureName);
	coreMetadata.getMIMetadata().getDataIdentification().setAbstract(stationName + " (" + locality + ")" + " - " + measureName);

	//
	// id
	//
	
	String resourceIdentifier = generateCode(dataset, stationId+"-"+measureName);
	
	coreMetadata.getMIMetadata().setFileIdentifier(resourceIdentifier);

	//
	// responsible party
	//
	ResponsibleParty publisherContact = new ResponsibleParty();

	publisherContact.setOrganisationName("CAE - Lazio");
	publisherContact.setRoleCode("publisher");

	// Contact contact = new Contact();
	//
	// Address address = new Address();
	// address.addElectronicMailAddress(contactInfo);
	// contact.setAddress(address);
	//
	// publisherContact.setContactInfo(contact);

	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);

	//
	// keywords
	//
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(locality);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(measureName);
	//coreMetadata.getMIMetadata().getDataIdentification().addKeyword(measureDescription);

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("LAZIO");

	Keywords kwd = new Keywords();
	kwd.setTypeCode("platform");
	kwd.addKeyword(stationName);
	coreMetadata.getMIMetadata().getDataIdentification().addKeywords(kwd);

	//
	// bbox
	//
	ReferenceSystem referenceSystem = new ReferenceSystem();
	referenceSystem.setCode("EPSG:4326");
	referenceSystem.setCodeSpace("EPSG");
	coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);
	
	coreMetadata.addBoundingBox(//
		pointLat, //
		pointLon, //
		pointLat, //
		pointLon);

	// vertical extent
	coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(altitude, altitude);

	

	coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);
	
	//
	// platform
	//

	MIPlatform platform = new MIPlatform();

	platform.setMDIdentifierCode(stationId);

	dataset.getExtensionHandler().setCountry("ITA");

	platform.setDescription(stationName);

	Citation platformCitation = new Citation();
	platformCitation.setTitle(stationName);
	platform.setCitation(platformCitation);

	coreMetadata.getMIMetadata().addMIPlatform(platform);

	//
	// temp extent
	//

	String beginPosition = normalizeUTCPosition(tempExtenBegin);

	TemporalExtent temporalExtent = new TemporalExtent();
	temporalExtent.setBeginPosition(beginPosition);
	// end time to be reviewed
	if (tempExtenEnd != null && tempExtenEnd.isEmpty()) {
	    temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
	} else {
	    temporalExtent.setEndPosition(normalizeUTCPosition(tempExtenEnd));
	}

	coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);
	
	setIndeterminatePosition(dataset);

	Distribution distribution = coreMetadata.getMIMetadata().getDistribution();

	//
	// distribution info, information
	//

	// Online online = new Online();
	// online.setLinkage(resourceLocator);
	// online.setFunctionCode("information");
	// online.setName("Rete Meteo-Idro-Pluviometrica");
	//
	// distribution.addDistributionOnline(online);

	//
	// distribution info, download
	//

	if (tempExtenBegin.contains("+")) {

	    tempExtenBegin = tempExtenBegin.substring(0, tempExtenBegin.indexOf("+"));
	}

	String linkage = HISCentralLazioConnector.BASE_URL + "/data/" + timeSeriesId + "?from=" + tempExtenBegin + "&type=Plausible&part=IsoTime&part=Value&part=Quality&part=QualityDescr&timing=Original&elab=None";

	Online online = new Online();
	online.setLinkage(linkage);
	online.setFunctionCode("download");
	online.setName(stationName + "_" + timeSeriesId);
	online.setProtocol(CommonNameSpaceContext.HISCENTRAL_LAZIO_NS_URI);

	distribution.addDistributionOnline(online);
	
	distribution.getDistributionOnline().setIdentifier(resourceIdentifier);


	//
	// coverage description
	//

	CoverageDescription coverageDescription = new CoverageDescription();

	coverageDescription.setAttributeIdentifier(measureName);
	switch (measureName) {
	case "Portata A":
	case "Portata B":
	    measureName = "Portata";
	    break;
	default:
	    break;
	}
	coverageDescription.setAttributeTitle(measureName);

	String missingValue = "-9999";
	dataset.getExtensionHandler().setAttributeMissingValue(missingValue);

	 if (measureUnits != null) {
	 dataset.getExtensionHandler().setAttributeUnits(measureUnits);
	 }
	
	 String units = measureUnits != null ? " Units: " + measureUnits : "";

	//String attributeDescription = nameMeasure + delta_t;

	// as no description is given this field is calculated
	HISCentralUtils.addDefaultAttributeDescription(dataset, coverageDescription);
	 
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
	
	
    }

    /**
     * @param timePosition
     * @return
     */
    private String normalizeUTCPosition(String timePosition) {

	if (timePosition.endsWith("/")) {
	    timePosition = timePosition.substring(0, timePosition.length() - 1);
	}

	return timePosition;
    }
}
