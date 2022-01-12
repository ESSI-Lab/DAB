package eu.essi_lab.pdk.handler;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;

public interface WebRequestDelegatorHandler<M extends RequestMessage> extends WebRequestHandler  {
    public M handleWebRequest(WebRequest webRequest) throws GSException;

    /**
     * Handles the supplied <code>message</code> and returns a {@link Response} suitable to be presented to the
     * client which triggered the request
     * 
     * @param message request non <code>null</code> {@link RequestMessage} to handle
     * @return a non <code>null</code> {@link Response} suitable for the client
     */
    public Response handleMessageRequest(M message) throws GSException;
}
