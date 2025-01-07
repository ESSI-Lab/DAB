package eu.essi_lab.profiler.gwis.request.data;

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

import java.util.HashMap;

import eu.essi_lab.messages.web.WebRequest;

/**
 * @author boldrini
 */
public class GWISDataRequest {

    String prefix = "gwis/iv/";

    public enum Parameter {
	START_DT("startDT"), //
	END_DT("endDT"), //
	SITE_CODE("sites"), //
	PARAMETER_CODE("parmCd");

	private String[] keys;

	public String[] getKeys() {
	    return keys;
	}

	private Parameter(String... keys) {
	    this.keys = keys;
	}

    }

    private HashMap<Parameter, String> map = new HashMap<>();

    public String getParameterValue(Parameter parameter) {
	return map.get(parameter);
    }

    public GWISDataRequest(WebRequest request) {

	if (request.isGetRequest()) {

	    String path = request.getRequestPath();

	    if (!path.contains(prefix)) {
		throw new IllegalArgumentException("Prefix not found: " + prefix);
	    }

	    for (Parameter parameter : Parameter.values()) {
		String[] keys = parameter.getKeys();
		for (String key : keys) {
		    String value = request.getServletRequest().getParameter(key);
		    if (value != null && !value.equals("")) {
			map.put(parameter, value);
			break;
		    }
		}

	    }

	} else
	    throw new IllegalArgumentException("Supported HTTP requests: GET");

    }

}
