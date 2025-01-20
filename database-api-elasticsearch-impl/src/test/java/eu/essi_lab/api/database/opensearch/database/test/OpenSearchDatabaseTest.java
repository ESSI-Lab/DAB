/**
 * 
 */
package eu.essi_lab.api.database.opensearch.database.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.api.database.opensearch.test.OpenSearchDatabaseInitTest;
import eu.essi_lab.api.database.opensearch.test.OpenSearchTest;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class OpenSearchDatabaseTest extends OpenSearchTest {

    @Test
    public void basicAndFoldersTest() throws GSException {

	OpenSearchDatabase database = OpenSearchDatabaseInitTest.create();

	//
	//
	//

	Assert.assertEquals(DatabaseImpl.OPENSEARCH.getName(), database.getType());

	Assert.assertEquals(OpenSearchDatabaseInitTest.createStorageInfo(), database.getStorageInfo());

	//
	//
	//

	Assert.assertNotNull(database.getWorker("sourceId"));

	//
	//
	//

	Assert.assertTrue(database.supports(OpenSearchDatabaseInitTest.createStorageInfo()));

	StorageInfo clone = OpenSearchDatabaseInitTest.createStorageInfo().clone();
	clone.setType(null);

	Assert.assertFalse(database.supports(clone));

	StorageInfo clone2 = OpenSearchDatabaseInitTest.createStorageInfo().clone();
	clone2.setType("type");

	Assert.assertFalse(database.supports(clone2));

	//
	//
	//

	Assert.assertNull(database.getSetting());

	database.configure(new DatabaseSetting());

	Assert.assertNotNull(database.getSetting());

	//
	//
	//
	{
	    DatabaseFolder folder = database.getFolder("testFolder1");

	    Assert.assertNull(folder);

	    Assert.assertFalse(database.existsFolder("testFolder1"));

	    DatabaseFolder[] folders = database.getFolders();

	    Assert.assertEquals(0, folders.length);

	    Assert.assertFalse(database.removeFolder("testFolder1"));
	}

	//
	//
	//

	{

	    Assert.assertTrue(database.addFolder("testFolder1"));
	    Assert.assertFalse(database.addFolder("testFolder1"));

	    Assert.assertTrue(database.existsFolder("testFolder1"));

	    DatabaseFolder[] folders = database.getFolders();

	    Assert.assertEquals(1, folders.length);
	}

	{

	    Assert.assertTrue(database.addFolder("testFolder2"));
	    Assert.assertFalse(database.addFolder("testFolder2"));

	    Assert.assertTrue(database.existsFolder("testFolder2"));

	    DatabaseFolder[] folders = database.getFolders();

	    Assert.assertEquals(2, folders.length);
	}

	{

	    Assert.assertTrue(database.addFolder("testFolder3"));
	    Assert.assertFalse(database.addFolder("testFolder3"));

	    Assert.assertTrue(database.existsFolder("testFolder3"));

	    DatabaseFolder[] folders = database.getFolders();

	    Assert.assertEquals(3, folders.length);
	}

	{

	    Assert.assertTrue(database.removeFolder("testFolder1"));
	    Assert.assertFalse(database.existsFolder("testFolder1"));

	    Assert.assertTrue(database.existsFolder("testFolder2"));
	    Assert.assertTrue(database.existsFolder("testFolder3"));

	    DatabaseFolder[] folders = database.getFolders();

	    Assert.assertEquals(2, folders.length);
	}

	{

	    Assert.assertTrue(database.removeFolder("testFolder2"));
	    Assert.assertFalse(database.existsFolder("testFolder2"));

	    Assert.assertFalse(database.existsFolder("testFolder1"));
	    Assert.assertFalse(database.existsFolder("testFolder2"));

	    Assert.assertTrue(database.existsFolder("testFolder3"));

	    DatabaseFolder[] folders = database.getFolders();

	    Assert.assertEquals(1, folders.length);
	}

	{
	    Assert.assertTrue(database.removeFolder("testFolder3"));
	    Assert.assertFalse(database.existsFolder("testFolder3"));

	    Assert.assertFalse(database.existsFolder("testFolder1"));
	    Assert.assertFalse(database.existsFolder("testFolder2"));
	    Assert.assertFalse(database.existsFolder("testFolder3"));

	    DatabaseFolder[] folders = database.getFolders();

	    Assert.assertEquals(0, folders.length);
	}

	Assert.assertFalse(database.getFolder("testFolder1", false).isPresent());

	Assert.assertTrue(database.getFolder("testFolder1", true).isPresent());

	Assert.assertFalse(database.addFolder("testFolder1"));

	Assert.assertTrue(database.existsFolder("testFolder1"));

	DatabaseFolder[] folders = database.getFolders();

	Assert.assertEquals(1, folders.length);
    }

}
