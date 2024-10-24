/**
 * 
 */
package eu.essi_lab.test.authorization.authzforce.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import eu.essi_lab.authorization.rps.WHOSRolePolicySet;
import eu.essi_lab.test.authorization.authzforce.XACMLTest;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

/**
 * @author Fabrizio
 */
public class PolicySetUnmarshallTest extends XACMLTest {

    public static void main(String[] args) throws JAXBException {

	PolicySet policySet = new WHOSRolePolicySet().getPolicySet();

	JAXBContext jaxbContext = JAXBContext.newInstance(oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory.class);

	Marshaller marshaller = jaxbContext.createMarshaller();
	marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

	marshaller.marshal(policySet, System.out);
    }
}
