package eu.essi_lab.lib.net.service;

import eu.essi_lab.lib.net.service.lock.*;
import eu.essi_lab.lib.utils.*;
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
    private volatile List<ServiceDefinition> definitions;

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
     * @param defs
     * @return
     */
    public static void initLocal(String hostName, int channelSize, List<ServiceDefinition> defs) {

	INSTANCE = new MultiServiceManager(hostName, channelSize, defs);
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
     * @param defs
     * @return
     */
    public static void initDistributed(JedisPool pool, String hostName, int maxServices, int channelSize, List<ServiceDefinition> defs) {

	if (pool == null) {

	    throw new IllegalArgumentException("JedisPool cannot be null");
	}

	INSTANCE = new MultiServiceManager(pool, hostName, maxServices, channelSize, defs);
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
    private MultiServiceManager(String hostName, int channelSize, List<ServiceDefinition> defs) {

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
    private MultiServiceManager(JedisPool pool, String hostName, int maxServices, int channelSize, List<ServiceDefinition> defs) {
	this.jedisPool = pool;
	this.hostName = hostName;
	this.maxServices = maxServices;
	this.definitions = defs;
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
     * @param definitions
     */
    public synchronized void setDefinitions(List<ServiceDefinition> definitions) {

	GSLoggerFactory.getLogger(getClass()).info("Set definitions");

	this.definitions = definitions;

	// shutdown and removes from the active map the service runner
	// that  are no longer in definitions list
	List<String> list = definitions.stream().map(ServiceDefinition::getId).toList();

	active.keySet().stream().//
		filter(serviceId -> !list.contains(serviceId)).//
		forEach(serviceId -> {

	    active.get(serviceId).shutdown();
	    active.remove(serviceId);
	});//

	GSLoggerFactory.getLogger(getClass()).info("Active map: {}", active);
    }

    /**
     * @return
     */
    public List<ServiceDefinition> getDefinitions() {

	return definitions;
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

	    GSLoggerFactory.getLogger(getClass()).info("Max services reached");

	    return;
	}

	for (ServiceDefinition def : definitions) {

	    if (active.containsKey(def.getId())) {

		GSLoggerFactory.getLogger(getClass()).info("Service {} is already active", def.id);

		continue;
	    }

	    if (active.size() >= maxServices) {

		GSLoggerFactory.getLogger(getClass()).info("Max services reached");

		break;
	    }

	    tryStartService(def);
	}
    }

    /**
     * @param def
     */
    private void tryStartService(ServiceDefinition def) {

	ServiceLock lock = buildLock(def.id);

	GSLoggerFactory.getLogger(getClass()).info("Trying to acquire lock for service {}", def.id);

	if (!lock.tryAcquire()) {

	    GSLoggerFactory.getLogger(getClass()).info("Lock for service {} already acquired", def.id);

	    return;
	}

	GSLoggerFactory.getLogger(getClass()).info("Lock for service {} acquired", def.id);

	ManagedService service = def.create();

	DistributedServiceRunner runner = new DistributedServiceRunner(service, lock, heartbeatSeconds);

	active.put(def.id, runner);

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
		getDistributedActiveServices(definitions, jedisPool);//
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
    private List<Map.Entry<String, String>> getDistributedActiveServices(List<ServiceDefinition> defs, JedisPool jedisPool) {

	return defs.stream().map(def -> {

	    try (Jedis jedis = jedisPool.getResource()) {

		String value = jedis.get(ServiceLock.getKey(def.getId()));

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
