package eu.essi_lab.accessor.hiscentral.marche;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.hiscentral.utils.HISCentralUtils;
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

/**
 * @author Fabrizio
 */
public class HISCentralMarcheMapper extends FileIdentifierMapper {

    private SimpleDateFormat iso8601Format;

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.HISCENTRAL_MARCHE_NS_URI;
    }

    public HISCentralMarcheMapper() {
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

	originalMetadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_MARCHE_NS_URI);

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
	//
	//

	String resourceLocator = datasetInfo.getString("ResourceLocator");

	String keyword = datasetInfo.getString("Keyword");

	String organisationName = datasetInfo.getJSONObject("ResponsibleParty").getString("organisationName");
	String contactInfo = datasetInfo.getJSONObject("ResponsibleParty").getString("contactInfo");

	String resourceConstraints = datasetInfo.getJSONObject("ResourceConstraints").getString("useLimitation");
	String resourceTitle = datasetInfo.getString("ResourceTitle");
	String resourceAbstract = datasetInfo.getString("ResourceAbstract");

	//
	//
	//

	String timeSeriesId = sensorInfo.get("timeSeriesId").toString();

	String tempExtenBegin = sensorInfo.getString("temporalExtent");

	String stationName = sensorInfo.getString("name");

	String statisticalFunction = "";
	if (sensorInfo.getJSONObject("observedProperty").has("statisticalFunction")) {

	    statisticalFunction = sensorInfo.getJSONObject("observedProperty").getString("statisticalFunction");

	    if (statisticalFunction.equals("sum")) {
		dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.TOTAL);
	    } else {
		dataset.getExtensionHandler().setTimeInterpolation(statisticalFunction);
	    }
	}

	String uom = sensorInfo.getJSONObject("observedProperty").getString("uom");
	String basePhenomenon = sensorInfo.getJSONObject("observedProperty").getString("basePhenomenon");

//	String pointLon = sensorInfo.getJSONObject("spatialSamplingFeature").getJSONArray("Point").get(0).toString();
//	String pointLat = sensorInfo.getJSONObject("spatialSamplingFeature").getJSONArray("Point").get(1).toString();
	
	JSONArray pointArray = sensorInfo
	        .getJSONObject("spatialSamplingFeature")
	        .getJSONArray("Point");

	BigDecimal lon = BigDecimal
	        .valueOf(pointArray.getDouble(0))
	        .setScale(5, RoundingMode.HALF_UP);

	BigDecimal lat = BigDecimal
	        .valueOf(pointArray.getDouble(1))
	        .setScale(5, RoundingMode.HALF_UP);


	String intendedObservationSpacing = sensorInfo.getString("intendedObservationSpacing");

	String aggregationTimePeriod = sensorInfo.get("aggregationTimePeriod").toString();

	//
	// intendedObservationSpacing = "P15M"
	//
	// 15 -> timeResolution
	// M -> timeUnits
	//
	String timeUnits = intendedObservationSpacing.substring(intendedObservationSpacing.length() - 1);

	dataset.getExtensionHandler().setTimeUnits(timeUnits);
	dataset.getExtensionHandler().setTimeResolutionDuration8601(intendedObservationSpacing);

	if (aggregationTimePeriod != null && aggregationTimePeriod.equals("0")) {
	    dataset.getExtensionHandler().setTimeAggregationDuration8601(intendedObservationSpacing);

	}

	//
	//
	//

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	coreMetadata.getMIMetadata().setLanguage("Italian");
	coreMetadata.getMIMetadata().setCharacterSetCode("utf8");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	coreMetadata.addDistributionFormat("WaterML 1.1");

	coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(stationName + " - " + basePhenomenon);
	coreMetadata.getMIMetadata().getDataIdentification().setAbstract(resourceAbstract);

	LegalConstraints legalConstraints = new LegalConstraints();
	legalConstraints.addUseLimitation(resourceConstraints);
	coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(legalConstraints);

	//
	// id
	//
	coreMetadata.setIdentifier(timeSeriesId);
	coreMetadata.getMIMetadata().setFileIdentifier(timeSeriesId);

	//
	// responsible party
	//
	ResponsibleParty publisherContact = new ResponsibleParty();

	publisherContact.setOrganisationName(organisationName);
	publisherContact.setRoleCode("publisher");

	Contact contact = new Contact();

	Address address = new Address();
	address.addElectronicMailAddress(contactInfo);
	contact.setAddress(address);

	publisherContact.setContactInfo(contact);

	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);

	//
	// keywords
	//
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(keyword);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("MARCHE");

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
		lat, //
		lon, //
		lat, //
		lon);

	//
	// platform
	//

	MIPlatform platform = new MIPlatform();

	platform.setMDIdentifierCode(stationName);

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
	temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);

	coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);

	setIndeterminatePosition(dataset);

	Distribution distribution = coreMetadata.getMIMetadata().getDistribution();

	//
	// distribution info, information
	//

	Online online = new Online();
	online.setLinkage(resourceLocator);
	online.setFunctionCode("information");
	online.setName("Rete Meteo-Idro-Pluviometrica");

	distribution.addDistributionOnline(online);

	//
	// distribution info, download
	//

	if (tempExtenBegin.contains("+")) {

	    tempExtenBegin = tempExtenBegin.substring(0, tempExtenBegin.indexOf("+"));
	}

	String linkage = HISCentralMarcheConnector.SENSOR_URL + "?id=" + timeSeriesId;

	online = new Online();
	online.setLinkage(linkage);
	online.setFunctionCode("download");
	online.setName(stationName + "_" + timeSeriesId);
	online.setProtocol(CommonNameSpaceContext.HISCENTRAL_MARCHE_NS_URI);

	distribution.addDistributionOnline(online);

	//
	// coverage description
	//

	CoverageDescription coverageDescription = new CoverageDescription();

	coverageDescription.setAttributeIdentifier(basePhenomenon);
	coverageDescription.setAttributeTitle(basePhenomenon);

	String missingValue = "-9999";
	dataset.getExtensionHandler().setAttributeMissingValue(missingValue);

	if (uom != null) {
	    dataset.getExtensionHandler().setAttributeUnits(uom);
	}

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
