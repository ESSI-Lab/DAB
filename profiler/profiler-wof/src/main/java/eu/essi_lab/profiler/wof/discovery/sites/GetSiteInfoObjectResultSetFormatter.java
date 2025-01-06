package eu.essi_lab.profiler.wof.discovery.sites;

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

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.profiler.wof.WOFRequest;

public class GetSiteInfoObjectResultSetFormatter extends SitesResultSetFormatter {
    public String getResponseName() {
	return "GetSiteInfoObjectResponse";
    }

    @Override
    protected WOFRequest getWOFRequest(WebRequest request) {
	return new GetSiteInfoObjectRequest(request);
    }

}
