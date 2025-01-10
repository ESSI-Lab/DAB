/**
 * 
 */
package eu.essi_lab.cfga.source.test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.cfg.DatabaseSource;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.model.StorageInfo;

/**
 * @author Fabrizio
 */
public class DatabaseSourceTest extends ConfigurationSourceTest {

    private static final StorageInfo URI = new StorageInfo(System.getProperty("dbUrl"));

    static {

	URI.setIdentifier("configtest");
	URI.setPassword(System.getProperty("dbPassword"));
	URI.setUser(System.getProperty("dbUser"));
	URI.setName("TEST-DB");
    }

    @Before
    public void init() throws Exception {

	StorageInfo clone = URI.clone();
	clone.setIdentifier("ROOT");

	Database database = DatabaseFactory.get(clone);

	if (database instanceof MarkLogicDatabase) {

	    MarkLogicDatabase db = (MarkLogicDatabase) database;
	    db.removeAllFolders();
	}
    }

    @Test
    public void locationTest() throws Exception {

	DatabaseSource source = new DatabaseSource(URI, "test-config");
	Assert.assertEquals("configtest\\test-config.json", source.getLocation());
    }

    @Test
    public void listTest() throws Exception {

	super.listTest(new DatabaseSource(URI, "test-config"));
    }

    @Test
    public void lockTest() throws Exception {

	super.lockTest(new DatabaseSource(URI, "test-config"));
    }

    @Test
    public void backupTest() throws Exception {

	DatabaseSource source = new DatabaseSource(URI, "test-config");

	Setting setting1 = new Setting();
	Setting setting2 = new Setting();
	Setting setting3 = new Setting();

	source.flush(Arrays.asList(setting1, setting2, setting3));

	DatabaseSource backup = source.backup();

	Assert.assertFalse(backup.isEmptyOrMissing());

	// Assert.assertEquals(source.getSource().getParent(), backup.getSource().getParent());

	Assert.assertTrue(backup.getLocation().endsWith(".backup"));
    }

}
