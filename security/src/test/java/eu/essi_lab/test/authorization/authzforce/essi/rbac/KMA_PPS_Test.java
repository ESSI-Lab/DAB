package eu.essi_lab.test.authorization.authzforce.essi.rbac;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.authorization.PolicySetWrapper.Action;
import eu.essi_lab.authorization.pps.AbstractPermissionPolicySet;
import eu.essi_lab.test.authorization.authzforce.XACMLTest;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

/**
 * @author Fabrizio
 */
public class KMA_PPS_Test extends XACMLTest {

    @Before
    public void init() throws Exception {

	super.init();

	wrapper.reset();

	wrapper.setUserRole("kma");

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
    public void kmaDiscoveryDenyTest1() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("csw");

	wrapper.setViewIdentifier(AbstractPermissionPolicySet.VIEW_ID_MISSING_VALUE);

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny: wrong path
     *
     * @throws IOException
     */
    @Test
    public void kmaDiscoveryDenyTest2() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("path");

	wrapper.setViewIdentifier("KMA");

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny: wrong IP
     *
     * @throws IOException
     */
    @Test
    public void kmaDiscoveryDenyTest3() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("csw");

	wrapper.setViewIdentifier("KMA");

	wrapper.setIPs("ip");

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny: wrong path
     *
     * @throws IOException
     */
    @Test
    public void kmaDiscoveryDenyTest4() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("path");

	wrapper.setViewIdentifier("KMA");

	wrapper.setIPs("203.247.93.102");

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny: wrong view id
     *
     * @throws IOException
     */
    @Test
    public void kmaDiscoveryDenyTest5() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("kmaoaipmh");

	wrapper.setViewIdentifier("kma"); // must be uppercase KMA

	wrapper.setIPs("203.247.93.102");

	evaluate(DecisionType.DENY);
    }

    /**
     * Permit: two wrong IPs
     *
     * @throws IOException
     */
    @Test
    public void kmaDiscoveryDenyTest6() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("kmaoaipmh");

	wrapper.setViewIdentifier("KMA"); // must be uppercase KMA

	wrapper.setIPs( //
		"10.0.0.157", //
		"10.0.0.158");

	evaluate(DecisionType.DENY);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void kmaDiscoveryPermitTest1() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("kmaoaipmh");

	wrapper.setViewIdentifier("KMA");

	wrapper.setIPs("203.247.93.102");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void kmaDiscoveryPermitTest2() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("csw");

	wrapper.setViewIdentifier("KMA");

	wrapper.setIPs("203.247.93.102");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit: all right IPs
     *
     * @throws IOException
     */
    @Test
    public void kmaDiscoveryPermitTest3() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("csw");

	wrapper.setViewIdentifier("KMA");

	wrapper.setIPs("203.247.93.102", //
		"203.247.93.103", //
		"203.247.93.114", //
		"221.151.118.17", //
		"210.107.255.106", //
		"210.107.255.24", //
		"210.107.255.22", //
		"210.107.255.108", //
		"203.239.43.21", //
		"218.154.54.13", //
		"203.247.93.106");//

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit: one wrong IP, the other allowed
     *
     * @throws IOException
     */
    @Test
    public void kmaDiscoveryPermitTest4() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("kmaoaipmh");

	wrapper.setViewIdentifier("KMA"); // must be uppercase KMA

	wrapper.setIPs( //
		"10.0.0.157", // load balancer IP (not allowed)
		"203.247.93.102" // x-forwarder-for header IP (allowed)
	);

	evaluate(DecisionType.PERMIT);
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
    public void kmaDiscoveryPermitTest1_AccessDeny() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("kmaoaipmh");

	wrapper.setViewIdentifier("KMA");

	wrapper.setIPs("203.247.93.102");

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny
     *
     * @throws IOException
     */
    @Test
    public void kmaDiscoveryPermitTest2_AccessDeny() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("csw");

	wrapper.setViewIdentifier("KMA");

	wrapper.setIPs("203.247.93.102");

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny
     *
     * @throws IOException
     */
    @Test
    public void kmaDiscoveryPermitTest3_AccessDeny() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("csw");

	wrapper.setViewIdentifier("KMA");

	wrapper.setIPs("203.247.93.102", //
		"203.247.93.103", //
		"203.247.93.114", //
		"221.151.118.17", //
		"210.107.255.106", //
		"210.107.255.24", //
		"210.107.255.22", //
		"210.107.255.108", //
		"203.239.43.21", //
		"218.154.54.13", //
		"203.247.93.106");//

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny
     *
     * @throws IOException
     */
    @Test
    public void kmaDiscoveryPermitTest4_AccessDeny() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("kmaoaipmh");

	wrapper.setViewIdentifier("KMA");

	wrapper.setIPs( //
		"10.0.0.157", // load balancer IP (not allowed)
		"203.247.93.102" // x-forwarder-for header IP (allowed)
	);

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
    public void kmaDiscoveryPermitTest1_DenyForGWP() throws IOException {

	wrapper.reset();

	wrapper.setUserRole("gwp");

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("kmaoaipmh");

	wrapper.setViewIdentifier("KMA");

	wrapper.setIPs("203.247.93.102");

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny
     *
     * @throws IOException
     */
    @Test
    public void kmaDiscoveryPermitTest1_DenyForWHOS() throws IOException {

	wrapper.reset();

	wrapper.setUserRole("whos");

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("kmaoaipmh");

	wrapper.setViewIdentifier("KMA");

	wrapper.setIPs("203.247.93.102");

	evaluate(DecisionType.DENY);
    }

}
