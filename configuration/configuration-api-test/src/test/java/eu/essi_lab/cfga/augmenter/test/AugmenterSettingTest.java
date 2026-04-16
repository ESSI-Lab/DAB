/**
 * 
 */
package eu.essi_lab.cfga.augmenter.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class AugmenterSettingTest {

    @Test
    public void downCastTest() {

	AugmenterSetting augmenterSetting = new AugmenterSetting();

	Setting setting = new Setting(augmenterSetting.getObject());

	Setting downCasted = SettingUtils.downCast(setting);

	Assert.assertEquals(augmenterSetting.getClass(), downCasted.getClass());

	// ----

	setting = new Setting(augmenterSetting.getObject().toString());

	downCasted = SettingUtils.downCast(setting);

	Assert.assertEquals(augmenterSetting.getClass(), downCasted.getClass());

	// ----

	downCasted = SettingUtils.downCast(augmenterSetting);

	Assert.assertEquals(augmenterSetting.getClass(), downCasted.getClass());
    }

}
