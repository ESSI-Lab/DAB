package eu.essi_lab.accessor.wms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import eu.essi_lab.jaxb.wms._1_1_1.WMTMSCapabilities;

public class WMS111UnmarhsalTest {

    @Test
    public void test() throws Exception {
	JAXBContext context = JAXBContext.newInstance(WMTMSCapabilities.class);
	Unmarshaller u = context.createUnmarshaller();

	InputStream stream = WMS111UnmarhsalTest.class.getClassLoader().getResourceAsStream("test-capabilities-111.xml");

	SAXParserFactory spf = SAXParserFactory.newInstance();
	spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
	spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
	spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
	SAXParser parser = spf.newSAXParser();
	XMLReader reader = parser.getXMLReader();
	InputSource inputSource = new InputSource(stream);
	SAXSource source = new SAXSource(reader, inputSource);

	Object ret = u.unmarshal(source);
	if (ret instanceof WMTMSCapabilities) {
	    WMTMSCapabilities wmts = (WMTMSCapabilities) ret;
	    String title = wmts.getService().getTitle();
	    assertEquals("Geoscience Data - Données Géoscientifiques", title);
	} else {
	    fail();
	}
    }

}
