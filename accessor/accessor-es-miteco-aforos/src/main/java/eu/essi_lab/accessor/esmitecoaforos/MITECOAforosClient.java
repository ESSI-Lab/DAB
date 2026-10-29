package eu.essi_lab.accessor.esmitecoaforos;

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
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Client of the Anuario de Aforos (yearbook of the official gauging network SAIH-ROEA) published by the Spanish Ministry for the
 * Ecological Transition and the Demographic Challenge (MITECO), with data validated by the Centro de Estudios Hidrográficos of
 * CEDEX.<br>
 * The whole yearbook is published as a single zipped archive (about 136 MB, 800 MB uncompressed) with a folder for each basin
 * (e.g. "EBRO/") containing CSV tables (";" separated, ISO-8859-1 encoded) of stations, daily values and monthly values. The
 * archive is linked by the page of the Sistema de Información del Anuario de Aforos (the default endpoint), from which the link of
 * the latest edition is discovered; the endpoint can also be the URL of the archive itself.<br>
 * <br>
 * <b>Disk usage</b>: the archive is downloaded once in a local cache directory,
 * <code>${java.io.tmpdir}/dab-miteco-aforos/&lt;edition&gt;/</code> (the root can be changed with the system property
 * {@value #CACHE_DIR_PROPERTY}), and it is read with {@link ZipFile} without being extracted. About 136 MB are used for each
 * edition (twice during the download of a new edition, then the directories of the previous editions are deleted). The archive is
 * downloaded again only if its size or its Last-Modified header (checked with an HTTP HEAD request at most every
 * {@link #CHECK_INTERVAL} milliseconds) changes.<br>
 * <b>Memory usage</b>: the tables are streamed line by line; only the period of each series and the descriptions of the stations
 * are kept in memory during the harvesting
 *
 * @author boldrini
 */
public class MITECOAforosClient {

    public static final String DEFAULT_ENDPOINT = "https://www.miteco.gob.es/es/agua/temas/evaluacion-de-los-recursos-hidricos/sistema-informacion-anuario-aforos.html";

    /**
     * Archive of the latest edition known at the time of writing, used if the page of the endpoint is not available
     */
    public static final String DEFAULT_ARCHIVE_URL = "https://www.miteco.gob.es/content/dam/miteco/es/agua/temas/evaluacion-de-los-recursos-hidricos/sistema-informacion-anuario-aforos/Anuario-21-22-csv.zip";

    /**
     * System property with the root directory of the cache of the archives
     */
    public static final String CACHE_DIR_PROPERTY = "dab.miteco.aforos.cache";

    static final String CACHE_DIR_NAME = "dab-miteco-aforos";

    /**
     * Minimum time between two checks of the remote archive (and between two reads of the page of the endpoint)
     */
    static final long CHECK_INTERVAL = TimeUnit.HOURS.toMillis(1);

    static final Charset CHARSET = StandardCharsets.ISO_8859_1;

    static final String SEPARATOR = ";";

    /**
     * Links to the archives in CSV format of the page of the endpoint, e.g. ".../Anuario-21-22-csv.zip"
     */
    static final Pattern ARCHIVE_LINK = Pattern.compile("href=\"([^\"]*?Anuario-(\\d{2})-(\\d{2})-csv\\.zip)\"", Pattern.CASE_INSENSITIVE);

    /**
     * Trust store with the root certificate of FNMT-RCM (AC RAIZ FNMT-RCM, the Spanish public certification authority), which
     * signs the certificate of www.miteco.gob.es but is not included in the default trust store of the JVM
     */
    static final String TRUST_STORE = "miteco-aforos-truststore.p12";
    static final String TRUST_STORE_PASSWORD = "mitecoaforos";

    static final String DAILY_DATE_COLUMN = "fecha";
    static final String MONTHLY_DATE_COLUMN = "anomes";

    // keys of the series
    public static final String SERIES_ID = "seriesId";
    public static final String BASIN = "basin";
    public static final String STATION_ID = "stationId";
    public static final String STATION_TYPE = "stationType";
    public static final String VARIABLE = "variable";
    public static final String STATION_NAME = "name";
    public static final String COMMENT = "comment";
    public static final String STATUS = "status";
    public static final String CATCHMENT_AREA = "catchmentArea";
    public static final String ALTITUDE = "altitude";
    public static final String RIVER = "river";
    public static final String MUNICIPALITY = "municipality";
    public static final String PROVINCE = "province";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String SAIH_CODE = "saihCode";
    public static final String RESERVOIR_ID = "reservoirId";
    public static final String BEGIN = "begin";
    public static final String END = "end";
    public static final String VALUES = "values";
    public static final String EDITION = "edition";

    private static final String SERIES_ID_SEPARATOR = "|";

    private static final Object CACHE_LOCK = new Object();

    /**
     * Time of the last check of each archive URL and resolved archive of each endpoint
     */
    private static final Map<String, Long> LAST_CHECKS = new ConcurrentHashMap<>();
    private static final Map<String, Archive> RESOLVED_ARCHIVES = new ConcurrentHashMap<>();

    private final String endpoint;
    private final Downloader downloader;

    /**
     * A published edition of the yearbook
     */
    public static class Archive {

	private final String url;
	private final String edition;
	private long resolved;

	/**
	 * @param url
	 * @param edition
	 */
	Archive(String url, String edition) {

	    this.url = url;
	    this.edition = edition;
	}

	/**
	 * @return the URL of the zipped archive
	 */
	public String getUrl() {

	    return url;
	}

	/**
	 * @return the edition, e.g. "2021-22" (the last hydrological year of the yearbook)
	 */
	public String getEdition() {

	    return edition;
	}
    }

    /**
     *
     */
    public MITECOAforosClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint the page linking the archive, or the URL of the archive
     */
    public MITECOAforosClient(String endpoint) {

	this.endpoint = endpoint == null || endpoint.isBlank() ? DEFAULT_ENDPOINT : endpoint.trim();
	this.downloader = new Downloader();
	this.downloader.setConnectionTimeout(TimeUnit.SECONDS, 30);
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 5);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    //
    // archive discovery and cache
    //

    /**
     * @return the latest edition linked by the endpoint (the endpoint itself if it is the URL of an archive)
     * @throws GSException
     */
    public Archive resolveArchive() throws GSException {

	if (endpoint.toLowerCase().endsWith(".zip")) {
	    return new Archive(endpoint, parseEdition(endpoint));
	}

	Archive cached = RESOLVED_ARCHIVES.get(endpoint);
	if (cached != null && System.currentTimeMillis() - cached.resolved < CHECK_INTERVAL) {
	    return cached;
	}

	Optional<String> page = getPage(endpoint);

	Archive ret;
	if (page.isPresent()) {
	    ret = findArchive(page.get(), endpoint).orElse(null);
	    if (ret == null) {
		GSLoggerFactory.getLogger(getClass()).warn("No Anuario de Aforos archive linked by {}, using {}", endpoint,
			DEFAULT_ARCHIVE_URL);
	    }
	} else {
	    ret = null;
	    GSLoggerFactory.getLogger(getClass()).warn("Page {} not available, using {}", endpoint, DEFAULT_ARCHIVE_URL);
	}

	if (ret == null) {
	    ret = cached != null ? cached : new Archive(DEFAULT_ARCHIVE_URL, parseEdition(DEFAULT_ARCHIVE_URL));
	}

	ret.resolved = System.currentTimeMillis();
	RESOLVED_ARCHIVES.put(endpoint, ret);

	return ret;
    }

    /**
     * @param html
     * @param pageUrl
     * @return the most recent edition linked by the page
     */
    static Optional<Archive> findArchive(String html, String pageUrl) {

	Matcher matcher = ARCHIVE_LINK.matcher(html);

	String best = null;
	int bestYear = -1;

	while (matcher.find()) {

	    int year = toYear(matcher.group(2));
	    if (year > bestYear) {
		bestYear = year;
		best = matcher.group(1);
	    }
	}

	if (best == null) {
	    return Optional.empty();
	}

	String url = URI.create(pageUrl).resolve(best.replace("&amp;", "&").replace(" ", "%20")).toString();

	return Optional.of(new Archive(url, parseEdition(url)));
    }

    /**
     * @param url e.g. ".../Anuario-21-22-csv.zip"
     * @return e.g. "2021-22", or the sanitized base name of the file if it does not follow the usual pattern
     */
    static String parseEdition(String url) {

	String name = getFileName(url);

	Matcher matcher = Pattern.compile("(\\d{2})-(\\d{2})", Pattern.CASE_INSENSITIVE).matcher(name);
	if (name.toLowerCase().startsWith("anuario") && matcher.find()) {
	    return toYear(matcher.group(1)) + "-" + matcher.group(2);
	}

	return name.replaceAll("(?i)\\.zip$", "");
    }

    /**
     * @param twoDigits
     * @return e.g. 2021 for "21", 1998 for "98"
     */
    private static int toYear(String twoDigits) {

	int year = Integer.parseInt(twoDigits);
	return year < 70 ? 2000 + year : 1900 + year;
    }

    /**
     * @param url
     * @return the sanitized name of the file of the URL
     */
    static String getFileName(String url) {

	String path = URI.create(url.replace(" ", "%20")).getPath();
	String name = path == null ? "" : path.substring(path.lastIndexOf('/') + 1);
	name = URLDecoder.decode(name, StandardCharsets.UTF_8);
	name = name.replaceAll("[^A-Za-z0-9._-]", "_");

	return name.isEmpty() ? "anuario.zip" : name;
    }

    /**
     * @return the root directory of the cache
     */
    static File getCacheRoot() {

	String root = System.getProperty(CACHE_DIR_PROPERTY);
	if (root == null || root.isBlank()) {
	    return new File(System.getProperty("java.io.tmpdir"), CACHE_DIR_NAME);
	}

	return new File(root);
    }

    /**
     * @return the local copy of the latest edition, downloaded if missing or changed
     * @throws GSException
     */
    public File getArchiveFile() throws GSException {

	return getArchiveFile(resolveArchive());
    }

    /**
     * @param archive
     * @return the local copy of the archive, downloaded if missing or changed
     * @throws GSException
     */
    public File getArchiveFile(Archive archive) throws GSException {

	synchronized (CACHE_LOCK) {

	    File directory = new File(getCacheRoot(), archive.getEdition());
	    File file = new File(directory, getFileName(archive.getUrl()));
	    File info = new File(directory, file.getName() + ".properties");

	    Long lastCheck = LAST_CHECKS.get(archive.getUrl());
	    if (file.exists() && lastCheck != null && System.currentTimeMillis() - lastCheck < CHECK_INTERVAL) {
		return file;
	    }

	    Optional<Properties> remote = head(archive.getUrl());

	    if (file.exists()) {

		if (remote.isEmpty()) {
		    GSLoggerFactory.getLogger(getClass()).warn("Unable to check {}, using the cached copy {}", archive.getUrl(), file);
		    LAST_CHECKS.put(archive.getUrl(), System.currentTimeMillis());
		    return file;
		}

		if (isUnchanged(file, readProperties(info), remote.get())) {
		    LAST_CHECKS.put(archive.getUrl(), System.currentTimeMillis());
		    return file;
		}

		GSLoggerFactory.getLogger(getClass()).info("Archive {} changed, downloading it again", archive.getUrl());
	    }

	    download(archive.getUrl(), directory, file);
	    writeProperties(info, remote.orElse(new Properties()));
	    deleteOtherEditions(directory);

	    LAST_CHECKS.put(archive.getUrl(), System.currentTimeMillis());

	    return file;
	}
    }

    /**
     * @param file
     * @param local the headers stored at the time of the download
     * @param remote the current headers
     * @return
     */
    static boolean isUnchanged(File file, Properties local, Properties remote) {

	String length = remote.getProperty("Content-Length");
	if (length != null && !length.equals(String.valueOf(file.length()))) {
	    return false;
	}

	String lastModified = remote.getProperty("Last-Modified");
	return lastModified == null || lastModified.equals(local.getProperty("Last-Modified"));
    }

    /**
     * @param url
     * @return the Content-Length and Last-Modified headers, or empty if the request failed
     */
    private Optional<Properties> head(String url) {

	try {

	    HttpResponse<InputStream> response = send(MethodNoBody.HEAD, url);

	    try (InputStream stream = response.body()) {
		// nothing to read
	    }

	    if (response.statusCode() != 200) {
		GSLoggerFactory.getLogger(getClass()).warn("HEAD {} returned HTTP {}", url, response.statusCode());
		return Optional.empty();
	    }

	    Properties ret = new Properties();
	    response.headers().firstValue("Content-Length").ifPresent(v -> ret.setProperty("Content-Length", v));
	    response.headers().firstValue("Last-Modified").ifPresent(v -> ret.setProperty("Last-Modified", v));

	    return Optional.of(ret);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).warn("HEAD {} failed: {}", url, e.getMessage());
	    return Optional.empty();
	}
    }

    /**
     * Downloads the archive in a temporary file of the cache directory, checks that it is a valid zip file and then moves it to its
     * final location
     *
     * @param url
     * @param directory
     * @param file
     * @throws GSException
     */
    private void download(String url, File directory, File file) throws GSException {

	GSLoggerFactory.getLogger(getClass()).info("Downloading {} to {}", url, file);

	File temp = null;

	try {

	    Files.createDirectories(directory.toPath());
	    temp = Files.createTempFile(directory.toPath(), file.getName(), ".part").toFile();

	    HttpResponse<InputStream> response = send(MethodNoBody.GET, url);

	    try (InputStream stream = response.body()) {

		if (response.statusCode() != 200) {
		    throw createException("Archive " + url + " returned HTTP " + response.statusCode(), null);
		}

		try (OutputStream output = Files.newOutputStream(temp.toPath())) {
		    IOUtils.copyLarge(stream, output);
		}
	    }

	    try (ZipFile zip = new ZipFile(temp)) {
		GSLoggerFactory.getLogger(getClass()).info("Downloaded {} ({} bytes, {} entries)", url, temp.length(), zip.size());
	    }

	    Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("Download of archive " + url + " failed", e);

	} finally {

	    if (temp != null && temp.exists()) {
		temp.delete();
	    }
	}
    }

    /**
     * Deletes the cached copies of the other editions
     *
     * @param directory the directory of the current edition
     */
    private void deleteOtherEditions(File directory) {

	File[] editions = directory.getParentFile().listFiles(File::isDirectory);
	if (editions == null) {
	    return;
	}

	for (File edition : editions) {
	    if (!edition.getName().equals(directory.getName())) {
		GSLoggerFactory.getLogger(getClass()).info("Deleting cached edition {}", edition);
		FileUtils.deleteQuietly(edition);
	    }
	}
    }

    /**
     * @param file
     * @return
     */
    private static Properties readProperties(File file) {

	Properties ret = new Properties();
	if (file.exists()) {
	    try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
		ret.load(reader);
	    } catch (IOException e) {
		GSLoggerFactory.getLogger(MITECOAforosClient.class).warn("Unable to read {}: {}", file, e.getMessage());
	    }
	}

	return ret;
    }

    /**
     * @param file
     * @param properties
     */
    private static void writeProperties(File file, Properties properties) {

	try (OutputStream output = Files.newOutputStream(file.toPath())) {
	    properties.store(output, "Headers of the cached Anuario de Aforos archive");
	} catch (IOException e) {
	    GSLoggerFactory.getLogger(MITECOAforosClient.class).warn("Unable to write {}: {}", file, e.getMessage());
	}
    }

    //
    // series
    //

    /**
     * Retrieves the series of the given variables, reading all the tables of the archive (downloaded if needed). Series without
     * values are discarded
     *
     * @param variables
     * @param basins the basin folders to read (e.g. "EBRO"), empty for all
     * @param stations the station codes to read, empty for all
     * @return the series, sorted by basin, station and variable
     * @throws GSException
     */
    public List<MITECOAforosEntity> retrieveSeries(Collection<MITECOAforosVariable> variables, Collection<String> basins,
	    Collection<String> stations) throws GSException {

	Archive archive = resolveArchive();
	File file = getArchiveFile(archive);

	List<MITECOAforosEntity> ret = new ArrayList<>();

	try (ZipFile zip = new ZipFile(file, StandardCharsets.UTF_8)) {

	    for (String basin : listBasins(zip)) {

		if (!basins.isEmpty() && basins.stream().noneMatch(b -> b.equalsIgnoreCase(basin))) {
		    continue;
		}

		GSLoggerFactory.getLogger(getClass()).debug("Reading basin {}", basin);

		ret.addAll(retrieveSeries(zip, basin, variables, stations, archive.getEdition()));
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("Unable to read archive " + file, e);
	}

	return ret;
    }

    /**
     * @param zip
     * @return the basin folders, sorted by name
     */
    static List<String> listBasins(ZipFile zip) {

	TreeSet<String> ret = new TreeSet<>();
	zip.stream().map(ZipEntry::getName).filter(n -> n.indexOf('/') > 0).forEach(n -> ret.add(n.substring(0, n.indexOf('/'))));

	return new ArrayList<>(ret);
    }

    /**
     * @param zip
     * @param basin
     * @param variables
     * @param stations
     * @param edition
     * @return
     * @throws IOException
     */
    private List<MITECOAforosEntity> retrieveSeries(ZipFile zip, String basin, Collection<MITECOAforosVariable> variables,
	    Collection<String> stations, String edition) throws IOException {

	// variables grouped by data table, so that each table is read only once
	Map<String, List<MITECOAforosVariable>> tables = new LinkedHashMap<>();
	for (MITECOAforosVariable variable : variables) {
	    tables.computeIfAbsent(variable.getFileName(), k -> new ArrayList<>()).add(variable);
	}

	TreeMap<String, SeriesPeriod> periods = new TreeMap<>();

	for (Map.Entry<String, List<MITECOAforosVariable>> table : tables.entrySet()) {

	    try (BufferedReader reader = openEntry(zip, basin, table.getKey())) {
		if (reader != null) {
		    scanTable(reader, table.getValue(), stations, periods);
		}
	    }
	}

	if (periods.isEmpty()) {
	    return new ArrayList<>();
	}

	Lookups lookups = readLookups(zip, basin);

	Map<MITECOAforosStationType, Map<String, JSONObject>> descriptions = new HashMap<>();

	List<MITECOAforosEntity> ret = new ArrayList<>();

	for (SeriesPeriod period : periods.values()) {

	    MITECOAforosStationType type = period.variable.getStationType();

	    Map<String, JSONObject> typeStations = descriptions.get(type);
	    if (typeStations == null) {
		try (BufferedReader reader = openEntry(zip, basin, type.getFileName())) {
		    typeStations = reader == null ? new HashMap<>() : readStations(reader, type, lookups);
		}
		descriptions.put(type, typeStations);
	    }

	    ret.add(createSeries(basin, period, typeStations.get(period.stationId), edition));
	}

	return ret;
    }

    /**
     * Period and number of the valid values of a series
     */
    static class SeriesPeriod {

	final String stationId;
	final MITECOAforosVariable variable;

	/**
	 * First and last date, e.g. "1912-01-01"
	 */
	String first;
	String last;
	int count;

	/**
	 * @param stationId
	 * @param variable
	 */
	SeriesPeriod(String stationId, MITECOAforosVariable variable) {

	    this.stationId = stationId;
	    this.variable = variable;
	}

	/**
	 * @param date
	 */
	void add(String date) {

	    if (first == null || date.compareTo(first) < 0) {
		first = date;
	    }
	    if (last == null || date.compareTo(last) > 0) {
		last = date;
	    }
	    count++;
	}
    }

    /**
     * Reads a data table, updating the periods of the series of the given variables
     *
     * @param reader
     * @param variables variables of the table
     * @param stations the station codes to read, empty for all
     * @param periods the periods, by series key
     * @throws IOException
     */
    static void scanTable(BufferedReader reader, List<MITECOAforosVariable> variables, Collection<String> stations,
	    Map<String, SeriesPeriod> periods) throws IOException {

	String headerLine = reader.readLine();
	if (headerLine == null) {
	    return;
	}

	Map<String, Integer> header = parseHeader(headerLine);

	boolean daily = variables.get(0).isDaily();
	Integer dateIndex = header.get(daily ? DAILY_DATE_COLUMN : MONTHLY_DATE_COLUMN);
	if (dateIndex == null) {
	    return;
	}

	int[] valueIndexes = new int[variables.size()];
	for (int i = 0; i < variables.size(); i++) {
	    valueIndexes[i] = header.getOrDefault(variables.get(i).getColumn(), -1);
	}

	String line;
	while ((line = reader.readLine()) != null) {

	    String[] split = line.split(SEPARATOR, -1);
	    if (split.length <= dateIndex) {
		continue;
	    }

	    String stationId = split[0].trim();
	    if (!stations.isEmpty() && !stations.contains(stationId)) {
		continue;
	    }

	    Optional<String> date = daily ? normalizeDailyDate(split[dateIndex]) : normalizeMonthlyDate(split[dateIndex]);
	    if (date.isEmpty()) {
		continue;
	    }

	    for (int i = 0; i < valueIndexes.length; i++) {

		int index = valueIndexes[i];
		if (index < 0 || index >= split.length || parseDecimal(split[index]) == null) {
		    continue;
		}

		MITECOAforosVariable variable = variables.get(i);
		periods.computeIfAbsent(buildKey(stationId, variable), k -> new SeriesPeriod(stationId, variable)).add(date.get());
	    }
	}
    }

    /**
     * @param stationId
     * @param variable
     * @return a key sorting the series by station number and variable
     */
    private static String buildKey(String stationId, MITECOAforosVariable variable) {

	return String.format("%12s", stationId) + SERIES_ID_SEPARATOR + String.format("%03d", variable.ordinal());
    }

    /**
     * @param basin
     * @param period
     * @param station the description of the station, possibly null
     * @param edition
     * @return
     */
    static MITECOAforosEntity createSeries(String basin, SeriesPeriod period, JSONObject station, String edition) {

	JSONObject object = station == null ? new JSONObject() : new JSONObject(station.toString());

	object.put(SERIES_ID, buildSeriesId(basin, period.stationId, period.variable));
	object.put(BASIN, basin);
	object.put(STATION_ID, period.stationId);
	object.put(STATION_TYPE, period.variable.getStationType().name());
	object.put(VARIABLE, period.variable.name());
	object.put(BEGIN, period.first + "T00:00:00Z");
	object.put(END, period.last + "T00:00:00Z");
	object.put(VALUES, period.count);
	if (edition != null) {
	    object.put(EDITION, edition);
	}

	return new MITECOAforosEntity(object);
    }

    /**
     * Lookup tables of a basin folder
     */
    static class Lookups {

	/**
	 * River (stream) names, by basin number (table CDR)
	 */
	final Map<String, String> rivers = new HashMap<>();
	/**
	 * Municipality names and province identifiers, by municipality identifier (table MUNI)
	 */
	final Map<String, String[]> municipalities = new HashMap<>();
	/**
	 * Province names, by province identifier (table PROV)
	 */
	final Map<String, String> provinces = new HashMap<>();
    }

    /**
     * @param zip
     * @param basin
     * @return
     * @throws IOException
     */
    private static Lookups readLookups(ZipFile zip, String basin) throws IOException {

	Lookups ret = new Lookups();

	readTable(zip, basin, "cdr.csv", row -> ret.rivers.putIfAbsent(row.getOrDefault("num_cuenca", ""), row.get("corriente")));
	readTable(zip, basin, "muni.csv", row -> ret.municipalities.putIfAbsent(row.getOrDefault("muni_id", ""),
		new String[] { row.get("munombre"), row.get("prov_id") }));
	readTable(zip, basin, "prov.csv", row -> ret.provinces.putIfAbsent(row.getOrDefault("prov_id", ""), row.get("prov_nombre")));

	return ret;
    }

    /**
     * @param zip
     * @param basin
     * @param fileName
     * @param consumer
     * @throws IOException
     */
    private static void readTable(ZipFile zip, String basin, String fileName, Consumer<Map<String, String>> consumer)
	    throws IOException {

	try (BufferedReader reader = openEntry(zip, basin, fileName)) {
	    if (reader != null) {
		readRows(reader, consumer);
	    }
	}
    }

    /**
     * @param reader
     * @param consumer receives each row as a map of trimmed values by column name
     * @throws IOException
     */
    static void readRows(BufferedReader reader, Consumer<Map<String, String>> consumer) throws IOException {

	String headerLine = reader.readLine();
	if (headerLine == null) {
	    return;
	}

	String[] header = headerLine.split(SEPARATOR, -1);

	String line;
	while ((line = reader.readLine()) != null) {

	    if (line.isBlank()) {
		continue;
	    }

	    String[] split = line.split(SEPARATOR, -1);
	    Map<String, String> row = new HashMap<>();
	    for (int i = 0; i < header.length && i < split.length; i++) {
		row.put(header[i].trim(), split[i].trim());
	    }

	    consumer.accept(row);
	}
    }

    /**
     * @param reader the station table
     * @param type
     * @param lookups
     * @return the descriptions of the stations, by station code
     * @throws IOException
     */
    static Map<String, JSONObject> readStations(BufferedReader reader, MITECOAforosStationType type, Lookups lookups)
	    throws IOException {

	Map<String, JSONObject> ret = new HashMap<>();

	readRows(reader, row -> {

	    String id = row.getOrDefault(type.getIdColumn(), "");
	    if (!id.isEmpty()) {
		ret.put(id, describeStation(row, type, lookups));
	    }
	});

	return ret;
    }

    /**
     * @param row
     * @param type
     * @param lookups
     * @return
     */
    static JSONObject describeStation(Map<String, String> row, MITECOAforosStationType type, Lookups lookups) {

	JSONObject ret = new JSONObject();

	putIfNotEmpty(ret, STATION_NAME, row.get(type.getNameColumn()));
	putIfNotEmpty(ret, COMMENT, row.get("comentario"));
	putIfNotEmpty(ret, STATUS, decodeStatus(row.get("serv")));
	putIfNotEmpty(ret, CATCHMENT_AREA, type == MITECOAforosStationType.CANAL ? row.get("suprcan") : row.get("suprest"));
	putIfNotEmpty(ret, ALTITUDE, row.get("alti"));
	putIfNotEmpty(ret, SAIH_CODE, row.get("cod_saih"));

	if (type == MITECOAforosStationType.EVAPORATION) {
	    putIfNotEmpty(ret, RESERVOIR_ID, row.get("ref_ceh"));
	}

	if (lookups != null) {

	    putIfNotEmpty(ret, RIVER, lookups.rivers.get(row.getOrDefault("num_cuenca", "")));

	    String[] municipality = lookups.municipalities.get(row.getOrDefault("muni_id", ""));
	    if (municipality != null) {
		putIfNotEmpty(ret, MUNICIPALITY, municipality[0]);
		putIfNotEmpty(ret, PROVINCE, lookups.provinces.get(municipality[1]));
	    }
	}

	parseCoordinates(row).ifPresent(c -> {
	    ret.put(LAT, c[0]);
	    ret.put(LON, c[1]);
	});

	return ret;
    }

    /**
     * @param serv
     * @return
     */
    private static String decodeStatus(String serv) {

	if (serv == null) {
	    return null;
	}

	switch (serv) {
	case "0":
	    return "Out of service";
	case "1":
	    return "In service";
	default:
	    return null;
	}
    }

    /**
     * @param object
     * @param key
     * @param value
     */
    private static void putIfNotEmpty(JSONObject object, String key, String value) {

	if (value != null && !value.trim().isEmpty()) {
	    object.put(key, value.trim());
	}
    }

    //
    // coordinates
    //

    /**
     * The coordinates of the tables are given both as WGS84 degrees, minutes and seconds packed in an integer (columns longwgs84,
     * latwgs84, e.g. -54105 for -5° 41' 05") and as ETRS89 UTM coordinates in zone 30 (columns xetrs89, yetrs89). The former are
     * used when valid, otherwise the latter are converted to geographic coordinates (ETRS89 can be considered equivalent to WGS84 at
     * the scale of a station)
     *
     * @param row
     * @return latitude and longitude in WGS84 decimal degrees, rounded to 6 decimals
     */
    static Optional<BigDecimal[]> parseCoordinates(Map<String, String> row) {

	Optional<Double> lat = parsePackedDegrees(row.get("latwgs84"));
	Optional<Double> lon = parsePackedDegrees(row.get("longwgs84"));

	if (lat.isPresent() && lon.isPresent() && isInSpain(lat.get(), lon.get())) {
	    return Optional.of(new BigDecimal[] { round(lat.get()), round(lon.get()) });
	}

	BigDecimal x = parseDecimal(row.get("xetrs89"));
	BigDecimal y = parseDecimal(row.get("yetrs89"));

	if (x != null && y != null && x.signum() > 0 && y.signum() > 0) {

	    double[] latLon = utmToLatLon(x.doubleValue(), y.doubleValue(), 30);
	    if (isInSpain(latLon[0], latLon[1])) {
		return Optional.of(new BigDecimal[] { round(latLon[0]), round(latLon[1]) });
	    }
	}

	return Optional.empty();
    }

    /**
     * @param text e.g. "-54105" for -5° 41' 05"
     * @return the decimal degrees, or empty if not valid (minutes or seconds out of range)
     */
    static Optional<Double> parsePackedDegrees(String text) {

	if (text == null || !text.trim().matches("-?\\d{1,7}")) {
	    return Optional.empty();
	}

	String trimmed = text.trim();
	boolean negative = trimmed.startsWith("-");
	int value = Math.abs(Integer.parseInt(trimmed));

	int degrees = value / 10000;
	int minutes = (value / 100) % 100;
	int seconds = value % 100;

	// seconds equal to 60 are rounding artefacts of the source
	if (value == 0 || minutes >= 60 || seconds > 60) {
	    return Optional.empty();
	}

	double ret = degrees + minutes / 60.0 + seconds / 3600.0;

	return Optional.of(negative ? -ret : ret);
    }

    /**
     * @param lat
     * @param lon
     * @return true if the point is in the area of Spain (including the Canary islands)
     */
    private static boolean isInSpain(double lat, double lon) {

	return lat > 27 && lat < 44.5 && lon > -18.5 && lon < 4.5;
    }

    /**
     * Converts UTM coordinates of the northern hemisphere to geographic coordinates (WGS84/GRS80 ellipsoid)
     *
     * @param easting
     * @param northing
     * @param zone
     * @return latitude and longitude in decimal degrees
     */
    static double[] utmToLatLon(double easting, double northing, int zone) {

	double a = 6378137.0;
	double f = 1 / 298.257222101;
	double k0 = 0.9996;

	double e2 = f * (2 - f);
	double ep2 = e2 / (1 - e2);
	double e1 = (1 - Math.sqrt(1 - e2)) / (1 + Math.sqrt(1 - e2));

	double x = easting - 500000.0;
	double m = northing / k0;
	double mu = m / (a * (1 - e2 / 4 - 3 * e2 * e2 / 64 - 5 * e2 * e2 * e2 / 256));

	double phi1 = mu + (3 * e1 / 2 - 27 * Math.pow(e1, 3) / 32) * Math.sin(2 * mu) //
		+ (21 * e1 * e1 / 16 - 55 * Math.pow(e1, 4) / 32) * Math.sin(4 * mu) //
		+ (151 * Math.pow(e1, 3) / 96) * Math.sin(6 * mu) //
		+ (1097 * Math.pow(e1, 4) / 512) * Math.sin(8 * mu);

	double sin = Math.sin(phi1);
	double cos = Math.cos(phi1);
	double tan = Math.tan(phi1);

	double n1 = a / Math.sqrt(1 - e2 * sin * sin);
	double t1 = tan * tan;
	double c1 = ep2 * cos * cos;
	double r1 = a * (1 - e2) / Math.pow(1 - e2 * sin * sin, 1.5);
	double d = x / (n1 * k0);

	double lat = phi1 - (n1 * tan / r1) * (d * d / 2 //
		- (5 + 3 * t1 + 10 * c1 - 4 * c1 * c1 - 9 * ep2) * Math.pow(d, 4) / 24 //
		+ (61 + 90 * t1 + 298 * c1 + 45 * t1 * t1 - 252 * ep2 - 3 * c1 * c1) * Math.pow(d, 6) / 720);

	double lon = (d - (1 + 2 * t1 + c1) * Math.pow(d, 3) / 6 //
		+ (5 - 2 * c1 + 28 * t1 - 3 * c1 * c1 + 8 * ep2 + 24 * t1 * t1) * Math.pow(d, 5) / 120) / cos;

	double lon0 = zone * 6 - 183;

	return new double[] { Math.toDegrees(lat), lon0 + Math.toDegrees(lon) };
    }

    /**
     * @param value
     * @return
     */
    private static BigDecimal round(double value) {

	return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP);
    }

    //
    // observations
    //

    /**
     * Retrieves the valid values of the given period from the local copy of the archive (downloaded if needed)
     *
     * @param series
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(MITECOAforosEntity series, Date begin, Date end) throws GSException {

	MITECOAforosVariable variable = series.getVariable()
		.orElseThrow(() -> createException("Invalid Anuario de Aforos series " + series.getSeriesId(), null));

	File file = getArchiveFile();

	try (ZipFile zip = new ZipFile(file, StandardCharsets.UTF_8);
		BufferedReader reader = openEntry(zip, series.getBasin(), variable.getFileName())) {

	    if (reader == null) {
		GSLoggerFactory.getLogger(getClass()).warn("Table {}/{} not found in {}", series.getBasin(), variable.getFileName(), file);
		return new TreeMap<>();
	    }

	    return parseObservations(reader, series.getStationId(), variable, begin, end);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("Unable to read series " + series.getSeriesId() + " from " + file, e);
	}
    }

    /**
     * Reads the values of a station from a data table. The rows of the daily tables are grouped by station and sorted by date, so
     * the reading stops at the end of the rows of the station (or of the period); monthly tables are read entirely
     *
     * @param reader
     * @param stationId
     * @param variable
     * @param begin the begin of the period, or null
     * @param end the end of the period, or null
     * @return
     * @throws IOException
     */
    static TreeMap<Date, BigDecimal> parseObservations(BufferedReader reader, String stationId, MITECOAforosVariable variable, Date begin,
	    Date end) throws IOException {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	String headerLine = reader.readLine();
	if (headerLine == null) {
	    return ret;
	}

	Map<String, Integer> header = parseHeader(headerLine);
	Integer dateIndex = header.get(variable.isDaily() ? DAILY_DATE_COLUMN : MONTHLY_DATE_COLUMN);
	Integer valueIndex = header.get(variable.getColumn());
	if (dateIndex == null || valueIndex == null) {
	    return ret;
	}

	boolean found = false;

	String line;
	while ((line = reader.readLine()) != null) {

	    int separator = line.indexOf(';');
	    if (separator < 0 || !line.substring(0, separator).trim().equals(stationId)) {
		if (found && variable.isDaily()) {
		    break;
		}
		continue;
	    }

	    found = true;

	    String[] split = line.split(SEPARATOR, -1);
	    if (split.length <= Math.max(dateIndex, valueIndex)) {
		continue;
	    }

	    Optional<String> text = variable.isDaily() ? normalizeDailyDate(split[dateIndex]) : normalizeMonthlyDate(split[dateIndex]);
	    if (text.isEmpty()) {
		continue;
	    }

	    Date date = toDate(text.get());

	    if (end != null && date.after(end)) {
		if (variable.isDaily()) {
		    break;
		}
		continue;
	    }

	    if (begin != null && date.before(begin)) {
		continue;
	    }

	    BigDecimal value = parseDecimal(split[valueIndex]);
	    if (value != null) {
		ret.put(date, value);
	    }
	}

	return ret;
    }

    //
    // utilities
    //

    /**
     * @param basin
     * @param stationId
     * @param variable
     * @return e.g. "EBRO|9001|RIVER_DISCHARGE_DAILY"
     */
    static String buildSeriesId(String basin, String stationId, MITECOAforosVariable variable) {

	return basin + SERIES_ID_SEPARATOR + stationId + SERIES_ID_SEPARATOR + variable.name();
    }

    /**
     * @param seriesId
     * @return the series identified by the given identifier, or null if not valid
     */
    public static MITECOAforosEntity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	String[] split = seriesId.split(Pattern.quote(SERIES_ID_SEPARATOR), -1);
	if (split.length != 3 || split[0].isEmpty() || split[1].isEmpty()) {
	    return null;
	}

	Optional<MITECOAforosVariable> variable = MITECOAforosVariable.decode(split[2]);
	if (variable.isEmpty()) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, buildSeriesId(split[0], split[1], variable.get()));
	object.put(BASIN, split[0]);
	object.put(STATION_ID, split[1]);
	object.put(STATION_TYPE, variable.get().getStationType().name());
	object.put(VARIABLE, variable.get().name());

	return new MITECOAforosEntity(object);
    }

    /**
     * @param zip
     * @param basin
     * @param fileName
     * @return a reader of the table, or null if the table is missing
     * @throws IOException
     */
    static BufferedReader openEntry(ZipFile zip, String basin, String fileName) throws IOException {

	ZipEntry entry = zip.getEntry(basin + "/" + fileName);
	if (entry == null) {
	    return null;
	}

	return new BufferedReader(new InputStreamReader(zip.getInputStream(entry), CHARSET), 64 * 1024);
    }

    /**
     * @param headerLine
     * @return the column indexes, by column name
     */
    private static Map<String, Integer> parseHeader(String headerLine) {

	Map<String, Integer> ret = new HashMap<>();
	String[] split = headerLine.split(SEPARATOR, -1);
	for (int i = 0; i < split.length; i++) {
	    ret.putIfAbsent(split[i].trim().toLowerCase(), i);
	}

	return ret;
    }

    /**
     * @param text e.g. "01/10/1978"
     * @return e.g. "1978-10-01"
     */
    static Optional<String> normalizeDailyDate(String text) {

	String trimmed = text.trim();
	if (!trimmed.matches("\\d{2}/\\d{2}/\\d{4}")) {
	    return Optional.empty();
	}

	return Optional.of(trimmed.substring(6, 10) + "-" + trimmed.substring(3, 5) + "-" + trimmed.substring(0, 2));
    }

    /**
     * @param text e.g. "197810"
     * @return e.g. "1978-10-01"
     */
    static Optional<String> normalizeMonthlyDate(String text) {

	String trimmed = text.trim();
	if (!trimmed.matches("\\d{6}")) {
	    return Optional.empty();
	}

	return Optional.of(trimmed.substring(0, 4) + "-" + trimmed.substring(4, 6) + "-01");
    }

    /**
     * @param isoDate e.g. "1978-10-01"
     * @return the date at midnight UTC
     */
    private static Date toDate(String isoDate) {

	return Date.from(LocalDate.parse(isoDate).atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    /**
     * @param text
     * @return
     */
    static BigDecimal parseDecimal(String text) {

	if (text == null) {
	    return null;
	}

	String trimmed = text.trim();
	if (trimmed.isEmpty()) {
	    return null;
	}

	try {
	    return new BigDecimal(trimmed);
	} catch (NumberFormatException e) {
	    return null;
	}
    }

    /**
     * @param url
     * @return the page, or empty if not available
     */
    private Optional<String> getPage(String url) {

	try {

	    HttpResponse<InputStream> response = send(MethodNoBody.GET, url);

	    String text;
	    try (InputStream stream = response.body()) {
		text = stream == null ? "" : IOUtils.toString(stream, StandardCharsets.UTF_8);
	    }

	    if (response.statusCode() != 200) {
		GSLoggerFactory.getLogger(getClass()).warn("{} returned HTTP {}", url, response.statusCode());
		return Optional.empty();
	    }

	    return text.isBlank() ? Optional.empty() : Optional.of(text);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).warn("{} request failed: {}", url, e.getMessage());
	    return Optional.empty();
	}
    }

    /**
     * @param method
     * @param url
     * @return
     * @throws Exception
     */
    private HttpResponse<InputStream> send(MethodNoBody method, String url) throws Exception {

	return downloader.downloadResponse(//
		HttpRequestUtils.build(method, url), //
		MITECOAforosClient.class.getClassLoader().getResourceAsStream(TRUST_STORE), //
		TRUST_STORE_PASSWORD);
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
}
