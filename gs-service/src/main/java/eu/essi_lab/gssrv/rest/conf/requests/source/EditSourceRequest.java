/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf.requests.source;

import java.util.ArrayList;

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

import java.util.List;

import org.json.JSONObject;

import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.gssrv.rest.conf.Parameter;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class EditSourceRequest extends PutSourceRequest {

    /**
     * 
     */
    public EditSourceRequest() {
    }

    /**
     * @param object
     */
    public EditSourceRequest(JSONObject object) {

	super(object);
    }

    @Override
    public List<Parameter> getSupportedParameters() {

	ArrayList<Parameter> list = new ArrayList<>();

	list.add(Parameter.of(SOURCE_ID, ContentType.TEXTUAL, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_MINUS, true));
	list.add(Parameter.of(SOURCE_LABEL, ContentType.TEXTUAL, false));
	list.add(Parameter.of(SOURCE_ENDPOINT, ContentType.TEXTUAL, false));
	list.add(Parameter.of(SERVICE_TYPE, ContentType.TEXTUAL, SourceType.class, false));

	return list;
    }

    /**
     * 
     */
    @Override
    protected void mandatoryCheck() {

	super.mandatoryCheck();

	if (readParameters().size() == 1) {

	    throw new IllegalArgumentException("At least one of the parameters '" + SOURCE_LABEL + "', '" + SOURCE_ENDPOINT + "', '"
		    + SERVICE_TYPE + "' must be provided'");
	}
    }
}
