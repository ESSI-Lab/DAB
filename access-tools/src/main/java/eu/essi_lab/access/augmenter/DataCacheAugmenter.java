package eu.essi_lab.access.augmenter;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
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
import eu.essi_lab.access.datacache.StationRecord;
import eu.essi_lab.access.datacache.StatisticsRecord;
import eu.essi_lab.augmenter.ResourceAugmenter;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.iso.datamodel.classes.Citation;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.mqtt.hive.MQTTPublisherHive;
import eu.essi_lab.lib.net.utils.whos.SKOSConcept;
import eu.essi_lab.lib.net.utils.whos.WHOSOntology;
import eu.essi_lab.lib.net.utils.whos.WMOOntology;
import eu.essi_lab.lib.net.utils.whos.WMOUnit;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.whos.MQTTUtils;
import eu.essi_lab.lib.whos.WIS2Level10Topic;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Country;
import eu.essi_lab.model.resource.ExtensionHandler;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.data.DataDescriptor;
import eu.essi_lab.model.resource.data.DataFormat;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.request.executor.IAccessExecutor;
import net.opengis.gml.v_3_2_0.TimeIndeterminateValueType;

/**
 * @author boldrini
 * @author Fabrizio
 */
public class DataCacheAugmenter extends ResourceAugmenter<DataCacheAugmenterSetting> {

    private static MQTTPublisherHive client;

    /**
     * 
     */
    private static final String DATA_CACHE_AUGMENTER_CONNECTOR_FACTORY_ERROR = "DATA_CACHE_AUGMENTER_CONNECTOR_FACTORY_ERROR";
    private static final int MAX_RETRY = 10;

    @Override
    public Optional<GSResource> augment(GSResource resource) throws GSException {

	if (client == null) {
	    try {

		SystemSetting systemSettings = ConfigurationWrapper.getSystemSettings();

		Optional<Properties> keyValueOption = systemSettings.getKeyValueOptions();

		if (keyValueOption.isPresent()) {

		    String host = keyValueOption.get().getProperty("mqttBrokerHost");
		    String port = keyValueOption.get().getProperty("mqttBrokerPort");
		    String user = keyValueOption.get().getProperty("mqttBrokerUser");
		    String pwd = keyValueOption.get().getProperty("mqttBrokerPwd");

		    if (host == null || port == null || user == null || pwd == null) {

			GSLoggerFactory.getLogger(getClass()).error("MQTT options not found!");

		    } else {

			client = new MQTTPublisherHive(host, Integer.valueOf(port), user, pwd);
		    }
		} else {

		    GSLoggerFactory.getLogger(getClass()).error("Key-value pair options not found!");

		}

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(DataCacheAugmenter.class).error(e);
		throw GSException.createException(getClass(), "MQTTClientInitError", e);
	    }
	}

	DataCacheConnector dataCacheConnector = null;
	String sourceId = null;
	String sourceAcronym = null;
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
	    sourceAcronym = sourceId;

	    DataRecord toUpdate = null;
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
		expected = cachedRecord.getNextRecordExpectedTime();
		if (expected != null) {
		    cachedRecord.setNextRecordExpectedTime(null);
		    toUpdate = cachedRecord;
		}
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
	    if (begin == null) {
		// calculating begin date for the first set of acquisitions
		if (ms != null) {
		    begin = new Date(end.getTime() - ms * 20);
		} else {
		    begin = new Date(end.getTime() - TimeUnit.DAYS.toMillis(30));
		}
	    }
	    targetDescriptor.setTemporalDimension(begin, end);
	    ServiceLoader<IAccessExecutor> accessLoader = ServiceLoader.load(IAccessExecutor.class);
	    IAccessExecutor accessExecutor = accessLoader.iterator().next();

	    ResultSet<DataObject> retrieved = retrieveAndCatch(accessExecutor, resource, onlineId, targetDescriptor);

	    List<DataObject> results = retrieved.getResultsList();
	    List<DataRecord> records = new ArrayList<>();

	    boolean effectiveCheck = false;
	    int attempts = 1;

	    while (results.isEmpty() && attempts < MAX_RETRY) {

		GSLoggerFactory.getLogger(getClass()).warn(
			"[DATA-CACHE] Waiting source {} online id {} every 5 minutes for results from: {} now: {}",
			resource.getSource().getUniqueIdentifier(), onlineId, time, ISO8601DateTimeUtils.getISO8601DateTime(end),
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

	    DataObject result = results.get(0);
	    File file = result.getFile();
	    TimeSeriesResponseType trt = null;
	    try {
		FileInputStream stream = new FileInputStream(file);
		trt = JAXBWML.getInstance().parseTimeSeries(stream);
		stream.close();
		file.delete();
	    } catch (Exception e) {
		throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());
	    }
	    if (trt == null) {
		throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());
	    }
	    TimeSeriesType series = trt.getTimeSeries().get(0);
	    List<ValueSingleVariable> values = series.getValues().get(0).getValue();
	    for (ValueSingleVariable wmlValue : values) {
		Date date = wmlValue.getDateTimeUTC().toGregorianCalendar(TimeZone.getTimeZone("GMT"), null, null).getTime();
		if (lastObservation == null || !date.equals(lastObservation.getDate())) {
		    BigDecimal value = wmlValue.getValue();
		    DataRecord record = new DataRecord();
		    record.setDate(date);
		    record.setValue(value);
		    record.setDataIdentifier(onlineId);
		    record.setSourceIdentifier(sourceId);
		    records.add(record);

		    ////////////////////////////////
		    // MQTT publishing
		    ////////////////////////////////
		    String country = "unknown";
		    VariableInfoType variableInfo = series.getVariable();
		    String variableURI = JAXBWML.getInstance().getVariableURI(variableInfo);
		    String variable = variableInfo.getVariableName();

		    JSONObject json = new JSONObject();
		    String id = onlineId + "-" + date.getTime();
		    json.put("id", id);
		    json.put("type", "Feature");
		    json.put("version", "v04");

		    SourceInfoType sourceInfo = series.getSourceInfo();
		    String siteId = "unknown";
		    if (sourceInfo instanceof SiteInfoType) {
			SiteInfoType siteInfo = (SiteInfoType) sourceInfo;

			if (sourceId.contains("-")) {
			    int dashIndex = sourceId.indexOf("-");
			    String c = sourceId.substring(0, dashIndex);
			    Country cc = Country.decode(c);
			    if (cc != null) {
				country = cc.getISO2().toLowerCase();
				sourceAcronym = sourceId.substring(dashIndex + 1);
			    }
			}

			if (country.equals("unknown")) {
			    try {
				country = JAXBWML.getInstance().getProperty(siteInfo, WML_SiteProperty.COUNTRY);
				if (country != null && !country.isEmpty()) {
				    country = Country.decode(country).getISO2().toLowerCase();
				} else {
				    country = "unknown";
				}
			    } catch (Exception ex) {
				GSLoggerFactory.getLogger(getClass()).error(ex);
			    }
			}

			siteId = siteInfo.getSiteCode().get(0).getValue();
			GeoLocation geoLocation = siteInfo.getGeoLocation();
			double lon;
			double lat;
			if (geoLocation != null) {
			    GeogLocationType geog = geoLocation.getGeogLocation();
			    if (geog != null) {
				if (geog instanceof LatLonPointType) {
				    LatLonPointType latLon = (LatLonPointType) geog;
				    lon = latLon.getLongitude();
				    lat = latLon.getLatitude();
				    JSONArray coordinates = new JSONArray();
				    coordinates.put(lon);
				    coordinates.put(lat);
				    Double alt = siteInfo.getElevationM();
				    if (alt != null) {
					coordinates.put(alt);
				    }
				    JSONObject geometry = new JSONObject();
				    geometry.put("type", "Point");
				    geometry.put("coordinates", coordinates);
				    json.put("geometry", geometry);
				}
			    }
			}
		    }

		    String level11 = variable;
		    WIS2Level10Topic wisLevel10 = WIS2Level10Topic.decode(variableURI);
		    String level10 = wisLevel10.getId();
		    String level9 = wisLevel10.getBroaderLevel().getId();

		    String topic = "origin/a/wis2/" + country + "-" + sourceAcronym + "/data/core/hydrology/"
			    + MQTTUtils.harmonizeTopicName(level9) + "/" + MQTTUtils.harmonizeTopicName(level10) + "/"
			    + MQTTUtils.harmonizeTopicName(level11);
		    JSONObject properties = new JSONObject();
		    properties.put("data_id", topic + "/" + id);
		    properties.put("datetime", ISO8601DateTimeUtils.getISO8601DateTime(date));
		    properties.put("pubtime", ISO8601DateTimeUtils.getISO8601DateTime(new Date()));

		    properties.put("value", record.getValue());
		    properties.put("observedProperty", variable.trim());
		    UnitsType units = series.getVariable().getUnit();
		    if (units != null) {
			String unitName = units.getUnitName();
			properties.put("uom", unitName);
		    }
		    String dataType = series.getVariable().getDataType();
		    if (dataType != null) {
			properties.put("interpolation", dataType);
		    }

		    String wigosId = "021016basin" + siteId;
		    properties.put("wigos_station_identifier", wigosId);

		    json.put("properties", properties);
		    JSONArray linksArray = new JSONArray();
		    JSONObject link = new JSONObject();
		    link.put("rel", "canonical");
		    link.put("type", "application/netcdf");
		    String variableCode = series.getVariable().getVariableCode().get(1).getValue();
		    String d = ISO8601DateTimeUtils.getISO8601DateTime(date).replace("Z", "");
		    link.put("href",
			    "https://whos.geodab.eu/gs-service/services/essi/token/TOKEN/view/gs-view-and(whos,gs-view-source(" + sourceId
				    + "))/cuahsi_1_1.asmx?request=GetValuesObject&site=" + siteId + "&variable=" + variableCode
				    + "&beginDate=" + d + "&endDate=" + d + "&format=NetCDF");
		    linksArray.put(link);
		    json.put("links", linksArray);
		    String msg = json.toString();
		    if (client == null) {
			GSLoggerFactory.getLogger(getClass()).info("MQTT broker not configured");
		    } else {
			client.publish(topic, msg, true);
		    }

		    ////////////////////////////////
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
		if (toUpdate != null) {
		    records.add(0, toUpdate);
		}

		dataCacheConnector.write(records);

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
			if (areEquals(s, n) && areEquals(w, e)) {
			    station.setLatitudeLongitude(new SimpleEntry<BigDecimal, BigDecimal>(south, west));
			}
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
		    Optional<String> unitsURI = resource.getExtensionHandler().getAttributeUnitsURI();
		    String unitsLabel = null;
		    if (unitsURI.isPresent()) {
			station.setUnitsURI(unitsURI.get());
			WMOUnit units = WMOOntology.decodeUnit(unitsURI.get());
			if (units != null) {
			    unitsLabel = units.getPreferredLabel().getKey();
			}
		    }
		    if (unitsLabel == null) {
			Optional<String> units = resource.getExtensionHandler().getAttributeUnits();
			if (units.isPresent()) {
			    unitsLabel = units.get();
			}
		    }
		    if (unitsLabel != null) {
			station.setUnits(unitsLabel);
		    }
		    String observedProperty = null;
		    Optional<String> observedPropertyURI = resource.getExtensionHandler().getObservedPropertyURI();
		    if (observedPropertyURI.isPresent()) {
			station.setObservedPropertyURI(observedPropertyURI.get());
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
					    SimpleEntry<String, String> preferredLabel = variable.getPreferredLabel();
					    if (preferredLabel != null) {
						observedProperty = preferredLabel.getKey();
					    }
					}
				    }
				} catch (Exception e) {
				    e.printStackTrace();
				}

			    } else {
				observedProperty = concept.getPreferredLabel().getKey();
			    }
			}
		    }
		    if (observedProperty == null) {
			CoverageDescription cd = resource.getHarmonizedMetadata().getCoreMetadata().getMIMetadata()
				.getCoverageDescription();
			if (cd != null) {
			    String attribute = cd.getAttributeTitle();
			    observedProperty = attribute;
			}
		    }
		    station.setObservedProperty(observedProperty);
		    dataCacheConnector.writeStation(station);
		}

		GSLoggerFactory.getLogger(getClass()).info("[DATA-CACHE] Augmented {}", onlineId);
		dataCacheConnector.writeStatistics(
			new StatisticsRecord(new Date(), sourceId, onlineId, records.size(), null, expected, lastObservationDate));
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

    private boolean areEquals(double s, double n) {
	return Math.abs(s - n) < 0.0000001d;
    }

    /**
     * @param accessExecutor
     * @param resource
     * @param onlineId
     * @param targetDescriptor
     * @return
     */
    private ResultSet<DataObject> retrieveAndCatch(IAccessExecutor accessExecutor, GSResource resource, String onlineId,
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
