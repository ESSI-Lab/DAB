package eu.essi_lab.test.authorization.xacmlauthorizer.geossview;

import java.io.IOException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.authorization.pps.GEOSSWritePermissionPolicySet;
import eu.essi_lab.authorization.rps.GEOSSPrivateWriteRolePolicySet;
import eu.essi_lab.authorization.rps.GEOSSReadRolePolicySet;
import eu.essi_lab.authorization.rps.GEOSSWriteRolePolicySet;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.bond.View.ViewVisibility;
import eu.essi_lab.messages.view.CreateViewMessage;
import eu.essi_lab.test.authorization.xacmlauthorizer.AbstractXACMLAuthorizerTest;

/**
 * @author Fabrizio
 */
public class CreateView_XACMLAuthorizerTest extends AbstractXACMLAuthorizerTest {

    /**
     * Permit:
     * - Role: "geoss-private-write"
     * - path OK
     * - view creator OK
     * - view visibility private
     * - view owner and user id ignored by the creation rule
     *
     * @throws IOException
     */
    @Test
    public void permitTest1() throws Exception {

	RequestMessage message = new CreateViewMessage();

	setUser(message, GEOSSPrivateWriteRolePolicySet.ROLE, "pippo");

	setWebRequest(message, GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	setView(message, UUID.randomUUID().toString(), "geoss", ViewVisibility.PRIVATE, "pippo");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(true, authorized);
    }

    /**
     * Deny:
     * - Role: "geoss-read"
     * - path OK
     * - view creator OK
     * - view visibility private
     * - view owner and user id ignored by the creation rule
     *
     * @throws IOException
     */
    @Test
    public void denyTest1_1() throws Exception {

	RequestMessage message = new CreateViewMessage();

	setUser(message, GEOSSReadRolePolicySet.ROLE, "pippo");

	setWebRequest(message, GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	setView(message, UUID.randomUUID().toString(), "geoss", ViewVisibility.PRIVATE, "pippo");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * Permit:
     * - Role: "geoss-private-write"
     * - path OK
     * - view creator OK
     * - view visibility private
     * - view owner and user id ignored by the creation rule
     *
     * @throws IOException
     */
    @Test
    public void permitTest2() throws Exception {

	RequestMessage message = new CreateViewMessage();

	setUser(message, GEOSSPrivateWriteRolePolicySet.ROLE, "pippo");

	setWebRequest(message, GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	setView(message, UUID.randomUUID().toString(), "geoss", ViewVisibility.PRIVATE, "pluto");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(true, authorized);
    }

    /**
     * Deny:
     * - Role: "geoss-read"
     * - path OK
     * - view creator OK
     * - view visibility private
     * - view owner and user id ignored by the creation rule
     *
     * @throws IOException
     */
    @Test
    public void denyTest2_2() throws Exception {

	RequestMessage message = new CreateViewMessage();

	setUser(message, GEOSSReadRolePolicySet.ROLE, "pippo");

	setWebRequest(message, GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	setView(message, UUID.randomUUID().toString(), "geoss", ViewVisibility.PRIVATE, "pluto");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * Permit:
     * - Role: "geoss-write"
     * - path OK
     * - view creator OK
     * - view visibility private
     * - view owner and user id ignored by the creation rule
     *
     * @throws IOException
     */
    @Test
    public void permitTest3() throws Exception {

	RequestMessage message = new CreateViewMessage();

	setUser(message, GEOSSWriteRolePolicySet.ROLE, "pippo");

	setWebRequest(message, GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	setView(message, UUID.randomUUID().toString(), "geoss", ViewVisibility.PRIVATE, "pippo");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(true, authorized);
    }

    /**
     * Permit:
     * - Role: "geoss-write"
     * - path OK
     * - view creator OK
     * - view visibility public
     * - view owner and user id ignored by the creation rule
     *
     * @throws IOException
     */
    @Test
    public void permitTest4() throws Exception {

	RequestMessage message = new CreateViewMessage();

	setUser(message, GEOSSWriteRolePolicySet.ROLE, "pippo");

	setWebRequest(message, GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	setView(message, UUID.randomUUID().toString(), "geoss", ViewVisibility.PUBLIC, "pippo");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(true, authorized);
    }

    /**
     * Deny:
     * - Role: "geoss-private-write"
     * - path OK
     * - view creator NOT SUPPORTED
     * - view visibility private
     * - view owner and user id ignored by the creation rule
     *
     * @throws IOException
     */
    @Test
    public void denyTest2() throws Exception {

	RequestMessage message = new CreateViewMessage();

	setUser(message, GEOSSPrivateWriteRolePolicySet.ROLE, "pippo");

	setWebRequest(message, GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	setView(message, UUID.randomUUID().toString(), "xx", ViewVisibility.PRIVATE, "pippo");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny:
     * - Role: "geoss-write"
     * - path OK
     * - view creator NOT SUPPORTED
     * - view visibility private
     * - view owner and user id ignored by the creation rule
     *
     * @throws IOException
     */
    @Test
    public void denyTest3() throws Exception {

	RequestMessage message = new CreateViewMessage();

	setUser(message, GEOSSWriteRolePolicySet.ROLE, "pippo");

	setWebRequest(message, GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	setView(message, UUID.randomUUID().toString(), "xx", ViewVisibility.PRIVATE, "pippo");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny:
     * - Role: "geoss-private-write"
     * - path NOT SUPPORTED
     * - view creator OK
     * - view visibility private
     * - view owner and user id ignored by the creation rule
     *
     * @throws IOException
     */
    @Test
    public void denyTest4() throws Exception {

	RequestMessage message = new CreateViewMessage();

	setUser(message, GEOSSPrivateWriteRolePolicySet.ROLE, "pippo");

	setWebRequest(message, "csw");

	setView(message, UUID.randomUUID().toString(), "geoss", ViewVisibility.PRIVATE, "pippo");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny:
     * - Role: "geoss-write"
     * - path NOT SUPPORTED
     * - view creator OK
     * - view visibility private
     * - view owner and user id ignored by the creation rule
     *
     * @throws IOException
     */
    @Test
    public void denyTest5() throws Exception {

	RequestMessage message = new CreateViewMessage();

	setUser(message, GEOSSWriteRolePolicySet.ROLE, "pippo");

	setWebRequest(message, "csw");

	setView(message, UUID.randomUUID().toString(), "geoss", ViewVisibility.PRIVATE, "pippo");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }

    /**
     * Deny:
     * - Role: "geoss-private-write"
     * - path OK
     * - view creator OK
     * - view visibility public, must be private
     * - view owner and user id ignored by the creation rule
     *
     * @throws IOException
     */
    @Test
    public void denyTest6() throws Exception {

	RequestMessage message = new CreateViewMessage();

	setUser(message, GEOSSPrivateWriteRolePolicySet.ROLE, "pippo");

	setWebRequest(message, GEOSSWritePermissionPolicySet.SUPPORTED_PATH);

	setView(message, UUID.randomUUID().toString(), "geoss", ViewVisibility.PUBLIC, "pippo");

	boolean authorized = isAuthorized(message);

	Assert.assertEquals(false, authorized);
    }
}
