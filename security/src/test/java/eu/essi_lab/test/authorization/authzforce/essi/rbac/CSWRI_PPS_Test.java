package eu.essi_lab.test.authorization.authzforce.essi.rbac;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.authorization.PolicySetWrapper.Action;
import eu.essi_lab.authorization.pps.AbstractPermissionPolicySet;
import eu.essi_lab.authorization.pps.CSW_RIPermissionPolicySet;
import eu.essi_lab.authorization.rps.CSW_RIRolePolicySet;
import eu.essi_lab.test.authorization.authzforce.XACMLTest;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

/**
 * @author Fabrizio
 */
public class CSWRI_PPS_Test extends XACMLTest {

    @Before
    public void init() throws Exception {

	super.init();

	wrapper.reset();

	wrapper.setUserRole(CSW_RIRolePolicySet.ROLE);

	//
	// max records and offset are ignored by the rule, by they are mandatory in
	// the request (put in order to avoid request compilation error)
	//

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);
    }

    /**
     * Deny: missing view id
     *
     * @throws IOException
     */
    @Test
    public void cswriDiscoveryDenyTest1() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath(CSW_RIPermissionPolicySet.SUPPORTED_PATH);

	wrapper.setViewIdentifier(AbstractPermissionPolicySet.VIEW_ID_MISSING_VALUE);

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny: wrong view id
     *
     * @throws IOException
     */
    @Test
    public void cswriDiscoveryDenyTest2() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath(CSW_RIPermissionPolicySet.SUPPORTED_PATH);

	wrapper.setViewIdentifier("id");

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny: wrong path
     *
     * @throws IOException
     */
    @Test
    public void cswriDiscoveryDenyTest3() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("path");

	wrapper.setViewIdentifier(CSW_RIPermissionPolicySet.SUPPORTED_VIEW_ID);

	evaluate(DecisionType.DENY);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void cswriDiscoveryPermitTest() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath(CSW_RIPermissionPolicySet.SUPPORTED_PATH);

	wrapper.setViewIdentifier(CSW_RIPermissionPolicySet.SUPPORTED_VIEW_ID);

	evaluate(DecisionType.PERMIT);
    }

    //
    //
    //

    /**
     * @throws IOException
     */
    @Test
    public void cswriAccessDenyTest() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath(CSW_RIPermissionPolicySet.SUPPORTED_PATH);

	wrapper.setViewIdentifier(CSW_RIPermissionPolicySet.SUPPORTED_VIEW_ID);

	evaluate(DecisionType.DENY);
    }
    //
    //
    //

    /**
     * Deny
     *
     * @throws IOException
     */
    @Test
    public void cswriDiscoveryPermitTest1_DenyForGWP() throws IOException {

	wrapper.reset();

	wrapper.setUserRole("gwp");

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);
	
	wrapper.setOriginHeader("https://www.geoportal.org");	

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath(CSW_RIPermissionPolicySet.SUPPORTED_PATH);
	
	//
	//
	//
	
	wrapper.setViewCreator("essilab");

	wrapper.setViewIdentifier(CSW_RIPermissionPolicySet.SUPPORTED_VIEW_ID);

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny
     *
     * @throws IOException
     */
    @Test
    public void cswriDiscoveryPermitTest1_DenyForWHOS() throws IOException {

	wrapper.reset();

	wrapper.setUserRole("whos");

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath(CSW_RIPermissionPolicySet.SUPPORTED_PATH);
	
	//
	//
	//

	wrapper.setViewCreator("essilab");

	wrapper.setViewIdentifier(CSW_RIPermissionPolicySet.SUPPORTED_VIEW_ID);
	
	evaluate(DecisionType.DENY);
    }
}
