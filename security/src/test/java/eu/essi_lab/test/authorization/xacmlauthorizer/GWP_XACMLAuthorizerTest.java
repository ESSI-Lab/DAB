/**
 * 
 */
package eu.essi_lab.test.authorization.xacmlauthorizer;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.DiscoveryMessage;

/**
 * @author Fabrizio
 */
public class GWP_XACMLAuthorizerTest extends AbstractXACMLAuthorizerTest {

    /**
     * Deny because the offset is gt 200 and origin is not set (so also the user cannot be recognized
     * since the registered GWP users has the two supported origins as identifier)
     *
     * @throws Exception
     */
    @Test
    public void gwpDiscoveryActionNotGWPClient1() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setWebRequest(message, "csw");

	setOffset(message, 201);

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny because the offset is gt 200 and origin is not valid (so also the user cannot be recognized
     * since the registered GWP users has the two supported origins as identifier)
     *
     * @throws Exception
     */
    @Test
    public void gwpDiscoveryActionNotGWPClient2() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setWebRequest(message, "csw", "https://google.com");

	setOffset(message, 201);

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny because the offset is gt 200 and origin is not valid, the user is set just for testing purpose
     * but without origin, it cannot be recognized
     *
     * @throws Exception
     */
    @Test
    public void gwpDiscoveryActionNotGWPClient3() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "gwp");

	setWebRequest(message, "csw", "https://google.com");

	setOffset(message, 201);

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny because the offset is gt 200 and origin is VALID but the user is not authorized (actually
     * with the origin of the portal, the correct user would be recognized, so this is
     * a non real case)
     *
     * @throws Exception
     */
    @Test
    public void gwpDiscoveryActionGWPClientWrongUser() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "geoss");

	setWebRequest(message, "csw", "https://www.geoportal.org");

	setOffset(message, 201);

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * @throws Exception
     */
    @Test
    public void gwpDiscoveryActionGWPClientCorrectUser_1() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "gwp");

	setWebRequest(message, "csw", "https://geoss.uat.esaportal.eu");

	setOffset(message, 201);

	setView(message, "geoss", "geoss");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(true, authorized);
    }

    /**
     * @throws Exception
     */
    @Test
    public void gwpDiscoveryActionGWPClientCorrectUser2_1() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "gwp");

	setWebRequest(message, "csw", "https://geoss.uat.esaportal.eu");

	setOffset(message, 50);

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * @throws Exception
     */
    @Test
    public void gwpDiscoveryActionGWPClientCorrectUser4_2() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "gwp");

	setWebRequest(message, "csw", "https://geoss.uat.esaportal.eu");

	setOffset(message, 300);

	// only view creator is required to be "geoss", no test on the view id
	setView(message, "xxx", "geoss");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(true, authorized);
    }

    /**
     * Deny because the offset is gt 200 and origin is not set (so also the user cannot be recognized
     * since the registered GWP users has the two supported origins as identifier)
     *
     * @throws Exception
     */
    @Test
    public void gwpAccessActionNotGWPClient1() throws Exception {

	AccessMessage message = new AccessMessage();

	setWebRequest(message, "wms");

	setOffset(message, 201);

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny because the offset is gt 200 and origin is not valid (so also the user cannot be recognized
     * since the registered GWP users has the two supported origins as identifier)
     *
     * @throws Exception
     */
    @Test
    public void gwpAccessActionNotGWPClient2() throws Exception {

	AccessMessage message = new AccessMessage();

	setWebRequest(message, "wms", "https://google.com");

	setOffset(message, 201);

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny because the offset is gt 200 and origin is not valid, the user is set just for testing purpose
     * but without origin, it cannot be recognized
     *
     * @throws Exception
     */
    @Test
    public void gwpAccessActionNotGWPClient3() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "gwp");

	setWebRequest(message, "wms", "https://google.com");

	setOffset(message, 201);

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny because the offset is gt 200 and origin is VALID but the user is not authorized (actually
     * with the origin of the portal, the correct user would be recognized, so this is
     * a non real case)
     *
     * @throws Exception
     */
    @Test
    public void gwpAccessActionGWPClientWrongUser() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "geoss");

	setWebRequest(message, "wms", "https://www.geoportal.org");

	setOffset(message, 201);

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Permits because the offset is gt 200 and origin is VALID and the user is the right one
     *
     * @throws Exception
     */
    @Test
    public void gwpAccessActionGWPClientCorrectUser() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "gwp");

	setWebRequest(message, "wms", "https://www.geoportal.org");

	setOffset(message, 201);

	setView(message, "xxx", "geoss");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(true, authorized);
    }

    /**
     * Permits because the offset is lt 200 and origin is VALID and the user is the right one
     *
     * @throws Exception
     */
    @Test
    public void gwpAccessActionGWPClientCorrectUser2() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "gwp");

	setWebRequest(message, "wms", "https://www.geoportal.org");

	setOffset(message, 50);

	setView(message, "geoss", "geoss");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(true, authorized);
    }

    /**
     * @throws Exception
     */
    @Test
    public void gwpAccessActionGWPClientCorrectUser4_2() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "gwp");

	setWebRequest(message, "wms", "https://geoss.uat.esaportal.eu");

	setOffset(message, 300);

	setView(message, "geoss", "geoss");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(true, authorized);
    }

    /**
     * Permits because the offset is gt 200 and origin is VALID and the user is the right one (view is set
     * but ignored by the policy)
     *
     * @throws Exception
     */
    @Test
    public void gwpAccessActionGWPClientCorrectUser5_1() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "gwp");

	setWebRequest(message, "wms", "https://geoss.uat.esaportal.eu");

	setOffset(message, 300);

	setView(message, "pippo", "geoss");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(true, authorized);
    }

    /**
     * Denied because the path is not supported
     *
     * @throws Exception
     */
    @Test
    public void gwpAccessActionGWPClientCorrectUserWrongPath() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "gwp");

	setWebRequest(message, "xxx", "https://geoss.uat.esaportal.eu");

	setOffset(message, 300);

	setView(message, "pippo");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Denied because the path is not suported
     *
     * @throws Exception
     */
    @Test
    public void gwpDiscoveryActionGWPClientCorrectUserWrongPath() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "gwp");

	setWebRequest(message, "xxx", "https://geoss.uat.esaportal.eu");

	setOffset(message, 300);

	setView(message, "pippo");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny because the offset is gt 200 and no view is set
     *
     * @throws Exception
     */
    @Test
    public void gwpAccessActionNotGWPCreator1() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "gwp");

	setWebRequest(message, "rest");

	setOffset(message, 201);

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny because the offset is gt 200 and view is not geoss
     *
     * @throws Exception
     */
    @Test
    public void gwpAccessActionNotGWPCreator2() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "gwp");

	setWebRequest(message, "sos");

	setOffset(message, 201);

	setView(message, null, "pippo");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny because the offset is lt 200, view is not geoss but path is not admitted
     *
     * @throws Exception
     */
    @Test
    public void gwpAccessActionNotGWPCreator4() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "gwp");

	setWebRequest(message, "xxx");

	setOffset(message, 200);

	setView(message, null, "pippo");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny because the offset is gt 200, the view is geoss but the path is not admitted
     *
     * @throws Exception
     */
    @Test
    public void gwpAccessActionGWPCreator7() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "gwp");

	setWebRequest(message, "xxx");

	setOffset(message, 300);

	setView(message, null, "geoss");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }
}
