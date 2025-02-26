package eu.essi_lab.stress.discovery;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Mattia Santoro
 */
public class DiscoveryStressExternalTestIT {

    public static void main(String[] args) {

	DiscoveryStressTest t1 = new DiscoveryStressTest();
	t1.setSearchText("ozone");

	DiscoveryStressPlan plan = new DiscoveryStressPlan();
	plan.addStressTest(t1);


	String hostname = "https://gs-service-test.geodab.eu";

	DiscoveryStressPlanExecutor planExecutor = new DiscoveryStressPlanExecutor(plan, hostname);

	DiscoveryStressPlanResultCollector collector = new DiscoveryStressPlanResultCollector();

	try {
	    planExecutor.execute(collector);
	} catch (InterruptedException e) {
	    throw new RuntimeException(e);
	}

	collector.printReport();


    }

}
