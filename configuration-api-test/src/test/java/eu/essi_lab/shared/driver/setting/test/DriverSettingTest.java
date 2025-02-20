/**
 * 
 */
package eu.essi_lab.shared.driver.setting.test;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.Selectable.SelectionMode;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.gs.setting.driver.SharedCacheDriverSetting;
import eu.essi_lab.cfga.gs.setting.driver.SharedPersistentDriverSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.shared.SharedContent.SharedContentCategory;

/**
 * @author Fabrizio
 */
public class DriverSettingTest {

    private static final int DEFAULT_SELECTED_VALUE = 12;

    @Test
    public void sharedPersistenDriverSettingTest() {

	SharedPersistentDriverSetting setting = new SharedPersistentDriverSetting();

	try {
	    setting.getConfigurableType();
	    fail("Not thrown");
	} catch (Exception ex) {
	    // OK
	}

	sharedPersistentDriverSettingTest1(setting);
	sharedPersistentDriverSettingTest1(new SharedPersistentDriverSetting(setting.getObject()));
	sharedPersistentDriverSettingTest1(new SharedPersistentDriverSetting(setting.getObject().toString()));

	setting.setCategory(SharedContentCategory.ELASTIC_SEARCH_PERSISTENT);
	setting.setSelectionMode(SelectionMode.MULTI);

	setting.getElasticSearchSetting().get().setDatabaseName("name");

	sharedPersistentDriverSettingTest2(setting);
	sharedPersistentDriverSettingTest2(new SharedPersistentDriverSetting(setting.getObject()));
	sharedPersistentDriverSettingTest2(new SharedPersistentDriverSetting(setting.getObject().toString()));
    }

    /**
     * @param setting
     */
    private void sharedPersistentDriverSettingTest1(SharedPersistentDriverSetting setting) {

	Assert.assertEquals(SelectionMode.SINGLE, setting.getSelectionMode());

	SharedContentCategory category = setting.getCategory();

	Assert.assertEquals(SharedContentCategory.LOCAL_PERSISTENT, category);

	DatabaseSetting databaseCacheSetting = setting.getElasticSearchSetting().get();

	StorageInfo asStorageUri = databaseCacheSetting.asStorageInfo();

	Assert.assertNull(asStorageUri.getIdentifier());
	Assert.assertNull(asStorageUri.getPassword());
	Assert.assertNull(asStorageUri.getName());
	Assert.assertNull(asStorageUri.getUri());
	Assert.assertNull(asStorageUri.getUser());

	Assert.assertNotNull(setting.getName());
    }

    /**
     * @param sharedCacheDriverSetting
     */
    private void sharedPersistentDriverSettingTest2(SharedPersistentDriverSetting setting) {

	Assert.assertEquals(SelectionMode.MULTI, setting.getSelectionMode());

	SharedContentCategory category = setting.getCategory();

	Assert.assertEquals(SharedContentCategory.ELASTIC_SEARCH_PERSISTENT, category);

	DatabaseSetting databaseCacheSetting = setting.getElasticSearchSetting().get();

	StorageInfo asStorageUri = databaseCacheSetting.asStorageInfo();

	Assert.assertEquals("name", asStorageUri.getName());
    }

    @Test
    public void sharedCacheDriverSettingRetetionTimeOptionTest() {

	SharedCacheDriverSetting setting = new SharedCacheDriverSetting();

	Option<Integer> option = setting.getOptions(Integer.class).get(0);

	SelectionMode multiSelectionMode = option.getSelectionMode();
	Assert.assertEquals(SelectionMode.SINGLE, multiSelectionMode);

	{
	    //
	    // values are from 0 to 24 included, step by 4
	    //
	    List<Integer> values = option.getValues();
	    boolean passed = true;
	    for (int v = 4, i = 0; i < values.size(); v += 4, i++) {

		passed &= values.get(i) == v;
	    }
	    Assert.assertTrue(passed);

	    int value = option.getValue();
	    Assert.assertEquals(values.get(0), new Integer(value));

	    Optional<Integer> optionalValue = option.getOptionalValue();
	    Assert.assertEquals(new Integer(4), optionalValue.get());

	    //
	    // The selected value is 6
	    //

	    List<Integer> selectedValues = option.getSelectedValues();
	    Assert.assertEquals(1, selectedValues.size());
	    Assert.assertEquals(new Integer(DEFAULT_SELECTED_VALUE), selectedValues.get(0));

	    int selectedValue = option.getSelectedValue();
	    Assert.assertEquals(DEFAULT_SELECTED_VALUE, selectedValue);

	    Optional<Integer> optSelValue = option.getOptionalSelectedValue();
	    Assert.assertEquals(new Integer(DEFAULT_SELECTED_VALUE), optSelValue.get());

	}

	//
	// Removing all unselected values
	//

	option.clean();

	{

	    //
	    // only the value 6 remains
	    //
	    List<Integer> values = option.getValues();

	    Assert.assertEquals(1, values.size());
	    Assert.assertEquals(new Integer(DEFAULT_SELECTED_VALUE), values.get(0));

	    int value = option.getValue();
	    Assert.assertEquals(DEFAULT_SELECTED_VALUE, value);

	    Optional<Integer> optionalValue = option.getOptionalValue();
	    Assert.assertEquals(new Integer(DEFAULT_SELECTED_VALUE), optionalValue.get());

	    //
	    // The selected value is still DEFAULT_SELECTED_VALUE
	    //

	    List<Integer> selectedValues = option.getSelectedValues();
	    Assert.assertEquals(1, selectedValues.size());
	    Assert.assertEquals(new Integer(DEFAULT_SELECTED_VALUE), selectedValues.get(0));

	    int selectedValue = option.getSelectedValue();
	    Assert.assertEquals(DEFAULT_SELECTED_VALUE, selectedValue);

	    Optional<Integer> optSelValue = option.getOptionalSelectedValue();
	    Assert.assertEquals(new Integer(DEFAULT_SELECTED_VALUE), optSelValue.get());
	}
    }

    @Test
    public void sharedCacheDriverSettingTest() {

	SharedCacheDriverSetting setting = new SharedCacheDriverSetting();

	try {
	    setting.getConfigurableType();
	    fail("Not thrown");
	} catch (Exception ex) {
	    // OK
	}

	sharedCacheDriverSettingTest1(setting);
	sharedCacheDriverSettingTest1(new SharedCacheDriverSetting(setting.getObject()));
	sharedCacheDriverSettingTest1(new SharedCacheDriverSetting(setting.getObject().toString()));

	setting.setCategory(SharedContentCategory.DATABASE_CACHE);
	setting.setSelectionMode(SelectionMode.MULTI);

	setting.disableCacheCleaningTime();
	setting.getDatabaseCacheSetting().get().setDatabaseName("name");

	sharedCacheDriverSettingTest2(setting);
	sharedCacheDriverSettingTest2(new SharedCacheDriverSetting(setting.getObject()));
	sharedCacheDriverSettingTest2(new SharedCacheDriverSetting(setting.getObject().toString()));
    }

    private void sharedCacheDriverSettingTest1(SharedCacheDriverSetting setting) {

	Assert.assertEquals(SelectionMode.SINGLE, setting.getSelectionMode());

	SharedContentCategory category = setting.getCategory();

	Assert.assertEquals(SharedContentCategory.LOCAL_CACHE, category);

	DatabaseSetting databaseCacheSetting = setting.getDatabaseCacheSetting().get();

	StorageInfo asStorageUri = databaseCacheSetting.asStorageInfo();

	Assert.assertNull(asStorageUri.getIdentifier());
	Assert.assertNull(asStorageUri.getPassword());
	Assert.assertNull(asStorageUri.getName());
	Assert.assertNull(asStorageUri.getUri());
	Assert.assertNull(asStorageUri.getUser());

	Assert.assertEquals(DEFAULT_SELECTED_VALUE, setting.getSelectedRetentionTime());

	Assert.assertNotNull(setting.getName());
    }

    /**
     * @param setting
     */
    private void sharedCacheDriverSettingTest2(SharedCacheDriverSetting setting) {

	Assert.assertEquals(SelectionMode.MULTI, setting.getSelectionMode());

	SharedContentCategory category = setting.getCategory();

	Assert.assertEquals(SharedContentCategory.DATABASE_CACHE, category);

	DatabaseSetting databaseCacheSetting = setting.getDatabaseCacheSetting().get();

	StorageInfo asStorageUri = databaseCacheSetting.asStorageInfo();

	Assert.assertEquals("name", asStorageUri.getName());

	Assert.assertEquals(DEFAULT_SELECTED_VALUE, setting.getSelectedRetentionTime());
    }
}
