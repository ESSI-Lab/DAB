package eu.essi_lab.stress.plan;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mattia Santoro
 */
public class StressPlanResultCollector {

    private List<StressTestResult> results = new ArrayList<>();

    private String host;
    private StressPlan plan;

    public void addResult(StressTestResult result) {
	getResults().add((StressTestResult) result);

    }

    public List<StressTestResult> getResults() {
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

	List<String> rsponseMetrics = getResults().get(0).getResponseMetrics();

	totalByType.keySet().stream().forEach(c -> {
	    columns.add("total_" + c);
	    columns.add("success_" + c);
	    columns.add("fail_" + c);
	    columns.add("mean_exec_" + c);

	    rsponseMetrics.stream().forEach(m -> columns.add(m + "_" + c));
	});

	return columns;
    }

    public Map<String, StressTestCSVValue> getCSVColumnValues() {

	Map<String, StressTestCSVValue> values = new HashMap<>();

	values.put("host", new StressTestCSVValue("host", host));
	values.put("total_req", new StressTestCSVValue("total_req", getResults().size() + ""));
	values.put("total_succ", new StressTestCSVValue("total_succ", totalOkTests() + ""));
	values.put("total_fail", new StressTestCSVValue("total_fail", getResults().size() - totalOkTests() + ""));
	values.put("total_mean_exec_time", new StressTestCSVValue("total_mean_exec_time", meanExecutionTime() + ""));
	values.put("max_parallel", new StressTestCSVValue("max_parallel", getPlan().getParallelRequests() + ""));

	Map<String, Integer> totalByType = totalByType();
	Map<String, Integer> successByType = successByType();
	Map<String, Long> meanExecByType = meanExecByType();

	Map<String, Map<String, Long>> metricsByType = metricsByType();

	totalByType.keySet().stream().forEach(c -> {

	    values.put("total_" + c, new StressTestCSVValue("total_" + c, totalByType.get(c) + ""));
	    values.put("success_" + c, new StressTestCSVValue("success_" + c, successByType.get(c) + ""));
	    values.put("fail_" + c, new StressTestCSVValue("fail_" + c, totalByType.get(c) - successByType.get(c) + ""));
	    values.put("mean_exec_" + c, new StressTestCSVValue("mean_exec_" + c, meanExecByType.get(c) + ""));

	    metricsByType.get(c).keySet().stream().forEach(m ->
		    values.put(m + "_" + c, new StressTestCSVValue(m + "_" + c, metricsByType.get(c).get(m) + ""))
	    );

	});

	return values;
    }

    public void saveReportToCSV(OutputStream out) throws IOException {
	List<String> columns = getCSVColumns();
	Map<String, StressTestCSVValue> values = getCSVColumnValues();

	OutputStreamWriter writer = new OutputStreamWriter(out);
	columns.stream().forEach(c -> {
	    try {
		writer.write(c);
		writer.write(",");
	    } catch (IOException e) {
		throw new RuntimeException(e);
	    }
	});

	writer.write(System.lineSeparator());

	columns.stream().forEach(c -> {
	    try {
		writer.write(values.get(c).getValue());
		writer.write(",");
	    } catch (IOException e) {
		throw new RuntimeException(e);
	    }
	});

	writer.flush();
    }

    public void printReport(OutputStream out) {

	String title = String.format("Results of Discovery Stress Tests on host %s", host);

	String summary = String.format("Number of tests: %d%sSuccess: %d%sMean Execution Time: %d milliseconds", getResults().size(),
		System.lineSeparator(),
		totalOkTests(), System.lineSeparator(),
		meanExecutionTime());

	String planSummary = createPlanSummary();

	OutputStreamWriter writer = new OutputStreamWriter(out);

	try {
	    writer.write(System.lineSeparator() + System.lineSeparator());
	    writer.write(title);
	    writer.write(System.lineSeparator() + System.lineSeparator());
	    writer.write(summary);
	    writer.write(System.lineSeparator() + System.lineSeparator());
	    writer.write(planSummary);
	    writer.write(System.lineSeparator() + System.lineSeparator());

	    for (StressTestResult result : getResults()) {
		writer.write(result.getRequest());
		writer.write(System.lineSeparator());
		writer.write(result.getResponseFile());
		writer.write(System.lineSeparator());
	    }
	    writer.flush();
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
    }

    private Map<String, Integer> totalByType() {
	Map<String, Integer> map = new HashMap<>();

	getResults().stream().map(r ->
		r.getTest()
	).forEach(test -> {
	    Integer total = 0;

	    String testcontraints = test.createTestKey();

	    if (map.get(testcontraints) != null)
		total = map.get(testcontraints);

	    map.put(testcontraints, total + 1);

	});

	return map;
    }

    private Map<String, Map<String, Long>> metricsByType() {
	Map<String, Map<String, Long>> map = new HashMap<>();

	getResults().stream().forEach(
		r -> {
		    IStressTest test = r.getTest();
		    String testcontraints = test.createTestKey();
		    Map<String, Long> testMetricsMap = new HashMap<>();

		    r.getResponseMetrics().forEach(m -> testMetricsMap.put(m, 0L));

		    map.put(testcontraints, testMetricsMap);
		}
	);

	getResults().stream().filter(r -> r.getCode() == 200).forEach(
		r -> {
		    IStressTest test = r.getTest();
		    String testcontraints = test.createTestKey();
		    Map<String, Long> testMetricsMap = map.get(testcontraints);
		    List<String> metrics = r.getResponseMetrics();

		    metrics.stream().forEach(m -> {
			Long metricValue = r.readMetric(m, test);

			Long total = 0L;

			if (testMetricsMap.get(m) != null)
			    total = testMetricsMap.get(m);

			testMetricsMap.put(m, total + metricValue);

		    });

		}
	);

	Map<String, Integer> successByType = successByType();

	map.keySet().stream().forEach(key -> {

	    map.get(key).keySet().stream().forEach(metric -> {
		Long n = map.get(key).get(metric);
		Integer d = successByType.get(key);

		if (d > 0) {
		    Long mean = n / d;

		    logger.debug("For test key {} total of metric {} is {}, calculated mean is {}", key, metric, n, mean);

		    map.get(key).put(metric, mean);
		}
	    });

	});

	return map;
    }

    private GSLoggerFactory.GSLogger logger = GSLoggerFactory.getLogger(getClass());

    private Map<String, Long> meanExecByType() {
	Map<String, Long> map = new HashMap<>();
	getResults().stream().forEach(r -> map.put(r.getTest().createTestKey(), 0L));

	getResults().stream().filter(r -> r.getCode() == 200).
		forEach(r -> {
		    IStressTest test = r.getTest();

		    Long total = 0L;

		    String testcontraints = test.createTestKey();

		    if (map.get(testcontraints) != null)
			total = map.get(testcontraints);

		    map.put(testcontraints, total + r.getExecTime());

		});

	Map<String, Integer> successByType = successByType();

	map.keySet().stream().forEach(key -> {
	    Long n = map.get(key);
	    Integer d = successByType.get(key);

	    if (d > 0) {
		Long mean = n / d;

		map.put(key, mean);
	    }

	});

	return map;
    }

    private Map<String, Integer> successByType() {
	Map<String, Integer> map = new HashMap<>();
	getResults().stream().forEach(r -> map.put(r.getTest().createTestKey(), 0));

	getResults().stream().filter(r -> r.getCode() == 200).
		forEach(r -> {
		    IStressTest test = r.getTest();
		    Integer total = 0;

		    String testcontraints = test.createTestKey();

		    if (map.get(testcontraints) != null)
			total = map.get(testcontraints);

		    map.put(testcontraints, total + 1);

		});

	return map;
    }

    private String createPlanSummary() {

	String planSummary = String.format("Total Number of Requests: %d%sParallel Requests: %d",
		getPlan().getStressTests().size() * getPlan().getMultiplicationFactor(), System.lineSeparator(),
		getPlan().getParallelRequests());

	List<String> testLines = new ArrayList<>();
	StringBuilder builder = new StringBuilder(planSummary);
	builder.append(System.lineSeparator());

	Map<String, Integer> map = totalByType();

	map.keySet().stream().forEach(key -> {

	    Integer total = map.get(key);

	    String testline = String.format("Number of requests by type [%s] = %d", key, total);

	    testLines.add(testline);
	    builder.append(testline);
	    builder.append(System.lineSeparator());

	});

	return builder.toString();
    }

    private Long meanExecutionTime() {

	Long total = 0L;

	for (StressTestResult result : getResults()) {
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

    public void setPlan(StressPlan plan) {
	this.plan = plan;
    }

    public StressPlan getPlan() {
	return plan;
    }
}
