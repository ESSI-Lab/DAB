package eu.essi_lab.gssrv.conf.task.trigger;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.oaipmh.ListSetsType;
import eu.essi_lab.lib.net.dirlisting.WAFClient;
import eu.essi_lab.lib.net.dirlisting.WAF_URL;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.Operations;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.referencing.CRS;
import org.geotools.util.factory.Hints;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

public class COGS3SyncProcessor {


    private final S3TransferWrapper s3;
    private final String bucketName;
    private final String[] variables;
    private final String sourceURL;
    private final Map<String, List<String>> variableFilesMap = new HashMap<>();
    private final Path outputBase = Paths.get("output");
    private static final String TARGET_EPSG = "EPSG:3857";

    public COGS3SyncProcessor(S3TransferWrapper s3, String bucketName, String[] variables, String sourceURL) {
	this.s3 = s3;
	this.bucketName = bucketName;
	this.variables = variables;
	this.sourceURL = sourceURL;
    }

    public void execute() throws Exception {
	// 1. Logica di download e processamento GDAL (omessa qui per brevità, è quella già scritta)
	// ... popola la variableFilesMap ...

	Path tempWorkDir = Files.createTempDirectory("geotiff_processing_");
	try {
	    //setupTrustStore();

	    URL listURL = URI.create(sourceURL).toURL();
	    String user = ConfigurationWrapper.getCredentialsSetting().getTriggerWAFUser().orElse(null);
	    String pass = ConfigurationWrapper.getCredentialsSetting().getTriggerWAFPassword().orElse(null);

	    // 1. Discovery
	    List<URL> allFiles = WAFClient.listFiles(new WAF_URL(listURL), true, user, pass);
	    List<URL> tiffUrls = allFiles.stream().filter(url -> url.toString().toLowerCase().contains(".tif")).collect(Collectors.toList());

	    List<URL> filteredURLs = filterURLs(tiffUrls);
	    //filteredURLs.forEach(System.out::println);

	    GSLoggerFactory.getLogger(getClass()).info("File to analyze: " + filteredURLs.size());

	    // 2. Download & Process
	    Downloader downloader = new Downloader();
	    for (URL url : filteredURLs) {
		processSingleFile(url, tempWorkDir, downloader, user, pass);
	    }

	    // 2. Generazione Indici
	    generateIndexFiles();

	    // 3. Sincronizzazione S3
	    //syncToS3();

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error during the task: " + e.getMessage());
	    throw e; // Rilanciamo per far fallire il task ufficialmente
	} finally {
	    // IL CLEANUP: Accade sempre, anche se il programma crasha
	    cleanup(tempWorkDir);
	}
    }

    private void processSingleFile(URL url, Path base, Downloader d, String u, String p) {
	String originalName = Paths.get(url.getPath()).getFileName().toString();
	String var = extractVariable(originalName);
	String finalBaseName = buildRenamedFileName(originalName);

	try {
	    Path varFolder = base.resolve(var);
	    Files.createDirectories(varFolder);

	    Path raw = varFolder.resolve(finalBaseName + ".raw");
	    Path reprojected = varFolder.resolve(finalBaseName + ".3857");
	    Path finalCog = varFolder.resolve(finalBaseName + ".tif");

	    // Download
	    try (InputStream in = d.downloadResponse(url.toExternalForm(), u, p).body()) {
		Files.copy(in, raw, StandardCopyOption.REPLACE_EXISTING);
	    }

	    // GeoTools Reproject
	    reprojectTo3857(raw, reprojected);

	    // GDAL COG
	    runGdalCog(reprojected, finalCog);

	    variableFilesMap.computeIfAbsent(var, k -> new ArrayList<>()).add(finalCog.getFileName().toString());

	    // Pulizia file intermedi immediatamente
	    Files.deleteIfExists(raw);
	    Files.deleteIfExists(reprojected);

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Errore su " + originalName + ": " + e.getMessage());
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


    private void reprojectTo3857(Path in, Path out) throws Exception {
	GeoTiffReader r = null; GeoTiffWriter w = null;
	GridCoverage2D c = null; GridCoverage2D res = null;
	try {
	    Hints h = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
	    h.put(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, CRS.decode("EPSG:4326", true));
	    r = new GeoTiffReader(in.toFile(), h);
	    c = r.read(null);
	    res = (GridCoverage2D) Operations.DEFAULT.resample(c, CRS.decode(TARGET_EPSG, true));
	    w = new GeoTiffWriter(out.toFile());
	    w.write(res, null);
	} finally {
	    if (w != null) w.dispose();
	    if (res != null) res.dispose(true);
	    if (c != null) c.dispose(true);
	    if (r != null) r.dispose();
	    System.gc(); Thread.sleep(300);
	}
    }

    private String buildRenamedFileName(String o) {
	String base = o.substring(0, o.lastIndexOf('.'));
	String[] p = base.split("_");
	return (p.length < 3) ? base : p[0] + "_" + p[2];
    }

    private void runGdalCog(Path in, Path out) throws Exception {
	ProcessBuilder pb = new ProcessBuilder("gdal_translate", "-of", "COG", "-co", "COMPRESS=DEFLATE",
		in.toAbsolutePath().toString(), out.toAbsolutePath().toString());
	if (pb.start().waitFor() != 0) throw new RuntimeException("GDAL failed");
    }

    private void cleanup(Path path) {
	try {
	    if (Files.exists(path)) {
		Files.walk(path)
			.sorted(Comparator.reverseOrder()) // Cancella prima i file, poi le cartelle
			.map(Path::toFile)
			.forEach(File::delete);
		GSLoggerFactory.getLogger(getClass()).info("Disk Cleanup Completed: " + path.getFileName());
	    }
	} catch (IOException e) {
	    GSLoggerFactory.getLogger(getClass()).error("Impossible to clenaup tmp folder: " + e.getMessage());
	}
    }

    private void generateIndexFiles() throws IOException {
	if (variableFilesMap.isEmpty()) return;

	for (Map.Entry<String, List<String>> entry : variableFilesMap.entrySet()) {
	    String var = entry.getKey();
	    List<String> files = entry.getValue();
	    if (files.isEmpty()) continue;

	    Collections.sort(files);
	    Path varDir = outputBase.resolve(var);
	    Files.createDirectories(varDir);
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

	// NUOVO: Aggiunta del range temporale globale
	json.append("  \"phenomenonTime\": {\n");
	json.append("    \"begin\": \"").append(minDate).append("\",\n");
	json.append("    \"end\": \"").append(maxDate).append("\"\n");
	json.append("  },\n");

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

    private void syncToS3() {
	for (String var : variables) {
	    String prefix = var + "/";
	    // Pulizia batch usando i tuoi metodi della classe S3TransferWrapper
	    List<S3Object> existing = s3.listObjectSummaries(bucketName, prefix);
	    if (!existing.isEmpty()) {
		List<String> keys = new ArrayList<>();
		existing.forEach(o -> keys.add(o.key()));
		s3.deleteObjects(bucketName, keys);
	    }
	    // Upload dei file nella cartella varDir...
	    // (Usa s3.uploadFile per ogni file in output/var/)
	}
    }

    private String formatIsoTime(String filename) {
	// Logica basata su lastIndexOf('_') che abbiamo rifinito prima
	try {
	    String baseName = filename.substring(0, filename.lastIndexOf('.'));
	    String ts = baseName.substring(baseName.lastIndexOf('_') + 1).replaceAll("[^0-9]", "");
	    return String.format("%s-%s-%sT%s:00:00Z", ts.substring(0,4), ts.substring(4,6), ts.substring(6,8), ts.substring(8,10));
	} catch (Exception e) { return "N/A"; }
    }
}
