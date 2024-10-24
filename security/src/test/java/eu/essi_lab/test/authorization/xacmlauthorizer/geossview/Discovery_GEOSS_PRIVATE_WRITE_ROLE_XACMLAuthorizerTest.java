/**
 * 
 */
package eu.essi_lab.test.authorization.xacmlauthorizer.geossview;

import eu.essi_lab.authorization.rps.GEOSSPrivateWriteRolePolicySet;

/**
 * @author Fabrizio
 */
public class Discovery_GEOSS_PRIVATE_WRITE_ROLE_XACMLAuthorizerTest extends Discovery_GEOSS_READ_ROLE_XACMLAuthorizerTest {

    /**
     * @return
     */
    @Override
    protected String getRole() {

	return GEOSSPrivateWriteRolePolicySet.ROLE;
    }
}
