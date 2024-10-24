package eu.essi_lab.accessor.hiscentral.valdaosta;

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

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.hiscentral.utils.HISCentralUtils;
import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Distribution;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
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
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;

/**
 * @author Roberto
 */
public class HISCentralValdaostaMapper extends FileIdentifierMapper {

    private SimpleDateFormat iso8601Format;

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.HISCENTRAL_VALDAOSTA_NS_URI;
    }

    public HISCentralValdaostaMapper() {
	// 2010-06-01T00:00:00
	// 2023-01-16T09:30:00+01:00
	this.iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
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
    static OriginalMetadata create(JSONObject datasetInfo, JSONObject sensorInfo, JSONObject organizationInfo, JSONObject parameterInfo) {

	OriginalMetadata originalMetadata = new OriginalMetadata();

	originalMetadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_VALDAOSTA_NS_URI);

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("dataset-info", datasetInfo);
	jsonObject.put("sensor-info", sensorInfo);
	jsonObject.put("organization-info", organizationInfo);
	jsonObject.put("parameter-info", parameterInfo);

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

    /**
     * @param metadata
     * @return
     */
    private JSONObject retrieveOrganizationInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getJSONObject("organization-info");
    }

    /**
     * @param metadata
     * @return
     */
    private JSONObject retrieveParameterInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getJSONObject("parameter-info");
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

	JSONObject organizationInfo = retrieveOrganizationInfo(originalMD);

	JSONObject parameterInfo = retrieveParameterInfo(originalMD);

	//
	// DATASET INFO
	// "abstract": null,
	// "site-information": "Chamençon,Dora di Valgrisenche,A",
	// "spatial-extent": "45.6783,7.10742,1238",
	// "station-id": 1000,
	// "station-name": "Arvier - Chamençon",
	// "temporal-extent": "2001-09-21T13:00:00Z/2023-03-02T11:28:21Z",
	// "territory-of-origin-of-data": "Valle d'Aosta,Aosta,Arvier",
	// "topic-category": "Meteo"

	String resourceTitle = datasetInfo.optString("station-name");
	String resourceAbstract = datasetInfo.optString("abstract"); // always null

	// locality repeat the name of station
	String locality = datasetInfo.optString("site-information");
	String[] splittedLocality = locality.split(",");
	String additionalInfo = "";
	if (splittedLocality.length > 1) {
	    for (int i = 1; i < splittedLocality.length; i++) {
		additionalInfo = additionalInfo + splittedLocality[i] + ", ";
	    }
	    additionalInfo = additionalInfo.substring(0, additionalInfo.length() - 2);
	}
	String stationId = datasetInfo.optString("station-id");
	String spatialExtent = datasetInfo.optString("spatial-extent");

	//
	// spatailextent: seems to be lat, lon, alt
	// e.g.45.6783,7.10742,1238
	Double pointLon = null;
	Double pointLat = null;
	Double altitude = null;

	String[] splittedSpatial = spatialExtent.split(",");
	if (splittedSpatial.length > 0) {
	    if (splittedSpatial.length > 2) {
		pointLat = Double.parseDouble(splittedSpatial[0]);
		pointLon = Double.parseDouble(splittedSpatial[1]);
		altitude = Double.parseDouble(splittedSpatial[2]);
	    } else {
		pointLat = Double.parseDouble(splittedSpatial[0]);
		pointLon = Double.parseDouble(splittedSpatial[1]);
	    }
	}
	String region = datasetInfo.optString("territory-of-origin-of-data");

	// topic category
	String topicCataegory = datasetInfo.optString("topic-category");

	// temporal
	String temporalExtentInterval = datasetInfo.optString("temporal-extent");

	String[] splittedTime = temporalExtentInterval.split("\\/");
	String tempExtenBegin = null;
	String tempExtenEnd = null;

	if (splittedTime.length > 0) {
	    if (splittedTime.length > 1) {
		tempExtenBegin = splittedTime[0];
		tempExtenEnd = splittedTime[1];
	    } else {
		tempExtenBegin = splittedTime[0];
	    }

	}

	String city = datasetInfo.optString("city");
	String province = datasetInfo.optString("prov");

	String country = datasetInfo.optString("country");

	String alertZone = datasetInfo.optString("alertZone");

	//
	// MEASURE INFO
	// {
	// "observed-property": "Portata",
	// "temporal-extent": "2023-03-02T14:14:29Z",
	// "time-series-id": 672,
	// "units-of-measure": "m³/sec"
	// }

	String timeSeriesId = sensorInfo.optString("time-series-id");
	String measureName = sensorInfo.optString("observed-property");
	// String tempExtenBegin = sensorInfo.optString("startDate");
	// String tempExtenEnd = sensorInfo.optString("endDate");

	String measureUnits = sensorInfo.optString("units-of-measure");

	//
	// ORGANIZATION INFO
	// {
	// "organization": {
	// "conditions-for-access-and-use": "URI: https://creativecommons.org/licenses/by/4.0/legalcode",
	// "creator-organization": "Centro Funzionale regionale - Centre fonctionnel régional. Email:
	// u-idrografico@regione.vda.it",
	// "limitations-on-public-access": "No limitations to public access. URI:
	// https://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/noLimitations",
	// "point-of-contact-organization": "Regione autonoma Valle d'Aosta - Région autonome Vallée d'Aoste. Presidenza
	// della Regione - Présidence de la Région. Dipartimento protezione civile e vigili del fuoco - Département de
	// la protection civile et des sapeurs-pompiers. Centro Funzionale regionale - Centre fonctionnel régional. Via
	// Promis, 2/a - 11100 Aosta (AO). Tel./Tél. +39 0165 272283-2749 - Fax/Télécopie +39 0165 272291"
	// }

	JSONObject organizationObject = organizationInfo.optJSONObject("organization");
	String legalConstraint = null;
	String creatorOrg = null;
	String legalLimitations = null;
	String pointOfContact = null;
	if (organizationObject != null) {
	    legalConstraint = organizationObject.optString("conditions-for-access-and-use");
	    creatorOrg = organizationObject.optString("creator-organization");
	    legalLimitations = organizationObject.optString("limitations-on-public-access");
	    pointOfContact = organizationObject.optString("point-of-contact-organization");
	}

	// PARAMETER INFO
	// {
	// "parameters": [
	// {
	// "keywords": "http://codes.wmo.int/wmdr/ObservedVariableAtmosphere/224",
	// "numerical-resolution": 1,
	// "observed-property": "Temperatura",
	// "parameter-id": 1,
	// "units-of-measure": "°C"
	// },
	// {
	// "keywords": null,
	// "numerical-resolution": 1,
	// "observed-property": "Portata",
	// "parameter-id": 30,
	// "units-of-measure": "m³/sec"
	// }
	// ]
	// }

	JSONArray parametersArr = parameterInfo.optJSONArray("parameters");
	String uriCode = null;
	for (int i = 0; i < parametersArr.length(); i++) {
	    JSONObject jsonParameter = parametersArr.getJSONObject(i);
	    if (jsonParameter != null) {
		String obsProp = jsonParameter.optString("observed-property");
		if (obsProp.toLowerCase().equals(measureName.toLowerCase())) {
		    uriCode = jsonParameter.optString("keywords");
		    break;
		}

	    }
	}

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

	coreMetadata.getMIMetadata().getDataIdentification()
		.setCitationTitle(resourceTitle + " (" + additionalInfo + ")" + " - " + measureName);
	coreMetadata.getMIMetadata().getDataIdentification().setAbstract(resourceTitle + " (" + additionalInfo + ")" + " - " + measureName);

	//
	// id
	//

	String resourceIdentifier = generateCode(dataset, stationId + "-" + measureName);

	coreMetadata.getMIMetadata().setFileIdentifier(resourceIdentifier);

	//
	// topic category
	//
	if (topicCataegory.toLowerCase().contains("meteo")) {
	    MDTopicCategoryCodeType topic = MDTopicCategoryCodeType.CLIMATOLOGY_METEOROLOGY_ATMOSPHERE;
	    coreMetadata.getMIMetadata().getDataIdentification().addTopicCategory(topic);

	}

	//
	// legal constraints
	//
	//
	if (legalConstraint != null && !legalConstraint.isEmpty()) {

	    LegalConstraints access = new LegalConstraints();
	    access.addAccessConstraintsCode("other");

	    String legalURL = null;
	    if (legalConstraint.toLowerCase().contains("uri:")) {
		String[] splittedLegal = legalConstraint.toLowerCase().split("uri:");
		legalURL = splittedLegal[1].trim();
	    }
	    if (legalURL != null) {
		access.getElementType().getOtherConstraints().add(ISOMetadata.createAnchorPropertyType(legalURL));
	    } else {
		access.addUseLimitation(legalLimitations);
	    }

	    coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(access);
	}
	if (legalLimitations != null && !legalLimitations.isEmpty()) {

	    LegalConstraints rc = new LegalConstraints();
	    rc.addUseConstraintsCode("other");

	    String limitationsURL = null;
	    if (legalLimitations.toLowerCase().contains("uri:")) {
		String[] splittedLegal = legalLimitations.toLowerCase().split("uri:");
		limitationsURL = splittedLegal[1].trim();
	    }
	    if (limitationsURL != null) {

		rc.getElementType().getOtherConstraints().add(ISOMetadata.createAnchorPropertyType(limitationsURL));
	    } else {
		rc.addUseLimitation(limitationsURL);
	    }

	    coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(rc);
	}

	//
	// responsible party
	//
	if (creatorOrg != null && !creatorOrg.isEmpty()) {

	    ResponsibleParty publisherContact = new ResponsibleParty();

	    if (creatorOrg.toLowerCase().contains("email:")) {
		String[] splittedOrg = creatorOrg.toLowerCase().split("email:");
		String emailOrg = splittedOrg[1].trim();
		String nameOrg = splittedOrg[0];
		publisherContact.setOrganisationName(nameOrg);
		Contact contact = new Contact();

		Address address = new Address();
		address.addElectronicMailAddress(emailOrg);
		contact.setAddress(address);

		publisherContact.setContactInfo(contact);

	    } else {
		publisherContact.setOrganisationName(creatorOrg);
	    }
	    publisherContact.setRoleCode("publisher");

	    // Contact contact = new Contact();
	    //
	    // Address address = new Address();
	    // address.addElectronicMailAddress(contactInfo);
	    // contact.setAddress(address);
	    //
	    // publisherContact.setContactInfo(contact);

	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);
	}

	if (pointOfContact != null && !pointOfContact.isEmpty()) {
	    ResponsibleParty pointOfContactResp = new ResponsibleParty();
	    pointOfContactResp.setOrganisationName(pointOfContact);
	    pointOfContactResp.setRoleCode("pointOfContact");
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(pointOfContactResp);
	}
	//
	// keywords
	//
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(region);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(measureName);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(topicCataegory);
	// coreMetadata.getMIMetadata().getDataIdentification().addKeyword(measureDescription);

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("VALLE D'AOSTA");

	Keywords kwd = new Keywords();
	kwd.setTypeCode("platform");
	kwd.addKeyword(resourceTitle);
	coreMetadata.getMIMetadata().getDataIdentification().addKeywords(kwd);

	//
	// bbox
	//
	ReferenceSystem referenceSystem = new ReferenceSystem();
	referenceSystem.setCode("EPSG:4326");
	referenceSystem.setCodeSpace("EPSG");
	coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);
	
	if (pointLat != null && pointLon != null) {
	    coreMetadata.addBoundingBox(//
		    pointLat, //
		    pointLon, //
		    pointLat, //
		    pointLon);
	}

	// vertical extent
	if (altitude != null)
	    coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(altitude, altitude);

	//
	// platform
	//

	MIPlatform platform = new MIPlatform();

	platform.setMDIdentifierCode(stationId);

	dataset.getExtensionHandler().setCountry("ITA");

	platform.setDescription(resourceTitle);

	Citation platformCitation = new Citation();
	platformCitation.setTitle(resourceTitle);
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
	// data linkage (last 24 hours)
	String linkage = HISCentralValdaostaConnector.BASE_URL.endsWith("/")
		? HISCentralValdaostaConnector.BASE_URL + "data_time_series/" + timeSeriesId + "/24"
		: HISCentralValdaostaConnector.BASE_URL + "/data_time_series/" + timeSeriesId + "/24";

	Online online = new Online();
	online.setLinkage(linkage);
	online.setFunctionCode("download");
	online.setName(resourceTitle + "_" + timeSeriesId);
	online.setIdentifier(resourceIdentifier);
	online.setProtocol(CommonNameSpaceContext.HISCENTRAL_VALDAOSTA_NS_URI);

	distribution.addDistributionOnline(online);

	//
	// coverage description
	//

	CoverageDescription coverageDescription = new CoverageDescription();

	coverageDescription.setAttributeIdentifier(measureName);
	coverageDescription.setAttributeTitle(measureName);

	String missingValue = "-9999";
	dataset.getExtensionHandler().setAttributeMissingValue(missingValue);

	if (measureUnits != null) {
	    dataset.getExtensionHandler().setAttributeUnits(measureUnits);
	}

	if (uriCode != null && !uriCode.isEmpty()) {
	    dataset.getExtensionHandler().setObservedPropertyURI(uriCode);
	}

	// as no description is given this field is calculated
	HISCentralUtils.addDefaultAttributeDescription(dataset, coverageDescription);
	
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

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
