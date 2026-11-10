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

import eu.essi_lab.cfga.option.*;
import eu.essi_lab.cfga.rest.*;
import eu.essi_lab.cfga.rest.source.*;
import eu.essi_lab.model.Queryable.*;
import org.json.*;

import java.util.*;

/**
 * Schedules (or reschedules) a custom task. Supports the same scheduling parameters of the
 * {@link HarvestSchedulingRequest}
 *
 * @author boldrini
 */
public class CustomTaskSchedulingRequest extends ConfigRequest {

    /**
     *
     */
    public CustomTaskSchedulingRequest() {
    }

    /**
     * @param object
     */
    public CustomTaskSchedulingRequest(JSONObject object) {

	super(object);
    }

    @Override
    public List<Parameter> getSupportedParameters() {

	ArrayList<Parameter> list = new ArrayList<>();

	list.add(Parameter.of(PutCustomTaskRequest.TASK_ID, ContentType.TEXTUAL, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_MINUS,
		true));

	list.add(Parameter.of(HarvestSchedulingRequest.START_TIME, ContentType.ISO8601_DATE_TIME, false));
	list.add(Parameter.of(HarvestSchedulingRequest.REPEAT_COUNT, ContentType.TEXTUAL, HarvestSchedulingRequest.RepeatCount.class,
		true));
	list.add(Parameter.of(HarvestSchedulingRequest.REPEAT_INTERVAL, ContentType.INTEGER, false));
	list.add(Parameter.of(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, ContentType.TEXTUAL,
		HarvestSchedulingRequest.RepeatIntervalUnit.class, false));

	return list;
    }

    @Override
    protected void mandatoryCheck() {

	super.mandatoryCheck();

	HarvestSchedulingRequest.mandatoryCheck(this);
    }

    @Override
    protected void contentTypeCheck(List<Parameter> supportedParams, String paramName, Object value) {

	HarvestSchedulingRequest.repeatIntervalCheck(paramName, value);

	super.contentTypeCheck(supportedParams, paramName, value);
    }
}
