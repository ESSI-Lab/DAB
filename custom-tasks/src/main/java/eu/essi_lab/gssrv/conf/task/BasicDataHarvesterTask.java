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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.JobExecutionContext;

import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory;
import eu.essi_lab.access.datacache.DataRecord;
import eu.essi_lab.access.datacache.LatitudeLongitude;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.OptionsKey;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.FileUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLFactories;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.profiler.om.OMHandler;

/**
 * Harvests all the time series of a given view (optionally restricted to a source and/or a single observation) through
 * the local OM-API and writes them, downsampled to at most one value per hour, into the data cache.
 *
 * @author boldrini
 */
public class BasicDataHarvesterTask extends AbstractCustomTask {

    public enum DataHarvesterTaskOptions implements OptionsKey {
	SOURCE_ID, MAX_RECORDS, VIEW_ID, TOKEN, OBSERVATION_ID,
	/**
	 * Comma separated list of observed property URIs. If present, only observations having one of these observed
	 * property URIs are harvested
	 */
	OBSERVED_PROPERTY_URIS;
    }

    private static final String OM_API_BASE = "http://localhost:9090/gs-service/services/essi/token/";

    /**
     * Writing is paused while the data cache buffer holds more than this number of records
     */
    private static final int WAITING_BUFFER_SIZE = 20000;

    /**
     * Max number of attempts for a single listing page
     */
    private static final int MAX_LIST_ATTEMPTS = 5;

    private static final long LIST_RETRY_DELAY_MS = 10000;

    /**
     * Max time to wait at the end of the task for the data cache buffer to be written
     */
    private static final long MAX_DRAIN_WAIT_MS = 30 * 60 * 1000;

    private static final long HOUR_MS = 60 * 60 * 1000;

    private static final int STATS_LOG_INTERVAL = 100;

    /**
     * Max number of characters of a non-decodable response reported in the log
     */
    private static final int MAX_LOGGED_CONTENT = 1000;

    private static final XMLInputFactory factory = XMLFactories.newXMLInputFactory();

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Basic Data harvester task STARTED");

	// SETTINGS RETRIEVAL

	Optional<EnumMap<DataHarvesterTaskOptions, String>> taskOptions = readTaskOptions(context, DataHarvesterTaskOptions.class);
	if (taskOptions.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).error("No options specified");
	    return;
	}

	String viewId = taskOptions.get().get(DataHarvesterTaskOptions.VIEW_ID);
	if (viewId == null) {
	    GSLoggerFactory.getLogger(getClass()).error("No view id option specified");
	    return;
	}

	String token = taskOptions.get().get(DataHarvesterTaskOptions.TOKEN);
	if (token == null) {
	    GSLoggerFactory.getLogger(getClass()).error("No token option specified");
	    return;
	}

	String sourceId = taskOptions.get().get(DataHarvesterTaskOptions.SOURCE_ID);

	String maxRecordsString = taskOptions.get().get(DataHarvesterTaskOptions.MAX_RECORDS);
	Integer maxRecords = null;
	if (maxRecordsString != null) {
	    maxRecords = Integer.parseInt(maxRecordsString);
	}

	String observationId = taskOptions.get().get(DataHarvesterTaskOptions.OBSERVATION_ID);

	Set<String> observedPropertyURIs = parseList(taskOptions.get().get(DataHarvesterTaskOptions.OBSERVED_PROPERTY_URIS));
	if (!observedPropertyURIs.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).info("Harvesting only observed property URIs: {}", observedPropertyURIs);
	}

	// INIT CACHE CONNECTOR

	DataCacheConnectorSetting setting = ConfigurationWrapper.getDataCacheConnectorSetting();
	DataCacheConnector dataCacheConnector = DataCacheConnectorFactory.newDataCacheConnector(setting);
	if (dataCacheConnector == null) {
	    GSLoggerFactory.getLogger(getClass()).error("Issues initializing the data cache connector");
	    return;
	}
	dataCacheConnector.configure(DataCacheConnector.MAX_BULK_SIZE, "10000"); // note: 100000 is too much, it returns
	// 413
	dataCacheConnector.configure(DataCacheConnector.FLUSH_INTERVAL_MS, "2000");
	dataCacheConnector.configure(DataCacheConnector.CACHED_DAYS, "0");

	// each run has its own temp folder, so that concurrent runs do not interfere
	File tempDirFile = FileUtils.getTempDir("basic-data-harvester-" + UUID.randomUUID(), true);

	HarvestStats stats = new HarvestStats();
	boolean canceled = false;

	try {

	    canceled = harvest(context, dataCacheConnector, tempDirFile, token, viewId, sourceId, observationId, observedPropertyURIs,
		    maxRecords, stats);

	} finally {

	    drainAndClose(dataCacheConnector);

	    try {
		FileUtils.clearFolder(tempDirFile, true);
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).warn("Unable to remove temp folder {}", tempDirFile, e);
	    }
	}

	String summary = "Source id " + sourceId + ". " + stats;

	if (canceled) {

	    status.setPhase(JobPhase.CANCELED);
	    log(status, "Basic Data harvester task CANCELED. " + summary);

	} else {

	    log(status, "Basic Data harvester task ENDED. " + summary);
	}
    }

    /**
     * @return true if the job has been canceled
     */
    private boolean harvest(JobExecutionContext context, DataCacheConnector dataCacheConnector, File tempDirFile, String token,
	    String viewId, String sourceId, String observationId, Set<String> observedPropertyURIs, Integer maxRecords,
	    HarvestStats stats) throws Exception {

	OMHandler omHandler = new OMHandler();

	String listURL = OM_API_BASE + token + "/view/" + viewId + "/om-api/observations?expandFeatures=true&";

	if (sourceId != null) {
	    listURL = listURL + "provider=" + sourceId + "&";
	}

	if (observationId != null) {
	    listURL = listURL + "observationIdentifier=" + observationId + "&";
	}

	boolean firstLoop = true;
	String resumptionToken = null;
	int blocks = 0;

	while (firstLoop || resumptionToken != null) {

	    firstLoop = false;

	    if (ConfigurationWrapper.isJobCanceled(context)) {
		return true;
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Listing block {}", ++blocks);

	    String currentListURL = listURL;
	    if (resumptionToken != null) {
		currentListURL = currentListURL + "resumptionToken=" + resumptionToken;
	    }

	    Optional<JSONArray> optMembers = Optional.empty();

	    for (int attempt = 1; attempt <= MAX_LIST_ATTEMPTS; attempt++) {

		Optional<JSONObject> response = omHandler.getJSONResponse(WebRequest.createGET(currentListURL));

		if (response.isPresent() && response.get().has("member")) {

		    JSONObject json = response.get();

		    if (json.has("completed") && !json.getBoolean("completed") && json.has("resumptionToken")) {
			resumptionToken = json.getString("resumptionToken");
		    } else {
			resumptionToken = null;
		    }

		    optMembers = Optional.of(json.getJSONArray("member"));
		    break;
		}

		GSLoggerFactory.getLogger(getClass()).error("Listing block {} failed (attempt {}/{}): {}", blocks, attempt,
			MAX_LIST_ATTEMPTS, response.map(JSONObject::toString).orElse("response not present"));

		if (attempt < MAX_LIST_ATTEMPTS) {

		    Thread.sleep(LIST_RETRY_DELAY_MS * attempt);

		    if (ConfigurationWrapper.isJobCanceled(context)) {
			return true;
		    }
		}
	    }

	    if (optMembers.isEmpty()) {

		stats.majorErrors++;
		GSLoggerFactory.getLogger(getClass()).error("Listing block {} failed after {} attempts, stopping", blocks,
			MAX_LIST_ATTEMPTS);
		return false;
	    }

	    JSONArray members = optMembers.get();

	    GSLoggerFactory.getLogger(getClass()).info("Retrieved block of size {}, resumption token {}", members.length(),
		    resumptionToken);

	    if (members.length() == 0) {
		return false;
	    }

	    // for each observation
	    for (int i = 0; i < members.length(); i++) {

		stats.totalRecords++;

		JSONObject member = members.getJSONObject(i);

		if (!observedPropertyURIs.isEmpty() && !observedPropertyURIs.contains(getObservedPropertyURI(member))) {
		    stats.skipped++;
		    continue;
		}

		harvestObservation(omHandler, dataCacheConnector, tempDirFile, token, viewId, sourceId, member, stats);

		if (maxRecords != null && stats.recordsDone >= maxRecords) {
		    return false;
		}

		int bufferSize;
		while ((bufferSize = dataCacheConnector.countRecordsInDataBuffer()) > WAITING_BUFFER_SIZE) {
		    GSLoggerFactory.getLogger(getClass()).info("Waiting writing data, buffer size: {}", bufferSize);

		    Thread.sleep(1000);
		}

		if (stats.totalRecords % STATS_LOG_INTERVAL == 0) {
		    GSLoggerFactory.getLogger(getClass()).info("Basic data harvester stats. Source id {}. {}", sourceId, stats);
		}

		// CHECKING CANCELED JOB

		if (ConfigurationWrapper.isJobCanceled(context)) {
		    return true;
		}
	    }
	}

	return false;
    }

    private void harvestObservation(OMHandler omHandler, DataCacheConnector dataCacheConnector, File tempDirFile, String token,
	    String viewId, String sourceId, JSONObject member, HarvestStats stats) {

	String id = member.getString("id");

	String uom = null;
	String observedProperty = null;
	String observedPropertyURI = null;
	String mySourceId = null;
	LatitudeLongitude latitudeLongitude = null;

	JSONObject result = member.optJSONObject("result");
	if (result != null) {
	    JSONObject dpm = result.optJSONObject("defaultPointMetadata");
	    if (dpm != null) {
		uom = dpm.optString("uom", null);
	    }
	}
	JSONArray parameter = member.optJSONArray("parameter");
	if (parameter != null) {
	    for (int j = 0; j < parameter.length(); j++) {
		JSONObject p = parameter.optJSONObject(j);
		if (p != null && "sourceId".equals(p.optString("name", null))) {
		    mySourceId = p.optString("value", null);
		}
	    }
	}
	if (mySourceId == null) {
	    mySourceId = sourceId;
	}
	JSONObject observedPropertyObject = member.optJSONObject("observedProperty");
	if (observedPropertyObject != null) {
	    observedProperty = observedPropertyObject.optString("title", null);
	    observedPropertyURI = getObservedPropertyURI(member);
	}
	JSONObject feature = member.optJSONObject("featureOfInterest");
	if (feature != null) {
	    JSONObject shape = feature.optJSONObject("shape");
	    if (shape != null) {
		JSONArray coords = shape.optJSONArray("coordinates");
		if (coords != null) {
		    BigDecimal lon = coords.optBigDecimal(0, null);
		    BigDecimal lat = coords.optBigDecimal(1, null);
		    if (lon != null && lat != null) {
			latitudeLongitude = new LatitudeLongitude(lat, lon);
		    }
		}
	    }
	}

	// download single observation URL
	String downloadURL = OM_API_BASE + token + "/view/" + viewId
		+ "/om-api/observations?includeData=true&format=WML1&observationIdentifier=" + id;

	File dataFile = new File(tempDirFile, "FILE-" + UUID.randomUUID());

	try {

	    try (FileOutputStream output = new FileOutputStream(dataFile)) {
		omHandler.handle(output, WebRequest.createGET(downloadURL));
	    }

	    long sizeInBytes = dataFile.length();

	    GSLoggerFactory.getLogger(getClass()).info("Downloaded observation {}. Size: {} bytes", id, sizeInBytes);

	    if (sizeInBytes == 0) {

		GSLoggerFactory.getLogger(getClass()).error("Empty response for observation {}", id);
		stats.errors++;
		return;
	    }

	    if (!isXML(dataFile)) {

		GSLoggerFactory.getLogger(getClass()).error("Unexpected response for observation {}: {}", id, readHead(dataFile));
		stats.errors++;
		return;
	    }

	    int written = addPointsFromWML(dataFile, dataCacheConnector, uom, observedProperty, observedPropertyURI, latitudeLongitude, id, mySourceId,
		    stats);

	    GSLoggerFactory.getLogger(getClass()).info("Observation {}: {} values sent to the data cache", id, written);

	    stats.recordsDone++;

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error harvesting observation {}: {}", id, e.getMessage(), e);
	    stats.errors++;

	} finally {

	    dataFile.delete();
	}
    }

    /**
     * Waits for the data cache buffer to be written, then closes the connector
     */
    private void drainAndClose(DataCacheConnector dataCacheConnector) {

	long start = System.currentTimeMillis();

	try {

	    int bufferSize;
	    while ((bufferSize = dataCacheConnector.countRecordsInDataBuffer()) > 0) {

		if (System.currentTimeMillis() - start > MAX_DRAIN_WAIT_MS) {
		    GSLoggerFactory.getLogger(getClass()).error("Timeout waiting for the data cache buffer to be written, {} records lost",
			    bufferSize);
		    break;
		}

		GSLoggerFactory.getLogger(getClass()).info("Waiting final data writing, buffer size: {}", bufferSize);
		Thread.sleep(1000);
	    }

	} catch (InterruptedException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Interrupted while waiting for the data cache buffer to be written");
	    Thread.currentThread().interrupt();
	}

	try {

	    dataCacheConnector.close();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error("Error closing the data cache connector", e);
	}
    }

    @Override
    public String getName() {

	return "Basic Data harvester task";
    }

    private static boolean isXML(File file) throws IOException {

	try (InputStream stream = new FileInputStream(file)) {
	    int c;
	    while ((c = stream.read()) != -1) {
		if (!Character.isWhitespace(c) && c != 0xEF && c != 0xBB && c != 0xBF) { // skips also UTF-8 BOM
		    return c == '<';
		}
	    }
	}
	return false;
    }

    private static String readHead(File file) {

	try (InputStream stream = new FileInputStream(file)) {
	    byte[] bytes = stream.readNBytes(MAX_LOGGED_CONTENT);
	    return new String(bytes, StandardCharsets.UTF_8);
	} catch (IOException e) {
	    return "unreadable: " + e.getMessage();
	}
    }

    private static String readValue(XMLEventReader reader) throws XMLStreamException {

	StringBuilder ret = new StringBuilder();
	XMLEvent event = null;
	do {
	    event = reader.nextEvent();
	    if (event instanceof Characters) {
		ret.append(((Characters) event).getData());
	    }
	} while (event != null && !event.isEndElement());

	return ret.toString().trim();
    }

    private static Optional<BigDecimal> parseNumber(String value) {

	if (value == null || value.isEmpty()) {
	    return Optional.empty();
	}
	try {
	    return Optional.of(new BigDecimal(value));
	} catch (NumberFormatException e) {
	    return Optional.empty();
	}
    }

    /**
     * Parses the given WML 1 file and sends to the cache at most one value per (UTC) hour, the earliest one of each
     * hour, so that the result does not depend on the order of the values in the file.
     *
     * @return the number of records sent to the cache
     */
    private int addPointsFromWML(File file, DataCacheConnector cache, String uom, String observedProperty, String observedPropertyURI,
	    LatitudeLongitude latitudeLongitude, String dataIdentifier, String sourceId, HarvestStats stats) throws Exception {

	GSLoggerFactory.getLogger(getClass()).debug("Sending file {} to OS, identifier: {}", file.getAbsolutePath(), dataIdentifier);

	// hour index -> earliest record of that hour
	TreeMap<Long, DataRecord> hourlyRecords = new TreeMap<>();

	try (InputStream stream = new FileInputStream(file)) {

	    XMLEventReader reader = factory.createXMLEventReader(new StreamSource(stream));

	    try {

		String nodataString = null;
		BigDecimal nodataValue = null;

		while (reader.hasNext()) {

		    XMLEvent event = reader.nextEvent();

		    if (!event.isStartElement()) {
			continue;
		    }

		    StartElement startElement = event.asStartElement();

		    switch (startElement.getName().getLocalPart()) {
		    case "noDataValue":
			nodataString = readValue(reader);
			nodataValue = parseNumber(nodataString).orElse(null);
			break;
		    case "value":

			Attribute dateTimeAttribute = startElement.getAttributeByName(new QName("dateTimeUTC"));
			if (dateTimeAttribute == null) {
			    dateTimeAttribute = startElement.getAttributeByName(new QName("dateTime"));
			}
			if (dateTimeAttribute == null) {
			    break;
			}

			String value = readValue(reader);

			Optional<Date> d = ISO8601DateTimeUtils.parseISO8601ToDate(dateTimeAttribute.getValue());
			if (d.isEmpty()) {
			    stats.invalidValues++;
			    break;
			}

			BigDecimal v = null;
			if (!value.equals(nodataString)) {
			    Optional<BigDecimal> parsed = parseNumber(value);
			    if (parsed.isEmpty()) {
				stats.invalidValues++;
				break;
			    }
			    if (nodataValue == null || parsed.get().compareTo(nodataValue) != 0) {
				v = parsed.get();
			    }
			}

			long time = d.get().getTime();
			long hour = Math.floorDiv(time, HOUR_MS);

			DataRecord existing = hourlyRecords.get(hour);
			if (existing != null && existing.getDate().getTime() <= time) {
			    // an earlier value of the same hour is already present. this is to skip sub hours data
			    break;
			}

			DataRecord dataRecord = new DataRecord(d.get(), v, uom, observedProperty, latitudeLongitude, dataIdentifier);
			dataRecord.setObservedPropertyURI(observedPropertyURI);
			Attribute qualityAttribute = startElement.getAttributeByName(new QName("qualityControlLevelCode"));
			if (qualityAttribute != null) {
			    dataRecord.setQuality(qualityAttribute.getValue());
			}
			dataRecord.setSourceIdentifier(sourceId);

			hourlyRecords.put(hour, dataRecord);
			break;
		    default:
			break;
		    }
		}

	    } finally {

		reader.close();
	    }

	} catch (XMLStreamException e) {

	    throw new Exception("WML decoding error: " + e.getMessage() + ". Content head: " + readHead(file), e);
	}

	for (DataRecord dataRecord : hourlyRecords.values()) {
	    cache.write(dataRecord);
	}

	return hourlyRecords.size();
    }

    /**
     * @return the observed property URI of the given OM observation, or null if missing
     */
    private static String getObservedPropertyURI(JSONObject member) {

	JSONObject observedPropertyObject = member.optJSONObject("observedProperty");
	if (observedPropertyObject == null) {
	    return null;
	}
	String uri = observedPropertyObject.optString("href", null);
	if (uri == null || uri.isBlank()) {
	    return null;
	}
	return uri.trim();
    }

    /**
     * @return the non empty, trimmed items of the given comma separated list
     */
    private static Set<String> parseList(String list) {

	Set<String> ret = new HashSet<>();
	if (list != null) {
	    for (String item : list.split(",")) {
		if (!item.isBlank()) {
		    ret.add(item.trim());
		}
	    }
	}
	return ret;
    }

    private static class HarvestStats {

	int totalRecords;
	int recordsDone;
	int errors;
	int majorErrors;
	int invalidValues;
	int skipped;

	@Override
	public String toString() {

	    return "Records seen: " + totalRecords + " Records inserted: " + recordsDone + " Errors: " + errors + " Major errors: "
		    + majorErrors + " Invalid values skipped: " + invalidValues
		    + " Records skipped (observed property filter): " + skipped;
	}
    }
}
