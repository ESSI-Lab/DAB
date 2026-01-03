package eu.essi_lab.messages;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.*;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public abstract class JobStatus {

    /**
     * @author Fabrizio
     */
    public enum JobPhase implements LabeledEnum {

	/**
	 * 
	 */
	RUNNING("Running"),

	/**
	 * 
	 */
	RESCHEDULED("Rescheduled"),

	/**
	 * 
	 */
	COMPLETED("Completed"),

	/**
	 * 
	 */
	CANCELED("Canceled"),

	/**
	 * 
	 */
	ERROR("Error");

	public static final String RUNNING_LABEL = "Running";
	public static final String RESCHEDULED_LABEL = "Rescheduled";
	public static final String COMPLETED_LABEL = "Completed";
	public static final String CANCELED_LABEL = "Canceled";
	public static final String ERROR_LABEL = "Error";

	private final String label;

	/**
	 * @param label
	 */
	JobPhase(String label) {

	    this.label = label;
	}

	@Override
	public String getLabel() {

	    return label;
	}
    }

    /**
     * @author Fabrizio
     */
    private enum MessageType {

	INFO, //
	WARN, //
	ERROR;//

	public String getType() {

	    return switch (this) {
		case ERROR -> "errorMessages";
		case WARN -> "warnMessages";
		default -> "infoMessages";
	    };
	}
    }

    /**
     * @param message
     */
    public void addInfoMessage(String message) {

	addMessage(message, MessageType.INFO);
    }

    /**
     * @param message
     */
    public void addErrorMessage(String message) {

	addMessage(message, MessageType.ERROR);
    }

    /**
     * @param message
     */
    public void addWarningMessage(String message) {

	addMessage(message, MessageType.WARN);
    }

    /**
     * @return
     */
    public List<String> getMessagesList() {

	return getMessagesList(true);
    }

    /**
     * @return
     */
    public List<String> getInfoMessages() {

	return getMessagesList(false, MessageType.INFO);
    }

    /**
     * @return
     */
    public List<String> getErrorMessages() {

	return getMessagesList(false, MessageType.ERROR);
    }

    /**
     * @return
     */
    public List<String> getWarningMessages() {

	return getMessagesList(false, MessageType.WARN);
    }

    /**
     * @return
     */
    public List<String> getMessagesList(boolean insertMessageType) {

	return getMessagesList(insertMessageType, null);
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<String> getMessagesList(boolean insertMessageType, MessageType targetType) {

	List<MessageType> types = new ArrayList<>();

	boolean hasInfo = getObject().has(MessageType.INFO.getType()) && (targetType == null || targetType == MessageType.INFO);

	if (hasInfo) {
	    types.add(MessageType.INFO);
	}

	boolean hasWarn = getObject().has(MessageType.WARN.getType()) && (targetType == null || targetType == MessageType.WARN);
	if (hasWarn) {
	    types.add(MessageType.WARN);
	}

	boolean hasError = getObject().has(MessageType.ERROR.getType()) && (targetType == null || targetType == MessageType.ERROR);
	if (hasError) {
	    types.add(MessageType.ERROR);
	}

	List<JSONObject> messages = new ArrayList<>();

	for (MessageType type : types) {

	    JSONArray jsonArray = getObject().getJSONArray(type.getType());

	    messages.addAll(jsonArray.//
		    toList().//
		    stream().//

		    map(o -> {
			JSONObject object = new JSONObject((HashMap<String, String>) o);

			switch (type.getType()) {
			case "errorMessages":

			    getMessages(object, "- Error: ", insertMessageType);

			    break;

			case "warnMessages":

			    getMessages(object, "- Warning: ", insertMessageType);

			    break;

			case "infoMessages":

			    getMessages(object, "- Info: ", insertMessageType);

			    break;
			}

			return object;
		    }).//
		    collect(Collectors.toList()));
	}

	return messages.//
		stream(). //

		sorted(Comparator.comparing(o -> o.getString("timeStamp"))).//

		flatMap(o -> o.keySet().stream().//

			filter(k -> !k.equals("timeStamp")).//
			sorted((k1, k2) -> {

			    int v1 = Integer.parseInt(k1.equals("message") ? "1" : k1.replace("message ", ""));
			    int v2 = Integer.parseInt(k2.equals("message") ? "1" : k2.replace("message ", ""));

			    return Integer.compare(v1, v2);
			}).//

			map(k -> o.get(k).toString())).//

		collect(Collectors.toList());
    }

    /**
     * 
     */
    public void clearMessages() {

	Arrays.asList(MessageType.values()).forEach(m -> {

	    if (getObject().has(m.getType())) {

		getObject().remove(m.getType());
	    }
	});
    }

    /**
     * @param object
     * @param msgType
     * @param insertMessageType
     */
    private void getMessages(JSONObject object, String msgType, boolean insertMessageType) {

	object.keySet().forEach(k -> {
	    if (insertMessageType) {

		object.put(k, msgType + object.get(k).toString());

	    } else {
		object.put(k, object.get(k).toString());
	    }
	});
    }

    /**
     * @return
     */
    public String getJoinedMessages() {

	return String.join("\n", getMessagesList());
    }

    /**
     * 
     */
    public void setErrorPhase() {

	setPhase(JobPhase.ERROR);
    }

    /**
     * @return
     */
    public JobPhase getPhase() {

	return LabeledEnum.valueOf(JobPhase.class, getObject().getString("phase")).get();
    }

    /**
     * @return
     */
    public String getJobIdentifier() {

	return getObject().getString("jobId");
    }

    /**
     * @return
     */
    public String getJobGroup() {

	return getObject().getString("jobGroup");
    }

    /**
     * @return
     */
    protected void setJobGroup(String jobGroup) {

	getObject().put("jobGroup", jobGroup);
    }

    /**
     * @param dataUri
     */
    public void setDataUri(String dataUri) {

	getObject().put("dataUri", dataUri);
    }

    /**
     * @return
     */
    public Optional<String> getDataUri() {

	return getObject().has("dataUri") ? Optional.of(getObject().getString("dataUri")) : Optional.empty();
    }

    /**
     * @param id
     */
    protected void setJobId(String id) {

	getObject().put("jobId", id);
    }

    /**
     * @param phase
     */
    public void setPhase(JobPhase phase) {

	getObject().put("phase", phase.getLabel());
    }

    /**
     * @param phase
     */
    public void setEndTime() {

	getObject().put("endTime", ISO8601DateTimeUtils.getISO8601DateTime());
    }

    /**
     * @return
     */
    public Optional<String> getStartTime() {

	return getObject().has("startTime") ? Optional.of(getObject().getString("startTime")) : Optional.empty();
    }

    /**
     * @return
     */
    public Optional<String> getEndTime() {

	return getObject().has("endTime") ? Optional.of(getObject().getString("endTime")) : Optional.empty();
    }

    /**
     * @param message
     */
    private void addMessage(String message, MessageType msgType) {

	if (message == null || message.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).warn("Missing message");
	    return;
	}

	// to avoid SQL error in MySQLConnectionManager.execUpdate
	// java.sql.SQLSyntaxErrorException: You have an error in your SQL syntax;
	// check the manual that corresponds to your MySQL server version for the right syntax to use near '['
	message = message.replace("'", "");

	// " are not allowed in JSON
	message = message.replace("\"", "");

	String key = msgType.getType();

	String timeStamp = ISO8601DateTimeUtils.getISO8601DateTime();

	JSONObject objectMessage = new JSONObject();

	objectMessage.put("timeStamp", timeStamp);
	objectMessage.put("message 1", message);

	//
	//
	//

	JSONArray messages = new JSONArray();

	boolean hasMessages = getObject().has(key);
	if (!hasMessages) {

	    getObject().put(key, messages);

	    messages.put(objectMessage);

	} else {

	    messages = getObject().getJSONArray(key);

	    for (Object object : messages) {

		JSONObject objMsg = (JSONObject) object;
		String msgTimeStamp = objMsg.getString("timeStamp");

		if (msgTimeStamp.equals(timeStamp)) {

		    objMsg.put("message " + objMsg.keySet().size(), message);
		    return;
		}
	    }

	    messages.put(objectMessage);
	}
    }

    /**
     * @param phase
     */
    protected void setStartTime() {

	getObject().put("startTime", ISO8601DateTimeUtils.getISO8601DateTime());
    }

    @Override
    public String toString() {

	return getObject().toString(3);
    }

    /**
     * @return the object
     */
    public abstract JSONObject getObject();

    /**
     * 
     */
    @Override
    public boolean equals(Object object) {

	return object instanceof JobStatus && ((JobStatus) object).getObject().equals(this.getObject());
    }
}
