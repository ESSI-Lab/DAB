package eu.essi_lab.cfga.request.executor.schedule.test;

import static org.junit.Assert.fail;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.request.executor.test.TestMessageResponseFormatter;
import eu.essi_lab.cfga.request.executor.test.TestMessageResponseMapper;
import eu.essi_lab.cfga.request.executor.test.TestProfilerHandler;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.request.executor.schedule.UserScheduledWorkerJobExecutor;

public class TestOfUserScheduledWorkerJobExecutor2 {

    Logger logger = GSLoggerFactory.getLogger(TestOfUserScheduledWorkerJobExecutor2.class);

    @Rule
    public ExpectedException exceptions = ExpectedException.none();

    @SuppressWarnings("rawtypes")
    @Test
    public void testTwoConcurrentJobs() throws Exception {

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	UserScheduledWorkerJobExecutor scheduleExecutor = new UserScheduledWorkerJobExecutor() {

	    protected Scheduler getScheduler() throws GSException {

		return SchedulerFactory.getVolatileScheduler();
	    }
	};

	scheduleExecutor.setWorkerHandler(//
		TestProfilerHandler.class.getName(), //
		TestMessageResponseMapper.class.getName(), //
		TestMessageResponseFormatter.class.getName());

	StorageInfo uri = new StorageInfo("http://localhost");
	uri.setName("dataBaseName");

	DiscoveryMessage message1 = new DiscoveryMessage();
	message1.setWebRequest(WebRequest.createGET("http://localhost"));
	message1.setUserBond(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TITLE, "3"));
	message1.setScheduled(true);
	message1.setUserJobStorageURI(uri);

	DiscoveryMessage message2 = new DiscoveryMessage();
	message2.setWebRequest(WebRequest.createGET("http://localhost"));
	message2.setUserBond(BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TITLE, "5"));
	message1.setScheduled(true);
	message2.setUserJobStorageURI(uri);

	executeTest(scheduleExecutor, message1, message2);
	TestProfilerHandler.list.clear();

	executeTest(scheduleExecutor, message1, message2);
	TestProfilerHandler.list.clear();

	executeTest(scheduleExecutor, message1, message2);
	TestProfilerHandler.list.clear();

	executeTest(scheduleExecutor, message1, message2);
	TestProfilerHandler.list.clear();

	executeTest(scheduleExecutor, message1, message2);
	TestProfilerHandler.list.clear();

    }

    @SuppressWarnings("rawtypes")
    private void executeTest(UserScheduledWorkerJobExecutor scheduleExecutor, DiscoveryMessage message1, DiscoveryMessage message2)
	    throws Exception {
	scheduleExecutor.retrieve(message1);

	scheduleExecutor.retrieve(message2);

	int maxSeconds = 10;
	while (maxSeconds-- > 0) {
	    Thread.sleep(1000);
	    if (TestProfilerHandler.list.size() == 2) {

		if (TestProfilerHandler.list.contains(3) && TestProfilerHandler.list.contains(5)) {
		    return;
		}

		fail("The list doesn't contain the expected results");
	    }
	}
	fail("Timeout");

    }
}
