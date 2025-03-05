package eu.essi_lab.accessor.hiscentral.puglia;

import java.math.BigDecimal;

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
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;
import net.opengis.iso19139.gmd.v_20060504.MDTopicCategoryCodeType;

/**
 * @author Roberto
 */
public class HISCentralPugliaMapper extends FileIdentifierMapper {

    private SimpleDateFormat iso8601Format;

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.HISCENTRAL_PUGLIA_NS_URI;
    }

    public HISCentralPugliaMapper() {
	// 2010-06-01T00:00:00
	// 2023-01-16T09:30:00+01:00
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
    static OriginalMetadata create(JSONObject datasetInfo, JSONObject stationInfo, JSONObject aggregationInfo) {

	OriginalMetadata originalMetadata = new OriginalMetadata();

	originalMetadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_PUGLIA_NS_URI);

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("dataset-info", datasetInfo);
	jsonObject.put("station-info", stationInfo);
	jsonObject.put("aggregation-info", aggregationInfo);

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
    private JSONObject retrieveStationInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getJSONObject("station-info");
    }

    /**
     * @param metadata
     * @return
     */
    private JSONObject retrieveAggregationInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getJSONObject("aggregation-info");
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

	JSONObject stationInfo = retrieveStationInfo(originalMD);

	JSONObject aggregationInfo = retrieveAggregationInfo(originalMD);

	String aggregationId = aggregationInfo.optString("id_aggregation");
	String aggregationName = aggregationInfo.optString("name");
	String aggregationAggregation = aggregationInfo.optString("aggregation");

	String resourceTitle = stationInfo.optString("station_name");
	String resourceAbstract = datasetInfo.optString("description"); // always null

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
	String stationId = stationInfo.optString("id_stz");

	// BBOX
	// "altitude": 60,
	// "long": "16.48389",
	// "lat": "41.23361"
	//
	BigDecimal pointLon = stationInfo.optBigDecimal("longitude", null);
	BigDecimal pointLat = stationInfo.optBigDecimal("latitude", null);
	Double altitude = stationInfo.optDouble("altitude");

	// temporal
	String tempExtenBegin = null;
	String tempExtenEnd = null;
	String temporalExtentInterval = datasetInfo.optString("date_range");
	if (temporalExtentInterval != null) {
	    temporalExtentInterval = temporalExtentInterval.replaceFirst(" ", "T").trim();
	    String[] splittedTime = temporalExtentInterval.split("\\|");

	    if (splittedTime.length > 0) {
		if (splittedTime.length > 1) {
		    tempExtenBegin = splittedTime[0].trim();
		    tempExtenEnd = splittedTime[1].trim();
		} else {
		    tempExtenBegin = splittedTime[0].trim();
		}

	    }
	}

	String city = datasetInfo.optString("city");
	String province = datasetInfo.optString("prov");

	String country = datasetInfo.optString("country");

	String alertZone = datasetInfo.optString("alertZone");

	//
	// MEASURE INFO
	// "measure": [
	// {
	// "id_measure": 21,
	// "measure_name": "Livello",
	// "measure_unit": "m"
	// },
	// {
	// "id_measure": 1021,
	// "measure_name": "Livello2",
	// "measure_unit": null
	// }
	// ]

	JSONArray measureInfo = datasetInfo.optJSONArray("measure");
	JSONObject measure = measureInfo.optJSONObject(0);
	String measureId = measure.optString("id_measure");
	String measureName = measure.optString("measure_name");
	String measureUnits = measure.optString("measure_unit");

	String varName = measureName;

	if (measureName.toLowerCase().contains("precipitazione")) {
	    varName = "Precipitazione";
	} else if (measureName.toLowerCase().contains("pioggia")) {
	    varName = "Pioggia";
	} else if (measureName.toLowerCase().contains("temperatura aria")) {
	    varName = "Temperatura aria";
	}

	String timeSeriesId = stationInfo.optString("time-series-id");

	// String tempExtenBegin = sensorInfo.optString("startDate");
	// String tempExtenEnd = sensorInfo.optString("endDate");

	// JSONObject organizationObject = organizationInfo.optJSONObject("organization");
	String legalConstraint = null;
	String creatorOrg = null;
	String legalLimitations = null;
	String pointOfContact = null;
	// if (organizationObject != null) {
	// legalConstraint = organizationObject.optString("conditions-for-access-and-use");
	// creatorOrg = organizationObject.optString("creator-organization");
	// legalLimitations = organizationObject.optString("limitations-on-public-access");
	// pointOfContact = organizationObject.optString("point-of-contact-organization");
	// }

	// JSONArray parametersArr = parameterInfo.optJSONArray("parameters");
	// String uriCode = null;
	// for (int i = 0; i < parametersArr.length(); i++) {
	// JSONObject jsonParameter = parametersArr.getJSONObject(i);
	// if (jsonParameter != null) {
	// String obsProp = jsonParameter.optString("observed-property");
	// if (obsProp.toLowerCase().equals(measureName.toLowerCase())) {
	// uriCode = jsonParameter.optString("keywords");
	// break;
	// }
	//
	// }
	// }

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

	coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(resourceTitle + " - " + aggregationName);
	coreMetadata.getMIMetadata().getDataIdentification().setAbstract(resourceTitle + " - " + aggregationName);

	//
	// id
	//

	String resourceIdentifier = generateCode(dataset, stationId + "-" + aggregationName);

	coreMetadata.getMIMetadata().setFileIdentifier(resourceIdentifier);

	//
	// topic category
	//
	// if (topicCataegory.toLowerCase().contains("meteo")) {
	// MDTopicCategoryCodeType topic = MDTopicCategoryCodeType.CLIMATOLOGY_METEOROLOGY_ATMOSPHERE;
	// coreMetadata.getMIMetadata().getDataIdentification().addTopicCategory(topic);
	//
	// }

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

	Keywords general = new Keywords();
	general.setTypeCode("general");
	general.addKeyword("Puglia");
	general.addKeyword(measureName);
	coreMetadata.getMIMetadata().getDataIdentification().addKeywords(general);

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
	if (tempExtenEnd == null || tempExtenEnd.isEmpty()) {
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

	// data linkage (last 24 hours)
	String linkage = HISCentralPugliaConnector.BASE_URL.endsWith("/")
		? HISCentralPugliaConnector.BASE_URL + "data/aggregation/" + aggregationId + "/station/" + stationId + "/measure/"
			+ measureId
		: HISCentralPugliaConnector.BASE_URL + "/data/aggregation/" + aggregationId + "/station/" + stationId + "/measure/"
			+ measureId;

	Online online = new Online();
	online.setLinkage(linkage);
	online.setFunctionCode("download");
	online.setName(stationId + "_" + measureId + "_" + aggregationId);
	online.setIdentifier(resourceIdentifier);
	online.setProtocol(CommonNameSpaceContext.HISCENTRAL_PUGLIA_NS_URI);

	distribution.addDistributionOnline(online);

	//
	// coverage description
	//

	CoverageDescription coverageDescription = new CoverageDescription();

	String[] splitAggregation = aggregationAggregation.split("_"); // p_sum_1y
	String aggParameter = null;
	String aggProcedure = null;
	String aggPeriod = null;
	if (splitAggregation.length == 3) {
	    aggParameter = splitAggregation[0];// p
	    aggProcedure = splitAggregation[1]; // sum
	    aggPeriod = splitAggregation[2]; // 1y
	} else if (splitAggregation.length == 2) {
	    aggParameter = splitAggregation[0];// p
	    aggProcedure = splitAggregation[1]; //
	    aggPeriod = splitAggregation[1]; // rt
	} else {
	    System.out.println();
	}

	String aggPeriodNumbers = aggPeriod.replaceAll("\\D+", ""); // 1
	String aggPeriodString = aggPeriod.replaceAll("\\d+", ""); // y

	String duration = null;
	switch (aggPeriodString) {
	case "d":
	    duration = "P" + aggPeriodNumbers + "D";
	    break;
	case "m":
	    duration = "P" + aggPeriodNumbers + "M";
	    break;
	case "y":
	    duration = "P" + aggPeriodNumbers + "Y";
	    break;
	case "min":
	    duration = "PT" + aggPeriodNumbers + "M";
	    break;
	case "rt":
	default:
	    break;
	}
	if (duration != null) {
	    dataset.getExtensionHandler().setTimeAggregationDuration8601(duration);
	    dataset.getExtensionHandler().setTimeResolutionDuration8601(duration);
	}
	switch (aggProcedure) {
	case "max":
	    dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.MAX);
	    break;
	case "min":
	    dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.MIN);
	    break;
	case "avg":
	    dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.AVERAGE);
	    break;
	case "sum":
	    dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.TOTAL);
	    break;
	case "real-time":
	    dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.CONTINUOUS);
	    break;
	default:
	    dataset.getExtensionHandler().setTimeInterpolation(aggProcedure);
	    break;
	}

	coverageDescription.setAttributeIdentifier(aggregationAggregation);
	coverageDescription.setAttributeTitle(measureName);

	coverageDescription.setAttributeDescription(aggregationName);

	String missingValue = "-9999";
	dataset.getExtensionHandler().setAttributeMissingValue(missingValue);

	if (measureUnits != null) {
	    dataset.getExtensionHandler().setAttributeUnits(measureUnits);
	}

	// if (uriCode != null && !uriCode.isEmpty()) {
	// dataset.getExtensionHandler().setObservedPropertyURI(uriCode);
	// }

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

    public static void main(String[] args) {
	String s = "2009-04-16 00:00:00 | ";
	s = s.replaceFirst(" ", "T").trim();
	String[] splittedTime = s.split("\\|");
	System.out.println(s);

    }
}
