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

package eu.essi_lab.profiler.sta.filter;

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.selector.WebRequestFilter;

/**
 * Filter for OGC STA service root (GET / or GET /v1.0).
 */
public class RootFilter implements WebRequestFilter {

    @Override
    public boolean accept(WebRequest request) throws GSException {
	String path = request.getRequestPath();
	if (path == null || path.isEmpty()) {
	    return true;
	}
	String normalized = path.replaceAll("/+", "/").replaceAll("^/|/$", "");
	if (normalized.isEmpty()) {
	    return true;
	}
	return normalized.equals("sta") || normalized.equals("v1.0") || normalized.endsWith("/v1.0");
    }
}
