package eu.essi_lab.accessor.hiscentral.friuli;

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

import org.json.JSONObject;

import eu.essi_lab.accessor.hiscentral.utils.DescriptionParsingResult;
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
import eu.essi_lab.model.resource.InterpolationType;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * @author Roberto
 */
public class HISCentralFriuliMapper extends FileIdentifierMapper {

    private SimpleDateFormat iso8601Format;

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.HISCENTRAL_FRIULI_NS_URI;
    }

    public HISCentralFriuliMapper() {
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

	originalMetadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_FRIULI_NS_URI);

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
	// "id": 16,
	// "nome": "Codroipo",
	// "sigla": "COD",
	// "codice": "16",
	// "data_inizio": "1999-07-20",
	// "data_fine": null,
	// "proprietario": "Protezione Civile FVG",
	// "lat": "45.952356",
	// "lon": "13.002742",
	// "alt": "37"
	//

	// String resourceLocator = datasetInfo.getString("ResourceLocator");
	//
	// String keyword = datasetInfo.getString("Keyword");
	//
	//
	// String contactInfo = datasetInfo.optJSONObject("ResponsibleParty").getString("contactInfo");
	//
	// String resourceConstraints = datasetInfo.optJSONObject("ResourceConstraints").getString("useLimitation");
	// String resourceAbstract = datasetInfo.optString("ResourceAbstract");

	String resourceTitle = datasetInfo.optString("nome");

	String ownerOrganisationName = "";
	JSONObject propObject = datasetInfo.optJSONObject("proprietario");
	if (propObject != null) {
	    ownerOrganisationName = propObject.optString("ragione_sociale");
	}
	String tempExtenBegin = datasetInfo.optString("data_inizio");
	String tempExtenEnd = datasetInfo.optString("data_fine");

	BigDecimal pointLon = datasetInfo.optBigDecimal("lon", null);
	BigDecimal pointLat = datasetInfo.optBigDecimal("lat", null);
	Double altitude = datasetInfo.optDouble("alt");

	String code = datasetInfo.optString("codice");
	String abbr = datasetInfo.optString("sigla");
	String id = datasetInfo.optString("id");

	//
	// MEASURE INFO
	// "id": 20481,
	// "nome": "T180_MIN",
	// "derivato": true,
	// "tipo": {
	// "id": 10002,
	// "descrizione": "Temperatura minima a 180 cm",
	// "delta_t": 86400
	// }
	//

	String timeSeriesId = sensorInfo.optString("id");
	String measureCode = sensorInfo.optString("nome");

	String valueType = sensorInfo.optString("derivato");

	JSONObject jsonType = sensorInfo.optJSONObject("tipo");

	String timeSeriesId2 = jsonType.optString("id");
	String description = jsonType.optString("descrizione");
	String delta_t = jsonType.optString("delta_t");

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	coreMetadata.getMIMetadata().setLanguage("Italian");
	coreMetadata.getMIMetadata().setCharacterSetCode("utf8");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	coreMetadata.addDistributionFormat("WaterML 1.1");

	coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(resourceTitle + " (" + abbr + ")" + " - " + description);
	coreMetadata.getMIMetadata().getDataIdentification().setAbstract(resourceTitle + " (" + abbr + ")" + " - " + description);

	//
	// id
	//
	coreMetadata.setIdentifier(timeSeriesId);
	coreMetadata.getMIMetadata().setFileIdentifier(timeSeriesId);

	//
	// responsible party
	//
	ResponsibleParty publisherContact = new ResponsibleParty();

	publisherContact.setOrganisationName(ownerOrganisationName);
	publisherContact.setRoleCode("owner");

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
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(abbr);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(measureCode);
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(description);

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("FRIULI");
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("FVG");
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("FRIULI VENEZIA GIULIA");

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
	coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(altitude, altitude);

	//
	// platform
	//

	MIPlatform platform = new MIPlatform();

	platform.setMDIdentifierCode(code);

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

	String linkage = dataset.getSource().getEndpoint() + "data?measure_id=" + timeSeriesId;

	Online online = new Online();
	online.setLinkage(linkage);
	online.setFunctionCode("download");
	online.setName(resourceTitle + "_" + timeSeriesId);
	online.setProtocol(CommonNameSpaceContext.HISCENTRAL_FRIULI_NS_URI);

	distribution.addDistributionOnline(online);

	//
	// coverage description
	//

	CoverageDescription coverageDescription = new CoverageDescription();

	coverageDescription.setAttributeIdentifier(measureCode);

	DescriptionParsingResult parsedDescription = HISCentralUtils.parseDescription(description);

	dataset.getExtensionHandler().setTimeResolutionDuration8601("PT" + delta_t + "S");

	String attributeTitle = null;

	if (parsedDescription == null) {
	    attributeTitle = description;
	} else {
	    attributeTitle = parsedDescription.getRest();
	    InterpolationType interpolation = parsedDescription.getInterpolation();
	    if (interpolation != null && !interpolation.equals(InterpolationType.CONTINUOUS)//
		    && !interpolation.equals(InterpolationType.DISCONTINUOUS)) {
		dataset.getExtensionHandler().setTimeAggregationDuration8601("PT"+delta_t+"S");
	    }
	    dataset.getExtensionHandler().setTimeInterpolation(interpolation);
	}
	if (attributeTitle.startsWith("Altezza idrometrica ")) {
	    attributeTitle = "Altezza idrometrica";
	}
	if (attributeTitle.startsWith("Portata")) {
	    attributeTitle = "Portata";
	}
	if (attributeTitle.toLowerCase().contains("temperatura")) {
	    if (attributeTitle.toLowerCase().contains("suolo")) {
		attributeTitle = "Temperatura suolo";
	    } else if (attributeTitle.toLowerCase().contains("aria")) {
		attributeTitle = "Temperatura aria";
	    } else if (attributeTitle.toLowerCase().contains("mare")) {
		attributeTitle = "Temperatura mare";
	    } else {
		attributeTitle = "Temperatura";
	    }
	}
	coverageDescription.setAttributeTitle(attributeTitle);

	String missingValue = "-9999";
	dataset.getExtensionHandler().setAttributeMissingValue(missingValue);

	// if (uom != null) {
	// dataset.getExtensionHandler().setAttributeUnits(uom);
	// }
	//
	// String units = uom != null ? " Units: " + uom : "";

	// // as no description is given this field is calculated
	// HISCentralUtils.addDefaultAttributeDescription(dataset, coverageDescription);

	coverageDescription.setAttributeDescription(description);

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
