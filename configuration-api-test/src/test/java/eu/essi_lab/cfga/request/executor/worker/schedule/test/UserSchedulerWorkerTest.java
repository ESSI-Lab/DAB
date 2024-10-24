package eu.essi_lab.cfga.request.executor.worker.schedule.test;

import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.request.executor.test.TestMessageResponseFormatter;
import eu.essi_lab.cfga.request.executor.test.TestMessageResponseMapper;
import eu.essi_lab.cfga.request.executor.test.TestProfilerHandler;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.request.executor.schedule.UserSchedulerWorker;

/**
 * This test implementation do not execute the request, it is used to
 * check the setting parameters are correctly received. If the check succeeds,
 * this worker writes a completion flag in the context
 * 
 * @author Fabrizio
 */
public class UserSchedulerWorkerTest extends UserSchedulerWorker {

    /**
     * 
     */
    public static final String USER_SCHEDULER_WORKER_TYPE = "USER_SCHEDULER_WORKER_TEST";

    public static final String JOB_EXECUTED = "testSchedulerWorkerJobExecuted";
    public static final String JOB_ERROR = "testSchedulerWorkerJobError";

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) {

	String handlerClass = getSetting().getHandler();
	String mapperClass = getSetting().getMapper();
	String formatterClass = getSetting().getFormatter();

	RequestMessage message = getSetting().getRequestMessage();

	boolean testSucceeded = true;

	try {

	    checkInputs(handlerClass, mapperClass, formatterClass, message);

	    testSucceeded &= handlerClass.equals(TestProfilerHandler.class.getName());
	    testSucceeded &= mapperClass.equals(TestMessageResponseMapper.class.getName());
	    testSucceeded &= formatterClass.equals(TestMessageResponseFormatter.class.getName());

	    createHandler(handlerClass, mapperClass, formatterClass);

	} catch (Exception ex) {

	    testSucceeded = false;
	}

	try {
	    Thread.sleep(5000);
	} catch (InterruptedException e) {
	}

	context.put(JOB_ERROR, !testSucceeded);

	context.put(JOB_EXECUTED, true);
    }

    @Override
    public String getType() {

	return USER_SCHEDULER_WORKER_TYPE;
    }
}
