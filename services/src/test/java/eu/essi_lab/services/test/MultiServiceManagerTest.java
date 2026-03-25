package eu.essi_lab.services.test;

import eu.essi_lab.services.*;
import eu.essi_lab.services.impl.*;
import redis.clients.jedis.*;

import java.util.*;
import java.util.concurrent.*;

public class MultiServiceManagerTest {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

	JedisPool pool = new JedisPool("localhost", 6379);

	Jedis resource = pool.getResource();

	String ping = resource.ping();

	String nodeId = java.net.InetAddress.getLocalHost().getHostName();

	ServiceDefinition def0 = ServiceDefinition.of("0", TestService.class);
	ServiceDefinition def1 = ServiceDefinition.of("1", TestService.class);
	ServiceDefinition def2 = ServiceDefinition.of("2", TestService.class);
	ServiceDefinition def3 = ServiceDefinition.of("3", TestService.class);
	ServiceDefinition def4 = ServiceDefinition.of("4", TestService.class);
	ServiceDefinition def5 = ServiceDefinition.of("5", TestService.class);
	ServiceDefinition def6 = ServiceDefinition.of("6", TestService.class);
	ServiceDefinition def7 = ServiceDefinition.of("7", TestService.class);
	ServiceDefinition def8 = ServiceDefinition.of("8", TestService.class);
	ServiceDefinition def9 = ServiceDefinition.of("9", TestService.class);

	MultiServiceManager.initDistributed( //
		pool, //
		nodeId, //
		5, //
		100, //
		List.of());//

	MultiServiceManager.get().start();

	Runtime.getRuntime().addShutdownHook(new Thread(MultiServiceManager.get()::shutdown));

	//
	//
	//

	Timer timer = new Timer();

	TimerTask activeTask = new TimerTask() {
	    @Override
	    public void run() {

		List<Map.Entry<String, String>> activeServices = MultiServiceManager.get().getActiveServices();

		activeServices.forEach(entry -> {

		    System.out.println("Host: " + entry.getKey() + "\nService: " + entry.getValue());
		});

		//		manager.getActiveServices().forEach(service -> GSLoggerFactory.getLogger(getClass()).info("Active: " + service.getId()));
	    }
	};

	timer.scheduleAtFixedRate(activeTask, 0, TimeUnit.SECONDS.toMillis(5));

	//
	//
	//

	Timer timer1 = new Timer();

	TimerTask defTask = new TimerTask() {

	    @Override
	    public void run() {

		MultiServiceManager.get().setDefinitions(List.of(def0, def1));
	    }
	};

	timer1.schedule(defTask, TimeUnit.SECONDS.toMillis(5));

	//
	//
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
