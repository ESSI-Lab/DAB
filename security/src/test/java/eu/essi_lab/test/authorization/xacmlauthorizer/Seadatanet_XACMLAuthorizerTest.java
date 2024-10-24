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
public class Seadatanet_XACMLAuthorizerTest extends AbstractXACMLAuthorizerTest {

    @Test
    public void seadatanetDiscoveryActionNotseadatanetCreator1() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "seadatanet");

	setWebRequest(message, "csw");

	setOffset(message, 201);

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny because the offset is gt 200 and creator is not seadatanet
     *
     * @throws Exception
     */
    @Test
    public void seadatanetDiscoveryActionNotseadatanetCreator2() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "seadatanet");

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
    public void seadatanetDiscoveryActionNotseadatanetCreator3() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "seadatanet");

	setWebRequest(message, "cuahsi_1_1.asmx");

	setOffset(message, 200);

	setView(message, null, "pippo");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny because the offset is lt 200, creator is not seadatanet but path is not admitted
     *
     * @throws Exception
     */
    @Test
    public void seadatanetDiscoveryActionNotseadatanetCreator4() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "seadatanet");

	setWebRequest(message, "xxx");

	setOffset(message, 200);

	setView(message, null, "pippo");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Permits because the offset is gt 200 and the creator is seadatanet
     *
     * @throws Exception
     */
    @Test
    public void seadatanetDiscoveryActionNotseadatanetCreator5() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "seadatanet");

	setWebRequest(message, "csw");

	setOffset(message, 300);

	setView(message, null, "seadatanet");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(true, authorized);
    }

    /**
     * Permits because the offset is lt 200 and the creator is seadatanet
     *
     * @throws Exception
     */
    @Test
    public void seadatanetDiscoveryActionNotseadatanetCreator6() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "seadatanet");

	setWebRequest(message, "hydrocsv");

	setOffset(message, 1);

	setView(message, null, "seadatanet");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(true, authorized);
    }

    /**
     * Deny because the offset is gt 200, the creator is seadatanet but the path is not admitted
     *
     * @throws Exception
     */
    @Test
    public void seadatanetDiscoveryActionNotseadatanetCreator7() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "seadatanet");

	setWebRequest(message, "xxx");

	setOffset(message, 300);

	setView(message, null, "seadatanet");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny because the offset is gt 200 and no creator is set
     *
     * @throws Exception
     */
    @Test
    public void seadatanetAccessActionNotseadatanetCreator1() throws Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "seadatanet");

	setWebRequest(message, "rest");

	setOffset(message, 201);

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny because the offset is gt 200 and creator is not seadatanet
     *
     * @throws Exception
     */
    @Test
    public void seadatanetAccessActionNotseadatanetCreator2() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "seadatanet");

	setWebRequest(message, "sos");

	setOffset(message, 201);

	setView(message, null, "pippo");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Permits because the offset is lt 200 and creator is not seadatanet
     *
     * @throws Exception
     */
    @Test
    public void seadatanetAccessActionNotseadatanetCreator3() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "seadatanet");

	setWebRequest(message, "wms");

	setOffset(message, 200);

	setView(message, "seadatanet", "seadatanet");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(true, authorized);
    }

    /**
     * Deny because the offset is lt 200, creator is not seadatanet but path is not admitted
     *
     * @throws Exception
     */
    @Test
    public void seadatanetAccessActionNotseadatanetCreator4() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "seadatanet");

	setWebRequest(message, "xxx");

	setOffset(message, 200);

	setView(message, null, "pippo");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

    /**
     * Permits because the offset is gt 200 and the creator is seadatanet
     *
     * @throws Exception
     */
    @Test
    public void seadatanetAccessActionNotseadatanetCreator5() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "seadatanet");

	setWebRequest(message, "gwps");

	setOffset(message, 300);

	setView(message, null, "seadatanet");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(true, authorized);
    }

    /**
     * Permits because the offset is lt 200 and the creator is seadatanet
     *
     * @throws Exception
     */
    @Test
    public void seadatanetAccessActionNotseadatanetCreator6() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "seadatanet");

	setWebRequest(message, "hydrocsv");

	setOffset(message, 1);

	setView(message, null, "seadatanet");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(true, authorized);
    }

    /**
     * Deny because the offset is gt 200, the creator is seadatanet but the path is not admitted
     *
     * @throws Exception
     */
    @Test
    public void seadatanetAccessActionNotseadatanetCreator7() throws Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "seadatanet");

	setWebRequest(message, "xxx");

	setOffset(message, 300);

	setView(message, null, "seadatanet");

	boolean authorized = isAuthorized(message);
	Assert.assertEquals(false, authorized);
    }

}
