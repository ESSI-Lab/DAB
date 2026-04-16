package eu.essi_lab.cfga.setting.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class SettingEnabledTest {

    @Test
    public void enabledTest() {

	Setting setting = new Setting();

	Option<String> option1 = new Option<>(String.class);
	option1.setKey("option1");

	Option<String> option2 = new Option<>(String.class);
	option2.setKey("option1");

	Option<String> option3 = new Option<>(String.class);
	option3.setKey("option3");

	setting.addOption(option1);
	setting.addOption(option2);
	setting.addOption(option3);

	// ---

	Setting setting2 = new Setting();

	Option<String> option21 = new Option<>(String.class);
	option21.setKey("option11");

	Option<String> option22 = new Option<>(String.class);
	option22.setKey("option22");

	setting2.addOption(option21);
	setting2.addOption(option22);

	setting.addSetting(setting2);

	//
	//
	//

	Assert.assertEquals(1, setting.getSettings().size());
	Assert.assertEquals(0, setting2.getSettings().size());

	//
	//
	//

	Assert.assertTrue(setting.isEnabled());
	Assert.assertTrue(setting2.isEnabled());

	Assert.assertEquals(setting.getOptions().size(), setting.getOptions().stream().filter(o -> o.isEnabled()).count());
	Assert.assertEquals(setting2.getOptions().size(), setting2.getOptions().stream().filter(o -> o.isEnabled()).count());

	//
	//
	//

	setting.setEnabled(false);

	Assert.assertFalse(setting.isEnabled());
	Assert.assertFalse(setting2.isEnabled());

	Assert.assertEquals(0, setting.getOptions().stream().filter(o -> o.isEnabled()).count());
	Assert.assertEquals(0, setting2.getOptions().stream().filter(o -> o.isEnabled()).count());

	//
	//
	//

	setting.setEnabled(true);

	Assert.assertTrue(setting.isEnabled());
	Assert.assertFalse(setting2.isEnabled());

	Assert.assertEquals(0, setting.getOptions().stream().filter(o -> o.isEnabled()).count());
	Assert.assertEquals(0, setting2.getOptions().stream().filter(o -> o.isEnabled()).count());

	//
	//
	//

	setting = new Setting();
	setting.setEnabled(false);

	Assert.assertFalse(setting2.isEnabled());

	setting.addSetting(setting2);

	Assert.assertFalse(setting2.isEnabled());

	option1.setEnabled(true);

	Assert.assertTrue(option1.isEnabled());

	setting.addOption(option1);

	Assert.assertFalse(option1.isEnabled());
	
	//
	// test with options that cannot be disabled
	//
	
	Setting setting3 = new Setting();
	
	Option<String> cannotBeDisabledOption = new Option<>(String.class);
	cannotBeDisabledOption.setKey("cannotBeDisabledOption");
	cannotBeDisabledOption.setCanBeDisabled(false);
	
	setting3.addOption(cannotBeDisabledOption);
	
	Assert.assertTrue(setting3.isEnabled());
	Assert.assertTrue(cannotBeDisabledOption.isEnabled());
	
	setting3.setEnabled(false);

	Assert.assertFalse(setting3.isEnabled());
	Assert.assertTrue(cannotBeDisabledOption.isEnabled());	
    }

}
