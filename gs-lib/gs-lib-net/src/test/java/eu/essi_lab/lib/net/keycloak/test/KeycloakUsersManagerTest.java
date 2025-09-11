/**
 * 
 */
package eu.essi_lab.lib.net.keycloak.test;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.lib.net.keycloak.KeycloakUser;
import eu.essi_lab.lib.net.keycloak.KeycloakUsersClient;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.net.keycloak.KeycloakUser.UserProfileAttribute;

/**
 * @author Fabrizio
 */
public class KeycloakUsersManagerTest {

    private KeycloakUsersClient manager;

    @Before
    public void before() throws IOException, InterruptedException {

	manager = new KeycloakUsersClient();
	manager.setServiceUrl("http://localhost:8080");
	manager.setAdminPassword(System.getProperty("keycloak.password"));
	manager.setAdminUser(System.getProperty("keycloak.user"));
	manager.setUsersRealm("testRealm");

	String accessToken = manager.getAccessToken();

	List<KeycloakUser> list = manager.list(accessToken);

	for (KeycloakUser user : list) {

	    manager.delete(accessToken, user.getIdentifier().get());
	}

	Assert.assertEquals(0, manager.count(accessToken));
    }

    /**
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {

	KeycloakUsersClient manager = new KeycloakUsersClient();
	manager.setServiceUrl("http://localhost:8080");
	manager.setAdminPassword(System.getProperty("keycloak.password"));
	manager.setAdminUser(System.getProperty("keycloak.user"));
	manager.setUsersRealm("testRealm");

	KeycloakUser user = new KeycloakUser.KeycloakUserBuilder().//
		enabled(false).//
		withUserProfileAttribute(UserProfileAttribute.USERNAME, "pluto").//
		withUserProfileAttribute(UserProfileAttribute.EMAIL, "pluto@gmail.com").//
		withUserProfileAttribute(UserProfileAttribute.FIRST_NAME, "Pluto").//
		withUserProfileAttribute(UserProfileAttribute.LAST_NAME, "De Plutis").//
		withAttribute("key1", "value1").//
		withAttribute("key2", "value2").//
		build();

	String accessToken = manager.getAccessToken();

	manager.create(accessToken, user);

	List<JSONObject> listRaw = manager.listRaw(accessToken);

	System.out.println(listRaw.get(0).toString(3));

    }

    @Test
    public void createAndCompareRawJSONTest() throws IOException, InterruptedException {

	KeycloakUser user = new KeycloakUser.KeycloakUserBuilder().//
		enabled(false).//
		withUserProfileAttribute(UserProfileAttribute.USERNAME, "pluto").//
		withUserProfileAttribute(UserProfileAttribute.EMAIL, "pluto@gmail.com").//
		withUserProfileAttribute(UserProfileAttribute.FIRST_NAME, "Pluto").//
		withUserProfileAttribute(UserProfileAttribute.LAST_NAME, "De Plutis").//
		withAttribute("key1", "value1").//
		withAttribute("key2", "value2").//
		build();

	String accessToken = manager.getAccessToken();

	//
	//
	// the JSON in the create request do not include several object/properties that are included
	// in the raw JSON; the missing object/properties are added by the server
	//
	//

	Assert.assertTrue(manager.create(accessToken, user));

	Assert.assertEquals(1, manager.count(accessToken));

	//
	//
	// the raw JSON object
	// it includes several other object/properties:
	//
	//
	// - "totp": false,
	// - "access": {"manage": true},
	// - "createdTimestamp": 1757578212956,
	// - "notBefore": 0,
	// - "disableableCredentialTypes": [],
	// - "emailVerified": false,
	// - "requiredActions": [],
	// - "userProfileMetadata": { ... }
	//
	// - "enabled": true,
	// - attributes : { ... }
	// - "id": "72a57bc7-75a7-4fca-84f2-c58250f6ec25",
	//
	// - "email": "mail@gmail.com",
	// - "username": "name",
	// - "lastName": "lastname",
	// - "firstName": "firstname",
	//
	//
	JSONObject rawJSON = manager.listRaw(accessToken).get(0);

	//
	// the JSON representation of the KeycloakUser has a subset of the raw JSON object/properties
	// that includes only some properties, the attributes and the user profile attributes:
	//
	// - "enabled": true,
	// - "createdTimestamp": 1757578212956,
	// - attributes : { ... }
	// - "id": "72a57bc7-75a7-4fca-84f2-c58250f6ec25",
	// - "email": "mail@gmail.com",
	// - "username": "name",
	// - "lastName": "lastname",
	// - "firstName": "firstname",
	//
	//
	//
	JSONObject simpleJSON = manager.list(accessToken).get(0).toJSON();

	//
	// common elements
	//

	Assert.assertEquals(rawJSON.getBoolean("enabled"), simpleJSON.getBoolean("enabled"));
	Assert.assertEquals(rawJSON.getString("id"), simpleJSON.getString("id"));
	Assert.assertEquals(rawJSON.getString("email"), simpleJSON.getString("email"));
	Assert.assertEquals(rawJSON.getString("username"), simpleJSON.getString("username"));
	Assert.assertEquals(rawJSON.getString("lastName"), simpleJSON.getString("lastName"));
	Assert.assertEquals(rawJSON.getString("firstName"), simpleJSON.getString("firstName"));
	Assert.assertEquals(rawJSON.getJSONObject("attributes").toString(), simpleJSON.getJSONObject("attributes").toString());

	Assert.assertNotNull(rawJSON.get("createdTimestamp")); // in the raw JSON is long
	Assert.assertNotNull(simpleJSON.get("createdTimestamp")); // in the simple JSON it is in ISO8601 format
	Assert.assertTrue(ISO8601DateTimeUtils.parseISO8601ToDate(simpleJSON.getString("createdTimestamp")).isPresent());

	//
	// elements only in the raw JSON
	//

	Assert.assertNotNull(rawJSON.opt("totp"));
	Assert.assertNotNull(rawJSON.opt("access"));
	Assert.assertNotNull(rawJSON.opt("notBefore"));
	Assert.assertNotNull(rawJSON.opt("disableableCredentialTypes"));
	Assert.assertNotNull(rawJSON.opt("emailVerified"));
	Assert.assertNotNull(rawJSON.opt("requiredActions"));

	//
	// missing elements in the simplified version
	//

	Assert.assertNull(simpleJSON.opt("totp"));
	Assert.assertNull(simpleJSON.opt("access"));
	Assert.assertNull(simpleJSON.opt("notBefore"));
	Assert.assertNull(simpleJSON.opt("disableableCredentialTypes"));
	Assert.assertNull(simpleJSON.opt("emailVerified"));
	Assert.assertNull(simpleJSON.opt("requiredActions"));
    }

    @Test
    public void createAndListTest() throws IOException, InterruptedException, RuntimeException {

	String accessToken = manager.getAccessToken();

	Assert.assertNotNull(accessToken);

	Assert.assertEquals(0, manager.count(accessToken));

	KeycloakUser newUser = new KeycloakUser.KeycloakUserBuilder().//
		withUserProfileAttribute(UserProfileAttribute.USERNAME, "pippo").//
		withUserProfileAttribute(UserProfileAttribute.EMAIL, "pippo@gmail.com").//
		withUserProfileAttribute(UserProfileAttribute.FIRST_NAME, "Pippo").//
		withUserProfileAttribute(UserProfileAttribute.LAST_NAME, "De' Pippis").//
		withAttribute("key0", "value00", "value01", "value02").//
		withAttribute("key1", "value1").//
		build();

	// invalid token
	Assert.assertFalse(manager.create(accessToken + "x", newUser));

	Assert.assertTrue(manager.create(accessToken, newUser));

	// username already exists
	Assert.assertFalse(manager.create(accessToken, newUser));

	//
	//
	//

	Assert.assertEquals(1, manager.count(accessToken));

	//
	//
	//

	KeycloakUser listedUser = manager.list(accessToken).get(0);

	Assert.assertNotNull(listedUser.getIdentifier());

	Assert.assertEquals(true, listedUser.isEnabled()); // default
	Assert.assertEquals("pippo", listedUser.getUserProfileAttribute(UserProfileAttribute.USERNAME).get());
	Assert.assertEquals("pippo@gmail.com", listedUser.getUserProfileAttribute(UserProfileAttribute.EMAIL).get());
	Assert.assertEquals("Pippo", listedUser.getUserProfileAttribute(UserProfileAttribute.FIRST_NAME).get());
	Assert.assertEquals("De' Pippis", listedUser.getUserProfileAttribute(UserProfileAttribute.LAST_NAME).get());

	Assert.assertEquals(2, listedUser.getAttributes().size());

	List<Entry<String, List<String>>> attributes = listedUser.getAttributes().stream()
		.sorted((attr1, attr2) -> attr1.getKey().compareTo(attr2.getKey())).collect(Collectors.toList());

	Entry<String, List<String>> entry0 = attributes.get(0);
	Assert.assertEquals("key0", entry0.getKey());
	Assert.assertEquals("value00", entry0.getValue().get(0));
	Assert.assertEquals("value01", entry0.getValue().get(1));
	Assert.assertEquals("value02", entry0.getValue().get(2));

	Entry<String, List<String>> entry1 = attributes.get(1);
	Assert.assertEquals("key1", entry1.getKey());
	Assert.assertEquals("value1", entry1.getValue().get(0));
    }

    @Test
    public void createAndUpdateTest() throws IOException, InterruptedException, RuntimeException {

	String accessToken = manager.getAccessToken();

	Assert.assertNotNull(accessToken);

	Assert.assertEquals(0, manager.count(accessToken));

	KeycloakUser newUser = new KeycloakUser.KeycloakUserBuilder().//
		enabled(false).//
		withUserProfileAttribute(UserProfileAttribute.USERNAME, "pippo").//

		withUserProfileAttribute(UserProfileAttribute.EMAIL, "pippo@gmail.com").//
		withUserProfileAttribute(UserProfileAttribute.FIRST_NAME, "Pippo").//
		withUserProfileAttribute(UserProfileAttribute.LAST_NAME, "De' Pippis").//

		withAttribute("key0", "value00", "value01", "value02").//
		withAttribute("key1", "value1").//
		build();

	Assert.assertTrue(manager.create(accessToken, newUser));

	KeycloakUser listedNewUser = manager.list(accessToken).get(0);

	//
	// attempt to update the user name
	//

	KeycloakUser notUpdatedUser = new KeycloakUser.KeycloakUserBuilder().//
		enabled(true).//
		withIdentifier(listedNewUser.getIdentifier().get()).//

		withUserProfileAttribute(UserProfileAttribute.USERNAME, "pippo2").// username cannot be modified!

		withUserProfileAttribute(UserProfileAttribute.EMAIL, "pippo2@gmail.com").//
		withUserProfileAttribute(UserProfileAttribute.FIRST_NAME, "Pippo").//
		withUserProfileAttribute(UserProfileAttribute.LAST_NAME, "De' Pippis").//

		withAttribute("key1", "value1").//
		build();

	Assert.assertFalse(manager.update(accessToken, notUpdatedUser));

	//
	// attempt to update a non existing
	//

	KeycloakUser notUpdatedUser2 = new KeycloakUser.KeycloakUserBuilder().//
		enabled(true).//
		withIdentifier("unknownIdentifier").//
		withUserProfileAttribute(UserProfileAttribute.USERNAME, "pippo").//
		withUserProfileAttribute(UserProfileAttribute.EMAIL, "pippo2@gmail.com").//
		withUserProfileAttribute(UserProfileAttribute.FIRST_NAME, "Pippo").//
		withUserProfileAttribute(UserProfileAttribute.LAST_NAME, "De' Pippis").//
		withAttribute("key1", "value1").//
		build();

	Assert.assertFalse(manager.update(accessToken, notUpdatedUser2));

	//
	//
	//

	KeycloakUser updatedUser = new KeycloakUser.KeycloakUserBuilder().//
		enabled(true).//
		withIdentifier(listedNewUser.getIdentifier().get()).//

		withUserProfileAttribute(UserProfileAttribute.USERNAME, "pippo").//
		withUserProfileAttribute(UserProfileAttribute.EMAIL, "pippo2@gmail.com").//
		withUserProfileAttribute(UserProfileAttribute.FIRST_NAME, "Pippo2").//
		withUserProfileAttribute(UserProfileAttribute.LAST_NAME, "De' Pippis").//

		withAttribute("key1", "value1").//
		build();

	Assert.assertTrue(manager.update(accessToken, updatedUser));

	//
	//
	//

	KeycloakUser listedUpdatedUser = manager.list(accessToken).get(0);

	Assert.assertEquals(listedNewUser.getIdentifier(), listedUpdatedUser.getIdentifier());

	Assert.assertEquals(true, listedUpdatedUser.isEnabled()); // now it's enabled
	Assert.assertEquals("pippo", listedUpdatedUser.getUserProfileAttribute(UserProfileAttribute.USERNAME).get()); // username
														      // cannot
														      // be
														      // modified!
	Assert.assertEquals("pippo2@gmail.com", listedUpdatedUser.getUserProfileAttribute(UserProfileAttribute.EMAIL).get()); // email
															      // changed
	Assert.assertEquals("Pippo2", listedUpdatedUser.getUserProfileAttribute(UserProfileAttribute.FIRST_NAME).get()); // name
															 // changed
	Assert.assertEquals("De' Pippis", listedUpdatedUser.getUserProfileAttribute(UserProfileAttribute.LAST_NAME).get());

	Assert.assertEquals(1, listedUpdatedUser.getAttributes().size()); // only one attribute now

	Entry<String, List<String>> entry1 = listedUpdatedUser.getAttributes().get(0);
	Assert.assertEquals("key1", entry1.getKey());
	Assert.assertEquals("value1", entry1.getValue().get(0));

    }

    @Test
    public void createAndDeleteTest() throws IOException, InterruptedException, RuntimeException {

	String accessToken = manager.getAccessToken();

	Assert.assertNotNull(accessToken);

	Assert.assertEquals(0, manager.count(accessToken));

	KeycloakUser newUser = new KeycloakUser.KeycloakUserBuilder().//
		enabled(false).//
		withUserProfileAttribute(UserProfileAttribute.USERNAME, "pippo").//
		withUserProfileAttribute(UserProfileAttribute.EMAIL, "pippo2@gmail.com").//
		withUserProfileAttribute(UserProfileAttribute.FIRST_NAME, "Pippo").//
		withUserProfileAttribute(UserProfileAttribute.LAST_NAME, "De' Pippis").//
		withAttribute("key0", "value00", "value01", "value02").//
		withAttribute("key1", "value1").//
		build();

	Assert.assertTrue(manager.create(accessToken, newUser));

	Assert.assertEquals(1, manager.count(accessToken));

	KeycloakUser listedNewUser = manager.list(accessToken).get(0);

	//
	//
	//

	Assert.assertFalse(manager.delete(accessToken, "unknownIdentifier"));

	//
	//
	//

	Assert.assertTrue(manager.delete(accessToken, listedNewUser.getIdentifier().get()));

	Assert.assertEquals(0, manager.count(accessToken));
    }

    @Test
    public void findIdTest() throws IOException, InterruptedException, RuntimeException {

	String accessToken = manager.getAccessToken();

	Assert.assertNotNull(accessToken);

	Assert.assertEquals(0, manager.count(accessToken));

	KeycloakUser newUser1 = new KeycloakUser.KeycloakUserBuilder().//
		withUserProfileAttribute(UserProfileAttribute.USERNAME, "user1").//
		build();

	KeycloakUser newUser2 = new KeycloakUser.KeycloakUserBuilder().//
		withUserProfileAttribute(UserProfileAttribute.USERNAME, "user2").//
		build();

	Assert.assertTrue(manager.create(accessToken, newUser1));
	Assert.assertTrue(manager.create(accessToken, newUser2));

	KeycloakUser listedNewUser1 = manager.list(accessToken).get(0);
	KeycloakUser listedNewUser2 = manager.list(accessToken).get(1);

	Assert.assertTrue(listedNewUser1.getUserProfileAttribute(UserProfileAttribute.EMAIL).isEmpty());
	Assert.assertTrue(listedNewUser1.getUserProfileAttribute(UserProfileAttribute.FIRST_NAME).isEmpty());
	Assert.assertTrue(listedNewUser1.getUserProfileAttribute(UserProfileAttribute.LAST_NAME).isEmpty());
	Assert.assertTrue(listedNewUser1.getAttributes().isEmpty());

	String id1 = listedNewUser1.getIdentifier().get();
	String id2 = listedNewUser2.getIdentifier().get();

	//
	//
	//

	Assert.assertEquals(id1, manager.findId(accessToken, "user1").get());
	Assert.assertEquals(id2, manager.findId(accessToken, "user2").get());
    }
}
