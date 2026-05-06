package eu.essi_lab.accessor.eurobis.ld;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

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
public class EurOBISLdMapper extends FileIdentifierMapper {

    /** Marineinfo project IRI prefix; {@code ProID} from dataset JSON appends the numeric id. */
    public static final String MARINEINFO_PROJECT_URI_PREFIX = "https://marineinfo.org/id/project/";

    public static Double TOL = Math.pow(10, -8);

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.EUROBIS_LD_NS_URI;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {
	DatasetCollection dataset = new DatasetCollection();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    // public static String[] ECOTAXA_IDs = new String[] { "6505", //
    // "6506", //
    // "6507", //
    // "6508", //
    // "6510", //
    // "6511", //
    // "6512", //
    // "6513", //
    // "6514", //
    // "6515" // ;
    // };

    private void mapMetadata(OriginalMetadata originalMD, DatasetCollection dataset) {

	GSLoggerFactory.getLogger(getClass()).info("EurOBIS-LD Mappper STARTED");

	String originalMetadata = originalMD.getMetadata();

	ByteArrayInputStream stream = new ByteArrayInputStream(originalMetadata.getBytes());

	DCATDataset turtle = null;
	try {
	    GSLoggerFactory.getLogger(getClass()).info("Parsing DCAT dataset");
	    turtle = new DCATDataset(stream);
	    GSLoggerFactory.getLogger(getClass()).info("Parsed DCAT dataset");
	} catch (IOException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error("Parsing error on DCAT dataset");
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	try {

	    String id = turtle.getElement(RDFElement.IDENTIFIER);
	    String dasid = id.replace("https://marineinfo.org/id/dataset/", "");
	    String title = turtle.getElement(RDFElement.TITLE);
	    String alternateTitle = turtle.getElement(RDFElement.ALTERNATENAME);
	    String summary = turtle.getElement(RDFElement.ABSTRACT);
	    String created = turtle.getElement(RDFElement.CREATED);
	    String modified = turtle.getElement(RDFElement.MODIFIED);
	    String license = turtle.getElement(RDFElement.LICENSE);
	    List<String> citations = turtle.getElements(RDFElement.CITATIONS);
	    String startDate = turtle.getElement(RDFElement.STARTDATE);
	    if (startDate!=null){
		startDate= startDate.replace("\"","");
	    }
	    String endDate = turtle.getElement(RDFElement.ENDDATE);
	    if (endDate!=null){
		endDate= endDate.replace("\"","");
	    }
	    String endDateInProgress = turtle.getElement(RDFElement.ENDDATEINPROGRESS);
	    String bbox = turtle.getElement(RDFElement.BBOX);
	    List<String> themes = turtle.getElements(RDFElement.THEMES);
	    List<List<String>> keywordsLabelsAndURIsAndThesaurusAndTypes = turtle.getElementsList(RDFElement.KEYWORDLABELSANDURISANDTYPES);
	    List<String> simpleKeywords = turtle.getElements(RDFElement.KEYWORDS);
	    List<String> parameters = turtle.getElements(RDFElement.PARAMETERS);
	    List<List<String>> parameterLabelsAndURIs = turtle.getElementsList(RDFElement.PARAMETERLABELSANDURIS);
	    List<String> instruments = turtle.getElements(RDFElement.INSTRUMENTS);
	    List<List<String>> instrumentLabelsAndURIs = turtle.getElementsList(RDFElement.INSTRUMENTLABELSANDURIS);
	    List<List<String>> urlsAndTypes = turtle.getElementsList(RDFElement.URLS_AND_TYPES);
	    List<String> creatorsURIs = turtle.getElements(RDFElement.CREATORS);
	    List<String> parents = turtle.getElements(RDFElement.ISPARTOF);
	    List<String> children = turtle.getElements(RDFElement.HASPART);

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    coreMetadata.getMIMetadata().setHierarchyLevelName("series");
	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("series");

	    // identifier
	    if (dasid != null && !dasid.isEmpty()) {

		try {
		    String identifier = StringUtils.hashSHA1messageDigest("eurobis:" + dasid);
		    coreMetadata.setIdentifier(identifier);
		    dataset.setOriginalId(dasid);
		    coreMetadata.getMIMetadata().setFileIdentifier(identifier);
		    if (parents!=null) {
			for(String parent:parents) {
			    coreMetadata.getDataIdentification().addAggregateInformation(parent,"largerWorkCitation");
			}
		    }
		    if (children!=null) {
			for(String child:children) {
			    coreMetadata.getDataIdentification().addAggregateInformation(child,"isComposedOf");
			}
		    }
		} catch (NoSuchAlgorithmException e) {
		    e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
		    e.printStackTrace();
		}
		// GSLoggerFactory.getLogger(getClass()).info("Alternative ID: " + dasid);
		// String[] splittedId = null;
		// if (dasid.contains("r=dasid_")) {
		// splittedId = dasid.split("r=dasid_");
		// } else if (dasid.contains("r=pangaea_")) {
		// splittedId = dasid.split("r=pangaea_");
		// } else if (dasid.contains("oai:marineinfo.org:id:dataset:")) {
		// splittedId = dasid.split("oai:marineinfo.org:id:dataset:");
		// } else {
		// splittedId = dasid.split("r=");
		// }

		// String onlineResource = "";

		// if (splittedId.length < 2) {
		// Node[] nodesIds =
		// turtle.evaluateNodes("//*:eml/*:dataset/*:alternateIdentifier");
		// if (nodesIds.length > 1) {
		// String alternateId = nodesIds[1].getTextContent();
		// if (alternateId.contains("r=dasid_")) {
		// resourceId = alternateId.split("r=dasid_")[1];
		// } else if (alternateId.contains("r=pangaea_")) {
		// resourceId = alternateId.split("r=pangaea_")[1];
		// } else {
		// resourceId = alternateId.split("r=")[1];
		// }
		// // onlineResource = alternateId.replace("resource?", "archive.do?");
		// } else {
		// GSLoggerFactory.getLogger(getClass()).info("UNREACHABLE POINT!!");
		// }
		//
		// } else {
		// resourceId = splittedId[1];
		// // onlineResource = id.replace("resource?", "archive.do?");
		// }
		boolean ecotaxaFound = false;
		// for (String ecotaxaId : ECOTAXA_IDs) {
		// if (ecotaxaId.equalsIgnoreCase(dasid)) {
		// ecotaxaFound = true;
		// break;
		// }
		// }
		for (String k : simpleKeywords) {
		    if (k.toLowerCase().contains("ecotaxa")) {
			ecotaxaFound = true;
			break;
		    }
		}

		if (ecotaxaFound) {
		    GSLoggerFactory.getLogger(getClass()).info("ExoTaxa id found: {}", dasid);

		    for (List<String> urlAndType : urlsAndTypes) {
			String url = urlAndType.get(0);
			String type = urlAndType.get(1);
			if (url.contains("ecotaxa.obs-vlfr.fr")) {
			    Downloader d = new Downloader();
			    Optional<String> res = d.downloadOptionalString(url);
			    if (res.isPresent()) {
				JSONObject json = new JSONObject(res.get());
				String projects = json.optString("project_ids");
				String ids = projects.replace("[", "").replace("]", "");
				coreMetadata.getDataIdentification().setResourceIdentifier(ids);
			    }
			}
		    }
		} else {
		    coreMetadata.getDataIdentification().setResourceIdentifier(dasid);
		}

		// online

		String wfsRequest = "http://geo.vliz.be/geoserver/wfs/ows?service=WFS&version=1.1.0&request=GetFeature&typeName=Dataportal:eurobis-obisenv_basic&resultType=results&viewParams=where:datasetid+IN+("
			+ dasid + ");context:0100&outputFormat=csv";
		Online online = new Online();
		online.setLinkage(wfsRequest);
		online.setProtocol("HTTP");
		online.setFunctionCode("download");
		online.setDescription("Direct Download");
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);

	    } else {
		GSLoggerFactory.getLogger(getClass()).error("No identifier!");
	    }

	    // title
	    if (title != null && !title.isEmpty()) {
		coreMetadata.setTitle(title);
	    }
	    if (alternateTitle != null && !alternateTitle.isBlank()) {
		coreMetadata.getDataIdentification().setCitationAlternateTitle(alternateTitle);
	    }
	    // abstract
	    if (summary != null && !summary.isEmpty()) {
		coreMetadata.setAbstract(summary);
	    }
	    LegalConstraints lc = new LegalConstraints();
	    lc.addUseLimitation(license);
	    coreMetadata.getDataIdentification().addLegalConstraints(lc);
	    // keywords and PARAMETER IDENTIFIERS
	    HashMap<String, Keywords> keywordMap = new HashMap<>();
	    Keywords ksimple = new Keywords();
	    keywordMap.put("", ksimple);
	    ksimple.addKeyword("EurOBIS");
	    for (String k : simpleKeywords) {
		ksimple.addKeyword(k);
	    }

	    Keywords kthemes = new Keywords();
	    keywordMap.put("theme", kthemes);
	    for (String theme : themes) {
		kthemes.addKeyword(theme);
	    }

	    for (List<String> k : keywordsLabelsAndURIsAndThesaurusAndTypes) {
		if (k.size() < 4) {
		    continue;
		}
		String label = k.get(0);
		String uri = k.get(1);
		String thesaurus = k.get(2);
		if (thesaurus == null) {
		    thesaurus = "";
		}
		String rawAdditionalType = k.get(3);
		String rawAlternativeType = k.size() >= 5 ? k.get(4) : "";
		String type = resolveDefinedTermKeywordType(rawAdditionalType, rawAlternativeType);
		Keywords keywords = keywordMap.get(type);
		if (keywords == null) {
		    keywords = new Keywords();
		    keywords.setTypeCode(type);
		    keywords.setThesaurusNameCitationTitle(thesaurus);
		    keywordMap.put(type, keywords);
		}
		if (uri != null) {
		    keywords.addKeyword(label, uri);
		} else {
		    keywords.addKeyword(label);
		}
	    }

	    if (dasid != null && !dasid.isEmpty()) {
		addDatasetJsonProjectKeywords(keywordMap, dasid);
	    }

	    for (Keywords keywords : keywordMap.values()) {
		coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keywords);
	    }

	    for (List<String> parameterLabelAndURI : parameterLabelsAndURIs) {
		CoverageDescription descr = new CoverageDescription();
		descr.setAttributeTitle(parameterLabelAndURI.get(0));
		descr.setAttributeIdentifier(parameterLabelAndURI.get(1));
		descr.setAttributeDescription(parameterLabelAndURI.get(0));
		coreMetadata.getMIMetadata().addCoverageDescription(descr);
	    }
	    for (String parameter : parameters) {
		CoverageDescription descr = new CoverageDescription();
		descr.setAttributeTitle(parameter);
		descr.setAttributeDescription(parameter);
		coreMetadata.getMIMetadata().addCoverageDescription(descr);
	    }

	    // platform
	    // for (Node node : platformList) {
	    // String platformDescription = turtle.evaluateString(node, "*:description");
	    // String platformIdentifier = turtle.evaluateString(node, "@id");
	    // MIPlatform platform = new MIPlatform();
	    // platform.setMDIdentifierCode(platformIdentifier);
	    // platform.setDescription(platformDescription);
	    // Citation platformCitation = new Citation();
	    // platformCitation.setTitle(platformDescription);
	    // platform.setCitation(platformCitation);
	    // coreMetadata.getMIMetadata().addMIPlatform(platform);
	    // Keywords keyword = new Keywords();
	    // keyword.setTypeCode("platform");
	    // keyword.addKeyword(platformDescription);// or platformDescription
	    // coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	    //
	    // }

	    // online resources
	    for (List<String> urlAndType : urlsAndTypes) {
		String url = urlAndType.get(0);
		String description = urlAndType.get(1);
		String name = dasid;
		Online online = new Online();
		online.setLinkage(url);
		// online.setProtocol("HTTP");
		online.setName(name);
		// online.setFunctionCode(function);
		online.setDescription(description);
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
	    }

	    // if (parameters != null && !parameters.isEmpty()) {
	    //
	    // String[] splittedKeyword = parameters.split(",");
	    // for (String s : splittedKeyword) {
	    // s = s.trim();
	    // String coverageAttr = "";
	    // String coverageAttrURI = "";
	    // for (Node node : taxonomicCoverage) {
	    // String taxonName = turtle.evaluateString(node,
	    // "*:taxonomicClassification/*:taxonRankName");
	    // String taxonValue = turtle.evaluateString(node,
	    // "*:taxonomicClassification/*:taxonRankValue");
	    // String taxonURI = turtle.evaluateString(node, "*:generalTaxonomicCoverage");
	    // if (taxonName != null && !taxonName.isEmpty()) {
	    // coverageAttr = coverageAttr + taxonName + " | ";
	    // }
	    // if (taxonValue != null && !taxonValue.isEmpty()) {
	    // coverageAttr = coverageAttr + taxonValue + " | ";
	    // }
	    // if (taxonURI != null && !taxonURI.isEmpty()) {
	    // coverageAttrURI = coverageAttrURI + taxonURI + " | ";
	    // }
	    // }
	    //
	    // if (!coverageAttr.isEmpty()) {
	    // coverageAttr = coverageAttr.substring(0, coverageAttr.length() - 2);
	    // CoverageDescription coverageDescription = new CoverageDescription();
	    // if (!coverageAttrURI.isEmpty()) {
	    // coverageAttrURI = coverageAttrURI.substring(0, coverageAttrURI.length() - 2);
	    // coverageDescription.setAttributeIdentifier(coverageAttrURI);
	    // }
	    // coverageDescription.setAttributeDescription(coverageAttr);
	    // // coverageDescription.setAttributeTitle(coverageAttr);
	    // coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);
	    //
	    // }

	    // bbox

	    if (bbox != null && !bbox.isBlank()) {
		WKTReader reader = new WKTReader();
		Geometry geometry = reader.read(bbox);
		Envelope envelope = geometry.getEnvelopeInternal();
		double w = envelope.getMinX(); // West
		double e = envelope.getMaxX(); // East
		double s = envelope.getMinY(); // South
		double n = envelope.getMaxY(); // North
		dataset.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(n, w, s, e);

	    } else {
		GSLoggerFactory.getLogger(getClass()).warn("Missing bbox for dataset {}", dasid);
	    }

	    // datestamp
	    if (modified != null && !modified.isBlank()) {
		coreMetadata.getMIMetadata().setDateStampAsDate(modified);
		coreMetadata.getMIMetadata().getDataIdentification().setCitationRevisionDate(modified);
	    }
	    if (created != null && !created.isBlank()) {
		coreMetadata.getMIMetadata().getDataIdentification().setCitationCreationDate(created);
	    }
	    // modified
	    // temporal extent

	    TemporalExtent tempExtent = new TemporalExtent();
	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(tempExtent);

	    boolean missing = true;
	    if (startDate != null && !startDate.isBlank()) {
		tempExtent.setBeginPosition(startDate);
		missing = false;
	    }

	    if (endDate != null && !endDate.isBlank()) {
		tempExtent.setEndPosition(startDate);
	    } else {
		if (endDateInProgress != null && endDateInProgress.toLowerCase().contains("in progress")) {
		    tempExtent.setIndeterminateEndPosition(TimeIndeterminateValueType.NOW);
		} else {
		    GSLoggerFactory.getLogger(getClass()).warn("Missing time for dataset {}", dasid);
		}

	    }

	    // // instrument

	    Keywords instrumentKeywords = new Keywords();
	    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(instrumentKeywords);
	    instrumentKeywords.setTypeCode("instrument");
	    for (String instrument : instruments) {
		if (instrument != null && !instrument.isBlank()) {
		    MIInstrument myInstrument = new MIInstrument();
		    // myInstrument.setMDIdentifierTypeIdentifier(instrument);
		    // myInstrument.setMDIdentifierTypeCode(instrument);
		    // myInstrument.setDescription(instrumentDescription);
		    myInstrument.setTitle(instrument);
		    coreMetadata.getMIMetadata().addMIInstrument(myInstrument);
		    instrumentKeywords.addKeyword(instrument);// or sensorModel
		}
	    }

	    for (List<String> instrumentLabelAndURI : instrumentLabelsAndURIs) {
		String label = instrumentLabelAndURI.get(0);
		String uri = instrumentLabelAndURI.get(1);
		MIInstrument myInstrument = new MIInstrument();
		myInstrument.setMDIdentifierTypeIdentifier(uri);
		// myInstrument.setMDIdentifierTypeCode(instrument);
		// myInstrument.setDescription(instrumentDescription);
		myInstrument.setTitle(label);
		coreMetadata.getMIMetadata().addMIInstrument(myInstrument);
		instrumentKeywords.addKeyword(label, uri);// or sensorModel

	    }

	    // // // INSTRUMENT IDENTIFIERS
	    // if (instrumentName != null && !instrumentName.isEmpty()) {
	    //
	    // MIInstrument myInstrument = new MIInstrument();
	    // String id = (instrumentId != null && !instrumentId.isEmpty()) ? instrumentId
	    // : instrumentName;
	    // String type = (orbitType != null && !orbitType.isEmpty()) ? orbitType : id;
	    // myInstrument.setMDIdentifierTypeIdentifier(id);
	    // myInstrument.setMDIdentifierTypeCode(param);
	    // myInstrument.setDescription(instrumentName);
	    // myInstrument.setTitle(instrumentName);
	    // // myInstrument.getElementType().getCitation().add(e)
	    // coreMetadata.getMIMetadata().addMIInstrument(myInstrument);
	    // Keywords keyword = new Keywords();
	    // keyword.setTypeCode("instrument");
	    // keyword.addKeyword(instrumentName);// or sensorModel
	    // coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	    // }
	    //
	    // // platform
	    // if (platformId != null && !platformId.isEmpty()) {
	    // MIPlatform platform = new MIPlatform();
	    // platform.setMDIdentifierCode(platformId);
	    // platformDescription = (platformDescription != null &&
	    // !platformDescription.isEmpty()) ?
	    // platformDescription : platformId;
	    // platform.setDescription(platformDescription);
	    // Citation platformCitation = new Citation();
	    // platformCitation.setTitle(platformId);
	    // platform.setCitation(platformCitation);
	    // coreMetadata.getMIMetadata().addMIPlatform(platform);
	    // Keywords keyword = new Keywords();
	    // keyword.setTypeCode("platform");
	    // keyword.addKeyword(platformId);// or platformDescription
	    // coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
	    // }
	    //
	    // // organization
	    //

	    for (String creatorsURI : creatorsURIs) {

		if (creatorsURI.contains("id/person")) {
		    continue;
		}
		ResponsibleParty creatorContact = new ResponsibleParty();
		MarineOrganization organization = new MarineOrganization(creatorsURI + ".ttl");
		String orgName = organization.getElement(RDFElement.ORGNAME);
		String mail = organization.getElement(RDFElement.ORGEMAILS);
		String telephone = organization.getElement(RDFElement.ORGTELEPHONES);
		String url = organization.getElement(RDFElement.ORGWEBPAGES);
		String addr = organization.getElement(RDFElement.ORGADDRESS);

		Contact contactInfo = new Contact();
		Address address = new Address();
		if (mail != null && !mail.isEmpty()) {
		    address.addElectronicMailAddress(mail);
		}
		if (addr != null && !addr.isBlank()) {
		    address.addDeliveryPoint(addr);
		}
		contactInfo.setAddress(address);
		if (telephone != null && !telephone.isBlank()) {
		    contactInfo.addPhoneVoice(telephone);
		}
		creatorContact.getElementType().setOrganisationName(ISOMetadata.createAnchorPropertyType(creatorsURI, orgName));

		if (url != null && !url.isEmpty()) {
		    Online online = new Online();
		    online.setLinkage(url);
		    contactInfo.setOnline(online);
		}

		creatorContact.setContactInfo(contactInfo);
		creatorContact.setRoleCode("author");
		coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);

	    }

	    GSLoggerFactory.getLogger(getClass()).info("EurOBIS-LD Mappper ENDED");

	} catch (

	Exception e) {
	    e.printStackTrace();
	}

    }

    private static final String MD_KEYWORD_TYPE_CODE_NS = "https://standards.iso.org/iso/19115/resources/Codelists/gml/MD_KeywordTypeCode.xml#";

    /**
     * Uses {@code schema:additionalType} when present (e.g. ISO keyword type code {@code place}); otherwise
     * {@code schema:alternativeType} (e.g. {@code Geographic term}).
     */
    private static String resolveDefinedTermKeywordType(String rawAdditionalType, String rawAlternativeType) {
	String fromAdditional = normalizeKeywordTypeFromAdditional(rawAdditionalType);
	if (!fromAdditional.isEmpty()) {
	    return fromAdditional;
	}
	return cleanKeywordLiteral(rawAlternativeType);
    }

    private static String normalizeKeywordTypeFromAdditional(String raw) {
	if (raw == null) {
	    return "";
	}
	String t = cleanKeywordLiteral(raw);
	if (t.isEmpty()) {
	    return "";
	}
	t = t.replace(MD_KEYWORD_TYPE_CODE_NS, "");
	if (t.contains("#")) {
	    t = t.substring(t.lastIndexOf('#') + 1);
	}
	return t.trim();
    }

    private static String cleanKeywordLiteral(String raw) {
	if (raw == null) {
	    return "";
	}
	String t = raw.trim();
	if (t.contains("^^")) {
	    t = t.substring(0, t.indexOf("^^"));
	}
	if (t.endsWith("@en")) {
	    t = t.substring(0, t.length() - 3);
	} else {
	    int at = t.lastIndexOf('@');
	    if (at > 0) {
		String tag = t.substring(at + 1);
		if (tag.matches("[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*")) {
		    t = t.substring(0, at);
		}
	    }
	}
	t = t.replace("\"", "").replace("\\n", "");
	return t.trim();
    }

    /**
     * Adds {@code projects} from {@code https://marineinfo.org/id/dataset/{dasId}.json} when present (anchor:
     * {@link #MARINEINFO_PROJECT_URI_PREFIX} + {@code ProID}, title: {@code StandardTitle}).
     */
    private void addDatasetJsonProjectKeywords(HashMap<String, Keywords> keywordMap, String dasid) {
	try {
	    Set<String> projectHrefsAdded = new HashSet<>();
	    Downloader d = new Downloader();
	    Optional<String> res = d.downloadOptionalString("https://marineinfo.org/id/dataset/" + dasid + ".json");
	    if (res.isEmpty()) {
		return;
	    }
	    JSONObject root = new JSONObject(res.get());
	    JSONArray projects = root.optJSONArray("projects");
	    if (projects == null || projects.length() == 0) {
		return;
	    }
	    Keywords kprojects = keywordMap.computeIfAbsent("project", t -> {
		Keywords k = new Keywords();
		k.setTypeCode("project");
		return k;
	    });
	    for (int i = 0; i < projects.length(); i++) {
		JSONObject p = projects.optJSONObject(i);
		if (p == null || !p.has("ProID") || p.isNull("ProID")) {
		    continue;
		}
		long proId = p.getLong("ProID");
		if (proId < 1) {
		    continue;
		}
		String href = MARINEINFO_PROJECT_URI_PREFIX + proId;
		if (!projectHrefsAdded.add(href)) {
		    continue;
		}
		String stdTitle = p.optString("StandardTitle", "").trim();
		if (stdTitle.isEmpty()) {
		    stdTitle = p.optString("Acronym", "").trim();
		}
		if (stdTitle.isEmpty()) {
		    kprojects.addKeyword(href, href);
		} else {
		    kprojects.addKeyword(stdTitle, href);
		}
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).debug("Could not merge dataset JSON projects for {}: {}", dasid,
		    e.getMessage());
	}
    }

    // private void retrieveOnline(MIMetadata miMetadata, ExtensionHandler
    // extendedMetadataHandler) {
    // Downloader downloader = new Downloader();
    // try {
    // Iterator<String> keywords =
    // miMetadata.getDataIdentification().getKeywordsValues();
    // boolean biologyFound = false;
    // while (keywords.hasNext() && !biologyFound) {
    // String kwd = keywords.next();
    // if (kwd.toLowerCase().contains("eurobis")) {
    // biologyFound = true;
    // }
    // }
    // if (biologyFound) {
    // String title = miMetadata.getDataIdentification().getCitationTitle();
    // String biologyDir = biologyURL + URLEncoder.encode(title, "UTF-8") + "/";
    // boolean biologyExist = HttpConnectionUtils.checkConnectivity(biologyDir);
    // if (biologyExist) {
    // String identifier =
    // miMetadata.getDataIdentification().getResourceIdentifier();
    // String biologyZip = "";
    // // 1.1 version
    // identifier = identifier.equalsIgnoreCase("dy11522") ? "dy11523" : identifier;
    // if (identifier.equalsIgnoreCase("dy21_meiofauna_2009") ||
    // identifier.equalsIgnoreCase("dy11523")
    // || identifier.equalsIgnoreCase("dy34_phy") ||
    // identifier.equalsIgnoreCase("dy23")) {
    // biologyZip = biologyDir + "file/" + "eml-" + identifier + "-v1.1.zip";
    // // 1.7 version
    // } else if (identifier.equalsIgnoreCase("dy115-20-megafauuna-nmdis")
    // || identifier.equalsIgnoreCase("dy115-20-meiofauna-nmdis")) {
    // biologyZip = biologyDir + "file/" + "eml-" + identifier + "-v1.7.zip";
    // // 1.8 version
    // } else if (identifier.equalsIgnoreCase("dy115-20-macrofauna-nmdis")
    // || identifier.equalsIgnoreCase("dy115-20-zooplankton-nmdis")) {
    // biologyZip = biologyDir + "file/" + "eml-" + identifier + "-v1.8.zip";
    // // 1.0 version
    // } else {
    // biologyZip = biologyDir + "file/" + "eml-" + identifier + "-v1.0.zip";
    // }
    //
    // boolean zipExist = HttpConnectionUtils.checkConnectivity(biologyZip);
    // if (zipExist) {
    // Online online = new Online();
    // online.setLinkage(biologyZip);
    // online.setProtocol("WWW:DOWNLOAD-1.0-http--download");
    // online.setFunctionCode("download");
    // online.setDescription("Direct Download");
    // miMetadata.getDistribution().addDistributionOnline(online);
    // extendedMetadataHandler.addThemeCategory(EMODPACEThemeCategory.BIOLOGY.getThemeCategory());
    // }
    // }
    // }
    // } catch (Exception e) {
    //
    // GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
    // }
    // }
}
