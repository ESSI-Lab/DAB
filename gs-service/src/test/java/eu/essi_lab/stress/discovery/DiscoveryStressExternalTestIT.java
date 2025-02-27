package eu.essi_lab.stress.discovery;

import eu.essi_lab.lib.utils.Base64Utils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Mattia Santoro
 */
public class DiscoveryStressExternalTestIT {

    private DiscoveryStressPlan definePlan() {
	DiscoveryStressTest t1 = new DiscoveryStressTest();
	t1.setSearchText("ozone");

	DiscoveryStressTest t2 = new DiscoveryStressTest();
	t2.setSearchText("temperature");
	t2.setBbox("-10.9,35.23,2.2,43.434");

	DiscoveryStressPlan plan = new DiscoveryStressPlan();
	plan.addStressTest(t1);
	plan.addStressTest(t2);
	plan.setParallelRequests(3);
	plan.setMultiplicationFactor(2);

	return plan;
    }

    private void executeAndPrint(DiscoveryStressPlan plan, String hostname, OutputStream report_out, OutputStream csv_out)
	    throws IOException {
	DiscoveryStressPlanExecutor planExecutor = new DiscoveryStressPlanExecutor(plan, hostname);

	DiscoveryStressPlanResultCollector collector = new DiscoveryStressPlanResultCollector();

	try {
	    planExecutor.execute(collector);
	} catch (InterruptedException e) {
	    throw new RuntimeException(e);
	}

	collector.printReport(report_out);

	collector.saveReportToCSV(csv_out);
    }

    public static void main(String[] args) {

	DiscoveryStressPlan plan = new DiscoveryStressExternalTestIT().definePlan();

	List<String> csvColumns = new ArrayList<>();

	List<List<String>> valueList = new ArrayList<>();

	Arrays.asList("production", "test").stream().forEach(env -> {

	    String hostname = "https://gs-service-" + env + ".geodab.eu";
	    DiscoveryStressPlanExecutor planExecutor = new DiscoveryStressPlanExecutor(plan, hostname);

	    DiscoveryStressPlanResultCollector collector = new DiscoveryStressPlanResultCollector();
	    try {
		planExecutor.execute(collector);
	    } catch (InterruptedException e) {
		throw new RuntimeException(e);
	    }

	    collector.printReport(System.out);

	    try {
		Path file = Files.createTempFile("stresstest-report-env-", ".txt");

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

	try {
	    Path file = Files.createTempFile("stresstestresult-all-", ".csv");
	    OutputStream outfile = new FileOutputStream(file.toFile());

	    OutputStreamWriter writer = new OutputStreamWriter(outfile);
	    csvColumns.stream().forEach(c -> {
		try {
		    writer.write(c);
		    writer.write(",");
		} catch (IOException e) {
		    throw new RuntimeException(e);
		}
	    });

	    writer.write("\n");
	    valueList.stream().forEach(values -> {
		values.stream().forEach(c -> {
		    try {
			writer.write(c);
			writer.write(",");
		    } catch (IOException e) {
			throw new RuntimeException(e);
		    }
		});
		try {
		    writer.write("\n");
		} catch (IOException e) {
		    throw new RuntimeException(e);
		}
	    });

	    writer.flush();

	    System.out.println("CSV file: " + file.toFile().getAbsolutePath());
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}

	//	String hostname = "https://gs-service-production.geodab.eu";
	//
	//	try {
	//	    Path file = Files.createTempFile("stresstest", ".csv");
	//	    OutputStream outfile = new FileOutputStream(file.toFile());
	//	    new DiscoveryStressExternalTestIT().executeAndPrint(plan, hostname, System.out, outfile);
	//
	//	    System.out.println("CSV file: " + file.toFile().getAbsolutePath());
	//	} catch (IOException e) {
	//	    throw new RuntimeException(e);
	//	}

    }

}
