package eu.essi_lab.messages;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
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
	COMPLETED("Completed"),
	/**
	 * 
	 */
	ERROR("Error");

	private String label;

	/**
	 * @param label
	 */
	private JobPhase(String label) {

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
    private static enum MessageType {

	INFO, //
	WARN, //
	ERROR;//

	public String getType() {

	    switch (this) {
	    case ERROR:
		return "errorMessages";
	    case WARN:
		return "warnMessages";
	    default:
		return "infoMessages";
	    }
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
    public List<String> getInfoMessages() {

	return getMessages(MessageType.INFO);
    }

    /**
     * @return
     */
    public List<String> getErrorMessages() {

	return getMessages(MessageType.ERROR);
    }

    /**
     * @return
     */
    public List<String> getWarningMessages() {

	return getMessages(MessageType.WARN);
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
    @SuppressWarnings("unchecked")
    public List<String> getMessagesList(boolean insertMessageType) {

	List<MessageType> types = new ArrayList<>();

	boolean hasInfo = getObject().has(MessageType.INFO.getType());
	if (hasInfo) {
	    types.add(MessageType.INFO);
	}

	boolean hasWarn = getObject().has(MessageType.WARN.getType());
	if (hasWarn) {
	    types.add(MessageType.WARN);
	}

	boolean hasError = getObject().has(MessageType.ERROR.getType());
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

			    if (insertMessageType) {
				object.put("message", "- Error: " + object.getString("message"));
			    } else {
				object.put("message", object.getString("message"));

			    }
			    break;

			case "warnMessages":

			    if (insertMessageType) {

				object.put("message", "- Warning: " + object.getString("message"));

			    } else {
				object.put("message", object.getString("message"));
			    }

			    break;
			case "infoMessages":

			    if (insertMessageType) {

				object.put("message", "- Info: " + object.getString("message"));

			    } else {
				object.put("message", object.getString("message"));

			    }

			    break;
			}

			return object;
		    }).//
		    collect(Collectors.toList()));
	}

	return messages.stream(). //
		sorted((o1, o2) -> o1.getString("timeStamp").compareTo(o2.getString("timeStamp"))).//
		map(o -> o.getString("message")).//
		collect(Collectors.toList());
    }

    /**
     * @return
     */
    public String getJoinedMessages() {

	return getMessagesList().stream().collect(Collectors.joining("\n"));
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
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<String> getMessages(MessageType msgType) {

	String key = msgType.getType();

	boolean has = getObject().has(key);
	if (has) {

	    JSONArray jsonArray = getObject().getJSONArray(key);

	    return jsonArray.//
		    toList().//
		    stream().//
		    map(o -> new JSONObject((HashMap<String, String>) o)).//
		    sorted((o1, o2) -> o1.getString("timeStamp").compareTo(o2.getString("timeStamp"))).//
		    map(o -> o.getString("message")).//
		    collect(Collectors.toList());

	}

	return new ArrayList<>();
    }

    /**
     * @param message
     */
    private void addMessage(String message, MessageType msgType) {

	if (message == null || message.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).warn("Missing message");
	    return;
	}

	String key = msgType.getType();

	JSONObject objectMessage = new JSONObject();
	objectMessage.put("timeStamp", ISO8601DateTimeUtils.getISO8601DateTime());
	objectMessage.put("message", message);

	JSONArray messages = new JSONArray();

	boolean hasMessages = getObject().has(key);
	if (!hasMessages) {

	    getObject().put(key, messages);

	} else {

	    messages = getObject().getJSONArray(key);
	}

	messages.put(objectMessage);
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
