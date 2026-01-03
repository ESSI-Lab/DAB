package eu.essi_lab.gssrv.servlet;

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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.pubsub.PubSubProfiler;

/**
 * @author Fabrizio
 */
public class PubSubService extends HttpServlet {

    private static final long serialVersionUID = -4044667892770599100L;

    private static final String PUB_SUB_SERVICE_DO_POST_ERROR = "PUB_SUB_SERVICE_DO_POST_ERROR";

    @Override
    protected void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException {
	doPost(httpRequest, httpResponse);
    }

    @Override
    protected void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException {

	GSException ex = null;

	PubSubProfiler profiler = new PubSubProfiler();

	WebRequest webRequest = new WebRequest();

	// webRequest.setServicesPath(AbstractProfilerService.SERVICES_PATH);

	String out = null;

	try {
	    webRequest.setServletRequest(httpRequest);

	    webRequest.setServletResponse(httpResponse);

	    Response response = profiler.handle(webRequest);

	    if (response.getEntity() != null) {
		out = response.getEntity().toString();
	    }
	} catch (GSException gsEx) {

	    ex = gsEx;

	} catch (Exception thr) {

	    ex = GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    PUB_SUB_SERVICE_DO_POST_ERROR, //
		    thr);
	}

	if (ex != null) {

	    ex.log();

	    out = ex.getErrorInfoList().get(0).toJSONObject().toString(3);
	}

	if (out != null) {

	    httpResponse.setContentType(MediaType.APPLICATION_JSON);

	    try {
		httpResponse.getWriter().write(out);

		httpResponse.getWriter().flush();

	    } catch (IOException e) {
		GSLoggerFactory.getLogger(PubSubService.class).error(e.getMessage(), e);
		throw new ServletException(e);
	    }
	}
    }
}
