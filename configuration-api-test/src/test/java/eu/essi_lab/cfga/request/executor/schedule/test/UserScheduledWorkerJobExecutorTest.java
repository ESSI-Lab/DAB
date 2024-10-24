package eu.essi_lab.cfga.request.executor.schedule.test;

import eu.essi_lab.cfga.request.executor.worker.schedule.test.UserSchedulerWorkerTest;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.request.executor.schedule.UserScheduledSetting;
import eu.essi_lab.request.executor.schedule.UserScheduledWorkerJobExecutor;

/**
 * @author Fabrizio
 */
@SuppressWarnings("rawtypes")
public class UserScheduledWorkerJobExecutorTest extends UserScheduledWorkerJobExecutor {
    
    /**
     * @param setting
     * @return
     * @throws GSException
     */
    protected Scheduler getScheduler() throws GSException {

	return SchedulerFactory.getVolatileScheduler();
    }

    @Override
    protected UserScheduledSetting createSetting(RequestMessage message) {

	UserScheduledSetting setting = new UserScheduledSetting();

	//
	// using the test worker
	//
	setting.setConfigurableType(UserSchedulerWorkerTest.USER_SCHEDULER_WORKER_TYPE);

	setting.setRequestMessage(message);

	setting.setHandler(workerHandlerClass);
	setting.setMapper(workerMapperClass);
	setting.setFormatter(workerFormatterClass);

	return setting;
    }
}
