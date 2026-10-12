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
 * Publishes the notifications of a metadata harvesting execution, at its start ({@link #publishStarted()}) and at its end
 * ({@link #publish()}):
 * <ul>
 * <li>on the MQTT broker configured in the system settings key-value options, on the topic
 * {@code dab/{sourceId}/harvesting}. If the MQTT options are missing, nothing is published</li>
 * <li>in the {@code {dbName}-harvests} index of the "DAB statistics gathering" database (see
 * {@link ElasticsearchHarvestingPublisher}). If the statistics gathering is disabled, nothing is stored</li>
 * </ul>
 * Each execution is stored as a single document with id {@link #getHarvestingId()}, created at the start with result
 * {@link HarvestingResult#RUNNING} and replaced at the end with the final result. If the execution continues a
 * previous execution which has been interrupted (e.g. because the process died), the previous execution document is
 * marked as {@link HarvestingResult#INTERRUPTED} and linked to this execution.<br>
 * At the end, the registered {@link HarvestingEndListener}s are notified.<br>
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
	 * The harvesting is running
	 */
	RUNNING,
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
	FAILED,
	/**
	 * The harvesting execution has been interrupted (e.g. the process died), and continued by another execution
	 */
	INTERRUPTED
    }

    /**
     * How an execution continues a previous interrupted execution
     *
     * @author boldrini
     */
    public enum Continuation {

	/**
	 * The scheduler re-fired the interrupted job, and the accessor supports recovery
	 */
	RECOVERY,
	/**
	 * A successive scheduled execution resumed the interrupted harvesting from its resumption token
	 */
	RESUME,
	/**
	 * The accessor supports neither recovery nor resuming, so the harvesting started from scratch
	 */
	RESTART
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

    private String harvestingId;
    private String rootHarvestingId;
    private String continuationOf;
    private Continuation continuation;
    private String accessorType;
    private String incrementalFrom;
    private String previousHarvestingEndDate;
    private Integer harvestingCount;

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
	this.harvestingId = source.getUniqueIdentifier() + "_" + startTimeMillis;
	this.rootHarvestingId = harvestingId;
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
     * @return the identifier of this harvesting execution
     */
    public String getHarvestingId() {

	return harvestingId;
    }

    /**
     * @return the identifier of the first execution of this harvesting; it differs from {@link #getHarvestingId()} if this
     *         execution continues one or more interrupted executions
     */
    public String getRootHarvestingId() {

	return rootHarvestingId;
    }

    /**
     * Declares that this execution continues the given interrupted execution
     *
     * @param previousHarvestingId the identifier of the interrupted execution
     * @param previousRootHarvestingId the root identifier of the interrupted execution, if known
     * @param continuation
     */
    public void setContinuationOf(String previousHarvestingId, Optional<String> previousRootHarvestingId, Continuation continuation) {

	this.continuationOf = previousHarvestingId;
	this.rootHarvestingId = previousRootHarvestingId.orElse(previousHarvestingId);
	this.continuation = continuation;
    }

    /**
     * @param accessorType
     */
    public void setAccessorType(String accessorType) {

	this.accessorType = accessorType;
    }

    /**
     * @param incrementalFrom the start date of the selective harvesting, if any
     */
    public void setIncrementalFrom(String incrementalFrom) {

	this.incrementalFrom = incrementalFrom;
    }

    /**
     * @param previousHarvestingEndDate
     */
    public void setPreviousHarvestingEndDate(String previousHarvestingEndDate) {

	this.previousHarvestingEndDate = previousHarvestingEndDate;
    }

    /**
     * @param harvestingCount number of the completed harvestings of the source, before this one
     */
    public void setHarvestingCount(int harvestingCount) {

	this.harvestingCount = harvestingCount;
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
     * @return the message published at the start of the harvesting
     */
    public JSONObject buildStartMessage() {

	JSONObject object = buildCommonFields();

	object.put("result", HarvestingResult.RUNNING.name());

	return object;
    }

    /**
     * @return the message published at the end of the harvesting
     */
    public JSONObject buildMessage() {

	JSONObject object = buildCommonFields();

	object.put("endDate", ISO8601DateTimeUtils.getISO8601DateTime());

	long durationMillis = System.currentTimeMillis() - startTimeMillis;

	// duration in milliseconds and in ISO 8601 format (e.g. PT42M3.5S)
	object.put("durationMillis", durationMillis);
	object.put("duration", Duration.ofMillis(durationMillis).toString());

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
     * @return the fields used to update the document of the interrupted execution continued by this one
     */
    public JSONObject buildInterruptedMessage() {

	JSONObject object = new JSONObject();

	object.put("result", HarvestingResult.INTERRUPTED.name());
	object.put("interruptionDetectedDate", startTimestamp);
	object.put("continuedBy", harvestingId);

	return object;
    }

    /**
     * Publishes the harvesting start notification on the MQTT broker and stores it in the statistics database, if
     * configured. If this execution continues an interrupted one, the interrupted execution document is updated
     */
    public void publishStarted() {

	JSONObject message = buildStartMessage();

	publishMQTT(message);

	ElasticsearchHarvestingPublisher.publish(harvestingId, message);

	if (continuationOf != null) {

	    GSLoggerFactory.getLogger(getClass()).info("Harvesting {} continues interrupted harvesting {} ({})", harvestingId,
		    continuationOf, continuation);

	    // the update is applied only if the interrupted execution is still RUNNING, so a final result is never replaced
	    ElasticsearchHarvestingPublisher.update(continuationOf, buildInterruptedMessage(), HarvestingResult.RUNNING.name());
	}
    }

    /**
     * Publishes the harvesting end notification on the MQTT broker and stores it in the statistics database, if
     * configured. Then notifies the registered {@link HarvestingEndListener}s
     */
    public void publish() {

	JSONObject message = buildMessage();

	publishMQTT(message);

	ElasticsearchHarvestingPublisher.publish(harvestingId, message);

	notifyListeners();
    }

    /**
     * @return
     */
    private JSONObject buildCommonFields() {

	JSONObject object = new JSONObject();

	object.put("harvestingId", harvestingId);
	object.put("rootHarvestingId", rootHarvestingId);
	object.put("continuationOf", continuationOf);
	object.put("continuation", continuation != null ? continuation.name() : null);
	object.put("sourceId", source.getUniqueIdentifier());
	object.put("sourceLabel", source.getLabel());
	object.put("sourceEndpoint", source.getEndpoint());
	object.put("accessorType", accessorType);
	object.put("strategy", strategy.name());
	object.put("recovery", recovery);
	object.put("resumed", resumed);
	object.put("startDate", startTimestamp);
	object.put("incrementalFrom", incrementalFrom);
	object.put("previousHarvestingEndDate", previousHarvestingEndDate);
	object.put("harvestingCount", harvestingCount);
	object.put("recordsBefore", recordsBefore);

	return object;
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

	Optional<MQTTBrokerSetting> broker = systemSettings.getDabMqttBroker();

	if (broker.isEmpty()) {

	    GSLoggerFactory.getLogger(getClass()).debug("DAB activity MQTT broker not configured, harvesting notification not sent");

	    return Optional.empty();
	}

	MQTTBrokerSetting mqtt = broker.get();

	return Optional.of(new MQTTPublisherHive(mqtt.getHost().get(), mqtt.getPort().get(), mqtt.getUser().get(), mqtt.getPassword().get()));
    }
}
