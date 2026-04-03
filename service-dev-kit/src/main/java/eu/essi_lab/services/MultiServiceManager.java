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
import redis.clients.jedis.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Fabrizio
 */
public class MultiServiceManager {

    /**
     *
     */
    private static final int DEFAULT_TTL_SECONDS = 30;

    /**
     *
     */
    private static final int DEFAULT_RENEW_SECONDS = 10;

    /**
     *
     */
    private static final long SCAN_DELAY_SECONDS = 5;

    /**
     *
     */
    private static final int SCHEDULER_THREAD_POOL_SIZE = 1;

    private final JedisPool jedisPool;
    private final String hostName;
    private final int maxServices;
    private final ScheduledExecutorService scheduler;

    private int ttlSeconds = DEFAULT_TTL_SECONDS;
    private int heartbeatSeconds = DEFAULT_RENEW_SECONDS;

    private volatile Map<String, DistributedServiceRunner> active;
    private volatile List<ManagedServiceSetting> settings;

    private static MultiServiceManager INSTANCE;

    /**
     * Creates a new instance of {@link MultiServiceManager} with local services coordination
     *
     * @param hostName
     * @param channelSize
     * @return
     */
    public static void initLocal(String hostName, int channelSize) {

	INSTANCE = new MultiServiceManager(hostName, channelSize);
    }

    /**
     * Creates a new instance of {@link MultiServiceManager} with local services coordination
     *
     * @param hostName
     * @param channelSize
     * @param settings
     * @return
     */
    public static void initLocal(String hostName, int channelSize, List<ManagedServiceSetting> settings) {

	INSTANCE = new MultiServiceManager(hostName, channelSize, settings);
    }

    /**
     * Creates a new instance of {@link MultiServiceManager} with distributed services coordination
     *
     * @param pool
     * @param hostName
     * @param channelSize
     * @param maxServices
     * @return
     */
    public static void initDistributed(JedisPool pool, String hostName, int maxServices, int channelSize) {

	if (pool == null) {

	    throw new IllegalArgumentException("JedisPool cannot be null");
	}

	INSTANCE = new MultiServiceManager(pool, hostName, maxServices, channelSize);
    }

    /**
     * Creates a new instance of {@link MultiServiceManager} with distributed services coordination
     *
     * @param pool
     * @param hostName
     * @param channelSize
     * @param maxServices
     * @param settings
     * @return
     */
    public static void initDistributed(JedisPool pool, String hostName, int maxServices, int channelSize,
	    List<ManagedServiceSetting> settings) {

	if (pool == null) {

	    throw new IllegalArgumentException("JedisPool cannot be null");
	}

	INSTANCE = new MultiServiceManager(pool, hostName, maxServices, channelSize, settings);
    }

    /**
     * @return
     */
    public static boolean isInitialized() {

	return INSTANCE != null;
    }

    /**
     * @return
     */
    public static MultiServiceManager get() {

	if (INSTANCE == null) {

	    throw new IllegalStateException("MultiServiceManager has not been initialized");
	}

	return INSTANCE;
    }

    /**
     * Creates a new instance of {@link MultiServiceManager} with local services coordination
     *
     * @param hostName
     * @param channelSize
     */
    private MultiServiceManager(String hostName, int channelSize) {

	this(null, hostName, Integer.MAX_VALUE, channelSize, List.of());
    }

    /**
     * Creates a new instance of {@link MultiServiceManager} with local services coordination
     *
     * @param hostName
     * @param channelSize
     * @param defs
     */
    private MultiServiceManager(String hostName, int channelSize, List<ManagedServiceSetting> defs) {

	this(null, hostName, Integer.MAX_VALUE, channelSize, defs);
    }

    /**
     * Creates a new instance of {@link MultiServiceManager} with distributed services coordination
     *
     * @param pool
     * @param hostName
     * @param maxServices
     * @param channelSize
     * @param defs
     */
    private MultiServiceManager(JedisPool pool, String hostName, int maxServices, int channelSize) {

	this(pool, hostName, maxServices, channelSize, List.of());
    }

    /**
     * Creates a new instance of {@link MultiServiceManager} with distributed services coordination
     *
     * @param pool
     * @param hostName
     * @param maxServices
     * @param channelSize
     * @param defs
     */
    private MultiServiceManager(JedisPool pool, String hostName, int maxServices, int channelSize, List<ManagedServiceSetting> defs) {
	this.jedisPool = pool;
	this.hostName = hostName;
	this.maxServices = maxServices;
	this.settings = defs;
	this.active = new ConcurrentHashMap<>();
	this.scheduler = Executors.newScheduledThreadPool(SCHEDULER_THREAD_POOL_SIZE);

	if (pool == null) {

	    MessageChannels.init(channelSize);

	} else {

	    MessageChannels.init(pool, channelSize);
	}
    }

    /**
     * @param ttlSeconds
     */
    public void setTTSeconds(int ttlSeconds) {

	this.ttlSeconds = ttlSeconds;
    }

    /**
     * @param renewSeconds
     */
    public void setHeartbeatSeconds(int renewSeconds) {

	this.heartbeatSeconds = renewSeconds;
    }

    /**
     * @param settings
     */
    public synchronized void setSettings(List<ManagedServiceSetting> settings) {

	this.settings = settings.stream().filter(ManagedServiceSetting::isEnabled).toList();

	//
	// shutdown and removes from the active map the service runner
	// that  are no longer in definitions list or that are disabled
	//
	List<String> list = this.settings.stream().
		map(ManagedServiceSetting::getServiceId).
		toList();

	active.keySet().stream().//
		filter(serviceId -> !list.contains(serviceId)).//
		forEach(serviceId -> {

	    active.get(serviceId).shutdown();
	    active.remove(serviceId);
	});//
    }

    /**
     * @return
     */
    public synchronized List<ManagedServiceSetting> getSettings() {

	return settings;
    }

    /**
     *
     */
    public void start() {

	GSLoggerFactory.getLogger(getClass()).info("Started");

	scheduler.scheduleWithFixedDelay(this::scan, 0, SCAN_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    /**
     *
     */
    private synchronized void scan() {

	if (active.size() >= maxServices) {

	    GSLoggerFactory.getLogger(getClass()).warn("Max services reached");

	    return;
	}

	for (ManagedServiceSetting set : settings) {

	    if (active.containsKey(set.getServiceId())) {

		continue;
	    }

	    if (active.size() >= maxServices) {

		GSLoggerFactory.getLogger(getClass()).warn("Max services reached");

		break;
	    }

	    tryStartService(set);
	}
    }

    /**
     * @param setting
     */
    private void tryStartService(ManagedServiceSetting setting) {

	ServiceLock lock = buildLock(setting.getServiceId());

	GSLoggerFactory.getLogger(getClass()).info("Trying to acquire lock for service {}", setting.getServiceId());

	if (!lock.tryAcquire()) {

	    return;
	}

	ManagedService service = setting.createService();

	DistributedServiceRunner runner = new DistributedServiceRunner(service, lock, heartbeatSeconds);

	active.put(setting.getServiceId(), runner);

	runner.start();
    }

    /**
     *
     */
    public void shutdown() {

	active.values().forEach(DistributedServiceRunner::shutdown);
	scheduler.shutdownNow();
    }

    /**
     * @return
     */
    public List<Map.Entry<String, String>> getActiveServices() {

	return jedisPool == null ? //
		getLocalActiveServices() : //
		getDistributedActiveServices(settings, jedisPool);//
    }

    /**
     * @return
     */
    private List<Map.Entry<String, String>> getLocalActiveServices() {

	return LocalServiceLock.ACTIVE_SERVICES.stream().toList();
    }

    /**
     * @return
     */
    private List<Map.Entry<String, String>> getDistributedActiveServices(List<ManagedServiceSetting> settings, JedisPool jedisPool) {

	return settings.stream().map(def -> {

	    try (Jedis jedis = jedisPool.getResource()) {

		String value = jedis.get(ServiceLock.getKey(def.getServiceId()));

		if (value != null && !value.equals("nil")) {

		    String host = value.split(":")[0];
		    String id = value.split(":")[1];

		    return Map.entry(host, id);
		}

		return null;
	    }

	}).filter(Objects::nonNull).toList();//
    }

    /**
     * @param serviceId
     * @return
     */
    private ServiceLock buildLock(String serviceId) {

	if (jedisPool == null) {

	    return new LocalServiceLock(serviceId, hostName);
	}

	return new RedisServiceLock(jedisPool, serviceId, ttlSeconds, hostName);
    }
}
