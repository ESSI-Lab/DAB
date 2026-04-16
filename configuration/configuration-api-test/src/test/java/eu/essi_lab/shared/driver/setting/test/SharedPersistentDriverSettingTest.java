package eu.essi_lab.shared.driver.setting.test;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.gs.setting.driver.SharedPersistentDriverSetting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.model.shared.SharedContent.SharedContentCategory;

/**
 * @author Fabrizio
 */
public class SharedPersistentDriverSettingTest {

    @Test
    public void test() {

	SharedPersistentDriverSetting setting = new SharedPersistentDriverSetting();

	initTest(setting);
	initTest(new SharedPersistentDriverSetting(setting.getObject()));
	initTest(new SharedPersistentDriverSetting(setting.getObject().toString()));
	initTest(SettingUtils.downCast(setting, SharedPersistentDriverSetting.class, true));

	setting.setCategory(SharedContentCategory.ELASTIC_SEARCH_PERSISTENT);

	categoryTest(setting);
	categoryTest(new SharedPersistentDriverSetting(setting.getObject()));
	categoryTest(new SharedPersistentDriverSetting(setting.getObject().toString()));
	categoryTest(SettingUtils.downCast(setting, SharedPersistentDriverSetting.class, true));

	SelectionUtils.deepClean(setting);

	categoryTest(setting);
	categoryTest(new SharedPersistentDriverSetting(setting.getObject()));
	categoryTest(new SharedPersistentDriverSetting(setting.getObject().toString()));
	categoryTest(SettingUtils.downCast(setting, SharedPersistentDriverSetting.class, true));
    }

    /**
     * @param setting
     */
    private void initTest(SharedPersistentDriverSetting setting) {

	Assert.assertFalse(setting.canBeCleaned());

	SharedContentCategory category = setting.getCategory();

	Assert.assertEquals(SharedContentCategory.LOCAL_PERSISTENT, category);

	Optional<DatabaseSetting> databaseCacheSetting = setting.getElasticSearchSetting();

	Assert.assertTrue(databaseCacheSetting.isPresent());

	Assert.assertFalse(databaseCacheSetting.get().isSelected());
    }

    /**
     * @param setting
     */
    private void categoryTest(SharedPersistentDriverSetting setting) {

	SharedContentCategory category = setting.getCategory();

	Assert.assertEquals(SharedContentCategory.ELASTIC_SEARCH_PERSISTENT, category);
    }
}
