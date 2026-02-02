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

import eu.essi_lab.iso.datamodel.*;
import eu.essi_lab.iso.datamodel.classes.*;
import eu.essi_lab.jaxb.common.*;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.model.resource.*;
import eu.essi_lab.ommdk.*;
import net.opengis.gml.v_3_2_0.CodeType;
import net.opengis.iso19139.gco.v_20060504.*;
import net.opengis.iso19139.gmd.v_20060504.*;
import net.opengis.iso19139.srv.v_20060504.*;
import org.apache.cxf.common.jaxb.JAXBUtils;
import org.json.*;
import org.slf4j.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.*;
import java.io.File;
import java.math.*;
import java.util.*;
import java.util.Date;

/**
 * Mapper for DataHub JSON metadata to GSResource
 *
 * @author Generated
 */
public class DatahubMapper extends FileIdentifierMapper {

    public static final String DATAHUB_NS_URI = "http://essi-lab.eu/datahub";

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    /**
     * Resolves hierarchy level from JSON. Supports v3.8 object form { uri, value } or string.
     *
     * @param json the metadata JSON
     * @return "dataset", "model", "series", or "dataset" as default
     */
    private static String resolveHierarchyLevel(JSONObject json) {
        Object hierarchyLevelObj = json.opt("hierarchy_level");
        if (hierarchyLevelObj == null) {
            return "dataset";
        }
        if (hierarchyLevelObj instanceof JSONObject) {
            String value = ((JSONObject) hierarchyLevelObj).optString("value", null);
            return (value != null && !value.isEmpty()) ? value : "dataset";
        }
        if (hierarchyLevelObj instanceof String) {
            String s = (String) hierarchyLevelObj;
            return (s != null && !s.isEmpty()) ? s : "dataset";
        }
        return "dataset";
    }

    /**
     * Returns the identification block to use for mapping (DataIdentification or ServiceIdentification).
     */
    private Identification getIdentification(CoreMetadata coreMetadata, String hierarchyLevel) {
        if ("service".equalsIgnoreCase(hierarchyLevel)) {
            ServiceIdentification svc = coreMetadata.getMIMetadata().getServiceIdentification();
            if (svc != null) {
                return svc;
            }
        }
        return coreMetadata.getMIMetadata().getDataIdentification();
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {
        return DATAHUB_NS_URI;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

        String originalMetadata = originalMD.getMetadata();
        JSONObject json = new JSONObject(originalMetadata);

        // Determine resource type based on hierarchy_level (v3.8: can be object with uri/value)
        String hierarchyLevel = resolveHierarchyLevel(json);
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
            case "service":
                // Service mapped to Dataset; identification will be SV_ServiceIdentification
                resource = new Dataset();
                logger.debug("Service hierarchy level mapped to Dataset with SV_ServiceIdentification");
                break;
            case "dataset":
            default:
                resource = new Dataset();
                break;
        }

        resource.setSource(source);
        CoreMetadata coreMetadata = resource.getHarmonizedMetadata().getCoreMetadata();

        // For services, use ServiceIdentification instead of DataIdentification
        if ("service".equalsIgnoreCase(hierarchyLevel)) {
            coreMetadata.getMIMetadata().clearDataIdentifications();
            coreMetadata.getMIMetadata().addServiceIdentification(new ServiceIdentification());
        }

        // Map basic identification
        mapBasicIdentification(json, coreMetadata, resource, hierarchyLevel);

        // Map responsible parties
        mapResponsibleParties(json, coreMetadata, hierarchyLevel);

        // Map keywords
        mapKeywords(json, coreMetadata, hierarchyLevel);

        // Map spatial information
        mapSpatialInformation(json, coreMetadata, hierarchyLevel);

        // Map temporal information
        mapTemporalInformation(json, coreMetadata, hierarchyLevel);

        // Map constraints
        mapConstraints(json, coreMetadata, hierarchyLevel);

        // Map distribution
        mapDistribution(json, coreMetadata, resource, hierarchyLevel);

        // Map quality information
        mapQualityInformation(json, coreMetadata, hierarchyLevel);

        // Map model-specific fields (if hierarchy_level is model)
        if ("model".equalsIgnoreCase(hierarchyLevel)) {
            mapModelSpecificFields(json, coreMetadata, resource);
        }

        // Map service-specific fields (if hierarchy_level is service)
        if ("service".equalsIgnoreCase(hierarchyLevel)) {
            mapServiceSpecificFields(json, coreMetadata);
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
    private void mapBasicIdentification(JSONObject json, CoreMetadata coreMetadata, GSResource resource, String hierarchyLevel) {
        Identification identification = getIdentification(coreMetadata, hierarchyLevel);
        // Identifier (v3.6+: renamed from "id" to "identifier")
        String identifier = json.optString("identifier", null);
        // Backward compatibility: also check "id" for older versions
        if (identifier == null) {
            identifier = json.optString("id", null);
        }
        if (identifier != null) {
            coreMetadata.setIdentifier(identifier);
            coreMetadata.getMIMetadata().setFileIdentifier(identifier);
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

        // Character set (v3.8+: JSON object with uri and value)
        Object characterSetObj = json.opt("character_set");
        if (characterSetObj != null) {
            if (characterSetObj instanceof JSONObject) {
                JSONObject characterSetJson = (JSONObject) characterSetObj;
                String uri = characterSetJson.optString("uri", null);
                String value = characterSetJson.optString("value", null);
                if (value != null) {
                    // Use custom URI if provided, otherwise use default
                    String codeListUri = (uri != null && !uri.isEmpty()) ? uri : ISOMetadata.MD_CHARACTER_SET_CODE_CODELIST;
                    MDCharacterSetCodePropertyType propertyType = new MDCharacterSetCodePropertyType();
                    CodeListValueType codeListValue = ISOMetadata.createCodeListValueType(codeListUri, value, ISOMetadata.ISO_19115_CODESPACE, value);
                    propertyType.setMDCharacterSetCode(codeListValue);
                    coreMetadata.getMIMetadata().getElementType().setCharacterSet(propertyType);
                }
            } else if (characterSetObj instanceof String) {
                // Backward compatibility: string value
                coreMetadata.getMIMetadata().setCharacterSetCode((String) characterSetObj);
            }
        }

        // Metadata language
        String metadataLanguage = json.optString("metadata_language", null);
        if (metadataLanguage != null) {
            coreMetadata.getMIMetadata().setLanguage(metadataLanguage);
        }

        // Hierarchy level (v3.8+: JSON object with uri and value)
        Object hierarchyLevelObj = json.opt("hierarchy_level");
        if (hierarchyLevelObj != null) {
            if (hierarchyLevelObj instanceof JSONObject) {
                JSONObject hierarchyLevelJson = (JSONObject) hierarchyLevelObj;
                String uri = hierarchyLevelJson.optString("uri", null);
                String value = hierarchyLevelJson.optString("value", "dataset");
                if (value != null) {
                    // Use custom URI if provided, otherwise use default
                    String codeListUri = (uri != null && !uri.isEmpty()) ? uri : ISOMetadata.MD_SCOPE_CODE_CODELIST;
                    MDScopeCodePropertyType mdScopeCodePropertyType = new MDScopeCodePropertyType();
                    CodeListValueType codeListValue = ISOMetadata.createCodeListValueType(codeListUri, value, ISOMetadata.ISO_19115_CODESPACE, value);
                    mdScopeCodePropertyType.setMDScopeCode(ObjectFactories.GMD().createMDScopeCode(codeListValue));
                    coreMetadata.getMIMetadata().getElementType().getHierarchyLevel().add(mdScopeCodePropertyType);
                }
            } else if (hierarchyLevelObj instanceof String) {
                // Backward compatibility: string value
                coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue((String) hierarchyLevelObj);
            }
        } else {
            // Default to "dataset" if not provided
            coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("dataset");
        }

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
        String metadataVersion = json.optString("metadata_version", null);
        if (metadataVersion != null) {
            resource.getExtensionHandler().setMetadataVersion(metadataVersion);
        }

        String metadataOriginalVersion = json.optString("metadata_original_version", null);
        if (metadataOriginalVersion != null) {
            resource.getExtensionHandler().setMetadataOriginalVersion(metadataOriginalVersion);
        }

        // Resource dates
        String resourceCreationDate = json.optString("resource_creation_date", null);
        if (resourceCreationDate != null) {
            identification.setCitationCreationDate(resourceCreationDate);
        }

        String resourcePublicationDate = json.optString("resource_publication_date", null);
        if (resourcePublicationDate != null) {
            identification.setCitationPublicationDate(resourcePublicationDate);
        }

        String resourceRevisionDate = json.optString("resource_revision_date", null);
        if (resourceRevisionDate != null) {
            identification.setCitationRevisionDate(resourceRevisionDate);
        }

        String resourceExpirationDate = json.optString("resource_expiration_date", null);
        if (resourceExpirationDate != null) {
            identification.setCitationExpiryDate(resourceExpirationDate);
        }

        // Edition
        String edition = json.optString("edition", null);
        if (edition != null) {
            identification.setCitationEdition(edition);
        }

        String editionDate = json.optString("edition_date", null);
        if (editionDate != null) {
            identification.setCitationEditionDate(editionDate);
        }

        // Presentation form (v3.8+: JSON object with uri and value)
        Object presentationFormObj = json.opt("presentation_form");
        if (presentationFormObj != null) {
            if (presentationFormObj instanceof JSONObject) {
                JSONObject presentationFormJson = (JSONObject) presentationFormObj;
                String uri = presentationFormJson.optString("uri", null);
                String value = presentationFormJson.optString("value", null);
                if (value != null) {
                    // Use custom URI if provided, otherwise use default
                    String codeListUri = (uri != null && !uri.isEmpty()) ? uri : ISOMetadata.CI_PRESENTATION_FORM_CODE_CODELIST;
                    CIPresentationFormCodePropertyType propertyType = new CIPresentationFormCodePropertyType();
                    CodeListValueType codeListValue = ISOMetadata.createCodeListValueType(codeListUri, value, ISOMetadata.ISO_19115_CODESPACE, value);
                    propertyType.setCIPresentationFormCode(codeListValue);
                    // Use the existing method which handles the citation internally
                    identification.getElementType().getCitation().getCICitation().getPresentationForm().clear();
                    identification.getElementType().getCitation().getCICitation().getPresentationForm().add(propertyType);
                }
            } else if (presentationFormObj instanceof String) {
                // Backward compatibility: string value
                identification.setCitationPresentationForm((String) presentationFormObj);
            }
        }

        // Graphic overview
        JSONObject graphicOverview = json.optJSONObject("graphic_overview");
        if (graphicOverview != null) {
            String graphicUrl = graphicOverview.optString("url", null);
            String graphicDescription = graphicOverview.optString("description", null);
            String fileType = graphicOverview.optString("file_type", null);
            if (graphicUrl != null) {
                if (identification instanceof DataIdentification) {
                    ((DataIdentification) identification).addBrowseGraphic(graphicUrl, graphicDescription, fileType);
                } else if (identification instanceof ServiceIdentification) {
                    BrowseGraphic bg = new BrowseGraphic();
                    bg.setFileName(graphicUrl);
                    if (graphicDescription != null) bg.setFileDescription(graphicDescription);
                    if (fileType != null) bg.setFileType(fileType);
                    ((ServiceIdentification) identification).addGraphicOverview(bg);
                }
            }
        }

        // Status (v3.8+: JSON object with uri and value)
        Object statusObj = json.opt("status");
        if (statusObj != null) {
            if (statusObj instanceof JSONObject) {
                JSONObject statusJson = (JSONObject) statusObj;
                String uri = statusJson.optString("uri", null);
                String value = statusJson.optString("value", null);
                if (value != null) {
                    // Use custom URI if provided, otherwise use default
                    String codeListUri = (uri != null && !uri.isEmpty()) ? uri : ISOMetadata.MD_PROGRESS_CODE_CODELIST;
                    MDProgressCodePropertyType propertyType = new MDProgressCodePropertyType();
                    CodeListValueType codeListValue = ISOMetadata.createCodeListValueType(codeListUri, value, ISOMetadata.ISO_19115_CODESPACE, value);
                    propertyType.setMDProgressCode(codeListValue);
                    identification.getElementType().getStatus().clear();
                    identification.getElementType().getStatus().add(propertyType);
                }
            } else if (statusObj instanceof String) {
                // Backward compatibility: string value
                identification.setStatus((String) statusObj);
            }
        }

        // Update frequency (v3.8+: JSON object with uri and value)
        Object updateFrequencyObj = json.opt("update_frequency");
        if (updateFrequencyObj != null) {
            if (updateFrequencyObj instanceof JSONObject) {
                JSONObject updateFrequencyJson = (JSONObject) updateFrequencyObj;
                String uri = updateFrequencyJson.optString("uri", null);
                String value = updateFrequencyJson.optString("value", null);
                identification.setMaintenanceAndUpdateFrequency(uri,value);

            } else if (updateFrequencyObj instanceof String) {
                // Backward compatibility: string value
                identification.setMaintenanceAndUpdateFrequency(null,(String) updateFrequencyObj);
            }
        }

        String dateOfNextUpdate = json.optString("date_of_next_update", null);
        // Skip placeholder values like "None" or "null"
        if (dateOfNextUpdate != null && !dateOfNextUpdate.isEmpty() && !"null".equalsIgnoreCase(dateOfNextUpdate) && !"none".equalsIgnoreCase(dateOfNextUpdate)) {
            identification.setDateOfNextUpdate(dateOfNextUpdate);
        }

        // Data language (DataIdentification and ServiceIdentification both support language in citation/identification)
        String dataLanguage = json.optString("data_language", null);
        if (dataLanguage != null && identification instanceof DataIdentification) {
            ((DataIdentification) identification).addLanguage(dataLanguage);
        }

        // Topic categories (dataset/series; service may also have topic categories per ISO 19119)
        JSONArray topicCategories = json.optJSONArray("topic_categories");
        if (topicCategories != null && identification instanceof DataIdentification) {
            for (int i = 0; i < topicCategories.length(); i++) {
                String topicCategory = topicCategories.optString(i, null);
                if (topicCategory != null) {
                    ((DataIdentification) identification).addTopicCategory(topicCategory);
                }
            }
        }
        // ServiceIdentification: topic category can be set on extent; for simplicity we skip when service
    }

    /**
     * Maps responsible parties
     */
    private void mapResponsibleParties(JSONObject json, CoreMetadata coreMetadata, String hierarchyLevel) {
        // Metadata owner (pointOfContact)
        JSONObject metadataOwner = json.optJSONObject("metadata_owner");
        if (metadataOwner != null) {
            ResponsibleParty party = createResponsibleParty(metadataOwner);
            if (party != null) {
                coreMetadata.getMIMetadata().setContact(party);
            }
        }

        Identification identification = getIdentification(coreMetadata, hierarchyLevel);
        // Resource owner
        JSONObject resourceOwner = json.optJSONObject("resource_owner");
        if (resourceOwner != null) {
            ResponsibleParty party = createResponsibleParty(resourceOwner);
            if (party != null) {
                identification.addCitedResponsibleParty(party);
            }
        }

        // Point of contact
        JSONObject pointOfContact = json.optJSONObject("point_of_contact");
        if (pointOfContact != null) {
            ResponsibleParty party = createResponsibleParty(pointOfContact);
            if (party != null) {
                identification.setPointOfContact(party);
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
                        identification.addCitedResponsibleParty(party);
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
    private void mapKeywords(JSONObject json, CoreMetadata coreMetadata, String hierarchyLevel) {
        Identification identification = getIdentification(coreMetadata, hierarchyLevel);
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
                        if (thesaurusUrl != null) {
                            // Use anchor property type when URL is available
                            String title = thesaurusName != null ? thesaurusName : "";
                            thesaurusCitation.getElementType().setTitle(ISOMetadata.createAnchorPropertyType(thesaurusUrl, title));
                        } else if (thesaurusName != null) {
                            thesaurusCitation.setTitle(thesaurusName);
                        }
                        if (publicationDate != null) {
                            thesaurusCitation.addDate(publicationDate, "publication");
                        }
                        if (revisionDate != null) {
                            thesaurusCitation.addDate(revisionDate, "revision");
                        }
                        keywords.setThesaurusCitation(thesaurusCitation);
                    }

                    identification.addKeywords(keywords);
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
                String name = thesaurus.optString("name", "Spatial scope");
                String url = thesaurus.optString("url", null);

                if (url != null) {
                    thesaurusCitation.getElementType().setTitle(ISOMetadata.createAnchorPropertyType(url, name));
                } else {
                    thesaurusCitation.setTitle("Spatial scope");
                }
                String publicationDate = thesaurus.optString("publication_date", null);
                if (publicationDate != null) {
                    thesaurusCitation.addDate(publicationDate, "publication");
                }
                keywords.setThesaurusCitation(thesaurusCitation);
            }
            identification.addKeywords(keywords);
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
                String url = thesaurus.optString("url", null);
                String name = thesaurus.optString("name", "INSPIRE priority data set");
                if (url != null) {
                    thesaurusCitation.getElementType().setTitle(ISOMetadata.createAnchorPropertyType(url, name));
                } else {
                    thesaurusCitation.setTitle("INSPIRE priority data set");
                }
                String publicationDate = thesaurus.optString("publication_date", null);
                if (publicationDate != null) {
                    thesaurusCitation.addDate(publicationDate, "publication");
                }
                keywords.setThesaurusCitation(thesaurusCitation);
            }
            identification.addKeywords(keywords);
        }
    }

    /**
     * Maps spatial information
     */
    private void mapSpatialInformation(JSONObject json, CoreMetadata coreMetadata, String hierarchyLevel) {
        Identification identification = getIdentification(coreMetadata, hierarchyLevel);
        // Bounding box (EPSG:4326) - support string or number values
        JSONObject bbox4326 = json.optJSONObject("bbox_4326");
        if (bbox4326 != null) {
            BigDecimal west = toBigDecimal(bbox4326.opt("west_bound_longitude"));
            BigDecimal east = toBigDecimal(bbox4326.opt("east_bound_longitude"));
            BigDecimal south = toBigDecimal(bbox4326.opt("south_bound_latitude"));
            BigDecimal north = toBigDecimal(bbox4326.opt("north_bound_latitude"));

            if (west != null && east != null && south != null && north != null) {
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
                // Use anchor property type if label is available, otherwise use plain text
                if (label != null && !label.isEmpty()) {
                    refSystem.setCodeWithAnchor(url, label);
                } else {
                    refSystem.setCode(url);
                }
                refSystem.setCodeSpace("EPSG");
                coreMetadata.getMIMetadata().addReferenceSystemInfo(refSystem);
            }
        }

        // Bounding polygon in native EPSG
        String bboxNativeEpsgGmlPolygon = json.optString("bbox_native_epsg_gml_polygon", null);
        if (bboxNativeEpsgGmlPolygon != null && !bboxNativeEpsgGmlPolygon.isEmpty()) {
            try {
                // Parse space-separated coordinates string (format: x1 y1 x2 y2 x3 y3 ...)
                String[] coordStrings = bboxNativeEpsgGmlPolygon.trim().split("\\s+");
                List<Double> coordinates = new ArrayList<>();
                for (String coordStr : coordStrings) {
                    if (coordStr != null && !coordStr.isEmpty()) {
                        coordinates.add(Double.parseDouble(coordStr));
                    }
                }

                if (!coordinates.isEmpty()) {
                    BoundingPolygon boundingPolygon = new BoundingPolygon();
                    boundingPolygon.setCoordinates(coordinates);
                    if (identification instanceof DataIdentification) {
                        ((DataIdentification) identification).addBoundingPolygon(boundingPolygon);
                    } else if (identification instanceof ServiceIdentification) {
                        ((ServiceIdentification) identification).addBoundingPolygon(boundingPolygon);
                    }
                }
            } catch (NumberFormatException e) {
                logger.warn("Error parsing coordinates from bbox_native_epsg_gml_polygon: {}", bboxNativeEpsgGmlPolygon, e);
            } catch (Exception e) {
                logger.warn("Error creating bounding polygon from bbox_native_epsg_gml_polygon", e);
            }
        }

        // Vertical extent
        JSONObject verticalExtent = json.optJSONObject("vertical_extent");
        if (verticalExtent != null) {
            Double minValue = verticalExtent.optDouble("minimum_value", Double.NaN);
            Double maxValue = verticalExtent.optDouble("maximum_value", Double.NaN);
            String crs = verticalExtent.optString("crs", null);

            if (!minValue.isNaN() && !maxValue.isNaN()) {
                if (identification instanceof DataIdentification) {
                    ((DataIdentification) identification).addVerticalExtent(minValue, maxValue, crs);
                } else if (identification instanceof ServiceIdentification) {
                    ((ServiceIdentification) identification).addVerticalExtent(minValue, maxValue);
                }
            }
        }
    }

    /**
     * Parses string or number to BigDecimal for bbox etc.
     */
    private static BigDecimal toBigDecimal(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue());
        if (val instanceof String) {
            String s = ((String) val).trim();
            if (s.isEmpty()) return null;
            try {
                return new BigDecimal(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Maps temporal information
     */
    private void mapTemporalInformation(JSONObject json, CoreMetadata coreMetadata, String hierarchyLevel) {
        Identification identification = getIdentification(coreMetadata, hierarchyLevel);
        JSONObject temporalExtent = json.optJSONObject("temporal_extent");
        if (temporalExtent != null) {
            String startDate = temporalExtent.optString("start_date", null);
            String endDate = temporalExtent.optString("end_date", null);

            if (startDate != null || endDate != null) {
                if (identification instanceof DataIdentification) {
                    ((DataIdentification) identification).addTemporalExtent(startDate, endDate);
                } else if (identification instanceof ServiceIdentification) {
                    ((ServiceIdentification) identification).addTemporalExtent(startDate, endDate);
                }
            }
        }
    }

    /**
     * Maps constraints
     */
    private void mapConstraints(JSONObject json, CoreMetadata coreMetadata, String hierarchyLevel) {
        Identification identification = getIdentification(coreMetadata, hierarchyLevel);
        JSONObject resourceConstraints = json.optJSONObject("resource_constraints");
        if (resourceConstraints != null) {
            LegalConstraints legalConstraints = new LegalConstraints();

            String useLimitation = resourceConstraints.optString("use_limitation", null);
            if (useLimitation != null) {
                legalConstraints.addUseLimitation(useLimitation);
            }

            // Access constraints (v3.8+: JSON object with uri and value)
            Object accessConstraintsObj = resourceConstraints.opt("access_constraints");
            if (accessConstraintsObj != null) {
                if (accessConstraintsObj instanceof JSONObject) {
                    JSONObject accessConstraintsJson = (JSONObject) accessConstraintsObj;
                    String uri = accessConstraintsJson.optString("uri", null);
                    String value = accessConstraintsJson.optString("value", null);
                    if (value != null) {
                        // Use custom URI if provided, otherwise use default
                        String codeListUri = (uri != null && !uri.isEmpty()) ? uri : ISOMetadata.MD_RESTRICTION_CODE_CODELIST;
                        MDRestrictionCodePropertyType restrictionCodeProperty = new MDRestrictionCodePropertyType();
                        CodeListValueType codeListValue = ISOMetadata.createCodeListValueType(codeListUri, value, ISOMetadata.ISO_19115_CODESPACE, value);
                        restrictionCodeProperty.setMDRestrictionCode(codeListValue);
                        legalConstraints.getElementType().getAccessConstraints().add(restrictionCodeProperty);
                    }
                } else if (accessConstraintsObj instanceof String) {
                    // Backward compatibility: string value
                    legalConstraints.addAccessConstraintsCode((String) accessConstraintsObj);
                }
            }

            // Use constraints (v3.8+: JSON object with uri and value)
            Object useConstraintsObj = resourceConstraints.opt("use_constraints");
            if (useConstraintsObj != null) {
                if (useConstraintsObj instanceof JSONObject) {
                    JSONObject useConstraintsJson = (JSONObject) useConstraintsObj;
                    String uri = useConstraintsJson.optString("uri", null);
                    String value = useConstraintsJson.optString("value", null);
                    if (value != null) {
                        // Use custom URI if provided, otherwise use default
                        String codeListUri = (uri != null && !uri.isEmpty()) ? uri : ISOMetadata.MD_RESTRICTION_CODE_CODELIST;
                        MDRestrictionCodePropertyType restrictionCodeProperty = new MDRestrictionCodePropertyType();
                        CodeListValueType codeListValue = ISOMetadata.createCodeListValueType(codeListUri, value, ISOMetadata.ISO_19115_CODESPACE, value);
                        restrictionCodeProperty.setMDRestrictionCode(codeListValue);
                        legalConstraints.getElementType().getUseConstraints().add(restrictionCodeProperty);
                    }
                } else if (useConstraintsObj instanceof String) {
                    // Backward compatibility: string value
                    legalConstraints.addUseConstraintsCode((String) useConstraintsObj);
                }
            }

            String otherConstraints = resourceConstraints.optString("other_constraints", null);
            if (otherConstraints != null) {
                legalConstraints.addOtherConstraints(otherConstraints);
            }

            // Security constraints
            // Security classification (v3.8+: JSON object with uri and value)
            Object securityClassificationObj = resourceConstraints.opt("security_classification");
            String securityClassification = null;
            String securityClassificationUri = null;
            if (securityClassificationObj != null) {
                if (securityClassificationObj instanceof JSONObject) {
                    JSONObject securityClassificationJson = (JSONObject) securityClassificationObj;
                    securityClassificationUri = securityClassificationJson.optString("uri", null);
                    securityClassification = securityClassificationJson.optString("value", null);
                } else if (securityClassificationObj instanceof String) {
                    // Backward compatibility: string value
                    securityClassification = (String) securityClassificationObj;
                }
            }

            String securityNote = resourceConstraints.optString("security_note", null);
            if (securityClassification != null || (securityNote != null && !"null".equals(securityNote))) {
                // Create security constraints
                MDSecurityConstraintsType securityConstraintsType = new MDSecurityConstraintsType();

                // Set classification (required field)
                if (securityClassification != null && !securityClassification.isEmpty()) {
                    MDClassificationCodePropertyType classificationProperty = new MDClassificationCodePropertyType();
                    // Use custom URI if provided, otherwise use default
                    String codeListUri = (securityClassificationUri != null && !securityClassificationUri.isEmpty())
                            ? securityClassificationUri : ISOMetadata.MD_CLASSIFICATION_CODE_CODELIST;
                    CodeListValueType classificationCode = ISOMetadata.createCodeListValueType(codeListUri,
                            securityClassification, ISOMetadata.ISO_19115_CODESPACE, securityClassification);
                    classificationProperty.setMDClassificationCode(classificationCode);
                    securityConstraintsType.setClassification(classificationProperty);
                }

                // Set user note (optional field)
                if (securityNote != null && !"null".equals(securityNote) && !securityNote.isEmpty()) {
                    securityConstraintsType.setUserNote(ISOMetadata.createCharacterStringPropertyType(securityNote));
                }

                // Add security constraints to resource constraints
                MDConstraintsPropertyType constraintProperty = new MDConstraintsPropertyType();
                constraintProperty.setMDConstraints(ObjectFactories.GMD().createMDSecurityConstraints(securityConstraintsType));
                identification.getElementType().getResourceConstraints().add(constraintProperty);
            }

            identification.addLegalConstraints(legalConstraints);
        }
    }

    /**
     * Maps distribution information
     */
    private void mapDistribution(JSONObject json, CoreMetadata coreMetadata, GSResource resource, String hierarchyLevel) {
        // Formats
        JSONArray formats = json.optJSONArray("formats");
        if (formats != null) {
            for (int i = 0; i < formats.length(); i++) {
                JSONObject formatObj = formats.optJSONObject(i);
                if (formatObj != null) {
                    Format format = new Format();

                    // Handle name field - can be string (backward compatibility) or object with uri and label (v3.7)
                    Object nameObj = formatObj.opt("name");
                    if (nameObj != null) {
                        if (nameObj instanceof String) {
                            // Backward compatibility: name is a string
                            format.setName((String) nameObj);
                        } else if (nameObj instanceof JSONObject) {
                            // New format: name is an object with uri and label
                            JSONObject nameJson = (JSONObject) nameObj;
                            String uri = nameJson.optString("uri", null);
                            String label = nameJson.optString("label", null);
                            if (uri != null && !uri.isEmpty()) {
                                // Use anchor when URI is available
                                format.setNameWithAnchor(uri, label != null ? label : "");
                            } else if (label != null && !label.isEmpty()) {
                                // Fall back to plain text if no URI
                                format.setName(label);
                            }
                        }
                    }

                    String version = formatObj.optString("version", null);
                    if (version != null) {
                        format.setVersion(version);
                    }

                    String specification = formatObj.optString("specification", null);
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

                    // Description (v3.8+: JSON object with uri and label)
                    Object descriptionObj = onlineResourceObj.opt("description");
                    if (descriptionObj != null) {
                        if (descriptionObj instanceof JSONObject) {
                            JSONObject descriptionJson = (JSONObject) descriptionObj;
                            String uri = descriptionJson.optString("uri", null);
                            String label = descriptionJson.optString("label", null);
                            if (uri != null && !uri.isEmpty()) {
                                // Use anchor when URI is available
                                online.setDescriptionGmxAnchor(uri, label != null ? label : "");
                            } else if (label != null && !label.isEmpty()) {
                                // Fall back to plain text if no URI
                                online.setDescription(label);
                            }
                        } else if (descriptionObj instanceof String) {
                            // Backward compatibility: string value
                            online.setDescription((String) descriptionObj);
                        }
                    }

                    // Function (v3.8+: JSON object with uri and value)
                    Object functionObj = onlineResourceObj.opt("function");
                    if (functionObj != null) {
                        if (functionObj instanceof JSONObject) {
                            JSONObject functionJson = (JSONObject) functionObj;
                            String uri = functionJson.optString("uri", null);
                            String value = functionJson.optString("value", null);
                            if (value != null) {
                                // Use custom URI if provided, otherwise use default
                                String codeListUri = (uri != null && !uri.isEmpty()) ? uri : ISOMetadata.CI_ON_LINE_FUNCTION_CODE_CODELIST;
                                CIOnLineFunctionCodePropertyType propertyType = new CIOnLineFunctionCodePropertyType();
                                CodeListValueType codeListValue = ISOMetadata.createCodeListValueType(codeListUri, value, ISOMetadata.ISO_19115_CODESPACE, value);
                                propertyType.setCIOnLineFunctionCode(codeListValue);
                                online.getElementType().setFunction(propertyType);
                            }
                        } else if (functionObj instanceof String) {
                            // Backward compatibility: string value
                            online.setFunctionCode((String) functionObj);
                        }
                    }

                    JSONObject protocol = onlineResourceObj.optJSONObject("protocol");
                    if (protocol != null) {
                        // v3.8+: renamed code -> uri, name -> label
                        String protocolUri = protocol.optString("uri", null);
                        String protocolLabel = protocol.optString("label", null);
                        // Backward compatibility: also check old field names
                        if (protocolUri == null) {
                            protocolUri = protocol.optString("code", null);
                        }
                        if (protocolLabel == null) {
                            protocolLabel = protocol.optString("name", null);
                        }
                        if (protocolUri != null) {
                            // Use anchor when URI is available
                            online.setProtocolAnchor(protocolUri, protocolLabel);
                        } else if (protocolLabel != null) {
                            online.setProtocol(protocolLabel);
                        }
                    }

                    // Application profile (v3.8+: JSON object with uri and label)
                    Object applicationProfileObj = onlineResourceObj.opt("application_profile");
                    if (applicationProfileObj != null) {
                        if (applicationProfileObj instanceof JSONObject) {
                            JSONObject applicationProfileJson = (JSONObject) applicationProfileObj;
                            String uri = applicationProfileJson.optString("uri", null);
                            String label = applicationProfileJson.optString("label", null);
                            if (uri != null && !uri.isEmpty()) {
                                // Use anchor when URI is available - set directly on element type
                                online.getElementType().setApplicationProfile(ISOMetadata.createAnchorPropertyType(uri, label != null ? label : ""));
                            } else if (label != null && !label.isEmpty()) {
                                // Fall back to plain text if no URI
                                online.setApplicationProfile(label);
                            }
                        } else if (applicationProfileObj instanceof String) {
                            // Backward compatibility: string value
                            online.setApplicationProfile((String) applicationProfileObj);
                        }
                    }

                    //
                    // EXT_Online
                    //

                    // Map protocol_request (v3.7) - free text string for function
                    String protocolRequest = onlineResourceObj.optString("protocol_request", null);
                    if (protocolRequest != null && !protocolRequest.isEmpty()) {
                        online.setFunctionCode(protocolRequest);
                    }

                    String queryStringFragment = onlineResourceObj.optString("query_string_fragment", null);

                    String layerPk = onlineResourceObj.optString("layer_pk", null);

                    boolean temporalWms = onlineResourceObj.optBoolean("temporal_wms", false);

                    JSONObject layerStyle = onlineResourceObj.optJSONObject("layer_style");

                    if (queryStringFragment != null || layerPk != null || temporalWms || layerStyle != null) {

                        EXT_Online extOnline = new EXT_Online(online);

                        if (queryStringFragment != null) {

                            extOnline.getElementType().setQueryStringFragment(queryStringFragment);
                        }

                        if (layerPk != null) {

                            extOnline.getElementType().setLayerPk(layerPk);
                        }

                        if (layerStyle != null) {

                            String layerStyleName = layerStyle.optString("name");
                            String layerStyleWs = layerStyle.optString("workspace");

                            if (layerStyleName != null) {

                                extOnline.getElementType().setLayerStyleName(layerStyleName);
                            }

                            if (layerStyleWs != null) {

                                extOnline.getElementType().setLayerStyleWorkspace(layerStyleWs);
                            }
                        }

                        extOnline.getElementType().setTemporal(temporalWms);

                        coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(extOnline);

                    } else {

                        coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
                    }
                }
            }
        }

        if (json.has("raster_mosaic")) {
            boolean rasterMosaic = json.optBoolean("raster_mosaic", false);
            resource.getExtensionHandler().setRasterMosaic(rasterMosaic);
        }
    }

    /**
     * Maps quality information
     */
    private void mapQualityInformation(JSONObject json, CoreMetadata coreMetadata, String hierarchyLevel) {
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
                        specificationCitation.addDate(specificationPublicationDate, "publication");
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
            // Scope code (v3.8+: JSON object with uri and value)
            Object scopeCodeObj = qualityScope.opt("scope_code");
            String scopeCode = null;
            String scopeCodeUri = null;
            if (scopeCodeObj != null) {
                if (scopeCodeObj instanceof JSONObject) {
                    JSONObject scopeCodeJson = (JSONObject) scopeCodeObj;
                    scopeCodeUri = scopeCodeJson.optString("uri", null);
                    scopeCode = scopeCodeJson.optString("value", null);
                } else if (scopeCodeObj instanceof String) {
                    // Backward compatibility: string value
                    scopeCode = (String) scopeCodeObj;
                }
            }

            String scopeDetail = qualityScope.optString("scope_details", null);
            if (scopeCode != null) {
                // Use custom URI if provided, otherwise use default
                String codeListUri = (scopeCodeUri != null && !scopeCodeUri.isEmpty())
                        ? scopeCodeUri : ISOMetadata.MD_SCOPE_CODE_CODELIST;
                // Get or create DataQuality
                Iterator<DataQuality> dataQualities = coreMetadata.getMIMetadata().getDataQualities();
                DataQuality dataQuality;
                if (dataQualities.hasNext()) {
                    dataQuality = dataQualities.next();
                } else {
                    dataQuality = new DataQuality();
                    coreMetadata.getMIMetadata().addDataQuality(dataQuality);
                }

                DQScopeType scopeType = new DQScopeType();
                MDScopeCodePropertyType levelProperty = new MDScopeCodePropertyType();
                CodeListValueType codeListValue = ISOMetadata.createCodeListValueType(codeListUri, scopeCode, ISOMetadata.ISO_19115_CODESPACE, scopeCode);
                levelProperty.setMDScopeCode(ObjectFactories.GMD().createMDScopeCode(codeListValue));
                scopeType.setLevel(levelProperty);
                if (scopeDetail != null && !scopeDetail.isEmpty()) {
                    MDScopeDescriptionType scopeDescriptionType = new MDScopeDescriptionType();
                    scopeDescriptionType.setOther(ISOMetadata.createCharacterStringPropertyType(scopeDetail));
                    MDScopeDescriptionPropertyType scopeDescriptionProperty = new MDScopeDescriptionPropertyType();
                    scopeDescriptionProperty.setMDScopeDescription(scopeDescriptionType);
                    scopeType.getLevelDescription().add(scopeDescriptionProperty);
                }
                DQScopePropertyType scopeProperty = new DQScopePropertyType();
                scopeProperty.setDQScope(scopeType);
                dataQuality.getElementType().setScope(scopeProperty);
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
                                        // Create Online resource and add to citation
                                        Online online = new Online();
                                        online.setLinkage(url);

                                        // TODO: Citation doesn't have a direct method to add online resources
                                        // This may need to be added as an extension or handled differently
                                        logger.debug(
                                                "TODO: Add online resource to citation - Citation class doesn't support online resources directly");
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
                    mapProcessStep(processStepObj, coreMetadata);
                }
            }
        }
    }

    /**
     * Maps a lineage process step from JSON to ISO 19115 structure.
     */
    private void mapProcessStep(JSONObject processStepObj, CoreMetadata coreMetadata) {
        // Ensure lineage exists by setting a statement if needed (this creates the lineage structure)
        // We'll access it directly through the metadata
        eu.essi_lab.iso.datamodel.classes.MDMetadata metadata = coreMetadata.getMIMetadata();
        Iterator<eu.essi_lab.iso.datamodel.classes.DataQuality> dataQualities = metadata.getDataQualities();
        eu.essi_lab.iso.datamodel.classes.DataQuality dataQuality;
        if (dataQualities.hasNext()) {
            dataQuality = dataQualities.next();
        } else {
            dataQuality = new eu.essi_lab.iso.datamodel.classes.DataQuality();
            metadata.addDataQuality(dataQuality);
        }

        // Get or create lineage structure
        net.opengis.iso19139.gmd.v_20060504.LILineagePropertyType lineageProperty = dataQuality.getElementType().getLineage();
        if (lineageProperty == null) {
            lineageProperty = new net.opengis.iso19139.gmd.v_20060504.LILineagePropertyType();
            dataQuality.getElementType().setLineage(lineageProperty);
        }
        net.opengis.iso19139.gmd.v_20060504.LILineageType lineageType = lineageProperty.getLILineage();
        if (lineageType == null) {
            lineageType = new net.opengis.iso19139.gmd.v_20060504.LILineageType();
            lineageProperty.setLILineage(lineageType);
        }

        // Create LIProcessStepType
        LEProcessStepType processStepType = new LEProcessStepType();
        // Map description (required)
        String description = processStepObj.optString("description", null);
        if (description != null && !description.isEmpty()) {
            processStepType.setDescription(ISOMetadata.createCharacterStringPropertyType(description));
        } else {
            // Set empty description if not provided (required field)
            processStepType.setDescription(ISOMetadata.createCharacterStringPropertyType(""));
        }

        // Map rationale (optional)
        String rationale = processStepObj.optString("rationale", null);
        if (rationale != null && !rationale.isEmpty()) {
            processStepType.setRationale(ISOMetadata.createCharacterStringPropertyType(rationale));
        }

        // Map date (optional) - can be Date or DateTime
        String date = processStepObj.optString("date", null);
        if (date != null && !date.isEmpty()) {
            try {
                Optional<Date> parsedDate = ISO8601DateTimeUtils.parseISO8601ToDate(date);
                if (parsedDate.isPresent()) {
                    XMLGregorianCalendar xmlCal = ISO8601DateTimeUtils.getXMLGregorianCalendar(parsedDate.get());
                    DateTimePropertyType dateTimeProperty = new DateTimePropertyType();
                    dateTimeProperty.setDateTime(xmlCal);
                    processStepType.setDateTime(dateTimeProperty);
                }
            } catch (DatatypeConfigurationException e) {
                logger.warn("Error parsing date for process step: {}", date, e);
            }
        }

        // Map processor (optional) - CI_ResponsibleParty with role "processor"
        JSONObject processor = processStepObj.optJSONObject("processor");
        if (processor != null) {
            ResponsibleParty processorParty = createResponsibleParty(processor);

            // Set role to "processor" if not already set
            if (processorParty.getRoleCode() == null || processorParty.getRoleCode().isEmpty()) {
                processorParty.setRoleCode("processor");
            }
            CIResponsiblePartyPropertyType processorProperty = new CIResponsiblePartyPropertyType();
            processorProperty.setCIResponsibleParty(processorParty.getElementType());
            processStepType.getProcessor().add(processorProperty);

        }

        // Map source (optional) - use LE_Source (GMI extension)
        JSONObject source = processStepObj.optJSONObject("source");
        if (source != null) {
            LESourceType sourceType = new LESourceType();

            // Map source citation if available
            JSONObject sourceCitation = source.optJSONObject("source_citation");
            if (sourceCitation != null) {
                Citation citation = new Citation();
                String title = sourceCitation.optString("title", null);
                if (title != null) {
                    citation.setTitle(title);
                }
                net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType citationProperty = new net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType();
                citationProperty.setCICitation(citation.getElementType());
                sourceType.setSourceCitation(citationProperty);
            }

            // Map source description if available
            String sourceDescription = source.optString("description", null);
            if (sourceDescription != null && !sourceDescription.isEmpty()) {
                sourceType.setDescription(ISOMetadata.createCharacterStringPropertyType(sourceDescription));
            }

            // Use LISourcePropertyType since getSource() returns List<LISourcePropertyType>
            // LESourceType extends LISourceType, so we can set it via setLISource
            net.opengis.iso19139.gmd.v_20060504.LISourcePropertyType sourceProperty = new net.opengis.iso19139.gmd.v_20060504.LISourcePropertyType();
            sourceProperty.setLISource(sourceType); // LESourceType extends LISourceType
            processStepType.getSource().add(sourceProperty);
        }

        // Map processing_information (optional) - gmi:LE_Processing
        JSONObject processingInformation = processStepObj.optJSONObject("processing_information");
        if (processingInformation != null) {
            LEProcessingType processingType = new LEProcessingType();

            // Map identifier (required)
            String id = processingInformation.optString("id", null);
            if (id != null && !id.isEmpty()) {
                MDIdentifierPropertyType identifierProperty = new MDIdentifierPropertyType();
                MDIdentifierType identifierType = new MDIdentifierType();
                identifierType.setCode(ISOMetadata.createCharacterStringPropertyType(id));
                identifierProperty.setMDIdentifier(ObjectFactories.GMD().createMDIdentifier(identifierType));
                processingType.setIdentifier(identifierProperty);
            } else {
                // Set empty identifier if not provided (required field)
                MDIdentifierPropertyType identifierProperty = new MDIdentifierPropertyType();
                MDIdentifierType identifierType = new MDIdentifierType();
                identifierType.setCode(ISOMetadata.createCharacterStringPropertyType(""));
                identifierProperty.setMDIdentifier(ObjectFactories.GMD().createMDIdentifier(identifierType));
                processingType.setIdentifier(identifierProperty);
            }

            // Map software_reference (optional)
            JSONObject softwareReference = processingInformation.optJSONObject("software_reference");
            if (softwareReference != null) {
                Citation citation = new Citation();
                String title = softwareReference.optString("title", null);
                if (title != null) {
                    citation.setTitle(title);
                }
                // TODO: Map online_resource if needed
                net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType citationProperty = new net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType();
                citationProperty.setCICitation(citation.getElementType());
                processingType.getSoftwareReference().add(citationProperty);
            }

            // Map procedure_description (optional)
            String procedureDescription = processingInformation.optString("procedure_description", null);
            if (procedureDescription != null && !procedureDescription.isEmpty()) {
                processingType.setProcedureDescription(ISOMetadata.createCharacterStringPropertyType(procedureDescription));
            }

            // Map documentation (optional) - array of citations
            JSONArray documentation = processingInformation.optJSONArray("documentation");
            if (documentation != null) {
                for (int j = 0; j < documentation.length(); j++) {
                    JSONObject docObj = documentation.optJSONObject(j);
                    if (docObj != null) {
                        Citation docCitation = new Citation();
                        String docTitle = docObj.optString("title", null);
                        if (docTitle != null) {
                            docCitation.setTitle(docTitle);
                        }
                        // TODO: Map online_resource if needed
                        net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType docCitationProperty = new net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType();
                        docCitationProperty.setCICitation(docCitation.getElementType());
                        processingType.getDocumentation().add(docCitationProperty);
                    }
                }
            }

            // Map run_time_parameters (optional)
            String runTimeParameters = processingInformation.optString("run_time_parameters", null);
            if (runTimeParameters != null && !runTimeParameters.isEmpty()) {
                processingType.setRunTimeParameters(ISOMetadata.createCharacterStringPropertyType(runTimeParameters));
            }

            // Map algorithm (optional)
            JSONObject algorithm = processingInformation.optJSONObject("algorithm");
            if (algorithm != null) {
                LEAlgorithmType algorithmType = new LEAlgorithmType();

                // Map citation (required)
                JSONObject algorithmCitation = algorithm.optJSONObject("citation");
                if (algorithmCitation != null) {
                    Citation citation = new Citation();
                    String title = algorithmCitation.optString("title", null);
                    if (title != null) {
                        citation.setTitle(title);
                    }
                    // TODO: Map online_resource if needed
                    net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType citationProperty = new net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType();
                    citationProperty.setCICitation(citation.getElementType());
                    algorithmType.setCitation(citationProperty);
                }

                // Map description (required)
                String algorithmDescription = algorithm.optString("description", null);
                if (algorithmDescription != null && !algorithmDescription.isEmpty()) {
                    algorithmType.setDescription(ISOMetadata.createCharacterStringPropertyType(algorithmDescription));
                } else {
                    // Set empty description if not provided (required field)
                    algorithmType.setDescription(ISOMetadata.createCharacterStringPropertyType(""));
                }

                LEAlgorithmPropertyType algorithmProperty = new LEAlgorithmPropertyType();
                algorithmProperty.setLEAlgorithm(algorithmType);
                processingType.getAlgorithm().add(algorithmProperty);
            }

            // Note: parameter array is mentioned in hints but requires mrl:LE_ProcessParameter
            // which may not be available. Skipping for now.

            LEProcessingPropertyType processingProperty = new LEProcessingPropertyType();
            processingProperty.setLEProcessing(processingType);
            processStepType.setProcessingInformation(processingProperty);
        }

        // Map output (optional) - gmi:LE_Source
        JSONObject output = processStepObj.optJSONObject("output");
        if (output != null) {
            LESourceType outputSourceType = new LESourceType();

            // Map source citation if available
            JSONObject outputCitation = output.optJSONObject("source_citation");
            if (outputCitation != null) {
                Citation citation = new Citation();
                String title = outputCitation.optString("title", null);
                if (title != null) {
                    citation.setTitle(title);
                }
                net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType citationProperty = new net.opengis.iso19139.gmd.v_20060504.CICitationPropertyType();
                citationProperty.setCICitation(citation.getElementType());
                outputSourceType.setSourceCitation(citationProperty);
            }

            // Map description if available
            String outputDescription = output.optString("description", null);
            if (outputDescription != null && !outputDescription.isEmpty()) {
                outputSourceType.setDescription(ISOMetadata.createCharacterStringPropertyType(outputDescription));
            }

            LESourcePropertyType outputProperty = new LESourcePropertyType();
            outputProperty.setLESource(outputSourceType);
            processStepType.getOutput().add(outputProperty);
        }

        // Map report (optional) - gmi:LE_ProcessStepReport
        JSONObject report = processStepObj.optJSONObject("report");
        if (report != null) {
            LEProcessStepReportType reportType = new LEProcessStepReportType();

            // Map name (required)
            String reportName = report.optString("name", null);
            if (reportName != null && !reportName.isEmpty()) {
                reportType.setName(ISOMetadata.createCharacterStringPropertyType(reportName));
            } else {
                // Set empty name if not provided (required field)
                reportType.setName(ISOMetadata.createCharacterStringPropertyType(""));
            }

            // Map description (optional)
            String reportDescription = report.optString("description", null);
            if (reportDescription != null && !reportDescription.isEmpty()) {
                reportType.setDescription(ISOMetadata.createCharacterStringPropertyType(reportDescription));
            }

            // Map file_type (optional)
            String fileType = report.optString("file_type", null);
            if (fileType != null && !fileType.isEmpty()) {
                reportType.setFileType(ISOMetadata.createCharacterStringPropertyType(fileType));
            }

            LEProcessStepReportPropertyType reportProperty = new LEProcessStepReportPropertyType();
            reportProperty.setLEProcessStepReport(reportType);
            processStepType.getReport().add(reportProperty);
        }

        // Wrap in LEProcessStepPropertyType and add to lineage processStep list
        // Note: getProcessStep() returns List<LIProcessStepPropertyType>, but LEProcessStepType extends LIProcessStepType
        // So we use LIProcessStepPropertyType and set the LEProcessStepType via setLIProcessStep
        net.opengis.iso19139.gmd.v_20060504.LIProcessStepPropertyType processStepProperty = new net.opengis.iso19139.gmd.v_20060504.LIProcessStepPropertyType();
        processStepProperty.setLIProcessStep(processStepType); // LEProcessStepType extends LIProcessStepType
        lineageType.getProcessStep().add(processStepProperty);
    }

    /**
     * Maps model-specific fields
     */
    private void mapModelSpecificFields(JSONObject json, CoreMetadata coreMetadata, GSResource resource) {
        // Create ModelSpecificFields extension object
        ModelSpecificFields modelFields = new ModelSpecificFields();
        boolean hasFields = false;

        // Map model_maturity_level
        String modelMaturityLevel = json.optString("model_maturity_level", null);
        if (modelMaturityLevel != null && !modelMaturityLevel.isEmpty()) {
            modelFields.setModelMaturityLevel(modelMaturityLevel);
            hasFields = true;
        }

        // Map model_computational_requirements
        JSONObject modelComputationalRequirements = json.optJSONObject("model_computational_requirements");
        if (modelComputationalRequirements != null) {
            String cpu = modelComputationalRequirements.optString("cpu", null);
            if (cpu != null && !cpu.isEmpty()) {
                modelFields.setCpu(cpu);
                hasFields = true;
            }

            String gpu = modelComputationalRequirements.optString("gpu", null);
            if (gpu != null && !gpu.isEmpty()) {
                modelFields.setGpu(gpu);
                hasFields = true;
            }

            String ram = modelComputationalRequirements.optString("ram", null);
            if (ram != null && !ram.isEmpty()) {
                modelFields.setRam(ram);
                hasFields = true;
            }

            String storage = modelComputationalRequirements.optString("storage", null);
            if (storage != null && !storage.isEmpty()) {
                modelFields.setStorage(storage);
                hasFields = true;
            }
        }

        // Map model_types
        JSONArray modelTypes = json.optJSONArray("model_types");
        if (modelTypes != null) {
            for (int i = 0; i < modelTypes.length(); i++) {
                String modelType = modelTypes.optString(i, null);
                if (modelType != null && !modelType.isEmpty()) {
                    modelFields.addModelType(modelType);
                    hasFields = true;
                }
            }
        }

        // Map supported_platforms
        JSONArray supportedPlatforms = json.optJSONArray("supported_platforms");
        if (supportedPlatforms != null) {
            for (int i = 0; i < supportedPlatforms.length(); i++) {
                String platform = supportedPlatforms.optString(i, null);
                if (platform != null && !platform.isEmpty()) {
                    modelFields.addSupportedPlatform(platform);
                    hasFields = true;
                }
            }
        }

        // Map model_category
        String modelCategory = json.optString("model_category", null);
        if (modelCategory != null && !modelCategory.isEmpty()) {
            modelFields.setModelCategory(modelCategory);
            hasFields = true;
        }

        // Map model_methodology_description
        String modelMethodologyDescription = json.optString("model_methodology_description", null);
        if (modelMethodologyDescription != null && !modelMethodologyDescription.isEmpty()) {
            modelFields.setModelMethodologyDescription(modelMethodologyDescription);
            hasFields = true;
        }

        // Set the extension if any fields were populated
        if (hasFields) {
            resource.getExtensionHandler().setModelSpecificFields(modelFields);
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
                            coreMetadata.getMIMetadata().getDataQualityInfo()
                                    .addQuantitativeAttributeAccuracy(metricName, metricDescription, metricValue, null);
                        }
                    }
                }
            }
        }
    }

    /**
     * Maps service-specific fields (ISO 19119 SV_ServiceIdentification).
     */
    private void mapServiceSpecificFields(JSONObject json, CoreMetadata coreMetadata) {
        ServiceIdentification serviceId = coreMetadata.getMIMetadata().getServiceIdentification();
        if (serviceId == null) {
            return;
        }
        SVServiceIdentificationType svcType = serviceId.getElementType();

        // service_type --> srv:serviceType/gco:LocalName
        String serviceTypeStr = json.optString("service_type", null);
        if (serviceTypeStr != null && !serviceTypeStr.isEmpty()) {
            try {
                net.opengis.iso19139.gco.v_20060504.ObjectFactory of = new net.opengis.iso19139.gco.v_20060504.ObjectFactory();
                GenericNamePropertyType localNameProp = new GenericNamePropertyType();
                CodeType codetype = new CodeType();
                codetype.setValue(serviceTypeStr);
                localNameProp.setAbstractGenericName(of.createLocalName(codetype));
                svcType.setServiceType(localNameProp);
            } catch (Exception e) {
                logger.warn("Could not set service_type: {}", e.getMessage());
            }
        }

        // service_type_version --> srv:serviceTypeVersion/gco:CharacterString (List)
        String serviceTypeVersion = json.optString("service_type_version", null);
        if (serviceTypeVersion != null && !serviceTypeVersion.isEmpty()) {
            try {
                List<CharacterStringPropertyType> list = new ArrayList<>();
                list.add(ISOMetadata.createCharacterStringPropertyType(serviceTypeVersion));
                svcType.setServiceTypeVersion(list);
            } catch (Exception e) {
                logger.warn("Could not set service_type_version: {}", e.getMessage());
            }
        }

        // service_coupling_type --> srv:couplingType
        String serviceCouplingType = json.optString("service_coupling_type", null);
        if (serviceCouplingType != null && !serviceCouplingType.isEmpty()) {
            serviceId.setCouplingType(serviceCouplingType);
        }

        // service_operates_on --> srv:operatesOn (xlink:href)
        JSONArray serviceOperatesOn = json.optJSONArray("service_operates_on");
        if (serviceOperatesOn != null) {
            for (int i = 0; i < serviceOperatesOn.length(); i++) {
                String href = serviceOperatesOn.optString(i, null);
                if (href != null && !href.isEmpty()) {
                    try {
                        MDDataIdentificationPropertyType prop = new MDDataIdentificationPropertyType();
                        prop.setHref(href);
                        svcType.getOperatesOn().add(prop);
                    } catch (Exception e) {
                        logger.warn("Could not add service_operates_on entry: {}", e.getMessage());
                    }
                }
            }
        }

        // service_operations --> srv:containsOperations/srv:SV_OperationMetadata
        JSONArray serviceOperations = json.optJSONArray("service_operations");
        if (serviceOperations != null) {
            for (int i = 0; i < serviceOperations.length(); i++) {
                JSONObject opObj = serviceOperations.optJSONObject(i);
                if (opObj != null) {
                    try {
                        SVOperationMetadataType opType = new SVOperationMetadataType();
                        String operationName = opObj.optString("operation_name", null);
                        if (operationName != null) {
                            opType.setOperationName(ISOMetadata.createCharacterStringPropertyType(operationName));
                        }
                        String operationDescription = opObj.optString("operation_description", null);
                        if (operationDescription != null) {
                            opType.setOperationDescription(ISOMetadata.createCharacterStringPropertyType(operationDescription));
                        }
                        String invocationName = opObj.optString("invocation_name", null);
                        if (invocationName != null) {
                            opType.setInvocationName(ISOMetadata.createCharacterStringPropertyType(invocationName));
                        }
                        // DCP list (codeListValue e.g. WebServices, HTTP-GET)
                        JSONArray dcpArray = opObj.optJSONArray("dcp");
                        if (dcpArray != null) {
                            for (int d = 0; d < dcpArray.length(); d++) {
                                String dcp = dcpArray.optString(d, null);
                                if (dcp != null && !dcp.isEmpty()) {
                                    try {
                                        DCPListPropertyType dcpProperty = new DCPListPropertyType();
                                        CodeListValueType dcpCode = ISOMetadata.createCodeListValueType(
                                                ISOMetadata.SV_DCP_LIST_CODELIST, dcp, ISOMetadata.ISO_19119_CODESPACE, dcp);
                                        dcpProperty.setDCPList(dcpCode);
                                        opType.getDCP().add(dcpProperty);
                                    } catch (Exception e) {
                                        logger.debug("Could not add DCP: {}", e.getMessage());
                                    }
                                }
                            }
                        }
                        // connect_point (array of URLs) --> srv:connectPoint
                        JSONArray connectPointArray = opObj.optJSONArray("connect_point");
                        if (connectPointArray != null) {
                            for (int c = 0; c < connectPointArray.length(); c++) {
                                String url = connectPointArray.optString(c, null);
                                if (url != null) {
                                    CIOnlineResourceType onlineType = new CIOnlineResourceType();
                                    URLPropertyType urlProp = new URLPropertyType();
                                    urlProp.setURL(url);
                                    onlineType.setLinkage(urlProp);
                                    CIOnlineResourcePropertyType onlineProp = new CIOnlineResourcePropertyType();
                                    onlineProp.setCIOnlineResource(onlineType);
                                    opType.getConnectPoint().add(onlineProp);
                                }
                            }
                        }
                        // parameters --> srv:parameters/srv:SV_Parameter
                        JSONArray parameters = opObj.optJSONArray("parameters");
                        if (parameters != null && !parameters.isEmpty()) {
                            try {
                                List<SVParameterPropertyType> paramList = opType.getParameters();
                                for (int p = 0; p < parameters.length(); p++) {
                                    JSONObject paramObj = parameters.optJSONObject(p);
                                    if (paramObj != null) {
                                        SVParameterType svParam = new SVParameterType();
                                        String name = paramObj.optString("name", null);
                                        if (name != null) {
                                            MemberNameType memberName = new MemberNameType();
                                            memberName.setAName(ISOMetadata.createCharacterStringPropertyType(name));
                                            svParam.setName(memberName);
                                        }
                                        String description = paramObj.optString("description", null);
                                        if (description != null) {
                                            svParam.setDescription(ISOMetadata.createCharacterStringPropertyType(description));
                                        }
                                        Boolean optionality = paramObj.optBoolean("optionality", true);
                                        svParam.setOptionality(ISOMetadata.createCharacterStringPropertyType(optionality.toString()));
                                        Boolean repeatability = paramObj.optBoolean("repeatability", false);
                                        BooleanPropertyType repeatProp = new BooleanPropertyType();
                                        repeatProp.setBoolean(repeatability);
                                        svParam.setRepeatability(repeatProp);
                                        SVParameterPropertyType paramProperty = new SVParameterPropertyType();
                                        paramProperty.setSVParameter(svParam);
                                        paramList.add(paramProperty);
                                    }
                                }
                            } catch (Exception e) {
                                logger.debug("Could not add operation parameters: {}", e.getMessage());
                            }
                        }
                        SVOperationMetadataPropertyType opProp = new SVOperationMetadataPropertyType();
                        opProp.setSVOperationMetadata(opType);
                        svcType.getContainsOperations().add(opProp);
                    } catch (Exception e) {
                        logger.warn("Could not add service_operation: {}", e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Maps dataset-specific fields
     */
    private void mapDatasetSpecificFields(JSONObject json, CoreMetadata coreMetadata, GSResource resource) {
        // Dataset type (v3.8+: JSON object with uri and value)
        Object datasetTypeObj = json.opt("dataset_type");
        if (datasetTypeObj != null) {
            if (datasetTypeObj instanceof JSONObject) {
                JSONObject datasetTypeJson = (JSONObject) datasetTypeObj;
                String uri = datasetTypeJson.optString("uri", null);
                String value = datasetTypeJson.optString("value", null);
                if (value != null) {
                    // Use custom URI if provided, otherwise use default
                    String codeListUri = (uri != null && !uri.isEmpty()) ? uri : ISOMetadata.MD_SPATIAL_REPRESENTATION_TYPE_CODE_CODELIST;
                    MDSpatialRepresentationTypeCodePropertyType spatialProperty = new MDSpatialRepresentationTypeCodePropertyType();
                    CodeListValueType codeListValue = ISOMetadata.createCodeListValueType(codeListUri, value, ISOMetadata.ISO_19115_CODESPACE, value);
                    spatialProperty.setMDSpatialRepresentationTypeCode(codeListValue);
                    List<MDSpatialRepresentationTypeCodePropertyType> spatialProperties = new ArrayList<>();
                    spatialProperties.add(spatialProperty);
                    coreMetadata.getMIMetadata().getDataIdentification().getElementType().setSpatialRepresentationType(spatialProperties);
                }
            } else if (datasetTypeObj instanceof String) {
                // Backward compatibility: string value
                coreMetadata.getMIMetadata().getDataIdentification().setSpatialRepresentationType((String) datasetTypeObj);
            }
        }

        // Spatial resolution (v3.6+: object with value and uom, or number for backward compatibility)
        Object spatialResolutionObj = json.opt("spatial_resolution");
        if (spatialResolutionObj != null) {
            if (spatialResolutionObj instanceof JSONObject) {
                // New format: object with value and uom
                JSONObject spatialResolutionJson = (JSONObject) spatialResolutionObj;
                // Handle both string and number values
                Double value = null;
                Object valueObj = spatialResolutionJson.opt("value");
                if (valueObj instanceof Number) {
                    value = ((Number) valueObj).doubleValue();
                } else if (valueObj instanceof String) {
                    try {
                        value = Double.parseDouble((String) valueObj);
                    } catch (NumberFormatException e) {
                        // Ignore invalid number format
                    }
                } else {
                    value = spatialResolutionJson.optDouble("value", Double.NaN);
                }
                String uom = spatialResolutionJson.optString("uom", null);
                if (value != null && !value.isNaN()) {
                    MDResolution resolution = new MDResolution();
                    // Use provided uom, or default to "m" if not provided
                    String unitOfMeasure = (uom != null && !uom.isEmpty()) ? uom : "m";
                    resolution.setDistance(unitOfMeasure, value);
                    coreMetadata.getMIMetadata().getDataIdentification().setSpatialResolution(resolution);
                }
            } else if (spatialResolutionObj instanceof Number) {
                // Backward compatibility: number (defaults to "m" unit)
                Double spatialResolution = ((Number) spatialResolutionObj).doubleValue();
                if (!spatialResolution.isNaN()) {
                    coreMetadata.getMIMetadata().getDataIdentification().setSpatialResolution(spatialResolution);
                }
            }
        }

        // Equivalent scale
        Double equivalentScale = json.optDouble("equivalent_scale", Double.NaN);
        if (!equivalentScale.isNaN()) {
            coreMetadata.getMIMetadata().getDataIdentification().setEquivalentScale(equivalentScale.intValue());
        }

        // Raster nodata value
        Double rasterNodataValue = json.optDouble("raster_nodata_value", Double.NaN);
        if (!rasterNodataValue.isNaN()) {
            resource.getExtensionHandler().setAttributeMissingValue(rasterNodataValue.toString());
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

