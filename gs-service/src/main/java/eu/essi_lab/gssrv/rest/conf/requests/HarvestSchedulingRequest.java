/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf.requests;

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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.gssrv.rest.conf.Parameter;
import eu.essi_lab.lib.utils.LabeledEnum;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class HarvestSchedulingRequest extends PutSourceRequest {

    public static final String START_TIME = "startTime";
    public static final String REPEAT_INTERVAL = "repeatInterval";
    public static final String REPEAT_INTERVAL_UNIT = "repeatIntervalUnit";
    public static final String REPEAT_COUNT = "repeatCount";

    /**
     * @author Fabrizio
     */
    public enum RepeatIntervalUnit implements LabeledEnum {

	/**
	 * 
	 */
	MINUTES("Minutes"),
	/**
	 * 
	 */
	HOURS("Hours"),
	/**
	 * 
	 */
	DAYS("Days"),
	/**
	 * 
	 */
	WEEKS("Weeks"),
	/**
	 * 
	 */
	MONTHS("Months");

	private String label;

	/**
	 * @param label
	 */
	private RepeatIntervalUnit(String label) {

	    this.label = label;
	}

	@Override
	public String getLabel() {

	    return label;
	}

	@Override
	public String toString() {

	    return getLabel();
	}
    }

    /**
     * @author Fabrizio
     */
    public enum RepeatCount implements LabeledEnum {

	/**
	 * 
	 */
	ONCE("Once"),
	/**
	 * 
	 */
	INDEFINITELY("Indefinitely");

	private String label;

	/**
	 * @param label
	 */
	private RepeatCount(String label) {

	    this.label = label;
	}

	@Override
	public String getLabel() {

	    return label;
	}

	@Override
	public String toString() {

	    return getLabel();
	}
    }

    /**
     * 
     */
    public HarvestSchedulingRequest() {
    }

    /**
     * @param object
     */
    public HarvestSchedulingRequest(JSONObject object) {

	super(object);
    }

    @Override
    public List<Parameter> getSupportedParameters() {

	ArrayList<Parameter> list = new ArrayList<>();

	list.add(Parameter.of(SOURCE_ID, ContentType.TEXTUAL, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_MINUS, true));

	list.add(Parameter.of(START_TIME, ContentType.ISO8601_DATE_TIME, false));
	list.add(Parameter.of(REPEAT_COUNT, ContentType.TEXTUAL, RepeatCount.class, true));
	list.add(Parameter.of(REPEAT_INTERVAL, ContentType.INTEGER, false));
	list.add(Parameter.of(REPEAT_INTERVAL_UNIT, ContentType.TEXTUAL, RepeatIntervalUnit.class, false));

	return list;
    }

    @Override
    protected void mandatoryCheck() {

	super.mandatoryCheck();

	mandatoryCheck(this);
    }

    @Override
    protected void contentTypeCheck(List<Parameter> supportedParams, String paramName, Object value) {

	if (paramName.equals(REPEAT_INTERVAL)) {
	    {
		Integer interval = Integer.valueOf(value.toString());
		if (interval < 1) {

		    throw new IllegalArgumentException("Value of '" + REPEAT_INTERVAL + "' parameter must be >= 1");
		}
	    }
	}
	
	super.contentTypeCheck(supportedParams, paramName, value);
    }

    /**
     * @param parameters
     */
    static void mandatoryCheck(HarvestSchedulingRequest request) {

	List<String> parameters = request.readParameters();

	if (parameters.contains(REPEAT_COUNT) && request.read(REPEAT_COUNT).get().equals(RepeatCount.INDEFINITELY.getLabel())) {

	    if (!parameters.contains(REPEAT_INTERVAL) && !parameters.contains(REPEAT_INTERVAL_UNIT)) {

		throw new IllegalArgumentException("Missing parameters '" + REPEAT_INTERVAL + "' and " + REPEAT_INTERVAL_UNIT
			+ "' which are mandatory when '" + REPEAT_COUNT + "' is '" + RepeatCount.INDEFINITELY.getLabel() + "'");
	    }

	    if (!parameters.contains(REPEAT_INTERVAL) && parameters.contains(REPEAT_INTERVAL_UNIT)) {

		throw new IllegalArgumentException("Missing parameter '" + REPEAT_INTERVAL + "' which is mandatory when '" + REPEAT_COUNT
			+ "' is '" + RepeatCount.INDEFINITELY.getLabel() + "'");
	    }

	    if (parameters.contains(REPEAT_INTERVAL) && !parameters.contains(REPEAT_INTERVAL_UNIT)) {

		throw new IllegalArgumentException("Missing parameter '" + REPEAT_INTERVAL + "' which is mandatory when '" + REPEAT_COUNT
			+ "' is '" + RepeatCount.INDEFINITELY.getLabel() + "'");
	    }
	}

	if (parameters.contains(REPEAT_COUNT) && request.read(REPEAT_COUNT).get().equals(RepeatCount.ONCE.getLabel())) {

	    if (parameters.contains(REPEAT_INTERVAL) || parameters.contains(REPEAT_INTERVAL_UNIT)) {

		throw new IllegalArgumentException("Parameters '" + REPEAT_INTERVAL + "' and '" + REPEAT_INTERVAL_UNIT
			+ "' must be omitted when " + REPEAT_COUNT + "' is '" + RepeatCount.ONCE.getLabel() + "'");
	    }
	}
    }
}
