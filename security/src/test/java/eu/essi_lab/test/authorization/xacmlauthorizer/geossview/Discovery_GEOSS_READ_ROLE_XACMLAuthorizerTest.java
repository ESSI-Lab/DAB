/**
 * 
 */
package eu.essi_lab.test.authorization.xacmlauthorizer.geossview;

import java.io.IOException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.authorization.rps.GEOSSReadRolePolicySet;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.bond.View.ViewVisibility;
import eu.essi_lab.test.authorization.xacmlauthorizer.AbstractXACMLAuthorizerTest;

/**
 * @author Fabrizio
 */
public class Discovery_GEOSS_READ_ROLE_XACMLAuthorizerTest extends AbstractXACMLAuthorizerTest {

    /**
     * Permit:
     * - discovery path OK
     * - view creator OK
     * - view visibility public
     * - view owner ignored, since the view visibility is public
     *
     * @throws IOException
     */
    @Test
    public void permitTest1() throws Exception {

	RequestMessage message = getMessage();

	setUser(message, getRole(), "pippo");

	setWebRequest(message, getPath());

	setView(message, UUID.randomUUID().toString(), "geoss", ViewVisibility.PUBLIC, "pluto");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(true, authorized);
    }

    /**
     * Deny:
     * - discovery path NOT supported
     * - view creator OK
     * - view visibility public
     * - view owner not required, since the view visibility is public
     *
     * @throws IOException
     */
    @Test
    public void denyTest1() throws Exception {

	RequestMessage message = getMessage();

	setUser(message, getRole(), "pippo");

	setWebRequest(message, "xxx");

	setView(message, UUID.randomUUID().toString(), "geoss", ViewVisibility.PUBLIC, "pippo");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny:
     * - discovery path OK
     * - view creator NOT supported
     * - view visibility public
     * - view owner not required, since the view visibility is public
     *
     * @throws IOException
     */
    @Test
    public void denyTest2() throws Exception {

	RequestMessage message = getMessage();

	setUser(message, getRole(), "pippo");

	setWebRequest(message, getPath());

	setView(message, UUID.randomUUID().toString(), "xxx", ViewVisibility.PUBLIC, "pippo");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny:
     * - discovery path OK
     * - view creator OK
     * - view visibility private
     * - view owner and user id DO NOT MATCH
     *
     * @throws IOException
     */
    @Test
    public void denyTest3() throws Exception {

	RequestMessage message = getMessage();

	setUser(message, getRole(), "pippo");

	setWebRequest(message, getPath());

	setView(message, UUID.randomUUID().toString(), "geoss", ViewVisibility.PRIVATE, "pluto");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * Permit:
     * - discovery path OK
     * - view creator OK
     * - view visibility private
     * - view owner and user id match
     *
     * @throws IOException
     */
    @Test
    public void permitTest2() throws Exception {

	RequestMessage message = getMessage();

	setWebRequest(message, getPath());

	setUser(message, getRole(), "pippo");

	setView(message, UUID.randomUUID().toString(), "geoss", ViewVisibility.PRIVATE, "pippo");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(true, authorized);
    }

    /**
     * Deny:
     * - discovery path OK
     * - view creator NOT SUPPORTED
     * - view visibility private
     * - view owner and user id match
     *
     * @throws IOException
     */
    @Test
    public void denyTest4() throws Exception {

	RequestMessage message = getMessage();

	setWebRequest(message, getPath());

	setUser(message, getRole(), "pippo");

	setView(message, UUID.randomUUID().toString(), "xxx", ViewVisibility.PRIVATE, "pippo");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * @return
     */
    protected String getPath() {

	return "csw";
    }

    /**
     * @return
     */
    protected RequestMessage getMessage() {

	return new DiscoveryMessage();
    }

    /**
     * @return
     */
    protected String getRole() {

	return GEOSSReadRolePolicySet.ROLE;
    }

}
