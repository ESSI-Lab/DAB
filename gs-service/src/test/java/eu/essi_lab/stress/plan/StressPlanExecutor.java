package eu.essi_lab.stress.plan;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Mattia Santoro
 */
public class StressPlanExecutor {

    private final StressPlan plan;
    private final ExecutorService executor;
    private final String host;

    public StressPlanExecutor(StressPlan plan, String host) {
	this.plan = plan;
	this.host = host;

	executor = Executors.newFixedThreadPool(plan.getParallelRequests());
    }

    public void execute(IStressPlanResultCollector resultCollector) throws InterruptedException {

	resultCollector.setHost(host);
	resultCollector.setPlan(plan);

	List<StressTestExecutor> tasks = new ArrayList<>();

	for (int i = 0; i < plan.getMultiplicationFactor(); i++) {

	    this.plan.getStressTests().stream().map(
		    test -> new StressTestExecutor(test, host)).collect(Collectors.toCollection(() -> tasks));
	}

	Collections.shuffle(tasks);

	List<Future<IStressTestResult>> futures = executor.invokeAll(tasks);

	GSLoggerFactory.getLogger(getClass()).info("start collect");
	futures.stream().forEach(result -> {
	    try {

		resultCollector.addResult(result.get());
	    } catch (InterruptedException e) {
		throw new RuntimeException(e);
	    } catch (ExecutionException e) {
		throw new RuntimeException(e);
	    }
	});

	GSLoggerFactory.getLogger(getClass()).info("collect ENDED");
	executor.shutdown();
	executor.awaitTermination(10L, TimeUnit.MINUTES);

    }

}
