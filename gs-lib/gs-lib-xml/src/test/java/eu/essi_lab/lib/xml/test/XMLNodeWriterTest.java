package eu.essi_lab.lib.xml.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.w3c.dom.Node;

import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.XMLNodeReader;
import eu.essi_lab.lib.xml.XMLNodeWriter;

public class XMLNodeWriterTest {

    @Test
    public void testName() throws Exception {
	XMLDocumentReader dr = new XMLDocumentReader(XMLNodeWriterTest.class.getClassLoader().getResourceAsStream("xml-document-4.xml"));
	Node node = dr.evaluateNode("//*:aaa");
	XMLNodeReader nr = new XMLNodeReader(node);
	assertEquals(3, dr.evaluateNodes("/*:abc/*:aaa/*").length);
	XMLNodeWriter nw = new XMLNodeWriter(nr);
	nw.remove("*:a[1]");
	assertEquals(2, dr.evaluateNodes("/*:abc/*:aaa/*").length);

    }

}
