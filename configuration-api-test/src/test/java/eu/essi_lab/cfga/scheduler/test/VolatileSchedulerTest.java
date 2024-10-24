package eu.essi_lab.cfga.scheduler.test;

import org.junit.Before;
import org.junit.Test;
import org.quartz.SchedulerException;

import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting;

/**
 * @author Fabrizio
 */
public class VolatileSchedulerTest extends SchedulerTest {

    private static Scheduler scheduler;

    /**
     * @return
     * @throws SchedulerException
     */
    protected Scheduler getScheduler() throws SchedulerException {

	if (scheduler == null) {

	    SchedulerSetting schedulerSetting = new SchedulerSetting();
	    schedulerSetting.setUserDateTimeZone("UTC");

	    SelectionUtils.deepClean(schedulerSetting);

	    scheduler = SchedulerFactory.getVolatileScheduler(schedulerSetting);
	    scheduler.start();
	}

	return scheduler;
    }

    @Before
    public void before() throws Exception {

	super.before();
    }

    @Test
    public void multipleScheduledSettingsTest() throws Exception {
	super.multipleScheduledSettingsTest();
    }

    @Test
    public void runOnceTest() throws Exception {
	super.runOnceTest();
    }

    @Test
    public void repeatCountTest() throws Exception {
	super.repeatCountTest();
    }

    @Test
    public void startTimeTest() throws Exception {
	super.startTimeTest();
    }

    @Test
    public void rescheduleTest() throws Exception {
	super.rescheduleTest();
    }

    @Test
    public void endTimeTest() throws Exception {
	super.endTimeTest();
    }

    @Test
    public void unscheduleTest() throws Exception {
	super.unscheduleTest();
    }
}
