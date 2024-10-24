package eu.essi_lab.accessor.cmr;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;

/**
 * @author roncella
 */
public class CWICCMROpensearchMapperTest {

    private Logger logger = GSLoggerFactory.getLogger(CWICCMROpensearchMapperTest.class);

    @Test
    public void testConceptCollectionMapper() throws SAXException, IOException, XPathExpressionException, TransformerException {
	InputStream stream = CWICCMROpensearchMapperTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/cmr/test/conceptCollection.xml");

	XMLDocumentReader xdoc = new XMLDocumentReader(stream);

	Node[] nodes = xdoc.evaluateNodes("//*:ScienceKeyword");
	List<String> keywords = new ArrayList<String>();
	for (Node keywordNode : nodes) {
	    NodeList listNode = keywordNode.getChildNodes();
	    String keyword = "";

	    for (int j = 0; j < listNode.getLength(); j++) {

		Node element = listNode.item(j);

		if (element.getNodeType() == Node.ELEMENT_NODE) {
		    String text = element.getTextContent().replaceAll("[\\n]", "").trim() + " > ";
		    keyword = keyword + text;

		}
	    }
	    if (!keyword.isEmpty()) {
		keyword = keyword.substring(0, keyword.length() - 3);
		keywords.add(keyword);
	    }
	}

	Assert.assertTrue(keywords.get(0).equals("EARTH SCIENCE > LAND SURFACE > LAND USE/LAND COVER"));
	Assert.assertTrue(keywords.get(1).equals("EARTH SCIENCE > SPECTRAL/ENGINEERING > INFRARED WAVELENGTHS > INFRARED IMAGERY"));

    }

    // @Test
    public void testOpnesarchKeywordMapper() throws SAXException, IOException, XPathExpressionException, TransformerException {
	InputStream stream = CWICCMROpensearchMapperTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/cmr/test/cwic_iso19139response.xml");

	XMLDocumentReader xdoc = new XMLDocumentReader(stream);

	NamespaceContext namespace = xdoc.getNamespaceContext();

	Node[] entries = xdoc.evaluateNodes("//*:entry");

	XMLDocumentReader entryxml = new XMLDocumentReader(XMLDocumentReader.asString(entries[0]));

	entryxml.setNamespaceContext(namespace);

	String firstEntry = entryxml.asString();// XMLDocumentReader.asString(entryxml);

	Assert.assertNotNull(firstEntry);

    }
}