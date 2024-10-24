package eu.essi_lab.cfga.setting.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class SettingVisibleTest {

    @Test
    public void visibleTest() {

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

	Assert.assertTrue(setting.isVisible());
	Assert.assertTrue(setting2.isVisible());

	Assert.assertEquals(setting.getOptions().size(), setting.getOptions().stream().filter(o -> o.isVisible()).count());
	Assert.assertEquals(setting2.getOptions().size(), setting2.getOptions().stream().filter(o -> o.isVisible()).count());

	//
	//
	//

	setting.setVisible(false);

	Assert.assertFalse(setting.isVisible());
	Assert.assertFalse(setting2.isVisible());

	Assert.assertEquals(0, setting.getOptions().stream().filter(o -> o.isVisible()).count());
	Assert.assertEquals(0, setting2.getOptions().stream().filter(o -> o.isVisible()).count());

	//
	//
	//

	setting.setVisible(true);

	Assert.assertTrue(setting.isVisible());
	Assert.assertTrue(!setting2.isVisible());

	Assert.assertEquals(0, setting.getOptions().stream().filter(o -> o.isVisible()).count());
	Assert.assertEquals(0, setting2.getOptions().stream().filter(o -> o.isVisible()).count());

	//
	//
	//

	setting = new Setting();
	setting.setVisible(false);

	Assert.assertFalse(setting2.isVisible());

	setting.addSetting(setting2);

	Assert.assertFalse(setting2.isVisible());

	option1.setVisible(true);

	Assert.assertTrue(option1.isVisible());

	setting.addOption(option1);

	Assert.assertFalse(option1.isVisible());
    }
}
