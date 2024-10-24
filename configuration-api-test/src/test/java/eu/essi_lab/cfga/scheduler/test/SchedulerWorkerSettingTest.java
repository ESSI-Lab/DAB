package eu.essi_lab.cfga.scheduler.test;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting.SchedulingGroup;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;

/**
 * @author Fabrizio
 */
public class SchedulerWorkerSettingTest {

    @Test
    public void test() {

	SchedulerWorkerSetting schedulerWorkerSetting = new SchedulerWorkerSetting();

	Scheduling scheduling = schedulerWorkerSetting.getScheduling();
	Assert.assertNotNull(scheduling);

	schedulingTypeTest(scheduling);
	schedulingTypeTest(new Scheduling(scheduling.getObject()));
	schedulingTypeTest(new Scheduling(scheduling.getObject().toString()));
	schedulingTypeTest(SettingUtils.downCast(scheduling, Scheduling.class, true));

	test1(schedulerWorkerSetting);
	test1(new SchedulerWorkerSetting(schedulerWorkerSetting.getObject()));
	test1(new SchedulerWorkerSetting(schedulerWorkerSetting.getObject().toString()));
	test1(SettingUtils.downCast(schedulerWorkerSetting, SchedulerWorkerSetting.class, true));

	schedulerWorkerSetting.setGroup(SchedulingGroup.HARVESTING);

	test2(schedulerWorkerSetting);
	test2(new SchedulerWorkerSetting(schedulerWorkerSetting.getObject()));
	test2(new SchedulerWorkerSetting(schedulerWorkerSetting.getObject().toString()));
	test2(SettingUtils.downCast(schedulerWorkerSetting, SchedulerWorkerSetting.class, true));

    }

    /**
     * 
     */
    private void schedulingTypeTest(Scheduling scheduling) {

	Assert.assertEquals(Scheduling.SCHEDULING_OBJECT_TYPE, scheduling.getObjectType());
    }

    /**
     * @param scheduling
     */
    private void test1(SchedulerWorkerSetting setting) {

	Assert.assertTrue(setting.isEnabled());

	Assert.assertEquals(SchedulingGroup.DEFAULT, setting.getGroup());

	Assert.assertTrue(setting.getScheduling().isRunOnceSet());
	Assert.assertFalse(setting.getScheduling().isRunIndefinitelySet());

	Assert.assertFalse(setting.getScheduling().isEnabled());

	Assert.assertFalse(setting.getScheduling().getStartTime().isPresent());
	Assert.assertFalse(setting.getScheduling().getEndTime().isPresent());

	Assert.assertFalse(setting.getScheduling().getRepeatCount().isPresent());

	Assert.assertEquals(new Integer(1), setting.getScheduling().getRepeatInterval());
	Assert.assertEquals(TimeUnit.DAYS, setting.getScheduling().getRepeatIntervalUnit());
    }

    /**
     * @param scheduling
     */
    private void test2(SchedulerWorkerSetting setting) {

	Assert.assertEquals(SchedulingGroup.HARVESTING, setting.getGroup());

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
