/**
 * 
 */
package eu.essi_lab.test.authorization.authzforce.essi.policies;

import eu.essi_lab.authorization.rps.AbstractRolePolicySet;

/**
 * @author Fabrizio
 */
public class LimitedRolePolicySet extends AbstractRolePolicySet {

    /**
     * @param role
     */
    public LimitedRolePolicySet() {

	super("limited");
    }
}
