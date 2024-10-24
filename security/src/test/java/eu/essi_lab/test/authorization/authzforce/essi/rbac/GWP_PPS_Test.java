/**
 * 
 */
package eu.essi_lab.test.authorization.authzforce.essi.rbac;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.authorization.PolicySetWrapper.Action;
import eu.essi_lab.authorization.pps.AbstractPermissionPolicySet;
import eu.essi_lab.test.authorization.authzforce.XACMLTest;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;

/**
 * GWP users are allowed to discovery and access if and only if:
 * -
 * 1) the view creator is "geoss"
 * -
 * 2) the origin header is "https://www.geoportal.org" or "https://geoss.uat.esaportal.eu"
 * -
 * 3) the discovery path is one of:
 * "opensearch",
 * "csw",
 * "cswisogeo",
 * "hiscentral.asmx",
 * "arpa-rest",
 * "cuahsi_1_1.asmx",
 * "rest",
 * "hydrocsv",
 * "sos",
 * "gwis",
 * "ArcGIS",
 * "gwps"
 * -
 * 4) the access path is one of:
 * "cuahsi_1_1.asmx",
 * "rest",
 * "hydrocsv",
 * "thredds",
 * "wms",
 * "wfs",
 * "sos",
 * "gwps",
 * "ArcGIS",
 * "ArcGISProxy",
 * "timeseries-api",
 * "gwis"
 * 
 * @author Fabrizio
 */
public class GWP_PPS_Test extends XACMLTest {

    @Before
    public void init() throws Exception {

	super.init();

	wrapper.reset();

	wrapper.setUserRole("gwp");

	//
	// max records and offset are ignored by the rule, by they are mandatory in
	// the request (put in order to avoid request compilation error)
	//

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);
    }

    /**
     * DenyTest: no view creator provided
     *
     * @throws IOException
     */
    @Test
    public void gwpDiscoveryDenyTest1() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("csw");

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	evaluate(DecisionType.DENY);
    }

    /**
     * DenyTest: invalid view creator provided
     *
     * @throws IOException
     */
    @Test
    public void gwpDiscoveryDenyTest2() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("csw");

	wrapper.setViewCreator("pippo");

	evaluate(DecisionType.DENY);
    }

    /**
     * DenyTest: invalid origin
     *
     * @throws IOException
     */
    @Test
    public void gwpDiscoveryDenyTest3() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("cuahsi_1_1.asmx");

	wrapper.setViewCreator("geoss");

	wrapper.setOriginHeader("https://");

	evaluate(DecisionType.DENY);
    }

    /**
     * DenyTest: invalid path
     *
     * @throws IOException
     */
    @Test
    public void gwpDiscoveryDenyTest4() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("path");

	wrapper.setViewCreator("geoss");

	wrapper.setOriginHeader("https://www.geoportal.org");

	evaluate(DecisionType.DENY);
    }

    /**
     * DenyTest: all invalid
     *
     * @throws IOException
     */
    @Test
    public void gwpDiscoveryDenyTest5() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("path");

	wrapper.setViewCreator("creator");

	wrapper.setOriginHeader("header");

	evaluate(DecisionType.DENY);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void gwpDiscoveryPermitTest1() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("csw");

	wrapper.setViewCreator("geoss");

	wrapper.setOriginHeader("https://www.geoportal.org");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void gwpDiscoveryPermitTest2() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("opensearch");

	wrapper.setViewCreator("geoss");

	wrapper.setOriginHeader("https://www.geoportal.org");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Deny
     *
     * @throws IOException
     */
    @Test
    public void gwpDiscoveryPermitTest1_denyForWHOS() throws IOException {

	wrapper.reset();

	wrapper.setUserRole("whos");

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("csw");

	wrapper.setViewCreator("geoss");

	wrapper.setOriginHeader("https://www.geoportal.org");

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny
     * 
     * @throws IOException
     */
    @Test
    public void gwpDiscoveryPermitTest1_denyForEIFFEL() throws IOException {

	wrapper.reset();

	wrapper.setUserRole("eiffel");

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("csw");

	wrapper.setViewCreator("geoss");

	wrapper.setOriginHeader("https://www.geoportal.org");

	evaluate(DecisionType.DENY);
    }

    //
    //
    //

    /**
     * DenyTest: no view creator provided
     *
     * @throws IOException
     */
    @Test
    public void gwpAccessDenyTest1() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("rest");

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	evaluate(DecisionType.DENY);
    }

    /**
     * DenyTest: invalid view creator provided
     *
     * @throws IOException
     */
    @Test
    public void gwpAccessDenyTest2() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("rest");

	wrapper.setViewCreator("pippo");

	evaluate(DecisionType.DENY);
    }

    /**
     * DenyTest: invalid origin
     *
     * @throws IOException
     */
    @Test
    public void gwpAccessDenyTest3() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("cuahsi_1_1.asmx");

	wrapper.setViewCreator("geoss");

	wrapper.setOriginHeader("https://");

	evaluate(DecisionType.DENY);
    }

    /**
     * DenyTest: invalid path
     *
     * @throws IOException
     */
    @Test
    public void gwpAccessDenyTest4() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("path");

	wrapper.setViewCreator("geoss");

	wrapper.setOriginHeader("https://www.geoportal.org");

	evaluate(DecisionType.DENY);
    }

    /**
     * DenyTest: all invalid
     *
     * @throws IOException
     */
    @Test
    public void gwpAccessDenyTest5() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("path");

	wrapper.setViewCreator("creator");

	wrapper.setOriginHeader("header");

	evaluate(DecisionType.DENY);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void gwpAccessPermitTest1() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("rest");

	wrapper.setViewCreator("geoss");

	wrapper.setOriginHeader("https://www.geoportal.org");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void gwpAccessPermitTest2() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("wms");

	wrapper.setViewCreator("geoss");

	wrapper.setOriginHeader("https://www.geoportal.org");

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Deny
     *
     * @throws IOException
     */
    @Test
    public void gwpAccessPermitTest1_denyForWHOS() throws IOException {

	wrapper.reset();

	wrapper.setUserRole("whos");

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("rest");

	wrapper.setViewCreator("geoss");

	wrapper.setOriginHeader("https://www.geoportal.org");

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny
     * 
     * @throws IOException
     */
    @Test
    public void gwpAccessPermitTest1_denyForEIFFEL() throws IOException {

	wrapper.reset();

	wrapper.setUserRole("eiffel");

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("rest");

	wrapper.setViewCreator("geoss");

	wrapper.setOriginHeader("https://www.geoportal.org");

	evaluate(DecisionType.DENY);
    }
}
