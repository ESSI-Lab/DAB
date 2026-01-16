package eu.essi_lab.accessor.emobon;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import eu.essi_lab.accessor.eurobis.ld.DCATDataset;
import eu.essi_lab.accessor.eurobis.ld.RDFElement;
import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.LegalConstraints;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * @author boldrini
 */
public class EMOBONMapper extends FileIdentifierMapper {

    public static Double TOL = Math.pow(10, -8);

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.EMOBON_NS_URI;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {
	DatasetCollection dataset = new DatasetCollection();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    private void mapMetadata(OriginalMetadata originalMD, DatasetCollection dataset) {

	GSLoggerFactory.getLogger(getClass()).info("EMOBON Mapper STARTED");

	String originalMetadata = originalMD.getMetadata();

	// Split TTL and JSON parts
	String ttlPart = "";
	String jsonPart = "";
	
	int jsonStart = originalMetadata.indexOf("# RO-Crate JSON Metadata");
	if (jsonStart > 0) {
	    ttlPart = originalMetadata.substring(0, jsonStart).trim();
	    jsonPart = originalMetadata.substring(jsonStart + "# RO-Crate JSON Metadata".length()).trim();
	} else {
	    // If no separator, assume it's all TTL
	    ttlPart = originalMetadata;
	}

	DCATDataset turtle = null;
	JSONObject roCrateJson = null;

	// Parse TTL and extract dataset URI
	String datasetURI = null;
	if (!ttlPart.isEmpty()) {
	    ByteArrayInputStream stream = new ByteArrayInputStream(ttlPart.getBytes());
	    try {
		GSLoggerFactory.getLogger(getClass()).info("Parsing DCAT dataset from TTL");
		turtle = new DCATDataset(stream);
		GSLoggerFactory.getLogger(getClass()).info("Parsed DCAT dataset");
		
		// Extract the dataset URI from the TTL by parsing it
		// The dataset URI is the subject of the statements in the TTL
		org.apache.jena.rdf.model.Model ttlModel = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();
		ByteArrayInputStream ttlStream = new ByteArrayInputStream(ttlPart.getBytes());
		ttlModel.read(ttlStream, null, "TURTLE");
		ttlStream.close();
		
		// Find the first resource that has rdf:type dcat:Dataset
		org.apache.jena.rdf.model.Property typeProp = ttlModel.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
		org.apache.jena.rdf.model.Resource datasetType = ttlModel.createResource("http://www.w3.org/ns/dcat#Dataset");
		org.apache.jena.rdf.model.StmtIterator typeIter = ttlModel.listStatements(null, typeProp, datasetType);
		if (typeIter.hasNext()) {
		    org.apache.jena.rdf.model.Statement stmt = typeIter.nextStatement();
		    org.apache.jena.rdf.model.Resource subject = stmt.getSubject();
		    if (subject != null && subject.getURI() != null) {
			datasetURI = subject.getURI();
			// Convert file:// URI to https:// URL if needed
			if (datasetURI.startsWith("file:///")) {
			    // Extract observatory name and reconstruct as https:// URL
			    int obsIndex = datasetURI.indexOf("observatory-");
			    if (obsIndex >= 0) {
				String obsPart = datasetURI.substring(obsIndex);
				int endIndex = obsPart.indexOf('/', "observatory-".length());
				if (endIndex > 0) {
				    String obsName = obsPart.substring(0, endIndex);
				    datasetURI = "https://data.emobon.embrc.eu/" + obsName + "/";
				} else {
				    datasetURI = "https://data.emobon.embrc.eu/" + obsPart.replace("/", "");
				}
			    }
			}
		    }
		}
		typeIter.close();
		ttlModel.close();
	    } catch (IOException e) {
		e.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).error("Parsing error on DCAT dataset", e);
	    }
	}

	// Parse JSON
	if (!jsonPart.isEmpty()) {
	    try {
		GSLoggerFactory.getLogger(getClass()).info("Parsing RO-Crate JSON metadata");
		roCrateJson = new JSONObject(jsonPart);
		GSLoggerFactory.getLogger(getClass()).info("Parsed RO-Crate JSON metadata");
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error("Error parsing JSON metadata", e);
	    }
	}

	try {

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    coreMetadata.getMIMetadata().setHierarchyLevelName("series");
	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("series");

	    String id = null;
	    String dasid = null;
	    String title = null;
	    String summary = null;
	    String created = null;
	    String modified = null;
	    String license = null;
	    String bbox = null;
	    String datePublished = null;
	    String accessURL = null;
	    String contactEmail = null;
	    String contactPointROR = null;
	    String publisher = null;
	    List<String> keywords = new ArrayList<>();
	    List<String> themes = new ArrayList<>();

	    // Extract from TTL
	    if (turtle != null) {
		// Use the dataset URI extracted from TTL as the identifier
		if (datasetURI != null) {
		    id = datasetURI;
		    dasid = datasetURI.replace("https://data.emobon.embrc.eu/", "").replace("/", "");
		} else {
		    // Fallback: try to get identifier from TTL
		    id = turtle.getElement(RDFElement.IDENTIFIER);
		    if (id != null) {
			dasid = id.replace("https://data.emobon.embrc.eu/", "").replace("/", "");
		    }
		}
		title = turtle.getElement(RDFElement.TITLE);
		summary = turtle.getElement(RDFElement.ABSTRACT);
		created = turtle.getElement(RDFElement.CREATED);
		modified = turtle.getElement(RDFElement.MODIFIED);
		license = turtle.getElement(RDFElement.LICENSE);
		bbox = turtle.getElement(RDFElement.BBOX);
		themes = turtle.getElements(RDFElement.THEMES);
		keywords = turtle.getElements(RDFElement.KEYWORDS);
	    }

	    // Extract from JSON (RO-Crate metadata)
	    if (roCrateJson != null) {
		JSONArray graph = roCrateJson.optJSONArray("@graph");
		if (graph != null) {
		    for (int i = 0; i < graph.length(); i++) {
			JSONObject item = graph.optJSONObject(i);
			if (item != null && "./".equals(item.optString("@id"))) {
			    // This is the main dataset entry
			    if (title == null || title.isEmpty()) {
				title = item.optString("title");
			    }
			    if (summary == null || summary.isEmpty()) {
				summary = item.optString("description");
			    }
			    datePublished = item.optString("datePublished");
			    accessURL = item.optString("accessURL");
			    publisher = item.optString("publisher");
			    if (license == null || license.isEmpty()) {
				license = item.optString("license");
			    }

			    // Contact point
			    Object contactPointObj = item.opt("contactPoint");
			    if (contactPointObj instanceof JSONObject) {
				JSONObject contactPoint = (JSONObject) contactPointObj;
				contactEmail = contactPoint.optString("email");
				// Extract ROR identifier from @id
				String contactPointId = contactPoint.optString("@id");
				if (contactPointId != null && contactPointId.startsWith("https://ror.org/")) {
				    contactPointROR = contactPointId;
				}
			    } else if (contactPointObj instanceof String) {
				// It might be a reference to another object in the graph
				String contactPointId = (String) contactPointObj;
				// Find the contact point in the graph
				for (int j = 0; j < graph.length(); j++) {
				    JSONObject cpItem = graph.optJSONObject(j);
				    if (cpItem != null && contactPointId.equals(cpItem.optString("@id"))) {
					contactEmail = cpItem.optString("email");
					// Extract ROR identifier from @id
					if (contactPointId.startsWith("https://ror.org/")) {
					    contactPointROR = contactPointId;
					}
					break;
				    }
				}
			    }

			    // Keywords from JSON
			    JSONArray jsonKeywords = item.optJSONArray("keywords");
			    if (jsonKeywords != null) {
				for (int k = 0; k < jsonKeywords.length(); k++) {
				    String keyword = jsonKeywords.optString(k);
				    if (keyword != null && !keyword.isEmpty()) {
					keywords.add(keyword);
				    }
				}
			    }
			    break;
			}
		    }
		}
	    }

	    // identifier
	    if (dasid != null && !dasid.isEmpty()) {
		try {
		    String identifier = StringUtils.hashSHA1messageDigest("emobon:" + dasid);
		    coreMetadata.setIdentifier(identifier);
		    dataset.setOriginalId(dasid);
		    coreMetadata.getMIMetadata().setFileIdentifier(identifier);
		} catch (NoSuchAlgorithmException e) {
		    e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
		    e.printStackTrace();
		}
		coreMetadata.getDataIdentification().setResourceIdentifier(dasid);
	    } else {
		GSLoggerFactory.getLogger(getClass()).error("No identifier!");
		return;
	    }

	    // title
	    if (title != null && !title.isEmpty()) {
		coreMetadata.setTitle(title);
	    }

	    // abstract
	    if (summary != null && !summary.isEmpty()) {
		coreMetadata.setAbstract(summary);
	    }

	    // license
	    if (license != null && !license.isEmpty()) {
		LegalConstraints lc = new LegalConstraints();
		lc.addUseLimitation(license);
		coreMetadata.getDataIdentification().addLegalConstraints(lc);
	    }

	    // keywords
	    HashMap<String, Keywords> keywordMap = new HashMap<>();
	    Keywords ksimple = new Keywords();
	    keywordMap.put("", ksimple);
	    ksimple.addKeyword("EMOBON");
	    for (String k : keywords) {
		if (k != null && !k.isEmpty()) {
		    ksimple.addKeyword(k);
		}
	    }

	    Keywords kthemes = new Keywords();
	    keywordMap.put("theme", kthemes);
	    for (String theme : themes) {
		if (theme != null && !theme.isEmpty()) {
		    kthemes.addKeyword(theme);
		}
	    }

	    // Add keywords from TTL if available
	    if (turtle != null) {
		List<List<String>> keywordsLabelsAndURIsAndTypes = turtle.getElementsList(RDFElement.KEYWORDLABELSANDURISANDTYPES);
		for (List<String> k : keywordsLabelsAndURIsAndTypes) {
		    if (k.size() < 3) {
			continue;
		    }
		    String label = k.get(0);
		    String uri = k.get(1);
		    String type = k.get(2);
		    if (type == null) {
			type = "";
		    }
		    Keywords keywordsObj = keywordMap.get(type);
		    if (keywordsObj == null) {
			keywordsObj = new Keywords();
			keywordsObj.setTypeCode(type);
			keywordMap.put(type, keywordsObj);
		    }
		    if (uri != null && !uri.isEmpty()) {
			keywordsObj.addKeyword(label, uri);
		    } else {
			keywordsObj.addKeyword(label);
		    }
		}
	    }

	    for (Keywords keywordsObj : keywordMap.values()) {
		coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keywordsObj);
	    }

	    // online resources
	    if (accessURL != null && !accessURL.isEmpty()) {
		// Transform raw.githubusercontent.com URLs to GitHub archive URLs
		// Pattern: https://raw.githubusercontent.com/{owner}/{repo}/{branch}/ro-crate-metadata.json
		// Transform to: https://github.com/{owner}/{repo}/archive/refs/heads/{branch}.zip
		Pattern rawGitHubPattern = Pattern.compile("^https://raw\\.githubusercontent\\.com/([^/]+)/([^/]+)/([^/]+)/ro-crate-metadata\\.json$");
		Matcher matcher = rawGitHubPattern.matcher(accessURL);
		if (matcher.matches()) {
		    String owner = matcher.group(1);
		    String repo = matcher.group(2);
		    String branch = matcher.group(3);
		    accessURL = String.format("https://github.com/%s/%s/archive/refs/heads/%s.zip", owner, repo, branch);
		}
		
		Online online = new Online();
		online.setLinkage(accessURL);
		online.setProtocol("HTTP");
		online.setFunctionCode("download");
		online.setDescription("RO-Crate Access URL");
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
	    }

	    // Add online resources from TTL if available
	    if (turtle != null) {
		List<List<String>> urlsAndTypes = turtle.getElementsList(RDFElement.URLS_AND_TYPES);
		for (List<String> urlAndType : urlsAndTypes) {
		    String url = urlAndType.get(0);
		    String description = urlAndType.get(1);
		    Online online = new Online();
		    online.setLinkage(url);
		    online.setName(dasid);
		    online.setDescription(description);
		    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
		}
	    }

	    // bbox
	    if (bbox != null && !bbox.isBlank()) {
		try {
		    WKTReader reader = new WKTReader();
		    Geometry geometry = reader.read(bbox.replace("\"",""));
		    Envelope envelope = geometry.getEnvelopeInternal();
		    double w = envelope.getMinX(); // West
		    double e = envelope.getMaxX(); // East
		    double s = envelope.getMinY(); // South
		    double n = envelope.getMaxY(); // North
		    dataset.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(n, w, s, e);
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).warn("Error parsing bbox for dataset {}", dasid, e);
		}
	    } else {
		GSLoggerFactory.getLogger(getClass()).warn("Missing bbox for dataset {}", dasid);
	    }

	    // datestamp
	    if (modified != null && !modified.isBlank()) {
		coreMetadata.getMIMetadata().setDateStampAsDate(modified);
		coreMetadata.getMIMetadata().getDataIdentification().setCitationRevisionDate(modified);
	    } else if (datePublished != null && !datePublished.isBlank()) {
		// Use datePublished from JSON if modified is not available
		coreMetadata.getMIMetadata().setDateStampAsDate(datePublished);
		coreMetadata.getMIMetadata().getDataIdentification().setCitationRevisionDate(datePublished);
	    }

	    if (created != null && !created.isBlank()) {
		coreMetadata.getMIMetadata().getDataIdentification().setCitationCreationDate(created);
	    } else if (datePublished != null && !datePublished.isBlank()) {
		coreMetadata.getMIMetadata().getDataIdentification().setCitationCreationDate(datePublished);
	    }

	    // temporal extent
	    TemporalExtent tempExtent = new TemporalExtent();
	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(tempExtent);

	    if (turtle != null) {
		String startDate = turtle.getElement(RDFElement.STARTDATE);
		String endDate = turtle.getElement(RDFElement.ENDDATE);
		String endDateInProgress = turtle.getElement(RDFElement.ENDDATEINPROGRESS);

		boolean missing = true;
		if (startDate != null && !startDate.isBlank()) {
		    tempExtent.setBeginPosition(startDate);
		    missing = false;
		}

		if (endDate != null && !endDate.isBlank()) {
		    tempExtent.setEndPosition(endDate);
		} else {
		    if (endDateInProgress != null && endDateInProgress.toLowerCase().contains("in progress")) {
			tempExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
		    } else {
			GSLoggerFactory.getLogger(getClass()).warn("Missing end time for dataset {}", dasid);
		    }
		}
	    }

	    // contact point / publisher
	    if (contactEmail != null && !contactEmail.isEmpty() || contactPointROR != null) {
		ResponsibleParty contact = new ResponsibleParty();
		Contact contactInfo = new Contact();
		Address address = new Address();
		if (contactEmail != null && !contactEmail.isEmpty()) {
		    address.addElectronicMailAddress(contactEmail);
		}
		contactInfo.setAddress(address);
		contact.setContactInfo(contactInfo);
		
		// Set organization name/identifier from ROR if available
		if (contactPointROR != null) {
		    // Fetch organization information from ROR API
		    ROROrganizationInfo rorInfo = fetchROROrganizationInfo(contactPointROR);
		    if (rorInfo != null) {
			// Set organization name
			if (rorInfo.name != null && !rorInfo.name.isEmpty()) {
			    contact.getElementType().setOrganisationName(ISOMetadata.createAnchorPropertyType(contactPointROR, rorInfo.name));
			} else {
			    contact.getElementType().setOrganisationName(ISOMetadata.createAnchorPropertyType(contactPointROR, contactPointROR));
			}
			
			// Add address information from ROR
			if (rorInfo.address != null && !rorInfo.address.isEmpty()) {
			    address.addDeliveryPoint(rorInfo.address);
			}
			if (rorInfo.city != null && !rorInfo.city.isEmpty()) {
			    address.addCity(rorInfo.city);
			}
			if (rorInfo.country != null && !rorInfo.country.isEmpty()) {
			    address.addCountry(rorInfo.country);
			}
			
			// Add website if available
			if (rorInfo.website != null && !rorInfo.website.isEmpty()) {
			    Online online = new Online();
			    online.setLinkage(rorInfo.website);
			    contactInfo.setOnline(online);
			}
		    } else {
			// Fallback: use ROR URL as organization name
			contact.getElementType().setOrganisationName(ISOMetadata.createAnchorPropertyType(contactPointROR, contactPointROR));
		    }
		}
		
		contact.setRoleCode("pointOfContact");
		coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(contact);
	    }

	    if (publisher != null && !publisher.isEmpty()) {
		ResponsibleParty publisherContact = new ResponsibleParty();
		publisherContact.getElementType().setOrganisationName(ISOMetadata.createAnchorPropertyType(publisher, publisher));
		publisherContact.setRoleCode("publisher");
		coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(publisherContact);
	    }

	    // Add creators from TTL if available
	    if (turtle != null) {
		List<String> creatorsURIs = turtle.getElements(RDFElement.CREATORS);
		for (String creatorsURI : creatorsURIs) {
		    if (creatorsURI != null && !creatorsURI.contains("id/person")) {
			ResponsibleParty creatorContact = new ResponsibleParty();
			creatorContact.getElementType().setOrganisationName(ISOMetadata.createAnchorPropertyType(creatorsURI, creatorsURI));
			creatorContact.setRoleCode("author");
			coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);
		    }
		}
	    }

	    // instruments from TTL
	    if (turtle != null) {
		Keywords instrumentKeywords = new Keywords();
		coreMetadata.getMIMetadata().getDataIdentification().addKeywords(instrumentKeywords);
		instrumentKeywords.setTypeCode("instrument");

		List<String> instruments = turtle.getElements(RDFElement.INSTRUMENTS);
		for (String instrument : instruments) {
		    if (instrument != null && !instrument.isBlank()) {
			MIInstrument myInstrument = new MIInstrument();
			myInstrument.setTitle(instrument);
			coreMetadata.getMIMetadata().addMIInstrument(myInstrument);
			instrumentKeywords.addKeyword(instrument);
		    }
		}

		List<List<String>> instrumentLabelsAndURIs = turtle.getElementsList(RDFElement.INSTRUMENTLABELSANDURIS);
		for (List<String> instrumentLabelAndURI : instrumentLabelsAndURIs) {
		    String label = instrumentLabelAndURI.get(0);
		    String uri = instrumentLabelAndURI.get(1);
		    MIInstrument myInstrument = new MIInstrument();
		    myInstrument.setMDIdentifierTypeIdentifier(uri);
		    myInstrument.setTitle(label);
		    coreMetadata.getMIMetadata().addMIInstrument(myInstrument);
		    instrumentKeywords.addKeyword(label, uri);
		}
	    }

	    // parameters from TTL
	    if (turtle != null) {
		List<List<String>> parameterLabelsAndURIs = turtle.getElementsList(RDFElement.PARAMETERLABELSANDURIS);
		for (List<String> parameterLabelAndURI : parameterLabelsAndURIs) {
		    CoverageDescription descr = new CoverageDescription();
		    descr.setAttributeTitle(parameterLabelAndURI.get(0));
		    descr.setAttributeIdentifier(parameterLabelAndURI.get(1));
		    descr.setAttributeDescription(parameterLabelAndURI.get(0));
		    coreMetadata.getMIMetadata().addCoverageDescription(descr);
		}
		List<String> parameters = turtle.getElements(RDFElement.PARAMETERS);
		for (String parameter : parameters) {
		    CoverageDescription descr = new CoverageDescription();
		    descr.setAttributeTitle(parameter);
		    descr.setAttributeDescription(parameter);
		    coreMetadata.getMIMetadata().addCoverageDescription(descr);
		}
	    }

	    GSLoggerFactory.getLogger(getClass()).info("EMOBON Mapper ENDED");

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error in EMOBON mapper", e);
	    e.printStackTrace();
	}

    }

    /**
     * Fetches organization information from ROR API
     * 
     * @param rorURL The ROR URL (e.g., https://ror.org/0038zss60)
     * @return ROROrganizationInfo object with organization details, or null if fetch fails
     */
    private ROROrganizationInfo fetchROROrganizationInfo(String rorURL) {
	if (rorURL == null || !rorURL.startsWith("https://ror.org/")) {
	    return null;
	}
	
	try {
	    // Extract ROR ID from URL (e.g., "0038zss60" from "https://ror.org/0038zss60")
	    String rorId = rorURL.replace("https://ror.org/", "");
	    String apiURL = "https://api.ror.org/v2/organizations/" + rorId;
	    
	    Downloader downloader = new Downloader();
	    Optional<String> response = downloader.downloadOptionalString(apiURL);
	    
	    if (response.isPresent()) {
		JSONObject rorData = new JSONObject(response.get());
		ROROrganizationInfo info = new ROROrganizationInfo();
		
		// Extract organization name (prefer primary name)
		info.name = rorData.optString("name");
		if (info.name == null || info.name.isEmpty()) {
		    // Try to get from names array - prefer ror_display or label over acronym
		    JSONArray names = rorData.optJSONArray("names");
		    if (names != null && names.length() > 0) {
			// First pass: look for ror_display or label
			for (int i = 0; i < names.length(); i++) {
			    JSONObject nameObj = names.optJSONObject(i);
			    if (nameObj != null) {
				JSONArray types = nameObj.optJSONArray("types");
				if (types != null && (hasType(types, "ror_display") || hasType(types, "label"))) {
				    info.name = nameObj.optString("value");
				    if (info.name != null && !info.name.isEmpty()) {
					break;
				    }
				}
			    }
			}
			// Second pass: if no ror_display/label, use first non-acronym
			if (info.name == null || info.name.isEmpty()) {
			    for (int i = 0; i < names.length(); i++) {
				JSONObject nameObj = names.optJSONObject(i);
				if (nameObj != null) {
				    JSONArray types = nameObj.optJSONArray("types");
				    if (types == null || !hasType(types, "acronym")) {
					info.name = nameObj.optString("value");
					if (info.name != null && !info.name.isEmpty()) {
					    break;
					}
				    }
				}
			    }
			}
			// Fallback: use first available name
			if ((info.name == null || info.name.isEmpty()) && names.length() > 0) {
			    JSONObject firstName = names.optJSONObject(0);
			    if (firstName != null) {
				info.name = firstName.optString("value");
			    }
			}
		    }
		}
		
		// Extract address information
		JSONArray addresses = rorData.optJSONArray("addresses");
		if (addresses != null && addresses.length() > 0) {
		    JSONObject address = addresses.optJSONObject(0);
		    if (address != null) {
			// Build address string from components
			StringBuilder addrBuilder = new StringBuilder();
			if (address.has("line1")) {
			    addrBuilder.append(address.optString("line1"));
			}
			if (address.has("line2")) {
			    if (addrBuilder.length() > 0) addrBuilder.append(", ");
			    addrBuilder.append(address.optString("line2"));
			}
			info.address = addrBuilder.toString();
			
			info.city = address.optString("city");
			info.state = address.optString("state");
			info.postcode = address.optString("postcode");
			
			// Get country from location if available
			if (address.has("geonames_details")) {
			    JSONObject geonames = address.optJSONObject("geonames_details");
			    if (geonames != null) {
				info.country = geonames.optString("country_name");
			    }
			}
			if (info.country == null || info.country.isEmpty()) {
			    info.country = address.optString("country_geonames_name");
			}
		    }
		}
		
		// Extract website from links
		JSONArray links = rorData.optJSONArray("links");
		if (links != null) {
		    for (int i = 0; i < links.length(); i++) {
			JSONObject link = links.optJSONObject(i);
			if (link != null && "website".equals(link.optString("type"))) {
			    info.website = link.optString("value");
			    break;
			}
		    }
		}
		
		return info;
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Failed to fetch ROR organization info for: " + rorURL, e);
	}
	
	return null;
    }
    
    private boolean hasType(JSONArray types, String type) {
	if (types == null) return false;
	for (int i = 0; i < types.length(); i++) {
	    if (type.equals(types.optString(i))) {
		return true;
	    }
	}
	return false;
    }
    
    /**
     * Helper class to hold ROR organization information
     */
    private static class ROROrganizationInfo {
	String name;
	String address;
	String city;
	String state;
	String postcode;
	String country;
	String website;
    }

}

