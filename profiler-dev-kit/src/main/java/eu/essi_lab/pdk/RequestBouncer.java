package eu.essi_lab.pdk;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * Allows the coordination of execution of multiple simultaneous requests by enforcing some rules:
 * There is a maximum number of requests that a specific client IP can simultaneous execute. Additional requests from
 * the same IP beyond the maximum number will wait.
 * 
 * @author boldrini
 */
public class RequestBouncer {

    private int maximumSimultaneousRequestsByIP = 3;

    Logger logger = GSLoggerFactory.getLogger(RequestBouncer.class);

    public int getMaximumSimultaneousRequestsByIP() {
	return maximumSimultaneousRequestsByIP;
    }

    private ConcurrentHashMap<String, ArrayBlockingQueue<String>> queues = new ConcurrentHashMap<>();

    public RequestBouncer() {
    }

    /**
     * Constructs a new request bouncer, with the given maximum number of allowed requests per IP.
     * 
     * @param maximumSimultaneousRequestsByIP
     */
    public RequestBouncer(int maximumSimultaneousRequestsByIP) {
	this.maximumSimultaneousRequestsByIP = maximumSimultaneousRequestsByIP;
    }

    /**
     * Asks the bouncer the permission to execute and will FIFO wait (for the specified time) in case too many requests
     * are being executed until a free slot will become available. Then the calling threadwill start executing the
     * request and at execution end will notify using notifyExecutionEnded method.
     * 
     * @param ipAddress
     * @param requestId
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     */
    public boolean askForExecutionAndWait(String ipAddress, String requestId, long timeout, TimeUnit unit) throws InterruptedException {
	ipAddress = validate(ipAddress);
	requestId = validate(requestId);
	synchronized (queues) {
	    ArrayBlockingQueue<String> queue = queues.get(ipAddress);
	    if (queue == null) {
		queues.put(ipAddress, new ArrayBlockingQueue<String>(maximumSimultaneousRequestsByIP, true));
	    }
	}
	ArrayBlockingQueue<String> queue = queues.get(ipAddress);
	logger.trace("REQUEST BOUNCER WAIT {} {}", ipAddress, requestId);
	boolean ret = queue.offer(requestId, timeout, unit);
	if (ret) {
	    logger.trace("REQUEST BOUNCER START {} {}", ipAddress, requestId);
	} else {
	    logger.trace("REQUEST BOUNCER BLOCKED {} {}", ipAddress, requestId);
	}
	return ret;
    }

    /**
     * To be called by the request executor at execution end to free up space for more request executions.
     * 
     * @param ipAddress
     * @param requestId
     * @return
     */
    public boolean notifyExecutionEnded(String ipAddress, String requestId) {
	ipAddress = validate(ipAddress);
	requestId = validate(requestId);
	ArrayBlockingQueue<String> queue = queues.get(ipAddress);
	if (queue != null) {
	    boolean ret = queue.remove(requestId);
	    logger.trace("REQUEST BOUNCER REMOVE {} {}", ipAddress, requestId);
	    return ret;
	}
	logger.trace("REQUEST BOUNCER REQUEST NOT FOUND {} {}", ipAddress, requestId);
	return false;
    }

    private String validate(String ipAddress) {
	if (ipAddress == null || ipAddress.isEmpty()) {
	    return "unknown";
	} else {
	    return ipAddress;
	}
    }

}
