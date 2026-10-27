package eu.essi_lab.gssrv.conf.task.trigger;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.oaipmh.ListSetsType;
import eu.essi_lab.lib.net.dirlisting.WAFClient;
import eu.essi_lab.lib.net.dirlisting.WAF_URL;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.Operations;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.referencing.CRS;
import org.geotools.util.factory.Hints;
import org.glassfish.jaxb.core.v2.TODO;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class COGS3SyncProcessor {

    private final S3TransferWrapper s3;
    private final String bucketName;
    private final String[] variables;
    private final String sourceURL;
    private Map<String, List<String>> variableFilesMap = new HashMap<>();

    private static final String TARGET_EPSG = "EPSG:3857";

    // Sea polygons: OpenStreetMap land polygons (osmdata.openstreetmap.de, land-polygons-split-3857 of 2026-10-23), rasterized on
    // the 2.5 km output grid (EPSG:3857, origin -2785000 11755000, 3121x3299) with all touched pixels (gdal_rasterize -at: every
    // pixel touching land is kept, so the coast and the small islands are always covered, data spill over the sea <= 1 pixel)
    // and the sea pixels polygonized, so the mask is reproduced exactly. Pixels inside are set to NoData with a plain
    // gdal_rasterize: the inverse (-i) of land polygons fails for islands inside lagoons (holes inside holes, e.g. Arabat spit).
    // To be regenerated if the ECMWF domain or TARGET_RESOLUTION change: see custom-tasks/sea-mask/README.md. Data (c)
    // OpenStreetMap contributors, ODbL. land_mask.geojson (Natural Earth 1:50m) is used by the CAMS optional sea masking
    private static final String SEA_MASK_RESOURCE = "trigger/sea_mask_osm.geojson";
    private static final String NODATA_VALUE = "-9999";

    // Output grid finer than the source (0.1 deg): values stay the model cells (nearest), but the coastline cut follows the coast
    private static final String TARGET_RESOLUTION = "2500";

    // Sea cells within this distance (in source cells) from land take the nearest land value, so coastal pixels never show sea values
    private static final double LAND_FILL_RADIUS_CELLS = 2.5;

    // Decimals kept in the published values: 0.1 is well below the model uncertainty and makes the COGs ~2.3 times smaller
    // (Float32 noise in the last bits compresses badly). SHIWE is an integer index, not affected
    private static final int VALUE_DECIMALS = 1;

    private Path seaMask;

    // Coast fill map (see computeFillSource): same for all the files of a run with the same source grid
    private String fillGridKey;
    private int[] fillSource;

    // Errors on single files written in the report: with a broken source there may be hundreds
    private static final int MAX_REPORTED_ERRORS = 10;

    // Waits before the download retries (seconds)
    private static final long[] DOWNLOAD_RETRY_WAITS_SECONDS = { 10, 30 };

    private TaskReport report = TaskReport.NONE;
    private int failedFiles;

    public COGS3SyncProcessor(S3TransferWrapper s3, String bucketName, String[] variables, String sourceURL) {
	this.s3 = s3;
	this.bucketName = bucketName;
	this.variables = variables;
	this.sourceURL = sourceURL;
    }

    /**
     * Summary of the execution, written in the task status
     */
    public void setReport(TaskReport report) {
	this.report = report;
    }

    public void execute() throws Exception {

	Path tempWorkDir = Paths.get(System.getProperty("java.io.tmpdir"), "geotiff_processing");

	if (Files.exists(tempWorkDir)) {
	    GSLoggerFactory.getLogger(getClass()).info("Cleaning existing working directory: " + tempWorkDir);
	    cleanup(tempWorkDir);
	}
	Files.createDirectories(tempWorkDir);

	GSLoggerFactory.getLogger(getClass()).info("=== COG S3 Sync START ===");
	GSLoggerFactory.getLogger(getClass()).info("Source URL: " + sourceURL);
	GSLoggerFactory.getLogger(getClass()).info("Bucket: " + bucketName);
	GSLoggerFactory.getLogger(getClass()).info("Variables: " + Arrays.toString(variables));
	GSLoggerFactory.getLogger(getClass()).info("Working dir: " + tempWorkDir);

	report.info("Source " + sourceURL + ", bucket " + bucketName + ", variables " + String.join(", ", variables));

	try {

	    seaMask = extractSeaMask(tempWorkDir);

	    URL listURL = URI.create(sourceURL).toURL();
	    String user = ConfigurationWrapper.getCredentialsSetting().getTriggerWAFUser().orElse(null);
	    String pass = ConfigurationWrapper.getCredentialsSetting().getTriggerWAFPassword().orElse(null);

	    // 1. Discovery
	    List<URL> allFiles = WAFClient.listFiles(new WAF_URL(listURL), true, user, pass);
	    GSLoggerFactory.getLogger(getClass()).info("Total files discovered: " + allFiles.size());

	    List<URL> tiffUrls = allFiles.stream().filter(url -> url.toString().toLowerCase().contains(".tif"))
		    .collect(Collectors.toList());

	    GSLoggerFactory.getLogger(getClass()).info("GeoTIFF files found: " + tiffUrls.size());

	    List<URL> filteredURLs = filterURLs(tiffUrls);

	    GSLoggerFactory.getLogger(getClass()).info("File to analyze: " + filteredURLs.size());
	    report.info("Files found: " + allFiles.size() + ", GeoTIFF: " + tiffUrls.size() + ", selected: " + filteredURLs.size());

	    // 2. Download & Process
	    Downloader downloader = new Downloader();
	    for (URL url : filteredURLs) {
		processSingleFile(url, tempWorkDir, downloader, user, pass);
	    }

	    report.info("Converted " + (filteredURLs.size() - failedFiles) + "/" + filteredURLs.size() + " files" //
		    + (failedFiles > 0 ? ", " + failedFiles + " errors" : ""));
	    if (failedFiles > MAX_REPORTED_ERRORS) {
		report.error("... and " + (failedFiles - MAX_REPORTED_ERRORS) + " more errors, see the GS log");
	    }

	    // 2. Index Generation
	    generateIndexFiles(tempWorkDir);

	    // 3. S3 sync
	    syncToS3(tempWorkDir);

	    GSLoggerFactory.getLogger(getClass()).info("=== COG S3 Sync COMPLETED ===");

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error during the task: " + e.getMessage());
	    throw e;
	} finally {

	    cleanup(tempWorkDir);
	    variableFilesMap = new HashMap<>();
	    seaMask = null;
	    fillGridKey = null;
	    fillSource = null;
	    failedFiles = 0;
	}
    }

    private Path extractSeaMask(Path workDir) {
	try (InputStream in = getClass().getClassLoader().getResourceAsStream(SEA_MASK_RESOURCE)) {
	    if (in == null) {
		GSLoggerFactory.getLogger(getClass()).warn("Sea mask resource not found, ocean will not be masked: " + SEA_MASK_RESOURCE);
		report.warn("Sea mask resource not found, ocean not masked");
		return null;
	    }
	    Path out = workDir.resolve(Paths.get(SEA_MASK_RESOURCE).getFileName());
	    Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
	    GSLoggerFactory.getLogger(getClass()).info("Sea mask ready: " + out);
	    return out;
	} catch (IOException e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Unable to extract sea mask, ocean will not be masked: " + e.getMessage());
	    report.warn("Unable to extract sea mask, ocean not masked: " + TaskReport.message(e));
	    return null;
	}
    }

    private void processSingleFile(URL url, Path base, Downloader d, String u, String p) {
	String originalName = Paths.get(url.getPath()).getFileName().toString();
	String var = extractVariable(originalName);
	String finalBaseName = buildRenamedFileName(originalName);

	GSLoggerFactory.getLogger(getClass()).info("Processing file: " + originalName + " -> variable=" + var);

	try {
	    Path varFolder = base.resolve(var);
	    Files.createDirectories(varFolder);

	    Path raw = varFolder.resolve(finalBaseName + "_" + System.nanoTime() + ".tif");
	    Path reprojected = varFolder.resolve(finalBaseName + "_" + System.nanoTime() + "_3857.tif");

	    Path tmpPath = varFolder.resolve(finalBaseName + "_" + System.nanoTime() + "_tmp.tif");

	    Path finalCog = varFolder.resolve(finalBaseName + ".tif");

	    download(url, raw, d, u, p);

	    //	    // GeoTools Reproject
	    //	    reprojectTo3857(raw, reprojected);
	    //
	    //	    // GDAL COG
	    //	    runGdalCog(reprojected, finalCog);

	    long start = System.currentTimeMillis();

	    runGdalWarpToCOG(raw, finalCog, tmpPath);

	    long end = System.currentTimeMillis();

	    GSLoggerFactory.getLogger(getClass()).info("GDAL completed: " + finalCog.getFileName() + " (" + (end - start) + " ms)");

	    variableFilesMap.computeIfAbsent(var, k -> new ArrayList<>()).add(finalCog.getFileName().toString());

	    // Clean tmp files
	    Files.deleteIfExists(tmpPath);
	    Files.deleteIfExists(raw);
	    Files.deleteIfExists(reprojected);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error on " + originalName + ": " + describe(e));
	    if (++failedFiles <= MAX_REPORTED_ERRORS) {
		report.error(originalName + ": " + TaskReport.message(e));
	    }
	}
    }

    /**
     * Downloads with retries: in a run of 2026-10-22, after ~50 requests every new ECPDS request failed immediately without an
     * HTTP response (exception with null message), so a failed download waits before trying again
     */
    private void download(URL url, Path target, Downloader d, String u, String p) throws Exception {

	for (int attempt = 1; ; attempt++) {
	    try (InputStream in = d.downloadResponse(url.toExternalForm(), u, p).body()) {
		Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
		return;
	    } catch (Exception e) {
		if (attempt >= DOWNLOAD_RETRY_WAITS_SECONDS.length + 1) {
		    throw e;
		}
		long wait = DOWNLOAD_RETRY_WAITS_SECONDS[attempt - 1];
		GSLoggerFactory.getLogger(getClass()).warn("Download of " + url + " failed (" + describe(e) + "), retry " + attempt + " in "
			+ wait + " s");
		Thread.sleep(wait * 1000);
	    }
	}
    }

    /**
     * Exception type, message and root cause: several HTTP client errors (e.g. ConnectException) have a null message
     */
    private static String describe(Throwable e) {

	Throwable root = e;
	while (root.getCause() != null && root.getCause() != root) {
	    root = root.getCause();
	}
	String text = e.getClass().getSimpleName() + (e.getMessage() != null ? ": " + e.getMessage() : "");
	if (root != e) {
	    text += " (cause " + root.getClass().getSimpleName() + (root.getMessage() != null ? ": " + root.getMessage() : "") + ")";
	}
	return text;
    }

    private void runGdalWarpToCOG(Path input, Path output, Path temp) throws Exception {

	Path landOnly = Paths.get(temp.toString().replace("_tmp.tif", "_land.tif"));
	Path rawAsc = Paths.get(temp.toString().replace("_tmp.tif", "_raw.asc"));
	Path landAsc = Paths.get(temp.toString().replace("_tmp.tif", "_land.asc"));
	Path filledAsc = Paths.get(temp.toString().replace("_tmp.tif", "_filled.asc"));

	try {

	    // Coast fix: mask the sea on the source grid and extend land values over the first sea cells, so that the coastal pixels
	    // of the output take land values (not sea values, e.g. SHIWE is 1 over the Mediterranean and often 0 on the coast)
	    Path source = input;
	    if (seaMask != null) {
		extendLandValues(input, landOnly, rawAsc, landAsc, filledAsc);
		source = filledAsc;
	    }

	    // Nearest resampling: the indices are classes (SHIWE 0..5) or model cells, any interpolation invents values
	    List<String> warpCmd = new ArrayList<>(List.of(
		    "gdalwarp",
		    "-s_srs", "EPSG:4326",
		    "-t_srs", "EPSG:3857",
		    "-r", "near",
		    "-ot", "Float32",
		    "-tr", TARGET_RESOLUTION, TARGET_RESOLUTION,
		    "-tap"));

	    if (seaMask != null) {
		warpCmd.addAll(List.of("-srcnodata", NODATA_VALUE, "-dstnodata", NODATA_VALUE));
	    }

	    // Plain tiled GeoTIFF: the sea mask is burned below, then overviews and COG layout are built at the end
	    warpCmd.addAll(List.of(
		    "-of", "GTiff",
		    "-co", "TILED=YES",
		    "-co", "COMPRESS=DEFLATE",
		    source.toAbsolutePath().toString(),
		    temp.toAbsolutePath().toString()));

	    ProcessBuilder pb = new ProcessBuilder(warpCmd);

	    GSLoggerFactory.getLogger(getClass()).info("Running GDALWARP: " + String.join(" ", pb.command()));

	    pb.redirectErrorStream(true);
	    Process p = pb.start();

	    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
		String line;
		while ((line = reader.readLine()) != null) {

		    if (line.toLowerCase().contains("error") || line.toLowerCase().contains("warning")) {
			GSLoggerFactory.getLogger(getClass()).warn("[GDAL] " + line);
		    }
		}
	    }

	    int exit = p.waitFor();

	    if (exit != 0) {
		throw new RuntimeException("GDAL warp failed with code " + exit);
	    }

	    if (!Files.exists(temp) || Files.size(temp) == 0) {
		throw new IOException("COG not created: " + temp);
	    }

	    // Ocean mask on the output grid: pixels whose centre is inside the sea polygons become NoData (transparent in the
	    // OpenLayers client). Not done with gdalwarp -cutline, which rasterizes the cutline on the coarse source grid
	    // (coast in 10 km steps) and shifts the nearest sampling
	    if (seaMask != null) {
		runGdalStep(new ProcessBuilder("gdal_rasterize", "-burn", NODATA_VALUE, seaMask.toAbsolutePath().toString(),
			temp.toAbsolutePath().toString()));
	    }

	    ProcessBuilder addoPb = new ProcessBuilder("gdaladdo", "-r", "nearest", temp.toString(), "2", "4", "8", "16");

	    GSLoggerFactory.getLogger(getClass()).info("Running gdaladdo: " + String.join(" ", addoPb.command()));

	    Process addo = addoPb.start();
	    addo.waitFor();

	    exit = addo.waitFor();

	    if (exit != 0) {
		throw new RuntimeException("GDALADDO warp failed with code " + exit);
	    }

	    if (!Files.exists(temp) || Files.size(temp) == 0) {
		throw new IOException("COG not created: " + temp);
	    }

	    // STEP 3: final COG
	    ProcessBuilder translatePb = new ProcessBuilder("gdal_translate", temp.toString(), output.toString(), "-of", "COG", "-co",
		    "COMPRESS=DEFLATE", "-co", "PREDICTOR=2", "-co", "BLOCKSIZE=512", "-co", "RESAMPLING=NEAREST");

	    GSLoggerFactory.getLogger(getClass()).info("Running gdal_translate: " + String.join(" ", translatePb.command()));

	    Process translate = translatePb.start();
	    translate.waitFor();

	    exit =  translate.waitFor();

	    if (exit != 0) {
		throw new RuntimeException("GDAL translate failed with code " + exit);
	    }

	    if (!Files.exists(output) || Files.size(output) == 0) {
		throw new IOException("COG not created: " + output);
	    }

	    verifyCogFile(output);

	} finally {

	    Files.deleteIfExists(temp);
	    Files.deleteIfExists(landOnly);
	    for (Path asc : List.of(rawAsc, landAsc)) {
		Files.deleteIfExists(asc);
		Files.deleteIfExists(Paths.get(asc + ".aux.xml"));
		Files.deleteIfExists(Paths.get(asc.toString().replace(".asc", ".prj")));
	    }
	    Files.deleteIfExists(filledAsc);
	}

    }

    /**
     * Writes in <code>filledAsc</code> the source grid where the sea cells within {@link #LAND_FILL_RADIUS_CELLS} from land take the
     * value of the nearest land cell. The other sea cells keep the model value: small islands with no land-centred cell nearby
     * (e.g. Pantelleria) would otherwise stay without data. The sea is removed later on the output grid. Not resampled. Values
     * are rounded to {@link #VALUE_DECIMALS} decimals.
     */
    private void extendLandValues(Path input, Path landOnly, Path rawAsc, Path landAsc, Path filledAsc) throws Exception {

	// 1. source grid as it is. ASCII grid: simple to read and write, GDAL reads it back with the same georeferencing
	runGdalStep(new ProcessBuilder("gdal_translate", "-ot", "Float32", "-a_srs", "EPSG:4326", "-a_nodata", NODATA_VALUE, "-of",
		"AAIGrid", input.toAbsolutePath().toString(), rawAsc.toAbsolutePath().toString()));

	AsciiGrid raw = readAsciiGrid(rawAsc);

	// 2. the cell giving the value of each cell depends only on the grid and on the mask: computed once per run
	String gridKey = raw.header().stream().filter(h -> !h.trim().toLowerCase().startsWith("nodata_value"))
		.collect(Collectors.joining("|"));
	if (!gridKey.equals(fillGridKey)) {
	    fillSource = computeFillSource(input, landOnly, landAsc, raw.cols(), raw.rows());
	    fillGridKey = gridKey;
	}

	// 3. values taken from the fill source cells, rounded
	double scale = Math.pow(10, VALUE_DECIMALS);
	int cols = raw.cols();
	int rows = raw.rows();

	try (BufferedWriter writer = Files.newBufferedWriter(filledAsc, StandardCharsets.US_ASCII)) {
	    for (String h : raw.header()) {
		writer.write(h);
		writer.newLine();
	    }
	    StringBuilder row = new StringBuilder();
	    for (int r = 0; r < rows; r++) {
		row.setLength(0);
		for (int c = 0; c < cols; c++) {
		    if (c > 0) {
			row.append(' ');
		    }
		    double v = raw.values()[fillSource[r * cols + c]];
		    if (v != raw.nodata()) {
			v = Math.round(v * scale) / scale;
		    }
		    row.append(v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v));
		}
		writer.write(row.toString());
		writer.newLine();
	    }
	}
    }

    /**
     * @return for each source cell, the index of the cell giving its value: itself for land cells, the nearest land cell within
     *         {@link #LAND_FILL_RADIUS_CELLS} for sea cells close to land, itself for the other sea cells
     */
    private int[] computeFillSource(Path input, Path landOnly, Path landAsc, int cols, int rows) throws Exception {

	// copy of the source grid with the sea cells (centre inside the sea polygons) set to NoData
	runGdalStep(new ProcessBuilder("gdal_translate", "-ot", "Float32", "-a_srs", "EPSG:4326", "-a_nodata", NODATA_VALUE,
		input.toAbsolutePath().toString(), landOnly.toAbsolutePath().toString()));
	runGdalStep(new ProcessBuilder("gdal_rasterize", "-burn", NODATA_VALUE, seaMask.toAbsolutePath().toString(),
		landOnly.toAbsolutePath().toString()));
	runGdalStep(new ProcessBuilder("gdal_translate", "-of", "AAIGrid", landOnly.toAbsolutePath().toString(),
		landAsc.toAbsolutePath().toString()));

	AsciiGrid land = readAsciiGrid(landAsc);
	double nodata = land.nodata();
	double[] values = land.values();

	int radius = (int) Math.ceil(LAND_FILL_RADIUS_CELLS);
	double maxDist2 = LAND_FILL_RADIUS_CELLS * LAND_FILL_RADIUS_CELLS;
	int[] source = new int[cols * rows];

	for (int r = 0; r < rows; r++) {
	    for (int c = 0; c < cols; c++) {
		source[r * cols + c] = r * cols + c;
		if (values[r * cols + c] != nodata) {
		    continue;
		}
		double best = maxDist2 + 1;
		for (int dr = -radius; dr <= radius; dr++) {
		    for (int dc = -radius; dc <= radius; dc++) {
			int rr = r + dr;
			int cc = c + dc;
			double d2 = dr * dr + dc * dc;
			if (rr < 0 || rr >= rows || cc < 0 || cc >= cols || d2 > maxDist2 || d2 >= best) {
			    continue;
			}
			if (values[rr * cols + cc] != nodata) {
			    best = d2;
			    source[r * cols + c] = rr * cols + cc;
			}
		    }
		}
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Coast fill map computed for a " + cols + "x" + rows + " source grid");
	return source;
    }

    private record AsciiGrid(List<String> header, int cols, int rows, double nodata, double[] values) {
    }

    private static AsciiGrid readAsciiGrid(Path asc) throws IOException {

	List<String> header = new ArrayList<>();
	int cols = 0;
	int rows = 0;
	double nodata = Double.parseDouble(NODATA_VALUE);

	try (BufferedReader reader = Files.newBufferedReader(asc, StandardCharsets.US_ASCII)) {

	    String line;
	    while ((line = reader.readLine()) != null) {
		String[] kv = line.trim().split("\\s+");
		if (!Character.isLetter(kv[0].charAt(0))) {
		    break; // first data row
		}
		header.add(line);
		switch (kv[0].toLowerCase()) {
		case "ncols" -> cols = Integer.parseInt(kv[1]);
		case "nrows" -> rows = Integer.parseInt(kv[1]);
		case "nodata_value" -> nodata = Double.parseDouble(kv[1]);
		default -> {
		}
		}
	    }

	    double[] values = new double[cols * rows];
	    int i = 0;
	    while (line != null) {
		for (String token : line.trim().split("\\s+")) {
		    if (!token.isEmpty()) {
			values[i++] = Double.parseDouble(token);
		    }
		}
		line = reader.readLine();
	    }
	    return new AsciiGrid(header, cols, rows, nodata, values);
	}
    }

    private void runGdalStep(ProcessBuilder pb) throws Exception {

	pb.redirectErrorStream(true);
	Process p = pb.start();
	StringBuilder out = new StringBuilder();
	try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
	    String line;
	    while ((line = reader.readLine()) != null) {
		out.append(line).append('\n');
	    }
	}
	if (p.waitFor() != 0) {
	    throw new RuntimeException("GDAL failed: " + String.join(" ", pb.command()) + "\n" + out);
	}
    }

    private void verifyCogFile(Path output) throws Exception {
	ProcessBuilder pb = new ProcessBuilder("gdalinfo", output.toAbsolutePath().toString());
	Process p = pb.start();

	boolean isCog = false;
	boolean hasOverviews = false;

	try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
	    String line;
	    while ((line = reader.readLine()) != null) {
		// Check for COG driver/layout metadata
		if (line.contains("LAYOUT=COG") || line.contains("Driver: COG")) {
		    isCog = true;
		}
		// Check for overviews (pyramids)
		if (line.contains("Overviews:")) {
		    hasOverviews = true;
		}
	    }
	}

	p.waitFor();

	if (isCog && hasOverviews) {
	    GSLoggerFactory.getLogger(getClass()).info("[GDAL Verify] Success: File is a valid optimized COG with overviews.");
	} else {
	    GSLoggerFactory.getLogger(getClass())
		    .warn("[GDAL Verify] Warning: File might not be fully optimized. COG: " + isCog + ", Overviews: " + hasOverviews);
	}
    }

    private List<URL> filterURLs(List<URL> urls) {
	List<URL> out = new ArrayList<>();
	Set<String> seen = new HashSet<>();

	String currentVar = null;
	boolean keepBlock = false;

	for (URL url : urls) {
	    String fileName = Paths.get(url.getPath()).getFileName().toString();
	    String variable = extractVariable(fileName);

	    if (!variable.equals(currentVar)) {
		// Block switch
		currentVar = variable;
		keepBlock = !seen.contains(variable);
		seen.add(variable);
	    }

	    if (keepBlock) {
		out.add(url);
	    }
	}
	return out;
    }

    private String extractVariable(String fileName) {
	return fileName.split("_")[0];
    }

    //    private void reprojectTo3857(Path in, Path out) throws Exception {
    //	GeoTiffReader reader = null;
    //	GeoTiffWriter writer = null;
    //	GridCoverage2D coverage = null;
    //	GridCoverage2D resampled = null;
    //
    //	try {
    //
    //	    Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
    //	    hints.put(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, CRS.decode("EPSG:4326", true));
    //
    //	    GSLoggerFactory.getLogger(getClass()).info("Opening GeoTIFF: " + in + " size=" + Files.size(in));
    //
    //
    //	    if(!Files.exists(in) || Files.size(in) == 0) {
    //		throw new IOException("Input file missing or empty: " + in);
    //	    }
    //
    //
    //	    System.out.println("GDAL present: " +
    //		    ClassLoader.getSystemResource(
    //			    "it/geosolutions/imageioimpl/plugins/tiff/gdal/GDALMetadataParser.class"
    //		    ));
    //
    //
    //	    try {
    //		reader = new GeoTiffReader(in.toFile(), hints);
    //	    } catch (Throwable t) {
    //		GSLoggerFactory.getLogger(getClass()).warn("GDAL reader failed, retry without GDAL");
    //
    //		System.setProperty("org.geotools.coverage.io.enableGDAL", "false");
    //
    //		reader = new GeoTiffReader(in.toFile(), hints);
    //	    }
    //
    //
    //	    reader = new GeoTiffReader(in.toFile(), hints);
    //	    coverage = reader.read(null);
    //
    //	    // EPSG:4326
    //	    if (coverage.getCoordinateReferenceSystem() == null) {
    //		System.out.println("CRS missing, set predefined EPSG:4326...");
    //		// NB: In una pipeline reale qui andrebbe ricostruita la copertura con il CRS corretto
    //	    }
    //
    //	    CoordinateReferenceSystem targetCRS = CRS.decode(TARGET_EPSG, true);
    //	    resampled = (GridCoverage2D) Operations.DEFAULT.resample(coverage, targetCRS);
    //
    //	    writer = new GeoTiffWriter(out.toFile());
    //	    writer.write(resampled, null);
    //
    //	    //
    //	    writer.dispose();
    //	    writer = null;
    //
    //	    resampled.dispose(true);
    //	    resampled = null;
    //
    //	    coverage.dispose(true);
    //	    coverage = null;
    //
    //	    reader.dispose();
    //	    reader = null;
    //
    //	} finally {
    //	    // clean
    //	    if (writer != null)
    //		try {
    //		    writer.dispose();
    //		} catch (Exception e) {
    //		}
    //	    if (resampled != null)
    //		resampled.dispose(true);
    //	    if (coverage != null)
    //		coverage.dispose(true);
    //	    if (reader != null)
    //		try {
    //		    reader.dispose();
    //		} catch (Exception e) {
    //		}
    //
    //
    //	    // Windows workaround
    //	    System.gc();
    //	    Thread.sleep(500);
    //	}
    //    }

    private String buildRenamedFileName(String o) {
	String base = o.substring(0, o.lastIndexOf('.'));
	String[] p = base.split("_");
	return (p.length < 3) ? base : p[0] + "_" + p[2];
    }

    //    private void runGdalCog(Path in, Path out) throws Exception {
    //	ProcessBuilder pb = new ProcessBuilder("gdal_translate", "-of", "COG", "-co", "COMPRESS=DEFLATE", "-co", "BLOCKSIZE=512",
    //		in.toAbsolutePath().toString(), out.toAbsolutePath().toString());
    //	if (pb.start().waitFor() != 0)
    //	    throw new RuntimeException("GDAL failed");
    //    }

    private void cleanup(Path path) {
	try {
	    if (Files.exists(path)) {
		Files.walk(path).sorted(Comparator.reverseOrder()).forEach(p -> {
		    try {
			Files.delete(p);
		    } catch (IOException e) {
			GSLoggerFactory.getLogger(getClass()).warn("Unable to delete: " + p);
		    }
		});

		GSLoggerFactory.getLogger(getClass()).info("Disk cleanup completed: " + path);
	    }
	} catch (IOException e) {
	    GSLoggerFactory.getLogger(getClass()).error("Cleanup failed: " + e.getMessage());
	}
    }

    private void generateIndexFiles(Path workingDir) throws IOException {
	if (variableFilesMap.isEmpty())
	    return;

	for (Map.Entry<String, List<String>> entry : variableFilesMap.entrySet()) {

	    String var = entry.getKey();
	    List<String> files = entry.getValue();

	    GSLoggerFactory.getLogger(getClass()).info("Generating index for variable: " + var + " (" + files.size() + " files)");

	    if (files.isEmpty())
		continue;

	    Collections.sort(files);
	    Path varDir = workingDir.resolve(var);
	    Files.createDirectories(varDir);

	    generateLegend(varDir, var);

	    Path indexPath = varDir.resolve("index.json");

	    Files.deleteIfExists(indexPath);
	    String json = buildJsonContent(var, files);
	    Files.write(indexPath, json.getBytes());
	}
    }

    private String buildJsonContent(String var, List<String> files) {
	String minDate = formatIsoTime(files.get(0));
	String maxDate = formatIsoTime(files.get(files.size() - 1));

	String baseUrl = String.format("https://s3.us-east-1.amazonaws.com/%s/%s/", bucketName, var);

	StringBuilder json = new StringBuilder();
	json.append("{\n");
	json.append("  \"variable\": \"").append(var).append("\",\n");
	json.append("  \"projection\": \"").append(TARGET_EPSG).append("\",\n");
	json.append("  \"format\": \"COG\",\n");

	json.append("  \"phenomenonTime\": {\n");
	json.append("    \"begin\": \"").append(minDate).append("\",\n");
	json.append("    \"end\": \"").append(maxDate).append("\"\n");
	json.append("  },\n");

	json.append("  \"legend\": \"").append(baseUrl).append("legend.svg\",\n");

	// Dynamic Availability Calculation Block
	json.append("  \"availability\": [\n");
	if (files.size() >= 2) {
	    Instant intervalStart = Instant.parse(formatIsoTime(files.get(0)));
	    Instant currentTrackedTime = intervalStart;
	    long currentStepHours = -1;
	    List<String> availabilityBlocks = new ArrayList<>();

	    for (int i = 1; i < files.size(); i++) {
		Instant nextTime = Instant.parse(formatIsoTime(files.get(i)));
		long calculatedDelta = Duration.between(currentTrackedTime, nextTime).toHours();

		if (currentStepHours == -1) {
		    currentStepHours = calculatedDelta;
		} else if (calculatedDelta != currentStepHours) {
		    // Flush completed range block
		    availabilityBlocks.add(formatAvailabilityBlock(intervalStart, currentTrackedTime, currentStepHours));
		    intervalStart = currentTrackedTime;
		    currentStepHours = calculatedDelta;
		}
		currentTrackedTime = nextTime;
	    }

	    // Flush trailing range block
	    if (currentStepHours != -1) {
		availabilityBlocks.add(formatAvailabilityBlock(intervalStart, currentTrackedTime, currentStepHours));
	    }

	    json.append(String.join(",\n", availabilityBlocks)).append("\n");
	}
	json.append("  ],\n");

	json.append("  \"files\": [\n");

	for (int i = 0; i < files.size(); i++) {
	    String fname = files.get(i);
	    String isoTime = formatIsoTime(fname);

	    String fileUrl = baseUrl + fname;

	    json.append("    {\n");
	    json.append("      \"time\": \"").append(isoTime).append("\",\n");
	    json.append("      \"filename\": \"").append(fname).append("\",\n");
	    json.append("      \"url\": \"").append(fileUrl).append("\"\n");
	    json.append("    }");

	    if (i < files.size() - 1)
		json.append(",");
	    json.append("\n");
	}

	json.append("  ]\n");
	json.append("}");
	return json.toString();
    }

    private String formatAvailabilityBlock(Instant from, Instant to, long stepHours) {
	return "    {\n" +
		"      \"from\": \"" + from.toString() + "\",\n" +
		"      \"to\": \"" + to.toString() + "\",\n" +
		"      \"stepHours\": " + stepHours + "\n" +
		"    }";
    }

    private void syncToS3(Path workingDir) {
	s3.setACLPublicRead(true);
	for (String var : variables) {
	    // Without new COGs the published files are kept: they would be deleted and the missing directory would stop the sync
	    List<String> files = variableFilesMap.getOrDefault(var, List.of());
	    if (files.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).warn("No COG produced for variable " + var + ", the published files are kept");
		report.warn(var + ": no COG produced, the published files are kept");
		continue;
	    }
	    GSLoggerFactory.getLogger(getClass()).info("Uploading variable: " + var);
	    String prefix = var + "/";
	    Path varDir = workingDir.resolve(var);
	    // Clean S3
	    List<S3Object> existing = s3.listObjectSummaries(bucketName, prefix);
	    if (!existing.isEmpty()) {
		List<String> keys = new ArrayList<>();
		existing.forEach(o -> keys.add(o.key()));
		s3.deleteObjects(bucketName, keys);
	    }
	    int uploaded = 0;
	    // Upload files in the var directories
	    try (DirectoryStream<Path> stream = Files.newDirectoryStream(varDir)) {
		for (Path localFile : stream) {
		    if (Files.isRegularFile(localFile)) {
			String fileName = localFile.getFileName().toString();
			String s3Key = prefix + fileName;

			String contentType;

			if (fileName.endsWith(".json")) {
			    contentType = "application/json";
			} else if (fileName.endsWith(".svg")) {
			    contentType = "image/svg+xml";
			} else {
			    contentType = "image/tiff";
			}

			GSLoggerFactory.getLogger(getClass()).info("  - Loading: " + s3Key);

			s3.uploadFile(localFile.toAbsolutePath().toString(), bucketName, s3Key, contentType);
			uploaded++;
		    }
		}
	    } catch (IOException e) {
		GSLoggerFactory.getLogger(getClass()).error("Error S3 for variable " + var + ": " + e.getMessage());
		throw new RuntimeException(e);
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Completed upload for: " + var);
	    report.info(var + ": " + files.size() + " COGs, " + formatIsoTime(files.getFirst()) + " -> " + formatIsoTime(files.getLast())
		    + ", removed " + existing.size() + ", uploaded " + uploaded);

	}
    }

    private void generateLegend(Path varDir, String var) throws IOException {

	Legend legend = LegendFactory.getLegend(var);

	if (legend == null)
	    return; // unknown variable

	String svg = generateLegendSvg(legend);

	Path svgPath = varDir.resolve("legend.svg");

	Files.write(svgPath, svg.getBytes(StandardCharsets.UTF_8));

	GSLoggerFactory.getLogger(getClass()).info("Legend created for " + var);
    }

    public String generateLegendSvg(Legend legend) {

	int width = 320;
	int itemHeight = 22;
	int spacing = 6;
	int margin = 10;

	int height = margin + 30 + legend.items.size() * (itemHeight + spacing);

	StringBuilder svg = new StringBuilder();
	svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	svg.append("<svg xmlns='http://www.w3.org/2000/svg' ").append("width='").append(width).append("' ").append("height='")
		.append(height).append("'>");

	//Background
	svg.append("<rect width='100%' height='100%' fill='#f5f5f5'/>");

	//Title
	svg.append("<text x='10' y='20' font-size='14' font-weight='bold'>").append(escapeXml(legend.title)).append("</text>");

	int y = 40;

	for (LegendItem item : legend.items) {

	    // color box
	    svg.append("<rect x='10' y='").append(y).append("' width='18' height='18' fill='").append(item.color)
		    .append("' stroke='black' stroke-width='0.5'/>");

	    // text
	    svg.append("<text x='35' y='").append(y + 13).append("' font-size='12'>").append(escapeXml(item.range)).append(" (")
		    .append(escapeXml(item.label)).append(")").append("</text>");

	    y += itemHeight + spacing;
	}

	svg.append("</svg>");

	return svg.toString();
    }

    private String escapeXml(String text) {
	return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String formatIsoTime(String filename) {
	// lastIndexOf('_')
	try {
	    String baseName = filename.substring(0, filename.lastIndexOf('.'));
	    String ts = baseName.substring(baseName.lastIndexOf('_') + 1).replaceAll("[^0-9]", "");
	    return String.format("%s-%s-%sT%s:00:00Z", ts.substring(0, 4), ts.substring(4, 6), ts.substring(6, 8), ts.substring(8, 10));
	} catch (Exception e) {
	    return "N/A";
	}
    }

}
