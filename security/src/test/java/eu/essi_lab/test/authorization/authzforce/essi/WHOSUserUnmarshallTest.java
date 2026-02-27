/**
 *
 */
package eu.essi_lab.test.authorization.authzforce.essi;

import eu.essi_lab.authorization.*;
import eu.essi_lab.authorization.psloader.*;
import eu.essi_lab.authorization.xacml.*;
import eu.essi_lab.test.authorization.authzforce.essi.rbac.*;
import eu.essi_lab.test.authorization.authzforce.jaxb.*;
import jakarta.xml.bind.*;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.*;
import org.junit.*;

import java.io.*;
import java.util.*;

/**
 * @author Fabrizio
 */
public class WHOSUserUnmarshallTest extends WHOS_PPS_Test {

    protected PolicySet getWHOS_RPS() throws Exception {

	JAXBContext jaxbContext = JAXBContext.newInstance(oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory.class);

	InputStream stream = PolicySetUnmarshallTest.class.getClassLoader().getResourceAsStream("essi-rbac-test/whos-rps.xml");

	return (PolicySet) jaxbContext.createUnmarshaller().unmarshal(stream);
    }

    protected PolicySet getWHOS_PPS() throws Exception {

	JAXBContext jaxbContext = JAXBContext.newInstance(oasis.names.tc.xacml._3_0.core.schema.wd_17.ObjectFactory.class);

	InputStream stream = PolicySetUnmarshallTest.class.getClassLoader().getResourceAsStream("essi-rbac-test/whos-pps.xml");

	return (PolicySet) jaxbContext.createUnmarshaller().unmarshal(stream);
    }

    @Before
    public void init() throws Exception {

	PolicySet whosRPS = getWHOS_RPS();
	PolicySet whosPPS = getWHOS_PPS();

	PolicySetLoader loader = new PolicySetLoader() {

	    @Override
	    public List<PolicySetWrapper> loadRolePolicySets() {

		return Arrays.asList(new PolicySetWrapper() {

		    @Override
		    public String getRole() {

			return "whos";
		    }

		    @Override
		    public PolicySet getPolicySet() {

			return whosRPS;
		    }
		});
	    }

	    @Override
	    public List<PolicySetWrapper> loadPermissionPolicySets() {

		return Arrays.asList(new PolicySetWrapper() {

		    @Override
		    public String getRole() {

			return "whos";
		    }

		    @Override
		    public PolicySet getPolicySet() {

			return whosPPS;
		    }
		});
	    }

	    @Override
	    public void setPermissionPolicySet(PolicySetWrapper pps) {
	    }
	};

	PdpEngineBuilder builder = new PdpEngineBuilder();

	builder.addPolicies(loader);

	wrapper = new PdpEngineWrapper(builder.build());

	wrapper.reset();

	wrapper.setUserRole(getRole());

	//
	// max records and offset are ignored by the rule, by they are mandatory in
	// the request (put in order to avoid request compilation error)
	//

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);
    }
}
