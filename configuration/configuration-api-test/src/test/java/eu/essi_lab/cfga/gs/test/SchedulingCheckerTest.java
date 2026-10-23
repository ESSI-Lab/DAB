package eu.essi_lab.cfga.gs.test;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.SchedulingChecker;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.scheduler.SchedulerUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;

/**
 * @author boldrini
 */
public class SchedulingCheckerTest {

    @Test
    public void test() throws Exception {

	// enabled, run indefinitely: expected to be scheduled
	CustomTaskSetting indefinitely = createSetting(true);
	indefinitely.getScheduling().setRunIndefinitely();

	// enabled, run once: its job is deleted when the execution ends, not checked
	CustomTaskSetting once = createSetting(true);
	once.getScheduling().setRunOnce();

	// disabled, run indefinitely: not checked
	CustomTaskSetting disabled = createSetting(false);
	disabled.getScheduling().setRunIndefinitely();

	// enabled, run indefinitely, but ended: not checked
	CustomTaskSetting ended = createSetting(true);
	ended.getScheduling().setRunIndefinitely();
	ended.getScheduling().setStartTime(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)));
	ended.getScheduling().setEndTime(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)));

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();
	configuration.put(indefinitely);
	configuration.put(once);
	configuration.put(disabled);
	configuration.put(ended);

	ConfigurationWrapper.setConfiguration(configuration);

	Scheduler scheduler = SchedulerFactory.getVolatileScheduler();
	scheduler.unscheduleAll();

	List<SchedulerWorkerSetting> scheduled = SchedulingChecker.getInstance().check(scheduler);

	Assert.assertEquals(1, scheduled.size());
	Assert.assertEquals(indefinitely.getIdentifier(), scheduled.getFirst().getIdentifier());
	Assert.assertTrue(scheduler.getQuartzScheduler().checkExists(SchedulerUtils.createJobKey(indefinitely)));

	// already scheduled
	Assert.assertTrue(SchedulingChecker.getInstance().check(scheduler).isEmpty());

	// lost job
	scheduler.getQuartzScheduler().deleteJob(SchedulerUtils.createJobKey(indefinitely));

	scheduled = SchedulingChecker.getInstance().check(scheduler);

	Assert.assertEquals(1, scheduled.size());
	Assert.assertTrue(scheduler.getQuartzScheduler().checkExists(SchedulerUtils.createJobKey(indefinitely)));

	scheduler.unscheduleAll();
    }

    private CustomTaskSetting createSetting(boolean enabled) {

	CustomTaskSetting setting = new CustomTaskSetting();
	setting.getScheduling().setEnabled(enabled);
	// far in the future, so that the jobs are not executed during the test
	setting.getScheduling().setStartTime(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)));
	setting.getScheduling().setRepeatInterval(1, TimeUnit.DAYS);

	return setting;
    }
}
