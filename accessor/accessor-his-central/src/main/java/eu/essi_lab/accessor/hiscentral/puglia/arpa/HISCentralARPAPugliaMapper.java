package eu.essi_lab.accessor.hiscentral.puglia.arpa;

import java.math.BigDecimal;

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

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.accessor.hiscentral.utils.HISCentralUtils;
import eu.essi_lab.downloader.hiscentral.HISCentralPugliaDownloader;
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
import eu.essi_lab.lib.utils.GSLoggerFactory;
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
public class HISCentralARPAPugliaMapper extends FileIdentifierMapper {

    private SimpleDateFormat iso8601Format;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.HISCENTRAL_ARPA_PUGLIA_NS_URI;
    }

    public HISCentralARPAPugliaMapper() {
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
    static OriginalMetadata create(JSONObject datasetInfo, JSONObject variableInfo, String originator, String contactPoint) {

	OriginalMetadata originalMetadata = new OriginalMetadata();

	originalMetadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_ARPA_PUGLIA_NS_URI);

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("dataset-info", datasetInfo);
	jsonObject.put("variable-info", variableInfo);
	jsonObject.put("originator", originator);
	jsonObject.put("contact-point", contactPoint);
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
    private JSONObject retrieveVariableInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getJSONObject("variable-info");
    }

    /**
     * @param metadata
     * @return
     */
    private String retrieveOriginator(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).optString("originator");
    }

    /**
     * @param metadata
     * @return
     */
    private String retrieveContactPoint(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).optString("contact-point");
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

	JSONObject stationInfo = retrieveVariableInfo(originalMD);

	String originator = retrieveOriginator(originalMD);
	String contactPoint = retrieveContactPoint(originalMD);

	String measureId = stationInfo.optString("pollutantId");
	String measureName = stationInfo.optString("pollutantName");
	String pollutantDescription = stationInfo.optString("pollutantDescription");
	String measureInterpolation = stationInfo.optString("pollutantInterpolation");
	String pollutantLimitValue = stationInfo.optString("pollutantLimitValue");
	String pollutantErrorValue = stationInfo.optString("pollutantErrorValue");
	String measureUnits = stationInfo.optString("pollutantUnits");
	String pollutantUri = stationInfo.optString("pollutantUri");

	// BBOX
	// "geometry": {
	// "type": "Point",
	// "coordinates": [
	// 16.787777,
	// 41.114445
	// ]
	// }
	BigDecimal pointLon = null;
	BigDecimal pointLat = null;
	JSONObject geometryObj = datasetInfo.optJSONObject("geometry");
	if (geometryObj != null) {
	    JSONArray coordinates = geometryObj.optJSONArray("coordinates");
	    if (coordinates.length() == 2) {
		pointLon = coordinates.optBigDecimal(0, null);
		pointLat = coordinates.optBigDecimal(1, null);
	    } else {
		logger.error("NO VALID COORDINATES");
	    }
	}

	// properties
	JSONObject propObj = datasetInfo.optJSONObject("properties");
	String stationId = propObj.optString("id_station");

	String city = propObj.optString("comune");
	String province = propObj.optString("provincia");
	String streetAddress = propObj.optString("indirizzo");
	String country = propObj.optString("paese_esteso");
	String resourceTitle = propObj.optString("denominazione");
	String resourceAbstract = propObj.optString("description"); // always null

	String varName = measureName;

	if (measureName.toLowerCase().contains("precipitazione")) {
	    varName = "Precipitazione";
	} else if (measureName.toLowerCase().contains("pioggia")) {
	    varName = "Pioggia";
	} else if (measureName.toLowerCase().contains("temperatura aria")) {
	    varName = "Temperatura aria";
	}

	// JSONObject organizationObject = organizationInfo.optJSONObject("organization");
	String legalConstraint = null;
	String legalLimitations = null;
	String pointOfContact = null;

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	coreMetadata.getMIMetadata().setLanguage("Italian");
	coreMetadata.getMIMetadata().setCharacterSetCode("utf8");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	coreMetadata.addDistributionFormat("WaterML 1.1");

	coreMetadata.getMIMetadata().getDataIdentification()
		.setCitationTitle(resourceTitle + " - " + measureName + " - " + measureInterpolation);
	coreMetadata.getMIMetadata().getDataIdentification()
		.setAbstract(resourceTitle + " - " + measureName + " - " + measureInterpolation);

	//
	// id
	//

	String resourceIdentifier = generateCode(dataset, stationId + "-" + measureId);

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
	if (originator != null && !originator.isEmpty()) {

	    ResponsibleParty publisherContact = new ResponsibleParty();
	    publisherContact.setOrganisationName(originator);
	    publisherContact.setRoleCode("publisher");
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);
	}

	if (contactPoint != null && !contactPoint.isEmpty()) {
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
	general.addKeyword("ARPA Puglia");
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
	// temp extent - HARD CODED: temporal extent from 2021 to now
	//

	String beginPosition = "2021-01-01";

	TemporalExtent temporalExtent = new TemporalExtent();
	temporalExtent.setBeginPosition(beginPosition);
	temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
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
	String linkage = HISCentralARPAPugliaConnector.BASE_URL.endsWith("/")
		? HISCentralARPAPugliaConnector.BASE_URL.substring(0, HISCentralARPAPugliaConnector.BASE_URL.length() - 1) + "?id_station="
		+ stationId + "&label_pollutant=" + measureName
		: HISCentralARPAPugliaConnector.BASE_URL + "?id_station=" + stationId + "&label_pollutant=" + measureName;

	Online online = new Online();
	online.setLinkage(linkage);
	online.setFunctionCode("download");
	online.setName(stationId + "_" + measureId);
	online.setIdentifier(resourceIdentifier);
	online.setProtocol(CommonNameSpaceContext.HISCENTRAL_ARPA_PUGLIA_NS_URI);

	distribution.addDistributionOnline(online);

	//
	// coverage description
	//

	CoverageDescription coverageDescription = new CoverageDescription();
	String aggProcedure = null;
	String duration = null;
	if (measureInterpolation != null && !measureInterpolation.isEmpty() && !measureInterpolation.contains("null")) {
	    //
	    if (measureInterpolation.toLowerCase().contains("max") || measureInterpolation.toLowerCase().contains("massimo")) {
		aggProcedure = "max";
	    } else if (measureInterpolation.toLowerCase().contains("medio") || measureInterpolation.toLowerCase().contains("media")) {
		aggProcedure = "avg";
	    } else if (measureInterpolation.toLowerCase().contains("min") || measureInterpolation.toLowerCase().contains("minimo")) {
		aggProcedure = "min";
	    }

	    // period
	    if (measureInterpolation.toLowerCase().contains("annua") || measureInterpolation.toLowerCase().contains("annuo")) {
		duration = "P1Y";
	    } else if (measureInterpolation.toLowerCase().contains("giornaliera") || measureInterpolation.toLowerCase()
		    .contains("giornaliero")) {
		duration = "P1D";
	    } else if (measureInterpolation.toLowerCase().contains("orario") || measureInterpolation.toLowerCase().contains("orario")) {
		duration = "P1H";
	    }
	}

	if (duration != null) {
	    dataset.getExtensionHandler().setTimeAggregationDuration8601(duration);
	    dataset.getExtensionHandler().setTimeResolutionDuration8601(duration);
	}
	if (aggProcedure != null) {
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
	}
	coverageDescription.setAttributeIdentifier(varName + "_" + measureId);
	coverageDescription.setAttributeTitle(varName);

	coverageDescription.setAttributeDescription(pollutantDescription);

	dataset.getExtensionHandler().setAttributeMissingValue(HISCentralPugliaDownloader.MISSING_VALUE);

	if (measureUnits != null) {
	    dataset.getExtensionHandler().setAttributeUnitsAbbreviation(measureUnits);
	}

//	if (pollutantUri != null && !pollutantUri.isEmpty()) {
//	    dataset.getExtensionHandler().setObservedPropertyURI(pollutantUri);
//	}

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
