/**
 * 
 */
package eu.essi_lab.profiler.arpa.rest;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import javax.ws.rs.core.MediaType;

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;

/**
 * @author boldrini
 */
public class ARPARESTViewsHandler extends DefaultRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ARPARESTValidator validator = new ARPARESTValidator();

	return validator.validate(request);
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	String method = webRequest.getServletRequest().getMethod();

	// POST: Adding a new view
	if (method.equals("POST")) {
	    PostViewWorker worker = new PostViewWorker(webRequest);
	    return worker.post();
	}

	String viewId = null;

	String query = webRequest.getRequestPath();

	if (query.contains("views/")) {
	    viewId = query.substring(query.lastIndexOf("views/") + 6);
	}

	// DELETE: Removing a view
	if (method.equals("DELETE")) {

	    DeleteViewWorker worker = new DeleteViewWorker(webRequest);

	    return worker.delete(viewId);
	}

	if (!method.equals("GET")) {
	    return ViewWorker.errorMessage("Unexpected method: " + method);
	}

	// GET: Listing all the views

	GetViewWorker worker = new GetViewWorker(webRequest);

	return worker.get(viewId);

    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return new MediaType("application", "json");
    }
}
