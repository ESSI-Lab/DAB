package eu.essi_lab.stress.discovery;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mattia Santoro
 */
public class DiscoveryStressPlanExecutor {

    private final DiscoveryStressPlan plan;
    private final ExecutorService executor;
    private final String host;

    public DiscoveryStressPlanExecutor(DiscoveryStressPlan plan, String host) {
	this.plan = plan;
	this.host = host;

	executor = Executors.newFixedThreadPool(plan.getParallelRequests());
    }

    public void execute(DiscoveryStressPlanResultCollector resultCollector) throws InterruptedException {

	List<DiscoveryStressTestExecutor> tasks = this.plan.getStressTests().stream().map(
		test -> new DiscoveryStressTestExecutor(test, host)).collect(Collectors.toList());

	List<Future<DiscoveryStressTestResult>> futures = executor.invokeAll(tasks);

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
	executor.awaitTermination(10L,TimeUnit.MINUTES);


    }

}
