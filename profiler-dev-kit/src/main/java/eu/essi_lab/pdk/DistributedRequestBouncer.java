package eu.essi_lab.pdk;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.HostNamePropertyUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;

/**
 * Allows the coordination of execution of multiple simultaneous requests by enforcing some rules:
 * There is a maximum number of requests that a specific client IP can simultaneous execute. Additional requests from
 * the same IP beyond the maximum number will wait.
 * The redis support enables distributed implementation of the bouncer.
 * 
 * @author boldrini
 */
public class DistributedRequestBouncer extends AbstractRequestBouncer {

    Logger logger = GSLoggerFactory.getLogger(DistributedRequestBouncer.class);

    private static final String REDIS_DEFAULT_HOSTNAME = "localhost";
    private static final int REDIS_DEFAULT_PORT = 6379;
    private static final String DB_DEFAULT_HASH = "default";

    private long oldRequestsMs = TimeUnit.MINUTES.toMillis(20);

    public long getOldRequestsMs() {
	return oldRequestsMs;
    }

    public void setOldRequestsMs(long oldRequestsMs) {
	this.oldRequestsMs = oldRequestsMs;
    }

    // by default it waits one second
    private int pollTimeMs = 1000;

    private JedisPool pool = null;

    public JedisPool getPool() {
	return pool;
    }

    // LIST of HOSTS
    public static final String HOSTS_KEY = "{hosts}";
    public static final String HOST_KEY = "{hosts}-";

    // HASHSET of each request
    public static final String REQUESTS_KEY = "requests";
    public static final String REQUEST_KEY = "request";
    public static final String REQUEST_ID_PROPERTY = "id";
    public static final String REQUEST_IP_PROPERTY = "ip";
    public static final String REQUEST_HOSTNAME_PROPERTY = "hostname";
    public static final String REQUEST_DATESTAMP_PROPERTY = "datestamp";

    // LIST of EXECUTING requests
    public static final String WAITING_ON_IP_KEY = "waiting_on_";

    // LIST of EXECUTING requests
    public static final String EXECUTING_KEY = "executing";
    public static final String EXECUTING_ON_IP_KEY = "executing_on_";

    private boolean facilitateOtherIps = true;

    private String hostname;

    private int port;

    private String hash;

    private LocalRequestBouncer backupBouncer;

    public boolean isFacilitateOtherIps() {
	return facilitateOtherIps;
    }

    public void setFacilitateOtherIps(boolean facilitateOtherIps) {
	this.facilitateOtherIps = facilitateOtherIps;
    }

    public String getHash() {
	return hash;
    }

    public void setHash(String hash) {
	this.hash = hash;
    }

    public DistributedRequestBouncer(int maxTotalRequests, int maxTotalRequestsPerIP, int overallMaxRequestsPerIp) {
	this(REDIS_DEFAULT_HOSTNAME, REDIS_DEFAULT_PORT, DB_DEFAULT_HASH, maxTotalRequests, maxTotalRequestsPerIP, overallMaxRequestsPerIp);
	this.backupBouncer = new LocalRequestBouncer(maxTotalRequests, maxTotalRequestsPerIP, maxTotalRequestsPerIP);
    }

    private boolean isHealthy = true;

    public boolean isHealthy() {
	return isHealthy;

    }

    public boolean isHealthyNow() {
	try (Jedis jedis = pool.getResource()) {
	    String pong = jedis.ping();
	    if (pong.equals("PONG")) {
		return true;
	    }
	}
	return false;
    }

    public void close() {
	if (pool != null) {
	    pool.close();
	}
    }

    public DistributedRequestBouncer(String hostname, int port, String hash, int maxTotalRequests, int maxTotalRequestsPerIP,
	    int overallMaxRequestsPerIp) {
	super(maxTotalRequests, maxTotalRequestsPerIP, overallMaxRequestsPerIp);
	GSLoggerFactory.getLogger(getClass()).info(
		"Initializing distributed request bouncer, host: {}, port: {}, hash: {}, maxRequest: {}, maxRequestPerIp: {}, overall max requests per IP: {}",
		hostname, port, hash, maxTotalRequests, maxTotalRequestsPerIP, overallMaxRequestsPerIp);
	this.hostname = hostname;
	this.port = port;
	this.hash = hash;
	JedisPoolConfig poolConfig = new JedisPoolConfig();
	poolConfig.setMaxTotal(200); // Maximum number of connections
	poolConfig.setMaxIdle(128);
	poolConfig.setBlockWhenExhausted(true);
	poolConfig.setMaxWait(Duration.ofMillis(1000));
	poolConfig.setMinIdle(50);
	this.pool = new JedisPool(poolConfig, hostname, port);
	if (!isHealthyNow()) {
	    GSLoggerFactory.getLogger(getClass()).error("unhealthy at startup");
	    throw new RuntimeException();
	}

	GSLoggerFactory.getLogger(getClass()).info("Initialized distributed request bouncer");

	ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

	Runnable task = new Runnable() {

	    @Override
	    public void run() {

		try (Jedis jedis = pool.getResource()) {
		    // here host information is updated to inform about hosts in good health
		    String myHostname = HostNamePropertyUtils.getHostNameProperty();
		    Transaction t = jedis.multi();
		    t.sadd(HOSTS_KEY, myHostname);
		    t.set(HOST_KEY + myHostname, ISO8601DateTimeUtils.getISO8601DateTime());
		    t.exec();
		    isHealthy = true;

		    checkForOldBlockingRequests(jedis);

		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e);
		    isHealthy = false;
		}
	    }
	};

	executor.scheduleAtFixedRate(task, 0, 20, TimeUnit.SECONDS);

    }

    public void checkForOldBlockingRequests(Jedis jedis) {
	// requests from dead hosts

	Set<String> deadHosts = getDeadHosts(jedis);

	List<String> requests = jedis.lrange(getPrefix() + REQUESTS_KEY, 0, -1);
	for (String requestId : requests) {
	    boolean old = checkIsOldRequest(jedis, requestId, deadHosts);
	    if (old) {
		// remove old requests from the list and from the executing queues (as they might block everything)
		BouncerRequest request = getRequestById(jedis, requestId);
		String ip = null;
		String dateStamp = null;
		if (request != null) {
		    ip = request.getIp();
		    dateStamp = request.getDateStamp();
		}
		GSLoggerFactory.getLogger(getClass()).error("Bouncer removed old request: {} ({})", requestId, dateStamp);
		Transaction t = jedis.multi();
		t.lrem(getPrefix() + REQUESTS_KEY, 0, requestId);
		t.del(getRequestKey(requestId));
		t.lrem(getPrefix() + EXECUTING_KEY, 0, requestId);
		if (ip != null) {
		    t.lrem(getExecutingOnKey(ip), 0, requestId);
		    t.lrem(getWaitingOnKey(ip), 0, requestId);
		}
		List<Object> res = t.exec();
		if (res == null) {
		    GSLoggerFactory.getLogger(getClass()).error("[BOUNCER] error while removing request in check old requests: {}",
			    requestId);
		}
	    }

	}
	Transaction t = jedis.multi();
	for (String offline : deadHosts) {
	    t.srem(HOSTS_KEY, offline);
	    t.del(HOST_KEY + offline);
	}
	t.exec();

    }

    /**
     * Returns hosts that hasn't signaled their status in three minutes
     * 
     * @param jedis
     * @return
     */
    private Set<String> getDeadHosts(Jedis jedis) {
	HashSet<String> deadHosts = new HashSet<>();
	Set<String> hostnames = jedis.smembers(HOSTS_KEY);
	long now = System.currentTimeMillis();
	for (String hostname : hostnames) {
	    String value = jedis.get(HOST_KEY + hostname);
	    if (value != null) {
		long time = ISO8601DateTimeUtils.parseISO8601ToDate(value).get().getTime();
		long gap = now - time;
		if (gap > TimeUnit.MINUTES.toMillis(1)) {
		    // offline host
		    deadHosts.add(hostname);
		}
	    }
	}
	return deadHosts;
    }

    /**
     * Checks if the request is old because is from a dead host or it took too long to complete (20 minutes)
     * 
     * @param jedis
     * @param requestId
     * @param offlineHosts
     * @return
     */
    private boolean checkIsOldRequest(Jedis jedis, String requestId, Set<String> offlineHosts) {
	String dateStamp = jedis.hget(getRequestKey(requestId), REQUEST_DATESTAMP_PROPERTY);
	if (dateStamp == null) {
	    return true;
	}
	long requestTime = ISO8601DateTimeUtils.parseISO8601ToDate(dateStamp).get().getTime();
	long now = System.currentTimeMillis();
	long gap = now - requestTime;
	if (gap > oldRequestsMs) {
	    return true;
	} else {
	    String hostname = jedis.hget(getRequestKey(requestId), REQUEST_HOSTNAME_PROPERTY);
	    if (hostname == null || (hostname != null && offlineHosts.contains(hostname))) {
		return true;
	    }
	    return false;
	}
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
	if (!isHealthy) {
	    GSLoggerFactory.getLogger(getClass()).error("Using backup local bouncer");
	    return backupBouncer.askForExecutionAndWait(ipAddress, requestId, timeout, unit);
	}
	ipAddress = validate(ipAddress);
	requestId = validate(requestId);
	String hostname = HostNamePropertyUtils.getHostNameProperty();

	pushRequestInformation(hostname, requestId, ipAddress);

	logger.trace("REQUEST BOUNCER WAIT {} {}", ipAddress, requestId);
	String waitingOnIpKey = getWaitingOnKey(ipAddress);
	String executingOnIpKey = getExecutingOnKey(ipAddress);

	long start = System.currentTimeMillis();
	long maxWaitTime = unit.toMillis(timeout);
	int errorsRemovingRequest = 0;
	while (true) {
	    try {
		if ((System.currentTimeMillis() - start) > maxWaitTime) {
		    // waited for execution for too long, returning error
		    GSLoggerFactory.getLogger(getClass()).error("[BOUNCER] Waited too long: {}", requestId);
		    List<Object> ret = null;
		    while (ret == null) {
			try (Jedis jedis = pool.getResource()) {
			    Transaction t = jedis.multi();
			    t.lrem(getPrefix() + REQUESTS_KEY, 0, requestId);
			    t.del(getRequestKey(requestId));
			    t.lrem(getWaitingOnKey(ipAddress), 0, requestId);
			    ret = t.exec();
			    if (ret == null) {
				errorsRemovingRequest++;
				if (errorsRemovingRequest > 10) {
				    GSLoggerFactory.getLogger(getClass()).error(
					    "[BOUNCER] error removing request - for execution: {} ({})", requestId,
					    errorsRemovingRequest);
				}
				Thread.sleep(1000);
			    }else {
				GSLoggerFactory.getLogger(getClass()).info("[BOUNCER] removed request {}",requestId);
			    }
			}

		    }
		    return false;
		}

		try (Jedis jedis = pool.getResource()) {
		    jedis.watch(getPrefix() + EXECUTING_KEY);
		    jedis.watch(waitingOnIpKey);
		    List<String> executing = jedis.lrange(getPrefix() + EXECUTING_KEY, 0, -1);
		    List<String> waitingOnIp = jedis.lrange(waitingOnIpKey, 0, -1);
		    if (!waitingOnIp.contains(requestId)) {
			// it may happen that the request is removed by the old request check
			GSLoggerFactory.getLogger(getClass()).error("[BOUNCER] Others removed this request: {}", requestId);
			return false;
		    }
		    if (waitingOnIp.size() >= getMaximumOverallRequestsPerIp()) {
			// the request is discharged, as too many requests from this IP are waiting
			Transaction t = jedis.multi();
			t.lrem(waitingOnIpKey, 0, requestId);
			t.lrem(getPrefix() + REQUESTS_KEY, 0, requestId);
			t.del(getRequestKey(requestId));
			List<Object> exec = t.exec();

			if (exec == null) {
			    errorsRemovingRequest++;
				if (errorsRemovingRequest > 10) {
				    GSLoggerFactory.getLogger(getClass()).error(
					    "[BOUNCER] error removing request - for too much requests: {} ({})", requestId,
					    errorsRemovingRequest);
				}
			    Thread.sleep(1000);
			    continue;
			} else {
			    GSLoggerFactory.getLogger(getClass()).info("[BOUNCER] Removed request {}", requestId);
			}
			GSLoggerFactory.getLogger(getClass()).error("[BOUNCER] Too many requests from this ip: {} {}", requestId,
				ipAddress);
			return false;
		    }
		    List<String> executingOnIp = jedis.lrange(executingOnIpKey, 0, -1);

		    if (executing.size() >= getMaximumConcurrentRequests() || executingOnIp.size() >= getMaximumConcurrentRequestsPerIP()) {

			sleep();
			continue;
		    }
		    // if facilitate other ip mode is active and
		    // if a request from the same ip is executed then:
		    // it sleeps a bit to facilitate executions from different ips
		    if (facilitateOtherIps && executingOnIp.size() > 0) {
			List<String> requests = jedis.lrange(getPrefix() + REQUESTS_KEY, 0, -1);
			boolean otherWaiting = false;
			for (String reqId : requests) {
			    BouncerRequest req = getRequestById(jedis, reqId);
			    if (req != null) {
				if (!req.getIp().equals(ipAddress)) {
				    otherWaiting = true;
				    break;
				}
			    }
			}
			// if no other is waiting, no need to facilitate
			if (otherWaiting) {
			    System.out.println("facilitating other than " + ipAddress);
			    Thread.sleep(pollTimeMs * executingOnIp.size());
			}
		    }

		    Transaction t = jedis.multi();
		    t.rpush(getPrefix() + EXECUTING_KEY, requestId);
		    t.rpush(executingOnIpKey, requestId);
		    t.lrem(waitingOnIpKey, 0, requestId);
		    List<Object> ret = t.exec();
		    if (ret != null) {
			break;
		    } else {
			// some other thread has executed before
			sleep();
			continue;
		    }
		}
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	}

	// if (ret == null || ret.size() != 1 || !ret.get(0).equals(RUN)) {
	// // timeout
	// logger.trace("REQUEST BOUNCER BLOCKED {} {}", ipAddress, requestId);
	//
	// return false;
	// }

	logger.trace("REQUEST BOUNCER START {} {}", ipAddress, requestId);

	return true;

    }

    private String getPrefix() {
	return "{" + hash + "}.";
    }

    public static void main(String[] args) {
	// System.out.println(TimeUnit.MINUTES.toMillis(5));
	// System.out.println(System.currentTimeMillis()-1708240978036l);
	// DistributedRequestBouncer b = new DistributedRequestBouncer("essi-lab.eu", REDIS_DEFAULT_PORT, "", 0, 0, 0);
	// JedisPool p = b.getPool();
	// Jedis jedis = p.getResource();
	// b.checkForOldBlockingRequests(jedis);
	long oldRequestsMs = TimeUnit.MINUTES.toMillis(5);
	System.out.println(oldRequestsMs);
	System.out.println(System.currentTimeMillis() - 1708378723265l);
    }

    private String getExecutingOnKey(String ipAddress) {
	return getPrefix() + EXECUTING_ON_IP_KEY + ipAddress;
    }

    private String getWaitingOnKey(String ipAddress) {
	return getPrefix() + WAITING_ON_IP_KEY + ipAddress;
    }

    private BouncerRequest getRequestById(Jedis jedis, String requestId) {
	String ip = jedis.hget(getRequestKey(requestId), REQUEST_IP_PROPERTY);
	String hostname = jedis.hget(getRequestKey(requestId), REQUEST_HOSTNAME_PROPERTY);
	String datestamp = jedis.hget(getRequestKey(requestId), REQUEST_DATESTAMP_PROPERTY);
	if (ip == null || hostname == null || datestamp == null) {
	    return null;
	}
	BouncerRequest req = new BouncerRequest(ip, hostname, datestamp, requestId);
	return req;
    }

    private void pushRequestInformation(String hostname, String requestId, String ipAddress) {
	if (hostname == null || hostname.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).error("bouncer missing hostname");
	}
	if (requestId == null || requestId.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).error("bouncer missing requestId");
	}
	if (ipAddress == null || ipAddress.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).error("bouncer missing ip address");
	}
	List<Object> result = null;
	try (Jedis jedis = pool.getResource()) {
	    while (result == null) {
		Transaction t = jedis.multi();
		t.rpush(getPrefix() + REQUESTS_KEY, requestId);
		String waitingOnIpKey = getWaitingOnKey(ipAddress);
		t.rpush(waitingOnIpKey, requestId);
		t.hset(getRequestKey(requestId), REQUEST_ID_PROPERTY, requestId);
		t.hset(getRequestKey(requestId), REQUEST_IP_PROPERTY, ipAddress);
		t.hset(getRequestKey(requestId), REQUEST_HOSTNAME_PROPERTY, hostname);
		t.hset(getRequestKey(requestId), REQUEST_DATESTAMP_PROPERTY, ISO8601DateTimeUtils.getISO8601DateTime());
		result = t.exec();
		if (result == null) {
		    try {
			Thread.sleep(1000);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		    GSLoggerFactory.getLogger(getClass()).error("error pushing request, retrying");
		}
	    }
	}
    }

    /**
     * To be called by the request executor at execution end to free up space for more request executions.
     * 
     * @param ipAddress
     * @param requestId
     * @return
     */
    public boolean notifyExecutionEnded(String ipAddress, String requestId) {
	if (!isHealthy) {
	    GSLoggerFactory.getLogger(getClass()).error("Using backup local bouncer");
	    return backupBouncer.notifyExecutionEnded(ipAddress, requestId);
	}
	ipAddress = validate(ipAddress);
	requestId = validate(requestId);
	try (Jedis jedis = pool.getResource()) {
	    String executingOnKey = getExecutingOnKey(ipAddress);
	    List<Object> result = null;
	    do {
		Transaction t = jedis.multi();
		t.lrem(getPrefix() + REQUESTS_KEY, 0, requestId);
		t.del(getRequestKey(requestId));
		t.lrem(getPrefix() + EXECUTING_KEY, 0, requestId);
		t.lrem(executingOnKey, 0, requestId);
		result = t.exec();
		if (result == null) {
		    try {
			GSLoggerFactory.getLogger(getClass()).error("[BOUNCER] error while removing request: {}", requestId);
			Thread.sleep(1000);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		    GSLoggerFactory.getLogger(getClass()).info("Transaction failed");
		}
	    } while (result == null);
	}

	logger.trace("REQUEST BOUNCER REMOVE {} {}", ipAddress, requestId);

	return true;

    }

    private String getRequestKey(String requestId) {
	return getPrefix() + REQUEST_KEY + "#" + requestId;
    }

    public void setPollTimeMs(int ms) {
	this.pollTimeMs = ms;

    }

    private void sleep() {
	try {
	    Thread.sleep(pollTimeMs);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    public void clearDB() {
	try (Jedis jedis = pool.getResource()) {
	    Set<String> keys = jedis.keys("*");
	    for (String key : keys) {
		if (key.startsWith(getPrefix() + REQUEST_KEY) || //
			key.startsWith(getPrefix() + EXECUTING_KEY) || //
			key.startsWith(getPrefix() + EXECUTING_ON_IP_KEY)) {
		    jedis.del(key);
		}
	    }
	}
    }

    public String getHostname() {
	return hostname;
    }

    public int getPort() {
	return port;
    }

}
