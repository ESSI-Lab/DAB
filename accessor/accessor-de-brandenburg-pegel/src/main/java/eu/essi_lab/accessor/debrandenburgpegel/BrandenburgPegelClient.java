package eu.essi_lab.accessor.debrandenburgpegel;

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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.CRS;
import eu.essi_lab.model.resource.data.CRSUtils;
import eu.essi_lab.model.resource.data.EPSGCRS;

/**
 * Client of the Pegelportal of the Landesamt für Umwelt Brandenburg (https://pegelportal.brandenburg.de/).<br>
 * The gauges are listed by the OpenLayers features of the map of the start page, with their ETRS89 / UTM zone 33N coordinates
 * (EPSG:25833), the measured parameters (W = water level, Q = discharge) and the time of the latest value. Only the gauges published
 * by the portal itself are considered (the map also shows gauges of neighbouring portals, linked to their own pages).<br>
 * Observations are raw data (Rohdaten) with a 15 minutes resolution, and only a rolling window is kept:
 * <ul>
 * <li>the gauge page (messstelle.php) embeds the water level and discharge values of the last year, since the midnight of the
 * same day of the previous year</li>
 * <li>the water level values of the last 30 days, up to the previous day, are published as a static CSV file
 * (source/download/{pkz}_wasserstand.csv)</li>
 * <li>the water level values of the last 7 days, up to now, are returned as CSV by a form of the gauge page</li>
 * </ul>
 * All the times are given in Central European Time (MEZ, UTC+1) all year round; the values of the gauge page are given as
 * epoch milliseconds of the MEZ wall clock time.
 *
 * @author boldrini
 */
public class BrandenburgPegelClient {

    public static final String DEFAULT_ENDPOINT = "https://pegelportal.brandenburg.de/";

    static final String START_PAGE = "start.php";
    static final String STATION_PAGE = "messstelle.php";
    static final String DOWNLOAD_PATH = "source/download/";
    static final String CSV_SUFFIX = "_wasserstand.csv";

    /**
     * The pages are encoded in ISO-8859-15
     */
    static final Charset CHARSET = Charset.forName("ISO-8859-15");

    /**
     * Central European Time (MEZ), used all year round
     */
    static final ZoneOffset MEZ = ZoneOffset.ofHours(1);

    /**
     * Value of the missing observations (Fehlwerte) in the CSV files
     */
    static final BigDecimal MISSING_VALUE = new BigDecimal("-777");

    /**
     * Rolling windows of the gauge page, of the static CSV file and of the CSV of the last week, starting at midnight (MEZ)
     */
    static final int PAGE_WINDOW_YEARS = 1;
    static final int CSV_WINDOW_DAYS = 30;
    static final int WEEK_WINDOW_DAYS = 7;

    /**
     * Value of the feature property {@link #LAYER} of the gauges
     */
    static final String GAUGE_LAYER = "bb";

    /**
     * ETRS89 / UTM zone 33N
     */
    private static final CRS UTM_33N = new EPSGCRS(25833);

    private static final Pattern FEATURE_PATTERN = Pattern.compile("new ol\\.Feature\\(\\{(.*?)\\}\\)", Pattern.DOTALL);
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("(\\w+): '([^']*)'");
    private static final Pattern POINT_PATTERN = Pattern.compile("ol\\.geom\\.Point\\(\\[\\s*([\\d.]+)\\s*,\\s*([\\d.]+)\\s*\\]\\)");
    private static final Pattern RIVER_BASIN_PATTERN = Pattern.compile("flussgebiet\\.php\\?fgid=(\\d+)&thema=karte'[^>]*>([^<]+)<");
    private static final Pattern CSV_ROW_PATTERN = Pattern.compile("^\"?(\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2})\"?;\\s*\"?([^\";]*)\"?");
    private static final Pattern PAGE_ROW_PATTERN = Pattern.compile("^data\\.push\\(\\[(\\d+),\\s*([^\\]]+)\\]\\)");
    private static final Pattern TAB_PATTERN = Pattern.compile("<!-- (Tab\\d+)");
    private static final Pattern ENTITY_PATTERN = Pattern.compile("&#(\\d+);");

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // properties of the features of the gauges
    public static final String LAYER = "pegel";
    public static final String STATION_NO = "pkz";
    public static final String STATION_NAME = "name";
    public static final String WATERBODY = "gewaesser";
    public static final String RIVER_KM = "flusskm";
    public static final String RIVER_POSITION = "flussmuendung";
    public static final String OPERATOR = "betreiber";
    public static final String PARAMETERS = "pgparameter";
    public static final String RIVER_BASIN_ID = "fgid";
    public static final String STATION_URL = "pegelurl";
    public static final String IN_BRANDENBURG = "pegelinbb";
    public static final String LATEST_DATE = "datum";
    public static final String LATEST_TIME = "zeit";
    public static final String EASTING = "x";
    public static final String NORTHING = "y";

    // keys added by the client and by the connector
    public static final String LAT = "latitude";
    public static final String LON = "longitude";
    public static final String RIVER_BASIN = "riverBasin";
    public static final String PARAMETER = "parameter";
    public static final String SERIES_ID = "seriesId";
    public static final String BEGIN = "begin";
    public static final String END = "end";

    static final String SERIES_ID_SEPARATOR = "|";

    private final String endpoint;
    private final Downloader downloader;

    /**
     *
     */
    public BrandenburgPegelClient() {

	this(DEFAULT_ENDPOINT);
    }

    /**
     * @param endpoint
     */
    public BrandenburgPegelClient(String endpoint) {

	this.endpoint = normalizeEndpoint(endpoint);
	this.downloader = new Downloader();
	this.downloader.setConnectionTimeout(TimeUnit.SECONDS, 30);
	this.downloader.setResponseTimeout(TimeUnit.MINUTES, 3);
	this.downloader.setRetryPolicy(r -> r.statusCode() >= 500 || r.statusCode() == 429, 3, TimeUnit.SECONDS, 10);
    }

    /**
     * Retrieves the time series of the given parameters at the gauges of the portal, from the map of the start page (a single
     * request)
     *
     * @param parameters
     * @return the series sorted by gauge number and parameter
     * @throws GSException
     */
    public List<BrandenburgPegelEntity> retrieveSeries(Collection<BrandenburgPegelParameter> parameters) throws GSException {

	return parseStartPage(get(START_PAGE), parameters, new Date());
    }

    /**
     * @param page the start page
     * @param parameters
     * @param now
     * @return the series sorted by gauge number and parameter
     */
    static List<BrandenburgPegelEntity> parseStartPage(String page, Collection<BrandenburgPegelParameter> parameters, Date now) {

	Map<String, String> riverBasins = parseRiverBasins(page);

	List<BrandenburgPegelEntity> ret = new ArrayList<>();
	List<String> stations = new ArrayList<>();

	for (JSONObject feature : parseFeatures(page)) {

	    if (!isPortalGauge(feature) || stations.contains(feature.optString(STATION_NO))) {
		continue;
	    }

	    stations.add(feature.optString(STATION_NO));

	    String basin = riverBasins.get(feature.optString(RIVER_BASIN_ID));
	    if (basin != null) {
		feature.put(RIVER_BASIN, basin);
	    }

	    for (BrandenburgPegelParameter parameter : parameters) {
		createSeries(feature, parameter, now).ifPresent(ret::add);
	    }
	}

	ret.sort(Comparator.comparing(BrandenburgPegelEntity::getStationNumber).thenComparing(BrandenburgPegelEntity::getSeriesId));

	return ret;
    }

    /**
     * @param page the start page
     * @return the properties of the features of the gauges, with the coordinates
     */
    static List<JSONObject> parseFeatures(String page) {

	List<JSONObject> ret = new ArrayList<>();

	Matcher matcher = FEATURE_PATTERN.matcher(page);
	while (matcher.find()) {

	    String body = matcher.group(1);

	    JSONObject feature = new JSONObject();

	    Matcher property = PROPERTY_PATTERN.matcher(body);
	    while (property.find()) {
		if (!feature.has(property.group(1))) {
		    feature.put(property.group(1), decodeEntities(property.group(2)).trim());
		}
	    }

	    if (!GAUGE_LAYER.equals(feature.optString(LAYER)) || feature.optString(STATION_NO).isEmpty()) {
		continue;
	    }

	    Matcher point = POINT_PATTERN.matcher(body);
	    if (point.find()) {
		feature.put(EASTING, point.group(1));
		feature.put(NORTHING, point.group(2));
	    }

	    ret.add(feature);
	}

	return ret;
    }

    /**
     * @param page the start page
     * @return the names of the river basins (Flussgebiete) by identifier
     */
    static Map<String, String> parseRiverBasins(String page) {

	Map<String, String> ret = new LinkedHashMap<>();

	Matcher matcher = RIVER_BASIN_PATTERN.matcher(page);
	while (matcher.find()) {
	    ret.putIfAbsent(matcher.group(1), decodeEntities(matcher.group(2)).trim());
	}

	return ret;
    }

    /**
     * @param feature
     * @return true if the gauge is published by the portal, false if the map links it to another portal (e.g. PEGELONLINE, IMGW,
     *         Saxony, Berlin)
     */
    static boolean isPortalGauge(JSONObject feature) {

	String url = feature.optString(STATION_URL, "");
	if (url.isEmpty()) {
	    return true;
	}

	try {
	    return URI.create(url).getHost().equalsIgnoreCase(URI.create(DEFAULT_ENDPOINT).getHost());
	} catch (Exception e) {
	    return false;
	}
    }

    /**
     * @param feature the feature of the gauge
     * @param parameter
     * @param now
     * @return the series, or empty if the parameter is not measured at the gauge or no value of the current window is available
     */
    static Optional<BrandenburgPegelEntity> createSeries(JSONObject feature, BrandenburgPegelParameter parameter, Date now) {

	String stationNumber = feature.optString(STATION_NO, "");
	if (stationNumber.isEmpty()) {
	    return Optional.empty();
	}

	List<String> measured = Arrays.asList(feature.optString(PARAMETERS, "").toUpperCase().split("/"));
	if (!measured.contains(parameter.name())) {
	    return Optional.empty();
	}

	Optional<Date> latest = parseDate(feature.optString(LATEST_DATE, "") + " " + feature.optString(LATEST_TIME, ""));
	Date begin = getPageWindowStart(now);

	if (latest.isEmpty() || latest.get().before(begin)) {
	    return Optional.empty();
	}

	JSONObject object = new JSONObject(feature.toString());

	object.put(PARAMETER, parameter.name());
	object.put(SERIES_ID, buildSeriesId(stationNumber, parameter));
	object.put(BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(begin));
	object.put(END, ISO8601DateTimeUtils.getISO8601DateTime(latest.get()));

	try {

	    double easting = Double.parseDouble(feature.optString(EASTING));
	    double northing = Double.parseDouble(feature.optString(NORTHING));

	    toWGS84(easting, northing).ifPresent(latLon -> {
		object.put(LAT, latLon.getKey());
		object.put(LON, latLon.getValue());
	    });

	} catch (NumberFormatException e) {

	    GSLoggerFactory.getLogger(BrandenburgPegelClient.class).warn("Invalid coordinates of gauge {}", stationNumber);
	}

	return Optional.of(new BrandenburgPegelEntity(object));
    }

    /**
     * @param series
     * @return the time of the latest valid water level of the gauge, from the CSV of the last week (also used for the discharge,
     *         which is derived from the water level)
     * @throws GSException
     */
    public Optional<Date> retrieveLatestObservation(BrandenburgPegelEntity series) throws GSException {

	TreeMap<Date, BigDecimal> values = readWeekCsv(series.getStationNumber());

	return values.isEmpty() ? Optional.empty() : Optional.of(values.lastKey());
    }

    /**
     * Retrieves the valid observations of the given period. Recent water levels are read from the CSV of the last week and from
     * the static CSV of the last 30 days, while older water levels and the discharge are read from the gauge page (last year)
     *
     * @param series
     * @param begin
     * @param end
     * @return
     * @throws GSException
     */
    public TreeMap<Date, BigDecimal> retrieveObservations(BrandenburgPegelEntity series, Date begin, Date end) throws GSException {

	BrandenburgPegelParameter parameter = getParameter(series);
	Date now = new Date();

	TreeMap<Date, BigDecimal> values = new TreeMap<>();

	if (parameter == BrandenburgPegelParameter.W && !begin.before(getWeekWindowStart(now))) {

	    values.putAll(readWeekCsv(series.getStationNumber()));

	} else if (parameter == BrandenburgPegelParameter.W && !begin.before(getCsvWindowStart(now))) {

	    values.putAll(readCsv(series.getStationNumber()));
	    if (values.isEmpty() || values.lastKey().before(end)) {
		values.putAll(readWeekCsv(series.getStationNumber()));
	    }

	} else {

	    values.putAll(readStationPage(series, parameter));
	}

	if (begin.after(end)) {
	    return new TreeMap<>();
	}

	return new TreeMap<>(values.subMap(begin, true, end, true));
    }

    /**
     * @param stationNumber
     * @return the valid water levels of the last 30 days, up to the previous day
     * @throws GSException
     */
    TreeMap<Date, BigDecimal> readCsv(String stationNumber) throws GSException {

	return getOptional(DOWNLOAD_PATH + stationNumber + CSV_SUFFIX).map(BrandenburgPegelClient::parseCsv).orElse(new TreeMap<>());
    }

    /**
     * @param stationNumber
     * @return the valid water levels of the last 7 days, up to now
     * @throws GSException
     */
    TreeMap<Date, BigDecimal> readWeekCsv(String stationNumber) throws GSException {

	String path = STATION_PAGE + "?pkz=" + stationNumber;

	HashMap<String, String> form = new HashMap<>();
	form.put("smode", "c");
	form.put("sdatum", "woche");

	try {

	    HttpRequest request = HttpRequestUtils.build(MethodWithBody.POST, endpoint + path, form);
	    HttpResponse<InputStream> response = downloader.downloadResponse(request);

	    String text;
	    try (InputStream stream = response.body()) {
		text = stream == null ? "" : IOUtils.toString(stream, CHARSET);
	    }

	    if (response.statusCode() != 200) {
		GSLoggerFactory.getLogger(getClass()).warn("Pegelportal weekly CSV of {} returned HTTP {}", stationNumber,
			response.statusCode());
		return new TreeMap<>();
	    }

	    return parseCsv(text);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("Pegelportal weekly CSV of " + stationNumber + " request failed", e);
	}
    }

    /**
     * @param text rows "Datum;Wert", e.g. "27.09.2026 00:00";68, with a header line
     * @return the valid values
     */
    static TreeMap<Date, BigDecimal> parseCsv(String text) {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	for (String line : text.split("\\r?\\n")) {

	    Matcher matcher = CSV_ROW_PATTERN.matcher(line.trim());
	    if (!matcher.find()) {
		continue;
	    }

	    Optional<Date> date = parseDate(matcher.group(1));
	    BigDecimal value = parseDecimal(matcher.group(2));

	    if (date.isPresent() && value != null) {
		ret.put(date.get(), value);
	    }
	}

	return ret;
    }

    /**
     * Reads the observations of the last year embedded in the gauge page. The page (some MB) is streamed and the download is
     * interrupted at the end of the tab of the parameter
     *
     * @param series
     * @param parameter
     * @return the valid values
     * @throws GSException
     */
    TreeMap<Date, BigDecimal> readStationPage(BrandenburgPegelEntity series, BrandenburgPegelParameter parameter) throws GSException {

	String path = STATION_PAGE + "?fgid=" + series.getRiverBasinId() + "&pkz=" + series.getStationNumber() + "&thema="
		+ parameter.getTheme();

	try {

	    HttpResponse<InputStream> response = downloader.downloadResponse(endpoint + path);

	    try (InputStream stream = response.body()) {

		if (response.statusCode() != 200) {
		    GSLoggerFactory.getLogger(getClass()).warn("Pegelportal page {} returned HTTP {}", path, response.statusCode());
		    return new TreeMap<>();
		}

		return parseStationPage(new BufferedReader(new InputStreamReader(stream, CHARSET)), parameter);
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("Pegelportal page " + path + " request failed", e);
	}
    }

    /**
     * @param reader the gauge page
     * @param parameter
     * @return the valid values of the chart of the last year in the tab of the parameter
     * @throws Exception
     */
    static TreeMap<Date, BigDecimal> parseStationPage(BufferedReader reader, BrandenburgPegelParameter parameter) throws Exception {

	TreeMap<Date, BigDecimal> ret = new TreeMap<>();

	boolean inTab = false;
	String line;

	while ((line = reader.readLine()) != null) {

	    Matcher tab = TAB_PATTERN.matcher(line);
	    if (tab.find()) {
		if (tab.group(1).equals(parameter.getTab())) {
		    inTab = true;
		} else if (inTab) {
		    return ret;
		}
	    }

	    if (!inTab) {
		continue;
	    }

	    Matcher row = PAGE_ROW_PATTERN.matcher(line.trim());
	    if (!row.find()) {
		continue;
	    }

	    BigDecimal value = parseDecimal(row.group(2));
	    if (value != null) {
		// epoch of the MEZ wall clock time
		ret.put(new Date(Long.parseLong(row.group(1)) - TimeUnit.HOURS.toMillis(1)), value);
	    }
	}

	return ret;
    }

    /**
     * @param now
     * @return the begin of the observations of the gauge page: the midnight (MEZ) of the same day of the previous year
     */
    static Date getPageWindowStart(Date now) {

	return Date.from(getMidnight(now).minusYears(PAGE_WINDOW_YEARS).toInstant());
    }

    /**
     * @param now
     * @return the begin of the static CSV of the last 30 days
     */
    static Date getCsvWindowStart(Date now) {

	return Date.from(getMidnight(now).minusDays(CSV_WINDOW_DAYS).toInstant());
    }

    /**
     * @param now
     * @return the begin of the CSV of the last week
     */
    static Date getWeekWindowStart(Date now) {

	return Date.from(getMidnight(now).minusDays(WEEK_WINDOW_DAYS).toInstant());
    }

    /**
     * @param now
     * @return
     */
    private static ZonedDateTime getMidnight(Date now) {

	return now.toInstant().atZone(MEZ).truncatedTo(ChronoUnit.DAYS);
    }

    /**
     * @param stationNumber
     * @param parameter
     * @return
     */
    static String buildSeriesId(String stationNumber, BrandenburgPegelParameter parameter) {

	return stationNumber + SERIES_ID_SEPARATOR + parameter.name();
    }

    /**
     * @param onlineName the series identifier, optionally followed by the river basin, e.g. "0491101|W|1"
     * @return the series identified by the given name, or null if not valid
     */
    public static BrandenburgPegelEntity parseSeriesId(String onlineName) {

	if (onlineName == null) {
	    return null;
	}

	String[] split = onlineName.split(Pattern.quote(SERIES_ID_SEPARATOR));
	if (split.length < 2 || split[0].isBlank()) {
	    return null;
	}

	String stationNumber = split[0].trim();
	Optional<BrandenburgPegelParameter> parameter = BrandenburgPegelParameter.decode(split[1]);
	if (parameter.isEmpty()) {
	    return null;
	}

	JSONObject object = new JSONObject();
	object.put(SERIES_ID, buildSeriesId(stationNumber, parameter.get()));
	object.put(STATION_NO, stationNumber);
	object.put(PARAMETER, parameter.get().name());
	if (split.length > 2 && !split[2].isBlank()) {
	    object.put(RIVER_BASIN_ID, split[2].trim());
	}

	return new BrandenburgPegelEntity(object);
    }

    /**
     * @param easting UTM 33N (EPSG:25833) easting
     * @param northing UTM 33N (EPSG:25833) northing
     * @return latitude and longitude (WGS84)
     */
    static Optional<SimpleEntry<Double, Double>> toWGS84(double easting, double northing) {

	try {

	    SimpleEntry<Double, Double> latLon = CRSUtils.translatePoint(new SimpleEntry<>(easting, northing), UTM_33N,
		    CRS.EPSG_4326());

	    double lat = round(latLon.getKey());
	    double lon = round(latLon.getValue());

	    if (Math.abs(lat) > 90 || Math.abs(lon) > 180) {
		return Optional.empty();
	    }

	    return Optional.of(new SimpleEntry<>(lat, lon));

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(BrandenburgPegelClient.class).warn("Unable to convert coordinates {} {}: {}", easting, northing,
		    e.getMessage());
	    return Optional.empty();
	}
    }

    /**
     * @param value
     * @return the value rounded to 6 decimals
     */
    private static double round(double value) {

	return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * @param series
     * @return
     * @throws GSException
     */
    private BrandenburgPegelParameter getParameter(BrandenburgPegelEntity series) throws GSException {

	return series.getParameter().orElseThrow(() -> createException("Invalid Pegelportal series " + series.getSeriesId(), null));
    }

    /**
     * @param text MEZ time, e.g. "27.09.2026 00:00"
     * @return
     */
    static Optional<Date> parseDate(String text) {

	if (text == null || text.isBlank()) {
	    return Optional.empty();
	}

	try {
	    return Optional.of(Date.from(LocalDateTime.parse(text.trim(), DATE_FORMAT).atOffset(MEZ).toInstant()));
	} catch (DateTimeParseException e) {
	    return Optional.empty();
	}
    }

    /**
     * @param text
     * @return the value, or null if missing or not valid
     */
    static BigDecimal parseDecimal(String text) {

	if (text == null || text.isBlank() || text.trim().equals("null") || text.trim().equals("-")) {
	    return null;
	}

	try {
	    BigDecimal value = new BigDecimal(text.trim().replace(',', '.'));
	    return value.compareTo(MISSING_VALUE) == 0 ? null : value;
	} catch (NumberFormatException e) {
	    return null;
	}
    }

    /**
     * @param text
     * @return the text with the numeric character references and the most common named entities decoded
     */
    static String decodeEntities(String text) {

	Matcher matcher = ENTITY_PATTERN.matcher(text);
	StringBuilder builder = new StringBuilder();
	while (matcher.find()) {
	    matcher.appendReplacement(builder, Matcher.quoteReplacement(new String(Character.toChars(Integer.parseInt(matcher.group(1))))));
	}
	matcher.appendTail(builder);

	return builder.toString().//
		replace("&auml;", "ä").//
		replace("&ouml;", "ö").//
		replace("&uuml;", "ü").//
		replace("&Auml;", "Ä").//
		replace("&Ouml;", "Ö").//
		replace("&Uuml;", "Ü").//
		replace("&szlig;", "ß").//
		replace("&quot;", "\"").//
		replace("&nbsp;", " ").//
		replace("&amp;", "&");
    }

    /**
     * @param path
     * @return
     * @throws GSException
     */
    private String get(String path) throws GSException {

	return getOptional(path).orElseThrow(() -> createException("Pegelportal " + path + " not available", null));
    }

    /**
     * @param path
     * @return the response, or empty if the resource is not available
     * @throws GSException
     */
    private Optional<String> getOptional(String path) throws GSException {

	try {

	    HttpResponse<InputStream> response = downloader.downloadResponse(endpoint + path);

	    String text;
	    try (InputStream stream = response.body()) {
		text = stream == null ? "" : IOUtils.toString(stream, CHARSET);
	    }

	    if (response.statusCode() == 404) {
		return Optional.empty();
	    }

	    if (response.statusCode() != 200) {
		throw createException("Pegelportal " + path + " returned HTTP " + response.statusCode(), null);
	    }

	    return text.isBlank() ? Optional.empty() : Optional.of(text);

	} catch (GSException e) {

	    throw e;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	    throw createException("Pegelportal " + path + " request failed", e);
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
     * @return
     */
    static String normalizeEndpoint(String endpoint) {

	if (endpoint == null || endpoint.isEmpty()) {
	    return DEFAULT_ENDPOINT;
	}

	String normalized = endpoint.trim();
	if (normalized.endsWith(START_PAGE)) {
	    normalized = normalized.substring(0, normalized.length() - START_PAGE.length());
	}
	if (!normalized.endsWith("/")) {
	    normalized += "/";
	}

	return normalized;
    }
}
