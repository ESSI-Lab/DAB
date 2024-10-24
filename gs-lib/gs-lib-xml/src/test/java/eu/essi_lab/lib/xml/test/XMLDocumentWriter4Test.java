package eu.essi_lab.lib.xml.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
public class XMLDocumentWriter4Test {
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
    public void testAttributeRenaming() throws Exception {
	System.out.println(documentReader1.asString());

	assertTrue(documentReader1.evaluateString("/t:test/t:b/@t:att").equals("pippo"));

	assertTrue(documentReader1.evaluateString("/t:test/t:b/@t:att2").isEmpty());

	documentWriter1.rename("/t:test/t:b/@t:att", "t:att2");
	System.out.println(documentReader1.asString());

	assertTrue(documentReader1.evaluateString("/t:test/t:b/@t:att").isEmpty());

	assertTrue(documentReader1.evaluateString("/t:test/t:b/@t:att2").equals("pippo"));

    }

}
