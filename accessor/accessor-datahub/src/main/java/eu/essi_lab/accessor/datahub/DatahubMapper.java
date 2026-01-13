package eu.essi_lab.accessor.datahub;

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

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.Format;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ReferenceSystem;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.model.resource.ResourceType;
import eu.essi_lab.ommdk.FileIdentifierMapper;

/**
 * Mapper for DataHub JSON metadata to GSResource
 * 
 * @author Generated
 */
public class DatahubMapper extends FileIdentifierMapper {

    public static final String DATAHUB_NS_URI = "http://essi-lab.eu/datahub";

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return DATAHUB_NS_URI;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	String originalMetadata = originalMD.getMetadata();
	JSONObject json = new JSONObject(originalMetadata);

	// Determine resource type based on hierarchy_level
	String hierarchyLevel = json.optString("hierarchy_level", "dataset");
	GSResource resource;

	switch (hierarchyLevel.toLowerCase()) {
	case "series":
	    resource = new DatasetCollection();
	    break;
	case "model":
	    // TODO: Model resource type not yet supported in GSResource hierarchy
	    // For now, map to Dataset
	    resource = new Dataset();
	    logger.warn("Model hierarchy level mapped to Dataset - extension needed for Model resource type");
	    break;
	case "dataset":
	default:
	    resource = new Dataset();
	    break;
	}

	resource.setSource(source);
	CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

	// Map basic identification
	mapBasicIdentification(json, coreMetadata, resource);

	// Map responsible parties
	mapResponsibleParties(json, coreMetadata);

	// Map keywords
	mapKeywords(json, coreMetadata);

	// Map spatial information
	mapSpatialInformation(json, coreMetadata);

	// Map temporal information
	mapTemporalInformation(json, coreMetadata);

	// Map constraints
	mapConstraints(json, coreMetadata);

	// Map distribution
	mapDistribution(json, coreMetadata);

	// Map quality information
	mapQualityInformation(json, coreMetadata);

	// Map model-specific fields (if hierarchy_level is model)
	if ("model".equalsIgnoreCase(hierarchyLevel)) {
	    mapModelSpecificFields(json, coreMetadata, resource);
	}

	// Map dataset-specific fields
	if ("dataset".equalsIgnoreCase(hierarchyLevel) || "series".equalsIgnoreCase(hierarchyLevel)) {
	    mapDatasetSpecificFields(json, coreMetadata, resource);
	}

	return resource;
    }

    /**
     * Maps basic identification information
     */
    private void mapBasicIdentification(JSONObject json, CoreMetadata coreMetadata, GSResource resource) {
	// Identifier
	String id = json.optString("id", null);
	if (id != null) {
	    coreMetadata.setIdentifier(id);
	    coreMetadata.getMIMetadata().setFileIdentifier(id);
	}

	// Title
	String title = json.optString("title", null);
	if (title != null) {
	    coreMetadata.setTitle(title);
	}

	// Abstract
	String abstract_ = json.optString("abstract", null);
	if (abstract_ != null) {
	    coreMetadata.setAbstract(abstract_);
	}

	// Character set
	String characterSet = json.optString("character_set", null);
	if (characterSet != null) {
	    coreMetadata.getMIMetadata().setCharacterSetCode(characterSet);
	}

	// Metadata language
	String metadataLanguage = json.optString("metadata_language", null);
	if (metadataLanguage != null) {
	    coreMetadata.getMIMetadata().setLanguage(metadataLanguage);
	}

	// Hierarchy level
	String hierarchyLevel = json.optString("hierarchy_level", "dataset");
	coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue(hierarchyLevel);

	// Metadata date stamp
	String metadataUpdatedAt = json.optString("metadata_updated_at", null);
	if (metadataUpdatedAt != null) {
	    try {
		// Try to parse as date
		coreMetadata.getMIMetadata().setDateStampAsDate(metadataUpdatedAt);
	    } catch (Exception e) {
		logger.warn("Error parsing metadata_updated_at: {}", metadataUpdatedAt, e);
	    }
	}

	// Metadata profile
	JSONObject metadataProfile = json.optJSONObject("metadata_profile");
	if (metadataProfile != null) {
	    String profileName = metadataProfile.optString("name", null);
	    String profileVersion = metadataProfile.optString("version", null);
	    if (profileName != null) {
		coreMetadata.getMIMetadata().setMetadataStandardName(profileName);
	    }
	    if (profileVersion != null) {
		coreMetadata.getMIMetadata().setMetadataStandardVersion(profileVersion);
	    }
	}

	// Metadata version and original version
	// TODO: metadata_version and metadata_original_version - no direct ISO 19115 mapping
	// These should be added as extension elements
	String metadataVersion = json.optString("metadata_version", null);
	String metadataOriginalVersion = json.optString("metadata_original_version", null);
	if (metadataVersion != null || metadataOriginalVersion != null) {
	    logger.debug("TODO: Add extension elements for metadata_version and metadata_original_version");
	}

	// Resource dates
	String resourceCreationDate = json.optString("resource_creation_date", null);
	if (resourceCreationDate != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationCreationDate(resourceCreationDate);
	}

	String resourcePublicationDate = json.optString("resource_publication_date", null);
	if (resourcePublicationDate != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationPublicationDate(resourcePublicationDate);
	}

	String resourceRevisionDate = json.optString("resource_revision_date", null);
	if (resourceRevisionDate != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationRevisionDate(resourceRevisionDate);
	}

	String resourceExpirationDate = json.optString("resource_expiration_date", null);
	if (resourceExpirationDate != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationExpiryDate(resourceExpirationDate);
	}

	// Edition
	String edition = json.optString("edition", null);
	if (edition != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationEdition(edition);
	}

	String editionDate = json.optString("edition_date", null);
	if (editionDate != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationEditionDate(editionDate);
	}

	// Presentation form
	String presentationForm = json.optString("presentation_form", null);
	if (presentationForm != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationPresentationForm(presentationForm);
	}

	// Graphic overview
	JSONObject graphicOverview = json.optJSONObject("graphic_overview");
	if (graphicOverview != null) {
	    String graphicUrl = graphicOverview.optString("url", null);
	    String graphicDescription = graphicOverview.optString("description", null);
	    if (graphicUrl != null) {
		coreMetadata.getMIMetadata().getDataIdentification().addBrowseGraphic(graphicUrl, graphicDescription);
	    }
	}

	// Status
	String status = json.optString("status", null);
	if (status != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().setStatus(status);
	}

	// Update frequency
	String updateFrequency = json.optString("update_frequency", null);
	if (updateFrequency != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().setMaintenanceAndUpdateFrequency(updateFrequency);
	}

	String dateOfNextUpdate = json.optString("date_of_next_update", null);
	if (dateOfNextUpdate != null && !"null".equals(dateOfNextUpdate)) {
	    coreMetadata.getMIMetadata().getDataIdentification().setDateOfNextUpdate(dateOfNextUpdate);
	}

	// Data language
	String dataLanguage = json.optString("data_language", null);
	if (dataLanguage != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().setLanguage(dataLanguage);
	}

	// Topic categories
	JSONArray topicCategories = json.optJSONArray("topic_categories");
	if (topicCategories != null) {
	    for (int i = 0; i < topicCategories.length(); i++) {
		String topicCategory = topicCategories.optString(i, null);
		if (topicCategory != null) {
		    coreMetadata.getMIMetadata().getDataIdentification().addTopicCategory(topicCategory);
		}
	    }
	}
    }

    /**
     * Maps responsible parties
     */
    private void mapResponsibleParties(JSONObject json, CoreMetadata coreMetadata) {
	// Metadata owner (pointOfContact)
	JSONObject metadataOwner = json.optJSONObject("metadata_owner");
	if (metadataOwner != null) {
	    ResponsibleParty party = createResponsibleParty(metadataOwner);
	    if (party != null) {
		coreMetadata.getMIMetadata().setContact(party);
	    }
	}

	// Resource owner
	JSONObject resourceOwner = json.optJSONObject("resource_owner");
	if (resourceOwner != null) {
	    ResponsibleParty party = createResponsibleParty(resourceOwner);
	    if (party != null) {
		coreMetadata.getMIMetadata().getDataIdentification().addCitedResponsibleParty(party);
	    }
	}

	// Point of contact
	JSONObject pointOfContact = json.optJSONObject("point_of_contact");
	if (pointOfContact != null) {
	    ResponsibleParty party = createResponsibleParty(pointOfContact);
	    if (party != null) {
		coreMetadata.getMIMetadata().getDataIdentification().setPointOfContact(party);
	    }
	}

	// Other roles
	JSONArray otherRoles = json.optJSONArray("other_roles");
	if (otherRoles != null) {
	    for (int i = 0; i < otherRoles.length(); i++) {
		JSONObject roleObj = otherRoles.optJSONObject(i);
		if (roleObj != null) {
		    ResponsibleParty party = createResponsibleParty(roleObj);
		    if (party != null) {
			coreMetadata.getMIMetadata().getDataIdentification().addCitedResponsibleParty(party);
		    }
		}
	    }
	}

	// Resource provider
	JSONObject resourceProvider = json.optJSONObject("resource_provider");
	if (resourceProvider != null) {
	    ResponsibleParty party = createResponsibleParty(resourceProvider);
	    if (party != null) {
		coreMetadata.getMIMetadata().getDistribution().addDistributorContact(party);
	    }
	}
    }

    /**
     * Creates a ResponsibleParty from JSON object
     */
    private ResponsibleParty createResponsibleParty(JSONObject json) {
	ResponsibleParty party = new ResponsibleParty();

	String organizationName = json.optString("organization_name", null);
	if (organizationName != null) {
	    party.setOrganisationName(organizationName);
	}

	String individualName = json.optString("individual_name", null);
	if (individualName != null) {
	    party.setIndividualName(individualName);
	}

	String role = json.optString("role", null);
	if (role != null) {
	    party.setRoleCode(role);
	}

	// Contact information
	JSONObject address = json.optJSONObject("address");
	Contact contact = new Contact();
	Address addr = new Address();

	if (address != null) {
	    String email = address.optString("email", null);
	    if (email != null) {
		addr.addElectronicMailAddress(email);
	    }

	    String deliveryPoint = address.optString("delivery_point", null);
	    if (deliveryPoint != null) {
		addr.addDeliveryPoint(deliveryPoint);
	    }

	    String city = address.optString("city", null);
	    if (city != null) {
		addr.setCity(city);
	    }

	    String administrativeArea = address.optString("administrative_area", null);
	    if (administrativeArea != null) {
		addr.setAdministrativeArea(administrativeArea);
	    }

	    String postalCode = address.optString("postal_code", null);
	    if (postalCode != null) {
		addr.setPostalCode(postalCode);
	    }

	    String country = address.optString("country", null);
	    if (country != null) {
		addr.setCountry(country);
	    }
	}

	String link = json.optString("link", null);
	if (link != null) {
	    Online onlineResource = new Online();
	    onlineResource.setLinkage(link);
	    contact.setOnlineResource(onlineResource);
	}

	if (addr.getElectronicMailAddresses().hasNext() || link != null) {
	    contact.setAddress(addr);
	    party.setContactInfo(contact);
	}

	return party;
    }

    /**
     * Maps keywords
     */
    private void mapKeywords(JSONObject json, CoreMetadata coreMetadata) {
	JSONArray descriptiveKeywords = json.optJSONArray("descriptive_keywords");
	if (descriptiveKeywords != null) {
	    for (int i = 0; i < descriptiveKeywords.length(); i++) {
		JSONObject keywordGroup = descriptiveKeywords.optJSONObject(i);
		if (keywordGroup != null) {
		    Keywords keywords = new Keywords();

		    JSONArray keywordsArray = keywordGroup.optJSONArray("keywords");
		    if (keywordsArray != null) {
			for (int j = 0; j < keywordsArray.length(); j++) {
			    JSONObject keywordObj = keywordsArray.optJSONObject(j);
			    if (keywordObj != null) {
				String label = keywordObj.optString("label", null);
				String uri = keywordObj.optString("uri", null);
				if (label != null) {
				    if (uri != null) {
					keywords.addKeyword(label, uri);
				    } else {
					keywords.addKeyword(label);
				    }
				}
			    }
			}
		    }

		    // Thesaurus
		    JSONObject thesaurus = keywordGroup.optJSONObject("thesaurus");
		    if (thesaurus != null) {
			String thesaurusName = thesaurus.optString("name", null);
			String thesaurusUrl = thesaurus.optString("url", null);
			String publicationDate = thesaurus.optString("publication_date", null);
			String revisionDate = thesaurus.optString("revision_date", null);

			Citation thesaurusCitation = new Citation();
			if (thesaurusName != null) {
			    thesaurusCitation.setTitle(thesaurusName);
			}
			if (thesaurusUrl != null) {
			    thesaurusCitation.setTitleAnchor(thesaurusUrl, thesaurusName);
			}
			if (publicationDate != null) {
			    thesaurusCitation.setPublicationDate(publicationDate);
			}
			if (revisionDate != null) {
			    thesaurusCitation.setRevisionDate(revisionDate);
			}
			keywords.setThesaurusCitation(thesaurusCitation);
		    }

		    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keywords);
		}
	    }
	}

	// Spatial scope
	JSONObject spatialScope = json.optJSONObject("spatial_scope");
	if (spatialScope != null) {
	    Keywords keywords = new Keywords();
	    JSONObject keywordsObj = spatialScope.optJSONObject("keywords");
	    if (keywordsObj != null) {
		String label = keywordsObj.optString("label", null);
		String uri = keywordsObj.optString("uri", null);
		if (label != null) {
		    if (uri != null) {
			keywords.addKeyword(label, uri);
		    } else {
			keywords.addKeyword(label);
		    }
		}
	    }
	    JSONObject thesaurus = spatialScope.optJSONObject("thesaurus");
	    if (thesaurus != null) {
		Citation thesaurusCitation = new Citation();
		thesaurusCitation.setTitle("Spatial scope");
		String url = thesaurus.optString("url", null);
		if (url != null) {
		    thesaurusCitation.setTitleAnchor(url, "Spatial scope");
		}
		String publicationDate = thesaurus.optString("publication_date", null);
		if (publicationDate != null) {
		    thesaurusCitation.setPublicationDate(publicationDate);
		}
		keywords.setThesaurusCitation(thesaurusCitation);
	    }
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keywords);
	}

	// INSPIRE priority dataset
	JSONObject inspirePriorityDataset = json.optJSONObject("inspire_priority_dataset");
	if (inspirePriorityDataset != null) {
	    Keywords keywords = new Keywords();
	    JSONObject keywordsObj = inspirePriorityDataset.optJSONObject("keywords");
	    if (keywordsObj != null) {
		String label = keywordsObj.optString("label", null);
		String uri = keywordsObj.optString("uri", null);
		if (label != null) {
		    if (uri != null) {
			keywords.addKeyword(label, uri);
		    } else {
			keywords.addKeyword(label);
		    }
		}
	    }
	    JSONObject thesaurus = inspirePriorityDataset.optJSONObject("thesaurus");
	    if (thesaurus != null) {
		Citation thesaurusCitation = new Citation();
		thesaurusCitation.setTitle("INSPIRE priority data set");
		String url = thesaurus.optString("url", null);
		if (url != null) {
		    thesaurusCitation.setTitleAnchor(url, "INSPIRE priority data set");
		}
		String publicationDate = thesaurus.optString("publication_date", null);
		if (publicationDate != null) {
		    thesaurusCitation.setPublicationDate(publicationDate);
		}
		keywords.setThesaurusCitation(thesaurusCitation);
	    }
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keywords);
	}
    }

    /**
     * Maps spatial information
     */
    private void mapSpatialInformation(JSONObject json, CoreMetadata coreMetadata) {
	// Bounding box (EPSG:4326)
	JSONObject bbox4326 = json.optJSONObject("bbox_4326");
	if (bbox4326 != null) {
	    Double west = bbox4326.optDouble("west_bound_longitude", Double.NaN);
	    Double east = bbox4326.optDouble("east_bound_longitude", Double.NaN);
	    Double south = bbox4326.optDouble("south_bound_latitude", Double.NaN);
	    Double north = bbox4326.optDouble("north_bound_latitude", Double.NaN);

	    if (!west.isNaN() && !east.isNaN() && !south.isNaN() && !north.isNaN()) {
		coreMetadata.addBoundingBox(north, west, south, east);
	    }
	}

	// Native EPSG
	JSONObject nativeEpsg = json.optJSONObject("native_epsg");
	if (nativeEpsg != null) {
	    String url = nativeEpsg.optString("url", null);
	    String label = nativeEpsg.optString("label", null);
	    if (url != null) {
		ReferenceSystem refSystem = new ReferenceSystem();
		refSystem.setCode(url);
		refSystem.setCodeSpace("EPSG");
		coreMetadata.getMIMetadata().addReferenceSystemInfo(refSystem);
	    }
	}

	// Bounding polygon in native EPSG
	// TODO: bbox_native_epsg_gml_polygon - no direct ISO 19115 mapping
	// Should be added as extension element
	String bboxNativeEpsgGmlPolygon = json.optString("bbox_native_epsg_gml_polygon", null);
	if (bboxNativeEpsgGmlPolygon != null) {
	    logger.debug("TODO: Add extension element for bbox_native_epsg_gml_polygon");
	}

	// Vertical extent
	JSONObject verticalExtent = json.optJSONObject("vertical_extent");
	if (verticalExtent != null) {
	    Double minValue = verticalExtent.optDouble("minimum_value", Double.NaN);
	    Double maxValue = verticalExtent.optDouble("maximum_value", Double.NaN);
	    String crs = verticalExtent.optString("crs", null);

	    if (!minValue.isNaN() && !maxValue.isNaN()) {
		coreMetadata.getMIMetadata().getDataIdentification().addVerticalExtent(minValue, maxValue, crs);
	    }
	}
    }

    /**
     * Maps temporal information
     */
    private void mapTemporalInformation(JSONObject json, CoreMetadata coreMetadata) {
	JSONObject temporalExtent = json.optJSONObject("temporal_extent");
	if (temporalExtent != null) {
	    String startDate = temporalExtent.optString("start_date", null);
	    String endDate = temporalExtent.optString("end_date", null);

	    if (startDate != null || endDate != null) {
		coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(startDate, endDate);
	    }
	}
    }

    /**
     * Maps constraints
     */
    private void mapConstraints(JSONObject json, CoreMetadata coreMetadata) {
	JSONObject resourceConstraints = json.optJSONObject("resource_constraints");
	if (resourceConstraints != null) {
	    LegalConstraints legalConstraints = new LegalConstraints();

	    String useLimitation = resourceConstraints.optString("use_limitation", null);
	    if (useLimitation != null) {
		legalConstraints.addUseLimitation(useLimitation);
	    }

	    String accessConstraints = resourceConstraints.optString("access_constraints", null);
	    if (accessConstraints != null) {
		legalConstraints.addAccessConstraintsCode(accessConstraints);
	    }

	    String useConstraints = resourceConstraints.optString("use_constraints", null);
	    if (useConstraints != null) {
		legalConstraints.addUseConstraintsCode(useConstraints);
	    }

	    String otherConstraints = resourceConstraints.optString("other_constraints", null);
	    if (otherConstraints != null) {
		legalConstraints.addOtherConstraints(otherConstraints);
	    }

	    // Security constraints
	    // TODO: security_classification and security_note - need to use MD_SecurityConstraints
	    // This requires proper ISO 19115-2 SecurityConstraints handling
	    String securityClassification = resourceConstraints.optString("security_classification", null);
	    String securityNote = resourceConstraints.optString("security_note", null);
	    if (securityClassification != null || (securityNote != null && !"null".equals(securityNote))) {
		logger.debug("TODO: Add security constraints (classification: {}, note: {})", securityClassification, securityNote);
	    }

	    coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(legalConstraints);
	}
    }

    /**
     * Maps distribution information
     */
    private void mapDistribution(JSONObject json, CoreMetadata coreMetadata) {
	// Formats
	JSONArray formats = json.optJSONArray("formats");
	if (formats != null) {
	    for (int i = 0; i < formats.length(); i++) {
		JSONObject formatObj = formats.optJSONObject(i);
		if (formatObj != null) {
		    Format format = new Format();
		    String name = formatObj.optString("name", null);
		    String version = formatObj.optString("version", null);
		    String specification = formatObj.optString("specification", null);

		    if (name != null) {
			format.setName(name);
		    }
		    if (version != null) {
			format.setVersion(version);
		    }
		    if (specification != null) {
			format.setSpecification(specification);
		    }

		    coreMetadata.getMIMetadata().getDistribution().addFormat(format);
		}
	    }
	}

	// Online resources
	JSONArray onlineResources = json.optJSONArray("online_resources");
	if (onlineResources != null) {
	    for (int i = 0; i < onlineResources.length(); i++) {
		JSONObject onlineResourceObj = onlineResources.optJSONObject(i);
		if (onlineResourceObj != null) {
		    Online online = new Online();

		    String url = onlineResourceObj.optString("url", null);
		    if (url != null) {
			online.setLinkage(url);
		    }

		    String name = onlineResourceObj.optString("name", null);
		    if (name != null) {
			online.setName(name);
		    }

		    String description = onlineResourceObj.optString("description", null);
		    if (description != null) {
			online.setDescription(description);
		    }

		    String function = onlineResourceObj.optString("function", null);
		    if (function != null) {
			online.setFunctionCode(function);
		    }

		    JSONObject protocol = onlineResourceObj.optJSONObject("protocol");
		    if (protocol != null) {
			String protocolCode = protocol.optString("code", null);
			String protocolName = protocol.optString("name", null);
			if (protocolCode != null) {
			    online.setProtocol(protocolCode);
			} else if (protocolName != null) {
			    online.setProtocol(protocolName);
			}
		    }

		    String applicationProfile = onlineResourceObj.optString("application_profile", null);
		    if (applicationProfile != null) {
			online.setApplicationProfile(applicationProfile);
		    }

		    // TODO: query_string_fragment, layer_pk, temporal_wms, layer_style, source_auth_type, source_auth_key
		    // These fields don't have direct ISO 19115 mappings and should be added as extension elements
		    String queryStringFragment = onlineResourceObj.optString("query_string_fragment", null);
		    String layerPk = onlineResourceObj.optString("layer_pk", null);
		    Boolean temporalWms = onlineResourceObj.optBoolean("temporal_wms", false);
		    JSONObject layerStyle = onlineResourceObj.optJSONObject("layer_style");
		    if (queryStringFragment != null || layerPk != null || temporalWms || layerStyle != null) {
			logger.debug("TODO: Add extension elements for online resource extensions (query_string_fragment, layer_pk, temporal_wms, layer_style)");
		    }

		    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
		}
	    }
	}

	// TODO: dataset_location, raster_mosaic - no direct ISO 19115 mapping
	// These should be added as extension elements
	String datasetLocation = json.optString("dataset_location", null);
	Boolean rasterMosaic = json.optBoolean("raster_mosaic", false);
	if (datasetLocation != null || rasterMosaic) {
	    logger.debug("TODO: Add extension elements for dataset_location and raster_mosaic");
	}
    }

    /**
     * Maps quality information
     */
    private void mapQualityInformation(JSONObject json, CoreMetadata coreMetadata) {
	// Conformity
	JSONArray conformity = json.optJSONArray("conformity");
	if (conformity != null) {
	    for (int i = 0; i < conformity.length(); i++) {
		JSONObject conformityObj = conformity.optJSONObject(i);
		if (conformityObj != null) {
		    String specificationTitle = conformityObj.optString("specification_title", null);
		    String specificationPublicationDate = conformityObj.optString("specification_publication_date", null);
		    String explanation = conformityObj.optString("explanation", null);
		    Boolean pass = conformityObj.optBoolean("pass", false);

		    Citation specificationCitation = new Citation();
		    if (specificationTitle != null) {
			specificationCitation.setTitle(specificationTitle);
		    }
		    if (specificationPublicationDate != null) {
			specificationCitation.setPublicationDate(specificationPublicationDate);
		    }

		    coreMetadata.getMIMetadata().getDataQualityInfo().addConformanceResult(specificationCitation, explanation, pass);
		}
	    }
	}

	// Positional accuracy
	JSONObject positionalAccuracy = json.optJSONObject("positional_accuracy");
	if (positionalAccuracy != null) {
	    String unit = positionalAccuracy.optString("unit", null);
	    String unitSystem = positionalAccuracy.optString("unit_system", null);
	    Double value = positionalAccuracy.optDouble("value", Double.NaN);

	    if (!value.isNaN()) {
		coreMetadata.getMIMetadata().getDataQualityInfo().addAbsoluteExternalPositionalAccuracy(value, unit, unitSystem);
	    }
	}

	// Quality scope
	JSONObject qualityScope = json.optJSONObject("quality_scope");
	if (qualityScope != null) {
	    String scopeCode = qualityScope.optString("scope_code", null);
	    String scopeDetail = qualityScope.optString("scope_detail", null);
	    if (scopeCode != null) {
		coreMetadata.getMIMetadata().getDataQualityInfo().setScope(scopeCode, scopeDetail);
	    }
	}

	// Lineage statement
	String lineageStatement = json.optString("lineage_statement", null);
	if (lineageStatement != null) {
	    coreMetadata.getMIMetadata().getDataQualityInfo().setLineageStatement(lineageStatement);
	}

	// Lineage source
	JSONArray lineageSource = json.optJSONArray("lineage_source");
	if (lineageSource != null) {
	    for (int i = 0; i < lineageSource.length(); i++) {
		JSONObject sourceObj = lineageSource.optJSONObject(i);
		if (sourceObj != null) {
		    String description = sourceObj.optString("description", null);
		    JSONObject sourceCitation = sourceObj.optJSONObject("source_citation");
		    if (sourceCitation != null) {
			Citation citation = new Citation();
			String title = sourceCitation.optString("title", null);
			if (title != null) {
			    citation.setTitle(title);
			}
			JSONArray onlineResources = sourceCitation.optJSONArray("online_resource");
			if (onlineResources != null) {
			    for (int j = 0; j < onlineResources.length(); j++) {
				JSONObject onlineResourceObj = onlineResources.optJSONObject(j);
				if (onlineResourceObj != null) {
				    String url = onlineResourceObj.optString("url", null);
				    if (url != null) {
					citation.addOnlineResource(url);
				    }
				}
			    }
			}
			coreMetadata.getMIMetadata().getDataQualityInfo().addLineageSource(citation, description);
		    }
		}
	    }
	}

	// Lineage process step
	JSONArray lineageProcessStep = json.optJSONArray("lineage_process_step");
	if (lineageProcessStep != null) {
	    for (int i = 0; i < lineageProcessStep.length(); i++) {
		JSONObject processStepObj = lineageProcessStep.optJSONObject(i);
		if (processStepObj != null) {
		    String description = processStepObj.optString("description", null);
		    String rationale = processStepObj.optString("rationale", null);
		    String date = processStepObj.optString("date", null);
		    JSONObject processor = processStepObj.optJSONObject("processor");
		    JSONObject source = processStepObj.optJSONObject("source");
		    JSONObject processingInformation = processStepObj.optJSONObject("processing_information");
		    JSONObject output = processStepObj.optJSONObject("output");
		    JSONObject report = processStepObj.optJSONObject("report");

		    // TODO: Complete lineage process step mapping - this is complex and may require
		    // additional ISO 19115-2 extensions
		    logger.debug("TODO: Complete lineage_process_step mapping with all sub-elements");
		}
	    }
	}
    }

    /**
     * Maps model-specific fields
     */
    private void mapModelSpecificFields(JSONObject json, CoreMetadata coreMetadata, GSResource resource) {
	// TODO: model_maturity_level - no direct ISO 19115 mapping
	String modelMaturityLevel = json.optString("model_maturity_level", null);
	if (modelMaturityLevel != null) {
	    logger.debug("TODO: Add extension element for model_maturity_level");
	}

	// TODO: model_computational_requirements - no direct ISO 19115 mapping
	JSONObject modelComputationalRequirements = json.optJSONObject("model_computational_requirements");
	if (modelComputationalRequirements != null) {
	    logger.debug("TODO: Add extension elements for model_computational_requirements (cpu, gpu, ram, storage)");
	}

	// TODO: model_types - no direct ISO 19115 mapping
	JSONArray modelTypes = json.optJSONArray("model_types");
	if (modelTypes != null) {
	    logger.debug("TODO: Add extension element for model_types");
	}

	// TODO: supported_platforms - no direct ISO 19115 mapping
	JSONArray supportedPlatforms = json.optJSONArray("supported_platforms");
	if (supportedPlatforms != null) {
	    logger.debug("TODO: Add extension element for supported_platforms");
	}

	// TODO: model_category - no direct ISO 19115 mapping
	String modelCategory = json.optString("model_category", null);
	if (modelCategory != null) {
	    logger.debug("TODO: Add extension element for model_category");
	}

	// TODO: model_methodology_description - no direct ISO 19115 mapping
	String modelMethodologyDescription = json.optString("model_methodology_description", null);
	if (modelMethodologyDescription != null) {
	    logger.debug("TODO: Add extension element for model_methodology_description");
	}

	// Model quality information
	JSONObject modelQualityInformation = json.optJSONObject("model_quality_information");
	if (modelQualityInformation != null) {
	    String modelQualityReport = modelQualityInformation.optString("model_quality_report", null);
	    JSONArray modelMetrics = modelQualityInformation.optJSONArray("model_metrics");

	    if (modelQualityReport != null) {
		coreMetadata.getMIMetadata().getDataQualityInfo().addDescriptiveResult(modelQualityReport);
	    }

	    if (modelMetrics != null) {
		for (int i = 0; i < modelMetrics.length(); i++) {
		    JSONObject metricObj = modelMetrics.optJSONObject(i);
		    if (metricObj != null) {
			String metricId = metricObj.optString("id", null);
			String metricName = metricObj.optString("name", null);
			String metricDescription = metricObj.optString("description", null);
			Double metricValue = metricObj.optDouble("value", Double.NaN);

			if (metricName != null && !metricValue.isNaN()) {
			    // TODO: Metric values are adimensional - may need special handling
			    coreMetadata.getMIMetadata().getDataQualityInfo().addQuantitativeAttributeAccuracy(metricName, metricDescription, metricValue, null);
			}
		    }
		}
	    }
	}
    }

    /**
     * Maps dataset-specific fields
     */
    private void mapDatasetSpecificFields(JSONObject json, CoreMetadata coreMetadata, GSResource resource) {
	// Dataset type
	String datasetType = json.optString("dataset_type", null);
	if (datasetType != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().setSpatialRepresentationType(datasetType);
	}

	// Spatial resolution
	Double spatialResolution = json.optDouble("spatial_resolution", Double.NaN);
	if (!spatialResolution.isNaN()) {
	    coreMetadata.getMIMetadata().getDataIdentification().setSpatialResolution(spatialResolution);
	}

	// Equivalent scale
	Double equivalentScale = json.optDouble("equivalent_scale", Double.NaN);
	if (!equivalentScale.isNaN()) {
	    coreMetadata.getMIMetadata().getDataIdentification().setEquivalentScale(equivalentScale.intValue());
	}

	// Raster nodata value
	// TODO: raster_nodata_value - no direct ISO 19115 mapping
	Double rasterNodataValue = json.optDouble("raster_nodata_value", Double.NaN);
	if (!rasterNodataValue.isNaN()) {
	    logger.debug("TODO: Add extension element for raster_nodata_value");
	}

	// Parent identifier
	String parentIdentifier = json.optString("parent_identifier", null);
	if (parentIdentifier != null) {
	    coreMetadata.getMIMetadata().setParentIdentifier(parentIdentifier);
	}
    }

    @Override
    protected String createOriginalIdentifier(GSResource resource) {
	// Use the id field from the original metadata as the original identifier
	return resource.getPublicId();
    }
}

