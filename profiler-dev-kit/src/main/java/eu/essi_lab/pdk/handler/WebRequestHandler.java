package eu.essi_lab.pdk.handler;

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

import javax.ws.rs.core.Response;

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.selector.WebRequestFilter;

/**
 * This interface defines objects which are in charge to handle a {@link WebRequest}s
 * 
 * @see WebRequestFilter
 * @see DiscoveryHandler
 * @see DefaultRequestHandler
 * @see Profiler
 * @see Profiler#getSelector()
 * @author Fabrizio
 */
public interface WebRequestHandler  {

    /**
     * Handles the supplied <code>webRequest</code> and returns a {@link Response} suitable to be presented to the
     * client which triggered the request
     * 
     * @param webRequest a non <code>null</code> {@link WebRequest} to handle
     * @return a non <code>null</code> {@link Response} suitable for the client
     */
    public Response handle(WebRequest webRequest) throws GSException;

}
