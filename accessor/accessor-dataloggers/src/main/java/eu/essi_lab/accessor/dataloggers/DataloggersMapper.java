package eu.essi_lab.accessor.dataloggers;

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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;

import eu.essi_lab.iso.datamodel.classes.*;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;

public class DataloggersMapper extends FileIdentifierMapper {

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private String PLATFORM_PREFIX = "DATALOGGERS";

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.DATALOGGERS_NS_URI;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	Dataset dataset = new Dataset();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    private void mapMetadata(OriginalMetadata originalMD, Dataset dataset) {

	String originalMetadata = originalMD.getMetadata();

	JSONObject originalObj = new JSONObject(originalMetadata);

	JSONObject dataloggerJson = originalObj.optJSONObject("datalogger");
	JSONObject datastreamJson = originalObj.optJSONObject("datastream");

	if (dataloggerJson != null && datastreamJson != null) {

	    // Extract datalogger information
	    Integer dataloggerId = dataloggerJson.optInt("datalogger_id", 0);
	    String dataloggerCod = getString(dataloggerJson, "datalogger_cod");
	    String dataproviderCod = getString(dataloggerJson, "dataprovider_cod");
	    String dataloggerLocation = getString(dataloggerJson, "datalogger_location");

	    // Extract datastream information
	    Integer datastreamId = datastreamJson.optInt("datastream_id", 0);
	    String varCod = getString(datastreamJson, "var_cod");
	    String uomCod = getString(datastreamJson, "uom_cod");
	    Integer datastreamStep = datastreamJson.optInt("datastream_step", 0);
	    String tipologiaRete = getString(datastreamJson, "tipologia_rete");
	    String datastreamAvailableSince = getString(datastreamJson, "datastream_available_since");
	    String datastreamAvailableUntil = getString(datastreamJson, "datastream_available_until");

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    // Parse location from POINT format
	    Double lat = null;
	    Double lon = null;
	    if (dataloggerLocation != null && dataloggerLocation.startsWith("POINT(")) {
		String coords = dataloggerLocation.replace("POINT(", "").replace(")", "");
		String[] parts = coords.split(" ");
		if (parts.length >= 2) {
		    try {
			lon = Double.parseDouble(parts[0]);
			lat = Double.parseDouble(parts[1]);
		    } catch (NumberFormatException e) {
			logger.warn("Error parsing coordinates: {}", dataloggerLocation);
		    }
		}
	    }

	    // IDENTIFIER
	    String id = null;
	    try {
		id = StringUtils.hashSHA1messageDigest(dataloggerCod + " - " + varCod + " - " + datastreamId);
		coreMetadata.setIdentifier(id);
		coreMetadata.getMIMetadata().setFileIdentifier(id);
	    } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
		logger.error("Error generating identifier", e);
	    }

	    // Keywords
	    if (dataloggerCod != null) {
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword(dataloggerCod);
	    }
	    if (varCod != null) {
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword(varCod);
	    }
	    if (tipologiaRete != null) {
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword(tipologiaRete);
	    }
	    if (dataproviderCod != null) {
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword(dataproviderCod);
			ResponsibleParty party = new ResponsibleParty();
			party.setOrganisationName(dataproviderCod);
			party.setRoleCode("owner");
			coreMetadata.getMIMetadata().getDataIdentification().addCitationResponsibleParty(party);
	    }

	    // Reference system
	    ReferenceSystem referenceSystem = new ReferenceSystem();
	    referenceSystem.setCode("EPSG:4326");
	    referenceSystem.setCodeSpace("EPSG");
	    coreMetadata.getMIMetadata().addReferenceSystemInfo(referenceSystem);

	    // Bounding box
	    if (lon != null && lat != null) {
		coreMetadata.addBoundingBox(lat, lon, lat, lon);
	    }

	    // Platform
	    MIPlatform platform = new MIPlatform();
	    platform.setMDIdentifierCode(String.valueOf(dataloggerId));
	    platform.setDescription(dataloggerCod);
	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(dataloggerCod);
	    platform.setCitation(platformCitation);
	    coreMetadata.getMIMetadata().addMIPlatform(platform);

	    Keywords keyword = new Keywords();
	    keyword.setTypeCode("platform");
	    if (dataloggerCod != null) {
		keyword.addKeyword(dataloggerCod);
	    }
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);

	    // Temporal extent
	    if (datastreamAvailableSince != null && datastreamAvailableUntil != null) {
		try {
		    OffsetDateTime start = OffsetDateTime.parse(datastreamAvailableSince);
		    OffsetDateTime end = OffsetDateTime.parse(datastreamAvailableUntil);
		    coreMetadata.addTemporalExtent(start.toString(), end.toString());
		} catch (Exception e) {
		    logger.warn("Error parsing temporal extent", e);
		}
	    }

	    // Title and abstract
	    String title = dataloggerCod;
	    if (varCod != null) {
		title += " - " + varCod;
	    }
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationTitle(title);
	    coreMetadata.getMIMetadata().getDataIdentification().setAbstract(title);

	    // Grid spatial representation
	    GridSpatialRepresentation grid = new GridSpatialRepresentation();
	    grid.setNumberOfDimensions(1);
	    grid.setCellGeometryCode("point");
	    coreMetadata.getMIMetadata().addGridSpatialRepresentation(grid);

	    // Identifier mangler
	    DataloggersIdentifierMangler mangler = new DataloggersIdentifierMangler();
	    mangler.setDataloggerIdentifier(String.valueOf(dataloggerId));
	    mangler.setDatastreamIdentifier(String.valueOf(datastreamId));
	    mangler.setVariableIdentifier(varCod);
	    mangler.setDataproviderIdentifier(dataproviderCod);

	    // Coverage description
	    CoverageDescription coverageDescription = new CoverageDescription();
	    if (varCod != null) {
		coverageDescription.setAttributeIdentifier(varCod);
		coverageDescription.setAttributeTitle(varCod);
	    }
	    coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	    // Distribution format
	    coreMetadata.addDistributionFormat("WaterML 1.1");

	    // Language and character set
	    coreMetadata.getMIMetadata().setLanguage("English");
	    coreMetadata.getMIMetadata().setCharacterSetCode("utf8");
	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");

	    // Extension handler
	    ExtensionHandler extensionHandler = dataset.getExtensionHandler();
	    if (uomCod != null) {
		extensionHandler.setAttributeUnits(uomCod);
	    }
	    if (datastreamStep != null && datastreamStep > 0) {
		extensionHandler.setTimeResolution(String.valueOf(datastreamStep));
	    }

	    // Online resource
	    String identifier = mangler.getMangling();
	    String dataUrl = dataset.getSource().getEndpoint();
	    coreMetadata.addDistributionOnlineResource(identifier, dataUrl, CommonNameSpaceContext.DATALOGGERS_NS_URI, "download");
	    coreMetadata.getDataIdentification().setResourceIdentifier(identifier);

	    String resourceIdentifier = generateCode(dataset, dataloggerId + "-" + datastreamId);
	    coreMetadata.getDataIdentification().setResourceIdentifier(resourceIdentifier);
	    if (coreMetadata.getMIMetadata().getDistribution() != null
		    && coreMetadata.getMIMetadata().getDistribution().getDistributionOnline() != null) {
		coreMetadata.getMIMetadata().getDistribution().getDistributionOnline().setIdentifier(resourceIdentifier);
	    }

	}

    }

    private String getString(JSONObject result, String key) {
	try {
	    String ret = result.optString(key, null);
	    if (ret == null || "".equals(ret) || "[]".equals(ret) || "null".equals(ret)) {
		return null;
	    }
	    return ret;
	} catch (Exception e) {
	    logger.warn("Error reading key {}: ", key, e);
	    return null;
	}
    }
}

