package eu.essi_lab.cfga.setting.editable.test;

import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.gs.setting.sessioncoordinator.*;
import org.junit.*;

/**
 * @author Fabrizio
 */
public class SessionCoordinatorSettingEditableTest {

    @Test
    public void test() {

	SessionCoordinatorSetting setting = new SessionCoordinatorSetting();

	boolean valid = EditableSetting.test(setting);

	Assert.assertTrue(valid);
    }
}
