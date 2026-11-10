package eu.essi_lab.accessor.adhi;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Client of ADHI, the African Database of Hydrometric Indices (Tramblay et al., 2021), published as the Dataverse dataset
 * doi:10.23708/LXGXQ9 on the IRD Dataverse (https://dataverse.ird.fr).<br>
 * The dataset is static (1950-2018): the station list is a CSV file and the time series are text files (one per station) contained
 * in zip files, see {@link ADHIVariable}. The identifiers of the files change with the versions of the dataset, so they are resolved
 * by name from the description of the latest version of the dataset. The zip files are cached in the temporary directory and
 * downloaded again only when their checksum changes
 *
 * @author boldrini
 */
public class ADHIClient {

    public static final String DEFAULT_ENDPOINT = "https://dataverse.ird.fr/dataset.xhtml?persistentId=doi:10.23708/LXGXQ9";

    public static final String DEFAULT_PERSISTENT_ID = "doi:10.23708/LXGXQ9";

    public static final String DATASET_DOI_URL = "https://doi.org/10.23708/LXGXQ9";

    static final String STATIONS_FILE = "ADHI_stations.csv";

    /**
     * Identifiers of the files in version 6.1 of the dataset, used if the dataset description is not available or does not list a
     * file
     */
    static final Map<String, Long> KNOWN_FILE_IDS = new HashMap<>();

    static {
	KNOWN_FILE_IDS.put(STATIONS_FILE, 4944L);
	KNOWN_FILE_IDS.put(ADHIVariable.MONTHLY_FILE, 4916L);
	KNOWN_FILE_IDS.put(ADHIVariable.ANNUAL_MEAN_FILE, 4919L);
	KNOWN_FILE_IDS.put(ADHIVariable.ANNUAL_MAX_FILE, 4921L);
	KNOWN_FILE_IDS.put(ADHIVariable.ANNUAL_7DAY_MIN_FILE, 4918L);
	KNOWN_FILE_IDS.put(ADHIVariable.ANNUAL_PERCENTILES_FILE, 4917L);
    }

    /**
     * Time after which the description of the dataset kept in memory is requested again
     */
    static final long DATASET_INFO_TTL = TimeUnit.HOURS.toMillis(12);

    static final String CACHE_FOLDER = "adhi";

    static final String MISSING_VALUE = "NaN";

    // columns of the station list
    public static final String STATION_ID = "ID";
    public static final String STATION_CODE = "Station_co";
    public static final String STATION_NAME = "Name";
    public static final String DATA_SOURCE = "Data_sourc";
    public static final String CATCHMENT_AREA = "Catchment";
    public static final String MEAN_ALTITUDE = "Mean_altit";
    public static final String START_YEAR = "Starting_y";
    public static final String END_YEAR = "Ending_yea";
    public static final String NUMBER_OF_DAMS = "Number_dams";
    public static final String COUNTRY = "Country";
    public static final String LON = "Longitude";
    public static final String LAT = "Latitude";
    public static final String FLAG = "Flag";
    public static final String COMMENT = "Comment";

    // keys added by the client and by the connector
    public static final String SERIES_ID = "seriesId";
    public static final String VARIABLE = "variable";
    public static final String BEGIN = "begin";
    public static final String END = "end";
    public static final String VALUES = "values";
    public static final String DATASET_VERSION = "datasetVersion";
    public static final String LICENSE = "license";

    private static final String SERIES_ID_SEPARATOR = ":";

    private static final Map<String, DatasetInfo> DATASET_INFO_CACHE = new ConcurrentHashMap<>();

    private static final Object CACHE_LOCK = new Object();

    private final String endpoint;
    private final String baseURL;
    private final String persistentId;
    private final Downloader downloader;

    /**
     *
     */
    public ADHIClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint the dataset page (e.g. {@link #DEFAULT_ENDPOINT}) or the Dataverse base URL
     */
    public ADHIClient(String endpoint) {

	this.endpoint = endpoint == null || endpoint.isBlank() ? DEFAULT_ENDPOINT : endpoint.trim();
	this.baseURL = getBaseURL(this.endpoint);
	this.persistentId = getPersistentId(this.endpoint);
	this.downloader = new Downloader();
	this.downloader.setConnectionTimeout(TimeUnit.SECONDS, 30);
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 10);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    /**
     * A file of the dataset
     */
    public static class DataFile {

	private final long id;
	private final String name;
	private final String md5;

	/**
	 * @param id
	 * @param name
	 * @param md5 the MD5 checksum, or null if not known
	 */
	DataFile(long id, String name, String md5) {

	    this.id = id;
	    this.name = name;
	    this.md5 = md5;
	}

	/**
	 * @return
	 */
	public long getId() {

	    return id;
	}

	/**
	 * @return
	 */
	public String getName() {

	    return name;
	}

	/**
	 * @return the MD5 checksum, or null if not known
	 */
	public String getMd5() {

	    return md5;
	}
    }

    /**
     * The description of the latest version of the dataset
     */
    public static class DatasetInfo {

	private String version;
	private String license;
	private final Map<String, DataFile> files = new LinkedHashMap<>();
	private long time;

	/**
	 * @return e.g. "6.1", or null if not known
	 */
	public String getVersion() {

	    return version;
	}

	/**
	 * @return the license statement, or null if not known
	 */
	public String getLicense() {

	    return license;
	}

	/**
	 * @param name e.g. "ADHI_MonthlySeries.zip"
	 * @return the file with the given name (or with the given original name), or the known file of version 6.1
	 */
	public Optional<DataFile> getFile(String name) {

	    DataFile file = files.get(name);
	    if (file != null) {
		return Optional.of(file);
	    }

	    // e.g. ADHI_stations.csv is ingested by Dataverse as ADHI_stations.tab
	    String baseName = getBaseName(name);
	    for (DataFile f : files.values()) {
		if (getBaseName(f.getName()).equals(baseName)) {
		    return Optional.of(f);
		}
	    }

	    return Optional.ofNullable(KNOWN_FILE_IDS.get(name)).map(id -> new DataFile(id, name, null));
	}

	/**
	 * @return the names of the files of the dataset
	 */
	public Collection<String> getFileNames() {

	    return files.keySet();
	}
    }

    /**
     * @param json the response of the Dataverse API api/datasets/:persistentId/
     * @return
     */
    static DatasetInfo parseDatasetInfo(JSONObject json) {

	DatasetInfo ret = new DatasetInfo();

	JSONObject version = json.optJSONObject("data", new JSONObject()).optJSONObject("latestVersion", new JSONObject());

	if (version.has("versionNumber")) {
	    ret.version = version.optInt("versionNumber") + "." + version.optInt("versionMinorNumber", 0);
	}

	ret.license = parseLicense(version);

	JSONArray files = version.optJSONArray("files", new JSONArray());
	for (int i = 0; i < files.length(); i++) {

	    JSONObject dataFile = files.getJSONObject(i).optJSONObject("dataFile");
	    if (dataFile == null || !dataFile.has("id")) {
		continue;
	    }

	    String md5 = dataFile.optJSONObject("checksum", new JSONObject()).optString("value", dataFile.optString("md5", ""));
	    if (md5.isEmpty() || !dataFile.optJSONObject("checksum", new JSONObject()).optString("type", "MD5").equals("MD5")) {
		md5 = null;
	    }

	    // the original name is the name of the uploaded file, before the ingestion of tabular data
	    String name = dataFile.optString("originalFileName", dataFile.optString("filename", "")).trim();
	    if (!name.isEmpty()) {
		ret.files.put(name, new DataFile(dataFile.getLong("id"), name, md5));
	    }
	}

	return ret;
    }

    /**
     * @param version
     * @return
     */
    private static String parseLicense(JSONObject version) {

	Object license = version.opt("license");

	if (license instanceof JSONObject) {
	    JSONObject object = (JSONObject) license;
	    String name = object.optString("name", "");
	    String uri = object.optString("uri", "");
	    return uri.isEmpty() ? name : name + " - " + uri;
	}

	if (license instanceof String && !((String) license).equals("NONE")) {
	    return (String) license;
	}

	String terms = version.optString("termsOfUse", "");
	if (terms.contains("creativecommons.org/licenses/by/4.0")) {
	    return ADHIMapper.CC_BY_4;
	}

	terms = terms.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
	return terms.isEmpty() ? null : terms;
    }

    /**
     * @return the description of the latest version of the dataset (kept in memory for {@link #DATASET_INFO_TTL})
     * @throws GSException
     */
    public DatasetInfo retrieveDatasetInfo() throws GSException {

	String key = baseURL + persistentId;

	DatasetInfo cached = DATASET_INFO_CACHE.get(key);
	if (cached != null && System.currentTimeMillis() - cached.time < DATASET_INFO_TTL) {
	    return cached;
	}

	String url = baseURL + "api/datasets/:persistentId/?persistentId=" + URLEncoder.encode(persistentId, StandardCharsets.UTF_8);

	try {

	    DatasetInfo info = parseDatasetInfo(new JSONObject(get(url)));
	    info.time = System.currentTimeMillis();

	    GSLoggerFactory.getLogger(getClass()).info("ADHI dataset {} version {} with {} files", persistentId, info.getVersion(),
		    info.getFileNames().size());

	    DATASET_INFO_CACHE.put(key, info);

	    return info;

	} catch (Exception e) {

	    if (cached != null) {
		GSLoggerFactory.getLogger(getClass()).warn("Using the previous description of the ADHI dataset: {}", e.getMessage());
		return cached;
	    }

	    GSLoggerFactory.getLogger(getClass()).warn("Using the known ADHI file identifiers: {}", e.getMessage());
	    // only the known files
	    DatasetInfo info = new DatasetInfo();
	    info.time = System.currentTimeMillis();
	    return info;
	}
    }

    /**
     * @param info
     * @return the stations, in the order of the station list
     * @throws GSException
     */
    public List<JSONObject> retrieveStations(DatasetInfo info) throws GSException {

	File file = getFile(info, STATIONS_FILE);

	try {

	    return parseStations(Files.readString(file.toPath(), StandardCharsets.UTF_8));

	} catch (IOException e) {

	    throw createException("Unable to read the ADHI station list", e);
	}
    }

    /**
     * @param csv the station list, with a header line
     * @return
     */
    static List<JSONObject> parseStations(String csv) {

	List<JSONObject> ret = new ArrayList<>();

	try (BufferedReader reader = new BufferedReader(new StringReader(csv))) {

	    String headerLine = reader.readLine();
	    if (headerLine == null) {
		return ret;
	    }

	    String[] header = headerLine.replace("﻿", "").split(",", -1);

	    String line;
	    while ((line = reader.readLine()) != null) {

		if (line.isBlank()) {
		    continue;
		}

		String[] split = line.split(",", -1);

		JSONObject station = new JSONObject();
		for (int i = 0; i < Math.min(header.length, split.length); i++) {
		    String value = repairEncoding(split[i].trim());
		    if (!value.isEmpty() && !value.equals(MISSING_VALUE)) {
			station.put(header[i].trim(), value);
		    }
		}

		if (station.has(STATION_ID)) {
		    ret.add(station);
		}
	    }

	} catch (IOException e) {
	    // not possible with a string reader
	    GSLoggerFactory.getLogger(ADHIClient.class).error(e);
	}

	return ret;
    }

    /**
     * Some names of the station list are encoded twice in UTF-8 (e.g. "TinÃ© a Pont" instead of "Tiné a Pont")
     *
     * @param value
     * @return
     */
    static String repairEncoding(String value) {

	if (value.indexOf('Ã') < 0 && value.indexOf('Â') < 0) {
	    return value;
	}

	try {

	    return StandardCharsets.UTF_8.newDecoder().//
		    onMalformedInput(CodingErrorAction.REPORT).//
		    onUnmappableCharacter(CodingErrorAction.REPORT).//
		    decode(ByteBuffer.wrap(value.getBytes(StandardCharsets.ISO_8859_1))).//
		    toString();

	} catch (CharacterCodingException e) {

	    return value;
	}
    }

    /**
     * Summary of a series: number of valid values, time of the first and of the last valid value
     */
    public static class SeriesSummary {

	private int values;
	private Date first;
	private Date last;

	/**
	 * @param date
	 */
	void add(Date date) {

	    if (first == null || date.before(first)) {
		first = date;
	    }
	    if (last == null || date.after(last)) {
		last = date;
	    }
	    values++;
	}

	/**
	 * @return
	 */
	public int getValues() {

	    return values;
	}

	/**
	 * @return
	 */
	public Date getFirst() {

	    return first;
	}

	/**
	 * @return
	 */
	public Date getLast() {

	    return last;
	}
    }

    /**
     * Reads all the station files of the given variables (each zip file is read once)
     *
     * @param info
     * @param variables
     * @return the summaries of the series with at least one valid value, by variable and station identifier
     * @throws GSException
     */
    public Map<ADHIVariable, Map<String, SeriesSummary>> retrieveSummaries(DatasetInfo info, Collection<ADHIVariable> variables)
	    throws GSException {

	Map<ADHIVariable, Map<String, SeriesSummary>> ret = new LinkedHashMap<>();
	Map<String, List<ADHIVariable>> byFile = new LinkedHashMap<>();

	for (ADHIVariable variable : variables) {
	    ret.put(variable, new HashMap<>());
	    byFile.computeIfAbsent(variable.getFileName(), f -> new ArrayList<>()).add(variable);
	}

	for (Map.Entry<String, List<ADHIVariable>> entry : byFile.entrySet()) {

	    File file = getFile(info, entry.getKey());

	    try (ZipFile zip = new ZipFile(file)) {

		Enumeration<? extends ZipEntry> entries = zip.entries();
		while (entries.hasMoreElements()) {

		    ZipEntry zipEntry = entries.nextElement();
		    Optional<String> stationId = getStationId(zipEntry.getName());
		    if (zipEntry.isDirectory() || stationId.isEmpty()) {
			continue;
		    }

		    String text;
		    try (InputStream stream = zip.getInputStream(zipEntry)) {
			text = IOUtils.toString(stream, StandardCharsets.UTF_8);
		    }

		    for (ADHIVariable variable : entry.getValue()) {

			SeriesSummary summary = new SeriesSummary();
			parseSeries(text, variable).keySet().forEach(summary::add);

			if (summary.getValues() > 0) {
			    ret.get(variable).put(stationId.get(), summary);
			}
		    }
		}

	    } catch (IOException e) {

		throw createException("Unable to read ADHI file " + entry.getKey(), e);
	    }
	}

	return ret;
    }

    /**
     * @param info
     * @param stationId e.g. "ADHI_1"
     * @param variable
     * @param begin the begin of the period, or null
     * @param end the end of the period, or null
     * @return the valid values of the given period
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(DatasetInfo info, String stationId, ADHIVariable variable, Date begin,
	    Date end) throws GSException {

	File file = getFile(info, variable.getFileName());

	try (ZipFile zip = new ZipFile(file)) {

	    Enumeration<? extends ZipEntry> entries = zip.entries();
	    while (entries.hasMoreElements()) {

		ZipEntry zipEntry = entries.nextElement();
		if (zipEntry.isDirectory() || !getStationId(zipEntry.getName()).filter(stationId::equals).isPresent()) {
		    continue;
		}

		try (InputStream stream = zip.getInputStream(zipEntry)) {

		    TreeMap<Date, BigDecimal> values = parseSeries(IOUtils.toString(stream, StandardCharsets.UTF_8), variable);

		    Date from = begin == null ? new Date(Long.MIN_VALUE) : begin;
		    Date to = end == null ? new Date(Long.MAX_VALUE) : end;

		    return from.after(to) ? new TreeMap<>() : new TreeMap<>(values.subMap(from, true, to, true));
		}
	    }

	} catch (IOException e) {

	    throw createException("Unable to read ADHI file " + variable.getFileName(), e);
	}

	GSLoggerFactory.getLogger(getClass()).warn("No {} file for station {}", variable.getFileName(), stationId);

	return new TreeMap<>();
    }

    /**
     * @param entryName e.g. "MonthlySeries/monthly_ADHI_1.txt"
     * @return the station identifier, e.g. "ADHI_1"
     */
    static Optional<String> getStationId(String entryName) {

	String name = entryName.substring(entryName.lastIndexOf('/') + 1);
	int index = name.indexOf("ADHI_");

	if (index < 0 || !name.endsWith(".txt")) {
	    return Optional.empty();
	}

	return Optional.of(name.substring(index, name.length() - 4));
    }

    /**
     * @param text the content of a station file
     * @param variable
     * @return the valid values, at the beginning of the month (monthly series) or of the year (annual series), UTC
     */
    static TreeMap<Date, BigDecimal> parseSeries(String text, ADHIVariable variable) {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	for (String line : text.split("\\r?\\n")) {

	    String[] split = line.trim().split(",", -1);
	    if (split.length <= variable.getColumn()) {
		continue;
	    }

	    try {

		int year = Integer.parseInt(split[0].trim());
		int month = variable.isMonthly() ? Integer.parseInt(split[1].trim()) : 1;

		String value = split[variable.getColumn()].trim();
		if (value.isEmpty() || value.equalsIgnoreCase(MISSING_VALUE) || value.equalsIgnoreCase("NA")) {
		    continue;
		}

		Date date = Date.from(LocalDate.of(year, month, 1).atStartOfDay(ZoneOffset.UTC).toInstant());
		ret.put(date, new BigDecimal(value));

	    } catch (RuntimeException e) {
		// invalid row
	    }
	}

	return ret;
    }

    /**
     * Returns the local copy of a file of the dataset, downloading it if it is not in the cache (in the temporary directory) or if its
     * checksum changed
     *
     * @param info
     * @param name
     * @return
     * @throws GSException
     */
    File getFile(DatasetInfo info, String name) throws GSException {

	DataFile dataFile = info.getFile(name).orElseThrow(() -> createException("ADHI file " + name + " not found", null));

	Path folder = Path.of(System.getProperty("java.io.tmpdir"), CACHE_FOLDER);

	String version = dataFile.getMd5() != null ? dataFile.getMd5() : "v" + Optional.ofNullable(info.getVersion()).orElse("");
	Path target = folder.resolve(dataFile.getId() + "_" + version + "_" + name);

	synchronized (CACHE_LOCK) {

	    if (Files.isRegularFile(target)) {
		try {
		    if (Files.size(target) > 0) {
			return target.toFile();
		    }
		} catch (IOException e) {
		    GSLoggerFactory.getLogger(getClass()).warn("Unable to check cached file {}", target);
		}
	    }

	    if (dataFile.getMd5() == null) {
		// the checksum is not known (dataset description not available): any cached copy of the file is used
		File[] cached = folder.toFile().listFiles((dir, n) -> n.startsWith(dataFile.getId() + "_") && n.endsWith("_" + name));
		if (cached != null && cached.length > 0 && cached[0].length() > 0) {
		    return cached[0];
		}
	    }

	    String url = baseURL + "api/access/datafile/" + dataFile.getId() + "?format=original";

	    GSLoggerFactory.getLogger(getClass()).info("Downloading ADHI file {} from {}", name, url);

	    Path temp = null;

	    try {

		Files.createDirectories(folder);
		temp = Files.createTempFile(folder, name, ".tmp");

		HttpResponse<InputStream> response = downloader.downloadResponse(url);

		MessageDigest digest = MessageDigest.getInstance("MD5");

		try (InputStream stream = response.body()) {

		    if (response.statusCode() != 200) {
			throw createException("ADHI file " + url + " returned HTTP " + response.statusCode(), null);
		    }

		    Files.copy(new DigestInputStream(stream, digest), temp, StandardCopyOption.REPLACE_EXISTING);
		}

		String md5 = toHex(digest.digest());
		if (dataFile.getMd5() != null && !dataFile.getMd5().equalsIgnoreCase(md5)) {
		    throw createException("Checksum mismatch of ADHI file " + name, null);
		}

		Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

		deleteOldVersions(folder, target, name);

		GSLoggerFactory.getLogger(getClass()).info("ADHI file {} downloaded ({} bytes)", name, Files.size(target));

		return target.toFile();

	    } catch (GSException e) {

		throw e;

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e);
		throw createException("ADHI file " + url + " request failed", e);

	    } finally {

		if (temp != null) {
		    try {
			Files.deleteIfExists(temp);
		    } catch (IOException e) {
			GSLoggerFactory.getLogger(getClass()).warn("Unable to delete {}", temp);
		    }
		}
	    }
	}
    }

    /**
     * @param folder
     * @param current
     * @param name
     */
    private void deleteOldVersions(Path folder, Path current, String name) {

	File[] files = folder.toFile().listFiles((dir, n) -> n.endsWith("_" + name) && !n.equals(current.getFileName().toString()));
	if (files == null) {
	    return;
	}

	for (File file : files) {
	    if (!file.delete()) {
		GSLoggerFactory.getLogger(getClass()).warn("Unable to delete old ADHI file {}", file);
	    }
	}
    }

    /**
     * @param bytes
     * @return
     */
    private static String toHex(byte[] bytes) {

	StringBuilder builder = new StringBuilder();
	for (byte b : bytes) {
	    builder.append(String.format("%02x", b));
	}
	return builder.toString();
    }

    /**
     * @param stationId
     * @param variable
     * @return e.g. "ADHI_1:MONTHLY_MEAN"
     */
    static String buildSeriesId(String stationId, ADHIVariable variable) {

	return stationId + SERIES_ID_SEPARATOR + variable.name();
    }

    /**
     * @param seriesId
     * @return the series identified by the given identifier, or null if not valid
     */
    public static ADHIEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	int index = seriesId.lastIndexOf(SERIES_ID_SEPARATOR);
	if (index <= 0) {
	    return null;
	}

	String stationId = seriesId.substring(0, index);
	Optional<ADHIVariable> variable = ADHIVariable.decode(seriesId.substring(index + 1));
	if (variable.isEmpty()) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, buildSeriesId(stationId, variable.get()));
	object.put(STATION_ID, stationId);
	object.put(VARIABLE, variable.get().name());

	return new ADHIEntity(object);
    }

    /**
     * @param station a station of the station list
     * @param variable
     * @param summary
     * @param info
     * @return
     */
    static ADHIEntity createSeries(JSONObject station, ADHIVariable variable, SeriesSummary summary, DatasetInfo info) {

	JSONObject object = new JSONObject(station.toString());

	object.put(SERIES_ID, buildSeriesId(station.getString(STATION_ID), variable));
	object.put(VARIABLE, variable.name());
	object.put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(summary.getFirst()));
	object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(summary.getLast()));
	object.put(VALUES, summary.getValues());

	if (info.getVersion() != null) {
	    object.put(DATASET_VERSION, info.getVersion());
	}
	if (info.getLicense() != null) {
	    object.put(LICENSE, info.getLicense());
	}

	return new ADHIEntity(object);
    }

    /**
     * @param url
     * @return
     * @throws GSException
     */
    private String get(String url) throws GSException {

	try {

	    HttpResponse<InputStream> response = downloader.downloadResponse(url);

	    String text;
	    try (InputStream stream = response.body()) {
		text = stream == null ? "" : IOUtils.toString(new InputStreamReader(stream, StandardCharsets.UTF_8));
	    }

	    if (response.statusCode() != 200) {
		throw createException("ADHI " + url + " returned HTTP " + response.statusCode(), null);
	    }

	    return text;

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("ADHI " + url + " request failed", e);
	}
    }

    /**
     * @param message
     * @param cause
     * @return
     */
    private GSException createException(String message, Exception cause) {

	GSLoggerFactory.getLogger(getClass()).error(message);

	// the overload taking only the cause uses its message as description, and fails when there is no cause
	return GSException.createException(//
		getClass(), //
		message, //
		null, //
		ErrorInfo.ERRORTYPE_SERVICE, //
		ErrorInfo.SEVERITY_ERROR, //
		getClass().getSimpleName() + "_ERROR", //
		cause);
    }

    /**
     * @param endpoint e.g. "https://dataverse.ird.fr/dataset.xhtml?persistentId=doi:10.23708/LXGXQ9"
     * @return e.g. "https://dataverse.ird.fr/"
     */
    static String getBaseURL(String endpoint) {

	int schemeIndex = endpoint.indexOf("://");
	int pathIndex = schemeIndex < 0 ? -1 : endpoint.indexOf('/', schemeIndex + 3);
	int queryIndex = endpoint.indexOf('?');

	if (pathIndex < 0 || (queryIndex >= 0 && queryIndex < pathIndex)) {
	    pathIndex = queryIndex;
	}

	return (pathIndex < 0 ? endpoint : endpoint.substring(0, pathIndex)) + "/";
    }

    /**
     * @param endpoint
     * @return the value of the persistentId parameter of the endpoint, or {@link #DEFAULT_PERSISTENT_ID}
     */
    static String getPersistentId(String endpoint) {

	int index = endpoint.indexOf("persistentId=");
	if (index < 0) {
	    return DEFAULT_PERSISTENT_ID;
	}

	String value = endpoint.substring(index + "persistentId=".length());
	int end = value.indexOf('&');
	if (end >= 0) {
	    value = value.substring(0, end);
	}

	value = URLDecoder.decode(value, StandardCharsets.UTF_8).trim();

	return value.isEmpty() ? DEFAULT_PERSISTENT_ID : value;
    }

    /**
     * @param name
     * @return the name without the extension
     */
    private static String getBaseName(String name) {

	int index = name.lastIndexOf('.');
	return index < 0 ? name : name.substring(0, index);
    }

    /**
     * @return
     */
    public String getEndpoint() {

	return endpoint;
    }
}
