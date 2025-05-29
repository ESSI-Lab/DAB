package eu.essi_lab.profiler.os.handler.srvinfo;

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

import java.util.Optional;

import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.selector.WebRequestFilter;
import eu.essi_lab.pdk.wrt.WebRequestParameter;
import eu.essi_lab.profiler.os.OSParameter;
import eu.essi_lab.profiler.os.OSParameters;
import eu.essi_lab.profiler.os.OSRequestParser;

/**
 * @author Fabrizio
 */
public class OSGetSourcesFilter implements WebRequestFilter {

    @Override
    public boolean accept(WebRequest request) throws GSException {

	return isGetSourcesQuery(request);
    }

    /**
     * @param request
     * @return
     */
    public static boolean isGetSourcesQuery(WebRequest request) {

	Optional<String> formData = request.getFormData();

	if (!formData.isPresent()) {

	    return false;
	}

	KeyValueParser keyValueParser = new KeyValueParser(formData.get());
	OSRequestParser parser = new OSRequestParser(keyValueParser);

	OSParameter sources = WebRequestParameter.findParameter(OSParameters.PARENTS.getName(), OSParameters.class);

	String sourcesValue = parser.parse(sources);
	OSParameter id = WebRequestParameter.findParameter(OSParameters.ID.getName(), OSParameters.class);

	String idValue = parser.parse(id);
	String queryString = request.getQueryString();

	return ((sourcesValue != null && sourcesValue.equals("ROOT")) || //
		(queryString != null && queryString.toLowerCase().contains("getcontent") && idValue.equals("ROOT")));

    }
}
