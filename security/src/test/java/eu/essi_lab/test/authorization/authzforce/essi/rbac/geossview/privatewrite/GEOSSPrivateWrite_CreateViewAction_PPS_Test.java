package eu.essi_lab.test.authorization.authzforce.essi.rbac.geossview.privatewrite;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.authorization.PolicySetWrapper.Action;
import eu.essi_lab.authorization.pps.GEOSSWritePermissionPolicySet;
import eu.essi_lab.authorization.rps.GEOSSPrivateWriteRolePolicySet;
import eu.essi_lab.authorization.rps.GEOSSReadRolePolicySet;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.View.ViewVisibility;
import eu.essi_lab.test.authorization.authzforce.XACMLTest;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

/**
 * @author Fabrizio
 */
public class GEOSSPrivateWrite_CreateViewAction_PPS_Test extends XACMLTest {

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
     * - view creator OK
     * - view visibility private OK
     *
     * @throws IOException
     */
    @Test
    public void actionPermitTest1() throws IOException {

	wrapper.setAction(Action.CREATE_VIEW.getId());

	wrapper.setPath(GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	wrapper.setViewCreator("geoss");

	wrapper.setViewVisibility(ViewVisibility.PRIVATE);

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Deny:
     * - path OK
     * - view creator OK
     * - view visibility private OK
     * - using "geoss-read" role!
     *
     * @throws IOException
     */
    @Test
    public void actionDenyTest1_1() throws IOException {

	wrapper.reset();

	wrapper.setUserRole(GEOSSReadRolePolicySet.ROLE);

	wrapper.setAction(Action.CREATE_VIEW.getId());

	wrapper.setPath(GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	wrapper.setViewCreator("geoss");

	wrapper.setViewVisibility(ViewVisibility.PRIVATE);

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny:
     * - path NOT SUPPORTED
     * - view creator OK
     * - view visibility private OK
     *
     * @throws IOException
     */
    @Test
    public void actionDenyTest1() throws IOException {

	wrapper.setAction(Action.CREATE_VIEW.getId());

	wrapper.setPath("xx");

	wrapper.setViewCreator("geoss");

	wrapper.setViewVisibility(ViewVisibility.PRIVATE);

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny:
     * - path OK
     * - view creator NOT SUPPORTED
     * - view visibility private OK
     *
     * @throws IOException
     */
    @Test
    public void actionDenyTest2() throws IOException {

	wrapper.setAction(Action.CREATE_VIEW.getId());

	wrapper.setPath(GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	wrapper.setViewCreator("xx");

	wrapper.setViewVisibility(ViewVisibility.PRIVATE);

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny:
     * - path OK
     * - view creator OK
     * - view visibility public, must be private
     *
     * @throws IOException
     */
    @Test
    public void actionDenyTest3() throws IOException {

	wrapper.setAction(Action.CREATE_VIEW.getId());

	wrapper.setPath(GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	wrapper.setViewCreator("geoss");

	wrapper.setViewVisibility(ViewVisibility.PUBLIC);

	evaluate(DecisionType.DENY);
    }

}
