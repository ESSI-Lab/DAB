package eu.essi_lab.pdk;

import java.net.URI;
import java.net.URISyntaxException;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.lib.utils.TaskListExecutor;

public class RequestBouncerInternalTestIT {

    private class Metrics {

	private int counter = 0;

	List<Integer> last = new LinkedList<Integer>();

	int capacity = 10;

	Timer timer = new Timer();

	private TimerTask task;

	public void add() {
	    synchronized (last) {
		counter++;
	    }
	}

	public void remove() {
	    synchronized (last) {
		counter--;
	    }
	}

	public Metrics() {
	    this.task = new TimerTask() {

		@Override
		public void run() {

		    synchronized (last) {
			last.add(counter);
			if (last.size() > capacity) {
			    last.remove(0);
			}
			int t = 0;
			for (Integer c : last) {
			    t += c;
			}
			double avg = (double) t / (double) capacity;
			System.out.println("Current parallel tasks: " + counter + " AVG: " + avg);
		    }

		}
	    };

	}

	public void start() {
	    timer.scheduleAtFixedRate(task, 0, 100);
	}

	public void finish() {
	    timer.cancel();

	}

    }

    private Metrics metrics;

    @Before
    public void before() {
	this.metrics = new Metrics();

	metrics.start();

    }

    @After
    public void after() {
	metrics.finish();

    }

    @Test
    public void testFromSingleIP() throws Exception {

	int maxRequests = 2;
	//	 AbstractRequestBouncer bouncer = getLocalRequestBouncer(maxRequests,maxRequests);
	AbstractRequestBouncer bouncer = getDistributedRequestBouncer(maxRequests, maxRequests, 100);

	int requests = 2;
	int executionTimeMs = 1000;
	String ip = "ip1";
	long start = System.currentTimeMillis();

	int executed = testFromSingleIP(bouncer, ip, requests, 100, executionTimeMs);

	long end = System.currentTimeMillis();

	long gap = end - start;

	long expectedGap = (requests * executionTimeMs) / maxRequests;
	expectedGap = (long) (expectedGap + expectedGap * 0.3); // added some time to manage threads
	System.out.println("Requests: " + requests);
	System.out.println("Execution time for each request: " + executionTimeMs);
	System.out.println("Concurrent workers: " + bouncer.getMaximumConcurrentRequests());
	System.out.println("Concurrent workers per IP: " + bouncer.getMaximumConcurrentRequestsPerIP());
	System.out.println("Expected gap: " + expectedGap);
	System.out.println("Actual gap: " + gap);
	assertTrue(executed == requests);
	assertTrue(gap < expectedGap);

    }

    private DistributedRequestBouncer getDistributedRequestBouncer(int maxRequests, int maxRequestsPerIp, int maxOverallRequestsPerIp)
	    throws URISyntaxException {

	String redishostProp = System.getProperty("redis.host");
	//	String redishostProp = "http://localhost:6379";

	URI redisuri = new URI(redishostProp);

	//TODO fix the hash hardcoded value to use the actual default (which is now private in DistributedRequestBouncer class)
	DistributedRequestBouncer ret = new DistributedRequestBouncer(redisuri.getHost(), redisuri.getPort(), "default", maxRequests,
		maxRequestsPerIp, maxOverallRequestsPerIp);

	ret.setHash("java-test");
	ret.clearDB();
	ret.setFacilitateOtherIps(true);
	ret.setPollTimeMs(5);
	return ret;
    }

    private AbstractRequestBouncer getLocalRequestBouncer(int maxRequests, int maxRequests2, int maxOverallRequestsPerIp) {
	LocalRequestBouncer bouncer = new LocalRequestBouncer(maxRequests, maxRequests, maxOverallRequestsPerIp);
	bouncer.setWaitTime(1);
	return bouncer;
    }

    @Test
    public void testFromTwoIPs() throws Exception {
	int maxRequests = 2;
	AbstractRequestBouncer bouncer = getDistributedRequestBouncer(maxRequests, 1, 100);
	// bouncer.setWaitTime(1);

	int requests = 20;
	int executionTimeMs = 100;

	TaskListExecutor<Boolean> tle = new TaskListExecutor<>(1000);
	tle.addTask(new Callable<Boolean>() {

	    @Override
	    public Boolean call() throws Exception {
		String ip = "ip1";
		long start = System.currentTimeMillis();

		int executed = testFromSingleIP(bouncer, ip, requests, 100, executionTimeMs);
		System.out.println("executed: " + executed);
		long end = System.currentTimeMillis();

		long gap = end - start;

		long expectedGap = (requests * executionTimeMs);
		expectedGap = (long) (expectedGap + expectedGap * 0.3); // added some time to manage threads
		System.out.println("Requests: " + requests);
		System.out.println("Execution time for each request: " + executionTimeMs);
		System.out.println("Concurrent workers: " + bouncer.getMaximumConcurrentRequests());
		System.out.println("Concurrent workers per IP: " + bouncer.getMaximumConcurrentRequestsPerIP());
		System.out.println("Expected gap: " + expectedGap);
		System.out.println("Actual gap: " + gap);
		return gap < expectedGap;
	    }
	});
	tle.addTask(new Callable<Boolean>() {

	    @Override
	    public Boolean call() throws Exception {
		String ip = "ip2";
		long start = System.currentTimeMillis();

		int executed = testFromSingleIP(bouncer, ip, requests, 100, executionTimeMs);
		System.out.println("executed: " + executed);
		long end = System.currentTimeMillis();

		long gap = end - start;

		long expectedGap = (requests * executionTimeMs);
		expectedGap = (long) (expectedGap + expectedGap * 0.3); // added some time to manage threads
		System.out.println("Requests: " + requests);
		System.out.println("Execution time for each request: " + executionTimeMs);
		System.out.println("Concurrent workers: " + bouncer.getMaximumConcurrentRequests());
		System.out.println("Concurrent workers per IP: " + bouncer.getMaximumConcurrentRequestsPerIP());
		System.out.println("Expected gap: " + expectedGap);
		System.out.println("Actual gap: " + gap);

		return gap < expectedGap;
	    }
	});

	List<Future<Boolean>> futures = tle.executeAndWait();
	for (Future<Boolean> future : futures) {
	    try {
		assertTrue(future.get());
	    } catch (Exception e) {
		// TODO: handle exception
		e.printStackTrace();
	    }
	}

    }

    @Test
    public void testWithOldRequests() throws Exception {
	int maxRequests = 1;
	DistributedRequestBouncer bouncer = getDistributedRequestBouncer(maxRequests, maxRequests, 100);
	bouncer.setOldRequestsMs(10000);

	TaskListExecutor<String> tle = new TaskListExecutor<>(1000);

	tle.addTask(new Callable<String>() {
	    @Override
	    public String call() throws Exception {
		bouncer.askForExecutionAndWait("ip", "id1", 10, TimeUnit.SECONDS);
		metrics.add();
		System.out.println("STARTED id1: ");
		try {
		    Thread.sleep(10000);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		metrics.remove();
		bouncer.notifyExecutionEnded("ip", "id1");
		return null;
	    }
	});
	tle.addTask(new Callable<String>() {
	    @Override
	    public String call() throws Exception {
		try {
		    Thread.sleep(1000);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		bouncer.askForExecutionAndWait("ip", "id2", 10, TimeUnit.SECONDS);
		metrics.add();
		System.out.println("STARTED id2: ");
		try {
		    Thread.sleep(1000);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		metrics.remove();
		bouncer.notifyExecutionEnded("ip", "id2");
		return null;
	    }
	});

	List<Future<String>> futures = tle.executeAndWait();

	int executed = 0;
	for (Future<String> future : futures) {
	    future.get();
	    System.out.println(executed);
	    executed++;
	}

    }

    @Test
    public void testFromTwoIPsSuffocating() throws Exception {
	int maxRequests = 2;
	DistributedRequestBouncer bouncer = getDistributedRequestBouncer(maxRequests, maxRequests, 100);

	int requests1 = 5;
	int requests2 = 20;
	int executionTimeMs = 100;

	TaskListExecutor<Boolean> tle = new TaskListExecutor<>(1000);
	tle.addTask(new Callable<Boolean>() {

	    @Override
	    public Boolean call() throws Exception {
		String ip = "ip1";
		long start = System.currentTimeMillis();

		int executed = testFromSingleIP(bouncer, ip, requests1, 100, executionTimeMs);

		long end = System.currentTimeMillis();

		long gap = end - start;

		long expectedGap = requests1 * executionTimeMs;
		expectedGap = (long) (expectedGap + expectedGap * 0.4); // added some time to manage threads
		System.out.println("Requests: " + (requests1));
		System.out.println("Execution time for each request: " + executionTimeMs);
		System.out.println("Concurrent workers: " + bouncer.getMaximumConcurrentRequests());
		System.out.println("Concurrent workers per IP: " + bouncer.getMaximumConcurrentRequestsPerIP());
		System.out.println("Expected gap: " + expectedGap);
		System.out.println("Actual gap: " + gap);
		return gap < expectedGap;
	    }
	});
	tle.addTask(new Callable<Boolean>() {

	    @Override
	    public Boolean call() throws Exception {

		String ip = "ip2";
		long start = System.currentTimeMillis();

		int executed = testFromSingleIP(bouncer, ip, requests2, 100, executionTimeMs);

		long end = System.currentTimeMillis();

		long gap = end - start;

		long expectedGap = (requests2 * executionTimeMs);
		expectedGap = (long) (expectedGap + expectedGap * 0.4); // added some time to manage threads
		System.out.println("Requests: " + requests2);
		System.out.println("Execution time for each request: " + executionTimeMs);
		System.out.println("Concurrent workers: " + bouncer.getMaximumConcurrentRequests());
		System.out.println("Concurrent workers per IP: " + bouncer.getMaximumConcurrentRequestsPerIP());
		System.out.println("Expected gap: " + expectedGap);
		System.out.println("Actual gap: " + gap);

		return gap < expectedGap;
	    }
	});

	List<Future<Boolean>> futures = tle.executeAndWait();
	for (Future<Boolean> future : futures) {
	    assertTrue(future.get());
	}

    }

    public int testFromSingleIP(AbstractRequestBouncer bouncer, String ip, int requests, int waitTimeSec, int executionTimeMs)
	    throws Exception {

	TaskListExecutor<String> tle = new TaskListExecutor<>(1000);

	for (int i = 0; i < requests; i++) {
	    final String id = ip + ":" + i;
	    tle.addTask(new Callable<String>() {

		@Override
		public String call() throws Exception {
		    // System.out.println("QUEUED Id: " + id);
		    bouncer.askForExecutionAndWait(ip, id, waitTimeSec, TimeUnit.SECONDS);
		    long start = System.currentTimeMillis();
		    metrics.add();
		    System.out.println("STARTED Id: " + id);
		    try {
			Thread.sleep(executionTimeMs);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		    metrics.remove();
		    bouncer.notifyExecutionEnded(ip, id);
		    // System.out.println("ENDED Id: " + id);
		    long end = System.currentTimeMillis();
		    return ip + ": " + (end - start);
		}
	    });
	}

	List<Future<String>> futures = tle.executeAndWait();

	int executed = 0;
	for (Future<String> future : futures) {
	    future.get();
	    System.out.println(executed);
	    executed++;
	}

	return executed;

    }

}
