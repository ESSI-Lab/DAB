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
public class SSC_PPS_Test extends XACMLTest {

    @Before
    public void init() throws Exception {

	super.init();

	wrapper.reset();

	wrapper.setUserRole("ssc");

	//
	// max records and offset are ignored by the rule, by they are mandatory in
	// the request (put in order to avoid request compilation error)
	//

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);
    }

    /**
     * Deny: wrong IP
     *
     * @throws IOException
     */
    @Test
    public void sscDiscoveryDenyTest1() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("sos");

	wrapper.setIPs("194.67.141.500");

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny: wrong path
     *
     * @throws IOException
     */
    @Test
    public void sscDiscoveryDenyTest2() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("path");

	wrapper.setIPs("194.67.141.5");

	evaluate(DecisionType.DENY);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void sscDiscoveryPermitTest1() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("sos");

	wrapper.setIPs("194.67.141.5");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void sscDiscoveryPermitTest2() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("wcs");

	wrapper.setIPs("194.67.141.5");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void sscDiscoveryPermitTest3() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("sos");

	wrapper.setIPs("152.61.128.50");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void sscDiscoveryPermitTest4() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("wcs");

	wrapper.setIPs("152.61.128.50");

	evaluate(DecisionType.PERMIT);
    }

    //
    //
    //

    /**
     * Deny: wrong IP
     *
     * @throws IOException
     */
    @Test
    public void sscAccessDenyTest1() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("sos");

	wrapper.setIPs("194.67.141.500");

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny: wrong path
     *
     * @throws IOException
     */
    @Test
    public void sscAccessDenyTest2() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("path");

	wrapper.setIPs("194.67.141.5");

	evaluate(DecisionType.DENY);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void sscAccessPermitTest1() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("sos");

	wrapper.setIPs("194.67.141.5");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void sscAccessPermitTest2() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("wcs");

	wrapper.setIPs("194.67.141.5");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void sscAccessPermitTest3() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("sos");

	wrapper.setIPs("152.61.128.50");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void sscAccessPermitTest4() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("wcs");

	wrapper.setIPs("152.61.128.50");

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
    public void sscDiscoveryPermitTest1_DenyForGWP() throws IOException {

	wrapper.reset();

	wrapper.setUserRole("gwp");

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setOriginHeader("https://www.geoportal.org");

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	//
	//
	//

	wrapper.setPath("sos");

	wrapper.setIPs("194.67.141.5");

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny
     *
     * @throws IOException
     */
    @Test
    public void sscDiscoveryPermitTest1_DenyForWHOS() throws IOException {

	wrapper.reset();

	wrapper.setUserRole("whos");

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	//
	//
	//

	wrapper.setPath("sos");

	wrapper.setIPs("194.67.141.5");

	evaluate(DecisionType.DENY);
    }
}
