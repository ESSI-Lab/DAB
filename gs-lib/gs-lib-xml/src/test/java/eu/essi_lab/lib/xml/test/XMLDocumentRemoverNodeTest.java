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
public class XMLDocumentRemoverNodeTest {


    @Test
    public void test() throws SAXException, IOException, XPathExpressionException, TransformerException {

	InputStream stream = getClass().getClassLoader().getResourceAsStream("wcs-cap.xml");
	XMLDocumentReader reader = new XMLDocumentReader(stream);
	XMLDocumentWriter writer = new XMLDocumentWriter(reader);
	writer.remove("//*:ContentMetadata/*:CoverageOfferingBrief[*:name[text() != '1000_1' ]]");

	String asString = reader.asString();
	Assert.assertTrue(asString.contains("1000_1"));
	Assert.assertFalse(asString.contains("1000_2"));

	List<String> textContent = reader.evaluateTextContent("//*:timePosition/text()");
	Assert.assertTrue(textContent.size() == 2);
    }
}
