/**
 * 
 */
package eu.essi_lab.test.authorization.userfinder.test;

import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.authorization.userfinder.KeycloakUserMapper;
import eu.essi_lab.authorization.userfinder.KeycloakUsersManager;
import eu.essi_lab.lib.net.keycloak.KeycloakUser;
import eu.essi_lab.lib.net.keycloak.KeycloakUser.UserProfileAttribute;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.auth.GSUser;

/**
 * @author Fabrizio
 */
public class KeycloakUserMapperTest {

    @Test
    public void test() throws UnsupportedEncodingException, JAXBException {

	GSUser gsUser = new GSUser();

	gsUser.setAuthProvider(KeycloakUsersManager.KEYCLOAK_TYPE);
	gsUser.setEnabled(false);
	gsUser.setIdentifier("http://xxx");

	gsUser.setRole(GSUser.ROLE);
	gsUser.setPropertyValue(UserProfileAttribute.FIRST_NAME.getAttribute(), "pippo");
	gsUser.setPropertyValue(UserProfileAttribute.LAST_NAME.getAttribute(), "foo");
	gsUser.setPropertyValue(UserProfileAttribute.EMAIL.getAttribute(), "xxx@gmail.com");

	gsUser.setPropertyValue("registrationDate", "2024-11-26T07:56:58Z");
	gsUser.setPropertyValue("institutionType", "nmhs");
	gsUser.setPropertyValue("position", "Hydrologist");
	gsUser.setPropertyValue("identifierType", "userTokenIdentifier");
	gsUser.setPropertyValue("userTokenIdentifier", "xxxx-bdecc34e-8c88-4bc3-9cbd-4e229dfa903f");
	gsUser.setPropertyValue("country", "kenya");

	//
	//
	//

	KeycloakUser keycloakUser = KeycloakUserMapper.toKeycloakUser(gsUser);

	Assert.assertEquals(gsUser.isEnabled(), keycloakUser.isEnabled());

	// keycloak users id is automatically set at creation time
	Assert.assertTrue(keycloakUser.getIdentifier().isEmpty());

	// gsuser id is mapped to the keycloak user username
	Assert.assertEquals(gsUser.getIdentifier(), keycloakUser.getUserProfileAttribute(UserProfileAttribute.USERNAME).get());

	Assert.assertEquals(gsUser.getAuthProvider(), keycloakUser.getAttribute(GSUser.AUTH_PROVIDER).get().getValue().get(0));

	Assert.assertEquals(gsUser.getRole(), keycloakUser.getAttribute(GSUser.ROLE).get().getValue().get(0));

	Assert.assertEquals(gsUser.getStringPropertyValue(UserProfileAttribute.FIRST_NAME.getAttribute()).get(),
		keycloakUser.getUserProfileAttribute(UserProfileAttribute.FIRST_NAME).get());

	Assert.assertEquals(gsUser.getStringPropertyValue(UserProfileAttribute.LAST_NAME.getAttribute()).get(),
		keycloakUser.getUserProfileAttribute(UserProfileAttribute.LAST_NAME).get());

	Assert.assertEquals(gsUser.getStringPropertyValue(UserProfileAttribute.EMAIL.getAttribute()).get(),
		keycloakUser.getUserProfileAttribute(UserProfileAttribute.EMAIL).get());

	Assert.assertEquals(gsUser.getStringPropertyValue("registrationDate").get(),
		keycloakUser.getAttributeValue("registrationDate").get());

	Assert.assertEquals(gsUser.getStringPropertyValue("institutionType").get(),
		keycloakUser.getAttributeValue("institutionType").get());

	Assert.assertEquals(gsUser.getStringPropertyValue("position").get(), keycloakUser.getAttributeValue("position").get());

	Assert.assertEquals(gsUser.getStringPropertyValue("identifierType").get(), keycloakUser.getAttributeValue("identifierType").get());

	Assert.assertEquals(gsUser.getStringPropertyValue("userTokenIdentifier").get(),
		keycloakUser.getAttributeValue("userTokenIdentifier").get());

	Assert.assertEquals(gsUser.getStringPropertyValue("country").get(), keycloakUser.getAttributeValue("country").get());

	//
	//
	//

	GSUser gsUser2 = KeycloakUserMapper.toGSUser(keycloakUser);

	Assert.assertEquals(gsUser.isEnabled(), gsUser2.isEnabled());
	Assert.assertEquals(gsUser.getIdentifier(), gsUser2.getIdentifier());
	Assert.assertEquals(gsUser.getAuthProvider(), gsUser2.getAuthProvider());
	Assert.assertEquals(gsUser.getRole(), gsUser2.getRole());
	Assert.assertEquals(gsUser.getUri(), gsUser2.getUri());

	Assert.assertEquals(gsUser.getUserIdentifierType(), gsUser2.getUserIdentifierType());
	Assert.assertEquals(//
		gsUser.getProperties().stream().sorted((p1, p2) -> p1.getName().compareTo(p2.getName())).collect(Collectors.toList()),
		gsUser2.getProperties().stream().sorted((p1, p2) -> p1.getName().compareTo(p2.getName())).collect(Collectors.toList()));

	Assert.assertEquals(gsUser2.isEnabled(), keycloakUser.isEnabled());

	Assert.assertEquals(gsUser2.getIdentifier(), keycloakUser.getUserProfileAttribute(UserProfileAttribute.USERNAME).get());

	Assert.assertEquals(gsUser2.getAuthProvider(), keycloakUser.getAttribute(GSUser.AUTH_PROVIDER).get().getValue().get(0));

	Assert.assertEquals(gsUser2.getRole(), keycloakUser.getAttribute(GSUser.ROLE).get().getValue().get(0));

	Assert.assertEquals(gsUser2.getStringPropertyValue(UserProfileAttribute.FIRST_NAME.getAttribute()).get(),
		keycloakUser.getUserProfileAttribute(UserProfileAttribute.FIRST_NAME).get());

	Assert.assertEquals(gsUser2.getStringPropertyValue(UserProfileAttribute.LAST_NAME.getAttribute()).get(),
		keycloakUser.getUserProfileAttribute(UserProfileAttribute.LAST_NAME).get());

	Assert.assertEquals(gsUser2.getStringPropertyValue(UserProfileAttribute.EMAIL.getAttribute()).get(),
		keycloakUser.getUserProfileAttribute(UserProfileAttribute.EMAIL).get());

	Assert.assertEquals(gsUser2.getStringPropertyValue("registrationDate").get(),
		keycloakUser.getAttributeValue("registrationDate").get());

	Assert.assertEquals(gsUser2.getStringPropertyValue("institutionType").get(),
		keycloakUser.getAttributeValue("institutionType").get());

	Assert.assertEquals(gsUser2.getStringPropertyValue("position").get(), keycloakUser.getAttributeValue("position").get());

	Assert.assertEquals(gsUser2.getStringPropertyValue("identifierType").get(), keycloakUser.getAttributeValue("identifierType").get());

	Assert.assertEquals(gsUser2.getStringPropertyValue("userTokenIdentifier").get(),
		keycloakUser.getAttributeValue("userTokenIdentifier").get());

	Assert.assertEquals(gsUser2.getStringPropertyValue("country").get(), keycloakUser.getAttributeValue("country").get());
    }
}
