package eu.essi_lab.stress;

import static org.junit.Assert.assertEquals;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import eu.essi_lab.lib.utils.GSLoggerFactory;

public abstract class StressExternalTestIT {

    @Test
    public void testStress() throws Exception {
	String start = "Start test at " + new Date();
	Integer iterations = getIterationsPerTest();

	int failures = 0;

	List<String> infos = new ArrayList<>();

	int tot = 0;

	int tests = getNumofTests();



	for (int t = 0; t < tests; t++) {
	    for (int it = 0; it < iterations; it++) {
		int threads = getNumberOfRequestForTest(t);

		long timeout = 10;
		TimeUnit timeoutUnits = TimeUnit.MINUTES;

		int parallel = getParallel();
		ExecutorService executor = Executors.newFixedThreadPool(parallel);

		List<Callable<SimpleEntry<Long, Boolean>>> tasks = new ArrayList<Callable<SimpleEntry<Long, Boolean>>>();
		List<Future<SimpleEntry<Long, Boolean>>> results = new ArrayList<Future<SimpleEntry<Long, Boolean>>>();

		for (int i = 0; i < threads; i++) {
		    Callable<SimpleEntry<Long, Boolean>> task = new Callable<SimpleEntry<Long, Boolean>>() {

			@Override
			public SimpleEntry<Long, Boolean> call() throws Exception {
			    long start = System.currentTimeMillis();
			    boolean result = false;
			    try {
				result = testOperation();
			    } catch (Exception e) {
				GSLoggerFactory.getLogger(getClass()).info("Unexpected exception: " + e.getMessage());
			    }
			    long end = System.currentTimeMillis();
			    long gap = end - start;
			    SimpleEntry<Long, Boolean> ret = new SimpleEntry<Long, Boolean>(gap, result);
			    return ret;
			}
		    };
		    tasks.add(task);
		}
		results = executor.invokeAll(tasks, timeout, timeoutUnits);

		Double mean = 0.;
		for (Future<SimpleEntry<Long, Boolean>> future : results) {
		    Long time = future.get().getKey();
		    String info = "Execution time: " + (time / 1000) + "s" + " (" + time + "ms)";
		    GSLoggerFactory.getLogger(getClass()).info(info);
		    mean += ((double) time / (double) threads);
		}

		String info = "Mean execution time for iteration " + it + " (" + threads + " jobs): " + (int) (mean / 1000) + "s (" + mean
			+ "ms)";

		infos.add(info);
		GSLoggerFactory.getLogger(getClass()).info(info);

		for (Future<SimpleEntry<Long, Boolean>> future : results) {
		    Boolean result = future.get().getValue();
		    if (!result) {
			failures++;
		    }
		}

		tot += threads;

		Thread.sleep(1000L);
	    }

	}

	GSLoggerFactory.getLogger(getClass()).info("** Test Summary " + start);

	infos.forEach(i -> {
	    GSLoggerFactory.getLogger(getClass()).info(i);
	});

	String info = "Success rate: " + (((tot - failures) / (double) tot) * 100) + "%";
	GSLoggerFactory.getLogger(getClass()).info(info);
	assertEquals(0, failures);

    }

    protected abstract int getParallel();

    protected abstract int getNumofTests();

    protected abstract Integer getNumberOfRequestForTest(int it);

    protected abstract Integer getIterationsPerTest();

    public abstract boolean testOperation() throws Exception;

    public static void main(String[] args) {

    }

}
