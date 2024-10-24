/**
 * 
 */
package eu.essi_lab.test.authorization.authzforce.essi;

import eu.essi_lab.authorization.pps.WHOSPermissionPolicySet;
import eu.essi_lab.authorization.rps.WHOSRolePolicySet;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.PolicySet;

/**
 * @author Fabrizio
 */
public class WHOSUserJavaTest extends WHOSUserUnmarshallTest {

    protected PolicySet getWHOS_RPS() throws Exception {

	return new WHOSRolePolicySet().getPolicySet();
    }

    protected PolicySet getWHOS_PPS() throws Exception {

	return new WHOSPermissionPolicySet().getPolicySet();
    }
}
