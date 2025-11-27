/**
 *
 */
package eu.essi_lab.lib.net.keycloak.test;

import eu.essi_lab.lib.net.keycloak.KeycloakUser;
import eu.essi_lab.lib.net.keycloak.KeycloakUser.UserProfileAttribute;
import eu.essi_lab.lib.net.keycloak.KeycloakUsersClient;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author Fabrizio
 */
public class KeycloakUsersClientInternalTestIT {

    private KeycloakUsersClient client;

    /**
     *
     */
    private static final String USERS_REALM = "testRealm";

    @Before
    public void before() throws IOException, InterruptedException {

	client = new KeycloakUsersClient();
	//	manager.setServiceUrl("http://localhost:8080");
	client.setServiceUrl(System.getProperty("keycloak.host"));
	client.setAdminPassword(System.getProperty("keycloak.password"));
	client.setAdminUser(System.getProperty("keycloak.user"));
	client.setUsersRealm(USERS_REALM);

	String accessToken = client.getAccessToken();

	if (!client.usersRealmExists(accessToken)) {

	    client.createUsersRealm(accessToken, USERS_REALM);

	} else {

	    client.clear(accessToken);
	}

	Assert.assertEquals(0, client.count(accessToken));
    }

    @Test
    public void realmsTest() throws IOException, InterruptedException {

	String accessToken = client.getAccessToken();

	List<String> realms = client.getRealms(accessToken).stream().sorted().toList();

	Assert.assertEquals("master", realms.getFirst());
	Assert.assertEquals("testRealm", realms.get(1));

	Assert.assertFalse(client.createUsersRealm(accessToken, USERS_REALM));

	Assert.assertTrue(client.usersRealmExists(accessToken));

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

	String accessToken = client.getAccessToken();

	//
	//
	// the JSON in the create request do not include several object/properties that are included
	// in the raw JSON; the missing object/properties are added by the server
	//
	//

	Assert.assertTrue(client.create(accessToken, user));

	Assert.assertEquals(1, client.count(accessToken));

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
	JSONObject rawJSON = client.listRaw(accessToken).getFirst();

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
	JSONObject simpleJSON = client.list(accessToken).getFirst().toJSON();

	//
	// common elements
	//

	Assert.assertEquals(rawJSON.getBoolean(KeycloakUser.ENABLED_FIELD), simpleJSON.getBoolean("enabled"));
	Assert.assertEquals(rawJSON.getString(KeycloakUser.ID_FIELD), simpleJSON.getString("id"));
	Assert.assertEquals(rawJSON.getString(UserProfileAttribute.EMAIL.getAttribute()),
		simpleJSON.getString(UserProfileAttribute.EMAIL.getAttribute()));
	Assert.assertEquals(rawJSON.getString(UserProfileAttribute.USERNAME.getAttribute()),
		simpleJSON.getString(UserProfileAttribute.USERNAME.getAttribute()));
	Assert.assertEquals(rawJSON.getString(UserProfileAttribute.LAST_NAME.getAttribute()),
		simpleJSON.getString(UserProfileAttribute.LAST_NAME.getAttribute()));
	Assert.assertEquals(rawJSON.getString(UserProfileAttribute.FIRST_NAME.getAttribute()),
		simpleJSON.getString(UserProfileAttribute.FIRST_NAME.getAttribute()));
	Assert.assertEquals(rawJSON.getJSONObject("attributes").toString(), simpleJSON.getJSONObject("attributes").toString());

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

	String accessToken = client.getAccessToken();

	Assert.assertNotNull(accessToken);

	Assert.assertEquals(0, client.count(accessToken));

	KeycloakUser newUser = new KeycloakUser.KeycloakUserBuilder().//
		withUserProfileAttribute(UserProfileAttribute.USERNAME, "pippo").//
		withUserProfileAttribute(UserProfileAttribute.EMAIL, "pippo@gmail.com").//
		withUserProfileAttribute(UserProfileAttribute.FIRST_NAME, "Pippo").//
		withUserProfileAttribute(UserProfileAttribute.LAST_NAME, "De' Pippis").//
		withAttribute("key0", "value00", "value01", "value02").//
		withAttribute("key1", "value1").//
		build();

	// invalid token
	Assert.assertFalse(client.create(accessToken + "x", newUser));

	Assert.assertTrue(client.create(accessToken, newUser));

	// username already exists
	Assert.assertFalse(client.create(accessToken, newUser));

	//
	//
	//

	Assert.assertEquals(1, client.count(accessToken));

	//
	//
	//

	KeycloakUser listedUser = client.list(accessToken).getFirst();

	Assert.assertNotNull(listedUser.getIdentifier());

	Assert.assertTrue(listedUser.isEnabled()); // default
	Assert.assertEquals("pippo", listedUser.getUserProfileAttribute(UserProfileAttribute.USERNAME).get());
	Assert.assertEquals("pippo@gmail.com", listedUser.getUserProfileAttribute(UserProfileAttribute.EMAIL).get());
	Assert.assertEquals("Pippo", listedUser.getUserProfileAttribute(UserProfileAttribute.FIRST_NAME).get());
	Assert.assertEquals("De' Pippis", listedUser.getUserProfileAttribute(UserProfileAttribute.LAST_NAME).get());

	Assert.assertEquals(2, listedUser.getAttributes().size());

	List<Entry<String, List<String>>> attributes = listedUser.getAttributes().stream().sorted(Comparator.comparing(Entry::getKey))
		.toList();

	Entry<String, List<String>> entry0 = attributes.getFirst();
	Assert.assertEquals("key0", entry0.getKey());
	Assert.assertEquals("value00", entry0.getValue().getFirst());
	Assert.assertEquals("value01", entry0.getValue().get(1));
	Assert.assertEquals("value02", entry0.getValue().get(2));

	Entry<String, List<String>> entry1 = attributes.get(1);
	Assert.assertEquals("key1", entry1.getKey());
	Assert.assertEquals("value1", entry1.getValue().getFirst());
    }

    @Test
    public void createAndUpdateTest() throws IOException, InterruptedException, RuntimeException {

	String accessToken = client.getAccessToken();

	Assert.assertNotNull(accessToken);

	Assert.assertEquals(0, client.count(accessToken));

	KeycloakUser newUser = new KeycloakUser.KeycloakUserBuilder().//
		enabled(false).//
		withUserProfileAttribute(UserProfileAttribute.USERNAME, "pippo").//

		withUserProfileAttribute(UserProfileAttribute.EMAIL, "pippo@gmail.com").//
		withUserProfileAttribute(UserProfileAttribute.FIRST_NAME, "Pippo").//
		withUserProfileAttribute(UserProfileAttribute.LAST_NAME, "De' Pippis").//

		withAttribute("key0", "value00", "value01", "value02").//
		withAttribute("key1", "value1").//
		build();

	Assert.assertTrue(client.create(accessToken, newUser));

	KeycloakUser listedNewUser = client.list(accessToken).getFirst();

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

	Assert.assertFalse(client.update(accessToken, notUpdatedUser));

	//
	// attempt to update a non-existing
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

	Assert.assertFalse(client.update(accessToken, notUpdatedUser2));

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

	Assert.assertTrue(client.update(accessToken, updatedUser));

	//
	//
	//

	KeycloakUser listedUpdatedUser = client.list(accessToken).getFirst();

	Assert.assertEquals(listedNewUser.getIdentifier(), listedUpdatedUser.getIdentifier());

	Assert.assertTrue(listedUpdatedUser.isEnabled()); // now it's enabled
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

	Entry<String, List<String>> entry1 = listedUpdatedUser.getAttributes().getFirst();
	Assert.assertEquals("key1", entry1.getKey());
	Assert.assertEquals("value1", entry1.getValue().getFirst());

    }

    @Test
    public void createAndDeleteTest() throws IOException, InterruptedException, RuntimeException {

	String accessToken = client.getAccessToken();

	Assert.assertNotNull(accessToken);

	Assert.assertEquals(0, client.count(accessToken));

	KeycloakUser newUser = new KeycloakUser.KeycloakUserBuilder().//
		enabled(false).//
		withUserProfileAttribute(UserProfileAttribute.USERNAME, "pippo").//
		withUserProfileAttribute(UserProfileAttribute.EMAIL, "pippo2@gmail.com").//
		withUserProfileAttribute(UserProfileAttribute.FIRST_NAME, "Pippo").//
		withUserProfileAttribute(UserProfileAttribute.LAST_NAME, "De' Pippis").//
		withAttribute("key0", "value00", "value01", "value02").//
		withAttribute("key1", "value1").//
		build();

	Assert.assertTrue(client.create(accessToken, newUser));

	Assert.assertEquals(1, client.count(accessToken));

	KeycloakUser listedNewUser = client.list(accessToken).getFirst();

	//
	//
	//

	Assert.assertFalse(client.delete(accessToken, "unknownIdentifier"));

	//
	//
	//

	Assert.assertTrue(client.delete(accessToken, listedNewUser.getIdentifier().get()));

	Assert.assertEquals(0, client.count(accessToken));
    }

    @Test
    public void findIdTest() throws IOException, InterruptedException, RuntimeException {

	String accessToken = client.getAccessToken();

	Assert.assertNotNull(accessToken);

	Assert.assertEquals(0, client.count(accessToken));

	KeycloakUser newUser1 = new KeycloakUser.KeycloakUserBuilder().//
		withUserProfileAttribute(UserProfileAttribute.USERNAME, "user1").//
		build();

	KeycloakUser newUser2 = new KeycloakUser.KeycloakUserBuilder().//
		withUserProfileAttribute(UserProfileAttribute.USERNAME, "user2").//
		build();

	Assert.assertTrue(client.create(accessToken, newUser1));
	Assert.assertTrue(client.create(accessToken, newUser2));

	KeycloakUser listedNewUser1 = client.list(accessToken).getFirst();
	KeycloakUser listedNewUser2 = client.list(accessToken).get(1);

	Assert.assertTrue(listedNewUser1.getUserProfileAttribute(UserProfileAttribute.EMAIL).isEmpty());
	Assert.assertTrue(listedNewUser1.getUserProfileAttribute(UserProfileAttribute.FIRST_NAME).isEmpty());
	Assert.assertTrue(listedNewUser1.getUserProfileAttribute(UserProfileAttribute.LAST_NAME).isEmpty());
	Assert.assertTrue(listedNewUser1.getAttributes().isEmpty());

	String id1 = listedNewUser1.getIdentifier().get();
	String id2 = listedNewUser2.getIdentifier().get();

	//
	//
	//

	Assert.assertEquals(id1, client.findId(accessToken, "user1").get());
	Assert.assertEquals(id2, client.findId(accessToken, "user2").get());
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

	System.out.println(listRaw.getFirst().toString(3));

    }
}

