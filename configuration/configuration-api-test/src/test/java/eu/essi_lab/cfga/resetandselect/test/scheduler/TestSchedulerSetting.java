/**
 * 
 */
package eu.essi_lab.cfga.resetandselect.test.scheduler;

import java.util.Arrays;

import org.json.JSONObject;

import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting;

/**
 * @author Fabrizio
 */
public class TestSchedulerSetting extends SchedulerSetting {

    public static void main(String[] args) {

	TestSchedulerSetting testSchedulerSetting = new TestSchedulerSetting();
	SelectionUtils.deepClean(testSchedulerSetting);

	System.out.println(testSchedulerSetting);
    }

    /**
     * 
     */
    public TestSchedulerSetting() {

	setName("TEST Scheduler");

	Option<Integer> testOption = IntegerOptionBuilder.get().//
		withKey("testOption").//
		withLabel("TEST OPTION").//
		withSingleSelection().//
		withValues(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)).//
		withSelectedValue(2).//
		cannotBeDisabled().//
		build();

	addOption(testOption);
    }

    /**
     * @param object
     */
    public TestSchedulerSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public TestSchedulerSetting(String object) {

	super(object);
    }

}
