package eu.essi_lab.profiler.geodcat;

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

import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.selector.WebRequestFilter;

/**
 * Accepts {@code geodcat/dataset/{id}} or {@code geodcat/dataset?identifier=...}.
 */
public class GeoDcatDatasetRequestFilter implements WebRequestFilter {

    @Override
    public boolean accept(WebRequest webRequest) throws GSException {

	if (!webRequest.isGetRequest()) {
	    return false;
	}

	List<String> segments = GeoDcatRequestPaths.normalizedSegments(webRequest);
	int dIdx = segments.lastIndexOf("dataset");
	if (dIdx > 0 && "geodcat".equals(segments.get(dIdx - 1))) {

	    if (dIdx + 1 < segments.size()) {
		return true;
	    }

	    KeyValueParser parser = new KeyValueParser(webRequest.getQueryString());
	    return parser.isValid("identifier");
	}

	return false;
    }
}
