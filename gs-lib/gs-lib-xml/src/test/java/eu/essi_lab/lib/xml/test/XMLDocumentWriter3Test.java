package eu.essi_lab.lib.xml.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;

/**
 * Tests the {@link XMLDocumentWriter} methods
 * 
 * @author boldrini
 */
public class XMLDocumentWriter3Test {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private XMLDocumentReader documentReader1;
    private XMLDocumentWriter documentWriter1;

    @Before
    public void init() throws SAXException, IOException {
	this.documentReader1 = new XMLDocumentReader(
		XMLDocumentReaderTest.class.getClassLoader().getResourceAsStream("xml-document-3.xml"));
	Map<String, String> namespaces = new HashMap<>();
	namespaces.put("t", "http://essi-lab.eu/test");
	documentReader1.setNamespaces(namespaces);
	this.documentWriter1 = new XMLDocumentWriter(documentReader1);
    }

    @Test
    public void testPrefixRemover() throws Exception {

	Assert.assertEquals(1, documentReader1.evaluateNumber("count(//t:test)").intValue());
	Assert.assertEquals(1, documentReader1.evaluateNumber("count(//t:a)").intValue());

	Assert.assertTrue(documentReader1.asString().contains("<t:a>A</t:a>"));
	Assert.assertTrue(documentReader1.asString().contains("<t:b t:att=\"pippo\">B</t:b>"));

	documentWriter1.removePrefixes();

	Assert.assertEquals(1, documentReader1.evaluateNumber("count(//t:test)").intValue());
	Assert.assertEquals(1, documentReader1.evaluateNumber("count(//t:a)").intValue());

	System.out.println(documentReader1.asString());
	Assert.assertTrue(documentReader1.asString().contains("<a>A</a>"));
	// TODO: extend to attribute renaming
	// Assert.assertTrue(documentReader1.asString().contains("<b att=\"pippo\">B</b>"));

    }

}
