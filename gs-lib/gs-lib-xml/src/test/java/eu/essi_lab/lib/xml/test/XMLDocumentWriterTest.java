package eu.essi_lab.lib.xml.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

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
public class XMLDocumentWriterTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private XMLDocumentReader documentReader1;
    private XMLDocumentWriter documentWriter1;

    @Before
    public void init() throws SAXException, IOException {
	this.documentReader1 = new XMLDocumentReader(
		XMLDocumentReaderTest.class.getClassLoader().getResourceAsStream("xml-document-1.xml"));
	Map<String, String> namespaces = new HashMap<>();
	namespaces.put("test", "http://test.com");
	this.documentReader1.setNamespaces(namespaces);
	this.documentWriter1 = new XMLDocumentWriter(documentReader1);
    }

    @Test
    public void testRemove() throws Exception {
	Assert.assertEquals(1, documentReader1.evaluateNumber("count(//*:a)").intValue());
	documentWriter1.remove("//*:a");
	Assert.assertEquals(0, documentReader1.evaluateNumber("count(//*:a)").intValue());
    }

    @Test
    public void testModifyName() throws Exception {
	Assert.assertEquals(1, documentReader1.evaluateNumber("count(//*:a)").intValue());
	Assert.assertEquals(1, documentReader1.evaluateNumber("count(//*:b)").intValue());
	documentWriter1.rename("//*:a", "b");
	Assert.assertEquals(0, documentReader1.evaluateNumber("count(//*:a)").intValue());
	Assert.assertEquals(2, documentReader1.evaluateNumber("count(//*:b)").intValue());	
    }

    @Test
    public void testModifyName2() throws Exception {
	Assert.assertEquals(1, documentReader1.evaluateNumber("count(//*:a)").intValue());
	Assert.assertEquals(1, documentReader1.evaluateNumber("count(//*:b)").intValue());
	documentWriter1.rename("//*:a", "test:b");
	
	Assert.assertEquals(0, documentReader1.evaluateNumber("count(//*:a)").intValue());
	Assert.assertEquals(2, documentReader1.evaluateNumber("count(//*:b)").intValue());
	Assert.assertEquals(1, documentReader1.evaluateNumber("count(//b)").intValue());
	Assert.assertEquals(1, documentReader1.evaluateNumber("count(//test:b)").intValue());
    }
    
    @Test
    public void testModifyName3() throws Exception {
	Assert.assertEquals(1, documentReader1.evaluateNumber("count(//*:a)").intValue());
	Assert.assertEquals(1, documentReader1.evaluateNumber("count(//*:b)").intValue());
	documentWriter1.rename("//*:a", "b");
	System.out.println(documentReader1.asString());
	Assert.assertEquals(0, documentReader1.evaluateNumber("count(//*:a)").intValue());
	Assert.assertEquals(2, documentReader1.evaluateNumber("count(//*:b)").intValue());
	Assert.assertEquals(2, documentReader1.evaluateNumber("count(//b)").intValue());
	Assert.assertEquals(0, documentReader1.evaluateNumber("count(//test:b)").intValue());
    }
    
    @Test
    public void testAddAttributes() throws Exception {
	Assert.assertEquals(0, documentReader1.evaluateNumber("count(//*:a/@att1)").intValue());
	Assert.assertEquals(0, documentReader1.evaluateNumber("count(//*:a/@att2)").intValue());
	documentWriter1.addAttributes("//*:a", "att1","val1","att2","val2");
	System.out.println(documentReader1.asString());
	Assert.assertEquals(1, documentReader1.evaluateNumber("count(//*:a/@att1)").intValue());
	Assert.assertEquals(1, documentReader1.evaluateNumber("count(//*:a/@att2)").intValue());
	Assert.assertTrue(documentReader1.evaluateBoolean("//*:a/@att1[1]='val1'").booleanValue());
	Assert.assertTrue(documentReader1.evaluateBoolean("//*:a/@att2[1]='val2'").booleanValue());
    }
    
    @Test
    public void testSetText() throws XPathExpressionException{
	Assert.assertEquals(1, documentReader1.evaluateNumber("count(//*:a[.='A'])").intValue());
	documentWriter1.setText("//*:a", "B");
	Assert.assertEquals(0, documentReader1.evaluateNumber("count(//*:a[.='A'])").intValue());
	Assert.assertEquals(1, documentReader1.evaluateNumber("count(//*:a[.='B'])").intValue());
	documentWriter1.setText("//*[.='B']", "C");
	Assert.assertEquals(0, documentReader1.evaluateNumber("count(//*[.='B'])").intValue());
	Assert.assertEquals(3, documentReader1.evaluateNumber("count(//*[.='C'])").intValue());
	documentWriter1.addAttributes("//*:a[1]", "test","true");
	Assert.assertTrue(documentReader1.evaluateBoolean("//*:a/@test='true'"));
	Assert.assertFalse(documentReader1.evaluateBoolean("//*:a/@test='false'"));
	documentWriter1.setText("//*:a/@test","false");
	Assert.assertTrue(documentReader1.evaluateBoolean("//*:a/@test='false'"));
	Assert.assertFalse(documentReader1.evaluateBoolean("//*:a/@test='true'"));
	
    }
}
