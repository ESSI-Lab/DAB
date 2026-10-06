package eu.essi_lab.harvester;

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

import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.cfga.gs.setting.SystemSetting.*;
import eu.essi_lab.lib.mqtt.hive.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.shared.driver.es.stats.*;
import org.json.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Publishes a summary of a metadata harvesting procedure:
 * <ul>
 * <li>on the MQTT broker configured in the system settings key-value options, on the topic
 * {@code dab/{sourceId}/harvesting}. If the MQTT options are missing, nothing is published</li>
 * <li>in the {@code {dbName}-harvests} index of the "DAB statistics gathering" database (see
 * {@link ElasticsearchHarvestingPublisher}). If the statistics gathering is disabled, nothing is stored</li>
 * </ul>
 * Finally, the registered {@link HarvestingEndListener}s are notified.<br>
 * Publishing errors are logged and never propagated, so they cannot affect the harvesting procedure
 *
 * @author boldrini
 */
public class HarvestingNotifier {

    /**
     * @author boldrini
     */
    public enum HarvestingResult {

	/**
	 * The harvested records replaced the previous ones
	 */
	COMPLETED,
	/**
	 * Low quantity of records detected: the harvested records are discarded and the consolidated folder survives
	 */
	CONSOLIDATED_FOLDER_SURVIVED,
	/**
	 * The harvesting has been canceled
	 */
	CANCELED,
	/**
	 * The harvesting ended with errors
	 */
	FAILED
    }

    private static final List<HarvestingEndListener> LISTENERS = new CopyOnWriteArrayList<>();

    private GSSource source;
    private HarvestingStrategy strategy;
    private boolean recovery;
    private boolean resumed;
    private String startTimestamp;
    private long startTimeMillis;
    private int recordsBefore;
    private Integer harvestedRecords;
    private int recordsAfter;
    private HarvestingResult result;
    private GSException exception;

    /**
     * @param source
     * @param strategy
     * @param recovery
     * @param resumed
     * @param recordsBefore number of records of the source before the harvesting, or -1 if unknown (e.g. first harvesting)
     */
    public HarvestingNotifier(GSSource source, HarvestingStrategy strategy, boolean recovery, boolean resumed, int recordsBefore) {

	this.source = source;
	this.strategy = strategy;
	this.recovery = recovery;
	this.resumed = resumed;
	this.recordsBefore = recordsBefore;
	this.recordsAfter = -1;
	this.startTimestamp = ISO8601DateTimeUtils.getISO8601DateTime();
	this.startTimeMillis = System.currentTimeMillis();
	this.result = HarvestingResult.COMPLETED;
    }

    /**
     * Registers a listener notified at the end of each harvesting procedure
     *
     * @param listener
     */
    public static void addListener(HarvestingEndListener listener) {

	LISTENERS.add(listener);
    }

    /**
     * @return
     */
    public GSSource getSource() {

	return source;
    }

    /**
     * @return
     */
    public HarvestingStrategy getStrategy() {

	return strategy;
    }

    /**
     * @return number of records of the source before the harvesting, or -1 if unknown (e.g. first harvesting)
     */
    public int getRecordsBefore() {

	return recordsBefore;
    }

    /**
     * @return number of records of the source after the harvesting, or -1 if unknown
     */
    public int getRecordsAfter() {

	return recordsAfter;
    }

    /**
     * @return
     */
    public HarvestingResult getResult() {

	return result;
    }

    /**
     * @param harvestedRecords number of records harvested in the writing folder, before the storage finalization
     */
    public void setHarvestedRecords(Integer harvestedRecords) {

	this.harvestedRecords = harvestedRecords;
    }

    /**
     * @param recordsAfter
     */
    public void setRecordsAfter(int recordsAfter) {

	this.recordsAfter = recordsAfter;
    }

    /**
     * @param result
     */
    public void setResult(HarvestingResult result) {

	this.result = result;
    }

    /**
     * @param exception
     */
    public void setException(GSException exception) {

	this.exception = exception;
    }

    /**
     * @return
     */
    public String getTopic() {

	return "dab/" + source.getUniqueIdentifier() + "/harvesting";
    }

    /**
     * @return
     */
    public JSONObject buildMessage() {

	JSONObject object = new JSONObject();

	object.put("sourceId", source.getUniqueIdentifier());
	object.put("sourceLabel", source.getLabel());
	object.put("strategy", strategy.name());
	object.put("recovery", recovery);
	object.put("resumed", resumed);
	object.put("startDate", startTimestamp);
	object.put("endDate", ISO8601DateTimeUtils.getISO8601DateTime());

	long durationMillis = System.currentTimeMillis() - startTimeMillis;

	// duration in milliseconds and in ISO 8601 format (e.g. PT42M3.5S)
	object.put("durationMillis", durationMillis);
	object.put("duration", Duration.ofMillis(durationMillis).toString());
	object.put("recordsBefore", recordsBefore);

	if (harvestedRecords != null) {

	    object.put("harvestedRecords", harvestedRecords.intValue());
	}

	object.put("recordsAfter", recordsAfter);
	object.put("result", result.name());

	if (exception != null && !exception.getErrorInfoList().isEmpty()) {

	    JSONArray errors = new JSONArray();

	    exception.getErrorInfoList().forEach(info -> errors.put(String.valueOf(info.getErrorDescription())));

	    object.put("errors", errors);
	}

	return object;
    }

    /**
     * Publishes the harvesting summary on the MQTT broker and stores it in the statistics database, if configured
     */
    public void publish() {

	JSONObject message = buildMessage();

	publishMQTT(message);

	ElasticsearchHarvestingPublisher.publish(source.getUniqueIdentifier() + "_" + startTimeMillis, message);

	notifyListeners();
    }

    /**
     * 
     */
    private void notifyListeners() {

	for (HarvestingEndListener listener : LISTENERS) {

	    try {

		listener.harvestingEnded(this);

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error("Harvesting end listener {} failed: {}", listener.getClass().getName(),
			ex.getMessage(), ex);
	    }
	}
    }

    /**
     * @param message
     */
    private void publishMQTT(JSONObject message) {

	try {

	    Optional<MQTTPublisherHive> client = createClient();

	    if (client.isPresent()) {

		GSLoggerFactory.getLogger(getClass()).info("Publishing harvesting notification on topic {} STARTED", getTopic());

		try {

		    client.get().publish(getTopic(), message.toString(3));

		} finally {

		    client.get().disconnect();
		}

		GSLoggerFactory.getLogger(getClass()).info("Publishing harvesting notification on topic {} ENDED", getTopic());
	    }

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to publish harvesting notification: {}", ex.getMessage(), ex);
	}
    }

    /**
     * @return
     * @throws Exception
     */
    private Optional<MQTTPublisherHive> createClient() throws Exception {

	SystemSetting systemSettings = ConfigurationWrapper.getSystemSettings();

	Optional<Properties> keyValueOption = systemSettings.getKeyValueOptions();

	if (keyValueOption.isEmpty()) {

	    return Optional.empty();
	}

	String mqttHost = keyValueOption.get().getProperty(KeyValueOptionKeys.MQTT_BROKER_HOST.getLabel());
	String mqttPort = keyValueOption.get().getProperty(KeyValueOptionKeys.MQTT_BROKER_PORT.getLabel());
	String mqttUser = keyValueOption.get().getProperty(KeyValueOptionKeys.MQTT_BROKER_USER.getLabel());
	String mqttPwd = keyValueOption.get().getProperty(KeyValueOptionKeys.MQTT_BROKER_PWD.getLabel());

	if (mqttHost == null || mqttPort == null || mqttUser == null || mqttPwd == null) {

	    GSLoggerFactory.getLogger(getClass()).debug("MQTT options not found, harvesting notification not sent");

	    return Optional.empty();
	}

	return Optional.of(new MQTTPublisherHive(mqttHost, Integer.parseInt(mqttPort), mqttUser, mqttPwd));
    }
}
