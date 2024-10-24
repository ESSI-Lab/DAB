package eu.essi_lab.accessor.sos;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import eu.essi_lab.jaxb.sos.factory.JAXBSOS;

public class TestCapabilities {

    @Test
    public void test() throws JAXBException {
	InputStream stream = TestCapabilities.class.getClassLoader().getResourceAsStream("test-cap.xml");
	Object obj = JAXBSOS.getInstance().unmarshal(stream);
	System.out.println();
	JAXBSOS.getInstance().marshal(obj, System.out);
    }

}
