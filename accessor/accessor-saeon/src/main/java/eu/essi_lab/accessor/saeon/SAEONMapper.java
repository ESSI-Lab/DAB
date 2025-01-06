/**
 * 
 */
package eu.essi_lab.accessor.saeon;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.XMLGregorianCalendarUtils;
import eu.essi_lab.model.AccessType;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.OriginalIdentifierMapper;

/**
 * @author Fabrizio
 */
public class SAEONMapper extends OriginalIdentifierMapper {

    /**
     * 
     */
    public static final String SAEON_SCHEME_URI = "https://proto.saeon.ac.za/api/schema/";

    @Override
    protected String createOriginalIdentifier(GSResource resource) {

	String metadata = resource.getOriginalMetadata().getMetadata();
	JSONObject object = new JSONObject(metadata);

	if (object.has("id")) {
	    return object.getString("id");
	}

	return null;
    }

    @Override
    public GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {

	try {

	    String metadata = originalMD.getMetadata();
	    JSONObject object = new JSONObject(metadata);

	    Dataset dataset = new Dataset();

	    dataset.setSource(source);

	    MIMetadata md_Metadata = dataset.getHarmonizedMetadata().getCoreMetadata().getMIMetadata();
	    DataIdentification dataId = md_Metadata.getDataIdentification();

	    String id = "";
	    String sid = "";
	    String doi = "";

	    String collection_id = "";
	    String tags = "";
	    // String longName = "HealthSites - ";
	    String title = "Title not provided";
	    String abstrakt = null;
	    String timeStamp = null;

	    String startDate = null;
	    String endDate = null;

	    Double eastBoundLongitude = null;
	    Double northBoundLatitude = null;
	    Double southBoundLatitude = null;
	    Double westBoundLongitude = null;
	    String language = null;

	    // doi: 10.15493/SAEON.EGAGASINI.10000056 -->
	    // https://catalogue.saeon.ac.za/records/10.15493/SAEON.EGAGASINI.10000056

	    // identifier
	    id = object.optString("id");

	    // identifier
	    doi = object.optString("doi");

	    // sid
	    sid = object.optString("id");

	    // collection_id
	    collection_id = object.optString("collection_id");

	    // tags
	    tags = object.optString("tags");

	    // timestamp
	    timeStamp = object.optString("timestamp");

	    // metadata (JSONArray)
	    JSONArray metadataArray = object.optJSONArray("metadata");
	    JSONObject elements = null;
	    JSONObject metadataObject = null;
	    if(metadataArray == null) {
		metadataArray = object.optJSONArray("metadata_records");
	    }
	    if (metadataArray != null && metadataArray.length() > 0) {

		for (int z = 0; z < metadataArray.length(); z++) {

		    elements = metadataArray.optJSONObject(z);

		    if (elements != null) {
			String schema = elements.optString("schema_id");
			if (schema != null && schema.toLowerCase().contains("iso19115")) {
			    continue;
			}
			metadataObject = elements.optJSONObject("metadata");
		    }

		    if (metadataObject == null) {
			return null;
		    }

		    String publisherName = metadataObject.optString("publisher");
		    String schemaVersion = metadataObject.optString("schemaVersion");
		    String publicationYear = metadataObject.optString("publicationYear");

		    // DATES
		    JSONArray datesArray = metadataObject.optJSONArray("dates");
		    if (datesArray != null && datesArray.length() > 0) {
			String dateString = datesArray.getJSONObject(0).optString("date");
			// should be something like this: 2018-05-31/2018-06-13
			String[] splittedDate = dateString.split("/");
			if (splittedDate.length > 1) {
			    startDate = splittedDate[0];
			    endDate = splittedDate[1];
			} else {
			    startDate = dateString;
			}
		    }
		    // TITLE
		    JSONArray titleArray = metadataObject.optJSONArray("titles");
		    if (titleArray != null && titleArray.length() > 0) {
			title = titleArray.getJSONObject(0).optString("title");
		    }

		    // CREATORS
		    HashMap<String, String> creatorsMap = new HashMap<String, String>();
		    JSONArray creatorsArray = metadataObject.optJSONArray("creators");
		    if (creatorsArray != null && creatorsArray.length() > 0) {
			for (int j = 0; j < creatorsArray.length(); j++) {
			    JSONObject creatorObject = creatorsArray.getJSONObject(j);
			    String creatorName = creatorObject.optString("name");
			    String affiliation = null;
			    JSONArray affilitianArray = creatorObject.optJSONArray("affiliation");
			    if (affilitianArray != null && affilitianArray.length() > 0) {
				affiliation = affilitianArray.getJSONObject(0).optString("affiliation");
			    }
			    creatorsMap.put(creatorName, affiliation);
			}
		    }

		    // language
		    language = metadataObject.optString("language");
		    language = (language != null) ? language : "en";
		    md_Metadata.setLanguage(language);

		    // SUBJECTS (keywords)
		    List<String> keywordList = new ArrayList<String>();
		    JSONArray keywordsArray = metadataObject.optJSONArray("subjects");
		    if (keywordsArray != null && keywordsArray.length() > 0) {
			for (int i = 0; i < keywordsArray.length(); i++) {
			    String key = keywordsArray.getJSONObject(i).optString("subject");
			    keywordList.add(key);
			}
		    }

		    // rightsList
		    JSONArray rightsArray = metadataObject.optJSONArray("rightsList");
		    String rights = null;
		    String rightsURI = null;
		    if (rightsArray != null && rightsArray.length() > 0) {
			rights = rightsArray.getJSONObject(0).optString("rights");
			rightsURI = rightsArray.getJSONObject(0).optString("rightsURI");
		    }

		    // publisher
		    String publisher = metadataObject.optString("publisher");
		    if (publisher != null && !publisher.isEmpty()) {
			ResponsibleParty party = new ResponsibleParty();
			party.setRoleCode("publisher");
			party.setOrganisationName(publisher);
			md_Metadata.getDataIdentification().addPointOfContact(party);
			md_Metadata.addContact(party);
		    }

		    // CONTRIBUTORS
		    HashMap<String, String> contributorsMap = new HashMap<String, String>();
		    JSONArray contributorsArray = metadataObject.optJSONArray("contributors");
		    if (contributorsArray != null && contributorsArray.length() > 0) {
			for (int j = 0; j < contributorsArray.length(); j++) {
			    JSONObject creatorObject = contributorsArray.getJSONObject(j);
			    String contributorName = creatorObject.optString("name");
			    String contributorType = creatorObject.optString("contributorType");
			    String affiliation = null;
			    JSONArray affilitianArray = creatorObject.optJSONArray("affiliation");
			    if (affilitianArray != null && affilitianArray.length() > 0) {
				affiliation = affilitianArray.getJSONObject(0).optString("affiliation");
			    }
			    contributorsMap.put(contributorName + ":" + contributorType, affiliation);
			}
		    }

		    // ABSTRACT (descriptions)
		    JSONArray abstarctArray = metadataObject.optJSONArray("descriptions");
		    if (abstarctArray != null && abstarctArray.length() > 0) {
			abstrakt = abstarctArray.getJSONObject(0).optString("description");
		    }

		    // BBOX (geoLocations)

		    JSONArray bboxArray = metadataObject.optJSONArray("geoLocations");
		    if (bboxArray != null && bboxArray.length() > 0) {
			JSONObject bboxObject = bboxArray.getJSONObject(0).optJSONObject("geoLocationBox");
			if (bboxObject != null) {
			    eastBoundLongitude = bboxObject.optDouble("eastBoundLongitude");
			    northBoundLatitude = bboxObject.optDouble("northBoundLatitude");
			    westBoundLongitude = bboxObject.optDouble("westBoundLongitude");
			    southBoundLatitude = bboxObject.optDouble("southBoundLatitude");
			} else {
			    // lat-long case
			    JSONObject latLonObject = bboxArray.getJSONObject(0).optJSONObject("geoLocationPoint");
			    if (latLonObject != null) {
				eastBoundLongitude = latLonObject.optDouble("pointLongitude");
				northBoundLatitude = latLonObject.optDouble("pointLatitude");
				westBoundLongitude = latLonObject.optDouble("pointLongitude");
				southBoundLatitude = latLonObject.optDouble("pointLatitude");
			    }
			}
		    }

		    // ONLINES (immutableResource)
		    JSONObject immutableResourceObj = metadataObject.optJSONObject("immutableResource");
		    if (immutableResourceObj != null && immutableResourceObj.length() > 0) {
			String resourceDesc = immutableResourceObj.optString("resourceDescription");
			String resourceName = immutableResourceObj.optString("resourceName");
			JSONObject resourceDownload = immutableResourceObj.optJSONObject("resourceDownload");
			if (resourceDownload != null) {
			    String onlineURL = resourceDownload.optString("downloadURL");
			    if (onlineURL != null) {
				Online o = new Online();
				o.setLinkage(onlineURL);
				if (resourceName != null) {
				    o.setName(resourceName);
				}
				if (resourceDesc != null) {
				    o.setDescription(resourceDesc);
				}
				o.setFunctionCode("download");
				o.setProtocol("HTTP");

				o.setDescriptionGmxAnchor(AccessType.DIRECT_ACCESS.getDescriptionAnchor());

				md_Metadata.getDistribution().addDistributionOnline(o);
			    }
			}
		    }

		    // ONLINES (linkedResources)
		    JSONArray linkedResourcesArray = metadataObject.optJSONArray("linkedResources");
		    if (linkedResourcesArray != null && linkedResourcesArray.length() > 0) {
			for (int j = 0; j < linkedResourcesArray.length(); j++) {
			    Online o = new Online();
			    String onlineURL = linkedResourcesArray.getJSONObject(j).optString("resourceURL");
			    String onlineName = linkedResourcesArray.getJSONObject(j).optString("resourceName");
			    String onlineDescription = linkedResourcesArray.getJSONObject(j).optString("resourceDescription");
			    if (onlineURL != null) {
				o.setLinkage(onlineURL);
				if (onlineDescription != null) {
				    o.setDescription(onlineDescription);
				}
				if (onlineName != null) {
				    o.setName(onlineName);
				}

				o.setFunctionCode("download");
				o.setProtocol("HTTP");

				o.setDescriptionGmxAnchor(AccessType.DIRECT_ACCESS.getDescriptionAnchor());

				md_Metadata.getDistribution().addDistributionOnline(o);
			    }
			}
		    }

		    // DOI online
		    if (doi != null && !doi.isEmpty()) {
			Online o = new Online();
			o.setLinkage("https://doi.org/" + doi);
			o.setFunctionCode("download");
			o.setProtocol("HTTP");
			o.setDescription("DOI reference");
			o.setDescriptionGmxAnchor(AccessType.DIRECT_ACCESS.getDescriptionAnchor());
			md_Metadata.getDistribution().addDistributionOnline(o);
		    } else {
			Online o = new Online();
			o.setLinkage("https://catalogue.saeon.ac.za/records/" + id);
			o.setFunctionCode("download");
			o.setProtocol("HTTP");
			o.setDescription("DOI reference");
			o.setDescriptionGmxAnchor(AccessType.DIRECT_ACCESS.getDescriptionAnchor());
			md_Metadata.getDistribution().addDistributionOnline(o);
		    }

		    if (title == null || title.isEmpty()) {
			title = "None";
		    }

		    dataId.setCitationTitle(title);
		    dataId.setAbstract(abstrakt);

		    // TEMPORAL EXTENT
		    if (startDate != null) {
			TemporalExtent timeExtent = new TemporalExtent();
			timeExtent.setId(UUID.randomUUID().toString().substring(0, 6));
			timeExtent.setBeginPosition(startDate);
			if (endDate != null) {
			    timeExtent.setEndPosition(endDate);
			}
			dataId.addTemporalExtent(timeExtent);
		    }

		    try {
			if (timeStamp != null) {
			    Date iso8601 = ISO8601DateTimeUtils.parseISO8601ToDate(timeStamp).get();
			    md_Metadata.setDateStampAsDateTime(XMLGregorianCalendarUtils.createGregorianCalendar(iso8601));
			}
		    } catch (Exception e) {
			// TODO: handle exception
		    }

		    // KEYWORDS
		    Keywords md_Keywords = new Keywords();

		    md_Keywords.addKeyword("SAEON");
		    // md_Keywords.addKeyword("HEALTH");

		    for (String k : keywordList) {
			md_Keywords.addKeyword(k);
		    }

		    dataId.addKeywords(md_Keywords);

		    if (!creatorsMap.isEmpty()) {
			// TODO: affiliation field should be more structured
			for (Map.Entry<String, String> entry : creatorsMap.entrySet()) {
			    ResponsibleParty creatorsContact = new ResponsibleParty();
			    String creatorName = entry.getKey();
			    String affiliation = entry.getValue();
			    creatorsContact.setRoleCode("originator");
			    creatorsContact.setIndividualName(creatorName);
			    dataId.addPointOfContact(creatorsContact);
			    md_Metadata.addContact(creatorsContact);
			}
		    }

		    if (!contributorsMap.isEmpty()) {
			// TODO: affiliation field should be more structured
			for (Map.Entry<String, String> entry : contributorsMap.entrySet()) {
			    ResponsibleParty contributorContact = new ResponsibleParty();
			    String nameAndType = entry.getKey();
			    String affiliation = entry.getValue();
			    String[] splittedNameType = nameAndType.split(":");
			    String contributorName = splittedNameType[0];
			    String type = splittedNameType[1];
			    contributorContact.setRoleCode(type);
			    contributorContact.setIndividualName(contributorName);
			    dataId.addPointOfContact(contributorContact);
			    md_Metadata.addContact(contributorContact);
			}
		    }
		    //
		    // if (attributes.has("addr_street")) {
		    // addr = attributes.getString("addr_street");
		    // }
		    //
		    // if (attributes.has("addr_city")) {
		    // city = attributes.getString("addr_city");
		    // }

		    // if (attributes.has("provincia")) {
		    // state = attributes.getString("provincia");
		    // }
		    //
		    // if (attributes.has("addr_postcode")) {
		    // postalCode = attributes.getString("addr_postcode");
		    // }
		    //
		    // if (attributes.has("email")) {
		    // email = attributes.getString("email");
		    // }
		    //
		    // if (attributes.has("tel")) {
		    // tel = attributes.getString("tel");
		    // }
		    //
		    // if (attributes.has("fax")) {
		    // fax = attributes.getString("fax");
		    // }

		    // contact.setRoleCode("pointOfContact");
		    //
		    // Contact contactInfo = new Contact();
		    // Address address = new Address();
		    //
		    // if (city != null) {
		    // address.setCity(city);
		    // }
		    // if (addr != null) {
		    // address.addDeliveryPoint(addr);
		    // }
		    //
		    // if (postalCode != null) {
		    // address.setPostalCode(postalCode);
		    // }
		    //
		    // if (state != null) {
		    // address.setAdministrativeArea(state);
		    // }
		    //
		    // if (email != null) {
		    // address.addElectronicMailAddress(email);
		    // }
		    //
		    // if (tel != null) {
		    // contactInfo.addPhoneVoice(tel);
		    // }
		    //
		    // if (fax != null) {
		    // contactInfo.addPhoneFax(fax);
		    // }

		    // contactInfo.setAddress(address);
		    // contact.setContactInfo(contactInfo);
		    // dataId.addPointOfContact(contact);

		    //
		    // BBOX
		    //
		    if (eastBoundLongitude != null && westBoundLongitude != null && northBoundLatitude != null
			    && southBoundLatitude != null) {
			dataId.addGeographicBoundingBox(northBoundLatitude, westBoundLongitude, southBoundLatitude, eastBoundLongitude);
		    }

		    // LEGAL CONSTRAINTS
		    if (rights != null && rightsURI != null) {
			LegalConstraints rc = new LegalConstraints();
			rc.addUseLimitation(rights + " - " + rightsURI);
			dataId.addLegalConstraints(rc);
		    }
		}
	    }

	    return dataset;

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).info("Error mapping" + e);
	    return null;
	}
    }

    @Override
    public String getSupportedOriginalMetadataSchema() {

	return SAEON_SCHEME_URI;
    }

}
