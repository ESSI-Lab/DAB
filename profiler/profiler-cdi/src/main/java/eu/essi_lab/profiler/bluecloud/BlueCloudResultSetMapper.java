package eu.essi_lab.profiler.bluecloud;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.iso.datamodel.classes.MDMetadata;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.common.ISO2014NameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.pluggable.ESSILabProvider;
import eu.essi_lab.model.pluggable.Provider;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.pdk.rsm.DiscoveryResultSetMapper;
import eu.essi_lab.pdk.rsm.MappingSchema;

/**
 * @author boldrini
 */
public class BlueCloudResultSetMapper extends DiscoveryResultSetMapper<Element> {

    /**
     * The {@link MappingSchema} schema of this mapper
     */
    public static final MappingSchema BLUECLOUD_MAPPING_SCHEMA = new MappingSchema();
    private static final String CDI_RES_SET_MAPPER_ERROR = "CDI_RES_SET_MAPPER_ERROR";
    private static final String REVISION_DATE_TYPE = "revision";
    private static final String CODE_LIST = "codeList";
    private static final String CODE_LIST_VLAUE = "codeListValue";
    private static final String CODE_SPACE = "codeSpace";
    private static final String BLUECLOUD = "BlueCloud";
    private static final String SDN_P02_UNKNOWN = "http://www.seadatanet.org/urnurl/SDN:P02::UNKNOWN/";
    private static final String SDN_L06_UNKNOWN = "http://www.seadatanet.org/urnurl/SDN:L06::UNKNOWN/";

    private static final String MI_METADATA_WRAPPER_ROOT_ERROR = "MI_METADATA_WRAPPER_ROOT_ERROR";

    public BlueCloudResultSetMapper() {
    }

    protected String getTargetNamespace() {

	return ISO2014NameSpaceContext.BLUECLOUD_NS_URI;
    }

    @Override
    public MappingSchema getMappingSchema() {

	return BLUECLOUD_MAPPING_SCHEMA;
    }

    @Override
    public Provider getProvider() {

	return new ESSILabProvider();
    }

    private String createKeywordXPath(String text) {
	return "//*:keyword/*:CharacterString[text()='" + text + "']";
    }

    @Override
    public Element map(DiscoveryMessage message, GSResource res) throws GSException {

	try {

	    HarmonizedMetadata harmonizedMetadata = res.getHarmonizedMetadata();
	    CoreMetadata coreMetadata = harmonizedMetadata.getCoreMetadata();
	    MDMetadata metadata = coreMetadata.getReadOnlyMDMetadata();
	    MIMetadata miMetadata = coreMetadata.getMIMetadata();

	    XMLDocumentReader reader = new XMLDocumentReader(miMetadata.asDocument(false));
	    XMLDocumentWriter writer = new XMLDocumentWriter(reader);
	    reader.setNamespaceContext(new ISO2014NameSpaceContext());

	    // Node node = reader.evaluateNode("//*:MI_Metadata[1]");
	    // NodeList children = node.getChildNodes();
	    // for(int i=0; i < children.getLength(); i++) {
	    // writer.addNode("/gmi:MI_Metadata", children.item(i));
	    // }
	    writer.rename("//gmd:MD_CoverageDescription", "gmi2019:MI_CoverageDescription");

//	    Node[] keywordsWithAnchor = reader.evaluateNodes("//*:descriptiveKeywords/*:MD_Keywords/*:keyword/*:Anchor");
//
//	    for (Node n : keywordsWithAnchor) {
//
//		writer.rename(n, ".", "gco:CharacterString");
//		writer.removeAllAttributesFromNode(n);
//		// writer.remove(xPath)
//	    }

	    Node[] resultGmi = reader.evaluateNodes("//gmiold:*");

	    for (Node n : resultGmi) {
		writer.rename(n, ".", "gmi2019:" + n.getLocalName());
	    }

	    Node[] resultGml = reader.evaluateNodes("//gmlold:*|//@gmlold:*");

	    for (Node n : resultGml) {
		writer.rename(n, ".", "gml32:" + n.getLocalName());
	    }

	    // Node[] attributeGml = reader.evaluateNodes("//@gmlold:*");
	    //
	    // for(Node n: attributeGml) {
	    // writer.rename(n, ".", "gml32:" + n.getLocalName());
	    // }

	    // System.out.println(reader.asString());

	    // String replaced = wrappedReader.asString().replace("xmlns:gml=\"http://www.opengis.net/gml\"", "")
	    // .replace("xmlns:gmi=\"http://www.isotc211.org/2005/gmi\"", "");

	    // wrappedReader = new XMLDocumentReader(replaced);
	    // wrappedReader.setNamespaceContext(new ISO2014NameSpaceContext());

	    // XMLDocumentWriter writer = new XMLDocumentWriter(reader);
	    // writer.rename("//*:MI_Metadata/@xsi:schemaLocation", "http://standards.iso.org/iso/19115/-2/gmi/1.0
	    // https://schemas.isotc211.org/schemas/19115/-2/gmi/1.0/gmi.xsd");
	    // writer.rename("//gmd:MD_CoverageDescription", "gmi:MI_CoverageDescription");
	    // String xmlnsChange = reader.asString();
	    // xmlnsChange = xmlnsChange.replace("http://www.isotc211.org/2005/gmi",
	    // "http://standards.iso.org/iso/19115/-2/gmi/1.0").replace("xmlns:gml32=\"http://www.opengis.net/gml/3.2\"",
	    // "");;
	    // xmlnsChange = xmlnsChange.replace("http://www.opengis.net/gml", "http://www.opengis.net/gml/3.2");
	    // reader = new XMLDocumentReader(xmlnsChange);
	    // reader.setNamespaceContext(new ISO2014NameSpaceContext());

	    // check xpath mandatory fields for BlueCloud profile
	    Node[] organizations = reader.evaluateNodes("//gmd:CI_ResponsibleParty/gmd:organisationName/*[1]");

	    if (organizations.length == 0) {
		// TODO: implements solutions
		GSLoggerFactory.getLogger(getClass()).trace("ORGANIZATION NOT FOUND!!! for record: " + coreMetadata.getIdentifier());
	    }

	    String title = reader.evaluateString(
		    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/*[1]");

	    if (title == null || title.isEmpty()) {
		// TODO: implements solutions
		GSLoggerFactory.getLogger(getClass()).trace("TITLE NOT FOUND!!! for record: " + coreMetadata.getIdentifier());
	    }

	    Node[] platforms = reader.evaluateNodes(
		    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='platform']/gmd:keyword/*[1]");

	    if (platforms.length == 0) {
		Node[] nodes = reader.evaluateNodes(
			"/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue");

		for (Node n : nodes) {
		    if (n.getNodeValue() != null && n.getNodeValue().contains("platform")) {
			n.setNodeValue("platform");
		    }
		}
	    }

	    Node[] instruments = reader.evaluateNodes(
		    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='instrument']/gmd:keyword/*[1]");
	    if (instruments.length == 0) {
		// TODO: implements solutions
		GSLoggerFactory.getLogger(getClass()).trace("INSTRUMENT NOT FOUND!!! for record: " + coreMetadata.getIdentifier());
	    }

	    Node[] keywords = reader.evaluateNodes(
		    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[not(gmd:type) or not(contains('platform instrument',gmd:type/gmd:MD_KeywordTypeCode/@codeListValue))]/gmd:keyword/*[1]");
	    if (keywords.length == 0) {
		// TODO: implements solutions
		GSLoggerFactory.getLogger(getClass()).trace("KEYWORDS NOT FOUND!!! for record: " + coreMetadata.getIdentifier());
	    }

	    String west = reader.evaluateString(
		    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal");
	    String east = reader.evaluateString(
		    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal");
	    String south = reader.evaluateString(
		    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal");
	    String north = reader.evaluateString(
		    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal");

	    if ((west == null || west.isEmpty()) && (east == null || east.isEmpty()) && (south == null || south.isEmpty())
		    && (north == null || north.isEmpty())) {
		GSLoggerFactory.getLogger(getClass()).trace("BBOX NOT FOUND!!! for record: " + coreMetadata.getIdentifier());
	    }
	    String start = reader.evaluateString(
		    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:beginPosition");
	    if (start == null || start.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).trace("START DATE NOT FOUND!!! for record: " + coreMetadata.getIdentifier());
	    }
	    String end = reader.evaluateString(
		    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:endPosition");
	    if (end == null || end.isEmpty()) {
		String now = reader.evaluateString(
			"/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:endPosition/@indeterminatePosition");
		if (now.toLowerCase().equals("now")) {
		    writer.setText(
			    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:endPosition",
			    ISO8601DateTimeUtils.getISO8601DateTime());
		} else {
		    GSLoggerFactory.getLogger(getClass()).trace("END DATE NOT FOUND!!! for record: " + coreMetadata.getIdentifier());
		}
	    }

	    String dateStamp = reader.evaluateString("/gmi2019:MI_Metadata/gmd:dateStamp/gco:Date");
	    if (dateStamp == null || dateStamp.isEmpty()) {
		dateStamp = reader.evaluateString("/gmi2019:MI_Metadata/gmd:dateStamp/gco:DateTime");
		if (dateStamp != null && !dateStamp.isEmpty()) {
		    Node n = reader.evaluateNode("/gmi2019:MI_Metadata/gmd:dateStamp/gco:DateTime");
		    writer.remove("/gmi2019:MI_Metadata/gmd:dateStamp/gco:Date");
		    writer.rename(n, ".", "gco:Date");
		}
	    }

	    return reader.getDocument().getDocumentElement();

	} catch (Exception e) {
	    throw GSException.createException( //
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CDI_RES_SET_MAPPER_ERROR);
	}

    }

    private Document createRoot() throws GSException {

	String root = "<gmi:MI_Metadata xmlns:gml=\"http://www.opengis.net/gml/3.2\"\r\n"
		+ "xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:gco=\"http://www.isotc211.org/2005/gco\"\r\n"
		+ "xmlns:gmi=\"http://standards.iso.org/iso/19115/-2/gmi/1.0\"\r\n"
		+ "xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gmx=\"http://www.isotc211.org/2005/gmx\"\r\n"
		+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:un=\"http://www.uncertml.org/2.0\"\r\n"
		+ "xmlns:wosis=\"https://www.isric.org/explore/wosis\" xmlns:gts=\"http://www.isotc211.org/2005/gts\"\r\n"
		+ "xsi:schemaLocation=\"http://standards.iso.org/iso/19115/-2/gmi/1.0 https://schemas.isotc211.org/schemas/19115/-2/gmi/1.0/gmi.xsd\"></gmi:MI_Metadata>";

	try {

	    ByteArrayInputStream stream = new ByteArrayInputStream(root.getBytes(StandardCharsets.UTF_8));

	    DocumentBuilderFactory builderFactory = XMLFactories.newDocumentBuilderFactory();

	    builderFactory.setNamespaceAware(false);

	    DocumentBuilder builder = builderFactory.newDocumentBuilder();

	    return builder.parse(stream);

	} catch (IOException | SAXException | ParserConfigurationException e) {

	    GSLoggerFactory.getLogger(getClass()).trace("Can't create root element for wrapping MI_METADATA", e);

	    throw GSException.createException(getClass(), "Can't create root element for wrapping MI_METADATA original metadata", null,
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_ERROR, MI_METADATA_WRAPPER_ROOT_ERROR, e);

	}

    }

    private String createOrganizationXPath(String originatorOrganisationIdentifier) {
	return "//*:organisationName/*:CharacterString[text()='" + originatorOrganisationIdentifier + "']";
    }

    private List<String> getCDIIdentifiers(List<String> originalSchemeIdentifiers, String originalSchemeURI) {
	List<String> ret = new ArrayList<>();
	for (String identifier : originalSchemeIdentifiers) {
	    String mappedIdentifier = getCDIIdentifier(identifier, originalSchemeURI);
	    if (mappedIdentifier != null) {
		ret.add(mappedIdentifier);
	    }
	}
	return ret;
    }

    private String getCDIIdentifier(String identifier, String originalSchemeURI) {

	// SOME MANUAL TRANSLATIONS

	if ("http://vocab.nerc.ac.uk/collection/P01/current/PSLTZZ01".equals(identifier)) {
	    // Practical salinity of the water body

	    // Salinity of the water column
	    return "http://www.seadatanet.org/urnurl/SDN:P02::PSAL/";
	}

	switch (originalSchemeURI) {
	case CommonNameSpaceContext.SDN_NS_URI:
	    // the identifier is added as is
	    return identifier;
	case CommonNameSpaceContext.MCP_2_NS_URI:
	    // TODO: Rosetta Stone mapping needed here!
	    // PARAMETER
	    if (identifier.contains("http://vocab.nerc.ac.uk/collection/P01")) {
		// fake record
		return SDN_P02_UNKNOWN;
	    }
	    if (identifier.contains("http://vocab.aodn.org.au/def/discovery_parameter/")) {
		// fake record
		return SDN_P02_UNKNOWN;
	    }
	    // INSTRUMENT
	    if (identifier.contains("http://vocab.nerc.ac.uk/collection/L05/current/")) {
		return identifier.replace("http://vocab.nerc.ac.uk/collection/L05/current/", "http://www.seadatanet.org/urnurl/SDN:L05::")
			+ "/";
	    }
	    if (identifier.contains("vocab.aodn.org.au/def/instrument/entity")) {
		// fake record
		return "http://www.seadatanet.org/urnurl/SDN:L05::UNKNOWN/";
	    }
	    // PLATFORMS
	    if (identifier.contains("http://vocab.nerc.ac.uk/collection/L06/current/")) {
		return identifier.replace("http://vocab.nerc.ac.uk/collection/L06/current/", "http://www.seadatanet.org/urnurl/SDN:L06::")
			+ "/";
	    }
	    if (identifier.contains("http://vocab.aodn.org.au/def/platform/entity")) {
		return SDN_L06_UNKNOWN;
	    }
	    if (identifier.contains("http://vocab.nerc.ac.uk/collection/C17/current")) {
		return SDN_L06_UNKNOWN;
	    }
	    break;
	case CommonNameSpaceContext.NODC_NS_URI:
	    // TODO: Rosetta Stone mapping needed here!
	    if (identifier.contains("www.nodc.noaa.gov/cgi-bin/OAS/prd/datatype/details")) {
		// fake record
		return SDN_P02_UNKNOWN;
	    }
	    if (identifier.contains("www.nodc.noaa.gov/cgi-bin/OAS/prd/insttype/details")) {
		// fake record
		return "http://www.seadatanet.org/urnurl/SDN:L05::UNKNOWN/";
	    }
	    if (identifier.contains("www.nodc.noaa.gov/cgi-bin/OAS/prd/platform")) {
		return SDN_L06_UNKNOWN;
	    }
	    if (identifier.contains("https://www.nodc.noaa.gov/cgi-bin/OAS/prd/institution/details/")) {
		return "http://www.seadatanet.org/urnurl/SDN:EDMO::UNKNOWN/";
	    }

	    break;

	default:
	    // no identifier is added

	}
	return null;
    }
}
