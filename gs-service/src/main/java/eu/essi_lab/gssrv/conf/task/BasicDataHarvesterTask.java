package eu.essi_lab.gssrv.conf.task;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

import eu.essi_lab.access.DataValidatorErrorCode;
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
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.profiler.om.OMHandler;

/**
 * @author boldrini
 */
public class BasicDataHarvesterTask extends AbstractCustomTask {

    public enum DataHarvesterTaskOptions implements OptionsKey {
	SOURCE_ID, THREADS_COUNT, MAX_RECORDS, VIEW_ID, TOKEN, OBSERVATION_ID;
    }

    private static final int WAITING_BUFFER_SIZE = 20000;

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Basic Data harvester task STARTED");

	// INIT CACHE CONNECTOR

	DataCacheConnector dataCacheConnector = null;

	DataCacheConnectorSetting setting = ConfigurationWrapper.getDataCacheConnectorSetting();
	dataCacheConnector = DataCacheConnectorFactory.newDataCacheConnector(setting);
	if (dataCacheConnector == null) {
	    GSLoggerFactory.getLogger(getClass()).error("Issues initializing the data cache connector");
	    return;
	}
	dataCacheConnector.configure(DataCacheConnector.MAX_BULK_SIZE, "10000"); // note: 100000 is too much, it returns
										 // 413
	dataCacheConnector.configure(DataCacheConnector.FLUSH_INTERVAL_MS, "2000");
	dataCacheConnector.configure(DataCacheConnector.CACHED_DAYS, "0");

	// SETTINGS RETRIEVAL

	Optional<EnumMap<DataHarvesterTaskOptions, String>> taskOptions = readTaskOptions(context, DataHarvesterTaskOptions.class);
	if (taskOptions.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).error("No options specified");
	    return;
	}
	String sourceId = taskOptions.get().get(DataHarvesterTaskOptions.SOURCE_ID);

	String viewId = taskOptions.get().get(DataHarvesterTaskOptions.VIEW_ID);
	if (viewId == null && sourceId == null) {
	    GSLoggerFactory.getLogger(getClass()).error("No view id option specified");
	    return;
	}

	String token = taskOptions.get().get(DataHarvesterTaskOptions.TOKEN);
	if (token == null) {
	    GSLoggerFactory.getLogger(getClass()).error("No token option specified");
	    return;
	}
	String threadsCountString = taskOptions.get().get(DataHarvesterTaskOptions.THREADS_COUNT);
	Integer threadsCount = 1;
	if (threadsCountString != null) {
	    threadsCount = Integer.parseInt(threadsCountString);
	}

	String maxRecordsString = taskOptions.get().get(DataHarvesterTaskOptions.MAX_RECORDS);
	Integer maxRecords = null;
	if (maxRecordsString != null) {
	    maxRecords = Integer.parseInt(maxRecordsString);
	}

	String observationId = taskOptions.get().get(DataHarvesterTaskOptions.OBSERVATION_ID);

	int totalRecords = 0;
	int recordsDone = 0;
	int errors = 0;
	int majorErrors = 0;

	OMHandler omHandler = new OMHandler();

	String listURL = "http://localhost:9090/gs-service/services/essi/token/" + token + "/view/" + viewId
		+ "/om-api/observations?expandFeatures=true&";

	if (sourceId != null) {
	    listURL = listURL + "provider=" + sourceId + "&";
	}

	if (observationId != null) {
	    listURL = listURL + "observationIdentifier=" + observationId + "&";
	}

	boolean firstLoop = true;
	String resumptionToken = null;
	int blocks = 0;

	Path tempPath = null;

	File tempDirFile = FileUtils.createTempDir("basic-data-harvester", false);

	tempPath = tempDirFile.toPath();

	if (tempDirFile.exists()) {

	    FileUtils.clearFolder(tempPath.toFile(), false);

	} else {

	    tempDirFile.mkdirs();
	}

	base: while (firstLoop || resumptionToken != null) {

	    firstLoop = false;

	    GSLoggerFactory.getLogger(getClass()).info("Listing block {}", ++blocks);

	    String currentListURL = listURL;
	    if (resumptionToken != null) {
		currentListURL = currentListURL + "resumptionToken=" + resumptionToken;
	    }

	    Optional<JSONObject> response = omHandler.getJSONResponse(WebRequest.createGET(currentListURL));

	    if (response.isPresent()) {

		JSONObject json = response.get();

		if (json.has("completed") && json.getBoolean("completed") == false && json.has("resumptionToken")) {
		    resumptionToken = json.getString("resumptionToken");
		} else {
		    resumptionToken = null;
		}

		if (json.has("member")) {

		    JSONArray members = json.getJSONArray("member");
		    GSLoggerFactory.getLogger(getClass()).info("Retrieved block of size {}, resumption token {}", members.length(),
			    resumptionToken);
		    if (members.length() == 0) {
			break;
		    }
		    // for each observation
		    for (int i = 0; i < members.length(); i++) {
			totalRecords++;
			JSONObject member = members.getJSONObject(i);
			String id = member.getString("id");

			String uom = null;
			String observedProperty = null;
			String mySourceId = null;
			LatitudeLongitude latitudeLongitude = null;

			JSONObject result = member.optJSONObject("result");
			if (result != null) {
			    JSONObject dpm = result.optJSONObject("defaultPointMetadata");
			    if (dpm != null) {
				uom = dpm.optString("uom");
			    }
			}
			JSONArray parameter = member.optJSONArray("parameter");
			if (parameter != null) {
			    for (int j = 0; j < parameter.length(); j++) {
				JSONObject p = parameter.optJSONObject(j);
				if (p != null) {
				    String name = p.optString("name");
				    if (name != null && name.equals("sourceId")) {
					mySourceId = p.optString("value");
				    }
				}
			    }
			}
			JSONObject observedPropertyObject = member.optJSONObject("observedProperty");
			if (observedPropertyObject != null) {
			    observedProperty = observedPropertyObject.optString("title");
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
			String downloadURL = "http://localhost:9090/gs-service/services/essi/token/" + token + "/view/" + viewId
				+ "/om-api/observations?includeData=true&format=WML1&observationIdentifier=" + id;

			WebRequest get2 = WebRequest.createGET(downloadURL);

			try {

			    File dataFile = new File(tempDirFile, "FILE-" + id.hashCode());

			    omHandler.handle(new FileOutputStream(dataFile), get2);

			    BigDecimal sizeInMB = null;
			    if (dataFile.exists()) {
				long sizeInBytes = dataFile.length();
				sizeInMB = BigDecimal.valueOf(sizeInBytes).divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.HALF_UP);
			    } else {
				sizeInMB = BigDecimal.ZERO;
			    }

			    GSLoggerFactory.getLogger(getClass()).info("Downloaded file. Size: {} MB", sizeInMB);

			    if (sizeInMB.equals(BigDecimal.ZERO)) {
				errors++;
			    } else {
				addPointsFromWML(dataFile, dataCacheConnector, uom, observedProperty, latitudeLongitude, id, mySourceId);

				dataFile.delete();
				recordsDone++;

			    }

			    if (maxRecords != null && recordsDone >= maxRecords) {
				break base;
			    }

			} catch (Exception e) {
			    GSLoggerFactory.getLogger(getClass()).error(e);
			    errors++;
			}

			int bufferSize;
			while ((bufferSize = dataCacheConnector.countRecordsInDataBuffer()) > WAITING_BUFFER_SIZE) {
			    GSLoggerFactory.getLogger(getClass()).info("Waiting writing data, buffer size: {}", bufferSize);

			    Thread.sleep(1000);
			}

			GSLoggerFactory.getLogger(getClass()).info(
				"Basic data augmenter stats. Source id {}. Records seen: {} Records inserted: {} Errors: {} Major errors: {}",
				sourceId, totalRecords, recordsDone, errors, majorErrors);

			// CHECKING CANCELED JOB

			if (ConfigurationWrapper.isJobCanceled(context)) {
			    GSLoggerFactory.getLogger(getClass()).info("Basic data harvester task CANCELED source id {} ", sourceId);

			    status.setPhase(JobPhase.CANCELED);
			    break base;
			}

		    }

		} else {
		    majorErrors++;
		    GSLoggerFactory.getLogger(getClass()).error("Member not present");

		}
	    } else {
		majorErrors++;
		GSLoggerFactory.getLogger(getClass()).error("Response not present");
	    }

	}

	log(status, "Basic Data harvester task ENDED");
    }

    @Override
    public String getName() {

	return "Basic Data harvester task";
    }

    static XMLInputFactory factory = XMLInputFactory.newInstance();

    private static String readValue(XMLEventReader reader) {

	String ret = "";
	XMLEvent event = null;
	do {
	    try {
		event = reader.nextEvent();
		if (event instanceof Characters) {
		    Characters cei = (Characters) event;
		    ret += cei.getData();
		}
	    } catch (XMLStreamException e) {
		e.printStackTrace();
	    }

	} while (event != null && !event.isEndElement());

	return ret.trim();
    }

    private void addPointsFromWML(File file, DataCacheConnector cache, String uom, String observedProperty,
	    LatitudeLongitude latitudeLongitude, String dataIdentifier, String sourceId) {
	GSLoggerFactory.getLogger(getClass()).info("Sending file {} to OS, identifier: {}", file.getAbsolutePath(), dataIdentifier);
	try {
	    FileInputStream stream = new FileInputStream(file);

	    StreamSource source = new StreamSource(stream);
	    XMLEventReader reader = factory.createXMLEventReader(source);

	    String nodataValue = null;
	    Set<String> insertedDateHours = new HashSet<String>();
	    while (reader.hasNext()) {

		XMLEvent event = reader.nextEvent();

		if (event.isStartElement()) {

		    StartElement startElement = event.asStartElement();

		    String startName = startElement.getName().getLocalPart();

		    switch (startName) {
		    case "noDataValue":
			nodataValue = readValue(reader);
			break;
		    case "value":

			Attribute dateTimeAttribute = startElement.getAttributeByName(new QName("dateTimeUTC"));
			if (dateTimeAttribute == null) {
			    dateTimeAttribute = startElement.getAttributeByName(new QName("dateTime"));
			}
			if (dateTimeAttribute != null) {
			    String date = dateTimeAttribute.getValue();
			    String value = readValue(reader);
			    BigDecimal v = null;
			    if (nodataValue == null || !nodataValue.equals(value)) {
				v = new BigDecimal(value);
			    }
			    Attribute qualityAttribute = startElement.getAttributeByName(new QName("qualityControlLevelCode"));
			    String quality = null;
			    if (qualityAttribute != null) {
				quality = qualityAttribute.getValue();
			    }

			    Optional<Date> d = ISO8601DateTimeUtils.parseISO8601ToDate(date);
			    try {
				if (d.isPresent()) {

				    String dateTime = ISO8601DateTimeUtils.getISO8601DateTime(d.get());
				    String dateAndHour = dateTime.substring(0, 14);
				    if (insertedDateHours.contains(dateAndHour)) {
					// already inserted for this hour. this is to skip sub hours data
					continue;
				    } else {
					insertedDateHours.add(dateAndHour);
				    }
				    DataRecord dataRecord = new DataRecord(d.get(), v, uom, observedProperty, latitudeLongitude,
					    dataIdentifier);
				    dataRecord.setQuality(quality);
				    dataRecord.setSourceIdentifier(sourceId);
				    cache.write(dataRecord);

				}
			    } catch (Exception e) {
			    }

			}
			break;
		    default:
			break;
		    }

		}
	    }

	    reader.close();
	    stream.close();

	    file.delete();

	} catch (Exception e) {
	    throw new IllegalArgumentException(DataValidatorErrorCode.DECODING_ERROR.toString());
	}

    }

    public static void main(String[] args) {
	System.out.println(new Date(-9990000000l));
    }
}
