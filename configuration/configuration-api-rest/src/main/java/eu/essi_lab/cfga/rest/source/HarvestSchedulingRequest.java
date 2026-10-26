/**
 * 
 */
package eu.essi_lab.cfga.rest.source;

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
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.model.Queryable.*;
import org.json.*;

import java.util.*;

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

	repeatIntervalCheck(paramName, value);

	super.contentTypeCheck(supportedParams, paramName, value);
    }

    /**
     * Checks the consistency of the scheduling parameters. Can be used by any request supporting the
     * {@link #START_TIME}, {@link #REPEAT_COUNT}, {@link #REPEAT_INTERVAL} and {@link #REPEAT_INTERVAL_UNIT} parameters
     *
     * @param request
     */
    public static void mandatoryCheck(ConfigRequest request) {

	boolean hasRepeatCount = request.read(REPEAT_COUNT).isPresent();
	boolean hasInterval = request.read(REPEAT_INTERVAL).isPresent();
	boolean hasIntervalUnit = request.read(REPEAT_INTERVAL_UNIT).isPresent();

	if (hasRepeatCount && request.read(REPEAT_COUNT).get().equals(RepeatCount.INDEFINITELY.getLabel())) {

	    if (!hasInterval && !hasIntervalUnit) {

		throw new IllegalArgumentException("Missing parameters '" + REPEAT_INTERVAL + "' and " + REPEAT_INTERVAL_UNIT
			+ "' which are mandatory when '" + REPEAT_COUNT + "' is '" + RepeatCount.INDEFINITELY.getLabel() + "'");
	    }

	    if (!hasInterval && hasIntervalUnit) {

		throw new IllegalArgumentException("Missing parameter '" + REPEAT_INTERVAL + "' which is mandatory when '" + REPEAT_COUNT
			+ "' is '" + RepeatCount.INDEFINITELY.getLabel() + "'");
	    }

	    if (hasInterval && !hasIntervalUnit) {

		throw new IllegalArgumentException("Missing parameter '" + REPEAT_INTERVAL + "' which is mandatory when '" + REPEAT_COUNT
			+ "' is '" + RepeatCount.INDEFINITELY.getLabel() + "'");
	    }
	}

	if (hasRepeatCount && request.read(REPEAT_COUNT).get().equals(RepeatCount.ONCE.getLabel())) {

	    if (hasInterval || hasIntervalUnit) {

		throw new IllegalArgumentException("Parameters '" + REPEAT_INTERVAL + "' and '" + REPEAT_INTERVAL_UNIT
			+ "' must be omitted when " + REPEAT_COUNT + "' is '" + RepeatCount.ONCE.getLabel() + "'");
	    }
	}
    }

    /**
     * @param paramName
     * @param value
     */
    public static void repeatIntervalCheck(String paramName, Object value) {

	if (paramName.equals(REPEAT_INTERVAL)) {

	    Integer interval = Integer.valueOf(value.toString());
	    if (interval < 1) {

		throw new IllegalArgumentException("Value of '" + REPEAT_INTERVAL + "' parameter must be >= 1");
	    }
	}
    }
}
