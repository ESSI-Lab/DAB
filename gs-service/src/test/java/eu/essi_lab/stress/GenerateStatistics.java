package eu.essi_lab.stress;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ilsanto
 */
public class GenerateStatistics {

    private static Logger logger = LoggerFactory.getLogger(GenerateStatistics.class);
    private static final String CSV_SEPARATOR = ";";

    public static void main(String[] args) throws IOException {

	String folder = "/Users/ilsanto/Desktop/querystats/auto/real/m3XLargeFrontend_c5_18xlarge_1node/";

	String outputFile = folder + "allTests.csv";

	FileWriter writer = new FileWriter(new File(outputFile));

	File folderFile = new File(folder);

	File[] files = folderFile.listFiles();

	List<File> list = Arrays.asList(files);

	list.sort(new Comparator<File>() {
	    @Override
	    public int compare(File o1, File o2) {

		return o1.getName().compareTo(o2.getName());

	    }
	});

	for (File f : list) {

	    String name = f.getName();

	    logger.info("Found file {}", name);

	    if (name.split("\\.")[0].contains("x")) {

		toFile(name, new FileReader(f), writer);

	    } else {
		logger.info("Skipping {}", name);
	    }

	}

	writer.close();

    }

    private static void toFile(String configuration, FileReader reader, FileWriter writer) throws IOException {

	writer.append("\n");

	writer.append(CSV_SEPARATOR);

	writer.append("1 Parallel Query");
	writer.append(CSV_SEPARATOR);

	writer.append("2 Parallel Queries");
	writer.append(CSV_SEPARATOR);

	writer.append("4 Parallel Queries");
	writer.append(CSV_SEPARATOR);

	writer.append("8 Parallel Queries");
	writer.append(CSV_SEPARATOR);

	writer.append("16 Parallel Queries");
	writer.append(CSV_SEPARATOR);

	writer.append("32 Parallel Queries");
	writer.append(CSV_SEPARATOR);

	writer.append("64 Parallel Queries");
	writer.append(CSV_SEPARATOR);
	writer.append("\n");

	BufferedReader buffer = new BufferedReader(reader);

	String line = buffer.readLine();

	Map<String, Map<String, String>> mapMap = new HashMap<>();

	writer.append(configuration + "\n");

	while ((line = buffer.readLine()) != null) {
	    // process the line.

	    String phase = getPhase(line);
	    logger.debug("Found phase trest {} from {}", phase, line);

	    String numqueries = getNumQueries(line);
	    logger.debug("Found excution trest {} from {}", numqueries, line);

	    String executionTime = getExecutionTime(line);

	    logger.debug("Found excution time {} from {}", executionTime, line);

	    if (mapMap.get(phase) == null) {
		mapMap.put(phase, new HashMap<>());
	    }

	    mapMap.get(phase).put(numqueries, executionTime);
	}

	String p = "RESULT_SET_MAPPING";
	writeLine(p, mapMap.get(p), writer);

	p = "REQUEST_HANDLING";
	writeLine(p, mapMap.get(p), writer);

	p = "BOND_NORMALIZATION";
	writeLine(p, mapMap.get(p), writer);

	p = "MARKLOGIC_COUNTQUERY_EXECUTION";
	writeLine(p, mapMap.get(p), writer);

	p = "MARKLOGIC_COUNTQUERY_GENERATION";
	writeLine(p, mapMap.get(p), writer);

	p = "MARKLOGIC_NODES_CREATION";
	writeLine(p, mapMap.get(p), writer);

	p = "MARKLOGIC_NODES_TO_GS_RESOURCE_MAPPING";
	writeLine(p, mapMap.get(p), writer);

	p = "MARKLOGIC_QUERY_EXECUTION";
	writeLine(p, mapMap.get(p), writer);

	p = "MARKLOGIC_QUERY_GENERATION";
	writeLine(p, mapMap.get(p), writer);

	p = "MESSAGE_AUTHORIZATION";
	writeLine(p, mapMap.get(p), writer);

	p = "RESULT_SET_FORMATTING";
	writeLine(p, mapMap.get(p), writer);

	p = "RESULT_SET_RETRIEVING";
	writeLine(p, mapMap.get(p), writer);
    }

    private static void writeLine(String p, Map<String, String> map, FileWriter writer) throws IOException {

	String line = p + CSV_SEPARATOR;

	line += map.get("1");
	line += CSV_SEPARATOR;

	line += map.get("2");
	line += CSV_SEPARATOR;

	line += map.get("4");
	line += CSV_SEPARATOR;

	line += map.get("8");
	line += CSV_SEPARATOR;

	line += map.get("16");
	line += CSV_SEPARATOR;

	line += map.get("32");
	line += CSV_SEPARATOR;

	line += map.get("64");
	//	line+= CSV_SEPARATOR;

	logger.debug("Appenginf line {}", line);
	writer.append(line + "\n");

    }

    private static String getPhase(String line) {

	String first = line.split(",")[0].trim();

	String[] split = first.split("_");

	String phase = "";

	for (int i = 1; i < split.length; i++) {
	    phase += "_";
	    phase += split[i];

	}

	return phase.replaceFirst("_", "");

    }

    private static String getExecutionTime(String line) {

	return line.split(",")[1].trim().replace(".", ",");

    }

    private static String getNumQueries(String line) {
	String first = line.split(",")[0].trim();

	return first.split("_")[0].split("-")[1];

    }
}
