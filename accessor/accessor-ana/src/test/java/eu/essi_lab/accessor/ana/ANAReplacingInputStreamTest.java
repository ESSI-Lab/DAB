package eu.essi_lab.accessor.ana;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.xml.sax.SAXException;

import eu.essi_lab.lib.xml.XMLDocumentReader;

public class ANAReplacingInputStreamTest {

    @Test
    public void test() throws SAXException, IOException {
	InputStream stream = ANAReplacingInputStreamTest.class.getClassLoader().getResourceAsStream("ana-invalid-response.xml");
	ANAReplacingInputStream replacing = new ANAReplacingInputStream(stream);
	new XMLDocumentReader(replacing);
    }

}
