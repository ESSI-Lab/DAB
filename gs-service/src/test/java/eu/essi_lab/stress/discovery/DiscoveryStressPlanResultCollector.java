package eu.essi_lab.stress.discovery;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public class DiscoveryStressPlanResultCollector {

    private List<DiscoveryStressTestResult> results = new ArrayList<>();

    private String host;

    public void addResult(DiscoveryStressTestResult result) {
	getResults().add(result);

    }

    public List<DiscoveryStressTestResult> getResults() {
	return results;
    }

    public Integer totalOkTests() {
	return Math.toIntExact(getResults().stream().filter(r -> r.getCode() == 200).count());
    }

    public void printReport(OutputStream out) {
	String title = String.format("\n\nResults of Discovery Stress Tests on host %s\n\n", host);
	String summary = String.format("Number of tests: %d\nSuccess: %d\nMean Execution Time: %d milliseconds", getResults().size(),
		totalOkTests(),
		meanExecutionTime());

	meanExecutionTime();

	OutputStreamWriter writer = new OutputStreamWriter(out);

	try {
	    writer.write(title);
	    writer.write(summary);
	    writer.write("\n\n");
	    writer.flush();
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}

    }

    private Long meanExecutionTime() {

	Long total = 0L;

	for (DiscoveryStressTestResult result : getResults()) {
	    total += result.getExecTime();
	}

	return total / totalOkTests();
    }

    public String getHost() {
	return host;
    }

    public void setHost(String host) {
	this.host = host;
    }
}
