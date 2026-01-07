package eu.essi_lab.lib.xml.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.lib.xml.XMLFactories.TransformerFactoryImpl;
import eu.essi_lab.lib.xml.XMLNodeReader;

/**
 * @author Fabrizio
 */
public class TransformerFactoryTest {

    /**
     * This test uses the default transformer factory SAXON.
     * The factory is used internally by the private method XMLNodeReader.asOutputStream.
     * With this factory, a node resulting from an xPath, maintains all the attributes of
     * the parent nodes
     */
    @Test
    public void test1() throws SAXException, IOException, XPathExpressionException, TransformerException {

	InputStream envelopeStream = getClass().getClassLoader().getResourceAsStream("soap.xml");
	XMLDocumentReader envelopeDoc = new XMLDocumentReader(envelopeStream);

	// -------------------------
	//
	// Envelope
	//
	testEnvelope(envelopeDoc);

	String xPath = "//*:TimeSeriesResponse[1]";

	Node[] nodes = envelopeDoc.evaluateNodes(xPath);
	XMLNodeReader nodeReader = new XMLNodeReader(nodes[0]);

	// --------------------------------
	//
	// here the transformation is done
	//
	ByteArrayInputStream asStream = nodeReader.asStream();

	XMLDocumentReader timeSeriesDoc = new XMLDocumentReader(asStream);

	// --------------------------------
	//
	// Envelope/Body/TimeSeriesResponse
	//
	{
	    testResponse(timeSeriesDoc);
	}

    }

    /**
     * This test uses the transformer factory XALAN and an assertion error is expected.
     * In particular the assertion in the testResponse method when 4 attributes are
     * expected, but only one is found, because the parent namespaces are lost.
     * 
     * !!!!!!!!!!!!!!!!!!!!!!!!!!
     * NOW THE PROBLEM IS GONE ?!
     * !!!!!!!!!!!!!!!!!!!!!!!!!!
     */
//    @Test(expected = AssertionError.class)
    public void test2() throws SAXException, IOException, XPathExpressionException, TransformerException {

	// -----------------------------------------------------------------------
	//
	XMLFactories.DEFAULT_IMPL = TransformerFactoryImpl.XALAN;
	//
	// -----------------------------------------------------------------------

	InputStream envelopeStream = getClass().getClassLoader().getResourceAsStream("soap.xml");
	XMLDocumentReader envelopeDoc = new XMLDocumentReader(envelopeStream);

	// -------------------------
	//
	// Envelope
	//
	testEnvelope(envelopeDoc);

	String xPath = "//*:TimeSeriesResponse[1]";

	Node[] nodes = envelopeDoc.evaluateNodes(xPath);
	XMLNodeReader nodeReader = new XMLNodeReader(nodes[0]);

	// --------------------------------
	//
	// here the transformation is done
	//
	ByteArrayInputStream asStream = nodeReader.asStream();

	XMLDocumentReader timeSeriesDoc = new XMLDocumentReader(asStream);

	// --------------------------------
	//
	// Envelope/Body/TimeSeriesResponse
	//
	{
	    testResponse(timeSeriesDoc);
	}

    }

    private void testEnvelope(XMLDocumentReader envDoc) {

	NamedNodeMap attributes = envDoc.getDocument().getDocumentElement().getAttributes();

	// 3 name space declarations
	int length = attributes.getLength();
	Assert.assertEquals(3, length);

	Attr attr0 = (Attr) attributes.item(0);
	Assert.assertEquals("xmlns:soap", attr0.getName());
	Assert.assertEquals("http://schemas.xmlsoap.org/soap/envelope/", attr0.getValue());

	Attr attr1 = (Attr) attributes.item(1);
	Assert.assertEquals("xmlns:xsd", attr1.getName());
	Assert.assertEquals("http://www.w3.org/2001/XMLSchema", attr1.getValue());

	Attr attr2 = (Attr) attributes.item(2);
	Assert.assertEquals("xmlns:xsi", attr2.getName());
	Assert.assertEquals("http://www.w3.org/2001/XMLSchema-instance", attr2.getValue());
    }

    private void testResponse(XMLDocumentReader timeSeriesDoc) {

	NamedNodeMap attributes = timeSeriesDoc.getDocument().getDocumentElement().getAttributes();

	// 4 name space declarations
	// the 3 of the parent node, plus one of the resulting node (waterml11 ns)
	int length = attributes.getLength();
	Assert.assertEquals(4, length);

	Attr attr0 = (Attr) attributes.item(0);
	Assert.assertEquals("xmlns", attr0.getName());
	Assert.assertEquals("http://www.cuahsi.org/waterML/1.1/", attr0.getValue());

	Attr attr1 = (Attr) attributes.item(1);
	Assert.assertEquals("xmlns:soap", attr1.getName());
	Assert.assertEquals("http://schemas.xmlsoap.org/soap/envelope/", attr1.getValue());

	Attr attr2 = (Attr) attributes.item(2);
	Assert.assertEquals("xmlns:xsd", attr2.getName());
	Assert.assertEquals("http://www.w3.org/2001/XMLSchema", attr2.getValue());

	Attr attr3 = (Attr) attributes.item(3);
	Assert.assertEquals("xmlns:xsi", attr3.getName());
	Assert.assertEquals("http://www.w3.org/2001/XMLSchema-instance", attr3.getValue());
    }

}
