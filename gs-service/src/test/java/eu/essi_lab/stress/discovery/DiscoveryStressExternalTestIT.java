package eu.essi_lab.stress.discovery;

import eu.essi_lab.stress.plan.StressPlan;
import eu.essi_lab.stress.plan.StressPlanExecutor;
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

	StressPlan plan = new StressPlan();
	plan.addStressTest(t1);
	plan.addStressTest(t2);
	plan.addStressTest(t3);
	plan.addStressTest(t4);
	plan.addStressTest(t5);
	plan.setParallelRequests(3);
	plan.setMultiplicationFactor(1);

	return plan;
    }

    private static void saveCSV(List<String> csvColumns, List<List<String>> valueList, OutputStream outfile) throws IOException {
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

    }

    public static void main(String[] args) {

	StressPlan plan = new DiscoveryStressExternalTestIT().definePlan();
	Path source = Paths.get(DiscoveryStressExternalTestIT.class.getResource("/").getPath());

	List<String> csvColumns = new ArrayList<>();

	List<List<String>> valueList = new ArrayList<>();

	File testresultFolder = new File(source + "/stresstest/");

	if (!testresultFolder.exists()) {
	    try {
		Files.createDirectory(testresultFolder.toPath());
	    } catch (IOException e) {
		throw new RuntimeException(e);
	    }
	}

	Arrays.asList("production").stream().forEach(env -> {

	    String hostname = "https://gs-service-" + env + ".geodab.eu";
	    StressPlanExecutor planExecutor = new StressPlanExecutor(plan, hostname);

	    DiscoveryStressPlanResultCollector collector = new DiscoveryStressPlanResultCollector();
	    try {
		planExecutor.execute(collector);
	    } catch (InterruptedException e) {
		throw new RuntimeException(e);
	    }

	    collector.printReport(System.out);

	    try {

		Path p = Paths.get(testresultFolder.getAbsolutePath() + "/stresstest-report-env-" + env + ".txt");

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
