package eu.essi_lab.stress.plan;

import eu.essi_lab.stress.discovery.DiscoveryStressTestResult;

/**
 * @author Mattia Santoro
 */
public interface IStressPlanResultCollector {
    void setHost(String host);

    void setPlan(StressPlan plan);

    void addResult(IStressTestResult discoveryStressTestResult);
}
