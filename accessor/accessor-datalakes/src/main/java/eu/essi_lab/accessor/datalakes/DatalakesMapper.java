package eu.essi_lab.accessor.datalakes;

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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;

import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
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

/**
 * Maps Datalakes dataset parameters to ISO19115 {@link Dataset} resources.
 */
public class DatalakesMapper extends FileIdentifierMapper {

    private final Logger logger = GSLoggerFactory.getLogger(getClass());

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.DATALAKES_NS_URI;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {
	JSONObject json = new JSONObject(originalMD.getMetadata());
	Dataset dataset = new Dataset();
	dataset.setSource(source);
	mapDataset(json, dataset);
	return dataset;
    }

    private void mapDataset(JSONObject json, Dataset dataset) {

	CoreMetadata core = dataset.getHarmonizedMetadata().getCoreMetadata();
	MIMetadata mi = core.getMIMetadata();

	JSONObject datasetInfo = json.getJSONObject("dataset");
	int datasetId = json.getInt("datasetId");
	String axis = json.getString("axis");
	String parseParameter = json.optString("parseparameter");
	String unit = json.optString("unit", null);
	boolean downloadable = json.optBoolean("downloadable", false);

	JSONObject parameter = json.optJSONObject("parameter");
	JSONObject lake = json.optJSONObject("lake");
	JSONObject organisation = json.optJSONObject("organisation");
	JSONObject license = json.optJSONObject("license");
	JSONObject sensor = json.optJSONObject("sensor");

	String parameterName = parameter != null ? parameter.optString("name", parseParameter) : parseParameter;
	String cfName = parameter != null ? parameter.optString("cfname", parseParameter) : parseParameter;
	String datasetTitle = datasetInfo.optString("title", "Datalakes dataset " + datasetId);

	String title = datasetTitle + " - " + parameterName;
	mi.getDataIdentification().setCitationTitle(title);

	String description = datasetInfo.optString("description", title);
	mi.getDataIdentification().setAbstract(description);

	try {
	    String hashInput = "DatalakesSeries:" + datasetId + ":" + axis;
	    String id = StringUtils.hashSHA1messageDigest(hashInput);
	    core.setIdentifier(id);
	    mi.setFileIdentifier(id);
	} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
	    logger.error("Error generating identifier", e);
	}

	if (lake != null) {
	    mi.getDataIdentification().addKeyword(lake.optString("name"));
	}
	mi.getDataIdentification().addKeyword(parameterName);
	if (cfName != null && !cfName.isEmpty()) {
	    mi.getDataIdentification().addKeyword(cfName);
	}
	mi.getDataIdentification().addKeyword("Datalakes");
	mi.getDataIdentification().addTopicCategory("environment");

	if (organisation != null && organisation.optString("name", null) != null) {
	    ResponsibleParty party = new ResponsibleParty();
	    party.setRoleCode("owner");
	    party.setOrganisationName(organisation.getString("name"));
	    if (organisation.optString("link", null) != null) {
		Contact contact = new Contact();
		Address address = new Address();
		address.addElectronicMailAddress(organisation.getString("link"));
		contact.setAddress(address);
		party.setContactInfo(contact);
	    }
	    mi.getDataIdentification().addCitationResponsibleParty(party);
	}

	if (license != null) {
	    LegalConstraints lc = new LegalConstraints();
	    if (license.optString("name", null) != null) {
		lc.addOtherConstraints(license.getString("name"));
	    }
	    if (license.optString("link", null) != null) {
		lc.addOtherConstraints(license.getString("link"), license.optString("name", license.getString("link")));
	    }
	    mi.getDataIdentification().addLegalConstraints(lc);
	}

	String citation = datasetInfo.optString("citation", null);
	if (citation != null && !citation.isEmpty()) {
	    Citation datasetCitation = new Citation();
	    datasetCitation.setTitle(datasetTitle);
	    datasetCitation.setOtherCitationDetails(citation);
	    mi.getDataIdentification().setCitation(datasetCitation);
	}

	if (datasetInfo.has("latitude") && datasetInfo.has("longitude")) {
	    try {
		BigDecimal lat = new BigDecimal(datasetInfo.getString("latitude"));
		BigDecimal lon = new BigDecimal(datasetInfo.getString("longitude"));
		if (lat.doubleValue() > -9990 && lon.doubleValue() > -9990) {
		    core.getDataIdentification().addGeographicBoundingBox(lat, lon, lat, lon);
		}
	    } catch (NumberFormatException e) {
		logger.debug("Skipping invalid coordinates for dataset {}", datasetId);
	    }
	}

	TemporalExtent temporalExtent = new TemporalExtent();
	if (datasetInfo.optString("mindatetime", null) != null) {
	    temporalExtent.setBeginPosition(datasetInfo.getString("mindatetime"));
	}
	if (datasetInfo.optString("maxdatetime", null) != null) {
	    temporalExtent.setEndPosition(datasetInfo.getString("maxdatetime"));
	}
	if (temporalExtent.getBeginPosition() != null || temporalExtent.getEndPosition() != null) {
	    mi.getDataIdentification().addTemporalExtent(temporalExtent);
	    setIndeterminatePosition(dataset);
	}

	if (sensor != null) {
	    MIPlatform platform = new MIPlatform();
	    platform.setMDIdentifierCode(String.valueOf(sensor.optInt("id")));
	    platform.setDescription(sensor.optString("name", null));
	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(sensor.optString("name", "Sensor"));
	    platform.setCitation(platformCitation);
	    mi.addMIPlatform(platform);
	}

	ReferenceSystem referenceSystem = new ReferenceSystem();
	referenceSystem.setCode("EPSG:4326");
	referenceSystem.setCodeSpace("EPSG");
	mi.addReferenceSystemInfo(referenceSystem);

	GridSpatialRepresentation grid = new GridSpatialRepresentation();
	grid.setNumberOfDimensions(1);
	grid.setCellGeometryCode("point");
	mi.addGridSpatialRepresentation(grid);

	CoverageDescription coverageDescription = new CoverageDescription();
	coverageDescription.setAttributeIdentifier(cfName != null ? cfName : parseParameter);
	coverageDescription.setAttributeTitle(parameterName);
	mi.addCoverageDescription(coverageDescription);

	ExtensionHandler extensionHandler = dataset.getExtensionHandler();
	if (unit != null && !unit.isEmpty() && !"none".equalsIgnoreCase(unit)) {
	    extensionHandler.setAttributeUnits(unit);
	    extensionHandler.setAttributeUnitsAbbreviation(unit);
	}

	DatalakesIdentifierMangler mangler = new DatalakesIdentifierMangler();
	mangler.setDatasetId(String.valueOf(datasetId));
	mangler.setAxis(axis);
	String identifier = mangler.getMangling();

	if (downloadable) {
	    core.addDistributionOnlineResource(identifier, dataset.getSource().getEndpoint(),
		    CommonNameSpaceContext.DATALAKES_NS_URI, "download");
	}

	core.getDataIdentification().setResourceIdentifier(identifier);
	String code = generateCode(dataset, identifier);
	core.getDataIdentification().setResourceIdentifier(code);
	if (mi.getDistribution() != null && mi.getDistribution().getDistributionOnline() != null) {
	    mi.getDistribution().getDistributionOnline().setIdentifier(code);
	}

	mi.setLanguage("English");
	mi.setCharacterSetCode("utf8");
	mi.addHierarchyLevelScopeCodeListValue("dataset");
    }
}
