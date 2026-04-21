package eu.essi_lab.shared.driver.setting.test;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.gs.setting.driver.SharedCacheDriverSetting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.model.shared.SharedContent.SharedContentCategory;

/**
 * @author Fabrizio
 */
public class SharedCacheDriverSettingTest {

    @Test
    public void test() {

	SharedCacheDriverSetting setting = new SharedCacheDriverSetting();

	initTest(setting);
	initTest(new SharedCacheDriverSetting(setting.getObject()));
	initTest(new SharedCacheDriverSetting(setting.getObject().toString()));
	initTest(SettingUtils.downCast(setting, SharedCacheDriverSetting.class, true));

	setting.setCategory(SharedContentCategory.DATABASE_CACHE);

	categoryTest(setting);
	categoryTest(new SharedCacheDriverSetting(setting.getObject()));
	categoryTest(new SharedCacheDriverSetting(setting.getObject().toString()));
	categoryTest(SettingUtils.downCast(setting, SharedCacheDriverSetting.class, true));

	setting.selectRetentionTime(16);

	retentionTimeTest(setting);
	retentionTimeTest(new SharedCacheDriverSetting(setting.getObject()));
	retentionTimeTest(new SharedCacheDriverSetting(setting.getObject().toString()));
	retentionTimeTest(SettingUtils.downCast(setting, SharedCacheDriverSetting.class, true));

	SelectionUtils.deepClean(setting);

	categoryTest(setting);
	categoryTest(new SharedCacheDriverSetting(setting.getObject()));
	categoryTest(new SharedCacheDriverSetting(setting.getObject().toString()));
	categoryTest(SettingUtils.downCast(setting, SharedCacheDriverSetting.class, true));
    }

    /**
     * @param setting
     */
    private void retentionTimeTest(SharedCacheDriverSetting setting) {

	int retentionTime = setting.getSelectedRetentionTime();

	Assert.assertEquals(new Integer(16), Integer.valueOf(retentionTime));
    }

    /**
     * @param setting
     */
    private void initTest(SharedCacheDriverSetting setting) {

	Assert.assertFalse(setting.canBeCleaned());

	SharedContentCategory category = setting.getCategory();

	Assert.assertEquals(SharedContentCategory.LOCAL_CACHE, category);

	Optional<DatabaseSetting> databaseCacheSetting = setting.getDatabaseCacheSetting();

	Assert.assertTrue(databaseCacheSetting.isPresent());

	Assert.assertFalse(databaseCacheSetting.get().isSelected());

	int selectedRetentionTime = setting.getSelectedRetentionTime();

	Assert.assertEquals(SharedCacheDriverSetting.DEFAULT_RETENTION_TIME, Integer.valueOf(selectedRetentionTime));
    }

    /**
     * @param setting
     */
    private void categoryTest(SharedCacheDriverSetting setting) {

	SharedContentCategory category = setting.getCategory();

	Assert.assertEquals(SharedContentCategory.DATABASE_CACHE, category);
    }
}
