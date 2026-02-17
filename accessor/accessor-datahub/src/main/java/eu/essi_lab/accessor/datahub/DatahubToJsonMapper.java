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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBElement;

import net.opengis.iso19139.gco.v_20060504.BooleanPropertyType;
import net.opengis.iso19139.gco.v_20060504.CharacterStringPropertyType;
import net.opengis.iso19139.gco.v_20060504.CodeListValueType;
import net.opengis.iso19139.gco.v_20060504.DatePropertyType;
import net.opengis.iso19139.gmd.v_20060504.*;
import net.opengis.iso19139.srv.v_20060504.SVOperationMetadataPropertyType;
import net.opengis.iso19139.srv.v_20060504.SVOperationMetadataType;
import net.opengis.iso19139.srv.v_20060504.SVParameterPropertyType;
import net.opengis.iso19139.srv.v_20060504.SVParameterType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.*;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.LEAlgorithmPropertyType;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.LEAlgorithmType;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.LEProcessStepReportPropertyType;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.LEProcessStepReportType;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.LEProcessStepType;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.LESourcePropertyType;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.LEProcessingType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.ModelSpecificFields;

/**
 * Maps a GSResource (ISO 19115 internal model) to DataHub JSON metadata model v3.8.
 * Reverse of {@link DatahubMapper}.
 */
public final class DatahubToJsonMapper {

    private static final Logger logger = GSLoggerFactory.getLogger(DatahubToJsonMapper.class);

    private DatahubToJsonMapper() {
    }

    /**
     * Converts a GSResource to DataHub JSON string (v3.8 metadata model).
     *
     * @param resource the resource (harmonized metadata)
     * @return JSON string; never null
     */
    public static String toJson(GSResource resource) {
        if (resource == null) {
            return new JSONObject().put("placeholder", true).put("message", "No resource").toString();
        }
        try {
            eu.essi_lab.model.resource.HarmonizedMetadata harmonized = resource.getHarmonizedMetadata();
            if (harmonized == null) {
                return minimalJson(resource.getPublicId(), null).toString();
            }
            CoreMetadata core = harmonized.getCoreMetadata();
            if (core == null) {
                return minimalJson(resource.getPublicId(), null).toString();
            }
            return toJson(core, resource).toString();
        } catch (Exception e) {
            logger.warn("Error mapping GSResource to DataHub JSON: {}", e.getMessage(), e);
            return new JSONObject().put("placeholder", true).put("error", e.getMessage()).toString();
        }
    }

    private static JSONObject minimalJson(String identifier, String title) {
        JSONObject o = new JSONObject();
        if (identifier != null) o.put("identifier", identifier);
        if (title != null) o.put("title", title);
        o.put("hierarchy_level", codeObj("dataset"));
        o.put("resource_constraints", new JSONObject().put("other_constraints", ""));
        o.put("online_resources", new JSONArray());
        return o;
    }

    private static JSONObject toJson(CoreMetadata core, GSResource resource) {
        JSONObject out = new JSONObject();
        eu.essi_lab.iso.datamodel.classes.MDMetadata mi = core.getMIMetadata();
        String hierarchyLevel = resolveHierarchyLevel(mi);
        Identification identification = getIdentification(core, hierarchyLevel);
        if (identification == null) {
            return minimalJson(core.getIdentifier(), core.getTitle());
        }

        mapBasicIdentification(out, core, mi, identification, hierarchyLevel, resource);
        mapResponsibleParties(out, core, mi, identification);
        mapKeywords(out, identification);
        mapSpatialInformation(out, core, identification);
        mapTemporalInformation(out, identification);
        mapConstraints(out, identification);
        mapDistribution(out, core);
        mapQualityInformation(out, mi, resource);

        if ("dataset".equalsIgnoreCase(hierarchyLevel) || "document".equalsIgnoreCase(hierarchyLevel) || "series".equalsIgnoreCase(hierarchyLevel)) {
            mapDatasetSpecific(out, core, identification);
        }
        if ("service".equalsIgnoreCase(hierarchyLevel)) {
            mapServiceSpecific(out, mi);
        }
        if ("model".equalsIgnoreCase(hierarchyLevel)) {
            mapModelSpecific(out, resource, core);
        }

        // Required by DataHub model
        if (!out.has("resource_constraints")) {
            out.put("resource_constraints", new JSONObject().put("other_constraints", ""));
        }
        if (!out.has("online_resources")) {
            out.put("online_resources", new JSONArray());
        }
        return out;
    }

    private static String resolveHierarchyLevel(eu.essi_lab.iso.datamodel.classes.MDMetadata mi) {
        try {
            Iterator<String> it = mi.getHierarchyLevelScopeCodeListValues();
            if (it != null && it.hasNext()) {
                String v = it.next();
                if (v != null && !v.isEmpty()) return v;
            }
        } catch (Exception e) {
            // ignore
        }
        return "dataset";
    }

    private static Identification getIdentification(CoreMetadata core, String hierarchyLevel) {
        if ("service".equalsIgnoreCase(hierarchyLevel)) {
            ServiceIdentification svc = core.getMIMetadata().getServiceIdentification();
            if (svc != null) return svc;
        }
        return core.getMIMetadata().getDataIdentification();
    }

    private static void putOpt(JSONObject o, String key, Object value) {
        if (value != null && !JSONObject.NULL.equals(value)) o.put(key, value);
    }



    private static String unwrapOptional(Optional<String> opt) {
        return opt != null && opt.isPresent() ? opt.get() : null;
    }

    private static JSONObject codeObj(String value) {
        return codeObj(value, null);
    }

    private static JSONObject codeObj(String value, String uri) {
        if (value == null) return null;
        JSONObject o = new JSONObject();
        if (uri != null) o.put("uri", uri);
        o.put("value", value);
        return o;
    }

    private static void mapBasicIdentification(JSONObject out, CoreMetadata core, eu.essi_lab.iso.datamodel.classes.MDMetadata mi,
	    Identification identification, String hierarchyLevel, GSResource resource) {
        putOpt(out, "identifier", core.getIdentifier());
        putOpt(out, "title", identification.getCitationTitle());
        putOpt(out, "abstract", identification.getAbstract());
        putOpt(out, "metadata_language", mi.getLanguage());
        putOpt(out, "metadata_updated_at", mi.getDateStamp());
        putOpt(out, "metadata_profile", metadataProfile(mi.getMetadataStandardName(), mi.getMetadataStandardVersion()));
        putOpt(out, "resource_creation_date", identification.getCitationCreationDate());
        putOpt(out, "resource_publication_date", identification.getCitationPublicationDate());
        putOpt(out, "resource_revision_date", identification.getCitationRevisionDate());
        putOpt(out, "resource_expiration_date", getCitationExpiryDate(identification));
        putOpt(out, "edition", getCitationEdition(identification));
        putOpt(out, "edition_date", identification.getCitationEditionDate());
        putOpt(out, "date_of_next_update", getDateOfNextUpdate(identification));
        putOpt(out, "hierarchy_level", codeObj(hierarchyLevel, "https://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_ScopeCode"));
        putOpt(out, "character_set", charSetObj(mi.getCharacterSetCode()));
        putOpt(out, "presentation_form", presentationFormObj(identification));
        putOpt(out, "status", statusObj(identification));
        putOpt(out, "update_frequency", updateFrequencyObj(identification));
        putOpt(out, "graphic_overview", graphicOverview(identification));
        if (resource != null && resource.getExtensionHandler() != null) {
            putOpt(out, "metadata_version", unwrapOptional(resource.getExtensionHandler().getMetadataVersion()));
            putOpt(out, "metadata_original_version", unwrapOptional(resource.getExtensionHandler().getMetadataOriginalVersion()));
        }
    }

    private static String getCitationExpiryDate(Identification id) {
        try {
            Object d = id.getCitationDate("expiry");
            return d != null ? d.toString() : null;
        } catch (Exception e) { return null; }
    }

    private static String getCitationEdition(Identification id) {
        try {
            CICitationPropertyType citationProp = id.getElementType().getCitation();
            if (citationProp == null) return null;
            CICitationType firstCitation = citationProp.getCICitation();
            if (firstCitation == null) return null;
            return ISOMetadata.getStringFromCharacterString(firstCitation.getEdition());
        } catch (Exception e) {
            return null;
        }
    }

    private static String getDateOfNextUpdate(Identification id) {
        return getDateOfNextUpdateFromMaintenance(id);
    }

    private static JSONObject metadataProfile(String name, String version) {
        if (name == null && version == null) return null;
        JSONObject o = new JSONObject();
        if (name != null) o.put("name", name);
        if (version != null) o.put("version", version);
        return o;
    }

    private static final String MD_CHARACTER_SET_CODE_URI = "https://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_CharacterSetCode";

    private static JSONObject charSetObj(String value) {
        if (value == null) return null;
        JSONObject o = new JSONObject();
        o.put("uri", MD_CHARACTER_SET_CODE_URI);
        o.put("value", value);
        return o;
    }

    private static JSONObject presentationFormObj(Identification id) {
        String defaultUri = "https://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_PresentationFormCode";
        try {
            JSONObject ret = new JSONObject();
            List<CIPresentationFormCodePropertyType> pf = id.getFirstCitation().getPresentationForm();
            if (pf==null){
               return null;
            }
            CIPresentationFormCodePropertyType cpcpt;
            if (pf.isEmpty()){
               return null;
            }else{
                cpcpt = pf.getFirst();
            }
            CodeListValueType code = cpcpt.getCIPresentationFormCode();
            if (code==null){
                return null;
            }
            String cdv = code.getCodeListValue();
            if (cdv!=null){
                ret.put("value",cdv);
            }
            String uri = code.getCodeList();
            if (uri!=null){
                ret.put("uri",uri);
            }

            return ret;

        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private static JSONObject statusObj(Identification identification) {
        return codeFromIdentificationType(identification, "getStatus", "getMDProgressCode", "https://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_ProgressCode");
    }

    private static String getDateOfNextUpdateFromMaintenance(Identification id) {
        try {
            AbstractMDIdentificationType elementType = id.getElementType();
            if (elementType == null) return null;
            List<MDMaintenanceInformationPropertyType> maintenanceList = elementType.getResourceMaintenance();
            if (maintenanceList == null || maintenanceList.isEmpty()) return null;
            MDMaintenanceInformationPropertyType first = maintenanceList.get(0);
            if (first == null) return null;
            MDMaintenanceInformationType info = first.getMDMaintenanceInformation();
            if (info == null) return null;
            DatePropertyType dateProp = info.getDateOfNextUpdate();
            if (dateProp == null) return null;
            String dt = dateProp.getDate();
            return dt;
        } catch (Exception e) {
            return null;
        }
    }

    private static JSONObject updateFrequencyObj(Identification id) {
        String defaultUri = "https://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_MaintenanceFrequencyCode";
        try {
            AbstractMDIdentificationType elementType = id.getElementType();
            if (elementType == null) return null;
            List<MDMaintenanceInformationPropertyType> maintenanceList = elementType.getResourceMaintenance();
            if (maintenanceList == null || maintenanceList.isEmpty()) return null;
            MDMaintenanceInformationPropertyType first = maintenanceList.get(0);
            if (first == null) return null;
            MDMaintenanceInformationType info = first.getMDMaintenanceInformation();
            if (info == null) return null;
            MDMaintenanceFrequencyCodePropertyType freqProp = info.getMaintenanceAndUpdateFrequency();
            if (freqProp == null) return null;
            Object codeObj = freqProp.getMDMaintenanceFrequencyCode();
            if (codeObj instanceof JAXBElement) codeObj = ((JAXBElement<?>) codeObj).getValue();
            if (!(codeObj instanceof CodeListValueType)) return null;
            String val = ((CodeListValueType) codeObj).getCodeListValue();
            return val != null ? codeObj(val, defaultUri) : null;
        } catch (Exception e) {
            return null;
        }
    }



    private static JSONObject codeFromIdentificationType(Identification id, String getterName, String codeGetter, String defaultUri) {
        try {
            List<MDProgressCodePropertyType> status = id.getElementType().getStatus();
            if (status == null||status.isEmpty()) return null;
            MDProgressCodePropertyType first = status.getFirst();
                if (first != null) {
                    CodeListValueType code = first.getMDProgressCode();
                    if (code != null) {

                        String val = code.getCodeListValue();
                        if (val != null) return codeObj( val, defaultUri);
                    }
                }

        } catch (Exception e) {
            // ignore
        }
        return null;
    }



    private static JSONObject graphicOverview(Identification identification) {
        try {
            if (identification instanceof DataIdentification) {
                BrowseGraphic bg = ((DataIdentification) identification).getGraphicOverview();
                if (bg == null) return null;
                JSONObject o = new JSONObject();
                putOpt(o, "url", bg.getFileName());
                putOpt(o, "description", bg.getFileDescription());
                putOpt(o, "file_type", bg.getFileType());
                return o;
            }
            if (identification instanceof ServiceIdentification) {
                BrowseGraphic bg = ((ServiceIdentification) identification).getGraphicOverview();
                if (bg == null) return null;
                JSONObject o = new JSONObject();
                putOpt(o, "url", bg.getFileName());
                putOpt(o, "description", bg.getFileDescription());
                putOpt(o, "file_type", bg.getFileType());
                return o;
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private static void mapResponsibleParties(JSONObject out, CoreMetadata core, eu.essi_lab.iso.datamodel.classes.MDMetadata mi, Identification identification) {
        ResponsibleParty metadataOwner = null;
        if (mi != null) {
            Iterator<ResponsibleParty> contacts = mi.getContacts();
            if (contacts != null && contacts.hasNext()) metadataOwner = contacts.next();
        }
        putOpt(out, "metadata_owner", responsiblePartyToJson(metadataOwner));
        putOpt(out, "point_of_contact", responsiblePartyToJson(identification.getPointOfContact()));
        putOpt(out, "resource_owner", getCitedPartyByRole(identification, "owner"));
        putOpt(out, "resource_provider", resourceProvider(core));
        List<JSONObject> other = otherRoles(identification);
        if (!other.isEmpty()) out.put("other_roles", new JSONArray(other));
    }

    private static JSONObject responsiblePartyToJson(ResponsibleParty party) {
        if (party == null) return null;
        JSONObject o = new JSONObject();
        putOpt(o, "organization_name", party.getOrganisationName());
        putOpt(o, "individual_name", party.getIndividualName());
        putOpt(o, "role", party.getRoleCode());
        Contact contact = party.getContact();
        if (contact != null) {
            String email = contact.getAddress() != null ? contact.getAddress().getElectronicMailAddress() : null;
            if (email != null) o.put("address", new JSONObject().put("email", email));
            Online online = contact.getOnline();
            if (online != null && online.getLinkage() != null) {
                o.put("link", online.getLinkage());
            }
        }
        return o;
    }

    private static JSONObject getCitedPartyByRole(Identification identification, String role) {
        try {
            List<ResponsibleParty> list = identification.getCitationResponsibleParties();
            if (list == null) return null;
            for (ResponsibleParty p : list) {
                if (role.equalsIgnoreCase(p.getRoleCode())) return responsiblePartyToJson(p);
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private static JSONObject resourceProvider(CoreMetadata core) {
        try {
            List<ResponsibleParty> list = core.getMIMetadata().getDistribution().getDistributorParties();
            if (list != null && !list.isEmpty()) return responsiblePartyToJson(list.get(0));
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private static List<JSONObject> otherRoles(Identification identification) {
        List<JSONObject> list = new ArrayList<>();
        try {
            List<ResponsibleParty> cited = identification.getCitationResponsibleParties();
            if (cited == null) return list;
            for (ResponsibleParty p : cited) {
                String role = p.getRoleCode();
                if (role == null || "owner".equalsIgnoreCase(role)) continue;
                list.add(responsiblePartyToJson(p));
            }
        } catch (Exception e) {
            // ignore
        }
        return list;
    }

    private static final String SPATIAL_SCOPE_THESAURUS_NAME = "Spatial scope";
    private static final String SPATIAL_SCOPE_THESAURUS_URL_FRAGMENT = "SpatialScope";
    private static final String INSPIRE_PRIORITY_DATASET_THESAURUS_NAME = "INSPIRE priority data set";
    private static final String INSPIRE_PRIORITY_DATASET_THESAURUS_URL_FRAGMENT = "PriorityDataset";

    private static void mapKeywords(JSONObject out, Identification identification) {
        JSONArray descriptiveKeywords = new JSONArray();
        try {
            Iterator<Keywords> it = identification.getKeywords();
            if (it != null) {
                while (it.hasNext()) {
                    Keywords kw = it.next();
                    Citation th = kw.getThesaurusCitation();
                    JSONObject thObj = th != null ? thesaurusToJson(th) : null;
                    boolean isSpatialScope = isSpatialScopeThesaurus(thObj);
                    boolean isInspirePriorityDataset = isInspirePriorityDatasetThesaurus(thObj);
                    List<CharacterStringPropertyType> keywordProps = kw.getElementType().getKeyword();
                    if (isSpatialScope && keywordProps != null && !keywordProps.isEmpty()) {
                        CharacterStringPropertyType first = keywordProps.get(0);
                        String label = ISOMetadata.getStringFromCharacterString(first);
                        String uri = ISOMetadata.getHREFStringFromCharacterString(first);
                        JSONObject keywordsObj = new JSONObject();
                        keywordsObj.put("label", label != null ? label : "");
                        if (uri != null && !uri.isEmpty()) keywordsObj.put("uri", uri);
                        JSONObject spatialScope = new JSONObject();
                        spatialScope.put("keywords", keywordsObj);
                        if (thObj != null) spatialScope.put("thesaurus", thObj);
                        out.put("spatial_scope", spatialScope);
                        continue;
                    }
                    if (isInspirePriorityDataset && keywordProps != null && !keywordProps.isEmpty()) {
                        CharacterStringPropertyType first = keywordProps.get(0);
                        String label = ISOMetadata.getStringFromCharacterString(first);
                        String uri = ISOMetadata.getHREFStringFromCharacterString(first);
                        JSONObject keywordsObj = new JSONObject();
                        keywordsObj.put("label", label != null ? label : "");
                        if (uri != null && !uri.isEmpty()) keywordsObj.put("uri", uri);
                        JSONObject inspirePriorityDataset = new JSONObject();
                        inspirePriorityDataset.put("keywords", keywordsObj);
                        if (thObj != null) inspirePriorityDataset.put("thesaurus", thObj);
                        out.put("inspire_priority_dataset", inspirePriorityDataset);
                        continue;
                    }
                    JSONObject group = new JSONObject();
                    JSONArray kwArr = new JSONArray();
                    if (keywordProps != null) {
                        for (CharacterStringPropertyType prop : keywordProps) {
                            String label = ISOMetadata.getStringFromCharacterString(prop);
                            String uri = ISOMetadata.getHREFStringFromCharacterString(prop);
                            JSONObject ko = new JSONObject();
                            ko.put("label", label != null ? label : "");
                            if (uri != null && !uri.isEmpty()) ko.put("uri", uri);
                            kwArr.put(ko);
                        }
                    }
                    group.put("keywords", kwArr);
                    if (thObj != null) group.put("thesaurus", thObj);
                    descriptiveKeywords.put(group);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        if (descriptiveKeywords.length() > 0) out.put("descriptive_keywords", descriptiveKeywords);
    }

    private static boolean isSpatialScopeThesaurus(JSONObject thesaurusObj) {
        if (thesaurusObj == null) return false;
        String name = thesaurusObj.optString("name", null);
        if (name != null && SPATIAL_SCOPE_THESAURUS_NAME.equalsIgnoreCase(name.trim())) return true;
        String url = thesaurusObj.optString("url", null);
        return url != null && url.contains(SPATIAL_SCOPE_THESAURUS_URL_FRAGMENT);
    }

    private static boolean isInspirePriorityDatasetThesaurus(JSONObject thesaurusObj) {
        if (thesaurusObj == null) return false;
        String name = thesaurusObj.optString("name", null);
        if (name != null && INSPIRE_PRIORITY_DATASET_THESAURUS_NAME.equalsIgnoreCase(name.trim())) return true;
        String url = thesaurusObj.optString("url", null);
        return url != null && url.contains(INSPIRE_PRIORITY_DATASET_THESAURUS_URL_FRAGMENT);
    }

    private static JSONObject thesaurusToJson(Citation th) {
        if (th == null) return null;
        String name = th.getTitle();
        String url = thesaurusCitationUrl(th);
        String publicationDate = thesaurusCitationPublicationDate(th);
        String revisionDate = thesaurusCitationRevisionDate(th);
        if (name == null && url == null && publicationDate == null && revisionDate == null) return null;
        JSONObject o = new JSONObject();
        if (name != null) o.put("name", name);
        if (url != null) o.put("url", url);
        if (publicationDate != null) o.put("publication_date", publicationDate);
        if (revisionDate != null) o.put("revision_date", revisionDate);
        return o;
    }

    private static String thesaurusCitationUrl(Citation th) {
        CharacterStringPropertyType titleProp = th.getElementType().getTitle();
        if (titleProp == null) return null;
        return ISOMetadata.getHREFStringFromCharacterString(titleProp);
    }

    private static String thesaurusCitationPublicationDate(Citation th) {
        return thesaurusCitationDateByType(th, "publication");
    }

    private static String thesaurusCitationRevisionDate(Citation th) {
        return thesaurusCitationDateByType(th, "revision");
    }

    private static String thesaurusCitationDateByType(Citation th, String dateTypeCode) {
        try {
            List<CIDatePropertyType> dateList = th.getElementType().getDate();
            if (dateList == null) return null;
            for (CIDatePropertyType dateItem : dateList) {
                if (dateItem == null) continue;
                CIDateType ciDate = dateItem.getCIDate();
                if (ciDate == null) continue;
                CIDateTypeCodePropertyType dateType = ciDate.getDateType();
                if (dateType == null) continue;
                Object codeVal = dateType.getCIDateTypeCode();
                if (codeVal instanceof JAXBElement) codeVal = ((JAXBElement<?>) codeVal).getValue();
                if (codeVal instanceof CodeListValueType) {
                    String typeStr = ((CodeListValueType) codeVal).getCodeListValue();
                    if (typeStr != null && dateTypeCode.equalsIgnoreCase(typeStr)) {
                        DatePropertyType dateProp = ciDate.getDate();
                        if (dateProp != null) {
                            String dt = dateProp.getDate();
                            if (dt != null) return dt;
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private static void mapSpatialInformation(JSONObject out, CoreMetadata core, Identification identification) {
        try {
            GeographicBoundingBox bbox = identification instanceof DataIdentification
                ? ((DataIdentification) identification).getGeographicBoundingBox()
                : (identification instanceof ServiceIdentification ? ((ServiceIdentification) identification).getGeographicBoundingBox() : null);
            if (bbox != null && bbox.getWest() != null && bbox.getEast() != null && bbox.getSouth() != null && bbox.getNorth() != null) {
                JSONObject b = new JSONObject();
                b.put("west_bound_longitude", bbox.getWest());
                b.put("east_bound_longitude", bbox.getEast());
                b.put("south_bound_latitude", bbox.getSouth());
                b.put("north_bound_latitude", bbox.getNorth());
                out.put("bbox_4326", b);
            }
        } catch (Exception e) {
            // ignore
        }
        try {
            Iterator<eu.essi_lab.iso.datamodel.classes.BoundingPolygon> polyIt = null;
            if (identification instanceof DataIdentification) polyIt = ((DataIdentification) identification).getBoundingPolygons();
            else if (identification instanceof ServiceIdentification) polyIt = ((ServiceIdentification) identification).getBoundingPolygons();
            if (polyIt != null && polyIt.hasNext()) {
                Iterator<Double> coords = polyIt.next().getCoordinates();
                if (coords != null) {
                    StringBuilder sb = new StringBuilder();
                    while (coords.hasNext()) {
                        if (sb.length() > 0) sb.append(' ');
                        sb.append(coords.next());
                    }
                    if (sb.length() > 0) putOpt(out, "bbox_native_epsg_gml_polygon", sb.toString());
                }
            }
        } catch (Exception e) {
            // ignore
        }
        if (core == null) return;
        try {
            Iterator<eu.essi_lab.iso.datamodel.classes.ReferenceSystem> it = core.getMIMetadata().getReferenceSystemInfos();
            if (it != null && it.hasNext()) {
                eu.essi_lab.iso.datamodel.classes.ReferenceSystem ref = it.next();
                CharacterStringPropertyType codeProp = ref.getElementType() != null
                    && ref.getElementType().getReferenceSystemIdentifier() != null
                    && ref.getElementType().getReferenceSystemIdentifier().getRSIdentifier() != null
                    ? ref.getElementType().getReferenceSystemIdentifier().getRSIdentifier().getCode()
                    : null;
                // When set with setCodeWithAnchor(url, label): url = href, label = anchor value
                String url = codeProp != null ? ISOMetadata.getHREFStringFromCharacterString(codeProp) : null;
                if (url == null) url = ref.getCodeString().orElse(null);
                if (url != null) {
                    JSONObject nativeEpsg = new JSONObject();
                    nativeEpsg.put("url", url);
                    String label = ref.getCodeAnchorType().isPresent() && codeProp != null
                        ? ISOMetadata.getStringFromCharacterString(codeProp)
                        : null;
                    putOpt(nativeEpsg, "label", label);
                    out.put("native_epsg", nativeEpsg);
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private static void mapTemporalInformation(JSONObject out, Identification identification) {
        try {
            TemporalExtent te = identification instanceof DataIdentification
                ? ((DataIdentification) identification).getTemporalExtent()
                : (identification instanceof ServiceIdentification ? ((ServiceIdentification) identification).getTemporalExtent() : null);
            if (te != null) {
                String start = te.getBeginPosition();
                String end = te.getEndPosition();
                if (start != null || end != null) {
                    JSONObject t = new JSONObject();
                    if (start != null) t.put("start_date", start);
                    if (end != null) t.put("end_date", end);
                    out.put("temporal_extent", t);
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private static void mapConstraints(JSONObject out, Identification identification) {
        try {
            Iterator<LegalConstraints> it = identification.getLegalConstraints();
            if (it == null || !it.hasNext()) return;
            LegalConstraints lc = it.next();
            JSONObject rc = new JSONObject();
            putOpt(rc, "use_limitation", lc.getUseLimitation());
            putOpt(rc, "access_constraints", codeObj(lc.getAccessConstraintCode(), "https://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_RestrictionCode"));
            putOpt(rc, "use_constraints", codeObj(lc.getUseConstraintsCode(), "https://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_RestrictionCode"));
            putOpt(rc, "other_constraints", lc.getOtherConstraint());
            mapSecurityConstraints(rc, identification);
            out.put("resource_constraints", rc);
        } catch (Exception e) {
            // ignore
        }
    }

    private static void mapSecurityConstraints(JSONObject rc, Identification identification) {
        try {
            AbstractMDIdentificationType elementType = identification.getElementType();
            if (elementType == null) return;
            List<MDConstraintsPropertyType> constraintsList = elementType.getResourceConstraints();
            if (constraintsList == null) return;
            for (MDConstraintsPropertyType item : constraintsList) {
                if (item == null) continue;
                Object constraintObj = item.getMDConstraints();
                if (constraintObj instanceof JAXBElement) constraintObj = ((JAXBElement<?>) constraintObj).getValue();
                if (constraintObj == null || !(constraintObj instanceof MDSecurityConstraintsType)) continue;
                MDSecurityConstraintsType constraint = (MDSecurityConstraintsType) constraintObj;
                String classification = null, classificationUri = null, note = null;
                MDClassificationCodePropertyType classProp = constraint.getClassification();
                if (classProp != null) {
                    CodeListValueType code = classProp.getMDClassificationCode();
                    if (code != null) {
                        classification = code.getCodeListValue();
                        if (code.getCodeList() != null) classificationUri = code.getCodeList();
                    }
                }
                CharacterStringPropertyType userNoteProp = constraint.getUserNote();
                if (userNoteProp != null) note = ISOMetadata.getStringFromCharacterString(userNoteProp);
                if (classification != null) putOpt(rc, "security_classification", codeObj(classification, classificationUri));
                if (note != null) putOpt(rc, "security_note", note);
                break;
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private static JSONObject formatNameObj(Format f) {
        if (f == null) return new JSONObject().put("label", "");
        String label = f.getName();
        String uri = formatNameUri(f);
        JSONObject o = new JSONObject();
        o.put("label", label != null ? label : "");
        if (uri != null) o.put("uri", uri);
        return o;
    }

    private static String formatNameUri(Format f) {
        CharacterStringPropertyType nameProp = f.getElementType().getName();
        if (nameProp == null) return null;
        return ISOMetadata.getHREFStringFromCharacterString(nameProp);
    }

    private static void mapDistribution(JSONObject out, CoreMetadata core) {
        try {
            eu.essi_lab.iso.datamodel.classes.Distribution dist = core.getMIMetadata().getDistribution();
            if (dist == null) return;
            Iterator<Format> fmtIt = dist.getFormats();
            if (fmtIt != null && fmtIt.hasNext()) {
                JSONArray formats = new JSONArray();
                while (fmtIt.hasNext()) {
                    Format f = fmtIt.next();
                    JSONObject fo = new JSONObject();
                    fo.put("name", formatNameObj(f));
                    putOpt(fo, "version", f.getVersion());
                    formats.put(fo);
                }
                out.put("formats", formats);
            }
            List<JSONObject> onlines = new ArrayList<>();
            Iterator<Online> onIt = dist.getDistributionOnlines();
            if (onIt != null) {
                while (onIt.hasNext()) onlines.add(onlineToJson(onIt.next()));
            }
            try {
                List<EXT_Online> extList = dist.getExtendedDistributionOnlines();
                if (extList != null) for (EXT_Online ext : extList) onlines.add(onlineToJson(ext));
            } catch (Exception e) {
                // ignore
            }
            if (!onlines.isEmpty()) out.put("online_resources", new JSONArray(onlines));
        } catch (Exception e) {
            // ignore
        }
    }

    private static JSONObject onlineToJson(Online online) {
        if (online == null) return null;
        JSONObject o = new JSONObject();
        putOpt(o, "url", online.getLinkage());
        putOpt(o, "name", online.getName());
        putOpt(o, "description", descriptionObj(online.getDescription(), online.getDescriptionGmxAnchor()));
        putOpt(o, "protocol", protocolObj(online));
        putOpt(o, "function", functionObj(online));
        putOpt(o, "application_profile", applicationProfileObj(online));
        return o;
    }

    private static JSONObject descriptionObj(String label, String uri) {
        if (label == null && uri == null) return null;
        JSONObject o = new JSONObject();
        if (label != null) o.put("label", label);
        if (uri != null) o.put("uri", uri);
        return o;
    }

    private static JSONObject protocolObj(Online online) {
        try {
            String uri = online.getProtocolGmxAnchorHref();
            String label = online.getProtocol();
            if (uri == null && label == null) return null;
            JSONObject o = new JSONObject();
            if (uri != null) o.put("uri", uri);
            if (label != null) o.put("label", label);
            return o;
        } catch (Exception e) {
            return null;
        }
    }

    private static JSONObject functionObj(Online online) {
        try {
            String v = online.getFunctionCode();
            String uri = online.getFunctionCodeURI();
            return v != null ? codeObj(v,uri) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static JSONObject applicationProfileObj(Online online) {
        try {
            String label = online.getApplicationProfile();
            String uri = applicationProfileUri(online);
            if (label == null && uri == null) return null;
            JSONObject o = new JSONObject();
            if (label != null) o.put("label", label);
            if (uri != null) o.put("uri", uri);
            return o;
        } catch (Exception e) {
            return null;
        }
    }

    private static String applicationProfileUri(Online online) {
        CharacterStringPropertyType appProfile = online.getElementType().getApplicationProfile();
        if (appProfile == null) return null;
        return ISOMetadata.getHREFStringFromCharacterString(appProfile);
    }

    private static void mapQualityInformation(JSONObject out, eu.essi_lab.iso.datamodel.classes.MDMetadata mi, GSResource resource) {
        try {
            Iterator<eu.essi_lab.iso.datamodel.classes.DataQuality> dqIt = mi.getDataQualities();
            if (dqIt == null || !dqIt.hasNext()) return;
            eu.essi_lab.iso.datamodel.classes.DataQuality dq = dqIt.next();
            putOpt(out, "lineage_statement", dq.getLineageStatement());
            List<JSONObject> lineageSources = lineageSourcesFromDataQuality(dq);
            if (lineageSources != null && !lineageSources.isEmpty()) out.put("lineage_source", new JSONArray(lineageSources));
            List<JSONObject> processSteps = lineageProcessStepsFromDataQuality(dq);
            if (processSteps != null && !processSteps.isEmpty()) {
                if (resource != null && resource.getExtensionHandler() != null) {
                    resource.getExtensionHandler().getDatahubLineageProcessStepParameters().ifPresent(paramsJson -> {
                        try {
                            JSONArray paramsPerStep = new JSONArray(paramsJson);
                            for (int i = 0; i < processSteps.size() && i < paramsPerStep.length(); i++) {
                                JSONObject stepObj = processSteps.get(i);
                                JSONArray paramArr = paramsPerStep.optJSONArray(i);
                                if (paramArr != null && paramArr.length() > 0) {
                                    JSONObject procInfo = stepObj.optJSONObject("processing_information");
                                    if (procInfo != null) {
                                        procInfo.put("parameter", paramArr);
                                    } else {
                                        stepObj.put("processing_information", new JSONObject().put("parameter", paramArr));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // ignore malformed stored parameters
                        }
                    });
                }
                out.put("lineage_process_step", new JSONArray(processSteps));
            }
            List<JSONObject> conformity = conformanceResultsFromDataQuality(dq);
            if (conformity != null && !conformity.isEmpty()) out.put("conformity", new JSONArray(conformity));
            putOpt(out, "quality_scope", qualityScopeObj(dq));
        } catch (Exception e) {
            // ignore
        }
    }

    private static List<JSONObject> lineageSourcesFromDataQuality(eu.essi_lab.iso.datamodel.classes.DataQuality dq) {
        List<JSONObject> list = new ArrayList<>();
        try {
            LILineagePropertyType lineageProp = dq.getElementType().getLineage();
            if (lineageProp == null) return list;
            LILineageType lineageType = lineageProp.getLILineage();
            if (lineageType == null) return list;
            List<LISourcePropertyType> sources = lineageType.getSource();
            if (sources == null) return list;
            for (LISourcePropertyType sp : sources) {
                LISourceType liSource = sp.getLISource();
                if (liSource == null) continue;
                JSONObject srcJson = lineageSourceToJson(liSource);
                if (srcJson != null) list.add(srcJson);
            }
        } catch (Exception e) {
            // ignore
        }
        return list;
    }

    private static List<JSONObject> lineageProcessStepsFromDataQuality(eu.essi_lab.iso.datamodel.classes.DataQuality dq) {
        List<JSONObject> list = new ArrayList<>();
        try {
            LILineagePropertyType lineageProp = dq.getElementType().getLineage();
            if (lineageProp == null) return list;
            LILineageType lineageType = lineageProp.getLILineage();
            if (lineageType == null) return list;
            List<LIProcessStepPropertyType> steps = lineageType.getProcessStep();
            if (steps == null) return list;
            for (LIProcessStepPropertyType stepProp : steps) {
                if (stepProp == null || !stepProp.isSetLIProcessStep()) continue;
                LIProcessStepType step = stepProp.getLIProcessStep();
                if (step == null) continue;
                JSONObject ps = processStepToJson(step);
                if (ps != null) list.add(ps);
            }
        } catch (Exception e) {
            // ignore
        }
        return list;
    }

    private static JSONObject processStepToJson(LIProcessStepType step) {
        if (step == null) return null;
        JSONObject o = new JSONObject();
        putOpt(o, "description", stringFromCharProp(step.getDescription()));
        putOpt(o, "rationale", stringFromCharProp(step.getRationale()));
        if (step.isSetDateTime() && step.getDateTime() != null && step.getDateTime().getDateTime() != null) {
            try {
                o.put("date", step.getDateTime().getDateTime().toXMLFormat());
            } catch (Exception e) {
                // ignore
            }
        }
        if (step.isSetProcessor() && step.getProcessor() != null && !step.getProcessor().isEmpty()) {
            CIResponsiblePartyPropertyType first = step.getProcessor().get(0);
            if (first != null && first.getCIResponsibleParty() != null) {
                ResponsibleParty rp = new ResponsibleParty(first.getCIResponsibleParty());
                JSONObject proc = responsiblePartyToJson(rp);
                if (proc != null) o.put("processor", proc);
            }
        }
        if (step.isSetSource() && step.getSource() != null && !step.getSource().isEmpty()) {
            LISourcePropertyType firstSource = step.getSource().get(0);
            if (firstSource != null && firstSource.getLISource() != null) {
                JSONObject src = lineageSourceToJson(firstSource.getLISource());
                if (src != null) o.put("source", src);
            }
        }
        if (step instanceof LEProcessStepType) {
            LEProcessStepType leStep = (LEProcessStepType) step;
            if (leStep.isSetOutput() && leStep.getOutput() != null && !leStep.getOutput().isEmpty()) {
                LESourcePropertyType firstOutput = leStep.getOutput().get(0);
                if (firstOutput != null && firstOutput.getLESource() != null) {
                    JSONObject outputJson = lineageSourceToJson(firstOutput.getLESource());
                    if (outputJson != null) o.put("output", outputJson);
                }
            }
            if (leStep.getProcessingInformation() != null && leStep.getProcessingInformation().getLEProcessing() != null) {
                JSONObject procInfoJson = processingInformationToJson(leStep.getProcessingInformation().getLEProcessing());
                if (procInfoJson != null) o.put("processing_information", procInfoJson);
            }
            if (leStep.isSetReport() && leStep.getReport() != null && !leStep.getReport().isEmpty()) {
                LEProcessStepReportPropertyType firstReportProp = leStep.getReport().get(0);
                if (firstReportProp != null && firstReportProp.getLEProcessStepReport() != null) {
                    JSONObject reportJson = processStepReportToJson(firstReportProp.getLEProcessStepReport());
                    if (reportJson != null) o.put("report", reportJson);
                }
            }
        }
        return o.length() > 0 ? o : null;
    }

    private static JSONObject processingInformationToJson(LEProcessingType proc) {
        if (proc == null) return null;
        JSONObject o = new JSONObject();
        if (proc.isSetIdentifier() && proc.getIdentifier() != null) {
            Object idVal = proc.getIdentifier().getMDIdentifier();
            if (idVal instanceof JAXBElement) idVal = ((JAXBElement<?>) idVal).getValue();
            if (idVal instanceof MDIdentifierType) {
                String code = ISOMetadata.getStringFromCharacterString(((MDIdentifierType) idVal).getCode());
                if (code != null) o.put("id", code);
            }
        }
        if (proc.isSetSoftwareReference() && proc.getSoftwareReference() != null && !proc.getSoftwareReference().isEmpty()) {
            JSONObject swRef = lineageSourceCitationToJson(proc.getSoftwareReference().get(0));
            if (swRef != null) o.put("software_reference", swRef);
        }
        putOpt(o, "procedure_description", stringFromCharProp(proc.getProcedureDescription()));
        if (proc.isSetDocumentation() && proc.getDocumentation() != null && !proc.getDocumentation().isEmpty()) {
            List<JSONObject> docList = new ArrayList<>();
            for (CICitationPropertyType docProp : proc.getDocumentation()) {
                JSONObject docJson = lineageSourceCitationToJson(docProp);
                if (docJson != null) docList.add(docJson);
            }
            if (!docList.isEmpty()) o.put("documentation", new JSONArray(docList));
        }
        putOpt(o, "run_time_parameters", stringFromCharProp(proc.getRunTimeParameters()));
        if (proc.isSetAlgorithm() && proc.getAlgorithm() != null && !proc.getAlgorithm().isEmpty()) {
            LEAlgorithmPropertyType firstAlg = proc.getAlgorithm().get(0);
            if (firstAlg != null && firstAlg.getLEAlgorithm() != null) {
                JSONObject algJson = algorithmToJson(firstAlg.getLEAlgorithm());
                if (algJson != null) o.put("algorithm", algJson);
            }
        }
        return o.length() > 0 ? o : null;
    }

    private static JSONObject algorithmToJson(LEAlgorithmType alg) {
        if (alg == null) return null;
        JSONObject o = new JSONObject();
        if (alg.getCitation() != null) {
            JSONObject citJson = lineageSourceCitationToJson(alg.getCitation());
            if (citJson != null) o.put("citation", citJson);
        }
        putOpt(o, "description", stringFromCharProp(alg.getDescription()));
        return o.length() > 0 ? o : null;
    }

    private static JSONObject processStepReportToJson(LEProcessStepReportType report) {
        if (report == null) return null;
        JSONObject o = new JSONObject();
        putOpt(o, "description", stringFromCharProp(report.getDescription()));
        putOpt(o, "file_type", stringFromCharProp(report.getFileType()));
        putOpt(o, "name", stringFromCharProp(report.getName()));
        return o.length() > 0 ? o : null;
    }

    private static JSONObject lineageSourceToJson(LISourceType liSource) {
        try {
            String description = ISOMetadata.getStringFromCharacterString(liSource.getDescription());
            JSONObject sourceCitation = lineageSourceCitationToJson(liSource.getSourceCitation());
            JSONObject o = new JSONObject();
            if (description != null) o.put("description", description);
            if (sourceCitation != null && sourceCitation.length() > 0) o.put("source_citation", sourceCitation);
            return o.length() > 0 ? o : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static JSONObject lineageSourceCitationToJson(CICitationPropertyType citationProp) {
        if (citationProp == null) return null;
        CICitationType cit = citationProp.getCICitation();
        if (cit == null) return null;
        JSONObject o = new JSONObject();
        String title = ISOMetadata.getStringFromCharacterString(cit.getTitle());
        if (title != null) o.put("title", title);
        List<JSONObject> onlineResources = new ArrayList<>();
        List<MDIdentifierPropertyType> identifiers = cit.getIdentifier();
        if (identifiers != null) {
            for (MDIdentifierPropertyType idProp : identifiers) {
                if (idProp == null) continue;
                Object idVal = idProp.getMDIdentifier();
                if (idVal instanceof JAXBElement) idVal = ((JAXBElement<?>) idVal).getValue();
                if (idVal instanceof MDIdentifierType) {
                    String code = ISOMetadata.getStringFromCharacterString(((MDIdentifierType) idVal).getCode());
                    if (code != null && !code.isEmpty()) {
                        JSONObject or = new JSONObject();
                        or.put("url", code);
                        onlineResources.add(or);
                    }
                }
            }
        }
        if (!onlineResources.isEmpty()) o.put("online_resource", new JSONArray(onlineResources));
        return o.length() > 0 ? o : null;
    }

    private static List<JSONObject> conformanceResultsFromDataQuality(eu.essi_lab.iso.datamodel.classes.DataQuality dq) {
        List<JSONObject> list = new ArrayList<>();
        try {
            List<?> reports = dq.getReports();
            if (reports == null) return list;
            for (Object r : reports) {
                JSONObject co = conformanceResultToJson(r);
                if (co != null) list.add(co);
            }
        } catch (Exception e) {
            // ignore
        }
        return list;
    }

    private static JSONObject conformanceResultToJson(Object reportElement) {
        try {
            if (reportElement == null || !(reportElement instanceof DQElementPropertyType)) return null;
            JAXBElement<? extends AbstractDQElementType> element = ((DQElementPropertyType) reportElement).getAbstractDQElement();
            if (element == null) return null;
            AbstractDQElementType value = element.getValue();
            if (value == null) return null;
            if (value instanceof DQDomainConsistencyType dqct) {
                List<DQResultPropertyType> resultList = dqct.getResult();
                if (resultList != null && !resultList.isEmpty()) {
                    DQResultPropertyType first = resultList.get(0);
                    JAXBElement<? extends AbstractDQResultType> resultElem = first.getAbstractDQResult();
                    if (resultElem != null) {
                        AbstractDQResultType inner = resultElem.getValue();
                        if (inner instanceof DQConformanceResultType) {
                            return buildConformanceResultJson((DQConformanceResultType) inner);
                        }
                    }
                }
                return null;
            }
            if (!DQConformanceResultType.class.isInstance(value)) return null;
            DQConformanceResultType result = DQConformanceResultType.class.cast(value);
            return buildConformanceResultJson(result);
        } catch (Exception e) {
            return null;
        }
    }

    private static JSONObject buildConformanceResultJson(DQConformanceResultType result) {
        try {
            String specTitle = null;
            String specPublicationDate = null;
            CICitationPropertyType specProp = result.getSpecification();
            if (specProp != null) {
                CICitationType citationRef = specProp.getCICitation();
                if (citationRef != null) {
                    specTitle = ISOMetadata.getStringFromCharacterString(citationRef.getTitle());
                    List<CIDatePropertyType> dateList = citationRef.getDate();
                    if (dateList != null) {
                        for (CIDatePropertyType dateItem : dateList) {
                            if (dateItem == null) continue;
                            CIDateType ciDate = dateItem.getCIDate();
                            if (ciDate == null) continue;
                            CIDateTypeCodePropertyType dateType = ciDate.getDateType();
                            if (dateType == null) continue;
                            Object codeVal = dateType.getCIDateTypeCode();
                            if (codeVal instanceof JAXBElement) codeVal = ((JAXBElement<?>) codeVal).getValue();
                            if (codeVal instanceof CodeListValueType) {
                                String typeStr = ((CodeListValueType) codeVal).getCodeListValue();
                                if (typeStr != null && "publication".equalsIgnoreCase(typeStr)) {
                                    DatePropertyType dateProp = ciDate.getDate();
                                    if (dateProp != null) specPublicationDate = dateProp.getDate();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            String explanation = ISOMetadata.getStringFromCharacterString(result.getExplanation());
            Boolean pass = null;
            BooleanPropertyType passProp = result.getPass();
            if (passProp != null) pass = passProp.isBoolean();
            JSONObject o = new JSONObject();
            if (specTitle != null) o.put("specification_title", specTitle);
            if (specPublicationDate != null) o.put("specification_publication_date", specPublicationDate);
            if (explanation != null) o.put("explanation", explanation);
            if (pass != null) o.put("pass", pass);
            return o.length() > 0 ? o : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static JSONObject qualityScopeObj(eu.essi_lab.iso.datamodel.classes.DataQuality dq) {
        try {
            DQScopePropertyType scopeProp = dq.getElementType().getScope();
            if (scopeProp == null) return null;
            DQScopeType scope = scopeProp.getDQScope();
            if (scope == null) return null;
            String scopeCode = null;
            String scopeDetails = null;
            MDScopeCodePropertyType level = scope.getLevel();
            if (level != null) {
                JAXBElement<CodeListValueType> codeElem = level.getMDScopeCode();
                if (codeElem != null) {
                    CodeListValueType code = codeElem.getValue();
                    if (code != null && code.getCodeListValue() != null) {
                        scopeCode = code.getCodeListValue();
                    }
                }
            }
            List<MDScopeDescriptionPropertyType> levelDescList = scope.getLevelDescription();
            if (levelDescList != null && !levelDescList.isEmpty()) {
                MDScopeDescriptionPropertyType first = levelDescList.get(0);
                if (first != null) {
                    MDScopeDescriptionType desc = first.getMDScopeDescription();
                    if (desc != null) {
                        scopeDetails = ISOMetadata.getStringFromCharacterString(desc.getOther());
                    }
                }
            }
            if (scopeCode == null && scopeDetails == null) return null;
            JSONObject o = new JSONObject();
            if (scopeCode != null) o.put("scope_code", codeObj(scopeCode, "https://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_ScopeCode"));
            if (scopeDetails != null) o.put("scope_details", scopeDetails);
            return o;
        } catch (Exception e) {
            return null;
        }
    }

    private static void mapDatasetSpecific(JSONObject out, CoreMetadata core, Identification identification) {
        if (!(identification instanceof DataIdentification)) return;
        DataIdentification di = (DataIdentification) identification;
        try {
            Iterator<String> langIt = di.getLanguages();
            if (langIt != null && langIt.hasNext()) putOpt(out, "data_language", langIt.next());
            putOpt(out, "parent_identifier", core.getMIMetadata().getParentIdentifier());
            putOpt(out, "dataset_type", codeObj(di.getSpatialRepresentationTypeCodeListValue()));
            Integer equivScale = null;
            Iterator<Integer> denIt = di.getDenominators();
            if (denIt != null && denIt.hasNext()) equivScale = denIt.next();
            if (equivScale != null && equivScale > 0) putOpt(out, "equivalent_scale", equivScale);
            JSONArray topicCategories = new JSONArray();
            Iterator<String> it = di.getTopicCategoriesStrings();
            if (it != null) while (it.hasNext()) topicCategories.put(it.next());
            if (topicCategories.length() > 0) out.put("topic_categories", topicCategories);
            eu.essi_lab.iso.datamodel.classes.MDResolution res = di.getSpatialResolution();
            if (res != null && res.getDistanceValue() != null) {
                JSONObject sr = new JSONObject();
                sr.put("value", res.getDistanceValue());
                sr.put("uom", "m");
                out.put("spatial_resolution", sr);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private static void mapServiceSpecific(JSONObject out, eu.essi_lab.iso.datamodel.classes.MDMetadata mi) {
        try {
            ServiceIdentification svc = mi.getServiceIdentification();
            if (svc == null) return;
            putOpt(out, "service_type", svc.getServiceType());
            putOpt(out, "service_type_version", svc.getServiceTypeVersion());
            putOpt(out, "service_coupling_type", svc.getCouplingType());
            JSONArray operatesOn = new JSONArray();
            Iterator<String> it = svc.getOperatesOnIdentifiers();
            if (it != null) while (it.hasNext()) operatesOn.put(it.next());
            if (operatesOn.length() > 0) out.put("service_operates_on", operatesOn);
            JSONArray serviceOps = serviceOperationsToJson(svc);
            if (serviceOps != null && serviceOps.length() > 0) out.put("service_operations", serviceOps);
        } catch (Exception e) {
            // ignore
        }
    }

    private static void mapModelSpecific(JSONObject out, GSResource resource, CoreMetadata core) {
        if (resource == null) return;
        try {
            ModelSpecificFields m = resource.getExtensionHandler().getModelSpecificFields().orElse(null);
            if (m != null) {
                putOpt(out, "model_maturity_level", m.getModelMaturityLevel());
                putOpt(out, "model_category", m.getModelCategory());
                putOpt(out, "model_methodology_description", m.getModelMethodologyDescription());
                if (m.getModelTypes() != null && !m.getModelTypes().isEmpty()) {
                    JSONArray arr = new JSONArray();
                    for (String t : m.getModelTypes()) arr.put(t);
                    out.put("model_types", arr);
                }
                if (m.getSupportedPlatforms() != null && !m.getSupportedPlatforms().isEmpty()) {
                    JSONArray arr = new JSONArray();
                    for (String p : m.getSupportedPlatforms()) arr.put(p);
                    out.put("supported_platforms", arr);
                }
                if (m.getCpu() != null || m.getGpu() != null || m.getRam() != null || m.getStorage() != null) {
                    JSONObject comp = new JSONObject();
                    putOpt(comp, "cpu", m.getCpu());
                    putOpt(comp, "gpu", m.getGpu());
                    putOpt(comp, "ram", m.getRam());
                    putOpt(comp, "storage", m.getStorage());
                    out.put("model_computational_requirements", comp);
                }
            }
            // model_quality_information from DataQualityInfo (descriptive result + quantitative metrics)
            if (core != null) {
                JSONObject qualityInfo = modelQualityInformationToJson(core);
                if (qualityInfo != null && qualityInfo.length() > 0) {
                    out.put("model_quality_information", qualityInfo);
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private static JSONObject modelQualityInformationToJson(CoreMetadata core) {
        if (core == null) return null;
        try {
            Iterator<eu.essi_lab.iso.datamodel.classes.DataQuality> dqIt = core.getMIMetadata().getDataQualities();
            if (dqIt == null || !dqIt.hasNext()) return null;
            String modelQualityReport = null;
            JSONArray modelMetrics = new JSONArray();
            while (dqIt.hasNext()) {
                eu.essi_lab.iso.datamodel.classes.DataQuality dq = dqIt.next();
                if (dq == null) continue;
                List<net.opengis.iso19139.gmd.v_20060504.DQElementPropertyType> reports = dq.getReports();
                if (reports == null) continue;
                for (net.opengis.iso19139.gmd.v_20060504.DQElementPropertyType report : reports) {
                    if (report == null || !report.isSetAbstractDQElement()) continue;
                    Object elem = report.getAbstractDQElement().getValue();
                    if (!(elem instanceof net.opengis.iso19139.gmd.v_20060504.DQDomainConsistencyType)) continue;
                    net.opengis.iso19139.gmd.v_20060504.DQDomainConsistencyType dqElem =
                        (net.opengis.iso19139.gmd.v_20060504.DQDomainConsistencyType) elem;
                    List<net.opengis.iso19139.gmd.v_20060504.DQResultPropertyType> results = dqElem.getResult();
                    boolean hasQuantitativeResult = results != null && !results.isEmpty();
                    if (!hasQuantitativeResult) {
                        if (modelQualityReport == null && dqElem.isSetMeasureDescription()) {
                            modelQualityReport = ISOMetadata.getStringFromCharacterString(dqElem.getMeasureDescription());
                        }
                    } else {
                        String id = null;
                        String name = null;
                        if (dqElem.isSetNameOfMeasure() && dqElem.getNameOfMeasure() != null && !dqElem.getNameOfMeasure().isEmpty()) {
                            if (dqElem.getNameOfMeasure().size() >= 2) {
                                id = ISOMetadata.getStringFromCharacterString(dqElem.getNameOfMeasure().get(0));
                                name = ISOMetadata.getStringFromCharacterString(dqElem.getNameOfMeasure().get(1));
                            } else {
                                name = ISOMetadata.getStringFromCharacterString(dqElem.getNameOfMeasure().get(0));
                            }
                        }
                        String description = dqElem.isSetMeasureDescription()
                            ? ISOMetadata.getStringFromCharacterString(dqElem.getMeasureDescription()) : null;
                        Double valueNum = null;
                        for (net.opengis.iso19139.gmd.v_20060504.DQResultPropertyType r : results) {
                            if (r == null || !r.isSetAbstractDQResult()) continue;
                            Object resVal = r.getAbstractDQResult().getValue();
                            if (resVal instanceof net.opengis.iso19139.gmd.v_20060504.DQQuantitativeResultType) {
                                valueNum = extractQuantitativeValue((net.opengis.iso19139.gmd.v_20060504.DQQuantitativeResultType) resVal);
                                break;
                            }
                        }
                        if (id != null || name != null || description != null || valueNum != null) {
                            JSONObject metric = new JSONObject();
                            putOpt(metric, "id", id);
                            putOpt(metric, "name", name);
                            putOpt(metric, "description", description);
                            if (valueNum != null && !valueNum.isNaN()) metric.put("value", String.valueOf(valueNum));
                            modelMetrics.put(metric);
                        }
                    }
                }
            }
            if (modelQualityReport == null && modelMetrics.length() == 0) return null;
            JSONObject o = new JSONObject();
            putOpt(o, "model_quality_report", modelQualityReport);
            if (modelMetrics.length() > 0) o.put("model_metrics", modelMetrics);
            return o;
        } catch (Exception e) {
            return null;
        }
    }

    private static Double extractQuantitativeValue(net.opengis.iso19139.gmd.v_20060504.DQQuantitativeResultType quant) {
        if (quant == null || !quant.isSetValue() || quant.getValue() == null || quant.getValue().isEmpty()) return null;
        try {
            net.opengis.iso19139.gco.v_20060504.RecordPropertyType rp = quant.getValue().get(0);
            if (rp == null) return null;
            Object record = rp.getRecord();
            if (record == null) return null;
            try {
                java.lang.reflect.Method getVal = record.getClass().getMethod("getValue");
                Object val = getVal.invoke(record);
                if (val != null) {
                    String s = val.toString();
                    if (s != null && !s.isEmpty()) return Double.parseDouble(s);
                }
            } catch (NoSuchMethodException e) {
                // record type has no getValue, skip
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private static JSONArray serviceOperationsToJson(ServiceIdentification svc) {
        if (svc == null) return null;
        try {
            List<SVOperationMetadataPropertyType> list = svc.getElementType().getContainsOperations();
            if (list == null || list.isEmpty()) return null;
            JSONArray arr = new JSONArray();
            for (SVOperationMetadataPropertyType prop : list) {
                if (prop == null) continue;
                SVOperationMetadataType op = prop.getSVOperationMetadata();
                if (op == null) continue;
                JSONObject o = operationMetadataToJson(op);
                if (o != null) arr.put(o);
            }
            return arr.length() > 0 ? arr : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static JSONObject operationMetadataToJson(SVOperationMetadataType op) {
        if (op == null) return null;
        JSONObject out = new JSONObject();
        putOpt(out, "operation_name", stringFromCharProp(op.getOperationName()));
        putOpt(out, "operation_description", stringFromCharProp(op.getOperationDescription()));
        putOpt(out, "invocation_name", stringFromCharProp(op.getInvocationName()));
        // DCP list
        if (op.isSetDCP() && op.getDCP() != null) {
            JSONArray dcpArr = new JSONArray();
            for (Object dcpProp : op.getDCP()) {
                String code = codeListValueFromDCP(dcpProp);
                if (code != null) dcpArr.put(code);
            }
            if (dcpArr.length() > 0) out.put("dcp", dcpArr);
        }
        // connect_point (array of URLs)
        if (op.isSetConnectPoint() && op.getConnectPoint() != null) {
            JSONArray cpArr = new JSONArray();
            for (Object cpProp : op.getConnectPoint()) {
                String url = urlFromConnectPoint(cpProp);
                if (url != null) cpArr.put(url);
            }
            if (cpArr.length() > 0) out.put("connect_point", cpArr);
        }
        // parameters
        if (op.isSetParameters() && op.getParameters() != null && !op.getParameters().isEmpty()) {
            JSONArray paramsArr = new JSONArray();
            for (SVParameterPropertyType paramProp : op.getParameters()) {
                if (paramProp == null) continue;
                JSONObject p = parameterToJson(paramProp.getSVParameter());
                if (p != null) paramsArr.put(p);
            }
            if (paramsArr.length() > 0) out.put("parameters", paramsArr);
        }
        return out.length() > 0 ? out : null;
    }

    private static String stringFromCharProp(CharacterStringPropertyType p) {
        return p != null ? ISOMetadata.getStringFromCharacterString(p) : null;
    }

    private static String codeListValueFromDCP(Object dcpProp) {
        try {
            if (dcpProp == null) return null;
            Object listObj = dcpProp.getClass().getMethod("getDCPList").invoke(dcpProp);
            if (listObj == null) return null;
            Object code = listObj.getClass().getMethod("getCodeListValue").invoke(listObj);
            return code != null ? code.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String urlFromConnectPoint(Object cpProp) {
        try {
            if (cpProp == null) return null;
            Object online = cpProp.getClass().getMethod("getCIOnlineResource").invoke(cpProp);
            if (online == null) return null;
            Object linkage = online.getClass().getMethod("getLinkage").invoke(online);
            if (linkage == null) return null;
            Object url = linkage.getClass().getMethod("getURL").invoke(linkage);
            return url != null ? url.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static JSONObject parameterToJson(SVParameterType param) {
        if (param == null) return null;
        JSONObject p = new JSONObject();
        String name = null;
        if (param.isSetName() && param.getName() != null && param.getName().getAName() != null) {
            name = ISOMetadata.getStringFromCharacterString(param.getName().getAName());
        }
        putOpt(p, "name", name);
        putOpt(p, "description", stringFromCharProp(param.getDescription()));
        if (param.isSetDirection() && param.getDirection() != null && param.getDirection().getSVParameterDirection() != null) {
            putOpt(p, "direction", param.getDirection().getSVParameterDirection().value());
        }
        if (param.isSetOptionality()) {
            String opt = stringFromCharProp(param.getOptionality());
            if (opt != null) p.put("optionality", Boolean.parseBoolean(opt));
        }
        if (param.isSetRepeatability() && param.getRepeatability() != null && param.getRepeatability().isSetBoolean()) {
            p.put("repeatability", param.getRepeatability().isBoolean());
        }
        return p;
    }
}
