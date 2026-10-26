package eu.essi_lab.access.augmenter;

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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataRecord;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * Safe handover of the next record expected time of a series, which is held by its newest cached record and drives the
 * scheduling of its refreshes (see {@link DataCacheConnector#getExpectedAvailableRecords(String)}).<br>
 * Since the data cache writes are buffered and not atomic, and the records of a failed write are lost, the expected
 * time held by the current record must be cleared only once the new records (with the new expected time) are known to
 * be written. Otherwise a failed write would leave the series without expected time, and so no longer scheduled.<br>
 * If more records hold an expected time (e.g. a clearing failed), the newest one is the current one, and the older
 * ones are stale and can be cleared at once
 *
 * @author boldrini
 */
public class ExpectedTimeHandover {

    private final String dataIdentifier;
    private final Date newestDate;
    private final List<DataRecord> toClear;

    /**
     * @param dataIdentifier the series
     * @param newestDate the date of the newest written record, whose presence in the cache confirms the write
     * @param toClear the records whose expected time is to be cleared once the write is confirmed
     */
    public ExpectedTimeHandover(String dataIdentifier, Date newestDate, List<DataRecord> toClear) {

	this.dataIdentifier = dataIdentifier;
	this.newestDate = newestDate;
	this.toClear = toClear;
    }

    /**
     * @param cachedRecords
     * @return the newest of the given records holding an expected time
     */
    public static Optional<DataRecord> getCurrent(List<DataRecord> cachedRecords) {

	return cachedRecords.stream().//
		filter(r -> r.getNextRecordExpectedTime() != null && r.getDate() != null).//
		max(Comparator.comparing(DataRecord::getDate));
    }

    /**
     * @param cachedRecords
     * @return the given records holding an expected time, except the current one (see {@link #getCurrent(List)})
     */
    public static List<DataRecord> getStale(List<DataRecord> cachedRecords) {

	Optional<DataRecord> current = getCurrent(cachedRecords);

	List<DataRecord> ret = new ArrayList<>();
	for (DataRecord record : cachedRecords) {
	    if (record.getNextRecordExpectedTime() != null && (current.isEmpty() || record != current.get())) {
		ret.add(record);
	    }
	}

	return ret;
    }

    /**
     * Clears the expected time of the given records and writes them
     *
     * @param records
     * @param dataCacheConnector
     * @throws Exception
     */
    public static void clear(List<DataRecord> records, DataCacheConnector dataCacheConnector) throws Exception {

	for (DataRecord record : records) {
	    record.setNextRecordExpectedTime(null);
	}

	dataCacheConnector.write(records);
    }

    /**
     * Clears the expected time of the records to clear, if the newest written record is found in the cache. Otherwise
     * the expected time is kept, so that the series is refreshed again
     *
     * @param dataCacheConnector
     * @return true if the write has been confirmed and the records cleared
     * @throws Exception
     */
    public boolean confirmAndClear(DataCacheConnector dataCacheConnector) throws Exception {

	boolean written = dataCacheConnector.//
		getRecords(newestDate, newestDate, dataIdentifier).//
		stream().//
		anyMatch(r -> newestDate.equals(r.getDate()));

	if (!written) {

	    GSLoggerFactory.getLogger(getClass()).warn("[DATA-CACHE] New records of {} not found in the cache, expected time kept",
		    dataIdentifier);
	    return false;
	}

	clear(toClear, dataCacheConnector);

	return true;
    }

    /**
     * @return
     */
    public String getDataIdentifier() {

	return dataIdentifier;
    }

    /**
     * @return
     */
    public Date getNewestDate() {

	return newestDate;
    }

    /**
     * @return
     */
    public List<DataRecord> getToClear() {

	return toClear;
    }
}
