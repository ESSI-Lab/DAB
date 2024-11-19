/**
 * 
 */
package eu.essi_lab.cfga.resetandselect.test;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.resetandselect.test.TestSetting5.TestObjectExension;
import eu.essi_lab.cfga.resetandselect.test.TestSetting5.TestValidator;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.IOStreamUtils;

/**
 * "testSetting5.json" is an instance of {@link TestSetting5} and have all the properties set outside the default
 * constructor, according to the {@link TestSetting5#main(String[])} method.<br>
 * This test verifies these properties first in the "testSetting5.json" setting, than in the setting resulting
 * from the {@link SelectionUtils#resetAndSelect(Setting, boolean)} method.
 * 
 * @author Fabrizio
 */
@Ignore
public class ResetAndSelectTest2 {

    @Test
    public void test() throws IOException {

	Setting targetSetting = new Setting(
		IOStreamUtils.asUTF8String(getClass().getClassLoader().getResourceAsStream("testSetting5.json")));

	test(targetSetting);

	Setting resetSetting = SelectionUtils.resetAndSelect(targetSetting, true);

	test(resetSetting);
    }

    /**
     * @param resetSetting
     */
    private void test(Setting setting) {

	Assert.assertFalse(setting.isEnabled());
	Assert.assertFalse(setting.canBeDisabled());
	Assert.assertFalse(setting.isVisible());
	Assert.assertFalse(setting.isEditable());
	Assert.assertEquals("description", setting.getDescription().get());

	Assert.assertFalse(setting.isCompactModeEnabled());
	Assert.assertTrue(setting.isFoldedModeEnabled());
	Assert.assertTrue(setting.canBeRemoved());
	Assert.assertFalse(setting.canBeCleaned());
	Assert.assertFalse(setting.isShowHeaderSet());

	Assert.assertEquals(TestObjectExension.class, setting.getOptionalExtensionClass().get());
	Assert.assertEquals(TestValidator.class, setting.getOptionalValidatorClass().get());

	Assert.assertEquals("identifier", setting.getIdentifier());
	Assert.assertEquals("Test setting 5", setting.getName());
	Assert.assertEquals("condigurableType", setting.getConfigurableType());
    }
}
