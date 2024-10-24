/**
 * 
 */
package eu.essi_lab.test.authorization.xacmlauthorizer;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class Anonymous_XACMLAuthorizerTest extends AbstractXACMLAuthorizerTest {

    /**
     * @throws GSException
     * @throws Exception
     */
    @Test
    public void anonymousUserAccessTest() throws GSException, Exception {

	AccessMessage message = new AccessMessage();

	setView(message, "default");
	setWebRequest(message, "kma");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * @throws GSException
     * @throws Exception
     */
    @Test
    public void anonymousUserDiscoveryTest() throws GSException, Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setWebRequest(message, "opensearch");
	setOffset(message, 1);

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(true, authorized);
    }

    /**
     * @throws GSException
     * @throws Exception
     */
    @Test
    public void anonymousUserDiscoveryTest2() throws GSException, Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setWebRequest(message, "opensearch");
	setOffset(message, 201);

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * This is authorized due to the ESSI-Lab client id
     * 
     * @throws GSException
     * @throws Exception
     */
    @Test
    public void anonymousUserDiscoveryTest3() throws GSException, Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setWebRequest(message, "opensearch", null, WebRequest.ESSI_LAB_CLIENT_IDENTIFIER);
	setOffset(message, 501);

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(true, authorized);
    }

    /**
     * anonymous user wants access
     *
     * @throws Exception
     */
    @Test
    public void anonymousAccessTest1() throws Exception {

	AccessMessage message = new AccessMessage();

	setWebRequest(message, "cuahsi_1_1.asmx");

	setOffset(message, 1);

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(true, authorized);
    }

    /**
     * anonymous user wants access
     *
     * @throws Exception
     */
    @Test
    public void anonymousAccessTest2() throws Exception {

	AccessMessage message = new AccessMessage();

	setWebRequest(message, "cuahsi_1_1.asmx");

	setOffset(message, 201);

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * anonymous user wants access
     *
     * @throws Exception
     */
    @Test
    public void anonymousAccessTest3() throws Exception {

	AccessMessage message = new AccessMessage();

	setWebRequest(message, "opensearch");

	setOffset(message, 1);

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }
}
