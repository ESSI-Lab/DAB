/**
 * 
 */
package eu.essi_lab.cfga.source.test;

import org.junit.Before;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.api.database.marklogic.MarkLogicDatabase;
import eu.essi_lab.model.StorageInfo;

/**
 * @author Fabrizio
 */
public class MarkLogicSourceTest extends DatabaseSourceTest {

    private static final StorageInfo INFO = new StorageInfo(System.getProperty("dbUrl"));

    static {

	INFO.setIdentifier("configtest");
	INFO.setPassword(System.getProperty("dbPassword"));
	INFO.setUser(System.getProperty("dbUser"));
	INFO.setName("TEST-DB");
    }

    /**
     * @param info
     */
    public MarkLogicSourceTest() {

	super(INFO);
    }

    @Before
    public void init() throws Exception {

	StorageInfo clone = storageInfo.clone();
	clone.setIdentifier("ROOT");

	Database database = DatabaseFactory.get(clone);

	MarkLogicDatabase db = (MarkLogicDatabase) database;
	db.removeAllFolders();
    }
}
