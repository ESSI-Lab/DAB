package eu.essi_lab.cfga.setting.editable.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting;

/**
 * @author Fabrizio
 */
public class SchedulerSettingEditableTest {

    @Test
    public void test() {

	SchedulerSetting setting = new SchedulerSetting();

	boolean valid = EditableSetting.test(setting);

	Assert.assertTrue(valid);
    }
}