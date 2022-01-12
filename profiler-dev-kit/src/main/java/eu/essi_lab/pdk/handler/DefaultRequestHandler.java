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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.selector.HandlerSelector;
import eu.essi_lab.pdk.validation.WebRequestValidator;
public abstract class DefaultRequestHandler implements WebRequestHandler, WebRequestValidator {

    /**
     * Publishes the response returned by the {@link #getStringResponse(WebRequest)} method according to
     * the media type provided by the {@link #getMediaType()} method
     * 
     * @throws GSException see {@link #getStringResponse(WebRequest)}
     */
    @Override
    public Response handle(WebRequest webRequest) throws GSException {

	ResponseBuilder builder = Response.status(Status.OK);
	builder = builder.entity(getEntity(webRequest));
	builder = builder.type(getMediaType(webRequest));

	List<SimpleEntry<String, String>> customHeaders = getResponseHeaders(webRequest);
	for (SimpleEntry<String, String> customHeader : customHeaders) {
	    builder.header(customHeader.getKey(), customHeader.getValue());
	}
	return builder.build();
    }

    protected Object getEntity(WebRequest webRequest) throws GSException {
	return getStringResponse(webRequest);
    }

    /**
     * Returns the response
     * 
     * @param webRequest the {@link WebRequest} handled by the {@link #handle(WebRequest)} method
     * @return a non <code>null</code> string
     * @throws GSException if errors occurred during the response creation
     */
    public abstract String getStringResponse(WebRequest webRequest) throws GSException;
    
    /**
     * Returns the response
     * 
     * @param webRequest the {@link WebRequest} handled by the {@link #handle(WebRequest)} method
     * @return a non <code>null</code> string
     * @throws GSException if errors occurred during the response creation
     */
    @Deprecated
    public String getResponse(WebRequest webRequest) throws GSException{
	return getStringResponse(webRequest);
    }

    /**
     * Returns the {@link MediaType} of the response
     * 
     * @return a non <code>null</code> media type
     * @param webRequest the {@link WebRequest} handled by the {@link #handle(WebRequest)} method
     */
    public abstract MediaType getMediaType(WebRequest webRequest);

    /**
     * Returns an empty list of custom headers to be added to the response.<br>
     * Subclasses may overwrite this method to add custom headers
     * 
     * @param webRequest the {@link WebRequest} handled by the {@link #handle(WebRequest)} method
     */
    protected List<SimpleEntry<String, String>> getResponseHeaders(WebRequest webRequest) {

	return new ArrayList<>();
    }
}
