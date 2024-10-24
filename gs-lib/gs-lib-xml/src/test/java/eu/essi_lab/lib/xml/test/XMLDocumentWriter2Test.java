package eu.essi_lab.lib.xml.test;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLDocumentWriter;

/**
 * Tests the {@link XMLDocumentWriter} methods
 * 
 * @author boldrini
 */
public class XMLDocumentWriter2Test {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private XMLDocumentReader documentReader1;
    private XMLDocumentReader documentReader2;
    private XMLDocumentWriter documentWriter1;

    @Before
    public void init() throws SAXException, IOException {
	this.documentReader1 = new XMLDocumentReader(
		XMLDocumentReaderTest.class.getClassLoader().getResourceAsStream("xml-document-1.xml"));
	this.documentReader2 = new XMLDocumentReader(
		XMLDocumentReaderTest.class.getClassLoader().getResourceAsStream("xml-document-4.xml"));
	this.documentWriter1 = new XMLDocumentWriter(documentReader1);
    }

    @Test
    public void testAddNodes() throws Exception {
	Assert.assertEquals(1, documentReader1.evaluateNumber("count(//*:a)").intValue());
	Assert.assertEquals(0, documentReader1.evaluateNumber("count(//*:aaa)").intValue());
	Node node = documentReader2.evaluateNode("//*:aaa");
	documentWriter1.remove("//*:test/*:a[1]/text()");
	documentWriter1.addNode("//*:a", node);
	System.out.println(documentReader1.asString());
	Assert.assertEquals(4, documentReader1.evaluateNumber("count(//*:a)").intValue());
	Assert.assertEquals(1, documentReader1.evaluateNumber("count(//*:aaa)").intValue());
	documentWriter1.addNode("//*:a", node);
	Assert.assertEquals(16, documentReader1.evaluateNumber("count(//*:a)").intValue());
	Assert.assertEquals(5, documentReader1.evaluateNumber("count(//*:aaa)").intValue());
    }

}
