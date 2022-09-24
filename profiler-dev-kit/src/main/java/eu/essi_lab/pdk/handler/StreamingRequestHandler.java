package eu.essi_lab.pdk.handler;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import javax.ws.rs.core.StreamingOutput;

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author boldrini
 */
public abstract class StreamingRequestHandler extends DefaultRequestHandler {

    /**
     * 
     */
    private static final String STREAMING_REQUEST_HANDLER_METHOD_NOT_IMPLEMENTED_ERROR = "STREAMING_REQUEST_HANDLER_METHOD_NOT_IMPLEMENTED_ERROR";

    protected Object getEntity(WebRequest webRequest) throws GSException {
	return getStreamingResponse(webRequest);
    }

    /**
     * Returns the response as a stream
     * 
     * @param webRequest the {@link WebRequest} handled by the {@link #handle(WebRequest)} method
     * @return a non <code>null</code> string
     * @throws GSException if errors occurred during the response creation
     */
    public abstract StreamingOutput getStreamingResponse(WebRequest webRequest) throws GSException;

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	throw GSException.createException(//
		getClass(), //
		"Method not implemented", //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		STREAMING_REQUEST_HANDLER_METHOD_NOT_IMPLEMENTED_ERROR);
    }

}
