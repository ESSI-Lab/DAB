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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.SeriesCacheInfo;
import eu.essi_lab.access.datacache.SeriesStatus;
import eu.essi_lab.access.datacache.SeriesStatusRecord;
import eu.essi_lab.access.datacache.SourceSeriesLister;
import eu.essi_lab.access.datacache.SourceSeriesLister.SeriesMetadata;
import eu.essi_lab.api.database.SourceStorageProvider;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.model.GSSource;

/**
 * Aligns the data cache of a source with its metadata, and stores the status of each time series (see
 * {@link SeriesStatus}). The source is processed in pages, so that its size is not limited by the memory:
 * <ol>
 * <li>the metadata records are read in pages. For each page, the cache content and the last update of its time series
 * are read, and their statuses are stored. The selected series missing in the cache ({@link SeriesStatus#MISSING}) or
 * current but not scheduled for update ({@link SeriesStatus#NOT_SCHEDULED}) are then available to be augmented (see
 * {@link DataCacheConnector#getSeriesToAugment(String, Date, int)})</li>
 * <li>the time series in the cache are read in pages: the ones without a status stored by the first step are no more in
 * the metadata ({@link SeriesStatus#NOT_IN_METADATA})</li>
 * <li>the time series no more in the metadata are removed from the cache</li>
 * </ol>
 * If a set of observed property URIs is given, only the time series having one of them are selected, and the cached
 * time series not selected are removed from the cache.
 *
 * @author boldrini
 */
class DataCacheReconciler {

    /**
     * Removal is skipped if more than this ratio of the cached time series would be removed, since this most likely
     * means an incomplete metadata harvesting
     */
    private static final double MAX_REMOVAL_RATIO = 0.2;

    /**
     * Below this number of time series to remove, the {@link #MAX_REMOVAL_RATIO} is not checked
     */
    private static final int MIN_REMOVAL_CHECK = 10;

    private final String sourceId;
    private final DataCacheConnector dataCacheConnector;
    private final Set<String> observedPropertyURIs;

    /**
     * Start of the last completed pass
     */
    private Date passStart;

    /**
     * The result of a reconciliation
     */
    static class Result {

	long metadataSeries;
	long selected;
	long cached;
	long missing;
	long unscheduled;
	long notInMetadata;
	long removed;
	long excludedRemoved;
	boolean removalSkipped;
	boolean canceled;

	@Override
	public String toString() {

	    return "Metadata series: " + metadataSeries + " Selected: " + selected + " Selected and cached: " + cached + " Missing: "
		    + missing + " Current and unscheduled: " + unscheduled + " Not in metadata: " + notInMetadata
		    + " Removed (not selected): " + excludedRemoved + " Removed (no more in the metadata): " + removed
		    + (removalSkipped ? " (removal skipped)" : "") + (canceled ? " CANCELED" : "");
	}
    }

    /**
     * @param sourceId
     * @param dataCacheConnector
     * @param observedPropertyURIs if not empty, only the time series having one of these observed property URIs are
     *        selected
     */
    DataCacheReconciler(String sourceId, DataCacheConnector dataCacheConnector, Set<String> observedPropertyURIs) {

	this.sourceId = sourceId;
	this.dataCacheConnector = dataCacheConnector;
	this.observedPropertyURIs = observedPropertyURIs;
    }

    String getSourceId() {

	return sourceId;
    }

    /**
     * @return the start of the last completed pass, if any. The series to augment are the ones not attempted after
     *         this time
     */
    Optional<Date> getPassStart() {

	return Optional.ofNullable(passStart);
    }

    /**
     * @return the harvesting properties of the source, if available
     */
    Optional<HarvestingProperties> getHarvestingProperties() {

	try {

	    GSSource source = ConfigurationWrapper.getSource(sourceId);
	    if (source == null) {
		return Optional.empty();
	    }

	    SourceStorageProvider sourceStorage = DatabaseProviderFactory.getSourceStorage(ConfigurationWrapper.getStorageInfo());
	    return Optional.ofNullable(sourceStorage.retrieveHarvestingProperties(source));

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).warn("[DATA-CACHE] Source {} unable to read harvesting properties: {}", sourceId,
		    e.getMessage());
	    return Optional.empty();
	}
    }

    /**
     * @return true if the given observed property URIs are selected
     */
    boolean isSelected(List<String> uris) {

	return observedPropertyURIs.isEmpty() || uris.stream().anyMatch(observedPropertyURIs::contains);
    }

    /**
     * @param removalAllowed if false, the time series no more in the metadata are not removed (e.g. while the source is
     *        being harvested)
     * @param canceled checked between pages
     * @return
     * @throws Exception
     */
    Result reconcile(boolean removalAllowed, BooleanSupplier canceled) throws Exception {

	Result result = new Result();

	GSSource source = ConfigurationWrapper.getSource(sourceId);
	if (source == null) {
	    throw new Exception("Source " + sourceId + " not found");
	}

	String passId = UUID.randomUUID().toString();
	Date start = new Date();

	// 1) METADATA

	new SourceSeriesLister().forEachPage(source, Optional.empty(), page -> {

	    if (canceled.getAsBoolean()) {
		result.canceled = true;
		return false;
	    }

	    result.metadataSeries += page.size();

	    Map<String, SeriesCacheInfo> cache = dataCacheConnector.getSeriesCacheInfo(sourceId, page.keySet());

	    List<SeriesStatusRecord> records = new ArrayList<>();

	    for (SeriesMetadata series : page.values()) {

		String onlineId = series.getOnlineId();
		SeriesCacheInfo info = cache.get(onlineId);

		if (!isSelected(series.getObservedPropertyURIs())) {

		    // removal of the series not selected by the observed property. since they are in the metadata, the
		    // removal is deliberate and the checks for incomplete metadata are not needed
		    if (info != null && info.getValuesCount() > 0) {
			GSLoggerFactory.getLogger(getClass()).info(
				"[DATA-CACHE] Source {} removing series not selected by the observed property: {}", sourceId, onlineId);
			dataCacheConnector.deleteData(sourceId, onlineId);
			result.excludedRemoved++;
		    }
		    continue;
		}

		result.selected++;

		SeriesStatusRecord record = new SeriesStatusRecord();
		record.setSourceIdentifier(sourceId);
		record.setDataIdentifier(onlineId);
		record.setInMetadata(true);
		record.setCurrent(series.isCurrent());
		record.setTitle(series.getTitle());
		record.setPlatform(series.getPlatform());
		record.setObservedProperty(series.getObservedProperty());
		record.setObservedPropertyURI(series.getObservedPropertyURI().orElse(null));
		record.setPassId(passId);
		record.update(info);

		if (info != null && info.getValuesCount() > 0) {
		    result.cached++;
		}

		switch (SeriesStatus.valueOf(record.getStatus())) {
		case MISSING -> result.missing++;
		case NOT_SCHEDULED -> result.unscheduled++;
		default -> {
		}
		}

		records.add(record);
	    }

	    dataCacheConnector.writeSeriesStatus(records);
	    return true;
	});

	if (result.canceled) {
	    return result;
	}

	// 2) CACHE: series without a status of this pass are no more in the metadata (or not selected)

	dataCacheConnector.forEachDataIdentifiersPage(sourceId, ids -> {

	    if (canceled.getAsBoolean()) {
		result.canceled = true;
		return false;
	    }

	    Map<String, SeriesStatusRecord> statuses = dataCacheConnector.getSeriesStatus(sourceId, ids);

	    List<String> notInMetadata = ids.stream()
		    .filter(id -> !statuses.containsKey(id) || !passId.equals(statuses.get(id).getPassId())).toList();

	    if (notInMetadata.isEmpty()) {
		return true;
	    }

	    Map<String, SeriesCacheInfo> cache = dataCacheConnector.getSeriesCacheInfo(sourceId, notInMetadata);

	    List<SeriesStatusRecord> records = new ArrayList<>();

	    for (String id : notInMetadata) {

		SeriesStatusRecord previous = statuses.get(id);

		SeriesStatusRecord record = new SeriesStatusRecord();
		record.setSourceIdentifier(sourceId);
		record.setDataIdentifier(id);
		record.setInMetadata(false);
		record.setCurrent(false);
		if (previous != null) {
		    record.setTitle(previous.getTitle());
		    record.setPlatform(previous.getPlatform());
		    record.setObservedProperty(previous.getObservedProperty());
		    record.setObservedPropertyURI(previous.getObservedPropertyURI());
		}
		record.setPassId(passId);
		record.update(cache.get(id));

		records.add(record);
	    }

	    dataCacheConnector.writeSeriesStatus(records);
	    result.notInMetadata += records.size();
	    return true;
	});

	if (result.canceled) {
	    return result;
	}

	// 3) REMOVAL of the series no more in the metadata

	if (result.notInMetadata > 0) {

	    long cachedTotal = result.cached + result.notInMetadata;

	    if (!removalAllowed) {

		GSLoggerFactory.getLogger(getClass()).info("[DATA-CACHE] Source {} removal of {} series postponed", sourceId,
			result.notInMetadata);
		result.removalSkipped = true;

	    } else if (result.metadataSeries == 0
		    || (result.notInMetadata >= MIN_REMOVAL_CHECK && result.notInMetadata > cachedTotal * MAX_REMOVAL_RATIO)) {

		GSLoggerFactory.getLogger(getClass()).warn(
			"[DATA-CACHE] Source {} removal of {} series out of {} cached skipped, metadata series: {}. Too many removals, possibly due to an incomplete harvesting",
			sourceId, result.notInMetadata, cachedTotal, result.metadataSeries);
		result.removalSkipped = true;

	    } else {

		dataCacheConnector.forEachSeriesStatusPage(sourceId, SeriesStatus.NOT_IN_METADATA, page -> {

		    for (SeriesStatusRecord record : page) {

			if (canceled.getAsBoolean()) {
			    result.canceled = true;
			    return false;
			}

			GSLoggerFactory.getLogger(getClass()).info("[DATA-CACHE] Source {} removing series no more in the metadata: {}",
				sourceId, record.getDataIdentifier());
			dataCacheConnector.deleteData(sourceId, record.getDataIdentifier());
			result.removed++;
		    }
		    return true;
		});
	    }
	}

	if (result.canceled) {
	    return result;
	}

	// 4) CLEANUP of the statuses of series no more in the metadata nor in the cache

	long deleted = dataCacheConnector.deleteSeriesStatusNotInPass(sourceId, passId);
	GSLoggerFactory.getLogger(getClass()).debug("[DATA-CACHE] Source {} obsolete statuses removed: {}", sourceId, deleted);

	passStart = start;

	return result;
    }

    /**
     * Updates the stored statuses of the given series, after their augmentation
     *
     * @param ids
     * @param attempted the series whose augmentation was attempted, to mark as such
     * @throws Exception
     */
    void updateStatuses(Collection<String> ids, Set<String> attempted) throws Exception {

	Map<String, SeriesStatusRecord> statuses = dataCacheConnector.getSeriesStatus(sourceId, ids);
	if (statuses.isEmpty()) {
	    return;
	}

	Map<String, SeriesCacheInfo> cache = dataCacheConnector.getSeriesCacheInfo(sourceId, statuses.keySet());

	Date now = new Date();
	List<SeriesStatusRecord> records = new ArrayList<>();

	for (SeriesStatusRecord record : statuses.values()) {

	    record.update(cache.get(record.getDataIdentifier()));
	    if (attempted.contains(record.getDataIdentifier())) {
		record.setAttempted(now);
	    }
	    records.add(record);
	}

	dataCacheConnector.writeSeriesStatus(records);
    }

    /**
     * @return the ids of the given records
     */
    static Set<String> ids(Collection<SeriesStatusRecord> records) {

	Set<String> ret = new HashSet<>();
	records.forEach(r -> ret.add(r.getDataIdentifier()));
	return ret;
    }
}
