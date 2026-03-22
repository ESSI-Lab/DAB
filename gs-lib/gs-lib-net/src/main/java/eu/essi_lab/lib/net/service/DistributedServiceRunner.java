package eu.essi_lab.lib.net.service;

import eu.essi_lab.lib.net.service.lock.*;
import eu.essi_lab.lib.utils.*;

import java.util.concurrent.*;

/**
 * @author Fabrizio
 */
public class DistributedServiceRunner {


    /**
     *
     */
    private static final long START_DELAY_SECONDS = 5;

    private final ManagedService service;
    private final ServiceLock lock;
    private final int renewSeconds;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private volatile boolean running = false;

    /**
     * @param service
     * @param lock
     */
    public DistributedServiceRunner(ManagedService service, ServiceLock lock, int renewSeconds) {

	this.service = service;
	this.lock = lock;
	this.renewSeconds = renewSeconds;
    }

    /**
     *
     */
    public void start() {

	GSLoggerFactory.getLogger(getClass()).info("Starting for service {}", service.getId());

	scheduler.scheduleWithFixedDelay(this::attemptStart, 0, START_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    /**
     *
     */
    private void attemptStart() {

	if (running) {

	    GSLoggerFactory.getLogger(getClass()).info("Service {} running", service.getId());

	    return;
	}

	GSLoggerFactory.getLogger(getClass()).info("Trying to acquire lock for service {}", service.getId());

	boolean acquired = lock.tryAcquire();

	if (acquired) {

	    GSLoggerFactory.getLogger(getClass()).info("Lock acquired for service {}", service.getId());

	    running = true;
	    service.start();

	    // starts heartbeat
	    scheduler.scheduleWithFixedDelay(this::renewLock, renewSeconds, renewSeconds, TimeUnit.SECONDS);
	}
    }

    /**
     *
     */
    private void renewLock() {

	if (!running) {

	    GSLoggerFactory.getLogger(getClass()).info("Renew lock skipped, service {} stopped", service.getId());

	    return;
	}

	boolean ok = lock.renew();

	GSLoggerFactory.getLogger(getClass()).info("Lock renewed for service {}: {}", service.getId(), ok);

	if (!ok) {

	    GSLoggerFactory.getLogger(getClass()).info("Lock lost for service {}", service.getId());

	    // lock lost → stop immediately
	    stopService();
	}
    }

    /**
     *
     */
    public void stopService() {

	try {

	    GSLoggerFactory.getLogger(getClass()).info("Stopping service {}", service.getId());

	    service.stop();

	} finally {

	    running = false;
	}
    }

    /**
     *
     */
    public void shutdown() {

	stopService();

	lock.release();
	scheduler.shutdownNow();
    }

    /**
     * @return
     */
    public ManagedService getService() {

	return service;
    }
}
