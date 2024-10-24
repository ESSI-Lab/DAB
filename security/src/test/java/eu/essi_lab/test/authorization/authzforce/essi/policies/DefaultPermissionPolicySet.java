/**
 * 
 */
package eu.essi_lab.test.authorization.authzforce.essi.policies;

import java.util.UUID;

import eu.essi_lab.authorization.BasicRole;
import eu.essi_lab.authorization.pps.AbstractPermissionPolicySet;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;

/**
 * @author Fabrizio
 */
public class DefaultPermissionPolicySet extends AbstractPermissionPolicySet {

    public DefaultPermissionPolicySet() {

	super(BasicRole.DEFAULT.getRole());
    }

    @Override
    protected void editPPSPolicy() {

	String discoveryRuleId = UUID.randomUUID().toString().substring(0, 8);

	//
	// edit the discover rule
	//
	setDiscoveryAction(discoveryRuleId);
	setOffsetLimit(discoveryRuleId, 200);
	
	setMaxRecordsLimit(discoveryRuleId, 50);

	ApplyType pathApply = createPathApply("csw", "opensearch");

	ApplyType viewApply = createViewIdentifiersApply("default", "distributed");

	setAndCondition(discoveryRuleId, pathApply, viewApply);

	String accessRuleId = UUID.randomUUID().toString().substring(0, 8);

	//
	// edit the access rule
	//
	setAccessAction(accessRuleId);

	setDownloadLimit(accessRuleId, 10);
    }
}
