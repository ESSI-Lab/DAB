package eu.essi_lab.profiler.pubsub.handler.csw;

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

import java.io.InputStream;

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.csw.handler.srvinfo.CSWGetCapabilitiesHandler;

/**
 * @author Fabrizio
 */
public class CSWPubSubGetCapabilitiesHandler extends CSWGetCapabilitiesHandler {

    @Override
    public String getResponse(WebRequest webRequest) throws GSException {

	try {

	    String out = super.getStringResponse(webRequest);
	    
	    out = out.replace("cswpubsub","csw");

	    // replaces the BASE_OS_URL token with the right URL
	    out = out.replace("BASE_OS_URL", webRequest.getUriInfo().getAbsolutePath().toString().replace("/cswpubsub", ""));

	    return out;

	} catch (Exception e) {
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CSW_GET_CAPABILITIES_ERROR, e);
	}
    }

    protected InputStream getTemplate() {

	return getClass().getClassLoader().getResourceAsStream("templates/PubSubGetCapabilities.xml");
    }
}
