package eu.essi_lab.test.authorization.authzforce.essi.rbac.geossview.privatewrite;

import eu.essi_lab.authorization.pps.AbstractGEOSSViewPermissionPolicySet;
import eu.essi_lab.authorization.pps.GEOSSPrivateWritePermissionPolicySet;
import eu.essi_lab.authorization.rps.GEOSSPrivateWriteRolePolicySet;
import eu.essi_lab.test.authorization.authzforce.essi.rbac.geossview.read.GEOSSReadDiscoveryPPS_Test;

/**
 * @author Fabrizio
 */
public class GEOSSPrivateWrite_DiscoveryAction_PPS_Test extends GEOSSReadDiscoveryPPS_Test {

    /**
     * @return
     */
    protected AbstractGEOSSViewPermissionPolicySet createPolicySet() {

	return new GEOSSPrivateWritePermissionPolicySet();
    }

    /**
     * @return
     */
    protected String getUserRole() {

	return GEOSSPrivateWriteRolePolicySet.ROLE;
    }

}
