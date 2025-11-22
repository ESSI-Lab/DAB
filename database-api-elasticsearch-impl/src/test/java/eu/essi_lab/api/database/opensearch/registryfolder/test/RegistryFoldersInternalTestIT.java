/**
 * 
 */
package eu.essi_lab.api.database.opensearch.registryfolder.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.api.database.opensearch.FolderRegistry;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.OpenSearchFolder;
import eu.essi_lab.api.database.opensearch.datafolder.test.TestUtils;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;

/**
 * @author Fabrizio
 */
public class RegistryFoldersInternalTestIT extends OpenSearchTest {

    @Test
    public void test() throws Exception {

OpenSearchDatabase database = createDataBase();

	String folderName1 = TestUtils.getMetaFolderName(database, "source_1");
	String folderName2 = TestUtils.getMetaFolderName(database, "source_2");
	String folderName3 = TestUtils.getMetaFolderName(database, "source_3");

	OpenSearchFolder folder1 = new OpenSearchFolder(database, folderName1);
	OpenSearchFolder folder2 = new OpenSearchFolder(database, folderName2);
	OpenSearchFolder folder3 = new OpenSearchFolder(database, folderName3);

	//
	//
	//

	FolderRegistry registry = FolderRegistry.get(database);

	Assert.assertFalse(registry.deregister(folder1));
	Assert.assertFalse(registry.isRegistered(folder1));

	Assert.assertFalse(registry.deregister(folder2));
	Assert.assertFalse(registry.isRegistered(folder2));

	Assert.assertFalse(registry.deregister(folder3));
	Assert.assertFalse(registry.isRegistered(folder3));

	Assert.assertTrue(registry.register(folder1));
	Assert.assertFalse(registry.register(folder1));

	Assert.assertTrue(registry.isRegistered(folder1));

	Assert.assertFalse(registry.deregister(folder2));
	Assert.assertFalse(registry.isRegistered(folder2));

	Assert.assertFalse(registry.deregister(folder3));
	Assert.assertFalse(registry.isRegistered(folder3));

	Assert.assertTrue(registry.register(folder2));
	Assert.assertFalse(registry.register(folder2));

	Assert.assertTrue(registry.register(folder3));
	Assert.assertFalse(registry.register(folder3));

	Assert.assertTrue(registry.isRegistered(folder2));
	Assert.assertTrue(registry.isRegistered(folder3));

	Assert.assertTrue(registry.deregister(folder1));
	Assert.assertFalse(registry.deregister(folder1));
	Assert.assertFalse(registry.isRegistered(folder1));

	Assert.assertTrue(registry.deregister(folder2));
	Assert.assertFalse(registry.deregister(folder2));
	Assert.assertFalse(registry.isRegistered(folder2));

	Assert.assertTrue(registry.deregister(folder3));
	Assert.assertFalse(registry.deregister(folder3));
	Assert.assertFalse(registry.isRegistered(folder3));
    }
}
