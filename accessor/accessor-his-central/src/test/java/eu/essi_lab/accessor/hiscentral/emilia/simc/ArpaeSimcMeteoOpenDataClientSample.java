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
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Fetches stations from the Arpae-SIMC Eve API and prints details for each (see {@link #main}).
 * Station {@code summaries[]} entries are cross-linked with the global dataset catalogue
 * {@code meteo_opendata_stazioni_dataset} so each variable line includes {@code unit}, {@code bcode},
 * and {@code timerange} (dballe {@code [pindicator, p1, p2]}). For each station, the first summary
 * dataset is queried for observations in a two-calendar-month window ending at that summary's
 * {@code reftime_end} (capped to now, floored by {@code reftime_start}).
 * Run from the IDE (test sources) or:
 *
 * <pre>
 * mvn -q org.codehaus.mojo:exec-maven-plugin:3.6.3:java -Dexec.classpathScope=test \
 *   -Dexec.mainClass=eu.essi_lab.accessor.hiscentral.emilia.simc.ArpaeSimcMeteoOpenDataClientSample
 * </pre>
 *
 * System properties: {@code simc.sample.pageSize} — Eve {@code max_results} per request (default
 * 200). {@code simc.sample.maxStations} — if set, stop fetching after this many stations; if
 * unset, fetch the full catalogue (all pages until empty). {@code simc.sample.maxObservations} —
 * cap when paging observations for the “last two months” sample (default 10000).
 * {@code simc.sample.maxObsPrint} — max observation lines printed per station (default 200).
 */
public final class ArpaeSimcMeteoOpenDataClientSample {

    /** Eve max_results per page while listing stations (manual recommends iterating page). */
    private static final int DEFAULT_STATION_PAGE_SIZE = 200;

    private static final int DEFAULT_MAX_OBSERVATIONS = 10_000;

    private static final int DEFAULT_MAX_OBS_PRINT = 200;

    private ArpaeSimcMeteoOpenDataClientSample() {
    }

    public static void main(String[] args) throws IOException {
	int pageSize = intProperty("simc.sample.pageSize", DEFAULT_STATION_PAGE_SIZE);
	Integer maxStations = intPropertyOptional("simc.sample.maxStations");

	ArpaeSimcMeteoOpenDataClient client = new ArpaeSimcMeteoOpenDataClient();

	System.out.println("Base URL: " + client.getBaseUrl());
	if (maxStations != null) {
	    System.out.println("Loading up to " + maxStations + " station(s), max_results per page: " + pageSize + "...");
	} else {
	    System.out.println("Loading all stations (max_results per page: " + pageSize + ")...");
	}

	List<ArpaeSimcMeteoOpenDataClient.SimcStation> stations = loadStations(client, pageSize, maxStations);
	System.out.println("Stations loaded: " + stations.size());

	System.out.println("Loading dataset catalogue (" + ArpaeSimcMeteoOpenDataClient.RESOURCE_DATASETS + ") for unit / bcode / timerange...");
	Map<String, ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor> datasetIndex = loadDatasetIndex(client, pageSize);
	System.out.println("Dataset index lookup keys: " + datasetIndex.size());
	System.out.println();

	int maxObservations = intProperty("simc.sample.maxObservations", DEFAULT_MAX_OBSERVATIONS);
	int maxObsPrint = intProperty("simc.sample.maxObsPrint", DEFAULT_MAX_OBS_PRINT);

	for (ArpaeSimcMeteoOpenDataClient.SimcStation st : stations) {
	    printStationBlock(client, st, datasetIndex, pageSize, maxObservations, maxObsPrint);
	}

	System.out.println("Done.");
    }

    /**
     * Paginates {@code meteo_opendata_stazioni} until {@code _items} is empty, or until
     * {@code maxStations} documents have been collected when that limit is set.
     */
    private static List<ArpaeSimcMeteoOpenDataClient.SimcStation> loadStations(ArpaeSimcMeteoOpenDataClient client, int pageSize,
	    Integer maxStations) throws IOException {
	List<ArpaeSimcMeteoOpenDataClient.SimcStation> out = new ArrayList<>();
	for (int page = 1;; page++) {
	    JSONObject response = client.getStationsPage(page, pageSize, null, "_id");
	    JSONArray items = response.optJSONArray("_items");
	    if (items == null || items.length() == 0) {
		break;
	    }
	    for (int i = 0; i < items.length(); i++) {
		out.add(ArpaeSimcMeteoOpenDataClient.SimcStation.fromJson(items.getJSONObject(i)));
		if (maxStations != null && out.size() >= maxStations) {
		    return out;
		}
	    }
	}
	return out;
    }

    /**
     * Indexes {@link ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor} by catalogue {@code _id}
     * and by the last path segment of the resolved {@code href} (matches station summary links).
     */
    private static Map<String, ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor> loadDatasetIndex(ArpaeSimcMeteoOpenDataClient client,
	    int pageSize) throws IOException {
	List<ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor> rows = client.listAllDatasets(null, "_id", pageSize);
	Map<String, ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor> index = new HashMap<>();
	for (ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor dd : rows) {
	    if (dd.id() != null && !dd.id().isEmpty()) {
		index.putIfAbsent(dd.id(), dd);
	    }
	    if (dd.href() != null && !dd.href().isEmpty()) {
		String resolved = client.resolveHref(dd.href());
		String seg = lastPathSegment(resolved);
		if (seg != null && !seg.isEmpty()) {
		    index.putIfAbsent(seg, dd);
		}
	    }
	}
	System.out.println("Dataset catalogue rows loaded: " + rows.size());
	return index;
    }

    private static void printStationBlock(ArpaeSimcMeteoOpenDataClient client, ArpaeSimcMeteoOpenDataClient.SimcStation st,
	    Map<String, ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor> datasetIndex, int obsPageSize, int maxObservationsToFetch,
	    int maxObservationsToPrint) throws IOException {
	JSONObject raw = st.raw();
	double[] lonLat = lonLatFromGeometry(st.geometry());

	System.out.println("---");
	System.out.println("id:   " + nullToDash(st.id()));
	System.out.println("name: " + nullToDash(st.name()));
	System.out.println("height (m): " + (st.heightMeters() != null ? st.heightMeters() : "—"));
	if (lonLat != null) {
	    System.out.println("lon:  " + lonLat[0]);
	    System.out.println("lat:  " + lonLat[1]);
	} else {
	    System.out.println("lon:  —");
	    System.out.println("lat:  —");
	}

	JSONArray rc = st.ratingCurvesOrNull();
	boolean hasRatingCurves = rc != null && rc.length() > 0;
	System.out.println("rating_curves present: " + hasRatingCurves + (hasRatingCurves ? " (count=" + rc.length() + ")" : ""));

	String stationDatum = formatRiverGaugeDatum(raw.opt("river_gauge_datum"));
	System.out.println("river_gauge_datum (station document): " + stationDatum);

	if (hasRatingCurves) {
	    int eqWithDatum = countEquationsWithRiverGaugeDatum(rc);
	    System.out.println("rating_curves: equations with non-null river_gauge_datum (all curves): " + eqWithDatum);
	    System.out.println("rating_curves / river_gauge_datum (per equation, first curve only):");
	    printRatingCurveRiverGaugeSummary(rc.getJSONObject(0), "  ");
	}

	System.out.println("variables / datasets (from summaries[], enriched from meteo_opendata_stazioni_dataset):");
	List<ArpaeSimcMeteoOpenDataClient.SimcStationSummary> sums = st.summaries();
	if (sums == null || sums.isEmpty()) {
	    System.out.println("  (none)");
	} else {
	    int i = 0;
	    for (ArpaeSimcMeteoOpenDataClient.SimcStationSummary s : sums) {
		String datasetLabel = variableLabelFromHref(client, s.href());
		ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor dd = lookupDataset(client, datasetIndex, s.href());
		System.out.println("  [" + i + "] " + datasetLabel);
		System.out.println("      reftime_start: " + nullToDash(s.reftimeStart()));
		System.out.println("      reftime_end:   " + nullToDash(s.reftimeEnd()));
		System.out.println("      href:          " + nullToDash(s.href()));
		if (dd != null) {
		    System.out.println("      unit:          " + nullToDash(dd.unit()));
		    System.out.println("      bcode:         " + nullToDash(dd.bcode()));
		    System.out.println("      timerange:     " + describeTimerange(dd.timerange()));
		} else {
		    System.out.println("      unit/bcode/timerange: — (no matching row in " + ArpaeSimcMeteoOpenDataClient.RESOURCE_DATASETS
			    + " for href / id \"" + datasetLabel + "\")");
		}
		i++;
	    }
	}

	printFirstDatasetLastTwoMonthsObservations(client, st, sums, obsPageSize, maxObservationsToFetch, maxObservationsToPrint);
	System.out.println();
    }

    /**
     * Uses the first {@code summaries[]} entry as the dataset, builds a reftime window of two
     * calendar months ending at the summary's {@code reftime_end} (latest data available for that
     * series at the station, capped to {@link Instant#now()}), not earlier than
     * {@code reftime_start}, then pages observations and prints them.
     */
    private static void printFirstDatasetLastTwoMonthsObservations(ArpaeSimcMeteoOpenDataClient client,
	    ArpaeSimcMeteoOpenDataClient.SimcStation st, List<ArpaeSimcMeteoOpenDataClient.SimcStationSummary> summaries, int obsPageSize,
	    int maxObservationsToFetch, int maxObservationsToPrint) throws IOException {

	System.out.println("observations — first dataset in summaries[], last ~2 calendar months of data available at station:");
	if (st.id() == null || st.id().isEmpty()) {
	    System.out.println("  (skipped: station has no _id)");
	    return;
	}
	if (summaries == null || summaries.isEmpty()) {
	    System.out.println("  (skipped: no summaries)");
	    return;
	}

	ArpaeSimcMeteoOpenDataClient.SimcStationSummary first = summaries.get(0);
	String href = first.href();
	if (href == null || href.isEmpty()) {
	    System.out.println("  (skipped: first summary has no href)");
	    return;
	}
	String datasetResource = variableLabelFromHref(client, href);
	if (datasetResource == null || datasetResource.startsWith("(")) {
	    System.out.println("  (skipped: could not derive dataset resource from href)");
	    return;
	}

	Instant now = Instant.now();
	Instant end = ArpaeSimcMeteoOpenDataClient.parseReftime(first.reftimeEnd()).map(e -> e.isAfter(now) ? now : e).orElse(now);

	Instant start = ZonedDateTime.ofInstant(end, ZoneOffset.UTC).minusMonths(2).toInstant();
	Optional<Instant> seriesStart = ArpaeSimcMeteoOpenDataClient.parseReftime(first.reftimeStart());
	if (seriesStart.isPresent() && start.isBefore(seriesStart.get())) {
	    start = seriesStart.get();
	}
	if (!start.isBefore(end)) {
	    System.out.println("  (skipped: empty or invalid time window after clamp; start=" + start + " end=" + end + ")");
	    return;
	}

	JSONObject where = whereStationAndReftimeInclusive(st.id(), start, end);

	List<ArpaeSimcMeteoOpenDataClient.SimcObservation> rows = new ArrayList<>();
	for (int page = 1;; page++) {
	    JSONObject resp = client.getObservationsPage(datasetResource, page, obsPageSize, where, "reftime");
	    JSONArray items = resp.optJSONArray("_items");
	    if (items == null || items.length() == 0) {
		break;
	    }
	    for (int i = 0; i < items.length(); i++) {
		rows.add(ArpaeSimcMeteoOpenDataClient.SimcObservation.fromJson(items.getJSONObject(i)));
		if (rows.size() >= maxObservationsToFetch) {
		    break;
		}
	    }
	    if (rows.size() >= maxObservationsToFetch) {
		break;
	    }
	    if (items.length() < obsPageSize) {
		break;
	    }
	}

	System.out.println("  dataset:       " + datasetResource);
	System.out.println("  station_id:    " + st.id());
	System.out.println("  reftime filter (RFC1123): " + ArpaeSimcMeteoOpenDataClient.formatReftime(start) + " .. "
		+ ArpaeSimcMeteoOpenDataClient.formatReftime(end));
	System.out.println("  rows fetched:  " + rows.size() + (rows.size() >= maxObservationsToFetch ? " (cap simc.sample.maxObservations)" : ""));
	int nPrint = Math.min(rows.size(), maxObservationsToPrint);
	for (int i = 0; i < nPrint; i++) {
	    ArpaeSimcMeteoOpenDataClient.SimcObservation o = rows.get(i);
	    System.out.println("    " + o.reftime() + "  value=" + o.valueOrNull() + "  _id=" + o.id());
	}
	if (rows.size() > nPrint) {
	    System.out.println("    ... (" + (rows.size() - nPrint) + " more not printed; simc.sample.maxObsPrint)");
	}
    }

    /** Eve {@code where} for {@code station_id} and {@code reftime} in {@code [$gte, $lte]} (RFC1123 strings). */
    private static JSONObject whereStationAndReftimeInclusive(String stationId, Instant fromInclusive, Instant toInclusive) {
	JSONObject range = new JSONObject();
	range.put("$gte", ArpaeSimcMeteoOpenDataClient.formatReftime(fromInclusive));
	range.put("$lte", ArpaeSimcMeteoOpenDataClient.formatReftime(toInclusive));
	return new JSONObject().put("station_id", stationId).put("reftime", range);
    }

    private static ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor lookupDataset(ArpaeSimcMeteoOpenDataClient client,
	    Map<String, ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor> index, String href) {
	if (href == null || href.isEmpty()) {
	    return null;
	}
	String key = variableLabelFromHref(client, href);
	ArpaeSimcMeteoOpenDataClient.SimcDatasetDescriptor dd = index.get(key);
	if (dd != null) {
	    return dd;
	}
	return index.get(href.trim());
    }

    /**
     * Human-readable description of the dballe timerange triple
     * {@code [pindicator, p1, p2]} (see API manual).
     */
    private static String describeTimerange(JSONArray tr) {
	if (tr == null || tr.length() == 0) {
	    return "—";
	}
	return tr.toString() + "  [pindicator, p1, p2]";
    }

    private static void printRatingCurveRiverGaugeSummary(JSONObject curve, String indent) {
	if (curve == null) {
	    return;
	}
	JSONArray eqs = curve.optJSONArray("equations");
	if (eqs == null || eqs.length() == 0) {
	    System.out.println(indent + "(no equations in first curve)");
	    return;
	}
	for (int e = 0; e < eqs.length(); e++) {
	    JSONObject eq = eqs.getJSONObject(e);
	    System.out.println(indent + "equation[" + e + "] river_gauge_datum: " + formatRiverGaugeDatum(eq.opt("river_gauge_datum")));
	}
    }

    private static int countEquationsWithRiverGaugeDatum(JSONArray ratingCurves) {
	int n = 0;
	for (int c = 0; c < ratingCurves.length(); c++) {
	    JSONObject curve = ratingCurves.optJSONObject(c);
	    if (curve == null) {
		continue;
	    }
	    JSONArray eqs = curve.optJSONArray("equations");
	    if (eqs == null) {
		continue;
	    }
	    for (int e = 0; e < eqs.length(); e++) {
		JSONObject eq = eqs.optJSONObject(e);
		if (eq == null) {
		    continue;
		}
		if (!eq.isNull("river_gauge_datum")) {
		    n++;
		}
	    }
	}
	return n;
    }

    private static String variableLabelFromHref(ArpaeSimcMeteoOpenDataClient client, String href) {
	if (href == null || href.isEmpty()) {
	    return "(no href)";
	}
	String resolved = client.resolveHref(href);
	String seg = lastPathSegment(resolved);
	return seg != null ? seg : resolved;
    }

    private static String formatRiverGaugeDatum(Object value) {
	if (value == null || JSONObject.NULL.equals(value)) {
	    return "— (null / absent)";
	}
	return String.valueOf(value);
    }

    /**
     * GeoJSON {@code Point}: {@code coordinates} are [lon, lat]. Returns {@code null} if not a
     * usable Point.
     */
    private static double[] lonLatFromGeometry(JSONObject geometry) {
	if (geometry == null) {
	    return null;
	}
	if (!"Point".equalsIgnoreCase(geometry.optString("type", ""))) {
	    return null;
	}
	JSONArray coords = geometry.optJSONArray("coordinates");
	if (coords == null || coords.length() < 2) {
	    return null;
	}
	return new double[] { coords.getDouble(0), coords.getDouble(1) };
    }

    private static String nullToDash(String s) {
	return (s == null || s.isEmpty()) ? "—" : s;
    }

    private static int intProperty(String key, int defaultValue) {
	String v = System.getProperty(key);
	if (v == null || v.isBlank()) {
	    return defaultValue;
	}
	try {
	    int i = Integer.parseInt(v.trim());
	    return i > 0 ? i : defaultValue;
	} catch (NumberFormatException e) {
	    return defaultValue;
	}
    }

    private static Integer intPropertyOptional(String key) {
	String v = System.getProperty(key);
	if (v == null || v.isBlank()) {
	    return null;
	}
	try {
	    int i = Integer.parseInt(v.trim());
	    return i > 0 ? i : null;
	} catch (NumberFormatException e) {
	    return null;
	}
    }

    /** Last path segment of a URL or absolute path (dataset resource name). */
    static String lastPathSegment(String urlOrPath) {
	if (urlOrPath == null || urlOrPath.isEmpty()) {
	    return null;
	}
	try {
	    URI u = URI.create(urlOrPath.contains("://") ? urlOrPath : "https://placeholder.local" + (urlOrPath.startsWith("/") ? urlOrPath : "/" + urlOrPath));
	    String path = u.getPath();
	    while (path.endsWith("/") && path.length() > 1) {
		path = path.substring(0, path.length() - 1);
	    }
	    int slash = path.lastIndexOf('/');
	    return slash >= 0 ? path.substring(slash + 1) : path;
	} catch (IllegalArgumentException e) {
	    return null;
	}
    }
}
