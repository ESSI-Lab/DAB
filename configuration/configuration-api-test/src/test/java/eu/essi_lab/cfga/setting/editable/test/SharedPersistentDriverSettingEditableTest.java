package eu.essi_lab.cfga.setting.editable.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.setting.driver.SharedPersistentDriverSetting;

/**
 * @author Fabrizio
 */
public class SharedPersistentDriverSettingEditableTest {

    @Test
    public void test() {

	SharedPersistentDriverSetting setting = new SharedPersistentDriverSetting();

	boolean valid = EditableSetting.test(setting);

	Assert.assertTrue(valid);
    }
}
