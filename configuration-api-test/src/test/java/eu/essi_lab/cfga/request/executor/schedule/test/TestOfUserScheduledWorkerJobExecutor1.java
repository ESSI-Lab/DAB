package eu.essi_lab.cfga.request.executor.schedule.test;

import static org.junit.Assert.fail;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.request.executor.test.TestMessageResponseFormatter;
import eu.essi_lab.cfga.request.executor.test.TestMessageResponseMapper;
import eu.essi_lab.cfga.request.executor.test.TestProfilerHandler;
import eu.essi_lab.cfga.request.executor.worker.schedule.test.UserSchedulerWorkerTest;
import eu.essi_lab.cfga.scheduler.Scheduler.JobEvent;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class TestOfUserScheduledWorkerJobExecutor1 {

    private volatile boolean jobExecuted;
    private volatile boolean jobError;

    @Before
    public void before() throws Exception {

	jobExecuted = false;
	jobError = false;
	
	DefaultConfiguration defaultConfiguration = new DefaultConfiguration();
	defaultConfiguration.clean();
	
	ConfigurationWrapper.setConfiguration(defaultConfiguration);
    }

    @Test
    public void test1() throws Exception {

	SchedulerFactory.getVolatileScheduler().addJobEventListener((e, c, ex) -> {
	    jobExecuted = (Boolean) c.get(UserSchedulerWorkerTest.JOB_EXECUTED);
	    jobError = (Boolean) c.get(UserSchedulerWorkerTest.JOB_ERROR);
	}, //
		JobEvent.JOB_EXECUTED, //
		UUID.randomUUID().toString(), //
		true); //

	//
	//
	//

	UserScheduledWorkerJobExecutorTest executor = new UserScheduledWorkerJobExecutorTest();

	executor.setWorkerHandler(//
		TestProfilerHandler.class.getName(), //
		TestMessageResponseMapper.class.getName(), //
		TestMessageResponseFormatter.class.getName());

	AccessMessage accessMessage = new AccessMessage();

	StorageInfo storageUri = new StorageInfo();
	storageUri.setUri("http://uri");
	storageUri.setName("storageName");

	accessMessage.setUserJobStorageURI(storageUri);

	executor.retrieve(accessMessage);

	while (!jobExecuted) {
	}

	Assert.assertFalse(jobError);

    }

    @Test
    public void test2() throws Exception {

	//
	//
	//

	UserScheduledWorkerJobExecutorTest executor = new UserScheduledWorkerJobExecutorTest();

	//
	// inputs not set, error expected
	//

	// executor.setWorkerHandler(//
	// TestProfilerHandler.class.getName(), //
	// TestMessageResponseMapper.class.getName(), //
	// TestMessageResponseFormatter.class.getName());

	AccessMessage accessMessage = new AccessMessage();

	StorageInfo storageUri = new StorageInfo();
	storageUri.setUri("http://uri");
	storageUri.setName("storageName");

	accessMessage.setUserJobStorageURI(storageUri);

	try {

	    executor.retrieve(accessMessage);
	    fail("Exception not thrown");

	} catch (GSException ex) {
	    // OK
	}

	// //
	// // scheduled settings
	// //
	//
	// Scheduler scheduler = SchedulerFactory.withRamJobStore();
	//
	// List<ScheduledSetting> scheduledSettings = scheduler.listScheduledSettings();
	//
	// Assert.assertEquals(1, scheduledSettings.size());
	//
	// scheduler.addJobListener(new JobListenerSupport() {
	//
	// public void jobToBeExecuted(JobExecutionContext context) {
	//
	// }
	//
	// public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
	//
	// jobExecuted = (Boolean) context.get("jobExecuted");
	// jobError = (Boolean) context.get("jobError");
	// }
	//
	// @Override
	// public String getName() {
	//
	// return "testJobListener_"+UUID.randomUUID().toString();
	// }
	// });
	//
	// while (!jobExecuted) {
	//
	// Thread.sleep(1000);
	// }
	//
	// Assert.assertTrue(jobError);
    }

}
