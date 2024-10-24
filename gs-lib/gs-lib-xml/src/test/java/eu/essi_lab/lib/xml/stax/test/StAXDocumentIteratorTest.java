package eu.essi_lab.lib.xml.stax.test;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.Test;

import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.lib.xml.stax.StAXDocumentIterator;

public class StAXDocumentIteratorTest {

    @Test
    public void test() throws Exception {
	InputStream stream = StAXDocumentIteratorTest.class.getClassLoader().getResourceAsStream("stax-test.xml");
	StAXDocumentIterator cutter = new StAXDocumentIterator(stream, "siteInfo");
	int i = 0;
	while (cutter.hasNext()) {
	    XMLDocumentReader reader = cutter.next();
	    System.out.println("Piece #" + ++i);
	    System.out.println(reader.asString());
	    if (i == 1) {
		assertEquals("Aktubinsk", reader.evaluateString("*:siteInfo/*:siteName"));
	    }
	    if (i == 13) {
		assertEquals("Tyumen-Aryk", reader.evaluateString("*:siteInfo/*:siteName"));
	    }
	    if (i == 14) {
		assertEquals("Petropavlovsk", reader.evaluateString("*:siteInfo/*:siteName"));
	    }
	    System.out.println();
	}
	System.out.println("Cut into " + i + " pieces.");
	assertEquals(14, i);
    }

}
