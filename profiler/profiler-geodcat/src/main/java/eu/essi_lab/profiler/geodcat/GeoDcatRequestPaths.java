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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Path helpers aligned with {@link eu.essi_lab.pdk.handler.selector.PathRequestFilter} (token/view stripping).
 */
public final class GeoDcatRequestPaths {

    private GeoDcatRequestPaths() {
    }

    public static List<String> normalizedSegments(WebRequest request) throws GSException {

	String requestPath = request.getRequestPath();
	if (requestPath == null) {
	    return List.of();
	}

	List<String> reqPaths = new ArrayList<>(Arrays.asList(requestPath.split("/")));

	for (int i = 0; i < reqPaths.size(); i++) {

	    String reqPath = reqPaths.get(i);
	    if (reqPath.equals("#")) {
		continue;
	    }

	    if (reqPath.equals("token") || reqPath.equals("view")) {

		reqPaths.set(i, "#");
		if (i + 1 < reqPaths.size()) {
		    reqPaths.set(i + 1, "#");
		}
	    }
	}

	return reqPaths.stream().filter(v -> !v.equals("#")).collect(Collectors.toList());
    }

    /**
     * Identifier from {@code .../geodcat/dataset/{id}} or query parameter {@code identifier}.
     */
    public static String extractDatasetIdentifier(WebRequest request) throws GSException {

	List<String> segments = normalizedSegments(request);
	int idx = segments.lastIndexOf("dataset");
	if (idx > 0 && "geodcat".equals(segments.get(idx - 1))) {

	    if (idx + 1 < segments.size()) {

		return URLDecoder.decode(segments.get(idx + 1), StandardCharsets.UTF_8);
	    }

	    KeyValueParser parser = new KeyValueParser(request.getQueryString());
	    if (parser.isValid("identifier")) {

		return parser.getDecodedValue("identifier");
	    }
	}

	return null;
    }
}
