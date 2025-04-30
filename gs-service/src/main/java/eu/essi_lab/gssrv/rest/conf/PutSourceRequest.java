/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf;

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
import eu.essi_lab.gssrv.rest.conf.HarvestSchedulingRequest.RepeatCount;
import eu.essi_lab.gssrv.rest.conf.HarvestSchedulingRequest.RepeatIntervalUnit;
import eu.essi_lab.lib.utils.LabeledEnum;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class PutSourceRequest extends ConfigRequest {

    public static final String SOURCE_ID = "id";
    public static final String SOURCE_LABEL = "label";
    public static final String SOURCE_ENDPOINT = "endpoint";
    public static final String SERVICE_TYPE = "serviceType";
    public static final String HARVEST_SCHEDULING = "harvestScheduling";

    /**
     * @author Fabrizio
     */
    public enum SourceType implements LabeledEnum {

	/**
	 * 
	 */
	CSW("CSW"),
	/**
	 * 
	 */
	WMS_111("WMS 1.1.1"),
	/**
	 * 
	 */
	WMS_130("WMS 1.3.0"),

	/**
	 * 
	 */
	WCS_100("WCS 1.0.0"),
	/**
	* 
	*/
	WCS_110("WCS 1.1.0"),
	/**
	 * 
	 */
	WCS_111("WCS 1.1.1"),

	/**
	 * 
	 */
	WFS_110("WFS 1.1.0");

	private String label;

	/**
	 * @param label
	 */
	private SourceType(String label) {

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
    public PutSourceRequest() {
    }

    /**
     * @param object
     */
    public PutSourceRequest(JSONObject object) {

	super(object);
    }

    @Override
    public List<Parameter> getSupportedParameters() {

	ArrayList<Parameter> list = new ArrayList<>();

	list.add(Parameter.of(SOURCE_ID, ContentType.TEXTUAL, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_MINUS, false));
	list.add(Parameter.of(SOURCE_LABEL, ContentType.TEXTUAL, true));
	list.add(Parameter.of(SOURCE_ENDPOINT, ContentType.TEXTUAL, true));
	list.add(Parameter.of(SERVICE_TYPE, ContentType.TEXTUAL, SourceType.class, true));

	list.add(Parameter.of(HARVEST_SCHEDULING, false, HarvestSchedulingRequest.START_TIME, ContentType.ISO8601_DATE_TIME, false));
	list.add(Parameter.of(HARVEST_SCHEDULING, false, HarvestSchedulingRequest.REPEAT_COUNT, ContentType.TEXTUAL, RepeatCount.class,
		true));
	list.add(Parameter.of(HARVEST_SCHEDULING, false, HarvestSchedulingRequest.REPEAT_INTERVAL, ContentType.INTEGER, false));
	list.add(Parameter.of(HARVEST_SCHEDULING, false, HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT, ContentType.TEXTUAL,
		RepeatIntervalUnit.class, false));

	return list;
    }

    @Override
    protected void mandatoryCheck() {

	super.mandatoryCheck();

	List<String> nestedParameters = readNestedParameters();

	if (!nestedParameters.isEmpty() && nestedParameters.get(0).equals(HARVEST_SCHEDULING)) {

	    HarvestSchedulingRequest harvestSchedulingRequest = new HarvestSchedulingRequest();

	    readSubParameters(HARVEST_SCHEDULING).forEach(subParam -> {

		Object subValue = readSubValue(HARVEST_SCHEDULING, subParam);

		harvestSchedulingRequest.put(subParam, subValue.toString());
	    });

	    HarvestSchedulingRequest.mandatoryCheck(harvestSchedulingRequest);
	}
    }

}
