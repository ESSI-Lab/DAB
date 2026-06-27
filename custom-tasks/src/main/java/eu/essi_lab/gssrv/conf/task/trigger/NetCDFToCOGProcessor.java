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
import java.util.stream.Stream;

public class NetCDFToCOGProcessor {

    private static final DateTimeFormatter FILE_TIME_FORMATTER =
	    DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    private static final String TARGET_EPSG = "EPSG:3857";


//    private static class FileStats {
//	double min = Double.MAX_VALUE;
//	double max = -Double.MAX_VALUE;
//	boolean hasData = false;
//
//	void merge(double bandMin, double bandMax) {
//	    this.min = Math.min(this.min, bandMin);
//	    this.max = Math.max(this.max, bandMax);
//	    this.hasData = true;
//	}
//    }

    public static void main(String[] args) {
	String[] targetVariables = {"WCT"};
	Path baseTriggerPath = Paths.get("E:/TRIGGER/CIMA");

	for (String variable : targetVariables) {
	    Path varSourceDir = baseTriggerPath.resolve(variable);

	    // 1. Setup the isolated nested target folder: E:\TRIGGER\CIMA\[VAR]\output_cog\
	    Path outputCogDir = varSourceDir.resolve("output_cog");

	    System.out.println("\n==============================================");
	    System.out.println("Processing Local Variable: " + variable);
	    System.out.println("Source Path: " + varSourceDir.toAbsolutePath());
	    System.out.println("Output Path: " + outputCogDir.toAbsolutePath());
	    System.out.println("==============================================");

	    if (!Files.exists(varSourceDir)) {
		System.err.println("Source directory does not exist! Skipping " + variable);
		continue;
	    }

	    try {
		Files.createDirectories(outputCogDir);

		// Dynamically look up all local NetCDF files inside the folder
		List<Path> netCdfFiles;
		try (Stream<Path> walk = Files.walk(varSourceDir, 1)) {
		    netCdfFiles = walk
			    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".nc"))
			    .toList();
		}

		if (netCdfFiles.isEmpty()) {
		    System.out.println("No matching .nc files found in this directory.");
		    continue;
		}
		NetCDFToCOGProcessor processor = new NetCDFToCOGProcessor();
		List<String> generatedCogNames = new ArrayList<>();

		// Track absolute minimum and maximum values across all bands and files
		double globalMin = Double.MAX_VALUE;
		double globalMax = -Double.MAX_VALUE;

		for (Path ncFile : netCdfFiles) {
		    System.out.println("\nProcessing: " + ncFile.getFileName());

		    // Convert NetCDF and parse statistics
		    processor.processNetCdfToMultipleCogs(ncFile, variable.toLowerCase(), outputCogDir, generatedCogNames);

//		    if (stats.hasData) {
//			globalMin = Math.min(globalMin, stats.min);
//			globalMax = Math.max(globalMax, stats.max);
//		    }
		}

		// 2. Wrap up configuration asset assets into the clean output folder
		if (!generatedCogNames.isEmpty()) {
		    Collections.sort(generatedCogNames);
		    System.out.println("\nWriting index.json and legend.svg configurations...");
		    processor.generateLegend(outputCogDir, variable.toLowerCase());
		    processor.generateIndexFile(outputCogDir, variable.toLowerCase(), generatedCogNames);

		    // Print the true calculated min/max boundaries
		    System.out.println(String.format("\n>>> [%s] HISTORICAL MATRIX BOUNDARIES:", variable.toUpperCase()));
		    System.out.println(String.format(" -> Absolute Global Minimum: %.4f", globalMin));
		    System.out.println(String.format(" -> Absolute Global Maximum: %.4f", globalMax));
		}

	    } catch (Exception e) {
		System.err.println("Critical error compiling cluster " + variable);
		e.printStackTrace();
	    }
	}
    }

    public void processNetCdfToMultipleCogs(Path ncFile, String variableName, Path outputCogDir, List<String> generatedCogNames) throws Exception {

	String subdataset = String.format("NETCDF:\"%s\":%s", ncFile.toAbsolutePath(), variableName);

	String jsonMetadataStr = executeGdalInfo(subdataset, false); // True to compute min/max statistics via -stats
	JSONObject root = new JSONObject(jsonMetadataStr);

	JSONObject globalMetadata = root.getJSONObject("metadata").getJSONObject("");
	String timeUnits = globalMetadata.getString("XTIME#units");

	String baseTimeIso = timeUnits.replace("minutes since ", "").trim().replace(" ", "T") + "Z";
	Instant baseInstant = Instant.parse(baseTimeIso);

	JSONArray bandsArray = root.getJSONArray("bands");
	System.out.println("Detected " + bandsArray.length() + " temporal bands. Starting pipeline...");

	JSONObject firstBand = bandsArray.getJSONObject(0);
	JSONObject firstBandMeta = firstBand.getJSONObject("metadata").getJSONObject("");
	long initialFileOffset = Long.parseLong(firstBandMeta.getString("NETCDF_DIM_XTIME"));
	long duplicateCutoffOffset = initialFileOffset + 1440;
	System.out.println(String.format("File starts at offset: %d min. Dynamic hour 24 cutoff set at: %d min.",
		initialFileOffset, duplicateCutoffOffset));

	for (int i = 0; i < bandsArray.length(); i++) {
	    JSONObject bandInfo = bandsArray.getJSONObject(i);
	    int bandNum = bandInfo.getInt("band");

	    JSONObject bandMeta = bandInfo.getJSONObject("metadata").getJSONObject("");
	    long minutesOffset = Long.parseLong(bandMeta.getString("NETCDF_DIM_XTIME"));

	    // Skip hour 24 to prevent next-day collision
	    if (minutesOffset >= duplicateCutoffOffset) {
		System.out.println(String.format("Skipping Band %d (Offset: %d min) to prevent cross-day overlapping.", bandNum, minutesOffset));
		continue;
	    }

	    // Read the band statistics safely generated by gdalinfo -stats
//	    if (bandInfo.has("minimum") && bandInfo.has("maximum")) {
//		double bMin = bandInfo.getDouble("minimum");
//		double bMax = bandInfo.getDouble("maximum");
//		stats.merge(bMin, bMax);
//	    }

	    Instant validInstant = baseInstant.plus(Duration.ofMinutes(minutesOffset));
	    String formattedTimestamp = FILE_TIME_FORMATTER.format(validInstant);

	    String baseFileName = variableName + "_" + formattedTimestamp;

	    Path tempVrt = outputCogDir.resolve(baseFileName + ".vrt");
	    Path tempWarpTiff = outputCogDir.resolve(baseFileName + "_warp.tif");
	    Path finalCog = outputCogDir.resolve(baseFileName + ".tif");

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

	    generatedCogNames.add(finalCog.getFileName().toString());
	}

	// Post-Processing: Generate index and legend directly inside output_cogs/wbgt/
	//System.out.println("Generating index and vector configuration resources...");
	//generateLegend(varOutputDir, variableName);
	//generateIndexFile(varOutputDir, variableName, createdFileNames);

	//return stats;
    }

    private void generateIndexFile(Path varDir, String var, List<String> files) throws Exception {
	String minDate = formatIsoTime(files.getFirst());
	String maxDate = formatIsoTime(files.getLast());

	JSONObject indexJson = new JSONObject();
	indexJson.put("variable", var);
	indexJson.put("projection", TARGET_EPSG);
	indexJson.put("format", "COG");

	JSONObject phenomenonTime = new JSONObject();
	phenomenonTime.put("begin", minDate);
	phenomenonTime.put("end", maxDate);
	indexJson.put("phenomenonTime", phenomenonTime);

	String baseUrl = String.format("https://s3.amazonaws.com/your-bucket-name/%s/output_cog/", var);
	indexJson.put("legend", baseUrl + "legend.svg");

	JSONArray availabilityArray = new JSONArray();
	if (files.size() >= 2) {
	    Instant intervalStart = Instant.parse(formatIsoTime(files.getFirst()));
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

	Files.writeString(varDir.resolve("index.json"), indexJson.toString(2), StandardCharsets.UTF_8);
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
	    svg.append("<rect x='10' y='").append(y).append("' width='18' height='18' fill='").append(item.color).append("' stroke='black' stroke-width='0.5'/>");
	    svg.append("<text x='35' y='").append(y + 13).append("' font-size='12'>").append(escapeXml(item.range)).append(" (").append(escapeXml(item.label)).append(")</text>");
	    y += itemHeight + spacing;
	}
	svg.append("</svg>");

	Files.writeString(varDir.resolve("legend.svg"), svg.toString(), StandardCharsets.UTF_8);
    }

    private String executeGdalInfo(String datasetPath, boolean computeStats) throws Exception {
	List<String> commands = new ArrayList<>(List.of("gdalinfo", "-json"));
	if (computeStats) {
	    commands.add("-stats"); // Forces GDAL to scan and append min/max data structures
	}
	commands.add(datasetPath);

	ProcessBuilder pb = new ProcessBuilder(commands);
	Process process = pb.start();
	StringBuilder output = new StringBuilder();
	try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
	    String line;
	    while ((line = reader.readLine()) != null) {
		output.append(line).append("\n");
	    }
	}
	if (process.waitFor() != 0) throw new RuntimeException("gdalinfo failed.");
	return output.toString();
    }

    private void runProcess(ProcessBuilder pb) throws Exception {
	pb.redirectErrorStream(true);
	Process p = pb.start();
	try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
	    while (r.readLine() != null) { }
	}
	if (p.waitFor() != 0) throw new RuntimeException("GDAL failed: " + pb.command());
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
}