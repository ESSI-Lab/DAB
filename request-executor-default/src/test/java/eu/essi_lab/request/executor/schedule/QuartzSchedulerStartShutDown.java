package eu.essi_lab.request.executor.schedule;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author ilsanto
 */
public class QuartzSchedulerStartShutDown {

    private static final String ERR_ID_QUARTZ_SCHEDULER = "ERR_ID_QUARTZ_SCHEDULER";

    public static void start() throws GSException {
	try {

	    Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

	    if (!scheduler.isStarted())
		// and start it off
		scheduler.start();
	} catch (SchedulerException se) {

	    throw GSException.createException(QuartzSchedulerStartShutDown.class, "Error thrwon by Quartz Scheduler", null, null,
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_FATAL, ERR_ID_QUARTZ_SCHEDULER, se);
	}

    }

    public static void stop() throws GSException {
	try {
	    Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

	    if (!scheduler.isShutdown())
		// and start it off
		scheduler.shutdown(true);
	} catch (SchedulerException se) {

	    throw GSException.createException(QuartzSchedulerStartShutDown.class, "Error thrwon by Quartz Scheduler", null, null,
		    ErrorInfo.ERRORTYPE_INTERNAL, ErrorInfo.SEVERITY_FATAL, ERR_ID_QUARTZ_SCHEDULER, se);
	}

    }
}
