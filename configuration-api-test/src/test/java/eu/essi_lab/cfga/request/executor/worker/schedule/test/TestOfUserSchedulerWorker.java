/**
 * 
 */
package eu.essi_lab.cfga.request.executor.worker.schedule.test;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.request.executor.test.TestMessageResponseFormatter;
import eu.essi_lab.cfga.request.executor.test.TestMessageResponseMapper;
import eu.essi_lab.cfga.request.executor.test.TestProfilerHandler;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.Scheduler.JobEvent;
import eu.essi_lab.cfga.scheduler.Scheduler.JobEventListener;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.scheduler.SchedulerUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.messages.AccessMessage;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.request.executor.schedule.UserScheduledSetting;

/**
 * @author Fabrizio
 */
public class TestOfUserSchedulerWorker {

    private transient volatile boolean jobExecuted;
    private transient volatile boolean jobError;
    private transient volatile boolean testSucceeded;

    @Before
    public void before() throws Exception {

	jobExecuted = false;
	jobError = false;
	testSucceeded = true;

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();
	
	ConfigurationWrapper.setConfiguration(configuration);
    }

    @Test
    public void test1() throws Exception {

	UserScheduledSetting setting = new UserScheduledSetting();
	setting.setDescription("Description");
	setting.setName("Name");

	setting.setConfigurableType(UserSchedulerWorkerTest.USER_SCHEDULER_WORKER_TYPE);

	setting.setHandler(TestProfilerHandler.class.getName());
	setting.setFormatter(TestMessageResponseFormatter.class.getName());
	setting.setMapper(TestMessageResponseMapper.class.getName());

	AccessMessage accessMessage = new AccessMessage();

	StorageInfo storageUri = new StorageInfo();
	storageUri.setUri("http://uri");
	storageUri.setName("storageName");

	accessMessage.setUserJobStorageURI(storageUri);

	setting.setRequestMessage(accessMessage);

	//
	//
	//

	Scheduler scheduler = SchedulerFactory.getVolatileScheduler();

	scheduler.schedule(setting);

	//
	// scheduled settings
	//
	List<SchedulerWorkerSetting> schedulingSettings = scheduler.listScheduledSettings();

	Assert.assertEquals(setting, schedulingSettings.get(0));

	Assert.assertEquals(1, schedulingSettings.size());

	String name = "testJobListener_" + UUID.randomUUID().toString();

	scheduler.addJobEventListener(new JobEventListener() {

	    @SuppressWarnings("incomplete-switch")
	    @Override
	    public void eventOccurred(JobEvent event, JobExecutionContext context, JobExecutionException jobException) {

		switch (event) {

		case JOB_TO_BE_EXECUTED:

		    testSucceeded &= setting.equals(SchedulerUtils.getSetting(context));

		    break;

		case JOB_EXECUTED:

		    jobExecuted = (Boolean) context.get(UserSchedulerWorkerTest.JOB_EXECUTED);
		    jobError = (Boolean) context.get(UserSchedulerWorkerTest.JOB_ERROR);
		    break;
		}
	    }
	}, name); //

	while (!jobExecuted) {
	}

	Assert.assertFalse(jobError);

	testSucceeded &= !jobError;

	Assert.assertTrue(testSucceeded);

	scheduler.removeJobListener(name);

    }

    @Test
    public void test3() throws Exception {

	UserScheduledSetting setting = new UserScheduledSetting();
	setting.setDescription("Description");
	setting.setName("Name");

	setting.setConfigurableType(UserSchedulerWorkerTest.USER_SCHEDULER_WORKER_TYPE);

	//
	// handler class not set, error expected
	//
	// setting.setHandler(TestProfilerHandler.class.getName());
	setting.setFormatter(TestMessageResponseFormatter.class.getName());
	setting.setMapper(TestMessageResponseMapper.class.getName());

	AccessMessage accessMessage = new AccessMessage();

	StorageInfo storageUri = new StorageInfo();
	storageUri.setUri("http://uri");
	storageUri.setName("storageName");

	accessMessage.setUserJobStorageURI(storageUri);

	setting.setRequestMessage(accessMessage);

	//
	//
	//

	Scheduler scheduler = SchedulerFactory.getVolatileScheduler();

	scheduler.schedule(setting);

	//
	// scheduled settings
	//
	List<SchedulerWorkerSetting> schedulingSettings = scheduler.listScheduledSettings();

	Assert.assertEquals(setting, schedulingSettings.get(0));

	Assert.assertEquals(1, schedulingSettings.size());

	String name = "testJobListener_" + UUID.randomUUID().toString();

	scheduler.addJobEventListener(new JobEventListener() {

	    @SuppressWarnings("incomplete-switch")
	    @Override
	    public void eventOccurred(JobEvent event, JobExecutionContext context, JobExecutionException jobException) {

		switch (event) {

		case JOB_TO_BE_EXECUTED:

		    testSucceeded &= setting.equals(SchedulerUtils.getSetting(context));

		    break;

		case JOB_EXECUTED:

		    jobExecuted = (Boolean) context.get(UserSchedulerWorkerTest.JOB_EXECUTED);
		    jobError = (Boolean) context.get(UserSchedulerWorkerTest.JOB_ERROR);
		    break;
		}
	    }
	}, name); //

	while (!jobExecuted) {
	}

	Assert.assertTrue(jobError);

	testSucceeded &= !jobError;

	Assert.assertFalse(testSucceeded);

	scheduler.removeJobListener(name);

    }

    @Test
    public void test4() throws Exception {

	UserScheduledSetting setting = new UserScheduledSetting();
	setting.setDescription("Description");
	setting.setName("Name");

	setting.setConfigurableType(UserSchedulerWorkerTest.USER_SCHEDULER_WORKER_TYPE);

	setting.setHandler(TestProfilerHandler.class.getName());

	//
	// formatter class not set, error expected
	//
	// setting.setFormatter(TestMessageResponseFormatter.class.getName());

	setting.setMapper(TestMessageResponseMapper.class.getName());

	AccessMessage accessMessage = new AccessMessage();

	StorageInfo storageUri = new StorageInfo();
	storageUri.setUri("http://uri");
	storageUri.setName("storageName");

	accessMessage.setUserJobStorageURI(storageUri);

	setting.setRequestMessage(accessMessage);

	//
	//
	//

	Scheduler scheduler = SchedulerFactory.getVolatileScheduler();

	scheduler.schedule(setting);

	//
	// scheduled settings
	//
	List<SchedulerWorkerSetting> schedulingSettings = scheduler.listScheduledSettings();

	Assert.assertEquals(setting, schedulingSettings.get(0));

	Assert.assertEquals(1, schedulingSettings.size());

	String name = "testJobListener_" + UUID.randomUUID().toString();

	scheduler.addJobEventListener(new JobEventListener() {

	    @SuppressWarnings("incomplete-switch")
	    @Override
	    public void eventOccurred(JobEvent event, JobExecutionContext context, JobExecutionException jobException) {

		switch (event) {

		case JOB_TO_BE_EXECUTED:

		    testSucceeded &= setting.equals(SchedulerUtils.getSetting(context));

		    break;

		case JOB_EXECUTED:

		    jobExecuted = (Boolean) context.get(UserSchedulerWorkerTest.JOB_EXECUTED);
		    jobError = (Boolean) context.get(UserSchedulerWorkerTest.JOB_ERROR);
		    break;
		}
	    }
	}, name); //

	while (!jobExecuted) {
	}

	Assert.assertTrue(jobError);

	testSucceeded &= !jobError;

	Assert.assertFalse(testSucceeded);

	scheduler.removeJobListener(name);

    }

    @Test
    public void test5() throws Exception {

	UserScheduledSetting setting = new UserScheduledSetting();
	setting.setDescription("Description");
	setting.setName("Name");

	setting.setConfigurableType(UserSchedulerWorkerTest.USER_SCHEDULER_WORKER_TYPE);

	setting.setHandler(TestProfilerHandler.class.getName());
	setting.setFormatter(TestMessageResponseFormatter.class.getName());

	//
	// mapper class not set, error expected
	//
	// setting.setMapper(TestMessageResponseMapper.class.getName());

	AccessMessage accessMessage = new AccessMessage();

	StorageInfo storageUri = new StorageInfo();
	storageUri.setUri("http://uri");
	storageUri.setName("storageName");

	accessMessage.setUserJobStorageURI(storageUri);

	setting.setRequestMessage(accessMessage);

	//
	//
	//

	Scheduler scheduler = SchedulerFactory.getVolatileScheduler();

	scheduler.schedule(setting);

	//
	// scheduled settings
	//
	List<SchedulerWorkerSetting> schedulingSettings = scheduler.listScheduledSettings();

	Assert.assertEquals(setting, schedulingSettings.get(0));

	Assert.assertEquals(1, schedulingSettings.size());

	String name = "testJobListener_" + UUID.randomUUID().toString();

	scheduler.addJobEventListener(new JobEventListener() {

	    @SuppressWarnings("incomplete-switch")
	    @Override
	    public void eventOccurred(JobEvent event, JobExecutionContext context, JobExecutionException jobException) {

		switch (event) {

		case JOB_TO_BE_EXECUTED:

		    testSucceeded &= setting.equals(SchedulerUtils.getSetting(context));

		    break;

		case JOB_EXECUTED:

		    jobExecuted = (Boolean) context.get(UserSchedulerWorkerTest.JOB_EXECUTED);
		    jobError = (Boolean) context.get(UserSchedulerWorkerTest.JOB_ERROR);
		    break;
		}
	    }
	}, name); //

	while (!jobExecuted) {
	}

	Assert.assertTrue(jobError);

	testSucceeded &= !jobError;

	Assert.assertFalse(testSucceeded);

	scheduler.removeJobListener(name);

    }

    @Test
    public void test6() throws Exception {

	UserScheduledSetting setting = new UserScheduledSetting();
	setting.setDescription("Description");
	setting.setName("Name");

	setting.setConfigurableType(UserSchedulerWorkerTest.USER_SCHEDULER_WORKER_TYPE);

	setting.setHandler(TestProfilerHandler.class.getName());
	setting.setFormatter(TestMessageResponseFormatter.class.getName());

	//
	// mapper class not of TestMessageResponseMapper class, error expected
	//
	setting.setMapper("noClassSet");

	AccessMessage accessMessage = new AccessMessage();

	StorageInfo storageUri = new StorageInfo();
	storageUri.setUri("http://uri");
	storageUri.setName("storageName");

	accessMessage.setUserJobStorageURI(storageUri);

	setting.setRequestMessage(accessMessage);

	//
	//
	//

	Scheduler scheduler = SchedulerFactory.getVolatileScheduler();

	scheduler.schedule(setting);

	//
	// scheduled settings
	//
	List<SchedulerWorkerSetting> schedulingSettings = scheduler.listScheduledSettings();

	Assert.assertEquals(setting, schedulingSettings.get(0));

	Assert.assertEquals(1, schedulingSettings.size());

	String name = "testJobListener_" + UUID.randomUUID().toString();

	scheduler.addJobEventListener(new JobEventListener() {

	    @SuppressWarnings("incomplete-switch")
	    @Override
	    public void eventOccurred(JobEvent event, JobExecutionContext context, JobExecutionException jobException) {

		switch (event) {

		case JOB_TO_BE_EXECUTED:

		    testSucceeded &= setting.equals(SchedulerUtils.getSetting(context));

		    break;

		case JOB_EXECUTED:

		    jobExecuted = (Boolean) context.get(UserSchedulerWorkerTest.JOB_EXECUTED);
		    jobError = (Boolean) context.get(UserSchedulerWorkerTest.JOB_ERROR);
		    break;
		}
	    }
	}, name); //

	while (!jobExecuted) {
	}

	Assert.assertTrue(jobError);

	testSucceeded &= !jobError;

	Assert.assertFalse(testSucceeded);

	scheduler.removeJobListener(name);
    }
}
