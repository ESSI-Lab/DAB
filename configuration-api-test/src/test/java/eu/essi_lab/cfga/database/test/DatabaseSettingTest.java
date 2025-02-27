package eu.essi_lab.cfga.database.test;

import static org.junit.Assert.fail;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.model.StorageInfo;

/**
 * @author Fabrizio
 */
public class DatabaseSettingTest {

    @Test
    public void cleanTest1() {

	DatabaseSetting setting = new DatabaseSetting();

	Assert.assertEquals(2, setting.getSettings().size());

	Assert.assertFalse(setting.isVolatile());

	setting.clean();

	Assert.assertEquals(2, setting.getSettings().size());

	Assert.assertFalse(setting.isVolatile());
    }

    @Test
    public void cleanTest2() {

	DatabaseSetting setting = new DatabaseSetting();

	setting.setVolatile(true);

	Assert.assertEquals(2, setting.getSettings().size());

	Assert.assertTrue(setting.isVolatile());

	setting.clean();

	Assert.assertEquals(2, setting.getSettings().size());

	Assert.assertTrue(setting.isVolatile());
    }

    @Test
    public void test() {

	DatabaseSetting databaseSetting = new DatabaseSetting();

	System.out.println(databaseSetting);

	try {
	    databaseSetting.createConfigurable();
	    fail("Exception not thrown");
	} catch (Exception e) {

	    // configurable class not set
	}

	initTest(databaseSetting);
	initTest(new DatabaseSetting(databaseSetting.getObject()));
	initTest(new DatabaseSetting(databaseSetting.getObject().toString()));
	initTest(SettingUtils.downCast(databaseSetting, DatabaseSetting.class, true));

	StorageInfo storageUri = new StorageInfo();

	storageUri.setUri("uri");
	storageUri.setName("dataBaseName");
	storageUri.setPassword("password");
	storageUri.setUser("user");
	storageUri.setIdentifier("folder");
	storageUri.setType("type");

	databaseSetting.setStorageUri(storageUri);

	test2(databaseSetting);
	test2(new DatabaseSetting(databaseSetting.getObject()));
	test2(new DatabaseSetting(databaseSetting.getObject().toString()));
	test2(SettingUtils.downCast(databaseSetting, DatabaseSetting.class, true));

	//
	//
	//

	databaseSetting.setVolatile(true);

	test2(databaseSetting);
	test2(new DatabaseSetting(databaseSetting.getObject()));
	test2(new DatabaseSetting(databaseSetting.getObject().toString()));
	test2(SettingUtils.downCast(databaseSetting, DatabaseSetting.class, true));

	try {
	    databaseSetting.setStorageUri(storageUri);
	    fail("Exception not thrown");
	} catch (UnsupportedOperationException ex) {
	}

	try {
	    databaseSetting.setDatabaseName("name");
	    fail("Exception not thrown");
	} catch (UnsupportedOperationException ex) {
	}

	try {
	    databaseSetting.setDatabaseUri("uri");
	    fail("Exception not thrown");
	} catch (UnsupportedOperationException ex) {
	}

	databaseSetting.setDatabaseName(DatabaseSetting.VOLATILE_DB_STORAGE_NAME);
	databaseSetting.setDatabaseUri(DatabaseSetting.VOLATILE_DB_URI);

	try {
	    databaseSetting.setDatabaseUser("user");
	    fail("Exception not thrown");
	} catch (UnsupportedOperationException ex) {
	}

	try {
	    databaseSetting.setDatabasePassword("pwd");
	    fail("Exception not thrown");
	} catch (UnsupportedOperationException ex) {
	}

	try {
	    databaseSetting.setConfigurationFolder("folder");
	    fail("Exception not thrown");
	} catch (UnsupportedOperationException ex) {
	}
	
	try {
	    databaseSetting.setDatabaseType("type");
	    fail("Exception not thrown");
	} catch (UnsupportedOperationException ex) {
	}

	//
	//
	//
    }

    private void initTest(DatabaseSetting databaseSetting) {

	Assert.assertFalse(databaseSetting.canBeCleaned());

	Assert.assertFalse(databaseSetting.isVolatile());

	Assert.assertTrue(databaseSetting.isEditable());
	Assert.assertFalse(databaseSetting.canBeDisabled());

	Assert.assertTrue(databaseSetting.getDatabaseType().isEmpty());

	Assert.assertNotNull(databaseSetting.asStorageInfo());

	String configurationFolder = databaseSetting.getConfigurationFolder();
	Assert.assertNull(configurationFolder);

	String databaseName = databaseSetting.getDatabaseName();
	Assert.assertNull(databaseName);

	String databaseUri = databaseSetting.getDatabaseUri();
	Assert.assertNull(databaseUri);

	String databaseUser = databaseSetting.getDatabaseUser();
	Assert.assertNull(databaseUser);

	String databasePassword = databaseSetting.getDatabasePassword();
	Assert.assertNull(databasePassword);

	StorageInfo storageUri = databaseSetting.asStorageInfo();

	Assert.assertNull(storageUri.getIdentifier());

	Assert.assertNull(storageUri.getName());

	Assert.assertNull(storageUri.getUri());

	Assert.assertNull(storageUri.getUser());

	Assert.assertNull(storageUri.getPassword());
    }

    private void test2(DatabaseSetting databaseSetting) {

	//
	// asStorageUri test
	//
	{
	    String databaseUri = databaseSetting.asStorageInfo().getUri();

	    if (databaseSetting.isVolatile()) {

		Assert.assertEquals(DatabaseSetting.VOLATILE_DB_URI, databaseUri);

	    } else {

		Assert.assertEquals("uri", databaseUri);
	    }

	    String storageName = databaseSetting.asStorageInfo().getName();

	    if (databaseSetting.isVolatile()) {

		Assert.assertEquals(DatabaseSetting.VOLATILE_DB_STORAGE_NAME, storageName);

	    } else {

		Assert.assertEquals("dataBaseName", storageName);
	    }

	    String configurationFolder = databaseSetting.asStorageInfo().getIdentifier();

	    if (databaseSetting.isVolatile()) {

		Assert.assertNull(configurationFolder);

	    } else {

		Assert.assertEquals("folder", configurationFolder);
	    }

	    String databasePassword = databaseSetting.asStorageInfo().getPassword();

	    if (databaseSetting.isVolatile()) {

		Assert.assertNull(databasePassword);

	    } else {

		Assert.assertEquals("password", databasePassword);
	    }

	    String user = databaseSetting.asStorageInfo().getUser();

	    if (databaseSetting.isVolatile()) {

		Assert.assertNull(user);

	    } else {

		Assert.assertEquals("user", user);
	    }
	    
	    Optional<String> type = databaseSetting.asStorageInfo().getType();

	    if (databaseSetting.isVolatile()) {

		Assert.assertFalse(type.isPresent());

	    } else {

		Assert.assertEquals("type", type.get());
	    }
	}
	//
	// setting test
	//

	{

	    String databaseUri = databaseSetting.getDatabaseUri();

	    if (databaseSetting.isVolatile()) {

		Assert.assertEquals(DatabaseSetting.VOLATILE_DB_URI, databaseUri);

	    } else {

		Assert.assertEquals("uri", databaseUri);
	    }

	    String storageName = databaseSetting.getDatabaseName();

	    if (databaseSetting.isVolatile()) {

		Assert.assertEquals(DatabaseSetting.VOLATILE_DB_STORAGE_NAME, storageName);

	    } else {

		Assert.assertEquals("dataBaseName", storageName);
	    }

	    String configurationFolder = databaseSetting.getConfigurationFolder();

	    if (databaseSetting.isVolatile()) {

		Assert.assertNull(configurationFolder);

	    } else {

		Assert.assertEquals("folder", configurationFolder);
	    }

	    String databasePassword = databaseSetting.getDatabasePassword();

	    if (databaseSetting.isVolatile()) {

		Assert.assertNull(databasePassword);

	    } else {

		Assert.assertEquals("password", databasePassword);
	    }

	    String user = databaseSetting.getDatabaseUser();

	    if (databaseSetting.isVolatile()) {

		Assert.assertNull(user);

	    } else {

		Assert.assertEquals("user", user);
	    }
	    
	    Optional<String> type = databaseSetting.getDatabaseType();

	    if (databaseSetting.isVolatile()) {

		Assert.assertFalse(type.isPresent());

	    } else {

		Assert.assertEquals("type", type.get());
	    }
	}
    }

    @Test
    public void downCastTest() {

	DatabaseSetting dbSetting = new DatabaseSetting();

	Setting setting = new Setting(dbSetting.getObject());

	Setting downCasted = SettingUtils.downCast(setting);

	Assert.assertEquals(dbSetting.getClass(), downCasted.getClass());

	// ----

	setting = new Setting(dbSetting.getObject().toString());

	downCasted = SettingUtils.downCast(setting);

	Assert.assertEquals(dbSetting.getClass(), downCasted.getClass());

	// ----

	downCasted = SettingUtils.downCast(dbSetting);

	Assert.assertEquals(dbSetting.getClass(), downCasted.getClass());
    }
}
