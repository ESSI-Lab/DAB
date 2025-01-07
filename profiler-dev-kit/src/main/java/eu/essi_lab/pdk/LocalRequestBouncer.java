package eu.essi_lab.pdk;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
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
public class LocalRequestBouncer extends AbstractRequestBouncer {

    // private int maximumSimultaneousRequests = 1;
    //
    // private int maximumSimultaneousRequestsByIP = 1;

    Logger logger = GSLoggerFactory.getLogger(LocalRequestBouncer.class);

    // a semaphore on the max total requests
    private int currentRequests = 0;
    // a semaphore for each IP address
    private final Map<String, Integer> currentRequestsPerIP = new HashMap<>();
    // here the next ip will be the first element
    private final LinkedList<SimpleEntry<String, Integer>> ipQueue = new LinkedList();
    // by default it waits one second
    private int waitTime = 1000;

    public LocalRequestBouncer(int maxTotalRequests, int maxTotalRequestsPerIP, int maximumConcurrentRequestsPerIp) {
	super(maxTotalRequests, maxTotalRequestsPerIP, maximumConcurrentRequestsPerIp);
	GSLoggerFactory.getLogger(getClass()).info("Initialized local request bouncer");
    }

    /**
     * Asks the bouncer the permission to execute and will FIFO wait (for the specified time) in case too many requests
     * are being executed until a free slot will become available. Then the calling thread will start executing the
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

	logger.trace("REQUEST BOUNCER WAIT {} {}", ipAddress, requestId);

	synchronized (ipQueue) {
	    boolean found = false;
	    for (SimpleEntry<String, Integer> ipEntry : ipQueue) {
		if (ipEntry.getKey().equals(ipAddress)) {
		    ipEntry.setValue(ipEntry.getValue() + 1);
		    found = true;
		}
	    }
	    if (!found) {
		ipQueue.add(new SimpleEntry<>(ipAddress, 1));
	    }
	}

	long maximum = System.currentTimeMillis() + unit.toMillis(timeout);
	while (System.currentTimeMillis() < maximum) {
	    Thread.sleep(waitTime);

	    synchronized (ipQueue) {
		// if it's the turn of this ip address
		SimpleEntry<String, Integer> first = ipQueue.getFirst();
		;
		if (first.getKey().equals(ipAddress)) {
		    // if the maximum requests constraint is not met
		    if (currentRequests < getMaximumConcurrentRequests()) {
			// if the maximum requests per IP constraint is not met
			currentRequestsPerIP.putIfAbsent(ipAddress, 0);
			Integer current = currentRequestsPerIP.get(ipAddress);
			if (current < getMaximumConcurrentRequestsPerIP()) {
			    // it's your turn!
			    Integer currentWaiting = first.getValue();
			    if (currentWaiting.equals(1)) {
				// was the last
				ipQueue.removeFirst();
			    } else {
				// we put the others at the end, to let other ip addresses to execute
				ipQueue.removeFirst();
				first.setValue(--currentWaiting);
				ipQueue.add(first);
			    }
			    currentRequestsPerIP.put(ipAddress, ++current);
			    currentRequests++;
			    logger.trace("REQUEST BOUNCER START {} {}", ipAddress, requestId);

			    return true;
			}
		    }
		}
	    }
	}
	logger.trace("REQUEST BOUNCER BLOCKED {} {}", ipAddress, requestId);

	synchronized (ipQueue) {
	    for (SimpleEntry<String, Integer> ipEntry : ipQueue) {
		if (ipEntry.getKey().equals(ipAddress)) {
		    Integer value = ipEntry.getValue();
		    value = value - 1;
		    if (value.equals(0)) {
			ipQueue.remove(ipEntry);
		    } else {
			ipEntry.setValue(value);
		    }
		    break;
		}
	    }
	}

	return false;

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

	logger.trace("REQUEST BOUNCER REMOVE {} {}", ipAddress, requestId);
	synchronized (ipQueue) {
	    currentRequests--;
	    Integer current = currentRequestsPerIP.get(ipAddress);
	    if (current == null) {
		logger.trace("REQUEST BOUNCER REQUEST NOT FOUND {} {}", ipAddress, requestId);
		return false;
	    }
	    current--;
	    if (current == 0) {
		currentRequestsPerIP.remove(ipAddress);
	    } else {
		currentRequestsPerIP.put(ipAddress, current);
	    }
	    return true;
	}

    }

    public void setWaitTime(int ms) {
	this.waitTime = ms;

    }

}
