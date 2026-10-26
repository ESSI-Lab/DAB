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

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.xml.xpath.XPathException;

import org.cuahsi.waterml._1.GeogLocationType;
import org.cuahsi.waterml._1.LatLonPointType;
import org.cuahsi.waterml._1.SiteInfoType;
import org.cuahsi.waterml._1.SiteInfoType.GeoLocation;
import org.cuahsi.waterml._1.SourceInfoType;
import org.cuahsi.waterml._1.TimeSeriesResponseType;
import org.cuahsi.waterml._1.TimeSeriesType;
import org.cuahsi.waterml._1.UnitsType;
import org.cuahsi.waterml._1.ValueSingleVariable;
import org.cuahsi.waterml._1.VariableInfoType;
import org.cuahsi.waterml._1.essi.JAXBWML;
import org.cuahsi.waterml._1.essi.JAXBWML.WML_SiteProperty;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.access.DataValidatorErrorCode;
import eu.essi_lab.access.datacache.BBOX4326;
import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory;
import eu.essi_lab.access.datacache.DataRecord;
import eu.essi_lab.access.datacache.LatitudeLongitude;
import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.access.datacache.StatisticsRecord;
import eu.essi_lab.augmenter.ResourceAugmenter;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
import eu.essi_lab.lib.net.utils.whos.WHOSOntology;
import eu.essi_lab.lib.net.utils.whos.WMOOntology;
import eu.essi_lab.lib.net.utils.whos.WMOUnit;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.request.executor.AccessExecutor;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * @author boldrini
 * @author Fabrizio
 */
public class DataCacheAugmenter extends ResourceAugmenter<DataCacheAugmenterSetting> {

    /**
     * 
     */
    private static final String DATA_CACHE_AUGMENTER_CONNECTOR_FACTORY_ERROR = "DATA_CACHE_AUGMENTER_CONNECTOR_FACTORY_ERROR";
    private static final int MAX_RETRY = 10;

    /**
     * Value of {@link #setFillDays(Integer)} to load the whole temporal extent of the series
     */
    public static final int FILL_ALL = 0;

    /**
     * Max number of values requested at once when loading the past data of a series, if its time resolution is known
     */
    private static final long FILL_CHUNK_VALUES = 10000;

    /**
     * Period requested at once when loading the past data of a series, if its time resolution is unknown
     */
    private static final long DEFAULT_FILL_CHUNK_MS = TimeUnit.DAYS.toMillis(365);

    /**
     * While loading the past data of a series, writing is paused while the data cache buffer holds more than this number
     * of records
     */
    private static final int FILL_BUFFER_SIZE = 20000;

    private boolean waitForResults = true;

    private List<DataRecord> newRecords = new ArrayList<>();

    private boolean deferExpectedTimeClearing = false;

    private ExpectedTimeHandover expectedTimeHandover;

    private Integer fillDays;

    private Date backfillBegin;

    private Date backfillEnd;

    /**
     * Sets the older data to load for a series already cached, before its update. Ignored if the series is not cached
     *
     * @param begin the begin of the data to load, null for none
     * @param end the end of the data to load (usually the first cached observation), null for none
     */
    public void setBackfill(Date begin, Date end) {

	this.backfillBegin = begin;
	this.backfillEnd = end;
    }

    /**
     * @param fillDays days of data loaded when the cache of a series is first filled, back from the end of its temporal
     *        extent. {@link #FILL_ALL} loads the whole temporal extent. If not set (default), only the latest 20 time
     *        steps are loaded (30 days, if the time resolution is unknown). The data older than this default window are
     *        loaded in chunks, after the default window has been processed
     */
    public void setFillDays(Integer fillDays) {

	this.fillDays = fillDays;
    }

    /**
     * @return the records retrieved and written to the cache by the last {@link #augment(GSResource)}, excluding the
     *         past data loaded in chunks (see {@link #setFillDays(Integer)}) and the older data loaded before the update
     *         (see {@link #setBackfill(Date, Date)}). For a series already cached, these are the new values of the series
     */
    public List<DataRecord> getNewRecords() {

	return newRecords;
    }

    /**
     * @param defer if true, the expected time of the current record of an updated series is not cleared by
     *        {@link #augment(GSResource)}: the caller must clear it with {@link #getExpectedTimeHandover()} once the new
     *        records are written. Otherwise (default) its clearing is written after the new records
     */
    public void setDeferExpectedTimeClearing(boolean defer) {

	this.deferExpectedTimeClearing = defer;
    }

    /**
     * @return the handover of the expected time of the series updated by the last {@link #augment(GSResource)}, if
     *         deferred (see {@link #setDeferExpectedTimeClearing(boolean)}) and the series held an expected time
     */
    public Optional<ExpectedTimeHandover> getExpectedTimeHandover() {

	return Optional.ofNullable(expectedTimeHandover);
    }

    /**
     * @param waitForResults if true (default), when no results are found the retrieval is repeated every 5 minutes, up
     *        to {@link #MAX_RETRY} times. Disabling it is useful when filling the cache of many resources, where missing
     *        recent data is not an exceptional case
     */
    public void setWaitForResults(boolean waitForResults) {

	this.waitForResults = waitForResults;
    }

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	newRecords = new ArrayList<>();
	expectedTimeHandover = null;

	DataCacheConnector dataCacheConnector = null;
	String sourceId = null;
	String onlineId = null;
	Date expected = null;
	try {

	    dataCacheConnector = DataCacheConnectorFactory.getDataCacheConnector();

	    if (dataCacheConnector == null) {
		DataCacheConnectorSetting setting = ConfigurationWrapper.getDataCacheConnectorSetting();
		dataCacheConnector = DataCacheConnectorFactory.newDataCacheConnector(setting);
		String cachedDays = setting.getOptionValue(DataCacheConnector.CACHED_DAYS).get();
		String flushInterval = setting.getOptionValue(DataCacheConnector.FLUSH_INTERVAL_MS).get();
		String maxBulkSize = setting.getOptionValue(DataCacheConnector.MAX_BULK_SIZE).get();
		dataCacheConnector.configure(DataCacheConnector.MAX_BULK_SIZE, maxBulkSize);
		dataCacheConnector.configure(DataCacheConnector.FLUSH_INTERVAL_MS, flushInterval);
		dataCacheConnector.configure(DataCacheConnector.CACHED_DAYS, cachedDays);
		DataCacheConnectorFactory.setDataCacheConnector(dataCacheConnector);
	    }

	    onlineId = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getDistribution().getDistributionOnline()
		    .getIdentifier();
	    if (onlineId == null) {
		throw new RuntimeException("Online identifier not set");
	    }

	    GSLoggerFactory.getLogger(getClass()).info("[DATA-CACHE] Augmenting {}", onlineId);

	    sourceId = resource.getSource().getUniqueIdentifier();

	    // get the last 22 records from cache
	    List<DataRecord> cachedRecords = dataCacheConnector.getLastRecords(22, onlineId);

	    // a counter for observations occurred in the last day
	    int dailyObservations = 0;

	    Date now = new Date();
	    Date yesterday = new Date(now.getTime() - TimeUnit.DAYS.toMillis(1));

	    // this is to exit if only new stations are to be considered
	    boolean exit = checkObtainedRecords(cachedRecords);

	    if (exit) {
		return Optional.empty();
	    }

	    for (DataRecord cachedRecord : cachedRecords) {
		Date date = cachedRecord.getDate();
		if (date.after(yesterday)) {
		    dailyObservations++;
		}
	    }

	    // the current expected time is held by the newest record having one. The older ones (e.g. left by a failed
	    // clearing) are stale and can be cleared at once; the current one is cleared only after the new records are
	    // written, so that the series never remains without expected time
	    List<DataRecord> stale = ExpectedTimeHandover.getStale(cachedRecords);
	    if (!stale.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).warn("[DATA-CACHE] Clearing {} stale expected times of {}", stale.size(), onlineId);
		ExpectedTimeHandover.clear(stale, dataCacheConnector);
	    }
	    DataRecord current = ExpectedTimeHandover.getCurrent(cachedRecords).orElse(null);
	    expected = current != null ? current.getNextRecordExpectedTime() : null;
	    // older data of a cached series
	    FillResult backfill = null;
	    if (backfillBegin != null && backfillEnd != null && backfillBegin.before(backfillEnd) && !cachedRecords.isEmpty()) {
		backfill = fillPast(dataCacheConnector, resource, onlineId, sourceId, getSeriesMetadata(resource), backfillBegin, backfillEnd,
			getTimeResolution(resource, onlineId));
		// the following update, if any, writes its own statistics
		dataCacheConnector.writeStatistics(new StatisticsRecord(new Date(), sourceId, onlineId, backfill.values(), backfill.error(),
			null, cachedRecords.get(0).getDate()));
	    }

	    DataRecord lastObservation = null;

	    //////////// BEGIN and END dates calculation
	    Date begin = null;
	    Date end = null;

	    boolean nearRealTime = false;
	    Long verifiedPublicationGap = null;
	    if (cachedRecords.size() > 1) {
		lastObservation = cachedRecords.get(0);
		if (lastObservation.getVerifiedPublicationGap() != null) {
		    verifiedPublicationGap = lastObservation.getVerifiedPublicationGap();
		}
		// something in the cache, let's fill it only for near real time data
		TemporalExtent temporalExtent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
		long lastYear = TimeUnit.DAYS.toMillis(365);
		if (temporalExtent.isEndPositionIndeterminate()
			|| lastObservation.getDate().after(new Date(new Date().getTime() - lastYear))) {
		    TimeIndeterminateValueType indeterminate = temporalExtent.getIndeterminateEndPosition();
		    if ((indeterminate != null && indeterminate.equals(TimeIndeterminateValueType.NOW))
			    || lastObservation.getDate().after(new Date(new Date().getTime() - lastYear))) {
			nearRealTime = true;
			begin = lastObservation.getDate();
			end = new Date();
		    } else {
			// nothing to do, return
			return Optional.empty();
		    }
		} else {
		    // nothing to do, return
		    return Optional.empty();
		}

	    } else {
		// nothing in the cache, let's fill it
		TemporalExtent temporalExtent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
		end = ISO8601DateTimeUtils.parseISO8601ToDate(temporalExtent.getEndPosition()).get();
		if (temporalExtent.isEndPositionIndeterminate()) {
		    TimeIndeterminateValueType indeterminate = temporalExtent.getIndeterminateEndPosition();
		    if (indeterminate != null && indeterminate.equals(TimeIndeterminateValueType.NOW)) {
			end = new Date();
			nearRealTime = true;
		    }
		}
	    }

	    // BEGIN AND END dates calculated

	    DataDescriptor targetDescriptor = new DataDescriptor();
	    targetDescriptor.setDataFormat(DataFormat.WATERML_1_1());

	    Long ms = getTimeResolution(resource, onlineId);

	    // begin of the past data to load after the first set of acquisitions, if any
	    Date fillBegin = null;
	    if (begin == null) {
		// calculating begin date for the first set of acquisitions
		if (ms != null) {
		    begin = new Date(end.getTime() - ms * 20);
		} else {
		    begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(30));
		}
		fillBegin = getFillBegin(resource, onlineId, end, begin);
		if (fillBegin.after(begin)) {
		    begin = fillBegin;
		}
	    }
	    targetDescriptor.setTemporalDimension(begin, end);
	    ServiceLoader<AccessExecutor> accessLoader = ServiceLoader.load(AccessExecutor.class);
	    AccessExecutor accessExecutor = accessLoader.iterator().next();

	    ResultSet<DataObject> retrieved = retrieveAndCatch(accessExecutor, resource, onlineId, targetDescriptor);

	    List<DataObject> results = retrieved.getResultsList();
	    List<DataRecord> records = new ArrayList<>();

	    boolean effectiveCheck = false;
	    int attempts = 1;

	    while (waitForResults && results.isEmpty() && attempts < MAX_RETRY) {

		GSLoggerFactory.getLogger(getClass()).warn(
			"[DATA-CACHE] Waiting source {} online id {} every 5 minutes for results from: {} now: {}",
			resource.getSource().getUniqueIdentifier(), onlineId, ISO8601DateTimeUtils.getISO8601DateTime(end),
			ISO8601DateTimeUtils.getISO8601DateTime(new Date()));

		effectiveCheck = true;
		targetDescriptor.setTemporalDimension(begin, new Date());

		retrieved = retrieveAndCatch(accessExecutor, resource, onlineId, targetDescriptor);
		results = retrieved.getResultsList();

		Thread.sleep(TimeUnit.MINUTES.toMillis(5));
		attempts++;
	    }

	    if (results.isEmpty()) {

		GSLoggerFactory.getLogger(getClass()).warn("[DATA-CACHE] No results found from: {}", onlineId);

		return Optional.empty();
	    }

	    TimeSeriesType series = decode(results.get(0));
	    List<ValueSingleVariable> values = series.getValues().get(0).getValue();
	    SeriesMetadata metadata = getSeriesMetadata(resource);
	    Double noDataValue = series.getVariable() != null ? series.getVariable().getNoDataValue() : null;
	    for (ValueSingleVariable wmlValue : values) {
		Date date = getDate(wmlValue);
		if (lastObservation == null || !date.equals(lastObservation.getDate())) {
		    DataRecord record = toRecord(wmlValue, date, noDataValue, onlineId, sourceId, metadata);
		    records.add(record);
		}
	    }
	    if (records.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).warn("[DATA-CACHE] Empty records for source {} online id {} this should not happen",
			resource.getSource().getUniqueIdentifier(), onlineId);
		return Optional.empty();
	    }
	    DataRecord r1 = records.get(0);
	    DataRecord rn = records.get(records.size() - 1);
	    if (r1.getDate().before(rn.getDate())) { // the usual situation for records from the DAB
		for (int i = 0; i < records.size(); i++) {
		    DataRecord record = records.get(i);
		    if (lastObservation == null || record.getDate().after(lastObservation.getDate())) {
			cachedRecords.add(0, record);
		    }
		}
	    } else {
		GSLoggerFactory.getLogger(getClass()).warn(
			"[DATA-CACHE] Reversed records from source {} online id {} this should not happen",
			resource.getSource().getUniqueIdentifier(), onlineId);
		for (int i = 0; i < records.size(); i++) {
		    DataRecord record = records.get(records.size() - 1 - i);
		    if (lastObservation == null || record.getDate().after(lastObservation.getDate())) {
			cachedRecords.add(0, record);
		    }
		}
	    }
	    Long actualResolution = null;
	    // the current resolution in the data
	    if (cachedRecords.size() > 2) {
		for (int i = 0; i < cachedRecords.size() - 1; i++) {
		    Date date1 = cachedRecords.get(i).getDate();
		    Date date2 = cachedRecords.get(i + 1).getDate();
		    if (actualResolution == null) {
			actualResolution = date1.getTime() - date2.getTime();
		    } else {
			long check = date1.getTime() - date2.getTime();
			if (!actualResolution.equals(check)) {
			    actualResolution = null;
			    break;
			}
		    }
		}
	    }
	    if (actualResolution == null) {
		GSLoggerFactory.getLogger(getClass()).warn("[DATA-CACHE] Varying time resolution for source {} online id {}",
			resource.getSource().getUniqueIdentifier(), onlineId);
		actualResolution = 1000 * 60 * 60l; // default: one hour
	    }
	    if (r1.getDate().before(rn.getDate())) { // the usual situation for records from the DAB
		lastObservation = records.get(records.size() - 1);
	    } else {
		lastObservation = records.get(0);
	    }

	    Date lastObservationDate = lastObservation.getDate();
	    Date nextObservationDate = new Date(lastObservationDate.getTime() + actualResolution);

	    if (new Date().getTime() - lastObservation.getDate().getTime() < TimeUnit.DAYS.toMillis(60)) {
		nearRealTime = true;
	    }

	    if (nearRealTime) {
		// let's put information about the next expected observation date
		Date nextExpectedObservationDate = null;
		Long publishingGap = now.getTime() - lastObservationDate.getTime();
		if (effectiveCheck || verifiedPublicationGap != null) {
		    if (verifiedPublicationGap != null) {
			publishingGap = verifiedPublicationGap;
		    }
		    nextExpectedObservationDate = new Date(nextObservationDate.getTime() + publishingGap);
		    lastObservation.setNextRecordExpectedTime(nextExpectedObservationDate);
		    lastObservation.setVerifiedPublicationGap(publishingGap);
		} else {
		    // the expected publishing gap is shortened, in order to optimize the retrieval
		    nextExpectedObservationDate = new Date(nextObservationDate.getTime() + publishingGap - (publishingGap / 2));
		    lastObservation.setNextRecordExpectedTime(nextExpectedObservationDate);
		}
	    }

	    if (records.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).info("[DATA-CACHE] Augmented {} zero record", onlineId);
		dataCacheConnector.writeStatistics(new StatisticsRecord(new Date(), sourceId, onlineId, 0, null, expected, null));
	    } else {
		for (DataRecord record : records) {
		    Date date = record.getDate();
		    if (date.after(yesterday)) {
			dailyObservations++;
		    }
		}
		newRecords = new ArrayList<>(records);

		dataCacheConnector.write(records);

		if (current != null) {

		    Date newestDate = records.stream().map(DataRecord::getDate).max(Date::compareTo).get();

		    if (deferExpectedTimeClearing) {

			expectedTimeHandover = new ExpectedTimeHandover(onlineId, newestDate, List.of(current));

		    } else {

			// written after the new records, so that it is not written before them
			ExpectedTimeHandover.clear(List.of(current), dataCacheConnector);
		    }
		}

		StationRecord station = new StationRecord();
		station.setSourceIdentifier(sourceId);
		GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
		if (bbox != null) {
		    Double s = bbox.getSouth();
		    Double n = bbox.getNorth();
		    Double e = bbox.getEast();
		    Double w = bbox.getWest();
		    if (e == null) {
			e = w;
		    }
		    if (w == null) {
			w = e;
		    }
		    if (s == null) {
			s = n;
		    }
		    if (n == null) {
			n = s;
		    }
		    if (n != null && w != null) {
			BigDecimal south = new BigDecimal(s);
			BigDecimal north = new BigDecimal(n);
			BigDecimal west = new BigDecimal(w);
			BigDecimal east = new BigDecimal(e);
			station.setBbox4326(new BBOX4326(south, north, west, east));
			
		    }
		}
		Optional<String> platformIdentifier = resource.getExtensionHandler().getUniquePlatformIdentifier();
		if (platformIdentifier.isPresent()) {
		    station.setPlatformIdentifier(platformIdentifier.get());
		}
		MIPlatform platform = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getMIPlatform();
		if (platform != null) {
		    Citation citation = platform.getCitation();
		    if (citation != null) {
			String title = citation.getTitle();
			if (title != null) {
			    station.setPlatformName(title);
			}
		    }
		}

		station.setDataIdentifier(onlineId);
		station.setMetadataIdentifier(onlineId);
		station.setDatasetName(resource.getHarmonizedMetadata().getCoreMetadata().getTitle());
		if (!records.isEmpty()) {
		    station.setLastObservation(records.get(records.size() - 1).getDate());
		    station.setLastHarvesting(records.get(records.size() - 1).getTimestamp());
		    station.setWhosCategory("sensor");
		    station.setLastDayObservations(dailyObservations);
		    if (metadata.unitsURI() != null) {
			station.setUnitsURI(metadata.unitsURI());
		    }
		    if (metadata.units() != null) {
			station.setUnits(metadata.units());
		    }
		    if (metadata.observedPropertyURI() != null) {
			station.setObservedPropertyURI(metadata.observedPropertyURI());
		    }
		    station.setObservedProperty(metadata.observedProperty());
		    dataCacheConnector.writeStation(station);
		}

		int writtenCount = records.size();
		String fillError = null;

		if (backfill != null) {
		    writtenCount += backfill.values();
		    fillError = backfill.error();
		}

		// past data older than the first set of acquisitions
		if (fillBegin != null && fillBegin.before(begin)) {
		    FillResult fill = fillPast(dataCacheConnector, resource, onlineId, sourceId, metadata, fillBegin, begin, ms);
		    writtenCount += fill.values();
		    if (fill.error() != null) {
			fillError = fill.error();
		    }
		}

		GSLoggerFactory.getLogger(getClass()).info("[DATA-CACHE] Augmented {}", onlineId);
		dataCacheConnector.writeStatistics(
			new StatisticsRecord(new Date(), sourceId, onlineId, writtenCount, fillError, expected, lastObservationDate));
	    }

	    try {

	    } catch (Exception e1) {
		e1.printStackTrace();
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    if (dataCacheConnector != null) {
		try {
		    dataCacheConnector
			    .writeStatistics(new StatisticsRecord(new Date(), sourceId, onlineId, 0, e.getMessage(), expected, null));
		} catch (Exception e1) {
		    e1.printStackTrace();
		}
	    }

	    throw GSException.createException(getClass(), DATA_CACHE_AUGMENTER_CONNECTOR_FACTORY_ERROR, e);
	}

	return Optional.empty();
    }

    /**
     * @return the time resolution of the series of the given resource in ms (or its time support, if the resolution is
     *         missing), null if unknown
     */
    private Long getTimeResolution(GSResource resource, String onlineId) throws Exception {

	ExtensionHandler handler = resource.getExtensionHandler();

	Optional<String> timeResolution = Optional.empty();
	Optional<String> timeSupport = Optional.empty();

	try {
	    timeResolution = handler.getTimeResolution();
	    timeSupport = handler.getTimeSupport();
	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);

	    if (ex instanceof XPathException) {

		GSLoggerFactory.getLogger(getClass()).error("Resource with XPathException:\n {}", resource.asString(false));
	    }
	}

	String time = null;
	if (timeResolution.isPresent()) {
	    time = timeResolution.get();
	} else if (timeSupport.isPresent()) {
	    time = timeSupport.get();
	}
	Optional<String> timeUnits = handler.getTimeUnits();
	Long ms = null;
	if (timeUnits.isPresent() && time != null) {
	    Long duration = null;
	    try {
		duration = Long.parseLong(time.trim());
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).warn("[DATA-CACHE] Time is not long for source {} online id {} time {}",
			resource.getSource().getUniqueIdentifier(), onlineId, time);
	    }
	    String timeUnitsString = timeUnits.get().toLowerCase();
	    switch (timeUnitsString) {
	    case "hour":
	    case "hours":
	    case "h":
		ms = TimeUnit.HOURS.toMillis(duration);
		break;
	    case "ms":
	    case "milliseconds":
	    case "millisecond":
		ms = TimeUnit.MILLISECONDS.toMillis(duration);
		break;
	    case "d":
	    case "day":
	    case "days":
		ms = TimeUnit.DAYS.toMillis(duration);
		break;
	    case "minute":
	    case "minutes":
		ms = TimeUnit.MINUTES.toMillis(duration);
		break;
	    case "month":
	    case "months":
		ms = TimeUnit.DAYS.toMillis(30 * duration);
		break;
	    default:
		break;
	    }
	}
	return ms;
    }

    /**
     * @param resource
     * @param onlineId
     * @param end the end of the series
     * @param defaultBegin the begin of the first set of acquisitions
     * @return the begin of the data to load when the cache of the series is first filled (see {@link #setFillDays(Integer)})
     */
    private Date getFillBegin(GSResource resource, String onlineId, Date end, Date defaultBegin) {

	if (fillDays == null) {
	    return defaultBegin;
	}

	if (fillDays > 0) {
	    return new Date(end.getTime() - TimeUnit.DAYS.toMillis(fillDays));
	}

	TemporalExtent temporalExtent = resource.getHarmonizedMetadata().getCoreMetadata().getTemporalExtent();
	String beginPosition = temporalExtent == null ? null : temporalExtent.getBeginPosition();
	Optional<Date> begin = beginPosition == null || beginPosition.isBlank() ? Optional.empty()
		: ISO8601DateTimeUtils.parseISO8601ToDate(beginPosition);

	if (begin.isEmpty() || !begin.get().before(end)) {
	    GSLoggerFactory.getLogger(getClass()).warn("[DATA-CACHE] Invalid begin position {} of online id {}, loading the latest data only",
		    beginPosition, onlineId);
	    return defaultBegin;
	}

	return begin.get();
    }

    /**
     * The outcome of the load of the past data of a series
     */
    private record FillResult(int values, int chunks, int failedChunks) {

	/**
	 * @return the error to record in the statistics, null if all the chunks were loaded
	 */
	String error() {
	    return failedChunks == 0 ? null : "Past data load incomplete: " + failedChunks + " of " + chunks + " chunks failed";
	}
    }

    /**
     * Loads in the cache the data of the given period, in chunks from the most recent one. A failed chunk is logged and
     * skipped
     *
     * @param from the begin of the period (inclusive)
     * @param to the end of the period (exclusive)
     * @param resolution the time resolution of the series in ms, null if unknown
     */
    private FillResult fillPast(DataCacheConnector dataCacheConnector, GSResource resource, String onlineId, String sourceId,
	    SeriesMetadata metadata, Date from, Date to, Long resolution) throws Exception {

	AccessExecutor accessExecutor = ServiceLoader.load(AccessExecutor.class).iterator().next();

	long chunk = resolution != null ? Math.max(resolution * FILL_CHUNK_VALUES, TimeUnit.DAYS.toMillis(1)) : DEFAULT_FILL_CHUNK_MS;

	GSLoggerFactory.getLogger(getClass()).info("[DATA-CACHE] Loading past data of {} from {} to {}", onlineId,
		ISO8601DateTimeUtils.getISO8601DateTime(from), ISO8601DateTimeUtils.getISO8601DateTime(to));

	int values = 0;
	int chunks = 0;
	int failedChunks = 0;

	long chunkEnd = to.getTime();

	while (chunkEnd > from.getTime()) {

	    long chunkBegin = Math.max(from.getTime(), chunkEnd - chunk);
	    chunks++;

	    try {

		DataDescriptor descriptor = new DataDescriptor();
		descriptor.setDataFormat(DataFormat.WATERML_1_1());
		descriptor.setTemporalDimension(new Date(chunkBegin), new Date(chunkEnd));

		List<DataObject> results = accessExecutor.retrieve(resource, onlineId, descriptor).getResultsList();

		if (!results.isEmpty()) {

		    TimeSeriesType series = decode(results.get(0));
		    Double noDataValue = series.getVariable() != null ? series.getVariable().getNoDataValue() : null;

		    List<DataRecord> records = new ArrayList<>();
		    for (ValueSingleVariable wmlValue : series.getValues().get(0).getValue()) {
			Date date = getDate(wmlValue);
			// the chunk end is excluded, being already loaded by the following chunk
			if (date.getTime() >= chunkBegin && date.getTime() < chunkEnd) {
			    records.add(toRecord(wmlValue, date, noDataValue, onlineId, sourceId, metadata));
			}
		    }

		    if (!records.isEmpty()) {
			dataCacheConnector.write(records);
			values += records.size();
		    }
		}

	    } catch (Exception e) {

		failedChunks++;
		GSLoggerFactory.getLogger(getClass()).error("[DATA-CACHE] Error loading past data of {} from {} to {}: {}", onlineId,
			ISO8601DateTimeUtils.getISO8601DateTime(new Date(chunkBegin)),
			ISO8601DateTimeUtils.getISO8601DateTime(new Date(chunkEnd)), e.getMessage(), e);
	    }

	    while (dataCacheConnector.countRecordsInDataBuffer() > FILL_BUFFER_SIZE) {
		Thread.sleep(1000);
	    }

	    chunkEnd = chunkBegin;
	}

	GSLoggerFactory.getLogger(getClass()).info("[DATA-CACHE] Loaded past data of {}: {} values, {} chunks, {} failed", onlineId,
		values, chunks, failedChunks);

	return new FillResult(values, chunks, failedChunks);
    }

    /**
     * @param result a WaterML 1.1 data object, deleted after decoding
     * @return the first time series of the given data object
     */
    private TimeSeriesType decode(DataObject result) {

	File file = result.getFile();
	TimeSeriesResponseType trt = null;
	try (FileInputStream stream = new FileInputStream(file)) {
	    trt = JAXBWML.getInstance().parseTimeSeries(stream);
	} catch (Exception e) {
	    throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());
	} finally {
	    file.delete();
	}
	if (trt == null) {
	    throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());
	}
	return trt.getTimeSeries().get(0);
    }

    private static Date getDate(ValueSingleVariable wmlValue) {

	return wmlValue.getDateTimeUTC().toGregorianCalendar(TimeZone.getTimeZone("GMT"), null, null).getTime();
    }

    private static DataRecord toRecord(ValueSingleVariable wmlValue, Date date, Double noDataValue, String onlineId, String sourceId,
	    SeriesMetadata metadata) {

	BigDecimal value = wmlValue.getValue();
	// no data values are stored as null
	if (value != null && noDataValue != null && value.compareTo(BigDecimal.valueOf(noDataValue)) == 0) {
	    value = null;
	}
	DataRecord record = new DataRecord();
	record.setDate(date);
	record.setValue(value);
	record.setDataIdentifier(onlineId);
	record.setSourceIdentifier(sourceId);
	record.setUom(metadata.units());
	record.setObservedProperty(metadata.observedProperty());
	record.setObservedPropertyURI(metadata.observedPropertyURI());
	record.setLatitudeLongitude(metadata.latitudeLongitude());
	record.setQuality(wmlValue.getQualityControlLevelCode());
	return record;
    }

    /**
     * Metadata of a time series, written both in each value record and in the station record
     */
    private record SeriesMetadata(String units, String unitsURI, String observedProperty, String observedPropertyURI,
	    LatitudeLongitude latitudeLongitude) {
    }

    /**
     * @param resource
     * @return the metadata of the time series of the given resource
     * @throws Exception
     */
    private SeriesMetadata getSeriesMetadata(GSResource resource) throws Exception {

	Optional<String> unitsURI = resource.getExtensionHandler().getAttributeUnitsURI();
	String unitsLabel = null;
	if (unitsURI.isPresent()) {
	    WMOUnit units = WMOOntology.decodeUnit(unitsURI.get());
	    if (units != null) {
		unitsLabel = units.getPreferredLabel(null);
	    }
	}
	if (unitsLabel == null) {
	    Optional<String> units = resource.getExtensionHandler().getAttributeUnits();
	    if (units.isPresent()) {
		unitsLabel = units.get();
	    }
	}

	String observedProperty = null;
	Optional<String> observedPropertyURI = resource.getExtensionHandler().getObservedPropertyURI();
	if (observedPropertyURI.isPresent()) {
	    WHOSOntology ontology = new WHOSOntology();
	    SKOSConcept concept = ontology.getConcept(observedPropertyURI.get());
	    if (concept != null) {
		HashSet<String> closeMatches = concept.getCloseMatches();
		if (closeMatches != null && !closeMatches.isEmpty()) {
		    try {
			WMOOntology wmoOntology = new WMOOntology();
			for (String closeMatch : closeMatches) {
			    SKOSConcept variable = wmoOntology.getVariable(closeMatch);
			    if (variable != null) {
				String preferredLabel = variable.getPreferredLabel("");
				if (preferredLabel != null) {
				    observedProperty = preferredLabel;
				}
			    }
			}
		    } catch (Exception e) {
			e.printStackTrace();
		    }

		} else {
		    observedProperty = concept.getPreferredLabel("");
		}
	    }
	}
	if (observedProperty == null) {
	    CoverageDescription cd = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata().getCoverageDescription();
	    if (cd != null) {
		observedProperty = cd.getAttributeTitle();
	    }
	}

	// the center of the bounding box (usually a point, for stations)
	LatitudeLongitude latitudeLongitude = null;
	GeographicBoundingBox bbox = resource.getHarmonizedMetadata().getCoreMetadata().getBoundingBox();
	if (bbox != null) {
	    Double s = bbox.getSouth() != null ? bbox.getSouth() : bbox.getNorth();
	    Double n = bbox.getNorth() != null ? bbox.getNorth() : bbox.getSouth();
	    Double w = bbox.getWest() != null ? bbox.getWest() : bbox.getEast();
	    Double e = bbox.getEast() != null ? bbox.getEast() : bbox.getWest();
	    if (s != null && w != null) {
		latitudeLongitude = new LatitudeLongitude(BigDecimal.valueOf((s + n) / 2), BigDecimal.valueOf((w + e) / 2));
	    }
	}

	return new SeriesMetadata(unitsLabel, unitsURI.orElse(null), observedProperty, observedPropertyURI.orElse(null),
		latitudeLongitude);
    }

    /**
     * @param accessExecutor
     * @param resource
     * @param onlineId
     * @param targetDescriptor
     * @return
     */
    private ResultSet<DataObject> retrieveAndCatch(AccessExecutor accessExecutor, GSResource resource, String onlineId,
	    DataDescriptor targetDescriptor) {

	try {
	    return accessExecutor.retrieve(resource, onlineId, targetDescriptor);

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
	}

	return new ResultSet<>();
    }

    protected boolean checkObtainedRecords(List<DataRecord> cachedRecords) {
	return false;
    }

    @Override
    public String getType() {

	return "DataCacheAugmenter";
    }

    @Override
    protected DataCacheAugmenterSetting initSetting() {

	return new DataCacheAugmenterSetting();
    }

    @Override
    protected String initName() {

	return "Data cache augmenter";
    }
}
