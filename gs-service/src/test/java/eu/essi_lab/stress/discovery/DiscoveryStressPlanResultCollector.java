package eu.essi_lab.stress.discovery;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mattia Santoro
 */
public class DiscoveryStressPlanResultCollector {

    private List<DiscoveryStressTestResult> results = new ArrayList<>();

    private String host;
    private DiscoveryStressPlan plan;

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

	String title = String.format("Results of Discovery Stress Tests on host %s", host);

	String summary = String.format("Number of tests: %d\nSuccess: %d\nMean Execution Time: %d milliseconds", getResults().size(),
		totalOkTests(),
		meanExecutionTime());

	String planSummary = createPlanSummary();

	OutputStreamWriter writer = new OutputStreamWriter(out);

	try {
	    writer.write("\n\n");
	    writer.write(title);
	    writer.write("\n\n");
	    writer.write(summary);
	    writer.write("\n\n");
	    writer.write(planSummary);
	    writer.write("\n\n");

	    for (DiscoveryStressTestResult result : getResults()) {
		writer.write(result.getRequest());
		writer.write("\n");
		writer.write(result.getResponseFile());
		writer.write("\n");
	    }
	    writer.flush();
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
    }

    private String createPlanSummary() {

	String planSummary = String.format("Total Number of Requests: %d\nParallel Requests: %d",
		getPlan().getStressTests().size() * getPlan().getMultiplicationFactor(), getPlan().getParallelRequests());

	Map<String, Integer> map = new HashMap<>();

	getPlan().getStressTests().stream().forEach(test -> {

	    StringBuilder contraintsBuilder = new StringBuilder();

	    if (test.getSearchText() != null)
		contraintsBuilder.append("searchtext").append(",");

	    if (test.getBbox() != null)
		contraintsBuilder.append("bbox,").append(test.getBboxrel()).append(",");

	    contraintsBuilder.append("n_sources=").append(test.getSources().size());

	    Integer total = 0;

	    String testcontraints = contraintsBuilder.toString();

	    if (map.get(testcontraints) != null)
		total = map.get(testcontraints);

	    map.put(testcontraints, total + 1);

	});

	List<String> testLines = new ArrayList<>();
	StringBuilder builder = new StringBuilder(planSummary);
	builder.append("\n");

	map.keySet().stream().forEach(key -> {

	    Integer total = map.get(key) * plan.getMultiplicationFactor();

	    String testline = String.format("Number of requests by type [%s] = %d", key, total);

	    testLines.add(testline);
	    builder.append(testline);
	    builder.append("\n");

	});

	return builder.toString();
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

    public void setPlan(DiscoveryStressPlan plan) {
	this.plan = plan;
    }

    public DiscoveryStressPlan getPlan() {
	return plan;
    }
}
