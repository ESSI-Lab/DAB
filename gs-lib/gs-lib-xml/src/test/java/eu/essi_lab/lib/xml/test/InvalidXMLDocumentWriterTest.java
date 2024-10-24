/**
 * 
 */
package eu.essi_lab.lib.xml.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;

/**
 * @author Fabrizio
 */
public class InvalidXMLDocumentWriterTest {

    /**
     * This test works with a WCS Capabilities document containing many coverage descriptions and 7134 occurrences of
     * the text
     * "null" outside any other elements thus invalidating the document. When all the coverages are removed except one,
     * all the
     * "null" elements remain. The test evaluates an xPath on the remained coverage description in order to demonstrate
     * that
     * the reader/writer continues to work fine also with an invalid XML document
     */
    @Test
    public void test() throws SAXException, IOException, XPathExpressionException, TransformerException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("wcs-cap.xml");
	XMLDocumentReader reader = new XMLDocumentReader(stream);
	XMLDocumentWriter writer = new XMLDocumentWriter(reader);
	writer.remove("//*:ContentMetadata/*:CoverageOfferingBrief[*:name[text() != '1000_1' ]]");

	String asString = reader.asString();
	Assert.assertTrue(asString.contains("null"));

	List<String> textContent = reader.evaluateTextContent("//*:timePosition/text()");
	Assert.assertTrue(textContent.size() == 19);
    }
}
