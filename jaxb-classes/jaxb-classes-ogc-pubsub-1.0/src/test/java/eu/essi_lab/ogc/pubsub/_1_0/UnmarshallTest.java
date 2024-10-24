package eu.essi_lab.ogc.pubsub._1_0;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import eu.essi_lab.ogc.pubsub._1_0.ObjectFactory;
import eu.essi_lab.ogc.pubsub._1_0.SubscribeType;
import junit.framework.TestCase;

public class UnmarshallTest extends TestCase {

    private static final String SUBSCRIBE_REQ = "subscribe.xml";

    protected InputStream getResourceAsStream(String resName) {
	return UnmarshallTest.class.getClassLoader().getResourceAsStream(resName);
    }

    @Test
    public void test() {
	try (BufferedReader reader = new BufferedReader(new InputStreamReader(getResourceAsStream(SUBSCRIBE_REQ)))) {
	    JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
	    Unmarshaller unmarshaller = jc.createUnmarshaller();
	    JAXBElement<?> elem = (JAXBElement<?>) unmarshaller.unmarshal(reader);
	    assertTrue(elem.getValue() instanceof SubscribeType);

	    // Marshaller marshaller = jc.createMarshaller();
	    // marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	    // marshaller.marshal(subscribeReq, System.out);

	} catch (Exception e1) {
	    e1.printStackTrace();
	}

    }
}
