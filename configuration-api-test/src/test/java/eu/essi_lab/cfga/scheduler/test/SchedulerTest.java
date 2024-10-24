package eu.essi_lab.cfga.scheduler.test;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.listeners.SchedulerListenerSupport;

import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.Scheduler.JobEvent;
import eu.essi_lab.cfga.scheduler.Scheduler.JobEventListener;
import eu.essi_lab.cfga.scheduler.SchedulerUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author Fabrizio
 */
public abstract class SchedulerTest {

    static long WORKER_SLEEP_TIME = 5000;
    static final int REPEAT_COUNT = 3;

    private Semaphore endJobSemaphore;

    private volatile int executionsCount;
    private volatile boolean startedCorrectly;
    private volatile boolean triggerFinalized;
    private volatile boolean jobUnscheduled;

    /**
     * @throws Exception
     */
    public void before() throws Exception {

	ISO8601DateTimeUtils.setGISuiteDefaultTimeZone();

	// jobStarted = false;
	// jobExecuted = false;

	TestWorker.startJobSemaphore = new Semaphore(0);
	endJobSemaphore = new Semaphore(0);

	startedCorrectly = false;
	triggerFinalized = false;

	//
	// waits until all jobs are unscheduled
	//
	Scheduler scheduler = getScheduler();

	scheduler.unscheduleAll();

	while (!scheduler.listExecutingSettings().isEmpty()) {
	}

	List<SchedulerWorkerSetting> executingSettings = scheduler.listExecutingSettings();
	Assert.assertEquals(0, executingSettings.size());

	Thread.sleep(5000);
    }

    /**
     * @return
     * @throws SchedulerException
     * @throws SQLException
     */
    protected abstract Scheduler getScheduler() throws Exception;

    /**
     * @throws SchedulerException
     * @throws InterruptedException
     * @throws SQLException
     */
    public void multipleScheduledSettingsTest() throws Exception {

	SchedulerWorkerSetting setting1 = new SchedulerWorkerSetting();
	setting1.setDescription("Description 1");
	setting1.setName("1");

	SchedulerWorkerSetting setting2 = new SchedulerWorkerSetting();
	setting2.setDescription("Description 2");
	setting2.setName("2");

	SchedulerWorkerSetting setting3 = new SchedulerWorkerSetting();
	setting3.setDescription("Description 3");
	setting3.setName("3");

	//
	// this is mandatory
	//
	setting1.setConfigurableType("TestWorker");
	setting2.setConfigurableType("TestWorker");
	setting3.setConfigurableType("TestWorker");

	Scheduler scheduler = getScheduler();

	List<SchedulerWorkerSetting> executingSettings = scheduler.listExecutingSettings();
	Assert.assertEquals(0, executingSettings.size());

	//
	// schedules 3 settings
	//
	scheduler.schedule(setting1);
	scheduler.schedule(setting2);
	scheduler.schedule(setting3);

	//
	// scheduled settings
	//
	List<SchedulerWorkerSetting> scheduledSettings = scheduler.listScheduledSettings().//
		stream().//
		sorted((s1, s2) -> s1.getName().compareTo(s2.getName())).//
		collect(Collectors.toList());

	Assert.assertEquals(3, scheduledSettings.size());

	Assert.assertEquals(setting1, scheduledSettings.get(0));
	Assert.assertEquals(setting2, scheduledSettings.get(1));
	Assert.assertEquals(setting3, scheduledSettings.get(2));
    }

    /**
     * @throws SchedulerException
     * @throws InterruptedException
     * @throws SQLException
     */
    public void runOnceTest() throws Exception {

	SchedulerWorkerSetting setting = new SchedulerWorkerSetting();
	setting.setDescription("Description");
	setting.setName("Name");

	//
	// this is mandatory
	//
	setting.setConfigurableType("TestWorker");

	Scheduler scheduler = getScheduler();

	scheduler.schedule(setting);

	//
	// scheduled settings
	//
	List<SchedulerWorkerSetting> schedulingSettings = scheduler.listScheduledSettings();

	Assert.assertEquals(setting, schedulingSettings.get(0));

	Assert.assertEquals(1, schedulingSettings.size());

	boolean[] jobExecuted = new boolean[] { false };

	scheduler.addJobEventListener((e, c, ex) -> {

	    jobExecuted[0] = (Boolean) c.get("jobExecuted");
	    endJobSemaphore.release();

	}, //
		JobEvent.JOB_EXECUTED, //
		UUID.randomUUID().toString(), //
		true); //

	//
	//
	//

	TestWorker.startJobSemaphore.acquire();

	//
	//
	//

	List<SchedulerWorkerSetting> settings = scheduler.listExecutingSettings();
	Assert.assertEquals(1, settings.size());

	Assert.assertEquals(settings.get(0), setting);

	//
	//
	//

	endJobSemaphore.acquire();

	//
	//
	//

	Assert.assertTrue(jobExecuted[0]);
    }

    /**
     * @throws SchedulerException
     * @throws InterruptedException
     * @throws SQLException
     */
    public void repeatCountTest() throws Exception {

	SchedulerWorkerSetting setting = new SchedulerWorkerSetting();

	setting.getScheduling().setRepeatCount(REPEAT_COUNT);
	setting.getScheduling().setRepeatInterval(5, TimeUnit.SECONDS);

	setting.setDescription("Description");
	setting.setName("Name");

	//
	// this is mandatory
	//
	setting.setConfigurableType("TestWorker");

	Scheduler scheduler = getScheduler();

	String name = UUID.randomUUID().toString();

	scheduler.addJobEventListener((e, c, ex) -> {
	    executionsCount++;
	    GSLoggerFactory.getLogger(getClass()).info("Executions count incremented " + executionsCount);

	}, //
		JobEvent.JOB_EXECUTED, //
		name, //
		false); //

	scheduler.addSchedulerListener(new SchedulerListenerSupport() {

	    public void triggerFinalized(Trigger trigger) {

		triggerFinalized = true;

		GSLoggerFactory.getLogger(getClass()).info("Trigger REPEAT COUNT finalized");

		try {
		    scheduler.removeSchedulerListener(this);
		} catch (SchedulerException e) {
		    e.printStackTrace();
		}
	    }
	});

	scheduler.schedule(setting);

	//
	// scheduled settings
	//
	List<SchedulerWorkerSetting> schedulingSettings = scheduler.listScheduledSettings();

	Assert.assertEquals(1, schedulingSettings.size());

	Assert.assertEquals(setting, schedulingSettings.get(0));

	while (!triggerFinalized) {
	}

	Assert.assertTrue(triggerFinalized);

	Assert.assertEquals(REPEAT_COUNT + 1, executionsCount);

	scheduler.removeJobListener(name);
    }

    /**
     * @throws SchedulerException
     * @throws InterruptedException
     * @throws SQLException
     */
    public void startTimeTest() throws Exception {

	final long START_DELAY = 15000;

	SchedulerWorkerSetting setting = new SchedulerWorkerSetting();

	long currentTimeMillis = System.currentTimeMillis();

	setting.getObject().put("schedulingTime", System.currentTimeMillis());

	//
	// the job will starts in 15 seconds from now
	// actually in seems that it starts in about 9.35 seconds
	//
	setting.getScheduling().setStartTime(new Date(currentTimeMillis + START_DELAY));

	setting.setDescription("Description");
	setting.setName("Name");

	//
	// this is mandatory
	//
	setting.setConfigurableType("TestWorker");

	Scheduler scheduler = getScheduler();

	scheduler.schedule(setting);

	//
	// scheduled settings
	//
	List<SchedulerWorkerSetting> schedulingSettings = scheduler.listScheduledSettings();

	Assert.assertEquals(setting, schedulingSettings.get(0));

	Assert.assertEquals(1, schedulingSettings.size());

	scheduler.addJobEventListener((e, c, ex) -> {

	}, //
		JobEvent.JOB_EXECUTED, //
		UUID.randomUUID().toString(), //
		true); //

	String name = "testJobListener_" + UUID.randomUUID().toString();

	boolean[] jobExecuted = new boolean[] { false };

	scheduler.addJobEventListener(new JobEventListener() {

	    @SuppressWarnings("incomplete-switch")
	    @Override
	    public void eventOccurred(JobEvent event, JobExecutionContext context, JobExecutionException jobException) {

		switch (event) {

		case JOB_TO_BE_EXECUTED:

		    long schedulingTime = setting.getObject().getLong("schedulingTime");
		    long currentTime = System.currentTimeMillis();

		    //
		    // if this job starts approximately 10 seconds after the scheduling time,
		    // considering that it should start 15 seconds after the scheduling time,
		    // the test can be considered passed
		    //
		    startedCorrectly = (currentTime - schedulingTime) >= START_DELAY - 5000;

		    break;

		case JOB_EXECUTED:

		    SchedulerWorkerSetting setting = SchedulerUtils.getSetting(context);
		    Assert.assertEquals(setting, setting);

		    jobExecuted[0] = (Boolean) context.get("jobExecuted");

		    endJobSemaphore.release();

		    break;
		}
	    }
	}, name); //

	//
	//
	//

	TestWorker.startJobSemaphore.acquire();

	//
	//
	//

	List<SchedulerWorkerSetting> settings = scheduler.listExecutingSettings();
	Assert.assertEquals(1, settings.size());

	Assert.assertEquals(settings.get(0), setting);

	//
	//
	//

	endJobSemaphore.acquire();

	//
	//
	//

	Assert.assertTrue(jobExecuted[0]);

	Assert.assertTrue(startedCorrectly);

	scheduler.removeJobListener(name);
    }

    /**
     * @throws SchedulerException
     * @throws InterruptedException
     * @throws SQLException
     */
    public void rescheduleTest() throws Exception {

	SchedulerWorkerSetting setting = new SchedulerWorkerSetting();

	setting.setDescription("Description");
	setting.setName("Name");
	setting.setConfigurableType("TestWorker");

	//
	// scheduling
	//
	setting.getScheduling().setStartTime(new Date(System.currentTimeMillis() + 30000));

	String startTimeString = setting.getScheduling().getStartTime().get().toString();

	Scheduler scheduler = getScheduler();

	scheduler.schedule(setting); // scheduling

	List<SchedulerWorkerSetting> schedulingSettings = scheduler.listScheduledSettings();

	Assert.assertEquals(setting, schedulingSettings.get(0));

	Assert.assertEquals(1, schedulingSettings.size());

	Assert.assertEquals(startTimeString, schedulingSettings.get(0).getScheduling().getStartTime().get().toString());

	//
	// rescheduling
	//
	setting.getScheduling().setStartTime(new Date(System.currentTimeMillis() + 90000));

	startTimeString = setting.getScheduling().getStartTime().get().toString();

	scheduler.reschedule(setting); // rescheduling

	schedulingSettings = scheduler.listScheduledSettings();

	Assert.assertEquals(setting, schedulingSettings.get(0));

	Assert.assertEquals(1, schedulingSettings.size());

	Assert.assertEquals(startTimeString, schedulingSettings.get(0).getScheduling().getStartTime().get().toString());

	//
	//
	//

	scheduler.unscheduleAll();
    }

    /**
     * @throws SchedulerException
     * @throws InterruptedException
     * @throws SQLException
     */
    public void endTimeTest() throws Exception {

	final long END_DELAY = 15000;

	SchedulerWorkerSetting setting = new SchedulerWorkerSetting();

	//
	// the job would repeat indefinitely every 5 seconds, but since it
	// will ends in 15 seconds from now, a maximum of 3 repetitions are expected
	//

	setting.getScheduling().setRepeatInterval(5, TimeUnit.SECONDS);

	setting.getScheduling().setEndTime(new Date(System.currentTimeMillis() + END_DELAY));

	setting.setDescription("Description");
	setting.setName("Name");

	//
	// this is mandatory
	//
	setting.setConfigurableType("TestWorker");

	Scheduler scheduler = getScheduler();

	scheduler.schedule(setting);

	//
	// scheduled settings
	//
	List<SchedulerWorkerSetting> schedulingSettings = scheduler.listScheduledSettings();

	Assert.assertEquals(1, schedulingSettings.size());

	Assert.assertEquals(setting, schedulingSettings.get(0));

	scheduler.addJobEventListener((e, c, ex) -> executionsCount++, //
		JobEvent.JOB_EXECUTED, //
		UUID.randomUUID().toString(), //
		true); //

	scheduler.addSchedulerListener(new SchedulerListenerSupport() {

	    public void triggerFinalized(Trigger trigger) {

		try {
		    scheduler.removeSchedulerListener(this);
		} catch (SchedulerException e) {
		    e.printStackTrace();
		}

		GSLoggerFactory.getLogger(getClass()).info("Trigger finalized");

		triggerFinalized = true;

		endJobSemaphore.release();
	    }
	});

	//
	//
	//

	TestWorker.startJobSemaphore.acquire();

	//
	//
	//

	List<SchedulerWorkerSetting> settings = scheduler.listExecutingSettings();
	Assert.assertEquals(1, settings.size());

	Assert.assertEquals(settings.get(0), setting);

	//
	//
	//

	endJobSemaphore.acquire();

	//
	//
	//

	Assert.assertTrue(triggerFinalized);

	Assert.assertTrue(executionsCount <= 3);
    }

    /**
     * @throws SchedulerException
     * @throws InterruptedException
     * @throws SQLException
     */
    public void unscheduleTest() throws Exception {

	SchedulerWorkerSetting setting = new SchedulerWorkerSetting();

	setting.getScheduling().setRepeatCount(REPEAT_COUNT);
	setting.getScheduling().setRepeatInterval(5, TimeUnit.SECONDS);

	setting.setDescription("Description");
	setting.setName("Name");

	//
	// this is mandatory
	//
	setting.setConfigurableType("TestWorker");

	Scheduler scheduler = getScheduler();

	scheduler.schedule(setting);

	//
	// scheduled settings
	//
	List<SchedulerWorkerSetting> schedulingSettings = scheduler.listScheduledSettings();

	Assert.assertEquals(setting, schedulingSettings.get(0));

	Assert.assertEquals(1, schedulingSettings.size());

	String name = UUID.randomUUID().toString();

	scheduler.addJobEventListener((e, c, ex) -> {

	    executionsCount++;

	    if (executionsCount == 1) {

		try {
		    scheduler.unschedule(setting);
		} catch (SchedulerException e1) {

		    GSLoggerFactory.getLogger(getClass()).error(e1.getMessage(), e1);

		    Assert.fail(e1.getMessage());

		    return;
		}
	    }
	}, //
		JobEvent.JOB_EXECUTED, //
		name, //
		false); //

	scheduler.addSchedulerListener(new SchedulerListenerSupport() {

	    public void jobUnscheduled(TriggerKey jobKey) {

		jobUnscheduled = true;

		try {
		    scheduler.removeSchedulerListener(this);
		} catch (SchedulerException e) {

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

		    Assert.fail(e.getMessage());

		    return;
		}
	    }

	    public void triggerFinalized(Trigger trigger) {

		//
		// this will never become true
		//
		triggerFinalized = true;
	    }
	});

	//
	// after 15 seconds we can check
	//
	Thread.sleep(15000);

	//
	//
	//

	Assert.assertTrue(jobUnscheduled);

	Assert.assertEquals(1, executionsCount);

	List<SchedulerWorkerSetting> settings = scheduler.listExecutingSettings();

	Assert.assertEquals(0, settings.size());
    }
}
