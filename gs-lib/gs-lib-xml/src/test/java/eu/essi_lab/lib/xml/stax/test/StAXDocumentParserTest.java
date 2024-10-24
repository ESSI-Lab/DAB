package eu.essi_lab.lib.xml.stax.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.lib.xml.NameSpace;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.stax.StAXDocumentParser;

/**
 * @author Fabrizio
 */
public class StAXDocumentParserTest {

    static {

	System.setProperty("javax.xml.stream.XMLInputFactory", "com.sun.xml.internal.stream.XMLInputFactoryImpl");
    }

    /**
     * Not a real test, just some code that rebuild a source XML document using STAX. It can be useful, for example, when some parts of the source doc needs to be modified
     * 
     * @throws XMLStreamException
     * @throws IOException
     */
    @Test
    public void hugeFileTest() throws XMLStreamException, IOException {

	InputStream stream = StAXDocumentParserTest.class.getClassLoader().getResourceAsStream("grbyid.xml");

	StreamSource source = new StreamSource(stream);

	XMLInputFactory FACTORY = XMLInputFactory.newFactory("javax.xml.stream.XMLInputFactory", null);

	XMLEventReader reader = FACTORY.createXMLEventReader(source);

	StringBuilder builder = new StringBuilder();

	while (reader.hasNext()) {

	    XMLEvent event = reader.nextEvent();

	    if (event.isStartDocument()) {

	    } else if (event.isStartElement()) {

		StartElement startElement = event.asStartElement();

		QName name = startElement.getName();

		builder.append("<");
		builder.append(name.getPrefix());
		builder.append(":");
		builder.append(name.getLocalPart());

		Stream<Attribute> attrStream = StreamUtils.iteratorToStream(startElement.getAttributes()).map(o -> (Attribute) o);
		List<Attribute> attributes = attrStream.collect(Collectors.toList());

		Stream<Namespace> nameSpacesStream = StreamUtils.iteratorToStream(startElement.getNamespaces()).map(o -> (Namespace) o);
		List<Namespace> namespaces = nameSpacesStream.collect(Collectors.toList());

		if (!attributes.isEmpty() || !namespaces.isEmpty()) {

		    builder.append(" ");
		}

		for (int i = 0; i < attributes.size(); i++) {

		    Attribute next = attributes.get(i);

		    builder.append(next.toString().replace("'", "\""));

		    if (i < attributes.size() - 1) {

			builder.append(" ");
		    }
		}

		for (int i = 0; i < namespaces.size(); i++) {

		    Namespace next = namespaces.get(i);

		    builder.append(next.toString().replace("'", "\""));

		    if (i < namespaces.size() - 1) {

			builder.append(" ");
		    }
		}

		builder.append(">");

	    } else if (event.isCharacters()) {

		String value = ((Characters) event).getData();
		builder.append(value);

	    } else if (event.isEndElement()) {

		EndElement endElement = event.asEndElement();

		QName name = endElement.getName();

		builder.append("</");
		builder.append(name.getPrefix());
		builder.append(":");
		builder.append(name.getLocalPart());
		builder.append(">");

	    } else if (event.isEndDocument()) {

	    }
	}

	//
	//
	//

	// System.out.println(builder);
    }

    @Test
    public void test1() throws XMLStreamException, IOException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("xml-document-1.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	parser.add(Arrays.asList(new QName("test"), new QName("a")), v -> Assert.assertEquals("A", v));
	parser.add(Arrays.asList(new QName("*"), new QName("a")), v -> Assert.assertEquals("A", v));
	parser.add(Arrays.asList(new QName("test"), new QName("a")), v -> Assert.assertNotEquals("x", v));
	parser.add(Arrays.asList(new QName("*"), new QName("a")), v -> Assert.assertNotEquals("x", v));

	parser.add(Arrays.asList(new QName("test"), new QName("b")), v -> Assert.assertEquals("B", v));
	parser.add(Arrays.asList(new QName("*"), new QName("b")), v -> Assert.assertEquals("B", v));
	parser.add(Arrays.asList(new QName("test"), new QName("b")), v -> Assert.assertNotEquals("y", v));
	parser.add(Arrays.asList(new QName("*"), new QName("b")), v -> Assert.assertNotEquals("y", v));

	parser.add(Arrays.asList(new QName("test"), new QName("c")), v -> Assert.assertEquals("C", v));
	parser.add(Arrays.asList(new QName("*"), new QName("c")), v -> Assert.assertEquals("C", v));
	parser.add(Arrays.asList(new QName("test"), new QName("c")), v -> Assert.assertNotEquals("u", v));
	parser.add(Arrays.asList(new QName("*"), new QName("c")), v -> Assert.assertNotEquals("u", v));

	boolean check = parser.checkPaths();

	Assert.assertTrue(check);

	parser.parse();
    }

    @Test
    public void test1_2() throws XMLStreamException, IOException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("xml-document-1.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	//
	// this path do not exists
	//
	parser.add(Arrays.asList(new QName("xxx"), new QName("a")), v -> Assert.assertEquals("A", v));

	parser.add(Arrays.asList(new QName("*"), new QName("a")), v -> Assert.assertEquals("A", v));
	parser.add(Arrays.asList(new QName("test"), new QName("a")), v -> Assert.assertNotEquals("x", v));

	//
	//
	//

	boolean check = parser.checkPaths();

	Assert.assertFalse(check);
    }

    @Test
    public void test2_1() throws XMLStreamException, IOException {

	test2("xml-document-5.xml");
    }

    @Test
    public void test2_2() throws XMLStreamException, IOException {

	test2("xml-document-6.xml");
    }

    /**
     * @param documentName
     * @throws XMLStreamException
     * @throws IOException
     */
    private void test2(String documentName) throws XMLStreamException, IOException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("xml-document-5.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	parser.add(Arrays.asList(new QName("doc"), new QName("aaa"), new QName("a")), v -> Assert.assertEquals("a", v));
	parser.add(Arrays.asList(new QName("*"), new QName("aaa"), new QName("a")), v -> Assert.assertEquals("a", v));

	//
	// this path is ambiguous, since there two <a> elements, one under <aaa> and one under <bbb>
	//

	// parser.add(Arrays.asList(new QName("doc"), new QName("*"), new QName("a")), v -> Assert.assertEquals("a",v));

	//
	// under <aaa> there is only <a>, so the path is OK
	//
	parser.add(Arrays.asList(new QName("*"), new QName("aaa"), new QName("*")), v -> Assert.assertEquals("a", v));

	//
	//
	//

	parser.add(Arrays.asList(new QName("doc"), new QName("bbb"), new QName("a")), v -> Assert.assertEquals("ba", v));

	parser.add(Arrays.asList(new QName("doc"), new QName("aaa"), new QName("a")), v -> Assert.assertNotEquals("x", v));
	parser.add(Arrays.asList(new QName("doc"), new QName("bbb"), new QName("a")), v -> Assert.assertNotEquals("x", v));

	parser.add(Arrays.asList(new QName("doc"), new QName("bbb"), new QName("b1")), v -> Assert.assertEquals("b1", v));
	parser.add(Arrays.asList(new QName("doc"), new QName("bbb"), new QName("b2")), v -> Assert.assertEquals("b2", v));
	parser.add(Arrays.asList(new QName("doc"), new QName("bbb"), new QName("b3")), v -> Assert.assertEquals("b3", v));

	//
	//
	//

	parser.add(Arrays.asList(new QName("doc"), new QName("c")), v -> Assert.assertEquals("C", v));
	parser.add(Arrays.asList(new QName("doc"), new QName("c")), v -> Assert.assertNotEquals("c", v));

	//
	//
	//

	// trim is necessary since the value is parsed as a return char
	parser.add(Arrays.asList(new QName("doc"), new QName("d")), v -> Assert.assertEquals("", v.trim()));

	//
	//
	//

	ArrayList<String> expectedValues = new ArrayList<>();
	expectedValues.add("a");
	expectedValues.add("ba");

	ArrayList<String> foundValues = new ArrayList<>();

	parser.add(new QName("a"), v -> foundValues.add(v));

	//
	//
	//

	boolean check = parser.checkPaths();

	Assert.assertTrue(check);

	//
	//
	//

	parser.parse();

	//
	//
	//

	Assert.assertEquals(expectedValues, foundValues);
    }

    @Test
    public void test3() throws XMLStreamException, IOException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("atom-feed.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	parser.add(Arrays.asList(new QName("feed"), new QName("title")), v -> Assert.assertEquals("Search results", v));

	//
	//
	//

	ArrayList<String> expectedEntryTitles = new ArrayList<>();

	expectedEntryTitles.add("Sentinel 1A");
	expectedEntryTitles.add("Sentinel 1B");
	expectedEntryTitles.add("Sentinel 2A");
	expectedEntryTitles.add("Sentinel 2B");
	expectedEntryTitles.add("Sentinel 3A");
	expectedEntryTitles.add("USGS Landsat 8 OLI/TIRS");
	expectedEntryTitles.add("Residual Water Content at 0-150 cm of Depth");
	expectedEntryTitles.add("ABoVE Fire Weather Index 2010203");
	expectedEntryTitles.add("ABoVE Drought Code 2004157");
	expectedEntryTitles.add("NLCD 2001: Percent Tree Canopy (Puerto Rico)");

	ArrayList<String> foundEntryTitles = new ArrayList<>();

	parser.add(Arrays.asList(new QName("feed"), new QName("entry"), new QName("title")), v -> foundEntryTitles.add(v));

	//
	//
	//

	ArrayList<String> expectedOrgNames = new ArrayList<>();

	expectedOrgNames.add("European Commission");
	expectedOrgNames.add("European Commission");
	expectedOrgNames.add("European Commission");
	expectedOrgNames.add("European Commission");
	expectedOrgNames.add("European Commission");
	expectedOrgNames.add("United States Geological Survey");
	expectedOrgNames.add("Oak Ridge National Laboratory Distributed Active Archive Center (ORNL DAAC -WCS)");
	expectedOrgNames.add("Oak Ridge National Laboratory Distributed Active Archive Center (ORNL DAAC -WMS)");
	expectedOrgNames.add("Oak Ridge National Laboratory Distributed Active Archive Center (ORNL DAAC -WMS)");
	expectedOrgNames.add("Oak Ridge National Laboratory Distributed Active Archive Center (ORNL DAAC -WMS)");

	ArrayList<String> foundOrgNames = new ArrayList<>();

	parser.add(Arrays.asList(new QName("feed"), new QName("entry"), new QName("contributor"), new QName("orgName")),
		v -> foundOrgNames.add(v));

	//
	//
	//

	boolean check = parser.checkPaths();

	Assert.assertTrue(check);

	//
	//
	//

	parser.parse();

	//
	//
	//

	Assert.assertEquals(expectedEntryTitles, foundEntryTitles);

	Assert.assertEquals(expectedOrgNames, foundOrgNames);
    }

    @Test
    public void test3_1() throws XMLStreamException, IOException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("atom-feed.xml");

	StAXDocumentParser parser = new StAXDocumentParser(IOStreamUtils.asUTF8String(stream));

	parser.add(Arrays.asList(new QName("feed"), new QName("title")), v -> Assert.assertEquals("Search results", v));

	//
	//
	//

	ArrayList<String> expectedEntryTitles = new ArrayList<>();

	expectedEntryTitles.add("Sentinel 1A");
	expectedEntryTitles.add("Sentinel 1B");
	expectedEntryTitles.add("Sentinel 2A");
	expectedEntryTitles.add("Sentinel 2B");
	expectedEntryTitles.add("Sentinel 3A");
	expectedEntryTitles.add("USGS Landsat 8 OLI/TIRS");
	expectedEntryTitles.add("Residual Water Content at 0-150 cm of Depth");
	expectedEntryTitles.add("ABoVE Fire Weather Index 2010203");
	expectedEntryTitles.add("ABoVE Drought Code 2004157");
	expectedEntryTitles.add("NLCD 2001: Percent Tree Canopy (Puerto Rico)");

	ArrayList<String> foundEntryTitles = new ArrayList<>();

	parser.add(Arrays.asList(new QName("feed"), new QName("entry"), new QName("title")), v -> foundEntryTitles.add(v));

	//
	//
	//

	ArrayList<String> expectedOrgNames = new ArrayList<>();

	expectedOrgNames.add("European Commission");
	expectedOrgNames.add("European Commission");
	expectedOrgNames.add("European Commission");
	expectedOrgNames.add("European Commission");
	expectedOrgNames.add("European Commission");
	expectedOrgNames.add("United States Geological Survey");
	expectedOrgNames.add("Oak Ridge National Laboratory Distributed Active Archive Center (ORNL DAAC -WCS)");
	expectedOrgNames.add("Oak Ridge National Laboratory Distributed Active Archive Center (ORNL DAAC -WMS)");
	expectedOrgNames.add("Oak Ridge National Laboratory Distributed Active Archive Center (ORNL DAAC -WMS)");
	expectedOrgNames.add("Oak Ridge National Laboratory Distributed Active Archive Center (ORNL DAAC -WMS)");

	ArrayList<String> foundOrgNames = new ArrayList<>();

	parser.add(Arrays.asList(new QName("feed"), new QName("entry"), new QName("contributor"), new QName("orgName")),
		v -> foundOrgNames.add(v));

	//
	//
	//

	ArrayList<String> expectedUrls = new ArrayList<>();

	expectedUrls.add("http://webmap.ornl.gov/ogcbroker/wcs?");
	expectedUrls.add("https://gs-service-preproduction.geodab.eu/gs-service/services/essi/wms?");
	expectedUrls.add("https://webmap.ornl.gov/ogcbroker/wms?");
	expectedUrls.add("https://gs-service-preproduction.geodab.eu/gs-service/services/essi/wms?");
	expectedUrls.add("https://webmap.ornl.gov/ogcbroker/wms?");
	expectedUrls.add("https://gs-service-preproduction.geodab.eu/gs-service/services/essi/wms?");
	expectedUrls.add("https://webmap.ornl.gov/ogcbroker/wms?");
	expectedUrls.add("https://gs-service-preproduction.geodab.eu/gs-service/services/essi/wms?");

	ArrayList<String> foundUrls = new ArrayList<>();

	parser.add(Arrays.asList(

		new QName("feed"), //

		new QName("entry"), //

		new QName("http://www.isotc211.org/2005/gmd", "distributionInfo"), //

		new QName("http://www.isotc211.org/2005/gmd", "MD_Distribution"), //

		new QName("http://www.isotc211.org/2005/gmd", "transferOptions"), //

		new QName("http://www.isotc211.org/2005/gmd", "MD_DigitalTransferOptions"), //

		new QName("http://www.isotc211.org/2005/gmd", "onLine"), //

		new QName("http://www.isotc211.org/2005/gmd", "CI_OnlineResource"), //

		new QName("http://www.isotc211.org/2005/gmd", "linkage"), //

		new QName("http://www.isotc211.org/2005/gmd", "URL")), //

		v -> foundUrls.add(v));

	//
	//
	//

	parser.checkPathsAndParse();

	//
	//
	//

	Assert.assertEquals(expectedEntryTitles, foundEntryTitles);

	Assert.assertEquals(expectedOrgNames, foundOrgNames);

	Assert.assertEquals(expectedUrls, foundUrls);
    }

    @Test
    public void datasetTest() throws XMLStreamException, IOException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("dataset.xml");

	StAXDocumentParser parser = new StAXDocumentParser(IOStreamUtils.asUTF8String(stream));

	//
	//
	//

	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "west"), v -> Assert.assertEquals("-4.62779", v));

	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "south"), v -> Assert.assertEquals("55.59709", v));

	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "east"), v -> Assert.assertEquals("-4.62779", v));

	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "north"), v -> Assert.assertEquals("55.59709", v));

	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "platformTitle"), v -> Assert.assertEquals("Irvine at Shewalton", v));

	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "uniquePlatformId"),
		v -> Assert.assertEquals("00097A849F94A7508A2E0598FC1F93FF2A9A604F", v));

	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "sourceId"), v -> Assert.assertEquals("uk-nrfa", v));

	parser.add(new QName(NameSpace.GS_DATA_MODEL_SCHEMA_URI, "Country"),
		v -> Assert.assertEquals("United Kingdom of Great Britain and Northern Ireland", v));

	parser.checkPathsAndParse();
    }

    @Test
    public void test4() throws XMLStreamException, IOException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("xml-document-7.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	ArrayList<String> expectedValues = new ArrayList<>();
	expectedValues.add("a");
	expectedValues.add("ba");
	expectedValues.add("bca");
	expectedValues.add("bcda");

	ArrayList<String> foundValues = new ArrayList<>();

	parser.add(new QName("a"), v -> foundValues.add(v));

	parser.parse();

	Assert.assertEquals(expectedValues, foundValues);
    }

    @Test
    public void test5() throws XMLStreamException, IOException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("xml-document-7.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	parser.add(new QName("aaa"), new QName("a"), v -> Assert.assertEquals("a", v));

	parser.add(new QName("bbb"), new QName("a"), v -> Assert.assertEquals("ba", v));

	parser.add(new QName("c"), new QName("a"), v -> Assert.assertEquals("bca", v));

	parser.add(new QName("d"), new QName("a"), v -> Assert.assertEquals("bcda", v));

	parser.parse();
    }

    @Test
    public void test6() throws XMLStreamException, IOException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("md.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	ArrayList<String> expectedEmails = new ArrayList<>();

	expectedEmails.add("info@pangaea.de");
	expectedEmails.add("Bernard.Dennielou@ifremer.fr");
	expectedEmails.add("cortijo@lsce.cnrs-gif.fr");
	expectedEmails.add("david.vanrooij@rug.ac.be");
	expectedEmails.add("f.grousset@epoc.u-bordeaux1.fr");
	expectedEmails.add("pujol@geocean.u-bordeaux.fr");
	expectedEmails.add("f.eynaud@epoc.u-bordeaux1.fr");
	expectedEmails.add("info@pangaea.de");
	expectedEmails.add("info@pangaea.de");

	ArrayList<String> foundEmails = new ArrayList<>();

	parser.add(//
		new QName("http://www.isotc211.org/2005/gmd", "electronicMailAddress"), //
		new QName("http://www.isotc211.org/2005/gco", "CharacterString"), //
		v -> foundEmails.add(v));

	//
	//
	//

	ArrayList<String> expectedTitles = new ArrayList<>();

	expectedTitles.add("Geochemistry and age models of sediments from the Celtic margin");
	expectedTitles.add("PANGAEA Project List");

	ArrayList<String> foundTitles = new ArrayList<>();

	parser.add(//
		new QName("http://www.isotc211.org/2005/gmd", "title"), //
		new QName("http://www.isotc211.org/2005/gco", "CharacterString"), //
		v -> foundTitles.add(v));

	//
	//
	//

	ArrayList<String> expectedNames = new ArrayList<>();

	expectedNames.add("Auffret, Gérard A");
	expectedNames.add("Zaragosi, Sebastien");
	expectedNames.add("Dennielou, Bernard");
	expectedNames.add("Cortijo, Elsa");
	expectedNames.add("Van Rooij, David");
	expectedNames.add("Grousset, Francis E");
	expectedNames.add("Pujol, Claude");
	expectedNames.add("Eynaud, Frédérique");
	expectedNames.add("Siegert, Martin J");

	ArrayList<String> foundNames = new ArrayList<>();

	parser.add(//
		new QName("http://www.isotc211.org/2005/gmd", "individualName"), //
		new QName("http://www.isotc211.org/2005/gco", "CharacterString"), //
		v -> foundNames.add(v));

	//
	//
	//

	parser.parse();

	//
	//
	//

	Assert.assertEquals(expectedEmails, foundEmails);
	Assert.assertEquals(expectedTitles, foundTitles);
	Assert.assertEquals(expectedNames, foundNames);
    }

    @Test
    public void test6_1() throws XMLStreamException, IOException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("md.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	ArrayList<String> expectedEmails = new ArrayList<>();

	expectedEmails.add("info@pangaea.de");
	expectedEmails.add("Bernard.Dennielou@ifremer.fr");
	expectedEmails.add("cortijo@lsce.cnrs-gif.fr");
	expectedEmails.add("david.vanrooij@rug.ac.be");
	expectedEmails.add("f.grousset@epoc.u-bordeaux1.fr");
	expectedEmails.add("pujol@geocean.u-bordeaux.fr");
	expectedEmails.add("f.eynaud@epoc.u-bordeaux1.fr");
	expectedEmails.add("info@pangaea.de");
	expectedEmails.add("info@pangaea.de");

	ArrayList<String> foundEmails = new ArrayList<>();

	parser.add(//
		new QName("http://www.isotc211.org/2005/gmd", "electronicMailAddress"), //
		new QName("*"), //
		v -> foundEmails.add(v));

	//
	//
	//

	ArrayList<String> expectedTitles = new ArrayList<>();

	expectedTitles.add("Geochemistry and age models of sediments from the Celtic margin");
	expectedTitles.add("PANGAEA Project List");

	ArrayList<String> foundTitles = new ArrayList<>();

	parser.add(//
		new QName("http://www.isotc211.org/2005/gmd", "title"), //
		new QName("*"), //
		v -> foundTitles.add(v));

	//
	//
	//

	ArrayList<String> expectedNames = new ArrayList<>();

	expectedNames.add("Auffret, Gérard A");
	expectedNames.add("Zaragosi, Sebastien");
	expectedNames.add("Dennielou, Bernard");
	expectedNames.add("Cortijo, Elsa");
	expectedNames.add("Van Rooij, David");
	expectedNames.add("Grousset, Francis E");
	expectedNames.add("Pujol, Claude");
	expectedNames.add("Eynaud, Frédérique");
	expectedNames.add("Siegert, Martin J");

	ArrayList<String> foundNames = new ArrayList<>();

	parser.add(//
		new QName("http://www.isotc211.org/2005/gmd", "individualName"), //
		new QName("*"), //
		v -> foundNames.add(v));

	//
	//
	//

	parser.parse();

	//
	//
	//

	Assert.assertEquals(expectedEmails, foundEmails);
	Assert.assertEquals(expectedTitles, foundTitles);
	Assert.assertEquals(expectedNames, foundNames);
    }

    @Test
    public void test6_2() throws XMLStreamException, IOException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("md.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	ArrayList<String> expectedEmails = new ArrayList<>();

	expectedEmails.add("info@pangaea.de");
	expectedEmails.add("Bernard.Dennielou@ifremer.fr");
	expectedEmails.add("cortijo@lsce.cnrs-gif.fr");
	expectedEmails.add("david.vanrooij@rug.ac.be");
	expectedEmails.add("f.grousset@epoc.u-bordeaux1.fr");
	expectedEmails.add("pujol@geocean.u-bordeaux.fr");
	expectedEmails.add("f.eynaud@epoc.u-bordeaux1.fr");
	expectedEmails.add("info@pangaea.de");
	expectedEmails.add("info@pangaea.de");

	ArrayList<String> foundEmails = new ArrayList<>();

	parser.add(//
		new QName("electronicMailAddress"), //
		new QName("CharacterString"), //
		v -> foundEmails.add(v));

	//
	//
	//

	ArrayList<String> expectedTitles = new ArrayList<>();

	expectedTitles.add("Geochemistry and age models of sediments from the Celtic margin");
	expectedTitles.add("PANGAEA Project List");

	ArrayList<String> foundTitles = new ArrayList<>();

	parser.add(//
		new QName("title"), //
		new QName("CharacterString"), //
		v -> foundTitles.add(v));

	//
	//
	//

	ArrayList<String> expectedNames = new ArrayList<>();

	expectedNames.add("Auffret, Gérard A");
	expectedNames.add("Zaragosi, Sebastien");
	expectedNames.add("Dennielou, Bernard");
	expectedNames.add("Cortijo, Elsa");
	expectedNames.add("Van Rooij, David");
	expectedNames.add("Grousset, Francis E");
	expectedNames.add("Pujol, Claude");
	expectedNames.add("Eynaud, Frédérique");
	expectedNames.add("Siegert, Martin J");

	ArrayList<String> foundNames = new ArrayList<>();

	parser.add(//
		new QName("individualName"), //
		new QName("CharacterString"), //
		v -> foundNames.add(v));

	//
	//
	//

	parser.parse();

	//
	//
	//

	Assert.assertEquals(expectedEmails, foundEmails);
	Assert.assertEquals(expectedTitles, foundTitles);
	Assert.assertEquals(expectedNames, foundNames);
    }

    @Test
    public void parseTest11() throws XMLStreamException, IOException, SAXException, XPathExpressionException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("xml-document-11.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	ArrayList<String> values = new ArrayList<>();

	parser.add(new QName("R"), v -> values.add(v));

	parser.parse();

	Assert.assertEquals(1, values.size());

	Assert.assertEquals("text", values.get(0));
    }

    /**
     * @throws IOException
     * @throws XMLStreamException
     */
    @Test
    public void conditionsTest1() throws XMLStreamException, IOException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("md.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	parser.add(//
		stack -> stack.peek().isCharacters() && !stack.peek().asCharacters().getData().trim().isEmpty(), //
		stack -> System.out.println("[" + stack.peek().asCharacters().getData().trim() + "]"));

	parser.add(//
		stack -> stack.peek().isStartElement() && stack.peek().asStartElement().getAttributes().hasNext(), //
		stack -> System.out.println("(" + stack.peek().asStartElement().getAttributes().next() + ")"));

	//
	//
	//

	parser.parse();
    }

    /**
     * @throws IOException
     * @throws XMLStreamException
     */
    @Test
    public void conditionsTest2() throws XMLStreamException, IOException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("md.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	ArrayList<String> expectedDistinctRoleCodes = new ArrayList<>();
	expectedDistinctRoleCodes.add("http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_RoleCode_author");
	expectedDistinctRoleCodes.add("http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_RoleCode_pointOfContact");
	expectedDistinctRoleCodes.add("http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_RoleCode_publisher");

	List<String> foundRoleCodes = new ArrayList<>();

	parser.add(//
		stack -> stack.peek().isStartElement() && stack.peek().asStartElement().getName().getLocalPart().equals("CI_RoleCode")
			&& stack.peek().asStartElement().getAttributes().hasNext(), //

		stack -> foundRoleCodes.add(((Attribute) stack.peek().asStartElement().getAttributes().next()).getValue()));

	//
	//
	//

	ArrayList<String> expectedDistinctFunctionCodes = new ArrayList<>();

	expectedDistinctFunctionCodes
		.add("http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_OnLineFunctionCode_download");
	expectedDistinctFunctionCodes
		.add("http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_OnLineFunctionCode_information");

	List<String> foundFunctionCodes = new ArrayList<>();

	parser.add(//
		stack -> stack.peek().isStartElement()
			&& stack.peek().asStartElement().getName().getLocalPart().equals("CI_OnLineFunctionCode")
			&& stack.peek().asStartElement().getAttributes().hasNext(), //

		stack -> foundFunctionCodes.add(((Attribute) stack.peek().asStartElement().getAttributes().next()).getValue()));

	//
	//
	//

	parser.parse();

	//
	//
	//

	List<String> distinctRoleCodes = foundRoleCodes.stream().distinct().sorted().collect(Collectors.toList());
	Assert.assertEquals(expectedDistinctRoleCodes, distinctRoleCodes);

	//
	//
	//

	List<String> distinctFunctionCodes = foundFunctionCodes.stream().distinct().sorted().collect(Collectors.toList());
	Assert.assertEquals(expectedDistinctFunctionCodes, distinctFunctionCodes);
    }

    @Test
    public void findWithQNameTest() throws XMLStreamException, IOException, SAXException, XPathExpressionException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("md.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	List<String> result = parser.find(new QName("CI_ResponsibleParty"));

	Assert.assertEquals(12, result.size());

	//
	//
	//

	List<String> expectedMailAddresses = new ArrayList<>();

	expectedMailAddresses.add("info@pangaea.de");
	expectedMailAddresses.add("");
	expectedMailAddresses.add("");
	expectedMailAddresses.add("Bernard.Dennielou@ifremer.fr");
	expectedMailAddresses.add("cortijo@lsce.cnrs-gif.fr");
	expectedMailAddresses.add("david.vanrooij@rug.ac.be");
	expectedMailAddresses.add("f.grousset@epoc.u-bordeaux1.fr");
	expectedMailAddresses.add("pujol@geocean.u-bordeaux.fr");
	expectedMailAddresses.add("f.eynaud@epoc.u-bordeaux1.fr");
	expectedMailAddresses.add("");
	expectedMailAddresses.add("info@pangaea.de");
	expectedMailAddresses.add("info@pangaea.de");

	List<String> foundMailAddresses = new ArrayList<>();

	for (String element : result) {

	    XMLDocumentReader reader = new XMLDocumentReader(element);

	    Node[] nodes = reader.evaluateNodes("//*:CI_ResponsibleParty");

	    Assert.assertEquals(1, nodes.length);

	    List<String> list = reader.evaluateTextContent("//*:electronicMailAddress/*:CharacterString/text()");

	    if (!list.isEmpty()) {
		foundMailAddresses.add(list.get(0));
	    } else {
		foundMailAddresses.add("");
	    }
	}

	Assert.assertEquals(expectedMailAddresses, foundMailAddresses);
    }

    @Test
    public void findWithQNameTest2() throws XMLStreamException, IOException, SAXException, XPathExpressionException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("xml-document-8.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	List<String> result = parser.find(new QName("aaa"));

	Assert.assertEquals(4, result.size());

	//
	//
	//
	List<String> expectedLocalNames = new ArrayList<>();

	expectedLocalNames.add("a1");
	expectedLocalNames.add("a2");
	expectedLocalNames.add("a3");
	expectedLocalNames.add("a4");

	List<String> expectedTextValues = new ArrayList<>();

	expectedTextValues.add("text1");
	expectedTextValues.add("text2");
	expectedTextValues.add("text3");
	expectedTextValues.add("text4");

	List<String> expectedAttrValues = new ArrayList<>();

	expectedAttrValues.add("attr1");
	expectedAttrValues.add("attr2");
	expectedAttrValues.add("attr3");
	expectedAttrValues.add("attr4");

	List<String> foundTextValues = new ArrayList<>();
	List<String> foundLocalNames = new ArrayList<>();
	List<String> foundAttrValues = new ArrayList<>();

	for (String element : result) {

	    StAXDocumentParser innerParser = new StAXDocumentParser(element);

	    innerParser.add(//
		    stack -> stack.peek().isCharacters() && !stack.peek().asCharacters().getData().trim().isEmpty(),

		    stack -> foundTextValues.add(stack.peek().asCharacters().getData()));

	    innerParser.add(//
		    stack -> stack.peek().isStartElement()
			    && expectedLocalNames.contains(stack.peek().asStartElement().getName().getLocalPart()),

		    stack -> foundLocalNames.add(stack.peek().asStartElement().getName().getLocalPart()));

	    innerParser.add(//
		    stack -> stack.peek().isStartElement()
			    && expectedLocalNames.contains(stack.peek().asStartElement().getName().getLocalPart()),

		    stack -> foundAttrValues.add(((Attribute) stack.peek().asStartElement().getAttributes().next()).getValue()));

	    innerParser.parse();
	}

	Assert.assertEquals(expectedTextValues, foundTextValues);

	Assert.assertEquals(expectedLocalNames, foundLocalNames);

	Assert.assertEquals(expectedAttrValues, foundAttrValues);
    }

    @Test
    public void findWithQNameTest3() throws XMLStreamException, IOException, SAXException, XPathExpressionException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("xml-document-8.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	List<String> result = parser.find(new QName("xxx"));

	Assert.assertEquals(0, result.size());
    }

    @Test
    public void findWithQNameTest4() throws XMLStreamException, IOException, SAXException, XPathExpressionException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("xml-document-11.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	List<String> result = parser.find(new QName("*"));

	Assert.assertEquals(1, result.size());

	StAXDocumentParser parser2 = new StAXDocumentParser(result.get(0));

	parser2.add(new QName("*"), "a", v -> Assert.assertEquals("pippo", v));
	parser2.add(new QName("*"), "b", v -> Assert.assertEquals("pluto", v));
	parser2.add(new QName("*"), "c", v -> Assert.assertEquals("topolino", v));

	parser2.add(new QName("*"), "a", v -> Assert.assertNotEquals("x", v));
	parser2.add(new QName("*"), "b", v -> Assert.assertNotEquals("y", v));
	parser2.add(new QName("*"), "c", v -> Assert.assertNotEquals("z", v));
    }

    @Test
    public void findWithParentTest() throws XMLStreamException, IOException, SAXException, XPathExpressionException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("xml-document-8.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	//
	// finds the <aaa> elements that are children of the <bbb> element
	//
	List<String> result = parser.find(new QName("bbb"), new QName("aaa"));

	Assert.assertEquals(1, result.size());

	//
	//
	//

	List<String> expectedLocalNames = new ArrayList<>();

	expectedLocalNames.add("aaa");
	expectedLocalNames.add("a2");

	List<String> expectedTextValues = new ArrayList<>();

	expectedTextValues.add("text2");

	List<String> expectedAttrValues = new ArrayList<>();

	expectedAttrValues.add("attr2");

	List<String> foundTextValues = new ArrayList<>();
	List<String> foundLocalNames = new ArrayList<>();
	List<String> foundAttrValues = new ArrayList<>();

	StAXDocumentParser innerParser = new StAXDocumentParser(result.get(0));

	innerParser.add(new QName("*"), v -> foundTextValues.add(v));

	//
	// the above call can be replaced with the following (just as example)
	//
	// innerParser.add(//
	// stack -> stack.peek().isCharacters() && !stack.peek().asCharacters().getData().trim().isEmpty(),
	// stack -> foundTextValues.add(stack.peek().asCharacters().getData()));

	innerParser.add(//
		stack -> stack.peek().isStartElement(),
		stack -> foundLocalNames.add(stack.peek().asStartElement().getName().getLocalPart()));

	innerParser.add(new QName("a2"), "attr", v -> foundAttrValues.add(v));

	innerParser.parse();

	//
	//
	//

	Assert.assertEquals(expectedTextValues, foundTextValues);

	Assert.assertEquals(expectedLocalNames, foundLocalNames);

	Assert.assertEquals(expectedAttrValues, foundAttrValues);
    }

    @Test
    public void findWithParentTest2() throws XMLStreamException, IOException, SAXException, XPathExpressionException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("xml-document-8.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	//
	// finds the elements that are children of the <aaa> element
	//
	List<String> result = parser.find(new QName("aaa"), new QName("*"));

	Assert.assertEquals(4, result.size());

	//
	//
	//

	List<String> expectedLocalNames = new ArrayList<>();

	expectedLocalNames.add("a1");
	expectedLocalNames.add("a2");
	expectedLocalNames.add("a3");
	expectedLocalNames.add("a4");

	List<String> expectedTextValues = new ArrayList<>();

	expectedTextValues.add("text1");
	expectedTextValues.add("text2");
	expectedTextValues.add("text3");
	expectedTextValues.add("text4");

	List<String> expectedAttrValues = new ArrayList<>();

	expectedAttrValues.add("attr1");
	expectedAttrValues.add("attr2");
	expectedAttrValues.add("attr3");
	expectedAttrValues.add("attr4");

	List<String> foundTextValues = new ArrayList<>();
	List<String> foundLocalNames = new ArrayList<>();
	List<String> foundAttrValues = new ArrayList<>();

	for (String element : result) {

	    StAXDocumentParser innerParser = new StAXDocumentParser(element);

	    innerParser.add(new QName("*"), v -> foundTextValues.add(v));

	    innerParser.add(//
		    stack -> stack.peek().isStartElement(),
		    stack -> foundLocalNames.add(stack.peek().asStartElement().getName().getLocalPart()));

	    // parses the attributes "attr" of any elements
	    innerParser.add(new QName("*"), "attr", v -> foundAttrValues.add(v));

	    innerParser.parse();
	}

	//
	//
	//

	Assert.assertEquals(expectedTextValues, foundTextValues);

	Assert.assertEquals(expectedLocalNames, foundLocalNames);

	Assert.assertEquals(expectedAttrValues, foundAttrValues);
    }

    @Test
    public void findWithParentTest3() throws XMLStreamException, IOException, SAXException, XPathExpressionException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("xml-document-8.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	//
	// finds the <xxx> elements that are children of the <bbb> element (none exists)
	//
	List<String> result = parser.find(new QName("bbb"), new QName("xxx"));

	Assert.assertEquals(0, result.size());
    }

    @Test
    public void parseWithAttributeTest() throws XMLStreamException, IOException, SAXException, XPathExpressionException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("xml-document-8.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	ArrayList<String> values = new ArrayList<>();

	parser.add(new QName("a1"), "attr", v -> values.add(v));

	parser.parse();

	Assert.assertEquals(1, values.size());

	Assert.assertEquals("attr1", values.get(0));
    }

    @Test
    public void parseWithAttributeTest2() throws XMLStreamException, IOException, SAXException, XPathExpressionException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("xml-document-9.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	ArrayList<String> values = new ArrayList<>();

	parser.add(new QName("doc"), "a", v -> values.add(v));
	parser.add(new QName("doc"), "b", v -> values.add(v));
	parser.add(new QName("doc"), "c", v -> values.add(v));

	parser.parse();

	Assert.assertEquals(3, values.size());

	Assert.assertEquals("pippo", values.get(0));
	Assert.assertEquals("pluto", values.get(1));
	Assert.assertEquals("topolino", values.get(2));
    }

    @Test
    public void parseWithAttributeTest3() throws XMLStreamException, IOException, SAXException, XPathExpressionException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("xml-document-10.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	final ArrayList<String> values = new ArrayList<>();

	parser.add(new QName("R"), "a", v -> values.add(v));
	parser.add(new QName("R"), "b", v -> values.add(v));
	parser.add(new QName("R"), "c", v -> values.add(v));

	parser.parse();

	Assert.assertEquals(3, values.size());

	Assert.assertEquals("pippo", values.get(0));
	Assert.assertEquals("pluto", values.get(1));
	Assert.assertEquals("topolino", values.get(2));
    }
    
    @Test
    public void parseDocumentWithSeveralIssues() throws XMLStreamException, IOException, SAXException, XPathExpressionException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("xml-document-12.xml");

	StAXDocumentParser parser = new StAXDocumentParser(stream);

	Optional<String> offering = parser.find(new QName("ObservationOffering")).//
		stream().//
		filter(v -> v.contains("name>OFFERING-NAME")).//
		findFirst();

	final List<String> description = new ArrayList<String>();

	if (offering.isPresent()) {

	    parser = new StAXDocumentParser(offering.get());
	    parser.add(new QName("description"), v -> description.add(v));
	    parser.parse();
	}
	
	Assert.assertEquals("OFFERING-DESC", description.stream().findFirst().orElse(""));
    }
}
