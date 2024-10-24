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
 * The "synch-test-configuration-2.json" has this test setting without option 1 in order to provide a difference between
 * the config setting and its current
 * version thus simulating a situation where this setting is changed due to the adding of one option
 * 
 * @author Fabrizio
 */
public class TestSetting2 extends Setting {

    public static void main(String[] args) {

	TestSetting2 testSetting1 = new TestSetting2();

	testSetting1.getOption("option2").get().select(v -> v.equals(2));

	testSetting1.getOption("option3").get().select(v -> v.equals("C"));

	SelectionUtils.deepClean(testSetting1);

	System.out.println(testSetting1);
    }

    /**
     * 
     */
    public TestSetting2() {

	setName("TestSetting 2");
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
		withSelectedValue(BooleanChoice.TRUE).//
		cannotBeDisabled().//
		build();

	addOption(option1);

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
