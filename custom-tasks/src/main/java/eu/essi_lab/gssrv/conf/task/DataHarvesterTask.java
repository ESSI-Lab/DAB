package eu.essi_lab.gssrv.conf.task;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.quartz.JobExecutionContext;

import eu.essi_lab.access.augmenter.DataCacheAugmenter;
import eu.essi_lab.access.augmenter.ExpectedTimeHandover;
import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory;
import eu.essi_lab.access.datacache.DataRecord;
import eu.essi_lab.access.datacache.SeriesStatusRecord;
import eu.essi_lab.access.datacache.StatisticsRecord;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.OptionsKey;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.gssrv.conf.task.collection.WIS2DataNotifier;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.TaskListExecutor;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsm.access.AccessQueryUtils;

/**
 * Keeps the data cache of a source up to date. The time series whose next record is expected to be available (according
 * to the next record expected time set by the {@link DataCacheAugmenter}) are periodically refreshed by the augmenter.
 * Series whose expected time is not updated by a refresh (e.g. resource not found, no new data, errors) are retried with
 * an exponential backoff.<br>
 * Unless disabled, the cache is also aligned with the metadata of the source (see {@link DataCacheReconciler}) at the task
 * start, after each metadata harvesting of the source and at least every {@link DataHarvesterTaskOptions#RECONCILE_INTERVAL_HOURS}:
 * missing series are filled, cached series starting after the data to load (see
 * {@link DataHarvesterTaskOptions#FILL_DAYS}) are completed with the older data, current but not scheduled series are
 * refreshed (with lower priority than the scheduled ones) and series no more in the metadata are removed.<br>
 * If {@link DataHarvesterTaskOptions#OBSERVED_PROPERTY_URIS} is set, only the series having one of the given observed
 * property URIs are filled and updated. The cached series of other observed properties are removed.
 *
 * @author boldrini
 */
public class DataHarvesterTask extends AbstractCustomTask {

    public enum DataHarvesterTaskOptions implements OptionsKey {
	SOURCE_ID, THREADS_COUNT, MAX_RECORDS,
	/**
	 * Comma separated list of observed property URIs. If present, only the series having one of these observed property
	 * URIs are harvested
	 */
	OBSERVED_PROPERTY_URIS,
	/**
	 * If "false", the data cache is not aligned with the metadata of the source. Default: "true"
	 */
	RECONCILE,
	/**
	 * Max hours between two alignments of the data cache with the metadata. Default: 24
	 */
	RECONCILE_INTERVAL_HOURS,
	/**
	 * If "true", WIS2 data notifications are published for the new values of the scheduled series, after they have
	 * been written to the cache. Values are notified only if the source is a core source, according to the data
	 * notifications mode of the WIS2 settings (see WIS2DataNotifier). Default: "false"
	 */
	MQTT,
	/**
	 * Days of data loaded in the cache, back from the end of the temporal extent of each series. Default: the whole
	 * temporal extent. Unless the alignment with the metadata is disabled, the cached series starting after this period
	 * are completed with the older data
	 */
	FILL_DAYS;
    }

    private static final int DEFAULT_RECONCILE_INTERVAL_HOURS = 24;

    /**
     * Min time between two checks of the end of a metadata harvesting of the source
     */
    private static final long HARVESTING_CHECK_INTERVAL_MS = 5 * 60 * 1000;

    /**
     * Number of not scheduled series (to fill or to refresh) added to a batch, per thread
     */
    private static final int UNSCHEDULED_PER_THREAD = 5;

    /**
     * Sleep step used while waiting, between two cancellation checks
     */
    private static final long SLEEP_STEP_MS = 5000;

    /**
     * Wait time when the source has no scheduled records
     */
    private static final long IDLE_WAIT_MS = 60 * 1000;

    /**
     * Wait time after an unexpected error of the main loop
     */
    private static final long ERROR_WAIT_MS = 60 * 1000;

    /**
     * Retry delay after the first not updated refresh of a series, doubled at each following one
     */
    private static final long BACKOFF_BASE_MS = 60 * 1000;

    private static final long BACKOFF_MAX_MS = 60 * 60 * 1000;

    /**
     * Max time to wait after a batch for the data cache buffer to be written
     */
    private static final long MAX_SETTLE_WAIT_MS = 60 * 1000;

    /**
     * Additional wait after a batch, to let the written records become searchable
     */
    private static final long REFRESH_WAIT_MS = 2000;

    private enum Outcome {
	AUGMENTED, NOT_FOUND, EXCLUDED, FAILED, SKIPPED_CANCELED
    }

    /**
     * A series to augment
     *
     * @param onlineId
     * @param expectedDate the next record expected time, null for not scheduled series
     * @param backfillBegin begin of the older data to load, for not scheduled incomplete series
     * @param backfillEnd end of the older data to load (the first cached observation), for not scheduled incomplete series
     */
    private record Job(String onlineId, Date expectedDate, Date backfillBegin, Date backfillEnd) {

	Job(String onlineId, Date expectedDate) {
	    this(onlineId, expectedDate, null, null);
	}

	boolean isScheduled() {
	    return expectedDate != null;
	}
    }

    /**
     * State of the alignment with the metadata
     */
    private static class ReconcileState {

	long lastReconcile;
	long lastHarvestingCheck;
	String lastHarvestingEnd;
    }

    /**
     * A series already processed, whose expected time was still the same at the time of the last check
     */
    private static class Attempt {

	Date expectedDate;
	int failures;
	long retryNotBefore;
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Data harvester task STARTED");

	// SETTINGS RETRIEVAL

	Optional<EnumMap<DataHarvesterTaskOptions, String>> taskOptions = readTaskOptions(context, DataHarvesterTaskOptions.class);
	if (taskOptions.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).error("No options specified");
	    return;
	}
	String sourceId = taskOptions.get().get(DataHarvesterTaskOptions.SOURCE_ID);
	if (sourceId == null) {
	    GSLoggerFactory.getLogger(getClass()).error("No source id option specified");
	    return;
	}

	int threadsCount = 1;
	String threadsCountString = taskOptions.get().get(DataHarvesterTaskOptions.THREADS_COUNT);
	if (threadsCountString != null) {
	    threadsCount = Integer.parseInt(threadsCountString.trim());
	    if (threadsCount < 1) {
		GSLoggerFactory.getLogger(getClass()).warn("Invalid threads count {}, using 1", threadsCount);
		threadsCount = 1;
	    }
	}

	Integer maxRecords = null;
	String maxRecordsString = taskOptions.get().get(DataHarvesterTaskOptions.MAX_RECORDS);
	if (maxRecordsString != null) {
	    maxRecords = Integer.parseInt(maxRecordsString.trim());
	}

	Set<String> observedPropertyURIs = new HashSet<>();
	String observedPropertyURIsString = taskOptions.get().get(DataHarvesterTaskOptions.OBSERVED_PROPERTY_URIS);
	if (observedPropertyURIsString != null) {
	    for (String uri : observedPropertyURIsString.split(",")) {
		if (!uri.isBlank()) {
		    observedPropertyURIs.add(uri.trim());
		}
	    }
	}

	String reconcileString = taskOptions.get().get(DataHarvesterTaskOptions.RECONCILE);
	boolean reconcile = reconcileString == null || !reconcileString.trim().equalsIgnoreCase("false");

	String mqttString = taskOptions.get().get(DataHarvesterTaskOptions.MQTT);
	boolean mqtt = mqttString != null && mqttString.trim().equalsIgnoreCase("true");

	int fillDays = DataCacheAugmenter.FILL_ALL;
	String fillDaysString = taskOptions.get().get(DataHarvesterTaskOptions.FILL_DAYS);
	if (fillDaysString != null) {
	    fillDays = Integer.parseInt(fillDaysString.trim());
	    if (fillDays < 1) {
		GSLoggerFactory.getLogger(getClass()).warn("Invalid fill days {}, filling the whole temporal extent", fillDays);
		fillDays = DataCacheAugmenter.FILL_ALL;
	    }
	}

	long reconcileInterval = TimeUnit.HOURS.toMillis(DEFAULT_RECONCILE_INTERVAL_HOURS);
	String reconcileIntervalString = taskOptions.get().get(DataHarvesterTaskOptions.RECONCILE_INTERVAL_HOURS);
	if (reconcileIntervalString != null) {
	    reconcileInterval = TimeUnit.HOURS.toMillis(Integer.parseInt(reconcileIntervalString.trim()));
	}

	// INIT CACHE CONNECTOR

	DataCacheConnector dataCacheConnector = DataCacheConnectorFactory.getDataCacheConnector();

	if (dataCacheConnector == null) {
	    DataCacheConnectorSetting setting = ConfigurationWrapper.getDataCacheConnectorSetting();
	    dataCacheConnector = DataCacheConnectorFactory.newDataCacheConnector(setting);
	    if (dataCacheConnector == null) {
		GSLoggerFactory.getLogger(getClass()).error("Issues initializing the data cache connector");
		return;
	    }
	    String cachedDays = setting.getOptionValue(DataCacheConnector.CACHED_DAYS).get();
	    String flushInterval = setting.getOptionValue(DataCacheConnector.FLUSH_INTERVAL_MS).get();
	    String maxBulkSize = setting.getOptionValue(DataCacheConnector.MAX_BULK_SIZE).get();
	    dataCacheConnector.configure(DataCacheConnector.MAX_BULK_SIZE, maxBulkSize);
	    dataCacheConnector.configure(DataCacheConnector.FLUSH_INTERVAL_MS, flushInterval);
	    dataCacheConnector.configure(DataCacheConnector.CACHED_DAYS, cachedDays);
	    DataCacheConnectorFactory.setDataCacheConnector(dataCacheConnector);
	}

	GSLoggerFactory.getLogger(getClass()).info(
		"[DATA-CACHE] Source {} harvesting with {} threads, max records {}, reconcile {}, MQTT {}, fill days {}, observed property URIs {}",
		sourceId, threadsCount, maxRecords, reconcile, mqtt, fillDays == DataCacheAugmenter.FILL_ALL ? "all" : fillDays,
		observedPropertyURIs.isEmpty() ? "all" : observedPropertyURIs);

	// online id -> last attempt of a series whose expected time was not (yet) updated
	Map<String, Attempt> attempts = new HashMap<>();

	// scheduled series not selected by the observed property URIs, whose removal failed
	Set<String> excluded = ConcurrentHashMap.newKeySet();

	DataCacheReconciler reconciler = new DataCacheReconciler(sourceId, dataCacheConnector, observedPropertyURIs, fillDays);

	WIS2DataNotifier notifier = mqtt ? new WIS2DataNotifier(sourceId) : null;
	ReconcileState reconcileState = new ReconcileState();

	int recordsDone = 0;
	boolean canceled = false;

	while (true) {

	    // CHECKING CANCELED JOB

	    if (ConfigurationWrapper.isJobCanceled(context)) {
		canceled = true;
		break;
	    }

	    if (maxRecords != null && recordsDone >= maxRecords) {
		GSLoggerFactory.getLogger(getClass()).info("[DATA-CACHE] Source {} max records {} reached", sourceId, maxRecords);
		break;
	    }

	    try {

		// ALIGNMENT WITH THE METADATA

		if (reconcile && isReconcileNeeded(reconciler, reconcileState, reconcileInterval)) {

		    reconcile(context, reconciler, reconcileState, excluded);
		}

		List<SimpleEntry<String, Date>> expectedRecords = dataCacheConnector.getExpectedAvailableRecords(sourceId);

		List<Job> toProcess = selectRecordsToProcess(expectedRecords, attempts, excluded);

		// the not scheduled series to augment (missing in the cache, or current but not scheduled), as found by the
		// last reconciliation, fill the batch after the scheduled ones
		int scheduledCount = toProcess.size();
		if (reconcile && reconciler.getPassStart().isPresent()) {
		    Set<String> scheduledIds = new HashSet<>();
		    toProcess.forEach(job -> scheduledIds.add(job.onlineId()));
		    for (SeriesStatusRecord record : dataCacheConnector.getSeriesToAugment(sourceId, reconciler.getPassStart().get(),
			    threadsCount * UNSCHEDULED_PER_THREAD)) {
			if (!scheduledIds.contains(record.getDataIdentifier()) && !attempts.containsKey(record.getDataIdentifier())) {
			    if (record.isIncomplete()) {
				toProcess.add(new Job(record.getDataIdentifier(), null, record.getFillBegin(), record.getFirstObservation()));
			    } else {
				toProcess.add(new Job(record.getDataIdentifier(), null));
			    }
			}
		    }
		}

		GSLoggerFactory.getLogger(getClass()).info(
			"[DATA-CACHE] Source {} expected available records {}, in backoff {}, not scheduled to augment {}", sourceId,
			expectedRecords.size(), expectedRecords.size() - scheduledCount, toProcess.size() - scheduledCount);

		if (toProcess.isEmpty()) {

		    if (waitForNextRecord(context, dataCacheConnector, sourceId, attempts)) {
			canceled = true;
			break;
		    }
		    continue;
		}

		if (maxRecords != null && toProcess.size() > maxRecords - recordsDone) {
		    toProcess = toProcess.subList(0, maxRecords - recordsDone);
		}

		recordsDone += toProcess.size();

		// the new values of the scheduled series, to be notified
		Queue<DataRecord> newRecords = notifier != null ? new ConcurrentLinkedQueue<>() : null;

		// the expected times to clear once the new values are written
		Queue<ExpectedTimeHandover> handovers = new ConcurrentLinkedQueue<>();

		List<Future<Outcome>> futures = harvestBatch(context, dataCacheConnector, reconciler, excluded, threadsCount, toProcess,
			scheduledCount, newRecords, handovers, fillDays);

		if (futures == null) {
		    GSLoggerFactory.getLogger(getClass()).warn("[DATA-CACHE] Source {} batch interrupted", sourceId);
		    canceled = true;
		    break;
		}

		// each processed scheduled series is registered as an attempt. if the augmenter updated its expected time,
		// the attempt is discarded when the series is listed again (or not listed at all)
		long now = System.currentTimeMillis();
		for (Job job : toProcess) {
		    if (job.isScheduled()) {
			registerAttempt(attempts, job, now);
		    }
		}

		if (waitForWrites(context, dataCacheConnector)) {
		    canceled = true;
		    break;
		}

		// the previous expected times are cleared only if the new values have been written, otherwise they are kept
		// so that the series are refreshed again
		if (completeHandovers(handovers, dataCacheConnector, sourceId) && waitForWrites(context, dataCacheConnector)) {
		    canceled = true;
		    break;
		}

		// the new values are notified once written and searchable, so that they are available at the notified link
		if (notifier != null) {
		    notifier.notify(newRecords, dataCacheConnector);
		}

		// the stored statuses of the processed series are updated
		if (reconcile) {
		    Set<String> unscheduledIds = new HashSet<>();
		    List<String> ids = new ArrayList<>();
		    for (Job job : toProcess) {
			ids.add(job.onlineId());
			if (!job.isScheduled()) {
			    unscheduledIds.add(job.onlineId());
			}
		    }
		    reconciler.updateStatuses(ids, unscheduledIds);
		}

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error("[DATA-CACHE] Source {} error: {}", sourceId, e.getMessage(), e);

		if (sleep(context, ERROR_WAIT_MS)) {
		    canceled = true;
		    break;
		}
	    }
	}

	if (canceled) {

	    status.setPhase(JobPhase.CANCELED);
	    log(status, "Data harvester task CANCELED. Source id " + sourceId + ". Records processed: " + recordsDone);

	} else {

	    log(status, "Data harvester task ENDED. Source id " + sourceId + ". Records processed: " + recordsDone);
	}
    }

    /**
     * @return true if the alignment with the metadata is due: at the start, after the reconcile interval, or after the
     *         end of a metadata harvesting of the source
     */
    private boolean isReconcileNeeded(DataCacheReconciler reconciler, ReconcileState state, long reconcileInterval) {

	long now = System.currentTimeMillis();

	if (state.lastReconcile == 0 || now - state.lastReconcile >= reconcileInterval) {
	    return true;
	}

	if (now - state.lastHarvestingCheck < HARVESTING_CHECK_INTERVAL_MS) {
	    return false;
	}

	state.lastHarvestingCheck = now;

	Optional<HarvestingProperties> properties = reconciler.getHarvestingProperties();
	if (properties.isEmpty() || !properties.get().isCompleted().orElse(true)) {
	    // unknown or harvesting in progress
	    return false;
	}

	String end = properties.get().getEndHarvestingTimestamp();
	return end != null && !end.equals(state.lastHarvestingEnd);
    }

    private void reconcile(JobExecutionContext context, DataCacheReconciler reconciler, ReconcileState state, Set<String> excluded)
	    throws Exception {

	// the state is updated before the alignment, so that a failing alignment is not repeated at each loop
	long now = System.currentTimeMillis();
	state.lastReconcile = now;
	state.lastHarvestingCheck = now;

	Optional<HarvestingProperties> properties = reconciler.getHarvestingProperties();
	boolean harvesting = properties.isPresent() && !properties.get().isCompleted().orElse(true);
	if (properties.isPresent() && !harvesting) {
	    state.lastHarvestingEnd = properties.get().getEndHarvestingTimestamp();
	}

	GSLoggerFactory.getLogger(getClass()).info("[DATA-CACHE] Source {} aligning the data cache with the metadata",
		reconciler.getSourceId());

	// series are not removed while the source is being harvested, since its metadata could be incomplete
	DataCacheReconciler.Result result = reconciler.reconcile(!harvesting, () -> ConfigurationWrapper.isJobCanceled(context));

	excluded.clear();

	GSLoggerFactory.getLogger(getClass()).info("[DATA-CACHE] Source {} data cache aligned with the metadata. {}",
		reconciler.getSourceId(), result);
    }

    /**
     * Discards the attempts of the series whose expected time has been updated, and returns the expected records not in
     * backoff and not excluded
     */
    private List<Job> selectRecordsToProcess(List<SimpleEntry<String, Date>> expectedRecords, Map<String, Attempt> attempts,
	    Set<String> excluded) {

	long now = System.currentTimeMillis();

	List<Job> ret = new ArrayList<>();
	Set<String> listed = new HashSet<>();

	for (SimpleEntry<String, Date> record : expectedRecords) {

	    String onlineId = record.getKey();
	    if (onlineId == null || !listed.add(onlineId) || excluded.contains(onlineId)) {
		continue;
	    }

	    Attempt attempt = attempts.get(onlineId);
	    if (attempt != null && !Objects.equals(attempt.expectedDate, record.getValue())) {
		// the expected time has been updated
		attempts.remove(onlineId);
		attempt = null;
	    }

	    if (attempt == null || now >= attempt.retryNotBefore) {
		ret.add(new Job(onlineId, record.getValue()));
	    }
	}

	// series no more expected have been updated
	attempts.keySet().retainAll(listed);

	return ret;
    }

    private void registerAttempt(Map<String, Attempt> attempts, Job job, long now) {

	Attempt attempt = attempts.get(job.onlineId());
	if (attempt == null || !Objects.equals(attempt.expectedDate, job.expectedDate())) {
	    attempt = new Attempt();
	    attempt.expectedDate = job.expectedDate();
	    attempts.put(job.onlineId(), attempt);
	}

	attempt.failures++;

	long delay = BACKOFF_BASE_MS << Math.min(attempt.failures - 1, 16);
	attempt.retryNotBefore = now + Math.min(delay, BACKOFF_MAX_MS);
    }

    /**
     * Waits until the next expected record of the source or the end of the earliest backoff, whichever comes first, at
     * most {@link #HARVESTING_CHECK_INTERVAL_MS}
     *
     * @return true if the job has been canceled
     */
    private boolean waitForNextRecord(JobExecutionContext context, DataCacheConnector dataCacheConnector, String sourceId,
	    Map<String, Attempt> attempts) throws Exception {

	long now = System.currentTimeMillis();
	long wakeUp = Long.MAX_VALUE;

	SimpleEntry<String, Date> nextRecord = dataCacheConnector.getNextExpectedRecord(sourceId);
	if (nextRecord != null && nextRecord.getValue() != null) {
	    wakeUp = nextRecord.getValue().getTime();
	}

	for (Attempt attempt : attempts.values()) {
	    wakeUp = Math.min(wakeUp, attempt.retryNotBefore);
	}

	if (wakeUp == Long.MAX_VALUE) {

	    GSLoggerFactory.getLogger(getClass()).info("[DATA-CACHE] Source {} no scheduled records, waiting", sourceId);
	    return sleep(context, IDLE_WAIT_MS);
	}

	// the wait is limited, so that the end of a metadata harvesting is promptly detected
	wakeUp = Math.min(wakeUp, now + HARVESTING_CHECK_INTERVAL_MS);

	GSLoggerFactory.getLogger(getClass()).info("[DATA-CACHE] Source {} sleeping until {}", sourceId, new Date(wakeUp));
	return sleep(context, wakeUp - now);
    }

    /**
     * Augments the given records in parallel
     *
     * @return the futures of the tasks, or null if the execution was interrupted
     */
    private List<Future<Outcome>> harvestBatch(JobExecutionContext context, DataCacheConnector dataCacheConnector,
	    DataCacheReconciler reconciler, Set<String> excluded, int threadsCount, List<Job> jobs, int scheduledCount,
	    Queue<DataRecord> newRecords, Queue<ExpectedTimeHandover> handovers, int fillDays) {

	String sourceId = reconciler.getSourceId();

	TaskListExecutor<Outcome> taskList = new TaskListExecutor<>(threadsCount);

	for (int i = 0; i < jobs.size(); i++) {

	    Job job = jobs.get(i);
	    int index = i + 1;

	    taskList.addTask(
		    () -> augment(context, dataCacheConnector, reconciler, excluded, job, index, jobs.size(), newRecords, handovers, fillDays));
	}

	List<Future<Outcome>> futures = taskList.executeAndWait();
	if (futures == null) {
	    return null;
	}

	EnumMap<Outcome, Integer> outcomes = new EnumMap<>(Outcome.class);
	int cancelled = 0;
	int errors = 0;

	for (Future<Outcome> future : futures) {

	    try {
		outcomes.merge(future.get(), 1, Integer::sum);
	    } catch (CancellationException ex) {
		cancelled++;
	    } catch (InterruptedException ie) {
		Thread.currentThread().interrupt();
		return null;
	    } catch (ExecutionException ee) {
		errors++;
		GSLoggerFactory.getLogger(getClass()).error("[DATA-CACHE] {}", ee.getCause().getMessage(), ee.getCause());
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info(
		"[DATA-CACHE] Source {} batch ended. Total {} (scheduled {}) outcomes {} cancelled {} errors {}", sourceId, futures.size(),
		Math.min(scheduledCount, futures.size()), outcomes, cancelled, errors);

	return futures;
    }

    /**
     * @param newRecords if not null, the new values of a scheduled series are added to it
     * @param handovers the expected time to clear once the new values are written, if any, is added to it
     */
    private Outcome augment(JobExecutionContext context, DataCacheConnector dataCacheConnector, DataCacheReconciler reconciler,
	    Set<String> excluded, Job job, int index, int total, Queue<DataRecord> newRecords, Queue<ExpectedTimeHandover> handovers,
	    int fillDays) {

	String sourceId = reconciler.getSourceId();

	if (ConfigurationWrapper.isJobCanceled(context)) {
	    return Outcome.SKIPPED_CANCELED;
	}

	String onlineId = job.onlineId();
	Date expectedDate = job.expectedDate();

	GSLoggerFactory.getLogger(getClass()).info("[DATA-CACHE] Augmenting {} record {} {}/{}",
		job.isScheduled() ? "expected available" : job.backfillBegin() != null ? "incomplete" : "not scheduled", onlineId, index,
		total);

	GSResource resource;

	try {

	    ResultSet<GSResource> resultSet = AccessQueryUtils.findResource(onlineId, Optional.empty(), onlineId);

	    if (resultSet.getResultsList().isEmpty()) {

		GSLoggerFactory.getLogger(getClass()).warn("[DATA-CACHE] No resource found for online identifier {}", onlineId);
		writeStatistics(dataCacheConnector, sourceId, onlineId, "DHT-Resource not found", expectedDate);
		return Outcome.NOT_FOUND;
	    }

	    if (resultSet.getResultsList().size() > 1) {
		GSLoggerFactory.getLogger(getClass()).warn("[DATA-CACHE] More than one resource for the same online identifier {}",
			onlineId);
	    }

	    resource = resultSet.getResultsList().get(0);

	    List<String> uris = resource.getExtensionHandler().getObservedPropertyURI().map(List::of).orElse(List.of());
	    if (!reconciler.isSelected(uris)) {

		GSLoggerFactory.getLogger(getClass()).info("[DATA-CACHE] Series {} not selected, observed property URI: {}. Removing it",
			onlineId, uris.isEmpty() ? "none" : uris.get(0));
		try {
		    dataCacheConnector.deleteData(sourceId, onlineId);
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error("[DATA-CACHE] Unable to remove series {}: {}", onlineId, e.getMessage(), e);
		    // not updated until the next alignment with the metadata
		    excluded.add(onlineId);
		}
		return Outcome.EXCLUDED;
	    }

	    // urn series are fully reloaded at each refresh
	    if (onlineId.startsWith("urn")) {
		dataCacheConnector.deleteBefore(null, sourceId, onlineId);
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("[DATA-CACHE] Error preparing augmentation of {}: {}", onlineId, e.getMessage(),
		    e);
	    writeStatistics(dataCacheConnector, sourceId, onlineId, "DHT-" + e.getMessage(), expectedDate);
	    return Outcome.FAILED;
	}

	try {

	    DataCacheAugmenter augmenter = new DataCacheAugmenter();
	    // for not scheduled series, missing recent data is not unexpected, so there is no need to wait for it
	    augmenter.setWaitForResults(job.isScheduled());
	    augmenter.setFillDays(fillDays);
	    augmenter.setBackfill(job.backfillBegin(), job.backfillEnd());
	    augmenter.setDeferExpectedTimeClearing(true);
	    augmenter.augment(resource);
	    augmenter.getExpectedTimeHandover().ifPresent(handovers::add);
	    // only the values of scheduled series are new: the other ones fill the cache
	    if (newRecords != null && job.isScheduled()) {
		newRecords.addAll(augmenter.getNewRecords());
	    }
	    return Outcome.AUGMENTED;

	} catch (Exception e) {

	    // the augmenter already writes the error statistics
	    GSLoggerFactory.getLogger(getClass()).error("[DATA-CACHE] Error augmenting {}: {}", onlineId, e.getMessage(), e);
	    return Outcome.FAILED;
	}
    }

    /**
     * Clears the previous expected times of the series whose new values have been written
     *
     * @return true if at least one expected time has been cleared
     */
    private boolean completeHandovers(Queue<ExpectedTimeHandover> handovers, DataCacheConnector dataCacheConnector, String sourceId) {

	int cleared = 0;
	int kept = 0;

	for (ExpectedTimeHandover handover : handovers) {

	    try {

		if (handover.confirmAndClear(dataCacheConnector)) {
		    cleared++;
		} else {
		    kept++;
		}

	    } catch (Exception e) {

		kept++;
		GSLoggerFactory.getLogger(getClass()).error("[DATA-CACHE] Unable to clear the previous expected time of {}: {}",
			handover.getDataIdentifier(), e.getMessage(), e);
	    }
	}

	if (kept > 0) {
	    GSLoggerFactory.getLogger(getClass()).warn("[DATA-CACHE] Source {} previous expected times cleared {}, kept {}", sourceId,
		    cleared, kept);
	}

	return cleared > 0;
    }

    private void writeStatistics(DataCacheConnector dataCacheConnector, String sourceId, String onlineId, String error,
	    Date expectedDate) {

	try {
	    dataCacheConnector.writeStatistics(new StatisticsRecord(new Date(), sourceId, onlineId, 0, error, expectedDate, null));
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("[DATA-CACHE] Unable to write statistics of {}", onlineId, e);
	}
    }

    /**
     * Waits for the data cache buffer to be written and for the written records to become searchable, so that the next
     * listing reflects the updated expected times
     *
     * @return true if the job has been canceled
     */
    private boolean waitForWrites(JobExecutionContext context, DataCacheConnector dataCacheConnector) {

	long start = System.currentTimeMillis();

	while (dataCacheConnector.countRecordsInDataBuffer() > 0 && System.currentTimeMillis() - start < MAX_SETTLE_WAIT_MS) {

	    if (sleep(context, 1000)) {
		return true;
	    }
	}

	return sleep(context, REFRESH_WAIT_MS);
    }

    /**
     * Sleeps for the given time, checking the job cancellation
     *
     * @return true if the job has been canceled or the thread interrupted
     */
    private boolean sleep(JobExecutionContext context, long millis) {

	long end = System.currentTimeMillis() + millis;

	try {

	    long remaining;
	    while ((remaining = end - System.currentTimeMillis()) > 0) {

		if (ConfigurationWrapper.isJobCanceled(context)) {
		    return true;
		}

		Thread.sleep(Math.min(remaining, SLEEP_STEP_MS));
	    }

	} catch (InterruptedException e) {

	    Thread.currentThread().interrupt();
	    return true;
	}

	return ConfigurationWrapper.isJobCanceled(context);
    }

    @Override
    public String getName() {

	return "Data harvester task";
    }
}
