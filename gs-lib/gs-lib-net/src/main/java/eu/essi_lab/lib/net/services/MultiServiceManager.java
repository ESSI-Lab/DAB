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
     *
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

		GSLoggerFactory.getLogger(getClass()).info("Service {} is already running", def.id);

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
    public List<ManagedService> getActiveServices() {

	return active.values().stream().map(DistributedServiceRunner::getService).toList();
    }

    /**
     * @return
     */
    public List<DistributedServiceRunner> getActiveRunners() {

	return active.values().stream().toList();
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

	JedisPool pool = new JedisPool("dab.v4tdiw.clustercfg.use1.cache.amazonaws.com", 6379);

	String nodeId = java.net.InetAddress.getLocalHost().getHostName();

//	ServiceDefinition def0 = ServiceDefinition.of("0", TestService.class);
//	ServiceDefinition def1 = ServiceDefinition.of("1", TestService.class);
//	ServiceDefinition def2 = ServiceDefinition.of("2", TestService.class);
//	ServiceDefinition def3 = ServiceDefinition.of("3", TestService.class);
//	ServiceDefinition def4 = ServiceDefinition.of("4", TestService.class);
//	ServiceDefinition def5 = ServiceDefinition.of("5", TestService.class);
//	ServiceDefinition def6 = ServiceDefinition.of("6", TestService.class);
//	ServiceDefinition def7 = ServiceDefinition.of("7", TestService.class);
//	ServiceDefinition def8 = ServiceDefinition.of("8", TestService.class);
//	ServiceDefinition def9 = ServiceDefinition.of("9", TestService.class);

	MultiServiceManager manager = new MultiServiceManager( //
		pool, //
		nodeId, //
		5, //
		List.of());//

	manager.start();

	Runtime.getRuntime().addShutdownHook(new Thread(manager::shutdown));

	//
	//
	//

	Timer timer = new Timer();

	TimerTask activeTask = new TimerTask() {
	    @Override
	    public void run() {

		manager.getActiveServices().forEach(service -> GSLoggerFactory.getLogger(getClass()).info("Active: " + service.getId()));
	    }
	};

	timer.scheduleAtFixedRate(activeTask, 0, TimeUnit.SECONDS.toMillis(5));

	//
	//
	//

//	Timer timer1 = new Timer();
//
//	TimerTask defTask = new TimerTask() {
//
//	    @Override
//	    public void run() {
//
//		manager.setDefinitions(List.of(def0, def1, def2, def3, def4, def5, def6, def7, def8, def9));
//	    }
//	};
//
//	timer1.schedule(defTask, TimeUnit.SECONDS.toMillis(1));
//
//	Timer timer2 = new Timer();
//
//	TimerTask defTask2 = new TimerTask() {
//
//	    @Override
//	    public void run() {
//
//		manager.setDefinitions(List.of(def0));
//	    }
//	};
//
//	timer2.schedule(defTask2, TimeUnit.SECONDS.toMillis(45));

	//	Timer timer3 = new Timer();
	//
	//	TimerTask defTask3 = new TimerTask() {
	//
	//	    @Override
	//	    public void run() {
	//
	//		manager.setDefinitions(List.of(def0, def1, def2, def3, def4, def5, def6, def7, def8, def9));
	//	    }
	//	};
	//
	//	timer3.schedule(defTask3, TimeUnit.SECONDS.toMillis(55));

	//	Timer timer2 = new Timer();
	//
	//	TimerTask stopTask = new TimerTask() {
	//	    @Override
	//	    public void run() {
	//
	//		manager.getActiveRunners(). //
	//			stream().//
	//			filter(//
	//			s -> s.getService().getId().equals("0") || //
	//				s.getService().getId().equals("1") || //
	//				s.getService().getId().equals("2") || //
	//				s.getService().getId().equals("3") || //
	//				s.getService().getId().equals("4") //
	//		).//
	//			forEach(DistributedServiceRunner::stopService);
	//	    }
	//	};
	//
	//	timer2.schedule(stopTask, TimeUnit.SECONDS.toMillis(30));

	//
	//
	//

    }
}
