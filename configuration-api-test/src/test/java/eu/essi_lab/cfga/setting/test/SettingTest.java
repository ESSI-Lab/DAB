/**
 * 
 */
package eu.essi_lab.cfga.setting.test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.cfga.Selectable.SelectionMode;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.lib.utils.StringUtils;

/**
 * @author Fabrizio
 */
public class SettingTest {

    private class TestSetting extends Setting {
    }

    @Test
    public void test() throws Exception {

	Setting setting1 = new Setting();

	test1(setting1);
	test1(new Setting(setting1.getObject()));
	test1(new Setting(setting1.toString()));
	test1(new Setting(setting1.getObject().toString()));

	Option<String> configurationOption = new Option<>(String.class);
	configurationOption.setValues(Arrays.asList("a", "b", "c"));
	configurationOption.setKey("lettersOption");
	configurationOption.setLabel("Choose the letter");
	configurationOption.setRequired(true);

	setting1.addOption(configurationOption);

	// adds a boolean option
	Option<Boolean> booleanOption = new Option<>(Boolean.class);
	booleanOption.setKey("booleanOption");

	setting1.addOption(booleanOption);

	Setting setting1_2 = new Setting();
	setting1_2.setIdentifier("innerSetting1");
	setting1_2.setDescription("Description 1_2");
	setting1_2.setName("Setting 1_1");

	setting1.addSetting(setting1_2);

	SchedulerWorkerSetting setting1_3 = new SchedulerWorkerSetting();
	setting1_3.setIdentifier("innerSetting2");
	setting1_3.setDescription("Description 1_3");
	setting1_3.setName("Setting 1_2");

	setting1.addSetting(setting1_3);

	Assert.assertEquals(SelectionMode.UNSET, setting1.getSelectionMode());

	setting1.setConfigurableType("ConfigurableTest");
	setting1.setIdentifier("identifier");
	setting1.setDescription("desc");
	setting1.setName("name");

	setting1.setSelectionMode(SelectionMode.MULTI);

	setting1.setEditable(false);
	setting1.setCanBeDisabled(false);
	setting1.setShowHeader(false);
	setting1.setCanBeCleaned(false);
	setting1.setCanBeRemoved(true);
	setting1.setSelected(true);
	setting1.setVisible(false);
	setting1.setEnabled(false);
	setting1.enableCompactMode(false);
	setting1.enableFoldedMode(true);

	test2(setting1);

	test2(new Setting(setting1.getObject()));

	test2(new Setting(setting1.toString()));

	test2(new Setting(setting1.getObject().toString()));

	setting1.clearDescription();
	Assert.assertFalse(setting1.getDescription().isPresent());

	setting1.setSelectionMode(SelectionMode.UNSET);

	boolean removed = setting1.removeSetting(setting1_2);
	Assert.assertTrue(removed);

	removed = setting1.removeSetting(setting1_3.getIdentifier());
	Assert.assertTrue(removed);

	removed = setting1.removeSetting("unknown");
	Assert.assertFalse(removed);

	setting1.setEditable(true);
	setting1.setCanBeDisabled(true);

	test3(setting1);

	test3(new Setting(setting1.getObject()));

	test3(new Setting(setting1.toString()));

	test3(new Setting(setting1.getObject().toString()));
    }

    /**
     * @param setting
     */
    private void test1(Setting setting) {

	String identifier = setting.getIdentifier();

	Assert.assertTrue(StringUtils.isUUID(identifier));

	String name = setting.getName();
	Assert.assertEquals(identifier, name);

	Assert.assertTrue(setting.getObject().getString("type").equals("setting"));
	Assert.assertTrue(setting.getObject().getString("settingClass").equals(Setting.class.getName()));
	Assert.assertTrue(setting.getSettings().isEmpty());

	Assert.assertFalse(setting.isSelected());
	Assert.assertTrue(setting.isEnabled());
	Assert.assertTrue(setting.isVisible());
	Assert.assertTrue(setting.canBeDisabled());
	Assert.assertTrue(setting.isEditable());
	Assert.assertFalse(setting.canBeRemoved());
	Assert.assertTrue(setting.canBeCleaned());

	Assert.assertTrue(setting.isCompactModeEnabled());
	Assert.assertFalse(setting.isFoldedModeEnabled());

	Assert.assertTrue(setting.isShowHeaderSet());
    }

    /**
     * @param setting
     * @throws Exception
     */
    private void test2(Setting setting) throws Exception {

	Assert.assertTrue(setting.isSelected());
	Assert.assertTrue(setting.isEnabled());
	Assert.assertFalse(setting.isVisible());
	Assert.assertFalse(setting.canBeDisabled());
	Assert.assertFalse(setting.isEditable());
	Assert.assertFalse(setting.isShowHeaderSet());
	Assert.assertFalse(setting.canBeCleaned());

	Assert.assertTrue(setting.canBeRemoved());

	Assert.assertFalse(setting.isCompactModeEnabled());
	Assert.assertTrue(setting.isFoldedModeEnabled());

	String type = setting.getObject().getString("type");
	Assert.assertEquals("setting", type);

	Assert.assertEquals(SelectionMode.MULTI, setting.getSelectionMode());

	//
	//
	//

	List<Option<?>> options = setting.getOptions();
	Assert.assertEquals(2, options.size());

	List<Option<String>> stringOptions = setting.getOptions(String.class);
	Assert.assertEquals(1, stringOptions.size());

	List<Option<Boolean>> booleanOptions = setting.getOptions(Boolean.class);
	Assert.assertEquals(1, booleanOptions.size());

	List<Option<Integer>> integerOptions = setting.getOptions(Integer.class);
	Assert.assertEquals(0, integerOptions.size());

	Optional<Option<Boolean>> booleanOption = setting.getOption("booleanOption", Boolean.class);
	Assert.assertTrue(booleanOption.isPresent());
	Assert.assertEquals(booleanOption.get(), setting.getOption("booleanOption").get());

	Optional<Option<String>> stringOption = setting.getOption("lettersOption", String.class);
	Assert.assertTrue(stringOption.isPresent());
	Assert.assertEquals(stringOption.get(), setting.getOption("lettersOption").get());

	String id = setting.getIdentifier();
	Assert.assertEquals("identifier", id);

	String desc = setting.getDescription().get();
	Assert.assertEquals("desc", desc);

	String name = setting.getName();
	Assert.assertEquals("name", name);

	//
	//
	//

	List<Setting> settings = setting.//
		getSettings().//
		stream().//
		sorted((s1, s2) -> s1.getName().compareTo(s2.getName())).//
		collect(Collectors.toList());

	Assert.assertEquals(2, settings.size());

	Setting innerSetting1 = settings.get(0);
	String innerId1 = innerSetting1.getIdentifier();
	Assert.assertEquals("innerSetting1", innerId1);

	Setting innerSetting2 = settings.get(1);
	String innerId2 = innerSetting2.getIdentifier();
	Assert.assertEquals("innerSetting2", innerId2);

	//
	//
	//

	Optional<Setting> opt1 = setting.getSetting(innerId1);
	Assert.assertEquals(opt1.get().getIdentifier(), innerId1);

	opt1 = setting.getSetting(innerId1, Setting.class);
	Assert.assertEquals(opt1.get().getIdentifier(), innerId1);

	Optional<Setting> opt2 = setting.getSetting(innerId2);
	Assert.assertEquals(opt2.get().getIdentifier(), innerId2);

	Optional<SchedulerWorkerSetting> schedOpt2 = setting.getSetting(innerId2, SchedulerWorkerSetting.class);
	Assert.assertEquals(schedOpt2.get().getIdentifier(), innerId2);

	Optional<Setting> opt3 = setting.getSetting("abc");
	Assert.assertFalse(opt3.isPresent());

	//
	//
	//

	List<Setting> settingsList = setting.getSettings(Setting.class);
	Assert.assertEquals(1, settingsList.size());
	Assert.assertEquals(innerSetting1, settingsList.get(0));

	List<SchedulerWorkerSetting> schedSettingsList = setting.getSettings(SchedulerWorkerSetting.class);
	Assert.assertEquals(1, schedSettingsList.size());
	Assert.assertEquals(innerSetting2, schedSettingsList.get(0));

	List<TestSetting> testSettingsList = setting.getSettings(TestSetting.class);
	Assert.assertEquals(0, testSettingsList.size());

	//
	//
	//

	String configurableType = setting.getConfigurableType();
	Assert.assertEquals(configurableType, "ConfigurableTest");

	Configurable<Setting> configurable = setting.createConfigurable();
	Assert.assertEquals(configurable.getType(), configurableType);

	Assert.assertEquals(setting, configurable.getSetting());
    }

    /**
     * @param setting
     * @throws Exception
     */
    private void test3(Setting setting) throws Exception {

	Assert.assertTrue(setting.canBeDisabled());
	Assert.assertTrue(setting.isEditable());

	Assert.assertEquals(SelectionMode.UNSET, setting.getSelectionMode());

	List<Setting> settings = setting.getSettings();

	Assert.assertEquals(0, settings.size());
    }
}
