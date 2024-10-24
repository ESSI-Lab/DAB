package eu.essi_lab.test.authorization.authzforce.essi.rbac.geossview.read;

import eu.essi_lab.authorization.PolicySetWrapper.Action;
import eu.essi_lab.authorization.pps.GEOSSWritePermissionPolicySet;

/**
 * @author Fabrizio
 */
public class GEOSSReadViewPPS_Test extends GEOSSReadDiscoveryPPS_Test {

    /**
     * @return
     */
    protected String getPath() {

	return GEOSSWritePermissionPolicySet.SUPPORTED_PATH;
    }

    /**
     * @return
     */
    protected Action getAction() {

	return Action.READ_VIEW;
    }
}
