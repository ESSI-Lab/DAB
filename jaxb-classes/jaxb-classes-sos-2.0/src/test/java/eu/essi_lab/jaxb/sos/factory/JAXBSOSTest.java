package eu.essi_lab.jaxb.sos.factory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBException;

import org.junit.Test;

public class JAXBSOSTest {

    @Test
    public void testName() throws Exception {
	JAXBSOS.getInstance();
	new JAXBSOSPrefixMapper();
    }

    @Test
    public void testSos() throws JAXBException {
	InputStream capStream = JAXBSOSTest.class.getClassLoader().getResourceAsStream("soscapabilities.xml");
	InputStreamReader reader = new InputStreamReader(capStream, StandardCharsets.UTF_16);
	JAXBSOS.getInstance().unmarshal(reader);
    }
    
    
    
    
    
    @Test
    public void testSos2() throws JAXBException {
	InputStream featuresStream = JAXBSOSTest.class.getClassLoader().getResourceAsStream("features.xml");
	InputStreamReader reader = new InputStreamReader(featuresStream, StandardCharsets.UTF_16);
	Object features = JAXBSOS.getInstance().unmarshal(reader);
	System.out.println();
    }

}
