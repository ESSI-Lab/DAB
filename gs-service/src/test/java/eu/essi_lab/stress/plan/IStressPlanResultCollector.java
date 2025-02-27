package eu.essi_lab.stress.plan;

import java.io.OutputStream;
import java.util.Collection;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public interface IStressPlanResultCollector {
    void setHost(String host);

    void setPlan(StressPlan plan);

    void addResult(StressTestResult discoveryStressTestResult);

    void printReport(OutputStream outfile);

    List<String> getCSVColumns();

    List<String> getCSVColumnValues();
}
