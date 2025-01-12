/**
 * 
 */
package eu.essi_lab.api.database.opensearch.usersfolder.test;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.index.IndexData;
import eu.essi_lab.api.database.opensearch.index.IndexData.DataType;
import eu.essi_lab.api.database.opensearch.index.SourceWrapper;
import eu.essi_lab.api.database.opensearch.index.mappings.UsersMapping;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.api.database.opensearch.test.OpenSearchdatabaseInitTest;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.auth.UserIdentifierType;

/**
 * @author Fabrizio
 */
public class OpenSearchUsersFolderTest extends OpenSearchTest {

    @Test
    public void sourceTest() throws Exception {

	OpenSearchDatabase database = OpenSearchdatabaseInitTest.create();

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

	Assert.assertEquals(database.getIdentifier(), wrapper.getDatabaseId());

	Assert.assertEquals(folderName, wrapper.getFolderName());

	Assert.assertEquals(OpenSearchFolder.getFolderId(folder), wrapper.getFolderId());

	Assert.assertEquals(OpenSearchFolder.getResourceId(folder, key), wrapper.getResourceId());

	Assert.assertEquals(key, wrapper.getResourceKey());

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
		IndexData.encode(FolderEntry.of(user.asDocument(true))), wrapper.getUser().get());
    }

    @Test
    public void folderTest() throws Exception {

	OpenSearchDatabase database = OpenSearchdatabaseInitTest.create();

	String folderName = Database.USERS_FOLDER;

	OpenSearchFolder folder = new OpenSearchFolder(database, folderName);

	//
	//

	GSUser user = new GSUser("userId", UserIdentifierType.VIEW_CREATOR, "anonymous");
	user.setEnabled(false);

	String key = user.getIdentifier();

	folder.store(//
		key, //
		FolderEntry.of(user.asDocument(true)), //
		EntryType.USER);

	//
	//
	//

	folder.store(key, FolderEntry.of(user.asDocument(true)), EntryType.USER);

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
}
