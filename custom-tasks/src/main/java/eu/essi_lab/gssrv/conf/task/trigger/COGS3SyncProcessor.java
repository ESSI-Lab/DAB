package eu.essi_lab.gssrv.conf.task.trigger;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.*;
import java.util.stream.Collectors;

public class COGS3SyncProcessor {

    private final S3TransferWrapper s3;
    private final String bucketName;
    private final String[] variables;
    private final String sourceURL;
    private Map<String, List<String>> variableFilesMap = new HashMap<>();

    private static final String TARGET_EPSG = "EPSG:3857";

    public COGS3SyncProcessor(S3TransferWrapper s3, String bucketName, String[] variables, String sourceURL) {
	this.s3 = s3;
	this.bucketName = bucketName;
	this.variables = variables;
	this.sourceURL = sourceURL;
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


	try {

	    URL listURL = URI.create(sourceURL).toURL();
	    String user = ConfigurationWrapper.getCredentialsSetting().getTriggerWAFUser().orElse(null);
	    String pass = ConfigurationWrapper.getCredentialsSetting().getTriggerWAFPassword().orElse(null);

	    // 1. Discovery
	    List<URL> allFiles = WAFClient.listFiles(new WAF_URL(listURL), true, user, pass);
	    GSLoggerFactory.getLogger(getClass())
		    .info("Total files discovered: " + allFiles.size());

	    List<URL> tiffUrls = allFiles.stream().filter(url -> url.toString().toLowerCase().contains(".tif"))
		    .collect(Collectors.toList());


	    GSLoggerFactory.getLogger(getClass())
		    .info("GeoTIFF files found: " + tiffUrls.size());

	    List<URL> filteredURLs = filterURLs(tiffUrls);

	    GSLoggerFactory.getLogger(getClass()).info("File to analyze: " + filteredURLs.size());

	    // 2. Download & Process
	    Downloader downloader = new Downloader();
	    for (URL url : filteredURLs) {
		processSingleFile(url, tempWorkDir, downloader, user, pass);
	    }

	    // 2. Index Generation
	    generateIndexFiles(tempWorkDir);

	    // 3. S3 sync
	    syncToS3(tempWorkDir);


	    GSLoggerFactory.getLogger(getClass())
		    .info("=== COG S3 Sync COMPLETED ===");


	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error during the task: " + e.getMessage());
	    throw e;
	} finally {

	    cleanup(tempWorkDir);
	    variableFilesMap = new HashMap<>();
	}
    }

    private void processSingleFile(URL url, Path base, Downloader d, String u, String p) {
	String originalName = Paths.get(url.getPath()).getFileName().toString();
	String var = extractVariable(originalName);
	String finalBaseName = buildRenamedFileName(originalName);

	GSLoggerFactory.getLogger(getClass())
		.info("Processing file: " + originalName + " -> variable=" + var);


	try {
	    Path varFolder = base.resolve(var);
	    Files.createDirectories(varFolder);

	    Path raw = varFolder.resolve(finalBaseName + "_" + System.nanoTime() + ".tif");
	    Path reprojected = varFolder.resolve(finalBaseName + "_" + System.nanoTime() + "_3857.tif");

	    Path finalCog = varFolder.resolve(finalBaseName + ".tif");

	    // Download
	    try (InputStream in = d.downloadResponse(url.toExternalForm(), u, p).body()) {
		Files.copy(in, raw, StandardCopyOption.REPLACE_EXISTING);
	    }

	    //	    // GeoTools Reproject
	    //	    reprojectTo3857(raw, reprojected);
	    //
	    //	    // GDAL COG
	    //	    runGdalCog(reprojected, finalCog);

	    long start = System.currentTimeMillis();

	    runGdalWarpToCOG(raw, finalCog);

	    long end = System.currentTimeMillis();

	    GSLoggerFactory.getLogger(getClass())
		    .info("GDAL completed: " + finalCog.getFileName() +
			    " (" + (end - start) + " ms)");

	    variableFilesMap.computeIfAbsent(var, k -> new ArrayList<>()).add(finalCog.getFileName().toString());

	    // Clean tmp files
	    Files.deleteIfExists(raw);
	    Files.deleteIfExists(reprojected);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error on " + originalName + ": " + e.getMessage());
	}
    }

    private void runGdalWarpToCOG(Path input, Path output) throws Exception {

	ProcessBuilder pb = new ProcessBuilder(
		"gdalwarp",
		"-s_srs", "EPSG:4326",
		"-t_srs", "EPSG:3857",
		"-r", "bilinear",
		"-of", "COG",
		"-co", "COMPRESS=DEFLATE",
		"-co", "BLOCKSIZE=512",
		"-co", "OVERVIEWS=FORCE",
		input.toAbsolutePath().toString(),
		output.toAbsolutePath().toString()
	);

	GSLoggerFactory.getLogger(getClass())
		.info("Running GDAL: " + String.join(" ", pb.command()));

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


	if (!Files.exists(output) || Files.size(output) == 0) {
	    throw new IOException("COG not created: " + output);
	}

	verifyCogFile(output);
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
	    GSLoggerFactory.getLogger(getClass()).warn("[GDAL Verify] Warning: File might not be fully optimized. COG: " + isCog + ", Overviews: " + hasOverviews);
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

	    GSLoggerFactory.getLogger(getClass())
		    .info("Generating index for variable: " + var +
			    " (" + files.size() + " files)");

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

    private void syncToS3(Path workingDir) {
	s3.setACLPublicRead(true);
	for (String var : variables) {
	    GSLoggerFactory.getLogger(getClass())
		    .info("Uploading variable: " + var);
	    String prefix = var + "/";
	    Path varDir = workingDir.resolve(var);
	    // Clean S3
	    List<S3Object> existing = s3.listObjectSummaries(bucketName, prefix);
	    if (!existing.isEmpty()) {
		List<String> keys = new ArrayList<>();
		existing.forEach(o -> keys.add(o.key()));
		s3.deleteObjects(bucketName, keys);
	    }
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
		    }
		}
	    } catch (IOException e) {
		GSLoggerFactory.getLogger(getClass()).info("Error S3 for variable " + var);
		throw new RuntimeException(e);
	    }

	    GSLoggerFactory.getLogger(getClass())
		    .info("Completed upload for: " + var);

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
