package eu.essi_lab.lib.xml.test;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.xml.XMLDocumentReader;

public class XMLDocumentReader2Test {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private XMLDocumentReader document1;

    @Before
    public void init() throws SAXException, IOException {
	this.document1 = new XMLDocumentReader(XMLDocumentReader2Test.class.getClassLoader().getResourceAsStream("test.xml"));
    }

    @Test
    public void test() {

    }

}
