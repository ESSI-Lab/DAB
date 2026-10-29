package eu.essi_lab.accessor.wiskiweb3;

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

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Client of the static JSON files published by a KISTERS WISKI-Web 3 "webpublic" site (e.g. Hesse HLNUG,
 * https://www.hlnug.de/static/pegel/wiskiweb3/webpublic/). The endpoint is the data directory of the site (the "apiPath" of
 * the web application configuration, e.g. https://www.hlnug.de/static/pegel/wiskiweb3/data/), containing:
 * <ul>
 * <li>internet/stations/stations.json: the station list</li>
 * <li>internet/layers/index.json: the map layers, one per published station parameter; internet/layers/{id}/index.json lists
 * the stations of a layer with the latest observation</li>
 * <li>internet/stations/{site}/{station}/{parameter}/week.json: the high resolution values of about the last week (rolling
 * window)</li>
 * <li>internet/stations/{site}/{station}/{parameter}/year.json: the daily mean, minimum and maximum values of the period
 * published by the site (the whole period of record in Hesse, the last year on other sites)</li>
 * </ul>
 * Each file is an array of time series, each one with its metadata (ts_shortname, ts_unitsymbol, ...), a "columns" description
 * ("Timestamp,Value[,...]") and the "data" rows.<br>
 * A harvested series is identified by the station directory, the file and the ts_shortname (see {@link #buildSeriesId(String,
 * String, String)}). The extent of the daily series is read with HTTP range requests (only the beginning and the end of the
 * year files are downloaded)
 *
 * @author boldrini
 */
public class WISKIWeb3Client {

    public static final String DEFAULT_ENDPOINT = "https://www.hlnug.de/static/pegel/wiskiweb3/data/";

    static final String DATA_PATH = "internet/";
    static final String STATIONS_PATH = DATA_PATH + "stations/stations.json";
    static final String LAYERS_PATH = DATA_PATH + "layers/index.json";
    static final String APP_CONFIG_PATH = "config/app.json";

    /**
     * The file of the high resolution values of about the last week
     */
    public static final String WEEK_FILE = "week";
    /**
     * The file of the daily values
     */
    public static final String YEAR_FILE = "year";

    /**
     * The ts_shortname (prefix) of the daily mean values of the year files
     */
    public static final String DAILY_MEAN = "Day.Mean";

    static final String TS_LAYER = "TS_LAYER";

    /**
     * Bytes downloaded from the beginning and from the end of a year file to find its first and last values
     */
    static final int HEAD_BYTES = 32768;
    static final int TAIL_BYTES = 4096;

    // keys of the layer and station entries, and of the time series in the files
    public static final String STATION_NO = "station_no";
    public static final String STATION_NAME = "station_name";
    public static final String STATION_LONG_NAME = "station_longname";
    public static final String SITE_NO = "site_no";
    public static final String LAT = "station_latitude";
    public static final String LON = "station_longitude";
    public static final String PARAMETER = "stationparameter_name";
    public static final String PARAMETER_NO = "stationparameter_no";
    public static final String PARAMETER_LONG_NAME = "stationparameter_longname";
    public static final String UNIT = "ts_unitsymbol";
    public static final String TS_SHORT_NAME = "ts_shortname";
    public static final String TS_NAME = "ts_name";
    public static final String TS_PATH = "ts_path";
    public static final String LATEST_TIMESTAMP = "timestamp";
    public static final String CATCHMENT = "catchment_name";
    public static final String WATERBODY = "WTO_OBJECT";
    public static final String RIVER = "EXTERN-WATERS-NAME";
    public static final String HYDRO_UNIT = "hydrounit_name";
    public static final String BODY_RESPONSIBLE = "BODY_RESPONSIBLE";
    public static final String CATCHMENT_SIZE = "CATCHMENT_SIZE";
    public static final String GAUGE_DATUM = "station_gauge_datum";
    public static final String GAUGE_DATUM_POSTFIX = "station_gauge_datum_postfix";
    static final String ROWS = "rows";
    static final String DATA = "data";

    /**
     * Keys copied from the station list, when not given by the layer entry
     */
    static final String[] STATION_KEYS = new String[] { STATION_LONG_NAME, SITE_NO, CATCHMENT, WATERBODY, RIVER, HYDRO_UNIT,
	    BODY_RESPONSIBLE, CATCHMENT_SIZE, GAUGE_DATUM, GAUGE_DATUM_POSTFIX };

    // keys added by the client and by the connector
    public static final String SERIES_ID = "seriesId";
    public static final String STATION_PATH = "stationPath";
    public static final String FILE = "file";
    public static final String LAYER_ID = "layerId";
    public static final String LAYER_LABEL = "layerLabel";
    public static final String BEGIN = "begin";
    public static final String END = "end";
    public static final String ORGANISATION = "organisation";
    public static final String LICENSE = "license";
    public static final String COUNTRY = "country";

    private static final String SERIES_ID_SEPARATOR = "|";

    private static final Pattern ROW_PATTERN = Pattern.compile("\\[\\s*\"([^\"]+)\"\\s*,\\s*(null|[-+0-9.eE]+)");
    private static final Pattern HEADER_PATTERN = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern TITLE_PATTERN = Pattern.compile("<title>\\s*([^<]+?)\\s*</title>",
	    Pattern.CASE_INSENSITIVE);
    private static final Pattern LICENSE_LABEL_PATTERN = Pattern
	    .compile("(CC[ -]BY(?:[ -](?:SA|NC|ND))*(?:[ -]\\d\\.\\d)?|CC0(?:[ -]1\\.0)?|dl-de/[a-z]+-\\d-\\d)", Pattern.CASE_INSENSITIVE);
    private static final Pattern LICENSE_URL_PATTERN = Pattern
	    .compile("https?://(?:www\\.)?(?:creativecommons\\.org|govdata\\.de)/[^\\s\"'<>,)]+");
    private static final Pattern ATTRIBUTION_PATTERN = Pattern.compile("[„\"]([^„“”\"]{5,200})[“”\"]");

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public WISKIWeb3Client() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint the data directory of the site
     */
    public WISKIWeb3Client(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setConnectionTimeout(TimeUnit.SECONDS, 30);
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 2);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    /**
     * @return the normalized endpoint
     */
    public String getEndpoint() {

	return endpoint;
    }

    /**
     * @return the time series layers listed by the site, e.g. {"id":"10","label":"Wasserstand","href":
     *         "internet/layers/10/index.json","layer_type":"TS_LAYER",...}
     * @throws GSException
     */
    public List<JSONObject> retrieveLayers() throws GSException {

	JSONArray links = new JSONObject(get(LAYERS_PATH)).optJSONArray("_links");

	List<JSONObject> ret = new ArrayList<>();
	if (links == null) {
	    return ret;
	}

	for (int i = 0; i < links.length(); i++) {

	    JSONObject link = links.getJSONObject(i);
	    String href = link.optString("href", "");

	    if (link.optString("type").equals("resource") && TS_LAYER.equals(link.optString("layer_type", TS_LAYER))
		    && href.endsWith(".json")) {
		ret.add(link);
	    }
	}

	return ret;
    }

    /**
     * Lists the candidate series of the given layers: one per station parameter directory and file. The extent of the series is
     * not yet known (see {@link #completeSeries(WISKIWeb3Entity)})
     *
     * @param layerIds the identifiers of the layers to read, empty for all the time series layers of the site
     * @param files the files to read ({@link #WEEK_FILE}, {@link #YEAR_FILE})
     * @return the series sorted by identifier
     * @throws GSException
     */
    public List<WISKIWeb3Entity> retrieveSeries(Collection<String> layerIds, Collection<String> files) throws GSException {

	HashMap<String, JSONObject> stations = new HashMap<>();

	Optional<String> stationList = getOptional(STATIONS_PATH);
	if (stationList.isPresent()) {
	    JSONArray array = new JSONArray(stationList.get());
	    for (int i = 0; i < array.length(); i++) {
		JSONObject station = array.getJSONObject(i);
		stations.put(station.optString(STATION_NO), station);
	    }
	}

	// the same station parameter can be listed by several layers (e.g. precipitation sums of 6, 12 and 24 hours)
	LinkedHashMap<String, WISKIWeb3Entity> ret = new LinkedHashMap<>();

	for (JSONObject layer : retrieveLayers()) {

	    String layerId = layer.optString("id");
	    if (!layerIds.isEmpty() && !layerIds.contains(layerId)) {
		continue;
	    }

	    Optional<String> text = getOptional(layer.getString("href"));
	    if (text.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).warn("Layer {} not available", layerId);
		continue;
	    }

	    JSONArray entries = new JSONArray(text.get());

	    for (int i = 0; i < entries.length(); i++) {

		JSONObject entry = entries.getJSONObject(i);

		for (String file : files) {
		    createSeries(entry, layer, stations.get(entry.optString(STATION_NO)), file).//
			    ifPresent(series -> ret.putIfAbsent(series.getStationPath() + SERIES_ID_SEPARATOR + file, series));
		}
	    }
	}

	List<WISKIWeb3Entity> list = new ArrayList<>(ret.values());
	list.sort(Comparator.comparing(WISKIWeb3Entity::getSeriesId));

	return list;
    }

    /**
     * @param layerEntry the entry of the station in the layer
     * @param layer the layer link
     * @param station the station description, possibly null
     * @param file {@link #WEEK_FILE} or {@link #YEAR_FILE}
     * @return
     */
    static Optional<WISKIWeb3Entity> createSeries(JSONObject layerEntry, JSONObject layer, JSONObject station, String file) {

	Optional<String> stationPath = getStationPath(layerEntry);
	if (stationPath.isEmpty()) {
	    return Optional.empty();
	}

	JSONObject object = new JSONObject(layerEntry.toString());

	if (station != null) {
	    for (String key : STATION_KEYS) {
		String value = station.optString(key, "").trim();
		if (!value.isEmpty() && object.optString(key, "").trim().isEmpty()) {
		    object.put(key, value);
		}
	    }
	}

	// the daily series is chosen when the file is read
	String tsShortName = file.equals(WEEK_FILE) ? layerEntry.optString(TS_SHORT_NAME, "") : DAILY_MEAN;

	object.put(STATION_PATH, stationPath.get());
	object.put(FILE, file);
	object.put(TS_SHORT_NAME, tsShortName);
	object.put(SERIES_ID, buildSeriesId(stationPath.get(), file, tsShortName));
	object.put(LAYER_ID, layer.optString("id"));
	object.put(LAYER_LABEL, layer.optString("label"));

	return Optional.of(new WISKIWeb3Entity(object));
    }

    /**
     * @param layerEntry
     * @return the directory of the station parameter, e.g. "0/23940359/W" (from the ts_path "0/23940359/W/15m.Cmd.RelAbs.P",
     *         otherwise from the site, station and parameter numbers)
     */
    static Optional<String> getStationPath(JSONObject layerEntry) {

	String tsPath = layerEntry.optString(TS_PATH, "").trim();
	if (tsPath.split("/").length == 4) {
	    return Optional.of(tsPath.substring(0, tsPath.lastIndexOf('/')));
	}

	String site = layerEntry.optString(SITE_NO, "0").trim();
	String station = layerEntry.optString(STATION_NO, "").trim();
	String parameter = layerEntry.optString(PARAMETER_NO, "").trim();
	if (parameter.isEmpty()) {
	    parameter = layerEntry.optString(PARAMETER, "").trim();
	}

	if (site.isEmpty() || station.isEmpty() || parameter.isEmpty() || station.contains("/") || parameter.contains("/")) {
	    return Optional.empty();
	}

	return Optional.of(site + "/" + station + "/" + parameter);
    }

    /**
     * Reads the extent and the description of the series from its file, and sets them in the given series
     *
     * @param series
     * @return false if the series has no valid values
     * @throws GSException
     */
    public boolean completeSeries(WISKIWeb3Entity series) throws GSException {

	Optional<SeriesInfo> info = retrieveSeriesInfo(series);
	if (info.isEmpty() || info.get().firstValid == null || info.get().lastValid == null) {
	    return false;
	}

	JSONObject object = series.getObject();

	info.get().header.forEach((key, value) -> {
	    if (!value.isEmpty() && !key.equals(ROWS) && !key.equals("columns")) {
		object.put(key, value);
	    }
	});

	String tsShortName = info.get().tsShortName;
	object.put(TS_SHORT_NAME, tsShortName);
	object.put(SERIES_ID, buildSeriesId(series.getStationPath(), series.getFile(), tsShortName));
	object.put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(info.get().firstValid));
	object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(info.get().lastValid));

	return true;
    }

    /**
     * @param series
     * @return the times of the first and of the last valid values currently published
     * @throws GSException
     */
    public Optional<SimpleEntry<Date, Date>> retrieveExtent(WISKIWeb3Entity series) throws GSException {

	Optional<SeriesInfo> info = retrieveSeriesInfo(series);
	if (info.isEmpty() || info.get().firstValid == null || info.get().lastValid == null) {
	    return Optional.empty();
	}

	return Optional.of(new SimpleEntry<>(info.get().firstValid, info.get().lastValid));
    }

    /**
     * Description and extent of a series read from its file
     */
    static class SeriesInfo {

	/**
	 * The metadata of the series (ts_shortname, ts_name, ts_unitsymbol, ...)
	 */
	Map<String, String> header = new LinkedHashMap<>();
	String tsShortName;
	Date firstValid;
	Date lastValid;
    }

    /**
     * @param series
     * @return
     * @throws GSException
     */
    Optional<SeriesInfo> retrieveSeriesInfo(WISKIWeb3Entity series) throws GSException {

	String path = getFilePath(series);

	if (series.getFile().equals(WEEK_FILE)) {

	    // small file (some tens of KB): read completely
	    Optional<String> text = getOptional(path);
	    if (text.isEmpty()) {
		return Optional.empty();
	    }

	    return parseSeriesInfo(text.get(), series.getTsShortName(), series.getFile());
	}

	RangeResponse head = getRange(path, "bytes=0-" + (HEAD_BYTES - 1));
	if (head == null) {
	    return Optional.empty();
	}

	if (head.complete) {
	    return parseSeriesInfo(head.text, series.getTsShortName(), series.getFile());
	}

	Optional<SeriesInfo> info = parseHead(head.text, series.getTsShortName());
	if (info.isEmpty()) {
	    return info;
	}

	RangeResponse tail = getRange(path, "bytes=-" + TAIL_BYTES);
	if (tail != null) {
	    info.get().lastValid = parseTail(tail.text).orElse(null);
	}

	return info;
    }

    /**
     * @param text a complete file
     * @param tsShortName the ts_shortname (or its prefix) of the series
     * @param file
     * @return
     */
    static Optional<SeriesInfo> parseSeriesInfo(String text, String tsShortName, String file) {

	JSONArray array;
	try {
	    array = new JSONArray(text);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(WISKIWeb3Client.class).warn("Unable to parse series file: {}", e.getMessage());
	    return Optional.empty();
	}

	Optional<JSONObject> block = selectSeries(array, tsShortName, file.equals(WEEK_FILE));
	if (block.isEmpty()) {
	    return Optional.empty();
	}

	SeriesInfo ret = new SeriesInfo();
	for (String key : block.get().keySet()) {
	    if (!key.equals(DATA) && !block.get().isNull(key) && !(block.get().get(key) instanceof JSONArray)) {
		ret.header.put(key, block.get().get(key).toString());
	    }
	}
	ret.tsShortName = block.get().optString(TS_SHORT_NAME, tsShortName);

	TreeMap<Date, BigDecimal> values = parseValues(block.get());
	if (!values.isEmpty()) {
	    ret.firstValid = values.firstKey();
	    ret.lastValid = values.lastKey();
	}

	return Optional.of(ret);
    }

    /**
     * Parses the beginning of a year file: the metadata of its first series and its first rows
     *
     * @param text
     * @param tsShortName the ts_shortname (or its prefix) of the series
     * @return empty if the first series has no rows
     */
    static Optional<SeriesInfo> parseHead(String text, String tsShortName) {

	int dataIndex = text.indexOf("\"" + DATA + "\"");
	if (dataIndex < 0) {
	    return Optional.empty();
	}

	SeriesInfo ret = new SeriesInfo();

	Matcher header = HEADER_PATTERN.matcher(text.substring(0, dataIndex));
	while (header.find()) {
	    ret.header.put(header.group(1), header.group(2));
	}

	if (ret.header.getOrDefault(ROWS, "").equals("0")) {
	    return Optional.empty();
	}

	String first = ret.header.getOrDefault(TS_SHORT_NAME, "");

	if (first.startsWith(tsShortName)) {

	    ret.tsShortName = first;

	} else if (first.startsWith("Day.Min") || first.startsWith("Day.Max")) {

	    // the daily mean is published together with the daily minimum and maximum: the header of the first series (e.g. the
	    // unit) is valid also for the mean
	    ret.tsShortName = tsShortName;
	    ret.header.remove(TS_SHORT_NAME);
	    ret.header.remove(TS_NAME);

	} else {

	    ret.tsShortName = first;
	}

	// the series of a file have the same timestamps: the first rows of the first series give the begin
	int end = text.indexOf("]]", dataIndex);
	String rows = end < 0 ? text.substring(dataIndex) : text.substring(dataIndex, end + 1);

	Date firstRow = null;
	Matcher matcher = ROW_PATTERN.matcher(rows);
	while (matcher.find()) {
	    Optional<Date> date = parseDate(matcher.group(1));
	    if (date.isEmpty()) {
		continue;
	    }
	    if (firstRow == null) {
		firstRow = date.get();
	    }
	    if (!matcher.group(2).equals("null")) {
		ret.firstValid = date.get();
		break;
	    }
	}

	if (ret.firstValid == null) {
	    ret.firstValid = firstRow;
	}

	return ret.firstValid == null ? Optional.empty() : Optional.of(ret);
    }

    /**
     * @param text the end of a year file
     * @return the time of the last valid row of the last series
     */
    static Optional<Date> parseTail(String text) {

	int dataIndex = text.lastIndexOf("\"" + DATA + "\"");
	String rows = dataIndex < 0 ? text : text.substring(dataIndex);

	Date last = null;
	Matcher matcher = ROW_PATTERN.matcher(rows);
	while (matcher.find()) {
	    if (!matcher.group(2).equals("null")) {
		Optional<Date> date = parseDate(matcher.group(1));
		if (date.isPresent()) {
		    last = date.get();
		}
	    }
	}

	return Optional.ofNullable(last);
    }

    /**
     * @param series the series of a file
     * @param tsShortName the ts_shortname (or its prefix) of the series to select
     * @param anyWithData true to select, if no series matches, the first one having rows (the week files do not always contain
     *            the series of the layer, e.g. moving sums of precipitation)
     * @return
     */
    static Optional<JSONObject> selectSeries(JSONArray series, String tsShortName, boolean anyWithData) {

	List<JSONObject> list = new ArrayList<>();
	for (int i = 0; i < series.length(); i++) {
	    if (series.get(i) instanceof JSONObject) {
		list.add(series.getJSONObject(i));
	    }
	}

	Optional<JSONObject> ret = list.stream().filter(s -> s.optString(TS_SHORT_NAME).equals(tsShortName)).findFirst();

	if (ret.isEmpty() && tsShortName != null && !tsShortName.isEmpty()) {
	    ret = list.stream().filter(s -> s.optString(TS_SHORT_NAME).startsWith(tsShortName)).findFirst();
	}

	if (ret.isEmpty() && anyWithData) {
	    ret = list.stream().filter(s -> s.optJSONArray(DATA) != null && !s.getJSONArray(DATA).isEmpty()).findFirst();
	}

	return ret;
    }

    /**
     * Retrieves the valid values of the given period, from the week file or from the year file of the series. The files are
     * replaced periodically by the site, so only the currently published period is available
     *
     * @param series
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(WISKIWeb3Entity series, Date begin, Date end) throws GSException {

	String path = getFilePath(series);

	Optional<String> text = getOptional(path);
	if (text.isEmpty()) {
	    return new TreeMap<>();
	}

	Optional<JSONObject> block = selectSeries(new JSONArray(text.get()), series.getTsShortName(),
		series.getFile().equals(WEEK_FILE));
	if (block.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).warn("Series {} not found in {}", series.getTsShortName(), path);
	    return new TreeMap<>();
	}

	return new TreeMap<>(parseValues(block.get()).subMap(begin, true, end, true));
    }

    /**
     * @param series a time series of a file, with rows [timestamp, value, ...]
     * @return the valid values
     */
    static TreeMap<Date, BigDecimal> parseValues(JSONObject series) {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	JSONArray data = series.optJSONArray(DATA);
	if (data == null) {
	    return ret;
	}

	for (int i = 0; i < data.length(); i++) {

	    JSONArray row = data.optJSONArray(i);
	    if (row == null || row.length() < 2 || row.isNull(1)) {
		continue;
	    }

	    Optional<Date> date = parseDate(row.optString(0, null));
	    BigDecimal value = row.optBigDecimal(1, null);

	    if (date.isPresent() && value != null) {
		ret.put(date.get(), value);
	    }
	}

	return ret;
    }

    /**
     * Information about the publisher of the site, read from the web application
     */
    public static class SiteInfo {

	private String organisation;
	private String license;

	/**
	 * @return
	 */
	public Optional<String> getOrganisation() {

	    return Optional.ofNullable(organisation);
	}

	/**
	 * @return
	 */
	public Optional<String> getLicense() {

	    return Optional.ofNullable(license);
	}
    }

    /**
     * Reads the publisher and the licence of the data from the web application (title of the page and configuration)
     *
     * @param appUrl the URL of the web application, e.g. https://www.hlnug.de/static/pegel/wiskiweb3/webpublic/
     * @return
     */
    public SiteInfo retrieveSiteInfo(String appUrl) {

	SiteInfo ret = new SiteInfo();

	String url = normalizeEndpoint(appUrl);

	Optional<String> config = Optional.empty();
	try {
	    config = getOptionalURL(url + APP_CONFIG_PATH);
	} catch (GSException e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Configuration of {} not available", url);
	}

	try {
	    ret.organisation = getOptionalURL(url).flatMap(WISKIWeb3Client::extractOrganisation).orElse(null);
	} catch (GSException e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Page {} not available", url);
	}

	if (config.isPresent()) {
	    ret.license = extractLicense(config.get()).orElse(null);
	    if (ret.organisation == null) {
		ret.organisation = extractCreditsOrganisation(config.get()).orElse(null);
	    }
	}

	return ret;
    }

    /**
     * @param endpoint the data directory, e.g. https://www.hlnug.de/static/pegel/wiskiweb3/data/
     * @return the default location of the web application, e.g. https://www.hlnug.de/static/pegel/wiskiweb3/webpublic/ (the
     *         application refers to the data directory as "../data")
     */
    public static String getDefaultAppURL(String endpoint) {

	String normalized = normalizeEndpoint(endpoint);
	String parent = normalized.substring(0, normalized.length() - 1);
	parent = parent.substring(0, parent.lastIndexOf('/') + 1);

	return parent + "webpublic/";
    }

    /**
     * @param html the page of the web application
     * @return the organisation, from the page title, e.g. "Hessisches Landesamt für Naturschutz, Umwelt und Geologie" from
     *         "Hessisches Landesamt für Naturschutz, Umwelt und Geologie: Wasser / Aktuelle Messdaten"
     */
    static Optional<String> extractOrganisation(String html) {

	Matcher matcher = TITLE_PATTERN.matcher(html);
	if (!matcher.find()) {
	    return Optional.empty();
	}

	String title = matcher.group(1).replaceAll("\\s+", " ").trim();

	for (String separator : new String[] { ": ", " - ", " | " }) {
	    int index = title.indexOf(separator);
	    if (index > 0) {
		title = title.substring(0, index).trim();
		break;
	    }
	}

	if (title.isEmpty() || title.toUpperCase().contains("WISKI") || title.toUpperCase().contains("KISTERS")) {
	    return Optional.empty();
	}

	return Optional.of(title);
    }

    /**
     * @param config the configuration of the web application (app.json)
     * @return the organisation from the chart credits, e.g. "HLNUG" from "Ungeprüfte Rohdaten © HLNUG"
     */
    static Optional<String> extractCreditsOrganisation(String config) {

	for (String credits : findCredits(config)) {
	    Matcher matcher = Pattern.compile("©\\s*([^<,;]+)").matcher(credits);
	    if (matcher.find()) {
		return Optional.of(matcher.group(1).trim());
	    }
	}

	return Optional.empty();
    }

    /**
     * Looks for a licence statement in the texts of the configuration of the web application (e.g. the description of the
     * download page) and for the credits of the charts
     *
     * @param config the configuration of the web application (app.json)
     * @return e.g. "Creative Commons Attribution 4.0 International (CC BY 4.0) - https://creativecommons.org/licenses/by/4.0/legalcode.de
     *         - Attribution: Hessisches Landesamt für Naturschutz, Umwelt und Geologie (HLNUG) - Ungeprüfte Rohdaten © HLNUG"
     */
    static Optional<String> extractLicense(String config) {

	List<String> texts = new ArrayList<>();
	try {
	    collectStrings(new JSONObject(config), null, texts, new ArrayList<>());
	} catch (Exception e) {
	    return Optional.empty();
	}

	StringBuilder builder = new StringBuilder();

	for (String text : texts) {

	    String plain = stripHTML(text);

	    Matcher url = LICENSE_URL_PATTERN.matcher(plain);
	    Matcher label = LICENSE_LABEL_PATTERN.matcher(plain);

	    boolean hasURL = url.find();
	    boolean hasLabel = label.find();

	    if (!hasURL && !hasLabel) {
		continue;
	    }

	    if (hasLabel) {
		builder.append(getLicenseName(label.group(1))).append(" (").append(label.group(1)).append(")");
	    }
	    if (hasURL) {
		builder.append(builder.length() > 0 ? " - " : "").append(url.group());
	    }

	    Matcher attribution = ATTRIBUTION_PATTERN.matcher(plain);
	    if (attribution.find()) {
		builder.append(" - Attribution: ").append(attribution.group(1).trim());
	    }

	    break;
	}

	List<String> credits = findCredits(config);
	if (!credits.isEmpty()) {
	    builder.append(builder.length() > 0 ? " - " : "").append(stripHTML(credits.get(0)));
	}

	return builder.length() == 0 ? Optional.empty() : Optional.of(builder.toString());
    }

    /**
     * @param label e.g. "CC BY 4.0"
     * @return e.g. "Creative Commons Attribution 4.0 International"
     */
    static String getLicenseName(String label) {

	String normalized = label.toUpperCase().replace('-', ' ');

	if (normalized.startsWith("CC0")) {
	    return "Creative Commons Zero";
	}

	if (normalized.startsWith("DL DE/")) {
	    return "Datenlizenz Deutschland";
	}

	StringBuilder ret = new StringBuilder("Creative Commons Attribution");
	if (normalized.contains(" NC")) {
	    ret.append("-NonCommercial");
	}
	if (normalized.contains(" SA")) {
	    ret.append("-ShareAlike");
	}
	if (normalized.contains(" ND")) {
	    ret.append("-NoDerivatives");
	}

	Matcher version = Pattern.compile("(\\d\\.\\d)").matcher(normalized);
	if (version.find()) {
	    ret.append(" ").append(version.group(1));
	    if (version.group(1).equals("4.0")) {
		ret.append(" International");
	    }
	}

	return ret.toString();
    }

    /**
     * @param config
     * @return the distinct texts of the chart credits, e.g. "<span style='font-weight:bold;'>Ungeprüfte Rohdaten</span> ©
     *         HLNUG<br>Alle Zeitstempel in MEZ"
     */
    private static List<String> findCredits(String config) {

	List<String> ret = new ArrayList<>();
	try {
	    collectStrings(new JSONObject(config), null, new ArrayList<>(), ret);
	} catch (Exception e) {
	    // not a valid configuration
	}

	return ret;
    }

    /**
     * @param object
     * @param parentKey
     * @param texts the distinct texts found
     * @param credits the distinct texts of the credits
     */
    private static void collectStrings(Object object, String parentKey, List<String> texts, List<String> credits) {

	if (object instanceof JSONObject) {

	    JSONObject json = (JSONObject) object;
	    for (String key : json.keySet()) {

		Object value = json.get(key);
		if ("credits".equals(parentKey) && key.equals("text") && value instanceof String) {
		    if (!credits.contains(value)) {
			credits.add((String) value);
		    }
		} else {
		    collectStrings(value, key, texts, credits);
		}
	    }

	} else if (object instanceof JSONArray) {

	    JSONArray array = (JSONArray) object;
	    for (int i = 0; i < array.length(); i++) {
		collectStrings(array.get(i), parentKey, texts, credits);
	    }

	} else if (object instanceof String && !texts.contains(object)) {

	    texts.add((String) object);
	}
    }

    /**
     * @param text
     * @return
     */
    static String stripHTML(String text) {

	return text.replaceAll("<br\\s*/?>", " ").replaceAll("<[^>]+>", "").replaceAll("\\s+", " ").trim();
    }

    /**
     * @param stationPath e.g. "0/23940359/W"
     * @param file e.g. "week"
     * @param tsShortName e.g. "15m.Cmd.RelAbs.P"
     * @return e.g. "0/23940359/W|week|15m.Cmd.RelAbs.P"
     */
    static String buildSeriesId(String stationPath, String file, String tsShortName) {

	return stationPath + SERIES_ID_SEPARATOR + file + SERIES_ID_SEPARATOR + tsShortName;
    }

    /**
     * @param seriesId
     * @return the series identified by the given identifier, or null if not valid
     */
    public static WISKIWeb3Entity parseSeriesId(String seriesId) {

	if (seriesId == null) {
	    return null;
	}

	String[] split = seriesId.split(Pattern.quote(SERIES_ID_SEPARATOR), -1);
	if (split.length != 3 || split[0].split("/").length != 3 || split[0].contains("..")
		|| !(split[1].equals(WEEK_FILE) || split[1].equals(YEAR_FILE))) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, seriesId);
	object.put(STATION_PATH, split[0]);
	object.put(STATION_NO, split[0].split("/")[1]);
	object.put(FILE, split[1]);
	object.put(TS_SHORT_NAME, split[2]);

	return new WISKIWeb3Entity(object);
    }

    /**
     * @param series
     * @return e.g. "internet/stations/0/23940359/W/week.json"
     */
    static String getFilePath(WISKIWeb3Entity series) {

	return DATA_PATH + "stations/" + series.getStationPath() + "/" + series.getFile() + ".json";
    }

    /**
     * @param text e.g. "2026-10-19T23:00:00.000+01:00"
     * @return
     */
    static Optional<Date> parseDate(String text) {

	if (text == null || text.isEmpty() || text.equals("null")) {
	    return Optional.empty();
	}

	try {
	    return Optional.of(Date.from(OffsetDateTime.parse(text).toInstant()));
	} catch (DateTimeParseException e) {
	    return ISO8601DateTimeUtils.parseISO8601ToDate(text);
	}
    }

    /**
     * Response to a range request
     */
    static class RangeResponse {

	String text;

	/**
	 * True if the whole file was returned
	 */
	boolean complete;
    }

    /**
     * @param path
     * @param range e.g. "bytes=0-32767" or "bytes=-4096"
     * @return the response, or null if the file is not available
     * @throws GSException
     */
    private RangeResponse getRange(String path, String range) throws GSException {

	try {

	    HttpResponse<InputStream> response = downloader.downloadResponse(endpoint + path, HttpHeaderUtils.build("Range", range));

	    RangeResponse ret = new RangeResponse();

	    try (InputStream stream = response.body()) {
		ret.text = stream == null ? "" : IOUtils.toString(stream, StandardCharsets.UTF_8);
	    }

	    if (response.statusCode() == 200) {

		ret.complete = true;

	    } else if (response.statusCode() == 206) {

		// e.g. "bytes 0-32767/1693337"
		String contentRange = response.headers().firstValue("Content-Range").orElse("");
		ret.complete = isWholeFile(contentRange);

	    } else if (response.statusCode() == 404 || response.statusCode() == 416) {

		return null;

	    } else {

		throw createException("WISKIweb3 " + path + " returned HTTP " + response.statusCode(), null);
	    }

	    return ret;

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("WISKIweb3 " + path + " request failed", e);
	}
    }

    /**
     * @param contentRange e.g. "bytes 0-32767/1693337"
     * @return true if the range covers the whole file
     */
    static boolean isWholeFile(String contentRange) {

	Matcher matcher = Pattern.compile("bytes (\\d+)-(\\d+)/(\\d+)").matcher(contentRange);
	if (!matcher.find()) {
	    return false;
	}

	return Long.parseLong(matcher.group(1)) == 0 && Long.parseLong(matcher.group(2)) + 1 >= Long.parseLong(matcher.group(3));
    }

    /**
     * @param path
     * @return
     * @throws GSException
     */
    private String get(String path) throws GSException {

	return getOptional(path).orElseThrow(() -> createException("WISKIweb3 " + endpoint + path + " not available", null));
    }

    /**
     * @param path the path relative to the endpoint
     * @return the response, or empty if the resource is not available
     * @throws GSException
     */
    private Optional<String> getOptional(String path) throws GSException {

	return getOptionalURL(endpoint + path);
    }

    /**
     * @param url
     * @return the response, or empty if the resource is not available
     * @throws GSException
     */
    private Optional<String> getOptionalURL(String url) throws GSException {

	try {

	    HttpResponse<InputStream> response = downloader.downloadResponse(url);

	    String text;
	    try (InputStream stream = response.body()) {
		text = stream == null ? "" : IOUtils.toString(stream, StandardCharsets.UTF_8);
	    }

	    if (response.statusCode() == 404 || response.statusCode() == 403) {
		return Optional.empty();
	    }

	    if (response.statusCode() != 200) {
		throw createException("WISKIweb3 " + url + " returned HTTP " + response.statusCode(), null);
	    }

	    return text.isBlank() ? Optional.empty() : Optional.of(text);

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("WISKIweb3 " + url + " request failed", e);
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
     * @param endpoint
     * @return the endpoint ending with "/" (a trailing "internet/" is removed)
     */
    static String normalizeEndpoint(String endpoint) {

	if (endpoint == null || endpoint.isBlank()) {
	    return DEFAULT_ENDPOINT;
	}

	String normalized = endpoint.trim();
	if (normalized.endsWith("index.html")) {
	    normalized = normalized.substring(0, normalized.length() - "index.html".length());
	}
	if (!normalized.endsWith("/")) {
	    normalized += "/";
	}
	if (normalized.endsWith("/" + DATA_PATH)) {
	    normalized = normalized.substring(0, normalized.length() - DATA_PATH.length());
	}

	return normalized;
    }
}
