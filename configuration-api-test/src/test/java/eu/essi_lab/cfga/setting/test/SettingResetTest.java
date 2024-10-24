package eu.essi_lab.cfga.setting.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.model.StorageInfo;

/**
 * @author Fabrizio
 */
public class SettingResetTest {

    /**
     * 
     */
    @Test
    public void resetTest() {

	DatabaseSetting databaseSetting = new DatabaseSetting();

	resetTest1(databaseSetting);
	resetTest1(new DatabaseSetting(databaseSetting.getObject()));
	resetTest1(new DatabaseSetting(databaseSetting.getObject().toString()));

	databaseSetting.setConfigurationFolder("folder");
	databaseSetting.setDatabaseName("name");
	databaseSetting.setDatabasePassword("password");
	databaseSetting.setDatabaseUri("uri");
	databaseSetting.setDatabaseUser("user");

	resetTest2(databaseSetting);
	resetTest2(new DatabaseSetting(databaseSetting.getObject()));
	resetTest2(new DatabaseSetting(databaseSetting.getObject().toString()));

	databaseSetting.reset();

	resetTest1(databaseSetting);
	resetTest1(new DatabaseSetting(databaseSetting.getObject()));
	resetTest1(new DatabaseSetting(databaseSetting.getObject().toString()));
    }

    /**
     * @param databaseSetting
     */
    private void resetTest2(DatabaseSetting databaseSetting) {

	String configurationFolder = databaseSetting.getConfigurationFolder();
	Assert.assertEquals("folder", configurationFolder);

	String databaseName = databaseSetting.getDatabaseName();
	Assert.assertEquals("name", databaseName);

	String databaseUri = databaseSetting.getDatabaseUri();
	Assert.assertEquals("uri", databaseUri);

	String databaseUser = databaseSetting.getDatabaseUser();
	Assert.assertEquals("user", databaseUser);

	String databasePassword = databaseSetting.getDatabasePassword();
	Assert.assertEquals("password", databasePassword);

	StorageInfo storageUri = databaseSetting.asStorageUri();

	Assert.assertEquals("folder", storageUri.getIdentifier());

	Assert.assertEquals("name", storageUri.getName());

	Assert.assertEquals("uri", storageUri.getUri());

	Assert.assertEquals("user", storageUri.getUser());

	Assert.assertEquals("password", storageUri.getPassword());

    }

    /**
     * @param databaseSetting
     */
    private void resetTest1(DatabaseSetting databaseSetting) {

	Assert.assertFalse(databaseSetting.isVolatile());

	Assert.assertNotNull(databaseSetting.asStorageUri());

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

	StorageInfo storageUri = databaseSetting.asStorageUri();

	Assert.assertNull(storageUri.getIdentifier());

	Assert.assertNull(storageUri.getName());

	Assert.assertNull(storageUri.getUri());

	Assert.assertNull(storageUri.getUser());

	Assert.assertNull(storageUri.getPassword());

    }
}
