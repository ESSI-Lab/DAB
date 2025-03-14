package eu.essi_lab.stress.plan.discovery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.stress.plan.StressPlan;
import eu.essi_lab.stress.plan.StressPlanExecutor;
import eu.essi_lab.stress.plan.StressPlanResultCollector;
import eu.essi_lab.stress.plan.StressTestCSVValue;

/**
 * @author Mattia Santoro
 */
public class DiscoveryStressExternalTestIT {

    private StressPlan definePlan() {
	DiscoveryStressTest t1 = new DiscoveryStressTest();
	t1.setSearchText("ozone");

	DiscoveryStressTest t2 = new DiscoveryStressTest();
	t2.setSearchText("temperature");
	t2.setBbox("-10.9,35.23,2.2,43.434");

	DiscoveryStressTest t3 = new DiscoveryStressTest();
	t3.setBbox("-10.9,35.23,2.2,43.434");

	DiscoveryStressTest t4 = new DiscoveryStressTest();
	t4.setSearchText("temperature");
	t4.setBbox("-10.9,35.23,2.2,43.434");
	t4.setBboxrel(DiscoveryStressTest.BBOXREL.CONTAINS);

	DiscoveryStressTest t5 = new DiscoveryStressTest();
	t5.setBbox("-10.9,35.23,2.2,43.434");
	t5.setBboxrel(DiscoveryStressTest.BBOXREL.CONTAINS);

	DiscoveryStressTest t5_1 = new DiscoveryStressTest();
	t5_1.setBbox("-160.9,-35.23,2.2,43.434");
	t5_1.setBboxrel(DiscoveryStressTest.BBOXREL.CONTAINS);

	DiscoveryStressTest t5_2 = new DiscoveryStressTest();
	t5_2.setBbox("-160.9,-35.23,2.2,43.434");
	t5_2.setBboxrel(DiscoveryStressTest.BBOXREL.OVERLAPS);

	DiscoveryStressTest t5_3 = new DiscoveryStressTest();
	t5_3.setBbox("0.9,42.23,2.2,43.434");
	t5_3.setBboxrel(DiscoveryStressTest.BBOXREL.CONTAINS);

	DiscoveryStressTest t5_4 = new DiscoveryStressTest();
	t5_4.setBbox("0.9,42.23,2.2,43.434");
	t5_4.setBboxrel(DiscoveryStressTest.BBOXREL.OVERLAPS);

	DiscoveryStressTest t6 = new DiscoveryStressTest();
	t6.setBbox("-10.9,35.23,2.2,43.434");
	t6.setView("geoss");

	DiscoveryStressTest t6_1 = new DiscoveryStressTest();
	t6_1.setSearchText("deforestation");
	t6_1.setView("geoss");

	DiscoveryStressTest t6_2 = new DiscoveryStressTest();
	t6_2.setSearchText("deforestation");
	t6_2.setBbox("-10.9,35.23,2.2,43.434");
	t6_2.setView("geoss");

	StressPlan plan = new StressPlan();
	plan.addStressTest(t1);
	//	plan.addStressTest(t2);
	//	plan.addStressTest(t3);
	//	plan.addStressTest(t4);
	//	plan.addStressTest(t5);
	//	plan.addStressTest(t5_1);
	//	plan.addStressTest(t5_2);
	//	plan.addStressTest(t5_3);
	//	plan.addStressTest(t5_4);
	//	plan.addStressTest(t6);
	//	plan.addStressTest(t6_1);
	//	plan.addStressTest(t6_2);
	plan.setParallelRequests(2);
	plan.setMultiplicationFactor(5);

	return plan;
    }

    private static void saveCSV(List<String> csvColumns, List<Map<String, StressTestCSVValue>> valueList, OutputStream outfile)
	    throws IOException {
	OutputStreamWriter writer = new OutputStreamWriter(outfile);
	csvColumns.stream().forEach(c -> {
	    try {
		writer.write(c);
		writer.write(",");
	    } catch (IOException e) {
		throw new RuntimeException(e);
	    }
	});

	writer.write(System.lineSeparator());
	valueList.stream().forEach(values -> {
	    csvColumns.stream().forEach(c -> {
		try {
		    writer.write(values.get(c).getValue());
		    writer.write(",");
		} catch (IOException e) {
		    throw new RuntimeException(e);
		}
	    });
	    try {
		writer.write(System.lineSeparator());

	    } catch (IOException e) {
		throw new RuntimeException(e);
	    }
	});

	writer.flush();

    }

    private static String logGroup(String host) {
	if ("https://gs-service-test.geodab.eu".equalsIgnoreCase(host))
	    return "gstestcluster";

	if ("https://gs-service-preproduction.geodab.eu".equalsIgnoreCase(host))
	    return "gsprodcluster";

	if ("https://gs-service-production.geodab.eu".equalsIgnoreCase(host))
	    return "gsprodcluster";
	return null;
    }

    private static String logPrefix(String host) {

	if ("https://gs-service-test.geodab.eu".equalsIgnoreCase(host))
	    return "gs-service-test";

	if ("https://gs-service-preproduction.geodab.eu".equalsIgnoreCase(host))
	    return "gs-service-preprod";

	if ("https://gs-service-production.geodab.eu".equalsIgnoreCase(host))
	    return "gs-service-production";

	return null;

    }

    public static void main(String[] args) {

	StressPlan plan = new DiscoveryStressExternalTestIT().definePlan();
	Path source = Paths.get(DiscoveryStressExternalTestIT.class.getResource("/").getPath());

	List<String> csvColumns = new ArrayList<>();

	List<Map<String, StressTestCSVValue>> valueList = new ArrayList<>();

	File testresultFolder = new File(source + "/stresstest/");

	if (!testresultFolder.exists()) {
	    try {
		Files.createDirectory(testresultFolder.toPath());
	    } catch (IOException e) {
		throw new RuntimeException(e);
	    }
	}
	Arrays.asList(1).stream().forEach(parallel -> {
	    Arrays.asList("production", "preproduction", "test").stream().forEach(env -> {
		//	    Arrays.asList("test").stream().forEach(env -> {

		String hostname = "https://gs-service-" + env + ".geodab.eu";
		String logGroup = logGroup(hostname);
		String logPrefix = logPrefix(hostname);

		plan.setParallelRequests(parallel);
		StressPlanExecutor planExecutor = new StressPlanExecutor(plan, hostname);

		StressPlanResultCollector collector = new StressPlanResultCollector(logGroup, logPrefix);
		try {
		    planExecutor.execute(collector, 10L, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
		    throw new RuntimeException(e);
		}

		collector.printReport(System.out);

		try {

		    Path p = Paths.get(testresultFolder.getAbsolutePath() + "/stresstest-report-env-" + env + "-" + parallel + ".txt");

		    if (Files.exists(p))
			Files.delete(p);

		    Path file = Files.createFile(p);

		    OutputStream outfile = new FileOutputStream(file.toFile());
		    collector.printReport(outfile);
		    System.out.println("TXT file: " + file.toFile().getAbsolutePath());
		} catch (IOException e) {
		    throw new RuntimeException(e);
		}

		if (csvColumns.size() == 0) {
		    csvColumns.addAll(collector.getCSVColumns());
		}

		valueList.add(collector.getCSVColumnValues());
	    });

	});

	try {

	    Path p = Paths.get(testresultFolder.getAbsolutePath() + "/stresstestresult-all.csv");

	    if (Files.exists(p))
		Files.delete(p);

	    Path file = Files.createFile(p);

	    OutputStream outfile = new FileOutputStream(file.toFile());

	    saveCSV(csvColumns, valueList, outfile);
	    System.out.println("CSV file: " + file.toFile().getAbsolutePath());

	} catch (IOException e) {
	    throw new RuntimeException(e);
	}

    }

}
