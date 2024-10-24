/**
 * 
 */
package eu.essi_lab.cfga.resetandselect.test;

import java.util.Arrays;

import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.BooleanChoiceOptionBuilder;
import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * The "synch-test-configuration-3.json" has this test setting with the sub-setting.<br>
 * Here the sub-setting 1 is NOT put, in order to provide a difference between the config setting and its current
 * version thus simulating a situation where {@link TestSetting3} is changed due to the
 * removal of one of its sub-settings
 * 
 * @author Fabrizio
 */
public class TestSetting3 extends Setting {

    /**
     * 
     */
    private static final String SUB_SETTING_ID = "testSetting3SubSettingId";

    public static void main(String[] args) {

	TestSetting3 testSetting3 = new TestSetting3();

	SelectionUtils.deepClean(testSetting3);

	System.out.println(testSetting3);
    }

    /**
     * 
     */
    public TestSetting3() {

	setName("TestSetting 3");
	enableCompactMode(false);
	setCanBeDisabled(false);

	Setting setting = new Setting();
	setting.setName("Subsetting of TestSetting3");
	setting.setIdentifier(SUB_SETTING_ID);

	// addSetting(setting);

	//
	// option 1
	//

	Option<BooleanChoice> option1 = BooleanChoiceOptionBuilder.get().//
		withKey("option1").//
		withLabel("Option 1").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.FALSE).//
		cannotBeDisabled().//
		build();

	setting.addOption(option1);

	//
	// option 2
	//

	Option<Integer> option2 = IntegerOptionBuilder.get().//
		withKey("option2").//
		withLabel("Option 2").//
		withSingleSelection().//
		withValues(Arrays.asList(0, 1, 2, 3, 4)).//
		withSelectedValue(1).//
		cannotBeDisabled().//
		build();

	setting.addOption(option2);

	//
	// option 3
	//

	Option<String> option3 = StringOptionBuilder.get().//
		withKey("option3").//
		withLabel("Option 3").//
		withSingleSelection().//
		withValues(Arrays.asList("A", "B", "C", "D")).//
		withSelectedValue("B").//
		cannotBeDisabled().//
		build();

	setting.addOption(option3);
    }
}
