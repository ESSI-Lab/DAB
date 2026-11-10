package eu.essi_lab.cfga.rest.task;

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

import eu.essi_lab.cfga.gs.task.CustomTaskSetting.*;
import eu.essi_lab.cfga.option.*;
import eu.essi_lab.cfga.rest.*;
import eu.essi_lab.cfga.rest.source.*;
import eu.essi_lab.model.Queryable.*;
import org.json.*;

import java.util.*;
import java.util.stream.*;

/**
 * @author boldrini
 */
public class PutCustomTaskRequest extends ConfigRequest {

    public static final String TASK_ID = "id";
    public static final String TASK_NAME = "taskName";
    public static final String TASK_DESCRIPTION = "description";
    public static final String TASK_OPTIONS = "options";
    public static final String EMAIL_RECIPIENTS = "emailRecipients";
    public static final String TASK_SCHEDULING = "TaskScheduling";

    /**
     *
     */
    public PutCustomTaskRequest() {
    }

    /**
     * @param object
     */
    public PutCustomTaskRequest(JSONObject object) {

	super(object);
    }

    @Override
    public List<Parameter> getSupportedParameters() {

	ArrayList<Parameter> list = new ArrayList<>();

	list.add(Parameter.of(TASK_ID, ContentType.TEXTUAL, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_MINUS, false));
	list.add(Parameter.of(TASK_NAME, ContentType.TEXTUAL, true));
	list.add(Parameter.of(TASK_DESCRIPTION, ContentType.TEXTUAL, false));
	list.add(Parameter.of(TASK_OPTIONS, ContentType.TEXTUAL, false));

	Parameter recipients = Parameter.of(EMAIL_RECIPIENTS, ContentType.TEXTUAL, false);
	recipients.setMultiValue();

	list.add(recipients);

	list.add(Parameter.of(TASK_SCHEDULING, false, HarvestSchedulingRequest.START_TIME, ContentType.ISO8601_DATE_TIME, false));
	list.add(Parameter.of(TASK_SCHEDULING, false, HarvestSchedulingRequest.REPEAT_COUNT, ContentType.TEXTUAL,
		HarvestSchedulingRequest.RepeatCount.class, true));
	list.add(Parameter.of(TASK_SCHEDULING, false, HarvestSchedulingRequest.REPEAT_INTERVAL, ContentType.INTEGER, false));
	list.add(Parameter.of(TASK_SCHEDULING, false, HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, ContentType.TEXTUAL,
		HarvestSchedulingRequest.RepeatIntervalUnit.class, false));

	return list;
    }

    @Override
    protected void mandatoryCheck() {

	super.mandatoryCheck();

	if (readCompositeParameters().contains(TASK_SCHEDULING)) {

	    HarvestSchedulingRequest.mandatoryCheck(getSchedulingRequest());
	}
    }

    @Override
    protected void contentTypeCheck(List<Parameter> supportedParams, String paramName, Object value) {

	if (paramName.equals(TASK_NAME)) {

	    taskNameCheck(value.toString());
	}

	HarvestSchedulingRequest.repeatIntervalCheck(paramName, value);

	super.contentTypeCheck(supportedParams, paramName, value);
    }

    /**
     * Returns a {@link CustomTaskSchedulingRequest} built from the {@link #TASK_SCHEDULING} composite parameter, if
     * present
     *
     * @return
     */
    public CustomTaskSchedulingRequest getSchedulingRequest() {

	CustomTaskSchedulingRequest schedulingRequest = new CustomTaskSchedulingRequest();

	readNestedParameters(TASK_SCHEDULING).forEach(subParam -> {

	    Object subValue = readNestedValue(TASK_SCHEDULING, subParam);

	    schedulingRequest.put(subParam, subValue.toString());
	});

	readString(TASK_ID).ifPresent(id -> schedulingRequest.put(TASK_ID, id));

	return schedulingRequest;
    }

    /**
     * @param taskName
     */
    static void taskNameCheck(String taskName) {

	List<String> names = CustomTaskValuesLoader.getValues();

	if (!names.contains(taskName)) {

	    throw new IllegalArgumentException("Unsupported value '" + taskName + "' for parameter '" + TASK_NAME
		    + "'. Supported values are: " + names.stream().map(n -> "'" + n + "'").collect(Collectors.joining(", ")));
	}
    }
}
