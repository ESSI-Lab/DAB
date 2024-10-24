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
public class WHOS_XACMLAuthorizerTest extends AbstractXACMLAuthorizerTest {

    /**
     * Deny because the offset is gt 200 and no creator is set
     *
     * @throws Exception
     */
    @Test
    public void whosDiscoveryActionNotWhosCreator1() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "whos");

	setWebRequest(message, "csw");

	setOffset(message, 201);

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny because the offset is gt 200 and creator is not whos
     *
     * @throws Exception
     */
    @Test
    public void whosDiscoveryActionNotWhosCreator2() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "whos");

	setWebRequest(message, "csw");

	setOffset(message, 201);

	setView(message, null, "pippo");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * @throws Exception
     */
    @Test
    public void whosDiscoveryActionNotWhosCreator3() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "whos");

	setWebRequest(message, "cuahsi_1_1.asmx");

	setOffset(message, 200);

	setView(message, null, "whos");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(true, authorized);
    }

    /**
     * Deny because the offset is lt 200, creator is not whos but path is not admitted
     *
     * @throws Exception
     */
    @Test
    public void whosDiscoveryActionNotWhosCreator4() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "whos");

	setWebRequest(message, "xxx");

	setOffset(message, 200);

	setView(message, null, "pippo");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Permits because the offset is gt 200 and the creator is whos
     *
     * @throws Exception
     */
    @Test
    public void whosDiscoveryActionNotWhosCreator5() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "whos");

	setWebRequest(message, "csw");

	setOffset(message, 300);

	setView(message, null, "whos");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(true, authorized);
    }

    /**
     * Permits because the offset is lt 200 and the creator is whos
     *
     * @throws Exception
     */
    @Test
    public void whosDiscoveryActionNotWhosCreator6() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "whos");

	setWebRequest(message, "hydrocsv");

	setOffset(message, 1);

	setView(message, null, "whos");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(true, authorized);
    }

    /**
     * Deny because the offset is gt 200, the creator is whos but the path is not admitted
     *
     * @throws Exception
     */
    @Test
    public void whosDiscoveryActionNotWhosCreator7() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "whos");

	setWebRequest(message, "xxx");

	setOffset(message, 300);

	setView(message, null, "whos");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny because the offset is gt 200 and no creator is set
     *
     * @throws Exception
     */
    @Test
    public void whosAccessActionNotWhosCreator1() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "whos");

	setWebRequest(message, "rest");

	setOffset(message, 201);

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny because the offset is gt 200 and creator is not whos
     *
     * @throws Exception
     */
    @Test
    public void whosAccessActionNotWhosCreator2() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "whos");

	setWebRequest(message, "sos");

	setOffset(message, 201);

	setView(message, null, "pippo");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * @throws Exception
     */
    @Test
    public void whosAccessActionNotWhosCreator3() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "whos");

	setWebRequest(message, "wms");

	setOffset(message, 200);

	setView(message, null, "pippo");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny because the offset is lt 200, creator is not whos but path is not admitted
     *
     * @throws Exception
     */
    @Test
    public void whosAccessActionNotWhosCreator4() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "whos");

	setWebRequest(message, "xxx");

	setOffset(message, 200);

	setView(message, null, "pippo");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Permits because the offset is gt 200 and the creator is whos
     *
     * @throws Exception
     */
    @Test
    public void whosAccessActionNotWhosCreator5() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "whos");

	setWebRequest(message, "gwps");

	setOffset(message, 300);

	setView(message, null, "whos");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(true, authorized);
    }

    /**
     * Permits because the offset is lt 200 and the creator is whos
     *
     * @throws Exception
     */
    @Test
    public void whosAccessActionNotWhosCreator6() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "whos");

	setWebRequest(message, "hydrocsv");

	setOffset(message, 1);

	setView(message, null, "whos");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(true, authorized);
    }

    /**
     * Deny because the offset is gt 200, the creator is whos but the path is not admitted
     *
     * @throws Exception
     */
    @Test
    public void whosAccessActionNotWhosCreator7() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "whos");

	setWebRequest(message, "xxx");

	setOffset(message, 300);

	setView(message, null, "whos");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

}
