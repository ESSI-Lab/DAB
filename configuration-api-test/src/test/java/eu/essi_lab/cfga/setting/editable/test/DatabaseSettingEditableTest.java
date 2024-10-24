package eu.essi_lab.cfga.setting.editable.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;

/**
 * @author Fabrizio
 */
public class DatabaseSettingEditableTest {

    @Test
    public void test() {

	DatabaseSetting setting = new DatabaseSetting();

	boolean valid = EditableSetting.test(setting);

	Assert.assertTrue(valid);
    }
}
