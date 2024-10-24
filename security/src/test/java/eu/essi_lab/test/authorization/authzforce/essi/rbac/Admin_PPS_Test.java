package eu.essi_lab.test.authorization.authzforce.essi.rbac;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.authorization.BasicRole;
import eu.essi_lab.authorization.PolicySetWrapper.Action;
import eu.essi_lab.test.authorization.authzforce.XACMLTest;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

/**
 * @author Fabrizio
 */
public class Admin_PPS_Test extends XACMLTest {

    @Before
    public void init() throws Exception {

	super.init();

	wrapper.reset();

	wrapper.setUserRole(BasicRole.ADMIN.getRole());
    }

    /**
     * admin user wants discover
     *
     * @throws IOException
     */
    @Test
    public void adminDiscoveryTest() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	evaluate(DecisionType.PERMIT);
    }

    /**
     * admin user wants access
     *
     * @throws IOException
     */
    @Test
    public void adminAccessTest() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	evaluate(DecisionType.PERMIT);
    }

    /**
     * admin user wants do unsupported action
     *
     * @throws IOException
     */
    @Test
    public void adminUnsupportedActionTest() throws IOException {

	wrapper.setAction("unsupported");

	evaluate(DecisionType.DENY);
    }

    /**
     * admin user wants discover on OS path
     *
     * @throws IOException
     */
    @Test
    public void adminDiscoveryOSPathTest() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("opensearch");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * admin user wants discover on kma path
     *
     * @throws IOException
     */
    @Test
    public void adminDiscoveryKMAPathTest() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("kma");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * admin user wants access on kma path
     *
     * @throws IOException
     */
    @Test
    public void adminAccessKMAPathTest() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("kma");

	evaluate(DecisionType.PERMIT);
    }

}
