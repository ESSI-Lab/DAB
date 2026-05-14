package eu.essi_lab.accessor.hiscentral.emilia.simc;

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

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * Thin Java wrapper around the Arpae-SIMC Python Eve REST API for open meteorological station
 * data ({@code https://apps.arpae.it/REST/...}). Pagination follows the service contract: increase
 * {@code page} until {@code _items} is empty.
 *
 * @see <a href="https://docs.python-eve.org/">Python Eve documentation</a>
 */
public class ArpaeSimcMeteoOpenDataClient {

    /** Default REST root as documented for the service. */
    public static final String DEFAULT_BASE_URL = "https://apps.arpae.it/REST";

    public static final String RESOURCE_STATIONS = "meteo_opendata_stazioni";
    public static final String RESOURCE_DATASETS = "meteo_opendata_stazioni_dataset";

    private static final DateTimeFormatter RFC1123_GMT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
	    .withZone(ZoneId.of("GMT"));

    private final String baseUrl;
    private final Downloader downloader;

    public ArpaeSimcMeteoOpenDataClient() {
	this(DEFAULT_BASE_URL, new Downloader());
    }

    public ArpaeSimcMeteoOpenDataClient(String baseUrl) {
	this(baseUrl, new Downloader());
    }

    public ArpaeSimcMeteoOpenDataClient(String baseUrl, Downloader downloader) {
	this.baseUrl = trimTrailingSlash(Objects.requireNonNull(baseUrl, "baseUrl"));
	this.downloader = Objects.requireNonNull(downloader, "downloader");
    }

    public String getBaseUrl() {
	return baseUrl;
    }

    /**
     * Formats an instant as RFC1123 with a literal {@code GMT} suffix, as required for Eve
     * {@code where} filters and returned timestamps in this API.
     */
    public static String formatReftime(Instant instant) {
	return RFC1123_GMT.format(Objects.requireNonNull(instant, "instant"));
    }

    /**
     * Parses a reftime string from the API (RFC1123 / GMT) into an {@link Instant}.
     */
    public static Optional<Instant> parseReftime(String reftime) {
	if (reftime == null || reftime.isEmpty()) {
	    return Optional.empty();
	}
	try {
	    return Optional.of(Instant.from(RFC1123_GMT.parse(reftime)));
	} catch (DateTimeParseException e) {
	    GSLoggerFactory.getLogger(ArpaeSimcMeteoOpenDataClient.class).warn("Unparseable reftime: {}", reftime, e);
	    return Optional.empty();
	}
    }

    /** Eve {@code where} filter: {@code station_id} equals the given station document {@code _id}. */
    public static JSONObject whereStationId(String stationId) {
	return new JSONObject().put("station_id", Objects.requireNonNull(stationId, "stationId"));
    }

    /**
     * Eve {@code where} filter: {@code reftime} strictly between two instants (uses {@code $gt} /
     * {@code $lt}), matching the style used in the API manual examples.
     */
    public static JSONObject whereReftimeBetweenExclusive(Instant greaterThan, Instant lessThan) {
	JSONObject range = new JSONObject();
	range.put("$gt", formatReftime(greaterThan));
	range.put("$lt", formatReftime(lessThan));
	return new JSONObject().put("reftime", range);
    }

    /** Eve {@code where} filter: {@code height} greater than {@code meters}. */
    public static JSONObject whereHeightGreaterThan(double meters) {
	return new JSONObject().put("height", new JSONObject().put("$gt", meters));
    }

    /**
     * Merges several {@code where} fragments into one object (same keys as {@link JSONObject#put}
     * would allow; later fragments overwrite duplicate keys).
     */
    public static JSONObject mergeWhere(JSONObject... parts) {
	JSONObject out = new JSONObject();
	for (JSONObject p : parts) {
	    if (p == null) {
		continue;
	    }
	    for (String k : p.keySet()) {
		out.put(k, p.get(k));
	    }
	}
	return out;
    }

    /**
     * Resolves a {@code href} from a document (may be absolute or relative to the REST root) to a
     * full URL suitable for {@link #getPage(String, Map)}.
     */
    public String resolveHref(String href) {
	if (href == null || href.isEmpty()) {
	    return baseUrl;
	}
	if (href.startsWith("http://") || href.startsWith("https://")) {
	    return href;
	}
	return URI.create(baseUrl + "/").resolve(href.startsWith("/") ? href : "/" + href).toString();
    }

    /**
     * Performs a single GET for one Eve page. Query keys match Eve (e.g. {@code where}, {@code sort},
     * {@code page}, {@code max_results}, {@code projection}).
     */
    public JSONObject getPage(String resourcePath, Map<String, String> queryParams) throws IOException {
	String url = buildUrl(resourcePath, queryParams);
	Optional<String> body = downloader.downloadOptionalString(url, HttpHeaderUtils.build("Accept", "application/json"));
	if (body.isEmpty()) {
	    throw new IOException("Empty or failed HTTP response for: " + url);
	}
	try {
	    return new JSONObject(body.get());
	} catch (Exception e) {
	    throw new IOException("Invalid JSON for: " + url, e);
	}
    }

    /**
     * One Eve page for {@link #RESOURCE_STATIONS}.
     */
    public JSONObject getStationsPage(int page, int maxResults, JSONObject where, String sort) throws IOException {
	return getPage(RESOURCE_STATIONS, eveQuery(where, sort, page, maxResults));
    }

    /**
     * One Eve page for {@link #RESOURCE_DATASETS}.
     */
    public JSONObject getDatasetsPage(int page, int maxResults, JSONObject where, String sort) throws IOException {
	return getPage(RESOURCE_DATASETS, eveQuery(where, sort, page, maxResults));
    }

    /**
     * One Eve page for an observation dataset (resource name only, without leading slash), e.g.
     * {@code meteo_opendata_stazioni_precipitazione_oraria}.
     */
    public JSONObject getObservationsPage(String datasetResourceName, int page, int maxResults, JSONObject where, String sort)
	    throws IOException {
	Objects.requireNonNull(datasetResourceName, "datasetResourceName");
	return getPage(datasetResourceName, eveQuery(where, sort, page, maxResults));
    }

    /**
     * Iterates all pages until {@code _items} is empty (per API manual).
     */
    public List<JSONObject> listAllItems(String resourcePath, JSONObject where, String sort, int maxResultsPerPage) throws IOException {
	List<JSONObject> out = new ArrayList<>();
	for (int page = 1;; page++) {
	    JSONObject response = getPage(resourcePath, eveQuery(where, sort, page, maxResultsPerPage));
	    JSONArray items = response.optJSONArray("_items");
	    if (items == null || items.length() == 0) {
		break;
	    }
	    for (int i = 0; i < items.length(); i++) {
		out.add(items.getJSONObject(i));
	    }
	}
	return out;
    }

    public List<SimcStation> listAllStations(JSONObject where, String sort, int maxResultsPerPage) throws IOException {
	List<SimcStation> out = new ArrayList<>();
	for (JSONObject o : listAllItems(RESOURCE_STATIONS, where, sort, maxResultsPerPage)) {
	    out.add(SimcStation.fromJson(o));
	}
	return out;
    }

    public List<SimcDatasetDescriptor> listAllDatasets(JSONObject where, String sort, int maxResultsPerPage) throws IOException {
	List<SimcDatasetDescriptor> out = new ArrayList<>();
	for (JSONObject o : listAllItems(RESOURCE_DATASETS, where, sort, maxResultsPerPage)) {
	    out.add(SimcDatasetDescriptor.fromJson(o));
	}
	return out;
    }

    public List<SimcObservation> listAllObservations(String datasetResourceName, JSONObject where, String sort, int maxResultsPerPage)
	    throws IOException {
	List<SimcObservation> out = new ArrayList<>();
	for (JSONObject o : listAllItems(datasetResourceName, where, sort, maxResultsPerPage)) {
	    out.add(SimcObservation.fromJson(o));
	}
	return out;
    }

    private static Map<String, String> eveQuery(JSONObject where, String sort, int page, int maxResults) {
	Map<String, String> m = new LinkedHashMap<>();
	m.put("page", String.valueOf(page));
	m.put("max_results", String.valueOf(maxResults));
	if (where != null && !where.isEmpty()) {
	    m.put("where", where.toString());
	}
	if (sort != null && !sort.isEmpty()) {
	    m.put("sort", sort);
	}
	return m;
    }

    private String buildUrl(String resourcePath, Map<String, String> queryParams) {
	String path = trimLeadingSlash(Objects.requireNonNull(resourcePath, "resourcePath"));
	StringBuilder sb = new StringBuilder(baseUrl).append('/').append(path);
	if (queryParams != null && !queryParams.isEmpty()) {
	    sb.append('?');
	    boolean first = true;
	    for (Map.Entry<String, String> e : queryParams.entrySet()) {
		if (!first) {
		    sb.append('&');
		}
		first = false;
		sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8));
		sb.append('=');
		if (e.getValue() != null) {
		    sb.append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
		}
	    }
	}
	return sb.toString();
    }

    private static String trimTrailingSlash(String u) {
	if (u.endsWith("/")) {
	    return u.substring(0, u.length() - 1);
	}
	return u;
    }

    private static String trimLeadingSlash(String p) {
	if (p.startsWith("/")) {
	    return p.substring(1);
	}
	return p;
    }

    /** Station document ({@code meteo_opendata_stazioni}). */
    public record SimcStation(//
	    String id, //
	    String name, //
	    Double heightMeters, //
	    JSONObject geometry, //
	    List<SimcStationSummary> summaries, //
	    JSONArray ratingCurvesOrNull, //
	    JSONObject raw //
    ) {
	public static SimcStation fromJson(JSONObject o) {
	    JSONArray sums = o.optJSONArray("summaries");
	    List<SimcStationSummary> list = new ArrayList<>();
	    if (sums != null) {
		for (int i = 0; i < sums.length(); i++) {
		    list.add(SimcStationSummary.fromJson(sums.getJSONObject(i)));
		}
	    }
	    JSONArray rc = o.optJSONArray("rating_curves");
	    Double h = null;
	    if (o.has("height") && !o.isNull("height")) {
		h = o.getDouble("height");
	    }
	    return new SimcStation(//
		    o.optString("_id", null), //
		    o.optString("name", null), //
		    h, //
		    o.optJSONObject("geometry"), //
		    list, //
		    rc, //
		    o //
	    );
	}
    }

    /** Element of {@code summaries} on a station. */
    public record SimcStationSummary(String reftimeStart, String reftimeEnd, String href) {
	public static SimcStationSummary fromJson(JSONObject o) {
	    return new SimcStationSummary(//
		    o.optString("reftime_start", null), //
		    o.optString("reftime_end", null), //
		    o.optString("href", null) //
	    );
	}
    }

    /** Dataset / parameter descriptor ({@code meteo_opendata_stazioni_dataset}). */
    public record SimcDatasetDescriptor(//
	    String id, //
	    String href, //
	    String reftimeStart, //
	    String reftimeEnd, //
	    String unit, //
	    String bcode, //
	    JSONArray level, //
	    JSONArray timerange, //
	    JSONObject raw //
    ) {
	public static SimcDatasetDescriptor fromJson(JSONObject o) {
	    return new SimcDatasetDescriptor(//
		    o.optString("_id", null), //
		    o.optString("href", null), //
		    o.optString("reftime_start", null), //
		    o.optString("reftime_end", null), //
		    o.optString("unit", null), //
		    o.optString("bcode", null), //
		    o.optJSONArray("level"), //
		    o.optJSONArray("timerange"), //
		    o //
	    );
	}
    }

    /** Single observation in a dataset resource. */
    public record SimcObservation(String id, String stationId, String reftime, Double valueOrNull, JSONObject raw) {
	public static SimcObservation fromJson(JSONObject o) {
	    Double v = null;
	    if (o.has("value") && !o.isNull("value")) {
		v = o.getDouble("value");
	    }
	    return new SimcObservation(//
		    o.optString("_id", null), //
		    o.optString("station_id", null), //
		    o.optString("reftime", null), //
		    v, //
		    o //
	    );
	}
    }
}
