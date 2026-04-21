package eu.essi_lab.cfga.setting.editable.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;

/**
 * @author Fabrizio
 */
public class CustomTaskSettingEditableTest {

    @Test
    public void test() {

	CustomTaskSetting setting = new CustomTaskSetting();

	boolean valid = EditableSetting.test(setting);

	Assert.assertTrue(valid);
    }
}
