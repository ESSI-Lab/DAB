package eu.essi_lab.stress.discovery;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public class DiscoveryStressPlanResultCollector {

    private List<DiscoveryStressTestResult> results = new ArrayList<>();

    public void addResult(DiscoveryStressTestResult result) {
	getResults().add(result);

    }

    public List<DiscoveryStressTestResult> getResults() {
	return results;
    }

    public void printReport() {
	String summary = String.format("Number of tests: %d\nSuccess: %d",getResults().size(), getResults().stream().filter(r->r.getCode()==200).count());

	System.out.println(summary);

    }
}
