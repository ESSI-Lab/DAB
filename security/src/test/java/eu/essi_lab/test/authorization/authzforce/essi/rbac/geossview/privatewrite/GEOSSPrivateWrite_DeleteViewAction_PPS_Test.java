package eu.essi_lab.test.authorization.authzforce.essi.rbac.geossview.privatewrite;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.authorization.PolicySetWrapper.Action;
import eu.essi_lab.authorization.pps.AbstractGEOSSViewPermissionPolicySet;
import eu.essi_lab.authorization.pps.GEOSSPrivateWritePermissionPolicySet;
import eu.essi_lab.authorization.pps.GEOSSWritePermissionPolicySet;
import eu.essi_lab.authorization.rps.GEOSSPrivateWriteRolePolicySet;
import eu.essi_lab.authorization.rps.GEOSSReadRolePolicySet;
import eu.essi_lab.authorization.xacml.PdpEngineWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.test.authorization.authzforce.XACMLTest;
import eu.essi_lab.test.authorization.authzforce.essi.rbac.geossview.read.GEOSSReadDiscoveryPPS_Test;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

/**
 * @author Fabrizio
 */
public class GEOSSPrivateWrite_DeleteViewAction_PPS_Test extends XACMLTest {

    @Before
    public void init() throws Exception {

	super.init();

	wrapper.reset();

	GSLoggerFactory.getLogger(getClass()).info("Testing role: " + GEOSSPrivateWriteRolePolicySet.ROLE);

	wrapper.setUserRole(GEOSSPrivateWriteRolePolicySet.ROLE);
    }

    /**
     * Permit:
     * - path OK
     * - view owner and user id match
     *
     * @throws IOException
     */
    @Test
    public void actionPermitTest1() throws Exception {

	//
	//
	//

	wrapper = reinitWrapper("pippo");

	//
	//
	//

	wrapper.setUserRole(GEOSSPrivateWriteRolePolicySet.ROLE);

	wrapper.setAction(Action.DELETE_VIEW.getId());

	wrapper.setPath(GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	wrapper.setViewOwner("pippo");

	//
	//
	//

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Deny:
     * - path OK
     * - view owner and user id match
     * - using "geoss-read" role!
     *
     * @throws IOException
     */
    @Test
    public void actionDenyTest1_1() throws Exception {

	//
	//
	//

	wrapper = reinitWrapper("pippo");

	//
	//
	//

	wrapper.setUserRole(GEOSSReadRolePolicySet.ROLE);

	wrapper.setAction(Action.DELETE_VIEW.getId());

	wrapper.setPath(GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	wrapper.setViewOwner("pippo");

	//
	//
	//

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny:
     * - path NOT OK
     * - view owner and user id match
     *
     * @throws IOException
     */
    @Test
    public void actionDenyTest1() throws Exception {

	//
	//
	//

	wrapper = reinitWrapper("pippo");

	//
	//
	//

	wrapper.setUserRole(GEOSSPrivateWriteRolePolicySet.ROLE);

	wrapper.setAction(Action.DELETE_VIEW.getId());

	wrapper.setPath("csw");

	wrapper.setViewOwner("pippo");

	//
	//
	//

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny:
     * - path NOT OK
     * - view owner and user id NOT MATCH
     *
     * @throws IOException
     */
    @Test
    public void actionDenyTest2() throws Exception {

	//
	//
	//

	wrapper = reinitWrapper("pippo");

	//
	//
	//

	wrapper.setUserRole(GEOSSPrivateWriteRolePolicySet.ROLE);

	wrapper.setAction(Action.DELETE_VIEW.getId());

	wrapper.setPath(GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	wrapper.setViewOwner("pluto");

	//
	//
	//

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny:
     * - path NOT OK
     * - user id not set
     *
     * @throws IOException
     */
    @Test
    public void actionDenyTest3() throws Exception {

	wrapper.setAction(Action.DELETE_VIEW.getId());

	wrapper.setPath(GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	wrapper.setViewOwner("pluto");

	//
	//
	//

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny:
     * - path NOT OK
     * - view owner not set
     *
     * @throws IOException
     */
    @Test
    public void actionDenyTest4() throws Exception {

	//
	//
	//

	wrapper = reinitWrapper("pippo");

	//
	//
	//

	wrapper.setUserRole(GEOSSPrivateWriteRolePolicySet.ROLE);

	wrapper.setAction(Action.DELETE_VIEW.getId());

	wrapper.setPath(GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	//
	//
	//

	evaluate(DecisionType.DENY);
    }

    /**
     * @return
     * @throws Exception
     */
    protected PdpEngineWrapper reinitWrapper(String userIdentifier) throws Exception {

	AbstractGEOSSViewPermissionPolicySet pps = new GEOSSPrivateWritePermissionPolicySet();

	return GEOSSReadDiscoveryPPS_Test.reinitWrapper(userIdentifier, pps, getClass());
    }

}
