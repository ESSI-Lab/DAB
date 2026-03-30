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
//
	Jedis resource = pool.getResource();
//
	String ping = resource.ping();

	String nodeId = java.net.InetAddress.getLocalHost().getHostName();

	ManagedServiceSetting set0 = ManagedServiceSetting.of("0", TestService.class);
	ManagedServiceSetting set1 = ManagedServiceSetting.of("1", TestService.class);
	ManagedServiceSetting set2 = ManagedServiceSetting.of("2", TestService.class);
	ManagedServiceSetting set3 = ManagedServiceSetting.of("3", TestService.class);
	ManagedServiceSetting set4 = ManagedServiceSetting.of("4", TestService.class);
	ManagedServiceSetting set5 = ManagedServiceSetting.of("5", TestService.class);
	ManagedServiceSetting set6 = ManagedServiceSetting.of("6", TestService.class);
	ManagedServiceSetting set7 = ManagedServiceSetting.of("7", TestService.class);
	ManagedServiceSetting set8 = ManagedServiceSetting.of("8", TestService.class);
	ManagedServiceSetting set9 = ManagedServiceSetting.of("9", TestService.class);

	ManagedService service = set0.createService();

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

	TimerTask setTask = new TimerTask() {

	    @Override
	    public void run() {

		MultiServiceManager.get().setSettings(List.of(set0, set1));
	    }
	};

	timer1.schedule(setTask, TimeUnit.SECONDS.toMillis(5));

	//
	//
	//

	//	Timer timer2 = new Timer();
	//
	//	TimerTask setTask2 = new TimerTask() {
	//
	//	    @Override
	//	    public void run() {
	//
	//		manager.setDefinitions(List.of(set0));
	//	    }
	//	};
	//
	//	timer2.schedule(setTask2, TimeUnit.SECONDS.toMillis(45));

	//	Timer timer3 = new Timer();
	//
	//	TimerTask setTask3 = new TimerTask() {
	//
	//	    @Override
	//	    public void run() {
	//
	//		manager.setDefinitions(List.of(set0, set1, set2, set3, set4, set5, set6, set7, set8, set9));
	//	    }
	//	};
	//
	//	timer3.schedule(setTask3, TimeUnit.SECONDS.toMillis(55));

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
