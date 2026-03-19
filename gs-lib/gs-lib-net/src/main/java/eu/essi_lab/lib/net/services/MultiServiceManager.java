package eu.essi_lab.lib.net.services;

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
    private static final int TTL_SECONDS = 30;

    /**
     *
     */
    private static final long SCAN_DELAY_SECONDS = 5;

    /**
     *
     */
    private static final int SCHEDULER_THREAD_POOL_SIZE = 1;

    private final JedisPool jedisPool;
    private final String nodeId;
    private final int maxServices;
    private final ScheduledExecutorService scheduler;

    private volatile Map<String, DistributedServiceRunner> active;
    private volatile List<ServiceDefinition> definitions;

    /**
     * @param pool
     * @param nodeId
     * @param maxServices
     * @param defs
     */
    public MultiServiceManager(JedisPool pool, String nodeId, int maxServices) {

	this(pool, nodeId, maxServices, List.of());
    }

    /**
     * @param pool
     * @param nodeId
     * @param maxServices
     * @param defs
     */
    public MultiServiceManager(JedisPool pool, String nodeId, int maxServices, List<ServiceDefinition> defs) {
	this.jedisPool = pool;
	this.nodeId = nodeId;
	this.maxServices = maxServices;
	this.definitions = defs;
	this.active = new ConcurrentHashMap<>();
	this.scheduler = Executors.newScheduledThreadPool(SCHEDULER_THREAD_POOL_SIZE);
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

	RedisDistributedLock lock = new RedisDistributedLock(jedisPool, def.id, TTL_SECONDS, nodeId);

	GSLoggerFactory.getLogger(getClass()).info("Trying to acquire lock for service {}", def.id);

	if (!lock.tryAcquire()) {

	    GSLoggerFactory.getLogger(getClass()).info("Lock for service {} already acquired", def.id);

	    return;
	}

	GSLoggerFactory.getLogger(getClass()).info("Lock for service {} acquired", def.id);

	ManagedService service = def.create();

	DistributedServiceRunner runner = new DistributedServiceRunner(service, lock);

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
    public  List<Map.Entry<String, String>> getActiveServices() {

	return getActiveServices(definitions, jedisPool);
    }

    /**
     * @return
     */
    public static List<Map.Entry<String, String>> getActiveServices(List<ServiceDefinition> defs, JedisPool jedisPool) {

	return defs.stream().map(def -> {

	    try (Jedis jedis = jedisPool.getResource()) {

		String value = jedis.get(RedisDistributedLock.getKey(def.getId()));

		if (value != null && !value.equals("nil")) {

		    String host = value.split(":")[0];
		    String id = value.split(":")[1];

		    return Map.entry(host, id);
		}

		return null;
	    }

	}).filter(Objects::nonNull).toList();//
    }
}
