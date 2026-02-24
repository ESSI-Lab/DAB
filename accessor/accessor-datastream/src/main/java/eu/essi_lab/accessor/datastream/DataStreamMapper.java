package eu.essi_lab.accessor.datastream;

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
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.GridSpatialRepresentation;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;

/**
 * Maps DataStream logical entities to the internal ISO19115-based data model.
 *
 * Each DataStream dataset (Metadata entry) is mapped to a {@link DatasetCollection}.
 * For each CharacteristicName at a given location, a {@link Dataset} is created
 * with a title like "Temperature, air at location Smith 14A".
 */
public class DataStreamMapper extends FileIdentifierMapper {

    private final Logger logger = GSLoggerFactory.getLogger(getClass());

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.DATASTREAM_NS_URI;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	String originalMetadata = originalMD.getMetadata();
	JSONObject json = new JSONObject(originalMetadata);

	String type = json.optString("type", "dataset");

	if ("collection".equalsIgnoreCase(type)) {

	    DatasetCollection collection = new DatasetCollection();
	    collection.setSource(source);
	    mapCollection(json, collection);
	    return collection;

	} else {

	    Dataset dataset = new Dataset();
	    dataset.setSource(source);
	    mapDataset(json, dataset);
	    return dataset;
	}
    }

    private void mapCollection(JSONObject json, DatasetCollection collection) {

	CoreMetadata core = collection.getHarmonizedMetadata().getCoreMetadata();
	MIMetadata mi = core.getMIMetadata();

	JSONObject md = json.optJSONObject("metadata");
	String doi = json.optString("doi", null);
	String datasetId = json.optString("datasetId", null);
	String datasetName = json.optString("datasetName", null);

	//
	// Identifier
	//
	String id = null;
	try {
	    id = StringUtils.hashSHA1messageDigest("DataStreamCollection:" + (doi != null ? doi : datasetId));
	    core.setIdentifier(id);
	    mi.setFileIdentifier(id);
	} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
	    logger.error("Error generating identifier", e);
	}

	//
	// Title / abstract
	//
	String title = datasetName != null ? datasetName : doi;
	if (title == null) {
	    title = "DataStream dataset";
	}
	mi.getDataIdentification().setCitationTitle(title);
	mi.getDataIdentification().setAbstract(md != null ? md.optString("Abstract", title) : title);

	//
	// DOI as identifier / citation
	//
	if (doi != null) {
	    mi.getDataIdentification().setResourceIdentifier(doi);
	    Citation citation = new Citation();
	    citation.setTitle(title);
	    citation.addIdentifier(doi);
	    String fullCitation = md != null ? md.optString("Citation", null) : null;
	    if (fullCitation != null && !fullCitation.isEmpty()) {
		citation.setOtherCitationDetails(fullCitation);
	    }
	    mi.getDataIdentification().setCitation(citation);
	}

	//
	// Keywords
	//
	if (md != null) {
	    JSONArray kwArray = md.optJSONArray("Keywords");
	    if (kwArray != null) {
		for (int i = 0; i < kwArray.length(); i++) {
		    String kw = kwArray.optString(i, null);
		    if (kw != null && !kw.isEmpty()) {
			mi.getDataIdentification().addKeyword(kw);
		    }
		}
	    }
	}

	//
	// Topic category
	//
	if (md != null) {
	    JSONArray topicArray = md.optJSONArray("TopicCategoryCode");
	    if (topicArray != null && topicArray.length() > 0) {
		String topic = topicArray.optString(0, null);
		if (topic != null && !topic.isEmpty()) {
		    mi.getDataIdentification().addTopicCategory(topic);
		}
	    }
	}

	if (md != null) {
	    //
	    // Licence
	    //
	    String licence = md.optString("Licence", null);
	    if (licence != null && !licence.isEmpty()) {
		LegalConstraints lc = new LegalConstraints();
		lc.addOtherConstraints(licence);
		mi.getDataIdentification().addLegalConstraints(lc);
	    }

	    //
	    // Responsible party (data collection/upload organization and steward email)
	    //
	    String org = md.optString("DataCollectionOrganization", null);
	    if (org == null || org.isEmpty()) {
		org = md.optString("DataUploadOrganization", null);
	    }
	    String stewardEmail = md.optString("DataStewardEmail", null);

	    if ((org != null && !org.isEmpty()) || (stewardEmail != null && !stewardEmail.isEmpty())) {
		ResponsibleParty party = new ResponsibleParty();
		party.setRoleCode("owner");
		if (org != null && !org.isEmpty()) {
		    party.setOrganisationName(org);
		}
		if (stewardEmail != null && !stewardEmail.isEmpty()) {
		    Contact contact = new Contact();
		    Address address = new Address();
		    address.addElectronicMailAddress(stewardEmail);
		    contact.setAddress(address);
		    party.setContactInfo(contact);
		}
		mi.getDataIdentification().addCitationResponsibleParty(party);
	    }
	}

	//
	// Reference system (WGS84, as used by DataStream Locations)
	//
	ReferenceSystem referenceSystem = new ReferenceSystem();
	referenceSystem.setCode("EPSG:4326");
	referenceSystem.setCodeSpace("EPSG");
	mi.addReferenceSystemInfo(referenceSystem);

	//
	// Generic grid representation (time series)
	//
	GridSpatialRepresentation grid = new GridSpatialRepresentation();
	grid.setNumberOfDimensions(1);
	grid.setCellGeometryCode("point");
	mi.addGridSpatialRepresentation(grid);

	//
	// Language and character set
	//
	mi.setLanguage("English");
	mi.setCharacterSetCode("utf8");
	mi.addHierarchyLevelScopeCodeListValue("series");
    }

    private void mapDataset(JSONObject json, Dataset dataset) {

	CoreMetadata core = dataset.getHarmonizedMetadata().getCoreMetadata();
	MIMetadata mi = core.getMIMetadata();

	String doi = json.optString("doi", null);
	String datasetId = json.optString("datasetId", null);
	String datasetName = json.optString("datasetName", null);
	String characteristicName = json.optString("characteristicName", null);
	JSONObject location = json.optJSONObject("location");
	JSONObject md = json.optJSONObject("metadata");

	//
	// Identifier
	//
	String id = null;
	try {
	    String locationId = location != null ? String.valueOf(location.optInt("Id")) : "unknown-location";
	    String hashInput = "DataStreamSeries:" + (doi != null ? doi : datasetId) + ":" + locationId + ":"
		    + (characteristicName != null ? characteristicName : "");
	    id = StringUtils.hashSHA1messageDigest(hashInput);
	    core.setIdentifier(id);
	    mi.setFileIdentifier(id);
	} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
	    logger.error("Error generating identifier", e);
	}

	//
	// Title and abstract: "CharacteristicName at location Name"
	//
	String locationName = null;
	if (location != null) {
	    locationName = location.optString("Name", null);
	    if (locationName == null || locationName.isEmpty()) {
		locationName = location.optString("ID", null);
	    }
	}

	StringBuilder titleBuilder = new StringBuilder();
	if (characteristicName != null && !characteristicName.isEmpty()) {
	    titleBuilder.append(characteristicName);
	} else {
	    titleBuilder.append("Variable");
	}
	if (locationName != null && !locationName.isEmpty()) {
	    titleBuilder.append(" at location ").append(locationName);
	}
	String title = titleBuilder.toString();
	mi.getDataIdentification().setCitationTitle(title);
	// default abstract is title, but will be overridden below if collection-level
	// abstract is available
	mi.getDataIdentification().setAbstract(title);

	//
	// Keywords: dataset name, characteristic, location type
	//
	if (datasetName != null && !datasetName.isEmpty()) {
	    mi.getDataIdentification().addKeyword(datasetName);
	}
	if (characteristicName != null && !characteristicName.isEmpty()) {
	    mi.getDataIdentification().addKeyword(characteristicName);
	}
	if (location != null) {
	    String locType = location.optString("MonitoringLocationType", null);
	    if (locType != null && !locType.isEmpty()) {
		mi.getDataIdentification().addKeyword(locType);
	    }
	}

	//
	// Propagate key collection-level metadata (abstract, licence, topic category,
	// keywords, organisations, steward email) into the dataset
	//
	if (md != null) {

	    // Abstract
	    String abs = md.optString("Abstract", null);
	    if (abs != null && !abs.isEmpty()) {
		mi.getDataIdentification().setAbstract(abs);
	    }

	    // Keywords
	    JSONArray kwArray = md.optJSONArray("Keywords");
	    if (kwArray != null) {
		for (int i = 0; i < kwArray.length(); i++) {
		    String kw = kwArray.optString(i, null);
		    if (kw != null && !kw.isEmpty()) {
			mi.getDataIdentification().addKeyword(kw);
		    }
		}
	    }

	    // Topic category
	    JSONArray topicArray = md.optJSONArray("TopicCategoryCode");
	    if (topicArray != null && topicArray.length() > 0) {
		String topic = topicArray.optString(0, null);
		if (topic != null && !topic.isEmpty()) {
		    mi.getDataIdentification().addTopicCategory(topic);
		}
	    }

	    // Licence
	    String licence = md.optString("Licence", null);
	    if (licence != null && !licence.isEmpty()) {
		LegalConstraints lc = new LegalConstraints();
		lc.addOtherConstraints(licence);
		mi.getDataIdentification().addLegalConstraints(lc);
	    }

	    // Organisations and steward email
	    String org = md.optString("DataCollectionOrganization", null);
	    if (org == null || org.isEmpty()) {
		org = md.optString("DataUploadOrganization", null);
	    }
	    String stewardEmail = md.optString("DataStewardEmail", null);

	    if ((org != null && !org.isEmpty()) || (stewardEmail != null && !stewardEmail.isEmpty())) {
		ResponsibleParty party = new ResponsibleParty();
		party.setRoleCode("owner");
		if (org != null && !org.isEmpty()) {
		    party.setOrganisationName(org);
		}
		if (stewardEmail != null && !stewardEmail.isEmpty()) {
		    Contact contact = new Contact();
		    Address address = new Address();
		    address.addElectronicMailAddress(stewardEmail);
		    contact.setAddress(address);
		    party.setContactInfo(contact);
		}
		mi.getDataIdentification().addCitationResponsibleParty(party);
	    }
	}

	//
	// Keywords: dataset name, characteristic, location type
	//
	if (datasetName != null && !datasetName.isEmpty()) {
	    mi.getDataIdentification().addKeyword(datasetName);
	}
	if (characteristicName != null && !characteristicName.isEmpty()) {
	    mi.getDataIdentification().addKeyword(characteristicName);
	}
	if (location != null) {
	    String locType = location.optString("MonitoringLocationType", null);
	    if (locType != null && !locType.isEmpty()) {
		mi.getDataIdentification().addKeyword(locType);
	    }
	}

	//
	// Platform: use location as a simple platform description
	//
	if (location != null) {
	    MIPlatform platform = new MIPlatform();
	    platform.setMDIdentifierCode(String.valueOf(location.optInt("Id")));
	    platform.setDescription(locationName);
	    Citation platformCitation = new Citation();
	    platformCitation.setTitle(locationName);
	    platform.setCitation(platformCitation);
	    mi.addMIPlatform(platform);

	    Keywords platformKeywords = new Keywords();
	    platformKeywords.setTypeCode("platform");
	    if (locationName != null) {
		platformKeywords.addKeyword(locationName);
	    }
	    mi.getDataIdentification().addKeywords(platformKeywords);
	}

	//
	// Spatial extent from location (lat/lon)
	//
	if (location != null) {
	    if (location.has("Latitude") && location.has("Longitude") && !location.isNull("Latitude")
		    && !location.isNull("Longitude")) {

		BigDecimal lat = location.optBigDecimal("Latitude",null);
			BigDecimal lon = location.optBigDecimal("Longitude",null);


		core.getDataIdentification().addGeographicBoundingBox(lat,lon,lat,lon);
	    }
	}

	//
	// Temporal extent: when available, use the first/last observation dates
	// computed at connector level; otherwise fall back to a placeholder extent to
	// mark it as a time series.
	//
	String firstObsDate = json.optString("firstObservationDate", null);
	String firstObsTime = json.optString("firstObservationTime", null);
	String lastObsDate = json.optString("lastObservationDate", null);
	String lastObsTime = json.optString("lastObservationTime", null);

	TemporalExtent tempExtent = new TemporalExtent();
	if (firstObsDate != null && !firstObsDate.isEmpty()) {
	    String begin = firstObsDate;
	    if (firstObsTime != null && !firstObsTime.isEmpty()) {
		begin = firstObsDate + "T" + firstObsTime;
	    }
	    tempExtent.setBeginPosition(begin);
	}
	if (lastObsDate != null && !lastObsDate.isEmpty()) {
	    String end = lastObsDate;
	    if (lastObsTime != null && !lastObsTime.isEmpty()) {
		end = lastObsDate + "T" + lastObsTime;
	    }
	    tempExtent.setEndPosition(end);
	}
	mi.getDataIdentification().addTemporalExtent(tempExtent);
	setIndeterminatePosition(dataset);
	//
	// Reference system (WGS84)
	//
	ReferenceSystem referenceSystem = new ReferenceSystem();
	referenceSystem.setCode("EPSG:4326");
	referenceSystem.setCodeSpace("EPSG");
	mi.addReferenceSystemInfo(referenceSystem);

	//
	// Grid spatial representation
	//
	GridSpatialRepresentation grid = new GridSpatialRepresentation();
	grid.setNumberOfDimensions(1);
	grid.setCellGeometryCode("point");
	mi.addGridSpatialRepresentation(grid);

	//
	// Coverage description: use CharacteristicName as attribute id/title
	//
	CoverageDescription coverageDescription = new CoverageDescription();
	if (characteristicName != null) {
	    coverageDescription.setAttributeIdentifier(characteristicName);
	    coverageDescription.setAttributeTitle(characteristicName);
	}
	mi.addCoverageDescription(coverageDescription);

	//
	// Distribution and extension handler: attribute units from observations
	//
	ExtensionHandler extensionHandler = dataset.getExtensionHandler();
	String resultUnit = json.optString("resultUnit", null);
	if (resultUnit != null && !resultUnit.isEmpty()) {
	    extensionHandler.setAttributeUnits(resultUnit);
	    extensionHandler.setAttributeUnitsAbbreviation(resultUnit);
	}
	// Time resolution is not inferred here; it can be filled by higher-level logic if needed.

	//
	// Online resource and resource identifier using the mangler
	//
	DataStreamIdentifierMangler mangler = new DataStreamIdentifierMangler();
	if (doi != null) {
	    mangler.setDoi(doi);
	} else if (datasetId != null) {
	    mangler.setDoi(datasetId);
	}
	if (location != null) {
	    mangler.setLocationId(String.valueOf(location.optInt("Id")));
	}
	if (characteristicName != null) {
	    mangler.setCharacteristicName(characteristicName);
	}

	String identifier = mangler.getMangling();
	String dataUrl = dataset.getSource().getEndpoint();
	core.addDistributionOnlineResource(identifier, dataUrl, CommonNameSpaceContext.DATASTREAM_NS_URI, "download");

	core.getDataIdentification().setResourceIdentifier(identifier);

	// Generate additional code-based identifier, similar to other accessors
	String code = generateCode(dataset, identifier);
	core.getDataIdentification().setResourceIdentifier(code);
	if (mi.getDistribution() != null && mi.getDistribution().getDistributionOnline() != null) {
	    mi.getDistribution().getDistributionOnline().setIdentifier(code);
	}

	//
	// Language and character set
	//
	mi.setLanguage("English");
	mi.setCharacterSetCode("utf8");
	mi.addHierarchyLevelScopeCodeListValue("dataset");
    }
}

