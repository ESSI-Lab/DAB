package eu.essi_lab.cfga.setting.editable.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting;

/**
 * @author Fabrizio
 */
public class OAuthSettingSettingEditableTest {

    @Test
    public void test() {

	OAuthSetting setting = new OAuthSetting();

	boolean valid = EditableSetting.test(setting);

	Assert.assertTrue(valid);
    }
}
