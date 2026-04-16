package eu.essi_lab.cfga.setting.editable.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting;

/**
 * 
 * @author Fabrizio
 *
 */
public class SystemSettingEditableTest {

    @Test
    public void test() {

	SystemSetting setting = new SystemSetting();

	boolean valid = EditableSetting.test(setting);

	Assert.assertTrue(valid);
    }
}
