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
abstract public class CreatorPSS_Test extends XACMLTest {

    /**
     * @return
     */
    protected abstract String getRole();

    /**
     * @return
     */
    protected abstract String getDenyForRoleOne();

    /**
     * @return
     */
    protected abstract String getDenyForRoleTwo();

    @Before
    public void init() throws Exception {

	super.init();

	wrapper.reset();

	wrapper.setUserRole(getRole());

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
    public void discoveryDenyTest1() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("csw");

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	evaluate(DecisionType.DENY);
    }

    /**
     * DenyTest: no view creator provided
     *
     * @throws IOException
     */
    @Test
    public void otherDenyTest1() throws IOException {

	wrapper.setAction(Action.OTHER.getId());

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
    public void discoveryDenyTest2() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("csw");

	wrapper.setViewCreator("pippo");

	evaluate(DecisionType.DENY);
    }

    /**
     * DenyTest: invalid view creator provided
     *
     * @throws IOException
     */
    @Test
    public void otherDenyTest2() throws IOException {

	wrapper.setAction(Action.OTHER.getId());

	wrapper.setPath("csw");

	wrapper.setViewCreator("pippo");

	evaluate(DecisionType.DENY);
    }

    /**
     * DenyTest: invalid path
     *
     * @throws IOException
     */
    @Test
    public void discoveryDenyTest4() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("path");

	wrapper.setViewCreator(getRole());

	evaluate(DecisionType.DENY);
    }

    /**
     * DenyTest: invalid path
     *
     * @throws IOException
     */
    @Test
    public void otherDenyTest4() throws IOException {

	wrapper.setAction(Action.OTHER.getId());

	wrapper.setPath("path");

	wrapper.setViewCreator(getRole());

	evaluate(DecisionType.DENY);
    }

    /**
     * DenyTest: all invalid
     *
     * @throws IOException
     */
    @Test
    public void discoveryDenyTest5() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("path");

	wrapper.setViewCreator("creator");

	evaluate(DecisionType.DENY);
    }

    /**
     * DenyTest: all invalid
     *
     * @throws IOException
     */
    @Test
    public void otherDenyTest5() throws IOException {

	wrapper.setAction(Action.OTHER.getId());

	wrapper.setPath("path");

	wrapper.setViewCreator("creator");

	evaluate(DecisionType.DENY);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void discoveryPermitTest1() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("csw");

	wrapper.setViewCreator(getRole());

	evaluate(DecisionType.PERMIT);
    }
    
    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void otherPermitTest1() throws IOException {

	wrapper.setAction(Action.OTHER.getId());

	wrapper.setPath("csw");

	wrapper.setViewCreator(getRole());

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void discoveryPermitTest2() throws IOException {

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("opensearch");

	wrapper.setViewCreator(getRole());

	evaluate(DecisionType.PERMIT);
    }
    
    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void otherPermitTest2() throws IOException {

	wrapper.setAction(Action.OTHER.getId());

	wrapper.setPath("opensearch");

	wrapper.setViewCreator(getRole());

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Deny for role 1
     *
     * @throws IOException
     */
    @Test
    public void discoveryPermitTest1_DenyForRoleOne() throws IOException {

	wrapper.reset();

	wrapper.setUserRole(getDenyForRoleOne());

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("opensearch");

	wrapper.setViewCreator(getRole());

	evaluate(DecisionType.DENY);
    }
    
    /**
     * Deny for role 1
     *
     * @throws IOException
     */
    @Test
    public void otherPermitTest1_DenyForRoleOne() throws IOException {

	wrapper.reset();

	wrapper.setUserRole(getDenyForRoleOne());

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);

	wrapper.setAction(Action.OTHER.getId());

	wrapper.setPath("opensearch");

	wrapper.setViewCreator(getRole());

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny for role 2
     *
     * @throws IOException
     */
    @Test
    public void discoveryPermitTest1_DenyForRoleTwo() throws IOException {

	wrapper.reset();

	wrapper.setUserRole(getDenyForRoleTwo());

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);

	wrapper.setAction(Action.DISCOVERY.getId());

	wrapper.setPath("opensearch");

	wrapper.setViewCreator(getRole());

	evaluate(DecisionType.DENY);
    }
    
    /**
     * Deny for role 2
     *
     * @throws IOException
     */
    @Test
    public void otherPermitTest1_DenyForRoleTwo() throws IOException {

	wrapper.reset();

	wrapper.setUserRole(getDenyForRoleTwo());

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);

	wrapper.setAction(Action.OTHER.getId());

	wrapper.setPath("opensearch");

	wrapper.setViewCreator(getRole());

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
    public void accessDenyTest1() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("rest");

	wrapper.setViewCreator(AbstractPermissionPolicySet.VIEW_CREATOR_MISSING_VALUE);

	evaluate(DecisionType.DENY);
    }
    
    /**
     * DenyTest: no view creator provided
     *
     * @throws IOException
     */
    @Test
    public void otherDenyTest1_2() throws IOException {

	wrapper.setAction(Action.OTHER.getId());

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
    public void accessDenyTest2() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("rest");

	wrapper.setViewCreator("pippo");

	evaluate(DecisionType.DENY);
    }
    
    /**
     * DenyTest: invalid view creator provided
     *
     * @throws IOException
     */
    @Test
    public void otherDenyTest2_2() throws IOException {

	wrapper.setAction(Action.OTHER.getId());

	wrapper.setPath("rest");

	wrapper.setViewCreator("pippo");

	evaluate(DecisionType.DENY);
    }

    /**
     * DenyTest: invalid path
     *
     * @throws IOException
     */
    @Test
    public void accessDenyTest4() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("path");

	wrapper.setViewCreator(getRole());

	evaluate(DecisionType.DENY);
    }

    /**
     * DenyTest: all invalid
     *
     * @throws IOException
     */
    @Test
    public void accessDenyTest5() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("path");

	wrapper.setViewCreator("creator");

	evaluate(DecisionType.DENY);
    }
    
    /**
     * DenyTest: all invalid
     *
     * @throws IOException
     */
    @Test
    public void otherDenyTest5_2() throws IOException {

	wrapper.setAction(Action.OTHER.getId());

	wrapper.setPath("path");

	wrapper.setViewCreator("creator");

	evaluate(DecisionType.DENY);
    }

    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void accessPermitTest1() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("rest");

	wrapper.setViewCreator(getRole());

	evaluate(DecisionType.PERMIT);
    }
    
    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void otherPermitTest1_2() throws IOException {

	wrapper.setAction(Action.OTHER.getId());

	wrapper.setPath("rest");

	wrapper.setViewCreator(getRole());

	evaluate(DecisionType.PERMIT);
    }
    
    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void accessPermitTest2() throws IOException {

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("wms");

	wrapper.setViewCreator(getRole());

	evaluate(DecisionType.PERMIT);
    }
    
    /**
     * Permit
     *
     * @throws IOException
     */
    @Test
    public void otherPermitTest2_2() throws IOException {

	wrapper.setAction(Action.OTHER.getId());

	wrapper.setPath("wms");

	wrapper.setViewCreator(getRole());

	evaluate(DecisionType.PERMIT);
    }

    /**
     * Deny for role 1
     *
     * @throws IOException
     */
    @Test
    public void accessPermitTest1_DenyForRoleOne() throws IOException {

	wrapper.reset();

	wrapper.setUserRole(getDenyForRoleOne());

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("rest");

	wrapper.setViewCreator(getRole());

	evaluate(DecisionType.DENY);
    }
    
    /**
     * Deny for role 1
     *
     * @throws IOException
     */
    @Test
    public void otherPermitTest1_DenyForRoleOne_2() throws IOException {

	wrapper.reset();

	wrapper.setUserRole(getDenyForRoleOne());

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);

	wrapper.setAction(Action.OTHER.getId());

	wrapper.setPath("rest");

	wrapper.setViewCreator(getRole());

	evaluate(DecisionType.DENY);
    }

    /**
     * Deny for role 2
     *
     * @throws IOException
     */
    @Test
    public void accessPermitTest1_DenyForRoleTwo() throws IOException {

	wrapper.reset();

	wrapper.setUserRole(getDenyForRoleTwo());

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);

	wrapper.setAction(Action.ACCESS.getId());

	wrapper.setPath("rest");

	wrapper.setViewCreator(getRole());

	evaluate(DecisionType.DENY);
    }
    
    /**
     * Deny for role 2
     *
     * @throws IOException
     */
    @Test
    public void otherPermitTest1_DenyForRoleTwo_2() throws IOException {

	wrapper.reset();

	wrapper.setUserRole(getDenyForRoleTwo());

	wrapper.setMaxRecords(1000);

	wrapper.setOffset(1000);

	wrapper.setAction(Action.OTHER.getId());

	wrapper.setPath("rest");

	wrapper.setViewCreator(getRole());

	evaluate(DecisionType.DENY);
    }

   
}
