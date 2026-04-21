package eu.essi_lab.cfga.scheduler.test;

import java.util.concurrent.Semaphore;

import org.junit.Assert;
import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.scheduler.SchedulerWorker;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class TestWorker extends SchedulerWorker<SchedulerWorkerSetting> {

    /**
     * 
     */
    static Semaphore startJobSemaphore = new Semaphore(0);

    /**
    * 
    */
    public TestWorker() {
    }

    /**
     * @param setting
     */
    public TestWorker(SchedulerWorkerSetting setting) {

	super(setting);
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	startJobSemaphore.release();

	Setting setting = getSetting();
	Assert.assertEquals(setting.getConfigurableType(), "TestWorker");

	Thread.sleep(SchedulerTest.WORKER_SLEEP_TIME);

	context.put("jobExecuted", true);
    }

    @Override
    protected SchedulerWorkerSetting initSetting() {

	return new SchedulerWorkerSetting();
    }

    @Override
    public void configure(SchedulerWorkerSetting setting) {

	this.setting = setting;
    }

    @Override
    public String getType() {

	return "TestWorker";
    }

    @Override
    protected void storeJobStatus(SchedulerJobStatus status) throws GSException {
    }
}
