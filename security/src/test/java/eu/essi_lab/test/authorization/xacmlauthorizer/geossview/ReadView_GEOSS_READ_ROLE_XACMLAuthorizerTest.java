/**
 * 
 */
package eu.essi_lab.test.authorization.xacmlauthorizer.geossview;

import eu.essi_lab.authorization.pps.GEOSSWritePermissionPolicySet;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.view.ReadViewMessage;

/**
 * @author Fabrizio
 */
public class ReadView_GEOSS_READ_ROLE_XACMLAuthorizerTest extends Discovery_GEOSS_READ_ROLE_XACMLAuthorizerTest {

    /**
     * 
     * @return
     */
    protected String getPath() {

	return GEOSSWritePermissionPolicySet.SUPPORTED_PATH;
    }
    
    /**
     * @return
     */
    protected RequestMessage getMessage() {

	return new ReadViewMessage();
    }
}
