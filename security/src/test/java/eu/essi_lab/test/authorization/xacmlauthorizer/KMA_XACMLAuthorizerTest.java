/**
 * 
 */
package eu.essi_lab.test.authorization.xacmlauthorizer;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class KMA_XACMLAuthorizerTest extends AbstractXACMLAuthorizerTest {

    /**
     * View is not required
     * Path is KO
     * 
     * @throws GSException
     * @throws Exception
     */
    @Test
    public void kmaUser1DiscoveryTest1() throws GSException, Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "kma");

	setView(message, "geoss");
	setWebRequest(message, "opensearch");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * @throws GSException
     * @throws Exception
     */
    @Test
    public void kmaUser1DiscoveryTest2() throws GSException, Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "kma");

	setWebRequest(message, "csw", "218.154.54.13", null);

	setView(message, "KMA");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(true, authorized);
    }

    /**
     * @throws GSException
     * @throws Exception
     */
    @Test
    public void kmaUser1DiscoveryMultipleIPTest1() throws GSException, Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "kma");

	setView(message, "KMA");

	setWebRequest(message, //
		"csw", //
		"10.0.0.157", // load balancer IP
		null, //
		"221.151.118.17, 93.57.245.45", // x-forwarder-for headers IP (the first is allowed)
		null);

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(true, authorized);
    }

    /**
     * Path is OK, IP no
     * 
     * @throws GSException
     * @throws Exception
     */
    @Test
    public void kmaUser1DiscoveryNotAllowedIPTest2() throws GSException, Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "kma");

	setWebRequest(message, "csw", "218.154.54.11", null);

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * @throws GSException
     * @throws Exception
     */
    @Test
    public void kmaUser1DiscoveryTest3() throws GSException, Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "kma");

	setWebRequest(message, "kmaoaipmh", "218.154.54.13", null);

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * @throws GSException
     * @throws Exception
     */
    @Test
    public void kmaUser1AccessTest() throws GSException, Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "kma");

	setWebRequest(message, "kma");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * View is not required
     * Path is KO
     * 
     * @throws GSException
     * @throws Exception
     */
    @Test
    public void kmaUser2DiscoveryTest1() throws GSException, Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "kma");

	setView(message, "geoss");
	setWebRequest(message, "opensearch");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * @throws GSException
     * @throws Exception
     */
    @Test
    public void kmaUser2AccessTest() throws GSException, Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "kma");

	setWebRequest(message, "kma");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * clientRule ----> fails
     * ipAndPathRule -> indeterminate because ip is null
     * -
     * result --------> not authorized
     * -
     * Note: since clientRule is tested as first and fails, also ipAndPathRule is tested. if an ip not provided
     * an error occurs since because the rule condition requires 2 args, path and id. in this case the result
     * is indeterminate and it not impacts in the result
     * 
     * @throws GSException
     * @throws Exception
     */
    @Test
    public void kmaUser1BothRuleTest2_1() throws GSException, Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "kma");

	setWebRequest(message, "csw", null, "pippo");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * clientRule ----> fails
     * ipAndPathRule -> fails
     * -
     * result --------> not authorized
     *
     * @throws GSException
     * @throws Exception
     */
    @Test
    public void kmaUser1BothRuleTest4() throws GSException, Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "kma");

	setWebRequest(message, "kmaoaipmhXXX", "210.156.50.10", "pippo");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * clientRule ----> fails
     * ipAndPathRule -> not tested (but id would be indeterminate because path is missing)
     * -
     * result --------> not authorized
     * 
     * @throws GSException
     * @throws Exception
     */
    @Test
    public void kmaUser1BothRuleTest4_2() throws GSException, Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "kma");

	setWebRequest(message, null, "210.156.50.10", "pippo");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * @throws GSException
     * @throws Exception
     */
    @Test
    public void kmaUser1BothRuleTest6() throws GSException, Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "kma");

	setWebRequest(message, "kmaoaipmh", "218.154.54.13", "pippo");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }
}
