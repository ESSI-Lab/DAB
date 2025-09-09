/**
 * 
 */
package eu.essi_lab.lib.net.keycloak.test;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.lib.net.keycloak.KeycloakUser;
import eu.essi_lab.lib.net.keycloak.KeycloakUsersManager;

/**
 * @author Fabrizio
 */
public class KeycloakUsersManagerTest {

    private KeycloakUsersManager manager;

    @Before
    public void before() throws IOException, InterruptedException {

	manager = new KeycloakUsersManager();
	manager.setServiceUrl("http://localhost:8080");
	manager.setAdminPassword(System.getProperty("keycloak.password"));
	manager.setAdminUser(System.getProperty("keycloak.user"));
	manager.setUsersRealm("usersRealm");

	String accessToken = manager.getAccessToken();

	List<KeycloakUser> list = manager.list(accessToken);

	for (KeycloakUser user : list) {

	    manager.delete(accessToken, user.getIdentifier().get());
	}

	Assert.assertEquals(0, manager.count(accessToken));
    }

    @Test
    public void createAndListTest() throws IOException, InterruptedException, RuntimeException {

	String accessToken = manager.getAccessToken();

	Assert.assertNotNull(accessToken);

	Assert.assertEquals(0, manager.count(accessToken));

	KeycloakUser newUser = new KeycloakUser.KeycloakUserBuilder().//
		withUserName("pippo").//
		withEmail("pippo@gmail.com").//
		withFirstName("Pippo").//
		withLastName("De' Pippis").//
		withAttribute("key0", "value00", "value01", "value02").//
		withAttribute("key1", "value1").//
		build();

	// invalid token
	Assert.assertFalse(manager.create(accessToken+"x", newUser));

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
	Assert.assertEquals("pippo", listedUser.getUserName());
	Assert.assertEquals("pippo@gmail.com", listedUser.getEmail().get());
	Assert.assertEquals("Pippo", listedUser.getFirstName().get());
	Assert.assertEquals("De' Pippis", listedUser.getLastName().get());

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
		withUserName("pippo").//
		withEmail("pippo@gmail.com").//
		withFirstName("Pippo").//
		withLastName("De' Pippis").//
		withAttribute("key0", "value00", "value01", "value02").//
		withAttribute("key1", "value1").//
		build();

	Assert.assertTrue(manager.create(accessToken, newUser));

	KeycloakUser listedNewUser = manager.list(accessToken).get(0);

	//
	//
	//

	KeycloakUser notUpdatedUser = new KeycloakUser.KeycloakUserBuilder().//
		enabled(true).//
		withIdentifier(listedNewUser.getIdentifier().get()).//
		withUserName("pippo2").// username cannot be modified!
		withEmail("pippo2@gmail.com").//
		withFirstName("Pippo").//
		withLastName("De' Pippis").//
		withAttribute("key1", "value1").//
		build();

	Assert.assertFalse(manager.update(accessToken, notUpdatedUser));

	//
	//

	KeycloakUser notUpdatedUser2 = new KeycloakUser.KeycloakUserBuilder().//
		enabled(true).//
		withIdentifier("unknownIdentifier").//
		withUserName("pippo").// username cannot be modified!
		withEmail("pippo2@gmail.com").//
		withFirstName("Pippo").//
		withLastName("De' Pippis").//
		withAttribute("key1", "value1").//
		build();

	Assert.assertFalse(manager.update(accessToken, notUpdatedUser2));

	//
	//
	//

	KeycloakUser updatedUser = new KeycloakUser.KeycloakUserBuilder().//
		enabled(true).//
		withIdentifier(listedNewUser.getIdentifier().get()).//
		withUserName("pippo").//
		withEmail("pippo2@gmail.com").//
		withFirstName("Pippo2").//
		withLastName("De' Pippis").//
		withAttribute("key1", "value1").//
		build();

	Assert.assertTrue(manager.update(accessToken, updatedUser));

	//
	//
	//

	KeycloakUser listedUpdatedUser = manager.list(accessToken).get(0);

	Assert.assertEquals(listedNewUser.getIdentifier(), listedUpdatedUser.getIdentifier());

	Assert.assertEquals(true, listedUpdatedUser.isEnabled()); // now it's enabled
	Assert.assertEquals("pippo", listedUpdatedUser.getUserName()); // username cannot be modified!
	Assert.assertEquals("pippo2@gmail.com", listedUpdatedUser.getEmail().get()); // email changed
	Assert.assertEquals("Pippo2", listedUpdatedUser.getFirstName().get()); // name changed
	Assert.assertEquals("De' Pippis", listedUpdatedUser.getLastName().get());

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
		withUserName("pippo").//
		withEmail("pippo@gmail.com").//
		withFirstName("Pippo").//
		withLastName("De' Pippis").//
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
		withUserName("user1").//
		build();

	KeycloakUser newUser2 = new KeycloakUser.KeycloakUserBuilder().//
		withUserName("user2").//
		build();

	Assert.assertTrue(manager.create(accessToken, newUser1));
	Assert.assertTrue(manager.create(accessToken, newUser2));

	KeycloakUser listedNewUser1 = manager.list(accessToken).get(0);
	KeycloakUser listedNewUser2 = manager.list(accessToken).get(1);

	Assert.assertTrue(listedNewUser1.getEmail().isEmpty());
	Assert.assertTrue(listedNewUser1.getFirstName().isEmpty());
	Assert.assertTrue(listedNewUser1.getLastName().isEmpty());
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
