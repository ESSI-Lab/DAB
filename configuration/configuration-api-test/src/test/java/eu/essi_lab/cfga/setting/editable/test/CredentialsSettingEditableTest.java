package eu.essi_lab.cfga.setting.editable.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.setting.CredentialsSetting;

/**
 * @author Fabrizio
 */
public class CredentialsSettingEditableTest {

    @Test
    public void test() {

	CredentialsSetting setting = new CredentialsSetting();

	boolean valid = EditableSetting.test(setting);

	Assert.assertTrue(valid);
    }
}
