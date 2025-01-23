/**
 * 
 */
package eu.essi_lab.api.database.opensearch.usersfolder.test;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.w3c.dom.Node;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.opensearch.ConversionUtils;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.OpenSearchWrapper;
import eu.essi_lab.api.database.opensearch.index.IndexData.DataType;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.AugmentersMapping;
import eu.essi_lab.api.database.opensearch.index.mappings.UsersMapping;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.auth.UserIdentifierType;

/**
 * @author Fabrizio
 */
public class OpenSearchUsersFolderTest extends OpenSearchTest {

    @Test
    public void sourceTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = Database.USERS_FOLDER;

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//

	GSUser user = new GSUser("testUser", UserIdentifierType.VIEW_CREATOR, "anonymous");
	user.setEnabled(true);

	String key = user.getIdentifier();

	folder.store(//
		key, //
		FolderEntry.of(user.asDocument(true)), //
		EntryType.USER);

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	//
	// base properties
	//

	Assert.assertEquals(UsersMapping.get().getIndex(), wrapper.getIndex());

	Assert.assertEquals(database.getIdentifier(), wrapper.getDatabaseId());

	Assert.assertEquals(folder.getName(), wrapper.getFolderName());

	Assert.assertEquals(OpenSearchFolder.getFolderId(folder), wrapper.getFolderId());

	Assert.assertEquals(OpenSearchFolder.getEntryId(folder, key), wrapper.getEntryId());

	Assert.assertEquals(key, wrapper.getEntryName());

	Assert.assertEquals(UsersMapping.USER, wrapper.getBinaryProperty());

	Assert.assertEquals(DataType.DOC, wrapper.getDataType());

	//
	// users-index property
	//

	Assert.assertEquals(user.getIdentifier(), wrapper.getUserIdentifier().get());
	Assert.assertEquals(user.getUserIdentifierType().get(), wrapper.getUserIdentifierType().get());
	Assert.assertEquals(user.getRole(), wrapper.getUserRole().get());
	Assert.assertEquals(user.isEnabled(), wrapper.getUserEnabled().get());

	Assert.assertEquals(//
		ConversionUtils.encode(FolderEntry.of(user.asDocument(true))), wrapper.getUser().get());
    }

    @Test
    public void sourceTest2() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = Database.USERS_FOLDER;

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//

	GSUser user = new GSUser();
	user.setEnabled(false);
	user.setIdentifier("identifier");

	String key = user.getIdentifier();

	folder.store(//
		key, //
		FolderEntry.of(user.asDocument(true)), //
		EntryType.USER);

	SourceWrapper wrapper = folder.getSourceWrapper(key);

	//
	// base properties
	//

	Assert.assertEquals(UsersMapping.get().getIndex(), wrapper.getIndex());

	Assert.assertEquals(database.getIdentifier(), wrapper.getDatabaseId());

	Assert.assertEquals(folder.getName(), wrapper.getFolderName());

	Assert.assertEquals(OpenSearchFolder.getFolderId(folder), wrapper.getFolderId());

	Assert.assertEquals(OpenSearchFolder.getEntryId(folder, key), wrapper.getEntryId());

	Assert.assertEquals(key, wrapper.getEntryName());

	Assert.assertEquals(UsersMapping.USER, wrapper.getBinaryProperty());

	Assert.assertEquals(DataType.DOC, wrapper.getDataType());
	
	Assert.assertEquals(wrapper.getUser().get(), wrapper.getBinaryValue());

	//
	// users-index property
	//

	Assert.assertEquals(user.getIdentifier(), wrapper.getUserIdentifier().get());
	Assert.assertFalse(user.getUserIdentifierType().isPresent());
	Assert.assertFalse(wrapper.getUserRole().isPresent());
	Assert.assertEquals(user.isEnabled(), wrapper.getUserEnabled().get());

	Assert.assertEquals(//
		ConversionUtils.encode(FolderEntry.of(user.asDocument(true))), wrapper.getUser().get());
    }

    @Test
    public void folderTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = Database.USERS_FOLDER;

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//

	GSUser user = new GSUser("userId", UserIdentifierType.VIEW_CREATOR, "anonymous");
	user.setEnabled(false);

	String key = user.getIdentifier();

	Assert.assertTrue(folder.store(//
		key, //
		FolderEntry.of(user.asDocument(true)), //
		EntryType.USER));
	//
	//
	//

	Node node = folder.get(key);

	GSUser user2 = GSUser.create(node);

	Assert.assertEquals(user, user2);

	//
	// trying to get a doc as a binary always works
	//

	InputStream binary = folder.getBinary(key);

	GSUser user3 = GSUser.create(binary);

	Assert.assertEquals(user, user3);
    }

    @Test
    public void searchUsersTest() throws Exception {

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	String folderName = Database.USERS_FOLDER;

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//
	//

	storeUsers(folder, 10);
	
	//
	//
	//

	OpenSearchWrapper wrapper = new OpenSearchWrapper(database.getClient());


	//
	//
	//
	
	Query query = wrapper.buildSearchQuery(database.getIdentifier(), UsersMapping.get().getIndex());

	List<GSUser> users = wrapper.searchBinaries(query).//
		stream().//
		map(binary -> GSUser.createOrNull(binary)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(10, users.size());
	
	//
	//
	//
	
	query = wrapper.buildSearchQuery(//
		database.getIdentifier(), //
		UsersMapping.get().getIndex(), //
		UsersMapping.USER_ID, //
		"User_3");

	users = wrapper.searchBinaries(query).//
		stream().//
		map(binary -> GSUser.createOrNull(binary)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(1, users.size());

	Assert.assertEquals("User_3", users.get(0).getIdentifier());
	
	
	//
	//
	//

	query = wrapper.buildSearchQuery(//
		database.getIdentifier(), //
		UsersMapping.get().getIndex(), //
		UsersMapping.USER_ROLE, //
		"Role_5");

	users = wrapper.searchBinaries(query).//
		stream().//
		map(binary -> GSUser.createOrNull(binary)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(1, users.size());

	Assert.assertEquals("Role_5", users.get(0).getRole());
	
	//
	// undefined value
	//
	
	query = wrapper.buildSearchQuery(//
		database.getIdentifier(), //
		UsersMapping.get().getIndex(), //
		UsersMapping.USER_ROLE, //
		"Role_11");

	users = wrapper.searchBinaries(query).//
		stream().//
		map(binary -> GSUser.createOrNull(binary)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(0, users.size());
	
	//
	// undefined property
	//
	
	query = wrapper.buildSearchQuery(//
		database.getIdentifier(), //
		UsersMapping.get().getIndex(), //
		"unknown_property", //
		"Role_5");

	users = wrapper.searchBinaries(query).//
		stream().//
		map(binary -> GSUser.createOrNull(binary)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(0, users.size());
	
	//
	// wrong index
	//
	
	query = wrapper.buildSearchQuery(//
		database.getIdentifier(), //
		AugmentersMapping.get().getIndex(), //
		UsersMapping.USER_ROLE, //
		"Role_5");

	users = wrapper.searchBinaries(query).//
		stream().//
		map(binary -> GSUser.createOrNull(binary)).//
		filter(Objects::nonNull).//
		collect(Collectors.toList());

	Assert.assertEquals(0, users.size());
    }

    /**
     * @param folder
     * @param usersCount
     * @throws Exception
     */
    private void storeUsers(DatabaseFolder folder, int usersCount) throws Exception {

	for (int i = 0; i < usersCount; i++) {

	    String id = "User_" + i;
	    String role = "Role_" + i;

	    GSUser user = new GSUser(id, UserIdentifierType.VIEW_CREATOR, "anonymous");
	    user.setRole(role);

	    String key = user.getIdentifier();

	    Assert.assertTrue(folder.store(//
		    key, //
		    FolderEntry.of(user.asDocument(true)), //
		    EntryType.USER));
	}
    }

}
