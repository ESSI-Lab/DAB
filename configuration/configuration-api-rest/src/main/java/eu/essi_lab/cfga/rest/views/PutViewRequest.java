package eu.essi_lab.cfga.rest.views;

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

import com.fasterxml.jackson.core.*;
import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.factory.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.database.*;
import eu.essi_lab.cfga.rest.*;
import eu.essi_lab.cfga.rest.source.*;
import eu.essi_lab.messages.bond.*;
import eu.essi_lab.messages.bond.jaxb.*;
import eu.essi_lab.model.exceptions.*;
import org.json.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class PutViewRequest extends ConfigRequest {

    /**
     *
     */
    public static String VIEW = "view";

    /**
     *
     */
    public PutViewRequest() {
    }

    /**
     * @param object
     */
    public PutViewRequest(JSONObject object) {

	super(object);
    }

    @Override
    public List<Parameter> getSupportedParameters() {

	ArrayList<Parameter> list = new ArrayList<>();

	list.add(Parameter.of(VIEW, true));

	return list;
    }

    @Override
    public void validate() {

	Optional<String> stringValue = readString(VIEW);

	if (stringValue.isEmpty()) {

	    throw new IllegalArgumentException("Missing mandatory 'view' payload");
	}

	JSONObject viewPayload = new JSONObject(stringValue.get());

	try {
	    ViewFactory.fromJSONObject(viewPayload);

	} catch (JsonProcessingException ex) {

	    throw new IllegalArgumentException("Invalid JSON encoding: " + ex.getMessage());

	} catch (Exception e) {

	    throw new IllegalArgumentException("Error occurred: " + e.getMessage());
	}
    }

}
