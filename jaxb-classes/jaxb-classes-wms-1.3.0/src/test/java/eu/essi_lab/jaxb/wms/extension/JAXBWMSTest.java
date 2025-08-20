package eu.essi_lab.jaxb.wms.extension;

import static org.junit.Assert.assertNotNull;

import java.util.ServiceLoader;

import javax.xml.bind.JAXBContextFactory;
import javax.xml.bind.JAXBException;

public class JAXBWMSTest {

    @org.junit.Test
    public void test() throws JAXBException {

	ServiceLoader<JAXBContextFactory> serviceLoader = ServiceLoader.load(JAXBContextFactory.class);

	// com.sun.xml.bind.v2.JAXBContextFactory
	JAXBContextFactory jaxbContextFactory = serviceLoader.findFirst().get();

	assertNotNull(jaxbContextFactory);

	JAXBWMS.getInstance().getMarshaller();
    }

}
