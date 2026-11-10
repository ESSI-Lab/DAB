package eu.essi_lab.access.datacache;

import java.io.IOException;

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

import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Collection;

import eu.essi_lab.access.datacache.DataCacheConnectorFactory.DataConnectorType;

/**
 * @author boldrini
 */
public abstract class DataCacheConnector {

    public static final Long DEFAULT_FLUSH_INTERVAL_MS = 5000l;
    public static final Integer DEFAULT_MAX_BULK_SIZE = 1000;
    public static final Integer DEFAULT_CACHED_DAYS = 0;

    public static final String FLUSH_INTERVAL_MS = "FLUSH_INTERVAL_MS";
    public static final String MAX_BULK_SIZE = "MAX_BULK_SIZE";
    public static final String CACHED_DAYS = "CACHED_DAYS";
    
    public abstract boolean supports(DataConnectorType type);

    public abstract void initialize(URL endpoint, String username, String password, String databaseName) throws Exception;

    /**
     * Configure the connector with custom parameters
     */
    public abstract void configure(String key, String value);

    public abstract void write(DataRecord record) throws IOException;

    public void write(List<DataRecord> records) throws IOException {
	for (DataRecord record : records) {
	    if (record != null) {
		write(record);
	    }
	}
    }

    /**
     * Releases resources
     * 
     * @throws Exception
     */
    public abstract void close() throws Exception;

    /**
     * Removes all records before the given date
     * Clear all records if date parameter is null
     * 
     * @param date
     * @throws Exception
     */
    public void deleteBefore(Date date, String sourceIdentifier) throws Exception {
	deleteBefore(date, sourceIdentifier, null);
    }

    /**
     * @param date
     * @param sourceIdentifier
     * @param dataIdentifier
     * @return the number of deleted records
     * @throws Exception
     */
    public abstract long deleteBefore(Date date, String sourceIdentifier, String dataIdentifier) throws Exception;

    /**
     * Removes all records before the given date from active stations and unknown type stations
     * Non active stations data are untouched
     * 
     * @param date
     * @throws Exception
     */
    public abstract void deleteFromActiveStationsBefore(Date date, String sourceIdentifier) throws Exception;

    /**
     * Count the records
     * 
     * @return
     * @throws Exception
     */
    public abstract Long count() throws Exception;
    
	public abstract Map<String, SourceCacheStats> getCacheStatsPerSource(List<String> selectedSourceIds) throws IOException;

    /**
     * @param sourceId
     * @return the cache statistics of the given source, per observed property (pair of observed property URI and name;
     *         a missing URI or name is an empty string)
     * @throws IOException
     */
    public abstract List<PropertyCacheStats> getCacheStatsPerObservedProperty(String sourceId) throws IOException;

    public abstract Date getFirstDate(String dataIdentifier) throws Exception;

    public abstract Date getLastDate(String dataIdentifier) throws Exception;

    /**
     * Synchronously retrieve the last data records
     * the list is ordered from the most recent to the older one
     * 
     * @param maxRecords the number of records to retrieve
     * @param dataIdentifier
     * @return
     * @throws Exception
     */
    public abstract List<DataRecord> getLastRecords(Integer maxRecords, String... dataIdentifier) throws Exception;

    /**
     * 
     * @param maxRecords
     * @param ascendOrder
     * @param necessaryProperties all these properties need to be present
     * @param sufficientProperties at least one of these properties need to be present
     * @return
     * @throws Exception
     */
    public abstract Response<DataRecord> getRecordsWithProperties(Integer maxRecords, Date begin, Date end, boolean ascendOrder,
	    List<SimpleEntry<String, String>> necessaryProperties, 
	    List<SimpleEntry<String, String>> sufficientProperties) throws Exception;

    /**
     * Asynchronously retrieve the last data records
     * the list is ordered from the most recent to the older one
     * 
     * @param maxRecords the number of records to retrieve
     * @param dataIdentifier
     * @return
     * @throws Exception
     */
    public abstract void getLastRecords(ResponseListener<DataRecord> listener, Integer maxRecords, String... dataIdentifier)
	    throws Exception;

    /**
     * Synchronously retrieve data records
     * the list is ordered from the most recent to the older one
     * 
     * @param begin
     * @param end
     * @param dataIdentifier
     * @return
     * @throws Exception
     */
    public abstract List<DataRecord> getRecords(Date begin, Date end, String... dataIdentifier) throws Exception;

    /**
     * Asynchronously retrieve data records
     * the list is ordered from the most recent to the older one
     * 
     * @param listener
     * @param begin
     * @param end
     * @param dataIdentifier
     * @throws Exception
     */
    public abstract void getRecords(ResponseListener<DataRecord> listener, Date begin, Date end, String... dataIdentifier) throws Exception;

    /**
     * Returns the list of expected available records from a given source identified by the provided sourceIdentifier,
     * in couples Online resource identifier, expected date
     * 
     * @return
     * @throws Exception
     */
    public abstract List<SimpleEntry<String, Date>> getExpectedAvailableRecords(String sourceIdentifier) throws Exception;

    /**
     * Returns the next (future) record expected time from a given source identified by the provided sourceIdentifier,
     * in couples Online resource identifier, expected date
     * 
     * @return
     * @throws Exception
     */
    public abstract SimpleEntry<String, Date> getNextExpectedRecord(String sourceIdentifier) throws Exception;

    /**
     * A consumer of pages
     *
     * @param <T>
     */
    @FunctionalInterface
    public interface PageConsumer<T> {

	/**
	 * @param page
	 * @return false to stop the iteration
	 * @throws Exception
	 */
	boolean accept(T page) throws Exception;
    }

    /**
     * Iterates the identifiers of the data (time series) of the given source having at least one record in the cache,
     * in pages
     *
     * @param sourceIdentifier
     * @param consumer
     * @throws Exception
     */
    public abstract void forEachDataIdentifiersPage(String sourceIdentifier, PageConsumer<List<String>> consumer) throws Exception;

    /**
     * Returns a summary of the given time series of the given source in the cache, including the outcome of their last
     * update recorded in the statistics. Time series having only statistics (e.g. never successfully updated) are
     * included as well, with no values
     *
     * @param sourceIdentifier
     * @param dataIdentifiers
     * @return the summaries, by data identifier
     * @throws Exception
     */
    public abstract Map<String, SeriesCacheInfo> getSeriesCacheInfo(String sourceIdentifier, Collection<String> dataIdentifiers)
	    throws Exception;

    /**
     * Removes the records, the stations and the status of the given data (time series) of the given source
     *
     * @param sourceIdentifier
     * @param dataIdentifier
     * @return the number of deleted records
     * @throws Exception
     */
    public abstract long deleteData(String sourceIdentifier, String dataIdentifier) throws Exception;

    /**
     * Writes (synchronously) the given series statuses
     *
     * @param records
     * @throws Exception
     */
    public abstract void writeSeriesStatus(List<SeriesStatusRecord> records) throws Exception;

    /**
     * @param sourceIdentifier
     * @param dataIdentifiers
     * @return the stored statuses of the given series, by data identifier
     * @throws Exception
     */
    public abstract Map<String, SeriesStatusRecord> getSeriesStatus(String sourceIdentifier, Collection<String> dataIdentifiers)
	    throws Exception;

    /**
     * @param sourceIdentifier
     * @param attemptedBefore
     * @param size
     * @return series to fill ({@link SeriesStatus#MISSING}), to complete with older data ({@link SeriesStatus#INCOMPLETE})
     *         or to refresh ({@link SeriesStatus#NOT_SCHEDULED}), whose
     *         augmentation was not attempted after the given time
     * @throws Exception
     */
    public abstract List<SeriesStatusRecord> getSeriesToAugment(String sourceIdentifier, Date attemptedBefore, int size)
	    throws Exception;

    /**
     * Iterates, in pages, the stored statuses of the given source having the given base status
     *
     * @param sourceIdentifier
     * @param status
     * @param consumer
     * @throws Exception
     */
    public abstract void forEachSeriesStatusPage(String sourceIdentifier, SeriesStatus status,
	    PageConsumer<List<SeriesStatusRecord>> consumer) throws Exception;

    /**
     * Removes the stored statuses of the given source not written by the given pass
     *
     * @param sourceIdentifier
     * @param passId
     * @return the number of removed statuses
     * @throws Exception
     */
    public abstract long deleteSeriesStatusNotInPass(String sourceIdentifier, String passId) throws Exception;

    /**
     * Removes all the stored series statuses of the given source
     *
     * @param sourceIdentifier
     * @return the number of removed statuses
     * @throws Exception
     */
    public abstract long deleteSeriesStatus(String sourceIdentifier) throws Exception;

    /**
     * Removes all the harvesting statistics of the given source
     *
     * @param sourceIdentifier
     * @return the number of removed statistics
     * @throws Exception
     */
    public abstract long deleteStatistics(String sourceIdentifier) throws Exception;

    /**
     * Searches the stored statuses of a source, sorted from the most severe (time dependent statuses are derived at the
     * time of the search)
     *
     * @param sourceIdentifier
     * @param conceptKey optional concept filter
     * @param status optional (effective) status filter
     * @param text optional text filter on data identifier, title, platform and observed property
     * @param cursor optional cursor of the page, as returned by the previous page
     * @param size
     * @return
     * @throws Exception
     */
    public abstract SeriesStatusPage searchSeriesStatus(String sourceIdentifier, String conceptKey, SeriesStatus status, String text,
	    String cursor, int size) throws Exception;

    /**
     * Returns the list of expected available records from a given source identified by the provided sourceIdentifier,
     * in couples Online resource identifier, expected date
     * 
     * @return
     * @throws Exception
     */
    public abstract void writeStatistics(StatisticsRecord record) throws Exception;

    /**
     * Retrieve maxRecords (or at most 10000) stations with the given parameters. To retrieve all stations in case they
     * are more than 10000 see the method with the listener
     * 
     * @param bbox
     * @param maxRecords
     * @param allProperties or at least one property
     * @param propertyValue
     * @return
     * @throws Exception
     */
    public abstract List<StationRecord> getStationsWithProperties(BBOX bbox, Integer offset, Integer maxRecords, boolean allProperties,
	    SimpleEntry<String, String>... propertyValue) throws Exception;
    
    public abstract List<StationRecord> getStationsWithProperties(BBOX bbox, Integer offset, Integer maxRecords, boolean allProperties, List<String>neededProperties,
	    SimpleEntry<String, String>... propertyValue) throws Exception;

    /**
     * Retrieve all the stations (until maxRecords).
     * 
     * @param bbox
     * @param maxRecords
     * @param allProperties or at least one property
     * @param propertyValue
     * @return
     * @throws Exception
     */
    public abstract void getStationsWithProperties(ResponseListener<StationRecord> listener,Date lastHarvesting, BBOX bbox, Integer maxRecords,
	    boolean allProperties, SimpleEntry<String, String>... propertyValue) throws Exception;

    /**
     * @param bbox
     * @param maxRecords
     * @param allProperties or at least one property
     * @param propertyValue
     * @return
     * @throws Exception
     */
    public abstract StationsStatistics getStationStatisticsWithProperties(BBOX bbox, boolean allProperties,
	    SimpleEntry<String, String>... propertyValue) throws Exception;

    public abstract void deleteStations(String sourceIdentifier) throws Exception;
    
    public abstract void deleteStations(String sourceIdentifier,String theme) throws Exception;
    
    public abstract void deleteStation(String stationId) throws Exception;

    public abstract void writeStation(StationRecord record) throws Exception;

    public abstract void clearStations() throws Exception;

    public abstract int countRecordsInDataBuffer();

}
