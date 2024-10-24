package eu.essi_lab.profiler.gwis;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.WebRequestHandler;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.profiler.gwis.request.GWISRequest;
import eu.essi_lab.profiler.gwis.request.GWISRequestValidator;

public class GWISGetCodeHandler implements WebRequestHandler, WebRequestValidator {

    @Override
    public Response handle(WebRequest webRequest) throws GSException {
	GWISRequest request = new GWISRequest(webRequest);
	String code = request.getCode();
	MediaType type = null;
	if (code.endsWith(".js")) {
	    type = new MediaType("application", "javascript");
	} else if (code.endsWith(".css")) {
	    type = new MediaType("text", "css");
	} else if (code.endsWith(".gif")) {
	    type = new MediaType("image", "gif");
	} else {
	    return Response.status(Status.FORBIDDEN).build();
	}
	InputStream stream = GWISGetCodeHandler.class.getClassLoader().getResourceAsStream("gwis-code/" + code);

	return Response.status(Status.OK).type(type).entity(stream).build();

    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	GWISRequestValidator validator = new GWISRequestValidator();
	return validator.validate(request);
    }

}
