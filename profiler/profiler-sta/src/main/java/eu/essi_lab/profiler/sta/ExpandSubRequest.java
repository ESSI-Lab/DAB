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

package eu.essi_lab.profiler.sta;

import java.io.IOException;

import eu.essi_lab.messages.web.WebRequest;

/**
 * WebRequest wrapper for $expand sub-requests. Overrides path and query string for
 * STA navigation requests (e.g. Locations(id)/Things?$top=100).
 */
public class ExpandSubRequest extends WebRequest {

    private final String pathOverride;
    private final String queryOverride;

    public ExpandSubRequest(WebRequest delegate, String pathOverride, String queryOverride) throws IOException {
	super(delegate.getServletRequest(), false);
	this.pathOverride = pathOverride;
	this.queryOverride = queryOverride;
	setQueryString(queryOverride);
    }

    @Override
    public String getRequestPath() {
	return pathOverride;
    }

    @Override
    public String getQueryString() {
	return queryOverride;
    }
}
