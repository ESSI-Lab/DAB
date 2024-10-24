/**
 * 
 */
package eu.essi_lab.test.authorization.xacmlauthorizer.geossview;

import eu.essi_lab.authorization.rps.GEOSSPrivateWriteRolePolicySet;

/**
 * @author Fabrizio
 */
public class ReadView_GEOSS_PRIVATE_WRITE_ROLE_XACMLAuthorizerTest extends ReadView_GEOSS_READ_ROLE_XACMLAuthorizerTest {

    /**
     * @return
     */
    protected String getRole() {

	return GEOSSPrivateWriteRolePolicySet.ROLE;
    }
}
