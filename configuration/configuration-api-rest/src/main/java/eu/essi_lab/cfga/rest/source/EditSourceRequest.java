/**
 *
 */
package eu.essi_lab.cfga.rest.source;

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

import eu.essi_lab.cfga.option.*;
import eu.essi_lab.cfga.rest.*;
import eu.essi_lab.model.Queryable.*;
import org.json.*;

import java.util.*;

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

	Parameter sourceDep = Parameter.of(SOURCE_DEPLOYMENT, ContentType.TEXTUAL, false);
	sourceDep.setMultiValue();

	list.add(sourceDep);

	return list;
    }

    /**
     *
     */
    @Override
    protected void mandatoryCheck() {

	super.mandatoryCheck();

	if (readParameters().size() == 1) {

	    throw new IllegalArgumentException(
		    "At least one of the parameters '" + SOURCE_LABEL + "', '" + SOURCE_ENDPOINT + "', '" + SERVICE_TYPE
			    + "' must be provided'");
	}
    }
}
