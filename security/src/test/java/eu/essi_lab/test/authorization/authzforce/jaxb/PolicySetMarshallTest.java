/**
 * 
 */
package eu.essi_lab.test.authorization.authzforce.jaxb;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import eu.essi_lab.test.authorization.authzforce.XACMLTest;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

/**
 * @author Fabrizio
 */
public class PolicySetMarshallTest extends XACMLTest {

    public static void main(String[] args) throws JAXBException {

	InputStream stream = PolicySetMarshallTest.class.getClassLoader().getResourceAsStream("essi-rbac-test/default-rps.xml");

	JAXBContext jaxbContext = JAXBContext.newInstance(oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory.class);

	PolicySet ps = (PolicySet) jaxbContext.createUnmarshaller().unmarshal(stream);

	System.out.println(ps);
    }
}
