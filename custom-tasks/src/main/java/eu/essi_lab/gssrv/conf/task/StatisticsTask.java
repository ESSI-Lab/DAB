package eu.essi_lab.gssrv.conf.task;

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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.quartz.JobExecutionContext;

import eu.essi_lab.access.availability.AvailabilityMonitor;
import eu.essi_lab.access.availability.DownloadInformation;
import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.OptionsKey;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.semantic.SourceStatistics;
import eu.essi_lab.profiler.semantic.Stats;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * Gathers statistics info
 * 
 * Task options (line-based {@code KEY=value}):
 * <ul>
 * <li>{@code VIEW_ID} — optional view identifier; when omitted, aggregate statistics use all sources (no view bond);
 * metadata completeness metrics still require a view and are skipped if {@code VIEW_ID} is not set</li>
 * <li>{@code METRICS} — optional comma-separated list of {@link StatisticsMetric} names; when omitted, all metrics are
 * computed</li>
 * <li>{@code METADATA_COMPLETENESS_ELEMENTS} — optional comma-separated {@link MetadataElement} identifiers for
 * {@link StatisticsMetric#METADATA_COMPLETENESS}: enum names (e.g. {@code IDENTIFIER,TITLE,BOUNDING_BOX}) or harmonized
 * index names (e.g. {@code fileId,title,bbox}); when omitted, defaults to {@code IDENTIFIER,TITLE}</li>
 * <li>{@code METADATA_SET_COMPLETENESS} — comma-separated set names for {@link StatisticsMetric#METADATA_SET_COMPLETENESS}
 * (e.g. {@code core,optional})</li>
 * <li>{@code METADATA_SET_COMPLETENESS_&lt;name&gt;} — for each set name, comma-separated {@link MetadataElement} values;
 * metric is the share of records having <b>all</b> listed elements (logical AND). Ex.:
 * {@code METADATA_SET_COMPLETENESS_core=IDENTIFIER} and
 * {@code METADATA_SET_COMPLETENESS_optional=IDENTIFIER,TITLE,BOUNDING_BOX}</li>
 * </ul>
 * Legacy: a single line without {@code =} is treated as the view id (same as only {@code VIEW_ID} before).
 * 
 * @author boldrini
 */
public class StatisticsTask extends AbstractCustomTask {

    public enum StatisticsTaskOptions implements OptionsKey {
	VIEW_ID,
	METRICS,
	METADATA_COMPLETENESS_ELEMENTS,
	/** Comma-separated set names; members defined by {@code METADATA_SET_COMPLETENESS_<name>=...} lines. */
	METADATA_SET_COMPLETENESS
    }

    /**
     * Prometheus statistics gauges this task can expose (subset per {@code METRICS} option).
     */
    public enum StatisticsMetric {

	SOURCE_INFO("source_info", "Metadata about each source."),
	DOWNLOAD_AVAILABILITY("download_availability", "Download availability "),
	HARVESTED_RECORDS("harvested_records", "Total number of harvested records "),
	LAST_HARVESTING_UNIX_TIMESTAMP_MS("last_harvesting_unix_timestamp_ms",
		"Last harvesting end timestamp in epoch seconds"),
	SOURCE_UP("source_up", "Source connectivity status (1=up, 0=down)"),
	CONNECTIVITY_TEST_DURATION_MS("connectivity_test_duration_ms",
		"Duration of the last source connectivity test in milliseconds"),
	LAST_SOURCE_UP_UNIX_TIMESTAMP_MS("last_source_up_unix_timestamp_ms",
		"Unix epoch milliseconds when the source was last observed up in a connectivity test"),
	PLATFORMS_TOTAL("platforms_total", "Total number of platforms "),
	VARIABLES_TOTAL("variables_total", "Total number of variables "),
	METADATA_COMPLETENESS("metadata_completeness",
		"Per-element share where the field exists (tag element); elements from METADATA_COMPLETENESS_ELEMENTS"),

	METADATA_SET_COMPLETENESS("metadata_set_completeness",
		"Per named set: share of records where all listed metadata elements exist (tag element_set); "
			+ "configured via METADATA_SET_COMPLETENESS and METADATA_SET_COMPLETENESS_<name> lines");

	private final String prometheusName;
	private final String description;

	StatisticsMetric(String prometheusName, String description) {

	    this.prometheusName = prometheusName;
	    this.description = description;
	}

	/** Prometheus / Micrometer metric name (e.g. {@code metadata_completeness}). */
	public String prometheusName() {

	    return prometheusName;
	}

	public String description() {

	    return description;
	}
    }

    private static final long CANCEL_CHECK_INTERVAL_MS = 10_000L;

    private static final String METADATA_SET_COMPLETENESS_LINE_PREFIX = "METADATA_SET_COMPLETENESS_";

    /** Name as in task options (tag {@code element_set}) plus resolved elements. */
    private static final class MetadataCompletenessSet {

	final String setNameTag;
	final List<MetadataElement> elements;

	MetadataCompletenessSet(String setNameTag, List<MetadataElement> elements) {

	    this.setNameTag = setNameTag;
	    this.elements = elements;
	}

	@Override
	public String toString() {

	    return setNameTag + " (" + elements.size() + " elements)";
	}
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Statistics task STARTED");
	final long taskStartMs = System.currentTimeMillis();
	long lastCancelCheckMs = 0L;

	Optional<EnumMap<StatisticsTaskOptions, String>> structured = readTaskOptions(context, StatisticsTaskOptions.class);
	Optional<String> rawTaskOptions = readTaskOptions(context);

	String viewId = null;
	Set<StatisticsMetric> metrics = EnumSet.allOf(StatisticsMetric.class);
	List<MetadataElement> metadataCompletenessElements = new ArrayList<>();
	List<MetadataCompletenessSet> metadataCompletenessSets = List.of();

	if (structured.isPresent() && !structured.get().isEmpty()) {
	    viewId = structured.get().get(StatisticsTaskOptions.VIEW_ID);
	    String metricsLine = structured.get().get(StatisticsTaskOptions.METRICS);
	    if (metricsLine != null && !metricsLine.isBlank()) {
		metrics = parseMetricsList(metricsLine);
	    }
	    String completenessElementsLine = structured.get().get(StatisticsTaskOptions.METADATA_COMPLETENESS_ELEMENTS);
	    if (completenessElementsLine != null && !completenessElementsLine.isBlank()) {
		List<MetadataElement> parsed = parseMetadataCompletenessElements(completenessElementsLine);
		if (parsed.isEmpty()) {
		    GSLoggerFactory.getLogger(getClass()).warn(
			    "METADATA_COMPLETENESS_ELEMENTS had no valid entries; using default IDENTIFIER, TITLE");
		} else {
		    metadataCompletenessElements = parsed;
		}
	    }
	    if (metrics.contains(StatisticsMetric.METADATA_SET_COMPLETENESS)) {
		metadataCompletenessSets = parseMetadataSetCompleteness(structured.get(), rawTaskOptions.orElse(""));
	    }
	}

	if (viewId == null || viewId.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).info(
		    "No view specified (VIEW_ID empty); statistics are computed for all sources (no view filter).");
	}

	GSLoggerFactory.getLogger(getClass()).info(
		"Updating metrics for {} (metrics: {}, metadata completeness elements: {}, metadata set completeness: {})",
		(viewId != null && !viewId.isEmpty()) ? ("view " + viewId) : "all sources (no view)",
		metrics, metadataCompletenessElements, metadataCompletenessSets);

	Optional<String> viewIdForStats = (viewId != null && !viewId.isEmpty()) ? Optional.of(viewId) : Optional.empty();
	String statsArtifactKey = (viewId != null && !viewId.isEmpty()) ? viewId : "all-sources";
	SourceStatistics sourceStats = null;
	try {
	    sourceStats = new SourceStatistics(null, viewIdForStats, ResourceProperty.SOURCE_ID);
	} catch (Exception e1) {
	    e1.printStackTrace();
	}
	HashMap<String, Stats> overallStats = sourceStats.getStatistics();
	HashMap<String, Integer> availability = new HashMap<String, Integer>();
	HashMap<String, Integer> datasets = new HashMap<String, Integer>();
	HashMap<String, Integer> platforms = new HashMap<String, Integer>();
	HashMap<String, Integer> variables = new HashMap<String, Integer>();
	HashMap<String, Integer> sourceUp = new HashMap<String, Integer>();
	HashMap<String, Long> connectivityTestDurationMs = new HashMap<String, Long>();
	HashMap<String, Long> lastSourceUpUnixTimestampMs = new HashMap<String, Long>();
	HashMap<String, Long> lastHarvestingTime = new HashMap<String, Long>();

	HashMap<String, Double> metadataCompleteness = new HashMap<String, Double>();
	HashMap<String, Double> metadataCompletenessBySet = new HashMap<String, Double>();
	GSLoggerFactory.getLogger(getClass()).info("source stats completed");

	Optional<View> reportView = Optional.empty();
	DatabaseFinder discoveryFinder = null;
	if (metrics.contains(StatisticsMetric.METADATA_COMPLETENESS)
		|| metrics.contains(StatisticsMetric.METADATA_SET_COMPLETENESS)) {
	    try {
		if (viewId != null && !viewId.isEmpty()) {
		    reportView = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId);
		    if (reportView.isEmpty()) {
			GSLoggerFactory.getLogger(getClass()).warn(
				"View {} not found; metadata completeness metrics will be skipped", viewId);
		    } else {
			discoveryFinder = DatabaseProviderFactory.getFinder(ConfigurationWrapper.getStorageInfo());
		    }
		}
	    } catch (GSException e) {
		GSLoggerFactory.getLogger(getClass()).warn("Cannot resolve view or finder for metadata completeness metrics: {}",
			viewId, e);
	    }
	}

	PrometheusConfig config = PrometheusConfig.DEFAULT;
	PrometheusMeterRegistry registry = new PrometheusMeterRegistry(config);
	Database database = DatabaseFactory.get(ConfigurationWrapper.getStorageInfo());
	int si = 0;
	for (String source : overallStats.keySet()) {

	    GSLoggerFactory.getLogger(getClass()).info("Processing source "+source+" "+ ++si+"/"+overallStats.size());

	    GSSource s = ConfigurationWrapper.getSource(source);
	    if (s==null){
		GSLoggerFactory.getLogger(getClass()).error("Source "+source+" not found,skipping");
		continue;
	    }
	    if (metrics.contains(StatisticsMetric.SOURCE_INFO)) {
		Gauge.builder(StatisticsMetric.SOURCE_INFO.prometheusName(), () -> 1)//
			.description(StatisticsMetric.SOURCE_INFO.description())//
			.tags("source_id", source, "source_label", s.getLabel())//
			.register(registry);
	    }
	    try {
		Stats stats = overallStats.get(source);
		boolean needHarvestingProps = metrics.contains(StatisticsMetric.LAST_HARVESTING_UNIX_TIMESTAMP_MS)
			|| metrics.contains(StatisticsMetric.SOURCE_UP)
			|| metrics.contains(StatisticsMetric.CONNECTIVITY_TEST_DURATION_MS)
			|| metrics.contains(StatisticsMetric.LAST_SOURCE_UP_UNIX_TIMESTAMP_MS);
		HarvestingProperties harvestingProperties = null;
		if (needHarvestingProps) {
		    harvestingProperties = database.getWorker(source).getHarvestingProperties();
		}

		if (metrics.contains(StatisticsMetric.DOWNLOAD_AVAILABILITY)) {
		    DownloadInformation goodInfo = AvailabilityMonitor.getInstance().getLastDownloadDate(source);
		    Date lastGoodDownload = goodInfo == null ? null : goodInfo.getDate();
		    DownloadInformation badInfo = AvailabilityMonitor.getInstance().getLastFailedDownloadDate(source);
		    Date lastBadDownload = badInfo == null ? null : badInfo.getDate();

		    Integer downloadAvailable = 0;
		    if (lastGoodDownload != null) {
			if (lastBadDownload == null || lastBadDownload.before(lastGoodDownload)) {
			    downloadAvailable = 1;
			}
		    }
		    availability.put(source, downloadAvailable);
		    io.micrometer.core.instrument.Gauge.builder(StatisticsMetric.DOWNLOAD_AVAILABILITY.prometheusName(), availability,
			    g -> g.get(source))//
			    .description(StatisticsMetric.DOWNLOAD_AVAILABILITY.description())//
			    .tag("source_id", source).//
			    register(registry);
		}

		if (metrics.contains(StatisticsMetric.HARVESTED_RECORDS)) {
		    datasets.put(source, Integer.parseInt(stats.getTimeSeriesCount()));
		    io.micrometer.core.instrument.Gauge.builder(StatisticsMetric.HARVESTED_RECORDS.prometheusName(), datasets,
			    g -> g.get(source))//
			    .description(StatisticsMetric.HARVESTED_RECORDS.description())//
			    .tag("source_id", source).//
			    register(registry);
		}

		if (metrics.contains(StatisticsMetric.LAST_HARVESTING_UNIX_TIMESTAMP_MS)) {
		    String endHarvestingTimestamp = harvestingProperties.getEndHarvestingTimestamp();
		    Optional<Date> lastHarvesting = ISO8601DateTimeUtils.parseISO8601ToDate(endHarvestingTimestamp);
		    if (lastHarvesting.isPresent()) {
			long ms = lastHarvesting.get().getTime();
			lastHarvestingTime.put(source, ms);
			io.micrometer.core.instrument.Gauge.builder(
				StatisticsMetric.LAST_HARVESTING_UNIX_TIMESTAMP_MS.prometheusName(), lastHarvestingTime,
				g -> g.get(source))//
				.description(StatisticsMetric.LAST_HARVESTING_UNIX_TIMESTAMP_MS.description())//
				.tag("source_id", source).//
				register(registry);
		    }
		}

		if (metrics.contains(StatisticsMetric.SOURCE_UP)) {
		    
		    int up = harvestingProperties.isSourceUp().orElse(false) ? 1 : 0;
		    sourceUp.put(source, up);
		    io.micrometer.core.instrument.Gauge.builder(StatisticsMetric.SOURCE_UP.prometheusName(), sourceUp, g -> g.get(source))//
			    .description(StatisticsMetric.SOURCE_UP.description())//
			    .tag("source_id", source).//
			    register(registry);
		}

		if (metrics.contains(StatisticsMetric.CONNECTIVITY_TEST_DURATION_MS)) {
		    Optional<Long> connectivityMs = harvestingProperties.getConnectivityTestDurationMs();
		    if (connectivityMs.isPresent()) {
			connectivityTestDurationMs.put(source, connectivityMs.get());
			io.micrometer.core.instrument.Gauge.builder(
				StatisticsMetric.CONNECTIVITY_TEST_DURATION_MS.prometheusName(), connectivityTestDurationMs,
				g -> g.get(source))//
				.description(StatisticsMetric.CONNECTIVITY_TEST_DURATION_MS.description())//
				.tag("source_id", source).//
				register(registry);
		    }
		}

		if (metrics.contains(StatisticsMetric.LAST_SOURCE_UP_UNIX_TIMESTAMP_MS)) {
		    Optional<Long> lastUpMs = harvestingProperties.getLastSourceUpUnixTimestampMs();
		    if (lastUpMs.isPresent()) {
			lastSourceUpUnixTimestampMs.put(source, lastUpMs.get());
			io.micrometer.core.instrument.Gauge.builder(
				StatisticsMetric.LAST_SOURCE_UP_UNIX_TIMESTAMP_MS.prometheusName(), lastSourceUpUnixTimestampMs,
				g -> g.get(source))//
				.description(StatisticsMetric.LAST_SOURCE_UP_UNIX_TIMESTAMP_MS.description())//
				.tag("source_id", source).//
				register(registry);
		    }
		}

		if (metrics.contains(StatisticsMetric.PLATFORMS_TOTAL)) {
		    platforms.put(source, Integer.parseInt(stats.getSiteCount()));
		    io.micrometer.core.instrument.Gauge.builder(StatisticsMetric.PLATFORMS_TOTAL.prometheusName(), platforms,
			    g -> g.get(source))//
			    .description(StatisticsMetric.PLATFORMS_TOTAL.description())//
			    .tag("source_id", source).//
			    register(registry);
		}

		if (metrics.contains(StatisticsMetric.VARIABLES_TOTAL)) {
		    variables.put(source, Integer.parseInt(stats.getAttributeCount()));
		    io.micrometer.core.instrument.Gauge.builder(StatisticsMetric.VARIABLES_TOTAL.prometheusName(), variables,
			    g -> g.get(source))//
			    .description(StatisticsMetric.VARIABLES_TOTAL.description())//
			    .tag("source_id", source).//
			    register(registry);
		}

		if (metrics.contains(StatisticsMetric.METADATA_COMPLETENESS) && discoveryFinder != null && reportView.isPresent()) {
		    try {
			View v = reportView.get();
			int totalRecords = discoveryFinder.count(newDiscoveryMessageForSource(v, source)).getCount();
			for (MetadataElement completenessElement : metadataCompletenessElements) {
			    double pct = occurrencePercentGivenTotal(discoveryFinder, newDiscoveryMessageForSource(v, source),
				    totalRecords, completenessElement);
			    final String elementTag = prometheusElementTag(completenessElement);
			    final String completenessKey = metadataCompletenessKey(source, elementTag);
			    metadataCompleteness.put(completenessKey, pct);
			    io.micrometer.core.instrument.Gauge.builder(StatisticsMetric.METADATA_COMPLETENESS.prometheusName(),
				    metadataCompleteness, g -> g.getOrDefault(completenessKey, 0.0))//
				    .description(StatisticsMetric.METADATA_COMPLETENESS.description())//
				    .tags("source_id", source, "element", elementTag)//
				    .register(registry);
			}
		    } catch (GSException e) {
			GSLoggerFactory.getLogger(getClass()).warn("metadata completeness count failed for source {}: {}", source,
				e.getMessage());
		    }
		}

		if (metrics.contains(StatisticsMetric.METADATA_SET_COMPLETENESS) && discoveryFinder != null && reportView.isPresent()) {
		    try {
			View v = reportView.get();
			int totalRecords = discoveryFinder.count(newDiscoveryMessageForSource(v, source)).getCount();
			for (MetadataCompletenessSet mcs : metadataCompletenessSets) {
			    double pct = occurrencePercentAllElements(discoveryFinder, newDiscoveryMessageForSource(v, source),
				    totalRecords, mcs.elements);
			    final String setKey = metadataCompletenessSetKey(source, mcs.setNameTag);
			    metadataCompletenessBySet.put(setKey, pct);
			    io.micrometer.core.instrument.Gauge.builder(StatisticsMetric.METADATA_SET_COMPLETENESS.prometheusName(),
				    metadataCompletenessBySet, g -> g.getOrDefault(setKey, 0.0))//
				    .description(StatisticsMetric.METADATA_SET_COMPLETENESS.description())//
				    .tags("source_id", source, "element_set", mcs.setNameTag)//
				    .register(registry);
			}
		    } catch (GSException e) {
			GSLoggerFactory.getLogger(getClass()).warn("metadata set completeness count failed for source {}: {}", source,
				e.getMessage());
		    }
		}

		// String content = "<tr><td colspan='15'><br/>"//
		// + "Data provider: <b>" + source + "</b><br/>"//
		// + "#Platforms: " + stats.getSiteCount() + "<br/>"//
		// + "#Variables:" + stats.getAttributeCount() + "<br/>"//
		// + "#Timeseries:" + stats.getTimeSeriesCount() + "<br/>"//
		// + "Begin:" + stats.getBegin() + "<br/>"//
		// + "End:" + stats.getEnd() + "<br/>"//
		// + "BBOX(w,s,e,n): " + stats.getWest() + "," + stats.getSouth() + "," +
		// stats.getEast() +
		// ","
		// + stats.getNorth() + "<br/>" //
		// + "Altitude:" + stats.getMinimumAltitude() + "/" + stats.getMaximumAltitude() +
		// "<br/>"//
		// + "</td></tr>" + "" //
		// + "<tr>";
	    } catch (Exception e) {
		e.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }

	    long now = System.currentTimeMillis();
	    if (now - taskStartMs >= CANCEL_CHECK_INTERVAL_MS
		    && (lastCancelCheckMs == 0 || now - lastCancelCheckMs >= CANCEL_CHECK_INTERVAL_MS)) {
		lastCancelCheckMs = now;
		if (ConfigurationWrapper.isJobCanceled(context)) {
		    GSLoggerFactory.getLogger(getClass()).info("Statistics task CANCELED (view / artifact key: {})", statsArtifactKey);

		    status.setPhase(JobPhase.CANCELED);
		    return;
		}
	    }

	}

	GSLoggerFactory.getLogger(getClass()).info("Updated metrics for {}",
		(viewId != null && !viewId.isEmpty()) ? ("view " + viewId) : "all sources (no view)");

	Optional<S3TransferWrapper> optS3TransferManager = getS3TransferManager();

	if (optS3TransferManager.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).info("Transfer download stats to s3 STARTED");

	    S3TransferWrapper manager = optS3TransferManager.get();
	    manager.setACLPublicRead(true);

	    File tempFile = File.createTempFile(getClass().getSimpleName() + statsArtifactKey, ".txt");

	    String text = registry.scrape();

	    Files.copy(IOStreamUtils.asStream(text), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

	    manager.uploadFile(
		    tempFile.getAbsolutePath(), 
		    "dabreporting", 
		    "monitoring/" + statsArtifactKey + ".txt");

	    tempFile.delete();

	    GSLoggerFactory.getLogger(getClass()).info("Transfer of statistics to s3 ENDED");
	}

	log(status, "Statistics task ENDED");
    }

    private static String metadataCompletenessKey(String sourceId, String elementTag) {

	return sourceId + '\0' + "e:" + elementTag;
    }

    private static String metadataCompletenessSetKey(String sourceId, String elementSetTag) {

	return sourceId + '\0' + "s:" + elementSetTag;
    }

    static Map<String, String> extractMetadataSetCompletenessDefinitionLines(String raw) {

	Map<String, String> byNormalizedSuffix = new LinkedHashMap<>();
	if (raw == null || raw.isBlank()) {
	    return byNormalizedSuffix;
	}
	for (String line : raw.split("\\R")) {
	    String[] parts = line.trim().split("=", 2);
	    if (parts.length != 2) {
		continue;
	    }
	    String key = parts[0].trim();
	    if (key.length() <= METADATA_SET_COMPLETENESS_LINE_PREFIX.length()) {
		continue;
	    }
	    if (!key.regionMatches(true, 0, METADATA_SET_COMPLETENESS_LINE_PREFIX, 0,
		    METADATA_SET_COMPLETENESS_LINE_PREFIX.length())) {
		continue;
	    }
	    String suffix = key.substring(METADATA_SET_COMPLETENESS_LINE_PREFIX.length()).trim();
	    if (suffix.isEmpty()) {
		continue;
	    }
	    byNormalizedSuffix.putIfAbsent(normalizeKey(suffix), parts[1].trim());
	}
	return byNormalizedSuffix;
    }

    static List<MetadataCompletenessSet> parseMetadataSetCompleteness(EnumMap<StatisticsTaskOptions, String> structured,
	    String rawTaskOptions) {

	String setsLine = structured.get(StatisticsTaskOptions.METADATA_SET_COMPLETENESS);
	if (setsLine == null || setsLine.isBlank()) {
	    return List.of();
	}
	Map<String, String> defByNormalizedSuffix = extractMetadataSetCompletenessDefinitionLines(rawTaskOptions);
	List<MetadataCompletenessSet> out = new ArrayList<>();
	for (String token : setsLine.split(",")) {
	    String name = token.trim();
	    if (name.isEmpty()) {
		continue;
	    }
	    String def = defByNormalizedSuffix.get(normalizeKey(name));
	    if (def == null) {
		GSLoggerFactory.getLogger(StatisticsTask.class).warn(
			"No {} line for set name '{}'; skipping (expected e.g. {}={})",
			METADATA_SET_COMPLETENESS_LINE_PREFIX + name, name, METADATA_SET_COMPLETENESS_LINE_PREFIX + name,
			"IDENTIFIER,TITLE");
		continue;
	    }
	    List<MetadataElement> els = parseMetadataCompletenessElements(def);
	    if (els.isEmpty()) {
		GSLoggerFactory.getLogger(StatisticsTask.class).warn("{} had no valid elements; skipping set '{}'",
			METADATA_SET_COMPLETENESS_LINE_PREFIX + name, name);
		continue;
	    }
	    out.add(new MetadataCompletenessSet(name, els));
	}
	return out;
    }



    /** Lowercase enum name for Prometheus {@code element} label (e.g. {@code bounding_box}). */
    private static String prometheusElementTag(MetadataElement element) {

	return element.name().toLowerCase();
    }

    /**
     * Comma-separated {@link MetadataElement} selectors: enum constant names and/or harmonized {@link MetadataElement#getName()}
     * values (same normalization as task option keys). Unknown tokens are logged and skipped; order is preserved, duplicates
     * removed.
     */
    static List<MetadataElement> parseMetadataCompletenessElements(String raw) {

	Map<String, MetadataElement> byNormalized = new LinkedHashMap<>();
	for (MetadataElement e : MetadataElement.values()) {
	    byNormalized.putIfAbsent(normalizeKey(e.name()), e);
	    byNormalized.putIfAbsent(normalizeKey(e.getName()), e);
	}
	List<MetadataElement> out = new ArrayList<>();
	Set<MetadataElement> seen = new HashSet<>();
	for (String token : raw.split(",")) {
	    String n = normalizeKey(token.trim());
	    if (n.isEmpty()) {
		continue;
	    }
	    MetadataElement e = byNormalized.get(n);
	    if (e != null) {
		if (seen.add(e)) {
		    out.add(e);
		}
	    } else {
		GSLoggerFactory.getLogger(StatisticsTask.class).warn("Unknown METADATA_COMPLETENESS_ELEMENTS token: {}",
			token.trim());
	    }
	}
	return out;
    }

    private static DiscoveryMessage newDiscoveryMessageForSource(View view, String sourceId) {

	DiscoveryMessage message = new DiscoveryMessage();
	List<GSSource> sources = new ArrayList<>();
	sources.add(ConfigurationWrapper.getSource(sourceId));
	message.setSources(sources);
	message.setView(view);
	Bond base = BondFactory.createAndBond(view.getBond(), BondFactory.createSourceIdentifierBond(sourceId));
	message.setUserBond(base);
	message.setPermittedBond(base);
	return message;
    }

    /**
     * Same logic as {@code blue-cloud-report.jsp}: percentage of records where the element exists
     * ({@link BondFactory#createExistsSimpleValueBond} on the element queryable), relative to {@code totalRecords}
     * for the same view and source.
     */
    private static double occurrencePercentGivenTotal(DatabaseFinder finder, DiscoveryMessage baseMessage, int totalRecords,
	    MetadataElement element) throws GSException {

	if (totalRecords == 0) {
	    return 0.0;
	}
	Bond baseClone = baseMessage.getUserBond().get().clone();
	Bond target = getBond(element);

	Bond withElement = BondFactory.createAndBond(target, baseClone);

	baseMessage.setUserBond(withElement);
	baseMessage.setPermittedBond(withElement);
	int withCount = finder.count(baseMessage).getCount();
	return 100.0 * withCount / totalRecords;
    }

    /**
     * Share of records where <b>all</b> {@link MetadataElement}s exist (successive {@link BondFactory#createAndBond} with
     * {@link BondFactory#createExistsSimpleValueBond}).
     */
    private static double occurrencePercentAllElements(DatabaseFinder finder, DiscoveryMessage baseMessage, int totalRecords,
	    List<MetadataElement> elements) throws GSException {

	if (totalRecords == 0 || elements.isEmpty()) {
	    return 0.0;
	}
	Bond combined = baseMessage.getUserBond().get().clone();
	for (MetadataElement el : elements) {
	    combined = BondFactory.createAndBond(getBond(el), combined);
	}
	baseMessage.setUserBond(combined);
	baseMessage.setPermittedBond(combined);
	int withCount = finder.count(baseMessage).getCount();
	return 100.0 * withCount / totalRecords;
    }

    private static Bond getBond(MetadataElement element) {
	Bond target = null;
	switch (element){
	case TEMP_EXTENT:
	    target = BondFactory.createOrBond(BondFactory.createExistsSimpleValueBond(MetadataElement.TEMP_EXTENT_BEGIN),BondFactory.createExistsSimpleValueBond(MetadataElement.TEMP_EXTENT_END),BondFactory.createExistsSimpleValueBond(MetadataElement.TEMP_EXTENT_BEGIN_NOW),BondFactory.createExistsSimpleValueBond(MetadataElement.TEMP_EXTENT_END_NOW),BondFactory.createExistsSimpleValueBond(MetadataElement.TEMP_EXTENT_BEGIN_BEFORE_NOW));
	    break;
	default:
	    target = BondFactory.createExistsSimpleValueBond(element);
	    break;
	}
	return target;
    }

    /**
     * Comma-separated metric names (same normalization as task option keys: ignore underscores/spaces, case-insensitive).
     * Unknown names are logged and skipped. If nothing valid remains, all metrics are used.
     */
    static Set<StatisticsMetric> parseMetricsList(String raw) {

	Map<String, StatisticsMetric> byNormalized = new LinkedHashMap<>();
	for (StatisticsMetric m : StatisticsMetric.values()) {
	    byNormalized.putIfAbsent(normalizeKey(m.name()), m);
	    byNormalized.putIfAbsent(normalizeKey(m.prometheusName()), m);
	}

	EnumSet<StatisticsMetric> set = EnumSet.noneOf(StatisticsMetric.class);
	for (String token : raw.split(",")) {
	    String n = normalizeKey(token.trim());
	    if (n.isEmpty()) {
		continue;
	    }
	    StatisticsMetric m = byNormalized.get(n);
	    if (m != null) {
		set.add(m);
	    } else {
		GSLoggerFactory.getLogger(StatisticsTask.class).warn("Unknown statistics metric in METRICS option: {}", token.trim());
	    }
	}
	if (set.isEmpty()) {
	    return EnumSet.allOf(StatisticsMetric.class);
	}
	return set;
    }

    private static String normalizeKey(String key) {

	return key.replaceAll("[_\\s]", "").toUpperCase();
    }

    @Override
    public String getName() {

	return "Statistics task";
    }
}
