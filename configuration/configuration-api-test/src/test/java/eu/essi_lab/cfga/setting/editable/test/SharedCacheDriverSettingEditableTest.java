package eu.essi_lab.cfga.setting.editable.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.setting.driver.SharedCacheDriverSetting;

/**
 * @author Fabrizio
 */
public class SharedCacheDriverSettingEditableTest {

    @Test
    public void test() {

	SharedCacheDriverSetting setting = new SharedCacheDriverSetting();

	boolean valid = EditableSetting.test(setting);

	Assert.assertTrue(valid);
    }
}
