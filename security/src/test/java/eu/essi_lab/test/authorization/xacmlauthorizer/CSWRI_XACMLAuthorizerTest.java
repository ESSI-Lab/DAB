/**
 * 
 */
package eu.essi_lab.test.authorization.xacmlauthorizer;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.authorization.pps.CSW_RIPermissionPolicySet;
import eu.essi_lab.authorization.rps.CSW_RIRolePolicySet;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.DiscoveryMessage;

/**
 * @author Fabrizio
 */
public class CSWRI_XACMLAuthorizerTest extends AbstractXACMLAuthorizerTest {

    /**
     * cswri user wants access
     *
     * @throws IOException
     */
    @Test
    public void cswriAccessTest() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, CSW_RIRolePolicySet.ROLE);

	setWebRequest(message, CSW_RIPermissionPolicySet.SUPPORTED_PATH);

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * cswri user wants discover on csw path with cswri view
     *
     * @throws IOException
     */
    @Test
    public void cswriDiscoveryCSWPathCiteViewIdTest() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, CSW_RIRolePolicySet.ROLE);

	setWebRequest(message, CSW_RIPermissionPolicySet.SUPPORTED_PATH);

	setView(message, CSW_RIPermissionPolicySet.SUPPORTED_VIEW_ID);

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(true, authorized);

    }

    /**
     * cswri user wants discover on csw path with wrong view
     *
     * @throws IOException
     */
    @Test
    public void cswriDiscoveryCSWPathWrongViewIdTest() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, CSW_RIRolePolicySet.ROLE);

	setWebRequest(message, CSW_RIPermissionPolicySet.SUPPORTED_PATH);

	setView(message, "xxx");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * cswri user wants discover on csw path with cswri view
     *
     * @throws IOException
     */
    @Test
    public void cswriDiscoveryOSPathCiteViewIdTest() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, CSW_RIRolePolicySet.ROLE);

	setWebRequest(message, "opensearch");

	setView(message, CSW_RIPermissionPolicySet.SUPPORTED_VIEW_ID);

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }
}
