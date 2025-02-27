package eu.essi_lab.stress.plan;

/**
 * @author Mattia Santoro
 */
public interface IStressPlanResultCollector {
    void setHost(String host);

    void setPlan(StressPlan plan);

    void addResult(StressTestResult discoveryStressTestResult);
}
