package eu.essi_lab.stress.discovery;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

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

    public List<String> getCSVColumns() {
	List<String> columns = new ArrayList<>();

	columns.addAll(Arrays.asList("host", "total_req", "total_succ", "total_fail", "total_mean_exec_time",
		"max_parallel"));

	Map<String, Integer> totalByType = totalByType();
	Map<String, Integer> successByType = successByType();
	Map<String, Long> meanExecByType = meanExecByType();

	totalByType.keySet().stream().forEach(c -> {
	    columns.add("total_" + c);
	    columns.add("success_" + c);
	    columns.add("fail_" + c);
	    columns.add("mean_exec_" + c);
	});

	return columns;
    }

    public List<String> getCSVColumnValues() {

	List<String> values = new ArrayList<>();

	values.add(host);
	values.add(getResults().size() + "");
	values.add(totalOkTests() + "");
	values.add(getResults().size() - totalOkTests() + "");
	values.add(meanExecutionTime() + "");
	values.add(getPlan().getParallelRequests() + "");

	Map<String, Integer> totalByType = totalByType();
	Map<String, Integer> successByType = successByType();
	Map<String, Long> meanExecByType = meanExecByType();

	totalByType.keySet().stream().forEach(c -> {

	    values.add(totalByType.get(c) + "");
	    values.add(successByType.get(c) + "");
	    values.add(totalByType.get(c) - successByType.get(c) + "");
	    values.add(meanExecByType.get(c) + "");

	});

	return values;
    }

    public void saveReportToCSV(OutputStream out) throws IOException {
	List<String> columns = getCSVColumns();
	List<String> values = getCSVColumnValues();

	OutputStreamWriter writer = new OutputStreamWriter(out);
	columns.stream().forEach(c -> {
	    try {
		writer.write(c);
		writer.write(",");
	    } catch (IOException e) {
		throw new RuntimeException(e);
	    }
	});

	writer.write("\n");

	values.stream().forEach(c -> {
	    try {
		writer.write(c);
		writer.write(",");
	    } catch (IOException e) {
		throw new RuntimeException(e);
	    }
	});

	writer.flush();
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

    private String createContraint(DiscoveryStressTest test) {
	StringBuilder contraintsBuilder = new StringBuilder();

	if (test.getSearchText() != null)
	    contraintsBuilder.append("searchtext").append("__");

	if (test.getBbox() != null)
	    contraintsBuilder.append("bbox__").append(test.getBboxrel()).append("__");

	contraintsBuilder.append("n_sources=").append(test.getSources().size());

	String testcontraints = contraintsBuilder.toString();

	return testcontraints;
    }

    private Map<String, Integer> totalByType() {
	Map<String, Integer> map = new HashMap<>();

	getResults().stream().map(r ->
		r.getTest()
	).forEach(test -> {
	    Integer total = 0;

	    String testcontraints = createContraint(test);

	    if (map.get(testcontraints) != null)
		total = map.get(testcontraints);

	    map.put(testcontraints, total + 1);

	});

	return map;
    }

    private Map<String, Long> meanExecByType() {
	Map<String, Long> map = new HashMap<>();

	getResults().stream().filter(r -> r.getCode() == 200).
		forEach(r -> {
		    DiscoveryStressTest test = r.getTest();

		    Long total = 0L;

		    String testcontraints = createContraint(test);

		    if (map.get(testcontraints) != null)
			total = map.get(testcontraints);

		    map.put(testcontraints, total + r.getExecTime());

		});

	Map<String, Integer> successByType = successByType();

	map.keySet().stream().forEach(key -> {

	    map.put(key, map.get(key) / successByType.get(key));

	});

	return map;
    }

    private Map<String, Integer> successByType() {
	Map<String, Integer> map = new HashMap<>();

	getResults().stream().filter(r -> r.getCode() == 200).
		map(r ->
			r.getTest()
		).forEach(test -> {

		    Integer total = 0;

		    String testcontraints = createContraint(test);

		    if (map.get(testcontraints) != null)
			total = map.get(testcontraints);

		    map.put(testcontraints, total + 1);

		});

	return map;
    }

    private String createPlanSummary() {

	String planSummary = String.format("Total Number of Requests: %d\nParallel Requests: %d",
		getPlan().getStressTests().size() * getPlan().getMultiplicationFactor(), getPlan().getParallelRequests());

	List<String> testLines = new ArrayList<>();
	StringBuilder builder = new StringBuilder(planSummary);
	builder.append("\n");

	Map<String, Integer> map = totalByType();

	map.keySet().stream().forEach(key -> {

	    Integer total = map.get(key);

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
