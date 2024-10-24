package eu.essi_lab.api.database.vol.test;

import static org.junit.Assert.fail;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.api.database.vol.VolatileDatabase;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class VolatileDatabaseTest {

    private static final StorageInfo TEST_DB_URI = new DatabaseSetting(true).asStorageUri();
    static {
	TEST_DB_URI.setIdentifier("test");
    }

    @Test
    public void providerFactoryTest() throws GSException {

	String suiteIdentifier = null;

	// ----------------------------
	//
	//
	Database database = DatabaseFactory.create(TEST_DB_URI);

	if (database == null) {

	    suiteIdentifier = UUID.randomUUID().toString();
	} else {
	    suiteIdentifier = ((VolatileDatabase) database).getIdentifier();
	}

	TEST_DB_URI.setIdentifier(suiteIdentifier);

	try {
	    database.initialize(TEST_DB_URI);
	} catch (GSException e) {

	    e.printStackTrace();

	    fail("Exception thrown");
	}

	// ----------------------------
	//
	//
	database = DatabaseFactory.create(TEST_DB_URI);
	try {
	    database.initialize(TEST_DB_URI);
	} catch (GSException e) {
	    fail("Exception thrown");
	}

	database = DatabaseFactory.create(TEST_DB_URI);
	try {
	    database.initialize(TEST_DB_URI);
	    VolatileDatabase db = (VolatileDatabase) database;
	    String suiteID = db.getIdentifier();
	    Assert.assertEquals("Suite identifier not equals to the given one", suiteID, suiteIdentifier);

	} catch (GSException e) {
	    fail("Exception thrown");
	}

	database = DatabaseFactory.create(TEST_DB_URI);
	TEST_DB_URI.setIdentifier("xxx");

	// ------------------------------------------------
	// since the DB is already initialized, initializing again with another suite id (or a null id)
	// does not produce a different suite id
	//
	try {
	    database.initialize(TEST_DB_URI);
	    VolatileDatabase db = (VolatileDatabase) database;
	    String suiteID = db.getIdentifier();
	    Assert.assertEquals("Suite identifier not equals to the given one", suiteID, suiteIdentifier);

	} catch (GSException e) {
	    fail("Exception thrown");
	}
    }
}
