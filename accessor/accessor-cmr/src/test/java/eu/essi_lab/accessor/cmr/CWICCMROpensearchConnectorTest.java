package eu.essi_lab.accessor.cmr;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;

/**
 * @author roncella
 */
public class CWICCMROpensearchConnectorTest {

    private Logger logger = GSLoggerFactory.getLogger(CWICCMROpensearchConnectorTest.class);

    @Test
    public void testFirstEntryCWICResponse() throws SAXException, IOException, XPathExpressionException, TransformerException {
	InputStream stream = CWICCMROpensearchConnectorTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/cmr/test/cwic_iso19139response.xml");

	XMLDocumentReader xdoc = new XMLDocumentReader(stream);

	Node[] entries = xdoc.evaluateNodes("//*:entry");
	String firstEntry = XMLDocumentReader.asString(entries[0]);

	Assert.assertNotNull(firstEntry);

    }

    @Test
    public void testCWICResponse() throws SAXException, IOException, XPathExpressionException, TransformerException {
	InputStream stream = CWICCMROpensearchConnectorTest.class.getClassLoader()
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