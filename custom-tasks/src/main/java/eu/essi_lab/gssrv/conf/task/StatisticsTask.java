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
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Year;
import java.time.ZoneOffset;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.quartz.JobExecutionContext;

import eu.essi_lab.access.availability.AvailabilityMonitor;
import eu.essi_lab.access.availability.DownloadInformation;
import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseExecutor;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.OptionsKey;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.skos.SKOSClient;
import eu.essi_lab.lib.skos.SKOSClient.SearchTarget;
import eu.essi_lab.lib.skos.SKOSConcept;
import eu.essi_lab.lib.skos.SKOSResponse;
import eu.essi_lab.lib.skos.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skos.expander.ExpansionLimit;
import eu.essi_lab.lib.skos.expander.ExpansionLimit.LimitTarget;
import eu.essi_lab.cfga.gs.setting.ontology.OntologySetting;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.SearchAfter;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.stats.ComputationResult;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.RuntimeInfoElement;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.model.resource.ResourceType;
import eu.essi_lab.pdk.wrt.WebRequestTransformer;
import eu.essi_lab.profiler.semantic.SourceStatistics;
import eu.essi_lab.profiler.semantic.Stats;
import eu.essi_lab.shared.driver.es.stats.ElasticsearchClient;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * Gathers statistics info
 * 
 * Task options (line-based {@code KEY=value}):
 * <ul>
 * <li>{@code VIEW_ID} — optional view identifier; when omitted, aggregate statistics and metadata completeness use all
 * sources with no view bond (per-source completeness counts include every resource of type {@link ResourceType#DATASET}
 * for that source). When set, the view’s bond restricts which records completeness percentages apply to; if the view id
 * is invalid, metadata completeness metrics are skipped</li>
 * <li>{@code METRICS} — optional comma-separated list of {@link StatisticsMetric} names; when omitted, all metrics are
 * computed</li>
 * <li>{@code METADATA_COMPLETENESS_ELEMENTS} — optional comma-separated {@link MetadataElement} identifiers for
 * {@link StatisticsMetric#METADATA_COMPLETENESS}: enum names (e.g. {@code IDENTIFIER,TITLE,BOUNDING_BOX}) or harmonized
 * index names (e.g. {@code fileId,title,bbox}); when omitted, defaults to {@code IDENTIFIER,TITLE}</li>
 * <li>{@code RECORDS_PER_ELEMENT_ELEMENTS} — comma-separated {@link MetadataElement} identifiers for
 * {@link StatisticsMetric#RECORDS_PER_ELEMENT} (same token syntax as {@code METADATA_COMPLETENESS_ELEMENTS}); when this
 * metric is enabled and the line is missing or has no valid entries, the metric is skipped</li>
 * <li>{@code METADATA_SET_COMPLETENESS} — comma-separated set names for {@link StatisticsMetric#METADATA_SET_COMPLETENESS}
 * (e.g. {@code core,optional})</li>
 * <li>{@code METADATA_SET_COMPLETENESS_&lt;name&gt;} — for each set name, comma-separated {@link MetadataElement} values;
 * metric is the share of <b>dataset</b> resources having <b>all</b> listed elements (logical AND). Ex.:
 * {@code METADATA_SET_COMPLETENESS_core=IDENTIFIER} and
 * {@code METADATA_SET_COMPLETENESS_optional=IDENTIFIER,TITLE,BOUNDING_BOX}</li>
 * <li>{@code PUBLISH_ON_S3} — when {@code false}, {@code 0}, or {@code no}, skip uploading the Prometheus scrape to S3;
 * default is {@code true} (publish).</li>
 * </ul>
 *
 * <h3>Available Prometheus metrics ({@link StatisticsMetric})</h3>
 * <p>
 * Each metric is registered as a Micrometer/Prometheus gauge. Most are per {@code source_id}; metadata completeness
 * metrics use {@code VIEW_ID} when set to filter records, or all {@link ResourceType#DATASET} resources per source when it
 * is omitted. Subset via {@code METRICS}.
 * </p>
 * <dl>
 * <dt>{@link StatisticsMetric#SOURCE_INFO SOURCE_INFO} ({@code source_info})</dt>
 * <dd>Constant {@code 1} used as a carrier for labels {@code source_id} and {@code source_label} (human-readable name),
 * so monitoring systems can resolve source identity alongside other series.</dd>
 *
 * <dt>{@link StatisticsMetric#OBSERVED_PROPERTY_INFO OBSERVED_PROPERTY_INFO} ({@code observed_property_info})</dt>
 * <dd>Constant {@code 1} used as a carrier for labels {@code uri}, {@code preferred_label_ita} (Italian),
 * {@code preferred_label_eng} (English), and {@code view} (same {@code VIEW_ID} filter as the OpenSearch query, when set).
 * Exposes the top observed property URIs from runtime statistics
 * ({@link RuntimeInfoElement#DISCOVERY_MESSAGE_OBSERVED_PROPERTY_URI}) with human-readable labels resolved via
 * {@link SKOSClient} using the ontologies configured in the system. Requires statistics DB settings.</dd>
 *
 * <dt>{@link StatisticsMetric#COUNTRY_INFO COUNTRY_INFO} ({@code country_info})</dt>
 * <dd>Constant {@code 1} used as a carrier for labels {@code short_name}, {@code official_name}, {@code iso2} and
 * {@code iso3}. One gauge per {@link Country} enum value, so monitoring systems can resolve country codes.</dd>
 *
 * <dt>{@link StatisticsMetric#DOWNLOAD_AVAILABILITY DOWNLOAD_AVAILABILITY} ({@code download_availability})</dt>
 * <dd>{@code 1} if the last successful download is newer than the last failed download (or there is no failed download),
 * {@code 0} otherwise. Reflects whether downloads from the source are currently considered available.</dd>
 *
 * <dt>{@link StatisticsMetric#HARVESTED_RECORDS HARVESTED_RECORDS} ({@code harvested_records})</dt>
 * <dd>Number of harvested time series for the source (from semantic statistics: time series count).</dd>
 *
 * <dt>{@link StatisticsMetric#LAST_HARVESTING_UNIX_TIMESTAMP_MS LAST_HARVESTING_UNIX_TIMESTAMP_MS}
 * ({@code last_harvesting_unix_timestamp_ms})</dt>
 * <dd>Unix epoch <b>milliseconds</b> of the last harvesting run end time, from harvesting properties. Omitted if no end
 * timestamp is stored.</dd>
 *
 * <dt>{@link StatisticsMetric#LAST_SUCCESSFUL_INGESTION_HARVEST_UNIX_TIMESTAMP_MS
 * LAST_SUCCESSFUL_INGESTION_HARVEST_UNIX_TIMESTAMP_MS} ({@code last_successful_ingestion_harvest_unix_timestamp_ms})</dt>
 * <dd>Unix epoch <b>milliseconds</b> of the last <b>completed</b> harvest end time when {@link HarvestingProperties}
 * reports {@code resourcesCount &gt; 0} and {@link SourceStorageWorker#consolidatedFolderSurvives()} matches
 * {@link ResourcesComparatorTask}: the consolidated snapshot does not “survive” (optional empty or {@code false}), i.e.
 * the same situation where remote-sourced records are processed for comparison. Omitted when the last run was not
 * completed, left no resources, consolidated survives, or end time is missing.</dd>
 *
 * <dt>{@link StatisticsMetric#SOURCE_UP SOURCE_UP} ({@code source_up})</dt>
 * <dd>{@code 1} if the source is marked up in harvesting properties, {@code 0} if down or unknown.</dd>
 *
 * <dt>{@link StatisticsMetric#CONNECTIVITY_TEST_DURATION_MS CONNECTIVITY_TEST_DURATION_MS}
 * ({@code connectivity_test_duration_ms})</dt>
 * <dd>Duration in milliseconds of the last connectivity test toward the source. Omitted if not recorded.</dd>
 *
 * <dt>{@link StatisticsMetric#CONNECTIVITY_TEST_UNIX_TIMESTAMP_MS CONNECTIVITY_TEST_UNIX_TIMESTAMP_MS}
 * ({@code connectivity_test_unix_timestamp_ms})</dt>
 * <dd>Unix epoch milliseconds when the last connectivity test completed for the source. Omitted if not recorded.</dd>
 *
 * <dt>{@link StatisticsMetric#LAST_SOURCE_UP_UNIX_TIMESTAMP_MS LAST_SOURCE_UP_UNIX_TIMESTAMP_MS}
 * ({@code last_source_up_unix_timestamp_ms})</dt>
 * <dd>Unix epoch milliseconds when the source was last observed as up during a connectivity test. Omitted if not
 * recorded.</dd>
 *
 * <dt>{@link StatisticsMetric#PLATFORMS_TOTAL PLATFORMS_TOTAL} ({@code platforms_total})</dt>
 * <dd>Total number of platforms (sites) for the source in semantic statistics.</dd>
 *
 * <dt>{@link StatisticsMetric#VARIABLES_TOTAL VARIABLES_TOTAL} ({@code variables_total})</dt>
 * <dd>Total number of variables (attributes) for the source in semantic statistics.</dd>
 *
 * <dt>{@link StatisticsMetric#METADATA_COMPLETENESS METADATA_COMPLETENESS} ({@code metadata_completeness})</dt>
 * <dd>Per metadata element, the fraction of <b>dataset</b> resources (for that source) where the element is present,
 * within the {@code VIEW_ID} filter if set, otherwise among all {@link ResourceType#DATASET} resources of the source.
 * Expressed as a percentage {@code 0–100}. Series are distinguished by label {@code element} (see
 * {@code METADATA_COMPLETENESS_ELEMENTS}).</dd>
 *
 * <dt>{@link StatisticsMetric#METADATA_SET_COMPLETENESS METADATA_SET_COMPLETENESS} ({@code metadata_set_completeness})</dt>
 * <dd>Per named set, the fraction of <b>dataset</b> resources (for that source) where <b>all</b> elements in that set are
 * present (logical AND), within the {@code VIEW_ID} filter if set, otherwise among all {@link ResourceType#DATASET}
 * resources of the source. Percentage {@code 0–100}. Series use label {@code element_set} (see
 * {@code METADATA_SET_COMPLETENESS} and {@code METADATA_SET_COMPLETENESS_&lt;name&gt;}).</dd>
 *
 * <dt>{@link StatisticsMetric#RECORDS_PER_ELEMENT RECORDS_PER_ELEMENT} ({@code records_per_element})</dt>
 * <dd>Per metadata element and per distinct indexed value, the number of <b>dataset</b> resources for that source
 * (absolute count). Labels: {@code source_id}, {@code element} (lowercase enum name, same as metadata completeness),
 * {@code value}. Uses the same view/source/resource-type scope as metadata completeness ({@link ResourceType#DATASET});
 * configure elements via {@code RECORDS_PER_ELEMENT_ELEMENTS}.</dd>
 *
 * <dt>{@link StatisticsMetric#STATION_PAGE_VISITS_TOTAL STATION_PAGE_VISITS_TOTAL} ({@code station_page_visits_total})</dt>
 * <dd>From request statistics (OpenSearch): all-time request counts per {@code source_id} (aggregation on
 * {@link RuntimeInfoElement#RESULT_SET_DISCOVERY_SOURCE_ID}), filtered by {@code VIEW_ID=view},
 * {@code PROFILER_NAME=BNHSProfiler}, {@code DISCOVERY_MESSAGE_isTimeseries=true}. Requires statistics DB settings.</dd>
 *
 * <dt>{@link StatisticsMetric#OM_DOWNLOADS_TOTAL OM_DOWNLOADS_TOTAL} ({@code om_downloads_total})</dt>
 * <dd>From request statistics: all-time counts per {@code source_id} (same aggregation), filtered by
 * {@code VIEW_ID=.tag("view", PORTAL_SEARCHES_VIEW)//}, {@code PROFILER_NAME=OMProfiler}. Requires statistics DB settings.</dd>
 *
 * <dt>{@link StatisticsMetric#SEARCH_ATTRIBUTE_TITLE_TOTAL SEARCH_ATTRIBUTE_TITLE_TOTAL} ({@code search_attribute_title_total})</dt>
 * <dd>From request statistics: all-time counts per {@code attribute_title} (terms on
 * {@link RuntimeInfoElement#DISCOVERY_MESSAGE_ATTRIBUTE_TITLE}), filtered by {@code VIEW_ID} (when set) and
 * {@code PROFILER_NAME=OSProfiler}. Labels: {@code attribute_title}, {@code view}. Requires statistics DB settings.</dd>
 *
 * <dt>{@link StatisticsMetric#SEARCH_OBSERVED_PROPERTY_URI_TOTAL SEARCH_OBSERVED_PROPERTY_URI} ({@code search_observed_property_uri_total})</dt>
 * <dd>From request statistics: all-time counts per {@code observed_property_uri} (terms on
 * {@link RuntimeInfoElement#DISCOVERY_MESSAGE_OBSERVED_PROPERTY_URI}), filtered by {@code VIEW_ID} (when set) and
 * {@code PROFILER_NAME=OSProfiler}. Labels: {@code observed_property_uri}, {@code view}. Requires statistics DB settings.</dd>
 *
 * <dt>{@link StatisticsMetric#PORTAL_SEARCHES_TOTAL PORTAL_SEARCHES_TOTAL} ({@code portal_search_total})</dt>
 * <dd>From request statistics: all-time count of requests with {@code VIEW_ID=view1} and {@code PROFILER_NAME=OSProfiler}.
 * Label {@code view} is the view id. Requires statistics DB settings.</dd>
 *
 * <dt>{@link StatisticsMetric#SEARCH_REQUESTS_GEOHASH_TOTAL SEARCH_REQUESTS_GEOHASH_TOTAL} ({@code search_requests_geohash_total})</dt>
 * <dd>From request statistics: top 50 geohash cells (precision 3) by count on
 * {@link RuntimeInfoElement#DISCOVERY_MESSAGE_SHAPE_GEO} ({@code geohash_grid} on stored geo_shape from user bboxes), filtered
 * by {@code VIEW_ID} (when set) and {@code PROFILER_NAME=OSProfiler}. Labels: {@code geohash}, {@code view}. Requires
 * statistics DB settings.</dd>
 *
 * <dt>{@link StatisticsMetric#SEARCH_REQUESTS_TIME_YEAR_TOTAL SEARCH_REQUESTS_TIME_YEAR_TOTAL}
 * ({@code search_requests_time_year_total})</dt>
 * <dd>From request statistics: counts per calendar year where the user temporal interval overlaps that year (UTC), on
 * {@link RuntimeInfoElement#DISCOVERY_MESSAGE_TMP_EXTENT_BEGIN}/{@link RuntimeInfoElement#DISCOVERY_MESSAGE_TMP_EXTENT_END};
 * filtered by {@code VIEW_ID} (when set) and {@code PROFILER_NAME=OSProfiler}. Labels: {@code year}, {@code view}. Requires
 * statistics DB settings.</dd>
 * </dl>
 *
 * Legacy: a single line without {@code =} is treated as the view id (same as only {@code VIEW_ID} before).
 * 
 * @author boldrini
 */
public class StatisticsTask extends AbstractCustomTask {

    public enum StatisticsTaskOptions implements OptionsKey {
	VIEW_ID,
	METRICS,
	METADATA_COMPLETENESS_ELEMENTS,
	/** Comma-separated {@link MetadataElement} selectors for {@link StatisticsMetric#RECORDS_PER_ELEMENT}. */
	RECORDS_PER_ELEMENT_ELEMENTS,
	/** Comma-separated set names; members defined by {@code METADATA_SET_COMPLETENESS_<name>=...} lines. */
	METADATA_SET_COMPLETENESS,

	/** If set to false, skip S3 upload of the metrics scrape (default: publish). */
	PUBLISH_ON_S3
    }

    /**
     * Prometheus statistics gauges this task can expose (subset per {@code METRICS} option).
     */
    public enum StatisticsMetric {

	SOURCE_INFO("source_info", "Metadata about each source."),
	OBSERVED_PROPERTY_INFO("observed_property_info",
		"Descriptive info for top observed properties from runtime statistics "
			+ "(tags: uri, preferred_label_ita, preferred_label_eng, view)"),
	COUNTRY_INFO("country_info",
		"Descriptive info for each country (tags: short_name, official_name, iso2, iso3)"),
	DOWNLOAD_AVAILABILITY("download_availability", "Download availability "),
	HARVESTED_RECORDS("harvested_records", "Total number of harvested records "),
	LAST_HARVESTING_UNIX_TIMESTAMP_MS("last_harvesting_unix_timestamp_ms",
		"Last harvesting end timestamp in Unix epoch milliseconds"),
	LAST_SUCCESSFUL_INGESTION_HARVEST_UNIX_TIMESTAMP_MS("last_successful_ingestion_harvest_unix_timestamp_ms",
		"Last completed harvest end time (Unix ms) when resourcesCount>0 and consolidated folder does not survive "
			+ "(same gate as ResourcesComparatorTask)"),
	SOURCE_UP("source_up", "Source connectivity status (1=up, 0=down)"),
	CONNECTIVITY_TEST_DURATION_MS("connectivity_test_duration_ms",
		"Duration of the last source connectivity test in milliseconds"),
	CONNECTIVITY_TEST_UNIX_TIMESTAMP_MS("connectivity_test_unix_timestamp_ms",
		"Unix epoch milliseconds when the last source connectivity test completed"),
	LAST_SOURCE_UP_UNIX_TIMESTAMP_MS("last_source_up_unix_timestamp_ms",
		"Unix epoch milliseconds when the source was last observed up in a connectivity test"),
	PLATFORMS_TOTAL("platforms_total", "Total number of platforms "),
	VARIABLES_TOTAL("variables_total", "Total number of variables "),
	METADATA_COMPLETENESS("metadata_completeness",
		"Per-element share among Dataset-type resources where the field exists (tag element); elements from METADATA_COMPLETENESS_ELEMENTS"),

	METADATA_SET_COMPLETENESS("metadata_set_completeness",
		"Per named set: share of Dataset-type resources where all listed metadata elements exist (tag element_set); "
			+ "configured via METADATA_SET_COMPLETENESS and METADATA_SET_COMPLETENESS_<name> lines"),

	RECORDS_PER_ELEMENT("records_per_element",
		"Per element and distinct value: Dataset-type resource count for the source (tags element, value); "
			+ "elements from RECORDS_PER_ELEMENT_ELEMENTS"),

	STATION_PAGE_VISITS_TOTAL("station_page_visits_total",
		"Request counts per source_id (BNHS / view / timeseries discovery) from OpenSearch statistics"),

	OM_DOWNLOADS_TOTAL("om_downloads_total", "Request counts per source_id (OMProfiler / view) from OpenSearch statistics"),

	SEARCH_ATTRIBUTE_TITLE_TOTAL("search_attribute_title_total",
		"Request counts per DISCOVERY_MESSAGE_attributeTitle bucket from OpenSearch statistics"),

	SEARCH_OBSERVED_PROPERTY_URI_TOTAL("search_observed_property_uri_total",
		"Request counts per DISCOVERY_MESSAGE_observedPropertyURI bucket from OpenSearch statistics"),

	PORTAL_SEARCHES_TOTAL("portal_search_total",
		"All-time OpenSearch request count for VIEW_ID=view1 and PROFILER_NAME=OSProfiler (label view)"),

	SEARCH_REQUESTS_GEOHASH_TOTAL("search_requests_geohash_total",
		"OpenSearch geohash_grid (precision 3, top 50) on DISCOVERY_MESSAGE_SHAPE for OSProfiler / view (labels geohash, view)"),

	SEARCH_REQUESTS_TIME_YEAR_TOTAL("search_requests_time_year_total",
		"OpenSearch request counts per calendar year overlapping user temporal extent (TMP_EXTENT begin/end); "
			+ "OSProfiler / view (labels year, view)");

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

    /** Matches {@link RuntimeInfoElement#VIEW_ID} for HIS Central runtime statistics filters. */

    private static final String RUNTIME_STATS_PROFILER_BNHS = "BNHSProfiler";

    private static final String RUNTIME_STATS_PROFILER_OM = "OMProfiler";

    private static final String PORTAL_SEARCHES_PROFILER_OS = "OSProfiler";

    private static final int RUNTIME_STATS_MAX_BUCKETS = 10_000;

    /**
     * {@link RuntimeInfoElement#DISCOVERY_MESSAGE_SHAPE_GEO} geohash length for
     * {@link StatisticsMetric#SEARCH_REQUESTS_GEOHASH_TOTAL} (e.g. 3 -> ~150km cells).
     */
    private static final int RUNTIME_STATS_GEOHASH_GRID_PRECISION = 3;

    /** Max geohash buckets returned (top by count) for {@link StatisticsMetric#SEARCH_REQUESTS_GEOHASH_TOTAL}. */
    private static final int RUNTIME_STATS_GEOHASH_TOP_BUCKETS = 50;

    /** Max observed property URI buckets for {@link StatisticsMetric#OBSERVED_PROPERTY_INFO}. */
    private static final int OBSERVED_PROPERTY_TOP_BUCKETS = 100;

    /**
     * JVM-wide cache for resolved observed property labels (key: {@code uri + '\0' + lang}, value: preferred label).
     * Survives across task runs; ontology labels rarely change.
     */
    private static final ConcurrentHashMap<String, String> observedPropertyLabelCache = new ConcurrentHashMap<>();

    /**
     * Inclusive calendar year range for {@link StatisticsMetric#SEARCH_REQUESTS_TIME_YEAR_TOTAL}; upper bound uses the
     * current UTC year when the statistics task runs.
     */
    private static final int RUNTIME_STATS_YEAR_RANGE_MIN = 1990;

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
	List<MetadataElement> recordsPerElementElements = new ArrayList<>();
	List<MetadataCompletenessSet> metadataCompletenessSets = List.of();

	boolean publishOnS3 = true;
	if (structured.isPresent()) {
	    String publishOpt = structured.get().get(StatisticsTaskOptions.PUBLISH_ON_S3);
	    if (publishOpt != null && !publishOpt.isBlank()) {
		publishOnS3 = parsePublishOnS3(publishOpt);
	    }
	}

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
	    String recordsPerElementLine = structured.get().get(StatisticsTaskOptions.RECORDS_PER_ELEMENT_ELEMENTS);
	    if (recordsPerElementLine != null && !recordsPerElementLine.isBlank()) {
		List<MetadataElement> parsed = parseMetadataCompletenessElements(recordsPerElementLine);
		if (parsed.isEmpty()) {
		    GSLoggerFactory.getLogger(getClass()).warn(
			    "RECORDS_PER_ELEMENT_ELEMENTS had no valid entries; records_per_element metrics will be skipped");
		} else {
		    recordsPerElementElements = parsed;
		}
	    }
	}

	if (viewId == null || viewId.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).info(
		    "No view specified (VIEW_ID empty); statistics are computed for all sources (no view filter).");
	}

	if (metrics.contains(StatisticsMetric.RECORDS_PER_ELEMENT) && recordsPerElementElements.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).warn(
		    "RECORDS_PER_ELEMENT is enabled but RECORDS_PER_ELEMENT_ELEMENTS is missing or empty; skipping records_per_element metrics");
	}

	GSLoggerFactory.getLogger(getClass()).info(
		"Updating metrics for {} (metrics: {}, metadata completeness elements: {}, records per element elements: {}, metadata set completeness: {})",
		(viewId != null && !viewId.isEmpty()) ? ("view " + viewId) : "all sources (no view)",
		metrics, metadataCompletenessElements, recordsPerElementElements, metadataCompletenessSets);

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
	HashMap<String, Long> connectivityTestUnixTimestampMs = new HashMap<String, Long>();
	HashMap<String, Long> lastSourceUpUnixTimestampMs = new HashMap<String, Long>();
	HashMap<String, Long> lastHarvestingTime = new HashMap<String, Long>();
	HashMap<String, Long> lastSuccessfulIngestionHarvestTime = new HashMap<String, Long>();

	HashMap<String, Double> metadataCompleteness = new HashMap<String, Double>();
	HashMap<String, Double> metadataCompletenessBySet = new HashMap<String, Double>();
	HashMap<String, Double> recordsPerElement = new HashMap<String, Double>();
	GSLoggerFactory.getLogger(getClass()).info("source stats completed");

	Optional<View> reportView = Optional.empty();
	DatabaseFinder discoveryFinder = null;
	DatabaseExecutor databaseExecutor = null;
	if (metrics.contains(StatisticsMetric.RECORDS_PER_ELEMENT) && !recordsPerElementElements.isEmpty()) {
	    try {
		databaseExecutor = DatabaseProviderFactory.getExecutor(ConfigurationWrapper.getStorageInfo());
	    } catch (GSException e) {
		GSLoggerFactory.getLogger(getClass()).warn("Cannot obtain database executor for records_per_element: {}", e.getMessage());
	    }
	}
	if (metrics.contains(StatisticsMetric.METADATA_COMPLETENESS)
		|| metrics.contains(StatisticsMetric.METADATA_SET_COMPLETENESS)
		|| metrics.contains(StatisticsMetric.RECORDS_PER_ELEMENT)) {
	    try {
		boolean completenessAllowed = false;
		if (viewId != null && !viewId.isEmpty()) {
		    reportView = WebRequestTransformer.findView(ConfigurationWrapper.getStorageInfo(), viewId);
		    if (reportView.isEmpty()) {
			GSLoggerFactory.getLogger(getClass()).warn(
				"View {} not found; metadata completeness metrics will be skipped", viewId);
		    } else {
			completenessAllowed = true;
		    }
		} else {
		    completenessAllowed = true;
		}
		if (completenessAllowed) {
		    discoveryFinder = DatabaseProviderFactory.getFinder(ConfigurationWrapper.getStorageInfo());
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
			|| metrics.contains(StatisticsMetric.LAST_SUCCESSFUL_INGESTION_HARVEST_UNIX_TIMESTAMP_MS)
			|| metrics.contains(StatisticsMetric.SOURCE_UP)
			|| metrics.contains(StatisticsMetric.CONNECTIVITY_TEST_DURATION_MS)
			|| metrics.contains(StatisticsMetric.CONNECTIVITY_TEST_UNIX_TIMESTAMP_MS)
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

		if (metrics.contains(StatisticsMetric.LAST_HARVESTING_UNIX_TIMESTAMP_MS) && harvestingProperties != null) {
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

		if (metrics.contains(StatisticsMetric.LAST_SUCCESSFUL_INGESTION_HARVEST_UNIX_TIMESTAMP_MS)
			&& harvestingProperties != null) {
		    if (harvestingProperties.isCompleted().orElse(false) && harvestingProperties.getResourcesCount() > 0) {
			try {
			    	String endHarvestingTimestamp = harvestingProperties.getEndHarvestingTimestamp();
				Optional<Date> endHarvest = ISO8601DateTimeUtils.parseISO8601ToDate(endHarvestingTimestamp);
				if (endHarvest.isPresent()) {
				    long ms = endHarvest.get().getTime();
				    lastSuccessfulIngestionHarvestTime.put(source, ms);
				    io.micrometer.core.instrument.Gauge.builder(
					    StatisticsMetric.LAST_SUCCESSFUL_INGESTION_HARVEST_UNIX_TIMESTAMP_MS.prometheusName(),
					    lastSuccessfulIngestionHarvestTime, g -> g.get(source))//
					    .description(StatisticsMetric.LAST_SUCCESSFUL_INGESTION_HARVEST_UNIX_TIMESTAMP_MS.description())//
					    .tag("source_id", source).//
					    register(registry);
				}

			} catch (Exception e) {
			    GSLoggerFactory.getLogger(getClass()).warn("consolidatedFolderSurvives for source {}: {}", source,
				    e.getMessage());
			}
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

		if (metrics.contains(StatisticsMetric.CONNECTIVITY_TEST_UNIX_TIMESTAMP_MS)) {
		    Optional<Long> testEndMs = harvestingProperties.getConnectivityTestUnixTimestampMs();
		    if (testEndMs.isPresent()) {
			connectivityTestUnixTimestampMs.put(source, testEndMs.get());
			io.micrometer.core.instrument.Gauge.builder(
				StatisticsMetric.CONNECTIVITY_TEST_UNIX_TIMESTAMP_MS.prometheusName(), connectivityTestUnixTimestampMs,
				g -> g.get(source))//
				.description(StatisticsMetric.CONNECTIVITY_TEST_UNIX_TIMESTAMP_MS.description())//
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

		if (metrics.contains(StatisticsMetric.METADATA_COMPLETENESS) && discoveryFinder != null) {
		    try {
			int totalRecords = discoveryFinder.count(newDiscoveryMessageForSource(reportView, source)).getCount();
			for (MetadataElement completenessElement : metadataCompletenessElements) {
			    double pct = occurrencePercentGivenTotal(discoveryFinder, newDiscoveryMessageForSource(reportView, source),
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

		if (metrics.contains(StatisticsMetric.METADATA_SET_COMPLETENESS) && discoveryFinder != null) {
		    try {
			int totalRecords = discoveryFinder.count(newDiscoveryMessageForSource(reportView, source)).getCount();
			for (MetadataCompletenessSet mcs : metadataCompletenessSets) {
			    double pct = occurrencePercentAllElements(discoveryFinder, newDiscoveryMessageForSource(reportView, source),
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

		if (metrics.contains(StatisticsMetric.RECORDS_PER_ELEMENT) && discoveryFinder != null && databaseExecutor != null
			&& !recordsPerElementElements.isEmpty()) {
		    try {
			DiscoveryMessage perSourceMsg = newDiscoveryMessageForSource(reportView, source);
			for (MetadataElement rel : recordsPerElementElements) {
			    final String elementTag = prometheusElementTag(rel);
			    List<TermFrequencyItem> buckets = collectAllTermFrequencies(databaseExecutor, perSourceMsg, rel);
			    for (TermFrequencyItem item : buckets) {
				String valueLabel = item.getTerm() != null ? item.getTerm() : "";
				double n = item.getFreq();
				String key = recordsPerElementKey(source, elementTag, valueLabel);
				recordsPerElement.put(key, n);
				io.micrometer.core.instrument.Gauge.builder(StatisticsMetric.RECORDS_PER_ELEMENT.prometheusName(), recordsPerElement,
					g -> g.getOrDefault(key, 0.0))//
					.description(StatisticsMetric.RECORDS_PER_ELEMENT.description())//
					.tags("source_id", source, "element", elementTag, "value", valueLabel)//
					.register(registry);
			    }
			}
		    } catch (GSException e) {
			GSLoggerFactory.getLogger(getClass()).warn("records_per_element failed for source {}: {}", source, e.getMessage());
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

//	    long now = System.currentTimeMillis();
//	    if (now - taskStartMs >= CANCEL_CHECK_INTERVAL_MS
//		    && (lastCancelCheckMs == 0 || now - lastCancelCheckMs >= CANCEL_CHECK_INTERVAL_MS)) {
//		lastCancelCheckMs = now;
//		if (ConfigurationWrapper.isJobCanceled(context)) {
//		    GSLoggerFactory.getLogger(getClass()).info("Statistics task CANCELED (view / artifact key: {})", statsArtifactKey);
//
//		    status.setPhase(JobPhase.CANCELED);
//		    return;
//		}
//	    }

	}

	if (metrics.contains(StatisticsMetric.COUNTRY_INFO)) {
	    for (Country country : Country.values()) {
		final Country c = country;
		Gauge.builder(StatisticsMetric.COUNTRY_INFO.prometheusName(), () -> 1)//
			.description(StatisticsMetric.COUNTRY_INFO.description())//
			.tags("short_name", c.getShortName(), "official_name", c.getOfficialName(), "iso2", c.getISO2(), "iso3",
				c.getISO3())//
			.register(registry);
	    }
	}

	registerElasticsearchRuntimeMetrics(registry, metrics, viewId);

	GSLoggerFactory.getLogger(getClass()).info("Updated metrics for {}",
		(viewId != null && !viewId.isEmpty()) ? ("view " + viewId) : "all sources (no view)");

	Optional<S3TransferWrapper> optS3TransferManager = getS3TransferManager();

	if (!publishOnS3) {

	    GSLoggerFactory.getLogger(getClass()).info("S3 publish skipped, printing to logs");

	    String text = registry.scrape();

	    GSLoggerFactory.getLogger(getClass()).info(text);

	} else if (optS3TransferManager.isPresent()) {

	    GSLoggerFactory.getLogger(getClass()).info("Transfer download stats to s3 STARTED");

	    S3TransferWrapper manager = optS3TransferManager.get();
	    manager.setACLPublicRead(true);

	    File tempFile = File.createTempFile(getClass().getSimpleName() + statsArtifactKey, ".txt");

	    String text = registry.scrape();

	    Files.copy(IOStreamUtils.asStream(text), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

	    String bucket = "dabreporting";
	    Optional<String> optionalBucket = ConfigurationWrapper.getDownloadSetting().getS3StorageSetting().getBucketName();
	    if (optionalBucket.isPresent()) {
		bucket = optionalBucket.get();
	    }

	    manager.uploadFile(
		    tempFile.getAbsolutePath(), 
		    bucket,
		    "monitoring/" + statsArtifactKey + ".txt");

	    tempFile.delete();

	    GSLoggerFactory.getLogger(getClass()).info("Transfer of statistics to s3 ENDED");
	}

	log(status, "Statistics task ENDED");
    }

    /**
     * Registers OpenSearch-backed runtime metrics: {@link StatisticsMetric#STATION_PAGE_VISITS_TOTAL},
     * {@link StatisticsMetric#OM_DOWNLOADS_TOTAL}, {@link StatisticsMetric#SEARCH_ATTRIBUTE_TITLE_TOTAL},
     * {@link StatisticsMetric#SEARCH_OBSERVED_PROPERTY_URI_TOTAL},
     * {@link StatisticsMetric#OBSERVED_PROPERTY_INFO}
     * ({@link ElasticsearchClient#countRuntimeInfoRequestsByBucket}),
     * {@link StatisticsMetric#PORTAL_SEARCHES_TOTAL} ({@link ElasticsearchClient#countRuntimeInfoRequests}),
     * {@link StatisticsMetric#SEARCH_REQUESTS_GEOHASH_TOTAL} ({@link ElasticsearchClient#countRuntimeInfoRequestsByGeohash}),
     * {@link StatisticsMetric#SEARCH_REQUESTS_TIME_YEAR_TOTAL}
     * ({@link ElasticsearchClient#countRuntimeInfoRequestsByOverlappingCalendarYear}).
     */
    private void registerElasticsearchRuntimeMetrics(PrometheusMeterRegistry registry, Set<StatisticsMetric> metrics,String viewId) {

	if (!metrics.contains(StatisticsMetric.STATION_PAGE_VISITS_TOTAL) && !metrics.contains(StatisticsMetric.OM_DOWNLOADS_TOTAL)
		&& !metrics.contains(StatisticsMetric.SEARCH_ATTRIBUTE_TITLE_TOTAL)
		&& !metrics.contains(StatisticsMetric.SEARCH_OBSERVED_PROPERTY_URI_TOTAL)
		&& !metrics.contains(StatisticsMetric.OBSERVED_PROPERTY_INFO)
		&& !metrics.contains(StatisticsMetric.PORTAL_SEARCHES_TOTAL)
		&& !metrics.contains(StatisticsMetric.SEARCH_REQUESTS_GEOHASH_TOTAL)
		&& !metrics.contains(StatisticsMetric.SEARCH_REQUESTS_TIME_YEAR_TOTAL)) {
	    return;
	}
	Optional<DatabaseSetting> settingOpt = ConfigurationWrapper.getSystemSettings().getStatisticsSetting();
	if (settingOpt.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).warn(
		    "OpenSearch runtime metrics requested but statistics database setting is missing; skipping ES-backed statistics gauges");
	    return;
	}
	DatabaseSetting ds = settingOpt.get();
	ElasticsearchClient es = new ElasticsearchClient(ds.getDatabaseUri(), ds.getDatabaseUser(), ds.getDatabasePassword());
	es.setDbName(ds.getDatabaseName());
	try {
	    es.init();
	} catch (IOException e) {
	    GSLoggerFactory.getLogger(getClass()).warn("Elasticsearch init failed for runtime metrics: {}", e.getMessage());
	    return;
	}

	Map<String, Double> stationVisits = new HashMap<>();
	Map<String, Double> omDownloads = new HashMap<>();
	Map<String, Double> searchAttributeTitleTotal = new HashMap<>();
	Map<String, Double> searchObservedPropertyUriTotal = new HashMap<>();
	Map<String, Double> searchRequestsGeohashTotal = new HashMap<>();
	Map<String, Double> searchRequestsTimeYearTotal = new HashMap<>();

	if (metrics.contains(StatisticsMetric.STATION_PAGE_VISITS_TOTAL)) {
	    try {
		LogicalBond bond = BondFactory.createAndBond();
		if (viewId!=null) {
		    bond.getOperands()
			    .add(BondFactory.createRuntimeInfoElementBond(BondOperator.EQUAL, RuntimeInfoElement.VIEW_ID, viewId));
		}
		bond.getOperands().add(BondFactory.createRuntimeInfoElementBond(BondOperator.EQUAL, RuntimeInfoElement.PROFILER_NAME,
			RUNTIME_STATS_PROFILER_BNHS));
		bond.getOperands().add(BondFactory.createRuntimeInfoElementBond(BondOperator.EQUAL,
			RuntimeInfoElement.DISCOVERY_MESSAGE_IS_TIMESERIES, "true"));
		StatisticsResponse resp = es.countRuntimeInfoRequestsByBucket(bond, RuntimeInfoElement.RESULT_SET_DISCOVERY_SOURCE_ID,
			RUNTIME_STATS_MAX_BUCKETS);
		stationVisits.putAll(parseRuntimeInfoFrequencyByBucket(resp));
	    } catch (GSException e) {
		GSLoggerFactory.getLogger(getClass()).warn("STATION_PAGE_VISITS_TOTAL query failed: {}", e.getMessage());
	    }
	}

	if (metrics.contains(StatisticsMetric.OM_DOWNLOADS_TOTAL)) {
	    try {
		LogicalBond bond = BondFactory.createAndBond();
		if (viewId!=null) {
		    bond.getOperands()
			    .add(BondFactory.createRuntimeInfoElementBond(BondOperator.EQUAL, RuntimeInfoElement.VIEW_ID, viewId));
		}
		bond.getOperands().add(BondFactory.createRuntimeInfoElementBond(BondOperator.EQUAL, RuntimeInfoElement.PROFILER_NAME,
			RUNTIME_STATS_PROFILER_OM));
		StatisticsResponse resp = es.countRuntimeInfoRequestsByBucket(bond, RuntimeInfoElement.RESULT_SET_DISCOVERY_SOURCE_ID,
			RUNTIME_STATS_MAX_BUCKETS);
		omDownloads.putAll(parseRuntimeInfoFrequencyByBucket(resp));
	    } catch (GSException e) {
		GSLoggerFactory.getLogger(getClass()).warn("OM_DOWNLOADS_TOTAL query failed: {}", e.getMessage());
	    }
	}

	if (metrics.contains(StatisticsMetric.SEARCH_ATTRIBUTE_TITLE_TOTAL)) {
	    try {
		LogicalBond bond = BondFactory.createAndBond();
		if (viewId != null) {
		    bond.getOperands()
			    .add(BondFactory.createRuntimeInfoElementBond(BondOperator.EQUAL, RuntimeInfoElement.VIEW_ID, viewId));
		}
		bond.getOperands().add(BondFactory.createRuntimeInfoElementBond(BondOperator.EQUAL, RuntimeInfoElement.PROFILER_NAME,
			PORTAL_SEARCHES_PROFILER_OS));
		StatisticsResponse resp = es.countRuntimeInfoRequestsByBucket(bond, RuntimeInfoElement.DISCOVERY_MESSAGE_ATTRIBUTE_TITLE,
			RUNTIME_STATS_MAX_BUCKETS);
		searchAttributeTitleTotal.putAll(parseRuntimeInfoFrequencyByBucket(resp));
	    } catch (GSException e) {
		GSLoggerFactory.getLogger(getClass()).warn("SEARCH_ATTRIBUTE_TITLE_TOTAL query failed: {}", e.getMessage());
	    }
	}

	if (metrics.contains(StatisticsMetric.SEARCH_OBSERVED_PROPERTY_URI_TOTAL)) {
	    try {
		LogicalBond bond = BondFactory.createAndBond();
		if (viewId != null) {
		    bond.getOperands()
			    .add(BondFactory.createRuntimeInfoElementBond(BondOperator.EQUAL, RuntimeInfoElement.VIEW_ID, viewId));
		}
		bond.getOperands().add(BondFactory.createRuntimeInfoElementBond(BondOperator.EQUAL, RuntimeInfoElement.PROFILER_NAME,
			PORTAL_SEARCHES_PROFILER_OS));
		StatisticsResponse resp = es.countRuntimeInfoRequestsByBucket(bond, RuntimeInfoElement.DISCOVERY_MESSAGE_OBSERVED_PROPERTY_URI,
			RUNTIME_STATS_MAX_BUCKETS);
		searchObservedPropertyUriTotal.putAll(parseRuntimeInfoFrequencyByBucket(resp));
	    } catch (GSException e) {
		GSLoggerFactory.getLogger(getClass()).warn("SEARCH_OBSERVED_PROPERTY_URI query failed: {}", e.getMessage());
	    }
	}

	if (metrics.contains(StatisticsMetric.OBSERVED_PROPERTY_INFO)) {
	    try {
		LogicalBond bond = BondFactory.createAndBond();
		if (viewId != null) {
		    bond.getOperands()
			    .add(BondFactory.createRuntimeInfoElementBond(BondOperator.EQUAL, RuntimeInfoElement.VIEW_ID, viewId));
		}
		bond.getOperands().add(BondFactory.createRuntimeInfoElementBond(BondOperator.EQUAL, RuntimeInfoElement.PROFILER_NAME,
			PORTAL_SEARCHES_PROFILER_OS));
		StatisticsResponse resp = es.countRuntimeInfoRequestsByBucket(bond,
			RuntimeInfoElement.DISCOVERY_MESSAGE_OBSERVED_PROPERTY_URI, OBSERVED_PROPERTY_TOP_BUCKETS);
		Map<String, Double> topUris = parseRuntimeInfoFrequencyByBucket(resp);

		final String viewTag = viewId != null ? viewId : "";

		List<String> ontologyUrls = ConfigurationWrapper.getOntologySettings().stream()
			.filter(s -> s.getOntologyAvailability() == OntologySetting.Availability.ENABLED)
			.map(OntologySetting::getOntologyEndpoint).toList();

		for (String uri : topUris.keySet()) {
		    String labelIta = "";
		    String labelEng = "";
		    try {
			labelIta = resolvePreferredLabel(uri, ontologyUrls, "it");
			labelEng = resolvePreferredLabel(uri, ontologyUrls, "en");
		    } catch (Exception ex) {
			GSLoggerFactory.getLogger(getClass()).warn("OBSERVED_PROPERTY_INFO label lookup failed for {}: {}", uri,
				ex.getMessage());
		    }
		    final String fUri = uri;
		    final String fLabelIta = labelIta;
		    final String fLabelEng = labelEng;
		    Gauge.builder(StatisticsMetric.OBSERVED_PROPERTY_INFO.prometheusName(), () -> 1)//
			    .description(StatisticsMetric.OBSERVED_PROPERTY_INFO.description())//
			    .tags("uri", fUri, "preferred_label_ita", fLabelIta, "preferred_label_eng", fLabelEng)//
			    .tag("view", viewTag)//
			    .register(registry);
		}
	    } catch (GSException e) {
		GSLoggerFactory.getLogger(getClass()).warn("OBSERVED_PROPERTY_INFO query failed: {}", e.getMessage());
	    }
	}

	if (metrics.contains(StatisticsMetric.SEARCH_REQUESTS_GEOHASH_TOTAL)) {
	    try {
		LogicalBond bond = BondFactory.createAndBond();
		if (viewId != null) {
		    bond.getOperands()
			    .add(BondFactory.createRuntimeInfoElementBond(BondOperator.EQUAL, RuntimeInfoElement.VIEW_ID, viewId));
		}
		bond.getOperands().add(BondFactory.createRuntimeInfoElementBond(BondOperator.EQUAL, RuntimeInfoElement.PROFILER_NAME,
			PORTAL_SEARCHES_PROFILER_OS));
		StatisticsResponse resp = es.countRuntimeInfoRequestsByGeohash(bond, RUNTIME_STATS_GEOHASH_GRID_PRECISION,
			RUNTIME_STATS_GEOHASH_TOP_BUCKETS);
		searchRequestsGeohashTotal.putAll(parseRuntimeInfoFrequencyByBucket(resp));
	    } catch (GSException e) {
		GSLoggerFactory.getLogger(getClass()).warn("SEARCH_REQUESTS_GEOHASH_TOTAL query failed: {}", e.getMessage());
	    }
	}

	if (metrics.contains(StatisticsMetric.SEARCH_REQUESTS_TIME_YEAR_TOTAL)) {
	    try {
		LogicalBond bond = BondFactory.createAndBond();
		if (viewId != null) {
		    bond.getOperands()
			    .add(BondFactory.createRuntimeInfoElementBond(BondOperator.EQUAL, RuntimeInfoElement.VIEW_ID, viewId));
		}
		bond.getOperands().add(BondFactory.createRuntimeInfoElementBond(BondOperator.EQUAL, RuntimeInfoElement.PROFILER_NAME,
			PORTAL_SEARCHES_PROFILER_OS));
		int maxYear = Year.now(ZoneOffset.UTC).getValue();
		StatisticsResponse resp =
			es.countRuntimeInfoRequestsByOverlappingCalendarYear(bond, RUNTIME_STATS_YEAR_RANGE_MIN, maxYear);
		searchRequestsTimeYearTotal.putAll(parseRuntimeInfoFrequencyByBucket(resp));
	    } catch (GSException e) {
		GSLoggerFactory.getLogger(getClass()).warn("SEARCH_REQUESTS_TIME_YEAR_TOTAL query failed: {}", e.getMessage());
	    }
	}

	AtomicLong portalSearchesTotal = new AtomicLong(0L);
	if (metrics.contains(StatisticsMetric.PORTAL_SEARCHES_TOTAL)) {
	    try {
		LogicalBond bond = BondFactory.createAndBond();
		if (viewId!=null) {
		    bond.getOperands()
			    .add(BondFactory.createRuntimeInfoElementBond(BondOperator.EQUAL, RuntimeInfoElement.VIEW_ID, viewId));
		}
		bond.getOperands().add(BondFactory.createRuntimeInfoElementBond(BondOperator.EQUAL, RuntimeInfoElement.PROFILER_NAME,
			PORTAL_SEARCHES_PROFILER_OS));
		portalSearchesTotal.set(es.countRuntimeInfoRequests(bond));
	    } catch (GSException e) {
		GSLoggerFactory.getLogger(getClass()).warn("PORTAL_SEARCHES_TOTAL query failed: {}", e.getMessage());
	    }
	    Gauge.builder(StatisticsMetric.PORTAL_SEARCHES_TOTAL.prometheusName(), portalSearchesTotal, a -> (double) a.get())//
		    .description(StatisticsMetric.PORTAL_SEARCHES_TOTAL.description())//
		    .tag("view", viewId)//
		    .register(registry);
	}

	for (String sourceId : stationVisits.keySet()) {
	    final String sid = sourceId;
	    Gauge.builder(StatisticsMetric.STATION_PAGE_VISITS_TOTAL.prometheusName(), stationVisits, g -> g.getOrDefault(sid, 0.0))//
		    .description(StatisticsMetric.STATION_PAGE_VISITS_TOTAL.description())//
		    .tag("source_id", sid)//
		    .tag("view", viewId)//
		    .register(registry);
	}
	for (String sourceId : omDownloads.keySet()) {
	    final String sid = sourceId;
	    Gauge.builder(StatisticsMetric.OM_DOWNLOADS_TOTAL.prometheusName(), omDownloads, g -> g.getOrDefault(sid, 0.0))//
		    .description(StatisticsMetric.OM_DOWNLOADS_TOTAL.description())//
		    .tag("source_id", sid)//
		    .tag("view", viewId)//
		    .register(registry);
	}
	for (String title : searchAttributeTitleTotal.keySet()) {
	    final String t = title;
	    Gauge.builder(StatisticsMetric.SEARCH_ATTRIBUTE_TITLE_TOTAL.prometheusName(), searchAttributeTitleTotal,
		    g -> g.getOrDefault(t, 0.0))//
		    .description(StatisticsMetric.SEARCH_ATTRIBUTE_TITLE_TOTAL.description())//
		    .tag("attribute_title", t)//
		    .tag("view", viewId)//
		    .register(registry);
	}
	for (String uri : searchObservedPropertyUriTotal.keySet()) {
	    final String u = uri;
	    Gauge.builder(StatisticsMetric.SEARCH_OBSERVED_PROPERTY_URI_TOTAL.prometheusName(), searchObservedPropertyUriTotal,
		    g -> g.getOrDefault(u, 0.0))//
		    .description(StatisticsMetric.SEARCH_OBSERVED_PROPERTY_URI_TOTAL.description())//
		    .tag("observed_property_uri", u)//
		    .tag("view", viewId)//
		    .register(registry);
	}
	for (String geohash : searchRequestsGeohashTotal.keySet()) {
	    final String gh = geohash;
	    Gauge.builder(StatisticsMetric.SEARCH_REQUESTS_GEOHASH_TOTAL.prometheusName(), searchRequestsGeohashTotal,
		    g -> g.getOrDefault(gh, 0.0))//
		    .description(StatisticsMetric.SEARCH_REQUESTS_GEOHASH_TOTAL.description())//
		    .tag("geohash", gh)//
		    .tag("view", viewId)//
		    .register(registry);
	}
	for (String yr : searchRequestsTimeYearTotal.keySet()) {
	    final String y = yr;
	    Gauge.builder(StatisticsMetric.SEARCH_REQUESTS_TIME_YEAR_TOTAL.prometheusName(), searchRequestsTimeYearTotal,
		    g -> g.getOrDefault(y, 0.0))//
		    .description(StatisticsMetric.SEARCH_REQUESTS_TIME_YEAR_TOTAL.description())//
		    .tag("year", y)//
		    .tag("view", viewId)//
		    .register(registry);
	}
    }

    /**
     * Parses {@link StatisticsResponse} from {@link ElasticsearchClient#countRuntimeInfoRequestsByBucket} (frequency
     * encoding).
     */
    static Map<String, Double> parseRuntimeInfoFrequencyByBucket(StatisticsResponse response) {

	Map<String, Double> out = new LinkedHashMap<>();
	if (response == null || response.getItems().isEmpty()) {
	    return out;
	}
	List<ComputationResult> freqs = response.getItems().get(0).getFrequency();
	if (freqs == null || freqs.isEmpty()) {
	    return out;
	}
	String packed = freqs.get(0).getValue();
	if (packed == null || packed.isBlank()) {
	    return out;
	}
	for (String token : packed.split(" ")) {
	    if (token.isEmpty()) {
		continue;
	    }
	    String[] parts = token.split(ComputationResult.FREQUENCY_ITEM_SEP, 2);
	    if (parts.length < 2) {
		continue;
	    }
	    try {
		String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
		double docCount = Double.parseDouble(parts[1]);
		out.put(key, docCount);
	    } catch (RuntimeException e) {
		GSLoggerFactory.getLogger(StatisticsTask.class).warn("Skipping malformed runtime stats bucket token: {}", token);
	    }
	}
	return out;
    }

    private static String metadataCompletenessKey(String sourceId, String elementTag) {

	return sourceId + '\0' + "e:" + elementTag;
    }

    private static String metadataCompletenessSetKey(String sourceId, String elementSetTag) {

	return sourceId + '\0' + "s:" + elementSetTag;
    }

    /**
     * Same condition as {@link ResourcesComparatorTask} uses to run folder comparison (when the consolidated snapshot does
     * not “survive” the smart-storage threshold): {@code optional} empty, or present {@code false}.
     */
    private static boolean consolidatedFolderDoesNotSurviveComparatorGate(Optional<Boolean> consolidatedFolderSurvives) {

	return consolidatedFolderSurvives.isEmpty() || !consolidatedFolderSurvives.get();
    }

    private static String recordsPerElementKey(String sourceId, String elementTag, String value) {

	return sourceId + '\0' + "re:" + elementTag + '\0' + value;
    }

    /**
     * Walks composite aggregation pages for the given element; returns empty if the backend executor does not support
     * {@link DatabaseExecutor#getIndexValues}.
     */
    private static List<TermFrequencyItem> collectAllTermFrequencies(DatabaseExecutor executor, DiscoveryMessage message,
	    MetadataElement element) throws GSException {

	message.setPage(new Page(1000));
	List<TermFrequencyItem> out = new ArrayList<>();
	String resumption = null;
	for (int pageIdx = 0; pageIdx < 100_000; pageIdx++) {
	    ResultSet<TermFrequencyItem> page = executor.getIndexValues(message, element, 0, resumption);
	    if (page == null) {
		return out;
	    }
	    if (page.getResultsList() != null) {
		out.addAll(page.getResultsList());
	    }
	    Optional<SearchAfter> sa = page.getSearchAfter();
	    if (sa.isEmpty()) {
		break;
	    }
	    Optional<List<Object>> vals = sa.get().getValues();
	    if (vals.isEmpty() || vals.get().isEmpty()) {
		break;
	    }
	    resumption = vals.get().get(0).toString();
	}
	return out;
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

    /**
     * Discovery query for one source, restricted to resources of type {@link ResourceType#DATASET} via
     * {@link BondFactory#createResourceTypeBond(ResourceType)}. When {@code view} is empty, the view bond is
     * {@link BondFactory#getTrueBond()} (same as an unconstrained view), so counts include all dataset resources for that
     * source.
     */
    private static DiscoveryMessage newDiscoveryMessageForSource(Optional<View> view, String sourceId) {

	Bond viewBond = view.map(View::getBond).orElse(BondFactory.getTrueBond());
	Bond base = BondFactory.createAndBond(viewBond, BondFactory.createSourceIdentifierBond(sourceId),
		BondFactory.createResourceTypeBond(ResourceType.DATASET));
	DiscoveryMessage message = new DiscoveryMessage();
	List<GSSource> sources = new ArrayList<>();
	sources.add(ConfigurationWrapper.getSource(sourceId));
	message.setSources(sources);
	view.ifPresent(message::setView);
	message.setUserBond(base);
	message.setPermittedBond(base);
	return message;
    }

    /**
     * Same logic as {@code blue-cloud-report.jsp}: percentage of {@link ResourceType#DATASET} resources where the element
     * exists ({@link BondFactory#createExistsSimpleValueBond} on the element queryable), relative to {@code totalRecords}
     * for the same view, source, and resource type.
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
     * Share of {@link ResourceType#DATASET} resources where <b>all</b> {@link MetadataElement}s exist (successive
     * {@link BondFactory#createAndBond} with {@link BondFactory#createExistsSimpleValueBond}).
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

    /** Explicit {@code false}, {@code 0}, or {@code no} disables S3 upload; any other non-blank value keeps publishing. */
    static boolean parsePublishOnS3(String raw) {

	String t = raw.trim();
	return !"false".equalsIgnoreCase(t) && !"0".equals(t) && !"no".equalsIgnoreCase(t);
    }

    /**
     * Resolves the preferred label for a concept URI using {@link SKOSClient} and the given ontology endpoints.
     * Results are cached in {@link #observedPropertyLabelCache} to avoid repeated SPARQL queries across task runs.
     *
     * @return the preferred label in the requested language, or empty string if not found
     */
    private static String resolvePreferredLabel(String conceptUri, List<String> ontologyUrls, String language) {

	if (ontologyUrls == null || ontologyUrls.isEmpty()) {
	    return "";
	}
	String cacheKey = conceptUri + '\0' + language;
	String cached = observedPropertyLabelCache.get(cacheKey);
	if (cached != null) {
	    return cached;
	}
	try {
	    SKOSClient client = new SKOSClient();
	    client.setOntologyUrls(ontologyUrls);
	    client.setSearchValue(SearchTarget.CONCEPTS, conceptUri);
	    client.setSearchLangs(List.of(language));
	    client.setSourceLangs(List.of(language));
	    client.setExpansionLevel(ExpansionLevel.NONE);
	    client.setExpansionLimit(ExpansionLimit.of(LimitTarget.CONCEPTS, 1));

	    SKOSResponse response = client.search();
	    for (SKOSConcept c : response.getResults()) {
		if (conceptUri.equals(c.getConceptURI()) && c.getPref().isPresent()) {
		    observedPropertyLabelCache.put(cacheKey, c.getPref().get());
		    return c.getPref().get();
		}
	    }
	    List<String> prefLabels = response.getPrefLabels();
	    if (!prefLabels.isEmpty()) {
		observedPropertyLabelCache.put(cacheKey, prefLabels.get(0));
		return prefLabels.get(0);
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(StatisticsTask.class).warn("SKOSClient label resolution failed for {} [{}]: {}",
		    conceptUri, language, e.getMessage());
	}
	return "";
    }

    @Override
    public String getName() {

	return "Statistics task";
    }
}
