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
 * The "synch-test-configuration-1.json" has this test setting with all the 3 options.<br>
 * Here the option 1 is NOT put, in order to provide a difference between the config setting and its current
 * version thus simulating a situation where this setting is changed due to the removal of one of its options
 * 
 * @author Fabrizio
 */
public class TestSetting1 extends Setting {

    public static void main(String[] args) {

	TestSetting1 testSetting1 = new TestSetting1();

	testSetting1.getOption("option2").get().select(v -> v.equals(2));

	testSetting1.getOption("option3").get().select(v -> v.equals("C"));

	SelectionUtils.deepClean(testSetting1);

	System.out.println(testSetting1);
    }

    /**
     * 
     */
    public TestSetting1() {

	setName("TestSetting 1");
	enableCompactMode(false);
	setCanBeDisabled(false);

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

	// addOption(option1);

	//
	// option 2
	//

	Option<Integer> option2 = IntegerOptionBuilder.get().//
		withKey("option2").//
		withLabel("Option 2").//
		withSingleSelection().//
		withValues(Arrays.asList(0, 1, 2, 3, 4)).//
		withSelectedValue(0).//
		cannotBeDisabled().//
		build();

	addOption(option2);

	//
	// option 3
	//

	Option<String> option3 = StringOptionBuilder.get().//
		withKey("option3").//
		withLabel("Option 3").//
		withSingleSelection().//
		withValues(Arrays.asList("A", "B", "C", "D")).//
		withSelectedValue("A").//
		cannotBeDisabled().//
		build();

	addOption(option3);
    }
}
