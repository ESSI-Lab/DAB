package eu.essi_lab.accessor.sos;

import java.io.InputStream;

import org.junit.Test;

import eu.essi_lab.jaxb.sos.factory.JAXBSOS;

public class SOSResponseTest {

    @Test
    public void test() throws Exception {
	InputStream stream = SOSResponseTest.class.getClassLoader().getResourceAsStream("sos-response.xml");
	Object obj = JAXBSOS.getInstance().unmarshal(stream);
	System.out.println();
    }

}
