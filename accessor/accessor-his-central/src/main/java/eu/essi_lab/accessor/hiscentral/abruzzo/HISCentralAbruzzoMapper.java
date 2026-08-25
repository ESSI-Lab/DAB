package eu.essi_lab.accessor.hiscentral.abruzzo;

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
 * Maps Abruzzo Polaris station/measure original metadata to ISO / WaterML
 * resources.
 * 
 * @author Roberto
 */
public class HISCentralAbruzzoMapper extends FileIdentifierMapper {

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.HISCENTRAL_ABRUZZO_NS_URI;
    }

    public static void setIndeterminatePosition(GSResource gsResource) {
	setIndeterminatePosition(gsResource, TimeUnit.DAYS.toMillis(30));
    }

    /**
     * @param stationInfo
     * @param measureInfo
     * @return
     */
    static OriginalMetadata create(JSONObject stationInfo, JSONObject measureInfo) {

	OriginalMetadata originalMetadata = new OriginalMetadata();

	originalMetadata.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_ABRUZZO_NS_URI);

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("station-info", stationInfo);
	jsonObject.put("measure-info", measureInfo);

	originalMetadata.setMetadata(jsonObject.toString(4));

	return originalMetadata;
    }

    private JSONObject retrieveStationInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getJSONObject("station-info");
    }

    private JSONObject retrieveMeasureInfo(OriginalMetadata metadata) {

	return new JSONObject(metadata.getMetadata()).getJSONObject("measure-info");
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {

	JSONObject stationInfo = retrieveStationInfo(originalMD);
	JSONObject measureInfo = retrieveMeasureInfo(originalMD);

	//
	// STATION INFO
	// {
	// "id": 10,
	// "name": "Crognaleto, Nerito",
	// "coordinates": { "lat": 42.551252, "lng": 13.477348 },
	// "altitude": 800,
	// "province": "TE",
	// "municipality": "Crognaleto",
	// "owner": "SIAP+MICROS",
	// "state": "I"
	// }
	//

	int stationId = stationInfo.optInt("id");
	String stationName = stationInfo.optString("name");
	String province = stationInfo.optString("province", null);
	String municipality = stationInfo.optString("municipality", null);

	JSONObject coordinates = stationInfo.optJSONObject("coordinates");
	BigDecimal lat = null;
	BigDecimal lon = null;
	if (coordinates != null) {
	    lat = coordinates.optBigDecimal("lat", null);
	    lon = coordinates.optBigDecimal("lng", null);
	}
	BigDecimal altitude = stationInfo.optBigDecimal("altitude", null);

	//
	// MEASURE INFO
	// {
	// "station_id": 10,
	// "measure_id": 882,
	// "name": "Termometro aria",
	// "short_name": "T_Aria",
	// "unit": "°C",
	// "state": "O"
	// }
	//

	int measureId = measureInfo.optInt("measure_id");
	String measureName = measureInfo.optString("name");
	String shortName = measureInfo.optString("short_name", null);
	String unit = measureInfo.optString("unit", null);

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	coreMetadata.getMIMetadata().setLanguage("Italian");
	coreMetadata.getMIMetadata().setCharacterSetCode("utf8");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
	coreMetadata.addDistributionFormat("WaterML 1.1");

	String title = stationName + " - " + measureName;
	coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(title);
	coreMetadata.getMIMetadata().getDataIdentification().setAbstract(title);

	String id = generateCode(dataset, stationId + "-" + measureId + "-" + stationName);
	coreMetadata.setIdentifier(id);
	coreMetadata.getMIMetadata().setFileIdentifier(id);

	ResponsibleParty publisherContact = new ResponsibleParty();
	publisherContact.setOrganisationName("Servizio Idrografico - Regione Abruzzo");
	publisherContact.setRoleCode("publisher");
	coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);

	coreMetadata.getMIMetadata().getDataIdentification().addKeyword(measureName);
	if (shortName != null && !shortName.isEmpty()) {
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(shortName);
	}
	if (municipality != null && !municipality.isEmpty()) {
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(municipality);
	}
	if (province != null && !province.isEmpty()) {
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(province);
	}
	coreMetadata.getMIMetadata().getDataIdentification().addKeyword("ABRUZZO");

	Keywords kwd = new Keywords();
	kwd.setTypeCode("platform");
	kwd.addKeyword(stationName);
	coreMetadata.getMIMetadata().getDataIdentification().addKeywords(kwd);

	ReferenceSystem referenceSystem = new ReferenceSystem();
	referenceSystem.setCode("EPSG:4326");
	referenceSystem.setCodeSpace("EPSG");
	coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);

	if (lat != null && lon != null) {
	    coreMetadata.addBoundingBox(lat, lon, lat, lon);
	}

	if (altitude != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(altitude.doubleValue(),
		    altitude.doubleValue());
	}

	MIPlatform platform = new MIPlatform();
	platform.setMDIdentifierCode(String.valueOf(stationId));
	dataset.getExtensionHandler().setCountry("ITA");
	platform.setDescription(stationName);
	Citation platformCitation = new Citation();
	platformCitation.setTitle(stationName);
	platform.setCitation(platformCitation);
	coreMetadata.getMIMetadata().addMIPlatform(platform);

	TemporalExtent temporalExtent = new TemporalExtent();
	temporalExtent.setBeginPosition("2000-01-01T00:00:00");
	temporalExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
	coreMetadata.getDataIdentification().addTemporalExtent(temporalExtent);
	setIndeterminatePosition(dataset);

	HISCentralAbruzzoIdentifierMangler mangler = new HISCentralAbruzzoIdentifierMangler();
	mangler.setPlatformIdentifier(stationName + ":" + stationId);
	mangler.setParameterIdentifier(String.valueOf(measureId));
	mangler.setSourceIdentifier(dataset.getSource().getUniqueIdentifier());

	String identifier = mangler.getMangling();

	String baseUrl = dataset.getSource().getEndpoint();
	if (baseUrl == null || baseUrl.isEmpty()) {
	    baseUrl = HISCentralAbruzzoConnector.BASE_URL;
	}
	String linkage = HISCentralAbruzzoConnector.buildDataSeriesLinkage(baseUrl, stationId, measureId);

	coreMetadata.addDistributionOnlineResource(identifier, linkage, CommonNameSpaceContext.HISCENTRAL_ABRUZZO_NS_URI,
		"download");

	String resourceIdentifier = generateCode(dataset, stationId + "-" + measureId);
	coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);
	coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeIdentifier(String.valueOf(measureId));
	coverageDescription.setAttributeTitle(measureName);

	String missingValue = "-9999";
	dataset.getExtensionHandler().setAttributeMissingValue(missingValue);

	if (unit != null && !unit.isEmpty()) {
	    dataset.getExtensionHandler().setAttributeUnits(unit);
	}

	HISCentralUtils.addDefaultAttributeDescription(dataset, coverageDescription);
	coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
    }
}
