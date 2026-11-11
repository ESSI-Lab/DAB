package eu.essi_lab.accessor.hiscentral.sicilia;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import eu.essi_lab.accessor.hiscentral.utils.HISCentralUtils;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
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
public class HISCentralSiciliaMapper extends FileIdentifierMapper {

    private static final String STATION_INFO = "station-info";
    private static final String ELEMENT_INFO = "element-info";

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return CommonNameSpaceContext.HISCENTRAL_SICILIA_NS_URI;
    }

    /**
     * @param station a {@code /v3/locations} item
     * @param element a {@code /v3/elements} item
     */
    static OriginalMetadata create(JSONObject station, JSONObject element) {

	OriginalMetadata originalMetadata = new OriginalMetadata();
	originalMetadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_SICILIA_NS_URI);

	JSONObject jsonObject = new JSONObject();
	jsonObject.put(STATION_INFO, station);
	jsonObject.put(ELEMENT_INFO, element);

	originalMetadata.setMetadata(jsonObject.toString(4));

	return originalMetadata;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	JSONObject json = new JSONObject(originalMD.getMetadata());

	mapMetadata(json.getJSONObject(STATION_INFO), json.getJSONObject(ELEMENT_INFO), dataset);

	return dataset;
    }

    private void mapMetadata(JSONObject station, JSONObject element, Dataset dataset) {

	String stationId = station.optString("i");
	String stationName = station.optString("n");
	if (stationName.isEmpty()) {
	    stationName = element.optString("stationName");
	}
	String owner = station.optString("o");
	BigDecimal lon = station.optBigDecimal("x", null);
	BigDecimal lat = station.optBigDecimal("y", null);
	BigDecimal altitude = station.optBigDecimal("z", null);

	String elementId = element.optString("elementId");
	String elementName = element.optString("elementName");
	String quantity = element.optString("quantity", elementName);
	if (quantity.isEmpty()) {
	    quantity = elementName;
	}
	String measUnit = element.optString("measUnit");
	String startDate = element.optString("startDate");
	String endDate = element.optString("endDate");
	String basin = element.optString("basin");
	long dtr = element.optLong("dtr", 0);

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	coreMetadata.getMIMetadata().setLanguage("Italian");
	coreMetadata.getMIMetadata().setCharacterSetCode("utf8");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	coreMetadata.addDistributionFormat("WaterML 1.1");

	String title = stationName + " - " + elementName;
	coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(title);
	coreMetadata.getMIMetadata().getDataIdentification().setAbstract(title);

	String resourceIdentifier = generateCode(dataset, stationId + "-" + elementId);
	coreMetadata.getMIMetadata().setFileIdentifier(resourceIdentifier);
	coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);

	//
	// responsible party
	//
	ResponsibleParty publisherContact = new ResponsibleParty();
	publisherContact.setOrganisationName("Protezione Civile Regione Siciliana");
	publisherContact.setRoleCode("publisher");
	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);

	//
	// keywords
	//
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("SICILIA");
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(quantity);
	for (String keyword : new String[] { basin, owner }) {
	    if (!keyword.isEmpty()) {
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword(keyword);
	    }
	}

	Keywords platformKeywords = new Keywords();
	platformKeywords.setTypeCode("platform");
	platformKeywords.addKeyword(stationName);
	coreMetadata.getMIMetadata().getDataIdentification().addKeywords(platformKeywords);

	//
	// spatial extent
	//
	ReferenceSystem referenceSystem = new ReferenceSystem();
	referenceSystem.setCode("EPSG:4326");
	referenceSystem.setCodeSpace("EPSG");
	coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);

	if (lat != null && lon != null) {
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	if (altitude != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(altitude.doubleValue(), altitude.doubleValue());
	}

	//
	// platform
	//
	MIPlatform platform = new MIPlatform();
	platform.setMDIdentifierCode(stationId);
	platform.setDescription(stationName);
	Citation platformCitation = new Citation();
	platformCitation.setTitle(stationName);
	platform.setCitation(platformCitation);
	coreMetadata.getMIMetadata().addMIPlatform(platform);

	dataset.getExtensionHandler().setCountry("ITA");

	//
	// temporal extent
	//
	TemporalExtent temporalExtent = new TemporalExtent();
	temporalExtent.setBeginPosition(startDate);
	if (endDate.isEmpty()) {
	    temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
	} else {
	    temporalExtent.setEndPosition(endDate);
	}
	coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);

	setIndeterminatePosition(dataset, TimeUnit.DAYS.toMillis(30));

	//
	// time resolution and interpolation
	//
	if (dtr > 0) {
	    dataset.getExtensionHandler().setTimeResolutionDuration8601("PT" + dtr + "S");
	}

	String lowerQuantity = quantity.toLowerCase();
	if (lowerQuantity.contains("pioggia") || lowerQuantity.contains("rain")) {
	    dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.TOTAL);
	    if (dtr > 0) {
		dataset.getExtensionHandler().setTimeAggregationDuration8601("PT" + dtr + "S");
	    }
	} else {
	    dataset.getExtensionHandler().setTimeInterpolation(InterpolationType.CONTINUOUS);
	}

	//
	// download
	//
	Online online = new Online();
	online.setLinkage(HISCentralSiciliaClient.getDataLinkagePrefix(dataset.getSource().getEndpoint()) + elementId);
	online.setFunctionCode("download");
	online.setName(stationName + "_" + elementId);
	online.setProtocol(CommonNameSpaceContext.HISCENTRAL_SICILIA_NS_URI);
	online.setIdentifier(resourceIdentifier);
	coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);

	//
	// coverage description
	//
	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeIdentifier(quantity);
	coverageDescription.setAttributeTitle(quantity);

	dataset.getExtensionHandler().setAttributeMissingValue("-9999");
	if (!measUnit.isEmpty()) {
	    dataset.getExtensionHandler().setAttributeUnits(measUnit);
	}

	HISCentralUtils.addDefaultAttributeDescription(dataset, coverageDescription);

	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }
}
