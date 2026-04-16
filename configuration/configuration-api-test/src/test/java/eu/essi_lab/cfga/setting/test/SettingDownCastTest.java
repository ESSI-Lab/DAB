package eu.essi_lab.cfga.setting.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;

/**
 * @author Fabrizio
 */
public class SettingDownCastTest {

    @Test
    public void downCastTest() {

	SchedulerWorkerSetting schedulingSetting = new SchedulerWorkerSetting();

	Setting setting = new Setting(schedulingSetting.getObject());

	Setting downCasted = SettingUtils.downCast(setting);

	Assert.assertEquals(schedulingSetting.getClass(), downCasted.getClass());

	// ----

	setting = new Setting(schedulingSetting.getObject().toString());

	downCasted = SettingUtils.downCast(setting);

	Assert.assertEquals(schedulingSetting.getClass(), downCasted.getClass());

	// ----

	downCasted = SettingUtils.downCast(schedulingSetting);

	Assert.assertEquals(schedulingSetting.getClass(), downCasted.getClass());
    }
}
