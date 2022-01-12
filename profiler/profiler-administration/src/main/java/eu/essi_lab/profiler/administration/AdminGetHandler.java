package eu.essi_lab.profiler.administration;

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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.essi_lab.configuration.GSConfigurationManager;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.WebRequestHandler;

public class AdminGetHandler implements WebRequestHandler {

    private static final String ERR_ID_CONFIGURATION_NOT_FOUND = "ERR_ID_CONFIGURATION_NOT_FOUND";

    public static final String CONFIGURATION_NOT_FOUND_USER_ERROR = "Configuration file was not found";
    public static final String CONFIGURATION_NOT_FOUND_CORRECTION_SUGGESTION = "You might need to initialize GI-suite first";
    private String resource;

    public AdminGetHandler(String requestedResource) {

	resource = requestedResource;
    }

    @Override
    public Response handle(WebRequest webRequest) throws GSException {

	try {

	    GSConfiguration configuration = new GSConfigurationManager().getConfiguration();

	    return Response.status(Status.OK).type(MediaType.APPLICATION_JSON_TYPE).entity(configuration.serialize()).build();

	} catch (GSException ex) {

	    ErrorInfo ei = new ErrorInfo();

	    ei.setContextId(this.getClass().getName());
	    ei.setErrorId(ERR_ID_CONFIGURATION_NOT_FOUND);

	    ei.setErrorType(ErrorInfo.ERRORTYPE_INTERNAL);

	    ei.setUserErrorDescription(CONFIGURATION_NOT_FOUND_USER_ERROR);
	    ei.setErrorCorrection(CONFIGURATION_NOT_FOUND_CORRECTION_SUGGESTION);

	    ex.addInfo(ei);
	    throw ex;
	}

    }

}
