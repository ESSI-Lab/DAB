package eu.essi_lab.lib.xml.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eu.essi_lab.lib.xml.XMLDocumentReader;

public class XMLDocumentReaderTest {

    private static final double TOL = 0.00000000001;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private XMLDocumentReader document1;
    private XMLDocumentReader document2;
    private XMLDocumentReader document3;

    @Before
    public void init() throws SAXException, IOException {
	InputStream resourceAsStream = XMLDocumentReaderTest.class.getClassLoader().getResourceAsStream("xml-document-1.xml");
	InputStream resourceAsStream2 = XMLDocumentReaderTest.class.getClassLoader().getResourceAsStream("xml-document-2.xml");
	InputStream resourceAsStream3 = XMLDocumentReaderTest.class.getClassLoader().getResourceAsStream("xml-document-3.xml");
	this.document1 = new XMLDocumentReader(resourceAsStream);
	this.document2 = new XMLDocumentReader(resourceAsStream2);
	this.document3 = new XMLDocumentReader(resourceAsStream3);
	resourceAsStream.close();
	resourceAsStream2.close();
	resourceAsStream3.close();
    }

    @Test
    public void testInitNull() throws Exception {
	expectedException.expect(IllegalArgumentException.class);
	ByteArrayInputStream bis = null;
	new XMLDocumentReader(bis);
    }

    @Test
    public void testIOException() throws Exception {
	expectedException.expect(IOException.class);
	InputStream problematicStream = new InputStream() {
	    @Override
	    public int read() throws IOException {
		// Simulates an I/O error.
		throw new IOException("Fake read error!");
	    }
	};
	new XMLDocumentReader(problematicStream);
    }

    @Test
    public void testParseException() throws Exception {
	expectedException.expect(SAXParseException.class);
	new XMLDocumentReader(XMLDocumentReaderTest.class.getClassLoader().getResourceAsStream("xml-document-1.bad"));
    }

    @Test
    public void testParse() throws Exception {
	new XMLDocumentReader(XMLDocumentReaderTest.class.getClassLoader().getResourceAsStream("xml-document-1.xml"));
    }

    @Test
    public void testXPathNullExpression1() throws XPathExpressionException {
	expectedException.expect(NullPointerException.class);
	document1.evaluateString(null);
    }

    @Test
    public void testXPathBadExpression() throws XPathExpressionException {
	expectedException.expect(XPathExpressionException.class);
	document1.evaluateString("8/52//*");
    }

    @Test
    public void testUnqualifiedXPath() throws XPathExpressionException {
	// the first document is not qualified, so it should pass
	testUnqualifiedXPath(document1, true);
	// the second document is qualified, so it should fail
	testUnqualifiedXPath(document2, false);
	// the third document is qualified, so it should fail
	testUnqualifiedXPath(document3, false);
    }

    public void testUnqualifiedXPath(XMLDocumentReader document, boolean pass) throws XPathExpressionException {
	Assert.assertEquals(pass ? "A" : "", document.evaluateString("//a[1]"));
	Assert.assertEquals(pass ? "A" : "", document.evaluateString("/test/a"));
	Assert.assertEquals(pass ? "B" : "", document.evaluateString("//b[1]"));
	Assert.assertEquals(pass ? "B" : "", document.evaluateString("/test/b"));
    }

    @Test
    public void testQualifiedXPath() throws XPathExpressionException {
	// the first document is not qualified, so it should fail
	testQualifiedXPath(document1, false);
	// the second document is qualified, so it should pass
	testQualifiedXPath(document2, true);
	// the third document is qualified, so it should pass
	testQualifiedXPath(document3, true);
    }

    String prefix = "t";
    String uri = "http://essi-lab.eu/test";

    public void testQualifiedXPath(XMLDocumentReader document, boolean pass) throws XPathExpressionException {
	document.setNamespaceContext(new NamespaceContext() {

	    @Override
	    public Iterator<String> getPrefixes(String namespaceURI) {
		List<String> prefixes = new ArrayList<>();
		if (namespaceURI.equals(uri)) {
		    prefixes.add(prefix);
		}
		return prefixes.iterator();
	    }

	    @Override
	    public String getPrefix(String namespaceURI) {
		if (namespaceURI.equals(uri)) {
		    return prefix;
		}
		return null;
	    }

	    @Override
	    public String getNamespaceURI(String prefix) {
		if (prefix.equals(prefix)) {
		    return uri;
		}
		return null;
	    }
	});
	Assert.assertEquals(pass ? "A" : "", document.evaluateString("//t:a[1]"));
	Assert.assertEquals(pass ? "A" : "", document.evaluateString("/t:test/t:a"));
	Assert.assertEquals(pass ? "B" : "", document.evaluateString("//t:b[1]"));
	Assert.assertEquals(pass ? "B" : "", document.evaluateString("/t:test/t:b"));
    }

    @Test
    public void testXPathStringStar() throws XPathExpressionException {
	testXPathStringStar(document1);
	testXPathStringStar(document2);
	testXPathStringStar(document3);
    }

    public void testXPathStringStar(XMLDocumentReader document) throws XPathExpressionException {
	Assert.assertEquals("A", document.evaluateString("//*:a[1]"));
	Assert.assertEquals("A", document.evaluateString("/*:test/*:a"));
	Assert.assertEquals("B", document.evaluateString("//*:b[1]"));
	Assert.assertEquals("B", document.evaluateString("/*:test/*:b"));
    }

    @Test
    public void testXPathNode() throws XPathExpressionException {
	Node result = document1.evaluateNode("//*:a[1]");
	Assert.assertNotNull(result);
    }

    @Test
    public void testXPathNode2() throws XPathExpressionException {
	Node result = document1.evaluateNode("//*:z[1]");
	Assert.assertNull(result);
    }

    @Test
    public void testXPathNodes() throws XPathExpressionException {
	Node[] result = document1.evaluateNodes("//*");
	Assert.assertEquals(4, result.length);

    }

    @Test
    public void testXPathNumber() throws XPathExpressionException {
	Number result = document1.evaluateNumber("count(//*)");
	Assert.assertEquals(4, result.doubleValue(), TOL);
    }

    @Test
    public void testXPathBoolean() throws XPathExpressionException {
	Boolean result = document1.evaluateBoolean("/*:test");
	Assert.assertEquals(true, result);
    }

    @Test
    public void testXPathString() throws XPathExpressionException {
	String result = document1.evaluateString("/*:test/*:a");
	Assert.assertEquals("A", result);
    }

    private Node getANode() throws XPathExpressionException {
	return document1.evaluateNode("//*:a[1]");
    }

    @Test
    public void testXPathNodeTarget() throws XPathExpressionException {
	Node result = getANode();
	Node childResult = document1.evaluateNode(result, "../.");
	Assert.assertNotNull(childResult);

    }

    @Test
    public void testXPathNodesTarget() throws XPathExpressionException {
	Node result = getANode();
	Node[] childResult = document1.evaluateNodes(result, "../*");
	Assert.assertEquals(3, childResult.length);

    }

    @Test
    public void testXPathNumberTarget() throws XPathExpressionException {
	Node result = getANode();
	Number childResult = document1.evaluateNumber(result, "count(../*)");
	Assert.assertEquals(3, childResult.doubleValue(), TOL);
    }

    @Test
    public void testXPathBooleanTarget() throws XPathExpressionException {
	Node result = getANode();
	Boolean childResult = document1.evaluateBoolean(result, "local-name(.)='a'");
	Assert.assertEquals(true, childResult);
    }

    @Test
    public void testXPathStringTarget() throws XPathExpressionException {
	Node result = getANode();
	String childResult = document1.evaluateString(result, "local-name(.)");
	Assert.assertEquals("a", childResult);
    }

    @Test
    public void testConstructor() throws XPathExpressionException, SAXException, IOException {
	String doc = "<doc><a/><a/><a/></doc>";
	XMLDocumentReader xml = new XMLDocumentReader(doc);
	Assert.assertEquals(3, xml.evaluateNumber("count(//*:a)").intValue());
	XMLDocumentReader xml2 = new XMLDocumentReader(xml.getDocument());
	Assert.assertEquals(3, xml2.evaluateNumber("count(//*:a)").intValue());
    }

}
