package eu.essi_lab.profiler.wps.executor.synch;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.util.Map;

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.selector.GETRequestFilter;

/**
 * @author boldrini
 */
public class GWPSExecuteRequestFilter extends GETRequestFilter {

    @Override
    public boolean accept(WebRequest request) throws GSException {

	if (request.isGetRequest()) {

	    Map<String, String[]> parameters = request.getServletRequest().getParameterMap();

	    boolean transformFound = false;
	    boolean synchronous = false;

	    for (String key : parameters.keySet()) {

		String[] values = parameters.get(key);

		if (key.toLowerCase().equals("identifier") && values.length > 0 && values[0].toLowerCase().equals("gi-axe-transform")) {
		    transformFound = true;
		}

		if (key.toLowerCase().equals("storeexecuteresponse") && values.length > 0 && values[0].toLowerCase().equals("false")) {
		    synchronous = true;
		}

	    }
	    return transformFound && synchronous;

	}

	return false;

    }
}
