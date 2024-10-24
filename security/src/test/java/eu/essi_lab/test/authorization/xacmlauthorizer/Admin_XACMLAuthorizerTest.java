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
public class Admin_XACMLAuthorizerTest extends AbstractXACMLAuthorizerTest {

    @Test
    public void adminDiscoveryTest1() throws GSException, Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "admin");
	setWebRequest(message, "opensearch");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(true, authorized);
    }

    @Test
    public void adminDiscoveryTest2() throws GSException, Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "admin");
	setWebRequest(message, "opensearch");
	setOffset(message, 1000);

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(true, authorized);
    }

    @Test
    public void adminDiscoveryTest4() throws GSException, Exception {

	DiscoveryMessage message = new DiscoveryMessage();

	setUser(message, "admin");
	setWebRequest(message, "opensearch");
	setOffset(message, 1000);
	setView(message, "aViewId");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(true, authorized);
    }

    @Test
    public void adminAccessTest1() throws GSException, Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "admin");
	setWebRequest(message, "csw");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(true, authorized);
    }

    @Test
    public void adminAccessTest2() throws GSException, Exception {

	AccessMessage message = new AccessMessage();

	setUser(message, "admin");
	setWebRequest(message, "opensearch");
	setView(message, "aViewId");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(true, authorized);
    }
}
