package eu.essi_lab.cfga.setting.editable.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.setting.WIS2Setting;

/**
 * @author boldrini
 */
public class WIS2SettingEditableTest {

    @Test
    public void test() {

	WIS2Setting setting = new WIS2Setting();

	boolean valid = EditableSetting.test(setting);

	Assert.assertTrue(valid);
    }
}
