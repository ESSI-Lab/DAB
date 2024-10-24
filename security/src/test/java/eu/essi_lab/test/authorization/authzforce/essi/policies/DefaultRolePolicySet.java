/**
 * 
 */
package eu.essi_lab.test.authorization.authzforce.essi.policies;

import eu.essi_lab.authorization.BasicRole;
import eu.essi_lab.authorization.rps.AbstractRolePolicySet;

/**
 * @author Fabrizio
 */
public class DefaultRolePolicySet extends AbstractRolePolicySet {

    /**
     * @param role
     */
    public DefaultRolePolicySet() {

	super(BasicRole.DEFAULT.getRole());
    }
}
