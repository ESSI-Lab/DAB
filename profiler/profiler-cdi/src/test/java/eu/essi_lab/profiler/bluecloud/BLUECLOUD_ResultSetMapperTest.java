package eu.essi_lab.profiler.bluecloud;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;

import eu.essi_lab.jaxb.common.ISO2014NameSpaceContext;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;
import junit.framework.TestCase;

public class BLUECLOUD_ResultSetMapperTest {

    private BlueCloudResultSetMapper mapper;

    @Before
    public void init() {
	this.mapper = new BlueCloudResultSetMapper();
    }

    @Test
    public void testFromSeadatanetOpen() throws Exception {
	InputStream stream = BLUECLOUD_ResultSetMapperTest.class.getClassLoader().getResourceAsStream("seadatanet-openMI_Metadata.xml");
	TestCase.assertNotNull(stream);

	XMLDocumentReader reader = new XMLDocumentReader(stream);

	reader.setNamespaceContext(new ISO2014NameSpaceContext());

	XMLDocumentWriter writer = new XMLDocumentWriter(reader);
	reader.setNamespaceContext(new ISO2014NameSpaceContext());

	// Node node = reader.evaluateNode("//*:MI_Metadata[1]");
	// NodeList children = node.getChildNodes();
	// for(int i=0; i < children.getLength(); i++) {
	// writer.addNode("/gmi:MI_Metadata", children.item(i));
	// }
	writer.rename("//gmd:MD_CoverageDescription", "gmi2019:MI_CoverageDescription");

	Node[] resultGmi = reader.evaluateNodes("//gmiold:*");

	for (Node n : resultGmi) {
	    writer.rename(n, ".", "gmi2019:" + n.getLocalName());
	}

	Node[] resultGml = reader.evaluateNodes("//gmlold:*");

	for (Node n : resultGml) {
	    writer.rename(n, ".", "gml32:" + n.getLocalName());
	}

	// 1) organisation: //gmd:CI_ResponsibleParty/gmd:organisationName/*[1]
	Node[] organizations = reader.evaluateNodes("//gmd:CI_ResponsibleParty/gmd:organisationName/*[1]");

	Assert.assertTrue(organizations.length == 4);
	// 2) title:
	// /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/*[1]
	String title = reader.evaluateString(
		"/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/*[1]");
	Assert.assertTrue(title.startsWith("SeaDataNet - Marine geology from Israel Oceanographic"));
	// 3) platform:
	// /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='platform']/gmd:keyword/*[1]
	// it seems that seadatanet open contains "@codeListValue='platform_class'" instead of
	// "@codeListValue='platform'"
	Node[] platforms = reader.evaluateNodes(
		"/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='platform']/gmd:keyword/*[1]");
	Assert.assertTrue(platforms.length == 0);
	if (platforms.length == 0) {
	    Node[] platforms_classes = reader.evaluateNodes(
		    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='platform_class']/gmd:keyword/*[1]");
	    Assert.assertTrue(platforms_classes.length == 1);
	    Node[] nodes = reader.evaluateNodes(
		    "/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:type/gmd:MD_KeywordTypeCode/@codeListValue");

	    for (Node n : nodes) {
		if (n.getNodeValue() != null && n.getNodeValue().contains("platform")) {
		    n.setNodeValue("platform");
		}
	    }

	}

	// 4) instrument:
	// /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='instrument']/gmd:keyword/*[1]
	Node[] instruments = reader.evaluateNodes(
		"/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[gmd:type/gmd:MD_KeywordTypeCode/@codeListValue='instrument']/gmd:keyword/*[1]");
	Assert.assertTrue(instruments.length == 2);
	// 5) keywords:
	// /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[not(gmd:type)
	// or not(contains('platform instrument',gmd:type/gmd:MD_KeywordTypeCode/@codeListValue))]/gmd:keyword/*[1]
	Node[] keywords = reader.evaluateNodes(
		"/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords[not(gmd:type) or not(contains('platform instrument',gmd:type/gmd:MD_KeywordTypeCode/@codeListValue))]/gmd:keyword/*[1]");
	Assert.assertTrue(keywords.length == 20);
	// 6) bbox
	// west:
	// /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal
	String west = reader.evaluateString(
		"/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal");
	Assert.assertEquals("34.5054168701", west);
	// east:
	// /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal
	String east = reader.evaluateString(
		"/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal");
	Assert.assertEquals("35.1102218628", east);
	// south:
	// /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal
	String south = reader.evaluateString(
		"/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal");
	Assert.assertEquals("31.6141662598", south);
	// north:
	// /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal
	String north = reader.evaluateString(
		"/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal");
	Assert.assertEquals("33.0770835876", north);
	// 7) temporal extent
	// start:
	// /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition
	String start = reader.evaluateString(
		"/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:beginPosition");
	Assert.assertEquals("2001-03-12", start);
	// end:
	// /gmi:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition
	String end = reader.evaluateString(
		"/gmi2019:MI_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml32:TimePeriod/gml32:endPosition");
	Assert.assertEquals("2018-12-23", end);
	// 8) parameter:
	// /gmi:MI_Metadata/gmd:contentInfo/gmi:MI_CoverageDescription/gmd:attributeDescription/gco:RecordType

	Node[] res1 = reader
		.evaluateNodes("gmi2019:MI_Metadata/gmd:contentInfo/gmi2019:MI_CoverageDescription/gmd:attributeDescription/gco:RecordType");

	Assert.assertTrue(res1.length == 19);

    }

}
