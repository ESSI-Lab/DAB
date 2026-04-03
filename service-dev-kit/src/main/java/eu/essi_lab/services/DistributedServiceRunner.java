package eu.essi_lab.services;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import eu.essi_lab.lib.utils.*;
import eu.essi_lab.services.lock.*;

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

	GSLoggerFactory.getLogger(getClass()).info("Starting service {}", service.getId());

	scheduler.scheduleWithFixedDelay(this::attemptStart, 0, START_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    /**
     *
     */
    private void attemptStart() {

	if (running) {

	    return;
	}

	GSLoggerFactory.getLogger(getClass()).info("Trying to acquire lock for service {}", service.getId());

	boolean acquired = lock.tryAcquire();

	if (acquired) {

	    GSLoggerFactory.getLogger(getClass()).info("Lock acquired for service {}", service.getId());

	    running = true;

	    MessageChannels.getWritable().removeAll(service.getId());

	    new Thread(service::start).start();

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

	if (!lock.renew()) {

	    GSLoggerFactory.getLogger(getClass()).info("Lock lost of service {}", service.getId());

	    // lock lost → stop immediately
	    stopService();
	}
    }

    /**
     *
     */
    private void stopService() {

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
    void shutdown() {

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
