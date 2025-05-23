package eu.essi_lab.jaxb.wms.extension;

import javax.xml.bind.JAXBException;

import org.junit.Test;

public class JAXBWMSTest {

    @Test
    public void test() throws JAXBException {
	JAXBWMS.getInstance().getMarshaller();
    }

}
