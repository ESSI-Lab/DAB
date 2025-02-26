package eu.essi_lab.stress.discovery;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import org.apache.jena.base.Sys;

/**
 * @author Mattia Santoro
 */
public class DiscoveryStressExternalTestIT {

    public static void main(String[] args) {

	DiscoveryStressTest t1 = new DiscoveryStressTest();
	t1.setSearchText("ozone");

	DiscoveryStressTest t2 = new DiscoveryStressTest();
	t2.setSearchText("temperature");

	DiscoveryStressPlan plan = new DiscoveryStressPlan();
	plan.addStressTest(t1);
	plan.addStressTest(t2);
	plan.setParallelRequests(3);


	String hostname = "https://gs-service-test.geodab.eu";

	DiscoveryStressPlanExecutor planExecutor = new DiscoveryStressPlanExecutor(plan, hostname);

	DiscoveryStressPlanResultCollector collector = new DiscoveryStressPlanResultCollector();

	try {
	    planExecutor.execute(collector);
	} catch (InterruptedException e) {
	    throw new RuntimeException(e);
	}

	collector.printReport(System.out);


    }

}
