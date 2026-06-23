package eu.essi_lab.gssrv.conf.task.trigger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetCDFToCOGProcessor {

    private static final DateTimeFormatter FILE_TIME_FORMATTER =
	    DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    private static final String TARGET_EPSG = "EPSG:3857";

    public static void main(String[] args) {
	String resourceFileName = "wbgt_wrfout_20250101.nc";
	String variableName = "wbgt";
	Path outputDir = Paths.get("./output_cogs");
	Path tempNcFile = null;

	try {
	    InputStream is = NetCDFToCOGProcessor.class.getClassLoader().getResourceAsStream(resourceFileName);
	    if (is == null) {
		throw new IllegalArgumentException("Resource file not found in src/main/resources: " + resourceFileName);
	    }

	    tempNcFile = Files.createTempFile("gdal_input_", "_" + resourceFileName);
	    System.out.println("Extracting classpath resource to temporary disk location: " + tempNcFile);

	    Files.copy(is, tempNcFile, StandardCopyOption.REPLACE_EXISTING);
	    is.close();

	    NetCDFToCOGProcessor processor = new NetCDFToCOGProcessor();
	    List<Path> createdCogs = processor.processNetCdfToMultipleCogs(tempNcFile, variableName, outputDir);

	    System.out.println("\nProcessing Complete! Created " + createdCogs.size() + " COG files.");
	    for (Path cog : createdCogs) {
		System.out.println("-> Generated: " + cog.toAbsolutePath());
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    if (tempNcFile != null) {
		try {
		    Files.deleteIfExists(tempNcFile);
		} catch (Exception ignored) {}
	    }
	}
    }

    public List<Path> processNetCdfToMultipleCogs(Path ncFile, String variableName, Path outputDir) throws Exception {
	List<Path> structuralCogFiles = new ArrayList<>();
	List<String> createdFileNames = new ArrayList<>();

	// Create the clean variable folder: output_cogs/wbgt/
	Path varOutputDir = outputDir.resolve(variableName);
	Files.createDirectories(varOutputDir);

	String subdataset = String.format("NETCDF:\"%s\":%s", ncFile.toAbsolutePath(), variableName);

	System.out.println("Inspecting NetCDF dimensions via gdalinfo...");
	String jsonMetadataStr = executeGdalInfo(subdataset);
	JSONObject root = new JSONObject(jsonMetadataStr);

	JSONObject globalMetadata = root.getJSONObject("metadata").getJSONObject("");
	String timeUnits = globalMetadata.getString("XTIME#units");

	String baseTimeIso = timeUnits.replace("minutes since ", "").trim().replace(" ", "T") + "Z";
	Instant baseInstant = Instant.parse(baseTimeIso);

	JSONArray bandsArray = root.getJSONArray("bands");
	System.out.println("Detected " + bandsArray.length() + " temporal bands. Starting pipeline...");

	for (int i = 0; i < bandsArray.length(); i++) {
	    JSONObject bandInfo = bandsArray.getJSONObject(i);
	    int bandNum = bandInfo.getInt("band");

	    JSONObject bandMeta = bandInfo.getJSONObject("metadata").getJSONObject("");
	    long minutesOffset = Long.parseLong(bandMeta.getString("NETCDF_DIM_XTIME"));

	    Instant validInstant = baseInstant.plus(Duration.ofMinutes(minutesOffset));
	    String formattedTimestamp = FILE_TIME_FORMATTER.format(validInstant);

	    String baseFileName = variableName + "_" + formattedTimestamp;

	    // Fix: Use a true temporary filename extension for the intermediate warp step
	    Path tempVrt = varOutputDir.resolve(baseFileName + ".vrt");
	    Path tempWarpTiff = varOutputDir.resolve(baseFileName + "_intermediate_warp.tif");
	    Path finalCog = varOutputDir.resolve(baseFileName + ".tif");

	    System.out.println(String.format("[%d/%d] Slicing Band %d -> Time: %s",
		    bandNum, bandsArray.length(), bandNum, formattedTimestamp));

	    // Pipeline Step 1: Create local VRT linking pixels to geolocation arrays
	    runProcess(new ProcessBuilder(
		    "gdal_translate",
		    "-of", "VRT",
		    "-b", String.valueOf(bandNum),
		    subdataset,
		    tempVrt.toAbsolutePath().toString()
	    ));

	    // Pipeline Step 2: Warp to a standard temporary GeoTIFF (GTiff)
	    runProcess(new ProcessBuilder(
		    "gdalwarp",
		    "-geoloc",
		    "-t_srs", TARGET_EPSG,
		    "-r", "cubic",
		    "-of", "GTiff", // <-- FIXED: Use standard GTiff for intermediate steps
		    tempVrt.toAbsolutePath().toString(),
		    tempWarpTiff.toAbsolutePath().toString()
	    ));

	    // Pipeline Step 3: Build standard multi-resolution overview pyramids
	    runProcess(new ProcessBuilder(
		    "gdaladdo",
		    "-r", "cubic",
		    tempWarpTiff.toAbsolutePath().toString(),
		    "2", "4", "8", "16"
	    ));

	    // Pipeline Step 4: Finalize as an optimized compressed cloud interleaved COG file
	    runProcess(new ProcessBuilder(
		    "gdal_translate",
		    tempWarpTiff.toAbsolutePath().toString(),
		    finalCog.toAbsolutePath().toString(),
		    "-of", "COG",
		    "-co", "COMPRESS=DEFLATE",
		    "-co", "PREDICTOR=3",
		    "-co", "BLOCKSIZE=512",
		    "-co", "RESAMPLING=CUBIC"
	    ));

	    // Clean up ALL temporary files immediately so they don't clutter your directory
	    Files.deleteIfExists(tempVrt);
	    Files.deleteIfExists(tempWarpTiff);

	    structuralCogFiles.add(finalCog);
	    createdFileNames.add(finalCog.getFileName().toString());
	}

	// Post-Processing: Generate index and legend directly inside output_cogs/wbgt/
	System.out.println("Generating index and vector configuration resources...");
	generateLegend(varOutputDir, variableName);
	generateIndexFile(varOutputDir, variableName, createdFileNames);

	return structuralCogFiles;
    }

    private void generateIndexFile(Path varDir, String var, List<String> files) throws Exception {
	if (files.isEmpty()) return;

	Collections.sort(files);

	String minDate = formatIsoTime(files.get(0));
	String maxDate = formatIsoTime(files.get(files.size() - 1));

	JSONObject indexJson = new JSONObject();
	indexJson.put("variable", var);
	indexJson.put("projection", TARGET_EPSG);
	indexJson.put("format", "COG");

	JSONObject phenomenonTime = new JSONObject();
	phenomenonTime.put("begin", minDate);
	phenomenonTime.put("end", maxDate);
	indexJson.put("phenomenonTime", phenomenonTime);

	String baseUrl = String.format("https://s3.us-east-1.amazonaws.com/s3-demo-geotiff/%s/", var);
	indexJson.put("legend", baseUrl + "legend.svg");

	JSONArray availabilityArray = new JSONArray();
	if (files.size() >= 2) {
	    Instant intervalStart = Instant.parse(formatIsoTime(files.get(0)));
	    Instant currentTrackedTime = intervalStart;
	    long currentStepHours = -1;

	    for (int i = 1; i < files.size(); i++) {
		Instant nextTime = Instant.parse(formatIsoTime(files.get(i)));
		long calculatedDelta = Duration.between(currentTrackedTime, nextTime).toHours();

		if (currentStepHours == -1) {
		    currentStepHours = calculatedDelta;
		} else if (calculatedDelta != currentStepHours) {
		    availabilityArray.put(buildAvailabilityObject(intervalStart, currentTrackedTime, currentStepHours));
		    intervalStart = currentTrackedTime;
		    currentStepHours = calculatedDelta;
		}
		currentTrackedTime = nextTime;
	    }
	    if (currentStepHours != -1) {
		availabilityArray.put(buildAvailabilityObject(intervalStart, currentTrackedTime, currentStepHours));
	    }
	}
	indexJson.put("availability", availabilityArray);

	JSONArray filesArray = new JSONArray();
	for (String fname : files) {
	    JSONObject fileItem = new JSONObject();
	    fileItem.put("time", formatIsoTime(fname));
	    fileItem.put("filename", fname);
	    fileItem.put("url", baseUrl + fname);
	    filesArray.put(fileItem);
	}
	indexJson.put("files", filesArray);

	Path indexPath = varDir.resolve("index.json");
	Files.writeString(indexPath, indexJson.toString(2), StandardCharsets.UTF_8);
    }

    private JSONObject buildAvailabilityObject(Instant from, Instant to, long stepHours) {
	JSONObject block = new JSONObject();
	block.put("from", from.toString());
	block.put("to", to.toString());
	block.put("stepHours", stepHours);
	return block;
    }

    private void generateLegend(Path varDir, String var) throws Exception {
	Legend legend = LegendFactory.getLegend(var);
	if (legend == null) return;

	int width = 320;
	int itemHeight = 22;
	int spacing = 6;
	int margin = 10;
	int height = margin + 30 + legend.items.size() * (itemHeight + spacing);

	StringBuilder svg = new StringBuilder();
	svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	svg.append("<svg xmlns='http://www.w3.org/2000/svg' width='").append(width).append("' height='").append(height).append("'>");
	svg.append("<rect width='100%' height='100%' fill='#f5f5f5'/>");
	svg.append("<text x='10' y='20' font-size='14' font-weight='bold'>").append(escapeXml(legend.title)).append("</text>");

	int y = 40;
	for (LegendItem item : legend.items) {
	    svg.append("<rect x='10' y='").append(y).append("' width='18' height='18' fill='").append(item.color)
		    .append("' stroke='black' stroke-width='0.5'/>");
	    svg.append("<text x='35' y='").append(y + 13).append("' font-size='12'>").append(escapeXml(item.range)).append(" (")
		    .append(escapeXml(item.label)).append(")</text>");
	    y += itemHeight + spacing;
	}
	svg.append("</svg>");

	Path svgPath = varDir.resolve("legend.svg");
	Files.writeString(svgPath, svg.toString(), StandardCharsets.UTF_8);
    }

    private String escapeXml(String text) {
	return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String formatIsoTime(String filename) {
	try {
	    String baseName = filename.substring(0, filename.lastIndexOf('.'));
	    String ts = baseName.substring(baseName.lastIndexOf('_') + 1).replaceAll("[^0-9]", "");
	    return String.format("%s-%s-%sT%s:00:00Z", ts.substring(0, 4), ts.substring(4, 6), ts.substring(6, 8), ts.substring(8, 10));
	} catch (Exception e) {
	    return "N/A";
	}
    }

    private String executeGdalInfo(String datasetPath) throws Exception {
	ProcessBuilder pb = new ProcessBuilder("gdalinfo", "-json", datasetPath);
	Process process = pb.start();
	StringBuilder output = new StringBuilder();
	try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
	    String line;
	    while ((line = reader.readLine()) != null) {
		output.append(line).append("\n");
	    }
	}
	if (process.waitFor() != 0) {
	    throw new RuntimeException("gdalinfo execution failed.");
	}
	return output.toString();
    }

    private void runProcess(ProcessBuilder pb) throws Exception {
	pb.redirectErrorStream(true);
	Process p = pb.start();
	try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
	    while (r.readLine() != null) { /* drain stream buffer */ }
	}
	if (p.waitFor() != 0) {
	    throw new RuntimeException("GDAL failed command execution: " + pb.command());
	}
    }
}