package eu.essi_lab.test.authorization.authzforce.essi.rbac.geossview.write;

import eu.essi_lab.authorization.pps.AbstractGEOSSViewPermissionPolicySet;
import eu.essi_lab.authorization.pps.GEOSSWritePermissionPolicySet;
import eu.essi_lab.authorization.rps.GEOSSWriteRolePolicySet;
import eu.essi_lab.test.authorization.authzforce.essi.rbac.geossview.read.GEOSSReadViewPPS_Test;

/**
 * @author Fabrizio
 */
public class GEOSSWrite_ReadViewAction_PPS_Test extends GEOSSReadViewPPS_Test {

    /**
     * @return
     */
    protected AbstractGEOSSViewPermissionPolicySet createPolicySet() {

	return new GEOSSWritePermissionPolicySet();
    }

    /**
     * @return
     */
    protected String getUserRole() {

	return GEOSSWriteRolePolicySet.ROLE;
    }

}
