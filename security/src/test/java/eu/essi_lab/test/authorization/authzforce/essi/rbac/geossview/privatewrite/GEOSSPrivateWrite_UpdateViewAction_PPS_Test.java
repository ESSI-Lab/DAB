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
import eu.essi_lab.messages.bond.View.ViewVisibility;
import eu.essi_lab.test.authorization.authzforce.XACMLTest;
import eu.essi_lab.test.authorization.authzforce.essi.rbac.geossview.read.GEOSSReadDiscoveryPPS_Test;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

/**
 * @author Fabrizio
 */
public class GEOSSPrivateWrite_UpdateViewAction_PPS_Test extends XACMLTest {

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
     * - view visibility private OK
     * - view owner and user id match
     * - view creator OK
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

	wrapper.setAction(Action.UPDATE_VIEW.getId());

	wrapper.setPath(GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	wrapper.setViewOwner("pippo");

	wrapper.setViewCreator("geoss");

	wrapper.setViewVisibility(ViewVisibility.PRIVATE);

	//
	//
	//

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Deny:
     * - path OK
     * - view visibility private OK
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

	wrapper.setAction(Action.UPDATE_VIEW.getId());

	wrapper.setPath(GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	wrapper.setViewOwner("pippo");
	
	wrapper.setViewCreator("geoss");

	wrapper.setViewVisibility(ViewVisibility.PRIVATE);

	//
	//
	//

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny:
     * - path OK
     * - view visibility private OK
     * - view owner and user id NOT match
     *
     * @throws IOException
     */
    @Test
    public void actionDenyTest1() throws Exception {

	//
	//
	//

	wrapper = reinitWrapper("pluto");

	//
	//
	//

	wrapper.setUserRole(GEOSSPrivateWriteRolePolicySet.ROLE);

	wrapper.setAction(Action.UPDATE_VIEW.getId());

	wrapper.setPath(GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	wrapper.setViewOwner("pippo");
	
	wrapper.setViewCreator("geoss");

	wrapper.setViewVisibility(ViewVisibility.PRIVATE);

	//
	//
	//

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny:
     * - path NOT SUPPORTED
     * - view visibility private OK
     * - view owner and user id match
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

	wrapper.setAction(Action.UPDATE_VIEW.getId());

	wrapper.setPath("csw");

	wrapper.setViewOwner("pippo");

	wrapper.setViewCreator("geoss");

	wrapper.setViewVisibility(ViewVisibility.PRIVATE);

	//
	//
	//

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny:
     * - path OK
     * - view visibility public NOT SUPPORTED
     * - view owner and user id match
     *
     * @throws IOException
     */
    @Test
    public void actionDenyTest3() throws Exception {

	//
	//
	//

	wrapper = reinitWrapper("pippo");

	//
	//
	//

	wrapper.setUserRole(GEOSSPrivateWriteRolePolicySet.ROLE);

	wrapper.setAction(Action.UPDATE_VIEW.getId());

	wrapper.setPath(GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	wrapper.setViewOwner("pippo");
	
	wrapper.setViewCreator("geoss");

	wrapper.setViewVisibility(ViewVisibility.PUBLIC);

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
