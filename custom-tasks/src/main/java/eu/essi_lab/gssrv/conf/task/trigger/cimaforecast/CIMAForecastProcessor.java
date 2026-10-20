package eu.essi_lab.gssrv.conf.task.trigger.cimaforecast;

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

import eu.essi_lab.gssrv.conf.task.trigger.Legend;
import eu.essi_lab.gssrv.conf.task.trigger.LegendFactory;
import eu.essi_lab.gssrv.conf.task.trigger.LegendItem;
import eu.essi_lab.gssrv.conf.task.trigger.TaskReport;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Publishes on S3 the CIMA WRF operational forecasts (Italy, domain d03, ~2.5 km) of the biometeorological indices UTCI, AT, WBGT and
 * WCT: every forecast hour (+0..+48 h) of the selected run (00 or 12 UTC) is converted into a Cloud Optimized GeoTIFF (EPSG:3857,
 * Float32, NoData -9999, sea optionally masked) and published with an index.json and a legend.svg per index, replacing the previous
 * run.<br>
 * <br>
 * The NetCDF files (rg_wrfita_d03-YYYYMMDDHH00_HH00UTC.nc, ~4 GB, netCDF-4/HDF5) are already regridded by CIMA on a regular lat/lon
 * grid, with 1D lat/lon/time coordinates. To avoid downloading the whole file, each index is read remotely by GDAL with HTTP range
 * requests (HDF5 driver on /vsicurl/); if that fails (e.g. GDAL without HDF5 or curl support) the whole file is downloaded once. The
 * HDF5 driver does not georeference the data, so the extent is computed from the lat/lon coordinates.<br>
 * <br>
 * If the run is not published yet, nothing is changed on S3. Each index is published only if all its hours are converted, otherwise
 * the previous run of that index is kept.
 */
public class CIMAForecastProcessor {

    public static final String DEFAULT_BASE_URL = "http://wrfget.cimafoundation.org/wrf/";

    public static final List<String> INDICES = List.of("utci", "at", "wbgt", "wct");

    /**
     * Publication times of the runs on the CIMA server
     */
    public static final LocalTime RUN_00_AVAILABILITY_UTC = LocalTime.of(3, 15);
    public static final LocalTime RUN_12_AVAILABILITY_UTC = LocalTime.of(15, 15);

    public static final int MAX_LEAD_HOUR = 48;

    public enum DownloadMode {
	/**
	 * Remote reading of the single indices with HTTP range requests, with automatic fallback to {@link #FULL}
	 */
	REMOTE,
	/**
	 * Download of the whole NetCDF file
	 */
	FULL
    }

    private static final String TARGET_EPSG = "EPSG:3857";
    private static final String TARGET_RESOLUTION = "2500";
    private static final String NODATA_VALUE = "-9999";
    private static final String SOURCE_FILL_VALUE = "-8.9999999e+33";

    // Natural Earth 1:50m land polygons clipped to the TRIGGER domain (35W-55E, 20N-80N), shared with COGS3SyncProcessor
    private static final String LAND_MASK_RESOURCE = "trigger/land_mask.geojson";

    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
	    .withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter RUN_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm").withZone(ZoneOffset.UTC);

    private static final Pattern UNITS_SINCE = Pattern.compile("(\\w+)\\s+since\\s+(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_TIME = Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})(?:[ T](\\d{1,2}):(\\d{2})(?::(\\d{2}))?)?");

    private final String user;
    private final String password;
    private final S3TransferWrapper s3;
    private final String bucketName;
    private final String prefix;
    private final List<String> indices;

    private String baseUrl = DEFAULT_BASE_URL;
    private Instant run;
    private int maxLeadHour = MAX_LEAD_HOUR;
    private boolean maskSea = true;
    private DownloadMode downloadMode = DownloadMode.REMOTE;
    private Path outputDir;
    private Path landMask;
    private TaskReport report = TaskReport.NONE;

    /**
     * @param user the CIMA WRF repository user
     * @param password the CIMA WRF repository password
     * @param s3 the S3 wrapper; if null, the products are only written in the output directory (see {@link #setOutputDir(Path)})
     * @param bucketName the target bucket
     * @param prefix the key prefix in the bucket (e.g. "CIMA-FORECAST/"), can be empty
     * @param indices the indices to process, among {@link #INDICES}
     */
    public CIMAForecastProcessor(String user, String password, S3TransferWrapper s3, String bucketName, String prefix,
	    List<String> indices) {
	this.user = user;
	this.password = password;
	this.s3 = s3;
	this.bucketName = bucketName;
	this.prefix = prefix == null || prefix.isEmpty() || prefix.endsWith("/") ? Objects.toString(prefix, "") : prefix + "/";
	this.indices = indices;
    }

    /**
     * The run to process (00 or 12 UTC of a given day). If not set, the latest available run is used
     */
    public void setRun(Instant run) {
	this.run = run;
    }

    public void setBaseUrl(String baseUrl) {
	this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    /**
     * Last forecast hour to publish (0-48), mainly for tests
     */
    public void setMaxLeadHour(int maxLeadHour) {
	this.maxLeadHour = Math.max(0, Math.min(MAX_LEAD_HOUR, maxLeadHour));
    }

    /**
     * If true (default), the pixels outside the land polygons are set to NoData, as for the other CIMA COGs
     */
    public void setMaskSea(boolean maskSea) {
	this.maskSea = maskSea;
    }

    public void setDownloadMode(DownloadMode downloadMode) {
	this.downloadMode = downloadMode;
    }

    /**
     * Local directory where the products are kept. If not set, a temporary directory is used and deleted at the end
     */
    public void setOutputDir(Path outputDir) {
	this.outputDir = outputDir;
    }

    /**
     * Summary of the execution, written in the task status
     */
    public void setReport(TaskReport report) {
	this.report = report;
    }

    /**
     * @return the most recent run expected to be published on the CIMA server at the given time
     */
    public static Instant latestRun(Instant now) {

	LocalDateTime utc = LocalDateTime.ofInstant(now, ZoneOffset.UTC);
	LocalDate today = utc.toLocalDate();
	LocalTime time = utc.toLocalTime();

	if (!time.isBefore(RUN_12_AVAILABILITY_UTC)) {
	    return today.atTime(12, 0).toInstant(ZoneOffset.UTC);
	}
	if (!time.isBefore(RUN_00_AVAILABILITY_UTC)) {
	    return today.atStartOfDay().toInstant(ZoneOffset.UTC);
	}
	return today.minusDays(1).atTime(12, 0).toInstant(ZoneOffset.UTC);
    }

    /**
     * @return the URL of the NetCDF of the given run, e.g. .../wrfita1200/rg_wrfita_d03-202610141200_1200UTC.nc
     */
    public String runUrl(Instant run) {

	String hour = String.format("%02d00", LocalDateTime.ofInstant(run, ZoneOffset.UTC).getHour());
	return baseUrl + "wrfita" + hour + "/rg_wrfita_d03-" + RUN_FORMATTER.format(run) + "_" + hour + "UTC.nc";
    }

    public void execute() throws Exception {

	Instant runInstant = run != null ? run : latestRun(Instant.now());
	String url = runUrl(runInstant);

	GSLoggerFactory.getLogger(getClass()).info("=== CIMA WRF forecast START ===");
	GSLoggerFactory.getLogger(getClass()).info("Run: " + runInstant + ", lead hours 0-" + maxLeadHour + ", indices: " + indices);
	GSLoggerFactory.getLogger(getClass()).info("Source: " + url);

	report.info("Run " + runInstant + ", lead hours 0-" + maxLeadHour + ", indices " + String.join(", ", indices) + ", sea "
		+ (maskSea ? "masked" : "not masked"));

	// 1. If the run is not published yet, the previous products are kept
	int status = head(url);
	if (status != 200) {
	    GSLoggerFactory.getLogger(getClass()).warn("Run not available (HTTP " + status + "), the published products are kept: " + url);
	    report.warn("Run not available (HTTP " + status + "), the published products are kept: " + url);
	    return;
	}

	boolean keepFiles = outputDir != null;
	Path workDir = keepFiles ? outputDir : Paths.get(System.getProperty("java.io.tmpdir"), "cima_wrf_forecast");

	if (!keepFiles && Files.exists(workDir)) {
	    cleanup(workDir);
	}
	Files.createDirectories(workDir);

	try {

	    if (maskSea) {
		landMask = extractLandMask(workDir);
	    }

	    // 2. Source: remote reading with HTTP range requests or local copy of the whole file
	    String source = null;
	    Grid grid = null;

	    if (downloadMode == DownloadMode.REMOTE) {
		try {
		    source = "/vsicurl/" + url;
		    grid = readGrid(source);
		    report.info("Source: remote reading of " + url);
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).warn("Remote reading failed, downloading the whole file: " + e.getMessage());
		    report.warn("Remote reading failed, downloading the whole file: " + TaskReport.message(e));
		    source = null;
		}
	    }

	    if (source == null) {
		Path nc = workDir.resolve("download").resolve(url.substring(url.lastIndexOf('/') + 1));
		long bytes = download(url, nc);
		report.info("Source: downloaded " + url + " (" + bytes / (1024 * 1024) + " MB)");
		source = nc.toAbsolutePath().toString();
		grid = readGrid(source);
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Grid: " + grid);
	    report.info("Grid: " + grid.times().size() + " times from " + grid.times().getFirst());

	    // 3. Conversion: an index is published only if all its hours are converted
	    Map<String, List<JSONObject>> completed = new LinkedHashMap<>();
	    for (String index : indices) {

		Path indexDir = workDir.resolve(index);
		try {
		    Files.createDirectories(indexDir);

		    List<JSONObject> files = convertToCOGs(source, index, grid, runInstant, indexDir);

		    writeLegend(indexDir, index);
		    writeIndex(indexDir, index, runInstant, files);

		    completed.put(index, files);

		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error("Error processing " + index + ", the published products are kept: " + e.getMessage(),
			    e);
		    report.error(index + ": " + TaskReport.message(e) + " - the published products are kept");
		    cleanup(indexDir);
		}
	    }

	    // 4. S3 publication
	    for (Map.Entry<String, List<JSONObject>> entry : completed.entrySet()) {

		String index = entry.getKey();
		List<JSONObject> files = entry.getValue();
		String summary = index + ": " + files.size() + " COGs, " + files.getFirst().getString("time") + " -> "
			+ files.getLast().getString("time");

		if (s3 != null) {
		    SyncResult result = syncToS3(workDir.resolve(index), prefix + index + "/");
		    summary += ", uploaded " + result.uploaded() + ", removed " + result.removed() + " of the previous run";
		}
		report.info(summary);
	    }

	    GSLoggerFactory.getLogger(getClass()).info("=== CIMA WRF forecast COMPLETED: " + completed.size() + "/" + indices.size() + " indices ===");
	    report.info("Published " + completed.size() + "/" + indices.size() + " indices");

	    if (completed.isEmpty()) {
		throw new IOException("No index has been processed");
	    }

	} finally {

	    if (!keepFiles) {
		cleanup(workDir);
	    } else {
		cleanup(workDir.resolve("download"));
		if (landMask != null) {
		    Files.deleteIfExists(landMask);
		}
	    }
	    landMask = null;
	}
    }

    /**
     * Extent and times of the regular lat/lon grid
     */
    private record Grid(double west, double south, double east, double north, List<Instant> times) {

	@Override
	public String toString() {
	    return "W " + west + ", S " + south + ", E " + east + ", N " + north + ", " + times.size() + " times from " + times.getFirst();
	}
    }

    /**
     * Reads the 1D lat/lon/time coordinates and computes the pixel-edge extent of the grid
     */
    private Grid readGrid(String source) throws Exception {

	double[] lat = readValues(source, "lat");
	double[] lon = readValues(source, "lon");
	double[] time = readValues(source, "time");

	if (lat.length < 2 || lon.length < 2 || time.length == 0) {
	    throw new IOException("Unexpected coordinates in " + source);
	}

	// the cell edges are half a step beyond the first and last centers; the step is negative if the coordinate is descending
	double dLon = (lon[lon.length - 1] - lon[0]) / (lon.length - 1);
	double dLat = (lat[lat.length - 1] - lat[0]) / (lat.length - 1);

	// the HDF5 driver reports the variable attributes in the band metadata
	JSONObject timeInfo = new JSONObject(runProcess(List.of("gdalinfo", "-json", hdf5(source, "time"))));
	String units = findString(timeInfo, "time_units").orElse("");

	List<Instant> times = new ArrayList<>();
	for (double value : time) {
	    times.add(toInstant(value, units));
	}

	// the first row of the raster is lat[0]: with ascending latitudes the raster is south-up, and the "upper" edge is the south one
	return new Grid(lon[0] - dLon / 2, lat[0] - dLat / 2, lon[lon.length - 1] + dLon / 2, lat[lat.length - 1] + dLat / 2, times);
    }

    private List<JSONObject> convertToCOGs(String source, String index, Grid grid, Instant runInstant, Path indexDir) throws Exception {

	int bands = Math.min(grid.times().size(), maxLeadHour + 1);

	// 1. Extraction of the index, georeferenced from the lat/lon extent
	Path extracted = indexDir.resolve(index + "_source.tif");

	List<String> extract = new ArrayList<>(List.of("gdal_translate", "-q"));
	for (int b = 1; b <= bands; b++) {
	    extract.addAll(List.of("-b", String.valueOf(b)));
	}
	extract.addAll(List.of("-a_srs", "EPSG:4326", //
		"-a_ullr", String.valueOf(grid.west()), String.valueOf(grid.south()), String.valueOf(grid.east()), String.valueOf(grid.north()), //
		"-a_nodata", SOURCE_FILL_VALUE, //
		hdf5(source, index), extracted.toAbsolutePath().toString()));

	GSLoggerFactory.getLogger(getClass()).info("Extracting " + index + " (" + bands + " hours)");
	runProcess(extract);

	// 2. One COG per hour
	List<JSONObject> files = new ArrayList<>();
	try {
	    for (int b = 1; b <= bands; b++) {

		Instant validTime = grid.times().get(b - 1);
		String baseName = index + "_" + FILE_TIME_FORMATTER.format(validTime);

		Path vrt = indexDir.resolve(baseName + ".vrt");
		Path warped = indexDir.resolve(baseName + "_warp.tif");
		Path cog = indexDir.resolve(baseName + ".tif");

		try {
		    runProcess(List.of("gdal_translate", "-q", "-of", "VRT", "-b", String.valueOf(b), extracted.toAbsolutePath().toString(),
			    vrt.toAbsolutePath().toString()));

		    List<String> warp = new ArrayList<>(List.of("gdalwarp", "-q", "-overwrite", //
			    "-t_srs", TARGET_EPSG, //
			    "-tr", TARGET_RESOLUTION, TARGET_RESOLUTION, "-tap", //
			    "-r", "bilinear", //
			    "-ot", "Float32", //
			    "-dstnodata", NODATA_VALUE));

		    // Sea mask: CUTLINE_ALL_TOUCHED keeps every pixel touching land (same options of COGS3SyncProcessor)
		    if (landMask != null) {
			warp.addAll(List.of("-cutline", landMask.toAbsolutePath().toString(), "-wo", "CUTLINE_ALL_TOUCHED=TRUE"));
		    }
		    warp.addAll(List.of("-of", "GTiff", vrt.toAbsolutePath().toString(), warped.toAbsolutePath().toString()));
		    runProcess(warp);

		    runProcess(List.of("gdal_translate", "-q", //
			    "-of", "COG", //
			    "-co", "COMPRESS=DEFLATE", //
			    "-co", "PREDICTOR=3", //
			    "-co", "BLOCKSIZE=512", //
			    "-co", "RESAMPLING=BILINEAR", //
			    warped.toAbsolutePath().toString(), //
			    cog.toAbsolutePath().toString()));

		} finally {
		    Files.deleteIfExists(vrt);
		    Files.deleteIfExists(warped);
		}

		JSONObject file = new JSONObject();
		file.put("time", validTime.toString());
		file.put("leadTimeHour", (validTime.getEpochSecond() - runInstant.getEpochSecond()) / 3600);
		file.put("filename", cog.getFileName().toString());
		files.add(file);
	    }
	} finally {
	    Files.deleteIfExists(extracted);
	}

	GSLoggerFactory.getLogger(getClass()).info("Converted " + index + ": " + files.size() + " COGs");
	return files;
    }

    /**
     * Reads the values of a 1D variable (e.g. lat) as the third column of the GDAL XYZ output
     */
    private double[] readValues(String source, String variable) throws Exception {

	String xyz = runProcess(List.of("gdal_translate", "-q", "-of", "XYZ", hdf5(source, variable), "/vsistdout/"));

	return Arrays.stream(xyz.split("\\R")) //
		.map(String::trim) //
		.filter(line -> !line.isEmpty()) //
		.mapToDouble(line -> {
		    String[] columns = line.split("\\s+");
		    return Double.parseDouble(columns[columns.length - 1]);
		}).toArray();
    }

    /**
     * Converts a time coordinate value such as 234816 with units "hours since 2000-1-1 00:00:00"
     */
    private static Instant toInstant(double value, String units) {

	Matcher since = UNITS_SINCE.matcher(units.trim());
	if (!since.matches()) {
	    throw new IllegalArgumentException("Unsupported time units: " + units);
	}

	long unitSeconds = switch (since.group(1).toLowerCase()) {
	case "seconds", "second", "s" -> 1;
	case "minutes", "minute", "min" -> 60;
	case "hours", "hour", "h" -> 3600;
	case "days", "day", "d" -> 86400;
	default -> throw new IllegalArgumentException("Unsupported time units: " + units);
	};

	Matcher m = DATE_TIME.matcher(since.group(2).trim());
	if (!m.find()) {
	    throw new IllegalArgumentException("Unsupported time units: " + units);
	}

	Instant reference = LocalDateTime.of(//
		Integer.parseInt(m.group(1)), //
		Integer.parseInt(m.group(2)), //
		Integer.parseInt(m.group(3)), //
		m.group(4) == null ? 0 : Integer.parseInt(m.group(4)), //
		m.group(5) == null ? 0 : Integer.parseInt(m.group(5)), //
		m.group(6) == null ? 0 : Integer.parseInt(m.group(6))).toInstant(ZoneOffset.UTC);

	return reference.plusSeconds(Math.round(value * unitSeconds));
    }

    /**
     * @return the first string value of the given key, searched recursively in the JSON
     */
    private static Optional<String> findString(Object json, String key) {

	if (json instanceof JSONObject object) {
	    if (object.opt(key) instanceof String value) {
		return Optional.of(value);
	    }
	    for (String k : object.keySet()) {
		Optional<String> found = findString(object.get(k), key);
		if (found.isPresent()) {
		    return found;
		}
	    }
	} else if (json instanceof JSONArray array) {
	    for (Object item : array) {
		Optional<String> found = findString(item, key);
		if (found.isPresent()) {
		    return found;
		}
	    }
	}
	return Optional.empty();
    }

    private static String hdf5(String source, String variable) {
	return "HDF5:\"" + source + "\"://" + variable;
    }

    /**
     * @return the HTTP status of a HEAD request to the given URL
     */
    private int head(String url) throws Exception {

	HttpResponse<InputStream> response = newDownloader()
		.downloadResponse(HttpRequestUtils.build(MethodNoBody.HEAD, url, authorizationHeader()));
	response.body().close();
	return response.statusCode();
    }

    /**
     * @return the size of the downloaded file
     */
    private long download(String url, Path target) throws Exception {

	GSLoggerFactory.getLogger(getClass()).info("Downloading " + url);
	Files.createDirectories(target.getParent());

	Downloader downloader = newDownloader();
	downloader.setResponseTimeout(TimeUnit.MINUTES, 10);

	HttpResponse<InputStream> response = downloader.downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url, authorizationHeader()));
	try (InputStream body = response.body()) {
	    if (response.statusCode() != 200) {
		throw new IOException("Download failed with HTTP " + response.statusCode() + ": " + url);
	    }
	    Files.copy(body, target, StandardCopyOption.REPLACE_EXISTING);
	}

	long bytes = Files.size(target);
	GSLoggerFactory.getLogger(getClass()).info("Downloaded " + bytes + " bytes");
	return bytes;
    }

    private Downloader newDownloader() {

	Downloader downloader = new Downloader();
	downloader.setConnectionTimeout(TimeUnit.SECONDS, 30);
	downloader.setResponseTimeout(TimeUnit.MINUTES, 2);
	return downloader;
    }

    /**
     * The basic authorization header is built here instead of using Downloader.downloadResponse(url, user, password), which logs the
     * credentials
     */
    private Map<String, String> authorizationHeader() {

	String token = Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
	return Map.of("Authorization", "Basic " + token);
    }

    private Path extractLandMask(Path workDir) {
	try (InputStream in = getClass().getClassLoader().getResourceAsStream(LAND_MASK_RESOURCE)) {
	    if (in == null) {
		GSLoggerFactory.getLogger(getClass()).warn("Land mask resource not found, sea will not be masked: " + LAND_MASK_RESOURCE);
		report.warn("Land mask resource not found, sea not masked");
		return null;
	    }
	    Path out = workDir.resolve("land_mask.geojson");
	    Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
	    return out;
	} catch (IOException e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Unable to extract land mask, sea will not be masked: " + e.getMessage());
	    report.warn("Unable to extract land mask, sea not masked: " + TaskReport.message(e));
	    return null;
	}
    }

    private void writeIndex(Path indexDir, String index, Instant runInstant, List<JSONObject> files) throws IOException {

	String baseUrl = String.format("https://s3.us-east-1.amazonaws.com/%s/%s%s/", bucketName, prefix, index);
	Legend legend = LegendFactory.getLegend(index);

	JSONObject json = new JSONObject();
	json.put("variable", index);
	json.put("title", legend != null ? legend.title : index);
	json.put("units", "°C");
	json.put("projection", TARGET_EPSG);
	json.put("format", "COG");
	json.put("dataType", "Float32");
	json.put("nodata", Double.parseDouble(NODATA_VALUE));
	json.put("source", "CIMA Foundation, WRF operational forecast, Italy domain d03");
	json.put("forecastReferenceTime", runInstant.toString());

	JSONObject phenomenonTime = new JSONObject();
	phenomenonTime.put("begin", files.getFirst().getString("time"));
	phenomenonTime.put("end", files.getLast().getString("time"));
	json.put("phenomenonTime", phenomenonTime);

	json.put("legend", baseUrl + "legend.svg");

	JSONObject availability = new JSONObject();
	availability.put("from", files.getFirst().getString("time"));
	availability.put("to", files.getLast().getString("time"));
	availability.put("stepHours", 1);
	json.put("availability", new JSONArray().put(availability));

	JSONArray filesArray = new JSONArray();
	for (JSONObject file : files) {
	    filesArray.put(new JSONObject(file.toMap()).put("url", baseUrl + file.getString("filename")));
	}
	json.put("files", filesArray);

	Files.writeString(indexDir.resolve("index.json"), json.toString(2), StandardCharsets.UTF_8);
    }

    private void writeLegend(Path indexDir, String index) throws IOException {

	Legend legend = LegendFactory.getLegend(index);
	if (legend == null) {
	    return;
	}

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

	Files.writeString(indexDir.resolve("legend.svg"), svg.toString(), StandardCharsets.UTF_8);
    }

    /**
     * Uploads the new run and then removes the objects of the previous run, so that the client never finds an empty folder. The
     * index.json is uploaded last since it references the new COGs
     */
    private SyncResult syncToS3(Path indexDir, String keyPrefix) throws IOException {

	GSLoggerFactory.getLogger(getClass()).info("Uploading " + indexDir + " to " + bucketName + "/" + keyPrefix);

	s3.setACLPublicRead(true);

	List<S3Object> previous = s3.listObjectSummaries(bucketName, keyPrefix);

	List<Path> localFiles = new ArrayList<>();
	try (DirectoryStream<Path> stream = Files.newDirectoryStream(indexDir)) {
	    stream.forEach(p -> {
		if (Files.isRegularFile(p)) {
		    localFiles.add(p);
		}
	    });
	}
	localFiles.sort(Comparator.comparing((Path p) -> p.getFileName().toString().equals("index.json")));

	Set<String> uploaded = new HashSet<>();
	for (Path file : localFiles) {

	    String fileName = file.getFileName().toString();
	    String key = keyPrefix + fileName;

	    String contentType;
	    if (fileName.endsWith(".json")) {
		contentType = "application/json";
	    } else if (fileName.endsWith(".svg")) {
		contentType = "image/svg+xml";
	    } else {
		contentType = "image/tiff";
	    }

	    s3.uploadFile(file.toAbsolutePath().toString(), bucketName, key, contentType);
	    uploaded.add(key);
	}

	List<String> stale = previous.stream().map(S3Object::key).filter(k -> !uploaded.contains(k)).toList();
	if (!stale.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).info("Removing " + stale.size() + " objects of the previous run from " + keyPrefix);
	    s3.deleteObjects(bucketName, stale);
	}

	GSLoggerFactory.getLogger(getClass()).info("Uploaded " + uploaded.size() + " files to " + keyPrefix);
	return new SyncResult(uploaded.size(), stale.size());
    }

    /**
     * Number of objects uploaded and of objects of the previous run removed by {@link #syncToS3(Path, String)}
     */
    private record SyncResult(int uploaded, int removed) {
    }

    /**
     * Runs a GDAL command. The repository credentials are given to GDAL (/vsicurl/) through the environment, so that they never appear in
     * the command line and in the logs
     */
    private String runProcess(List<String> command) throws Exception {

	ProcessBuilder pb = new ProcessBuilder(command);
	pb.redirectErrorStream(true);

	Map<String, String> env = pb.environment();
	env.put("GDAL_HTTP_USERPWD", user + ":" + password);
	env.put("GDAL_HTTP_AUTH", "BASIC");
	env.put("CPL_VSIL_CURL_ALLOWED_EXTENSIONS", ".nc");
	env.put("GDAL_DISABLE_READDIR_ON_OPEN", "EMPTY_DIR");
	env.put("GDAL_HTTP_MAX_RETRY", "5");
	env.put("GDAL_HTTP_RETRY_DELAY", "10");

	Process p = pb.start();

	StringBuilder output = new StringBuilder();
	try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
	    String line;
	    while ((line = reader.readLine()) != null) {
		output.append(line).append("\n");
	    }
	}

	if (p.waitFor() != 0) {
	    throw new IOException("Command failed: " + String.join(" ", command) + "\n" + output);
	}

	// gdalinfo -json may print warnings before the JSON document
	int jsonStart = output.indexOf("{");
	return command.contains("-json") && jsonStart > 0 ? output.substring(jsonStart) : output.toString();
    }

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
	    }
	} catch (IOException e) {
	    GSLoggerFactory.getLogger(getClass()).error("Cleanup failed: " + e.getMessage());
	}
    }

    private static String escapeXml(String text) {
	return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * Local test without S3: converts a run and writes the COGs in the given directory.<br>
     * Usage: CIMAForecastProcessor [outputDir] [maxLeadHour] [run yyyyMMddHH] [remote|full] [maskSea], with the repository credentials in
     * the CIMA_USER and CIMA_PASSWORD environment variables (e.g. E:/TRIGGER/CIMA-FORECAST 12 2026101500)
     */
    public static void main(String[] args) throws Exception {

	String user = System.getenv("CIMA_USER");
	String password = System.getenv("CIMA_PASSWORD");
	if (user == null || password == null) {
	    System.err.println("Set the CIMA_USER and CIMA_PASSWORD environment variables");
	    return;
	}

	CIMAForecastProcessor processor = new CIMAForecastProcessor(user, password, null, "s3-demo-geotiff", "CIMA/forecast", INDICES);
	processor.setOutputDir(Paths.get(args.length > 0 ? args[0] : "E:/TRIGGER/CIMA-FORECAST"));
	processor.setMaxLeadHour(args.length > 1 ? Integer.parseInt(args[1]) : 3);
	if (args.length > 2) {
	    processor.setRun(parseRun(args[2]));
	}
	if (args.length > 3) {
	    processor.setDownloadMode(DownloadMode.valueOf(args[3].toUpperCase()));
	}
	processor.setMaskSea(args.length <= 4 || Boolean.parseBoolean(args[4]));
	processor.execute();
    }

    /**
     * @param run the run as yyyyMMddHH, e.g. 2026101412
     */
    public static Instant parseRun(String run) {
	return LocalDateTime.parse(run, DateTimeFormatter.ofPattern("yyyyMMddHH")).toInstant(ZoneOffset.UTC);
    }
}
