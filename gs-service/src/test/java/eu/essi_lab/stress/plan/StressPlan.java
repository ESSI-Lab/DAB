package eu.essi_lab.stress.plan;

import eu.essi_lab.stress.discovery.DiscoveryStressTest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public class StressPlan {

    private int parallelRequests = 1;
    private int multiplicationFactor = 4;

    private List<IStressTest> stressTests = new ArrayList<>();

    public int getParallelRequests() {
	return parallelRequests;
    }

    public void setParallelRequests(int parallelRequests) {
	this.parallelRequests = parallelRequests;
    }

    public void addStressTest(DiscoveryStressTest test) {
	getStressTests().add(test);
    }

    public List<IStressTest> getStressTests() {
	return stressTests;
    }

    public int getMultiplicationFactor() {
	return multiplicationFactor;
    }

    public void setMultiplicationFactor(int multiplicationFactor) {
	this.multiplicationFactor = multiplicationFactor;
    }
}
