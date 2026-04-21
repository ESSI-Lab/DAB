/**
 * 
 */
package eu.essi_lab.cfga.augmenter.worker.test;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSettingLoader;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.Scheduler.JobEvent;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;

/**
 * @author Fabrizio
 */
public class AugmenterWorkerSchedulingTest {

    private volatile boolean test1Ended;
    private DefaultConfiguration configuration;

    @Before
    public void before() throws Exception {

	configuration = new DefaultConfiguration(UUID.randomUUID().toString() + ".json");
	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);
    }

    @Test
    public void test1() throws Exception {

	TestAugmenterWorker worker = new TestAugmenterWorker();

	worker.getSetting().setConfigurableType("TestAugmenterWorker");

	worker.getSetting().setMaxRecords(5);
	worker.getSetting().setLessRecentSort(true);
	worker.getSetting().setMaxAge(7);

	List<AugmenterSetting> settings = worker.getSetting().getAugmentersSetting().getSettings(AugmenterSetting.class, false);

	for (int i = 0; i < settings.size(); i++) {

	    AugmenterSetting setting = settings.get(i);
	    setting.setSelected(true);
	    setting.setPriority(i);
	}


	Scheduler scheduler = SchedulerFactory.getVolatileScheduler();

	scheduler.addJobEventListener((e, c, ex) -> test1Ended = true, //
		JobEvent.JOB_EXECUTED, //
		UUID.randomUUID().toString(), //
		true); //

	scheduler.schedule(worker.getSetting());

	while (!test1Ended) {
	}

	Assert.assertTrue(worker.testPassed());
    }

    @Test
    public void test2() throws Exception {

	AugmenterWorkerSetting setting = AugmenterWorkerSettingLoader.load();

	//
	// this is required to create the correct instance
	//
	setting.setConfigurableType("TestAugmenterWorker");

	setting.setMaxRecords(5);
	setting.setLessRecentSort(true);
	setting.setMaxAge(7);

	List<AugmenterSetting> settings = setting.getAugmentersSetting().getSettings(AugmenterSetting.class, false);

	for (int i = 0; i < settings.size(); i++) {

	    AugmenterSetting s = settings.get(i);
	    s.setSelected(true);
	    s.setPriority(i);
	}

 
	//
	//
	// creates the worker configured instance
	//
	//
	TestAugmenterWorker configuredWorker = setting.createConfigurable();

	configuredWorker.doJob(null, null);

	Assert.assertTrue(configuredWorker.testPassed());
    }
}
