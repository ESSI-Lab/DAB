package eu.essi_lab.accessor.eurobis;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.w3c.dom.Node;

import eu.essi_lab.iso.datamodel.classes.Address;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.Contact;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.Keywords;
import eu.essi_lab.iso.datamodel.classes.MIInstrument;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.TransferOptions;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.emod_pace.EMODPACEThemeCategory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.ommdk.FileIdentifierMapper;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

public class EurOBISMapper extends FileIdentifierMapper {

    private static final String EUROBIS_MAPPER_DOWNLOAD_ERROR = "EUROBIS_MAPPER_DOWNLOAD_ERROR";

    private String biologyURL = "http://222.186.3.18:8888/erddap/files/data/Biology%20Data/";
    public static Double TOL = Math.pow(10, -8);

    @Override
    public String getSupportedOriginalMetadataSchema() {
	return CommonNameSpaceContext.EUROBIS_NS_URI;
    }

    @Override
    protected GSResource execMapping(OriginalMetadata originalMD, GSSource source) throws GSException {
	DatasetCollection dataset = new DatasetCollection();
	dataset.setSource(source);

	mapMetadata(originalMD, dataset);

	return dataset;
    }

    public static enum ECOTAXA_IDs {
	A("6505"), //
	B("6506"), //
	C("6507"), //
	D("6508"), //
	E("6510"), //
	F("6511"), //
	G("6512"), //
	H("6513"), //
	I("6514"), //
	J("6515");// ;

	private String id;

	public String getId() {
	    return id;
	}

	ECOTAXA_IDs(String id) {
	    this.id = id;
	}

    }

    private void mapMetadata(OriginalMetadata originalMD, DatasetCollection dataset) {

	GSLoggerFactory.getLogger(getClass()).info("EurOBIS Mappper STARTED");

	String originalMetadata = originalMD.getMetadata();

	ExtensionHandler extendedMetadataHandler = dataset.getExtensionHandler();

	try {

	    XMLDocumentReader xdoc = new XMLDocumentReader(originalMetadata);

	    String title = xdoc.evaluateString("//*:eml/*:dataset/*:title");
	    String id = xdoc.evaluateString("//*:eml/*:dataset/*:alternateIdentifier");
	    if (id == null || id.isEmpty()) {
		id = xdoc.evaluateString("//*:identifier");
	    }

	    Node[] keywordNodes = xdoc.evaluateNodes("//*:eml/*:dataset/*:keywordSet/*:keyword");
	    List<String> keywords = new ArrayList<>();
	    for (Node n : keywordNodes) {
		keywords.add(n.getTextContent());
	    }

	    Node[] bboxNode = xdoc.evaluateNodes("//*:eml/*:dataset/*:coverage/*:geographicCoverage");
	    
	    Node[] instrumentNodes = xdoc.evaluateNodes("//*:eml/*:dataset/*:methods/*:methodStep");

	    Node additionalMetadata = xdoc.evaluateNode("//*:eml/*:additionalMetadata");

	    String startDate = xdoc
		    .evaluateString("//*:eml/*:dataset/*:coverage/*:temporalCoverage/*:rangeOfDates/*:beginDate/*:calendarDate");
	    String endDate = xdoc.evaluateString("//*:eml/*:dataset/*:coverage/*:temporalCoverage/*:rangeOfDates/*:endDate/*:calendarDate");

	    Node[] creatorNodes = xdoc.evaluateNodes("//*:eml/*:dataset/*:creator");

	    Node[] taxonomicCoverage = xdoc.evaluateNodes("//*:eml/*:dataset/*:coverage/*:taxonomicCoverage");

	    Node metadataProvider = xdoc.evaluateNode("//*:eml/*:dataset/*:metadataProvider");

	    String email = xdoc.evaluateString("//*:eml/*:dataset/*:metadataProvider/*:electronicMailAddress");

	    String orgURL = xdoc.evaluateString("//*:eml/*:dataset/*:metadataProvider/*:onlineUrl");

	    String country = xdoc.evaluateString("//*:eml/*:dataset/*:metadataProvider/*:address/*:country");

	    String providerOrgName = xdoc.evaluateString("//*:eml/*:dataset/*:metadataProvider/*:organizationName");

	    String providerName = xdoc.evaluateString("//*:eml/*:dataset/*:metadataProvider/*:individualName/*:surName");

	    String pubDate = xdoc.evaluateString("//*:eml/*:dataset/*:pubDate").trim();

	    Node[] nodesAbstract = xdoc.evaluateNodes("//*:eml/*:dataset/*:abstract/*:para");
	    String description = "";
	    for (Node n : nodesAbstract) {
		description += n.getTextContent() + " ";
	    }

	    // String abstrakt = xdoc.evaluateString("/*:eml/*:dataset/*:abstract/*:para");

	    String timeStamp = xdoc.evaluateString("//*:dateStamp");

	    CoreMetadata coreMetadata = dataset.getHarmonizedMetadata().getCoreMetadata();

	    coreMetadata.getMIMetadata().setHierarchyLevelName("series");
	    coreMetadata.getMIMetadata().addHierarchyLevelScopeCodeListValue("series");

	    // identifier
	    if (id != null && !id.isEmpty()) {

		try {
		    String identifier = StringUtils.hashSHA1messageDigest("eurobis:" + id);
		    coreMetadata.setIdentifier(identifier);
		    coreMetadata.getMIMetadata().setFileIdentifier(identifier);
		} catch (NoSuchAlgorithmException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		GSLoggerFactory.getLogger(getClass()).info("Alternative ID: " + id);
		String[] splittedId = null;
		if (id.contains("r=dasid_")) {
		    splittedId = id.split("r=dasid_");
		} else if (id.contains("r=pangaea_")) {
		    splittedId = id.split("r=pangaea_");
		} else if (id.contains("oai:marineinfo.org:id:dataset:")) {
		    splittedId = id.split("oai:marineinfo.org:id:dataset:");
		} else {
		    splittedId = id.split("r=");
		}

		String resourceId = "";
		// String onlineResource = "";

		if (splittedId.length < 2) {
		    Node[] nodesIds = xdoc.evaluateNodes("//*:eml/*:dataset/*:alternateIdentifier");
		    if (nodesIds.length > 1) {
			String alternateId = nodesIds[1].getTextContent();
			if (alternateId.contains("r=dasid_")) {
			    resourceId = alternateId.split("r=dasid_")[1];
			} else if (alternateId.contains("r=pangaea_")) {
			    resourceId = alternateId.split("r=pangaea_")[1];
			} else {
			    resourceId = alternateId.split("r=")[1];
			}
			// onlineResource = alternateId.replace("resource?", "archive.do?");
		    } else {
			GSLoggerFactory.getLogger(getClass()).info("UNREACHABLE POINT!!");
		    }

		} else {
		    resourceId = splittedId[1];
		    // onlineResource = id.replace("resource?", "archive.do?");
		}
		boolean ecotaxaFound = false;
		for (ECOTAXA_IDs ecotaxaId : ECOTAXA_IDs.values()) {
		    if (ecotaxaId.getId().equalsIgnoreCase(resourceId)) {
			ecotaxaFound = true;
			break;
		    }
		}

		if (ecotaxaFound) {
		    System.out.println(resourceId);
		    String ecotaxaUrl = xdoc.evaluateString("//*:eml/*:dataset/*:distribution[@scope='document']/*:online/*:url");
		    if (ecotaxaUrl.contains("ecotaxa.obs-vlfr.fr")) {
			Downloader d = new Downloader();
			Optional<String> res = d.downloadOptionalString(ecotaxaUrl);
			if (res.isPresent()) {
			    JSONObject json = new JSONObject(res.get());
			    String projects = json.optString("project_ids");
			    String ids = projects.replace("[", "").replace("]", "");
			    coreMetadata.getDataIdentification().setResourceIdentifier(ids);
			}
		    }
		    // TODO: find a way to get strings
		} else {
		    coreMetadata.getDataIdentification().setResourceIdentifier(resourceId);
		}

		// online
		String dasid = "";
		Node[] onlineNodes = xdoc.evaluateNodes("//*:online/*:url");
		for (Node n : onlineNodes) {
		    String url = n.getTextContent();
		    if (url.contains("dasid")) {
			dasid = url.split("dasid=")[1];
			break;
		    }
		}
		String wfsRequest = "http://geo.vliz.be/geoserver/wfs/ows?service=WFS&version=1.1.0&request=GetFeature&typeName=Dataportal:eurobis-obisenv_basic&resultType=results&viewParams=where:datasetid+IN+("
			+ dasid + ");context:0100&outputFormat=csv";
		Online online = new Online();
		online.setLinkage(wfsRequest);
		online.setProtocol("HTTP");
		online.setFunctionCode("download");
		online.setDescription("Direct Download");
		coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);

	    }

	    // title
	    if (title != null && !title.isEmpty()) {
		coreMetadata.setTitle(title);
	    }
	    // abstract
	    if (description != null && !description.isEmpty()) {
		coreMetadata.setAbstract(description);
	    }
	    // keywords and PARAMETER IDENTIFIERS

	    for (String s : keywords) {
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword(s);
	    }
	    coreMetadata.getMIMetadata().getDataIdentification().addKeyword("EUROBIS");

	    // ADDITIONAL METADATA: parameter/platform
	    if (additionalMetadata != null) {
		Node[] parameterList = xdoc.evaluateNodes(additionalMetadata, "*:metadata/*:parameter");
		for (Node n : parameterList) {
		    String parameter = n.getTextContent();
		    CoverageDescription descr = new CoverageDescription();
		    //descr.setAttributeIdentifier(parameter);
		    descr.setAttributeDescription(parameter);
		    descr.setAttributeTitle(parameter);
		    coreMetadata.getMIMetadata().addCoverageDescription(descr);

		}

		// platform
		Node[] platformList = xdoc.evaluateNodes(additionalMetadata, "*:metadata/*:platform");
		for (Node node : platformList) {
		    String platformDescription = xdoc.evaluateString(node, "*:description");
		    String platformIdentifier = xdoc.evaluateString(node, "@id");
		    MIPlatform platform = new MIPlatform();
		    platform.setMDIdentifierCode(platformIdentifier);
		    platform.setDescription(platformDescription);
		    Citation platformCitation = new Citation();
		    platformCitation.setTitle(platformDescription);
		    platform.setCitation(platformCitation);
		    coreMetadata.getMIMetadata().addMIPlatform(platform);
		    Keywords keyword = new Keywords();
		    keyword.setTypeCode("platform");
		    keyword.addKeyword(platformDescription);// or platformDescription
		    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);

		}

		// online resources
		Node[] onlineList = xdoc.evaluateNodes(additionalMetadata, "*:metadata/*:gbif/*:physical");
		for (Node n : onlineList) {
		    String url = xdoc.evaluateString(n, "*:distribution/*:online/*:url");
		    String function = xdoc.evaluateString(n, "*:distribution/*:online/*:url/@function");
		    String name = xdoc.evaluateString(n, "*:objectName");
		    Online online = new Online();
		    online.setLinkage(url);
		    online.setProtocol("HTTP");
		    online.setName(name);
		    online.setFunctionCode(function);
		    online.setDescription(name);
		    coreMetadata.getMIMetadata().getDistribution().addDistributionOnline(online);
		}

	    }

	    // if (parameters != null && !parameters.isEmpty()) {
	    //
	    // String[] splittedKeyword = parameters.split(",");
	    // for (String s : splittedKeyword) {
	    // s = s.trim();
	    String coverageAttr = "";
	    String coverageAttrURI = "";
	    for (Node node : taxonomicCoverage) {
		String taxonName = xdoc.evaluateString(node, "*:taxonomicClassification/*:taxonRankName");
		String taxonValue = xdoc.evaluateString(node, "*:taxonomicClassification/*:taxonRankValue");
		String taxonURI = xdoc.evaluateString(node, "*:generalTaxonomicCoverage");
		if (taxonName != null && !taxonName.isEmpty()) {
		    coverageAttr = coverageAttr + taxonName + " | ";
		}
		if (taxonValue != null && !taxonValue.isEmpty()) {
		    coverageAttr = coverageAttr + taxonValue + " | ";
		}
		if(taxonURI != null && !taxonURI.isEmpty()) {
		    coverageAttrURI = coverageAttrURI + taxonURI + " | ";
		}
	    }

	    if (!coverageAttr.isEmpty()) {
		coverageAttr = coverageAttr.substring(0, coverageAttr.length() - 2);
		CoverageDescription coverageDescription = new CoverageDescription();
		if(!coverageAttrURI.isEmpty()) {
		    coverageAttrURI = coverageAttrURI.substring(0, coverageAttrURI.length()-2);
		    coverageDescription.setAttributeIdentifier(coverageAttrURI);
		}
		coverageDescription.setAttributeDescription(coverageAttr);
		// coverageDescription.setAttributeTitle(coverageAttr);
		coreMetadata.getMIMetadata().addCoverageDescription(coverageDescription);

	    }

	    // bbox

	    for (Node node : bboxNode) {

		String descBbox = xdoc.evaluateString(node, "*:geographicDescription");
		if (descBbox.toLowerCase().contains("eurobis calculated")) {
		    String west = xdoc.evaluateString(node, "*:boundingCoordinates/*:westBoundingCoordinate");
		    String east = xdoc.evaluateString(node, "*:boundingCoordinates/*:eastBoundingCoordinate");
		    String north = xdoc.evaluateString(node, "*:boundingCoordinates/*:northBoundingCoordinate");
		    String south = xdoc.evaluateString(node, "*:boundingCoordinates/*:southBoundingCoordinate");
		    if ((west != null && !west.isEmpty()) && (east != null && !east.isEmpty()) && (north != null && !north.isEmpty())
			    && (south != null && !south.isEmpty())) {
			double w = Double.valueOf(west);
			double e = Double.valueOf(east);
			double n = Double.valueOf(north);
			double s = Double.valueOf(south);

			dataset.getHarmonizedMetadata().getCoreMetadata().addBoundingBox(n, w, s, e);
		    }
		}
	    }

	    // datestamp
	    if (timeStamp != null && !timeStamp.isEmpty()) {
		coreMetadata.getMIMetadata().getDataIdentification().setCitationRevisionDate(timeStamp);
		coreMetadata.getMIMetadata().setDateStampAsDate(timeStamp);
	    }

	    // temporal extent
	    TemporalExtent tempExtent = new TemporalExtent();

	    if (startDate != null && !startDate.isEmpty() && !startDate.contains("unknown")) {
		tempExtent.setBeginPosition(startDate);
		// coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(startDate, endDate);
	    }
	    if (endDate != null && !endDate.isEmpty() && !endDate.contains("unknown") && !endDate.contains("progress")) {
		tempExtent.setEndPosition(endDate);
		if (coreMetadata.getMIMetadata().getDataIdentification().getCitationRevisionDate() == null
			|| coreMetadata.getMIMetadata().getDataIdentification().getCitationRevisionDate().isEmpty()) {
		    coreMetadata.getMIMetadata().getDataIdentification().setCitationRevisionDate(endDate);
		    coreMetadata.getMIMetadata().setDateStampAsDate(endDate);
		}
		// coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(startDate, endDate);
	    } else if (endDate.contains("progress")) {
		TimeIndeterminateValueType endTimeInderminate = TimeIndeterminateValueType.NOW;
		tempExtent.setIndeterminateEndPosition(endTimeInderminate);
	    }

	    if (tempExtent.getBeginPosition() != null && tempExtent.getEndPosition() == null) {
		TimeIndeterminateValueType endTimeInderminate = TimeIndeterminateValueType.NOW;
		tempExtent.setIndeterminateEndPosition(endTimeInderminate);
	    }

	    coreMetadata.getMIMetadata().getDataIdentification().addTemporalExtent(tempExtent);

	    if (pubDate != null && !pubDate.isEmpty()) {
		coreMetadata.getMIMetadata().getDataIdentification().setCitationCreationDate(pubDate);
	    }

	    // // instrument
	    
	    for (Node node : instrumentNodes) {

		String instrument = xdoc.evaluateString(node, "*:description/*:instrumentation");
		if (instrument != null && !instrument.isEmpty()) {
		    String instrumentDescription = xdoc.evaluateString(node, "*:description/*:para");
		    String instrumentTitle = xdoc.evaluateString(node, "*:description/*:title");
		     MIInstrument myInstrument = new MIInstrument();
//		     String id = (instrumentId != null && !instrumentId.isEmpty()) ? instrumentId : instrumentName;
//		     String type = (orbitType != null && !orbitType.isEmpty()) ? orbitType : id;
		     myInstrument.setMDIdentifierTypeIdentifier(instrument);
		     myInstrument.setMDIdentifierTypeCode(instrument);
		     myInstrument.setDescription(instrumentDescription);
		     myInstrument.setTitle(instrumentTitle);
		     coreMetadata.getMIMetadata().addMIInstrument(myInstrument);
		     Keywords keyword = new Keywords();
			    keyword.setTypeCode("instrument");
			    keyword.addKeyword(instrumentTitle);// or sensorModel
			    coreMetadata.getMIMetadata().getDataIdentification().addKeywords(keyword);
		}
	    }
	    
	    
	    // // // INSTRUMENT IDENTIFIERS
	    // if (instrumentName != null && !instrumentName.isEmpty()) {
	    //
	    // MIInstrument myInstrument = new MIInstrument();
	    // String id = (instrumentId != null && !instrumentId.isEmpty()) ? instrumentId : instrumentName;
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
	    // platformDescription = (platformDescription != null && !platformDescription.isEmpty()) ?
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
	    for (Node n : creatorNodes) {
		ResponsibleParty creatorContact = new ResponsibleParty();
		String orgName = xdoc.evaluateString(n, "*:organizationName");
		String ct = xdoc.evaluateString(n, "*:address/*:country");
		String individualName = xdoc.evaluateString(n, "*:individualName/*:surName");
		String mail = xdoc.evaluateString(n, "*:electronicMailAddress");
		String url = xdoc.evaluateString(n, "*:onlineUrl");

		Contact contactInfo = new Contact();
		Address address = new Address();
		if (mail != null && !mail.isEmpty()) {
		    address.addElectronicMailAddress(mail);
		}
		if (ct != null && !ct.isEmpty()) {
		    address.setCountry(ct);
		}
		contactInfo.setAddress(address);
		if (individualName != null && !individualName.isEmpty()) {
		    creatorContact.setIndividualName(individualName);
		}

		if (orgName != null && !orgName.isEmpty()) {
		    creatorContact.setOrganisationName(orgName);
		}

		if (url != null && !url.isEmpty()) {
		    Online online = new Online();
		    online.setLinkage(url);
		    contactInfo.setOnline(online);
		}

		creatorContact.setContactInfo(contactInfo);
		creatorContact.setRoleCode("author");
		coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(creatorContact);

	    }

	    if (metadataProvider != null) {
		ResponsibleParty pointOfContact = new ResponsibleParty();

		Contact contactInfo = new Contact();
		Address address = new Address();
		if (email != null && !email.isEmpty()) {
		    address.addElectronicMailAddress(email);
		}
		if (country != null && !country.isEmpty()) {
		    address.setCountry(country);
		}
		contactInfo.setAddress(address);
		if (providerName != null && !providerName.isEmpty()) {
		    pointOfContact.setIndividualName(providerName);
		}

		if (providerOrgName != null && !providerOrgName.isEmpty()) {
		    pointOfContact.setOrganisationName(providerOrgName);
		}

		if (orgURL != null && !orgURL.isEmpty()) {
		    Online online = new Online();
		    online.setLinkage(orgURL);
		    contactInfo.setOnline(online);
		}

		pointOfContact.setContactInfo(contactInfo);
		pointOfContact.setRoleCode("pointOfContact");
		coreMetadata.getMIMetadata().getDataIdentification().addPointOfContact(pointOfContact);
	    }

	    //
	    if (dataset.getSource().getEndpoint().contains("222.186.3.18:8889/services/xml")) {
		// onlineResource
		// add keywords EMOD-PACE
		GeographicBoundingBox bbox = coreMetadata.getMIMetadata().getDataIdentification().getGeographicBoundingBox();
		// Double east = bbox.getEast();
		// Double north = bbox.getNorth();
		// Double west = bbox.getWest();
		// Double south = bbox.getSouth();
		if (bbox != null) {
		    if (Math.abs(bbox.getEast()) < TOL && Math.abs(bbox.getNorth()) < TOL) {
			// set east=west
			coreMetadata.getMIMetadata().getDataIdentification().getGeographicBoundingBox().setEast(bbox.getWest());
			coreMetadata.getMIMetadata().getDataIdentification().getGeographicBoundingBox().setNorth(bbox.getSouth());
		    }
		}
		Iterator<TransferOptions> iterator2 = coreMetadata.getMIMetadata().getDistribution().getDistributionTransferOptions();
		while (iterator2.hasNext()) {
		    TransferOptions transfer2 = iterator2.next();
		    transfer2.clearOnlines();
		}
		coreMetadata.getMIMetadata().getDataIdentification().addKeyword("EMOD-PACE project");
		retrieveOnline(coreMetadata.getMIMetadata(), extendedMetadataHandler);
	    }

	    GSLoggerFactory.getLogger(getClass()).info("EurOBIS Mappper ENDED");

	} catch (Exception e) {
	    e.printStackTrace();
	}
	// identifier
	// if (datasetID != null && !datasetID.isEmpty()) {
	//
	// try {
	// String identifier = StringUtils.hashSHA1messageDigest(datasetID);
	// coreMetadata.setIdentifier(identifier);
	// coreMetadata.getMIMetadata().setFileIdentifier(identifier);
	// } catch (NoSuchAlgorithmException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (UnsupportedEncodingException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
    }

    private void retrieveOnline(MIMetadata miMetadata, ExtensionHandler extendedMetadataHandler) {
	Downloader downloader = new Downloader();
	try {
	    Iterator<String> keywords = miMetadata.getDataIdentification().getKeywordsValues();
	    boolean biologyFound = false;
	    while (keywords.hasNext() && !biologyFound) {
		String kwd = keywords.next();
		if (kwd.toLowerCase().contains("eurobis")) {
		    biologyFound = true;
		}
	    }
	    if (biologyFound) {
		String title = miMetadata.getDataIdentification().getCitationTitle();
		String biologyDir = biologyURL + URLEncoder.encode(title, "UTF-8") + "/";
		boolean biologyExist = HttpConnectionUtils.checkConnectivity(biologyDir);
		if (biologyExist) {
		    String identifier = miMetadata.getDataIdentification().getResourceIdentifier();
		    String biologyZip = "";
		    // 1.1 version
		    identifier = identifier.equalsIgnoreCase("dy11522") ? "dy11523" : identifier;
		    if (identifier.equalsIgnoreCase("dy21_meiofauna_2009") || identifier.equalsIgnoreCase("dy11523")
			    || identifier.equalsIgnoreCase("dy34_phy") || identifier.equalsIgnoreCase("dy23")) {
			biologyZip = biologyDir + "file/" + "eml-" + identifier + "-v1.1.zip";
			// 1.7 version
		    } else if (identifier.equalsIgnoreCase("dy115-20-megafauuna-nmdis")
			    || identifier.equalsIgnoreCase("dy115-20-meiofauna-nmdis")) {
			biologyZip = biologyDir + "file/" + "eml-" + identifier + "-v1.7.zip";
			// 1.8 version
		    } else if (identifier.equalsIgnoreCase("dy115-20-macrofauna-nmdis")
			    || identifier.equalsIgnoreCase("dy115-20-zooplankton-nmdis")) {
			biologyZip = biologyDir + "file/" + "eml-" + identifier + "-v1.8.zip";
			// 1.0 version
		    } else {
			biologyZip = biologyDir + "file/" + "eml-" + identifier + "-v1.0.zip";
		    }

		    boolean zipExist = HttpConnectionUtils.checkConnectivity(biologyZip);
		    if (zipExist) {
			Online online = new Online();
			online.setLinkage(biologyZip);
			online.setProtocol("WWW:DOWNLOAD-1.0-http--download");
			online.setFunctionCode("download");
			online.setDescription("Direct Download");
			miMetadata.getDistribution().addDistributionOnline(online);
			extendedMetadataHandler.addThemeCategory(EMODPACEThemeCategory.BIOLOGY.getThemeCategory());
		    }
		}
	    }
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }
}
