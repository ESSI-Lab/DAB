package eu.essi_lab.accessor.invenio;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;

public class INVENIOMapper extends FileIdentifierMapper {

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.INVENIO_NS_URI;
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

	// logger.debug("STARTED mapping for record number {} .", station.getRecordID());

	String resourceTypeId = null;

	JSONObject object = new JSONObject(originalMetadata);

	int size = object.length();
	logger.info("NUMBER OF MAPPAPLE OBJECTS: {}", size);
	if (size != 12) {
	    logger.debug("<<<ABNORMAL NUMBER>>>: {}", size);
	}

	JSONObject parent = object.optJSONObject("parent");
	String parentIdentifier = (parent != null) ? getString(parent, "id") : null;

	JSONObject metadata = object.optJSONObject("metadata");

	// contact point
	JSONArray metadataCreators = getJSONArray(metadata, "creators");

	JSONArray languagesArray = getJSONArray(metadata, "languages");
	JSONArray relatedIdentifiers = getJSONArray(metadata, "related_identifiers");
	JSONArray subjects = getJSONArray(metadata, "subjects");
	JSONArray rights = getJSONArray(metadata, "rights");
	JSONObject resourceType = metadata.optJSONObject("resource_type");
	if (resourceType != null) {
	    resourceTypeId = getString(resourceType, "id");
	}

	String description = getString(metadata, "description");
	String publicationDate = getString(metadata, "publication_date");
	String publisher = getString(metadata, "publisher");
	String title = getString(metadata, "title");
	JSONArray dates = getJSONArray(metadata, "dates");
	String metadataVersion = getString(metadata, "version");

	JSONObject access = object.optJSONObject("access");

	JSONObject version = object.optJSONObject("version");

	String isPublished = getString(object, "is_published");

	String creationDate = getString(object, "created");

	String id = getString(object, "id");

	JSONObject links = object.optJSONObject("links");

	JSONObject pids = object.optJSONObject("pids");

	String updatedDate = getString(object, "updated");

	String revisionId = getString(object, "revision_id");

	Double minLat = null;
	Double minLon = null;
	Double maxLat = null;
	Double maxLon = null;

	// List<SimpleEntry<Double, Double>> latLongs = new ArrayList<>();
	// for (int i = 0; i < cyclesArray.length(); i++) {
	// JSONObject cycle = cyclesArray.getJSONObject(i);
	// Double lat = getDouble(cycle, "lat");
	// Double lon = getDouble(cycle, "lon");
	// SimpleEntry<Double, Double> latLon = new SimpleEntry<>(lat, lon);
	// latLongs.add(latLon);
	// }
	// SimpleEntry<SimpleEntry<Double, Double>, SimpleEntry<Double, Double>> bbox = BBOXUtils.getBBOX(latLongs);
	// SimpleEntry<Double, Double> lowerCorner = bbox.getKey();
	// SimpleEntry<Double, Double> upperCorner = bbox.getValue();
	// minLat = lowerCorner.getKey();
	// minLon = lowerCorner.getValue();
	// maxLat = upperCorner.getKey();
	// maxLon = upperCorner.getValue();

	CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();
	if (languagesArray.length() > 0) {
	    String language = getString(languagesArray.optJSONObject(0), "id");
	    coreMetadata.getMIMetadata().setLanguage(language);
	}

	if (resourceTypeId != null && !(resourceTypeId.contains("series") || resourceTypeId.contains("collection"))) {
	    coreMetadata.getMIMetadata().setHierarchyLevelName(resourceTypeId);
	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue(resourceTypeId);
	}

	// title
	if (title != null && !title.isEmpty()) {
	    // title = (platformType != null && !platformType.isEmpty()) ? title + " - " + platformType : title;
	    coreMetadata.setTitle(title);
	}

	// abstract
	if (description != null && !description.isEmpty()) {
	    // title = (platformType != null && !platformType.isEmpty()) ? title + " - " + platformType : title;
	    coreMetadata.setAbstract(description);
	}

	if (id != null) {
	    coreMetadata.setIdentifier(id);
	    coreMetadata.getMIMetadata().setFileIdentifier(id);
	}
	// keywords
	for (int j = 0; j < subjects.length(); j++) {
	    JSONObject jsonSubject = subjects.optJSONObject(j);
	    if (jsonSubject != null) {
		String keyword = getString(jsonSubject, "subject");
		if (keyword != null)
		    coreMetadata.getMIMetadata().getDataIdentification().addKeyword(keyword);
	    }
	}
	//coreMetadata.getMIMetadata().getDataIdentification().addKeyword("GKH");

	// bbox

	// if (minLat != null && minLon != null) {
	//
	// coreMetadata.addBoundingBox(maxLat, minLon, minLat, maxLon);
	// }
	// temporal extent
	if (creationDate != null && updatedDate != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(creationDate, updatedDate);
	}

	if (updatedDate != null) {
	    coreMetadata.getMIMetadata().getDataIdentification().setCitationRevisionDate(updatedDate);
	    coreMetadata.getMIMetadata().setDateStampAsDate(updatedDate);
	    // SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");// 2020-01-02T06:06:30.000+0000
	    // df.setTimeZone(TimeZone.getTimeZone("UTC"));
	    // Date d;
	    // try {
	    // d = df.parse(endTime);
	    // coreMetadata.getMIMetadata().setDateStampAsDateTime(XMLGregorianCalendarUtils.createGregorianCalendar(d));
	    // } catch (Exception e) {
	    // e.printStackTrace();
	    // }
	}

	// parameters
	// adding extracted information in ISO 19115-2 (where possible) and extended parts
	// PARAMETER IDENTIFIERS

	// for (Object o : variablesArray) {
	// CoverageDescription description = new CoverageDescription();
	// description.setAttributeIdentifier(o.toString());
	// description.setAttributeDescription(o.toString());
	// description.setAttributeTitle(o.toString());
	// coreMetadata.getMIMetadata().addCoverageDescription(description);
	//
	// }

	// instrument
	// // INSTRUMENT IDENTIFIERS
	// for (int j = 0; j < sensorArray.length(); j++) {
	//
	// JSONObject jsonObject = sensorArray.getJSONObject(j);
	// String sensorId = getString(jsonObject, "id");
	// String sensorModel = getString(jsonObject, "model");
	// String sensorMaker = getString(jsonObject, "maker");
	// String sensorSerial = getString(jsonObject, "serial");
	//
	// MIInstrument myInstrument = new MIInstrument();
	// myInstrument.setMDIdentifierTypeIdentifier(sensorSerial);
	// myInstrument.setMDIdentifierTypeCode(sensorId);
	// myInstrument.setDescription("Sensor Model: " + sensorModel + ". Maker: " + sensorMaker);
	// myInstrument.setTitle(sensorModel);
	// // myInstrument.getElementType().getCitation().add(e)
	// coreMetadata.getMIMetadata().addMIInstrument(myInstrument);
	// Keywords keyword = new Keywords();
	// keyword.setTypeCode("instrument");
	// keyword.addKeyword(sensorModel);// or sensorModel
	// coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	// }

	// platform
	// if (platformName != null || platformCode != null || platformDescription != null) {
	// MIPlatform platform = new MIPlatform();
	// platform.setMDIdentifierCode(platformCode);
	// platform.setDescription(platformDescription);
	// Citation platformCitation = new Citation();
	// platformCitation.setTitle(platformName);
	// platform.setCitation(platformCitation);
	// coreMetadata.getMIMetadata().addMIPlatform(platform);
	// Keywords keyword = new Keywords();
	// keyword.setTypeCode("platform");
	// keyword.addKeyword(platformName);// or platformDescription
	// coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	// }

	// if (platformCode != null && !platformCode.isEmpty()) {
	// // OVERVIEW
	//
	// String overview = WMS_URL.replace("ARGO_PLACEHOLDER", platformCode);
	//
	// BrowseGraphic graphic = new BrowseGraphic();
	// graphic.setFileDescription(coreMetadata.getTitle());
	// graphic.setFileName(overview);
	// graphic.setFileType("image/png");
	// coreMetadata.getMIMetadata().getDataIdentification().addGraphicOverview(graphic);
	//
	// // IDENTIFIER
	// try {
	// String identifier = StringUtils.hashSHA1messageDigest(platformCode);
	// coreMetadata.setIdentifier(identifier);
	// coreMetadata.getMIMetadata().setFileIdentifier(identifier);
	// coreMetadata.getDataIdentification().setResourceIdentifier(platformCode);
	// } catch (NoSuchAlgorithmException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (UnsupportedEncodingException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	// legalContraints
	for (int i = 0; i < rights.length(); i++) {
	    JSONObject rightsObject = rights.optJSONObject(i);
	    if (rightsObject != null) {
		String rightsId = getString(rightsObject, "id");
		JSONObject rightsDesc = rightsObject.optJSONObject("description");
		String rightsDescription = rightsDesc != null ? getString(rightsDesc, "en") : null;
		JSONObject rightsTitle = rightsObject.optJSONObject("title");
		String rightsTitleString = rightsTitle != null ? getString(rightsTitle, "en") : null;
		JSONObject rightsProps = rightsObject.optJSONObject("props");
		String rightsUrl = rightsProps != null ? getString(rightsProps, "url") : null;

		if (rightsTitleString != null && rightsDescription != null && rightsUrl != null) {
		    LegalConstraints legal = new LegalConstraints();
		    legal.addUseConstraintsCode("otherRestrictions");
		    legal.addOtherConstraints(rightsTitleString + "\r\n" + rightsDescription + "\r\n" + rightsUrl);
		    coreMetadata.getMIMetadata().getDataIdentification().addLegalConstraints(legal);
		}

		// access.addUseLimitation(rights);
		// access.addOtherConstraints("le");

	    }
	}

	// contact points
	String affiliationName = null;
	String givenName = null;
	String familyName = null;
	String fullName = null;
	String identifierNumber = null;
	String scheme = null;
	for (int i = 0; i < metadataCreators.length(); i++) {
	    JSONObject creatorObject = metadataCreators.optJSONObject(i);
	    if (creatorObject != null) {
		JSONArray affiliationsArray = getJSONArray(creatorObject, "affiliations");
		if (affiliationsArray.length() > 0) {
		    affiliationName = getString(affiliationsArray.optJSONObject(0), "name");
		}
		JSONObject personAndOrg = creatorObject.optJSONObject("person_or_org");
		if (personAndOrg != null) {
		    givenName = getString(personAndOrg, "given_name");
		    familyName = getString(personAndOrg, "family_name");
		    fullName = getString(personAndOrg, "name");
		    JSONArray identifierssArray = getJSONArray(personAndOrg, "identifiers");
		    if (identifierssArray.length() > 0) {
			identifierNumber = getString(identifierssArray.optJSONObject(0), "identifier");
			scheme = getString(identifierssArray.optJSONObject(0), "scheme");
		    }
		}
	    }

	    ResponsibleParty creatorContact = new ResponsibleParty();
	    if (givenName != null && familyName != null) {
		creatorContact.setIndividualName(givenName + " " + familyName);
	    } else if (fullName != null) {
		creatorContact.setIndividualName(familyName);
	    }
	    if (affiliationName != null) {
		creatorContact.setOrganisationName(affiliationName);
	    }

	    creatorContact.setRoleCode("author");
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);

	}

	if (publisher != null) {
	    ResponsibleParty istitutionContact = new ResponsibleParty();
	    istitutionContact.setOrganisationName(publisher);
	    istitutionContact.setRoleCode("publisher");
	    coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(istitutionContact);
	}

	if (links != null) {
	    String accessLink = getString(links, "access_link");
	    String draft = getString(links, "draft");
	    String files = getString(links, "files");
	    String latest = getString(links, "latest");
	    String latest_html = getString(links, "latest_html");
	    String reserve_doi = getString(links, "reserve_doi");
	    String self = getString(links, "self");
	    String self_doi = getString(links, "self_doi");
	    String self_html = getString(links, "self_html");
	    String versions = getString(links, "versions");

	    if (accessLink != null) {
		Online online = new Online();
		online.setLinkage(accessLink);
		online.setProtocol("WWW:LINK-1.0-http–link");
		online.setDescription("Access Link");
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
	    }
	    if (draft != null) {
		Online online = new Online();
		online.setLinkage(draft);
		online.setProtocol("WWW:LINK-1.0-http–link");
		online.setDescription("Draft Link");
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
	    }
	    if (files != null) {
		Online online = new Online();
		online.setLinkage(files);
		online.setProtocol("WWW:LINK-1.0-http–link");
		online.setDescription("Files Link");
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);

		Downloader d = new Downloader();
		String fileResponse = d.downloadOptionalString(files).orElse(null);

		if (fileResponse != null) {
		    JSONObject fileObject = new JSONObject(fileResponse);
		    JSONArray entries = getJSONArray(fileObject, "entries");
		    for (int h = 0; h < entries.length(); h++) {
			JSONObject jsonEntry = entries.optJSONObject(h);
			if (jsonEntry != null) {
			    JSONObject entryLink = jsonEntry.optJSONObject("links");
			    if (entryLink != null) {
				String urlContent = getString(entryLink, "content");
				if (urlContent != null) {
				    Online directOnline = new Online();
				    directOnline.setLinkage(urlContent);
				    directOnline.setProtocol("WWW:DOWNLOAD-1.0-http–download");
				    directOnline.setFunctionCode("download");
				    directOnline.setDescription("Direct Link Download");
				    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(directOnline);
				}
			    }
			}
		    }
		}

	    }
	    if (latest != null) {
		Online online = new Online();
		online.setLinkage(latest);
		online.setProtocol("WWW:LINK-1.0-http–link");
		online.setDescription("Latest Link");
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
	    }
	    if (latest_html != null) {
		Online online = new Online();
		online.setLinkage(latest_html);
		online.setProtocol("WWW:LINK-1.0-http–link");
		online.setDescription("Latest_html Link");
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
	    }
	    if (reserve_doi != null) {
		Online online = new Online();
		online.setLinkage(reserve_doi);
		online.setProtocol("WWW:LINK-1.0-http–link");
		online.setDescription("Reserve_doi Link");
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
	    }
	    if (self != null) {
		Online online = new Online();
		online.setLinkage(self);
		online.setProtocol("WWW:LINK-1.0-http–link");
		online.setDescription("Self Link");
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
	    }
	    if (self_doi != null) {
		Online online = new Online();
		online.setLinkage(self_doi);
		online.setProtocol("WWW:LINK-1.0-http–link");
		online.setDescription("Self_doi Link");
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
	    }
	    if (self_html != null) {
		Online online = new Online();
		online.setLinkage(self_html);
		online.setProtocol("WWW:LINK-1.0-http–link");
		online.setDescription("Self_html Link");
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
	    }
	    if (versions != null) {
		Online online = new Online();
		online.setLinkage(versions);
		online.setProtocol("WWW:LINK-1.0-http–link");
		online.setDescription("Versions Link");
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
	    }

	}

	// if(owner != null || authorName != null || dataCenterName != null || istitutionName != null ) {
	//
	// ResponsibleParty creatorContact = new ResponsibleParty();
	//// Contact info = new Contact();
	//// Online online = new Online();
	//// online.setLinkage("https://www.ana.gov.br/");
	//// info.setOnline(online);
	//// creatorContact.setContactInfo(info);
	// if(authorName != null)
	// creatorContact.setIndividualName(authorName);
	// if(istitutionName != null) {
	// creatorContact.setOrganisationName(istitutionName);
	// }else if(owner != null){
	// creatorContact.setOrganisationName(owner);
	// } else {
	// creatorContact.setOrganisationName(dataCenterName);
	// }
	// creatorContact.setRoleCode("author");
	// coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);
	// }

    }

    private Double getDouble(JSONObject result, String key) {
	try {
	    Double d = result.optDouble(key);
	    if (d == null || d.isNaN()) {
		return null;
	    }
	    return d;
	} catch (Exception e) {
	    logger.warn("Error reading key {}: ", key, e);
	    return null;
	}
    }

    private JSONArray getJSONArray(JSONObject result, String key) {
	try {
	    boolean hasKey = result.has(key);
	    if (!hasKey) {
		return new JSONArray();
	    }
	    JSONArray ret = result.getJSONArray(key);
	    if (ret == null || ret.length() == 0) {
		ret = new JSONArray();
	    }
	    return ret;
	} catch (Exception e) {
	    logger.warn("Error getting json array", e);
	    return new JSONArray();
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
