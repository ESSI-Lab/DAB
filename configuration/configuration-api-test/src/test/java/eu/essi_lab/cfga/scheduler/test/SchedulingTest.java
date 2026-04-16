package eu.essi_lab.cfga.scheduler.test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting.SchedulingGroup;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class SchedulingTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void test() {

	Scheduling scheduling = new Scheduling();

	schedulingTypeTest(scheduling);
	schedulingTypeTest(new Scheduling(scheduling.getObject()));
	schedulingTypeTest(new Scheduling(scheduling.getObject().toString()));
	schedulingTypeTest(SettingUtils.downCast(scheduling, Scheduling.class, true));
	

	defaultValuesTest(scheduling, false);
	defaultValuesTest(new Scheduling(scheduling.getObject()), false);
	defaultValuesTest(new Scheduling(scheduling.getObject().toString()), false);
	defaultValuesTest(SettingUtils.downCast(scheduling, Scheduling.class, true), false);

	scheduling.setEnabled(true);

	defaultValuesTest(scheduling, true);
	defaultValuesTest(new Scheduling(scheduling.getObject()), true);
	defaultValuesTest(new Scheduling(scheduling.getObject().toString()), true);
	defaultValuesTest(SettingUtils.downCast(scheduling, Scheduling.class, true), true);

	scheduling.setStartTime("2000-10-18");
	scheduling.setRunIndefinitely();

	runIndefinitelyTest(scheduling);
	runIndefinitelyTest(new Scheduling(scheduling.getObject()));
	runIndefinitelyTest(new Scheduling(scheduling.getObject().toString()));
	runIndefinitelyTest(SettingUtils.downCast(scheduling, Scheduling.class, true));

	scheduling.setRunOnce();

	runOnceSetTest(scheduling);
	runOnceSetTest(new Scheduling(scheduling.getObject()));
	runOnceSetTest(new Scheduling(scheduling.getObject().toString()));
	runOnceSetTest(SettingUtils.downCast(scheduling, Scheduling.class, true));

	// now the scheduler is no longer set to run once
	scheduling.setRepeatCount(10);

	repeatCount10Test(scheduling);
	repeatCount10Test(new Scheduling(scheduling.getObject()));
	repeatCount10Test(new Scheduling(scheduling.getObject().toString()));
	repeatCount10Test(SettingUtils.downCast(scheduling, Scheduling.class, true));

	// when set to run once, the repeat count value is cleared
	scheduling.setRunOnce();

	runOnceAgain(scheduling);
	runOnceAgain(new Scheduling(scheduling.getObject()));
	runOnceAgain(new Scheduling(scheduling.getObject().toString()));
	runOnceAgain(SettingUtils.downCast(scheduling, Scheduling.class, true));

	// when the end time is set, the repeat count value is removed because the scheduler
	// runs until the date is reached
	scheduling.setRepeatCount(15);
	scheduling.setEndTime("2030-10-18");
	scheduling.setRepeatInterval(5, TimeUnit.MINUTES);

	endTimeAndRepeatIntervalTest(scheduling);
	endTimeAndRepeatIntervalTest(new Scheduling(scheduling.getObject()));
	endTimeAndRepeatIntervalTest(new Scheduling(scheduling.getObject().toString()));
	endTimeAndRepeatIntervalTest(SettingUtils.downCast(scheduling, Scheduling.class, true));

	scheduling.setStartTime(ISO8601DateTimeUtils.parseISO8601ToDate("2020-10-18").get());
	scheduling.setEndTime(ISO8601DateTimeUtils.parseISO8601ToDate("2021-12-18").get());

	test5(scheduling, "2020-10-18", "2021-12-18");
	test5(new Scheduling(scheduling.getObject()), "2020-10-18", "2021-12-18");
	test5(new Scheduling(scheduling.getObject().toString()), "2020-10-18", "2021-12-18");
	test5(SettingUtils.downCast(scheduling, Scheduling.class, true), "2020-10-18", "2021-12-18");

	expectedException.expect(IllegalArgumentException.class);
	scheduling.setRepeatCount(0);

    }

    /**
     * 
     */
    private void schedulingTypeTest(Scheduling scheduling) {

	Assert.assertEquals(Scheduling.SCHEDULING_OBJECT_TYPE, scheduling.getObjectType());
    }

    /**
     * @param scheduling
     * @param enabled
     */
    private void defaultValuesTest(Scheduling scheduling, boolean enabled) {

	Assert.assertFalse(scheduling.isEditable());

	Assert.assertEquals(scheduling.isEnabled(), enabled);

	//
	//
	//

	Assert.assertTrue(scheduling.isRunOnceSet());
	Assert.assertFalse(scheduling.isRunIndefinitelySet());

	//
	//
	//

	Assert.assertFalse(scheduling.getRepeatCount().isPresent());

	Assert.assertEquals(new Integer(1), scheduling.getRepeatInterval());

	Assert.assertEquals(TimeUnit.DAYS, scheduling.getRepeatIntervalUnit());

	//
	//
	//

	Assert.assertFalse(scheduling.getStartTime().isPresent());
	Assert.assertFalse(scheduling.getEndTime().isPresent());

    }

    /**
     * @param scheduling
     */
    private void runIndefinitelyTest(Scheduling scheduling) {

	Assert.assertFalse(scheduling.isRunOnceSet());

	Assert.assertTrue(scheduling.isRunIndefinitelySet());

	Assert.assertFalse(scheduling.getRepeatCount().isPresent());

	Assert.assertEquals("2000-10-18", scheduling.getStartTime().get().getValue());
    }

    /**
     * @param scheduling
     */
    private void runOnceSetTest(Scheduling scheduling) {

	Assert.assertFalse(scheduling.isRunIndefinitelySet());

	Assert.assertTrue(scheduling.isRunOnceSet());

	Assert.assertFalse(scheduling.getRepeatCount().isPresent());

	Assert.assertEquals("2000-10-18", scheduling.getStartTime().get().getValue());
    }

    /**
     * @param scheduling
     */
    private void repeatCount10Test(Scheduling scheduling) {

	Assert.assertFalse(scheduling.isRunIndefinitelySet());

	Assert.assertFalse(scheduling.isRunOnceSet());
	
	//
	//
	//

	Assert.assertEquals(new Integer(10), scheduling.getRepeatCount().get());
	
	Assert.assertEquals("2000-10-18", scheduling.getStartTime().get().getValue());
    }

    /**
     * @param scheduling
     */
    private void runOnceAgain(Scheduling scheduling) {

	Assert.assertFalse(scheduling.isRunIndefinitelySet());

	Assert.assertTrue(scheduling.isRunOnceSet());
	
	//
	//
	//

	Assert.assertFalse(scheduling.getRepeatCount().isPresent());
	
	Assert.assertEquals("2000-10-18", scheduling.getStartTime().get().getValue());

    }

    /**
     * @param scheduling
     */
    private void endTimeAndRepeatIntervalTest(Scheduling scheduling) {

 	Assert.assertEquals(new Integer(5), scheduling.getRepeatInterval());

 	Assert.assertEquals(TimeUnit.MINUTES, scheduling.getRepeatIntervalUnit());

	Assert.assertEquals("2030-10-18", scheduling.getEndTime().get().getValue());

	//
	// this is removed when the end time is set...
	//
	Assert.assertFalse(scheduling.getRepeatCount().isPresent());
	
	// ... so it runs indefinitely
	Assert.assertTrue(scheduling.isRunIndefinitelySet());

	// and run once is false
	Assert.assertFalse(scheduling.isRunOnceSet());

     }

    /**
     * @param scheduling
     * @param start
     * @param end
     */
    private void test5(Scheduling scheduling, String start, String end) {

	Date startTime = scheduling.getStartTime().get().asDate();
	String isoStartTime = ISO8601DateTimeUtils.getISO8601Date(startTime);

	Assert.assertEquals(start, isoStartTime);

	Date endTime = scheduling.getEndTime().get().asDate();
	String isoEndTime = ISO8601DateTimeUtils.getISO8601Date(endTime);

	Assert.assertEquals(end, isoEndTime);

    }

    @Test
    public void test6() {

	SchedulerWorkerSetting setting = new SchedulerWorkerSetting();

	test6_1(setting);
	test6_1(new SchedulerWorkerSetting(setting.getObject()));
	test6_1(new SchedulerWorkerSetting(setting.getObject().toString()));

	setting = new SchedulerWorkerSetting();

	test6_1(setting);
	test6_1(new SchedulerWorkerSetting(setting.getObject()));
	test6_1(new SchedulerWorkerSetting(setting.getObject().toString()));

	// !!! real type must be provided !!!
	setting.setConfigurableType("Configurable");

	test6_2(setting);
	test6_2(new SchedulerWorkerSetting(setting.getObject()));
	test6_2(new SchedulerWorkerSetting(setting.getObject().toString()));
    }

    /**
     * @param setting
     */
    private void test6_1(SchedulerWorkerSetting setting) {

	Assert.assertTrue(setting.getGroup() == SchedulingGroup.DEFAULT);
    }

    /**
     * @param scheduledItem
     */
    private void test6_2(SchedulerWorkerSetting setting) {

	Assert.assertEquals("Configurable", setting.getConfigurableType());
    }
}
