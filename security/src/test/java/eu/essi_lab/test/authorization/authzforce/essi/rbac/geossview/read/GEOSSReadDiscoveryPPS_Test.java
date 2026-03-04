package eu.essi_lab.test.authorization.authzforce.essi.rbac.geossview.read;

import eu.essi_lab.authorization.*;
import eu.essi_lab.authorization.PolicySetWrapper.*;
import eu.essi_lab.authorization.pps.*;
import eu.essi_lab.authorization.psloader.*;
import eu.essi_lab.authorization.rps.*;
import eu.essi_lab.authorization.xacml.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.bond.View.*;
import eu.essi_lab.test.authorization.authzforce.*;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.*;
import org.junit.*;

import java.io.*;

/**
 * @author Fabrizio
 */
public class GEOSSReadDiscoveryPPS_Test extends XACMLTest {

    @Before
    public void init() throws Exception {

	super.init();

	wrapper.reset();

	GSLoggerFactory.getLogger(getClass()).info("Testing role: " + getUserRole());

	wrapper.setUserRole(getUserRole());
    }

    /**
     * Permit: - discovery path OK - view creator OK - view visibility public - view owner not required, since the view visibility is
     * public
     *
     * @throws IOException
     */
    @Test
    public void actionPermitTest1() throws IOException {

	GSLoggerFactory.getLogger(getClass()).info("Testing action: " + getAction().getId());

	GSLoggerFactory.getLogger(getClass()).info("Testing path: " + getPath());

	wrapper.setAction(getAction().getId());

	wrapper.setPath(getPath());

	wrapper.setViewCreator("geoss");

	wrapper.setViewVisibility(ViewVisibility.PUBLIC);

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Deny: - discovery path NOT supported - view creator OK - view visibility public - view owner not required, since the view visibility
     * is public
     *
     * @throws IOException
     */
    @Test
    public void actionDenyTest1_1() throws IOException {

	GSLoggerFactory.getLogger(getClass()).info("Testing action: " + getAction().getId());

	wrapper.setAction(getAction().getId());

	wrapper.setPath("xxx");

	wrapper.setViewCreator("geoss");

	wrapper.setViewVisibility(ViewVisibility.PUBLIC);

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny: - discovery path OK - view creator NOT supported - view visibility public - view owner not required, since the view visibility
     * is public
     *
     * @throws IOException
     */
    @Test
    public void actionDenyTest1_2() throws IOException {

	GSLoggerFactory.getLogger(getClass()).info("Testing action: " + getAction().getId());

	GSLoggerFactory.getLogger(getClass()).info("Testing path: " + getPath());

	wrapper.setAction(getAction().getId());

	wrapper.setPath(getPath());

	wrapper.setViewCreator("xx");

	wrapper.setViewVisibility(ViewVisibility.PUBLIC);

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny: - discovery path OK - view creator OK - view visibility private - view owner set in the request, but the related value in the
     * PPS {@link GEOSSReadPermissionPolicySet#setUserIdentifier(String)} is not set so the comparison fails
     *
     * @throws IOException
     */
    @Test
    public void actionDenyTest1() throws IOException {

	GSLoggerFactory.getLogger(getClass()).info("Testing action: " + getAction().getId());

	GSLoggerFactory.getLogger(getClass()).info("Testing path: " + getPath());

	wrapper.setAction(getAction().getId());

	wrapper.setPath(getPath());

	wrapper.setViewCreator("geoss");

	wrapper.setViewVisibility(ViewVisibility.PRIVATE);

	wrapper.setViewOwner("pippo");

	evaluate(DecisionType.DENY);
    }

    /**
     * Permit: - discovery path OK - view creator OK - view visibility private - view owner set in the request and the pps is updated with
     * the same user id
     *
     * @throws Exception
     */
    @Test
    public void actionPermitTest2() throws Exception {

	GSLoggerFactory.getLogger(getClass()).info("Testing action: " + getAction().getId());

	GSLoggerFactory.getLogger(getClass()).info("Testing path: " + getPath());

	//
	//
	//

	wrapper = reinitWrapper("pippo");

	//
	//
	//

	wrapper.setUserRole(getUserRole());

	wrapper.setAction(getAction().getId());

	wrapper.setPath(getPath());

	wrapper.setViewCreator("geoss");

	wrapper.setViewVisibility(ViewVisibility.PRIVATE);

	wrapper.setViewOwner("pippo");

	//
	//
	//

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Deny: - discovery path OK - view creator OK - view visibility private - view owner set in the request and the pps is updated with a
     * different user id
     *
     * @throws Exception
     */
    @Test
    public void actionDenyTest2() throws Exception {

	GSLoggerFactory.getLogger(getClass()).info("Testing action: " + getAction().getId());

	GSLoggerFactory.getLogger(getClass()).info("Testing path: " + getPath());

	//
	//
	//

	wrapper = reinitWrapper("pluto");

	//
	//
	//

	wrapper.setUserRole(getUserRole());

	wrapper.setAction(getAction().getId());

	wrapper.setPath(getPath());

	wrapper.setViewCreator("geoss");

	wrapper.setViewVisibility(ViewVisibility.PRIVATE);

	wrapper.setViewOwner("pippo");

	//
	//
	//

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny: - discovery path NOT allowed - view creator OK - view visibility private - view owner set in the request and the pps is updated
     * with the same user id
     *
     * @throws Exception
     */
    @Test
    public void actionDenyTest3() throws Exception {

	GSLoggerFactory.getLogger(getClass()).info("Testing action: " + getAction().getId());

	GSLoggerFactory.getLogger(getClass()).info("Testing path: " + getPath());

	//
	//
	//

	wrapper = reinitWrapper("pippo");

	//
	//
	//

	wrapper.setUserRole(getUserRole());

	wrapper.setAction(getAction().getId());

	wrapper.setPath("none");

	wrapper.setViewCreator("geoss");

	wrapper.setViewVisibility(ViewVisibility.PRIVATE);

	wrapper.setViewOwner("pippo");

	//
	//
	//

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny: - discovery path OK - view creator NOT allowed - view visibility private - view owner set in the request and the pps is updated
     * with the same user id
     *
     * @throws Exception
     */
    @Test
    public void actionDenyTest4() throws Exception {

	GSLoggerFactory.getLogger(getClass()).info("Testing action: " + getAction().getId());

	GSLoggerFactory.getLogger(getClass()).info("Testing path: " + getPath());

	//
	//
	//

	wrapper = reinitWrapper("pippo");

	//
	//
	//

	wrapper.setUserRole(getUserRole());

	wrapper.setAction(getAction().getId());

	wrapper.setPath(getPath());

	wrapper.setViewCreator("pluto");

	wrapper.setViewVisibility(ViewVisibility.PRIVATE);

	wrapper.setViewOwner("pippo");

	//
	//
	//

	evaluate(DecisionType.DENY);
    }

    /**
     * @return
     */
    protected String getPath() {

	return "csw";
    }

    /**
     * @return
     */
    protected Action getAction() {

	return Action.DISCOVERY;
    }

    /**
     * @return
     */
    protected String getUserRole() {

	return GEOSSReadRolePolicySet.ROLE;
    }

    /**
     * @return
     */
    protected AbstractGEOSSViewPermissionPolicySet createPolicySet() {

	return new GEOSSReadPermissionPolicySet();
    }

    /**
     * @return
     * @throws Exception
     */
    protected PdpEngineWrapper reinitWrapper(String userIdentifier) throws Exception {

	AbstractGEOSSViewPermissionPolicySet pps = createPolicySet();

	return reinitWrapper(userIdentifier, pps, getClass());
    }

    /**
     * @return
     * @throws Exception
     */
    public static PdpEngineWrapper reinitWrapper(String userIdentifier, AbstractGEOSSViewPermissionPolicySet pps, Class<?> clazz)
	    throws Exception {

	GSLoggerFactory.getLogger(clazz).info("Testing PPS: " + pps.getClass());

	pps.setUserIdentifier(userIdentifier);

	PolicySetLoader loader = new DefaultPolicySetLoader();

	loader.setPermissionPolicySet(pps);

	PdpEngineBuilder builder = new PdpEngineBuilder();

	builder.addPolicies(loader);

	return new PdpEngineWrapper(builder.build(true));
    }
}
