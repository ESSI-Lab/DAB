package eu.essi_lab.cfga.scheduler.test;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.scheduler.SchedulerUtils;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * Reproduces a job which, after a restart, is executed by a Quartz recovery trigger instead of the original one. The
 * rescheduling must replace the job, so that the new setting is stored
 *
 * @author boldrini
 */
public class RescheduleRecoveredJobTest {

    @Test
    public void test() throws Exception {

	Scheduler scheduler = SchedulerFactory.getVolatileScheduler();
	org.quartz.Scheduler quartz = scheduler.getQuartzScheduler();

	CustomTaskSetting setting = new CustomTaskSetting();
	setting.setTaskOptions("observedPropertyURIs=old");
	setting.getScheduling().setEnabled(true);
	setting.getScheduling().setRunOnce();
	// far in the future, so that the job is not executed during the test
	setting.getScheduling().setStartTime(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)));

	scheduler.schedule(setting);

	JobKey jobKey = SchedulerUtils.createJobKey(setting);

	//
	// simulates the recovery: the job is left with a recovery trigger only
	//
	Trigger recoveryTrigger = TriggerBuilder.newTrigger().//
		withIdentity("recover_test", "RECOVERING_JOBS").//
		forJob(jobKey).//
		usingJobData("setting", setting.toString()).//
		startAt(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1))).//
		build();

	quartz.scheduleJob(recoveryTrigger);
	quartz.unscheduleJob(SchedulerUtils.createTriggerKey(setting));

	Assert.assertTrue(quartz.checkExists(jobKey));
	Assert.assertFalse(quartz.checkExists(SchedulerUtils.createTriggerKey(setting)));

	//
	// the options are edited and the worker rescheduled
	//
	setting.setTaskOptions("observedPropertyURIs=new");

	scheduler.reschedule(setting);

	JobDetail jobDetail = quartz.getJobDetail(jobKey);

	CustomTaskSetting jobSetting = SettingUtils.downCast(SchedulerUtils.getSetting(jobDetail.getJobDataMap()), CustomTaskSetting.class);

	Assert.assertEquals("observedPropertyURIs=new", jobSetting.getTaskOptions().get());

	List<? extends Trigger> triggers = quartz.getTriggersOfJob(jobKey);

	Assert.assertEquals(1, triggers.size());
	Assert.assertEquals(SchedulerUtils.createTriggerKey(setting), triggers.getFirst().getKey());

	scheduler.unscheduleAll();
    }
}
