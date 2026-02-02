import eu.essi_lab.jaxb.wms.extension.JAXBWMS;

import java.io.File;
import java.util.ServiceLoader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBContextFactory;
import javax.xml.bind.JAXBException;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 * @author Mattia Santoro
 */
public class JAXBTest {

    @Test
    public void test() throws JAXBException {

//	ServiceLoader<JAXBContextFactory> serviceLoader = ServiceLoader.load(JAXBContextFactory.class);
//
//	// com.sun.xml.bind.v2.JAXBContextFactory
//	JAXBContextFactory jaxbContextFactory = serviceLoader.findFirst().get();
//
//	assertNotNull(jaxbContextFactory);

	JAXBWMS.getInstance().getMarshaller();
    }



}
