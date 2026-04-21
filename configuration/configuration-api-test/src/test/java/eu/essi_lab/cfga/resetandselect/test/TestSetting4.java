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
 * The "synch-test-configuration-4.json" has this test setting WITHOUT the sub-setting in order to provide a difference
 * between the config setting and its current
 * version thus simulating a situation where {@link TestSetting4} is changed due to the adding of one sub-setting
 * 
 * @author Fabrizio
 */
public class TestSetting4 extends Setting {

    /**
     * 
     */
    private static final String SUB_SETTING_ID = "testSetting4SubSettingId";

    public static void main(String[] args) {

	TestSetting4 testSetting4 = new TestSetting4();

	SelectionUtils.deepClean(testSetting4);

	System.out.println(testSetting4);
    }

    /**
     * 
     */
    public TestSetting4() {

	setName("TestSetting 4");
	enableCompactMode(false);
	setCanBeDisabled(false);

	Setting setting = new Setting();
	setting.setName("Subsetting of TestSetting4");
	setting.setIdentifier(SUB_SETTING_ID);

	addSetting(setting);

	//
	// option 1
	//

	Option<BooleanChoice> option1 = BooleanChoiceOptionBuilder.get().//
		withKey("option1").//
		withLabel("Option 1").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.TRUE).//
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
		withSelectedValue(3).//
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
		withSelectedValue("C").//
		cannotBeDisabled().//
		build();

	setting.addOption(option3);
    }
}
