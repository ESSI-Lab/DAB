/**
 * 
 */
package eu.essi_lab.test.authorization.authzforce.essi.policies;

import java.util.UUID;

import eu.essi_lab.authorization.pps.AbstractPermissionPolicySet;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ApplyType;

/**
 * @author Fabrizio
 */
public class LimitedPermissionPolicySet extends AbstractPermissionPolicySet {

    public LimitedPermissionPolicySet() {

	super("limited");
    }

    @Override
    protected void editPPSPolicy() {

	String discoveryRuleId = UUID.randomUUID().toString().substring(0, 8);

	//
	// edit the discover rule
	//
	setDiscoveryAction(discoveryRuleId);

	ApplyType sourceApply = createSourcesApply("source1", "source2", "source3");

	setAndCondition(discoveryRuleId, sourceApply);
    }
}
