package eu.essi_lab.gssrv.conf.task.trigger.airquality;

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
import eu.essi_lab.gssrv.conf.task.trigger.LegendItem;
import eu.essi_lab.gssrv.conf.task.trigger.TaskReport;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Downloads the latest CAMS European air quality forecast from the Copernicus Atmosphere Data Store, converts every forecast hour of
 * every pollutant into a Cloud Optimized GeoTIFF (EPSG:3857) and publishes them on S3 together with an index.json and a legend.svg per
 * pollutant, using the same layout read by the cog-demo-2 client.<br>
 * Each publication replaces the previous forecast run (rolling forecast).
 */
public class CAMSAirQualityProcessor {

    public static final String DATASET = "cams-europe-air-quality-forecasts";

    /**
     * The 00 UTC run is usually published on ADS in the morning: before this hour the previous day's run is used
     */
    public static final int RUN_AVAILABILITY_HOUR_UTC = 10;

    /**
     * CAMS Europe regional forecasts go up to 96 hours (4 days)
     */
    public static final int MAX_LEAD_HOUR = 96;

    private static final String MODEL = "ensemble";
    private static final String DATA_FORMAT = "netcdf";

    private static final String TARGET_EPSG = "EPSG:3857";
    private static final String NODATA_VALUE = "-9999";

    // Natural Earth 1:50m land polygons clipped to the TRIGGER domain (35W-55E, 20N-80N), shared with COGS3SyncProcessor
    private static final String LAND_MASK_RESOURCE = "trigger/land_mask.geojson";

    /**
     * Quantization of the published COGs: stored value = concentration / VALUE_SCALE, as UInt16. Concentrations above 6553.4 µg/m³ are
     * clamped
     */
    public static final double VALUE_SCALE = 0.1;
    public static final int QUANTIZED_MAX = 65534;
    public static final int QUANTIZED_NODATA = 65535;

    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
	    .withZone(ZoneOffset.UTC);

    private static final Pattern UNITS_SINCE = Pattern.compile("(\\w+)\\s+since\\s+(.+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern UNITS_FROM = Pattern.compile("from\\s+(\\d{4})(\\d{2})(\\d{2})", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_TIME = Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})(?:[ T](\\d{1,2}):(\\d{2})(?::(\\d{2}))?)?");

    private final ADSClient ads;
    private final S3TransferWrapper s3;
    private final String bucketName;
    private final String prefix;
    private final List<CAMSPollutant> pollutants;
    private final LocalDate runDate;
    private final int maxLeadHour;

    private double[] area;
    private Path outputDir;
    private boolean maskSea;
    private Path landMask;
    private TaskReport report = TaskReport.NONE;

    /**
     * @param ads the ADS client
     * @param s3 the S3 wrapper; if null, the products are only written in the output directory (see {@link #setOutputDir(Path)})
     * @param bucketName the target bucket
     * @param prefix the key prefix in the bucket (e.g. "CAMS/"), can be empty
     * @param pollutants the pollutants to process
     * @param runDate the date of the 00 UTC run to download; if null, the latest available run is used
     * @param maxLeadHour the last forecast hour to download (0-96)
     */
    public CAMSAirQualityProcessor(ADSClient ads, S3TransferWrapper s3, String bucketName, String prefix, List<CAMSPollutant> pollutants,
	    LocalDate runDate, int maxLeadHour) {
	this.ads = ads;
	this.s3 = s3;
	this.bucketName = bucketName;
	this.prefix = prefix == null || prefix.isEmpty() || prefix.endsWith("/") ? Objects.toString(prefix, "") : prefix + "/";
	this.pollutants = pollutants;
	this.runDate = runDate;
	this.maxLeadHour = Math.max(0, Math.min(MAX_LEAD_HOUR, maxLeadHour));
    }

    /**
     * Optional sub-area to request, as North, West, South, East. By default the whole CAMS Europe domain (72N, 25W, 30N, 45E) is
     * downloaded
     */
    public void setArea(double[] area) {
	this.area = area;
    }

    /**
     * Local directory where the products are kept. If not set, a temporary directory is used and deleted at the end
     */
    public void setOutputDir(Path outputDir) {
	this.outputDir = outputDir;
    }

    /**
     * If true, the pixels outside the land polygons (sea and ocean) are set to NoData, using the same land mask of the other TRIGGER
     * COGs (see {@link #LAND_MASK_RESOURCE}). By default the sea is kept, since air quality over the sea is meaningful (ship plumes,
     * ozone, desert dust)
     */
    public void setMaskSea(boolean maskSea) {
	this.maskSea = maskSea;
    }

    /**
     * Summary of the execution, written in the task status
     */
    public void setReport(TaskReport report) {
	this.report = report;
    }

    public void execute() throws Exception {

	LocalDate run = runDate != null ? runDate : latestRunDate();
	Instant runInstant = run.atStartOfDay().toInstant(ZoneOffset.UTC);

	boolean keepFiles = outputDir != null;
	Path workDir = keepFiles ? outputDir : Paths.get(System.getProperty("java.io.tmpdir"), "cams_air_quality");

	if (!keepFiles && Files.exists(workDir)) {
	    cleanup(workDir);
	}
	Files.createDirectories(workDir);

	GSLoggerFactory.getLogger(getClass()).info("=== CAMS air quality START ===");
	GSLoggerFactory.getLogger(getClass()).info("Run: " + runInstant + ", lead hours 0-" + maxLeadHour);
	GSLoggerFactory.getLogger(getClass()).info("Pollutants: " + pollutants);
	GSLoggerFactory.getLogger(getClass()).info("Working dir: " + workDir);
	GSLoggerFactory.getLogger(getClass()).info("Sea masked: " + maskSea);

	report.info("Run " + runInstant + ", lead hours 0-" + maxLeadHour + ", pollutants "
		+ String.join(", ", pollutants.stream().map(CAMSPollutant::getKey).toList())
		+ (area != null ? ", area N,W,S,E " + Arrays.toString(area) : "") + ", sea " + (maskSea ? "masked" : "not masked"));

	try {

	    if (maskSea) {
		landMask = extractLandMask(workDir);
	    }

	    // 1. Submission: all the jobs are queued at once on ADS, one per pollutant
	    Map<CAMSPollutant, String> jobs = new LinkedHashMap<>();
	    for (CAMSPollutant pollutant : pollutants) {
		try {
		    jobs.put(pollutant, ads.submit(DATASET, buildRequest(pollutant, run)));
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error("Unable to submit ADS request for " + pollutant.getKey() + ": " + e.getMessage());
		    report.error(pollutant.getKey() + ": unable to submit the ADS request: " + TaskReport.message(e));
		}
	    }

	    // 2. Download, conversion to COG, index and legend generation
	    Map<CAMSPollutant, List<JSONObject>> completed = new LinkedHashMap<>();
	    Map<CAMSPollutant, Long> downloadedBytes = new HashMap<>();
	    for (Map.Entry<CAMSPollutant, String> job : jobs.entrySet()) {

		CAMSPollutant pollutant = job.getKey();
		try {
		    Path pollutantDir = workDir.resolve(pollutant.getKey());
		    Path downloadDir = workDir.resolve("download").resolve(pollutant.getKey());
		    Files.createDirectories(pollutantDir);
		    Files.createDirectories(downloadDir);

		    Path downloaded = downloadDir.resolve(pollutant.getKey() + "_" + run + ".download");
		    ads.waitAndDownload(job.getValue(), downloaded);
		    downloadedBytes.put(pollutant, Files.size(downloaded));

		    List<Path> ncFiles = extractNetCDF(downloaded, downloadDir);
		    List<JSONObject> files = convertToCOGs(ncFiles, pollutant, runInstant, pollutantDir);

		    if (files.isEmpty()) {
			GSLoggerFactory.getLogger(getClass()).warn("No COG generated for " + pollutant.getKey());
			report.warn(pollutant.getKey() + ": no COG generated, the published products are kept");
			continue;
		    }

		    writeLegend(pollutantDir, pollutant.getLegend());
		    writeIndex(pollutantDir, pollutant, runInstant, files);

		    if (!keepFiles) {
			cleanup(downloadDir);
		    }

		    completed.put(pollutant, files);

		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error("Error processing " + pollutant.getKey() + ": " + e.getMessage(), e);
		    report.error(pollutant.getKey() + ": " + TaskReport.message(e) + " - the published products are kept");
		}
	    }

	    // 3. S3 publication
	    for (Map.Entry<CAMSPollutant, List<JSONObject>> entry : completed.entrySet()) {

		CAMSPollutant pollutant = entry.getKey();
		List<JSONObject> files = entry.getValue();
		String summary = pollutant.getKey() + ": downloaded " + downloadedBytes.get(pollutant) / (1024 * 1024) + " MB, " + files.size()
			+ " COGs, " + files.getFirst().getString("time") + " -> " + files.getLast().getString("time");

		if (s3 != null) {
		    SyncResult result = syncToS3(workDir.resolve(pollutant.getKey()), prefix + pollutant.getKey() + "/");
		    summary += ", uploaded " + result.uploaded() + ", removed " + result.removed() + " of the previous run";
		}
		report.info(summary);
	    }

	    GSLoggerFactory.getLogger(getClass()).info("=== CAMS air quality COMPLETED: " + completed.size() + "/" + pollutants.size() + " pollutants ===");
	    report.info("Published " + completed.size() + "/" + pollutants.size() + " pollutants");

	    if (completed.isEmpty()) {
		throw new IOException("No pollutant has been processed");
	    }

	} finally {

	    if (!keepFiles) {
		cleanup(workDir);
	    } else if (landMask != null) {
		Files.deleteIfExists(landMask);
	    }
	    landMask = null;
	}
    }

    /**
     * @return the date of the most recent 00 UTC run expected to be available on ADS
     */
    public static LocalDate latestRunDate() {

	LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
	return now.getHour() >= RUN_AVAILABILITY_HOUR_UTC ? now.toLocalDate() : now.toLocalDate().minusDays(1);
    }

    private JSONObject buildRequest(CAMSPollutant pollutant, LocalDate run) {

	JSONArray leadHours = new JSONArray();
	for (int h = 0; h <= maxLeadHour; h++) {
	    leadHours.put(String.valueOf(h));
	}

	JSONObject request = new JSONObject();
	request.put("variable", new JSONArray().put(pollutant.getAdsVariable()));
	request.put("model", new JSONArray().put(MODEL));
	request.put("level", new JSONArray().put("0"));
	request.put("date", new JSONArray().put(run + "/" + run));
	request.put("type", new JSONArray().put("forecast"));
	request.put("time", new JSONArray().put("00:00"));
	request.put("leadtime_hour", leadHours);
	request.put("data_format", DATA_FORMAT);

	if (area != null) {
	    request.put("area", new JSONArray(area));
	}

	return request;
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
	    GSLoggerFactory.getLogger(getClass()).info("Land mask ready: " + out);
	    return out;
	} catch (IOException e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Unable to extract land mask, sea will not be masked: " + e.getMessage());
	    report.warn("Unable to extract land mask, sea not masked: " + TaskReport.message(e));
	    return null;
	}
    }

    /**
     * ADS may deliver either a plain NetCDF or a zip archive containing one or more NetCDF files
     */
    private List<Path> extractNetCDF(Path downloaded, Path dir) throws IOException {

	byte[] magic = new byte[2];
	try (InputStream in = Files.newInputStream(downloaded)) {
	    if (in.read(magic) < 2) {
		throw new IOException("Empty file downloaded from ADS: " + downloaded);
	    }
	}

	if (magic[0] != 'P' || magic[1] != 'K') {
	    Path nc = dir.resolve(downloaded.getFileName().toString().replace(".download", ".nc"));
	    Files.move(downloaded, nc, StandardCopyOption.REPLACE_EXISTING);
	    return List.of(nc);
	}

	List<Path> out = new ArrayList<>();
	try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(downloaded))) {
	    ZipEntry entry;
	    while ((entry = zip.getNextEntry()) != null) {
		String name = Paths.get(entry.getName()).getFileName().toString();
		if (!entry.isDirectory() && name.toLowerCase().endsWith(".nc")) {
		    Path nc = dir.resolve(name);
		    Files.copy(zip, nc, StandardCopyOption.REPLACE_EXISTING);
		    out.add(nc);
		}
	    }
	}
	Files.deleteIfExists(downloaded);

	GSLoggerFactory.getLogger(getClass()).info("Extracted " + out.size() + " NetCDF files from " + downloaded.getFileName());
	return out;
    }

    private List<JSONObject> convertToCOGs(List<Path> ncFiles, CAMSPollutant pollutant, Instant run, Path pollutantDir) throws Exception {

	String dataset = findDataset(ncFiles, pollutant);
	GSLoggerFactory.getLogger(getClass()).info("Converting " + dataset);

	JSONObject info = new JSONObject(runProcess(List.of("gdalinfo", "-json", dataset)));
	JSONObject globalMeta = info.optJSONObject("metadata", new JSONObject()).optJSONObject("", new JSONObject());
	List<String> georeferencing = buildGeoreferencingOptions(info);

	JSONArray bands = info.getJSONArray("bands");
	GSLoggerFactory.getLogger(getClass()).info("Detected " + bands.length() + " temporal bands for " + pollutant.getKey());

	List<JSONObject> files = new ArrayList<>();
	Set<Instant> seen = new HashSet<>();

	for (int i = 0; i < bands.length(); i++) {

	    JSONObject band = bands.getJSONObject(i);
	    int bandNum = band.getInt("band");
	    JSONObject bandMeta = band.optJSONObject("metadata", new JSONObject()).optJSONObject("", new JSONObject());

	    Instant validTime = validTime(bandMeta, globalMeta, i, run);
	    if (!seen.add(validTime)) {
		GSLoggerFactory.getLogger(getClass()).warn("Skipping band " + bandNum + ": duplicated time " + validTime);
		continue;
	    }

	    String baseName = pollutant.getKey() + "_" + FILE_TIME_FORMATTER.format(validTime);
	    Path vrt = pollutantDir.resolve(baseName + ".vrt");
	    Path warped = pollutantDir.resolve(baseName + "_warp.tif");
	    Path cog = pollutantDir.resolve(baseName + ".tif");

	    try {
		List<String> translate = new ArrayList<>(List.of("gdal_translate", "-of", "VRT", "-b", String.valueOf(bandNum)));
		translate.addAll(georeferencing);
		translate.addAll(List.of(dataset, vrt.toAbsolutePath().toString()));
		runProcess(translate);

		// Concentrations are never negative: bilinear avoids the undershoots of the cubic resampling
		List<String> warp = new ArrayList<>(List.of("gdalwarp", "-overwrite", //
			"-t_srs", TARGET_EPSG, //
			"-r", "bilinear", //
			"-ot", "Float32", //
			"-dstnodata", NODATA_VALUE));

		// Sea mask: pixels outside the land polygons become NoData. CUTLINE_ALL_TOUCHED keeps every pixel touching land,
		// so the coastline is never left uncovered (same options of COGS3SyncProcessor)
		if (landMask != null) {
		    warp.addAll(List.of("-cutline", landMask.toAbsolutePath().toString(), "-wo", "CUTLINE_ALL_TOUCHED=TRUE"));
		}

		warp.addAll(List.of("-of", "GTiff", vrt.toAbsolutePath().toString(), warped.toAbsolutePath().toString()));
		runProcess(warp);

		// Float32 values do not compress (~1.2 MB per hour). Values are quantized to 0.1 µg/m³ and stored as UInt16
		// (value / 0.1, nodata 65535): ~80% smaller files, with the scale written in the metadata for GDAL/QGIS clients.
		// OpenLayers reads the raw integers, so the web client divides by 10 (see valueScale in the index.json)
		runProcess(List.of("gdal_translate", //
			"-ot", "UInt16", //
			"-scale", "0", String.valueOf(QUANTIZED_MAX * VALUE_SCALE), "0", String.valueOf(QUANTIZED_MAX), //
			"-a_nodata", String.valueOf(QUANTIZED_NODATA), //
			"-a_scale", String.valueOf(VALUE_SCALE), //
			"-a_offset", "0", //
			"-of", "COG", //
			"-co", "COMPRESS=DEFLATE", //
			"-co", "PREDICTOR=2", //
			"-co", "BLOCKSIZE=512", //
			"-co", "RESAMPLING=AVERAGE", //
			warped.toAbsolutePath().toString(), //
			cog.toAbsolutePath().toString()));

	    } finally {
		Files.deleteIfExists(vrt);
		Files.deleteIfExists(warped);
	    }

	    JSONObject file = new JSONObject();
	    file.put("time", validTime.toString());
	    file.put("leadTimeHour", (validTime.getEpochSecond() - run.getEpochSecond()) / 3600);
	    file.put("filename", cog.getFileName().toString());
	    files.add(file);
	}

	files.sort(Comparator.comparing(f -> f.getString("time")));
	return files;
    }

    /**
     * Finds the GDAL dataset of the pollutant among the downloaded files: either a NetCDF subdataset (e.g. NETCDF:"file.nc":pm2p5_conc)
     * or the file itself when it contains a single variable
     */
    private String findDataset(List<Path> ncFiles, CAMSPollutant pollutant) throws Exception {

	String varPrefix = pollutant.getNetcdfPrefix().toLowerCase();

	for (Path nc : ncFiles) {

	    String path = nc.toAbsolutePath().toString();
	    JSONObject metadata = new JSONObject(runProcess(List.of("gdalinfo", "-json", path))).optJSONObject("metadata", new JSONObject());

	    JSONObject subdatasets = metadata.optJSONObject("SUBDATASETS");
	    if (subdatasets != null) {
		for (String key : subdatasets.keySet()) {
		    String name = subdatasets.getString(key);
		    if (key.endsWith("_NAME") && name.substring(name.lastIndexOf(':') + 1).toLowerCase().startsWith(varPrefix)) {
			return name;
		    }
		}
		continue;
	    }

	    JSONObject globalMeta = metadata.optJSONObject("", new JSONObject());
	    boolean hasVariable = globalMeta.keySet().stream().anyMatch(k -> k.toLowerCase().startsWith(varPrefix));
	    if (hasVariable || nc.getFileName().toString().toLowerCase().contains(varPrefix) || ncFiles.size() == 1) {
		return "NETCDF:\"" + path + "\"";
	    }
	}

	throw new IOException("Variable " + varPrefix + "* not found in " + ncFiles);
    }

    /**
     * The NetCDF does not always carry a CRS, and CAMS longitudes may be expressed in the 0-360 range: in that case the grid is shifted to
     * -180/180 so that it is correctly warped to EPSG:3857
     */
    private List<String> buildGeoreferencingOptions(JSONObject info) {

	List<String> options = new ArrayList<>(List.of("-a_srs", "EPSG:4326"));

	JSONArray gt = info.optJSONArray("geoTransform");
	JSONArray size = info.optJSONArray("size");
	if (gt == null || size == null) {
	    GSLoggerFactory.getLogger(getClass()).warn("No geotransform found in the NetCDF, the COGs might be misplaced");
	    return options;
	}

	double originX = gt.getDouble(0);
	if (originX >= 180) {

	    double ulx = originX - 360;
	    double uly = gt.getDouble(3);
	    double lrx = ulx + size.getInt(0) * gt.getDouble(1);
	    double lry = uly + size.getInt(1) * gt.getDouble(5);

	    GSLoggerFactory.getLogger(getClass()).info("Shifting longitudes from 0/360 to -180/180: " + ulx + " to " + lrx);
	    options.addAll(List.of("-a_ullr", String.valueOf(ulx), String.valueOf(uly), String.valueOf(lrx), String.valueOf(lry)));
	}

	return options;
    }

    /**
     * Computes the valid time of a band from its NETCDF_DIM_* time coordinate and the related units ("hours since ...", "FORECAST time
     * from yyyyMMdd" or a plain lead time in hours). If the time coordinate cannot be decoded, bands are assumed to follow the requested
     * lead hours
     */
    private Instant validTime(JSONObject bandMeta, JSONObject globalMeta, int bandIndex, Instant run) {

	for (String key : bandMeta.keySet()) {

	    if (!key.startsWith("NETCDF_DIM_")) {
		continue;
	    }

	    String dim = key.substring("NETCDF_DIM_".length());
	    String lowerDim = dim.toLowerCase();
	    if (!lowerDim.contains("time") && !lowerDim.contains("step") && !lowerDim.contains("period")) {
		continue;
	    }

	    try {
		double value = Double.parseDouble(bandMeta.getString(key));
		String units = globalMeta.optString(dim + "#units", "").trim();

		Matcher since = UNITS_SINCE.matcher(units);
		if (since.matches()) {
		    return parseDateTime(since.group(2)).plusSeconds(Math.round(value * unitSeconds(since.group(1))));
		}

		Matcher from = UNITS_FROM.matcher(units);
		if (from.find()) {
		    Instant base = LocalDate.of(Integer.parseInt(from.group(1)), Integer.parseInt(from.group(2)),
			    Integer.parseInt(from.group(3))).atStartOfDay().toInstant(ZoneOffset.UTC);
		    return base.plusSeconds(Math.round(value * 3600));
		}

		long seconds = unitSeconds(units);
		if (seconds > 0) {
		    return run.plusSeconds(Math.round(value * seconds));
		}

	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).warn("Unable to decode time dimension " + dim + ": " + e.getMessage());
	    }
	}

	GSLoggerFactory.getLogger(getClass()).warn("Time coordinate not decoded for band " + (bandIndex + 1) + ", assuming lead hour " + bandIndex);
	return run.plusSeconds(bandIndex * 3600L);
    }

    private static long unitSeconds(String unit) {

	switch (unit.toLowerCase()) {
	case "s":
	case "sec":
	case "second":
	case "seconds":
	    return 1;
	case "min":
	case "minute":
	case "minutes":
	    return 60;
	case "h":
	case "hour":
	case "hours":
	    return 3600;
	case "d":
	case "day":
	case "days":
	    return 86400;
	default:
	    return -1;
	}
    }

    private static Instant parseDateTime(String text) {

	Matcher m = DATE_TIME.matcher(text.trim());
	if (!m.find()) {
	    throw new IllegalArgumentException("Unparsable date: " + text);
	}

	return LocalDateTime.of(//
		Integer.parseInt(m.group(1)), //
		Integer.parseInt(m.group(2)), //
		Integer.parseInt(m.group(3)), //
		m.group(4) == null ? 0 : Integer.parseInt(m.group(4)), //
		m.group(5) == null ? 0 : Integer.parseInt(m.group(5)), //
		m.group(6) == null ? 0 : Integer.parseInt(m.group(6))).toInstant(ZoneOffset.UTC);
    }

    private void writeIndex(Path pollutantDir, CAMSPollutant pollutant, Instant run, List<JSONObject> files) throws IOException {

	String baseUrl = String.format("https://s3.us-east-1.amazonaws.com/%s/%s%s/", bucketName, prefix, pollutant.getKey());

	JSONObject index = new JSONObject();
	index.put("variable", pollutant.getKey());
	index.put("title", pollutant.getTitle());
	index.put("units", "µg/m³");
	index.put("dataType", "UInt16");
	index.put("valueScale", VALUE_SCALE);
	index.put("nodata", QUANTIZED_NODATA);
	index.put("projection", TARGET_EPSG);
	index.put("format", "COG");
	index.put("source", "CAMS European air quality forecasts (" + MODEL + "), Copernicus Atmosphere Monitoring Service");
	index.put("attribution", "Generated using Copernicus Atmosphere Monitoring Service information " + run.toString().substring(0, 4));
	index.put("forecastReferenceTime", run.toString());

	JSONObject phenomenonTime = new JSONObject();
	phenomenonTime.put("begin", files.getFirst().getString("time"));
	phenomenonTime.put("end", files.getLast().getString("time"));
	index.put("phenomenonTime", phenomenonTime);

	index.put("legend", baseUrl + "legend.svg");
	index.put("thresholds", new JSONArray(pollutant.getThresholds()));

	JSONObject availability = new JSONObject();
	availability.put("from", files.getFirst().getString("time"));
	availability.put("to", files.getLast().getString("time"));
	availability.put("stepHours", 1);
	index.put("availability", new JSONArray().put(availability));

	JSONArray filesArray = new JSONArray();
	for (JSONObject file : files) {
	    filesArray.put(new JSONObject(file.toMap()).put("url", baseUrl + file.getString("filename")));
	}
	index.put("files", filesArray);

	Files.writeString(pollutantDir.resolve("index.json"), index.toString(2), StandardCharsets.UTF_8);
    }

    private void writeLegend(Path pollutantDir, Legend legend) throws IOException {

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

	Files.writeString(pollutantDir.resolve("legend.svg"), svg.toString(), StandardCharsets.UTF_8);
    }

    /**
     * Uploads the new run and then removes the objects of the previous run, so that the client never finds an empty folder. The
     * index.json is uploaded last since it references the new COGs
     */
    private SyncResult syncToS3(Path pollutantDir, String keyPrefix) throws IOException {

	GSLoggerFactory.getLogger(getClass()).info("Uploading " + pollutantDir + " to " + bucketName + "/" + keyPrefix);

	s3.setACLPublicRead(true);

	List<S3Object> previous = s3.listObjectSummaries(bucketName, keyPrefix);

	List<Path> localFiles = new ArrayList<>();
	try (DirectoryStream<Path> stream = Files.newDirectoryStream(pollutantDir)) {
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

    private String runProcess(List<String> command) throws Exception {

	ProcessBuilder pb = new ProcessBuilder(command);
	pb.redirectErrorStream(true);
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
     * Local test without S3: downloads the latest run and writes the COGs in the given directory.<br>
     * Usage: CAMSAirQualityProcessor [outputDir] [maxLeadHour] [pollutants] [maskSea], with the ADS personal access token in the ADS_KEY
     * environment variable (e.g. E:/TRIGGER/CAMS 96 pm25,no2 true)
     */
    public static void main(String[] args) throws Exception {

	String key = System.getenv("ADS_KEY");
	if (key == null || key.isBlank()) {
	    System.err.println("Set the ADS_KEY environment variable with your ADS personal access token");
	    return;
	}

	Path outputDir = Paths.get(args.length > 0 ? args[0] : "E:/TRIGGER/CAMS");
	int maxLeadHour = args.length > 1 ? Integer.parseInt(args[1]) : 3;
	List<CAMSPollutant> pollutants = args.length > 2 ? parsePollutants(args[2]) : List.of(CAMSPollutant.values());

	CAMSAirQualityProcessor processor = new CAMSAirQualityProcessor(new ADSClient(ADSClient.DEFAULT_URL, key), null, "trigger", "CAMS/",
		pollutants, null, maxLeadHour);
	processor.setOutputDir(outputDir);
	processor.setMaskSea(args.length > 3 && Boolean.parseBoolean(args[3]));
	processor.execute();
    }

    /**
     * @param keys comma separated pollutant keys (e.g. "pm25,pm10,no2")
     */
    public static List<CAMSPollutant> parsePollutants(String keys) {

	List<CAMSPollutant> out = new ArrayList<>();
	for (String key : keys.split(",")) {
	    CAMSPollutant.fromKey(key).ifPresentOrElse(out::add,
		    () -> GSLoggerFactory.getLogger(CAMSAirQualityProcessor.class).warn("Unknown pollutant: " + key));
	}
	return out;
    }
}
